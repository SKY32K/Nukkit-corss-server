package com.fast.server;

import cn.nukkit.Player;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.TextFormat;

public class ServerCheckTask extends AsyncTask {
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
                try {
                    player.transfer(address, port);
                    plugin.getLogger().info(TextFormat.YELLOW + "已将玩家 " + player.getName() + " 转移到服务器 " + address + ":" + port);
                } catch (Exception e) {
                    player.sendMessage(TextFormat.RED + "转移失败，请稍后再试！");
                    player.kick(TextFormat.RED + "转移失败", false);
                    plugin.getLogger().warning(TextFormat.RED + "玩家 " + player.getName() + " 转移失败：" + e.getMessage());
                }
            } else {
                player.sendMessage(TextFormat.RED + "目标服务器当前不可用，请稍后再试！");
                player.kick(TextFormat.RED + "目标服务器离线", false);
                plugin.getLogger().warning(TextFormat.RED + "目标服务器离线，玩家 " + player.getName() + " 已被断开连接");
            }
        });
    }

    private boolean isTargetServerOnline(String address, int port) {
        try (java.net.Socket socket = new java.net.Socket()) {
            
            socket.connect(new java.net.InetSocketAddress(address, port), 1000);
            return true;
        } catch (Exception e) {
            return false; 
        }
    }
}
