package com.up72.game.dto.resp;

import java.io.Serializable;

public class Card implements Comparable<Card>,Serializable{
	
	 

	private int origin;//三位数牌数
	
	private int type;//黑红梅方
	
	private int symble;//KQJ987654321 牌点
	
	
	private int sortNum;//排序用数 1-10=self 11,12,13=10
	
	
	public int getSymble() {
		return symble;
	}

	public void setSymble(int symble) {
		this.symble = symble;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getOrigin() {
		return origin;
	}

	public void setOrigin(int origin) {
		this.origin = origin;
	}
		
	
	public int getSortNum() {
		return sortNum;
	}

	public void setSortNum(int sortNum) {
		this.sortNum = sortNum;
	}

	public void setChu(boolean isChu) {
		//只有A 可为储
		if(this.symble!=1)
			return;
		if(isChu==true){
			//设置储牌
			this.sortNum = 16;
		}else{
			//取消储牌
			this.sortNum = 14;
		}
		
	}
	
	public Card(int src){
		this.origin = src;
		this.type = src/100;
		this.symble = src%100;
		if(this.symble>10){
			this.sortNum = 10;
		}else{
			this.sortNum = this.symble;
		}
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + origin;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Card other = (Card) obj;
		if (origin != other.origin)
			return false;
		return true;
	}

	@Override
	public int compareTo(Card o) {
		int i = this.getSymble() - o.getSymble();//先按照大小排序
		
		if(i == 0){ 
            return this.getType() - o.getType();//如果大小相等了再花色进行排序  
        }  
		return -i;
	}

	public Card(){
		
	}

	@Override
	public String toString() {
		return "Card [origin=" + origin + ", type=" + type + ", symble="
				+ symble + ", sortNum=" + sortNum + "]";
	}
	
	
}
