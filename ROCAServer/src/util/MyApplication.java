package util;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * Created by resul on 17/05/15.
 */
@ApplicationPath("/rest")
public class MyApplication extends ResourceConfig {

    public MyApplication() {
        packages("resources");
        register(
//                JacksonFeature.class,
                DefaultContextResolver.class
        );
    }

}
