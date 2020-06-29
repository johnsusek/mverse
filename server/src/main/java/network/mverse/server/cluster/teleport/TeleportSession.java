package network.mverse.server.cluster.teleport;

public class TeleportSession {
  public String teleportalId;
  public float yaw;
  public float pitch;

  public TeleportSession(String id, float y, float p) {
    teleportalId = id;
    yaw = y;
    pitch = p;
  }

  public String toString() {
    return "teleportalId: " + teleportalId + " yaw: " + yaw + " pitch: " + pitch;
  }
}