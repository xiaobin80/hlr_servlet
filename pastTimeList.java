/**
 * ******************************************************************************************************
 *                                             Vehicle information system
 *                                               Browse data
 *                                                Main Class
 *                                               
 *                              (c) Copyright 2010-2015, TDTC, HRB, ZH
 *                                            All Right Reserved
 *                              license: Apache License Version 2.0
 * 
 * File : pastTimeList.java
 * By   : Li.Guibin
 * ******************************************************************************************************
 */

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//
import java.io.*;
//import java.util.*;
import java.sql.*;


/**
 * Servlet implementation class for Servlet: pastTimeList
 * @author xiaobin
 * @date 2007.12.23
 * @version 1.1
 */
 public class pastTimeList extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
   static final long serialVersionUID = 1L;
   
    /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public pastTimeList() {
		super();
	}   	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//设置 网页头
		response.setContentType("text/html; charset=GBK");
		//声明 PrintWriter
		PrintWriter out=new PrintWriter(response.getOutputStream());
		//Properties props=new Properties();
		//输出 网页代码
		out.println("<html>");
		out.println("<head><title>车号过车列表</title></head>");
		out.println("<h2><center>");
		out.println("车号详细列表");
		out.println("</center></h2>");
		out.println("<br>");//换行
		String sURIstr="http://localhost:9090/CarNumber/pastTimeList";//声明 servlet uri
		out.println("<form method=POST action=\""
					+sURIstr
					+"\">选择过车时间");
		out.println("<select name=pastTime size=1>");
		//设置
		String urlStr="jdbc:jtds:sqlserver://localhost:1433/pubs";
		String driverNameStr="net.sourceforge.jtds.jdbc.Driver";
		String userNameStr="sa";
		String passWordStr="123456";
		//输出 下拉框数据
		String queryStrB="select DISTINCT past_time from trainOrder order by past_time";
		outPastTimeList(driverNameStr, 
				urlStr,
				userNameStr,
				passWordStr,
				//props, 
				queryStrB, 
				out);
		out.println("</select>");
		out.println("<input type=submit value=\"  查询  \">");
		out.println("</form>");
		//输出 车号数据
		String pastTimeStr=request.getParameter("pastTime");
		String queryStrA="select seriary_number as 辆序,car_number as 车号," +
        				"car_marque as 车型,carry_weight1 as [标重(吨)],self_weight1 as [自重(吨)],past_time as 过车时间 " +
        				"from trainOrder where past_time='"
        				+pastTimeStr
        				+"' order by seriary_number";
		outCarNumber(driverNameStr, 
					urlStr,
					userNameStr,
					passWordStr, 
					//props, 
					queryStrA, 
					out);
		//输出 网页结尾代码
		out.println("</body></html>");
		//处理 PrintWriter
		out.flush();
		out.close();
	}  	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		this.doGet(request, response);
	}
	//
	private boolean outCarNumber(String driveName,
								String connectionURL,
								String userName,
								String passWord,
								//Properties props,
								String queryStr,
								PrintWriter outPW){
		boolean rc=true;
		Connection conn=null;
		Statement stmt=null;
		ResultSet resultSet1=null;
		
		long startMS=System.currentTimeMillis();
		int rowCount=0;
		try {
			Class.forName(driveName).newInstance();
			conn=DriverManager.getConnection(connectionURL,userName,passWord);
			stmt=conn.createStatement();
			resultSet1=stmt.executeQuery(queryStr);
			//
			rowCount=formatTableCarNum(resultSet1, outPW);
		} catch (Exception e) {
			// TODO: handle exception
			outPW.println("Exception!");
			e.printStackTrace(outPW);
			rc=false;
		}finally{
			try {
				if(resultSet1!=null)
					resultSet1.close();
				if(conn!=null)
					conn.close();
				if(stmt!=null)
					stmt.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
		if(rc){
			long elapsed=System.currentTimeMillis()-startMS;
			outPW.println("<br><i>"+rowCount+"row in"+elapsed+"ms</i>");//输出行数和用时
		}
		return rc;
	}
	//
	private int formatTableCarNum(ResultSet resultSet2,
							PrintWriter outPW)throws Exception{
		int rowCount=0;
		outPW.println("<center><table border>");
		ResultSetMetaData rsmd=resultSet2.getMetaData();
		int columnCount=rsmd.getColumnCount();
		outPW.println("<tr>");
		for (int i = 0; i < columnCount; i++) {
			outPW.println("<th>"
						+rsmd.getColumnLabel(i+1)
						+"</th>");
		}
		outPW.println("</tr>");
		//检索数据
		while (resultSet2.next()) {
			rowCount++;
			outPW.println("<tr>");
			for (int i = 0; i < columnCount; i++) {
				outPW.println("<td>"
							+resultSet2.getString(i+1)
							+"</td>");
			}
			outPW.println("</tr>");
		}
		outPW.println("</table></center>");
		return rowCount;
	}
	//输出下拉框函数与输出车号函数相同，只是输出格式不同
	//
	private boolean outPastTimeList(String driveName,
								String connectionURL,
								String userName,
								String passWord,
								//Properties props,
								String queryStr,
								PrintWriter outPW){
		boolean rc=true;
		Connection conn=null;
		Statement stmt=null;
		ResultSet resultSet1=null;
		
		int rowCount=0;
		try {
			Class.forName(driveName).newInstance();
			conn=DriverManager.getConnection(connectionURL,userName,passWord);
			stmt=conn.createStatement();
			resultSet1=stmt.executeQuery(queryStr);
			//
			rowCount=formatDropDown(resultSet1, outPW);
		} catch (Exception e) {
			// TODO: handle exception
			outPW.println("Exception!");
			e.printStackTrace(outPW);
			rc=false;
		}finally{
			try {
				if(resultSet1!=null)
					resultSet1.close();
				if(conn!=null)
					conn.close();
				if(stmt!=null)
					stmt.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
		return rc;
	}
	//
	private int formatDropDown(ResultSet resultSet3,PrintWriter outPW)throws Exception{
		int rowCount=0;
		ResultSetMetaData rsmd=resultSet3.getMetaData();
		int columnCount=rsmd.getColumnCount();
		while (resultSet3.next()) {
			rowCount++;
			outPW.print("<option value=\"");
			//
			for (int i = 0; i < columnCount; i++) {
				String pastTimeValueStr=resultSet3.getString(i+1);
				outPW.print(pastTimeValueStr
						+"\">"
						+pastTimeValueStr);
			}
			outPW.println("</option>");
		}
		return rowCount;
	}
}