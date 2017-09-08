/*
 *  Copyright (c) 2017 Red Hat, Inc. and/or its affiliates.
 *  Copyright (c) 2017 INSA Lyon, CITI Laboratory.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vertx.guides.wiki;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.guides.wiki.User.LoginVerticle;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
// tag::preamble[]
public class WikiDatabaseVerticle extends AbstractVerticle {


  private static final Logger LOGGER = LoggerFactory.getLogger(WikiDatabaseVerticle.class);

  private SQLClient  mySQLClient;

  @Override
  public void start(Future<Void> startFuture) throws Exception {

    /*
     * Note: this uses blocking APIs, but data is small...
     */
    JsonObject mySQLClientConfig = new JsonObject()
    		.put("database", "ytdb")
    		.put("host", "localhost")
    		.put("port", 3306)
    		.put("username", "root")
    		.put("password", "123456")
    		.put("charset", "UTF-8");
	mySQLClient = MySQLClient.createShared(vertx, mySQLClientConfig);
	
	mySQLClient.getConnection(res -> {
		
      if (res.failed()) {
        LOGGER.error("Could not open a database connection", res.cause());
        startFuture.fail(res.cause());
      } else {
        SQLConnection connection = res.result();
        connection.execute("select * from ytdb.t_user", create -> {   // <2>
          connection.close();
          if (create.failed()) {
            LOGGER.error("Database preparation error", create.cause());
            startFuture.fail(create.cause());
          } else {
            vertx.eventBus().consumer("wikidb.queue", this::onMessage);  // <3>
            startFuture.complete();
          }
        });
      }
    });
  }
  // end::start[]

  // tag::onMessage[]
  public enum ErrorCodes {
    NO_ACTION_SPECIFIED,
    BAD_ACTION,
    DB_ERROR
  }

  public void onMessage(Message<JsonObject> message) {

    if (!message.headers().contains("action")) {
      LOGGER.error("No action header specified for message with headers {} and body {}",
        message.headers(), message.body().encodePrettily());
      message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), "No action header specified");
      return;
    }
    String action = message.headers().get("action");

    switch (action) {
      case "login":
        userLogin(message);
        break;
      case "register":
        fetchRegisterPage(message);
        break;
      case "login_input":
    	insertLogin(message);
    	break;
      case "saveOrUpdateuserInfo":
    	  saveOrUpdateuserInfo(message);
    	  break;
      case "all-pages":
    	  fetchAllPages(message);
        break;
      case "save-page":
        savePage(message);
        break;
      case "delete-page":
        deletePage(message);
        break;
      default:
        message.fail(ErrorCodes.BAD_ACTION.ordinal(), "Bad action: " + action);
    }
  }
  // end::onMessage[]

  // tag::rest[]
  private void userLogin(Message<JsonObject> message) {
	  JsonObject request = message.body();
	  
	  mySQLClient.getConnection(car -> {
      if (car.succeeded()) {
        SQLConnection connection = car.result();
        String username = request.getString("username");
        String password = request.getString("password");

        connection.query("select login_name from ytdb.t_user_login where login_name='"+username+"'", res -> {
          connection.close();
          if (res.succeeded()) {
            List<String> users = res.result()
            		 .getResults()
                     .stream()
                     .map(json -> json.getString(0))
                     .sorted()
                     .collect(Collectors.toList());
            if(!users.isEmpty()){
            	message.reply(new JsonObject().put("user", new JsonArray(users)));
            }else{
            	message.reply(new JsonObject().put("user", "用户名或密码错误！"));
            }
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

  
  private void fetchRegisterPage(Message<JsonObject> message) {
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
    	});
  }
  
  private void saveOrUpdateuserInfo(Message<JsonObject> message) {
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

  
  
  private void createPage(Message<JsonObject> message) {
    JsonObject request = message.body();

    mySQLClient.getConnection(car -> {

      if (car.succeeded()) {
        SQLConnection connection = car.result();
        JsonArray data = new JsonArray()
          .add(request.getString("title"))
          .add(request.getString("markdown"));

        connection.updateWithParams("", data, res -> {
          connection.close();
          if (res.succeeded()) {
            message.reply("ok");
          } else {
            reportQueryError(message, res.cause());
          }
        });
      } else {
        reportQueryError(message, car.cause());
      }
    });
  }

  private void savePage(Message<JsonObject> message) {
    JsonObject request = message.body();

    mySQLClient.getConnection(car -> {

      if (car.succeeded()) {
        SQLConnection connection = car.result();
        JsonArray data = new JsonArray()
          .add(request.getString("markdown"))
          .add(request.getString("id"));

        connection.updateWithParams("", data, res -> {
          connection.close();
          if (res.succeeded()) {
            message.reply("ok");
          } else {
            reportQueryError(message, res.cause());
          }
        });
      } else {
        reportQueryError(message, car.cause());
      }
    });
  }

  private void deletePage(Message<JsonObject> message) {
	  mySQLClient.getConnection(car -> {
      if (car.succeeded()) {
        SQLConnection connection = car.result();
        JsonArray data = new JsonArray().add(message.body().getString("id"));
        connection.updateWithParams("", data, res -> {
          connection.close();
          if (res.succeeded()) {
            message.reply("ok");
          } else {
            reportQueryError(message, res.cause());
          }
        });
      } else {
        reportQueryError(message, car.cause());
      }
    });
  }

  
  private void fetchAllPages(Message<JsonObject> message) {
	  
	  mySQLClient.getConnection(car -> {
      if (car.succeeded()) {
        SQLConnection connection = car.result();
        connection.query("select username from ytdb.t_user where id = '1'", res -> {
          connection.close();
          if (res.succeeded()) {
            String users = res.result().getResults().toString();
            		System.out.println("users = "+users);
            		message.reply(new JsonObject().put("user", users));
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
    message.fail(ErrorCodes.DB_ERROR.ordinal(), cause.getMessage());
  }
  // end::rest[]
}
