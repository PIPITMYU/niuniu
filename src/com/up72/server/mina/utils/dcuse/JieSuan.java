package com.up72.server.mina.utils.dcuse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Card;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.function.TCPGameFunctions;
import com.up72.server.mina.utils.RedisBackFileUtil;
import com.up72.server.mina.utils.redis.RedisUtil;

public class JieSuan {
	public static void xiaoJieSuan(String roomId,String cid) {
		RoomResp room = RedisUtil.getRoomRespByRoomId(roomId,cid);
		List<Player> players = RedisUtil.getPlayerList(room,cid);
		//设置玩家的当局分数
		getThisScore(players, room);
		// 修改圈数
		room.setLastNum(room.getLastNum() - 1);
		room.setTotalNum(room.getTotalNum() + 1);
		// 初始化房间
		room.initRoom();
		if(room.getType().equals(Cnst.ROOM_PALYTYPE_NIUNIU)){//类型为牛牛上庄
			Player maxScorePlayer = getMaxScorePlayer(players);
			if(maxScorePlayer.getNiuNum()<10){//没牛的时候，庄不改变
			}else{
				room.setZhuangPlayer(maxScorePlayer.getUserId());
			}
			List<Long> xianList = new ArrayList<Long>();
			Long[] playIds = room.getPlayerIds();
			for(Long id:playIds){
				if(!id.equals(room.getZhuangPlayer())){
					xianList.add(id);
				}
			}
			room.setXianPlayers(xianList);
		}
		List<Integer> xiaoJS = new ArrayList<Integer>();
		for (Player p : players) {
			p.setPlayStatus(Cnst.PLAYER_STATE_IN);
			xiaoJS.add(p.getThisScore());
		}
		room.addXiaoJSInfo(xiaoJS);
		RedisUtil.updateRedisData(room, null,cid);
		RedisUtil.setPlayersList(players,cid);
		//获取庄家
		Long zhuangUserId = room.getZhuangPlayer();
		Player zhuangPlayer=null;
		if(zhuangUserId!=null){
			zhuangPlayer = RedisUtil.getPlayerByUserId(zhuangUserId.toString(),cid);
		}

		
		// 写入文件
		List<Map<String, Object>> userInfos = new ArrayList<Map<String, Object>>();
		for (Player p : players) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("userId", p.getUserId());
			List<Integer> pais = new ArrayList<Integer>();
			for (Card c : p.getPais()) {
				pais.add(c.getOrigin());
			}	
			map.put("score", p.getThisScore());
			if(zhuangPlayer!=null && zhuangPlayer.getUserId().equals(p.getUserId())){
				map.put("zhuang", 1);//1是庄 0是闲
			}else{
				map.put("zhuang", 0);//1是庄 0是闲
			}
			userInfos.add(map);
		}
		JSONObject info = new JSONObject();
		info.put("lastNum", room.getLastNum());
		info.put("userInfo", userInfos);
		RedisBackFileUtil.save(100102, room, null,info,cid);
		//小结算 存入一次回放
		RedisBackFileUtil.write(room,cid);
			
