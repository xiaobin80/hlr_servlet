package com.cartionsoft.web;
/**
 * @(#)PastTimeList.java
 * 
 * @author xiaobin
 * @version 1.1
 * @date 2007.12.23
 * 
 * @version 1.2
 * @date 2009.03.06
 * 
 * @version 1.3.2
 * @date 2009.03.15
 */

import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.*;

import com.tdtc.xbf.ConfigServlet;
import com.tdtc.xbf.ReadConfig;


/**
 * <p>This is class search car number data.
 * data source is MS SQL 2000. use JNI interface class.  
 * 
 */

 public class PastTimeList extends javax.servlet.http.HttpServlet {
   static final long serialVersionUID = 1L;
   
   private final Logger logger = Logger.getLogger(this.getClass());
   
   private String strURI;
   private String strSN;
   private String strXMLPath;
   private String strPastTime;
   private String strQueryPastTime, strQueryDataTable;
   private String[] strColNameAry;
   private int iRowCount;
   private StringBuilder strBuilder;
   private StringBuffer strBuffer;

   // Our connection pool. Note that instance variables are
   // actually global to all clients since there is only
   // one instance of the servlet that has multiple threads
   // of execution
   //com.tdtc.jdbc.ConnectionPool m_connectionPool;
   
    /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public PastTimeList() {
		super();
		strColNameAry = 
			new String[] {"trainNo", "carN", "carM", "carW1", "carSW", "pTime"};
	}
	
	/**
	 * <p>Initialize the servlet. This is called once when the servlet
	 * is loaded. It is guaranteed to complete before any 
	 * requests are made to the servlet
	 * 
	 */
	@Override
	public void init(ServletConfig cfg) throws ServletException {
		// TODO Auto-generated method stub
		super.init(cfg);
		
		//strXMLPath = cfg.getInitParameter("xmlPath");
		ConfigServlet cs = new ConfigServlet();
		strXMLPath = cs.getConfigContext(cfg, "xmlPath");
	}
	
	/**
	 * <p>Destroy the servlet. This is called once when the servlet 
	 * is unloaded.
	 */
	@Override
	public void destroy() {
		super.destroy();
	}
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
	  throws ServletException, IOException {
		// TODO Auto-generated method stub
		this.doGet(request, response);
	}
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
	  throws ServletException, IOException {
		// TODO Auto-generated method stub
		// set html header
		response.setContentType("text/html; charset=GBK");
		// define PrintWriter
		PrintWriter outPrintWriter = new PrintWriter(response.getOutputStream());
		// output html code
		StringBuilder strBuilderTemp = new StringBuilder();
		
		strBuilderTemp.append("<html>\n");
		strBuilderTemp.append("<head><title>past car</title></head>\n");
		strBuilderTemp.append("<h2><center>\n");
		strBuilderTemp.append("car number list" + "\n");
		strBuilderTemp.append("</center></h2>" + "\n");
		strBuilderTemp.append("<br>" + "\n");
		
		try {
			ReadConfig rc = new ReadConfig(strXMLPath);
			strURI = rc.getElement(0, "url1");
		} catch (Exception e) {
			// TODO: handle exception
			logger.warn("read xml error");
		}
		
		strBuilderTemp.append("<form method=POST action=\"" + 
				strURI + 
				"\">select past car time" + "\n");
		
		strBuilderTemp.append("<select name=pastTime size=1>" + "\n");
		
		// 
		outPrintWriter.print(strBuilderTemp.toString());
		//
		strSN = request.getParameter("lastSN");
		if(strSN == null) {
			strSN = "0";
		}
		
		String strTempPastTime = 
			request.getParameter("lastPastTime");
		
		if(strTempPastTime != null) {
			strPastTime = strTempPastTime;
		} else {
			strPastTime = 
				request.getParameter("pastTime");
		}
		
		strQueryPastTime = "select DISTINCT past_time from trainOrder order by past_time";
		
		strQueryDataTable = 
			"select seriary_number as trainNo, " +
			"car_number as carN, car_marque as carM, " +
			"carry_weight1 as carW1, self_weight1 as carSW, " +
			"past_time as pTime from trainOrder " +
			"where past_time = '" + 
			strPastTime + 
			"' and seriary_number > " + 
			strSN + 
			" order by seriary_number";
		
		//
		outHtmlData(
				strQueryPastTime,
				strQueryDataTable, 
				outPrintWriter);
		
		// Wrap up
		outPrintWriter.println("</body></html>");
		
		// process printWriter
		outPrintWriter.flush();
		outPrintWriter.close();
	}  	
	
	/**
	 * <p>output dropList and table data
	 * 
	 * @param queryDropList SQL statement(drop list)
	 * @param queryCarData  SQL statement(table)
	 * @param out servlet's PrintWriter
	 * @return execute success
	 */
	private boolean outHtmlData(
			String queryDropList,
			String queryCarData,
			PrintWriter out) throws ServletException {
		ServletConfig config = getServletConfig();
		ServletContext context = config.getServletContext();
		Object obj = context.getAttribute(ConnectionServlet.KEY);
		
	    if (obj == null) {
	          throw new ServletException("ConnectionServlet not started");
	    }
	    ConnectionServlet connServlet = (ConnectionServlet)obj;
		//
		Connection conn = null;
		Statement 
		  stmtDropList = null, stmtCarData = null;
		ResultSet 
		  resultSetDropList = null, resultSetCarData = null;
			
		long startMS = System.currentTimeMillis();
		int rowCount = 0;
		boolean blnRc = false;
			
		try {
			//conn = m_connectionPool.getConnection();
			conn = connServlet.getConnection();
			
			// output drop list
			stmtDropList = conn.createStatement();
			resultSetDropList = 
				stmtDropList.executeQuery(queryDropList);
			
			formatDropDown(resultSetDropList);
			strBuffer.append("</select>\n");
			strBuffer.append("<input type=submit value=\"  search  \">");
			strBuffer.append("</form>");
			out.print(strBuffer.toString());
			
			if(iRowCount == 0) {
				//
				out.flush();
				out.print(printTableHead(strColNameAry));
			} else {
				// output data table
				stmtCarData = conn.createStatement();
				
				resultSetCarData = 
					stmtCarData.executeQuery(queryCarData);
				// output car number data
				rowCount = formatTable(resultSetCarData);
				
				out.print(strBuilder.toString());
				
				blnRc = true;
			}
			
			
		} catch (Exception ex) {
			// TODO: handle exception
			logger.warn("DB SQL execute Error");
			blnRc = false;
		} finally {
			try {
				if(resultSetDropList != null)
					resultSetDropList.close();
				if(resultSetCarData != null)
					resultSetCarData.close();
				if(conn != null)
					conn.close();
				if(stmtDropList != null)
					stmtDropList.close();
				if(stmtCarData != null)
					stmtCarData.close();
			} catch (Exception ex) {
				// TODO: handle exception
				logger.warn("DB destory error");
			}
		}
		
		if(blnRc) {
			long elapsed = System.currentTimeMillis() - startMS;
			out.println(
					"<br><i>" + rowCount + 
					" row in " + elapsed + 
					" ms</i>");// output total row 
		}
		
		return blnRc;
	}
    
	/**
	 * <p>format table.
	 * 
	 * @param resultSet enter
	 * @param out servlet's PrintWriter
	 * @return row count
	 * @throws Exception
	 */
	private int formatTable(ResultSet resultSet) 
	  throws Exception {
		
		int iRowsPerPage = 15;
		boolean blnMore = false;
		
		int iRowCount = 0;
		
		strBuilder = new StringBuilder();
		strBuilder.append("<center><table border>\n");
		
		ResultSetMetaData rsmd = resultSet.getMetaData();
		int columnCount = rsmd.getColumnCount();
		
		strBuilder.append("<tr>\n");

		for (int i = 0; i < columnCount; i++) {
			strBuilder.append("<th>" + 
					rsmd.getColumnLabel(i+1) + 
					"</th>" + "\n");
		}

		strBuilder.append("</tr>" + "\n");
		
		// search data
		while (resultSet.next()) {
			iRowCount++;

			strBuilder.append("<tr>\n");
			for (int i = 0; i < columnCount; i++) {
				String strData = resultSet.getString(i+1);				
				strBuilder.append("<td>" + 
						strData + 
						"</td>" + "\n");
				//
				if(i == 0) {
					strSN = strData;
				}
			}
			
			strBuilder.append("</tr>\n");
			//
			if ((iRowsPerPage > 0)&&(iRowCount >= iRowsPerPage)) {
				blnMore = resultSet.next();
				break;
			}
		}
		
		strBuilder.append("</table></center>\n");
		
		// add 'next' button
		if(blnMore) {
			strBuilder.append("<form method=POST action=\"" + strURI + "\">" + "\n");
			strBuilder.append("<br>\n");
			strBuilder.append("<center>\n");
			strBuilder.append("<input type=submit value=\"Next " +
                    iRowsPerPage + " rows\">" + "\n");
			strBuilder.append("</center>\n");
			//
			strBuilder.append("<input type=hidden name=lastSN value=" +
	                strSN + ">" + "\n");
			strBuilder.append("<input type=hidden name=lastPastTime value=\"" +
	                strPastTime + "\">" + "\n");
		}
		
		return iRowCount;
	}

	/**
	 * <p>format drop list
	 * 
	 * @param resultSet enter
	 * @return row count for private variable
	 * @throws Exception
	 */
	private String formatDropDown(ResultSet resultSet) 
	  throws Exception { 
		int iRowCount1 = 0;
		
		strBuffer = new StringBuffer();
		ResultSetMetaData rsmd = resultSet.getMetaData();
		int columnCount = rsmd.getColumnCount();
		
		while (resultSet.next()) {
			iRowCount1++;
			
			strBuffer.append("<option value=\"");
			//
			for (int i = 0; i < columnCount; i++) {
				String strPastTimeValue = resultSet.getString(i+1);
				
				strBuffer.append(strPastTimeValue
						+ "\">"
						+ strPastTimeValue);
			}
			strBuffer.append("</option>\n");
		}
		
		iRowCount = iRowCount1;
		return strBuffer.toString();
	}
	
	/**
	 * <p> print simulated table head
	 * 
	 * @param strings column name
	 * @return HTML code
	 */
    private String printTableHead(String[] strings) {
    	StringBuilder strBuilderTemp = new StringBuilder();
    	strBuilderTemp.append("<center><table border>\n");
    	for (int i = 0; i < strings.length; i++) {
    		strBuilderTemp.append("<th>" + 
    				strings[i] + 
    				"</th>" + "\n");
		}
    	return strBuilderTemp.toString();
    }
	
}