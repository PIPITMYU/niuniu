package com.up72.server.mina.utils.dcuse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.up72.game.dto.resp.Card;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.RoomResp;
import com.up72.game.model.Room;

public class GameUtil {
	//数组转list
	public static List<Long> changeList(Long[] ids) {
		List<Long> list = new ArrayList<Long>();
		for (Long l : ids) {
			list.add(l);
		}
		return list;
	}
	
	// 检测list是否为递增
	public static boolean checkListAdd(List<Integer> keyList) {
		int firstNum = keyList.get(0);
		for (int i = 1; i < keyList.size(); i++) {
			if (firstNum + i != keyList.get(i)) {
				return false;
			}
		}
		return true;
	}

	// 得到全部牌个数的map
	public static Map<Integer, Integer> getPaiMap(List<Card> cards) {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		if (cards == null || cards.size() == 0) {
			return map;
		}
		for (int i = 0; i < cards.size(); i++) {
			int num = cards.get(i).getSymble();
			if (map.get(num) == null) {
				map.put(num, 1);
			} else {
				map.put(num, map.get(num) + 1);
			}
		}
		return map;
	}
	
	//返回牛几
	//五小牛(16)>炸弹牛(15)>葫芦牛(14)>五花牛(13)>同花牛(12)>顺子牛(11)>牛牛(10)>牛九>牛八>牛七>牛六>牛五>牛四>牛三>牛二>牛一>无牛(-1)
	public static Integer getNiuNum(List<Card> cards,RoomResp room){
		Map<Integer,Integer> mapCard = getPaiMap(cards);
		Integer niuNum = -1;
		for(int m=0;m<=2;m++){
			for(int n=m+1;n<=3;n++){
				for(int z=n+1;z<=4;z++){
					if((cards.get(m).getSortNum()+cards.get(n).getSortNum()+cards.get(z).getSortNum())%10==0){
						Integer num = 0;
						for(int x=0;x<=4;x++){
							if(x!=m&&x!=n&&x!=z){
								num += cards.get(x).getSortNum();
							}
						}
						if(num%10 == 0){
							//牛牛
							niuNum = 10;
						}else if(num%10>niuNum){
							niuNum = num%10;
						}
					}
				}
			}
		}
		//五小牛
		a:if(room.getWuXiao() == 1){
			for(int i=0;i<=4;i++){
				if(cards.get(i).getSymble()>=5){
					break a;
				}
			}
			if(cards.get(0).getSortNum()+cards.get(1).getSortNum()+cards.get(2).getSortNum()
					+cards.get(3).getSortNum()+cards.get(4).getSortNum() <= 10){
				return 16;
			}
		}
		//炸弹牛
		if(room.getZhaDan() == 1){
			Iterator<Map.Entry<Integer, Integer>> it = mapCard.entrySet()
					.iterator();
			while (it.hasNext()) {
				Map.Entry<Integer, Integer> entry = it.next();
				if (entry.getValue() == 4) {
					return 15;
				}
			}

		}
		//葫芦牛
		if(room.getHuLu() == 1){
			Iterator<Map.Entry<Integer, Integer>> it = mapCard.entrySet()
					.iterator();
			Boolean has3 = false;
			Boolean has2 = false;
			while (it.hasNext()) {
				Map.Entry<Integer, Integer> entry = it.next();
				if (entry.getValue() == 3) {
					has3 = true;
				}
				if(entry.getValue() == 2){
					has2 = true;
				}
			}
			if(has3 == true && has2 == true){
				return 14;
			}
		}
		
		//五花牛
		b:if(room.getWuHua() == 1){
			for(int i=0;i<=4;i++){
				if(cards.get(i).getSymble()<=10){
					break b;
				}
			}
			return  13;
		}
		//同花牛
		c:if(room.getTongHua() == 1){
			int color = 0;
			for(int y=0;y<=4;y++){
				if(y==0){
					color = cards.get(y).getType();
				}
				if(color != cards.get(y).getType()){
					break c;
				}
			}
			return 12;
		}
		//顺子牛
		d:if(room.getShunZi() == 1){
			if (mapCard.size() != cards.size()) {
				break d;
			}
			Set<Integer> keySet = mapCard.keySet();
			List<Integer> keyList = new ArrayList<Integer>(keySet);
			if(checkListAdd(keyList)){
				return 11;
			}
		}
		return niuNum;
	}
	
	//list中随机返回一个Long
	public static Long getRondomId(List<Long> lists){
		return lists.get((int) (Math.random() * (lists.size())));
	}
	
	public static void main(String[] args) {
		RoomResp room = new RoomResp();
		room.setHuLu(1);
		room.setWuXiao(1);
		room.setWuHua(1);
		room.setZhaDan(1);
		room.setTongHua(1);
		room.setShunZi(1);
		Card c1 = new Card(112);
		Card c2 = new Card(211);
		Card c3 = new Card(313);
		Card c4 = new Card(111);
		Card c5 = new Card(108);
		List<Card> list = new ArrayList<Card>();
		list.add(c1);
		list.add(c2);
		list.add(c3);
		list.add(c4);
		list.add(c5);
		System.out.println(getNiuNum(list, room));
	}
}
