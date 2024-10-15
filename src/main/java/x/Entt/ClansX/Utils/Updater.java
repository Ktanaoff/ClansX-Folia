package x.Entt.ClansX.Utils;

import x.Entt.ClansX.CX;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Updater {

    private static CX plugin;
    private static int resourceId;

    public Updater(CX plugin, int resourceId) {
        Updater.plugin = plugin;
        Updater.resourceId = resourceId;
    }

    public static String getLatestVersion() throws IOException {
        URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (InputStream inputStream = connection.getInputStream();
             Scanner scanner = new Scanner(inputStream)) {
            if (scanner.hasNext()) {
                return scanner.next();
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to retrieve the latest version: " + e.getMessage());
            throw e;
        }
        return null;
    }

    public static boolean isUpdateAvailable() {
        try {
            String currentVersion = plugin.getDescription().getVersion();
            String latestVersion = getLatestVersion();
            return latestVersion != null && !latestVersion.equals(currentVersion);
        } catch (IOException e) {
            plugin.getLogger().severe("Error checking for updates: " + e.getMessage());
            return false;
        }
    }
}