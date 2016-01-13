/**
 * @(#)ConnectionPool.java
 * 
 * @author xiaobin
 * @version 1.1
 * @date 2009.03.13
 */
package com.tdtc.jdbc;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.tdtc.timer.Timer;

import org.apache.log4j.*;

/**
 * <p>This class serves as a JDBC connection repository. Since
 * creating database connections is one of the most time
 * intensive aspects of JDBC, we'll create a pool of connections.
 * Each connection can be used and then replaced back into the
 * pool.
 *
 *   #(comment)
 *   JDBCDriver=<JDBC driver name>
 *   JDBCConnectionURL=<JDBC Connection URL>
 *   ConnectionPoolSize=<minimum size of the pool>
 *   ConnectionPoolMax=<maximum size of the pool, or -1 for none>
 *   ConnectionUseCount=<maximum usage count for one connection>
 *   ConnectionTimeout=<maximum idle lifetime (in minutes) of
 *        a connection>
 *   <other property for JDBC connection>=<value>
 *
 * <p>Any number of additional properties may be given (such
 * as username and password) as required by the JDBC driver.
 *
 */
public class ConnectionPool 
  implements com.cartionsoft.timer.TimerListener {
	private final Logger logger = Logger.getLogger(this.getClass());
	
	private String strUserName, strPWD;
	private ExecutorService execService;
	// set
	// MS SQL
	
//	String strUrl = "jdbc:jtds:sqlserver://127.0.0.1:1433/pubs";
//	String strDriverName = "net.sourceforge.jtds.jdbc.Driver";
//	String strUserName = "sa";
//	String strPassWord = "123456";
	
	/*
	 * mysql
	 */

//	String strUrl = "jdbc:mysql://localhost:3306/carnumber";
//	String strDriverName = "com.mysql.jdbc.Driver";
//	String strUserName="root";
//	String strPassWord="123456";
	
	// JDBC Driver name
	//String m_JDBCDriver = "com.mysql.jdbc.Driver"; // mySQL
	String m_JDBCDriver = "net.sourceforge.jtds.jdbc.Driver"; // MSSQL

	// JDBC Connection URL
	//String m_JDBCConnectionURL = "jdbc:mysql://localhost:3306/carnumber"; // mySQL
	String m_JDBCConnectionURL = "jdbc:jtds:sqlserver://127.0.0.1:1433/pubs"; // MSSQL
	
	// Minimum size of pool
	int m_ConnectionPoolSize = 5;
	
	// Maximum size of pool
	int m_ConnectionPoolMax = 100;
	
	// Maximum number of uses for a single connection, or -1 for
	// none
	int m_ConnectionUseCount = 5;
	
	// Maximum connection idle time (in minutes)
	int m_ConnectionTimeOut = 2;
	
	// objects
	Vector<ConnObject> m_pool;
	
	// The maximum number of simultaneous connections as reported
	// by the JDBC driver
	int m_MaxConnections = -1;
	
	// Our timer object
	com.cartionsoft.timer.Timer m_timer;
	
	public ConnectionPool(String userID, String password) {
		// TODO Auto-generated constructor stub
		this.strUserName = userID;
		this.strPWD = password;
	}
	
	/**
	 * <p>Initializes the ConnectionPool object
	 * 
	 * @return true if the ConnectionPool 
	 * @throws Exception
	 */
	public boolean initialize() throws Exception {
		execService = Executors.newCachedThreadPool();
		
	    // Load the configuration parameters. Any leftover parameters
	    // from the configuration file will be considered JDBC
	    // connection attributes to be used to establish the
	    // JDBC connections
		boolean blnRc = false;
		try {
			// Properties were loaded; attempt to create our pool
	        // of connections
			createPool();
			
	        // Start our timer so we can timeout connections. The
	        // clock cycle will be 20 seconds
			m_timer = new Timer(this, 20);
			execService.execute(m_timer);
			blnRc = true;
		} catch (Exception ex) {
			// TODO: handle exception
		}
		return blnRc;
	}
	
	/**
	 * <p>Destroys the pool and it's contents. Close any open 
	 * JDBC connections and frees all resource
	 */
	public void destroy() {
		try {
			// Stop our time thread
			if (m_timer != null) {
				execService.shutdown();
			}
			
			// Clear our pool
			if (m_pool != null) {
				// Loop through the pool and close each connection
				for (int i = 0; i < m_pool.size(); i++) {
					close((ConnObject) m_pool.elementAt(i));
				}
			}
			m_pool = null;
		} catch (Exception ex) {
			// TODO: handle exception
			ex.printStackTrace();
		}
	}	
	
	/**
	 * <p>Creates the initial connection pool. A timer thread
     * is also created so that connection timeouts can be
     * handled.
	 * 
	 * @throws Exception
	 */
	private void createPool() throws Exception {		
	    // Attempt to create a new instance of the specified
	    // JDBC driver. Well behaved drivers will register
	    // themselves with the JDBC DriverManager when they
	    // are instantiated
		java.sql.Driver driver = (java.sql.Driver) 
		  Class.forName(m_JDBCDriver).newInstance();
		
		// Create the vector for the pool
		m_pool = new Vector<ConnObject>();
		
		// Bring the pool to the minimum size
		fillPool(m_ConnectionPoolSize);
	}
	

	
	/**
	 * <p>Brings the pool to the given size
	 * 
	 * @param size pool size
	 * @throws Exception
	 */
	private synchronized void fillPool(int size) throws Exception {		
		// Loop while we need to create more connections
		while (m_pool.size() < size) {
			ConnObject co = new ConnObject();

			// if mysql 
			//strUserName = "root";
			co.connection = 
				DriverManager.getConnection(m_JDBCConnectionURL, strUserName, strPWD);

			// Do some sanity checking on the first connection in 
			// the pool
			if (m_pool.size() == 0) {
				// Get the maximum number of simultaneous connections
				// as reported by the JDBC driver
				java.sql.DatabaseMetaData md = co.connection.getMetaData();
				m_MaxConnections = md.getMaxConnections();
			}
			
			// Given a warning if the size of the pool will exceed
			// the maximum number of connections allowed by the 
			// JDBC driver
			if ((m_MaxConnections > 0) && 
					size < m_MaxConnections) {
				trace("WARNING: Size of pool will exceed safe maximum of " + m_MaxConnections);
			}
			
			// Clear the in use flag
			co.inUse = false;
			
			// Set last access time
			touch(co);
			
			m_pool.addElement(co);
		}
	}
	

	
	/**
	 * <p>Find the given connection in the pool
	 * 
	 * @param connection Connection object
	 * @return Index into the pool, or -1 if not found
	 */
	private int find(java.sql.Connection connection) {
		int index = -1;
		
		// Find the matching Connection in the pool
		if ((connection != null) && 
				(m_pool != null)) {
			for (int i = 0; i < m_pool.size(); i++) {
				ConnObject co = 
					(ConnObject) m_pool.elementAt(i);
				if (co.connection == connection) {
					index = i;
					break;
				}
			}
		}
		return index;
	}
	
	/**
	 * <p>Sets the last access time for the given ConnObject
	 * 
	 * @param connObject
	 */
	private void touch(ConnObject connObject) {
		if (connObject != null) {
			connObject.lastAccess = System.currentTimeMillis();
		}
	}
	
	
	/**
	 * <p>Gets an available JDBC Connection. Connection will be 
	 * created if necessary, up to the maximum number of connections 
	 * as specified in the configuration file.
	 *  
	 * @return JDBC Connection, or null if the maximum
	 * number of connections has been exceeded
	 */
	public synchronized java.sql.Connection getConnection() {
		// If there is no pool it must have been destroyed
		if (m_pool == null) {
			return null;
		}
		
		java.sql.Connection conn = null;
		ConnObject connObject = null;
		int poolSize = m_pool.size();
		
		// Get the next available connection
		for (int i = 0; i < poolSize; i++) {
			// Get the ConnObject from the pool
			ConnObject co = 
				(ConnObject) m_pool.elementAt(i);
			
			// If this a valid connection and it is not use,
			// grab it
			if (co.isAvailable()) {
				connObject = co;
				break;
			}
		}
		
		// No more available connections. If we aren't at the 
		// maximum number of connections, create a new entry
		// in the pool
		if (connObject == null) {
			if ((m_ConnectionPoolMax < 0) || 
					((m_ConnectionPoolMax > 0) && 
							(poolSize < m_ConnectionPoolMax))) {
				
				// Add a new connection
				int i = addConnection();
				
				// If a new connection was created, use it
				if (i >= 0) {
					connObject = 
						(ConnObject) m_pool.elementAt(i);
				} 
			} else {
				trace("Maximum number of connections exceeded");
			}
		}
		
		// If we have a connection, set the last time accessed,
		// the use count, and the in use flag
		if (connObject != null) {
			connObject.inUse = true;
			connObject.useCount++;
			touch(connObject);
			conn = connObject.connection;
		}
		return conn;
	}
	
	/**
	 * <p>Add a new connection to the pool
	 * 
	 * @return Index of the new pool entry, 
	 * or -1 if an error has occurred
	 */
	private int addConnection() {
		int index = -1;
		try {
			// Calculate the new size of the pool
			int size = m_pool.size() + 1;
			
			// Create a new entry 
			fillPool(size);
			
			// Set the index pointer to the new connection if one
			// was created
			if (size == m_pool.size()) {
				index = size - 1;
			}
		} catch (Exception ex) {
			// TODO: handle exception
			ex.printStackTrace();
		}
		return index;
	}
	
	/**
	 * <p>Called by the time each time a clock cycle expires.
	 * This given us the opportunity to timeout connections
	 */
	public synchronized void timeEvent(Object object) {
		// TODO Auto-generated method stub
		// No pool means  no work
		if (m_pool == null) {
			return;
		}
		
		// Get current time in milliseconds
		long now = System.currentTimeMillis();
		
		for (int i = m_pool.size() - 1; i >= 0; i--) {
			ConnObject co = 
				(ConnObject) m_pool.elementAt(i);
			
			// If the connection is not in use and it has not been
			// uses recently, remove it
			if (!co.inUse) {
				if ((m_ConnectionTimeOut > 0) && 
						co.lastAccess + (m_ConnectionTimeOut * 1000) < now) {
					removeFromPool(i);
				}
			}
		}
		
		// Remove any connections that are no longer open
		for (int i = m_pool.size() - 1; i >= 0; i--) {
			ConnObject co = 
				(ConnObject) m_pool.elementAt(i);
			try {
				// If the connection is closed, remove it from the pool
				if (co.connection.isClosed()) {
					trace("Connection closed unexpectedly");
					removeFromPool(i);
				}
			} catch (Exception ex) {
				// TODO: handle exception
			}
		}
		
		// Now ensure that that the pool is still at it's minimum size
		try {
			if (m_pool != null) {
				if (m_pool.size() < m_ConnectionPoolSize) {
					fillPool(m_ConnectionPoolSize);
				}
			}
		} catch (Exception ex) {
			// TODO: handle exception
			ex.printStackTrace();
		}
	}
	
	/**
	 * <p>Removes the ConnObject from the pool at the 
	 * given index
	 * 
	 * @param index Index into the pool vector
	 */
	private synchronized void removeFromPool(int index) {
		// Make sure the pool and index are valid
		if (m_pool != null) {
			if (index < m_pool.size()) {
				// Get the ConnObject and close the connection
				ConnObject co = 
					(ConnObject) m_pool.elementAt(index);
				close(co);
				
				// Remove the element from the pool
				m_pool.removeElementAt(index);
			}
		}
	}
	
	/**
	 * <p>Closes the connection in the given ConnectionObject
	 * 
	 * @param connObject ConnObject object
	 */
	private void close(ConnObject connObject) {
		if (connObject != null) {
			if (connObject.connection != null) {
				try {
					connObject.connection.close();
				} catch (Exception ex) {
					// TODO: handle exception
				}
				
				// Clear the connection object reference
				connObject.connection = null;
			}
		}
	}
	
	/**
	 * <p>Places the connection back into the connection pool,
	 * or closes the connection if maximum use count has 
	 * been reached
	 * 
	 * @param connection Connection object to close
	 */
	public synchronized void close(java.sql.Connection connection) {
		// Find the connection in the pool
		int index = find(connection);
		
		if (index != -1) {
			ConnObject co = 
				(ConnObject) m_pool.elementAt(index);
			
			// If the use count exceeded the max, remove it from 
			// the pool.
			if ((m_ConnectionUseCount > 0) && 
					(co.useCount >= m_ConnectionUseCount)) {
				trace("Connection use count exceeded");
				removeFromPool(index);
			} else {
				// Clear the use count and reset the time last used
				touch(co);
				co.inUse = false;
			}
		}
	}
	
	/**
	 * <p>Trace the given string
	 * 
	 * @param msg content
	 */
	private void trace(String msg) {
		logger.info("Connection Pool: " + msg);
	}
}






/**
 * <p>This package-private class is used to represent a single
 * connection object
 * 
 * @author xiaobin
 *
 */
class ConnObject {
	// The JDBC Connection
	public java.sql.Connection connection;
	
	// true if this connection is currently in use
	public boolean inUse;
	
	// The last time (in milliseconds) that this connection was used
	public long lastAccess;
	
	// The number of times this connection has been used
	public int useCount;
	
	/**
	 * <p>Determine if the connection is available 
	 * 
	 * @return true if the connection can be used
	 */
	public boolean isAvailable() {
		boolean result = false;
		try {
			
			// To be available, the connection cannot be in use
			// and must be open
			if (connection != null) {
				if (!inUse && 
						!connection.isClosed()) {
					result = true;
				}
			}
		} catch (Exception ex) {
			// TODO: handle exception
		}
		
		return result;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Connection= " + connection + ", inUse=" + inUse + 
		  ", lastAccess=" + lastAccess + ", useCount=" + useCount;
	}
}
