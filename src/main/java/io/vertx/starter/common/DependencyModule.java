package io.vertx.starter.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.inject.AbstractModule;
import io.vertx.core.Vertx;

public class DependencyModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ObjectMapper.class).toInstance(this.newMapper());
    bind(Vertx.class).toInstance(Vertx.currentContext().owner());
  }

  private ObjectMapper newMapper() {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    return mapper;
  }
}
