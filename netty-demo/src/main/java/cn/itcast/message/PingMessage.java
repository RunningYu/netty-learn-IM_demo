package cn.itcast.message;

/**
 * @author : 其然乐衣Letitbe
 * @date : 2023/2/1
 */
public class PingMessage extends Message{
    @Override
    public int getMessageType() {
//        return PingMessage;
        return 0;
    }
}