package com.aston.nest.servlet;

import java.util.Map;

import com.aston.nest.domain.RoomAccess;

public class UserContext {

	private long id;
	private String nick;
	private String email;
	private boolean admin;
	private String avatarImg;
	private String backgroundImg;
	private Map<Long, RoomAccess> access;
	private String password;

	public UserContext() {
		throw new RuntimeException("UserContext use constructr with params");
	}

	public UserContext(long id, String nick, String email, boolean admin, Map<Long, RoomAccess> access) {
		this.id = id;
		this.nick = nick;
		this.email = email;
		this.admin = admin;
		this.access = access;
	}

	public long getId() {
		return id;
	}

	public String getNick() {
		return nick;
	}

	public String getEmail() {
		return email;
	}

	public void checkRoomViewer(long roomId) throws Exception {
		if (!isRoomViewer(roomId))
			throw new Exception("permition dennied view room " + roomId);
	}

	public void checkRoomEditor(long roomId) throws Exception {
		if (!isRoomEditor(roomId))
			throw new Exception("permition dennied edit room " + roomId);
	}

	public void checkRoomManager(long roomId) throws Exception {
		if (!isRoomManager(roomId))
			throw new Exception("permition dennied manage room " + roomId);
	}

	public void checkAdmin() throws Exception {
		if (!admin)
			throw new Exception("permition dennied administrate");
	}

	public boolean isRoomViewer(long roomId) {
		if (admin)
			return true;
		RoomAccess a = access.get(roomId);
		return a != null;
	}

	public boolean isRoomEditor(long roomId) {
		if (admin)
			return true;
		RoomAccess a = access.get(roomId);
		return a != null && (RoomAccess.manage.equals(a) || RoomAccess.edit.equals(a));
	}

	public boolean isRoomManager(long roomId) {
		if (admin)
			return true;
		RoomAccess a = access.get(roomId);
		return a != null && RoomAccess.manage.equals(a);
	}

	public boolean isAdmin() {
		return admin;
	}

	public String getAvatarImg() {
		return avatarImg;
	}

	public void setAvatarImg(String avatarImg) {
		this.avatarImg = avatarImg;
	}

	public String getBackgroundImg() {
		return backgroundImg;
	}

	public void setBackgroundImg(String backgroundImg) {
		this.backgroundImg = backgroundImg;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
