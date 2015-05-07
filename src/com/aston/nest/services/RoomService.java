package com.aston.nest.services;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aston.nest.domain.Room;
import com.aston.nest.domain.RoomAccess;
import com.aston.nest.domain.RoomUser;
import com.aston.nest.domain.SynchroRoom;
import com.aston.nest.domain.SynchroRoom.SynchroRoomGroup;
import com.aston.utils.MergeHelper;
import com.aston.utils.MergeHelper.IMergeLists;
import com.aston.utils.sql.Dbc;

public class RoomService implements IRoomService {

	private Dbc dbc;
	private IAttachmentService attachmentService;

	public RoomService(Dbc dbc, IAttachmentService attachmentService) {
		this.dbc = dbc;
		this.attachmentService = attachmentService;
	}

	@Override
	public Room loadRoom(long roomId) throws SQLException {
		return dbc.load(Room.class, roomId);
	}

	@Override
	public List<Room> selectAllRooms() throws SQLException {
		return dbc.select(Room.class, "select * from sb_room order by id asc");
	}

	class SynchroRoomMerge implements IMergeLists<SynchroRoom, Room> {

		private Map<Long, List<Object[]>> mroomGroups;

		SynchroRoomMerge(Map<Long, List<Object[]>> mroomGroups) {
			this.mroomGroups = mroomGroups;
		}

		@Override
		public Object getNewId(SynchroRoom newItem) throws Exception {
			return newItem.getPath();
		}

		@Override
		public Object getOldId(Room oldItem) throws Exception {
			return oldItem.getPath();
		}

		@Override
		public void newItem(SynchroRoom newItem) throws Exception {
			Room r = new Room();
			r.setName(newItem.getName());
			r.setPath(newItem.getPath());
			r.setDescription(newItem.getDescription());
			r.setAutoSynchronized(true);
			r.setActive(true);
			r.setModified(new Date());
			dbc.save(r);
			MergeHelper.merge(newItem.getGroups(), new ArrayList<Object[]>(0), new SynchroRoomGroupMerge(r.getId()));
		}

		@Override
		public void updateItem(SynchroRoom newItem, Room oldItem) throws Exception {
			oldItem.setName(newItem.getName());
			oldItem.setDescription(newItem.getDescription());
			oldItem.setModified(new Date());
			dbc.save(oldItem);
			List<Object[]> l = mroomGroups.get(oldItem.getId());
			MergeHelper.merge(newItem.getGroups(), l != null ? l : new ArrayList<Object[]>(0), new SynchroRoomGroupMerge(oldItem.getId()));
		}

		@Override
		public void deleteItem(Room oldItem) throws Exception {
			dbc.update("update sb_room set active=false where id=?", oldItem.getId());
		}

	}

	class SynchroRoomGroupMerge implements IMergeLists<SynchroRoomGroup, Object[]> {

		long roomId;

		public SynchroRoomGroupMerge(long roomId) {
			this.roomId = roomId;
		}

		@Override
		public Object getNewId(SynchroRoomGroup newItem) throws Exception {
			return newItem.getName();
		}

		@Override
		public Object getOldId(Object[] oldItem) throws Exception {
			return oldItem[1];
		}

		@Override
		public void newItem(SynchroRoomGroup newItem) throws Exception {
			if (newItem.getName() == null)
				return;
			RoomAccess a = newItem.getAccess() != null ? newItem.getAccess() : RoomAccess.edit;
			dbc.update("insert into sb_room_group (room_id,name,group_access) values (?,?,?)", roomId, newItem.getName(), a.name());
		}

		@Override
		public void updateItem(SynchroRoomGroup newItem, Object[] oldItem) throws Exception {
			RoomAccess a = newItem.getAccess() != null ? newItem.getAccess() : RoomAccess.edit;
			if (!a.name().equals(oldItem[2]))
				dbc.update("update sb_room_group set access=? where room_id=? and name=?", a.name(), roomId, oldItem[1]);
		}

		@Override
		public void deleteItem(Object[] oldItem) throws Exception {
			dbc.update("delete from sb_room_group where room_id=? and name=?", roomId, oldItem[1]);
		}
	}

	@Override
	public void synchroSystemRooms(List<SynchroRoom> synchroRooms) throws SQLException {

		try {
			List<Room> oldRooms = dbc.select(Room.class, "select * from sb_room where auto_synchronized=true order by id asc");
			List<Object[]> l = dbc.select(Dbc.array, "select room_id, name, group_access from sb_room_group");
			Map<Long, List<Object[]>> mroomGroups = new HashMap<Long, List<Object[]>>();
			for (Object[] row : l) {
				List<Object[]> ll = mroomGroups.get((Long) row[0]);
				if (ll == null) {
					ll = new ArrayList<Object[]>();
					mroomGroups.put((Long) row[0], ll);
				}
				ll.add(row);
			}

			MergeHelper.merge(synchroRooms, oldRooms, new SynchroRoomMerge(mroomGroups));

		} catch (Exception e) {
			if (e instanceof SQLException)
				throw ((SQLException) e);
			throw new SQLException(e);
		}

	}

	@Override
	public long createRoom(Room room) throws SQLException {
		room.setModified(new Date());
		dbc.save(room);
		return room.getId();
	}

	@Override
	public void updateRoom(long id, String name, String path, String description) throws SQLException {
		Room r = dbc.load(Room.class, id);
		if (r == null)
			throw new SQLException("undefined roomId " + id);
		if (name != null)
			r.setName(name);
		if (path != null)
			r.setPath(path);
		if (description != null)
			r.setDescription(description);
		dbc.save(r);
	}

	@Override
	public void updateRoomState(long id, boolean active) throws SQLException {
		dbc.update("update sb_room set active=? where id=?", active, id);
	}

	@Override
	public String updateLogoImg(long roomId, String name, InputStream is) throws SQLException, IOException {
		String fullName = attachmentService.saveAttachment("room_logo", roomId, name, is);
		dbc.update("update sb_room set logo_img=? where id=?", fullName, roomId);
		return fullName;
	}

	@Override
	public String updateBackgroundImg(long roomId, String name, InputStream is) throws SQLException, IOException {
		String fullName = attachmentService.saveAttachment("room_background", roomId, name, is);
		dbc.update("update sb_room set background_img=? where id=?", fullName, roomId);
		return fullName;
	}

	@Override
	public List<RoomUser> selectRoomUsers(long roomId) throws SQLException {
		String sql = "select distinct u.id, u.nick, u.avatar_img, ru.access, ru.with_group from sb_user u, sb_room_user ru where u.id=ru.user_id and ru.room_id=? order by u.nick asc";
		return dbc.select(RoomUser.class, sql, roomId);
	}

	@Override
	public List<RoomUser> selectAllUsersByRoom(long roomId) throws SQLException {
		String sql = "select distinct u.id, u.nick, u.avatar_img, ru.access, ru.with_group from sb_user u left join sb_room_user ru on u.id=ru.user_id and ru.room_id=? order by u.nick asc";
		return dbc.select(RoomUser.class, sql, roomId);
	}
}
