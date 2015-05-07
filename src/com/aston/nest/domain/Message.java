package com.aston.nest.domain;

import java.util.Date;

import com.aston.utils.sql.anot.Column;
import com.aston.utils.sql.anot.Table;

@Table(name = "sb_message", camelize = true)
public class Message {

	private long id;
	private Long parentId;
	private long roomId;
	private long userId;
	@Column(name = "mtype")
	private MessageType type;
	@Column(name = "mtext")
	private String text;
	private String images;
	private Date modified;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public long getRoomId() {
		return roomId;
	}

	public void setRoomId(long roomId) {
		this.roomId = roomId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getImages() {
		return images;
	}

	public void setImages(String images) {
		this.images = images;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

}
