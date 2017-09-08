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
import io.vertx.guides.wiki.util.UUId;

public class LoginVerticle extends AbstractVerticle {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

	public static final String CONFIG_HTTP_SERVER_PORT = "http.server.port";  // <1>
	public static final String CONFIG_WIKIDB_QUEUE = "wikidb.queue";

	private String wikiDbQueue = "wikidb.queue";
	
	private final FreeMarkerTemplateEngine templateEngine = FreeMarkerTemplateEngine.create();
	
	@Override
	public void start(Future<Void> startFuture) throws Exception {
		wikiDbQueue = config().getString(CONFIG_WIKIDB_QUEUE, "wikidb.queue");  // <2>

		HttpServer server = vertx.createHttpServer();

		Router router = Router.router(vertx);
		router.get("/").handler(this::indexHandler);
		router.post().handler(BodyHandler.create());
		router.post("/login").handler(this::loginHandler);

		int portNumber = config().getInteger(CONFIG_HTTP_SERVER_PORT, 8080);  // <3>
		server
	      .requestHandler(router::accept)
	      .listen(portNumber, ar -> {
	        if (ar.succeeded()) {
	          LOGGER.info("HTTP server running on port " + portNumber);
	          startFuture.complete();
	        } else {
	          LOGGER.error("Could not start a HTTP server", ar.cause());
	          startFuture.fail(ar.cause());
	        }
	      });
	}
	
	private void indexHandler(RoutingContext context) {

		DeliveryOptions options = new DeliveryOptions().addHeader("action", "all-pages"); // <2>

		vertx.eventBus().send(wikiDbQueue, new JsonObject(), options, reply -> {  // <1>
			if (reply.succeeded()) {
				LOGGER.info("================="+ UUId.getUUID().toString());
//				JsonObject body = (JsonObject) reply.result().body();   // <3>

				context.put("title", "Login");
				//context.put("pages", body.getJsonArray("pages").getList());
				templateEngine.render(context, "templates", "/login.ftl", ar -> {
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
	
	private void loginHandler(RoutingContext context) {
		
		String username = context.request().getParam("username");
		String password = context.request().getParam("password");
		JsonObject request = new JsonObject();
		request.put("username", username);
		request.put("password", password);
		LOGGER.info("username="+request.getString("username"));

		DeliveryOptions options = new DeliveryOptions().addHeader("action", "login");
		vertx.eventBus().send(wikiDbQueue, request, options, reply -> {

			if (reply.succeeded()) {
				JsonObject body = (JsonObject) reply.result().body();
		// context.put("user", body.getString("user"));
				context.put("title", request.getString("会员中心"));
				context.put("username", body.getString("user"));
				context.put("timestamp", new Date().toString());
				LOGGER.info("-----------------"+context.get("username"));

				templateEngine.render(context, "templates","/mainPage.ftl", ar -> {
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
