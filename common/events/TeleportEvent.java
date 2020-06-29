package network.mverse.common.events;

import net.minecraft.entity.player.ServerPlayerEntity;

public class TeleportEvent {
  public String teleportalId;
  public ServerPlayerEntity serverPlayer;

  public TeleportEvent(String portalId, ServerPlayerEntity player) {
    teleportalId = portalId;
    serverPlayer = player;
  }
}
