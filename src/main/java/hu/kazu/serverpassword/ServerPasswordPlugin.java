
package hu.kazu.serverpassword;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ServerPasswordPlugin extends JavaPlugin {

    private final Set<java.util.UUID> authed = Collections.synchronizedSet(new HashSet<>());
    private final Map<java.util.UUID, Integer> attempts = Collections.synchronizedMap(new HashMap<>());

    private String password;
    private int maxAttempts;
    private boolean kickOnMax;
    private boolean requireEveryJoin;

    private String msgPrompt, msgSuccess, msgWrong, msgLocked, msgKicked;

    private boolean effBlindness, effSlowness, effDisableFly;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfigValues();

        // Parancsok
        if (getCommand("login") != null) {
            getCommand("login").setExecutor(new LoginCommand(this));
        }
        if (getCommand("serverpassword") != null) {
            getCommand("serverpassword").setExecutor((sender, cmd, label, args) -> {
                if (!sender.hasPermission("serverpassword.admin")) {
                    sender.sendMessage(color("&cNincs jogosultságod."));
                    return true;
                }
                if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                    reloadConfig();
                    loadConfigValues();
                    sender.sendMessage(color("&aServerPassword config újratöltve."));
                    return true;
                }
                sender.sendMessage(color("&eHasználat: /serverpassword reload"));
                return true;
            });
        }

        // Eseménykezelő
        Bukkit.getPluginManager().registerEvents(new AuthListener(this), this);

        getLogger().info("ServerPassword bekapcsolva.");
    }

    @Override
    public void onDisable() {
        authed.clear();
        attempts.clear();
    }

    private void loadConfigValues() {
        FileConfiguration c = getConfig();
        password = c.getString("password", "valtsd-meg");
        maxAttempts = c.getInt("max-attempts", 3);
        kickOnMax = c.getBoolean("kick-on-max", true);
        requireEveryJoin = c.getBoolean("require-every-join", true);

        msgPrompt  = c.getString("messages.prompt", "&eÍrd be: &6/login <jelszó>&e a folytatáshoz.");
        msgSuccess = c.getString("messages.success", "&aSikeres bejelentkezés! Jó játékot!");
        msgWrong   = c.getString("messages.wrong", "&cHibás jelszó!");
        msgLocked  = c.getString("messages.locked", "&cAmíg nem jelentkezel be, semmit sem tehetsz!");
        msgKicked  = c.getString("messages.kicked", "&cTúl sok hibás próbálkozás.");

        effBlindness = c.getBoolean("effects.blindness", true);
        effSlowness  = c.getBoolean("effects.slowness", true);
        effDisableFly = c.getBoolean("effects.disable-fly", true);
    }

    public static String color(String s) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
    }

    public boolean isAuthed(java.util.UUID id) { return authed.contains(id); }
    public void setAuthed(java.util.UUID id, boolean value) { if (value) authed.add(id); else authed.remove(id); }
    public void resetAttempts(java.util.UUID id) { attempts.remove(id); }
    public int addAttempt(java.util.UUID id) {
        int n = attempts.getOrDefault(id, 0) + 1;
        attempts.put(id, n);
        return n;
    }
    public void clearAttempt(java.util.UUID id) { attempts.remove(id); }

    public String getPassword() { return password; }
    public int getMaxAttempts() { return maxAttempts; }
    public boolean isKickOnMax() { return kickOnMax; }
    public boolean isRequireEveryJoin() { return requireEveryJoin; }

    public String msgPrompt()  { return msgPrompt; }
    public String msgSuccess() { return msgSuccess; }
    public String msgWrong()   { return msgWrong; }
    public String msgLocked()  { return msgLocked; }
    public String msgKicked()  { return msgKicked; }

    public boolean effBlindness() { return effBlindness; }
    public boolean effSlowness()  { return effSlowness; }
    public boolean effDisableFly() { return effDisableFly; }
}
