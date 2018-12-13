
package org.beyene.zmq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.UnmatchedArgumentException;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@EnableScheduling
@SpringBootApplication
public class Application {

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        CommandLine cmd = new CommandLine(options);
        cmd.setUsageHelpWidth(120);

        try {
            cmd.parse(args);
            if (cmd.isUsageHelpRequested()) {
                cmd.usage(System.out);
                return;
            } else if (cmd.isVersionHelpRequested()) {
                cmd.printVersionHelp(System.out);
                return;
            }

            new Application().runWith(options);
        } catch (ParameterException ex) {
            System.err.println(ex.getMessage());
            if (!UnmatchedArgumentException.printSuggestions(ex, System.err)) {
                ex.getCommandLine().usage(System.err);
            }
        }
    }

    public void runWith(Options options) throws Exception {
        SpringApplication application = new SpringApplication(Application.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        registerBeans(application, options);
        ConfigurableApplicationContext context = application.run();

        Thread.sleep(2_500);
        int exitCode = SpringApplication.exit(context, () -> 0);
        System.exit(exitCode);
    }

    private void registerBeans(SpringApplication application, Options options) {
        List<Function<Integer, Integer>> functions = IntStream.range(2, 2 + options.endpoints.size())
                .mapToObj(i -> (Function<Integer, Integer>) (x) -> i * x)
                .collect(Collectors.toList());

        for (int i = 0; i < options.endpoints.size(); i++) {
            String endpoint = options.endpoints.get(i);
            Server server = new Server(endpoint, functions.get(i));
            application.addInitializers(
                    (GenericApplicationContext ctx) -> ctx.registerBean(endpoint, Server.class, () -> server));
        }

        Client client = new Client(options.endpoints);
        application.addInitializers((GenericApplicationContext ctx) -> ctx.registerBean(Client.class, () -> client));
    }
}
