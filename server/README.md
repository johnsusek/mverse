# MVerse - Server

MVerse is fully functional but experimental right now.
Don't just add this to your main minecraft server, at least without making a backup copy first.
It's suggested to set up a new server for MVerse right now,
while it is in alpha. For these instructions we'll assume your minecraft server is installed to ~/mverse but it can be anywhere.

## 1) Install Minecraft Forge

If you already have a recent forge-enabled server installed you can skip these steps.

```
mkdir ~/mverse
cd ~/mverse
export FORGE_VERSION=1.15.2-31.2.27
wget http://files.minecraftforge.net/maven/net/minecraftforge/forge/$FORGE_VERSION/forge-$FORGE_VERSION-launcher.jar
wget http://files.minecraftforge.net/maven/net/minecraftforge/forge/$FORGE_VERSION/forge-$FORGE_VERSION-installer.jar
java -jar forge-$FORGE_VERSION-installer.jar --installServer
java -cp "forge-$FORGE_VERSION-launcher.jar" "net.minecraftforge.server.ServerMain" nogui
```

- Accept EULA by editing eula.txt

- Try to launch again, it should be running a plain forge server now and prepare the spawn area.
  - You may see a message "Unable to find spawn biome" and nothing else, give it a few minutes while things generate.

- After spawn is prepared, close down server (Ctrl-C) and continue on to installing MVerse

## 2) Install MVerse

### Download mod jar to server mods folder
```
cd ~/mverse/mods
export MVERSE_VERSION=0.2.1
wget https://github.com/johnsusek/mverse/releases/download/$MVERSE_VERSION/mverse-server-$MVERSE_VERSION.jar
```

## 3) Set Up Clustering

Note, clustering is only recommended for dedicated servers that are up 24/7.

### ☞ I want to join a cluster

* Find a cluster looking for a new server, the admin will give you the address and password to use.
  * Email john@johnsolo.net or discord johnsolo on the minecraft forge discord if you want to join the test mverse cluster. US-based dedicated servers only please.
* When you launch, change `mverse.example.com` to the cluster address and `examplepass` to the cluster password.
* You're done. Skip to the firewalls section.

### ☞ I want to start a cluster

It's a little more work but not too difficult. You have to have experience adding DNS records to a domain.

_Coming Soon: A simpler way to do this without DNS if your servers are all on the same private network. Stay tuned._

#### Discovery

* Servers in the cluster use DNS to discover their peers.

* Add multiple DNS A records to a domain you control, one for each server in your cluster.

* Let's say you own the domain `example.com`. If you want to set up a cluster at `mverse.example.com` with three servers, you would create three DNS A records. Each of these three records would have the same name ("mverse"), but a different address. When someone wants to join, add a new DNS A record pointing to their server.

* Use the command `host mverse.example.com` to verify the list of servers in your cluster. It may take a few minutes for DNS to propagate.

#### Security

* We can't let just anyone join the cluster though, so set the password in `-Dmverse.cluster_password=examplepass` to something unique when you launch for the first time. Only share this password with people you want to join the cluster. I suggest using something [like this page](https://www.lastpass.com/password-generator) to generate a strong cluster_password.

* As your server will be the first one in the cluster, it will have a special role as the "coordinator". The coordinator verifies the cluster_password of nodes trying to join. For this reason the most stable server should be the one to start the cluster.

## 4) Open Firewall

Your server should always be behind a firewall of some kind, so you'll need to open up port 7800 to allow it to communicate with the cluster. If you are using ufw, a common linux software firewall, the command is `sudo ufw allow 7800`. For hosted/cloud providers you would configure the firewall in their control panel to allow incoming TCP traffic on port 7800.

## 5) Launch

- `cd ~/mverse`

- `export FORGE_VERSION=1.15.2-31.2.27`

- `export MVERSE_VERSION=0.2.1`

💡In the following command, you will have to change `mverse.example.com` and `examplepass` to match the cluster you are joining/starting.

- `java -cp "./mods/mverse-server-$MVERSE_VERSION.jar:forge-$FORGE_VERSION-launcher.jar" -Djgroups.dns.query=mverse.example.com -Dmverse.cluster_password=examplepass "net.minecraftforge.server.ServerMain" nogui`

💡At this point you can also add any additional options to this java command as needed to customize for your environment, things like `-Xmx1024M -Xms1024M`, etc.

You should see some output in the logs now related to the cluster and Infinispan.

> If two machines can't find each other and start a cluster, double-check the firewall and that DNS records are in place. You should be able to telnet to each machine on port 7800, and `host yourcluster.example.com` should return records for each server.

Once servers indicate they are joined into a cluster (you will see this in the logs) there's nothing more for you to do. Data will be saved to a folder called `cluster`, don't modify or remove that.

## Wrapping Up

Please file a github issue if you have a question or constructive feedback. I welcome any PRs with enhancements or bug fixes, even documentation updates to the README.
