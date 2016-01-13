/**
 * @(@)ConnectionServlet.java
 * 
 * @author xiaobin
 * @version 1.0
 * @date 2009.03.15
 * 
 */

package com.tdtc.web;

import java.sql.SQLException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.tdtc.jdbc.ConnectionPool;
import com.tdtc.xbf.ReadConfig;
import com.tdtc.xbf.ReadXbf;
import com.tdtc.xbf.ConfigServlet;
import org.apache.log4j.*;

/**
 * <p>This is a simple servlet that holds a global connection
 * pool. The Servlet context is used to store a named attribute
 * (this servlet) so that other servlets have access to the
 * connection pool
 */
 public class ConnectionServlet 
   extends javax.servlet.http.HttpServlet {
   
   static final long serialVersionUID = 1L;
   
   private final Logger logger = Logger.getLogger(getClass());
   // Context attribute key
   public static String KEY = "com.cartionsoft.web.ConnectionServlet";
   private String strXbfContent;
   private String strLibPath;
   private String strXbfPath;
   private String strXmlPath;
   private ReadConfig readConfig;
   private String strUserName;
   private String strPassWord;
   
   // Our Connection Pool
   com.cartionsoft.jdbc.ConnectionPool m_ConnectionPool;
   
    /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public ConnectionServlet() {
		super();
	} 
	
	
	
	/* (non-Javadoc)
	 * @see javax.servlet.Servlet#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
		// Remove the attribute from the context
		getServletContext().removeAttribute(KEY);
		
		// Tear down our connection pool if it was created
		if (m_ConnectionPool != null) {
			m_ConnectionPool.destroy();
		}
		
		super.destroy();
	}   	 	  	  	  
	
	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init()
	 */
	public void init(ServletConfig cfg) throws ServletException {
		// TODO Auto-generated method stub
		super.init(cfg);
		
		//strXmlPath =  cfg.getInitParameter("xmlPath");
		ConfigServlet cs = new ConfigServlet();
		strXmlPath = cs.getConfigContext(cfg, "xmlPath");
		
		readConfig = new ReadConfig(strXmlPath);
		
		try {
			strLibPath = readConfig.getElement(1, "lib-file1");
			strXbfPath = readConfig.getElement(2, "xbf-file1");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		logger.info(strLibPath);
		ReadXbf readX = new ReadXbf(strLibPath);
		strXbfContent = readX.readRecordMsSql(-1, strXbfPath);
		
		//
		strUserName = getDBUserMsSql(strXbfContent);
		strPassWord = getPWDMsSql(strXbfContent);
		
		// Create our connection pool
	    m_ConnectionPool = new ConnectionPool(strUserName, strPassWord);
		
		// Initialize the connection pool. This will start all
		// of the connections as specified in the connection
		// pool configuration file
		try {
			m_ConnectionPool.initialize();
		} catch (Exception ex) {
			// TODO: handle exception
			ex.printStackTrace();
			throw new ServletException("Unable to initialize connection pool");
		}
		
		// Add this servlet to the context so that other servlets
		// can find us
		getServletContext().setAttribute(KEY, this);
		
	}
	
	/**
	 * <p>Get a JDBC connection from the pool
	 * 
	 * @return JDBC connection
	 * @throws SQLException
	 */
	public java.sql.Connection getConnection() throws SQLException {
		java.sql.Connection conn = null;
		if (m_ConnectionPool != null) {
			conn = m_ConnectionPool.getConnection();
		}
		
		return conn;
	}
	
	/**
	 * <p>Closes the given JDBC connection
	 * 
	 * @param connection JDBC connection
	 */
	public void close(java.sql.Connection connection) {
		if (m_ConnectionPool != null) {
			m_ConnectionPool.close(connection);
		}
	}
	
	/**
	 * <p> read private strXbfContent value
	 * 
	 * @param XbfContent xbf file's content
	 * @return xbf file's DB user
	 */
	
	public String getDBUserMsSql(String xbfContent) {
		int iBeginIndex = xbfContent.indexOf("ID=");
		int iEndIndex = xbfContent.indexOf(";I");
		String strUser = xbfContent.substring(iBeginIndex + 3, iEndIndex);
		return strUser;
	}

	/**
	 * <p> read private strXbfContent value
	 * 
	 * @param XbfContent xbf file's content
	 * @return xbf file's password
	 */
	public String getPWDMsSql(String xbfContent) {
		int iBeginIndex = xbfContent.indexOf("d=");
		int iEndIndex = xbfContent.indexOf(";Per");
		String strPwd = xbfContent.substring(iBeginIndex + 2, iEndIndex);
		return strPwd;
	}
}