package name.guoyuntao.web.verticle.user.userWeb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.templ.FreeMarkerTemplateEngine;
import name.guoyuntao.web.verticle.util.UUId;

public class UserVerticle  extends AbstractVerticle{
    private static final Logger LOGGER = LoggerFactory.getLogger(UserVerticle.class);
    public static final String CONFIG_HTTP_SERVER_PORT = "http.server.port";
    public static final String CONFIG_USERDB_QUEUE = "userdb.queue";
    public static final String CONFIG_LOGINDB_QUEUE = "logindb.queue";
    public static final String CONFIG_USERINFODB_QUEUE = "userInfodb.queue";

    private String userQueue = "userdb.queue";
    private String loginQueue = "logindb.queue";
    private String userInfoQueue = "userInfodb.queue";
    @Override
    public void start(Future<Void> startFuture) throws Exception {

        userQueue = config().getString(CONFIG_USERDB_QUEUE, "userdb.queue");
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);

        router.get("/").handler(this::indexHandler);
        router.get("/loginPage").handler(this::loginPageHandler);
        router.get("/registerPage").handler(this::registerPageHandler);
        router.get("/userInfo").handler(this::userBaseInfoHandler);
        router.route().handler(BodyHandler.create());
        router.post("/login").handler(this::loginHandler);
        router.post("/register").handler(this::registerHandler);


        int portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT, 8080);
        server.requestHandler(router::accept).listen(portNumber, ar -> {
            if (ar.succeeded()) {
                LOGGER.info("HTTP server running on port " + portNumber);
                startFuture.complete();
            } else {
                LOGGER.error("Could not start a HTTP server", ar.cause());
                startFuture.fail(ar.cause());
            }
        });
    }

    private final FreeMarkerTemplateEngine templateEngine = FreeMarkerTemplateEngine.create();
    
    private void indexHandler(RoutingContext context){
      	 context.put("title", "网站首页");
      	 String username = context.request().getParam("username");
      	 String islogin = context.request().getParam("islogin");
      	 if(username == null) {
      		 username = "";
      	 }
      	 
      	 if("true".equals(islogin)) {
      		 islogin = "true";
      	 }else {
      		 islogin = "false";
      	 }
      	 
      	 context.put("islogin", islogin);
      	 context.put("username", username);
           templateEngine.render(context, "templates", "/index.ftl",ar -> {
               if (ar.succeeded()) {
                   context.response().putHeader("Content-Type", "text/html;charset=UTF-8");
                   context.response().end(ar.result());
               } else {
                   context.fail(ar.cause());
               }
           });
      }

    private void registerPageHandler(RoutingContext context) {
        context.put("title", "用户注册");
       
        templateEngine.render(context, "templates", "/register.ftl",ar -> {
            if (ar.succeeded()) {
                context.response().putHeader("Content-Type", "text/html;charset=UTF-8");
                context.response().end(ar.result());
            } else {
                context.fail(ar.cause());
            }
        });
    }
    
   

    private void registerHandler(RoutingContext context) {
    	
        String username = context.request().getParam("username");
	    String password = context.request().getParam("password");
	    String conPass = context.request().getParam("conformPassword");
	    if(!password.equals(conPass) ){
	    	 context.response().end("两次密码不一致");
	    }
		
	    JsonObject request = new JsonObject();
		request.put("username", username);
		request.put("password", password);
		request.put("guid", UUId.getUUID());          ;


        DeliveryOptions options = new DeliveryOptions().addHeader("action", "save-user");
        vertx.eventBus().publish(userQueue, request,options);
        vertx.eventBus().consumer("registerQueue", message -> {
            boolean obj = (boolean)message.body();
            if (obj) {
                context.response().end("注册成功！！！");
            }
        });
       
        context.response().setStatusCode(303);
        context.response().putHeader("Location", "/loginPage");
      	context.response().end();
    }

    private void loginHandler(RoutingContext context) {

	    String username = context.request().getParam("username");
	    String password = context.request().getParam("password");
	    JsonObject request = new JsonObject();
		request.put("username", username);
		request.put("password", password);
		
	    DeliveryOptions options = new DeliveryOptions().addHeader("action", "login");
	    vertx.eventBus().send(loginQueue, request, options, reply -> {

	      if (reply.succeeded()) {
	        JsonObject body = (JsonObject) reply.result().body();
	        Boolean isLogin = body.getBoolean("isLogin");
	        String location = "/";
	        if(isLogin == true){
	        	location += "?username="+ body.getString("username")+"&islogin=true";
	        }else{
	        	location = "/loginPage?error=true";
	        }
	        context.response().setStatusCode(303);
	        context.response().putHeader("Location", location);
          	context.response().end();


	      } else {
	        context.fail(reply.cause());
	      }
	    });

    }

    private void loginPageHandler(RoutingContext context) {
    	
    	 context.put("title", "Login");
         templateEngine.render(context, "templates", "/login.ftl", ar -> {
           if (ar.succeeded()) {
             context.response().putHeader("Content-Type", "text/html");
             context.response().end(ar.result());
           } else {
             context.fail(ar.cause());
           }
         });

    }

    private void userBaseInfoHandler(RoutingContext context) {
        DeliveryOptions options = new DeliveryOptions().addHeader("action", "user-info"); // <2>
        JsonObject request = new JsonObject()
                .put("username",context.request().getParam("username"));
        vertx.eventBus().send(userInfoQueue, request, options, reply -> {  // <1>
            if (reply.succeeded()) {
                JsonObject body = (JsonObject) reply.result().body();   // <3>
                context.put("title", "个人基本信息");
                context.put("username",body.getString("userInfo"));

                templateEngine.render(context, "templates", "/userInfo.ftl", ar -> {
                    if (ar.succeeded()) {
                        context.response().putHeader("Content-Type", "text/html");
                        context.response().end(ar.result());
                    } else {
                        context.fail(ar.cause());
                    }
                });
            } else {
                context.fail(reply.cause());
            }
        });
    }
}
