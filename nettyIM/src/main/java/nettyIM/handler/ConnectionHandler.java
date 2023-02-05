package nettyIM.handler;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandlerContext;
import nettyIM.command.Command;
import nettyIM.Common.Result;
import nettyIM.im.IMserver;

/**
 * @author : 其然乐衣Letitbe
 * @date : 2023/2/5
 */
public class ConnectionHandler {
    public static void execute(ChannelHandlerContext ctx, Command command) {

        // 判断是否已经有同名的人（即同一个人）已经在线
        if (IMserver.USERS.containsKey(command.getNickname())) {
            ctx.channel().writeAndFlush(Result.fail("改用户已经上线，请更换昵称后再试~"));
            ctx.channel().disconnect();
            return;
        }

        // 加到映射表里
        IMserver.USERS.put(command.getNickname(), ctx.channel());

        ctx.channel().writeAndFlush(Result.success("与服务端连接建立成功"));
        // 返回当前群聊中的所有用户
        ctx.channel().writeAndFlush(Result.success(JSON.toJSONString(IMserver.USERS.keySet())));
    }
}
