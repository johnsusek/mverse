package network.mverse.server;

import com.google.common.eventbus.EventBus;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import network.mverse.common.Registrations;
import network.mverse.server.cluster.ClusterManager;
import network.mverse.server.cluster.ClusterPlayer;
import network.mverse.server.handlers.HandleAppEvents;
import network.mverse.server.handlers.HandleGeneralEvents;
import network.mverse.server.handlers.HandleSyncEvents;
import network.mverse.server.router.RouterPacketHandler;

@Mod("mverse-server")
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MVServer {
    private static final Logger LOGGER = LogManager.getLogger();
    public static EventBus eventBus = new EventBus();

    public MVServer() {
        // Make this a server-only mod
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST,
                () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::handleCommonSetup);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(Registrations.class);

        Registrations.eventBus = MVServer.eventBus;

        ClusterManager clusterManager = new ClusterManager();
        clusterManager.initStores();
    }

    @SubscribeEvent
    public void handleCommonSetup(final FMLCommonSetupEvent event) {
        RouterPacketHandler.register();
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) throws Exception {
        if (ClusterManager.players == null) {
            LOGGER.error("Player cache was null. Could not connect to cluster? Shutting down!");
            MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
            server.initiateShutdown(true);
        } else {
            eventBus.register(new HandleAppEvents());
            MinecraftForge.EVENT_BUS.register(new HandleGeneralEvents());
            MinecraftForge.EVENT_BUS.register(new HandleSyncEvents());

            ClusterPlayer.startSyncingPlayersToCluster();
        }
    }

    @SubscribeEvent
    public void onServerStopping(FMLServerStoppingEvent event) {
        LOGGER.info("Stopping cluster manager...");
        ClusterManager.cacheManager.stop();
        LOGGER.info("Stopped cluster manager...");
    }
}
