package com.aston.nest.domain;

import java.util.List;

import com.aston.utils.sql.anot.Table;

@Table(camelize = true, id = "email")
public class UserInfo {
	private String nick;
	private String email;
	private String phone;
	private String orgUnit;
	private String workPosition;
	private String avatarImg;
	private String backgroundImg;
	private boolean admin;
	private List<MyRoom> rooms;

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getOrgUnit() {
		return orgUnit;
	}

	public void setOrgUnit(String orgUnit) {
		this.orgUnit = orgUnit;
	}

	public String getWorkPosition() {
		return workPosition;
	}

	public void setWorkPosition(String workPosition) {
		this.workPosition = workPosition;
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

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public List<MyRoom> getRooms() {
		return rooms;
	}

	public void setRooms(List<MyRoom> rooms) {
		this.rooms = rooms;
	}

	public static class MyRoom {

		private long id;
		private String name;
		private String path;
		private String logoImg;
		private String backgroundImg;
		private RoomAccess access;

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getLogoImg() {
			return logoImg;
		}

		public void setLogoImg(String logoImg) {
			this.logoImg = logoImg;
		}

		public String getBackgroundImg() {
			return backgroundImg;
		}

		public void setBackgroundImg(String backgroundImg) {
			this.backgroundImg = backgroundImg;
		}

		public RoomAccess getAccess() {
			return access;
		}

		public void setAccess(RoomAccess access) {
			this.access = access;
		}

	}

}
