package cn.itcast.message;

import lombok.Data;
import lombok.ToString;

/**
 * 向服务器发送一个登录的消息，服务器对这个登录消息进行一个处理，如果用户密码正确就登录成功，就可以进行下一个聊天的业务。否则就登陆失败退出
 */
@Data
@ToString(callSuper = true)
public class LoginRequestMessage extends Message {
    private String username;
    private String password;

    public LoginRequestMessage(String username, String password) {
    }

    public LoginRequestMessage(String username, String password, String nickname) {
        this.username = username;
        this.password = password;
    }

    @Override
    public int getMessageType() {
        return LoginRequestMessage;
    }
}
