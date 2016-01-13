/**
 * @(#)TimerListener.java
 * 
 * 
 * @author xiaobin
 * @version 1.0
 * @date 2009.03.08
 */
package com.tdtc.timer;

/**
 * <p>This interface is implemented by classes that wish to
 * receive timer events. The Timer class will invoke the
 * TimerEvent method for every time interval specified when
 * the Timer is started. This gives the implementing class
 * an opportunity to perform some type of time-dependent
 * checking.
 */

public interface TimerListener {
	
	/**
	 * <p>Called for each timer clock cycle
	 */
	void timeEvent(Object object);
}
