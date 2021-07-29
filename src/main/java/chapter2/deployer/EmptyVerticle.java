package chapter2.deployer;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmptyVerticle extends AbstractVerticle {

    private Logger logger = LoggerFactory.getLogger(EmptyVerticle.class);

    @Override
//    public void start(Promise<Void> startPromise) throws Exception {
    public void start() throws Exception {
        logger.info("start");
//        startPromise.complete();
    }

    @Override
//    public void stop(Promise<Void> stopPromise) throws Exception {
    public void stop() throws Exception {
        logger.info("stop");
//        stopPromise.complete();
    }
}
