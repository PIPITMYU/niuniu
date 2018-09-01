package com.up72.server.mina.function;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Card;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.MyLog;
import com.up72.server.mina.utils.ProjectInfoPropertyUtil;
import com.up72.server.mina.utils.StringUtils;
import com.up72.server.mina.utils.redis.RedisUtil;

public class TCPFunctionExecutor {

	private static final MyLog log = MyLog.getLogger(TCPFunctionExecutor.class);

	public static void execute(IoSession session, ProtocolData readDatas)
			throws IOException, NoSuchMethodException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			ClassNotFoundException, InstantiationException, Exception {

        int interfaceId = readDatas.getInterfaceId();
        JSONObject obj = JSONObject.parseObject(readDatas.getJsonString());
        
        //路由转换
        Map<String,Object> readData = new ConcurrentHashMap<String, Object>();
        Iterator<String> iterator = obj.keySet().iterator();
        while(iterator.hasNext()) {  
            String str = iterator.next();  
            readData.put(Cnst.ROUTE_MAP.get(str), obj.get(str));
        }
        //转换完成
        System.out.println("==========================>接收到的数据"+readData);
		switch (interfaceId) {
		// 大厅消息段
		case 100002:
			HallFunctions.interface_100002(session, readData);
			break;
		case 100003:
			HallFunctions.interface_100003(session, readData);
			break;
		case 100004:
			HallFunctions.interface_100004(session, readData);
			break;
		case 100005:
			HallFunctions.interface_100005(session, readData);
			break;
		case 100006:
			HallFunctions.interface_100006(session, readData);
			break;
		case 100007:
			HallFunctions.interface_100007(session, readData);
			break;// 经典玩法创建房间
		case 100008:
			HallFunctions.interface_100008(session, readData);
			break;
		case 100009:
			HallFunctions.interface_100009(session, readData);
			break;
		case 100010:
			HallFunctions.interface_100010(session, readData);
			break;
		case 100011:
			HallFunctions.interface_100011(session, readData);
			break;
		case 100012:
			HallFunctions.interface_100012(session, readData);
			break;
		case 100013:
			HallFunctions.interface_100013(session, readData);
			break;
		case 100014:
			HallFunctions.interface_100014(session, readData);
			break;
		case 100015:
			HallFunctions.interface_100015(session, readData);
			break;

		// 推送消息段
		case 100100:
			MessageFunctions.interface_100100(session, readData);
			break;// 大接口
		case 100102:
			MessageFunctions.interface_100102(session, readData);
			break;// 小结算
		case 100103:
			MessageFunctions.interface_100103(session, readData);
			break;// 大结算

		// 游戏中消息段
		case 100200:
			GameFunctions.interface_100200(session, readData);
			break;
		case 100202:
			GameFunctions.interface_100202(session, readData);
			break;
		case 100203:
			GameFunctions.interface_100203(session, readData);
			break;
		case 100204:
			GameFunctions.interface_100204(session, readData);
			break;
		case 100205:
			GameFunctions.interface_100205(session, readData);
			break;
		case 100206:
			GameFunctions.interface_100206(session, readData);
			break;
		case 100207:
			GameFunctions.interface_100207(session, readData);
			break;
		case 100208:
			GameFunctions.interface_100208(session, readData);
			break;
		case 100209:
			GameFunctions.interface_100209(session, readData);
			break;
		case 100210:
			GameFunctions.interface_100210(session, readData);
			break;

			// 强制解散房间
		case 999800:
			disRoomForce(session, readData);			
			break;
		case 999801:
			changePai(session,readData);
			break;
		
		default:
			Map<String,String> user = RedisUtil.hgetAll
			(Cnst.REDIS_PREFIX_USER_ID_USER_MAP.concat(session.getAttribute(Cnst.USER_SESSION_USER_ID)+""));
			if (user == null) {

			} else {
				log.I("未知interfaceId" + interfaceId);
				MessageFunctions.illegalRequest(interfaceId, session);// 非法请求
			}
			break;
		}
		
		
		
		
		if (interfaceId!=100100) {
			if(readData.get("userId")!=null){
				String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
				Long userId = Long.valueOf(String.valueOf(readData.get("userId")));
				Player cp = RedisUtil.getPlayerByUserId(String.valueOf(userId),cid);
				if (cp!=null) {
					if (cp.getRoomId()==null&&((Integer)Cnst.PLAYER_STATE_GAME).equals(cp.getPlayStatus())) {
						System.err.println("玩家状态不正确******************************************************************");
						System.out.println(readData);
						System.out.println();
					}
				}
			}			
		}
		
		
		
		
		
		

	}

