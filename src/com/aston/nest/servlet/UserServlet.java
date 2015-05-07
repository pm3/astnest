package com.aston.nest.servlet;

import java.util.Date;
import java.util.List;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.Part;

import com.aston.nest.domain.Message;
import com.aston.nest.domain.MessageType;
import com.aston.nest.domain.RoomMessage;
import com.aston.nest.domain.RoomUser;
import com.aston.nest.domain.UserInfo;
import com.aston.nest.services.IMessageService;
import com.aston.nest.services.IRoomService;
import com.aston.nest.services.IUserService;
import com.aston.utils.servlet.AServlet;
import com.aston.utils.servlet.path.Path;
import com.aston.utils.servlet.provider.Provider;
import com.aston.utils.servlet.provider.RequestProvider;

@MultipartConfig(maxRequestSize = 2097152)
public class UserServlet extends AServlet {

	private static final long serialVersionUID = 1L;

	private IUserService userService;
	private IRoomService roomService;
	private IMessageService messageService;
	private Provider<UserContext> userContext = new RequestProvider<UserContext>(UserContext.class);

	public UserServlet(IUserService userService, IRoomService roomService, IMessageService messageService) {
		this.userService = userService;
		this.roomService = roomService;
		this.messageService = messageService;
	}

	@Path(name = "/user/user-info", contentType = "text/json")
	public UserInfo selectLogedUser(Long userId) throws Exception {
		if (userId == null || userId == 0) {
			userId = userContext.get().getId();
		}
		return userService.loadUserInfo(userId);
	}

	@Path(name = "/user/update-my-avatar-img", contentType = "text/plain")
	public String updateMyAvatarImg(String name, Part img) throws Exception {
		if (img == null || name == null)
			throw new Exception("undefined img part or name");
		String path = userService.updateAvatarImg(userContext.get().getId(), name, img.getInputStream());
		userContext.get().setAvatarImg(path);
		return path;
	}

	@Path(name = "/user/update-my-background-img", contentType = "text/plain")
	public String updateMyBackgroundImg(String name, Part img) throws Exception {
		if (img == null || name == null)
			throw new Exception("undefined img part or name");
		String path = userService.updateBackgroundImg(userContext.get().getId(), name, img.getInputStream());
		userContext.get().setBackgroundImg(path);
		return path;
	}

	@Path(name = "/user/select-room-users", contentType = "text/json")
	public List<RoomUser> selectRoomUsers(long roomId) throws Exception {
		userContext.get().isRoomViewer(roomId);
		return roomService.selectRoomUsers(roomId);
	}

	@Path(name = "/user/create-post", contentType = "text/json")
	public RoomMessage createMyPost(long roomId, String post, String[] images) throws Exception {
		userContext.get().isRoomEditor(roomId);
		long id = messageService.createPost(roomId, userContext.get().getId(), post, images);
		RoomMessage c = new RoomMessage();
		c.setId(id);
		c.setCreatorId(userContext.get().getId());
		c.setCreatorNick(userContext.get().getNick());
		c.setCreatorAvatarImg(userContext.get().getAvatarImg());
		c.setType(MessageType.post);
		c.setText(post);
		c.setImages(images);
		c.setModified(new Date());
		return c;
	}

	@Path(name = "/user/create-comment", contentType = "text/json")
	public RoomMessage createMyComment(long parentId, String comment, String[] images) throws Exception {
		Message p = messageService.load(parentId);
		if (p == null)
			throw new Exception("comment parent not found");
		if (p.getParentId() != null)
			throw new Exception("only post or question parent");
		userContext.get().checkRoomEditor(p.getRoomId());
		long id = messageService.createComment(p.getRoomId(), p.getId(), userContext.get().getId(), comment, images);

		RoomMessage c = new RoomMessage();
		c.setId(id);
		c.setParentId(p.getId());
		c.setCreatorId(userContext.get().getId());
		c.setCreatorNick(userContext.get().getNick());
		c.setCreatorAvatarImg(userContext.get().getAvatarImg());
		c.setType(MessageType.comment);
		c.setText(comment);
		c.setImages(images);
		c.setModified(new Date());
		return c;
	}

