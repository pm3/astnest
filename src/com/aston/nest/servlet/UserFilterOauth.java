package com.aston.nest.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aston.nest.services.IUserService;
import com.aston.utils.servlet.HttpStateException;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;

public class UserFilterOauth implements Filter {

	private IUserService userService;
	private GoogleAuthorizationCodeFlow flow;
	private String redirectUri;

	public UserFilterOauth(IUserService userService) throws Exception {
		this.userService = userService;

		String clientId = "387055348893-cjgcrjh71k4v3ur1sigj5bnrp236f0b7.apps.googleusercontent.com";
		String clientSecret = "8fvKcCfFA6L6mwd1G_K3f26p";
		this.redirectUri = "https://www.aston.sk/nest/oauth2callback";

		List<String> scopes = Arrays.asList("https://www.googleapis.com/auth/userinfo.profile", "https://www.googleapis.com/auth/userinfo.email");
		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		this.flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientId, clientSecret, scopes).build();

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
		} catch (HttpStateException e) {
			e.defineStatus(request, response);
			return;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			return;
		}
	}

	protected UserContext createUserContext(HttpServletRequest request) throws Exception, SQLException {

		UserContext uc = (UserContext) request.getSession().getAttribute(UserContext.class.getName());
		if (uc != null)
			return uc;

		String code = request.getParameter("code");
		if (code != null) {
			TokenResponse res = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
			GoogleCredential cr = new GoogleCredential().setFromTokenResponse(res);
			Oauth2 o2 = new Oauth2(flow.getTransport(), flow.getJsonFactory(), cr);
			Userinfoplus ui = o2.userinfo().get().execute();
			String email = ui.getEmail();

			try {
				uc = userService.createUserContext(email);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (uc == null)
				throw new Exception("incorect login");
			uc.setPassword(null);
			request.getSession().setAttribute(UserContext.class.getName(), uc);
			throw new HttpStateException(302, "https://" + request.getServerName() + request.getContextPath() + "/index.html");
		}

		String url = flow.newAuthorizationUrl().setRedirectUri(redirectUri).build();
		throw new HttpStateException(302, url);
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
	}

	@Override
	public void destroy() {
	}
}
