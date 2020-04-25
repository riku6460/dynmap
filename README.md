# Dynmap - dynamic web maps for Minecraft servers

## How to build
Dynmap 3.x+ uses Gradle for building support for all platforms, with all resulting artifacts produced in the /targets directory

To build, run:
    ./gradlew clean build install
Or (on Windows):
    gradlew.bat clean build install
    
# What platforms are supported?
The following target platforms are supported:
- CraftBukkit/Spigot - via the Dynmap-<version>-spigot.jar plugin (supports MC v1.8.9 through v1.14.2)
- Forge v1.8.9 - via Dynmap-<version>-forge-1.8.9.jar mod
- Forge v1.9.4 - via Dynmap-<version>-forge-1.9.4.jar mod
- Forge v1.10.2 - via Dynmap-<version>-forge-1.10.2.jar mod
- Forge v1.11.2 - via Dynmap-<version>-forge-1.11.2.jar mod
- Forge v1.12.2 - via Dynmap-<version>-forge-1.12.2.jar mod

# Data Storage
Dynmap supports the following storage backends:
- Flat files: The default for a new installation
- SQLite
- MySQL
- PostgreSQL: EXPERIMENTAL

# Where to go for questions and discussions
I've just created a Reddit for the Dynmap family of mods/plugins - please give it a try - https://www.reddit.com/r/Dynmap/

# Where to go to make donations
I've set up a coffee-fund jar (I believe in the theory that software developers are machines that turn caffeine into code), for anyone who wants to throw in some tips!  https://ko-fi.com/michaelprimm
