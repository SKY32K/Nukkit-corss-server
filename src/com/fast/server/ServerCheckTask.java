package com.fast.server;

import cn.nukkit.Player;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.TextFormat;

public class ServerCheckTask extends AsyncTask {
    private final AutoTransferPlugin plugin; // 插件实例
    private final Player player; // 玩家实例
    private final String address; // 目标服务器地址
    private final int port; // 目标服务器端口

    /**
     * 构造函数
     *
     * @param plugin  插件实例
     * @param player  玩家实例
     * @param address 目标服务器地址
     * @param port    目标服务器端口
     */
    public ServerCheckTask(AutoTransferPlugin plugin, Player player, String address, int port) {
        this.plugin = plugin;
        this.player = player;
        this.address = address;
        this.port = port;
    }

    @Override
    public void onRun() {
        // 检查目标服务器是否在线
        boolean isServerOnline = isTargetServerOnline(address, port);

        // 回到主线程处理玩家
        plugin.getServer().getScheduler().scheduleTask(plugin, () -> {
            if (isServerOnline) {
                // 如果服务器在线，尝试转移玩家
                try {
                    player.transfer(address, port);
                    plugin.getLogger().info(TextFormat.YELLOW + "已将玩家 " + player.getName() + " 转移到服务器 " + address + ":" + port);
                } catch (Exception e) {
                    // 转移失败，恢复玩家状态
                    player.sendMessage(TextFormat.RED + "转移失败，请稍后再试！");
                    player.kick(TextFormat.RED + "转移失败", false);
                    plugin.getLogger().warning(TextFormat.RED + "玩家 " + player.getName() + " 转移失败：" + e.getMessage());
                }
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
            // 尝试连接目标服务器，设置超时时间为 1 秒
            socket.connect(new java.net.InetSocketAddress(address, port), 1000);
            return true; // 连接成功，服务器在线
        } catch (Exception e) {
            return false; // 连接失败，服务器离线
        }
    }
}