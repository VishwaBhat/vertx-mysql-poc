package io.vertx.starter.user;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.web.RoutingContext;
import io.vertx.starter.common.BaseController;
import io.vertx.starter.db.DatabaseManager;
import io.vertx.starter.db.Query;
import io.vertx.starter.db.SQLParams;
import io.vertx.starter.db.SQLUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
class UserController extends BaseController {

  @Inject private DatabaseManager db;

  @Inject private UserService userService;

  void getAll(RoutingContext context) {
    db.withConnection(
        connection ->
            connection.query(
                db.getQuery(Query.GET_ALL_USERS),
                status -> {
                  if (status.succeeded()) {
                    final List<UserModel> users =
                        status.result().getRows().stream()
                            .map(row -> mapTo(row, UserModel.class))
                            .collect(Collectors.toList());
                    success(context, new JsonObject().put("users", users));
                  } else {
                    failure(context, "Failed to fetch users" + status.cause().getMessage());
                  }
                }));
  }

  void getUserAsync(RoutingContext context) {
    String userId = context.pathParam("userId");

    userService
        .getUser2(userId)
        .setHandler(
            result -> {
              if (result.succeeded()) {
                success(context, result.result());
              } else {
                failure(context, result.cause().getMessage());
              }
            });
    /*userService.getUser(
    userId,
    result -> {
      if (result.succeeded()) {
        success(context, result.result());
      } else {
        failure(context, result.cause().getMessage());
      }
    });*/
  }

  void getUser(RoutingContext context) {
    String userId = context.pathParam("userId");
    db.withConnection(
        connection -> {
          JsonArray params = new JsonArray().add(userId);
          connection.queryWithParams(
              db.getQuery(Query.GET_USER_BY_ID),
              params,
              status -> {
                connection.close();
                if (status.succeeded()) {
                  final ResultSet result = status.result();
                  final boolean hasData = result.getNumRows() > 0;
                  if (hasData) {
                    JsonObject user = buildUser(result);
                    success(context, Map.of("user", user));
                  } else {
                    failure(context, "No user found");
                  }
                } else {
                  failure(context, "Failed to fetch user" + status.cause().getMessage());
                }
              });
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

  void createUser(RoutingContext context) {
    final UserModel userModel = parseBody(context, UserModel.class);
    db.withConnection(
        connection -> {
          final SQLUtils.GeneratedQueryContent queryContent =
              SQLUtils.generateInsertQuery(userModel, UserModel.class, UserModel.SCHEMA);
          connection.updateWithParams(
              queryContent.getQuery(),
              queryContent.getSqlParams(),
              status -> {
                if (status.succeeded()) {
                  final int updated = status.result().getUpdated();
                  success(context, String.format("Successfully created %d user", updated));
                } else {
                  failure(context, "Failed to create user" + status.cause().getMessage());
                }
              });
        });
  }

  void updateUser(RoutingContext context) {
    final UserModel userModel = parseBody(context, UserModel.class);
    final String userId = context.pathParam("userId");
    db.withConnection(
        connection -> {
          final SQLUtils.GeneratedQueryContent queryContent =
              SQLUtils.generateUpdateQuery(
                  userModel, UserModel.class, UserModel.SCHEMA, Map.of("id", userId));
          connection.updateWithParams(
              queryContent.getQuery(),
              queryContent.getSqlParams(),
              status -> {
                if (status.succeeded()) {
                  final int updated = status.result().getUpdated();
                  success(context, String.format("Successfully created %d user", updated));
                } else {
                  failure(context, "Failed to create user" + status.cause().getMessage());
                }
              });
        });
  }

  void getUserSkillsAsync(RoutingContext context) {
    final String userId = context.pathParam("userId");
    db.executeQuery(
        db.getQuery(Query.GET_USER_SKILLS),
        new SQLParams().add(userId),
        status -> {
          if (status.succeeded()) {
            final ResultSet result = status.result();
            final boolean hasData = result.getNumRows() > 0;
            if (hasData) {
              success(context, Map.of("skills", result.getRows()));
            }
          } else {
            failure(context, "Failed to fetch user with all skills" + status.cause().getMessage());
          }
        });
  }

  void getUserSkills(RoutingContext context) {
    final String userId = context.pathParam("userId");

    db.withConnection(
        connection ->
            connection.queryWithParams(
                db.getQuery(Query.GET_USER_SKILLS),
                new SQLParams().add(userId),
                status -> {
                  if (status.succeeded()) {
                    final ResultSet result = status.result();
                    final boolean hasData = result.getNumRows() > 0;
                    if (hasData) {
                      success(context, Map.of("skills", result.getRows()));
                    }
                  } else {
                    failure(context, "Failed to fetch user with all skills");
                  }
                }));
  }

  void addNewUserSkill(RoutingContext context) {
    final String userId = context.pathParam("userId");
    final String skillId = context.getBodyAsJson().getString("skillId");

    db.withConnection(
        connection -> {
          connection.updateWithParams(
              db.getQuery(Query.ADD_NEW_USER_SKILL),
              new SQLParams().add(userId).add(skillId),
              status -> {
                if (status.succeeded()) {
                  final int updated = status.result().getUpdated();
                  success(context, String.format("Successfully added %d skill(s)", updated));
                } else {
                  failure(context, "Failed to create user" + status.cause().getMessage());
                }
              });
        });
  }
}
