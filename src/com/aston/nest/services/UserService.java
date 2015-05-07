package com.aston.nest.services;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aston.nest.domain.RoomAccess;
import com.aston.nest.domain.SynchroUser;
import com.aston.nest.domain.User;
import com.aston.nest.domain.UserInfo;
import com.aston.nest.domain.UserInfo.MyRoom;
import com.aston.nest.servlet.UserContext;
import com.aston.utils.MergeHelper;
import com.aston.utils.MergeHelper.IMergeLists;
import com.aston.utils.sql.Dbc;
import com.aston.utils.sql.IResult;

public class UserService implements IUserService {

	private Dbc dbc;
	private IAttachmentService attachmentService;

	public UserService(Dbc dbc, IAttachmentService attachmentService) {
		this.dbc = dbc;
		this.attachmentService = attachmentService;
	}

	@Override
	public List<User> selectAllUsers() throws SQLException {
		return dbc.select(User.class, "select * from sb_user where active=true order by id asc");
	}

	class SynchroUserMerge implements IMergeLists<SynchroUser, User> {

		private Map<Long, List<String>> oldUserGroups;

		SynchroUserMerge(Map<Long, List<String>> oldUserGroups) {
			this.oldUserGroups = oldUserGroups;
		}

		@Override
		public Object getNewId(SynchroUser newItem) throws SQLException {
			return newItem.getEmail();
		}

		@Override
		public Object getOldId(User oldItem) throws SQLException {
			return oldItem.getEmail();
		}

		@Override
		public void newItem(SynchroUser newItem) throws SQLException {
			System.out.println("new user" + newItem);
			User u = new User();
			u.setEmail(newItem.getEmail());
			u.setNick(newItem.getNick());
			u.setOrgUnit(newItem.getOrgUnit());
			u.setWorkPosition(newItem.getWorkPosition());
			u.setActive(true);
			u.setAdmin(newItem.isAdmin());
			dbc.save(u);
			dbc.update("update sb_user set password=lower(md5(?)) where email=?", newItem.getEmail(), newItem.getEmail());

			if (newItem.getGroups() != null) {
				try {
					MergeHelper.merge(newItem.getGroups(), new ArrayList<String>(0), new SynchroUserGroupMerge(u.getId()));
				} catch (Exception e) {
					if (e instanceof SQLException)
						throw ((SQLException) e);
					throw new SQLException(e);
				}
			}
		}

		@Override
		public void updateItem(SynchroUser newItem, User oldItem) throws SQLException {
			System.out.println("update user" + newItem);
			oldItem.setEmail(newItem.getEmail());
			oldItem.setOrgUnit(newItem.getOrgUnit());
			oldItem.setWorkPosition(newItem.getWorkPosition());
			oldItem.setAdmin(oldItem.isAdmin());
			oldItem.setActive(true);
			dbc.save(oldItem);

			try {
				List<String> newGroups = newItem.getGroups();
				if (newGroups == null)
					newGroups = new ArrayList<String>(0);
				List<String> oldGroups = oldUserGroups.get(oldItem.getId());
				if (oldGroups == null)
					oldGroups = new ArrayList<String>(0);
				MergeHelper.merge(newGroups, oldGroups, new SynchroUserGroupMerge(oldItem.getId()));
			} catch (Exception e) {
				if (e instanceof SQLException)
					throw ((SQLException) e);
				throw new SQLException(e);
			}
		}

		@Override
		public void deleteItem(User oldItem) throws SQLException {
			dbc.update("update sb_user set active=false where id=?", oldItem.getId());
		}

	}

	class SynchroUserGroupMerge implements IMergeLists<String, String> {

		long userId;

		SynchroUserGroupMerge(long userId) {
			this.userId = userId;
		}

		@Override
		public Object getNewId(String newItem) throws Exception {
			return newItem;
		}

		@Override
		public Object getOldId(String oldItem) throws Exception {
			return oldItem;
		}

		@Override
		public void newItem(String newItem) throws Exception {
			dbc.update("insert into sb_user_group (user_id,name) values (?,?)", userId, newItem);
		}

		@Override
		public void updateItem(String newItem, String oldItem) throws Exception {
		}

		@Override
		public void deleteItem(String oldItem) throws Exception {
			dbc.update("delete from sb_user_group where user_id=? and name=?", userId, oldItem);
		}
	}

	private IResult<Map<Long, List<String>>> mapLongListStringResult = new IResult<Map<Long, List<String>>>() {

		@Override
		public Map<Long, List<String>> result(ResultSet rs) throws SQLException {
			Map<Long, List<String>> m = new HashMap<Long, List<String>>();
			while (rs.next()) {
				long rid = rs.getLong(1);
				String g = rs.getString(2);
				List<String> l = m.get(rid);
				if (l == null) {
					l = new ArrayList<String>();
					m.put(rid, l);
				}
				l.add(g);
			}
			return m;
		}

	};

