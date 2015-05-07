package astnest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

public class TestManager {

	public static void main(String[] args) {
		try {
			TestManager tm = new TestManager();
			tm.test();
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

		String sid = "9";

		// long roomId, String group
		HttpBuilder.create(client, "/astnest/manage/add-room-group").add("roomId", sid).add("group", "g5").add("access", "edit").sendGet();

		// long roomId, String group
		HttpBuilder.create(client, "/astnest/manage/add-room-group").add("roomId", sid).add("group", "g6").sendGet();
		// long roomId, String group
		HttpBuilder.create(client, "/astnest/manage/delete-room-group").add("roomId", sid).add("group", "g6").sendGet();

		long userId = 4;

		// long roomId, long userId, RoomAccess access
		HttpBuilder.create(client, "/astnest/manage/add-room-user").add("roomId", sid).add("userId", userId + "").add("access", "manage").sendGet();

		// long roomId, long userId
		HttpBuilder.create(client, "/astnest/manage/delete-room-user").add("roomId", sid).add("userId", userId + "").sendGet();

		// long id, String name, String path, String description
		HttpBuilder.create(client, "/astnest/manage/update-room").add("id", sid).add("name", "room_update " + System.currentTimeMillis())
				.add("path", "update_" + System.currentTimeMillis()).add("description", "desc updated").sendGet();

		HttpBuilder.create(client, "/astnest/manage/select-users-by-room").add("roomId", sid).sendGetStr();

		// @Path(name = "/manage/update-logo-img", contentType = "text/plain")
		// @Path(name = "/manage/update-background-img", contentType = "text/plain")
	}
}
