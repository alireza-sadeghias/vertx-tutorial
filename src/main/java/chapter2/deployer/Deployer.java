package chapter2.deployer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.LogManager;

public class Deployer extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(Deployer.class);

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        long delay=1000;
        for(int i=0;i<50;i++){
            logger.info("#{} is deploying",i);
            vertx.setTimer(delay,id->deploy());
            delay = delay+1000;
        }
    }

    private void deploy() {
        vertx.deployVerticle(new EmptyVerticle(),ar->{
           if(ar.succeeded()){
               String id = ar.result();
               logger.info("Successfully  deployed {}",id);
               vertx.setTimer(5000,tid->undeployLater(id));
           } else {
               logger.error("Error while Deploying",ar.cause());
           }
        });
    }

    private void undeployLater(String id) {
        vertx.undeploy(id,ar->{
           if(ar.succeeded()){
               logger.info("{} was undeployed.",id);
           } else {
               logger.error("{} unable to undeployed",id);
           }
        });
    }
}
