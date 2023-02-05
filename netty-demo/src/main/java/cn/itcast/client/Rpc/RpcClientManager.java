package cn.itcast.client.Rpc;

import cn.itcast.message.Rpc.RpcRequestMessage;
import cn.itcast.protocol.MessageCodecSharable;
import cn.itcast.protocol.ProcotolFrameDecoder;
import cn.itcast.protocol.SequenceIdGenerator;
import cn.itcast.server.handler.Rpc.RpcResponseMessageHandler;
import cn.itcast.server.service.HelloService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

/**
 * @author : 其然乐衣Letitbe
 * @date : 2023/2/3
 */
@Slf4j
public class RpcClientManager {
    private static Channel channel = null;
    public static void main(String[] args) {
        HelloService service = getProxyService(HelloService.class);
        System.out.println(service.sayHello("zhangsan"));
        System.out.println(service.sayHello("lisi"));
        System.out.println(service.sayHello("wangwu"));

    }

    /**
     * 创建一个代理类
     * 实现远程调用的接口，代理类里所有方法的调用，都会多做一件事，就是把本身方法的调用转化成 rpc 的请求消息，
     * 再由代理类去调用channel去发送消息，这样就把复杂的过程屏蔽起来了
     */
    public static <T> T getProxyService(Class<T> serviceClass) {
        // 获取类加载器
        ClassLoader loader = serviceClass.getClassLoader();
        // 代理类要实现的接口
        Class<?>[] interfaces = new Class[]{serviceClass};

        //                      （proxy：代理对象  method：代理类正在执行的方法  args：方法的实际参数）
        // 代理类里的任何一个方法的调用都会进入这个 (proxy, method,args)->{} lambda表达式
        Object o = Proxy.newProxyInstance(loader, interfaces, (proxy, method, args) -> {
            // 1. 将方法调用转换为 消息对象
            int sequenceId = SequenceIdGenerator.nextId();
            RpcRequestMessage msg = new RpcRequestMessage(
                    sequenceId,  // 让序列号id唯一
                    serviceClass.getName(),        // 接口类名
                    method.getName(),
                    method.getReturnType(),
                    method.getParameterTypes(),    // 方法的参数类型
                    args                           // 方法的参数
            );
            // 2. 将消息对象发送出去
            getChannel().writeAndFlush(msg);

            // 3. 准备一个空 Promise 对象，来接收结果            指定 promise 对象异步接收的结果线程
            DefaultPromise<Object> promise = new DefaultPromise<>(getChannel().eventLoop());
            RpcResponseMessageHandler.PROMISES.put(sequenceId, promise);

            // 4. 等来 promise 的结果
            // ( 这个结果其实就是 异步的网络调用，通过同步的方式来等待结果，其中用到了promise )
            // 因为要等promise中有结果了才返回，所以这里要用promise的同步方法
            // await()将来无论有成功失败都不会抛异常，而sync()失败会抛异常
            promise.await();
            if (promise.isSuccess()) {
                // 调用正常
                return promise.getNow();
            } else {
                // 调用失败
                throw new RuntimeException(promise.cause());
            }
        });
        return (T) o;
    }


    /**
     * 用单例模式来 实现 channel 只被初始化一次
     */
    private static final Object LOCK = new Object();
    /**
     * 获取唯一的channel对像
     */
    public static Channel getChannel() {
        if (channel != null) {
            return channel;
        }
        initChannel();
        return channel;
    }

    /**
     * 初始化 channel 方法
     */
    private static void initChannel() {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();

        // rpc 响应消息处理器，待实现
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(group);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ProcotolFrameDecoder());
                ch.pipeline().addLast(LOGGING_HANDLER);
                ch.pipeline().addLast(MESSAGE_CODEC);
                ch.pipeline().addLast(RPC_HANDLER);
            }
        });

        try {
            channel = bootstrap.connect("localhost", 8080).sync().channel();

            // 这里必须用异步，否则 getChannel 永远返回不了而一直处于阻塞直达 channel 关闭
            channel.closeFuture().addListener(future -> {
                group.shutdownGracefully();
            });
        } catch (Exception e) {
            log.error("client error", e);
        }
    }
}