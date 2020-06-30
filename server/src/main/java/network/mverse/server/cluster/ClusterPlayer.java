package network.mverse.server.cluster;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.PacketDistributor;
import network.mverse.server.cluster.teleport.TeleportSession;
import network.mverse.server.cluster.teleport.Teleportal;
import network.mverse.server.router.PacketConnectRemote;
import network.mverse.server.router.RouterPacketHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.infinispan.context.Flag;
import com.github.underscore.Supplier;
import com.github.underscore.U;

public class ClusterPlayer {
  private static final Logger LOGGER = LogManager.getLogger();

  public static HashMap<ServerPlayerEntity, ClusterPlayer> players = new HashMap<ServerPlayerEntity, ClusterPlayer>();
  public static Set<ClusterPlayer> playersTeleporting = new HashSet<>();
  public static Set<ClusterPlayer> playersToSync = new HashSet<>();
  public static Set<ClusterPlayer> playersLoading = new HashSet<>();

  private ExecutorService executor = Executors.newSingleThreadExecutor();

  private ServerPlayerEntity player;
  public LocalDateTime joined;

  public ClusterPlayer(ServerPlayerEntity serverPlayer) {
    player = serverPlayer;
    joined = LocalDateTime.now();

    logger("Player joining... {}", player);
  }

  // Every 500ms, sync to the cluster any players which have changed
  public static void startSyncingPlayersToCluster() {
    Supplier<Void> incr = new Supplier<Void>() {
      public Void get() {
        if (playersToSync.isEmpty()) {
          return null;
        }

        for (ClusterPlayer playerToSync : playersToSync) {
          playerToSync.saveToCluster();
        }

        playersToSync.clear();

        return null;
      }
    };

    U.setInterval(incr, 500);
  }

  public void loadFromCluster() {
    UUID playerId = PlayerEntity.getUUID(player.getGameProfile());
    byte[] playerBytes = ClusterManager.players.get(playerId.toString());

    ClusterPlayer.players.put(player, this);

    // Keep track of players loading in this set, so we can ignore
    // events from them while they load in
    ClusterPlayer.playersLoading.add(this);

    try {

      if (playerBytes != null) {
        logger("Loading existing player from cluster");

        CompoundNBT clusterPlayerNBT = CompressedStreamTools.readCompressed(new ByteArrayInputStream(playerBytes));

        // Now that we've loaded the NBT from the cluster, reload the player
        // with the cluster data + adjustments
        reload(clusterPlayerNBT);
      }
      else {
        logger("Player from cluster was null, a new player has joined!");
      }
    } catch (Exception e) {
      LOGGER.error("There was an error syncing the MVerse player from the cluster, please see below for more details. {}", player.getUniqueID());
      e.printStackTrace();

      // Something went wrong, with writing the player to disk or something
      // else in syncAndReload. We don't want to let the player in if there is some problem
      // like this, so we disconnect them. The above stack trace should explain more to
      // the server operator what the issue is.
      player.disconnect();
    } finally {
      logger("Removing player from playersLoading...");
      ClusterPlayer.playersLoading.remove(this);
      this.saveToCluster();
    }
  }

  public void saveToCluster() {
    CompoundNBT playerToSave = player.serializeNBT();
    UUID playerId = PlayerEntity.getUUID(player.getGameProfile());

    Callable<Void> saveTask = () -> {
      ByteArrayOutputStream playerBytes = new ByteArrayOutputStream();
      CompressedStreamTools.writeCompressed(playerToSave, playerBytes);

      ClusterManager.players.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES)
        .put(playerId.toString(), playerBytes.toByteArray());

      logger("Put player data into cluster: {}", player);

      return null;
    };

    try {
      executor.submit(saveTask).get(1000, TimeUnit.MILLISECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      LOGGER.error("Error saving player to cluster!");
      e.printStackTrace();
    }
  }

  public void createTeleportal(BlockPos blockPos, String teleportalId) {
    MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);

    String hostname = System.getProperty("mverse.hostname");

    if (hostname == null) {
      LOGGER.error("Could not create portal because mverse.hostname is not set");
      return;
    }

    Teleportal teleportal = ClusterManager.teleportals.get(teleportalId);

    // Check cluster to see if this exists already, if so we are placing the other side
    if (teleportal == null) {
      // We are creating a new one
      teleportal = new Teleportal(blockPos.getX(), blockPos.getY(), blockPos.getZ(), hostname, server.getServerPort());
    }
    else {
      if (teleportal.hostB.equals("") && teleportal.portB == 0) {
        // We are finishing a one sided existing portal
        teleportal.complete(blockPos.getX(), blockPos.getY(), blockPos.getZ(), hostname, server.getServerPort());
      }
      else {
        LOGGER.warn("Trying to complete an already completed portal with ID {}", teleportalId);
      }
    }

