package com.aston.nest.services;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import com.aston.nest.domain.RoomAccess;
import com.aston.nest.domain.SynchroUser;
import com.aston.nest.domain.User;
import com.aston.nest.domain.UserInfo;
import com.aston.nest.servlet.UserContext;

public interface IUserService {

	public List<User> selectAllUsers() throws SQLException;

	public void synchroUsers(List<SynchroUser> synchroUsers) throws SQLException;

	public String updateAvatarImg(long userId, String name, InputStream is) throws SQLException, IOException;

	public String updateBackgroundImg(long userId, String name, InputStream is) throws SQLException, IOException;

	public void addRoomGroup(long roomId, String group, RoomAccess access) throws SQLException;

	public void deleteRoomGroup(long roomId, String group) throws SQLException;

	public void updateRoomGroupAccess(long roomId, String group, RoomAccess access) throws SQLException;

	public void synchroRoomUserGroups(long roomId, long userId) throws SQLException;

	public void addRoomUser(long roomId, long userId, RoomAccess access) throws SQLException;

	public void deleteRoomUser(long roomId, long userId) throws SQLException;

	public void updateRoomUserAccess(long roomId, long userId, RoomAccess access) throws SQLException;

	public UserInfo loadUserInfo(long userId) throws SQLException;

	public UserContext createUserContext(String email) throws SQLException;

	public String loadGlobalVar(String key) throws SQLException;

	public long createUser(User user, String password, String[] groups) throws SQLException;
}
