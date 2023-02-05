package nettyIM.handler;

import com.alibaba.fastjson2.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import nettyIM.command.Command;
import nettyIM.enums.CommandType;
import nettyIM.Common.Result;

/**
 * @author : 其然乐衣Letitbe
 * @date : 2023/2/5
 */
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        System.out.println(frame.text());
        try {
            Command command = JSON.parseObject(frame.text(), Command.class);
            switch (CommandType.match(command.getCode())) {
                case CONNECTION : ConnectionHandler.execute(ctx, command);
                case CHAT : ChatHandler.execute(ctx, frame);
                case JOIN_GROUP : JoinGroupHandler.execute(ctx);
                default : ctx.channel().writeAndFlush(Result.fail("不支持的CODE"));
            }
        } catch (Exception e) {
            ctx.channel().writeAndFlush(Result.fail(e.getMessage()));
        }


    }
}
