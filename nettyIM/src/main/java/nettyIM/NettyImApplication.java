package nettyIM;

import nettyIM.im.IMserver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NettyImApplication {

    public static void main(String[] args) {
        IMserver.start();
        SpringApplication.run(NettyImApplication.class, args);
    }

}
