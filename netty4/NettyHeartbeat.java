package com.example.demo.netty4;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 心跳超时处理类
 */
public abstract class NettyHeartbeat extends ChannelInboundHandlerAdapter {

    public abstract void handler(ChannelHandlerContext ctx, IdleStateEvent evt);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        handler(ctx, (IdleStateEvent) evt);
        ctx.fireUserEventTriggered(evt);
    }
}
