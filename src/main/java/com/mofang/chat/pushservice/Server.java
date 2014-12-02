package com.mofang.chat.pushservice;

import com.mofang.chat.pushservice.init.Initializer;
import com.mofang.chat.pushservice.init.impl.MainInitializer;
import com.mofang.chat.pushservice.job.JobServer;

public class Server 
{
	public static void main(String[] args) 
	{
		//String configpath = "/Users/milo/document/workspace/mofang.chat.pushservice/src/main/resources/config.ini";
		
		if(args.length <= 0)
		{
			System.out.println("usage:java -server -Xms1024m -Xmx1024m -jar mofang-chat-pushservice.jar configpath");
			System.exit(1);
		}
		String configpath = args[0];
		
		try
		{
			///服务器初始化
			System.out.println("prepare to initializing config......");
			Initializer initializer = new MainInitializer(configpath);
			initializer.init();
			System.out.println("initialize config completed!");
			
			///启动服务
			JobServer server = new JobServer();
			System.out.println("Push Server Start.");
			server.start();
		}
		catch(Exception e)
		{
			System.out.println("push server start error. message:" + e.getMessage());
			e.printStackTrace();
		}
	}
}
