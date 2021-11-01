package protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;
import message.Message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

@Slf4j
@ChannelHandler.Sharable
/**
 * 必须和 LengthFieldBasedFrameDecoder 一起使用，确保接到的 ByteBuf 消息是完整的
 */
public class MessageCodecSharable extends MessageToMessageCodec<ByteBuf, Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> outlist) throws Exception {
        ByteBuf out = ctx.alloc().buffer();
        // 魔数，用来在第一时间判定是否是无效数据包
        // 1. 4 个字节的魔数
        out.writeBytes(new byte[]{1, 2, 3, 4});
        // 版本号，可以支持协议的升级
        // 2. 1 个字节的版本号
        out.writeByte(1);
        // 序列化算法，消息正文到底采用哪种序列化反序列化方法，可以由此拓展，例如：json、protobuf、hessian、jdk
        // 3. 1 个字节代表序列化的方式，jdk：0   json：1
        out.writeByte(0);
        // 指令类型，是登陆、注册、单聊、群聊...跟业务相关
        // 4. 1 个字节的指令类型
        out.writeByte(msg.getMessageType());
        // 请求序号，为了双工通信，提供异步能力
        // 5. 4 个字节的请求序号
        out.writeInt(msg.getSequenceId());
        // 无意义，对齐填充
        out.writeByte(0xff);
        // 6. 获取内容的字节数组
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        byte[] bytes = bos.toByteArray();
        // 正文长度
        // 7. 长度
        out.writeInt(bytes.length);
        // 消息正文
        // 8.写入内容
        out.writeBytes(bytes);
        outlist.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magicNum = in.readInt();
        byte version = in.readByte();
        byte serializerType = in.readByte();
        byte messageType = in.readByte();
        int sequenceId = in.readInt();
        in.readByte();
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Message message = (Message) ois.readObject();
        log.debug("{},{},{},{},{},{}", magicNum, version, serializerType, messageType, sequenceId, length);
        log.debug("{}", message);
        out.add(message);
    }
}
