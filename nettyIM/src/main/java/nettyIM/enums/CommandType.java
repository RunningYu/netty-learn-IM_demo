package nettyIM.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author : 其然乐衣Letitbe
 * @date : 2023/2/5
 */
@Getter
@AllArgsConstructor
public enum CommandType {
    /**
     * 建立连接
     */
    CONNECTION(10001),

    /**
     * 聊天消息
     */
    CHAT(10002),

    /**
     * 取聊消息
     */
    JOIN_GROUP(10003),

    ERROR(-1),
    ;


    private final Integer code;

    public static CommandType match(Integer code) {
        for ( CommandType value : CommandType.values() ) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return ERROR;
    }
}
