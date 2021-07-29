package chapter2.deployer;

import io.vertx.core.Vertx;

public class SimpleClass {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Deployer());
    }
}
