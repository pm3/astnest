package com.aston.nest.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.aston.utils.StreamHelper;

public class FileStoreService {

	File rootFolder;

	public void setRootFolder(File rootFolder) {
		this.rootFolder = rootFolder;
	}

	public long saveContent(String group, long id, byte[] content) throws IOException {
		FileOutputStream fos = null;
		long length = -1;
		try {
			File f = file(group, id);
			File parent = f.getParentFile();
			if (parent.exists() == false)
				parent.mkdir();
			fos = new FileOutputStream(f);
			fos.write(content);
			fos.close();
			length = f.length();
		} catch (IOException e) {
			if (fos != null)
				try {
					fos.close();
				} catch (Exception e2) {
				}
		}
		return length;
	}

	public long saveContent(String group, long id, InputStream is) throws IOException {
		FileOutputStream fos = null;
		long length = -1;
		try {
			File f = file(group, id);
			File parent = f.getParentFile();
			if (parent.exists() == false)
				parent.mkdirs();
			fos = new FileOutputStream(f);
			StreamHelper.copy(is, fos, 4096);
			fos.close();
			length = f.length();
		} catch (IOException e) {
			e.printStackTrace();
			if (fos != null)
				try {
					fos.close();
				} catch (Exception e2) {
				}
		}
		return length;
	}

	public byte[] loadContent(String group, long id) throws IOException {
		File f = file(group, id);
		if (!f.exists())
			throw new IOException("invalid file store id");
		return StreamHelper.file2bytea(f);
	}

	public void loadContent(String group, long id, OutputStream os) throws IOException {
		File f = file(group, id);
		if (!f.exists())
			throw new IOException("invalid file store id");
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			StreamHelper.copy(fis, os, 4096);
			fis.close();
		} catch (Exception e) {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception ee) {
				}
			}
		}
	}

	public File file(String group, long id) {
		long mod = id % 1000;
		String fn = group + File.separator + (id - mod) + File.separator + id;
		return new File(rootFolder, fn);
	}

	public static String imageContentType(InputStream is) throws IOException {
		try {
			ImageInputStream iis = ImageIO.createImageInputStream(is);
			Iterator<ImageReader> it = ImageIO.getImageReaders(iis);
			return "image/" + it.next().getFormatName().toLowerCase();
		} catch (Exception e) {
			throw new IOException("content is not image");
		}
	}

}
