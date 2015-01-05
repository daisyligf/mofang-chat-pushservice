package com.mofang.chat.pushservice.job;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.mofang.chat.business.redis.GroupRedis;
import com.mofang.chat.business.redis.GuildRedis;
import com.mofang.chat.business.redis.RoomRedis;
import com.mofang.chat.business.redis.UserRedis;
import com.mofang.chat.business.redis.impl.GroupRedisImpl;
import com.mofang.chat.business.redis.impl.GuildRedisImpl;
import com.mofang.chat.business.redis.impl.RoomRedisImpl;
import com.mofang.chat.business.redis.impl.UserRedisImpl;
import com.mofang.chat.business.sysconf.common.PushDataType;
import com.mofang.chat.pushservice.global.GlobalObject;
import com.mofang.chat.pushservice.job.listener.impl.FrontendPusher;
import com.mofang.framework.util.StringUtil;

/**
 * 
 * @author zhaodx
 *
 */
public class JobHandler implements Runnable
{
	private String message;
	private JSONObject pushJson = null;
	private Map<String, FrontendEntity> feMap;
	private RoomRedis roomRedis = RoomRedisImpl.getInstance();
	private UserRedis userRedis = UserRedisImpl.getInstance();
	private GroupRedis groupRedis = GroupRedisImpl.getInstance();
	private GuildRedis guildRedis = GuildRedisImpl.getInstance();
	
	public JobHandler(String message, Map<String, FrontendEntity> feMap)
	{
		this.feMap = feMap;
		this.message = message;
	}
	
	@Override
	public void run() 
	{
		try
		{
			pushJson = new JSONObject(message);
			pushJson.put("act", "push");
			
			int dataType = pushJson.optInt("push_data_type");
			if(dataType == PushDataType.ROOM_NOTIFY)
			{
				int roomId = pushJson.optInt("rid", 0);
				long fromUserId = pushJson.optLong("from_uid", 0L);
				if(0 == roomId)
					return;
				pushRoomNotify(roomId, fromUserId);
			}
			else if(dataType == PushDataType.PRIVATE_NOTIFY)
			{
				long toUserId = pushJson.optLong("to_uid", 0L);
				if(0 == toUserId)
					return;
				pushPrivateNotify(toUserId);
			}
			else if(dataType == PushDataType.FRIEND_NOTIFY)
			{
				long toUserId = pushJson.optLong("to_uid", 0L);
				if(0 == toUserId)
					return;
				pushFriendNotify(toUserId);
			}
			else if(dataType == PushDataType.ROOM_ACTIVITY_NOTIFY)
			{
				int roomId = pushJson.optInt("rid", 0);
				if(0 == roomId)
					return;
				pushRoomActivityNotify(roomId);
			}
			else if(dataType == PushDataType.GROUP_NOTIFY)
			{
				long groupId = pushJson.optLong("group_id", 0L);
				long fromUserId = pushJson.optLong("from_uid", 0L);
				if(0 == groupId)
					return;
				pushGroupNotify(groupId, fromUserId);
			}
			else if(dataType == PushDataType.POST_REPLY_NOTIFY)
			{
				long toUserId = pushJson.optLong("to_uid", 0L);
				if(0 == toUserId)
					return;
				pushPostReplyNotify(toUserId);
			}
			else if(dataType == PushDataType.SYS_MESSAGE_NOTIFY)
			{
				long toUserId = pushJson.optLong("to_uid", 0L);
				if(0 == toUserId)
					return;
				pushSysMessageNotify(toUserId);
			}
			else if(dataType == PushDataType.USER_TASK_NOTIFY)
			{
				long toUserId = pushJson.optLong("to_uid", 0L);
				if(0 == toUserId)
					return;
				pushTaskNotify(toUserId);
			}
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at JobHandler.run throw an error.", e);
		}
	}
	
	private void pushRoomNotify(int roomId, long exceptUserId)
	{
		try
		{
			GlobalObject.INFO_LOG.info("prepare get enter room user set. rid:" + roomId + ", from_uid:" + exceptUserId);
			///根据roomid获取进入房间的uid集合
			Set<String> uidSet = roomRedis.getEnterUsers(roomId);
			if(null == uidSet || uidSet.size() == 0)
				return;
			
			GlobalObject.INFO_LOG.info("get enter room user set completed. rid:" + roomId + ", from_uid:" + exceptUserId);
			Iterator<String> iterator = uidSet.iterator();
			long userId;
			while(iterator.hasNext())
			{
				userId = Long.parseLong(iterator.next());
				if(userId == exceptUserId)
					continue;
				
				///推送消息
				pushJson.put("uid", userId);
				notifyPush(userId, pushJson.toString());
				GlobalObject.INFO_LOG.info("prepare send push notify. rid:" + roomId + ", to_uid: " + userId + " from_uid:" + exceptUserId);
			}
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at JobHandler.pushRoomNotify throw an error.", e);
		}
	}
	
