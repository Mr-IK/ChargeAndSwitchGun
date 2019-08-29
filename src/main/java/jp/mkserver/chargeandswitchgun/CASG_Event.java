package jp.mkserver.chargeandswitchgun;

import com.shampaggon.crackshot.events.WeaponPrepareShootEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CASG_Event implements Listener {

    ChargeAndSwitchGun plugin;
    HashMap<UUID, Integer> tasks;
    HashMap<UUID, String> loadingweap;
    List<String> cooldownweap;


    public CASG_Event(ChargeAndSwitchGun plugin){
        this.plugin = plugin;
        tasks = new HashMap<>();
        loadingweap = new HashMap<>();
        cooldownweap = new ArrayList<>();
    }

    public void cancelTask(UUID uuid){
        Bukkit.getScheduler().cancelTask(tasks.get(uuid));
        tasks.remove(uuid);
    }

    public int startTask(Player p,String id){
        loadingweap.put(p.getUniqueId(),id);
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                int add = plugin.chargeweapons.get(id).chargeper;
                if(p.getLevel()+add>100){
                    p.setLevel(100);
                    p.setExp(1);
                    plugin.chargeweapons.get(id).playCompleteSound(p);
                    tasks.remove(p.getUniqueId());
                    cancel();
                    return;
                }
                if(!CrackShotAPI.validHotbar(p,plugin.chargeweapons.get(id).group)){
                    loadingweap.remove(p.getUniqueId());
                    p.setLevel(0);
                    p.setExp(0f);
                    tasks.remove(p.getUniqueId());
                    cancel();
                    return;
                }
                p.setLevel(p.getLevel()+add);
                BigDecimal bd3 = new BigDecimal(add);
                BigDecimal bd4 = new BigDecimal("100");
                BigDecimal result2 = bd3.divide(bd4, 2, BigDecimal.ROUND_HALF_UP);
                p.setExp(p.getExp()+result2.floatValue());
                plugin.chargeweapons.get(id).playChargeSound(p);
            }
        };
        BukkitTask tas = task.runTaskTimerAsynchronously(plugin,plugin.chargeweapons.get(id).chargetick,plugin.chargeweapons.get(id).chargetick);
        tasks.put(p.getUniqueId(),tas.getTaskId());
        return tas.getTaskId();
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent e){
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItem(e.getNewSlot());
        if(loadingweap.containsKey(p.getUniqueId())){
            loadingweap.remove(p.getUniqueId());
            if(p.getLevel()!=100) {
                cancelTask(p.getUniqueId());
            }
            p.setLevel(0);
            p.setExp(0f);
        }
        if(item==null){
            return;
        }
        if(item.getItemMeta()==null||item.getItemMeta().getDisplayName()==null){
            return;
        }
        for(ChargeYML yml: plugin.chargeweapons.values()){
            if(item.getItemMeta().getDisplayName().equals(yml.itemname)){
                if(cooldownweap.contains(p.getUniqueId().toString()+" : "+yml.name)){
                    continue;
                }
                if(!CrackShotAPI.validHotbar(p,yml.group)){
                    continue;
                }
                startTask(p,yml.name);
                return;
            }
        }
    }

    @EventHandler
    public void onPreWeaponShoot(WeaponPrepareShootEvent e){
        Player p = e.getPlayer();
        String group = CrackShotAPI.director.returnParentNode(p);
        String groups = CrackShotAPI.director.getString(group + ".Item_Information.Inventory_Control");
        if(!CrackShotAPI.validHotbar(p,groups)){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        Player p = (Player)e.getWhoClicked();
        if(p.getInventory().getType()== InventoryType.PLAYER&&e.getSlot()==p.getInventory().getHeldItemSlot()){
            if(loadingweap.containsKey(p.getUniqueId())){
                loadingweap.remove(p.getUniqueId());
                if(p.getLevel()!=100) {
                    cancelTask(p.getUniqueId());
                }
                p.setLevel(0);
                p.setExp(0f);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e){
        Player p = (Player)e.getPlayer();
        if(loadingweap.containsKey(p.getUniqueId())){
            return;
        }
        ItemStack item = p.getInventory().getItemInMainHand();
        if(item==null){
            return;
        }
        if(item.getItemMeta()==null||item.getItemMeta().getDisplayName()==null){
            return;
        }
        for(ChargeYML yml: plugin.chargeweapons.values()){
            if(item.getItemMeta().getDisplayName().equals(yml.itemname)){
                if(cooldownweap.contains(p.getUniqueId().toString()+" : "+yml.name)){
                    continue;
                }
                if(!CrackShotAPI.validHotbar(p,yml.group)){
                    continue;
                }
                startTask(p,yml.name);
                return;
            }
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e){
        Player p = e.getPlayer();
        if(loadingweap.containsKey(p.getUniqueId())&&p.getLevel()==100&& (e.getAction()==Action.RIGHT_CLICK_AIR||e.getAction()==Action.RIGHT_CLICK_BLOCK)){
            String id = loadingweap.get(p.getUniqueId());
            if(!CrackShotAPI.validHotbar(p,plugin.chargeweapons.get(id).group)){
                return;
            }
            loadingweap.remove(p.getUniqueId());
            p.setLevel(0);
            p.setExp(0f);
            ItemStack items = p.getInventory().getItemInMainHand();
            p.getInventory().setItemInMainHand(null);
            CrackShotAPI.fire(p,plugin.chargeweapons.get(id).csname,true);
            p.getInventory().setItemInMainHand(items);
            cooldownweap.add(p.getUniqueId().toString()+" : "+plugin.chargeweapons.get(id).name);
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,()->{
                cooldownweap.remove(p.getUniqueId().toString()+" : "+plugin.chargeweapons.get(id).name);
                if(loadingweap.containsKey(p.getUniqueId())){
                    return;
                }
                ItemStack item = p.getInventory().getItemInMainHand();
                if(item==null){
                    return;
                }
                if(item.getItemMeta()==null||item.getItemMeta().getDisplayName()==null){
                    return;
                }
                for(ChargeYML yml: plugin.chargeweapons.values()){
                    if(item.getItemMeta().getDisplayName().equals(yml.itemname)){
                        if(cooldownweap.contains(p.getUniqueId().toString()+" : "+yml.name)){
                            continue;
                        }
                        if(!CrackShotAPI.validHotbar(p,yml.group)){
                            continue;
                        }
                        startTask(p,yml.name);
                        return;
                    }
                }
            },plugin.chargeweapons.get(id).cooldowntick);
        }
    }


}
