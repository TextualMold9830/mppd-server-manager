# MPPD Server Manager

MPPD Server Manager is a comprehensive tool for managing your MPPD server, providing both a modern JavaFX graphical user interface and a robust command-line interface.

## Features

- **Graphical User Interface**: A modern JavaFX UI with dedicated tabs for Dashboard, Configuration, Plugins & Textures, Marketplace, and Backups.
- **Server Lifecycle Management**: Easily start, stop, and reset your server.
- **Plugin & Texture Management**: Install, remove, and update plugins and texture packs.
- **Marketplace**: Browse and install curated plugins and textures.
- **Backup Manager**: Create timestamped backups of your server data, restore from previous states, and manage storage by deleting old backups.
- **Auto-Loading**: Configuration, plugins, and texture packs are automatically loaded from the installation directory on startup.
- **CLI & Interactive Mode**: Original CLI commands and interactive shell are supported as fallbacks.

## Technical Details

- **Language**: Java 21
- **Framework**: JavaFX 21
- **Dependencies**: Gson, Apache Commons IO

## Usage

### UI Mode (Default)
To launch the graphical interface, use:
```bash
mvn javafx:run
# OR
mvn exec:java -Dexec.mainClass="textualmold9830.mppd.Main"
```

### CLI Mode
You can also run commands directly from the terminal:
```bash
# Install the server JAR and setup environment
mvn exec:java -Dexec.mainClass="textualmold9830.mppd.Main" -Dexec.args="install"

# See available commands
mvn exec:java -Dexec.mainClass="textualmold9830.mppd.Main" -Dexec.args="help"
```

## Installation

Ensure you have Maven and JDK 21 installed. Clone the repository and run `mvn clean install` to build the project.

## Releases

This project uses GitHub Actions for automated releases. To trigger a new release, simply push a version tag:
```bash
git tag v1.0.0
git push origin v1.0.0
```
This will build the project for Windows, macOS, and Linux, and create a GitHub release with the compiled JAR.