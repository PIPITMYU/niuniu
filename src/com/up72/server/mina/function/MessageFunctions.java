package com.up72.server.mina.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.StringUtils;
import com.up72.server.mina.utils.redis.RedisUtil;

/**
 * Created by Administrator on 2017/7/10. 推送消息类
 */
public class MessageFunctions extends TCPGameFunctions {

	/**
	 * 发送玩家信息
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100100(IoSession session, Map<String,Object> readData)
			throws Exception {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Map<String, Object> info = new HashMap<>();
		if (interfaceId.equals(100100)) {// 刚进入游戏主动请求
			String openId = String.valueOf(readData.get("openId"));
			Player currentPlayer = null;
			String cid = null;
			if (openId == null) {			
				illegalRequest(interfaceId, session);
				return;	
			} else {
				String ip = (String) session.getAttribute(Cnst.USER_SESSION_IP);
				cid = String.valueOf(readData.get("cId"));
				currentPlayer = HallFunctions.getPlayerInfos(openId, ip, cid,
						session);
			}
			if (currentPlayer == null) {
				illegalRequest(interfaceId, session);
				return;
			}

			// 更新心跳为最新上线时间
//			RedisUtil.hset(Cnst.REDIS_HEART_PREFIX,String.valueOf(currentPlayer.getUserId()) , String.valueOf(new Date().getTime()), null);
			
			if (cid != null) {
				currentPlayer.setCId(cid);
			}
			currentPlayer.setSessionId(session.getId());// 更新sesisonId
			session.setAttribute(Cnst.USER_SESSION_USER_ID,
					currentPlayer.getUserId());
			session.setAttribute(Cnst.USER_SESSION_CID, cid);
			if (openId != null) {
				RedisUtil.setObject(Cnst.get_REDIS_PREFIX_OPENIDUSERMAP(cid).concat(openId),currentPlayer.getUserId() ,null);
			}

			RoomResp room = null;
			List<Player> players = null;
			
			if (currentPlayer.getRoomId() != null) {// 玩家下有roomId，证明在房间中
				room = RedisUtil.getRoomRespByRoomId(String.valueOf(currentPlayer
						.getRoomId()),cid);
				if (room != null
						&& room.getState() != Cnst.ROOM_STATE_YJS) {
					info.put("roomInfo", getRoomInfo(room));
					players = RedisUtil.getPlayerList(room,cid);
					if(players == null){
						currentPlayer.initPlayer(null,Cnst.PLAYER_STATE_DATING,0);
					}else{
						for (int m = 0; m < players.size(); m++) {
							Player p = players.get(m);
							if (p.getUserId().equals(currentPlayer.getUserId())) {
								players.remove(m);
								break;
							}
						}

						
						info.put("anotherUsers", getAnotherUserInfo(players, room));
					}					

				} else {
					currentPlayer.initPlayer(null,Cnst.PLAYER_STATE_DATING,0);
				}

			} else {
				currentPlayer.initPlayer(null,Cnst.PLAYER_STATE_DATING,0);
			}

			RedisUtil.updateRedisData(room, currentPlayer,cid);
			info.put("currentUser", getCurrentUserInfo(currentPlayer,room));

			if (room != null) {
				// room.setWsw_sole_main_id(room.getWsw_sole_main_id()+1);

				info.put("wsw_sole_main_id", room.getWsw_sole_main_id());
				info.put("wsw_sole_action_id", room.getWsw_sole_action_id());
				Map<String, Object> roomInfo = (Map<String, Object>) info
						.get("roomInfo");
				List<Map<String, Object>> anotherUsers = (List<Map<String, Object>>) info
						.get("anotherUsers");

				info.remove("roomInfo");
				info.remove("anotherUsers");

				JSONObject result = getJSONObj(interfaceId, 1, info);
				ProtocolData pd = new ProtocolData(interfaceId,
						result.toJSONString());
				session.write(pd);

				info.remove("currentUser");
				info.put("roomInfo", roomInfo);
				result = getJSONObj(interfaceId, 1, info);
				pd = new ProtocolData(interfaceId, result.toJSONString());
				session.write(pd);

				info.remove("roomInfo");
				info.put("anotherUsers", anotherUsers);
				result = getJSONObj(interfaceId, 1, info);
				pd = new ProtocolData(interfaceId, result.toJSONString());
				session.write(pd);
			} else {
				JSONObject result = getJSONObj(interfaceId, 1, info);
				ProtocolData pd = new ProtocolData(interfaceId,
						result.toJSONString());
				session.write(pd);
			}

		}  else {
			session.close(true);
		}

	}
	
	//封装currentUser
	public static Map<String,Object> getCurrentUserInfo(Player player,RoomResp room){
		Map<String,Object> currentUserInfo = new HashMap<String, Object>();
		currentUserInfo.put("version", String.valueOf(Cnst.version));
		currentUserInfo.put("userId",player.getUserId());
		currentUserInfo.put("position", player.getPosition());
		currentUserInfo.put("playStatus", player.getPlayStatus());
		currentUserInfo.put("userName", player.getUserName());
		currentUserInfo.put("userImg", player.getUserImg());
		currentUserInfo.put("gender", player.getGender());
		currentUserInfo.put("ip", player.getIp());
		currentUserInfo.put("joinIndex", player.getJoinIndex());
		currentUserInfo.put("userAgree", player.getUserAgree());
		currentUserInfo.put("money", player.getMoney());
		currentUserInfo.put("score", player.getScore());
		currentUserInfo.put("notice", player.getNotice());
		currentUserInfo.put("yaZhu", player.getYaZhu() == null? -2:player.getYaZhu());
		currentUserInfo.put("qiangZhuang", player.getQiangZhuang() == null? -2:player.getQiangZhuang());
		if(room != null && room.getPlayStatus()!=null){
			List<Integer> pais = new ArrayList<Integer>();
			if(room.getPlayStatus() == Cnst.ROOM_PALYSTATE_LIANGPAI){
				for(int i=0;i<5;i++){
					pais.add(player.getPais().get(i).getOrigin());
				}
			}else{
				if(room.getType() == Cnst.ROOM_PALYTYPE_MINGPAI){
					for(int i=0;i<4;i++){
						pais.add(player.getPais().get(i).getOrigin());
					}
				}
			}
			currentUserInfo.put("pais", pais);
		}					
		return currentUserInfo;
	}
	//封装anotherUsers
	public static List<Map<String,Object>> getAnotherUserInfo(List<Player> players ,RoomResp room){
		List<Map<String,Object>> anotherUserInfos = new ArrayList<Map<String,Object>>();
		for(Player player:players){
			Map<String,Object> currentUserInfo = new HashMap<String, Object>();
			currentUserInfo.put("userId", player.getUserId());
			currentUserInfo.put("position", player.getPosition());
			currentUserInfo.put("playStatus", player.getPlayStatus());
			currentUserInfo.put("userName", player.getUserName());
			currentUserInfo.put("userImg", player.getUserImg());
			currentUserInfo.put("gender", player.getGender());
			currentUserInfo.put("ip", player.getIp());
			currentUserInfo.put("joinIndex", player.getJoinIndex());
			currentUserInfo.put("userAgree", player.getUserAgree());
			currentUserInfo.put("money", player.getMoney());
			currentUserInfo.put("score", player.getScore());
			currentUserInfo.put("notice", player.getNotice());
			currentUserInfo.put("yaZhu", player.getYaZhu() == null? -2:player.getYaZhu());
			currentUserInfo.put("qiangZhuang", player.getQiangZhuang() == null? -2:player.getQiangZhuang());
			if(room != null && room.getPlayStatus()!=null){
				if(room.getPlayStatus() == Cnst.ROOM_PALYSTATE_LIANGPAI && player.getPlayStatus() == Cnst.PLAYER_STATE_OVER){
					List<Integer> pais = new ArrayList<Integer>();
					for(int i=0;i<5;i++){
						pais.add(player.getPais().get(i).getOrigin());
					}
					currentUserInfo.put("pais", pais);		
				}
			}
			anotherUserInfos.add(currentUserInfo);
		}
		return anotherUserInfos;
	}
	//封装房间信息
	public static Map<String,Object> getRoomInfo(RoomResp room){
		Map<String,Object> roomInfo = new HashMap<String, Object>();
		roomInfo.put("userId",room.getCreateId());
		roomInfo.put("userName", room.getOpenName());
		roomInfo.put("createTime", room.getCreateTime());
		roomInfo.put("roomId", room.getRoomId());
		roomInfo.put("state", room.getState());
		roomInfo.put("playStatus", room.getPlayStatus());
		roomInfo.put("lastNum", room.getLastNum());
		roomInfo.put("totalNum", room.getCircleNum());//总局数
 		roomInfo.put("roomType", room.getRoomType());
		roomInfo.put("xjst", room.getXjst());
		roomInfo.put("type", room.getType());
		roomInfo.put("diFen", room.getDiFen());
		roomInfo.put("maxPeople",room.getMaxPeople());
		roomInfo.put("zhuangNum", room.getZhuangNum());
		roomInfo.put("fanRule", room.getFanRule());
		roomInfo.put("shunZi", room.getShunZi());
		roomInfo.put("huLu", room.getHuLu());
		roomInfo.put("wuHua", room.getWuHua());
		roomInfo.put("tongHua", room.getTongHua());
		roomInfo.put("zhuangPlayer", room.getZhuangPlayer());
		roomInfo.put("maxQZhuang", room.getMaxQZhuang());
		if(room.getDissolveRoom()!=null){
			Map<String,Object> dissolveRoom = new HashMap<String, Object>();
			dissolveRoom.put("dissolveTime", room.getDissolveRoom().getDissolveTime());
			dissolveRoom.put("userId", room.getDissolveRoom().getUserId());
			dissolveRoom.put("othersAgree", room.getDissolveRoom().getOthersAgree());
			roomInfo.put("dissolveRoom", dissolveRoom);
		}else{
			roomInfo.put("dissolveRoom", null);
		}
		return roomInfo;
	}
	
	/** 
	 * 小结算
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100102(IoSession session, Map<String,Object> readData) {
		 Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
	     Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
	     String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
	     RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId),cid);
	     List<Player> players = RedisUtil.getPlayerList(room,cid);
		List<Map<String,Object>> userInfos = new ArrayList<Map<String,Object>>();
		Long zhuangPlayer = room.getZhuangPlayer();
		for(Player p:players){
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("userId", p.getUserId());
			map.put("score", p.getThisScore());
			if(zhuangPlayer!=null && zhuangPlayer.equals(p.getUserId())){
				map.put("zhuang", 1);//1是庄 0是闲
			}else{
				map.put("zhuang", 0);//1是庄 0是闲
			}
			userInfos.add(map);
		}
		JSONObject info = new JSONObject();
		info.put("lastNum", room.getLastNum());
		info.put("totalFan", room.getDiFen());
		info.put("userInfo", userInfos);
		JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
	}
	

	/**
	 * 大结算
	 * @param session
	 * @param readData
	 */
	public synchronized static void interface_100103(IoSession session, Map<String,Object> readData) {
		 Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
	     Long userId = StringUtils.parseLong(readData.get("userId"));
	     Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
	     String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
	     RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId),cid);
	     if(room == null){
	    	 return;
	     }
	     String key = roomId+"-"+room.getCreateTime();
		List<Map> userInfos = RedisUtil.getPlayRecord(Cnst.get_REDIS_PLAY_RECORD_PREFIX_OVERINFO(cid).concat(key));
		JSONObject info = new JSONObject();
		info.put("xiaoJuNum", room.getXiaoJuNum());
		if(!RedisUtil.exists(Cnst.REDIS_PLAY_RECORD_PREFIX_OVERINFO.concat(key))){
			List<Map<String,Object>> zeroUserInfos = new ArrayList<Map<String,Object>>();
			List<Player> players = RedisUtil.getPlayerList(room,cid);
			for(Player p:players){
  				Map<String,Object> map = new HashMap<String, Object>();
  				map.put("userId", p.getUserId());
  				map.put("finalScore", 0);
  				map.put("position", p.getPosition());
  				zeroUserInfos.add(map);
  			}
			info.put("userInfo",zeroUserInfos);
		}else{
			info.put("userInfo", userInfos);
		}
		
		JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        session.write(pd);
        
        //更新 player
        Player p = RedisUtil.getPlayerByUserId(String.valueOf(userId),cid);
        p.initPlayer(null,Cnst.PLAYER_STATE_DATING,0);

        Integer outNum = room.getOutNum()==null?1:room.getOutNum()+1;
        if(outNum == room.getPlayerIds().length){
        	RedisUtil.deleteByKey(Cnst.get_REDIS_PREFIX_ROOMMAP(cid).concat(String.valueOf(roomId)));
        }else{
        	 //更新outNum
        	room.setOutNum(outNum);
            RedisUtil.updateRedisData(room, p,cid);
        }      
	}

	/**
	 * 多地登陆提示
	 * 
	 * @param session
	 */
	public static void interface_100106(IoSession session) {
		Integer interfaceId = 100106;
		JSONObject result = getJSONObj(interfaceId, 1, "out");
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
		session.close(true);
	}

	/**
	 * 玩家被踢/房间被解散提示
	 * 
	 * @param session
	 */
	public static void interface_100107(IoSession session, Integer type,
			List<Player> players) {
		Integer interfaceId = 100107;
		Map<String, Object> info = new HashMap<String, Object>();
		
		if (players == null || players.size() == 0) {
			return;
		}
		info.put("userId", session.getAttribute(Cnst.USER_SESSION_USER_ID));
		info.put("type", type);

		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		for (Player p : players) {
			IoSession se = MinaServerManager.tcpServer.getSessions().get(
					p.getSessionId());
			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}
	}
	

	/**
	 * 方法id不符合
	 * 
	 * @param session
	 */
	public static void interface_100108(IoSession session) {
		Integer interfaceId = 100108;
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("reqState", Cnst.REQ_STATE_9);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}
	/**
	 * 用户离线/上线提示
	 * 
	 * @param state
	 */
	public static void interface_100109(List<Player> players, List<Integer> states,List<Long> userIds,int type,String cid) {
		Integer interfaceId = 100109;
		
		if (type==1) {
			Map<String, Object> info = new HashMap<String, Object>();
			info.put("userId", userIds);
			info.put("state", states);
			info.put("type", 2);//给其他人发2
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
			List<Long> others = new ArrayList<Long>();
			if (players != null && players.size() > 0) {
				boolean flag = false;
				for (Player p : players) {
					if (p != null && !userIds.contains(p.getUserId())) {
						flag = true;
						others.add(p.getUserId());
						IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
						if (se != null && se.isConnected()) {
							se.write(pd);
						}
					}
				}
				if (flag) {
					info.put("type", 1);//给自己发1
					info.put("userId", others);
					List<Integer> ostates = new ArrayList<Integer>();
					if (others.size()>0) {
						for(Long uid:others){
							String lastHeartTime = RedisUtil.hget(Cnst.get_REDIS_HEART_PREFIX(cid), String.valueOf(uid));
							if (lastHeartTime==null) {
								ostates.add(Cnst.PLAYER_LINE_STATE_OUT);
							}else{
								ostates.add(Cnst.PLAYER_LINE_STATE_INLINE);
							}
						}
					}
					info.put("state", ostates);
					result = getJSONObj(interfaceId, 1, info);
					pd = new ProtocolData(interfaceId, result.toJSONString());
					IoSession se = MinaServerManager.tcpServer.getSessions().get(userIds.get(0));
					if (se != null && se.isConnected()) {
						se.write(pd);
					}
				}
			}
		}else if(type==2){
			Map<String, Object> info = new HashMap<String, Object>();
			info.put("userId", userIds);
			info.put("state", states);
			info.put("type", 2);//给其他人发2
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
			List<Long> others = new ArrayList<Long>();
			if (players != null && players.size() > 0) {
				for (Player p : players) {
					if (p != null && !userIds.contains(p.getUserId())) {
						others.add(p.getUserId());
						IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
						if (se != null && se.isConnected()) {
							se.write(pd);
						}
					}
				}
			}
		}
	}
	
	

    
    /**
     * 后端主动解散房间推送
     * @param reqState
     * @param players
     */
	public static void interface_100111(int reqState,List<Player> players,Integer roomId){
    	Integer interfaceId = 100111;
        Map<String,Object> info = new HashMap<String, Object>();
        info.put("reqState",reqState);
        JSONObject result = getJSONObj(interfaceId,1,info);
        ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
        if (players!=null&&players.size()>0) {
			for(Player p:players){
				if (p.getRoomId()!=null&&p.getRoomId().equals(roomId)) {
					IoSession se = MinaServerManager.tcpServer.getSessions().get(p.getSessionId());
					if (se!=null&&se.isConnected()) {
						se.write(pd);
					}
				}
			}
		}
    	
    }
	
	 /**
     * 后端主动加入代开房间推送
     * @param reqState
     * @param players
     */
	public static void interface_100112(Player player,RoomResp room,Integer type,String cid){
    	Integer interfaceId = 100112;
    	//先判断房主是否在线
    	Player roomCreater = RedisUtil.getPlayerByUserId(String.valueOf(room.getCreateId()),cid);
    	IoSession se = MinaServerManager.tcpServer.getSessions().get(roomCreater.getSessionId());
    	if(player == null && type == 3){
    		if (se!=null&&se.isConnected()) {
      			 Map<String,Object> info = new HashMap<String, Object>();
      		     info.put("roomSn",room.getRoomId());
      		     info.put("extraType", type);
      		     JSONObject result = getJSONObj(interfaceId,1,info);
      		     ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());				
      			se.write(pd);
       		}else{
       			return;
       		}
    	}else{
    		if (se!=null&&se.isConnected()) {
   			 Map<String,Object> info = new HashMap<String, Object>();
   		     info.put("roomSn",room.getRoomId());
   		     info.put("userId",player.getUserId());
   		     info.put("userName",player.getUserName());
   		     info.put("userImg", player.getUserImg());
   		     info.put("position", player.getPosition());
   		     info.put("extraType", type);
   		     JSONObject result = getJSONObj(interfaceId,1,info);
   		     ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());				
   			se.write(pd);
    		}else{
    			return;
    		}
    	}   	
    }

}
