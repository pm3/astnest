package astnest;

import java.io.File;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import com.aston.utils.StreamHelper;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestAdmin {

	public static void main(String[] args) {
		try {
			TestAdmin ta = new TestAdmin();
			ta.test();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// @Test
	public void test() throws Exception {

		HttpClient client = new HttpClient();
		client.getHostConfiguration().setHost("localhost", 8080);
		client.getParams().setAuthenticationPreemptive(true);
		client.getState().setCredentials(new AuthScope("localhost", 8080, AuthScope.ANY_REALM), new UsernamePasswordCredentials("pm@aston.sk", "aston"));

		HttpBuilder.create(client, "/astnest/admin/select-all-users").sendGetStr();

		byte[] dataUsers = StreamHelper.file2bytea(new File("synchro-users-json.txt"));
		HttpBuilder.create(client, "/astnest/admin/synchro-users").sendPostString(dataUsers, "text/json");

		byte[] dataRooms = StreamHelper.file2bytea(new File("synchro-rooms-json.txt"));
		HttpBuilder.create(client, "/astnest/admin/synchro-rooms").sendPostString(dataRooms, "text/json");

		// String name, String path, String description, boolean active
		String sid = HttpBuilder.create(client, "/astnest/admin/create-room").add("name", "room1_" + System.currentTimeMillis()).add("description", "room1 description")
				.add("active", "true").sendPostString();

		long id = Long.parseLong(sid);

		// long id, boolean active
		HttpBuilder.create(client, "/astnest/admin/update-room").add("id", String.valueOf(id)).add("active", "true").sendPostString();

		HttpBuilder.create(client, "/astnest/admin/select-all-rooms").sendGetStr();

		// String name, String path, String description, boolean active
		String sid2 = HttpBuilder.create(client, "/astnest/admin/create-room").add("name", "managed1" + System.currentTimeMillis()).add("description", "managed description")
				.add("active", "true").sendPostString();
		// long roomId, long userId, RoomAccess access
		HttpBuilder.create(client, "/astnest/manage/add-room-user").add("roomId", sid2).add("userId", "2").add("access", "manage").sendGet();
	}
}