	/**
	 * 修改用户手牌 仅限测试使用
	 * @param session
	 * @param readData
	 */
	private static void changePai(IoSession session, Map<String, Object> readData) {
		// TODO Auto-generated method stub
		if(ProjectInfoPropertyUtil.getProperty("develop", "0").equals("0")){
			return;
		}
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Player p = RedisUtil.getPlayerByUserId(String.valueOf(userId),cid);
		Integer oldPai = StringUtils.parseInt(readData.get("oldPai"));
		Integer newPai = StringUtils.parseInt(readData.get("newPai"));
		if(p == null || p.getPlayStatus() != Cnst.PLAYER_STATE_GAME || p.getRoomId() == null || !p.getRoomId().equals(roomId)){
			return;
		}
		if(oldPai == null || newPai == null){
			return;
		}
		List<Card> pais = p.getPais();
		Card a = new Card(newPai);
		for(Card c:pais){
			if(c.getOrigin() == oldPai){
				pais.remove(c);
				break;
			}
		}
		pais.add(a);
		p.setPais(pais);
		RedisUtil.updateRedisData(null, p,cid);
		JSONObject info = new JSONObject();
		info.put("roomId", roomId);
		info.put("userId", userId);
		List<Integer> pai = new ArrayList<Integer>();
		for(Card c:pais){
			pai.add(c.getOrigin());
		}
		info.put("pais", pai);
		JSONObject result = TCPGameFunctions.getJSONObj(999801, 1, info);
		ProtocolData pd = new ProtocolData(999801, result.toJSONString());
		session.write(pd);
	}


	/**
	 * 心跳操作
	 * 所有用户的id对应心跳，存在redis的map集合里，集合的key为Cnst.REDIS_HEART_PREFIX，map的key为userId，value为最后long类型的心跳时间
	 * 当用户掉线之后，会把map中的这个userId删掉，（考虑：是否修改用户的state字段）
	 * @param session
	 * @param readData
	 */
	public synchronized static void heart(IoSession session, ProtocolData readData)
			throws Exception {
		String userIdStr = String.valueOf(session.getAttribute(Cnst.USER_SESSION_USER_ID));
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		if (userIdStr==null) {
			session.close(true);
		}else{
			try {
				Player p = RedisUtil.getPlayerByUserId(userIdStr,cid);
				if (p!=null) {
					long ct = System.currentTimeMillis();
					String lastHeartTime = RedisUtil.hget(Cnst.get_REDIS_HEART_PREFIX(cid), userIdStr);
					if (lastHeartTime==null) {//说明用户重新上线
						//需要看用户是否在房间里，如果在，要通知其他玩家
						String roomId = String.valueOf(p.getRoomId()); 
						if (roomId!=null) {//用户在房间里
							RoomResp room = RedisUtil.getRoomRespByRoomId(roomId,cid);
							if (room!=null&&room.getState()!=(Cnst.ROOM_STATE_YJS)) {//房间还没解散
								//toAdd  通知房间内其他人用户上线
								List<Player> players =  RedisUtil.getPlayerList(room,cid); 
								List<Long> userIds = new ArrayList<Long>();
								userIds.add(p.getUserId());
								List<Integer> status = new ArrayList<Integer>();
								status.add(Cnst.PLAYER_LINE_STATE_INLINE);
								MessageFunctions.interface_100109(players, status,userIds,1,cid);
							}
						}
					}else{//说明用户正常心跳
						//如果玩家在房间里，需要计算其他用户是否心跳超时
						String roomId = String.valueOf(p.getRoomId());
						if (roomId!=null) {//用户在房间里
							RoomResp room = RedisUtil.getRoomRespByRoomId(roomId,cid);
							if (room!=null&&room.getState()!=(Cnst.ROOM_STATE_YJS)) {//房间还没解散
								//toAdd 计算房间里其他玩家的心跳时间
								
								Long[] uids = room.getPlayerIds();
								List<Long> outs = new ArrayList<Long>();
								List<Integer> status = new ArrayList<Integer>();
								for(Long uid:uids){
									if (uid!=null&&!(String.valueOf(uid)).equals(userIdStr)) {
										String uidHeartTime = RedisUtil.hget(Cnst.get_REDIS_HEART_PREFIX(cid),String.valueOf(uid));
										if (uidHeartTime!=null) {
											long t = Long.valueOf(uidHeartTime);
											if ((ct-t)>Cnst.HEART_TIME) {
												RedisUtil.hdel(Cnst.get_REDIS_HEART_PREFIX(cid), String.valueOf(uid));
												outs.add(uid);
												status.add(Cnst.PLAYER_LINE_STATE_OUT);
											}
										}
									}
								}
								
								if (outs.size()>0) {
									//toAdd  通知其他人，outs里面的玩家掉线
									List<Player> players =  RedisUtil.getPlayerList(room,cid); 
									MessageFunctions.interface_100109(players, status,outs,2,cid);
								}
							}
						}
					}
					//更新用户心跳时间
					RedisUtil.hset(Cnst.get_REDIS_HEART_PREFIX(cid), userIdStr,String.valueOf(ct), null);
				}else{
					session.close(true);
				}
			} catch (Exception e) {
				e.printStackTrace();
				session.close(true);
			}
		}
	}

