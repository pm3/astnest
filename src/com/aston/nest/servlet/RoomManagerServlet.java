package com.aston.nest.servlet;

import java.util.List;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.Part;

import com.aston.nest.domain.Room;
import com.aston.nest.domain.RoomAccess;
import com.aston.nest.domain.RoomUser;
import com.aston.nest.services.IRoomService;
import com.aston.nest.services.IUserService;
import com.aston.utils.servlet.AServlet;
import com.aston.utils.servlet.path.Path;
import com.aston.utils.servlet.provider.Provider;
import com.aston.utils.servlet.provider.RequestProvider;

@MultipartConfig(maxRequestSize = 2097152)
public class RoomManagerServlet extends AServlet {

	private static final long serialVersionUID = 1L;
	private IUserService userService;
	private IRoomService roomService;
	private Provider<UserContext> userContext = new RequestProvider<UserContext>(UserContext.class);

	public RoomManagerServlet(IUserService userService, IRoomService roomService) {
		this.userService = userService;
		this.roomService = roomService;
	}

	@Path(name = "/manage/add-room-group", contentType = "text/plain")
	public void addRoomGroup(long roomId, String group, RoomAccess access) throws Exception {
		userContext.get().checkRoomManager(roomId);
		userService.addRoomGroup(roomId, group, access);
		userService.synchroRoomUserGroups(roomId, 0);
	}

	@Path(name = "/manage/delete-room-group", contentType = "text/plain")
	public void deleteRoomGroup(long roomId, String group) throws Exception {
		userContext.get().checkRoomManager(roomId);
		userService.deleteRoomGroup(roomId, group);
		userService.synchroRoomUserGroups(roomId, 0);
	}

	@Path(name = "/manage/update-room-group-access", contentType = "text/plain")
	public void updateRoomGroupAccess(long roomId, String group, RoomAccess access) throws Exception {
		userContext.get().checkRoomManager(roomId);
		userService.updateRoomGroupAccess(roomId, group, access);
		userService.synchroRoomUserGroups(roomId, 0);
	}

	@Path(name = "/manage/add-room-user", contentType = "text/plain")
	public void addRoomUser(long roomId, long userId, RoomAccess access) throws Exception {
		userContext.get().checkRoomManager(roomId);
		userService.addRoomUser(roomId, userId, access);
		userService.synchroRoomUserGroups(roomId, userId);
	}

	@Path(name = "/manage/delete-room-user", contentType = "text/plain")
	public void deleteRoomUser(long roomId, long userId) throws Exception {
		userContext.get().checkRoomManager(roomId);
		userService.deleteRoomUser(roomId, userId);
		userService.synchroRoomUserGroups(roomId, userId);
	}

	@Path(name = "/manage/update-room-user-access", contentType = "text/plain")
	public void updateRoomUserAccess(long roomId, long userId, RoomAccess access) throws Exception {
		userContext.get().checkRoomManager(roomId);
		userService.updateRoomUserAccess(roomId, userId, access);
	}

	@Path(name = "/manage/update-room", contentType = "text/plain")
	public void updateRoom(long id, String name, String path, String description) throws Exception {
		userContext.get().checkRoomManager(id);
		Room r = roomService.loadRoom(id);
		if (r.isAutoSynchronized())
			throw new Exception("can't update auto synchronized room");
		roomService.updateRoom(id, name, path, description);
	}

	@Path(name = "/manage/update-logo-img", contentType = "text/plain")
	public String updateLogoImg(long roomId, String name, Part img) throws Exception {
		if (img == null || name == null)
			throw new Exception("undefined img part or name");
		userContext.get().checkRoomManager(roomId);
		return roomService.updateLogoImg(roomId, name, img.getInputStream());
	}

	@Path(name = "/manage/update-background-img", contentType = "text/plain")
	public String updateBackgroundImg(long roomId, String name, Part img) throws Exception {
		if (img == null || name == null)
			throw new Exception("undefined img part or name");

		userContext.get().checkRoomManager(roomId);
		return roomService.updateBackgroundImg(roomId, name, img.getInputStream());
	}

	@Path(name = "/manage/select-users-by-room", contentType = "text/json")
	public List<RoomUser> selectUsersByRoom(long roomId) throws Exception {
		userContext.get().checkRoomManager(roomId);
		return roomService.selectAllUsersByRoom(roomId);
	}
}
