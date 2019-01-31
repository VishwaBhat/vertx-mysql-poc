package io.vertx.starter.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import java.io.IOException;

import static io.vertx.starter.db.SQLUtils.convertToCamelCase;

/**
 * @author vishwa161
 */
@Singleton
public class BaseController {
  @Inject
  protected ObjectMapper mapper;
  private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

  protected <T> T parseBody(RoutingContext context, Class<T> clazz) {
    try {
      return mapper.readValue(context.getBody().getBytes(), clazz);
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse request body", e);
    }
  }

  protected <T> T mapTo(JsonObject o, Class<T> clazz) {
    try {
      return mapper.readValue(o.encode(), clazz);
    } catch (IOException e) {
      throw new RuntimeException("Failed to map the object to given class");
    }
  }

  protected <T> void success(RoutingContext context, T data) {
    final HttpResponse<T> success = HttpResponse.success(data);
    final HttpServerResponse response = context.response();

    if (!response.closed()) {
      response.end(Json.encodeToBuffer(success));
    }
  }

  protected void failure(RoutingContext context, String message) {
    final HttpServerResponse response = context.response();
    if (!response.closed()) {
      response.end(Json.encodeToBuffer(HttpResponse.error(message)));
    }
  }

  protected JsonObject toCamelCase(JsonObject existing) {
    JsonObject newObj = new JsonObject();
    existing.stream().forEachOrdered(e -> newObj.put(convertToCamelCase(e.getKey()), e.getValue()));
    return newObj;
  }
}
