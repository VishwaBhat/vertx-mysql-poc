package io.vertx.starter.user;

import io.vertx.starter.db.SQLParams;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Test {

  public static void main(String[] args) {

    Pattern p = Pattern.compile("^(get)");
    final String pattern = p.pattern();

    UserModel model = new UserModel();
    model.setEmail("vishwa@rocks");
    model.setName("Vishwa Bhat");

    buildSqlQueryContentFromObj(model, UserModel.class, "USERS");
  }

  public static <T> SQLParams buildSqlQueryContentFromObj(
      T model, Class<T> clazz, String schemaName) {
    SQLParams params = new SQLParams();
    Predicate<String> isGetterMethod = name -> name.matches("^(get)[\\w]+");
    StringBuilder builder = new StringBuilder("INSERT INTO " + schemaName + " (");
    final Method[] methods = clazz.getDeclaredMethods();
    for (int i = 0; i < methods.length; i++) {
      Method method = methods[i];
      final String methodName = method.getName();
      if (isGetterMethod.test(methodName) && method.canAccess(model)) {
        try {
          final String prop = methodName.split("get")[1];
          final String dbFieldName = convertToSnakeCase(prop);
          builder.append(dbFieldName);
          if (i != methods.length - 1) {
            builder.append(",");
          }
          params.add(method.invoke(model));
        } catch (IllegalAccessException | InvocationTargetException e) {
          e.printStackTrace();
        }
      }
    }

    builder.append(") VALUES (");

    if (params.size() == 1) {
      builder.append("?");
    } else {
      for (int i = 0; i <= params.size() - 1; i++) {
        builder.append("?");
        if (i != params.size() - 1) {
          builder.append(",");
        }
      }
    }
    builder.append(")");

    System.out.println(String.format("Query: %s", builder.toString()));
    System.out.println(params);

    return params;
  }

  public static String convertToSnakeCase(String field) {
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
}
