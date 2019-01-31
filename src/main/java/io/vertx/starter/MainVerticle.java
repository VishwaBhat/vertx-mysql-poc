package io.vertx.starter;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.starter.common.DependencyModule;

/**
 * @author vishwa161
 */
public class MainVerticle extends AbstractVerticle {

  private static final String VERTX_POOL_NAME = "vertx::pool";

  @Override
  public void start() throws Exception {
    final Injector injector = Guice.createInjector(new DependencyModule());

    final DeploymentOptions deploymentOptions =
      new DeploymentOptions()
        .setConfig(config());


    vertx.deployVerticle(injector.getInstance(ServerVerticle.class), deploymentOptions);
  }
}
