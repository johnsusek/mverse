package network.mverse.server.handlers;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import network.mverse.common.Registrations;
import network.mverse.server.cluster.ClusterPlayer;

public class HandleGeneralEvents {
  private static final Logger LOGGER = LogManager.getLogger();

  // A player has logged in, so sync their data and reload them
  @SubscribeEvent
  public void handleLoggedIn(PlayerLoggedInEvent event) {
    if (!(event.getPlayer() instanceof ServerPlayerEntity)) {
      return;
    }

    ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
    ClusterPlayer clusterPlayer = new ClusterPlayer(player);

    clusterPlayer.loadFromCluster();
  }

  // A player event has happened that should mark the player for the next
  // sync tick, so do that here

  @SubscribeEvent
  public void handleRightClickBlock(RightClickBlock event) {
    if (!(event.getPlayer() instanceof ServerPlayerEntity)) {
      return;
    }

    ServerPlayerEntity serverPlayer = (ServerPlayerEntity) event.getEntity();
    World world = event.getWorld();
    BlockPos blockPos = event.getPos();
    ItemStack heldItemStack = event.getPlayer().getHeldItem(event.getHand());
    Item itemHeld = heldItemStack.getItem();
    TileEntity tileEntity = world.getTileEntity(blockPos);

    if (tileEntity == null) {
      return;
    }

    CompoundNBT tileEntityNBT = tileEntity.getTileData();

    if(!heldItemStack.hasTag()) {
      heldItemStack.setTag(new CompoundNBT());
    }

    CompoundNBT forkNBT = heldItemStack.getTag();

    if (itemHeld.equals(Registrations.tuningFork)) {
      LOGGER.info("Right clicked with tuning fork!");

      String teleportalId = UUID.randomUUID().toString();
      String teleportIdFromFork = forkNBT.getString("teleportalId");

      if (!teleportIdFromFork.equals("")) {
        LOGGER.info("Using existing id from fork");
        teleportalId = teleportIdFromFork;
      }
      else {
        // Tuning fork doesn't have an ID on it, put the generated UUID on there
        forkNBT.putString("teleportalId", teleportalId);
      }

      // Save teleportalId to the teleportal
      tileEntityNBT.putString("teleportalId", teleportalId);

      // Create the portal in the cluster
      ClusterPlayer clusterPlayer = ClusterPlayer.players.get(serverPlayer);
      clusterPlayer.createTeleportal(blockPos, teleportalId);

      if (!teleportIdFromFork.equals("")) {
        LOGGER.info("Fork is used up");
        heldItemStack.setCount(0);
      }
    }
    else if (itemHeld.equals(Items.STICK)) {
      LOGGER.info("tileEntityNBT: {}", tileEntityNBT);
    }
  }

}