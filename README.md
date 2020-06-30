# MVerse

<img align="right" width="280" height="auto" src="https://user-images.githubusercontent.com/611996/85967969-4fe09d80-b989-11ea-9100-3976bf05ab8a.png">

A [Minecraft Forge](https://github.com/MinecraftForge) mod to easily cluster Minecraft servers into a multiverse.

Once installed on your servers, all player data is synchronized and the cluster becomes the "source of truth" for player data.

For players, this gives the effect of one seamless multiverse where your character follows you across servers.

## Features

- Seamlessly sync player data across servers, creating an "MVerse". Play as normal, but your character follows you across servers!
- Can work with other forge mods as long as they are installed on all servers.
- Works at a low level so all custom metadata on the player is synchronized
- Uses powerful [Infinispan](https://infinispan.org/features/) clustering technology.
- Doesn't require a centralized proxy server; servers talk to each other in a p2p fashion and discover each other through DNS
- Doesn't require `online-mode=false`

## Installing

### Client

* Install a [compatible forge client](http://files.minecraftforge.net/maven/net/minecraftforge/forge/1.15.2-31.2.27/forge-1.15.2-31.2.27-installer.jar)
* [Download the latest client .jar](https://github.com/johnsusek/mverse/releases/download/0.2.2/mverse-client-0.2.2.jar)
* Place into mods folder of a forge-enabled minecraft installation
* Connect to a server with MVerse installed.
  * Right now use *`clio.mverse.network`* or *`thalia.mverse.network`*
* Do something on one server, then connect to the other.
  * Note your player follows you across servers.

### Server

[Server Documentation](/server/README.md)

## One more thing...

For the bleeding edge folks...
Buildable player portals are already in the client, but need to be given manually by a server admin.

`/give playername mverse:teleportal 2`

`/give playername mverse:tuning_fork 1`

Process to build a portal between servers:
* Place a teleportal
* Equip the tuning fork (that looks like an egg...) and right click the teleportal ONCE
* Disconnect and join another server in the cluster
* Place another teleportal
* Use the same tuning fork, right click this teleportal
* The tuning fork should be destroyed and the link should be created.
* Now stand on top of a portal for 3 seconds, and you should be teleported to the other side

## FAQ

Coming Soon