	private void pushPrivateNotify(long userId)
	{
		try
		{
			///私聊不需要判断unread=0的情况
			///推送消息
			notifyPush(userId, pushJson.toString());
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at JobHandler.pushPrivateNotify throw an error.", e);
		}
	}
	
	private void pushFriendNotify(long userId)
	{
		try
		{
			///推送消息
			notifyPush(userId, pushJson.toString());
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at JobHandler.pushFriendNotify throw an error.", e);
		}
	}
	
	private void pushRoomActivityNotify(int roomId)
	{
		try
		{
			///根据roomid获取进入房间的uid集合
			Set<String> uidSet = roomRedis.getEnterUsers(roomId);
			if(null == uidSet || uidSet.size() == 0)
				return;
			
			Iterator<String> iterator = uidSet.iterator();
			long userId;
			while(iterator.hasNext())
			{
				userId = Long.parseLong(iterator.next());
				
				///推送消息
				pushJson.put("uid", userId);
				notifyPush(userId, pushJson.toString());
			}
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at JobHandler.pushRoomActivityNotify throw an error.", e);
		}
	}
	
	private void pushGroupNotify(long groupId, long exceptUserId)
	{
		try
		{
			///根据groupid获取uid集合
			Map<String, String> uidMap = groupRedis.getUserList(groupId);
			if(null == uidMap || uidMap.size() == 0)
				return;
			
			String groupName = "";
			String guildAvatar = "";
			JSONObject groupJson = groupRedis.getInfo(groupId);
			if(null != groupJson)
			{
				groupName = groupJson.optString("name", "");
				long guildId = groupJson.optLong("guildId", 0L);
				if(guildId > 0)
				{
					JSONObject guildJson = guildRedis.getInfo(guildId);
					if(null != guildJson)
						guildAvatar = guildJson.optString("avatar", "");
				}
			}
			Iterator<String> iterator = uidMap.keySet().iterator();
			long userId;
			while(iterator.hasNext())
			{
				userId = Long.parseLong(iterator.next());
				if(userId == exceptUserId)
					continue;
				
				String receiveNotify = uidMap.get(String.valueOf(userId));
				if(!StringUtil.isNullOrEmpty(receiveNotify) && receiveNotify.equals("0"))
					continue;
				
				///推送消息
				pushJson.put("uid", userId);
				pushJson.put("name", groupName);
				pushJson.put("avatar", guildAvatar);
				notifyPush(userId, pushJson.toString());
			}
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at JobHandler.pushGroupNotify throw an error.", e);
		}
	}
	
	private void pushPostReplyNotify(long userId)
	{
		try
		{
			///推送消息
			notifyPush(userId, pushJson.toString());
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at JobHandler.pushPostReplyNotify throw an error.", e);
		}
	}
	
	private void pushSysMessageNotify(long userId)
	{
		try
		{
			///推送消息
			notifyPush(userId, pushJson.toString());
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at JobHandler.pushSysMessageNotify throw an error.", e);
		}
	}
	
	private void pushTaskNotify(long userId)
	{
		try
		{
			///推送消息
			notifyPush(userId, pushJson.toString());
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at JobHandler.pushTaskNotify throw an error.", e);
		}
	}
	
	private void notifyPush(long userId, String message)
	{
		try
		{
			///根据uid获取FE信息
			String feHost = userRedis.getFrontend(userId);
			if(!feMap.containsKey(feHost))
			{
				GlobalObject.INFO_LOG.info("femap dosn't contains fe host:" + feHost);
				return;
			}
			///为指定FE的指定uid发送消息通知
			FrontendEntity entity = feMap.get(feHost);
			FrontendPusher pushListener = new FrontendPusher(entity);
			GlobalObject.INFO_LOG.info("job handler prepare to push message:" + message);
			pushListener.push(message);
		}
		catch(Exception e)
		{
			GlobalObject.ERROR_LOG.error("at JobHandler.notifyPush throw an error.", e);
		}
	}
}