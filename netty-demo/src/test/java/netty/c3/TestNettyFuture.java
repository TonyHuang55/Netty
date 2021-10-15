package netty.c3;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class TestNettyFuture {
    /**
     * jdk Future
     * cancel       | 取消任务
     * isCanceled   | 任务是否取消
     * isDone       | 任务是否完成，不能区分成功失败
     * get          | 获取任务结果，阻塞等待
     * <p>
     * netty Future
     * getNow       | 获取任务结果，非阻塞，还未产生结果时返回 null
     * await        | 等待任务结束，如果任务失败，不会抛异常，而是通过 isSuccess 判断
     * sync         | 等待任务结束，如果任务失败，抛出异常
     * isSuccess    | 判断任务是否成功
     * cause        | 获取失败信息，非阻塞，如果没有失败，返回 null
     * addLinstener | 添加回调，异步接收结果
     * <p>
     * Promise
     * setSuccess   |设置成功结果
     * setFailure   |设置失败结果
     */
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        EventLoop eventLoop = group.next();
        Future<Integer> future = eventLoop.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("执行计算");
                Thread.sleep(1000);
                return 50;
            }
        });
        // 1. 同步方式
        // log.debug("等待结果");
        // log.debug("结果是{}",future.get());

        /*
        20:17:01 [DEBUG] [nioEventLoopGroup-2-1] n.c.TestNettyFuture - 执行计算
        20:17:01 [DEBUG] [main] n.c.TestNettyFuture - 等待结果
        20:17:02 [DEBUG] [main] n.c.TestNettyFuture - 结果是50
        */

        // 2. 异步方式
        future.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                log.debug("结果是{}",future.getNow());
            }
        });
        /*
        20:21:48 [DEBUG] [nioEventLoopGroup-2-1] n.c.TestNettyFuture - 执行计算
        20:21:49 [DEBUG] [nioEventLoopGroup-2-1] n.c.TestNettyFuture - 结果是50
        */
    }
}
