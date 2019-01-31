package io.vertx.starter.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author vishwa161
 */
@Singleton
public final class DatabaseManager {

  private static final String MYSQL_CONFIG_KEY = "mysql.config";
  private AsyncSQLClient sqlClient;
  private Map queries;
  private WorkerExecutor worker;

  @Inject
  public DatabaseManager(ObjectMapper mapper) {
    initialiseDB();
    this.queries = SQLUtils.loadQueries(mapper);
    this.worker =
      Vertx.currentContext()
        .owner()
        .createSharedWorkerExecutor("SQL::WORKER::VERTICLE", 10, 2, TimeUnit.MINUTES);
  }

  private void initialiseDB() {
    final JsonObject config = Vertx.currentContext().config();
    final JsonObject configOptions = config.getJsonObject(MYSQL_CONFIG_KEY);
    sqlClient = MySQLClient.createNonShared(Vertx.currentContext().owner(), configOptions);
  }

  public final String getQuery(Query query) {
    if (this.queries == null) {
      throw new RuntimeException("Failed to get query as Queries are not loaded from json");
    }
    final Object queryStr = this.queries.get(query.name());
    if (queryStr == null) {
      throw new IllegalArgumentException("Query does not exist in the field");
    }
    return String.valueOf(queryStr);
  }

  public final void withConnection(Consumer<SQLConnection> consumer) {
    sqlClient.getConnection(
      res -> {
        if (res.succeeded()) {
          final SQLConnection connection = res.result();
          consumer.accept(connection);
          connection.close();
        } else {
          System.out.println("Failed to get withConnection" + res.cause());
        }
      });
  }

  @Deprecated
  public final void withAsyncConnection(Consumer<CompletableFuture<SQLConnection>> consumer) {
    CompletableFuture<SQLConnection> future = new CompletableFuture<>();
  }

  public final void executeQuery(
    String query, JsonArray params, Consumer<AsyncResult<ResultSet>> onSuccess) {
    this.worker.executeBlocking((Future<SQLConnection> future) -> {
        sqlClient.getConnection(asyncResult -> future.handle(asyncResult));
      }
      , false, (AsyncResult<SQLConnection> status) -> {
        if (status.succeeded()) {
          final SQLConnection connection = status.result();
          this.worker
            .executeBlocking((Future<ResultSet> future) ->
                connection.queryWithParams(
                  query,
                  params,
                  (AsyncResult<ResultSet> queryResult) -> {
                    connection.close();
                    future.handle(queryResult);
                  })
              ,
              onSuccess::accept);
        } else {
          System.out.println("Failed to get withConnection" + status.cause().getMessage());
        }
      });
  }


  public final void executeQuery2(
    String query, JsonArray params, Handler<AsyncResult<ResultSet>> resultHandler) {
    this.worker.executeBlocking((Future<ResultSet> future) ->
        sqlClient.getConnection(asyncResult ->
          asyncResult.map(connection ->
            connection.queryWithParams(query, params, queryResult -> {
              connection.close();
              future.handle(queryResult);
            })))
      , false, resultHandler::handle);
  }

  public final void executeGenericQuery(
    Function<SQLConnection, ResultSet> queryOp, Consumer<AsyncResult<ResultSet>> onSuccess) {
    this.withConnection(
      connection ->
        this.worker
          .executeBlocking(
            (Future<ResultSet> future) -> future.complete(queryOp.apply(connection)),
            onSuccess::accept));
  }

  public WorkerExecutor worker() {
    return this.worker;
  }
}