	@Override
	public void synchroUsers(List<SynchroUser> synchroUsers) throws SQLException {

		try {

			for (SynchroUser u : synchroUsers)
				System.out.println(u);

			List<User> oldUsers = dbc.select(User.class, "select * from sb_user where nick<>'admin' order by id asc");
			Map<Long, List<String>> oldUserGroups = dbc.select0(mapLongListStringResult, "select user_id, name from sb_user_group order by user_id, name");

			MergeHelper.merge(synchroUsers, oldUsers, new SynchroUserMerge(oldUserGroups));

		} catch (Exception e) {
			if (e instanceof SQLException)
				throw ((SQLException) e);
			throw new SQLException(e);
		}
	}

	@Override
	public String updateAvatarImg(long userId, String name, InputStream is) throws SQLException, IOException {
		String fullName = attachmentService.saveAttachment("user_avatar", userId, name, is);
		dbc.update("update sb_user set avatar_img=? where id=?", fullName, userId);
		return fullName;
	}

	@Override
	public String updateBackgroundImg(long userId, String name, InputStream is) throws SQLException, IOException {
		String fullName = attachmentService.saveAttachment("user_background", userId, name, is);
		dbc.update("update sb_user set background_img=? where id=?", fullName, userId);
		return fullName;
	}

	@Override
	public void addRoomGroup(long roomId, String group, RoomAccess access) throws SQLException {
		if (access == null)
			access = RoomAccess.read;
		dbc.update("insert into sb_room_group (room_id,name,group_access) values (?,?,?)", roomId, group, access.toString());
	}

	@Override
	public void deleteRoomGroup(long roomId, String group) throws SQLException {
		dbc.update("delete from sb_room_group where room_id=? and name=?", roomId, group);
	}

	@Override
	public void updateRoomGroupAccess(long roomId, String group, RoomAccess access) throws SQLException {
		if (access == null)
			access = RoomAccess.read;
		dbc.update("update sb_room_group set group_access=? where room_id=? and name=?", access.name(), roomId, group);
	}

	private IMergeLists<Object[], Object[]> mergeRommUser = new IMergeLists<Object[], Object[]>() {

		// newItem roomId,userId,access
		// oldItem roomId,userId,access,with_group

		@Override
		public Object getNewId(Object[] newItem) throws Exception {
			return newItem[0] + "-" + newItem[1];
		}

		@Override
		public Object getOldId(Object[] oldItem) throws Exception {
			return oldItem[0] + "-" + oldItem[1];
		}

		@Override
		public void newItem(Object[] newItem) throws Exception {
			dbc.update("insert into sb_room_user (room_id,user_id,access,with_group) values(?,?,?,?)", newItem[0], newItem[1], newItem[2], true);
		}

		@Override
		public void updateItem(Object[] newItem, Object[] oldItem) throws Exception {
			if (!Boolean.TRUE.equals(oldItem[3])) {
				// group defined
				if (!oldItem[2].equals(newItem[2])) {
					dbc.update("update sb_room_user set access=? where room_id=? and user_id=?", newItem[2], oldItem[0], oldItem[1]);
				}
			}
		}

		@Override
		public void deleteItem(Object[] oldItem) throws Exception {
			if (Boolean.TRUE.equals(oldItem[3])) {
				dbc.update("delete from sb_room_user where room_id=? and user_id=?", oldItem[0], oldItem[1]);
			}
		}

	};

	@Override
	public void synchroRoomUserGroups(long roomId, long userId) throws SQLException {
		try {
			List<Object> args1 = new ArrayList<Object>();
			StringBuilder sb1 = new StringBuilder();
			sb1.append("select rg.room_id, ug.user_id, rg.group_access from sb_room_group rg, sb_user_group ug where rg.name=ug.name ");
			if (roomId > 0) {
				sb1.append("and rg.room_id=? ");
				args1.add(roomId);
			}
			if (userId > 0) {
				sb1.append("and ug.user_id=? ");
				args1.add(userId);
			}
			List<Object[]> groupl = dbc.select(Dbc.array, sb1.toString(), args1.toArray());
			Map<String, Object[]> mapGroup = new HashMap<String, Object[]>();
			// row roomId,userId,access
			for (Object[] row : groupl) {
				String key = row[0] + "-" + row[1];
				String access = (String) row[2];
				Object[] oldrow = mapGroup.get(key);
				String old = oldrow != null ? (String) oldrow[2] : null;
				if (old == null || access.equals("manage") || (access.equals("edit") && old.equals("read")))
					mapGroup.put(key, row);
			}

			List<Object> args2 = new ArrayList<Object>();
			StringBuilder sb2 = new StringBuilder();
			sb2.append("select ru.room_id, ru.user_id, ru.access, ru.with_group from sb_room_user ru where 1=1 ");
			if (roomId > 0) {
				sb2.append("and ru.room_id=? ");
				args2.add(roomId);
			}
			if (userId > 0) {
				sb2.append("and ru.user_id=? ");
				args2.add(userId);
			}
			List<Object[]> userl = dbc.select(Dbc.array, sb2.toString(), args2.toArray());
			// old roomId,userId,access,with_group
			MergeHelper.merge(new ArrayList<Object[]>(mapGroup.values()), userl, mergeRommUser);

		} catch (Exception e) {
			if (e instanceof SQLException)
				throw ((SQLException) e);
			throw new SQLException(e);
		}
	}

