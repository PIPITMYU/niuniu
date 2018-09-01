package com.up72.server.mina.function;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Feedback;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.game.model.SystemMessage;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.BackFileUtil;
import com.up72.server.mina.utils.CommonUtil;
import com.up72.server.mina.utils.StringUtils;
import com.up72.server.mina.utils.redis.RedisUtil;

/**
 * Created by Administrator on 2017/7/8. 大厅方法类
 */
public class HallFunctions extends TCPGameFunctions {

	/**
	 * 大厅查询战绩
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100002(IoSession session,
			Map<String, Object> readData) {
		logger.I("大厅查询战绩,interfaceId -> 100002");
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		Integer interfaceId = Integer.parseInt(String.valueOf(readData
				.get("interfaceId")));
		String userId = String.valueOf(readData.get("userId"));
		Integer page = Integer.parseInt(String.valueOf(readData.get("page")));
		String userKey = Cnst.get_REDIS_PLAY_RECORD_PREFIX_ROE_USER(cid).concat(userId);

		Long pageSize = RedisUtil.llen(userKey);
		int start = (page - 1) * Cnst.PAGE_SIZE;
		int end = start + Cnst.PAGE_SIZE - 1;
		List<String> keys = RedisUtil.lrange(userKey, start, end);
		JSONObject info = new JSONObject();
		List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
		for (String roomKey : keys) {
			Map<String, String> roomInfos = RedisUtil
					.hgetAll(Cnst.get_REDIS_PLAY_RECORD_PREFIX(cid).concat(roomKey));
			maps.add(roomInfos);
		}
		info.put("infos", maps);
		info.put("pages", pageSize == null ? 0
				: pageSize % Cnst.PAGE_SIZE == 0 ? pageSize / Cnst.PAGE_SIZE
						: (pageSize / Cnst.PAGE_SIZE + 1));
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 大厅查询系统消息
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100003(IoSession session,
			Map<String, Object> readData) {
		logger.I("大厅查询系统消息,interfaceId -> 100003");
		Integer interfaceId = Integer.parseInt(String.valueOf(readData
				.get("interfaceId")));
		Integer page = Integer.parseInt(String.valueOf(readData.get("page")));
		List<SystemMessage> info = userService.getSystemMessage(null,
				(page - 1) * Cnst.PAGE_SIZE, Cnst.PAGE_SIZE);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 大厅请求联系我们
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100004(IoSession session,
			Map<String, Object> readData) {
		logger.I("大厅请求联系我们,interfaceId -> 100004");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Map<String, String> info = new HashMap<>();
		info.put("connectionInfo", userService.getConectUs());
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 大厅请求帮助信息
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100005(IoSession session,
			Map<String, Object> readData) {
		logger.I("大厅请求帮助信息,interfaceId -> 100005");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Map<String, String> info = new HashMap<>();
		info.put("help", "帮助帮助帮助帮助帮助帮助帮助帮助帮助");
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 反馈信息
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100006(IoSession session,
			Map<String, Object> readData) {
		logger.I("反馈信息,interfaceId -> 100006");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		String content = String.valueOf(readData.get("content"));
		String tel = String.valueOf(readData.get("tel"));
		// 插入反馈信息
		Feedback back = new Feedback();
		back.setContent(content);
		back.setCreateTime(new Date().getTime());
		back.setTel(tel);
		back.setUserId(userId);
		userService.userFeedback(back);
		// 返回反馈信息
		Map<String, String> info = new HashMap<>();
		info.put("content", "感谢您的反馈！");
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 创建房间-经典玩法
	 * 
	 * @param session
	 * @param readData
	 */
	public synchronized static void interface_100007(IoSession session,
			Map<String, Object> readData) {
		logger.I("创建房间,interfaceId -> 100007");
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));// 接口Id
		Long userId = StringUtils.parseLong(readData.get("userId"));// 用户id
		Integer circleNum = StringUtils.parseInt(readData.get("circleNum"));// 局数，详见说明
																			// 10(默认)/20
		Integer roomType = StringUtils.parseInt(readData.get("roomType"));// 房间类型
		Integer type = StringUtils.parseInt(readData.get("type"));// 玩法选项
		Integer diFen = StringUtils.parseInt(readData.get("diFen"));// 底分
																	// 1(默认),2,4,5
		Integer maxPeople = StringUtils.parseInt(readData.get("maxPeople"));// 开局人数
		Integer zhuangNum = -1;															// 4,5,6,8
		if(type == Cnst.ROOM_PALYTYPE_GUDING){
			zhuangNum = StringUtils.parseInt(readData.get("zhuangNum"));// 上庄分数（封顶分）
			// 默认为0，可选100，150，200
		}
		Integer maxQZhuang = -1;															// 4,5,6,8
		if(type == Cnst.ROOM_PALYTYPE_MINGPAI){
			maxQZhuang = StringUtils.parseInt(readData.get("maxQZhuang"));// 
		}
		Integer fanRule = StringUtils.parseInt(readData.get("fanRule"));// 是否根据牌翻倍
																		// 传递0和1
		// 下面都是特殊牛 1:选 ,0:不选
		Integer shunZi = StringUtils.parseInt(readData.get("shunZi"));
		Integer huLu = StringUtils.parseInt(readData.get("huLu"));
		Integer wuHua = StringUtils.parseInt(readData.get("wuHua"));
		Integer zhaDan = StringUtils.parseInt(readData.get("zhaDan"));
		Integer wuXiao = StringUtils.parseInt(readData.get("wuXiao"));
		Integer tongHua = StringUtils.parseInt(readData.get("tongHua"));

		Player p = RedisUtil.getPlayerByUserId(String.valueOf(session
				.getAttribute(Cnst.USER_SESSION_USER_ID)),cid);

		if (p.getMoney() < Cnst.moneyMap.get(circleNum)) {// 玩家房卡不足
			playerMoneyNotEnough(interfaceId, session, roomType);
			return;
		}
		if (p.getRoomId() != null) {// 已存在其他房间
			playerExistOtherRoom(interfaceId, session);
			return;
		}

		if (roomType != null && roomType.equals(Cnst.ROOM_TYPE_2)) {// 自由模式开房，玩家房卡必须大于等于100
			if (p.getMoney() < 100) {
				playerMoneyNotEnough(interfaceId, session, roomType);
				return;
			}
		}

		if (roomType != null && roomType.equals(Cnst.ROOM_TYPE_2)) {
			// 从自己的代开房间列表中查找代开房间
			Map<String, String> rooms = RedisUtil
					.hgetAll(Cnst.get_ROOM_DAIKAI_KEY(cid) + userId);
			int size = 0;
			if (rooms != null) {
				size = rooms.size();
			}
			if (size >= 10) {
				roomEnough(interfaceId, session);
				return;
			}
		}

		RoomResp room = new RoomResp();

		String createTime = String.valueOf(new Date().getTime());
		room.setCreateId(userId);
		room.setState(Cnst.ROOM_STATE_CREATED);
		room.setCircleNum(circleNum);
		room.setTotalNum(0);
		room.setLastNum(circleNum);
		room.setCreateTime(createTime);
		room.setOpenName(p.getUserName());
		room.setRoomType(roomType);
		room.setType(type);
		room.setDiFen(diFen);
		room.setMaxPeople(maxPeople);
		room.setZhuangNum(zhuangNum);
		room.setFanRule(fanRule);
		room.setShunZi(shunZi);
		room.setTongHua(tongHua);
		room.setHuLu(huLu);
		room.setWuHua(wuHua);
		room.setZhaDan(zhaDan);
		room.setWuXiao(wuXiao);
		room.setMaxQZhuang(maxQZhuang);
		List<Integer> positions;
		if(maxPeople.equals(2)){//自由入座   -- 几人都可以开
			positions = new ArrayList<Integer>(8);
			for (int i = 0; i < 8; i++) {
				positions.add(i + 1);// 1开始
			}
		}else{
			positions = new ArrayList<Integer>(maxPeople);
			for (int i = 0; i < maxPeople; i++) {
				positions.add(i + 1);// 1开始
			}
		}
		room.setPositions(positions);
		// 初始化大接口的id
		room.setWsw_sole_action_id(1);
		room.setWsw_sole_main_id(1);
		// toEdit 需要去数据库匹配，查看房间号是否存在，如果存在，则重新生成
		while (true) {
			room.setRoomId(CommonUtil.getGivenRamdonNum(6));// 设置随机房间密码
			if (RedisUtil.getRoomRespByRoomId(String.valueOf(room.getRoomId()),cid) == null) {
				break;
			}
		}

		Long[] userIds ;
		if(maxPeople.equals(2)){//自由入座   -- 几人都可以开
			userIds = new Long[8];
		}else{
			userIds = new Long[maxPeople];
		}
		Map<String, Object> info = new HashMap<>();
		Map<String, Object> userInfos = new HashMap<String, Object>();
		// 处理开房模式
		if (roomType == null) {
			illegalRequest(interfaceId, session);
		} else if (roomType.equals(Cnst.ROOM_TYPE_1)) {// 房主模式
			// 设置用户信息

			// 获取玩家位置（1-maxPeoPle）
			p.setPosition(getPosition(room.getPositions()));
			p.setPlayStatus(Cnst.PLAYER_STATE_IN);// 进入房间状态
			p.setRoomId(room.getRoomId());
			p.setJoinIndex(1);
			// 初始化 用户
			p.initPlayer(p.getRoomId(), Cnst.PLAYER_STATE_IN, 0);
			userIds[p.getPosition() - 1] = p.getUserId();
			info.put("reqState", Cnst.REQ_STATE_1);
			info.put("playerNum", 1);
			p.setMoney(p.getMoney() - Cnst.moneyMap.get(room.getCircleNum()));
			userInfos.put("playerNum", 1);
			userInfos.put("money", p.getMoney());
			userInfos.put("playStatus", String.valueOf(Cnst.PLAYER_STATE_IN));
			userInfos.put("position", p.getPosition());
		} else if (roomType.equals(Cnst.ROOM_TYPE_2)) {// 自由模式
			// 将新创建的房间加入到代开房间列表里面
			RedisUtil.hset(Cnst.get_ROOM_DAIKAI_KEY(cid) + userId, room
					.getRoomId().toString(), "lzsb", null);
			p.setMoney(p.getMoney() - Cnst.moneyMap.get(room.getCircleNum()));
			info.put("reqState", Cnst.REQ_STATE_10);
			userInfos.put("money", p.getMoney());
			userInfos.put("playerNum", 0);
			userInfos.put("playStatus",
					String.valueOf(Cnst.PLAYER_STATE_DATING));
		} else if (roomType.equals(Cnst.ROOM_TYPE_3)) {// AA
			comingSoon(interfaceId, session);
			return;
		} else {
			illegalRequest(interfaceId, session);
			return;
		}
		room.setPlayerIds(userIds);
		room.setIp(Cnst.SERVER_IP);

		info.put("userInfo", userInfos);
		// 直接把传来的readData处理 返回
		readData.put("roomId", room.getRoomId());
		readData.put("state", room.getState());
		readData.put("userId", userId);
		readData.put("lastNum", room.getLastNum());
		readData.put("totalNum", room.getCircleNum());
		readData.remove("interfaceId");
		info.put("roomInfo", readData);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);

		// 更新redis数据 player roomMap
		RedisUtil.updateRedisData(null, p,cid);
		RedisUtil.setObject(Cnst.get_REDIS_PREFIX_ROOMMAP(cid).concat(String
				.valueOf(room.getRoomId())), room, Cnst.ROOM_LIFE_TIME_CREAT);

		// 解散房间超时任务开启
		startDisRoomTask(room.getRoomId(), Cnst.DIS_ROOM_TYPE_1,cid);

	}

	public static void main(String[] args) {
		// int x=1+(int)(Math.random()*8);
		// System.out.println(x);
		List<Integer> positions = new ArrayList<Integer>(8);
		for (int i = 0; i < 8; i++) {
			positions.add(i + 1);// 1开始
		}
		Integer position = getPosition(positions);
		System.out.println(position);
		position = getPosition(positions);
		System.out.println(position);
		position = getPosition(positions);
		System.out.println(position);
		position = getPosition(positions);
		System.out.println(position);
		position = getPosition(positions);
		System.out.println(position);
		position = getPosition(positions);
		System.out.println(position);
		position = getPosition(positions);
		System.out.println(position);
		positions.add(position);//添加到最后一个
		position = getPosition(positions);
		System.out.println(position);
		position = getPosition(positions);
		System.out.println(position);
	}

	/**
	 * 加入房间
	 * 
	 * @param session
	 * @param readData
	 */
	public synchronized static void interface_100008(IoSession session,
			Map<String, Object> readData) {
		logger.I("加入房间,interfaceId -> 100008");
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));

		Player p = RedisUtil.getPlayerByUserId(String.valueOf(session
				.getAttribute(Cnst.USER_SESSION_USER_ID)),cid);

		// 已经在其他房间里
		if (p.getRoomId() != null) {// 玩家已经在非当前请求进入的其他房间里
			playerExistOtherRoom(interfaceId, session);
			return;
		}
		// 房间不存在
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId),cid);
		if (room == null || room.getState() == Cnst.ROOM_STATE_YJS) {
			roomDoesNotExist(interfaceId, session);
			return;
		}

		// 房间人满
		Long[] userIds = room.getPlayerIds();
		boolean hasNull = false;
		int jionIndex = 0;
		for (Long uId : userIds) {
			if (uId == null) {
				hasNull = true;
			} else {
				jionIndex++;
			}
		}
		if (!hasNull) {
			roomFully(interfaceId, session);
			return;
		}

		// 验证ip是否一致
		if (!Cnst.SERVER_IP.equals(room.getIp())) {
			Map<String, Object> info = new HashMap<>();
			info.put("reqState", Cnst.REQ_STATE_14);
			info.put("roomSn", roomId);
			info.put("roomIp", room.getIp().concat(":").concat(Cnst.MINA_PORT));
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId,
					result.toJSONString());
			session.write(pd);
			return;
		}

		// 设置用户信息
