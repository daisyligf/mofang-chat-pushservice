package com.mofang.chat.pushservice.job.listener.impl;

import org.apache.http.impl.client.CloseableHttpClient;

import com.mofang.chat.pushservice.global.GlobalObject;
import com.mofang.chat.pushservice.job.FrontendEntity;
import com.mofang.chat.pushservice.job.listener.Pusher;
import com.mofang.framework.net.http.HttpClientSender;

/**
 * 
 * @author zhaodx
 *
 */
public class FrontendPusher implements Pusher
{
	private FrontendEntity feEntity;
	public FrontendPusher(FrontendEntity feInfo)
	{
		this.feEntity = feInfo;
	}
	
	@Override
	public void push(String message)
	{
		try
		{
			CloseableHttpClient httpClient = feEntity.getHttpClient();
			String url = feEntity.getFrontendInfo().getPushUrl();
			String result = HttpClientSender.post(httpClient, url, message);
			GlobalObject.INFO_LOG.info("pusher component push message:" + message + " response:" + result);
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at FrontendPusher.push throw an error.", e);
		}
	}
}