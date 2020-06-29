package network.mverse.client;

import com.google.common.eventbus.EventBus;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import network.mverse.client.router.RouterPacketHandler;
import network.mverse.common.Registrations;

@Mod("mverse")
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class MVerseClient {
    public static EventBus eventBus = new EventBus();

    public MVerseClient() {
        // Make this a client-only mod
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST,
                () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::handleCommonSetup);

        MinecraftForge.EVENT_BUS.register(Registrations.class);

        // eventBus.register(new MVAppEvents());
        Registrations.eventBus = MVerseClient.eventBus;
    }

    @SubscribeEvent
    public void handleCommonSetup(final FMLClientSetupEvent event) {
        RouterPacketHandler.register();
    }
}
