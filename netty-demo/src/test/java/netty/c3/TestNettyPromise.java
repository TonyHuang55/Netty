package netty.c3;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

@Slf4j
public class TestNettyPromise {
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
        // 1. 准备 EventLoop 对象
        EventLoop eventLoop = new NioEventLoopGroup().next();

        // 2. 可以主动创建 promise，结果容器
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);

        new Thread(() -> {
            // 3. 任意一个线程执行计算，计算完毕后 向 promise 填充结果
            log.debug("开始计算...");
            try {
                int i = 1 / 0;
                Thread.sleep(1000);
                promise.setSuccess(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
                promise.setFailure(e);
            }
        }).start();

        // 4. 填充结果的线程
        log.debug("等待结果");
        log.debug("结果是{}", promise.get());
        /*
        20:31:43 [DEBUG] [main] n.c.TestNettyPromise - 等待结果
        20:31:43 [DEBUG] [Thread-0] n.c.TestNettyPromise - 开始计算...
        20:31:44 [DEBUG] [main] n.c.TestNettyPromise - 结果是50
        */

        /*
        20:33:05 [DEBUG] [Thread-0] n.c.TestNettyPromise - 开始计算...
        20:33:05 [DEBUG] [main] n.c.TestNettyPromise - 等待结果
        Exception in thread "Thread-0" java.lang.ArithmeticException: / by zero
            at netty.c3.TestNettyPromise.lambda$main$0(TestNettyPromise.java:46)
            at java.lang.Thread.run(Thread.java:745)
        */
    }
}
