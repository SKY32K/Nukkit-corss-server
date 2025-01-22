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
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        this.targetServerAddress = this.getConfig().getString("target-server.address", "127.0.0.1");
        this.targetServerPort = this.getConfig().getInt("target-server.port", 19132);

        this.getServer().getPluginManager().registerEvents(this, this);

        this.getLogger().info(TextFormat.GREEN + "AutoTransferPlugin 已启用！");
        this.getLogger().info(TextFormat.GREEN + "玩家將轉發到" + this.targetServerAddress + ":" + this.targetServerPort);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        this.getServer().getScheduler().scheduleAsyncTask(this, new ServerCheckTask(this, player, targetServerAddress, targetServerPort));
    }

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

            plugin.getServer().getScheduler().scheduleTask(plugin, () -> {
                if (isServerOnline) {
                    player.transfer(address, port);
                    plugin.getLogger().info(TextFormat.YELLOW + "已将玩家 " + player.getName() + " 转移到服务器 " + address + ":" + port);
                } else {
                    player.sendMessage(TextFormat.RED + "目标服务器当前不可用，请稍后再试！");
                    player.kick(TextFormat.RED + "目标服务器离线", false);
                    plugin.getLogger().warning(TextFormat.RED + "目标服务器离线，玩家 " + player.getName() + " 已被断开连接");
                }
            });
        }

        /**
         *
         * @param address 目标服务器地址
         * @param port    目标服务器端口
         * @return 是否在线
         */
        private boolean isTargetServerOnline(String address, int port) {
            try (java.net.Socket socket = new java.net.Socket()) {
                socket.connect(new java.net.InetSocketAddress(address, port), 1000);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
