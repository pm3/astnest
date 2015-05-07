package com.aston.nest.domain;

import java.util.List;

public class SynchroUser {

	private String nick;
	private String email;
	private String orgUnit;
	private String workPosition;
	private boolean admin;
	private List<String> groups;

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

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public List<String> getGroups() {
		return groups;
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

	@Override
	public String toString() {
		return "SynchroUser [nick=" + nick + ", email=" + email + ", admin=" + admin + ", groups=" + groups + "]";
	}
}
