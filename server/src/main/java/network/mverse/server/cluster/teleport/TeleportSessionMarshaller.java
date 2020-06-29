package network.mverse.server.cluster.teleport;

import java.io.IOException;
import org.infinispan.protostream.MessageMarshaller;

public class TeleportSessionMarshaller implements MessageMarshaller<TeleportSession> {

   @Override
   public String getTypeName() {
      return "network.mverse.server.TeleportSession";
   }

   @Override
   public Class<? extends TeleportSession> getJavaClass() {
      return TeleportSession.class;
   }

   @Override
   public void writeTo(MessageMarshaller.ProtoStreamWriter writer, TeleportSession session) throws IOException {
      writer.writeString("teleportalId", session.teleportalId);
      writer.writeFloat("yaw", session.yaw);
      writer.writeFloat("pitch", session.pitch);
   }

   @Override
   public TeleportSession readFrom(MessageMarshaller.ProtoStreamReader reader) throws IOException {
      String teleportalId = reader.readString("teleportalId");
      float yaw = reader.readFloat("yaw");
      float pitch = reader.readFloat("pitch");

      TeleportSession session = new TeleportSession(teleportalId, yaw, pitch);

      return session;
   }
}
