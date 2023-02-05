package cn.itcast.client;

import cn.itcast.message.*;
import cn.itcast.protocol.MessageCodecSharable;
import cn.itcast.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ChatClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        // 倒计时锁，设置初始计数值为1
        CountDownLatch WAIT_FOR_LOGIN = new CountDownLatch(1);
        // false-未登录  true-已登录
        AtomicBoolean LOGIN = new AtomicBoolean(false);
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(MESSAGE_CODEC);

                    // 用来判断是不是 读空闲时间过长，或 写空闲时间过长
                    // 3s 内如果没有向服务器写数据，会触发一个 IdleState#WRITER_IDLE 事件
                    // 写空闲时间一般都要比读空闲时间短1/2
                    ch.pipeline().addLast(new IdleStateHandler(0, 3, 0));
                    // ChannelDuplexHandler 可以同时作为入站和出站处理器
                    ch.pipeline().addLast(new ChannelDuplexHandler() {
                        // 用来触发特殊事件
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception{
                            IdleStateEvent event = (IdleStateEvent) evt;
                            // 触发了写空闲事件
                            if (event.state() == IdleState.WRITER_IDLE) {
                                // log.debug("3s 没有写数据了，发送一个心跳包");
                                ctx.writeAndFlush(new PingMessage());
                            }
                        }
                    });

                    // 创建一个跟业务相关得handler
                    ch.pipeline().addLast("client handler", new ChannelInboundHandlerAdapter(){

                        // 接收服务器响应消息
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.debug("msg: {}", msg);

                            if (( msg instanceof LoginResponseMessage)) {
                                LoginResponseMessage response = (LoginResponseMessage) msg;
                                if (response.isSuccess()) {
                                    // 如果登录成功了
                                    LOGIN.set( true );
                                }
                                // 唤醒 system in 线程
                                WAIT_FOR_LOGIN.countDown(); // 计数减1
                            }

                        }

                        // 在连接建立之后触发 active 事件
                        @Override
                       public void channelActive(ChannelHandlerContext ctx) throws Exception {
                           /**
                            * 我们最好是在这里启动一个新的线程，这个线程跟netty的的线程是不想关联的，
                            * 可以用来独立地去接收用户在控制台的输入，负责向服务器发送各种消息
                            *
                            * 为什么要新开一个线程？
                            * 如果在这不创建新的线程，那么你将来接收用户输入、发送消息啊用的都是nio的NioEventLoopGroup里线程
                            * 这样子不太好，而且用户的 sync 老是阻塞，而我们不能总是让用户的 sync 影响到其它nio的操作
                            */
                           new Thread(() -> {
                               Scanner scanner = new Scanner(System.in);
                               System.out.println("请输入用户名：");
                               String username = scanner.nextLine();
                               System.out.println("请输入密码：");
                               String password = scanner.nextLine();

                               // 构造消息对象
                               LoginRequestMessage message = new LoginRequestMessage(username, password);
                               // 发送消息
                               ctx.writeAndFlush(message);

                               System.out.println("等待后续操作。。。");
                               try {
                                   // 当减为0的时候，就可以向下运行了，否则就会等待
                                   WAIT_FOR_LOGIN.await();
                               } catch (InterruptedException e) {
                                   e.printStackTrace();
                               }


                               // 如果登录失败
                               if (!LOGIN.get()) {
                                   ctx.channel().close();
                                   return;
                               }
                               while ( true ) {
                                   // 编写用户可选择的菜单
                                   System.out.println("==================================");
                                   System.out.println("send [username] [content]");
                                   System.out.println("gsend [group name] [content]");
                                   System.out.println("gcreate [group name] [m1,m2,m3...]");
                                   System.out.println("gmembers [group name]");
                                   System.out.println("gjoin [group name]");
                                   System.out.println("gquit [group name]");
                                   System.out.println("quit");
                                   System.out.println("==================================");
                                   String command = scanner.nextLine();
                                   String[] s = command.split(" ");
                                   switch (s[0]){
                                       case "send":
                                           ctx.writeAndFlush(new ChatRequestMessage(username, s[1], s[2]));
                                           break;
                                       case "gsend":
                                           ctx.writeAndFlush(new GroupChatRequestMessage(username, s[1], s[2]));
                                           break;
                                       case "gcreate":
                                           Set<String> set = new HashSet<>(Arrays.asList(s[2].split(",")));
                                           set.add(username); // 加入自己
                                           ctx.writeAndFlush(new GroupCreateRequestMessage(s[1], set));
                                           break;
                                       case "gmembers":
                                           ctx.writeAndFlush(new GroupMembersRequestMessage(s[1]));
                                           break;
                                       case "gjoin":
                                           ctx.writeAndFlush(new GroupJoinRequestMessage(username, s[1]));
                                           break;
                                       case "gquit":
                                           ctx.writeAndFlush(new GroupQuitRequestMessage(username, s[1]));
                                           break;
                                       case "quit":
                                           ctx.channel().close();
                                           return;
                                   }
                               }
                           }, "system in").start();
                       }
                    });
                }
            });
            Channel channel = bootstrap.connect("localhost", 8080).sync().channel();
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("client error", e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
