package pl.okej.fallhubkoth.config;

import pl.okej.fallhubkoth.Main;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final Main plugin;
    private final Map<String, KothConfig> kothConfigs = new HashMap<>();
    
    private String prefix;
    private String noPermissionMessage;
    private String alreadyRunningMessage;
    private String notRunningMessage;
    private String regionNotFoundMessage;
    private String reloadSuccessMessage;
    private String invalidKothMessage;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        
        kothConfigs.clear();
        
        prefix = plugin.getConfig().getString("messages.prefix", "&7[&eFallKOTH&7] ");
        noPermissionMessage = plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to use this command.");
        alreadyRunningMessage = plugin.getConfig().getString("messages.already-running", "&cThis KOTH event is already running!");
        notRunningMessage = plugin.getConfig().getString("messages.not-running", "&cThis KOTH event is not currently running!");
        regionNotFoundMessage = plugin.getConfig().getString("messages.region-not-found", "&cThe specified region could not be found!");
        reloadSuccessMessage = plugin.getConfig().getString("messages.reload-success", "&aConfiguration reloaded successfully!");
        invalidKothMessage = plugin.getConfig().getString("messages.invalid-koth", "&cInvalid KOTH event specified!");
        
        ConfigurationSection events = plugin.getConfig().getConfigurationSection("events");
        if (events != null) {
            for (String kothId : events.getKeys(false)) {
                ConfigurationSection kothSection = events.getConfigurationSection(kothId);
                if (kothSection == null) continue;

                try {
                    KothConfig kothConfig = loadKothConfig(kothId, kothSection);
                    kothConfigs.put(kothId, kothConfig);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load KOTH config for " + kothId + ": " + e.getMessage());
                }
            }
        }
    }

    private KothConfig loadKothConfig(String id, ConfigurationSection section) {
        int timeToWin = section.getInt("time-to-win", 60);
        String regionName = section.getString("region.name", "koth_" + id);
        String regionWorld = section.getString("region.world", "world");
        String bossBarTitle = section.getString("bossbar.title", "&e&lKOTH &7- &f%player% &8| &f%time%s remaining");

        String barColorStr = section.getString("bossbar.color", "RED");
        BarColor bossBarColor;
        try {
            bossBarColor = BarColor.valueOf(barColorStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid boss bar color for " + id + ": " + barColorStr + ". Using RED instead.");
            bossBarColor = BarColor.RED;
        }

        String barStyleStr = section.getString("bossbar.style", "SOLID");
        BarStyle bossBarStyle;
        try {
            bossBarStyle = BarStyle.valueOf(barStyleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid boss bar style for " + id + ": " + barStyleStr + ". Using SOLID instead.");
            bossBarStyle = BarStyle.SOLID;
        }

        String eventStartMessage = section.getString("messages.event-start", "&aThe KOTH event has started!");
        String eventStopMessage = section.getString("messages.event-stop", "&cThe KOTH event has been stopped.");
        String eventWinMessage = section.getString("messages.event-win", "&e%player% &ahas won the KOTH event!");

        return new KothConfig(
                id,
                timeToWin,
                regionName,
                regionWorld,
                bossBarTitle,
                bossBarColor,
                bossBarStyle,
                eventStartMessage,
                eventStopMessage,
                eventWinMessage,
                section.getStringList("win-commands")
        );
    }

    public String formatMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public KothConfig getKothConfig(String id) {
        return kothConfigs.get(id);
    }

    public Map<String, KothConfig> getAllKothConfigs() {
        return kothConfigs;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getNoPermissionMessage() {
        return formatMessage(prefix + noPermissionMessage);
    }

    public String getAlreadyRunningMessage() {
        return formatMessage(prefix + alreadyRunningMessage);
    }

    public String getNotRunningMessage() {
        return formatMessage(prefix + notRunningMessage);
    }

    public String getRegionNotFoundMessage() {
        return formatMessage(prefix + regionNotFoundMessage);
    }

    public String getReloadSuccessMessage() {
        return formatMessage(prefix + reloadSuccessMessage);
    }

    public String getInvalidKothMessage() {
        return formatMessage(prefix + invalidKothMessage);
    }
}
