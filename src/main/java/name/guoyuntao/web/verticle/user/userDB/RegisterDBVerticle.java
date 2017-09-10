package name.guoyuntao.web.verticle.user.userDB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;

import name.guoyuntao.web.verticle.DBConnectionVerticle;
public class RegisterDBVerticle extends AbstractVerticle {

    public static final String USER_QUEUE = "userdb.queue";

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterDBVerticle.class);


    private SQLClient  mySQLClient;
    @Override
    public void start(Future<Void> startFuture) throws Exception {
    	//连接数据库
    	DBConnectionVerticle dbConnectionVerticle = new DBConnectionVerticle();
    	JsonObject mySQLClientConfig = dbConnectionVerticle.dbConnection();
    	mySQLClient = MySQLClient.createShared(vertx, mySQLClientConfig);
    	
        mySQLClient.getConnection(res -> {
            if (res.failed()) {
                LOGGER.error("Could not open a database connection", res.cause());
                startFuture.fail(res.cause());
            } else {
                SQLConnection connection = res.result();
                connection.close();
                EventBus bus = vertx.eventBus();
                bus.consumer(config().getString(USER_QUEUE, "userdb.queue"), this::onMessage);
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
            	register(message);
                break;
            default:
                message.fail(LoginDBVerticle.ErrorCodes.BAD_ACTION.ordinal(), "Bad action: " + action);
        }
    }

    private void register(Message<JsonObject> message) {
        JsonObject request = message.body();

       mySQLClient.getConnection(car -> {
    	    
       	if (car.failed()) {
   	        LOGGER.error("Could not open a database connection", car.cause());
   	      } else {
   	        SQLConnection connection = car.result();
   	        String username = request.getString("username");
   	        String password = request.getString("password");
   	        String guid = request.getString("guid");
   	        connection.execute("insert into ytdb.t_user_reg values (null,'"+username+"','"+password+"','"+guid+"')", resp -> {   // <2>
   	          connection.close();
   	          if (resp.failed()) {
   	            LOGGER.error("Database insert error", resp.cause());
   	          } else {
   	        	  message.reply("ok");
   	          }
   	        });
   	      }
       	
//
//            if (car.succeeded()) {
//                SQLConnection connection = car.result();
//                
//                connection.updateWithParams(sqlQueries.get(SqlQuery.SAVE_USER), data, res -> {
//                    connection.close();
//                    if (res.succeeded()) {
//                        message.reply("ok");
//                        vertx.eventBus().send("registerQueue",true);
//                    } else {
//                        reportQueryError(message, res.cause());
//                    }
//                });
//            } else {
//                reportQueryError(message, car.cause());
//            }
       });
    }

    private void reportQueryError(Message<JsonObject> message, Throwable cause) {
        LOGGER.error("Database query error", cause);
        message.fail(LoginDBVerticle.ErrorCodes.DB_ERROR.ordinal(), cause.getMessage());
    }

}
