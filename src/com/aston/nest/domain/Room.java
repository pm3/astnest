package com.aston.nest.domain;

import java.util.Date;

import com.aston.utils.sql.anot.Table;

@Table(name = "sb_room", camelize = true)
public class Room {

	private long id;
	private String name;
	private String path;
	private String description;
	private boolean active;
	private String logoImg;
	private String backgroundImg;
	private boolean autoSynchronized;
	private Date modified;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
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

	public boolean isAutoSynchronized() {
		return autoSynchronized;
	}

	public void setAutoSynchronized(boolean autoSynchronized) {
		this.autoSynchronized = autoSynchronized;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

}
