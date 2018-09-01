package com.up72.game.dto.resp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;





import com.up72.game.constant.Cnst;
import com.up72.game.model.Room;
import com.up72.server.mina.bean.DissolveRoom;

/**
 * Created by Administrator on 2017/7/8.
 */
public class RoomResp extends Room {
	
	private List<Card> currentCardList = new ArrayList<Card>();//房间牌局
    private Long zhuangPlayer;//庄家
    private List<Long> xianPlayers;//闲家
    private Integer state;//本房间状态，0等待玩家入坐；1人满等待；2游戏中；3小结算
    private Integer lastNum;//房间剩余局数
    private Integer totalNum;//当前第几局
    private DissolveRoom dissolveRoom;//申请解散信息
    private Long xjst;//小局开始时间    
    private Integer playStatus;//游戏中房间状态
    private Integer createDisId;
    private Integer applyDisId;
    private Integer outNum;//请求大接口的玩家次数

    private Integer wsw_sole_main_id;//大接口id 暂时没用
    private Integer wsw_sole_action_id;//吃碰杠出牌发牌id
    
    private String openName;//房主id
    private Long[] playerIds;//玩家id集合
    private Integer xiaoJuNum;//每次小局，这个字段++，回放用
    

    private Integer qiangZhuangNum;//抢庄num
    private List<Long> qiangZhuangList;//抢庄集合
    private Integer maxQiangZhuang;//最高抢庄分
    private Integer yaZhuNum;//押注num
    private Integer liangPaiNum;//亮牌num
    private List<List<Integer>> xiaoJSInfo = new ArrayList<List<Integer>>();//回放用的小结算信息
    private List<Integer> positions;//玩家位置的数组


    public void initRoom(){
//    	initCurrentCardList();
    	this.liangPaiNum = null;
    	this.yaZhuNum = null;
    	this.maxQiangZhuang = null;
    	this.qiangZhuangList = null;
    	this.qiangZhuangNum = null;
    	this.playStatus = null;
    	this.xjst = null;
    }

	
	
	//初始化房间手牌
    public void initCurrentCardList() {
		List<Card> cards = new ArrayList<Card>();
		for (int i = 0; i < Cnst.CARD_ARRAY.length; i++) {
			cards.add(new Card(Cnst.CARD_ARRAY[i]));
		}
		this.currentCardList = cards;
    }
    


	public List<Integer> getPositions() {
		return positions;
	}



	public void setPositions(List<Integer> positions) {
		this.positions = positions;
	}


    //发牌
    public void dealCard(int num,Player player){
    	for(int i=1;i<=num;i++){
    		Card card = currentCardList.get(randomVal());
    		player.dealCard(card);
    		currentCardList.remove(card);
        }
    }
    //获取发牌随机数
    public int randomVal(){
		return (int) (Math.random() * (currentCardList.size()));
	}


	public Long getZhuangPlayer() {
		return zhuangPlayer;
	}


	public void setZhuangPlayer(Long zhuangPlayer) {
		this.zhuangPlayer = zhuangPlayer;
	}


	public Integer getState() {
		return state;
	}


	public void setState(Integer state) {
		this.state = state;
	}


	public Integer getLastNum() {
		return lastNum;
	}


	public void setLastNum(Integer lastNum) {
		this.lastNum = lastNum;
	}


	public Integer getTotalNum() {
		return totalNum;
	}


	public void setTotalNum(Integer totalNum) {
		this.totalNum = totalNum;
	}


	public DissolveRoom getDissolveRoom() {
		return dissolveRoom;
	}


	public void setDissolveRoom(DissolveRoom dissolveRoom) {
		this.dissolveRoom = dissolveRoom;
	}


	public Long getXjst() {
		return xjst;
	}


	public void setXjst(Long xjst) {
		this.xjst = xjst;
	}


	public Integer getPlayStatus() {
		return playStatus;
	}


	public void setPlayStatus(Integer playStatus) {
		this.playStatus = playStatus;
	}


	public Integer getCreateDisId() {
		return createDisId;
	}


	public void setCreateDisId(Integer createDisId) {
		this.createDisId = createDisId;
	}


	public Integer getApplyDisId() {
		return applyDisId;
	}


	public void setApplyDisId(Integer applyDisId) {
		this.applyDisId = applyDisId;
	}


