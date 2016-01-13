/**
 * @(#)ConfigServlet.java
 * 
 * @author xiaobin
 * @version 1.0
 * @date 2009.03.15
 * 
 */

package com.tdtc.xbf;

import javax.servlet.*;

/**
 * <p>Read Servlet Config is value
 *
 */

public class ConfigServlet {
	
	/**
	 * <p>This is method read "context-param" value
	 * 
	 * @param cfg The Servlet is ServletConfig
	 * @param contextName "param-name"
	 * @return "param-value"
	 */
	public String getConfigContext(ServletConfig cfg, String contextName) {
		ServletContext context = cfg.getServletContext();
		String result = context.getInitParameter(contextName);
		return result;
	}
}
