package cn.itcast.protocol;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : 其然乐衣Letitbe
 * @date : 2023/2/3
 */
public class SequenceIdGenerator {
    private static final AtomicInteger id = new AtomicInteger();

    public static int nextId(){
        return id.incrementAndGet();
    }

}
