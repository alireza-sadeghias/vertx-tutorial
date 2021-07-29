package chapter2.worker;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Offload extends AbstractVerticle {

    private final Logger logger = LoggerFactory.getLogger(Offload.class);

    @Override
    public void start(){
        logger.info("Thick!!!");
        vertx.setPeriodic(5000,id->{
           vertx.executeBlocking(this::blockingCode,this::resultHandler);
        });
    }

    private void resultHandler(AsyncResult<String> tAsyncResult) {
        if(tAsyncResult.succeeded()){
            logger.info("Blocking code result:{}", tAsyncResult.result());
        }else{
            logger.info("Woops!!!",tAsyncResult.cause());
        }
    }

    private void blockingCode(Promise<String> tPromise) {
        logger.info("blocking code running!!!");

        try{
            Thread.sleep(4000);
            logger.info("Done!");
            tPromise.complete("OK!");
        }catch (InterruptedException e){
            logger.error("Woop",e);
            tPromise.fail(e);
        }

    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new Offload());
    }

}
