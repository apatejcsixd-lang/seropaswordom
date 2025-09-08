
package hu.kazu.serverpassword;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AuthListener implements Listener {

    private final ServerPasswordPlugin plugin;

    public AuthListener(ServerPasswordPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean locked(Player p) {
        return !plugin.isAuthed(p.getUniqueId()) && !p.hasPermission("serverpassword.bypass");
    }

    static void clearLockEffects(Player p) {
        p.removePotionEffect(PotionEffectType.BLINDNESS);
        p.removePotionEffect(PotionEffectType.SLOW);
        try { p.setWalkSpeed(0.2f); } catch (Exception ignored) {}
        try { p.setFlySpeed(0.1f); } catch (Exception ignored) {}
        p.setAllowFlight(false);
    }

    private void applyLockEffects(Player p) {
        if (plugin.effDisableFly()) {
            p.setAllowFlight(false);
            p.setFlying(false);
        }
        if (plugin.effBlindness()) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, true, false, false));
        }
        if (plugin.effSlowness()) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 255, true, false, false));
        }
        try { p.setWalkSpeed(0f); } catch (Exception ignored) {}
        try { p.setFlySpeed(0f); } catch (Exception ignored) {}
    }

    private void sendPrompt(Player p) {
        p.sendMessage(ServerPasswordPlugin.color(plugin.msgPrompt()));
        p.sendActionBar(ServerPasswordPlugin.color(plugin.msgPrompt()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("serverpassword.bypass")) {
            plugin.setAuthed(p.getUniqueId(), true);
            return;
        }
        if (plugin.isRequireEveryJoin()) {
            plugin.setAuthed(p.getUniqueId(), false);
        }
        if (locked(p)) {
            applyLockEffects(p);
            sendPrompt(p);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        plugin.setAuthed(p.getUniqueId(), false);
        plugin.clearAttempt(p.getUniqueId());
        clearLockEffects(p);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!locked(p)) return;
        var from = e.getFrom();
        var to = e.getTo();
        if (to == null) return;
        boolean moved = from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ();
        if (moved) {
            e.setTo(from);
            e.setCancelled(true);
            sendPrompt(p);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (!locked(p)) return;
        String msg = e.getMessage().toLowerCase();
        if (msg.startsWith("/login") || msg.startsWith("/serverpassword")) return;
        e.setCancelled(true);
        p.sendMessage(ServerPasswordPlugin.color(plugin.msgLocked()));
        sendPrompt(p);
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!locked(p)) return;
        e.setCancelled(true);
        Bukkit.getScheduler().runTask(plugin, () -> {
            p.sendMessage(ServerPasswordPlugin.color(plugin.msgLocked()));
            sendPrompt(p);
        });
    }

    @EventHandler(ignoreCancelled = true) public void onInteract(PlayerInteractEvent e){ if (locked(e.getPlayer())) e.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void onBlockBreak(BlockBreakEvent e){ if (locked(e.getPlayer())) e.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void onBlockPlace(BlockPlaceEvent e){ if (locked(e.getPlayer())) e.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void onInvOpen(InventoryOpenEvent e){ if (e.getPlayer() instanceof Player p && locked(p)) { e.setCancelled(true); p.closeInventory(); } }
    @EventHandler(ignoreCancelled = true) public void onInvClick(InventoryClickEvent e){ if (e.getWhoClicked() instanceof Player p && locked(p)) e.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void onDrop(PlayerDropItemEvent e){ if (locked(e.getPlayer())) e.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void onPickup(EntityPickupItemEvent e){ if (e.getEntity() instanceof Player p && locked(p)) e.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void onDamage(EntityDamageEvent e){ if (e.getEntity() instanceof Player p && locked(p)) e.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void onDamageBy(EntityDamageByEntityEvent e){ if (e.getDamager() instanceof Player p && locked(p)) e.setCancelled(true); if (e.getEntity() instanceof Player p2 && locked(p2)) e.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void onFood(FoodLevelChangeEvent e){ if (e.getEntity() instanceof Player p && locked(p)) e.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void onPortal(PlayerPortalEvent e){ if (locked(e.getPlayer())) e.setCancelled(true); }
    @EventHandler(ignoreCancelled = true) public void onRespawn(PlayerRespawnEvent e){ Player p = e.getPlayer(); if (locked(p)) { Bukkit.getScheduler().runTask(plugin, () -> { applyLockEffects(p); sendPrompt(p); }); } }
    @EventHandler(ignoreCancelled = true) public void onGamemode(PlayerGameModeChangeEvent e){ if (locked(e.getPlayer())) e.setCancelled(true); }
}
