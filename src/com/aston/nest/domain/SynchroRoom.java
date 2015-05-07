package com.aston.nest.domain;

import java.util.List;

public class SynchroRoom {

	private String name;
	private String path;
	private String description;
	private List<SynchroRoomGroup> groups;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<SynchroRoomGroup> getGroups() {
		return groups;
	}

	public void setGroups(List<SynchroRoomGroup> groups) {
		this.groups = groups;
	}

	@Override
	public String toString() {
		return "SynchroRoom [name=" + name + ", path=" + path + ", description=" + description + ", groups=" + groups + "]";
	}

	public static class SynchroRoomGroup {
		private String name;
		private RoomAccess access;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public RoomAccess getAccess() {
			return access;
		}

		public void setAccess(RoomAccess access) {
			this.access = access;
		}

		@Override
		public String toString() {
			return "SynchroRoomGroup [name=" + name + ", access=" + access + "]";
		}

	}
}
