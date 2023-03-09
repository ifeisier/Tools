package com.example.demo.netty4;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象的 netty 服务类
 */
public abstract class AbstractNettyServer {

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private ServerBootstrap bootstrap;

    /**
     * the boss channel that receive connections and dispatch these to worker channel.
     */
    private Channel channel;

    /**
     * 缓存 worker channel.
     * <ip:port, channel>
     */
    private final Map<String, Channel> channels = new ConcurrentHashMap<>();


    /**
     * 关闭服务
     */
    public void close() {
        channels.forEach((key, channel) -> channel.close());
        channels.clear();

        if (channel != null) {
            channel.close();
        }

        if (bootstrap != null) {
            Future<?> bossGroupShutdownFuture = bossGroup.shutdownGracefully();
            Future<?> workerGroupShutdownFuture = workerGroup.shutdownGracefully();
            bossGroupShutdownFuture.syncUninterruptibly();
            workerGroupShutdownFuture.syncUninterruptibly();
        }
    }


    /**
     * 启动服务
     *
     * @param port 服务端口
     */
    public void start(int port) {
        bossGroup = NettyEventLoopFactory.eventLoopGroup(1, "NettyServerBoss");
        int workerThreads = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);
        workerGroup = NettyEventLoopFactory.eventLoopGroup(workerThreads, "NettyServerWorker");

        bootstrap = new ServerBootstrap();
        initServerBootstrap();

        ChannelFuture channelFuture = bootstrap.bind(port);
        channelFuture.syncUninterruptibly();
        channel = channelFuture.channel();
    }


    /**
     * 添加自定义的 ChannelHandler 到 ChannelPipeline。
     * <p>
     * 添加了一个 60s 秒的超时读检查，但是需要自己添加一个 NettyHeartbeat 实现类。
     * <p>
     * 该方法会在 initChannel(SocketChannel ch) 方法中调用。
     *
     * @param pipeline 会添加到这个 ChannelPipeline 中。
     */
    protected abstract void addChannelHandler(final ChannelPipeline pipeline);


    private void initServerBootstrap() {
        bootstrap.group(bossGroup, workerGroup)
                .channel(NettyEventLoopFactory.serverSocketChannelClass())
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {

                        ch.pipeline().addLast("server-idle-handler",
                                new IdleStateHandler(10, 0, 0));

                        addChannelHandler(ch.pipeline());

                        ch.pipeline().addLast("connect-handler", new ConnectHandler());
                    }
                });
    }


    // 获取远程IP和端口
    private class ConnectHandler extends ChannelDuplexHandler {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            channels.put(getRemoteAddress(ctx), ctx.channel());
            ctx.fireChannelActive();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            channels.remove(getRemoteAddress(ctx));
            ctx.fireChannelInactive();
        }

        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            channels.remove(getRemoteAddress(ctx));
            ctx.close(promise);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            channels.remove(getRemoteAddress(ctx));
            ctx.fireExceptionCaught(cause);
        }

        private String getRemoteAddress(ChannelHandlerContext ctx) {
            InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            String hostAddress = socketAddress.getAddress().getHostAddress();
            int port = socketAddress.getPort();
            return hostAddress + ":" + port;
        }
    }

}
