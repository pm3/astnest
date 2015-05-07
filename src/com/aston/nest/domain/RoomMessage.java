package com.aston.nest.domain;

import java.util.Date;
import java.util.List;

import com.aston.utils.sql.anot.Column;
import com.aston.utils.sql.anot.Table;
import com.aston.utils.sql.convert.StringArrayConverter;

@Table(camelize = true)
public class RoomMessage {

	private long id;
	private Long parentId;
	private long creatorId;
	private String creatorNick;
	private String creatorAvatarImg;

	@Column(name = "mtype")
	private MessageType type;
	@Column(name = "mtext")
	private String text;
	@Column(convert = StringArrayConverter.class)
	private String[] images;
	private int likes;
	private Boolean mylike;
	private Date modified;

	List<RoomMessage> children;

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

	public long getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(long creatorId) {
		this.creatorId = creatorId;
	}

	public String getCreatorNick() {
		return creatorNick;
	}

	public void setCreatorNick(String creatorNick) {
		this.creatorNick = creatorNick;
	}

	public String getCreatorAvatarImg() {
		return creatorAvatarImg;
	}

	public void setCreatorAvatarImg(String creatorAvatarImg) {
		this.creatorAvatarImg = creatorAvatarImg;
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

	public void setImages(String[] images) {
		this.images = images;
	}

	public String[] getImages() {
		return images;
	}

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	public Boolean getMylike() {
		return mylike;
	}

	public void setMylike(Boolean mylike) {
		this.mylike = mylike;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public List<RoomMessage> getChildren() {
		return children;
	}

	public void setChildren(List<RoomMessage> children) {
		this.children = children;
	}

}
