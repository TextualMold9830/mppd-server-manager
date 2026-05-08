package textualmold9830.mppd;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Manager {
    private static final String SERVER_DIR = "server";
    private static final String JAR_NAME = "MPPDServer.jar";
    private static final String REMOTE_JAR_URL = "https://github.com/TextualMold9830/MPPDServerJar/releases/download/Stable/MPPDServer.jar";
    private static final String CONFIG_FILE = "config.json";
    private static final String SOURCE_JAR = "/Users/denis/IdeaProjects/MPPDServerJar/out/artifacts/MPPDServer_jar/MPPDServer.jar";
    private static final String BACKUP_DIR = "backups";
    private static final String MARKETPLACE_API_URL = "https://api.github.com/repos/TextualMold9830/mppd-marketplace/contents/";
    private static final String MARKETPLACE_RAW_URL = "https://raw.githubusercontent.com/TextualMold9830/mppd-marketplace/refs/heads/master/";

    private static Process serverProcess = null;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static List<String> trustedUsers = new ArrayList<>();

    static {
        loadTrustedUsers();
    }

    private static void loadTrustedUsers() {
        try {
            // Added cache-buster to trusted users list
            String content = new String(new URL(MARKETPLACE_RAW_URL + "trustedusers.txt?t=" + System.currentTimeMillis()).openStream().readAllBytes());
            trustedUsers = Arrays.asList(content.split("\\s+"));
        } catch (IOException e) {
            System.err.println("Failed to load trusted users: " + e.getMessage());
        }
    }

    public static void install() {
        try {
            File serverDir = new File(SERVER_DIR);
            if (!serverDir.exists()) {
                serverDir.mkdirs();
                System.out.println("Created server directory.");
            }

            File targetJar = new File(serverDir, JAR_NAME);
            File localSource = new File(SOURCE_JAR);

            if (localSource.exists()) {
                System.out.println("Local source found. Copying local JAR...");
                FileUtils.copyFile(localSource, targetJar);
            } else {
                System.out.println("Local source not found. Downloading from GitHub...");
                FileUtils.copyURLToFile(new URL(REMOTE_JAR_URL), targetJar);
                System.out.println("Download complete.");
            }

            System.out.println("Installation complete. Folders and config will be created on server's first run.");

        } catch (IOException e) {
            System.err.println("Installation failed: " + e.getMessage());
        }
    }

    public static void start(String[] args) {
        if (serverProcess != null && serverProcess.isAlive()) {
            System.out.println("Server is already running.");
            return;
        }

        File jarFile = new File(SERVER_DIR, JAR_NAME);
        if (!jarFile.exists()) {
            System.out.println("Server JAR not found. Run 'install' first.");
            return;
        }

        try {
            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-jar");
            command.add(JAR_NAME);
            if (args != null && args.length > 0 && args[0].equals("--reset")) {
                command.add("reset");
            }
            
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(SERVER_DIR));
            pb.inheritIO();
            serverProcess = pb.start();
            System.out.println("Server started (PID: " + serverProcess.pid() + ")" + (command.contains("reset") ? " with reset." : "."));
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }

    public static String getConfigRaw() {
        File configFile = new File(SERVER_DIR, CONFIG_FILE);
        if (!configFile.exists()) return "No config file present.";
        try {
            return FileUtils.readFileToString(configFile, "UTF-8");
        } catch (IOException e) {
            return "Error reading config: " + e.getMessage();
        }
    }

    public static boolean saveConfigRaw(String content) {
        File configFile = new File(SERVER_DIR, CONFIG_FILE);
        try {
            JsonParser.parseString(content);
            FileUtils.writeStringToFile(configFile, content, "UTF-8");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static List<String> getPluginsList() {
        File pluginsDir = new File(SERVER_DIR, "plugins");
        if (!pluginsDir.exists()) return new ArrayList<>();
        File[] files = pluginsDir.listFiles((dir, name) -> name.endsWith(".jar"));
        List<String> list = new ArrayList<>();
        if (files != null) {
            for (File f : files) list.add(f.getName());
        }
        return list;
    }

    public static List<String> getTexturesList() {
        File texturesDir = new File(SERVER_DIR, "textures");
        if (!texturesDir.exists()) return new ArrayList<>();
        File[] files = texturesDir.listFiles((dir, name) -> name.endsWith(".zip"));
        List<String> list = new ArrayList<>();
        if (files != null) {
            for (File f : files) list.add(f.getName());
        }
        return list;
    }

    public static boolean removePlugin(String filename) {
        File file = new File(SERVER_DIR + "/plugins", filename);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    public static boolean removeTexture(String filename) {
        File file = new File(SERVER_DIR + "/textures", filename);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    public static void stop() {
        if (serverProcess == null || !serverProcess.isAlive()) {
            System.out.println("Server is not running.");
            return;
        }

        System.out.println("Stopping server...");
        serverProcess.destroy();
        try {
            serverProcess.waitFor();
            System.out.println("Server stopped successfully.");
        } catch (InterruptedException e) {
            System.err.println("Wait for server stop interrupted: " + e.getMessage());
        }
    }

    public static void update() {
        System.out.println("Checking for updates...");
        try {
            File targetJar = new File(SERVER_DIR, JAR_NAME);
            System.out.println("Downloading latest stable version from GitHub...");
            FileUtils.copyURLToFile(new URL(REMOTE_JAR_URL), targetJar);
            System.out.println("Update complete.");
        } catch (IOException e) {
            System.err.println("Update failed: " + e.getMessage());
        }
    }

    public static class MarketItem {
        public String id, repository;
        // Fields fetched from manifest
        public String name, description, url, version;
        public boolean trusted;
    }

    public static List<MarketItem> getMarketItems(String type) {
        loadTrustedUsers(); // Refresh trust list on every market browse
        String folder = type.equals("textures") ? "texturepacks" : "plugins";
        List<MarketItem> items = new ArrayList<>();
        try {
            // Added cache-buster to API URL
            URL url = new URL(MARKETPLACE_API_URL + folder + "?t=" + System.currentTimeMillis());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            
            if (conn.getResponseCode() == 200) {
                String json = new String(conn.getInputStream().readAllBytes());
                JsonArray files = JsonParser.parseString(json).getAsJsonArray();
                for (int i = 0; i < files.size(); i++) {
                    JsonObject file = files.get(i).getAsJsonObject();
                    if (file.get("name").getAsString().endsWith(".json")) {
                        MarketItem item = new MarketItem();
                        item.id = file.get("name").getAsString().replace(".json", "");
                        items.add(item);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to fetch market items: " + e.getMessage());
        }
        return items;
    }

    public static MarketItem fetchItemDetails(MarketItem item, String type) {
        String folder = type.equals("textures") ? "texturepacks" : "plugins";
        try {
            // 1. Fetch repo URL from marketplace (with cache-buster)
            String marketJsonUrl = MARKETPLACE_RAW_URL + folder + "/" + item.id + ".json?t=" + System.currentTimeMillis();
            String marketJson = new String(new URL(marketJsonUrl).openStream().readAllBytes());
            JsonObject marketObj = JsonParser.parseString(marketJson).getAsJsonObject();
            item.repository = marketObj.get("repository").getAsString();
            if (item.repository.endsWith("/")) item.repository = item.repository.substring(0, item.repository.length() - 1);

            // 2. Fetch plugin/texture manifest from the repository (with cache-buster)
            String repoPath = item.repository.replace("https://github.com/", "");
            String manifestName = "market-info.json";
            String manifestUrl = "https://raw.githubusercontent.com/" + repoPath + "/refs/heads/master/" + manifestName + "?t=" + System.currentTimeMillis();
            
            String manifestJson = new String(new URL(manifestUrl).openStream().readAllBytes());
            MarketItem details = gson.fromJson(manifestJson, MarketItem.class);
            
            details.id = item.id;
            details.repository = item.repository;
            
            // 3. Check trust
            String owner = repoPath.split("/")[0];
            details.trusted = trustedUsers.contains(owner);
            
            return details;
        } catch (IOException e) {
            System.err.println("Failed to fetch details for " + item.id + ": " + e.getMessage());
            return item;
        }
    }

    public static void checkForPluginUpdates() {
        List<MarketItem> marketItems = getMarketItems("plugins");
        for (MarketItem item : marketItems) {
            MarketItem detailed = fetchItemDetails(item, "plugins");
            if (detailed.url != null) {
                File pluginFile = new File(SERVER_DIR + "/plugins", detailed.url.substring(detailed.url.lastIndexOf("/") + 1));
                if (pluginFile.exists()) {
                    System.out.println("Checking update for: " + detailed.name + " (Market version: " + detailed.version + ")");
                }
            }
        }
    }

    public static void installFromMarket(MarketItem item, String type) {
        try {
            File targetDir = new File(SERVER_DIR, type.equals("plugins") ? "plugins" : "textures");
            if (!targetDir.exists()) targetDir.mkdirs();
            File targetFile = new File(targetDir, item.url.substring(item.url.lastIndexOf("/") + 1));
            FileUtils.copyURLToFile(new URL(item.url), targetFile);
            System.out.println("Installed: " + item.name);
        } catch (IOException e) {
            System.err.println("Failed to install from market: " + e.getMessage());
        }
    }

    public static void backup() {
        File saveDir = new File(SERVER_DIR, "save");
        if (!saveDir.exists() || !saveDir.isDirectory()) {
            System.out.println("No save directory found to backup.");
            return;
        }

        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) backupDir.mkdirs();
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        File backupFile = new File(backupDir, "Backup_" + timestamp + ".zip");

        System.out.println("Creating backup of 'save' folder...");
        try (FileOutputStream fos = new FileOutputStream(backupFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            Path sourcePath = saveDir.toPath();
            Files.walk(sourcePath).filter(path -> !Files.isDirectory(path)).forEach(path -> {
                ZipEntry zipEntry = new ZipEntry(sourcePath.relativize(path).toString());
                try {
                    zos.putNextEntry(zipEntry);
                    Files.copy(path, zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    System.err.println("Error adding file to backup: " + path);
                }
            });
            System.out.println("Backup created successfully: " + backupFile.getName());
        } catch (IOException e) {
            System.err.println("Backup failed: " + e.getMessage());
        }
    }

    public static List<String> getBackupsList() {
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) return new ArrayList<>();
        File[] files = backupDir.listFiles((dir, name) -> name.endsWith(".zip"));
        List<String> list = new ArrayList<>();
        if (files != null) {
            Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            for (File f : files) list.add(f.getName());
        }
        return list;
    }

    public static boolean deleteBackup(String filename) {
        File file = new File(BACKUP_DIR, filename);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    public static boolean restoreBackup(String filename) {
        File backupFile = new File(BACKUP_DIR, filename);
        if (!backupFile.exists()) return false;

        File saveDir = new File(SERVER_DIR, "save");
        try {
            if (saveDir.exists()) {
                FileUtils.deleteDirectory(saveDir);
            }
            saveDir.mkdirs();

            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(backupFile))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    File outFile = new File(saveDir, entry.getName());
                    if (entry.isDirectory()) {
                        outFile.mkdirs();
                    } else {
                        outFile.getParentFile().mkdirs();
                        try (FileOutputStream fos = new FileOutputStream(outFile)) {
                            zis.transferTo(fos);
                        }
                    }
                    zis.closeEntry();
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("Restore failed: " + e.getMessage());
            return false;
        }
    }
}
