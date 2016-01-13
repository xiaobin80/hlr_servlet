/**
 * @(#)ReadConfig.java
 * 
 * @author xiaobin
 * @date 2009.03.06
 * @version 1.0
 */

package com.tdtc.xbf;

import java.io.*;
import nu.xom.*;

/**
 * <p>read XML file.
 * 
 */
public class ReadConfig {
	private String xmlFile1;
	
	public ReadConfig(String xmlFile) {
		// TODO Auto-generated constructor stub
		this.xmlFile1 = xmlFile;
	}
	
	/**
	 * <p>get XML node value.
	 * 
	 * @param index XML file index this's -1 begin
	 * @param childElements child element's name
	 * @return node value
	 * @throws Exception IOException(not found file)
	 */
	
	public String getElement(int index, String childElements) 
	  throws Exception {
		// direct read file
		//Document doc = new Builder().build(xmlFile);
		
		// use InputStream read file
		//InputStream in = new FileInputStream(xmlFile1);
		
		// use ClassLoad
		InputStream in = ClassLoader.getSystemResourceAsStream(xmlFile1);
		if (in == null) {
			ClassLoader cl = ReadConfig.class.getClassLoader();
			in = cl.getResourceAsStream(xmlFile1);
		}
		
		Document doc = new Builder().build(in);
		Elements elements = doc.getRootElement().getChildElements();
		Element element = elements.get(index);
		
		String result = element.getFirstChildElement(childElements).getValue();
		return result;
	}
}