	public Integer getOutNum() {
		return outNum;
	}


	public void setOutNum(Integer outNum) {
		this.outNum = outNum;
	}


	public Integer getWsw_sole_main_id() {
		return wsw_sole_main_id;
	}


	public void setWsw_sole_main_id(Integer wsw_sole_main_id) {
		this.wsw_sole_main_id = wsw_sole_main_id;
	}


	public Integer getWsw_sole_action_id() {
		return wsw_sole_action_id;
	}


	public void setWsw_sole_action_id(Integer wsw_sole_action_id) {
		this.wsw_sole_action_id = wsw_sole_action_id;
	}


	public String getOpenName() {
		return openName;
	}


	public void setOpenName(String openName) {
		this.openName = openName;
	}


	public Integer getXiaoJuNum() {
		return xiaoJuNum;
	}


	public void setXiaoJuNum(Integer xiaoJuNum) {
		this.xiaoJuNum = xiaoJuNum;
	}


	public List<Card> getCurrentCardList() {
		return currentCardList;
	}


	public void setCurrentCardList(List<Card> currentCardList) {
		this.currentCardList = currentCardList;
	}


	public List<Long> getXianPlayers() {
		return xianPlayers;
	}


	public void setXianPlayers(List<Long> xianPlayers) {
		this.xianPlayers = xianPlayers;
	}


	public Integer getQiangZhuangNum() {
		return qiangZhuangNum;
	}


	public void setQiangZhuangNum(Integer qiangZhuangNum) {
		this.qiangZhuangNum = qiangZhuangNum;
	}


	public Integer getYaZhuNum() {
		return yaZhuNum;
	}


	public void setYaZhuNum(Integer yaZhuNum) {
		this.yaZhuNum = yaZhuNum;
	}


	public Integer getLiangPaiNum() {
		return liangPaiNum;
	}


	public void setLiangPaiNum(Integer liangPaiNum) {
		this.liangPaiNum = liangPaiNum;
	}


	public List<Long> getQiangZhuangList() {
		return qiangZhuangList;
	}


	public void setQiangZhuangList(List<Long> qiangZhuangList) {
		this.qiangZhuangList = qiangZhuangList;
	}


	public Integer getMaxQiangZhuang() {
		return maxQiangZhuang;
	}


	public void setMaxQiangZhuang(Integer maxQiangZhuang) {
		this.maxQiangZhuang = maxQiangZhuang;
	}



	public Long[] getPlayerIds() {
		return playerIds;
	}



	public void setPlayerIds(Long[] playerIds) {
		this.playerIds = playerIds;
	}


	public List<List<Integer>> getXiaoJSInfo() {
		return xiaoJSInfo;
	}
	public void setXiaoJSInfo(List<List<Integer>> xiaoJSInfo) {
		this.xiaoJSInfo = xiaoJSInfo;
	}
	public void addXiaoJSInfo(List<Integer> xiaoJS){
		this.xiaoJSInfo.add(xiaoJS);
	}



	@Override
	public String toString() {
		return "RoomResp [currentCardList=" + currentCardList
				+ ", zhuangPlayer=" + zhuangPlayer + ", xianPlayers="
				+ xianPlayers + ", state=" + state + ", lastNum=" + lastNum
				+ ", totalNum=" + totalNum + ", dissolveRoom=" + dissolveRoom
				+ ", xjst=" + xjst + ", playStatus=" + playStatus
				+ ", createDisId=" + createDisId + ", applyDisId=" + applyDisId
				+ ", outNum=" + outNum + ", wsw_sole_main_id="
				+ wsw_sole_main_id + ", wsw_sole_action_id="
				+ wsw_sole_action_id + ", openName=" + openName
				+ ", playerIds=" + Arrays.toString(playerIds) + ", xiaoJuNum="
				+ xiaoJuNum + ", qiangZhuangNum=" + qiangZhuangNum
				+ ", qiangZhuangList=" + qiangZhuangList + ", maxQiangZhuang="
				+ maxQiangZhuang + ", yaZhuNum=" + yaZhuNum + ", liangPaiNum="
				+ liangPaiNum + ", xiaoJSInfo=" + xiaoJSInfo + ", positions="
				+ positions + "]";
	}
	
	
}
