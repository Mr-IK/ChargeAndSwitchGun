package jp.mkserver.chargeandswitchgun;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class ChargeAndSwitchGun extends JavaPlugin {

    CASG_Event event;
    HashMap<String,ChargeYML> chargeweapons;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            return true;
        }
        if(args.length==0){
            chargeweapons.clear();
            event.cooldownweap.clear();
            for(UUID uuid:event.loadingweap.keySet()){
                Player p = Bukkit.getPlayer(uuid);
                if(p==null){
                    continue;
                }
                event.loadingweap.remove(p.getUniqueId());
                if(p.getLevel()!=100) {
                    event.cancelTask(p.getUniqueId());
                }
                p.setLevel(0);
                p.setExp(0f);
            }
            event.loadingweap.clear();
            for(String str:chargelist()){
                chargeweapons.put(str,new ChargeYML(this,str));
            }
        }
        return true;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        chargeweapons = new HashMap<>();
        event = new CASG_Event(this);
        getServer().getPluginManager().registerEvents(event,this);
        getCommand("casgreload").setExecutor(this);
        for(String str:chargelist()){
            chargeweapons.put(str,new ChargeYML(this,str));
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public List<String> chargelist() {

        List<String> list = new ArrayList<>();

        if(!getDataFolder().exists()){
            getDataFolder().mkdir();
        }

        File[] files = getDataFolder().listFiles();  // (a)
        if (files != null) {
            for (File f : files) {
                if (f.isFile()){  // (c)
                    String filename = f.getName();
                    list.add(remove_yml(filename));
                }
            }
        }

        return list;
    }

    public String remove_yml(String filename){
        if(filename.substring(0,1).equalsIgnoreCase(".")){
            return filename;
        }

        int point = filename.lastIndexOf(".");
        if (point != -1) {
            filename =  filename.substring(0, point);
        }
        return filename;
    }
}
