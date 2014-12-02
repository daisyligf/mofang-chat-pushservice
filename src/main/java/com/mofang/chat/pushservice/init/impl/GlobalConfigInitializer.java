package com.mofang.chat.pushservice.init.impl;

import java.io.IOException;

import com.mofang.chat.pushservice.init.AbstractInitializer;
import com.mofang.chat.pushservice.global.GlobalConfig;
import com.mofang.framework.util.IniParser;

/**
 * 
 * @author zhaodx
 *
 */
public class GlobalConfigInitializer extends AbstractInitializer
{
	private String configPath;
	
	public GlobalConfigInitializer(String configPath)
	{
		this.configPath = configPath;
	}
	
	@Override
	public void load() throws IOException 
	{
		IniParser config = new IniParser(configPath);
		GlobalConfig.LISTEN_PUSH_QUEUE_DATA_TYPE = config.getInt("common", "listen_push_queue_data_type");
		
		GlobalConfig.LOG4J_CONFIG_PATH = config.get("conf", "log4j_config_path");
		GlobalConfig.REDIS_MASTER_CONFIG_PATH = config.get("conf", "redis_master_config_path");
		GlobalConfig.REDIS_SLAVE_CONFIG_PATH = config.get("conf", "redis_slave_config_path");
		GlobalConfig.PUSH_QUEUE_CONFIG_PATH = config.get("conf", "push_queue_config_path");
		GlobalConfig.FESERVER_CONFIG_PATH = config.get("conf", "feserver_config_path");
		GlobalConfig.GUILD_SLAVE_CONFIG_PATH = config.get("conf", "guild_slave_config_path");
	}
}