//		p.setPlayStatus(Cnst.PLAYER_STATE_PREPARED);// 准备状态
		p.setRoomId(roomId);
		p.setPosition(getPosition(room.getPositions()));
		userIds[p.getPosition() - 1] = p.getUserId();
		// 初始化用户
		p.initPlayer(p.getRoomId(), Cnst.PLAYER_STATE_IN, 0);

		p.setJoinIndex(jionIndex + 1);

		Map<String, Object> info = new HashMap<>();
		info.put("reqState", Cnst.REQ_STATE_1);
		info.put("playerNum", jionIndex + 1);
		info.put("roomSn", roomId);
		info.put("ip", room.getIp().concat(":").concat(Cnst.MINA_PORT));
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);

		// 更新redis数据
		RedisUtil.updateRedisData(room, p,cid);

		// 通知另外几个人
		for (Long ids : userIds) {
			if (ids == null) {
				continue;
			}
			if (ids.equals(userId)) {
				continue;
			}
			Map<String, Object> userInfos = new HashMap<String, Object>();
			userInfos.put("userId", p.getUserId());
			userInfos.put("position", p.getPosition());
			userInfos.put("score", p.getScore());
			userInfos.put("money", p.getMoney());
			userInfos.put("playStatus", p.getPlayStatus());
			userInfos.put("userName", p.getUserName());
			userInfos.put("userImg", p.getUserImg());
			userInfos.put("ip", p.getIp());
			userInfos.put("joinIndex", p.getJoinIndex());
			userInfos.put("gender", p.getGender());
			Player pp = RedisUtil.getPlayerByUserId(String.valueOf(ids),cid);
			IoSession se = MinaServerManager.tcpServer.getSessions().get(
					pp.getSessionId());
			if (se != null && se.isConnected()) {
				JSONObject result1 = getJSONObj(interfaceId, 1, userInfos);
				ProtocolData pd1 = new ProtocolData(interfaceId,
						result1.toJSONString());
				se.write(pd1);
			}
		}

		// 如果加入的代开房间 通知房主
		if (room.getRoomType() == Cnst.ROOM_TYPE_2
				&& !userId.equals(room.getCreateId())) {
			MessageFunctions.interface_100112(p, room,Cnst.PLAYER_EXTRATYPE_ADDROOM,cid);
		}
	}

	/**
	 * 用户点击同意协议
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100009(IoSession session,
			Map<String, Object> readData) throws Exception {
		logger.I("用户点击同意协议,interfaceId -> 100009");
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Player p = RedisUtil.getPlayerByUserId(String.valueOf(session
				.getAttribute(Cnst.USER_SESSION_USER_ID)),cid);
		if (p == null) {
			illegalRequest(interfaceId, session);
			return;
		}
		p.setUserAgree(1);
		Map<String, Object> info = new JSONObject();
		info.put("reqState", Cnst.REQ_STATE_1);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);

		// 更新redis数据
		RedisUtil.updateRedisData(null, p,cid);

		/* 刷新数据库，用户同意协议 */
		userService.updateUserAgree(p.getUserId(),cid);
	}

	/**
	 * 查看代开房间列表
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100010(IoSession session,
			Map<String, Object> readData) throws Exception {
		logger.I("查看代开房间列表,interfaceId -> 100010");
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		List<Map<String, Object>> info = new ArrayList<Map<String, Object>>();
		String daiKaiKey = Cnst.get_ROOM_DAIKAI_KEY(cid) + String.valueOf(userId);
		Map<String, String> hgetAll = RedisUtil
				.hgetAll(daiKaiKey);
		if (hgetAll != null && hgetAll.size() > 0) {
			Set<String> keySet = hgetAll.keySet();
			for (String roomId : keySet) {
				// 获取每个房间的信息
				RoomResp room = RedisUtil.getRoomRespByRoomId(roomId,cid);
				if (room!=null && room.getCreateId().equals(userId)
						&& room.getState() != Cnst.ROOM_STATE_YJS
						&& room.getRoomType() == Cnst.ROOM_TYPE_2) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("roomId", room.getRoomId());
					map.put("userName", room.getOpenName());
					map.put("createTime", room.getCreateTime());
					map.put("circleNum", room.getCircleNum());
					map.put("lastNum", room.getLastNum());
					map.put("state", room.getState());
					map.put("fanRule", room.getFanRule());
					map.put("zhuangNum", room.getZhuangNum());
					map.put("maxPeople", room.getMaxPeople());
					map.put("diFen", room.getDiFen());
					map.put("type", room.getType());
					map.put("userId", room.getPlayerIds());
					
					map.put("shunZi", room.getShunZi());
					map.put("huLu", room.getHuLu());
					map.put("wuHua", room.getWuHua());
					map.put("zhaDan", room.getZhaDan());
					map.put("wuXiao", room.getWuXiao());
					map.put("tongHua", room.getTongHua());
					map.put("maxQZhuang", room.getMaxQZhuang());

					List<Map<String, Object>> playerInfo = new ArrayList<Map<String, Object>>();

					List<Player> list = RedisUtil.getPlayerList(room,cid);
					if (list != null && list.size() > 0) {
						for (Player p : list) {
							Map<String, Object> pinfo = new HashMap<String, Object>();
							pinfo.put("userId", p.getUserId());
							pinfo.put("position", p.getPosition());
							pinfo.put("userName", p.getUserName());
							pinfo.put("userImg", p.getUserImg());
							pinfo.put("state", p.getState());
							playerInfo.add(pinfo);
						}
					}
					map.put("playerInfo", playerInfo);
					info.add(map);
				}
				if(room == null){
					RedisUtil.hdel(daiKaiKey, String.valueOf(roomId));
				}
			}
		}
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 查看历史代开房间列表
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100011(IoSession session,
			Map<String, Object> readData) throws Exception {
		logger.I("查看历史代开房间列表,interfaceId -> 100011");
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		String userId = String.valueOf(readData.get("userId"));
		Integer page = StringUtils.parseInt(readData.get("page"));
		String key = Cnst.get_REDIS_PLAY_RECORD_PREFIX_ROE_DAIKAI(cid).concat(userId);

		Long pageSize = RedisUtil.llen(key);
		int start = (page - 1) * Cnst.PAGE_SIZE;
		int end = start + Cnst.PAGE_SIZE - 1;
		List<String> keys = RedisUtil.lrange(key, start, end);

		Map<String, Object> info = new HashMap<>();
		List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
		for (String roomKey : keys) {
			Map<String, String> roomInfos = RedisUtil
					.hgetAll(Cnst.get_REDIS_PLAY_RECORD_PREFIX(cid).concat(roomKey));
			maps.add(roomInfos);
		}
		info.put("roomInfo", maps);
		info.put("pages", pageSize == null ? 0
				: pageSize % Cnst.PAGE_SIZE == 0 ? pageSize / Cnst.PAGE_SIZE
						: (pageSize / Cnst.PAGE_SIZE + 1));
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 代开模式中踢出玩家
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100012(IoSession session,
			Map<String, Object> readData) {
		logger.I("准备,interfaceId -> 100012");
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		Long userId = StringUtils.parseLong(readData.get("userId"));

		// 房间不存在
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId),cid);
		if (room == null) {
			roomDoesNotExist(interfaceId, session);
			return;
		}

		try {
			// 验证解散人是否是真正的房主
			Long createId = (Long) session
					.getAttribute(Cnst.USER_SESSION_USER_ID);
			if (createId == null || !createId.equals(room.getCreateId())) {
				illegalRequest(interfaceId, session);
				return;
			}
		} catch (Exception e) {
			illegalRequest(interfaceId, session);
			return;
		}
		// 房间已经开局
		if (room.getState() != Cnst.ROOM_STATE_CREATED) {
			roomIsGaming(interfaceId, session);
			return;
		}

		List<Player> list = RedisUtil.getPlayerList(room,cid);
		boolean hasPlayer = false;// 列表中有当前玩家
		for (Player p : list) {
			if (p.getUserId().equals(userId)) {
				//将位置添加到房间列表
				room.getPositions().add(p.getPosition());
				// 初始化玩家
				p.initPlayer(null, Cnst.PLAYER_STATE_DATING, 0);

				// 刷新房间用户列表
				Long[] pids = room.getPlayerIds();
				if (pids != null) {
					for (int i = 0; i < pids.length; i++) {
						if (userId.equals(pids[i])) {
							pids[i] = null;
							break;
						}
					}
				}

				// 更新redis数据
				RedisUtil.updateRedisData(room, p,cid);
				hasPlayer = true;
				IoSession se = MinaServerManager.tcpServer.getSessions().get(
						p.getSessionId());
				MessageFunctions.interface_100107(se, Cnst.EXIST_TYPE_EXIST,
						list);
				break;
			}
		}

		Map<String, String> info = new HashMap<String, String>();
		info.put("reqState",
				String.valueOf(hasPlayer ? Cnst.REQ_STATE_1 : Cnst.REQ_STATE_8));
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 代开模式中房主解散房间
	 * 
	 * @param session
	 * @param readData
	 */
	public synchronized static void interface_100013(IoSession session,
			Map<String, Object> readData) {
		logger.I("代开模式中踢出玩家,interfaceId -> 100013");
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId),cid);
		// 房间不存在
		if (room == null) {
			roomDoesNotExist(interfaceId, session);
			return;
		}

		try {
			// 验证解散人是否是真正的房主
			Long createId = (Long) session
					.getAttribute(Cnst.USER_SESSION_USER_ID);
			if (createId == null || !createId.equals(room.getCreateId())) {
				illegalRequest(interfaceId, session);
				return;
			}
		} catch (Exception e) {
			illegalRequest(interfaceId, session);
			return;
		}

		// 房间已经开局
		if (room.getState() != Cnst.ROOM_STATE_CREATED) {
			roomIsGaming(interfaceId, session);
			return;
		}
		List<Player> players = RedisUtil.getPlayerList(room,cid);
		if (players != null && players.size() > 0) {
			for (Player p : players) {
				// 初始化玩家
				p.initPlayer(null, Cnst.PLAYER_STATE_DATING, 0);
			}
			RedisUtil.setPlayersList(players,cid);
		}

		MessageFunctions.interface_100107(session, Cnst.EXIST_TYPE_DISSOLVE,
				players);
		// 归还玩家房卡
		Player cp = RedisUtil.getPlayerByUserId(String.valueOf(session
				.getAttribute(Cnst.USER_SESSION_USER_ID)),cid);
		cp.setMoney(cp.getMoney() + Cnst.moneyMap.get(room.getCircleNum()));

		// 更新房主的redis数据
		RedisUtil.updateRedisData(null, cp,cid);

		RedisUtil.deleteByKey(Cnst.get_REDIS_PREFIX_ROOMMAP(cid).concat(String
				.valueOf(roomId)));
		String daiKaiKey = Cnst.get_ROOM_DAIKAI_KEY(cid).concat(String.valueOf(room.getCreateId()));
		if(RedisUtil.exists(daiKaiKey)){
			RedisUtil.hdel(daiKaiKey, String.valueOf(roomId));
		}
		Map<String, String> info = new HashMap<String, String>();
		info.put("reqState", String.valueOf(Cnst.REQ_STATE_1));
		info.put("money", String.valueOf(cp.getMoney()));
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 回放的时候，获取房间的局数
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100014(IoSession session,
			Map<String, Object> readData) {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		String roomId = StringUtils.toString((readData.get("roomSn")));
		String createTime = StringUtils.toString(readData.get("createTime"));
		Map<String, Object> info = new HashMap<String, Object>();
		int juNum = BackFileUtil.getFileNumByRoomId(Integer.parseInt(roomId));
		info.put("num", juNum);
		info.put("url", Cnst.HTTP_URL.concat(Cnst.BACK_FILE_PATH));
		info.put("roomSn", String.valueOf(roomId));
		info.put("createTime", createTime);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}

	/**
	 * 强制解散房间
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public static void interface_100015(IoSession session,
			Map<String, Object> readData) throws Exception {
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		System.out.println("*******强制解散房间" + roomId);
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		Long userId = (Long) session.getAttribute(Cnst.USER_SESSION_USER_ID);
		if (userId == null) {
			illegalRequest(interfaceId, session);
			return;
		}
		if (roomId != null) {
			RoomResp room = RedisUtil.getRoomRespByRoomId(String
					.valueOf(roomId),cid);
			if (room != null && room.getCreateId().equals(userId)) {
				if (room.getState() == Cnst.ROOM_STATE_GAMIING) {
					// 中途准备阶段解散房间不计入回放中
					List<Integer> xiaoJSInfo = new ArrayList<Integer>();
					for (int i = 0; i < room.getPlayerIds().length; i++) {
						xiaoJSInfo.add(0);
					}
					room.addXiaoJSInfo(xiaoJSInfo);
					room.setState(Cnst.ROOM_STATE_YJS);
					List<Player> players = RedisUtil.getPlayerList(room,cid);

					MessageFunctions.updateDatabasePlayRecord(room,cid);

					RedisUtil.deleteByKey(Cnst.get_REDIS_PREFIX_ROOMMAP(cid)
							.concat(String.valueOf(roomId)));// 删除房间
					if (players != null && players.size() > 0) {
						for (Player p : players) {
							// 初始化玩家
							p.initPlayer(null, Cnst.PLAYER_STATE_DATING, 0);
							RedisUtil.updateRedisData(null, p,cid);
						}
						for (Player p : players) {
							IoSession se = MinaServerManager.tcpServer
									.getSessions().get(p.getSessionId());
							if (se != null && se.isConnected()) {
								Map<String, Object> data = new HashMap<String, Object>();
								data.put("interfaceId", 100100);
								data.put("openId", p.getOpenId());
								data.put("cId", Cnst.cid);
								MessageFunctions.interface_100100(se, data);
							}
						}
					}

					// BackFileUtil.write(null, 100103, room,null,null);//写入文件内容
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
			JSONObject result = MessageFunctions.getJSONObj(interfaceId, 1,
					info);
			ProtocolData pd = new ProtocolData(interfaceId,
					result.toJSONString());
			session.write(pd);
		}
	}

	/**
	 * 产生随机的风
	 * 
	 * @param players
	 * @return
	 */
	protected static Integer getPosition(List<Integer> list) {
		int playerPositon = 0;
		int x = list.size();
		if (x != 1) {
			int s = ( (int) (Math.random() * x));// 获取随机的角标
			playerPositon = list.get(s);
			list.remove(s);//移除该角标
		} else {
			playerPositon = list.get(0);
			list.remove(0);//移除该角标
		}
		return playerPositon;
	}

	/**
	 * 或得到的是一个正数，要拿当前玩家的剩余房卡，减去这个值
	 * 
	 * @param userId
	 * @return
	 */
	private static Integer getFrozenMoney(Long userId,String cid) {
		int frozenMoney = 0;
		Set<String> roomMapKeys = RedisUtil
				.getSameKeys(Cnst.get_REDIS_PREFIX_ROOMMAP(cid));
		if (roomMapKeys != null && roomMapKeys.size() > 0) {
			for (String roomId : roomMapKeys) {
				RoomResp room = RedisUtil.getRoomRespByRoomId(roomId,cid);
				if (room.getCreateId().equals(userId)
						&& room.getState() == Cnst.ROOM_STATE_CREATED) {
					frozenMoney += Cnst.moneyMap.get(room.getCircleNum());
				}
			}
		}
		return frozenMoney;
	}

	/**
	 * 返回用户
	 * 
	 * @param openId
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	public static Player getPlayerInfos(String openId, String ip, String cid,
			IoSession session) {
		if (cid == null || !cid.equals(Cnst.cid)) {
			return null;
		}
		Player p = null;
		long updateTime = 0;
		try {
			String notice = RedisUtil.getStringByKey(Cnst.get_NOTICE_KEY(cid));
			if (notice == null) {
				notice = userService.getNotice(cid);
				RedisUtil.setObject(Cnst.get_NOTICE_KEY(cid), notice, null);
				// setStringByKey(Cnst.NOTICE_KEY, "接口都是经济");
			}
			Set<String> openIds = RedisUtil
					.getSameKeys(Cnst.get_REDIS_PREFIX_OPENIDUSERMAP(cid));
			if (openIds != null && openIds.contains(openId)) {// 用户是断线重连
				Long userId = RedisUtil.getUserIdByOpenId(openId,cid);
				p = RedisUtil.getPlayerByUserId(String.valueOf(userId),cid);
				IoSession se = session.getService().getManagedSessions()
						.get(p.getSessionId());
				p.setNotice(notice);
				p.setState(Cnst.PLAYER_LINE_STATE_INLINE);
				updateTime = p.getUpdateTime() == null ? 0l : p.getUpdateTime();
				if (se != null) {
					Long tempuserId = Long.valueOf(se.getAttribute(Cnst.USER_SESSION_USER_ID).toString());
					if (se.getId() != session.getId()
							&& userId.equals(tempuserId)) {
						MessageFunctions.interface_100106(se);
					}
				}
				if (p.getPlayStatus() != null
						&& p.getPlayStatus().equals(Cnst.PLAYER_STATE_DATING)) {// 去数据库重新请求用户，//需要减去玩家开的房卡
					Player loaclMysql = userService.getByOpenId(openId, cid);
					if (loaclMysql == null) {
						p = userService_login.getUserInfoByOpenId(openId,cid);
						if (p == null) {
							return null;
						} else {
							p.setUserAgree(0);
							p.setGender(p.getGender());
//							p.setTotalGameNum("0");
							p.setMoney(Cnst.MONEY_INIT);
							p.setLoginStatus(1);
							p.setCId(cid);
							String time = String.valueOf(new Date().getTime());
							p.setLastLoginTime(time);
							p.setSignUpTime(time);
							p.setUpdateTime(System.currentTimeMillis());
							userService.save(p);

						}
					} else {
						// FIXME 判断是否更新昵称等数据 注意这个是从数据库读取到的数据 所以怎么判断 要注意
						// 但是这个库里面金币是正确的 UID也是正确的 只是昵称不太一样
						if (System.currentTimeMillis() - updateTime > Cnst.updateDiffTime) {
							Player updatep = userService_login
									.getUserInfoByOpenId(openId,cid);
							p.setUserName(updatep.getUserName());
							p.setUserImg(updatep.getUserImg());
							p.setGender(updatep.getGender());
							p.setUpdateTime(System.currentTimeMillis());
							//更新用户头像 
							userService.updateInfo(p);
						}
					}
					p.setScore(0);
					p.setIp(ip);
					p.setNotice(notice);
					p.setState(Cnst.PLAYER_LINE_STATE_INLINE);
					p.setPlayStatus(Cnst.PLAYER_STATE_DATING);
					p.setMoney(loaclMysql.getMoney() - getFrozenMoney(p.getUserId(),cid));
					p.setUserAgree(1);
				}
				// 更新用户ip 最后登陆时间
				userService.updateIpAndLastTime(openId, ip,cid);
				return p;
			}
			p = userService.getByOpenId(openId, cid);
			if (p != null) {// 当前游戏的数据库中存在该用户
				p.setNotice(notice);

				Player redisP = RedisUtil.getPlayerByUserId(String.valueOf(p
						.getUserId()),cid);
				updateTime = (redisP == null || redisP.getUpdateTime() == null) ? 0l
						: redisP.getUpdateTime();
				// FIXME 判断是否更新昵称等数据 注意这个是从数据库读取到的数据 所以怎么判断 要注意
				// 但是这个库里面金币是正确的 UID也是正确的
				if (System.currentTimeMillis() - updateTime > Cnst.updateDiffTime) {
					Player updatep = userService_login
							.getUserInfoByOpenId(openId,cid);
					p.setUserName(updatep.getUserName());
					p.setUserImg(updatep.getUserImg());
					p.setGender(updatep.getGender());
					p.setUpdateTime(System.currentTimeMillis());
					userService.updateInfo(p);
				}
			} else {// 如果没有，需要去微信的用户里查询
				p = userService_login.getUserInfoByOpenId(openId,cid);
				if (p == null) {
					return null;
				} else {
					p.setTotalGameNum("0");
					p.setMoney(Cnst.MONEY_INIT);
					p.setLoginStatus(1);
					p.setCId(cid);
					String time = String.valueOf(new Date().getTime());
					p.setLastLoginTime(time);
					p.setSignUpTime(time);
					p.setUpdateTime(System.currentTimeMillis());
					p.setUserAgree(0);
					userService.save(p);
				}
			}
			p.setScore(0);
			p.setIp(ip);
			p.setNotice(notice);
			p.setState(Cnst.PLAYER_LINE_STATE_INLINE);
			p.setPlayStatus(Cnst.PLAYER_STATE_DATING);
			p.setMoney(p.getMoney() - getFrozenMoney(p.getUserId(),cid));
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 更新用户ip 最后登陆时间
		userService.updateIpAndLastTime(openId, ip,cid);
		return p;
	}

}
