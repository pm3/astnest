package com.aston.nest.servlet;

import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.xml.bind.DatatypeConverter;

import com.aston.nest.domain.Room;
import com.aston.nest.domain.SynchroRoom;
import com.aston.nest.domain.SynchroUser;
import com.aston.nest.domain.User;
import com.aston.nest.services.IRoomService;
import com.aston.nest.services.IUserService;
import com.aston.utils.MethodParamNameParser;
import com.aston.utils.mail.MailData;
import com.aston.utils.mail.MailFactory;
import com.aston.utils.servlet.AServlet;
import com.aston.utils.servlet.path.Path;
import com.aston.utils.servlet.provider.Provider;
import com.aston.utils.servlet.provider.RequestProvider;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class AdminServlet extends AServlet {

	private static final long serialVersionUID = 1L;

	private IUserService userService;
	private IRoomService roomService;
	private Provider<UserContext> userContext = new RequestProvider<UserContext>(UserContext.class);
	private MailFactory mailFactory;

	public AdminServlet(IUserService userService, IRoomService roomService, MailFactory mailFactory) {
		this.userService = userService;
		this.roomService = roomService;
		this.mailFactory = mailFactory;
	}

	@Path(name = "/admin/select-all-rooms", contentType = "text/json")
	public List<Room> selectAllRooms() throws Exception {
		userContext.get().checkAdmin();
		return roomService.selectAllRooms();
	}

	@Path(name = "/admin/synchro-users", contentType = "text/plain")
	public void synchroUsers(Reader reader) throws Exception {
		userContext.get().checkAdmin();

		Gson g = new GsonBuilder().create();
		Type listType = new TypeToken<List<SynchroUser>>() {
		}.getType();
		List<SynchroUser> l = g.fromJson(reader, listType);

		userService.synchroUsers(l);
		userService.synchroRoomUserGroups(0, 0);
	}

	@Path(name = "/admin/synchro-rooms", contentType = "text/plain")
	public void synchroSystemRooms(Reader reader) throws Exception {
		userContext.get().checkAdmin();

		Gson g = new GsonBuilder().create();
		Type listType = new TypeToken<List<SynchroRoom>>() {
		}.getType();
		List<SynchroRoom> l = g.fromJson(reader, listType);

		roomService.synchroSystemRooms(l);
		userService.synchroRoomUserGroups(0, 0);
	}

	@Path(name = "/admin/create-room", contentType = "text/plain")
	public long createRoom(String name, String path, String description, boolean active) throws Exception {
		userContext.get().checkAdmin();
		Room r = new Room();
		r.setName(name);
		r.setPath(path != null ? path : name);
		r.setDescription(description);
		r.setActive(active);
		r.setAutoSynchronized(false);
		r.setModified(new Date());
		long roomId = roomService.createRoom(r);
		userService.synchroRoomUserGroups(roomId, 0);
		return roomId;
	}

	@Path(name = "/admin/update-room", contentType = "text/plain")
	public void updateRoomState(long id, boolean active) throws Exception {
		userContext.get().checkAdmin();
		Room r = roomService.loadRoom(id);
		if (r.isAutoSynchronized())
			throw new Exception("can't update auto synchronized room");
		roomService.updateRoomState(id, active);
	}

	@Path(name = "/admin/select-all-users", contentType = "text/json")
	public List<User> selectAllUsers() throws Exception {
		userContext.get().checkAdmin();
		return userService.selectAllUsers();
	}

	@Path(name = "/admin/create-user", contentType = "text/plain")
	public void createUser(String nick, String email, String phone, String orgUnit, String workPosition, boolean admin, String[] groups) throws Exception {
		userContext.get().checkAdmin();
		if (!userContext.get().getNick().equals("admin"))
			throw new Exception("permition dennied");

		User u = new User();
		u.setNick(nick);
		u.setEmail(email);
		u.setPhone(phone);
		u.setOrgUnit(orgUnit);
		u.setWorkPosition(workPosition);
		u.setActive(true);
		u.setAdmin(admin);
		String password = generatePassword();
		userService.createUser(u, encodePassword(password, null), groups);

		MailData md = new MailData();
		md.addMailTo(u.getEmail());
		md.setSubject("Nové heslo do aplikácie nest");
		md.setBody("v alikácii <a href=\"https://www.aston.sk/nest/\">https://www.aston.sk/nest</a> Vám bol vytvorený účet " + email + " / " + password);
		md.setHtml(true);
		mailFactory.sendMail(md);

		userService.synchroRoomUserGroups(0, u.getId());
	}

	public static String encodePassword(String password, String salt) {
		if (password == null)
			return null;
		if (salt == null)
			salt = new BigInteger(32, new SecureRandom()).toString(32);
		String md5 = md5(salt + password + salt);
		return salt + ":" + md5;
	}

	public static String md5(String s) {
		String s2 = null;
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(s.getBytes("utf-8"));
			byte messageDigest[] = digest.digest();
			s2 = DatatypeConverter.printHexBinary(messageDigest).toLowerCase();
		} catch (Exception e) {
			throw new SecurityException(e);
		}
		return s2;
	}

	public static String generatePassword() {
		String letters = "abcdefghjkmnpqrstuvwxyz1234567890+@#";
		Random random = new SecureRandom();
		StringBuilder pw = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			int index = (int) (random.nextDouble() * letters.length());
			pw.append(letters.substring(index, index + 1));
		}
		return pw.toString();
	}

	public static void main(String[] args) {
		try {
			data(UserServlet.class);
			System.out.println();
			data(RoomManagerServlet.class);
			System.out.println();
			data(AdminServlet.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void data(Class<?> cl) {
		try {
			MethodParamNameParser.prepareClass(cl, Path.class);
			List<String> l = new ArrayList<String>();
			for (Method m : cl.getMethods()) {
				Path p = m.getAnnotation(Path.class);
				if (p == null)
					continue;
				StringBuilder sb = new StringBuilder();
				sb.append(p.name()).append("   ");
				String names[] = MethodParamNameParser.params(m);
				if (names != null) {
					for (int i = 0; i < names.length; i++) {
						if (i > 0)
							sb.append(", ");
						sb.append(names[i]).append(":").append(m.getParameterTypes()[i].getSimpleName());
					}
				}
				l.add(sb.toString());
			}
			Collections.sort(l);
			for (String s : l)
				System.out.println(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
