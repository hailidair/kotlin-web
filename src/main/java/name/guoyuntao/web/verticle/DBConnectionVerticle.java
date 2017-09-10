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

package name.guoyuntao.web.verticle;

import io.vertx.core.json.JsonObject;

public class DBConnectionVerticle {



  //public SQLClient  mySQLClient;

  public JsonObject dbConnection() {
	//连接数据库
	    JsonObject mySQLClientConfig = new JsonObject()
	    		.put("database", "ytdb")
	    		.put("host", "localhost")
	    		.put("port", 3306)
	    		.put("username", "root")
	    		.put("password", "123456")
	    		.put("charset", "UTF-8");
		//mySQLClient = MySQLClient.createShared(vertx, mySQLClientConfig);
		/*mySQLClient.getConnection(res -> {
			 if (res.failed()) {
			        LOGGER.error("Could not open a database connection", res.cause());
			  } else {
			        SQLConnection connection = res.result();
			        connection.close();
			       
			   }
		});*/
		 return mySQLClientConfig;

}
 /* @Override
  public void start(Future<Void> startFuture) throws Exception {

	//连接数据库
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
        connection.close();
        //数据库连接成功后，初始化dbverticle
        Future<String> dbVerticleDeployment = Future.future();
        //用户注册.
   	 	vertx.deployVerticle(new RegisterDBVerticle(),dbVerticleDeployment.completer());  
   	 	//用户登录
        vertx.deployVerticle(new LoginDBVerticle()); 
        //用户信息
        vertx.deployVerticle(new UserInfoDBVerticle()); 
        
        startFuture.complete();
      }
    });
  }*/
}
  