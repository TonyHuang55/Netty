package netty.c3;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;

@Slf4j
public class CloseFutureClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        ChannelFuture channelFuture = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    // 连接建立后被调用
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                .connect(new InetSocketAddress("localhost", 8080));
        Channel channel = channelFuture.sync().channel();
        log.debug("{}", channel);
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                if ("q".equals(line)) {
                    channel.close();
                    // 不能在这里善后，不是一个线程
                    // log.debug("处理关闭后的操作");
                    break;
                }
                channel.writeAndFlush(line);
            }
        }, "input").start();

        // 获取 ClosedFuture 对象

        ChannelFuture closeFuture = channel.closeFuture();

        // 1)同步处理关闭
        /*log.debug("waiting close");
        closeFuture.sync();
        log.debug("处理关闭后的操作");*/

        // 2)异步处理关闭
        closeFuture.addListener((ChannelFutureListener) future -> {
            log.debug("处理关闭后的操作");
            // 不做 shutdown 操作的话，java 进程不会结束，因为 NioEventLoopGroup 中还有一些线程没有结束
            // 优雅结束：并非立即停止，而是拒绝接受新的任务，能运行完的运行完，没发的数据发完，最后停止正在运行的线程，
            group.shutdownGracefully();
        });
    }
}
