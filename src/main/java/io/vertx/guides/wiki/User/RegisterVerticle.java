package io.vertx.guides.wiki.User;

import java.util.Date;

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
import io.vertx.guides.wiki.HttpServerVerticle;

public class RegisterVerticle extends AbstractVerticle {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

	public static final String CONFIG_HTTP_SERVER_PORT = "http.server.port";  // <1>
	public static final String CONFIG_WIKIDB_QUEUE = "wikidb.queue";
	private final FreeMarkerTemplateEngine templateEngine = FreeMarkerTemplateEngine.create();
	private String wikiDbQueue = "wikidb.queue";
	protected Router regRouter;
	
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		/*  Future<String> dbVerticleDeployment = Future.future();  // <1>
			vertx.deployVerticle(new WikiDatabaseVerticle(), dbVerticleDeployment.completer());  // <2>
		}*/

		wikiDbQueue = config().getString(CONFIG_WIKIDB_QUEUE, "wikidb.queue");  // <2>

		regRouter = Router.router(vertx);
		regRouter.get("/registerPage").handler(this::pageRegisterHandler);
		regRouter.post().handler(BodyHandler.create());
		regRouter.post("/register").handler(this::registerHandler);
	}
	
	
	public void registerHandler(RoutingContext context) {
			
		String username = context.request().getParam("username");
		String password = context.request().getParam("password");
			
		JsonObject request = new JsonObject();
		request.put("username", username);
		request.put("password", password);
		DeliveryOptions options = new DeliveryOptions().addHeader("action", "register");
		vertx.eventBus().send(wikiDbQueue, request, options, reply -> {});
			
		options = new DeliveryOptions().addHeader("action", "register");
		vertx.eventBus().send(wikiDbQueue, request, options, reply -> {

			if (reply.succeeded()) {
				// JsonObject body = (JsonObject) reply.result().body();

				context.put("title", "注册成功");
				context.put("username", request.getString("username"));
				context.put("timestamp", new Date().toString());

				templateEngine.render(context, "templates","/main.ftl", ar -> {
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
	 
	public void pageRegisterHandler(RoutingContext context) {

		DeliveryOptions options = new DeliveryOptions().addHeader("action", "register"); // <2>

		vertx.eventBus().send(wikiDbQueue, new JsonObject(), options, reply -> {  // <1>
			if (reply.succeeded()) {
				//   JsonObject body = (JsonObject) reply.result().body();   // <3>
				context.put("title", "Register");
				//  context.put("pages", body.getJsonArray("pages").getList());
				templateEngine.render(context, "templates", "/register.ftl", ar -> {
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
