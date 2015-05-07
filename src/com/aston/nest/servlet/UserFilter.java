package com.aston.nest.servlet;

import java.io.IOException;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import com.aston.nest.services.IUserService;

public class UserFilter implements Filter {

	private IUserService userService;

	public UserFilter(IUserService userService) {
		this.userService = userService;
	}

	@Override
	public void doFilter(ServletRequest request0, ServletResponse response0, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) request0;
		HttpServletResponse response = (HttpServletResponse) response0;

		String path = request.getRequestURI();
		String cp = request.getContextPath();
		if (cp != null)
			path = path.substring(cp.length());

		UserContext uc = (UserContext) request.getSession().getAttribute(UserContext.class.getName());
		if (uc == null) {
			try {
				uc = createUserContext(request);
				request.getSession().setAttribute(UserContext.class.getName(), uc);
			} catch (Exception e) {
				e.printStackTrace();
				response.sendRedirect(request.getContextPath() + "/login.html");
				return;
			}
		}
		request.setAttribute(UserContext.class.getName(), uc);
		chain.doFilter(request0, response0);
	}

	protected UserContext createUserContext(HttpServletRequest request) throws Exception, SQLException {
		UserContext uc;
		String email = request.getParameter("email");
		if (email == null)
			throw new Exception("undefined email");
		String token = request.getParameter("token");
		if (token == null)
			throw new Exception("undefined token");
		if (!token.equals("test") && !checkToken(email, token))
			throw new Exception("token broken");
		uc = userService.createUserContext(email);
		if (uc == null)
			throw new NullPointerException("null user context " + email);
		return uc;
	}

	protected boolean checkToken(String email, String token) throws Exception {
		long now = System.currentTimeMillis();
		now = now - (now % 60000);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm");
		for (int i = -2; i < 2; i++) {

			String key = "aston" + email + "itm" + sdf.format(new Date(now + (i * 60000))) + "bratislava" + secreetKey + "tomasikova";
			String md5 = md5(key);
			if (token.equals(md5))
				return true;
		}
		return false;
	}

	protected static String md5(String s) throws Exception {
		String res = null;
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			byte[] data = m.digest(s.getBytes("utf-8"));
			res = DatatypeConverter.printBase64Binary(data);
		} catch (Exception e) {
			throw new Exception("create md5 " + e.getMessage(), e);
		}
		return res;
	}

	private String secreetKey = null;

	@Override
	public void init(FilterConfig config) throws ServletException {
		try {
			this.secreetKey = userService.loadGlobalVar("secreet-key");
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}

	@Override
	public void destroy() {
	}

	public static String token(String email, String secreetKey) throws Exception {
		long now = System.currentTimeMillis();
		now = now - (now % 60000);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm");
		String key = "aston" + email + "itm" + sdf.format(new Date(now)) + "bratislava" + secreetKey + "tomasikova";
		String md5 = md5(key);
		return md5;
	}

	public static void main(String[] args) {
		try {
			String email = "admin";
			String secreetKey = null;
			String md5 = token(email, secreetKey);
			String p = "email=" + email + "&token=" + md5;
			System.out.println("localhost:8080/astnest/dashboard?" + p);
			System.out.println("localhost:8080/astnest/create-room?");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
