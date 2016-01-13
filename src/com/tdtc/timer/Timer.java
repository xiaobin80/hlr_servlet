/**
 * @(#)Timer.java
 * 
 * @author xiaobin
 * @version 1.1
 * @date 2009.03.13
 */
package com.tdtc.timer;

import java.util.concurrent.*;

/**
 * <p>This class implements a simple timer. Every time the
 * timer clock cycle that is specified expires the TimerEvent
 * method on the given TimerListener object will be invoked.
 * This gives the object a chance to perform some type of
 * timeout checking.
 */

public class Timer implements Runnable {

	TimerListener m_timerListener;
	
	// Number of seconds in each timer cycle
	int m_cycle;
	
	Object m_object;
	
	
	/**
	 * <p>Constructs a new Timer object(normal)
	 * 
	 * @param timerListener Object that will receive TimerEvent
	 * notifications
	 * @param cycle Number of seconds in each timer cycle
	 */
	public Timer(TimerListener timerListener, int cycle) {
		// TODO Auto-generated constructor stub
		m_timerListener = timerListener;
		m_cycle = cycle;
		m_object = null;
	}
	
	/**
	 * <p>Constructs a new Timer object(Object evaluation)
	 * 
	 * @param timerListener Object that will receive TimerEvent
	 * notifications
	 * @param cycle Number of seconds in each timer cycle
	 * @param object Object to be supplied with the TimerEvent
     * notification
	 */
	public Timer(TimerListener timerListener, int cycle, Object object) {
		// TODO Auto-generated constructor stub
		m_timerListener = timerListener;
		m_cycle = cycle;
		m_object = object;
	}

	/**
	 * <p>Runs the timer. The timer will run until stopped and
     * fire a TimerEvent notification every clock cycle
	 */
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			try {
				TimeUnit.SECONDS.sleep(m_cycle);
			} catch (InterruptedException interEx) {
				// TODO: handle exception
				
			}
			if (m_timerListener != null) {
				m_timerListener.timeEvent(m_object);
			}
		}
	}

}
