package chapter2.some;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class SomeVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> promise) throws Exception {
        vertx.createHttpServer().requestHandler(req->req.response().end("OK!")).listen(8080,ar->{
            if(ar.succeeded()){
                promise.complete();
            }else{
                promise.fail(ar.cause());
            }
        });
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }
}
