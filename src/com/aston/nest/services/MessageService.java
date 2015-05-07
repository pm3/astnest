package com.aston.nest.services;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aston.nest.domain.Message;
import com.aston.nest.domain.MessageType;
import com.aston.nest.domain.RoomMessage;
import com.aston.utils.sql.Dbc;
import com.aston.utils.sql.convert.StringArrayConverter;

public class MessageService implements IMessageService {

	private Dbc dbc;
	private IAttachmentService attachmentService;

	public MessageService(Dbc dbc, IAttachmentService attachmentService) {
		this.dbc = dbc;
		this.attachmentService = attachmentService;
	}

	@Override
	public Message load(long id) throws SQLException {
		return dbc.load(Message.class, id);
	}

	@Override
	public boolean hasMessageChilds(long parentId) throws SQLException {
		Integer count = dbc.select1(Dbc.singleInt, "select count(*) from sb_message where parent_id=?", parentId);
		return count != null && count.intValue() > 0;
	}

	@Override
	public long createPost(long roomId, long userId, String post, String[] images) throws SQLException {
		Message c = new Message();
		c.setRoomId(roomId);
		c.setUserId(userId);
		c.setParentId(null);
		c.setType(MessageType.post);
		c.setText(post);
		c.setImages(StringArrayConverter.toString(images, "\n"));
		c.setModified(new Date());
		dbc.save(c);
		return c.getId();
	}

	@Override
	public long createComment(long roomId, long postId, long userId, String comment, String[] images) throws SQLException {
		Message c = new Message();
		c.setRoomId(roomId);
		c.setUserId(userId);
		c.setParentId(postId);
		c.setType(MessageType.comment);
		c.setText(comment);
		c.setImages(StringArrayConverter.toString(images, "\n"));
		c.setModified(new Date());
		dbc.save(c);
		return c.getId();
	}

	@Override
	public long createQuestion(long roomId, long userId, String question, boolean multi, String[] answers, String[] images) throws SQLException {

		Message q = new Message();
		q.setRoomId(roomId);
		q.setUserId(userId);
		q.setParentId(null);
		q.setType(MessageType.question);
		q.setText(question);
		q.setImages(StringArrayConverter.toString(images, "\n"));
		q.setModified(new Date());
		dbc.save(q);

		if (answers != null) {
			for (String answer : answers) {
				Message a = new Message();
				a.setRoomId(roomId);
				a.setUserId(userId);
				a.setParentId(q.getId());
				a.setType(multi ? MessageType.multiAnswer : MessageType.singleAnswer);
				a.setText(answer);
				a.setModified(q.getModified());
				dbc.save(a);
			}
		}

		return q.getId();
	}

	@Override
	public void updateMessage(long id, String text) throws SQLException {
		dbc.update("update sb_message set mtext=? where id=?", text, id);
	}

	@Override
	public void deleteMessage(long messageId) throws SQLException {
		dbc.update("delete from sb_like where message_id in (select id from sb_message where parent_id=?) or message_id=?", messageId, messageId);
		dbc.update("delete from sb_message where parent_id=?", messageId);
		dbc.update("delete from sb_message where id=?", messageId);
	}

	@Override
	public void saveLike(long userId, long roomId, long messageId) throws SQLException {
		dbc.update("insert into sb_like (room_id,message_id,user_id,created) values (?,?,?,current_timestamp)", roomId, messageId, userId);
		dbc.update("update sb_message set likes=likes+1 where id=?", messageId);
	}

	@Override
	public int likeCount(long messageId) throws SQLException {
		return dbc.select1(Dbc.singleInt, "select likes from sb_message where id=?", messageId);
	}

	@Override
	public List<String> selectLikes(long messageId) throws SQLException {
		return dbc.select(Dbc.singleString, "select u.nick from sb_user u, sb_like l where u.id=l.user_id and l.message_id=? order by u.nick", messageId);
	}

	@Override
	public List<RoomMessage> selectRoomMessages(long roomId, long userId, long last) throws SQLException {
		if (last <= 0)
			last = Long.MAX_VALUE - 1;
		StringBuilder sql1 = new StringBuilder();
		sql1.append("select distinct m.id, u.id as creator_id, u.nick as creator_nick, u.avatar_img as creator_avatar_img, ");
		sql1.append("m.mtype, m.mtext, m.images, m.likes, m.modified, ");
		sql1.append("l.message_id is not null as mylike ");
		sql1.append("from sb_message m ");
		sql1.append("inner join sb_user u on m.user_id=u.id ");
		sql1.append("left join sb_like l on m.id=l.message_id and l.user_id=? ");
		sql1.append("where m.room_id=? and m.parent_id is null and m.id<? order by m.id desc limit 50");
		List<RoomMessage> l = dbc.select(RoomMessage.class, sql1.toString(), userId, roomId, last);

		if (l.size() > 0) {

			Map<Long, RoomMessage> map = new HashMap<Long, RoomMessage>(l.size());
			for (RoomMessage rc : l)
				map.put(rc.getId(), rc);

			StringBuilder sql3 = new StringBuilder();

			sql3.append("select distinct m.id, m.parent_id, u.id as creator_id, u.nick as creator_nick, u.avatar_img as creator_avatar_img, ");
			sql3.append("m.mtype, m.mtext, m.images, m.likes, m.modified, ");
			sql3.append("l.message_id is not null as mylike ");
			sql3.append("from sb_message m ");
			sql3.append("inner join sb_user u on m.user_id=u.id ");
			sql3.append("left join sb_like l on m.id=l.message_id and l.user_id=? ");
			sql3.append("where m.room_id=? and m.parent_id>=? and m.parent_id<=? order by m.id asc");
			List<RoomMessage> l3 = dbc.select(RoomMessage.class, sql3.toString(), userId, roomId, l.get(l.size() - 1).getId(), l.get(0).getId());

			for (RoomMessage child : l3) {
				RoomMessage parent = map.get(child.getParentId());
				if (parent != null) {
					List<RoomMessage> l4 = parent.getChildren();
					if (l4 == null) {
						l4 = new ArrayList<RoomMessage>();
						parent.setChildren(l4);
					}
					l4.add(child);
				}
			}
		}

		return l;
	}

