package com.aston.nest.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

public interface IAttachmentService {

	public String saveAttachment(String group, long id, String name, InputStream is) throws SQLException, IOException;

	public File getAttachmentFile(String group, long id);

	public void loadContent(String group, long id, OutputStream os) throws IOException;

}
