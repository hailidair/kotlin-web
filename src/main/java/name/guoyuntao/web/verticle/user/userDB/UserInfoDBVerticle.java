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

public class UserInfoDBVerticle extends AbstractVerticle {

    public static final String USER_QUEUE = "userdb.queue";
    public static final String USERINFO_QUEUE = "userInfodb.queue";

    private static final Logger LOGGER = LoggerFactory.getLogger(UserInfoDBVerticle.class);


    private SQLClient  mySQLClient;
    @Override
    public void start(Future<Void> startFuture) throws Exception {
      
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
                bus.consumer(config().getString(USERINFO_QUEUE, "userInfodb.queue"), this::onMessage);
                // <3>
                startFuture.complete();
            }
        });
    }
    private void onMessage(Message<JsonObject> message) {
        if (!message.headers().contains("action")) {
            LOGGER.error("No action header specified for message with headers {} and body {}",
                    message.headers());
            message.fail(LoginDBVerticle.ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), "No action header specified");
            return;
        }
        String action = message.headers().get("action");

        switch (action) {
            case "save-user":
                saveUser(message);
                break;
            case "user-info":
            	findUserInfo(message);
            	break;
            default:
                message.fail(LoginDBVerticle.ErrorCodes.BAD_ACTION.ordinal(), "Bad action: " + action);
        }
    }

    private void saveUser(Message<JsonObject> message) {
  	  JsonObject request = message.body();
	  
      mySQLClient.getConnection(car -> {    
      	if (car.failed()) {
  	        LOGGER.error("Could not open a database connection", car.cause());
  	      } else {
  	        SQLConnection connection = car.result();
  	        String username = request.getString("username");
  	        String guid = request.getString("guid");
  	        connection.execute("insert into ytdb.t_user_info values (null,'"+username+"',null,null,null,'"+guid+"')", resp -> {   // <2>
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
    
    private void findUserInfo(Message<JsonObject> message) {
        JsonObject request = message.body();
        mySQLClient.getConnection(car -> {
            if (car.succeeded()) {
                SQLConnection connection = car.result();
                String username = request.getString("username");

                connection.query("SELECT * FROM ytdb.t_user_info where user_name="+username, res -> {
                    connection.close();
                    if (res.succeeded()) {
                        ResultSet resultSet = res.result();
                        JsonArray userInfo = resultSet.getResults().get(0);
                        message.reply(new JsonObject().put("userInfo", userInfo.getString(1)));
                    } else {
                        reportQueryError(message, res.cause());
                    }
                });
            } else {
                reportQueryError(message, car.cause());
            }
        });
    }


    private void reportQueryError(Message<JsonObject> message, Throwable cause) {
        LOGGER.error("Database query error", cause);
        message.fail(LoginDBVerticle.ErrorCodes.DB_ERROR.ordinal(), cause.getMessage());
    }
}
