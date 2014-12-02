package com.mofang.chat.pushservice.init.impl;

import com.mofang.chat.business.sysconf.SysObject;
import com.mofang.chat.pushservice.init.AbstractInitializer;
import com.mofang.chat.pushservice.global.GlobalConfig;

/**
 * 
 * @author zhaodx
 *
 */
public class GlobalObjectInitializer extends AbstractInitializer
{
	@Override
	public void load() throws Exception
	{
		SysObject.initRedisMaster(GlobalConfig.REDIS_MASTER_CONFIG_PATH);
		SysObject.initRedisSlave(GlobalConfig.REDIS_SLAVE_CONFIG_PATH);
		SysObject.initGuildSlave(GlobalConfig.GUILD_SLAVE_CONFIG_PATH);
		SysObject.initPushQueue(GlobalConfig.PUSH_QUEUE_CONFIG_PATH);
	}
}