package io.vertx.starter;

import com.google.inject.Inject;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.starter.user.UserRoutes;

public class Routes {

  private Router rootRouter;

  @Inject
  public Routes(Vertx vertx, UserRoutes userRoutes) {
    this.rootRouter = Router.router(vertx);
    rootRouter.get("/").handler(r -> r.response().end("Ping!"));
    rootRouter
        .get("/loaderio-28016b04fdb0ed4ea066ecec5a19c1ad")
        .handler(r -> r.response().end("loaderio-28016b04fdb0ed4ea066ecec5a19c1ad"));
    this.rootRouter.mountSubRouter("/users", userRoutes.getRouter());
  }

  public Router getRouter() {
    return this.rootRouter;
  }
}
