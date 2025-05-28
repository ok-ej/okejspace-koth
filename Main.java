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
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        eventManager = new EventManager(this);

        getCommand("okejkoth").setExecutor(new CommandHandler(this));

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getLogger().info("");
        getLogger().info(" PLUGIN OKEJSPACE-KOTH");
        getLogger().info(" Plugin został uruchomiony!");
        getLogger().info(" Discord: https://dc.okej.space");
        getLogger().info("");
    }

    @Override
    public void onDisable() {
        if (eventManager.isEventRunning()) {
            eventManager.stopEvent();
        }

        getLogger().info("");
        getLogger().info(" PLUGIN OKEJSPACE-KOTH");
        getLogger().info(" Plugin został zatrzymany!");
        getLogger().info(" Discord: https://dc.okej.space");
        getLogger().info("");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }
}
