package network.mverse.server.cluster.teleport;

public class Teleportal {
  public double posAX;
  public double posAY;
  public double posAZ;
  public String hostA;
  public int portA;

  public double posBX;
  public double posBY;
  public double posBZ;
  public String hostB;
  public int portB;

  public Teleportal(double x, double y, double z, String host, int port) {
    posAX = x;
    posAY = y;
    posAZ = z;
    hostA = host;
    portA = port;

    posBX = -1;
    posBY = -1;
    posBZ = -1;
    hostB = "";
    portB = 0;
  }

  public void complete(double x, double y, double z, String host, int port) {
    posBX = x;
    posBY = y;
    posBZ = z;
    hostB = host;
    portB = port;
  }

  public String toString() {
    return "\nSide A) x: " + posAX + " y: " + posAY + " z: " + posAZ + " " + hostA + ":" + portA +
           "\nSide B) x: " + posBX + " y: " + posBY + " z: " + posBZ + " " + hostB + ":" + portB;
  }
}