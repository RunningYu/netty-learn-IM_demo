package nettyIM.handler;

import io.netty.channel.ChannelHandlerContext;
import nettyIM.Common.Result;
import nettyIM.im.IMserver;

/**
 * @author : 其然乐衣Letitbe
 * @date : 2023/2/5
 */
public class JoinGroupHandler {
    public static void execute(ChannelHandlerContext ctx) {
        IMserver.GROUP.add(ctx.channel());
        ctx.channel().writeAndFlush(Result.success("加入系统默认群聊成功~"));
    }
}
