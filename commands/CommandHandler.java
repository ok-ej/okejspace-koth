package pl.okej.fallhubkoth.commands;

import pl.okej.fallhubkoth.Main;
import pl.okej.fallhubkoth.manager.EventManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public CommandHandler(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("okejkoth.admin")) {
            sender.sendMessage(plugin.getConfigManager().getNoPermissionMessage());
            return true;
        }

        if (args.length < 1) {
            sendHelpMessage(sender);
            return true;
        }

        EventManager eventManager = plugin.getEventManager();

        switch (args[0].toLowerCase()) {
            case "start":
                if (args.length < 2) {
                    sender.sendMessage(plugin.getConfigManager().formatMessage("&cUżycie: /okejkoth start <kothId>"));
                    return true;
                }

                String kothId = args[1].toLowerCase();
                if (plugin.getConfigManager().getKothConfig(kothId) == null) {
                    sender.sendMessage(plugin.getConfigManager().getInvalidKothMessage());
                    return true;
                }

                if (eventManager.isEventRunning()) {
                    sender.sendMessage(plugin.getConfigManager().getAlreadyRunningMessage());
                    return true;
                }

                boolean success = eventManager.startEvent(kothId);
                if (!success) {
                    sender.sendMessage(plugin.getConfigManager().getRegionNotFoundMessage());
                    return true;
                }

                sender.sendMessage(plugin.getConfigManager().formatMessage(
                        plugin.getConfigManager().getKothConfig(kothId).getEventStartMessage()
                ));
                break;

            case "stop":
                if (args.length < 2) {
                    sender.sendMessage(plugin.getConfigManager().formatMessage("&cUżycie: /okejkoth stop <kothId>"));
                    return true;
                }

                kothId = args[1].toLowerCase();
                if (!eventManager.isEventRunning()) {
                    sender.sendMessage(plugin.getConfigManager().getNotRunningMessage());
                    return true;
                }

                eventManager.stopEvent();
                sender.sendMessage(plugin.getConfigManager().formatMessage(
                        plugin.getConfigManager().getKothConfig(kothId).getEventStopMessage()
                ));
                break;

            case "reload":
                plugin.getConfigManager().loadConfig();
                sender.sendMessage(plugin.getConfigManager().getReloadSuccessMessage());
                break;

            default:
                sendHelpMessage(sender);
                break;
        }

        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(plugin.getConfigManager().formatMessage("&x&0&0&8&D&F&F&lP&x&0&0&8&B&F&F&lL&x&0&0&8&A&F&F&lU&x&0&0&8&8&F&F&lG&x&0&0&8&7&F&F&lI&x&0&0&8&5&F&F&lN &x&0&0&8&2&F&F&lN&x&0&0&8&1&F&F&lA &x&0&0&7&E&F&F&lK&x&0&0&7&C&F&F&lO&x&0&0&7&B&F&F&lT&x&0&0&7&9&F&F&lH &x&0&0&7&6&F&F&lB&x&0&0&7&5&F&F&lY &x&0&0&7&2&F&F&lO&x&0&0&7&0&F&F&lK&x&0&0&6&F&F&F&lE&x&0&0&6&D&F&F&lJ&x&0&0&6&C&F&F&l.&x&0&0&6&A&F&F&lS&x&0&0&6&9&F&F&lP&x&0&0&6&7&F&F&lA&x&0&0&6&6&F&F&lC&x&0&0&6&4&F&F&lE"));
        sender.sendMessage(plugin.getConfigManager().formatMessage("&f/okejkoth start <nazwa> &7- &fWystartuj event koth"));
        sender.sendMessage(plugin.getConfigManager().formatMessage("&f/okejkoth stop <nazwa> &7- &fZatrzymaj event koth"));
        sender.sendMessage(plugin.getConfigManager().formatMessage("&f/okejkoth reload &7- &fPrzeładuj konfigurację"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("start", "stop", "reload");
            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("start") || args[0].equalsIgnoreCase("stop"))) {
            return new ArrayList<>(plugin.getConfigManager().getAllKothConfigs().keySet()).stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