    ClusterManager.teleportals.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES)
      .put(teleportalId, teleportal);

    // The portal was created/completed
    LOGGER.info("Saved teleportal to cluster: {} {}", teleportalId, teleportal);
  }

  public void teleport(String teleportalId) {
    UUID playerId = PlayerEntity.getUUID(player.getGameProfile());
    Teleportal teleportal = ClusterManager.teleportals.get(teleportalId);

    LOGGER.info("Got teleportal from cluster: \n{}", teleportal);

    TeleportSession teleportSession = new TeleportSession(teleportalId, player.rotationYaw, player.rotationPitch);

    // Create TeleportSession
    ClusterManager.teleportSessions
      .getAdvancedCache()
      .withFlags(Flag.IGNORE_RETURN_VALUES)
      .put(playerId.toString(), teleportSession);

    LOGGER.info("Created teleport session for player {}", teleportSession);

    String hostname = System.getProperty("mverse.hostname");

    String destHost;
    int destPort;

    // Find the side that's not us and send them there
    if (teleportal.hostA.equals(hostname)) {
      destHost = teleportal.hostB;
      destPort = teleportal.portB;
    }
    else {
      destHost = teleportal.hostA;
      destPort = teleportal.portA;
    }

    // As a final step we save them to the cluster, just in case they did something within the last tick
    saveToCluster();

    // At this point they will join the remote server, which will check the
    // TeleportSession we just created for where to put them.
    sendToRemote(destHost, destPort);
  }

  public void sendToRemote(String host, int port) {
    try {
      PacketConnectRemote packetConnect = new PacketConnectRemote();
      packetConnect.host = host;
      packetConnect.port = port;
      RouterPacketHandler.HANDLER.send(PacketDistributor.PLAYER.with(() -> player), packetConnect);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void reload(CompoundNBT clusterPlayerNBT) {
    // At this point, the `player` variable has values from the data file on disk,
    // which has things we need like last position and spawn point. So let's use
    // those to calculate where the user and their spawn should end up

    Hashtable<String, Double> finalPosition = calculateFinalPosition();

    // Copy their original spawn, i.e. where their spawn point was last on this server,
    // not the spawn coords from the cluster data
    CompoundNBT playerNBT = player.serializeNBT();
    int spawnX = playerNBT.getInt("SpawnX");
    int spawnY = playerNBT.getInt("SpawnY");
    int spawnZ = playerNBT.getInt("SpawnZ");
    int dimension = playerNBT.getInt("Dimension");

    // If player has joined immediately after death on another server
    // we have to reset their health to not cause them to get into a death loop
    if (clusterPlayerNBT.getShort("DeathTime") > 0) {
      clusterPlayerNBT.putShort("DeathTime", (short)0);
    }

    if (clusterPlayerNBT.getFloat("Health") < 0.01) {
      clusterPlayerNBT.putFloat("Health", 100.0f);
    }

    // Apply NBT from cluster to the player
    logger("deserializeNBT...");
    player.deserializeNBT(clusterPlayerNBT);

    // Now the `player` variable has values from the cluster

    // Some values we don't want carried over from the cluster though, like position
    // and spawn. So set those values back from the original data.

    // Set server specific spawn point
    setOriginalSpawn(spawnX, spawnY, spawnZ, dimension);

    // Now move them to where they should be
    setOriginalPosition(
      finalPosition.get("x"),
      finalPosition.get("y"),
      finalPosition.get("z"),
      finalPosition.get("yaw").floatValue(),
      finalPosition.get("pitch").floatValue());
  }

  private void setOriginalSpawn(int x, int y, int z, int dimension) {
    CompoundNBT playerNBT = player.serializeNBT();

    logger("Moving spawn from {} {} {}", playerNBT.getInt("SpawnX"), playerNBT.getInt("SpawnY"), playerNBT.getInt("SpawnZ"));
    logger("Moving dimension from {}", playerNBT.getInt("Dimension"));

    playerNBT.putInt("SpawnX", x);
    playerNBT.putInt("SpawnY", y);
    playerNBT.putInt("SpawnZ", z);
    playerNBT.putInt("Dimension", dimension);
    player.deserializeNBT(playerNBT);

    CompoundNBT playerNBTNow = player.serializeNBT();
    logger("Spawn is now {} {} {}", playerNBTNow.getInt("SpawnX"), playerNBTNow.getInt("SpawnY"), playerNBTNow.getInt("SpawnZ"));
    logger("Dimension is now {}", playerNBTNow.getInt("Dimension"));
  }

  private void setOriginalPosition(double x, double y, double z, float yaw, float pitch) {
    logger("Moving player from {} {} {} {} {}", player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), player.rotationYaw, player.rotationPitch);
    logger("Moving player to {} {} {} {} {}", x, y, z, yaw, pitch);
    player.rotationYaw = yaw;
    player.rotationPitch = pitch;
    player.setPositionAndUpdate(x, y, z);
  }

  private Hashtable<String, Double> calculateFinalPosition() {
    // Copy their original positions, i.e. where they last were on this server
    // not the coords from the cluster data
    double finalX = player.getPosX();
    double finalY = player.getPosY();
    double finalZ = player.getPosZ();
    double finalYaw = player.rotationYaw;
    double finalPitch = player.rotationPitch;

    // Check if the player is teleporting in, and send them to those coords instead saved ones
    UUID playerId = PlayerEntity.getUUID(player.getGameProfile());
    TeleportSession session = ClusterManager.teleportSessions.get(playerId.toString());

    if (session != null) {
      logger("Found teleport session for this player: {}", session);

      Teleportal teleportal = ClusterManager.teleportals.get(session.teleportalId);
      String hostname = System.getProperty("mverse.hostname");

      if (teleportal.hostA.equals(hostname)) {
        finalX = teleportal.posAX;
        finalY = teleportal.posAY;
        finalZ = teleportal.posAZ;
      }
      else if (teleportal.hostB.equals(hostname)) {
        finalX = teleportal.posBX;
        finalY = teleportal.posBY;
        finalZ = teleportal.posBZ;
      }

      finalYaw = session.yaw;
      finalPitch = session.pitch;

      ClusterManager.teleportSessions.remove(playerId.toString());
    }
    else {
      logger("Player is not teleporting in");
    }

    Hashtable<String, Double> position = new Hashtable<String, Double>();
    position.put("x", finalX);
    position.put("y", finalY);
    position.put("z", finalZ);
    position.put("yaw", finalYaw);
    position.put("pitch", finalPitch);

    return position;
  }

  public void logger(String message, Object... params) {
    LOGGER.info("["+player.getDisplayName().getString()+"] " + message, params);
  }
}