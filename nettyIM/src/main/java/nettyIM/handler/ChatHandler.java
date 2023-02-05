package nettyIM.handler;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.internal.StringUtil;
import nettyIM.Common.Result;
import nettyIM.command.ChatMessage;
import nettyIM.enums.MessageType;
import nettyIM.im.IMserver;

/**
 * @author : 其然乐衣Letitbe
 * @date : 2023/2/5
 */
public class ChatHandler {


    public static void execute(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        try {
            ChatMessage chat = JSON.parseObject(frame.text(), ChatMessage.class);
            switch (MessageType.match(chat.getType())) {
                // 私聊消息
                case PRIVATE : {
                    if (StringUtil.isNullOrEmpty(chat.getTarget())) {
                        ctx.channel().writeAndFlush(Result.fail("消息发送失败，发送消息前请指定接收对象"));
                        return;
                    }
                    Channel channel = IMserver.USERS.get(chat.getTarget());
                    if (null == channel || !channel.isActive()) {
                        ctx.channel().writeAndFlush(Result.fail("消息发送失败，对方" + chat.getTarget() + "不在线"));
                    } else {
                        channel.writeAndFlush(Result.success("私聊消息（" + chat.getNickname() + ")", chat.getContent()));
                    }
                }
                // 群聊消息   ChannelGroup 发送消息，会给每一个注册进 ChannelGroup 中的channel发送消息
                case GROUP : IMserver.GROUP.writeAndFlush(Result.success("群消息 发送者(" + chat.getNickname() + ")", chat.getContent()));
                default : ctx.channel().writeAndFlush(Result.fail("不支持消息类型"));
            }
        } catch (Exception e) {
            ctx.channel().writeAndFlush(Result.fail("发送消息格式错误，请确认后再试"));
        }
    }
}
