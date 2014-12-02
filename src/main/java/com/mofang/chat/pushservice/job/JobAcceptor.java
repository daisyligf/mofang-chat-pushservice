package com.mofang.chat.pushservice.job;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.mofang.chat.business.redis.PushQueueRedis;
import com.mofang.chat.business.redis.impl.PushQueueRedisImpl;
import com.mofang.chat.pushservice.global.GlobalConfig;
import com.mofang.chat.pushservice.global.GlobalObject;
import com.mofang.framework.util.StringUtil;

/**
 * 
 * @author zhaodx
 *
 */
public class JobAcceptor
{
	private PushQueueRedis pushQueue = PushQueueRedisImpl.getInstance();
	private Map<String, FrontendEntity> feMap;
	private ExecutorService executor = null;
	
	public JobAcceptor(Map<String, FrontendEntity> feMap, ExecutorService executor)
	{
		this.feMap = feMap;
		this.executor = executor;
	}
	
	public void start()
	{
		try
		{
			while(true)
			{
				String message = pushQueue.get(GlobalConfig.LISTEN_PUSH_QUEUE_DATA_TYPE);
				if(StringUtil.isNullOrEmpty(message))
				{
					Thread.sleep(1);
					continue;
				}
				
				GlobalObject.INFO_LOG.info("job acceptor receive message:" + message);
				JobHandler handler = new JobHandler(message, feMap);
				executor.execute(handler);
			}
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at JobAcceptor.start throw an error.", e);
		}
	}
}