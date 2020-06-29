package network.mverse.client.router;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.NetworkManager;
import net.minecraft.client.network.login.ClientLoginNetHandler;
import net.minecraft.network.handshake.client.CHandshakePacket;
import net.minecraft.network.login.client.CLoginStartPacket;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.network.ProtocolType;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class PacketConnectRemote {
	private static final Logger LOGGER = LogManager.getLogger();
	private static ExecutorService executor = Executors.newFixedThreadPool(2);
	public String host;
	public int port;

	public static void encode(PacketConnectRemote msg, PacketBuffer buf) {
	}

	public static PacketConnectRemote decode(PacketBuffer buf) {
		PacketConnectRemote packetConnectRemote = new PacketConnectRemote();
		packetConnectRemote.host = buf.readString();
		packetConnectRemote.port = buf.readInt();
		return packetConnectRemote;
	}

	public static class Handler {
		public static void handle(PacketConnectRemote message, Supplier<NetworkEvent.Context> ctx) {
			String host = message.host;
			int port = message.port;

			ctx.get().enqueueWork(() -> {
				NetworkManager networkManager = ctx.get().getNetworkManager();
				LOGGER.debug("Attempting to connect to {}:{}", host, port);
				attemptConnect(networkManager, host, port);
			});

			ctx.get().setPacketHandled(true);
		}

		private static void attemptConnect(NetworkManager networkManager, String host, int port) {
			Minecraft minecraft = Minecraft.getInstance();

			executor.execute(() -> {
				LOGGER.info("Checking if server is up: {}:{}", host, port);
				Boolean serverUp = isServerResponding(host, port);
				LOGGER.info("Check completed: {}", serverUp);

				if (!serverUp) {
					StringTextComponent closeMessage = new StringTextComponent("Could not connect to " + host);
					closeMessage.applyTextStyle(TextFormatting.RED);
					minecraft.player.sendMessage(closeMessage);
					return;
				}

				minecraft.enqueue(() -> {
					StringTextComponent closeMessage = new StringTextComponent("Connecting to " + host);
					closeMessage.applyTextStyle(TextFormatting.YELLOW);
					closeMessage.applyTextStyle(TextFormatting.BOLD);
					networkManager.closeChannel(closeMessage);

					try {
						connect(host, port);
					} catch (UnknownHostException e) {
						throw new RuntimeException(e);
					}
				});
			});
		}

		private static Boolean isServerResponding(String host, int port) {
			Socket s = null;
			try {
				s = new Socket(host, port);
				return true;
			} catch (Exception e) {
				return false;
			} finally {
				if (s != null)
					try {
						s.close();
					} catch (Exception e) {
					}
			}
		}

		private static void connect(String host, int port) throws UnknownHostException {
			Minecraft minecraft = Minecraft.getInstance();
			boolean isUsingNativeTransport = minecraft.gameSettings.isUsingNativeTransport();

			Screen previousGuiScreen = null;
			InetAddress inetaddress = InetAddress.getByName(host);

			NetworkManager networkManager = NetworkManager.createNetworkManagerAndConnect(inetaddress, port,
							isUsingNativeTransport);
			ClientLoginNetHandler handler = new ClientLoginNetHandler(networkManager, minecraft, previousGuiScreen,
							(a) -> {});

			networkManager.setNetHandler(handler);
			networkManager.sendPacket(new CHandshakePacket(inetaddress.toString(), port, ProtocolType.LOGIN));
			networkManager.sendPacket(new CLoginStartPacket(minecraft.getSession().getProfile()));
		}
	}
}