		//当是固定庄家的时候并且底分不是0
		if (room.getType().equals(Cnst.ROOM_PALYTYPE_GUDING) && !room.getZhuangNum().equals(0)) {
			if(zhuangPlayer.getScore()+room.getZhuangNum()<=0){
				// 最后一局 大结算
				room = RedisUtil.getRoomRespByRoomId(roomId,cid);
				room.setState(Cnst.ROOM_STATE_YJS);
				// 这里更新数据库吧
				TCPGameFunctions.updateDatabasePlayRecord(room,cid);	
			}else if (room.getXiaoJuNum() == room.getCircleNum()) {
				// 最后一局 大结算
				room = RedisUtil.getRoomRespByRoomId(roomId,cid);
				room.setState(Cnst.ROOM_STATE_YJS);
				// 这里更新数据库吧
				TCPGameFunctions.updateDatabasePlayRecord(room,cid);
			}
		} 
		if (room.getXiaoJuNum() == room.getCircleNum()) {
			// 最后一局 大结算
			room = RedisUtil.getRoomRespByRoomId(roomId,cid);
			room.setState(Cnst.ROOM_STATE_YJS);
			// 这里更新数据库吧
			TCPGameFunctions.updateDatabasePlayRecord(room,cid);
		}
	}

	/**
	 * 获取本局当前分数
	 * 
	 * @param players
	 * @param b
	 */
	private static void getThisScore(List<Player> players,
			RoomResp room) {
		//根据牌型获取的倍数
		Integer type = room.getType();
		Integer beiShu;
		//玩家此局的分数
		Integer score;
		Player zhuangPlayer=new Player();
		for (Player player : players) {
			if(player.getUserId().equals(room.getZhuangPlayer())){
				zhuangPlayer=player;
				break;
			}
		}
		if (!type.equals(5)) {// 有庄
			Integer zhuangNiuNum = zhuangPlayer.getNiuNum();
			Integer zhuangfen = zhuangPlayer.getQiangZhuang();
			//玩家押注分，如果不压为-1
			Integer yazhuFen=1;
			if(zhuangfen==null ){
				zhuangfen=1;
			}
			if(type.equals(3)){
				zhuangfen =1;
			}
			// 分别对比庄玩家和闲玩家的牌
			for (Player player : players) {
				if (!player.getUserId().equals(zhuangPlayer.getUserId())) {// 这个人不是庄
//					Integer xianNiuNum2 = GameUtil.getNiuNum(player.getPais(),
//							room);
					Integer xianNiuNum2 = player.getNiuNum();
					if (zhuangNiuNum > xianNiuNum2) {// 庄赢
						beiShu = getBeiShuByNiuNum(zhuangNiuNum,
								room.getFanRule());
						yazhuFen=player.getYaZhu();
						if(yazhuFen.equals(-1)){
							yazhuFen=1;
						}
						score = zhuangfen
								* yazhuFen * beiShu;
						player.setThisScore(-score);// 闲家减分
						//
						zhuangPlayer.setThisScore(zhuangPlayer.getThisScore()
								+ score);// 庄加分
						player.setScore(player.getScore()-score);
						zhuangPlayer.setScore(zhuangPlayer.getScore()+score);
					} else if (zhuangNiuNum == xianNiuNum2) {// 牌大小相等
						// 根据最大的牌对比牌的大小
						if (getMaxForSameNum(zhuangPlayer, player) < 0) {// 说名庄牌大
							beiShu = getBeiShuByNiuNum(zhuangNiuNum,
									room.getFanRule());
							yazhuFen=player.getYaZhu();
							if(yazhuFen.equals(-1)){
								yazhuFen=1;
							}
							score = zhuangfen
									* yazhuFen * beiShu;
							player.setThisScore(-score);// 闲家减分
							zhuangPlayer.setThisScore(zhuangPlayer
									.getThisScore() + score);// 庄加分
							player.setScore(player.getScore()-score);
							zhuangPlayer.setScore(zhuangPlayer.getScore()+score);
						} else {// 说明闲牌大
							beiShu = getBeiShuByNiuNum(player.getNiuNum(),
									room.getFanRule());
							yazhuFen=player.getYaZhu();
							if(yazhuFen.equals(-1)){
								yazhuFen=1;
							}
							score = zhuangfen
									* yazhuFen * beiShu;
							player.setThisScore(score);// 闲家减分
							zhuangPlayer.setThisScore(zhuangPlayer
									.getThisScore() - score);// 庄加分
							player.setScore(player.getScore()+score);
							zhuangPlayer.setScore(zhuangPlayer.getScore()-score);
						}

					} else {// 闲家赢
						beiShu = getBeiShuByNiuNum(player.getNiuNum(),
								room.getFanRule());
						yazhuFen=player.getYaZhu();
						if(yazhuFen.equals(-1)){
							yazhuFen=1;
						}
						score = zhuangfen
								* yazhuFen * beiShu;
						player.setThisScore(score);// 闲家减分
						zhuangPlayer.setThisScore(zhuangPlayer.getThisScore()
								- score);// 庄加分
						player.setScore(player.getScore()+score);
						zhuangPlayer.setScore(zhuangPlayer.getScore()-score);
						
					}
				}
			}

			// 根据返回牌型+牌型分
			// 赢的人+num分，输的人-num分 (num=牌型分*压庄分*加注分)

		} else {// 没有庄
			Player maxPlayer = getMaxScorePlayer(players);
			beiShu = getBeiShuByNiuNum(maxPlayer.getNiuNum(), room.getFanRule());
			Integer winYaZhu = maxPlayer.getYaZhu();
			if(winYaZhu.equals(-1)){
				winYaZhu=1;
			}
			Integer shuYaZhu;
			for (Player player2 : players) {
				if(!player2.getUserId().equals(maxPlayer.getUserId())){//输的人
					shuYaZhu=player2.getYaZhu();
					if(shuYaZhu.equals(-1)){
						shuYaZhu=1;
					}
					score=winYaZhu*shuYaZhu*beiShu;
					//输的人扣分
					player2.setThisScore(-score);
					//赢的人加分
					maxPlayer.setThisScore(maxPlayer.getThisScore()+score);;
					player2.setScore(player2.getScore()-score);
					maxPlayer.setScore(maxPlayer.getScore()+score);
				}
			}
		}
	}

	/**
	 * 五小牛(16)>炸弹牛(15)>葫芦牛(14)>五花牛(13)>同花牛(12)>顺子牛(11)>牛牛(10)>牛九>牛八>牛七>牛六>牛五>牛四>
	 * 牛三>牛二>牛一>无牛(-1) 根据牛的大小获取要翻的倍数
	 * 
	 * @param zhuangNiuNum
	 * @return
	 */
	private static Integer getBeiShuByNiuNum(Integer niuNum, Integer fanRule) {
		Integer beiShu;
		// 获取特殊牌型的倍数
		if (niuNum.equals(16)) {
			beiShu = 8;
		} else if (niuNum.equals(15)) {
			beiShu = 6;
		} else if (niuNum.equals(14)) {
			beiShu = 6;
		} else if (niuNum.equals(13)) {
			beiShu = 5;
		} else if (niuNum.equals(12)) {
			beiShu = 5;
		} else if (niuNum.equals(11)) {
			beiShu = 5;
		}
		// 获取普通类型倍数
		if (fanRule.equals(0)) {
			if (niuNum.equals(10)) {
				beiShu = 4;
			} else if (niuNum.equals(9)) {
				beiShu = 3;
			} else if (niuNum.equals(8)) {
				beiShu = 2;
			} else if (niuNum.equals(7)) {
				beiShu = 2;
			} else {
				beiShu = 1;
			}
		} else {
			if (niuNum.equals(10)) {
				beiShu = 3;
			} else if (niuNum.equals(9)) {
				beiShu = 2;
			} else if (niuNum.equals(8)) {
				beiShu = 2;
			} else {
				beiShu = 1;
			}
		}
		return beiShu;
	}

	/**
	 * 获取牌数最大的玩家
	 * 
	 * @param players
	 * @return
	 */
	private static Player getMaxScorePlayer(List<Player> players) {
		Player maxPlayer = players.get(0);
		int maxNum = maxPlayer.getNiuNum();
		for (int i = 1; i < players.size(); i++) {
			if (maxNum < players.get(i).getNiuNum()) {
				maxPlayer = players.get(i);
				maxNum = players.get(i).getNiuNum();
			} else if (maxNum == players.get(i).getNiuNum()) {
				if(getMaxForSameNum(maxPlayer, players.get(i))>0){
					maxPlayer=players.get(i);
				}
			}
		}
		return maxPlayer;
	}

	/**
	 * 当牌大小相同时，根据颜色等比较大小
	 * 
	 * @param maxPlayer
	 * @param player
	 * @return
	 */
	private static int getMaxForSameNum(Player player1, Player player2) {
		List<Card> pais = player1.getPais();
		Card card1 = getMaxCardsInShouPai(pais);
		List<Card> pais2 = player2.getPais();
		Card card2 = getMaxCardsInShouPai(pais2);
		// card1大返回负数，否则返回正数
		int compareTo = card1.compareTo(card2);
		return compareTo;
	}

	public static void main(String[] args) {
//		Card card1 = new Card();
//		card1.setType(1);
//		card1.setSymble(11);
//		Card card2 = new Card();
//		card2.setType(1);
//		card2.setSymble(12);
//		int i = card1.compareTo(card2);
		short s1=1;
		System.out.println(s1);
	}

	/**
	 * 获取最大的牌
	 * 
	 * @param pais
	 * @return
	 */
	private static Card getMaxCardsInShouPai(List<Card> pais) {
		Collections.sort(pais);
		return pais.get(0);
	}

}
