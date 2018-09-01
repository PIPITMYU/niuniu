package com.up72.server.mina.main;


import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.up72.game.dto.resp.Card;
import com.up72.game.dto.resp.RoomResp;
import com.up72.server.mina.utils.dcuse.GameUtil;
import com.up72.server.mina.utils.redis.MyRedis;
import com.up72.server.mina.utils.redis.RedisUtil;

public class test {
	public static void main1(String[] args) {
		Card c1 = new Card(110);
		Card c2 = new Card(111);
		Card c3 = new Card(112);
		Card c4 = new Card(111);
		Card c5 = new Card(107);
		List<Card> cards = new ArrayList<Card>();
		cards.add(c1);
		cards.add(c2);
		cards.add(c3);
		cards.add(c4);
		cards.add(c5);
		
		System.out.print(GameUtil.getNiuNum(cards, new RoomResp()));
	}
	static String[] words = "BoGe sha bi !".split("");//自定义，英文的

	public static void printStr() {
		int num = 0;
		for (float y = (float) 1.5; y > -1.5; y -= 0.1) {
			for (float x = (float) -1.5; x < 1.5; x += 0.05) {
				float a = x * x + y * y - 1;
				if ((a * a * a - x * x * y * y * y) <= 0.0) {
					if (num == words.length) {
						num = 0;
					}
					System.out.print(words[num++]);

				} else
					System.out.print(" ");
			}
			System.out.print("\n");
		}
	}
	public static void main(String[] args) {
		printStr();
	}
}
