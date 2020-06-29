package network.mverse.server.cluster.teleport;

import java.io.IOException;
import org.infinispan.protostream.MessageMarshaller;

public class TeleportalMarshaller implements MessageMarshaller<Teleportal> {

   @Override
   public String getTypeName() {
      return "network.mverse.server.Teleportal";
   }

   @Override
   public Class<? extends Teleportal> getJavaClass() {
      return Teleportal.class;
   }

   @Override
   public void writeTo(MessageMarshaller.ProtoStreamWriter writer, Teleportal teleportal) throws IOException {
      writer.writeDouble("posAX", teleportal.posAX);
      writer.writeDouble("posAY", teleportal.posAY);
      writer.writeDouble("posAZ", teleportal.posAZ);
      writer.writeString("hostA", teleportal.hostA);
      writer.writeInt("portA", teleportal.portA);
      writer.writeDouble("posBX", teleportal.posBX);
      writer.writeDouble("posBY", teleportal.posBY);
      writer.writeDouble("posBZ", teleportal.posBZ);
      writer.writeString("hostB", teleportal.hostB);
      writer.writeInt("portB", teleportal.portB);
   }

   @Override
   public Teleportal readFrom(MessageMarshaller.ProtoStreamReader reader) throws IOException {
      double posAX = reader.readDouble("posAX");
      double posAY = reader.readDouble("posAY");
      double posAZ = reader.readDouble("posAZ");
      String hostA = reader.readString("hostA");
      int portA = reader.readInt("portA");
      double posBX = reader.readDouble("posBX");
      double posBY = reader.readDouble("posBY");
      double posBZ = reader.readDouble("posBZ");
      String hostB = reader.readString("hostB");
      int portB = reader.readInt("portB");

      Teleportal teleportal = new Teleportal(posAX, posAY, posAZ, hostA, portA);
      teleportal.complete(posBX, posBY, posBZ, hostB, portB);

      return teleportal;
   }
}
