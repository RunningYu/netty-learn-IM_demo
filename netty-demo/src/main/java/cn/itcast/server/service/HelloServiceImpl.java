package cn.itcast.server.service;

/**
 * @author : 其然乐衣Letitbe
 * @date : 2023/2/3
 */
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String msg) {
        return "你好，" + msg;
    }
}
