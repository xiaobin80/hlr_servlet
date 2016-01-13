/**
 * @(#)ReadXbf.java
 * 
 * @author xiaobin
 * @date 2009.03.06
 * @version 1.0
 */


package com.tdtc.xbf;

/**
 * <p> JNI interface's class.
 * 
 */

public class ReadXbf {
	private int count = 0;
	private final int incI = count++;
	
	public ReadXbf(String libPath) {
		// TODO Auto-generated constructor stub
		if(incI < 2) System.load(libPath);
		else return;
	}
	
	public native String readRecordMsSql(int dimRecord, String filename1);
	public native String readRecordMdb(int dimRecord, String filename1);
	
}
