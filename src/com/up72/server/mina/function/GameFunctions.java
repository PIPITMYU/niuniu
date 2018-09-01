package com.up72.server.mina.function;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.bean.DissolveRoom;
import com.up72.server.mina.bean.ProtocolData;
import com.up72.server.mina.main.MinaServerManager;
import com.up72.server.mina.utils.RedisBackFileUtil;
import com.up72.server.mina.utils.StringUtils;
import com.up72.server.mina.utils.redis.RedisUtil;
import com.up72.server.mina.utils.dcuse.GameUtil;
import com.up72.server.mina.utils.dcuse.JieSuan;

/**
 * Created by Administrator on 2017/7/13. 游戏中
 */

public class GameFunctions extends TCPGameFunctions {
	final static Object object = new Object();

	/**
	 * 用户点击准备，用在小结算那里，
	 * 
	 * @param session
	 * @param readData
	 */
	public synchronized static void interface_100200(IoSession session,
			Map<String, Object> readData) {
		logger.I("准备,interfaceId -> 100200");
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));

		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId),cid);
		Player currentPlayer = null;
		List<Player> players = RedisUtil.getPlayerList(room,cid);
		for (Player p : players) {
			if (p.getUserId().equals(userId)) {
				currentPlayer = p;
				break;
			}
		}

		if (room.getState() == Cnst.ROOM_STATE_GAMIING) {
			return;
		}
		if (currentPlayer == null
				|| currentPlayer.getPlayStatus() == Cnst.PLAYER_STATE_PREPARED) {
			return;
		}

		currentPlayer.initPlayer(currentPlayer.getRoomId(),
				Cnst.PLAYER_STATE_PREPARED, currentPlayer.getScore());

		boolean allPrepared = true;

		for (Player p : players) {
			if (!p.getPlayStatus().equals(Cnst.PLAYER_STATE_PREPARED)) {
				allPrepared = false;
			}
		}

		if (allPrepared && players != null ) {
			if(room.getMaxPeople() != 2){
				if(players.size() == room.getMaxPeople()){
					//开局
					startGame(room, players,cid);
					//关闭解散房间计时任务			        
					RedisBackFileUtil.save(interfaceId, room,players,null,cid);//写入文件内容
				}
			}
			//自由开局
			if(room.getMaxPeople() == 2){
				List<Long> ids = new ArrayList<Long>();
				Long[] playIds = room.getPlayerIds();
				for(Long id:playIds){
					if(id!=null){
						ids.add(id);
					}
				}
				Long[] array = new Long[ids.size()];
				for (int i = 0; i < ids.size(); i++) {
			        array[i] = ids.get(i);
			    }
				room.setPlayerIds(array);
				startGame(room, players,cid);
				//关闭解散房间计时任务		        
				RedisBackFileUtil.save(interfaceId, room,players,null,cid);//写入文件内容
			}			 
		}
		Map<String, Object> info = new HashMap<String, Object>();
		List<Map<String, Object>> userInfo = new ArrayList<Map<String, Object>>();
		for (Player p : players) {
			Map<String, Object> i = new HashMap<String, Object>();
			i.put("userId", p.getUserId());
			i.put("playStatus", p.getPlayStatus());			
			userInfo.add(i);
		}
		info.put("userInfo", userInfo);
		Map<String, Object> roominfo = new HashMap<String, Object>();
		roominfo.put("state", room.getState());
		roominfo.put("playStatus", room.getPlayStatus());
		if(room.getState() == Cnst.ROOM_STATE_GAMIING){
			roominfo.put("zhuangPlayer", room.getZhuangPlayer());
		}
		info.put("roomInfo", roominfo);
		for (Player p : players) {
			//明牌抢庄时发四张牌
			if(room.getType() == Cnst.ROOM_PALYTYPE_MINGPAI && room.getPlayStatus()!= null &&room.getPlayStatus() == Cnst.ROOM_PALYSTATE_QIANGZHUANG){
				List<Integer> pais = new ArrayList<Integer>();
				for(int i=0;i<=3;i++){
					pais.add(p.getPais().get(i).getOrigin());
				}
				info.put("pais", pais);
			}			
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
			IoSession se = MinaServerManager.tcpServer.getSessions().get(
					p.getSessionId());
			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}

		RedisUtil.setPlayersList(players,cid);
		RedisUtil.updateRedisData(room, null,cid);
	}

	/**
	 * 开局发牌
	 * 
	 * @param roomId
	 */
	public static void startGame(RoomResp room, List<Player> players,String cid) {
		room.setXiaoJuNum(room.getXiaoJuNum() == null ? 1
				: room.getXiaoJuNum() + 1);
		room.setXjst(System.currentTimeMillis());
		room.setState(Cnst.ROOM_STATE_GAMIING);
		room.initCurrentCardList();
		//每人发五张牌
		for(int i=0;i<players.size();i++){
			players.get(i).setPlayStatus(Cnst.PLAYER_STATE_GAME);
			room.dealCard(5, players.get(i));
		}
		room.setCurrentCardList(null);
		//设置房间状态
		Integer type = room.getType();
		if(type == Cnst.ROOM_PALYTYPE_MINGPAI || type == Cnst.ROOM_PALYTYPE_ZIYOU){
			room.setZhuangPlayer(null);
			room.setXianPlayers(null);
		}
		List<Long> playerIds = GameUtil.changeList(room.getPlayerIds());
		if(room.getXiaoJuNum() == 1){
			//根据房间模式设置庄家 开局初始化方法中 只有自由抢庄和名牌抢庄会有初始化 牛牛上庄会在小结算时设置庄家
			if(type == Cnst.ROOM_PALYTYPE_GUDING){
				//固定庄家 
				if(room.getRoomType() == Cnst.ROOM_TYPE_1){
					room.setZhuangPlayer(room.getCreateId());
				}else{
					room.setZhuangPlayer(GameUtil.getRondomId(playerIds));
				}
				List<Long> xianList = getSameList(playerIds);
				xianList.remove(room.getZhuangPlayer());
				room.setXianPlayers(xianList);
			}else if(type == Cnst.ROOM_PALYTYPE_NIUNIU){
				room.setZhuangPlayer(GameUtil.getRondomId(playerIds));
				List<Long> xianList = getSameList(playerIds);
				xianList.remove(room.getZhuangPlayer());
				room.setXianPlayers(xianList);
			}
			
		}
		if(type == Cnst.ROOM_PALYTYPE_NIUNIU || type == Cnst.ROOM_PALYTYPE_GUDING || type == Cnst.ROOM_PALYTYPE_TONGBI){
			room.setPlayStatus(Cnst.ROOM_PALYSTATE_YAZHU);
		}else{
			room.setPlayStatus(Cnst.ROOM_PALYSTATE_QIANGZHUANG);
		}
		if(room.getXiaoJuNum() == 1){
			notifyDisRoomTask(room,Cnst.DIS_ROOM_TYPE_1,true);
			addRoomToDB(room,cid);
		}
	}
	
	/**
	 * 切换房间流程
	 * 
	 * @param session
	 * @param readData
	 */
	public static void interface_100202(IoSession session,
			Map<String, Object> readData) {
		logger.I("切换房间流程,interfaceId -> 100202");
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		JSONObject info = new JSONObject();
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId),cid);
		Player player = RedisUtil.getPlayerByUserId(String.valueOf(userId),cid);
		if(room.getPlayStatus()!=null && room.getPlayStatus() == Cnst.ROOM_PALYSTATE_LIANGPAI){	
			List<Integer> pais = new ArrayList<Integer>();
			if(room.getType() == Cnst.ROOM_PALYTYPE_MINGPAI){
				pais.add(player.getPais().get(4).getOrigin());
			}else{
				for(int i=0;i<5;i++){
					pais.add(player.getPais().get(i).getOrigin());
				}
			}
			info.put("pais", pais);	
		}
		info.put("playStatus", room.getPlayStatus());
		info.put("state", room.getState());
		info.put("zhuangPlayer", room.getZhuangPlayer());
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		session.write(pd);
	}
	
	/**
	 * 抢庄
	 * 
	 * @param session
	 * @param readData
	 */
	public synchronized static void interface_100207(IoSession session,
			Map<String, Object> readData) {
		logger.I("抢庄,interfaceId -> 100207");
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		Integer playStatus = StringUtils.parseInt(readData.get("playStatus"));
		Integer qiangZhuang = StringUtils.parseInt(readData.get("qiangZhuang"));
		//自由抢庄
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId),cid);
		Player player = RedisUtil.getPlayerByUserId(String.valueOf(userId),cid);
		if(player.getQiangZhuang() != null){
			illegalRequest(interfaceId, session);
			return;
		}
		List<Player> players = RedisUtil.getPlayerList(room,cid);
		if(playStatus != room.getPlayStatus() || room.getType() == Cnst.ROOM_PALYTYPE_GUDING 
				|| room.getType() == Cnst.ROOM_PALYTYPE_NIUNIU || room.getType() == Cnst.ROOM_PALYTYPE_TONGBI){
			illegalRequest(interfaceId, session);
			return;
		}
		room.setQiangZhuangNum(room.getQiangZhuangNum()==null? 1 :room.getQiangZhuangNum()+1);
		if(room.getType() == Cnst.ROOM_PALYTYPE_ZIYOU){
			List<Long> qiangZhuangList = room.getQiangZhuangList();
			if(qiangZhuangList == null){
				qiangZhuangList = new ArrayList<Long>();
			}
			if(qiangZhuang == 0){
				qiangZhuangList.add(userId);
			}else{
				//不抢
			}
			room.setQiangZhuangList(qiangZhuangList);
		}
		if(room.getType() == Cnst.ROOM_PALYTYPE_MINGPAI){
			if(qiangZhuang == -1){
				//不抢
			}else{
				List<Long> qiangZhuangList;
				if(room.getMaxQiangZhuang() == null || qiangZhuang>room.getMaxQiangZhuang()){
					room.setMaxQiangZhuang(qiangZhuang);
					qiangZhuangList = new ArrayList<Long>();
					qiangZhuangList.add(userId);
					room.setQiangZhuangList(qiangZhuangList);
				}else{
					if(qiangZhuang == room.getMaxQiangZhuang()){
						qiangZhuangList = room.getQiangZhuangList();
						qiangZhuangList.add(userId);
						room.setQiangZhuangList(qiangZhuangList);
					}
					if(qiangZhuang < room.getMaxQiangZhuang()){
						
					}
				}
			}
		}
		Integer gogo = 0;
		if(room.getQiangZhuangNum() == room.getPlayerIds().length){
			gogo = 1;
			room.setPlayStatus(Cnst.ROOM_PALYSTATE_YAZHU);
		
			if(room.getQiangZhuangList() == null || room.getQiangZhuangList().size() == 0){
				List<Long> playerIds = GameUtil.changeList(room.getPlayerIds());
				room.setZhuangPlayer(GameUtil.getRondomId(playerIds));
				List<Long> xianList = getSameList(playerIds);
				xianList.remove(room.getZhuangPlayer());
				room.setXianPlayers(xianList);
			}else{
				room.setZhuangPlayer(GameUtil.getRondomId(room.getQiangZhuangList()));
				List<Long> xianList = GameUtil.changeList(room.getPlayerIds());
				xianList.remove(room.getZhuangPlayer());
				room.setXianPlayers(xianList);
			}
		}
		player.setQiangZhuang(qiangZhuang);
		RedisUtil.updateRedisData(room, player,cid);
		JSONObject info = new JSONObject();
		JSONObject userInfo = new JSONObject();
		userInfo.put("userId", userId);
		userInfo.put("qiangZhuang", qiangZhuang);
		info.put("userInfo", userInfo);
		info.put("continue", gogo);
		if(gogo == 1){
			info.put("zhuangPlayer", room.getZhuangPlayer());
			info.put("playStatus", room.getPlayStatus());
		}
		RedisBackFileUtil.save(interfaceId, room,null,info,cid);//写入文件内容
		
		for (Player p : players) {
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
			IoSession se = MinaServerManager.tcpServer.getSessions().get(
					p.getSessionId());
			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}
	}

	/**
	 * 押注
	 * 
	 * @param session
	 * @param readData
	 */
	public synchronized static void interface_100208(IoSession session,
			Map<String, Object> readData) throws Exception {
		logger.I("押注,interfaceId -> 100208 ");
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		String roomId = StringUtils.toString((readData.get("roomSn")));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer playStatus = StringUtils.parseInt(readData.get("playStatus"));
		Integer yaZhu = StringUtils.parseInt(readData.get("yaZhu"));
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId),cid);
		if(playStatus != room.getPlayStatus()){
			illegalRequest(interfaceId, session);
			return;
		}
		Player player = RedisUtil.getPlayerByUserId(String.valueOf(userId),cid);
		if(player.getYaZhu() != null){
			illegalRequest(interfaceId, session);
			return;
		}
		if(room.getType() != Cnst.ROOM_PALYTYPE_TONGBI && room.getZhuangPlayer().equals(userId)){
			illegalRequest(interfaceId, session);
			return;
		}
		List<Player> players = RedisUtil.getPlayerList(room,cid);
		player.setYaZhu(yaZhu);
		room.setYaZhuNum(room.getYaZhuNum()==null? 1 :room.getYaZhuNum()+1);
		Integer gogo = 0;
		if(room.getType() == Cnst.ROOM_PALYTYPE_TONGBI){
			if(room.getYaZhuNum() == room.getPlayerIds().length){
				gogo = 1;
				room.setPlayStatus(Cnst.ROOM_PALYSTATE_LIANGPAI);
			}	
		}else{
			if(room.getYaZhuNum() == room.getPlayerIds().length-1){
				gogo = 1;
				room.setPlayStatus(Cnst.ROOM_PALYSTATE_LIANGPAI);
			}	
		}	
		RedisUtil.updateRedisData(room, player,cid);
		JSONObject info = new JSONObject();
		JSONObject userInfo = new JSONObject();
		userInfo.put("userId", userId);
		userInfo.put("yaZhu", yaZhu);
		info.put("userInfo", userInfo);
		info.put("continue", gogo);	
		if(gogo == 1){
			info.put("playStatus", room.getPlayStatus());	
		}
		RedisBackFileUtil.save(interfaceId, room,null,info,cid);//写入文件内容
		
		for (Player p : players) {
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
			IoSession se = MinaServerManager.tcpServer.getSessions().get(
					p.getSessionId());
			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}
 	}

	/**
	 * 亮牌
	 * 
	 * @param session
	 * @param readData
	 */
	public synchronized static void interface_100209(IoSession session,
			Map<String, Object> readData) throws Exception {
		logger.I("亮牌,interfaceId -> 100209");
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer playStatus = StringUtils.parseInt(readData.get("playStatus"));
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId),cid);
		Player player = RedisUtil.getPlayerByUserId(String.valueOf(userId),cid);
		//房间状态在游戏中，玩家状态必须没有两过牌，亮过之后不能再亮
		if(playStatus != room.getPlayStatus() || player.getPlayStatus().equals(Cnst.PLAYER_STATE_OVER)){
			illegalRequest(interfaceId, session);
			return;
		}
		
		List<Player> players = RedisUtil.getPlayerList(room,cid);
		
		player.setPlayStatus(Cnst.PLAYER_STATE_OVER);
		
		room.setLiangPaiNum(room.getLiangPaiNum()==null? 1 :room.getLiangPaiNum()+1);
		Integer gogo = 0;
		if(room.getLiangPaiNum() == room.getPlayerIds().length){
			gogo = 1;
			room.setState(Cnst.ROOM_STATE_XJS);
		}
		player.setNiuNum(GameUtil.getNiuNum(player.getPais(), room));
		RedisUtil.updateRedisData(room, player,cid);
		JSONObject info = new JSONObject();
		JSONObject userInfo = new JSONObject();
		userInfo.put("userId", userId);
		userInfo.put("niuNum", player.getNiuNum());
		List<Integer> pais = new ArrayList<Integer>();
		for(int i=0;i<5;i++){
			pais.add(player.getPais().get(i).getOrigin());
		}
		userInfo.put("pais", pais);
		JSONObject roomInfo = new JSONObject();
		roomInfo.put("continue", gogo);
		if(gogo == 1){
			roomInfo.put("state", room.getState());
		}
		info.put("userInfo", userInfo);
		info.put("roomInfo", roomInfo);
		
		RedisBackFileUtil.save(interfaceId, room,null,info,cid);//写入文件内容
		
		for (Player p : players) {
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
			IoSession se = MinaServerManager.tcpServer.getSessions().get(
					p.getSessionId());
			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}
		//全部亮牌 进入小结算
		if(room.getState() == Cnst.ROOM_STATE_XJS){
			JieSuan.xiaoJieSuan(String.valueOf(roomId),cid);
		}
	}

	/**
	 * 玩家申请解散房间
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public synchronized static void interface_100203(IoSession session,
			Map<String, Object> readData) throws Exception {
		logger.I("玩家请求解散房间,interfaceId -> 100203");
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId),cid);
		if (room.getDissolveRoom() != null) {
			return;
		}
		DissolveRoom dis = new DissolveRoom();
		dis.setDissolveTime(new Date().getTime());
		dis.setUserId(userId);
		List<Map<String, Object>> othersAgree = new ArrayList<>();
		List<Player> players = RedisUtil.getPlayerList(room,cid);
		for (Player p : players) {
			if (!p.getUserId().equals(userId)) {
				Map<String, Object> map = new HashMap<>();
				map.put("userId", p.getUserId());
				map.put("agree", 0);// 1同意；2解散；0等待
				othersAgree.add(map);
			}
		}
		dis.setOthersAgree(othersAgree);
		room.setDissolveRoom(dis);

		Map<String, Object> info = new HashMap<>();
		info.put("dissolveTime", dis.getDissolveTime());
		info.put("userId", dis.getUserId());
		info.put("othersAgree", dis.getOthersAgree());
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		for (Player p : players) {
			IoSession se = session.getService().getManagedSessions()
					.get(p.getSessionId());
			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}

		for (Player p : players) {
			RedisUtil.updateRedisData(null, p,cid);
		}

		RedisUtil.updateRedisData(room, null,cid);
		
		//解散房间超时任务开启
        startDisRoomTask(room.getRoomId(),Cnst.DIS_ROOM_TYPE_2,cid);
	}

	/**
	 * 同意或者拒绝解散房间
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */

	public synchronized static void interface_100204(IoSession session,
			Map<String, Object> readData) throws Exception {
		logger.I("同意或者拒绝解散房间,interfaceId -> 100203");
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		Integer userAgree = StringUtils.parseInt(readData.get("userAgree"));
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId),cid);
		if (room == null) {// 房间已经自动解散
			Map<String, Object> info = new HashMap<>();
			info.put("reqState", Cnst.REQ_STATE_4);
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId,
					result.toJSONString());
			session.write(pd);
			return;
		}
		if (room.getDissolveRoom() == null) {
			Map<String, Object> info = new HashMap<>();
			info.put("reqState", Cnst.REQ_STATE_7);
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId,
					result.toJSONString());
			session.write(pd);
			return;
		}
		List<Map<String, Object>> othersAgree = room.getDissolveRoom()
				.getOthersAgree();
		for (Map<String, Object> m : othersAgree) {
			if (String.valueOf(m.get("userId")).equals(String.valueOf(userId))) {
				m.put("agree", userAgree);
				break;
			}
		}
		Map<String, Object> info = new HashMap<>();
		info.put("dissolveTime", room.getDissolveRoom().getDissolveTime());
		info.put("userId", room.getDissolveRoom().getUserId());
		info.put("othersAgree", room.getDissolveRoom().getOthersAgree());
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());

		if (userAgree == 2) {
			//有玩家拒绝解散房间
			room.setDissolveRoom(null);
			RedisUtil.setObject(Cnst.get_REDIS_PREFIX_ROOMMAP(cid).concat(String.valueOf(roomId)), room,
					Cnst.ROOM_LIFE_TIME_CREAT);
		}
		int agreeNum = 0;
		int rejectNunm = 0;

		for (Map<String, Object> m : othersAgree) {
			if (m.get("agree").equals(1)) {
				agreeNum++;
			} else if (m.get("agree").equals(2)) {
				rejectNunm++;
			}
		}
		RedisUtil.updateRedisData(room, null,cid);

		List<Player> players = RedisUtil.getPlayerList(room,cid);

		if (agreeNum == room.getPlayerIds().length-1 || rejectNunm >= 1) {
			if (agreeNum == room.getPlayerIds().length-1) {
				//解散房间是 xiaoJSInfo 写入0
				if(room.getState() == Cnst.ROOM_STATE_GAMIING){
					//中途准备阶段解散房间不计入回放中
					List<Integer> xiaoJSInfo = new ArrayList<Integer>();
					for(int i=0;i<room.getPlayerIds().length;i++){
						xiaoJSInfo.add(0);
					}
					room.addXiaoJSInfo(xiaoJSInfo);
				}
				if (room.getRoomType() == Cnst.ROOM_TYPE_2) {
					MessageFunctions.interface_100112(null, room,Cnst.PLAYER_EXTRATYPE_JIESANROOM,cid);
				}
				room.setState(Cnst.ROOM_STATE_YJS);
				
				MessageFunctions.updateDatabasePlayRecord(room,cid);
				for (Player p : players) {
					p.initPlayer(null, Cnst.PLAYER_STATE_DATING, 0);
				}
				room.setDissolveRoom(null);
				RedisUtil.setObject(Cnst.get_REDIS_PREFIX_ROOMMAP(cid).concat(String.valueOf(roomId)),
						room, Cnst.ROOM_LIFE_TIME_DIS);
				RedisUtil.setPlayersList(players,cid);
				//关闭解散房间计时任务
		        notifyDisRoomTask(room,Cnst.DIS_ROOM_TYPE_2,false);

			}
		}
		

		for (Player p : players) {
			IoSession se = session.getService().getManagedSessions()
					.get(p.getSessionId());
			if (se != null && se.isConnected()) {
				se.write(pd);
			}
		}

	}

	/**
	 * 退出房间
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public synchronized static void interface_100205(IoSession session,
			Map<String, Object> readData) throws Exception {
		logger.I("准备,interfaceId -> 100205");
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		Long userId = StringUtils.parseLong(readData.get("userId"));

		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId),cid);
		if (room == null) {
			roomDoesNotExist(interfaceId, session);
			return;
		}
		if (room.getState() == Cnst.ROOM_STATE_CREATED) {
			List<Player> players = RedisUtil.getPlayerList(room,cid);
			Map<String, Object> info = new HashMap<>();
			info.put("userId", userId);
			if (room.getCreateId().equals(userId)) {// 房主退出，
				if (room.getRoomType().equals(Cnst.ROOM_TYPE_1)) {// 房主模式
					int circle = room.getCircleNum();

				
					info.put("type", Cnst.EXIST_TYPE_DISSOLVE);

					for (Player p : players) {
						if (p.getUserId().equals(userId)) {
							p.setMoney(p.getMoney() + Cnst.moneyMap.get(circle));
							break;
						}
					}

					RedisUtil.deleteByKey(Cnst.get_REDIS_PREFIX_ROOMMAP(cid)
							.concat(String.valueOf(roomId)));

					for (Player p : players) {
						p.initPlayer(null, Cnst.PLAYER_STATE_DATING,0);
					}
					 //关闭解散房间计时任务
                    notifyDisRoomTask(room,Cnst.DIS_ROOM_TYPE_1,false);
				} else {// 自由模式，走正常退出
					for (Player player : players) {
						if(player.getUserId().equals(userId)){//找到退出的玩家
							room.getPositions().add(player.getPosition());
						}
					}
					info.put("type", Cnst.EXIST_TYPE_EXIST);
					existRoom(room, players, userId);
					RedisUtil.updateRedisData(room, null,cid);					
				}
			} else {// 正常退出
				for (Player player : players) {
					if(player.getUserId().equals(userId)){//找到退出的玩家
						room.getPositions().add(player.getPosition());//将位置添加到房间列表
						// 如果加入的代开房间 通知房主
						if (room.getRoomType() == Cnst.ROOM_TYPE_2
								&& !userId.equals(room.getCreateId())) {
							MessageFunctions.interface_100112(player, room,Cnst.PLAYER_EXTRATYPE_EXITROOM,cid);
						}
					}
				}
				info.put("type", Cnst.EXIST_TYPE_EXIST);
				existRoom(room, players, userId);
				RedisUtil.updateRedisData(room, null,cid);							
			}
			JSONObject result = getJSONObj(interfaceId, 1, info);
			ProtocolData pd = new ProtocolData(interfaceId,
					result.toJSONString());

			for (Player p : players) {
				RedisUtil.updateRedisData(null, p,cid);
			}

			for (Player p : players) {
				IoSession se = session.getService().getManagedSessions()
						.get(p.getSessionId());
				if (se != null && se.isConnected()) {
					se.write(pd);
				}
			}

		} else {
			roomIsGaming(interfaceId, session);
		}
	}

	private static void existRoom(RoomResp room, List<Player> players,
			Long userId) {
		for (Player p : players) {
			if (p.getUserId().equals(userId)) {
				p.initPlayer(null, Cnst.PLAYER_STATE_DATING,  0);
				break;
			}
		}
		Long[] pids = room.getPlayerIds();
		if (pids != null) {
			for (int i = 0; i < pids.length; i++) {
				if (userId.equals(pids[i])) {
					pids[i] = null;
					break;
				}
			}
		}
	}

	/**
	 * 语音表情
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */
	public static void interface_100206(IoSession session,
			Map<String, Object> readData) throws Exception {
		logger.I("准备,interfaceId -> 100206");
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		String userId = String.valueOf(readData.get("userId"));
		String type = String.valueOf(readData.get("type"));
		String idx = String.valueOf(readData.get("idx"));

		Map<String, Object> info = new HashMap<>();
		info.put("roomId", roomId);
		info.put("userId", userId);
		info.put("type", type);
		info.put("idx", idx);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		List<Player> players = RedisUtil.getPlayerList(roomId,cid);
		for (Player p : players) {
			if (!p.getUserId().equals(userId)) {
				IoSession se = session.getService().getManagedSessions()
						.get(p.getSessionId());
				if (se != null && se.isConnected()) {
					se.write(pd);
				}
			}
		}
	}
	
	/**
	 * 固定庄家 下庄
	 * 
	 * @param session
	 * @param readData
	 * @throws Exception
	 */

	public synchronized static void interface_100210(IoSession session,
			Map<String, Object> readData) throws Exception {
		logger.I("固定庄家下庄,interfaceId -> 100203");
		String cid = (String) session.getAttribute(Cnst.USER_SESSION_CID);
		Integer interfaceId = StringUtils.parseInt(readData.get("interfaceId"));
		Integer roomId = StringUtils.parseInt(readData.get("roomSn"));
		Long userId = StringUtils.parseLong(readData.get("userId"));
		RoomResp room = RedisUtil.getRoomRespByRoomId(String.valueOf(roomId),cid);
		if(room.getState() == Cnst.PLAYER_STATE_GAME || !room.getZhuangPlayer().equals(userId)){
			illegalRequest(interfaceId, session);
		}
		List<Player> players = RedisUtil.getPlayerList(room,cid);
		JSONObject info = new JSONObject();
		info.put("reqState", Cnst.REQ_STATE_1);
		JSONObject result = getJSONObj(interfaceId, 1, info);
		ProtocolData pd = new ProtocolData(interfaceId, result.toJSONString());
		for (Player p : players) {
			if (!p.getUserId().equals(userId)) {
				IoSession se = session.getService().getManagedSessions()
						.get(p.getSessionId());
				if (se != null && se.isConnected()) {
					se.write(pd);
				}
			}
		}
 	}
	
	public static synchronized List<Long> getSameList(List<Long> list) {
		List<Long> list1 = new ArrayList<Long>();
		if (list == null) {
			return list1;
		} else {
			for (int i = 0; i < list.size(); i++) {
				list1.add(list.get(i));
			}
			return list1;
		}
	}
	public static synchronized List<Long> getAllExistList(List<Long> list1,List<Long> list2){
		List<Long> list3 = new ArrayList<Long>();
		if(list1 == null || list1.size() == 0){
			return list3;
		}
		if(list2 == null || list2.size() == 0){
			return list3;
		}
		for(int i=0;i<list1.size();i++){
			if(list2.contains(list1.get(i))){
				list3.add(list1.get(i));
			}
		}
		return list3;
	}
}
