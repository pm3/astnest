package com.aston.nest.services;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import com.aston.nest.domain.Message;
import com.aston.nest.domain.RoomMessage;

public interface IMessageService {

	public Message load(long id) throws SQLException;

	public boolean hasMessageChilds(long parentId) throws SQLException;

	public long createPost(long roomId, long userId, String post, String[] images) throws SQLException;

	public long createComment(long roomId, long postId, long userId, String comment, String[] images) throws SQLException;

	public long createQuestion(long roomId, long userId, String question, boolean multi, String[] answers, String[] images) throws SQLException;

	public void updateMessage(long id, String text) throws SQLException;

	public void deleteMessage(long mesageId) throws SQLException;

	public String saveMessageImg(Long id, String name, InputStream is) throws SQLException, IOException;

	public void saveLike(long userId, long roomId, long messageId) throws SQLException;

	public int likeCount(long messageId) throws SQLException;

	public List<String> selectLikes(long messageId) throws SQLException;

	public List<RoomMessage> selectRoomMessages(long roomId, long userId, long last) throws SQLException;

	public List<RoomMessage> selectDashboardMessages(long userId, long last) throws SQLException;

	public List<RoomMessage> seletNewRoomMessages(long roomId, long userId, long lastId) throws SQLException;

	public List<RoomMessage> seletNewDashboardMessages(long userId, long lastId) throws SQLException;

	public List<RoomMessage> selectQuestionAnswers(long questionId) throws SQLException;
}
