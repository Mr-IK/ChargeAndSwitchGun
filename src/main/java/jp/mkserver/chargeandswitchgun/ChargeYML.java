package jp.mkserver.chargeandswitchgun;

import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class ChargeYML {

    ChargeAndSwitchGun plugin;
    String name;
    String itemname;
    String csname;
    String chargesound;
    String completesound;
    int chargetick;
    int chargeper;
    int cooldowntick;

    public ChargeYML(ChargeAndSwitchGun plugin,String name) {
        this.plugin = plugin;
        this.name = name;
        loadFile();
    }

    public void loadFile(){
        File f = new File(plugin.getDataFolder(), File.separator + name + ".yml");
        FileConfiguration data = YamlConfiguration.loadConfiguration(f);

        if (f.exists()) {
            itemname = data.getString("itemname");
            csname = data.getString("csname");
            chargesound = data.getString("chargesound");
            completesound = data.getString("completesound");
            chargetick = data.getInt("chargetick");
            chargeper = data.getInt("chargeper");
            cooldowntick = data.getInt("cooldowntick");
        }
    }

    public void playChargeSound(Player p){
        if(chargesound.equalsIgnoreCase("none")){
            return;
        }
        String[] arg = chargesound.split(":");
        Sound sound = Sound.valueOf(arg[0]);
        p.playSound(p.getLocation(),sound,Float.parseFloat(arg[1]),Float.parseFloat(arg[2]));
    }

    public void playCompleteSound(Player p){
        if(completesound.equalsIgnoreCase("none")){
            return;
        }
        String[] arg = completesound.split(":");
        Sound sound = Sound.valueOf(arg[0]);
        p.playSound(p.getLocation(),sound,Float.parseFloat(arg[1]),Float.parseFloat(arg[2]));
    }
}
