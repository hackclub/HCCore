# Setup

For the purposes of this guide we'll designate the `~/minecraft-server` and `~/HCCore` directories
for the locations of the minecraft (paper) server, and the HCCore source code. Of course, you don't
have to put the files in the same place

## 1. Download and Build HCCore

We use [Gradle](https://gradle.org/install) for dependency management and as a build time tool

```sh
git clone https://github.com/hackclub/HCCore
cd HCCore

# linux
./gradlew build

# windows
./gradlew.bat build
```

## 2. run the built-in Minecraft Paper Server

```sh
./gradlew runServer
```

the server will fail to load, because you havnt accepted the EULA, so do that, and re-run  
next it will error out due to missing dependancies, so install those:

## 3. Install Dependencies

Download:

- [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) [Dev Builds](https://ci.dmulloy2.net/job/ProtocolLib/lastSuccessfulBuild/)
- [UltimateAdvancementAPI](https://www.spigotmc.org/resources/ultimateadvancementapi-1-15-1-19-3.95585/)

```sh
mv ~/Downloads/ProtocolLib.jar run/plugins
mv ~/Downloads/UltimateAdvancementAPI-2.2.2-(1.15-1.19.3).jar run/plugins
```

Note: Make sure you stop the server and restart it for the plugins to properly load

BOOM! Now we can connect with our Minecraft client and test/use the plugin

```text
-> wilde_fox joined the game
/color
-> /color <chat|name> [color]
```

## 4. ~~Install BileTools~~

> Note: As of writing, BileTools is not working properly with the latest Paper version. See this
> issue for details: https://github.com/VolmitSoftware/BileTools/issues/8

We can use BileTools to hot-reload our plugin. BileTools looks for changes in the directory, and
when it detects that a `.jar` file has changed, it will reload that plugin

You can test this out by modifying the strings in `player.getServer().broadcastMessage()`
in `src/main/java/com/hackclub/hccore/listeners/AFKListener.java`

Once you have made your modifications, rebuild

```
cd ~/HCCore
./gradlew build
cp ~/HCCore/build/libs/HCCore-1.0.0.jar ~/minecraft-server/plugins
```

Without closing the server, your console should look like the following

```text
[00:47:50 INFO]: [BileTools] File change detected: HCCore.jar
[00:47:50 INFO]: [BileTools] Identified Plugin: HCCore <-> HCCore.jar
[00:47:50 INFO]: [BileTools] Reloading: HCCore
[00:47:50 INFO]: Backed up HCCore 1.0.0
[00:47:50 INFO]: [Bile]: Unloading HCCore
[00:47:50 INFO]: [HCCore] Disabling HCCore v1.0.0
[00:47:50 INFO]: [Bile]: Loading HCCore 1.0.0
[00:47:50 INFO]: [HCCore] Enabling HCCore v1.0.0
[00:47:50 INFO]: Reloading ResourceManager: Default, bukkit
[00:47:50 INFO]: Loaded 7 recipes
```