	/**
	 * 强制解散房间
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public static void disRoomForce(IoSession session, Map<String,Object> readData)
			throws Exception {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		System.out.println("*******强制解散房间" + roomId);
		if (roomId != null) {
			RoomResp room = RedisUtil.getRoomRespByRoomId(String
					.valueOf(roomId),cid);
			if (room != null) {
				if(room.getState() == Cnst.ROOM_STATE_GAMIING){
					//中途准备阶段解散房间不计入回放中
					List<Integer> xiaoJSInfo = new ArrayList<Integer>();
					for(int i=0;i<room.getPlayerIds().length;i++){
						xiaoJSInfo.add(0);
					}
					room.addXiaoJSInfo(xiaoJSInfo);
				}
				MessageFunctions.updateDatabasePlayRecord(room,cid);				
				room.setState(Cnst.ROOM_STATE_YJS);
				List<Player> players = RedisUtil.getPlayerList(room,cid);

				RedisUtil.deleteByKey(Cnst.get_REDIS_PREFIX_ROOMMAP(cid)
						.concat(String.valueOf(roomId)));// 删除房间
				if (players != null && players.size() > 0) {
					for (Player p : players) {
						p.initPlayer(null, Cnst.PLAYER_STATE_DATING,  0);
						RedisUtil.updateRedisData(null, p,cid);
					}
					for (Player p : players) {
						IoSession se = MinaServerManager.tcpServer
								.getSessions().get(p.getSessionId());
						if (se != null && se.isConnected()) {
							Map<String,Object> readDatas = new HashMap<String, Object>();
							readDatas.put("interfaceId", 100100);
							readDatas.put("openId", p.getOpenId());
							readDatas.put("cId", Cnst.cid);
							MessageFunctions.interface_100100(se,readDatas);
						}
					}
				}
				if(room.getRoomType() == Cnst.ROOM_TYPE_2){
					String daiKaiKey = Cnst.get_ROOM_DAIKAI_KEY(cid).concat(String.valueOf(room.getCreateId()));
					if(RedisUtil.exists(daiKaiKey)){
						RedisUtil.hdel(daiKaiKey, String.valueOf(roomId));
					}
				}
			} else {
				System.out.println("*******强制解散房间" + roomId + "，房间不存在");
			}
		}

		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_1);
		JSONObject result = MessageFunctions.getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);

	}

}
