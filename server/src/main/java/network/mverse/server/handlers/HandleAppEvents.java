package network.mverse.server.handlers;

import java.time.Duration;
import java.time.LocalDateTime;

import com.github.underscore.Supplier;
import com.github.underscore.U;
import com.google.common.eventbus.Subscribe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import network.mverse.common.events.TeleportEvent;
import network.mverse.server.cluster.ClusterPlayer;

public class HandleAppEvents {
  private static final Logger LOGGER = LogManager.getLogger();

  @Subscribe
  public void handleTeleport(TeleportEvent e) {
    ClusterPlayer clusterPlayer = ClusterPlayer.players.get(e.serverPlayer);
    long timeSinceJoined = Duration.between(clusterPlayer.joined, LocalDateTime.now()).getSeconds();

    // If the player joined with the last few seconds just ignore this
    if (timeSinceJoined < 3) {
      return;
    }

    // They've already started teleporting so ignore
    if (ClusterPlayer.playersTeleporting.contains(clusterPlayer)) {
      return;
    }

    LOGGER.info("Got teleport event! {} {}", clusterPlayer, e.teleportalId);

    // Mark them as starting the telport process
    ClusterPlayer.playersTeleporting.add(clusterPlayer);

    // Wait 3 seconds in case a player e.g. teleports on top of another portal
    U.delay(new Supplier<Void>() {
      public Void get() {
        clusterPlayer.teleport(e.teleportalId);
        ClusterPlayer.playersTeleporting.remove(clusterPlayer);
        return null;
      }
    }, 3000);
  }
}