	@Path(name = "/user/create-question", contentType = "text/json")
	public RoomMessage createMyQuestion(long roomId, String question, boolean multi, String[] answers, String[] images) throws Exception {
		userContext.get().isRoomEditor(roomId);
		long id = messageService.createQuestion(roomId, userContext.get().getId(), question, multi, answers, images);
		RoomMessage c = new RoomMessage();
		c.setId(id);
		c.setCreatorId(userContext.get().getId());
		c.setCreatorNick(userContext.get().getNick());
		c.setCreatorAvatarImg(userContext.get().getAvatarImg());
		c.setType(MessageType.question);
		c.setText(question);
		c.setImages(images);
		c.setModified(new Date());
		c.setChildren(messageService.selectQuestionAnswers(c.getId()));
		return c;
	}

	@Path(name = "/user/save-message-img", contentType = "text/plain")
	public String saveMessageImg(Long id, String name, Part img) throws Exception {
		if (img == null || name == null)
			throw new Exception("undefined img part or name");
		return messageService.saveMessageImg(id, name, img.getInputStream());
	}

	@Path(name = "/user/update-message", contentType = "text/plain")
	public void updateMessage(long id, String text) throws Exception {
		Message c = messageService.load(id);
		if (c == null)
			throw new IllegalStateException("undefined message");
		UserContext uc = userContext.get();
		if (uc.isAdmin()) {
			messageService.updateMessage(id, text);
		} else if (uc.isRoomManager(c.getRoomId())) {
			messageService.updateMessage(id, text);
		} else if (uc.getId() == c.getUserId()) {
			messageService.updateMessage(id, text);
		} else {
			throw new Exception("update message not permit");
		}
	}

	@Path(name = "/user/delete-message", contentType = "text/plain")
	public void deleteMessage(long id) throws Exception {
		Message c = messageService.load(id);
		if (c == null)
			throw new IllegalStateException("undefined message");
		boolean hasChilds = false;
		if (c.getParentId() == null)
			hasChilds = messageService.hasMessageChilds(id);

		UserContext uc = userContext.get();
		if (uc.isAdmin()) {
			messageService.deleteMessage(id);
		} else if (uc.isRoomManager(c.getRoomId())) {
			messageService.deleteMessage(id);
		} else if (uc.getId() == c.getUserId()) {
			if (hasChilds)
				throw new Exception("deleted message has childs");
			messageService.deleteMessage(id);
		} else {
			throw new Exception("update message not permit");
		}

	}

	@Path(name = "/user/add-like", contentType = "text/json")
	public int saveMyLike(long messageId) throws Exception {
		Message c = messageService.load(messageId);
		if (c == null)
			throw new IllegalStateException("undefined message");
		userContext.get().checkRoomEditor(c.getRoomId());
		messageService.saveLike(userContext.get().getId(), c.getRoomId(), c.getId());
		return messageService.likeCount(c.getId());
	}

	@Path(name = "/user/select-message-likes", contentType = "text/json")
	public List<String> selectLikes(long messageId) throws Exception {
		Message m = messageService.load(messageId);
		if (m == null)
			throw new IllegalStateException("undefined comment");
		userContext.get().checkRoomEditor(m.getRoomId());
		return messageService.selectLikes(messageId);
	}

	@Path(name = "/user/select-room-messages", contentType = "text/json")
	public List<RoomMessage> selectRoomMessages(long roomId, long last) throws Exception {
		userContext.get().checkRoomViewer(roomId);
		return messageService.selectRoomMessages(roomId, userContext.get().getId(), last);
	}

	@Path(name = "/user/select-dashboard-messages", contentType = "text/json")
	public List<RoomMessage> selectMyDashboardMessages(long last) throws Exception {
		return messageService.selectDashboardMessages(userContext.get().getId(), last);
	}

	@Path(name = "/user/select-room-new-messages", contentType = "text/json")
	public List<RoomMessage> selectNewRoomMessages(long roomId, long lastId) throws Exception {
		userContext.get().checkRoomViewer(roomId);
		return messageService.seletNewRoomMessages(roomId, userContext.get().getId(), lastId);
	}

	@Path(name = "/user/select-dashboard-new-messages", contentType = "text/json")
	public List<RoomMessage> selectNewDashboardMessages(Long roomId, long lastId) throws Exception {
		return messageService.seletNewDashboardMessages(userContext.get().getId(), lastId);
	}
}
