
package org.beyene.zmq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;

public class Application
{

  public static void main(String[] args)
  {
    SpringApplication application = new SpringApplication(Application.class);
    application.setWebApplicationType(WebApplicationType.NONE);

    // Manually register bean that provides ZMQ context (and closes it)
    //      application.addInitializers((GenericApplicationContext ctx) -> ctx.registerBean(TypeClass.class, () -> typeObject));
    application.run(args);
  }
}
