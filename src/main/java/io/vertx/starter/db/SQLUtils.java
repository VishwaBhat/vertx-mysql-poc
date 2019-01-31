package io.vertx.starter.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class SQLUtils {

  private static final String QUERIES_FILE_PATH = "./src/main/conf/queries.json";

  static Map loadQueries(ObjectMapper mapper) {
    File file = new File(QUERIES_FILE_PATH);
    try {
      return mapper.readValue(file, Map.class);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load queries from JSON");
    }
  }

  public static <T> GeneratedQueryContent generateUpdateQuery(
      T model, Class<T> clazz, String schemaName, Map<String,Object> whereClause) {
    SQLParams params = new SQLParams();

    StringBuilder queryBuilder = new StringBuilder(String.format("UPDATE %s SET ", schemaName));
    final Method[] methods = clazz.getDeclaredMethods();

    final List<Method> getterMethods =
        Arrays.stream(methods)
            .filter(m -> m.getName().matches("^(get)[\\w]+") && m.canAccess(model))
            .collect(Collectors.toList());

    for (int i = 0; i < getterMethods.size(); i++) {
      Method method = getterMethods.get(i);
      final String methodName = method.getName();
      try {
        final Object value = method.invoke(model);
        if(value == null) {
          continue;
        }
        final String field = methodName.split("get")[1];
        final String dbFieldName = convertToSnakeCase(field);
        queryBuilder.append(String.format("%s=?", dbFieldName));
        if (i != (getterMethods.size() - 1)) {
          queryBuilder.append(",");
        }
        params.add(value);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException("Failed to generate insert query content");
      }
    }

    if (whereClause != null && !whereClause.isEmpty()) {
      queryBuilder.append(" WHERE ");

      final Iterator<Map.Entry<String, Object>> iterator = whereClause.entrySet().iterator();
      while (iterator.hasNext()) {
        final Map.Entry<String, Object> entry = iterator.next();
        queryBuilder.append(entry.getKey());
        queryBuilder.append("=?");
        if (iterator.hasNext()) {
          queryBuilder.append(" AND ");
        }
        params.add(entry.getValue());
      }
    }

    return new GeneratedQueryContent(queryBuilder.toString(), params);
  }

  public static <T> GeneratedQueryContent generateInsertQuery(
      T model, Class<T> clazz, String schemaName) {
    SQLParams params = new SQLParams();

    StringBuilder queryBuilder = new StringBuilder(String.format("INSERT INTO %s (", schemaName));
    final Method[] methods = clazz.getDeclaredMethods();

    final List<Method> getterMethods =
        Arrays.stream(methods)
            .filter(m -> m.getName().matches("^(get)[\\w]+") && m.canAccess(model))
            .collect(Collectors.toList());

    for (int i = 0; i < getterMethods.size(); i++) {
      Method method = getterMethods.get(i);
      final String methodName = method.getName();
      try {
        final String field = methodName.split("get")[1];
        final String dbFieldName = convertToSnakeCase(field);
        queryBuilder.append(dbFieldName);
        if (i != (getterMethods.size() - 1)) {
          queryBuilder.append(",");
        }
        params.add(method.invoke(model));
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException("Failed to generate insert query content");
      }
    }

    queryBuilder.append(") VALUES (");

    if (params.size() == 1) {
      queryBuilder.append("?");
    } else {
      for (int i = 0; i <= params.size() - 1; i++) {
        queryBuilder.append("?");
        if (i != params.size() - 1) {
          queryBuilder.append(",");
        }
      }
    }
    queryBuilder.append(")");

    return new GeneratedQueryContent(queryBuilder.toString(), params);
  }

  @Getter
  public static class GeneratedQueryContent {
    private String query;
    private SQLParams sqlParams;

    GeneratedQueryContent(final String query, final SQLParams sqlParams) {
      this.query = query;
      this.sqlParams = sqlParams;
    }
  }

  private static String convertToSnakeCase(String field) {
    final char[] chars = field.toCharArray();
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < chars.length; i++) {
      char character = chars[i];
      if (i != 0 && Character.isUpperCase(character)) {
        builder.append("_");
      }
      builder.append(Character.toLowerCase(character));
    }
    return builder.toString();
  }

  public static String convertToCamelCase(String field) {
    final char[] chars = field.toCharArray();
    StringBuilder builder = new StringBuilder();
    boolean underScoreFound = false;
    for (char character : chars) {
      if (character == '_') {
        underScoreFound = true;
        continue;
      }
      if (underScoreFound) {
        builder.append(Character.toUpperCase(character));
        underScoreFound = false;
      } else {
        builder.append(Character.toLowerCase(character));
      }
    }
    return builder.toString();
  }
}
