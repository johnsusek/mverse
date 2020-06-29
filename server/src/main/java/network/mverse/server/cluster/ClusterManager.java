package network.mverse.server.cluster;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.StorageType;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.partitionhandling.PartitionHandling;

import network.mverse.server.cluster.teleport.TeleportSession;
import network.mverse.server.cluster.teleport.TeleportSessionSerializationInit;
import network.mverse.server.cluster.teleport.Teleportal;
import network.mverse.server.cluster.teleport.TeleportalSerializationInit;

public class ClusterManager {
  public static Cache<String, byte[]> players;
  public static Cache<String, Teleportal> teleportals;
  public static Cache<String, TeleportSession> teleportSessions;
  public static EmbeddedCacheManager cacheManager;

  public ClusterManager() {
    GlobalConfigurationBuilder global = GlobalConfigurationBuilder.defaultClusteredBuilder();

    global.globalState().enable().persistentLocation("mverse", "cluster")
        .transport().clusterName("MVerse")
        .addProperty("configurationFile", "mverse-cluster.xml")
        .serialization()
          .addContextInitializers(new TeleportalSerializationInit(), new TeleportSessionSerializationInit());

    cacheManager = new DefaultCacheManager(global.build());
  }

  public void initStores() {
    players = getPlayerStore();
    teleportals = getTeleportalStore();
    teleportSessions = getTeleportSessionStore();
  }

  public ConfigurationBuilder defaultConfig() {
    ConfigurationBuilder config = new ConfigurationBuilder();

    config.persistence()
        .passivation(false)
        .addSingleFileStore()
            .preload(true)
            .shared(false)
            .fetchPersistentState(true)
            .ignoreModifications(false)
            .purgeOnStartup(false)
            .async()
                .enabled(true)
        .memory()
            .storage(StorageType.HEAP)
        .clustering()
            .cacheMode(CacheMode.DIST_SYNC)
            .partitionHandling()
            .whenSplit(PartitionHandling.ALLOW_READ_WRITES)
            .encoding()
            .mediaType("application/x-protostream");

    return config;
  }

  public Cache<String, TeleportSession> getTeleportSessionStore() {
    cacheManager.defineConfiguration("teleportSession", defaultConfig().expiration().lifespan(1000 * 60).build());
    return cacheManager.getCache("teleportSession");
  }

  public Cache<String, Teleportal> getTeleportalStore() {
    cacheManager.defineConfiguration("teleportals", defaultConfig().build());
    return cacheManager.getCache("teleportals");
  }

  public Cache<String, byte[]> getPlayerStore() {
    cacheManager.defineConfiguration("players", defaultConfig().build());
    return cacheManager.getCache("players");
  }
}
