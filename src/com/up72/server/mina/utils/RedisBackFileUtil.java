package com.up72.server.mina.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.up72.game.constant.Cnst;
import com.up72.game.dto.resp.Card;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.function.MessageFunctions;
import com.up72.server.mina.utils.redis.RedisUtil;

public class RedisBackFileUtil {
	public static void save(Integer interfaceId,RoomResp room,List<Player> players,Object infos,String cid){
		Integer xjn = room.getXiaoJuNum()==null? 1 :room.getXiaoJuNum();
		String redisKey = Cnst.get_REDIS_HUIFANG(cid).concat(room.getRoomId()+"_"
				+room.getCreateTime()+"_"+xjn);
		Map<String,Object> map = new HashMap<String, Object>();
		switch(interfaceId){
		case 100200:
			if (room.getState() == Cnst.ROOM_STATE_GAMIING) {				
				Map<String,Object> info = new HashMap<String, Object>();
				List<Map<String,Object>> userInfo = new ArrayList<Map<String,Object>>();
				for (Player p:players) {
					Map<String,Object> users = new HashMap<String, Object>();
					users.put("userId",p.getUserId());
					users.put("gender", p.getGender());
					users.put("userName", p.getUserName());
					users.put("userImg", p.getUserImg());
					users.put("position", p.getPosition());
					users.put("score", p.getScore());
					users.put("playStatus", Cnst.PLAYER_STATE_GAME);
					List<Card> cards = p.getPais();
					List<Integer> pais = new ArrayList<Integer>();
					for(Card c:cards){
						pais.add(c.getOrigin());
					}
					users.put("pais", pais);
					userInfo.add(users);						
				}
				map.put("interfaceId", interfaceId);
				info.put("userInfo", userInfo);
				info.put("roomInfo", MessageFunctions.getRoomInfo(room));
				map.put("jsonStr", info);
				RedisUtil.rpush(redisKey, Cnst.HUIFANG_LIFE_TIME, JSONObject.toJSONString(map));
			}
			break;
		case 100202:
			map.put("interfaceId", interfaceId);
			map.put("jsonStr", infos);
			RedisUtil.rpush(redisKey,null, JSONObject.toJSONString(map));
			break;
		case 100207:
			map.put("interfaceId", interfaceId);
			map.put("jsonStr", infos);
			RedisUtil.rpush(redisKey,null, JSONObject.toJSONString(map));
			break;
		case 100208:
			map.put("interfaceId", interfaceId);
			map.put("jsonStr", infos);
			RedisUtil.rpush(redisKey,null, JSONObject.toJSONString(map));
			break;
		case 100209:
			map.put("interfaceId", interfaceId);
			map.put("jsonStr", infos);
			RedisUtil.rpush(redisKey,null, JSONObject.toJSONString(map));
			break;
		case 100102:
			map.put("interfaceId", interfaceId);
			map.put("jsonStr", infos);
			RedisUtil.rpush(redisKey,null, JSONObject.toJSONString(map));
			break;
		case 100103:
			if(room.getDissolveRoom()!=null){
				map.put("interfaceId", interfaceId);
				map.put("jsonStr", infos);
				RedisUtil.rpush(redisKey,null, JSONObject.toJSONString(map));
			}
			break;
		}
	}
	public static boolean write(RoomResp room,String cid){
		boolean result = true;
		FileWriter fw = null;
		BufferedWriter w = null;
		Integer xjn = room.getXiaoJuNum()==null?1:room.getXiaoJuNum();
		String redisKey = Cnst.get_REDIS_HUIFANG(cid).concat(room.getRoomId()+"_"
				+room.getCreateTime()+"_"+xjn);
		try {			
			File parent = new File(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH));
			if (!parent.exists()) {
				parent.mkdirs();
			}
			if(!RedisUtil.exists(redisKey)){
				return false;
			}
			Date d = new Date(Long.valueOf(room.getCreateTime()));
			String time_prefix = new SimpleDateFormat("yyyyMMddHHmmss").format(d);
			String fineName = new StringBuffer().append(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH))
					.append(time_prefix)
					.append("-")
					.append(room.getRoomId())
					.append("-")
					.append(xjn)
					.append(".txt").toString();
			File file = new File(fineName);
			if (!file.exists()) {//说明是新开的小局
				file.createNewFile();
			}else{
				return false;
			}
			fw = new FileWriter(file,true);
			w = new BufferedWriter(fw);
			
			List<String> info = RedisUtil.lrange(redisKey, 0, -1);
			w.write("{\"state\":1,\"info\":");
			w.newLine();
			w.write(info.toString());
			w.newLine();
			w.write("}");
			w.flush();
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}finally{
			try {
				if (w!=null) {
					w.close();
				}
				if (fw!=null) {
					fw.close();
				}
				//FIXME 删除redis记录
				RedisUtil.deleteByKey(redisKey);
			} catch (Exception e2) {
				
			}
		}
		
		return result;
	}
	
	/**
	 * 每次启动服务，清零所有回放文件
	 */
	public static void deleteAllRecord(){
		try {
			File path = new File(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH));
			File[] files = path.listFiles();

			if (files!=null&&files.length>0) {
				for(int i=0;i<files.length;i++){
					File f = files[i];
					f.delete();
				}
			}
			path.delete();
			System.out.println("回放文件清理完成！");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
    
	}
	//文件格式为yyyyMMddHHmmss-roomId-xiaoJuNum.txt
		public static void deletePlayRecord(){
			try {
				File path = new File(Cnst.FILE_ROOT_PATH.concat(Cnst.BACK_FILE_PATH));
				File[] files = path.listFiles();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				long currentDate = new Date().getTime();
				if (files!=null&&files.length>0) {
					for(int i=0;i<files.length;i++){
						File f = files[i];
						String name = f.getName();
						String dateStr = name.split("_")[0];
						Date createDate = sdf.parse(dateStr);
						if ((currentDate-createDate.getTime())>=Cnst.BACKFILE_STORE_TIME) {
							f.delete();
						}else{
							break;
						}
					}
				}
				System.out.println("回放文件清理完成！");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
	    }
}
