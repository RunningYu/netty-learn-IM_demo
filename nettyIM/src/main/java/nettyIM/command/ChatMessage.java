package nettyIM.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : 其然乐衣Letitbe
 * @date : 2023/2/5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    /** 消息类型，私有消息 or 群聊消息 */
    private Integer type;

    /** 目标接收对象 */
    private String target;

    /** 昵称 */
    private String nickname;

    /** 内容 */
    private String content;
}
