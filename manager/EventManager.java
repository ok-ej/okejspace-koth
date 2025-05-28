package pl.okej.fallhubkoth.manager;

import pl.okej.fallhubkoth.Main;
import pl.okej.fallhubkoth.config.KothConfig;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventManager {

    private final Main plugin;
    private final Map<String, ActiveKoth> activeKoths = new HashMap<>();
    private String kothId;

    public EventManager(Main plugin) {
        this.plugin = plugin;
    }

    public boolean startEvent(String kothId) {
        if (activeKoths.containsKey(kothId)) {
            return false;
        }

        KothConfig config = plugin.getConfigManager().getKothConfig(kothId);
        if (config == null) {
            return false;
        }
        
        String regionName = config.getRegionName();
        String worldName = config.getRegionWorld();

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("Świat '" + worldName + "' nie został znaleziony!");
            return false;
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world));

        if (regions == null) {
            plugin.getLogger().warning("Nie można zaaktualizowac: " + worldName);
            return false;
        }

        ProtectedRegion region = regions.getRegion(regionName);
        if (region == null) {
            plugin.getLogger().warning("Region '" + regionName + "' nie został znaleziony na świecie '" + worldName + "'!");
            return false;
        }

        ActiveKoth activeKoth = new ActiveKoth(config, region);
        activeKoths.put(kothId, activeKoth);

        for (Player player : Bukkit.getOnlinePlayers()) {
            activeKoth.getBossBar().addPlayer(player);
        }

        return true;
    }

    public void stopEvent() {
        ActiveKoth activeKoth = activeKoths.remove(kothId);
        if (activeKoth != null) {
            activeKoth.cleanup();
        }
    }

    public void stopAllEvents() {
        for (String kothId : new HashMap<>(activeKoths).keySet()) {
            stopEvent();
        }
    }

    public boolean isEventRunning() {
        return activeKoths.containsKey(kothId);
    }

    public void addPlayerToBossBar(Player player) {
        for (ActiveKoth activeKoth : activeKoths.values()) {
            activeKoth.getBossBar().addPlayer(player);
        }
    }

    public void removePlayer(Player player) {
        for (ActiveKoth activeKoth : activeKoths.values()) {
            activeKoth.removePlayer(player);
        }
    }

    private class ActiveKoth {
        private final KothConfig config;
        private final ProtectedRegion region;
        private final BossBar bossBar;
        private final BukkitTask updateTask;
        private final Map<UUID, Integer> playerTimes = new HashMap<>();
        private final Map<UUID, Boolean> wasInRegion = new HashMap<>();

        public ActiveKoth(KothConfig config, ProtectedRegion region) {
            this.config = config;
            this.region = region;

            this.bossBar = Bukkit.createBossBar(
                    plugin.getConfigManager().formatMessage(config.getBossBarTitle()
                            .replace("%player%", "Nikt")
                            .replace("%time%", String.valueOf(config.getTimeToWin()))),
                    config.getBossBarColor(),
                    config.getBossBarStyle()
            );

            this.updateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::update, 0L, 20L);
        }

        private void update() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                boolean inRegion = isPlayerInRegion(player);
                UUID uuid = player.getUniqueId();

                if (!inRegion && wasInRegion.getOrDefault(uuid, false)) {
                    playerTimes.remove(uuid);
                }

                wasInRegion.put(uuid, inRegion);

                if (inRegion) {
                    updatePlayerTime(player);
                }
            }

            Player leadingPlayer = getLeadingPlayer();
            int leadingTime = leadingPlayer != null ? playerTimes.get(leadingPlayer.getUniqueId()) : 0;

            if (bossBar != null) {
                if (leadingPlayer != null) {
                    int remaining = config.getTimeToWin() - leadingTime;
                    bossBar.setTitle(plugin.getConfigManager().formatMessage(
                            config.getBossBarTitle()
                                    .replace("%player%", leadingPlayer.getName())
                                    .replace("%time%", String.valueOf(remaining))
                    ));

                    double progress = (double) leadingTime / config.getTimeToWin();
                    bossBar.setProgress(Math.min(1.0, progress));

                    if (leadingTime >= config.getTimeToWin()) {
                        handleWinner(leadingPlayer);
                    }
                } else {
                    bossBar.setTitle(plugin.getConfigManager().formatMessage(
                            config.getBossBarTitle()
                                    .replace("%player%", "Nikt")
                                    .replace("%time%", String.valueOf(config.getTimeToWin()))
                    ));
                    bossBar.setProgress(0);
                }
            }
        }

        private boolean isPlayerInRegion(Player player) {
            com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(player.getLocation());
            return region.contains(loc.toVector().toBlockPoint());
        }

        private void updatePlayerTime(Player player) {
            UUID uuid = player.getUniqueId();
            playerTimes.put(uuid, playerTimes.getOrDefault(uuid, 0) + 1);
        }

        private Player getLeadingPlayer() {
            if (playerTimes.isEmpty()) {
                return null;
            }

            UUID leadingUUID = null;
            int maxTime = -1;

            for (Map.Entry<UUID, Integer> entry : playerTimes.entrySet()) {
                if (entry.getValue() > maxTime) {
                    leadingUUID = entry.getKey();
                    maxTime = entry.getValue();
                }
            }

            return leadingUUID != null ? Bukkit.getPlayer(leadingUUID) : null;
        }

        private void handleWinner(Player player) {
            Bukkit.broadcastMessage(plugin.getConfigManager().formatMessage(
                    config.getEventWinMessage().replace("%player%", player.getName())
            ));

            for (String command : config.getWinCommands()) {
                String formattedCommand = command.replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCommand);
            }

            stopEvent();
        }

        public void removePlayer(Player player) {
            playerTimes.remove(player.getUniqueId());
            wasInRegion.remove(player.getUniqueId());
            bossBar.removePlayer(player);
        }

        public void cleanup() {
            updateTask.cancel();
            bossBar.removeAll();
            playerTimes.clear();
            wasInRegion.clear();
        }

        public BossBar getBossBar() {
            return bossBar;
        }
    }
}
