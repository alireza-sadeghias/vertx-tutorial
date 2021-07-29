package chapter2.worker;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cntxt {

    private final static Logger logger = LoggerFactory.getLogger(Cntxt.class);

    public static void main(String[] args) {
        runAndContext();
        runAndException();
    }

    private static void runAndException() {
        Vertx vertx = Vertx.vertx();
        Context ctx = vertx.getOrCreateContext();
        ctx.put("foo","bar");
        ctx.exceptionHandler(t->{
            if("Tada".equals(t.getMessage())){
                logger.info("Got a TADA exception");
            }else{
                logger.error("Woops!!!",t);
            }
        });

        ctx.runOnContext(v->{
            throw new RuntimeException("Tada");
        });
        ctx.runOnContext(v->{
            logger.info("foo={}",(String) ctx.get("foo"));
        });
    }

    private static void runAndContext() {
        Vertx vertx = Vertx.vertx();

        vertx.getOrCreateContext().runOnContext(v -> {
            logger.info("ABC");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info("ABC2");
        });
        vertx.getOrCreateContext().runOnContext(v -> {
            logger.info("123");
            try {
                Thread.sleep(2000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            logger.info("123!!!");
        });
    }
}
