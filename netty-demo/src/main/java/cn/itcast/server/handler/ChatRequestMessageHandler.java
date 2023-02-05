package cn.itcast.server.handler;

import cn.itcast.message.ChatRequestMessage;
import cn.itcast.message.ChatResponseMessage;
import cn.itcast.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 单聊消息处理器
 *
 * @author : 其然乐衣Letitbe
 * @date : 2023/2/1
 */
@ChannelHandler.Sharable
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatRequestMessage msg) throws Exception {
        // 拿到要发送到的用户名（发给谁）
        String to = msg.getTo();
        // 通过用户名获取channel
        Channel channel = SessionFactory.getSession().getChannel(to);
        // 如果channel 不为null，表示用户在线
        if (channel != null) {
            channel.writeAndFlush(new ChatResponseMessage(msg.getFrom(), msg.getContent()));
        }
        // 不在线
        else {
            ctx.writeAndFlush(new ChatResponseMessage( false, "对方用户不存在或者不在线"));
        }
    }
}
