
package org.beyene.zmq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;
import java.util.List;

@EnableScheduling
@SpringBootApplication
public class Application {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication application = new SpringApplication(Application.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        registerBeans(application);
        ConfigurableApplicationContext context = application.run(args);

        Thread.sleep(2_500);
        int exitCode = SpringApplication.exit(context, () -> 0);
        System.exit(exitCode);
    }

    private static void registerBeans(SpringApplication application) {
        List<String> addresses = Arrays.asList("tcp://*:5555", "tcp://*:5556");
        Server serverA = new Server(addresses.get(0), (x) -> 2 * x);
        application.addInitializers((GenericApplicationContext ctx) -> ctx.registerBean("serverA", Server.class, () -> serverA));

        Server serverB = new Server(addresses.get(1), (x) -> 3 * x);
        application.addInitializers((GenericApplicationContext ctx) -> ctx.registerBean("serverB", Server.class, () -> serverB));

        Client client = new Client(addresses);
        application.addInitializers((GenericApplicationContext ctx) -> ctx.registerBean(Client.class, () -> client));
    }
}
