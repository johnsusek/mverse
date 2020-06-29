package network.mverse.server.router;

import java.util.function.Supplier;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketConnectRemote {
	public String host;
	public int port;

	public static void encode(PacketConnectRemote msg, PacketBuffer buf) {
		buf.writeString(msg.host);
		buf.writeInt(msg.port);
	}

	public static PacketConnectRemote decode(PacketBuffer buf) {
		return new PacketConnectRemote();
	}

	public static class Handler {
		public static void handle(PacketConnectRemote message, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().setPacketHandled(true);
		}
	}
}
