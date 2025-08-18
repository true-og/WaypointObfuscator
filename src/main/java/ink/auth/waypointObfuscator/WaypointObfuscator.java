package ink.auth.waypointObfuscator;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import java.io.File;
import java.security.SecureRandom;
import org.bukkit.plugin.java.JavaPlugin;

public final class WaypointObfuscator extends JavaPlugin {
    private static final int CURRENT_CONFIG_VERSION = 1;
    private static final int SECRET_LENGTH = 64; // good amount of letters i think
    private WaypointPacketListener waypointListener;
    private PacketListenerCommon registeredListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        updateConfigIfOutdated();
        reloadConfig();
        String secretSalt = resolveSecretSalt();
        this.waypointListener = new WaypointPacketListener(secretSalt);
        this.registeredListener = this.waypointListener.asAbstract(PacketListenerPriority.NORMAL);
        PacketEvents.getAPI().getEventManager().registerListener(this.registeredListener);

    }

    @Override
    public void onDisable() {
        if (this.registeredListener != null) {
            PacketEvents.getAPI().getEventManager().unregisterListener(this.registeredListener);
            this.registeredListener = null;
            this.waypointListener = null;
        }
    }

    private void updateConfigIfOutdated() {
        int version = getConfig().getInt("config-version", -1);
        if (version != CURRENT_CONFIG_VERSION) {
            File dataFolder = getDataFolder();
            File cfg = new File(dataFolder, "config.yml");
            if (cfg.exists()) {
                File bak = new File(dataFolder, "config.yml.bak." + System.currentTimeMillis());
                cfg.renameTo(bak);
            }
            saveResource("config.yml", true);
        }
    }

    private String resolveSecretSalt() {
        boolean rotate = getConfig().getBoolean("rotate-secret-on-restart", false);
        if (rotate) {
            String secret = generateRandomSecret(SECRET_LENGTH);
            getLogger().fine("Generated ephemeral secret for this session.");
            return secret;
        }
        String cfgSecret = getConfig().getString("secret-salt", "");
        if (cfgSecret == null || cfgSecret.isBlank()) {
            cfgSecret = generateRandomSecret(SECRET_LENGTH);
            getConfig().set("secret-salt", cfgSecret);
            saveConfig();
            getLogger().info("Secret was missing; generated and saved a new persistent secret.");
        }
        return cfgSecret;
    }

    private String generateRandomSecret(int len) {
        final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom rng = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(alphabet.charAt(rng.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
}
