package name.guoyuntao.web.verticle.user.userDB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import name.guoyuntao.web.verticle.DBConnectionVerticle;

public class LoginDBVerticle extends AbstractVerticle {

    public static final String USER_QUEUE = "userdb.queue";
    public static final String LOGIN_QUEUE = "logindb.queue";

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginDBVerticle.class);

    private SQLClient  mySQLClient;
    @Override
    public void start(Future<Void> startFuture) throws Exception {

    	//连接数据库
    	DBConnectionVerticle dbConnectionVerticle = new DBConnectionVerticle();
    	JsonObject mySQLClientConfig = dbConnectionVerticle.dbConnection();
    	mySQLClient = MySQLClient.createShared(vertx, mySQLClientConfig);

    	mySQLClient.getConnection(ar -> {
            if (ar.failed()) {
                LOGGER.error("Could not open a database connection", ar.cause());
                startFuture.fail(ar.cause());
            } else {
                SQLConnection connection = ar.result();
                connection.close();
                EventBus bus = vertx.eventBus();
                bus.consumer(config().getString(USER_QUEUE, "userdb.queue"), this::onMessage);
                bus.consumer(config().getString(LOGIN_QUEUE, "logindb.queue"), this::onMessage);
                // <3>
                startFuture.complete();
            }
        });
    }

    public enum ErrorCodes {
        NO_ACTION_SPECIFIED,
        BAD_ACTION,
        DB_ERROR
    }

    private void onMessage(Message<JsonObject> message) {
        if (!message.headers().contains("action")) {
            LOGGER.error("No action header specified for message with headers {} and body {}",
                    message.headers());
            message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), "No action header specified");
            return;
        }
        String action = message.headers().get("action");

        switch (action) {
            case "login":
            	login(message);
            	break;
            case "save-user":
            	insertLogin(message);
            	break;
            default:
                message.fail(ErrorCodes.BAD_ACTION.ordinal(), "Bad action: " + action);
        }
    }

   
    private  void login(Message<JsonObject> message) {
        JsonObject request = message.body();

        mySQLClient.getConnection(car -> {

            if (car.succeeded()) {
                SQLConnection connection = car.result();
                String username = request.getString("username");
                String password = request.getString("password");
                connection.query("select login_name from ytdb.t_user_login where login_name='"+username+"' and password='"+password+"'", res -> {
                    connection.close();
                    if (res.succeeded()) {
                        JsonObject response = new JsonObject();
                        ResultSet resultSet =  res.result();
                        if (resultSet.getNumRows() == 0) {
                            response.put("isLogin",false);
                        } else {
                            response.put("isLogin",true);
                            JsonArray row = resultSet.getResults().get(0);
                            response.put("username",row.getString(0));
                        }
                        message.reply(response);
                    } else {
                        reportQueryError(message, res.cause());
                    }
                });
            } else {
                reportQueryError(message, car.cause());
            }
        });
    }
    private void insertLogin(Message<JsonObject> message) {
  	  JsonObject request = message.body();
  	  
  	    mySQLClient.getConnection(car -> {    
  	    	if (car.failed()) {
  		        LOGGER.error("Could not open a database connection", car.cause());
  		      } else {
  		        SQLConnection connection = car.result();
  		        String loginname = request.getString("username");
  		        String password = request.getString("password");
  		        String guid = request.getString("guid");
  		        connection.execute("insert into ytdb.t_user_login values (null,'"+loginname+"','"+password+"',null,'"+guid+"')", resp -> {   // <2>
  		          connection.close();
  		          if (resp.failed()) {
  		            LOGGER.error("Database insert error", resp.cause());
  		          } else {
  		        	  message.reply("ok");
  		          }
  		        });
  		      }
  	    	});

  	  }

    private void reportQueryError(Message<JsonObject> message, Throwable cause) {
        LOGGER.error("Database query error", cause);
        message.fail(ErrorCodes.DB_ERROR.ordinal(), cause.getMessage());
    }
}
