package pl.okej.fallhubkoth;

import pl.okej.fallhubkoth.commands.CommandHandler;
import pl.okej.fallhubkoth.config.ConfigManager;
import pl.okej.fallhubkoth.listener.PlayerListener;
import pl.okej.fallhubkoth.manager.EventManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private ConfigManager configManager;
    private EventManager eventManager;

    @Override
    public void onEnable() {
        // Initialize config manager
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // Initialize event manager
        eventManager = new EventManager(this);

        // Register commands
        getCommand("fallkoth").setExecutor(new CommandHandler(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getLogger().info("");
        getLogger().info("PLUGIN FALLHUB-KOTH ZOSTAŁ WŁĄCZONY");
        getLogger().info("DISCORD: DC.FALLHUB.PL");
        getLogger().info("");
    }

    @Override
    public void onDisable() {
        // Stop any running event when the plugin is disabled
        if (eventManager.isEventRunning()) {
            eventManager.stopEvent();
        }

        getLogger().info("");
        getLogger().info("PLUGIN FALLHUB-KOTH ZOSTAŁ WYŁĄCZONY");
        getLogger().info("DISCORD: DC.FALLHUB.PL");
        getLogger().info("");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }
}
