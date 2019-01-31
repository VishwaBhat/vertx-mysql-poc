package io.vertx.starter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerOptions;

/**
 * @author vishwa161
 */
@Singleton
public class ServerVerticle extends AbstractVerticle {

  private Routes routes;

  @Inject
  public ServerVerticle(Routes routes) {
    this.routes = routes;
  }

  private static final int SERVER_PORT = 8888;

  @Override
  public void start(Future<Void> startFuture) {
    vertx
        .createHttpServer(new HttpServerOptions().setUseAlpn(true).setCompressionSupported(true))
        .requestHandler(routes.getRouter())
        .listen(
            SERVER_PORT,
            http -> {
              if (http.succeeded()) {
                startFuture.complete();
                System.out.println("HTTP server running on http://localhost:8888");
              } else {
                startFuture.fail(http.cause());
              }
            });
  }
}
