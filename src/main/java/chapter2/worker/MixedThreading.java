package chapter2.worker;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class MixedThreading extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(MixedThreading.class);

    @Override
    public void start() throws Exception {
        Context context = vertx.getOrCreateContext();
        new Thread(()->{
            try{
                run(context);
            }catch (InterruptedException e){
                logger.error("Woops ");
            }
        }).start();
    }

    private void run(Context context) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(5);
        logger.info("I am on the non vertx-event loop");
        context.runOnContext(v->{
            logger.info("I am on the event loop");
            vertx.setTimer(1000,id->{
               logger.info("this is the final countdown {}",id.longValue());
               latch.countDown();
            });
        });
        logger.info("Waiting on the countdown latch....");
        latch.await();
        logger.info("bye");
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MixedThreading());
    }
}
