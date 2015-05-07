package astnest;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

import com.aston.utils.StreamHelper;
import com.google.gson.JsonElement;

public class TestUser {
	public static void main(String[] args) {
		try {
			TestUser tu = new TestUser();
			tu.test();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// @Test
	public void test() throws Exception {

		HttpClient client = new HttpClient();
		client.getHostConfiguration().setHost("localhost", 8080);
		client.getParams().setAuthenticationPreemptive(true);
		client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("su1@aston.sk", "su1@aston.sk"));

		HttpBuilder.create(client, "/astnest/user/user-info").sendGetStr();

		// long roomId
		HttpBuilder.create(client, "/astnest/user/select-room-users").add("roomId", "6").sendGetStr();

		// long roomId, String post
		JsonElement e0 = HttpBuilder.create(client, "/astnest/user/create-post").add("roomId", "6").add("post", "prvy komment").sendGetJson();
		String scid = e0.getAsJsonObject().get("id").getAsString();
		System.out.println(e0.toString());

		// long parentId, String comment
		JsonElement e1 = HttpBuilder.create(client, "/astnest/user/create-comment").add("parentId", scid).add("comment", "prvy sub komment").sendGetJson();
		String scid11 = e1.getAsJsonObject().get("id").getAsString();
		System.out.println(e1.toString());

		// long id, String text
		HttpBuilder.create(client, "/astnest/user/update-message").add("id", scid).add("text", "prvy update komment").sendGetStr();

		// long id
		JsonElement e2 = HttpBuilder.create(client, "/astnest/user/create-post").add("roomId", "6").add("post", "druhy komment").sendGetJson();
		String scid2 = e2.getAsJsonObject().get("id").getAsString();
		HttpBuilder.create(client, "/astnest/user/delete-message").add("id", scid2).sendGetStr();

		// long roomId, String question, boolean multi, String[] answers, String[] images
		HttpBuilder b3 = HttpBuilder.create(client, "/astnest/user/create-question").add("roomId", "6").add("question", "druhy komment").add("multi", "true");
		b3.add("answers", "a1").add("answers", "a2").add("answers", "a3");
		b3.add("images", "/i1").add("images", "/i2").add("images", "/i3");
		b3.sendGetStr();

		// long messageId
		HttpBuilder.create(client, "/astnest/user/add-like").add("messageId", scid).sendGetStr();
		HttpBuilder.create(client, "/astnest/user/add-like").add("messageId", scid11).sendGetStr();

		// long messageId
		HttpBuilder.create(client, "/astnest/user/select-message-likes").add("messageId", scid).sendGetStr();

		// long roomId, int page
		HttpBuilder.create(client, "/astnest/user/select-room-messages").add("roomId", "6").add("last", "0").sendGetStr();

		// int page
		HttpBuilder.create(client, "/astnest/user/select-dashboard-messages").add("last", "0").sendGetStr();

		// long roomId, long lastId
		HttpBuilder.create(client, "/astnest/user/select-room-new-messages").add("roomId", "6").add("lastId", "0").sendGetStr();

		// long lastId
		HttpBuilder.create(client, "/astnest/user/select-dashboard-new-messages").add("lastId", "0").sendGetStr();

		// @Path(name = "/user/update-avatar", contentType = "text/plain")
		// @Path(name = "/user/update-background", contentType = "text/plain")

		byte[] data = StreamHelper.file2bytea(new File("matthew128.png"));
		String ppath = HttpBuilder.multipart(client, "/astnest/user/save-message-img").add("name", "matthew128.png").add("img", "matthew128.png", data, "image/png")
				.sendPostString();
		System.out.println("/astnest" + ppath);
		byte[] data2 = HttpBuilder.create(client, "/astnest" + ppath).sendGet();
		if (Arrays.equals(data, data2))
			System.out.println("sucess load matthew128.png");
		else
			System.err.println("error load matthew128.png");
	}
}
