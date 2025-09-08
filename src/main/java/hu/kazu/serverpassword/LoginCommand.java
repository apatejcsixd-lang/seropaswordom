
package hu.kazu.serverpassword;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginCommand implements CommandExecutor {

    private final ServerPasswordPlugin plugin;

    public LoginCommand(ServerPasswordPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Csak játékos használhatja.");
            return true;
        }

        if (p.hasPermission("serverpassword.bypass")) {
            p.sendMessage(ServerPasswordPlugin.color("&eBypass jogod van, nincs szükség bejelentkezésre."));
            plugin.setAuthed(p.getUniqueId(), true);
            return true;
        }

        if (plugin.isAuthed(p.getUniqueId())) {
            p.sendMessage(ServerPasswordPlugin.color("&aMár be vagy jelentkezve."));
            return true;
        }

        if (args.length != 1) {
            p.sendMessage(ServerPasswordPlugin.color("&eHasználat: &6/login <jelszó>"));
            return true;
        }

        String supplied = args[0];
        if (supplied.equals(plugin.getPassword())) {
            plugin.setAuthed(p.getUniqueId(), true);
            plugin.resetAttempts(p.getUniqueId());
            AuthListener.clearLockEffects(p);
            p.sendMessage(ServerPasswordPlugin.color(plugin.msgSuccess()));
        } else {
            int n = plugin.addAttempt(p.getUniqueId());
            p.sendMessage(ServerPasswordPlugin.color(plugin.msgWrong()));
            if (plugin.isKickOnMax() && n >= plugin.getMaxAttempts()) {
                p.kickPlayer(ServerPasswordPlugin.color(plugin.msgKicked()));
            }
        }
        return true;
    }
}
