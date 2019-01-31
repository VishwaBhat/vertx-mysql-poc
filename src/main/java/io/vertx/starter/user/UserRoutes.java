package io.vertx.starter.user;

import com.google.inject.Inject;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * @author vishwa161
 */
public class UserRoutes {

  private static final String APPLICATION_JSON = "application/json";
  private Router router;

  @Inject
  public UserRoutes(UserController userController, Vertx vertx) {
    router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    defineRoutes(userController);
  }

  public Router getRouter() {
    return router;
  }

  private void defineRoutes(UserController controller) {
    this.router.get("/").handler(controller::getAll);
    this.router.get("/:userId").handler(controller::getUser);
    this.router.get("/:userId/async").handler(controller::getUserAsync);
    this.router.get("/:userId/skills").handler(controller::getUserSkills);
    this.router
      .put("/:userId")
      .consumes(APPLICATION_JSON)
      .produces(APPLICATION_JSON)
      .handler(controller::updateUser);
    this.router
      .post("/")
      .consumes(APPLICATION_JSON)
      .produces(APPLICATION_JSON)
      .handler(controller::createUser);
    this.router
      .post("/:userId/skills")
      .consumes(APPLICATION_JSON)
      .produces(APPLICATION_JSON)
      .handler(controller::addNewUserSkill);
    this.router
      .get("/:userId/skills/async")
      .handler(controller::getUserSkillsAsync);

    this.router.exceptionHandler(throwable -> {
      System.out.println("================ EXCEPTION HANDLER ===============");
      throwable.printStackTrace();
    });
  }
}
