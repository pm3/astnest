package com.aston.nest.services;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import com.aston.nest.domain.Room;
import com.aston.nest.domain.RoomUser;
import com.aston.nest.domain.SynchroRoom;

public interface IRoomService {

	public Room loadRoom(long roomId) throws SQLException;

	public List<Room> selectAllRooms() throws SQLException;

	public void synchroSystemRooms(List<SynchroRoom> synchroRooms) throws SQLException, IOException;

	public long createRoom(Room room) throws SQLException;

	public void updateRoom(long id, String name, String path, String description) throws SQLException;

	public void updateRoomState(long id, boolean active) throws SQLException;

	public String updateLogoImg(long roomId, String name, InputStream is) throws SQLException, IOException;

	public String updateBackgroundImg(long roomId, String name, InputStream is) throws SQLException, IOException;

	public List<RoomUser> selectRoomUsers(long roomId) throws SQLException;

	public List<RoomUser> selectAllUsersByRoom(long roomId) throws SQLException;
}
