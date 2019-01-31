package io.vertx.starter.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HttpResponse<T> {
  private T data;
  private boolean success;
  private String message;

  public static <T> HttpResponse<T> success(T data) {
    HttpResponse<T> response = new HttpResponse<>();
    response.data = data;
    response.success = true;
    return response;
  }

  public static <T> HttpResponse<T> error(String message) {
    HttpResponse<T> response = new HttpResponse<>();
    response.message = message;
    response.success = false;
    return response;
  }
}
