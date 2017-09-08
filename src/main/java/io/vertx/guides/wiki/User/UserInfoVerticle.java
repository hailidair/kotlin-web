package io.vertx.guides.wiki.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.guides.wiki.HttpServerVerticle;

public class UserInfoVerticle extends AbstractVerticle {
	 private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

	  public static final String CONFIG_HTTP_SERVER_PORT = "http.server.port";  // <1>
	  public static final String CONFIG_WIKIDB_QUEUE = "wikidb.queue";

	  private String wikiDbQueue = "wikidb.queue";

	@Override
	  public void start(Future<Void> startFuture) throws Exception {
	  /*  Future<String> dbVerticleDeployment = Future.future();  // <1>
	    vertx.deployVerticle(new WikiDatabaseVerticle(), dbVerticleDeployment.completer());  // <2>
	    }*/
	}
}
