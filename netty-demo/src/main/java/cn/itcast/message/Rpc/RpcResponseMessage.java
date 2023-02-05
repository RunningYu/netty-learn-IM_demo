package cn.itcast.message.Rpc;

import cn.itcast.message.Message;
import lombok.Data;
import lombok.ToString;

/**
 * @author : 其然乐衣Letitbe
 * @date : 2023/2/3
 */
@Data
@ToString(callSuper = true)
public class RpcResponseMessage extends Message {
    /**
     * 返回值
     */
    private Object returnValue;
    /**
     * 异常值
     */
    private Exception exceptionValue;

    @Override
    public int getMessageType() {
        return RPC_MESSAGE_TYPE_RESPONSE;
    }
}