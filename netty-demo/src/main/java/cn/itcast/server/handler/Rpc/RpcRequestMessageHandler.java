package cn.itcast.server.handler.Rpc;

import cn.itcast.message.Rpc.RpcRequestMessage;
import cn.itcast.message.Rpc.RpcResponseMessage;
import cn.itcast.server.service.HelloService;
import cn.itcast.server.service.Rpc.ServicesFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 服务器端的代码只要是发射调用
 * @author : 其然乐衣Letitbe
 * @date : 2023/2/3
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage message) {
        RpcResponseMessage response = new RpcResponseMessage();
        // 将请求消息中把 序列号id 放入响应消息中
        response.setSequenceId(message.getSequenceId());
        try {
            // 获取真正的实现对象
            HelloService service = (HelloService)
                    ServicesFactory.getService(Class.forName(message.getInterfaceName()));

            // 获取要调用的方法
            Method method = service.getClass().getMethod(message.getMethodName(), message.getParameterTypes());

            // 调用方法
            Object invoke = method.invoke(service, message.getParameterValue());
            // 调用成功
            // 将返回结果放入response
            response.setReturnValue(invoke);
        } catch (Exception e) {
                    e.printStackTrace();
            // 调用异常
            String errMsg = e.getCause().getMessage();
            response.setExceptionValue(new Exception("远程调用出错" + errMsg));
        }
        // 返回结果
        ctx.writeAndFlush(response);
        // 这个response响应对象，会由我们设置的编解码器进行编码，编码编程 bytebuf, 最后bytebuf变成字节，经过网络返回给客户端，这样的调用就完成了
    }

    public static void main(String[] args) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        RpcRequestMessage message = new RpcRequestMessage(
                1,
                "cn.itcast.server.service.HelloService",
                "sayHello",
                String.class,
                new Class[]{String.class},
                new Object[]{"张三"}
        );
        HelloService service = (HelloService) ServicesFactory.getService(Class.forName(message.getInterfaceName()));
        Method method = service.getClass().getMethod(message.getMethodName(), message.getParameterTypes());
        Object invoke = method.invoke(service, message.getParameterValue());
        System.out.println(invoke);
        // 打印出 “你好，张三”
    }

}