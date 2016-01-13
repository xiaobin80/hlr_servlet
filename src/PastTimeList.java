/**
 * @(#)PastTimeList.java
 * 
 * @author xiaobin
 * @version 1.1
 * @date 2007.12.23
 * 
 * @version 1.2
 * @date 2009.03.06
 */

import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.tdtc.xbf.ReadConfig;
import com.tdtc.xbf.ReadXbf;
import org.apache.log4j.*;


/**
 * <p> This is class search car number data.
 * data source is MS SQL 2000. use JNI interface class.  
 * 
 */
 public class PastTimeList extends javax.servlet.http.HttpServlet 
 											implements javax.servlet.Servlet {
   static final long serialVersionUID = 1L;
   
   private final Logger logger = Logger.getLogger(this.getClass());
   private String strUserName;
   private String strPassWord;
   private String strURI;
   private static String strXbfContent;
   private String strSN;
   private String strPastTime;
   private String strQueryA, strQueryB;
   private int iRowCount;
   private StringBuilder strBuilder;
   private static String strLibPath;
   private static String strXbfPath;
   private static ReadConfig readConfig;
   
    /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public PastTimeList() {
		super();
		strBuilder = new StringBuilder();
	}
	static {
		// 
		readConfig = new ReadConfig("D://war//config//configP.xml");
		
		try {
			strLibPath = readConfig.getElement(1, "lib-file1");
			strXbfPath = readConfig.getElement(2, "xbf-file1");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		ReadXbf readX = new ReadXbf(strLibPath);
		strXbfContent = readX.readRecordMsSql(-1, strXbfPath);
	}	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
	  throws ServletException, IOException {
		// TODO Auto-generated method stub
		logger.info("post html: " + strPastTime);
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
		//Properties props=new Properties();
		// output html code
		outPrintWriter.println("<html>");
		outPrintWriter.println("<head><title>past car</title></head>");
		outPrintWriter.println("<h2><center>");
		outPrintWriter.println("car number list");
		outPrintWriter.println("</center></h2>");
		outPrintWriter.println("<br>");// enter
		
		try {
			strURI = readConfig.getElement(0, "url1");
		} catch (Exception e) {
			// TODO: handle exception
			logger.warn("read xml error");
		}
		
		outPrintWriter.println("<form method=POST action=\""
					+ strURI
					+ "\">select past car time");
		outPrintWriter.println("<select name=pastTime size=1>");
		// set
		// MS SQL
		
//		String strUrl = "jdbc:jtds:sqlserver://127.0.0.1:1433/pubs";
//		String strDriverName = "net.sourceforge.jtds.jdbc.Driver";
//		String strUserName = "sa";
//		String strPassWord = "123456";
		
		/*
		 * mysql
		 */
	
		String strUrl = "jdbc:mysql://localhost:3306/carnumber";
		String strDriverName = "com.mysql.jdbc.Driver";
//		String strUserName="root";
//		String strPassWord="123456";
		
		try {
			strPassWord = getPWDMsSql(strXbfContent);
			strUserName = getDBUserMsSql(strXbfContent);
			strUserName = "root";
		} catch (Exception e) {
			// TODO: handle exception
			logger.warn("DB connection error");
			e.printStackTrace();
		}
		
		//
		strSN = request.getParameter("lastSN");
		if(strSN == null) {
			strSN = "0";
		}
		
		String strTempPastTime = request.getParameter("lastPastTime");
		
		if(strTempPastTime != null) {
			strPastTime = strTempPastTime;
		} else {
			logger.info("per page: " + strPastTime);
			strPastTime = request.getParameter("pastTime");
		}
		
		if (iRowCount == 0) {
			strQueryB = "select DISTINCT past_time from trainOrder order by past_time";
		}
		
		strQueryA = 
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
				strDriverName, 
				strUrl,
				strUserName,
				strPassWord, 
				//props,
				strQueryB,
				strQueryA, 
				outPrintWriter);
		
		// output html last
		outPrintWriter.println("</body></html>");
		
		// process printWriter
		outPrintWriter.flush();
		outPrintWriter.close();
	}  	
	

	/**
	 * <p> output dropList and table data
	 * 
	 * @param driveName JDBC drive name
	 * @param connectionURL JDBC URL
	 * @param userName DB user 
	 * @param passWord DB password
	 * @param queryDropList SQL statement(drop list)
	 * @param queryCarData  SQL statement(table)
	 * @param out servlet's PrintWriter
	 * @return execute success
	 */
	private boolean outHtmlData(
			String driveName,
			String connectionURL,
			String userName,
			String passWord,
			//Properties props,
			String queryDropList,
			String queryCarData,
			PrintWriter out) {
		
		boolean blnRc = true;
		Connection conn = null;
		Statement 
		  stmtDropList = null, stmtCarData = null;
		ResultSet 
		  resultSetDropList = null, resultSetCarData = null;
			
		long startMS = System.currentTimeMillis();
		int rowCount = 0;
		try {
			Class.forName(driveName).newInstance();
			conn = DriverManager.getConnection(
					connectionURL, userName, passWord);
			stmtDropList = conn.createStatement();
			stmtCarData = conn.createStatement();
			//
			resultSetDropList = stmtDropList.executeQuery(queryDropList);
			resultSetCarData = stmtCarData.executeQuery(queryCarData);
			// output drop list
			if(iRowCount == 0) {
				formatDropDown(resultSetDropList);
				out.print(strBuilder.toString());
			} else {
				out.print(strBuilder.toString());
			}
			
			out.flush();
			// output car number data
			rowCount = formatTable(resultSetCarData, out);
		} catch (Exception e) {
			// TODO: handle exception
			logger.warn("DB Error");
			out.println("Data Base Error");
			e.printStackTrace(out);
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
				logger.info("DB destory error");
			}
		}
		
		if(blnRc) {
			long elapsed = System.currentTimeMillis() - startMS;
			out.println(
					"<br><i>" + rowCount + " row in " + elapsed + " ms</i>");// output total row 
		}
		
		return blnRc;
	}

	/**
	 * <p> format table.
	 * 
	 * @param resultSet enter
	 * @param out servlet's PrintWriter
	 * @return row count
	 * @throws Exception
	 */
	
	private int formatTable(ResultSet resultSet,PrintWriter out) 
	  throws Exception {
		
		int iRowsPerPage = 15;
		boolean blnMore = false;
		
		int iRowCount = 0;
		out.println("<center><table border>");
		ResultSetMetaData rsmd = resultSet.getMetaData();
		int columnCount = rsmd.getColumnCount();
		out.println("<tr>");

		for (int i = 0; i < columnCount; i++) {
			out.println("<th>"
						+ rsmd.getColumnLabel(i+1)
						+ "</th>");
		}
		out.println("</tr>");

		// search data
		while (resultSet.next()) {
			iRowCount++;
			out.println("<tr>");
			for (int i = 0; i < columnCount; i++) {
				String strData = resultSet.getString(i+1);
				out.println("<td>"
							+ strData
							+ "</td>");
				//
				if(i == 0) {
					strSN = strData;
				}
			}
		
			out.println("</tr>");

			//
			if ((iRowsPerPage > 0)&&(iRowCount >= iRowsPerPage)) {
				blnMore = resultSet.next();
				break;
			}
		}
		out.println("</table></center>");
		// add 'next' button
		if(blnMore) {
			out.println("<form method=POST action=\"" + strURI + "\">");
			out.println("<br/>");
			out.println("<center>");
			out.println("<input type=submit value=\"Next " +
                    iRowsPerPage + " rows\">");
			out.println("</center>");
			//
			out.println("<input type=hidden name=lastSN value=" +
                strSN + ">");
			out.println("<input type=hidden name=lastPastTime value=\"" +
	                strPastTime + "\">");
		}
		
		return iRowCount;
	}

	/**
	 * <p> format drop list
	 * 
	 * @param resultSet enter
	 * @return row count for private variable
	 * @throws Exception
	 */
	private int formatDropDown(ResultSet resultSet) 
	  throws Exception { 
		int iRowCount1 = 0;
		ResultSetMetaData rsmd = resultSet.getMetaData();
		int columnCount = rsmd.getColumnCount();
		
		while (resultSet.next()) {
			iRowCount1++;
			
			strBuilder.append("<option value=\"");
			//
			for (int i = 0; i < columnCount; i++) {
				String strPastTimeValue = resultSet.getString(i+1);
				
				strBuilder.append(strPastTimeValue
						+ "\">"
						+ strPastTimeValue);
			}
			strBuilder.append("</option>\n");
		}
		
		strBuilder.append("</select>\n");
		strBuilder.append("<input type=submit value=\"  search  \">");
		strBuilder.append("</form>");
		
		iRowCount = iRowCount1;
		return iRowCount1;
	}
	
	/**
	 * <p> read private strXbfContent value
	 * 
	 * @param XbfContent xbf file's content
	 * @return xbf file's DB user
	 */
	
	public String getDBUserMsSql(String XbfContent) {
		int iBeginIndex = XbfContent.indexOf("ID=");
		int iEndIndex = XbfContent.indexOf(";I");
		String strUser = XbfContent.substring(iBeginIndex + 3, iEndIndex);
		return strUser;
	}

	/**
	 * <p> read private strXbfContent value
	 * 
	 * @param XbfContent xbf file's content
	 * @return xbf file's password
	 */
	public String getPWDMsSql(String XbfContent) {
		int iBeginIndex = XbfContent.indexOf("d=");
		int iEndIndex = XbfContent.indexOf(";Per");
		String strPwd = XbfContent.substring(iBeginIndex + 2, iEndIndex);
		return strPwd;
	}
}