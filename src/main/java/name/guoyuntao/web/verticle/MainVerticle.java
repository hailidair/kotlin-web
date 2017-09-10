package name.guoyuntao.web.verticle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import name.guoyuntao.web.verticle.user.userDB.LoginDBVerticle;
import name.guoyuntao.web.verticle.user.userDB.RegisterDBVerticle;
import name.guoyuntao.web.verticle.user.userDB.UserInfoDBVerticle;

public class MainVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Future<Void> startFuture) throws Exception {
    	logger.info("begin");
        Future<String> dbConnectionVerticleDeployment = Future.future();  // <1>
        
      //  vertx.deployVerticle(new DBConnectionVerticle(), dbConnectionVerticleDeployment.completer());  // <2>
        //用户注册.
   	 	vertx.deployVerticle(new RegisterDBVerticle(),dbConnectionVerticleDeployment.completer());  
   	 	//用户登录
        vertx.deployVerticle(new LoginDBVerticle()); 
        //用户信息
        vertx.deployVerticle(new UserInfoDBVerticle()); 
        
        dbConnectionVerticleDeployment.compose(id -> {  // <3>

            Future<String> httpVerticleDeployment = Future.future();
            vertx.deployVerticle(
                    "name.guoyuntao.web.verticle.user.userWeb.UserVerticle",  // <4>
                    new DeploymentOptions().setInstances(2),    // <5>
                    httpVerticleDeployment.completer());

            return httpVerticleDeployment;  // <6>

        }).setHandler(ar -> {   // <7>
        	logger.info("end");
            if (ar.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(ar.cause());
            }
        });
    }
}
