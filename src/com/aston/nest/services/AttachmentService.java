package com.aston.nest.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

import com.aston.utils.ServletHelper;

public class AttachmentService implements IAttachmentService {

	private FileStoreService storeService;

	public AttachmentService(FileStoreService storeService) {
		this.storeService = storeService;
	}

	public String saveAttachment(String group, long id, String name, InputStream is) throws SQLException, IOException {
		storeService.saveContent(group, id, is);
		if (name != null && name.startsWith("/"))
			name = name.substring(1);
		return "/att/" + group + "/" + id + "/" + ServletHelper.normalizeUri(name);
	}

	@Override
	public File getAttachmentFile(String group, long id) {
		return storeService.file(group, id);
	}

	@Override
	public void loadContent(String group, long id, OutputStream os) throws IOException {
		storeService.loadContent(group, id, os);
	}

}
