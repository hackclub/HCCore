# HCCore [![Build Status](https://github.com/hackclub/HCCore/actions/workflows/workflow.yml/badge.svg)](https://github.com/hackclub/HCCore/actions/workflows/workflow.yml)

Main plugin for the Hack Club Minecraft server.

## Features
- Syncs chat and server events to/from a Slack channel
- Enables account linking and lookup between Slack and Minecraft accounts
  - Including an option to require linking to play on the server
- Pulls the server MOTD icon from [Shrimp Shuffler](https://shrimp-shuffler.a.hackclub.dev/)
- Adds custom advancements
- Adds custom commands
- Adds an AFK system

## Usage
To use HCCore, you need a Minecraft server with [PaperMC](https://papermc.io/downloads/paper) 1.21 or newer. \
On said server, install the following dependencies:
- [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) (you will likely need a [dev build](https://ci.dmulloy2.net/job/ProtocolLib/lastSuccessfulBuild/))
- [UltimateAdvancementAPI](https://www.spigotmc.org/resources/ultimateadvancementapi-1-15-1-19-3.95585/)

After that, you can install the [latest release](https://github.com/hackclub/HCCore/releases/latest) of HCCore. Install these by moving them to your `plugins/` folder. \
Launch your server, then stop it and open the `plugins/HCCore/config.yml` file. There, you can configure it!

## Development
See the [`./SETUP.md`](SETUP.md) documentation to help you get a development environment started!

## License
[MIT License](LICENSE.txt)
