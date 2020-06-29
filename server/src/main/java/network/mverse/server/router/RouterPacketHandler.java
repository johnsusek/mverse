package network.mverse.server.router;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class RouterPacketHandler {
  private static final String PROTOCOL_VERSION = "1";
  public static final SimpleChannel HANDLER = NetworkRegistry.newSimpleChannel(
      new ResourceLocation("mverse", "main"),
      () -> PROTOCOL_VERSION,
      PROTOCOL_VERSION::equals,
      PROTOCOL_VERSION::equals
  );

	public static void register()
	{
		int disc = 0;
		HANDLER.registerMessage(disc++, PacketConnectRemote.class, PacketConnectRemote::encode, PacketConnectRemote::decode, PacketConnectRemote.Handler::handle);
	}
}