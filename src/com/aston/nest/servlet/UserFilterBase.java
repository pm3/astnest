package com.aston.nest.servlet;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import com.aston.nest.services.IUserService;

public class UserFilterBase implements Filter {

	private IUserService userService;

	public UserFilterBase(IUserService userService) {
		this.userService = userService;
	}

	@Override
	public void doFilter(ServletRequest request0, ServletResponse response0, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) request0;
		HttpServletResponse response = (HttpServletResponse) response0;

		String path = request.getRequestURI();
		String cp = request.getContextPath();
		if (cp != null && cp.length() > 0)
			path = path.substring(cp.length());

		if (path.startsWith("/public/")) {
			chain.doFilter(request0, response0);
			return;
		}

		try {
			UserContext uc = createUserContext(request);
			request.setAttribute(UserContext.class.getName(), uc);
			chain.doFilter(request0, response0);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			response.setStatus(401);
			response.addHeader("WWW-Authenticate", "Basic realm=\"nest login\"");
			response.setContentType("text/html");
			response.setCharacterEncoding("utf-8");
			response.getWriter().write("<html><head><title>401 Authorization Required</title></head><body><h1>Authorization Required</h1></body></html>");
			info(request, System.out);
			return;
		}
	}

	protected void info(HttpServletRequest r, PrintStream out) {
		out.println("getAuthType: " + r.getAuthType());
		out.println("getPathInfo: " + r.getPathInfo());
		out.println("getPathTranslated: " + r.getPathTranslated());
		out.println("getContextPath: " + r.getContextPath());
		out.println("getQueryString: " + r.getQueryString());
		out.println("getRemoteUser: " + r.getRemoteUser());
		out.println("getUserPrincipal: " + r.getUserPrincipal());
		if (r.getUserPrincipal() != null) {
			out.println("getUserPrincipal-class: " + r.getUserPrincipal().getClass());
			out.println("getUserPrincipal-name: " + r.getUserPrincipal().getName());
		}
		out.println("getRequestedSessionId: " + r.getRequestedSessionId());
		out.println("getRequestURI: " + r.getRequestURI());
		out.println("getRequestURL: " + r.getRequestURL());
		out.println("getServletPath: " + r.getServletPath());
		out.println("isRequestedSessionIdValid: " + r.isRequestedSessionIdValid());
		out.println("isRequestedSessionIdFromCookie: " + r.isRequestedSessionIdFromCookie());
		out.println("isRequestedSessionIdFromURL: " + r.isRequestedSessionIdFromURL());
		out.println("getMethod: " + r.getMethod());
		out.println("getCharacterEncoding: " + r.getCharacterEncoding());
		out.println("getServerName: " + r.getServerName());
		out.println("getServerPort: " + r.getServerPort());
		out.println("getRemoteAddr: " + r.getRemoteAddr());
		out.println("getRemoteHost: " + r.getRemoteHost());
		out.println("isSecure: " + r.isSecure());
		out.println("getRemotePort: " + r.getRemotePort());
		out.println("getLocalAddr: " + r.getLocalAddr());
		out.println("getScheme: " + r.getScheme());
		out.println("getProtocol: " + r.getProtocol());
		out.println("getContentLength: " + r.getContentLength());
		out.println("getContentType: " + r.getContentType());
		out.println("getLocale: " + r.getLocale());
		out.println("getLocalPort: " + r.getLocalPort());
		out.println("getLocalName: " + r.getLocalName());

		String sroles = r.getParameter("roles");
		if (sroles != null) {
			for (String s : sroles.split(","))
				out.println("role[" + s + "]:" + r.isUserInRole(s));
		}

		for (java.util.Enumeration<String> e1 = r.getHeaderNames(); e1.hasMoreElements();) {
			String n = e1.nextElement();
			out.println("header[" + n + "]:" + r.getHeader(n));
		}

		for (java.util.Enumeration<String> e2 = r.getParameterNames(); e2.hasMoreElements();) {
			String n = e2.nextElement();
			out.println("param[" + n + "]:" + r.getParameter(n));
		}

		for (java.util.Enumeration<String> e3 = r.getAttributeNames(); e3.hasMoreElements();) {
			String n = e3.nextElement();
			out.println("attribute[" + n + "]:" + r.getAttribute(n));
		}

		for (java.util.Enumeration<String> e4 = r.getSession().getAttributeNames(); e4.hasMoreElements();) {
			String n = e4.nextElement();
			out.println("session[" + n + "]:" + r.getSession().getAttribute(n));
		}

		if (r.getCookies() != null)
			for (Cookie cc : r.getCookies()) {
				out.println("cookie[" + cc.getName() + "]:" + cc.getValue());
			}

	}

	protected UserContext createUserContext(HttpServletRequest request) throws Exception, SQLException {

		UserContext uc = (UserContext) request.getSession().getAttribute(UserContext.class.getName());
		String authorization = request.getHeader("Authorization");
		if (uc != null && authorization == null)
			return uc;
		if (uc != null && authorization != null && authorization.equals(request.getSession().getAttribute("_Authorization")))
			return uc;
		if (uc != null)
			request.getSession().invalidate();
		if (authorization == null || !authorization.startsWith("Basic "))
			throw new Exception("please login");

		String auth = new String(DatatypeConverter.parseBase64Binary(authorization.substring(6)));
		int pos = auth.indexOf(':');
		String email = pos > 0 ? auth.substring(0, pos) : auth;
		String password = pos > 0 ? auth.substring(pos + 1) : "";

		uc = userService.createUserContext(email);
		if (uc == null || !checkPassword(password, uc.getPassword()))
			throw new Exception("incorect login or password");
		uc.setPassword(null);
		request.getSession().setAttribute(UserContext.class.getName(), uc);
		request.getSession().setAttribute("_Authorization", authorization);

		return uc;
	}

	protected static boolean checkPassword(String raw, String fromDb) {
		if (raw == null || fromDb == null)
			return false;
		int pos = fromDb.indexOf(':');
		if (pos > 0) {
			String salt = fromDb.substring(0, pos);
			return fromDb.equals(AdminServlet.encodePassword(raw, salt));
		}
		return fromDb.equals(AdminServlet.md5(raw));
	}

	public static void main(String[] args) {
		String s1 = "r0s6k3";
		System.out.println(AdminServlet.md5(s1));
		String s2 = "1782jlb:a7093c5a65c18f471a7b2ef3b887affa";
		System.out.println(checkPassword(s1, s2));
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
	}

	@Override
	public void destroy() {
	}
}
