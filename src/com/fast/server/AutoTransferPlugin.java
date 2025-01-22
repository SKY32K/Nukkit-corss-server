package com.fast.server;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.scheduler.AsyncTask;

public class AutoTransferPlugin extends PluginBase implements Listener {

    private ConfigManager configManager;
    private String targetServerAddress;
    private int targetServerPort;

    @Override
    public void onEnable() {
        // 从配置文件加载目标服务器的地址和端口
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        this.targetServerAddress = this.getConfig().getString("target-server.address", "awa.freeserver.tw");
        this.targetServerPort = this.getConfig().getInt("target-server.port", 22174);

        // 注册事件监听器
        this.getServer().getPluginManager().registerEvents(this, this);

        this.getLogger().info(TextFormat.GREEN + "AutoTransferPlugin 已启用！");
        this.getLogger().info(TextFormat.GREEN + "玩家將轉發到" + this.targetServerAddress + ":" + this.targetServerPort);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 异步检查目标服务器是否在线
        this.getServer().getScheduler().scheduleAsyncTask(this, new ServerCheckTask(this, player, targetServerAddress, targetServerPort));
    }

    /**
     * 静态内部类：用于异步检查服务器状态
     */
    private static class ServerCheckTask extends AsyncTask {
        private final AutoTransferPlugin plugin;
        private final Player player;
        private final String address;
        private final int port;

        public ServerCheckTask(AutoTransferPlugin plugin, Player player, String address, int port) {
            this.plugin = plugin;
            this.player = player;
            this.address = address;
            this.port = port;
        }

        @Override
        public void onRun() {
            boolean isServerOnline = isTargetServerOnline(address, port);

            // 回到主线程处理玩家
            plugin.getServer().getScheduler().scheduleTask(plugin, () -> {
                if (isServerOnline) {
                    // 如果服务器在线，转移玩家
                    player.transfer(address, port);
                    plugin.getLogger().info(TextFormat.YELLOW + "已将玩家 " + player.getName() + " 转移到服务器 " + address + ":" + port);
                } else {
                    // 如果服务器离线，通知玩家并断开连接
                    player.sendMessage(TextFormat.RED + "目标服务器当前不可用，请稍后再试！");
                    player.kick(TextFormat.RED + "目标服务器离线", false);
                    plugin.getLogger().warning(TextFormat.RED + "目标服务器离线，玩家 " + player.getName() + " 已被断开连接");
                }
            });
        }

        /**
         * 检查目标服务器是否在线
         *
         * @param address 目标服务器地址
         * @param port    目标服务器端口
         * @return 是否在线
         */
        private boolean isTargetServerOnline(String address, int port) {
            try (java.net.Socket socket = new java.net.Socket()) {
                socket.connect(new java.net.InetSocketAddress(address, port), 1000); // 1秒超时
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
/*
package com.fast.server;

import cn.nukkit.plugin.PluginBase;

public class AutoTransferPlugin extends PluginBase {

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        // 初始化配置管理器
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // 获取配置文件
        String targetAddress = configManager.getConfig().getString("target-server.address", "127.0.0.1");
        int targetPort = configManager.getConfig().getInt("target-server.port", 19132);

        this.getLogger().info("目标服务器地址: " + targetAddress);
        this.getLogger().info("目标服务器端口: " + targetPort);

        this.getLogger().info("AutoTransferPlugin 已启用！");
    }
}*/