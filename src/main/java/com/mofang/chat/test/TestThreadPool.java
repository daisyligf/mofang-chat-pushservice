package com.mofang.chat.test;

import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestThreadPool {
	private static final int produceTaskSleepTime = 2;
	private static final int produceTaskMaxNumber = 10;
	
	public static void main(String[] args) {
		//构造一个线程池
		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2, 4, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(3),
				new ThreadPoolExecutor.CallerRunsPolicy()
			);
		
		for (int i = 0; i < produceTaskMaxNumber; i++) {
			try {
				//产生一个人任务，并放入线程池
				String task = "task@" + i;
				System.out.println(task);
				//System.out.println(threadPool.getQueue());
				threadPool.execute(new ThreadPoolTask(task));
				
				//便于观察，等待一段时间
				Thread.sleep(produceTaskSleepTime);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		
	}
}

class ThreadPoolTask implements Runnable,Serializable {
	private static int consumeTaskSleepTime = 2000;
	
	//保存任务所需要的数据
	private Object threadPoolTaskData;
	
	ThreadPoolTask(Object tasks) {
		this.threadPoolTaskData = tasks;
	}
	@Override
	public void run() {
		//处理一个任务，这里仅仅打印
		System.out.println(Thread.currentThread().getName());
		System.out.println("start .. " + threadPoolTaskData);
		
		try {
			Thread.sleep(consumeTaskSleepTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
		threadPoolTaskData = null;
	}
	
	public Object getTask() {
		return this.threadPoolTaskData;
	}
	
}
