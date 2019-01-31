package io.vertx.starter.user;

import com.google.inject.Inject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;
import io.vertx.starter.db.DatabaseManager;
import io.vertx.starter.db.Query;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class UserService {

  @Inject private DatabaseManager db;
  private Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

  public Future<Map> getUser2(String userId) {
    Future<Map> future = Future.future();
    JsonArray params = new JsonArray().add(userId);
    db.executeQuery2(
        db.getQuery(Query.GET_USER_BY_ID),
        params,
        status -> {
          final ResultSet result = status.result();
          final boolean hasData = result.getNumRows() > 0;
          if (hasData) {
            JsonObject user = buildUser(result);
            future.complete(Map.of("user", user));
          } else {
            future.fail("No user found");
          }
        });
    return future;
  }

  public void getUser(String userId, Consumer<AsyncResult<Map>> onDone) {
    JsonArray params = new JsonArray().add(userId);

    db.executeQuery(
        db.getQuery(Query.GET_USER_BY_ID),
        params,
        status -> {
          System.out.println("inside execute running in " + Thread.currentThread().getName());
          if (status.succeeded()) {
            final ResultSet result = status.result();
            final boolean hasData = result.getNumRows() > 0;
            if (hasData) {
              JsonObject user = buildUser(result);
              onDone.accept(Future.succeededFuture(Map.of("user", user)));
            } else {
              onDone.accept(Future.failedFuture("No user found"));
            }
          } else {
            onDone.accept(Future.failedFuture(status.cause()));
          }
        });
  }

  private JsonObject buildUser(ResultSet result) {
    JsonObject user = new JsonObject();
    JsonArray userExperiences = new JsonArray();
    JsonArray userEducations = new JsonArray();
    JsonArray userSkills = new JsonArray();

    Set<String> addedItems = new HashSet<>();

    result
        .getRows()
        .iterator()
        .forEachRemaining(
            obj -> {
              if (!user.containsKey("id")) {
                user.put("id", obj.getValue("user_id"));
              }

              String expKey = "exp" + obj.getValue("exp_id");
              if (!addedItems.contains(expKey)) {
                JsonObject experience = new JsonObject();
                experience.put("id", obj.getValue("exp_id"));
                experience.put("company", obj.getValue("company"));
                experience.put("designation", obj.getValue("designation"));
                experience.put("location", obj.getValue("exp_location"));
                experience.put("startMonth", obj.getValue("exp_start_month"));
                experience.put("endMonth", obj.getValue("exp_end_month"));
                experience.put("startYear", obj.getValue("exp_start_year"));
                experience.put("endYear", obj.getValue("exp_end_year"));
                experience.put("isCurrent", obj.getValue("exp_is_current"));
                userExperiences.add(experience);
                addedItems.add(expKey);
              }

              final String eduKey = "edu" + obj.getValue("edu_id");
              if (!addedItems.contains(eduKey)) {
                JsonObject education = new JsonObject();
                education.put("id", obj.getValue("edu_id"));
                education.put("degree", obj.getValue("edu_degree"));
                education.put("college", obj.getValue("edu_college"));
                education.put("location", obj.getValue("exp_location"));
                education.put("startMonth", obj.getValue("exp_start_month"));
                education.put("endMonth", obj.getValue("exp_end_month"));
                education.put("startYear", obj.getValue("exp_start_year"));
                education.put("endYear", obj.getValue("exp_end_year"));
                education.put("isCurrent", obj.getValue("exp_is_current"));
                userEducations.add(education);
                addedItems.add(eduKey);
              }

              final String skillKey = "skill" + obj.getValue("skill_id");
              if (!addedItems.contains(skillKey)) {
                JsonObject skill = new JsonObject();
                skill.put("id", obj.getValue("skill_id"));
                skill.put("name", obj.getValue("skill_name"));
                userSkills.add(skill);
                addedItems.add(skillKey);
              }
            });

    user.put("experiences", userExperiences);
    user.put("educations", userEducations);
    user.put("skills", userSkills);
    return user;
  }
}
