/*
 * Copyright (c) 2017 David Shen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package pw.xyphrusrealms.XyphrusGuns;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class XyphrusGuns extends JavaPlugin implements Listener {

    private static XyphrusGuns instance;

    private Map<UUID, Gun> projectiles;
    private Map<Gun, Map<UUID, Long>> cooldowns;
    private Map<Gun, Map<UUID, Short>> ammos;

    public static XyphrusGuns getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        projectiles = new HashMap<>();
        cooldowns = new HashMap<>();
        ammos = new HashMap<>();

        for (Gun gun : Gun.values()) {
            cooldowns.put(gun, new HashMap<>());
            ammos.put(gun, new HashMap<>());
            for (Player p : getServer().getOnlinePlayers()) {
                ammos.get(gun).put(p.getUniqueId(), (short) gun.getMaxAmmo());
            }
        }

        new Recipes();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        for (Gun gun : Gun.values()) {
            ammos.get(gun).put(e.getPlayer().getUniqueId(), (short) gun.getMaxAmmo());
        }
    }

    @EventHandler
    public void onTeleport(EntityTeleportEvent e) {
        if (e.getEntity() instanceof Enderman) {
            e.getEntity().getWorld().getNearbyEntities(e.getFrom(), 5, 5, 5).stream().filter(entity -> projectiles.containsKey(entity.getUniqueId())).forEach(entity -> {
                e.setTo(e.getFrom());
                ((Enderman) e.getEntity()).damage(projectiles.get(entity.getUniqueId()).getDamage(entity.getLocation().distance(((Player) ((Projectile) entity).getShooter()).getLocation())));
            });
        }
    }

    @EventHandler
    public void onHoldItem(PlayerItemHeldEvent e) {
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItem(e.getNewSlot());
        ItemStack old = p.getInventory().getItem(e.getPreviousSlot());

        if (isSpecial(old) && Gun.getGun(old.getDurability()) == Gun.AS50) {
            p.removePotionEffect(PotionEffectType.SLOW);
            p.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }

        if (isSpecial(item)) {
            Gun gun = Gun.getGun(item.getDurability());
            if (gun != null) {
                updateAmmo(p, gun, 0);
                if (gun == Gun.AS50) {
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999 * 20, 127, true, false));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999 * 20, 2, true, false));
                }
                if (cooldowns.get(gun).containsKey(p.getUniqueId())) {
                    p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(gun.getCooldownWeapon());
                    return;
                }
            }
        } else if (isSpecial(p.getInventory().getItemInOffHand()) && p.getInventory().getItemInOffHand().getDurability() <= 11){
            p.getInventory().setItemInOffHand(null);
        }
        p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(4);
    }

    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent e) {
        if (!e.isCancelled() && isSpecial(e.getMainHandItem()) && e.getMainHandItem().getDurability() <= 11) {
            if (Gun.getGun(e.getOffHandItem().getDurability()) == Gun.AS50) {
                e.getPlayer().removePotionEffect(PotionEffectType.SLOW);
                e.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            }
            e.setMainHandItem(null);
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        ItemStack stack = e.getItem().getItemStack();
        if (isSpecial(stack) && stack.getDurability() <= 11) {
            e.getItem().remove();
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        ItemStack item = e.getItemDrop().getItemStack();
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        if (isSpecial(item)) {
            if (item.getDurability() > 11) {
                Gun gun = Gun.getGun(item.getDurability());
                if (gun != null) {
                    if (ammos.get(gun).get(p.getUniqueId()) == gun.getMaxAmmo()) {
                        if (gun == Gun.AS50) {
                            p.removePotionEffect(PotionEffectType.SLOW);
                            p.removePotionEffect(PotionEffectType.NIGHT_VISION);
                        }
                        return;
                    }

                    setAmmo(p, gun, gun.getMaxAmmo());

                    final long time = System.currentTimeMillis();
                    cooldowns.get(gun).put(uuid, time);
                    refreshCooldown(p, 2);
                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        if (cooldowns.get(gun).get(uuid) == time) {
                            cooldowns.get(gun).remove(uuid);
                        }
                    }, (long) gun.getCooldownTicks() * 2);

                    p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(gun.getCooldownWeapon() * 0.5);
                }
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (projectiles.containsKey(e.getEntity().getUniqueId())) {

            Projectile proj = e.getEntity();

            Set<Material> passThrough = new HashSet<>();
            passThrough.add(Material.AIR);
            passThrough.add(Material.LONG_GRASS);
            passThrough.add(Material.YELLOW_FLOWER);
            passThrough.add(Material.RED_ROSE);
            passThrough.add(Material.DOUBLE_PLANT);
            passThrough.add(Material.VINE);
            passThrough.add(Material.SUGAR_CANE_BLOCK);
            passThrough.add(Material.NETHER_WARTS);
            passThrough.add(Material.WHEAT);
            passThrough.add(Material.DEAD_BUSH);
            passThrough.add(Material.TORCH);
            passThrough.add(Material.TRIPWIRE);
            passThrough.add(Material.WHEAT);
            passThrough.add(Material.CARROT);
            passThrough.add(Material.POTATO);
            passThrough.add(Material.BEETROOT_BLOCK);

            if (passThrough.contains(proj.getLocation().getBlock().getType())) {
                passThrough(proj);
            } else if (projectiles.get(e.getEntity().getUniqueId()) == Gun.M32) {
                TNTPrimed tnt = e.getEntity().getWorld().spawn(e.getEntity().getLocation(), TNTPrimed.class);
                tnt.setYield(2);
                tnt.setFuseTicks(0);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Snowball) {
            Snowball damager = (Snowball) e.getDamager();
            if (projectiles.containsKey(damager.getUniqueId())) {
                double dmg = damager.getLocation().distance(((Player) damager.getShooter()).getLocation());
                if (damager.getShooter() == e.getEntity()) {
                    e.setCancelled(true);
                    e.getEntity().setVelocity(new Vector(0, 0, 0));
                    passThrough(damager);
                } else if (projectiles.get(damager.getUniqueId()) == Gun.M32) {
                    TNTPrimed tnt = damager.getWorld().spawn(damager.getLocation(), TNTPrimed.class);
                    tnt.setYield(2);
                    tnt.setFuseTicks(0);
                } else {
                    e.setDamage(projectiles.get(damager.getUniqueId()).getDamage(dmg));
                    projectiles.remove(damager.getUniqueId());
                }
            }
        } else if (e.getDamager() instanceof TNTPrimed) {
            TNTPrimed damager = (TNTPrimed) e.getDamager();
            if (projectiles.containsKey(damager.getUniqueId())) {
                e.setDamage(projectiles.get(damager.getUniqueId()).getDamage(Integer.MAX_VALUE));
                projectiles.remove(damager.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.isCancelled() && isSpecial(e.getCurrentItem()) && e.getCurrentItem().getDurability() <= 11) {
            e.setCancelled(true);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        ItemStack gunItem = p.getInventory().getItemInMainHand();
        if (isSpecial(gunItem)) {
            Gun gun = Gun.getGun(gunItem.getDurability());
            if (gun != null) {
                e.setCancelled(true);
                if (cooldowns.get(gun).containsKey(uuid) || ammos.get(gun).get(uuid) <= 0) {
                    if (ammos.get(gun).get(uuid) <= 0) {
                        p.sendTitle("", ChatColor.RED + "[Q] to reload");
                    }
                    return;
                }

                final long time = System.currentTimeMillis();
                cooldowns.get(gun).put(uuid, time);
                refreshCooldown(p, 2);
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    if (cooldowns.get(gun).get(uuid) == time) {
                        cooldowns.get(gun).remove(uuid);
                    }
                }, (long) gun.getCooldownTicks());

                updateAmmo(p, gun, -1);

                Snowball projectile;

                switch (gun) {
                    case SPAS12:
                        for (int i = 0; i < 10; i++) {
                            projectile = p.getWorld().spawn(p.getEyeLocation(), Snowball.class);
                            projectile.setShooter(p);
                            projectile.setVelocity(p.getLocation().getDirection().add(new Vector(genRandom(), genRandom(), genRandom())).multiply(gun.getSpeed()));
                            projectiles.put(projectile.getUniqueId(), gun);
                        }
                        break;
                    default:
                        projectile = p.getWorld().spawn(p.getEyeLocation(), Snowball.class);
                        projectile.setShooter(p);
                        projectile.setVelocity(p.getLocation().getDirection().multiply(gun.getSpeed()));
                        projectiles.put(projectile.getUniqueId(), gun);
                        break;
                }
            }
        }
    }

    private void passThrough(Projectile proj) {
        proj.remove();
        Projectile newProj = proj.getWorld().spawn(proj.getLocation().add((proj.getVelocity().normalize()).multiply(2)), Snowball.class);
        newProj.setShooter(proj.getShooter());
        newProj.setVelocity(proj.getVelocity());
        newProj.setTicksLived(proj.getTicksLived());
        projectiles.put(newProj.getUniqueId(), projectiles.get(proj.getUniqueId()));
    }

    private boolean isSpecial(ItemStack item) {
        return item != null && item.getType() == Material.DIAMOND_HOE && item.getItemMeta() != null && item.getItemMeta().spigot().isUnbreakable();
    }

    private double genRandom() {
        return Math.random() * 0.1 * (Math.random() > 0.5 ? 1 : -1);
    }

    private void refreshCooldown(Player p, int delay) {
        final int slot = p.getInventory().getHeldItemSlot();
        final int newSlot = slot != 8 ? slot + 1 : 0;
        Bukkit.getScheduler().runTaskLater(this, () -> p.getInventory().setHeldItemSlot(newSlot), delay);
        Bukkit.getScheduler().runTaskLater(this, () -> p.getInventory().setHeldItemSlot(slot), delay + 1);
    }

    private void setAmmo(Player p, Gun gun, int newAmmo) {
        int change = newAmmo - ammos.get(gun).get(p.getUniqueId());
        updateAmmo(p, gun, change);
    }

    private void updateAmmo(Player p, Gun gun, int change) {
        UUID uuid = p.getUniqueId();
        short ammo = (short) (ammos.get(gun).get(uuid) + change);
        ammos.get(gun).put(uuid, ammo);

        ItemStack ammoCounter = new ItemStack(Material.DIAMOND_HOE, 1, (short) (ammo + 1));
        ItemMeta meta = ammoCounter.getItemMeta();
        meta.spigot().setUnbreakable(true);
        ammoCounter.setItemMeta(meta);


        if (!(isSpecial(p.getInventory().getItemInOffHand()) && p.getInventory().getItemInOffHand().getDurability() <= 11)) {
            p.getInventory().addItem(p.getInventory().getItemInOffHand());
        }
        p.getInventory().setItemInOffHand(ammoCounter);
    }

    enum Gun {
        AS50(1557, 4, 6, 30, 40, "AI AS50 Sniper Rifle"), //Sniper rifle, Special: scope (slowness effect)
        M32(1558, 6, 8, 1.5, 7, "M32 40mm Grenade Launcher"), //Grenade launcher, Special: will also explode tnt, additional damage
        SPAS12(1559, 4, 3.5, 2.5, 10, "SPAS-12"), //Shotgun, Special: shoots 10 snowballs, 10dmg each
        BLUNDERBUSS(1560, 8, 1, 3, 10, "Blunderbuss"),
        MUSKET(1561, 6, 2.5, 4.5, 15, "Musket");

        private int id;
        private int maxAmmo;
        private double cooldown;
        private double speed;
        private double damage;
        private String name;

        Gun(int id, int maxAmmo, double cooldown, double speed, double damage, String name) {
            this.id = id;
            this.maxAmmo = maxAmmo;
            this.cooldown = cooldown;
            this.speed = speed;
            this.damage = damage;
            this.name = name;
        }

        public static Gun getGun(int id) {
            for (Gun gun : Gun.values()) {
                if (gun.getId() == id) {
                    return gun;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }

        public int getMaxAmmo() {
            return maxAmmo;
        }

        public float getCooldownWeapon() {
            return (float) (1 / cooldown);
        }

        public double getCooldownTicks() {
            return cooldown * 20;
        }

        public double getSpeed() {
            return speed;
        }

        public double getDamage(double distance) {
            double dmg = damage;
            double var = distance / speed;
            if (var <= 0.5) dmg = dmg * 1.75;
            if (var <= 1) dmg = dmg * 1.5;
            if (var <= 5) dmg = dmg * 1.25;
            return dmg;
        }

        public String getName() {
            return name;
        }
    }
}
