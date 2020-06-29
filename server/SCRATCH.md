
  @SubscribeEvent
  public void handleEntityEvent(EntityEvent event) {
    if (!(event.getEntity() instanceof ServerPlayerEntity)) {
      return;
    }

    if (event instanceof LivingUpdateEvent) {
      return;
    }

    if (event instanceof PlayerEvent.StartTracking) {
      return;
    }

    if (event instanceof PlayerEvent.StopTracking) {
      return;
    }

    if (event instanceof PlayerEvent.BreakSpeed) {
      return;
    }

    if (event instanceof PlayerContainerEvent.Close) {
      return;
    }

    if (event instanceof PlayerEvent.HarvestCheck) {
      return;
    }

    if (event instanceof PlayerInteractEvent.LeftClickBlock) {
      return;
    }

    if (event instanceof PlayerInteractEvent.RightClickBlock) {
      return;
    }

    if (event instanceof EntityEvent.EnteringChunk) {
      return;
    }

    if (event instanceof LivingAttackEvent) {
      return;
    }

    if (event instanceof LivingHurtEvent) {
      return;
    }

    if (event instanceof LivingDamageEvent) {
      return;
    }

    if (event instanceof LivingFallEvent) {
      return;
    }

    if (event instanceof LivingJumpEvent) {
      return;
    }

    if (event instanceof LootingLevelEvent) {
      return;
    }

    if (event instanceof LivingExperienceDropEvent) {
      return;
    }

    if (event instanceof PlaySoundAtEntityEvent) {
      return;
    }


    LOGGER.info("EntityEvent: {}", event.getClass().getName());
  }


  @SubscribeEvent
  public void handleBlockEvent(BlockEvent event) {

    LOGGER.info("BlockEvent: {}", event.getClass().getName());
  }
