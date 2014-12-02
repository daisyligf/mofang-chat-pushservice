package com.mofang.chat.pushservice.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.impl.client.CloseableHttpClient;

import com.mofang.chat.pushservice.global.GlobalConfig;
import com.mofang.chat.pushservice.global.GlobalObject;
import com.mofang.framework.net.http.HttpClientConfig;
import com.mofang.framework.net.http.HttpClientProvider;

/**
 * 
 * @author zhaodx
 *
 */
public class JobServer
{
	private final static int queueSize = 100000;
	private final static int coreThreads = Runtime.getRuntime().availableProcessors() * 2 + 1;
	private final static int maxThreads = coreThreads * 2 + 1;
	private final static BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(queueSize);
	private final static RejectedExecutionHandler rejectHandler = new ThreadPoolExecutor.CallerRunsPolicy();
	
	public void start()
	{
		try
		{
			///handlers thread pool
			ThreadPoolExecutor executor = new ThreadPoolExecutor(coreThreads, maxThreads, 0L, TimeUnit.MILLISECONDS, queue, rejectHandler);
			///fill listener map (FE)
			Map<String, FrontendEntity> feMap = getFrontendEntityMap();
			///start job acceptor
			JobAcceptor acceptor = new JobAcceptor(feMap, executor);
			acceptor.start();
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at JobServer.start throw an error.", e);
		}
	}
	
	private Map<String, FrontendEntity> getFrontendEntityMap()
	{
		Map<String, FrontendEntity> feMap = new HashMap<String, FrontendEntity>();
		FrontendProvider feprovider = new FrontendProvider();
		List<FrontendInfo> frontendList = feprovider.getFrontendList(GlobalConfig.FESERVER_CONFIG_PATH);
		if(null == frontendList || frontendList.size() == 0)
			return null;
		
		FrontendEntity entity = null;
		HttpClientConfig config = null;
		CloseableHttpClient httpClient = null;
		for(FrontendInfo feInfo : frontendList)
		{
			config = new HttpClientConfig();
			config.setHost(feInfo.getHost());
			config.setPort(feInfo.getPort());
			config.setMaxTotal(feInfo.getMaxTotal());
			config.setCharset(feInfo.getCharset());
			config.setConnTimeout(feInfo.getConnTimeout());
			config.setSocketTimeout(feInfo.getSocketTimeout());
			config.setDefaultKeepAliveTimeout(feInfo.getDefaultKeepAliveTimeout());
			config.setCheckIdleInitialDelay(feInfo.getCheckIdleInitialDelay());
			config.setCheckIdlePeriod(feInfo.getCheckIdlePeriod());
			config.setCloseIdleTimeout(feInfo.getCloseIdleTimeout());
			
			HttpClientProvider provider = new HttpClientProvider(config);
			httpClient = provider.getHttpClient();
			
			entity = new FrontendEntity();
			entity.setFrontendInfo(feInfo);
			entity.setHttpClient(httpClient);
			feMap.put(feInfo.getHost(), entity);
		}
		return feMap;
	}
}