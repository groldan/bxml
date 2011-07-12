package org.gvsig.bxml.stream.io;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

class ByteBufferPool {

    private static final int MIN_CAPACITY = 1024;

    private static Queue<ByteBuffer> pool = new ArrayBlockingQueue<ByteBuffer>(4);

    public static ByteBuffer getByteBuffer(final int minimumCapacity) {
        ByteBuffer buff;
        while (null != (buff = pool.poll())) {
            if (buff.capacity() >= minimumCapacity) {
                return buff;
            }
        }
        if (buff == null) {
            buff = ByteBuffer.allocateDirect(Math.max(MIN_CAPACITY, minimumCapacity));
        }
        return buff;
    }

    public static void returnToPool(final ByteBuffer buff) {
        if (buff != null && buff.capacity() >= MIN_CAPACITY) {
            pool.offer(buff);
        }
    }
}
