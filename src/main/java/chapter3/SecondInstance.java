package chapter3;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecondInstance {

    private static final Logger logger = LoggerFactory.getLogger(SecondInstance.class);

    public static void main(String[] args) {
        Vertx.clusteredVertx(new VertxOptions(),ar->{
            if(ar.succeeded()){
                logger.info("Second instance has been started");
                Vertx vertx = ar.result();
                vertx.deployVerticle("chapter3.HeatSensor", new DeploymentOptions().setInstances(4));
                vertx.deployVerticle("chapter3.Listener" );
                vertx.deployVerticle("chapter3.SensorData" );
                JsonObject config = new JsonObject().put("port", 8081);
                DeploymentOptions options = new DeploymentOptions().setConfig(config);
                vertx.deployVerticle("chapter3.HttpServer", options);
            }else{
               logger.error("could not start",ar.cause());
            }
        });
    }

}
