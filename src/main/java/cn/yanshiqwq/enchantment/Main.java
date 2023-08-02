package cn.yanshiqwq.enchantment;

import cn.yanshiqwq.enchantment.Listener.EnchantListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    public final String prefix = "[Enchantment] ";
    public static Main INSTANCE = null;
    @Override
    public void onEnable() {
        getLogger().info(prefix + "Plugin enabled");
        getServer().getPluginManager().registerEvents(new EnchantListener(), getInstance());
        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info(prefix + "Plugin disabled");
        INSTANCE = null;
    }

    public static Plugin getInstance() {
        return INSTANCE;
    }
}
