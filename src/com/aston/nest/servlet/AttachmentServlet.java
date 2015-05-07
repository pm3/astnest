package com.aston.nest.servlet;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aston.nest.services.IAttachmentService;

public class AttachmentServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private IAttachmentService attachmentService;
	private final String[] allow;
	private final String[] allowPath;

	public AttachmentServlet(IAttachmentService attachmentService) {
		this.attachmentService = attachmentService;
		this.allow = new String[] { "msg", "room_logo", "room_background", "user_avatar", "user_background", "comment" };
		this.allowPath = new String[this.allow.length];
		for (int i = 0; i < allow.length; i++)
			allowPath[i] = "/att/" + allow[i] + "/";
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = req.getRequestURI();
		String cp = req.getContextPath();
		if (cp != null && cp.length() > 0)
			path = path.substring(cp.length());
		try {

			String group = null;
			for (int i = 0; i < allowPath.length; i++) {
				if (path.startsWith(allowPath[i])) {
					path = path.substring(allowPath[i].length());
					group = allow[i];
					break;
				}
			}
			if (group == null)
				throw new Exception("undefined group " + path);

			String sid = path;
			String name = null;
			int pos = path.indexOf('/');
			if (pos > 0) {
				sid = path.substring(0, pos);
				name = path.substring(pos + 1);
			}

			long id = Long.parseLong(sid);
			File f = attachmentService.getAttachmentFile(group, id);
			String ct = null;
			if (name != null)
				ct = getServletContext().getMimeType(name);
			if (ct == null)
				ct = "application/octet-stream";

			resp.setContentType(ct);

			long len = f.length();
			String etag = f.lastModified() + "-" + len;
			String h1 = req.getHeader("If-None-Match");
			if (etag.equals(h1)) {
				resp.setStatus(304);
				return;
			}

			String webdate = webGMTdate(new Date(f.lastModified()));
			String h2 = req.getHeader("If-Modified-Since");
			if (webdate.equalsIgnoreCase(h2)) {
				resp.setStatus(304);
				return;
			}

			resp.setHeader("ETag", etag);
			resp.setHeader("Last-Modified", webdate);
			resp.setContentLength((int) len);

			attachmentService.loadContent(group, id, resp.getOutputStream());
		} catch (Exception e) {
			resp.setStatus(404);
			System.err.println(e.getMessage());
		}
	}

	private static final ThreadLocal<SimpleDateFormat> threadLocalDateFormat = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			return sdf;
		}
	};

	public static String webGMTdate(Date d) {
		if (d == null)
			return null;
		return threadLocalDateFormat.get().format(d);
	}
}
