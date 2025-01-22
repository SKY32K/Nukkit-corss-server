package com.fast.server;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;

import java.io.File;

public class ConfigManager {
    private final PluginBase plugin;
    private Config config;

    public ConfigManager(PluginBase plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        plugin.saveResource("config.yml", false);

        config = new Config(new File(plugin.getDataFolder(), "config.yml"), Config.YAML);
    }

    public Config getConfig() {
        return config;
    }

    public void saveConfig() {
        config.save();
    }

    public void reloadConfig() {
        config.reload();
    }
}
