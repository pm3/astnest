package com.aston.nest.domain;

import com.aston.utils.sql.anot.Table;

@Table(camelize = true, id = "id")
public class RoomUser {
	private long id;
	private String nick;
	private String avatarImg;
	private RoomAccess access;
	private Boolean withGroup;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getAvatarImg() {
		return avatarImg;
	}

	public void setAvatarImg(String avatarImg) {
		this.avatarImg = avatarImg;
	}

	public RoomAccess getAccess() {
		return access;
	}

	public void setAccess(RoomAccess access) {
		this.access = access;
	}

	public Boolean getWithGroup() {
		return withGroup;
	}

	public void setWithGroup(Boolean withGroup) {
		this.withGroup = withGroup;
	}
}
