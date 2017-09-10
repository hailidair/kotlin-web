package name.guoyuntao.web.verticle.util;
import java.util.UUID;

public class UUId{
	
	
	public static String getUUID(){
		UUID uuid = UUID.randomUUID();
		return uuid.toString().replaceAll("-", "");
	}

}
