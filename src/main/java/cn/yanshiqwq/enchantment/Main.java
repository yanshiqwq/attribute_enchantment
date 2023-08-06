package cn.yanshiqwq.enchantment;

import cn.yanshiqwq.enchantment.Listener.AnvilListener;
import cn.yanshiqwq.enchantment.Listener.EnchantListener;
import cn.yanshiqwq.enchantment.Listener.InventoryClickListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    public final String prefix = "[Enchantment] ";
    public static Main INSTANCE = null;
    @Override
    public void onEnable() {
        INSTANCE = this;
        getLogger().info(prefix + "Plugin enabled");
        getServer().getPluginManager().registerEvents(new AnvilListener(), getInstance());
        getServer().getPluginManager().registerEvents(new EnchantListener(), getInstance());
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), getInstance());
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
