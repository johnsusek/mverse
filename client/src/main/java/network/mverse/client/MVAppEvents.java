package network.mverse.client;

import com.google.common.eventbus.Subscribe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import network.mverse.common.events.TeleportEvent;

public class MVAppEvents {
  private static final Logger LOGGER = LogManager.getLogger();

  // @Subscribe
  // public void handleTeleport(TeleportEvent e) {
  //   LOGGER.info("Got teleport event! {}", e);
  // }
}
