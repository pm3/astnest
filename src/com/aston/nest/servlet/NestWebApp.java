package com.aston.nest.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Properties;

import javax.mail.Session;
import javax.naming.InitialContext;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.sql.DataSource;

import com.aston.nest.services.AttachmentService;
import com.aston.nest.services.FileStoreService;
import com.aston.nest.services.IAttachmentService;
import com.aston.nest.services.IMessageService;
import com.aston.nest.services.IRoomService;
import com.aston.nest.services.IUserService;
import com.aston.nest.services.MessageService;
import com.aston.nest.services.RoomService;
import com.aston.nest.services.UserService;
import com.aston.utils.mail.MailFactory;
import com.aston.utils.servlet.AConfig;
import com.aston.utils.servlet.PathStore;
import com.aston.utils.servlet.japi.JApiMethodExecFactory;
import com.aston.utils.servlet.jsp.JspFactory;
import com.aston.utils.servlet.path.PathMethodExecFactory;
import com.aston.utils.sql.ThreadTrDbc;

public class NestWebApp implements ServletContextListener, HttpSessionListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			initResources(sce.getServletContext());
			initServices(sce.getServletContext());
			initWebConf(sce.getServletContext());
			initModules(sce.getServletContext());
		} catch (Exception e) {
			throw new IllegalStateException("init " + this.getClass().getName(), e);
		}
	}

	protected Properties appPrperties = null;
	protected DataSource dataSource = null;
	protected ThreadTrDbc dbc = null;
	protected MailFactory mailFactory = null;
	protected File rootDir = null;

	protected void initResources(ServletContext servletContext) throws Exception {

		this.appPrperties = new Properties();
		FileInputStream fis = null;
		try {
			String path = servletContext.getRealPath("/WEB-INF/application.properties");
			fis = new FileInputStream(path);
			this.appPrperties.load(fis);
		} finally {
			try {
				fis.close();
			} catch (Exception ee) {
			}
		}

		this.dataSource = (DataSource) loadJndi("jdbc/astnest");
		this.dbc = new ThreadTrDbc(dataSource);

		MailFactory mf = new MailFactory();
		mf.setMailFrom("nest@aston.sk");
		mf.setSession((Session) loadJndi("mail/all"));
		mf.setBaseTemplatePath(servletContext.getRealPath("/WEB-INF/mail"));
		this.mailFactory = mf;

		String thome = System.getProperty("catalina.home");
		if (thome == null)
			thome = "/aston/";

		this.rootDir = new File(thome, "data/astnest");
		if (!rootDir.exists() || !rootDir.isDirectory())
			throw new IOException("not exist root directory - " + rootDir.getAbsolutePath());
	}

	IUserService userService = null;
	IMessageService messageService = null;
	IRoomService roomService = null;
	IAttachmentService attachmentService = null;

	protected void initServices(ServletContext servletContext) throws IOException {

		FileStoreService fss = new FileStoreService();
		fss.setRootFolder(rootDir);

		this.attachmentService = new AttachmentService(fss);
		this.userService = new UserService(dbc, attachmentService);
		this.messageService = new MessageService(dbc, attachmentService);
		this.roomService = new RoomService(dbc, attachmentService);
	}

	protected AConfig aconfig = null;
	protected PathStore pathStore = null;

	protected void initWebConf(ServletContext servletContext) throws Exception {
		// aconfig
		this.aconfig = new AConfig(servletContext);
		aconfig.addExecFactory(new JApiMethodExecFactory());
		aconfig.addExecFactory(new PathMethodExecFactory());
		aconfig.addPathFactory(new JspFactory("/WEB-INF/jsp/", ".jsp"));

		// modules
		this.pathStore = new PathStore();
		servletContext.setAttribute(PathStore.class.getName(), pathStore);

		servletContext.addFilter("UserFilterOauth", new UserFilterOauth(userService)).addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
		servletContext.addFilter("UserContentFilter", new UserContentFilter(rootDir)).addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");

		servletContext.addServlet("UserServlet", new UserServlet(userService, roomService, messageService)).addMapping("/user/*");
		servletContext.addServlet("RoomManagerServlet", new RoomManagerServlet(userService, roomService)).addMapping("/manage/*");
		servletContext.addServlet("AdminServlet", new AdminServlet(userService, roomService, mailFactory)).addMapping("/admin/*");
		servletContext.addServlet("AttachmentServlet", new AttachmentServlet(attachmentService)).addMapping("/att/*");
	}

	protected void initModules(ServletContext servletContext) {

	}

	public static Object loadJndi(String jndi) throws Exception {
		InitialContext cxt = new InitialContext();
		Object o = cxt.lookup("java:/comp/env/" + jndi);
		if (o == null)
			throw new Exception("Data source not found: " + jndi);
		return o;
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}

	@Override
	public void sessionCreated(HttpSessionEvent se) {
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
	}

}
