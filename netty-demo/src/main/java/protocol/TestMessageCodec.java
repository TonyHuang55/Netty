package protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import message.LoginRequestMessage;

public class TestMessageCodec {
    public static void main(String[] args) throws Exception {
        // 注：没有状态信息的 Handler 是线程安全的。@Sharable 注解意味着该 Handler 可以在多线程下共享使用，只要创建一个示例就足够了。
        LoggingHandler LOGGING_HANDLER = new LoggingHandler();
        EmbeddedChannel channel = new EmbeddedChannel(
                LOGGING_HANDLER,
                // 黏包半包，一般解码器类都不是 @Shareable
                new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0),
                new MessageCodec());

        // encode
        LoginRequestMessage message = new LoginRequestMessage("zhangsan", "123", "张三");
        channel.writeOutbound(message);

        // decode
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null, message, buf);

        // 黏包半包测试
        ByteBuf s1 = buf.slice(0, 100);
        ByteBuf s2 = buf.slice(100, buf.readableBytes() - 100);

        // 应用技术变为 2
        s1.retain();
        // release 引用计数变为 0
        channel.writeInbound(s1);
        channel.writeInbound(s2);
    }
}
