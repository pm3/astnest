package com.aston.nest.servlet;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aston.utils.ServletHelper;
import com.aston.utils.servlet.HttpStateException;
import com.aston.utils.servlet.provider.Provider;
import com.aston.utils.servlet.provider.RequestProvider;

public class UserContentFilter implements Filter {

	protected File rootDir;
	protected File adminDir;
	protected Provider<UserContext> userContext = new RequestProvider<UserContext>(UserContext.class);
	protected ServletContext servletContext = null;

	public UserContentFilter(File rootDir) throws IOException {
		this.rootDir = rootDir;

		this.adminDir = new File(rootDir, "admin");
		if (!adminDir.exists())
			adminDir.mkdir();
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.servletContext = filterConfig.getServletContext();
	}

	@Override
	public void doFilter(ServletRequest request0, ServletResponse response0, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) request0;
		HttpServletResponse response = (HttpServletResponse) response0;

		String nick = userContext.get().getNick();
		String path = request.getRequestURI();
		String cp = request.getContextPath();
		if (cp != null && cp.length() > 0)
			path = path.substring(cp.length());
		path = URLDecoder.decode(path, "utf-8");
		path = ServletHelper.normalizeUri(path);

		try {
			if (path.startsWith("/att/") || path.startsWith("/user/") || path.startsWith("/manage/") || path.startsWith("/admin/")) {

				chain.doFilter(request0, response0);

			} else if (path.startsWith("/my/")) {

				path = path.substring(3);
				File userRoot = new File(rootDir, nick);
				File f = new File(userRoot, path);
				file(request, response, f, path);

			} else if (path.startsWith("/~")) {

				path = path.substring(2);
				File f = new File(rootDir, path);
				file(request, response, f, path);

			} else if (path.equals("/api.jsp")) {

				chain.doFilter(request0, response0);

			} else {

				if (path.equals("/"))
					path = "/index.html";
				File f = new File(adminDir, path);
				file(request, response, f, path);

			}
		} catch (HttpStateException ee) {
			ee.defineStatus(request, response);
		}
	}

	protected void file(HttpServletRequest request, HttpServletResponse response, File f, String path) throws HttpStateException {
		if (!f.exists() || !f.isFile() || f.getName().contains("/."))
			throw new HttpStateException(404, "404 " + path);
		ServletHelper.sendFile(request, response, servletContext, f);
	}

	@Override
	public void destroy() {

	}

}
