package netty.c3;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class TestJdkFuture {
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
        // 1. 线程池
        ExecutorService service = Executors.newFixedThreadPool(2);
        // 2. 提交任务
        Future<Integer> future = service.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("执行计算");
                Thread.sleep(1000);
                return 50;
            }
        });
        // 3. 主线程通过 future 来获取结果
        future.get();
        log.debug("等待结果");
        log.debug("结果是{}",future.get());
        /*
        20:17:41 [DEBUG] [pool-1-thread-1] n.c.TestJdkFuture - 执行计算
        20:17:42 [DEBUG] [main] n.c.TestJdkFuture - 等待结果
        20:17:42 [DEBUG] [main] n.c.TestJdkFuture - 结果是50
        */
    }
}
