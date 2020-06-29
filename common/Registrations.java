package network.mverse.common;

import com.google.common.eventbus.EventBus;
import com.mojang.datafixers.DSL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Registrations {
    private static final Logger LOGGER = LogManager.getLogger();
    public static Block teleportalBlock;
    public static BlockItem blockItem;
    public static Item tuningFork;
    public static EventBus eventBus;

    @SubscribeEvent
    public static void onClientSetupEvent(FMLClientSetupEvent event) {
        RenderTypeLookup.setRenderLayer(teleportalBlock, RenderType.getSolid());
    }

    @SubscribeEvent
    public static void onRegisterBlock(RegistryEvent.Register<Block> event) {
        LOGGER.info("Registering teleportal block ...");
        teleportalBlock = new TeleportalBlock(eventBus);
        teleportalBlock.setRegistryName("mverse", "teleportal");
        event.getRegistry().register(teleportalBlock);
    }

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        LOGGER.info("Registering teleportal item ...");
        tuningFork = new TuningFork();
        tuningFork.setRegistryName("mverse", "tuning_fork");
        event.getRegistry().register(tuningFork);

        LOGGER.info("Registering teleportal blockItem ...");
        blockItem = new BlockItem(teleportalBlock, new Item.Properties());
        blockItem.setRegistryName("mverse", "teleportal");
        event.getRegistry().register(blockItem);
    }

    @SubscribeEvent
    public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        TileEntityType<?> tileEntity = TileEntityType.Builder.create(TeleportalTileEntity::new, TeleportalBlock.BLOCK)
                .build(DSL.remainderType());
        tileEntity.setRegistryName("mverse", "teleportal");
        event.getRegistry().register(tileEntity);
    }
}