	public List<RoomMessage> selectDashboardMessages(long userId, long last) throws SQLException {
		if (last <= 0)
			last = Long.MAX_VALUE - 1;
		StringBuilder sql1 = new StringBuilder();
		sql1.append("select distinct m.id, null as parent_id, u.id as creator_id, u.nick as creator_nick, u.avatar_img as creator_avatar_img, ");
		sql1.append("m.mtype, m.mtext, m.images, m.likes, m.modified, ");
		sql1.append("l.message_id is not null as mylike ");
		sql1.append("from sb_message m ");
		sql1.append("inner join sb_user u on m.user_id=u.id ");
		sql1.append("inner join sb_room_user ru on m.room_id=ru.room_id and ru.user_id=? ");
		sql1.append("left join sb_like l on m.id=l.message_id and l.user_id=? ");
		sql1.append("where m.parent_id is null and m.id<? order by m.id desc limit 50");
		List<RoomMessage> l = dbc.select(RoomMessage.class, sql1.toString(), userId, userId, last);

		if (l.size() > 0) {

			Map<Long, RoomMessage> map = new HashMap<Long, RoomMessage>(l.size());
			for (RoomMessage rc : l)
				map.put(rc.getId(), rc);

			StringBuilder sql3 = new StringBuilder();
			sql3.append("select distinct m.id, m.parent_id, u.id as creator_id, u.nick as creator_nick, u.avatar_img as creator_avatar_img, ");
			sql3.append("m.mtype, m.mtext, m.images, m.likes, m.modified, ");
			sql3.append("l.message_id is not null as mylike ");
			sql3.append("from sb_message m ");
			sql3.append("inner join sb_user u on m.user_id=u.id ");
			sql3.append("inner join sb_room_user ru on m.room_id=ru.room_id and ru.user_id=? ");
			sql3.append("left join sb_like l on m.id=l.message_id and l.user_id=? ");
			sql3.append("where m.parent_id>=? and m.parent_id<=? order by m.id asc");
			List<RoomMessage> l3 = dbc.select(RoomMessage.class, sql3.toString(), userId, userId, l.get(l.size() - 1).getId(), l.get(0).getId());

			for (RoomMessage child : l3) {
				RoomMessage parent = map.get(child.getParentId());
				if (parent != null) {
					List<RoomMessage> l4 = parent.getChildren();
					if (l4 == null) {
						l4 = new ArrayList<RoomMessage>();
						parent.setChildren(l4);
					}
					l4.add(child);
				}
			}

		}
		return l;
	}

	@Override
	public List<RoomMessage> seletNewRoomMessages(long roomId, long userId, long lastId) throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("select distinct m.id, m.parent_id, u.id as creator_id, u.nick as creator_nick, u.avatar_img as creator_avatar_img, ");
		sql.append("m.mtype, m.mtext, m.images, m.likes, m.modified, ");
		sql.append("l.message_id is not null as mylike ");
		sql.append("from sb_message m ");
		sql.append("inner join sb_user u on m.user_id=u.id ");
		sql.append("left join sb_like l on m.id=l.message_id and l.user_id=? ");
		sql.append("where m.room_id=? and m.id>? order by m.id asc limit 51");
		List<RoomMessage> l = dbc.select(RoomMessage.class, sql.toString(), userId, roomId, lastId);
		return l;
	}

	@Override
	public List<RoomMessage> seletNewDashboardMessages(long userId, long lastId) throws SQLException {

		StringBuilder sql = new StringBuilder();
		sql.append("select distinct m.id, m.parent_id, u.id as creator_id, u.nick as creator_nick, u.avatar_img as creator_avatar_img, ");
		sql.append("m.mtype, m.mtext, m.images, m.likes, m.modified, ");
		sql.append("l.message_id is not null as mylike ");
		sql.append("from sb_message m ");
		sql.append("inner join sb_user u on m.user_id=u.id ");
		sql.append("inner join sb_room_user ru on m.room_id=ru.room_id and ru.user_id=? ");
		sql.append("left join sb_like l on m.id=l.message_id and l.user_id=? ");
		sql.append("where m.id>? order by m.id asc limit 51");
		List<RoomMessage> l = dbc.select(RoomMessage.class, sql.toString(), userId, userId, lastId);
		return l;
	}

	@Override
	public List<RoomMessage> selectQuestionAnswers(long questionId) throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("select distinct m.id, m.parent_id, u.id as creator_id, u.nick as creator_nick, u.avatar_img as creator_avatar_img, ");
		sql.append("m.mtype, m.mtext, m.modified ");
		sql.append("from sb_message m ");
		sql.append("inner join sb_user u on m.user_id=u.id ");
		sql.append("where m.parent_id=? order by m.id asc");
		return dbc.select(RoomMessage.class, sql.toString(), questionId);
	}

	@Override
	public String saveMessageImg(Long id, String name, InputStream is) throws SQLException, IOException {
		if (id == null || id <= 0) {
			id = dbc.select1(Dbc.singleLong, "select nextval('message_img_sequence')");
		}
		return attachmentService.saveAttachment("msg", id, name, is);
	}
}
