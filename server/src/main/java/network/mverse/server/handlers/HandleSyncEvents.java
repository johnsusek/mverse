package network.mverse.server.handlers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.SaveToFile;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import network.mverse.server.cluster.ClusterPlayer;

public class HandleSyncEvents {
  private void markDirty(PlayerEntity player) {
    if (!(player instanceof ServerPlayerEntity)) {
      return;
    }

    ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
    ClusterPlayer clusterPlayer = ClusterPlayer.players.get(serverPlayer);

    if (ClusterPlayer.playersLoading.contains(clusterPlayer)) {
      return;
    }

    ClusterPlayer.playersToSync.add(clusterPlayer);
  }

  // An entity event has happened that should mark the player for the next
  // sync tick, so do that here
  private void markDirty(Entity entity) {
    if (!(entity instanceof ServerPlayerEntity)) {
      return;
    }

    ServerPlayerEntity serverPlayer = (ServerPlayerEntity) entity;
    ClusterPlayer clusterPlayer = ClusterPlayer.players.get(serverPlayer);

    if (ClusterPlayer.playersLoading.contains(clusterPlayer)) {
      return;
    }

    ClusterPlayer.playersToSync.add(clusterPlayer);
  }

  @SubscribeEvent
  public void handleSave(SaveToFile event) {
    markDirty(event.getPlayer());
  }

  @SubscribeEvent
  public void handleContainerClose(PlayerContainerEvent.Close event) {
    markDirty(event.getPlayer());
  }

  @SubscribeEvent
  public void handleLivingEquipmentChange(LivingEquipmentChangeEvent event) {
    markDirty(event.getEntity());
  }

  @SubscribeEvent
  public void handleUseItem(LivingEntityUseItemEvent event) {
    markDirty(event.getEntity());
  }

  @SubscribeEvent
  public void handleDeath(LivingDeathEvent event) {
    markDirty(event.getEntity());
  }

  @SubscribeEvent
  public void handleDrops(LivingDropsEvent event) {
    markDirty(event.getEntity());
  }

  @SubscribeEvent
  public void destroyItem(PlayerDestroyItemEvent event) {
    markDirty(event.getPlayer());
  }

  @SubscribeEvent
  public void handleCrafted(ItemCraftedEvent event) {
    markDirty(event.getPlayer());
  }

  @SubscribeEvent
  public void pickupItem(ItemPickupEvent event) {
    markDirty(event.getPlayer());
  }

  @SubscribeEvent
  public void handleLeave(PlayerLoggedOutEvent event) {
    markDirty(event.getPlayer());
  }
}