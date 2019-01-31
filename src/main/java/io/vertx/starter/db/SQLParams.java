package io.vertx.starter.db;

import io.vertx.core.json.JsonArray;

public class SQLParams extends JsonArray {

  public SQLParams() {
    super();
  }

  @Override
  public SQLParams add(Object param) {
    if (param == null) {
      super.addNull();
    } else {
      super.add(param);
    }
    return this;
  }

  @Override
  public SQLParams add(String param) {
    if (param == null) {
      super.addNull();
    } else {
      super.add(param);
    }
    return this;
  }

  @Override
  public SQLParams add(Long param) {
    if (param == null) {
      super.addNull();
    } else {
      super.add(param);
    }
    return this;
  }

  @Override
  public SQLParams add(Integer param) {
    if (param == null) {
      super.addNull();
    } else {
      super.add(param);
    }
    return this;
  }
}
