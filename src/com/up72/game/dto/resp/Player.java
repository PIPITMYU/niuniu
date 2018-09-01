package com.up72.game.dto.resp;

import com.up72.game.model.User;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/6/26.
 */
public class Player extends User {

	private Integer roomId;// 房间密码，也是roomSn
	private Integer state;// out离开状态（断线）;inline正常在线；
	private List<Card> pais;// 用户手中当前的牌
	private Integer position;// 位置信息；详见Cnst
	private String ip;//所在服务器ip 需与加入房间ip一致
	private Integer score;// 玩家积分；初始为0，待定
	private Integer thisScore;//当局得分
	private String notice;// 跑马灯信息
	private Integer playStatus;// dating用户在大厅中; in刚进入房间，等待状态; prepared准备状态; 亮牌状态
	private Integer joinIndex;// 加入顺序
	private Long sessionId;
	private Integer yaZhu;//押注信息
	private Integer qiangZhuang;//抢庄信息
	private Integer niuNum;//本局玩家牌的大小
	
	public Integer getNiuNum() {
		return niuNum;
	}



	public void setNiuNum(Integer niuNum) {
		this.niuNum = niuNum;
	}



	private Long updateTime;//更新时间 间隔时间3天更新
	
	public void initPlayer(Integer roomId,Integer playStatus,Integer score){
		if(roomId == null){
			this.position = null;
			this.joinIndex = null;			
		}
		this.roomId = roomId;
		this.pais = new ArrayList<Card>();
		this.playStatus = playStatus;
		this.thisScore = 0;
		this.score = score;
		this.niuNum=-1;
		this.yaZhu = null;
		this.qiangZhuang = null;
	}
	
	
	
	public Long getUpdateTime() {
		return updateTime;
	}



	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}



	public Integer getRoomId() {
		return roomId;
	}


	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}

	
	public Integer getState() {
		return state;
	}


	public void setState(Integer state) {
		this.state = state;
	}


	public Integer getPosition() {
		return position;
	}


	public void setPosition(Integer position) {
		this.position = position;
	}




	public String getIp() {
		return ip;
	}


	public void setIp(String ip) {
		this.ip = ip;
	}




	public List<Card> getPais() {
		return pais;
	}



	public void setPais(List<Card> pais) {
		this.pais = pais;
	}



	public Integer getScore() {
		return score;
	}


	public void setScore(Integer score) {
		this.score = score;
	}


	public Integer getThisScore() {
		return thisScore;
	}


	public void setThisScore(Integer thisScore) {
		this.thisScore = thisScore;
	}


	public String getNotice() {
		return notice;
	}


	public void setNotice(String notice) {
		this.notice = notice;
	}


	public Integer getPlayStatus() {
		return playStatus;
	}


	public void setPlayStatus(Integer playStatus) {
		this.playStatus = playStatus;
	}


	public Integer getJoinIndex() {
		return joinIndex;
	}


	public void setJoinIndex(Integer joinIndex) {
		this.joinIndex = joinIndex;
	}


	public Long getSessionId() {
		return sessionId;
	}


	public void setSessionId(Long sessionId) {
		this.sessionId = sessionId;
	}




	//发牌
	public void dealCard(Card card){
		this.pais.add(card);
	}



	public Integer getYaZhu() {
		return yaZhu;
	}



	public void setYaZhu(Integer yaZhu) {
		this.yaZhu = yaZhu;
	}



	public Integer getQiangZhuang() {
		return qiangZhuang;
	}



	public void setQiangZhuang(Integer qiangZhuang) {
		this.qiangZhuang = qiangZhuang;
	}



	@Override
	public String toString() {
		return "Player [roomId=" + roomId + ", state=" + state + ", pais="
				+ pais + ", position=" + position + ", ip=" + ip + ", score="
				+ score + ", thisScore=" + thisScore + ", notice=" + notice
				+ ", playStatus=" + playStatus + ", joinIndex=" + joinIndex
				+ ", sessionId=" + sessionId + ", yaZhu=" + yaZhu
				+ ", qiangZhuang=" + qiangZhuang + ", niuNum=" + niuNum
				+ ", updateTime=" + updateTime + "]";
	}


	
	
	
}
