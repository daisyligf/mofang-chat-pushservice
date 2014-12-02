package com.mofang.chat.pushservice.global;

import org.apache.log4j.Logger;

/**
 * 
 * @author zhaodx
 *
 */
public class GlobalObject
{
	/**
	 * Global Info Logger Instance 
	 */
	public final static Logger INFO_LOG = Logger.getLogger("pushservice.info");
	
	/**
	 * Global Error Logger Instance
	 */
	public final static Logger ERROR_LOG = Logger.getLogger("pushservice.error");
}