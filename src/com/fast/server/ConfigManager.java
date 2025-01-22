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

    /**
     * 加载配置文件
     */
    public void loadConfig() {
        // 确保插件文件夹存在
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // 保存默认配置文件（如果不存在）
        plugin.saveResource("config.yml", false);

        // 加载配置文件
        config = new Config(new File(plugin.getDataFolder(), "config.yml"), Config.YAML);
    }

    /**
     * 获取配置文件
     *
     * @return 配置文件实例
     */
    public Config getConfig() {
        return config;
    }

    /**
     * 保存配置文件
     */
    public void saveConfig() {
        config.save();
    }

    /**
     * 重新加载配置文件
     */
    public void reloadConfig() {
        config.reload();
    }
}