	@Override
	public void addRoomUser(long roomId, long userId, RoomAccess access) throws SQLException {
		Boolean withGroup = dbc.select1(Dbc.singleBoolean, "select with_group from sb_room_user where room_id=? and user_id=?", roomId, userId);
		if (withGroup == null) {
			// add item
			dbc.update("insert into sb_room_user (room_id,user_id,with_group,access) values(?,?,?,?)", roomId, userId, false, access.name());
		} else if (withGroup.booleanValue() == true) {
			// change item to user defined
			dbc.update("update sb_room_user set with_group=false where room_id=? and user_id=?", roomId, userId);
		} else {
			// item exist, any changes
		}
	}

	@Override
	public void deleteRoomUser(long roomId, long userId) throws SQLException {
		Boolean withGroup = dbc.select1(Dbc.singleBoolean, "select with_group from sb_room_user where room_id=? and user_id=?", roomId, userId);
		if (withGroup == null) {
			// item not exist, any changes
		} else if (withGroup.booleanValue() == true) {
			// group defined, any changes
		} else {
			// user defined, delete
			dbc.update("delete from sb_room_user where room_id=? and user_id=? and with_group=false", roomId, userId);
		}
	}

	@Override
	public void updateRoomUserAccess(long roomId, long userId, RoomAccess access) throws SQLException {
		dbc.update("update sb_room_user set access=? where room_id=? and user_id=?", access.name(), roomId, userId);
	}

	private IResult<Map<Long, RoomAccess>> roomAccessResult = new IResult<Map<Long, RoomAccess>>() {

		@Override
		public Map<Long, RoomAccess> result(ResultSet rs) throws SQLException {
			Map<Long, RoomAccess> m = new HashMap<Long, RoomAccess>();
			while (rs.next()) {
				m.put(rs.getLong(1), RoomAccess.valueOf(rs.getString(2)));
			}
			return m;
		}
	};

	@Override
	public UserContext createUserContext(String email) throws SQLException {

		Object[] urow = dbc.select1(Dbc.array, "select password,active from sb_user where email=?", email);
		if (!Boolean.TRUE.equals(urow[1]) || urow[0] == null)
			throw new SQLException("email not active");

		User u = dbc.select1(User.class, "select * from sb_user where email=?", email);
		Map<Long, RoomAccess> access = dbc.select0(roomAccessResult, "select room_id,access from sb_room_user where user_id=?", u.getId());
		dbc.update("update sb_user set last_login=current_timestamp where id=?", u.getId());

		UserContext uc = new UserContext(u.getId(), u.getNick(), u.getEmail(), u.isAdmin(), access);
		uc.setPassword((String) urow[0]);
		uc.setAvatarImg(u.getAvatarImg());
		uc.setBackgroundImg(u.getBackgroundImg());
		return uc;
	}

	@Override
	public String loadGlobalVar(String key) throws SQLException {
		return dbc.select1(Dbc.singleString, "select val from sb_global_var where vkey=?", key);
	}

	@Override
	public long createUser(User user, String password, String[] groups) throws SQLException {
		user.setId(0);
		dbc.save(user);
		dbc.update("update sb_user set password=? where email=?", password, user.getEmail());
		if (groups != null) {
			for (String g : groups) {
				dbc.update("insert into sb_user_group (user_id,name) values (?, ?)", user.getId(), g);
			}
		}
		return user.getId();
	}

	@Override
	public UserInfo loadUserInfo(long userId) throws SQLException {

		UserInfo ui = dbc.select1(UserInfo.class, "select * from sb_user where id=?", userId);
		if (ui != null) {
			String sql = "select distinct r.id,r.name,r.path,r.logo_img,r.background_img,ru.access from sb_room r, sb_room_user ru where r.id=ru.room_id and ru.user_id=? order by r.name asc";
			ui.setRooms(dbc.select(MyRoom.class, sql, userId));
		}
		return ui;
	}
}
