package netty.c4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

public class TestByteBuf {
    /*
    * 可以使用下面的代码来创建池化基于堆的 ByteBuf : ByteBuf buffer = ByteBufAllocator.DEFAULT.heapBuffer(10);
    * 也可以使用下面的代码来创建池化基于直接内存的 ByteBuf : ByteBuf buffer = ByteBufAllocator.DEFAULT.directBuffer(10);
    *
    * 直接内存创建和销毁的代价昂贵，但读写性能高（少一次内存复制），适合配合池化功能一起用
    * 直接内存对 GC 压力小，因为这部分内存不受 JVM 垃圾回收的管理，但也要注意及时主动释放
    *
    */

    /*
    * 池化的最大意义在于可以重用 ByteBuf，优点有
    *   没有池化，则每次都得创建新的 ByteBuf 实例，这个操作对直接内存代价昂贵，就算是堆内存，也会增加 GC 压力
    *   有了池化，则可以重用池中 ByteBuf 实例，并且采用了与 jemalloc 类似的内存分配算法提升分配效率
    *   高并发时，池化功能更节约内存，减少内存溢出的可能
    *
    * 池化功能是否开启，可以通过下面的系统环境变量来设置
    * -Dio.netty.allocator.type={unpooled|pooled}
    *
    * 4.1 以后，非 Android 平台默认启用池化实现，Android 平台启用非池化实现
    * 4.1 之前，池化功能还不成熟，默认是非池化实现
    *
    * */

    public static void main(String[] args) {
        /*
         * 默认 256，最大容量是 int 的最大值。可以动态扩容
         * 扩容规则：
         *      如果写入后数据大小未超过 512，则选择下一个 16 的整数倍，例如写入后大小为 12 ，则扩容后 capacity 是 16
         *      如果写入后数据大小超过 512，则选择下一个 2^n，例如写入后大小为 513，则扩容后 capacity 是 2^10=1024（2^9=512 已经不够了）
         *      扩容不能超过 max capacity 会报错
         */
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        // PooledUnsafeDirectByteBuf(ridx: 0, widx: 0, cap: 256)
        log(buf);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            sb.append("a");
        }
        buf.writeBytes(sb.toString().getBytes());
        // PooledUnsafeDirectByteBuf(ridx: 0, widx: 300, cap: 512)
        log(buf);
        /*
         * read index:0 write index:0 capacity:256
         *
         * read index:0 write index:300 capacity:512
         *          +-------------------------------------------------+
         *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
         * +--------+-------------------------------------------------+----------------+
         * |00000000| 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 |aaaaaaaaaaaaaaaa|
         * |00000010| 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 |aaaaaaaaaaaaaaaa|
         * |00000020| 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 |aaaaaaaaaaaaaaaa|
         * |00000030| 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 |aaaaaaaaaaaaaaaa|
         * |00000040| 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 |aaaaaaaaaaaaaaaa|
         * |00000050| 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 |aaaaaaaaaaaaaaaa|
         * |00000060| 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 |aaaaaaaaaaaaaaaa|
         * |00000070| 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 |aaaaaaaaaaaaaaaa|
         * |00000080| 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 |aaaaaaaaaaaaaaaa|
         * |00000090| 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 |aaaaaaaaaaaaaaaa|
         * |000000a0| 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 |aaaaaaaaaaaaaaaa|
         * |000000b0| 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 |aaaaaaaaaaaaaaaa|
         * |000000c0| 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 |aaaaaaaaaaaaaaaa|
         * |000000d0| 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 |aaaaaaaaaaaaaaaa|
         * |000000e0| 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 |aaaaaaaaaaaaaaaa|
         * |000000f0| 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 |aaaaaaaaaaaaaaaa|
         * |00000100| 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 |aaaaaaaaaaaaaaaa|
         * |00000110| 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 61 |aaaaaaaaaaaaaaaa|
         * |00000120| 61 61 61 61 61 61 61 61 61 61 61 61             |aaaaaaaaaaaa    |
         * +--------+-------------------------------------------------+----------------+
         * */
    }

    private static void log(ByteBuf buffer) {
        int length = buffer.readableBytes();
        int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
        StringBuilder buf = new StringBuilder(rows * 80 * 2)
                .append("read index:").append(buffer.readerIndex())
                .append(" write index:").append(buffer.writerIndex())
                .append(" capacity:").append(buffer.capacity())
                .append(NEWLINE);
        appendPrettyHexDump(buf, buffer);
        System.out.println(buf.toString());
    }
}
