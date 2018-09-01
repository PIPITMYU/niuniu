/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.up72.game.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;




/**
 * 
 * 
 * @author up72
 * @version 1.0
 * @since 1.0
 */
public class Room implements java.io.Serializable{

   
	private Long id;
    private Integer roomId;
    private Long createId;
    private String createTime;

    private Integer isPlaying;
    private String userIds;


    private Integer roomType;//房间类型
    private Integer circleNum;//局数
    
    //开房选项
    private Integer type;//玩法
    private Integer diFen;//底分
    private Integer maxPeople;//开局人数
    private Integer zhuangNum;//上庄分数
    private Integer fanRule;//翻倍规则
    private Integer shunZi;//顺子牛
    private Integer huLu;//葫芦牛
    private Integer wuHua;//五花牛
    private Integer zhaDan;//炸弹牛
    private Integer wuXiao;//五小牛
    private Integer tongHua;//同花牛
    
    private Integer maxQZhuang;//最高抢庄分数
    
    
    private Integer clubId;// 俱乐部id
    private String ip;//当前房间所在服务器的ip


    //用户玩家id的集合
    
	public Integer getType() {
		return type;
	}


	public void setType(Integer type) {
		this.type = type;
	}



	public Long getCreateId() {
		return createId;
	}

	public void setCreateId(Long createId) {
		this.createId = createId;
	}


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }


    public Integer getRoomType() {
        return roomType;
    }

    public void setRoomType(Integer roomType) {
        this.roomType = roomType;
    }

    public Integer getCircleNum() {
        return circleNum;
    }

    public void setCircleNum(Integer circleNum) {
        this.circleNum = circleNum;
    }

    public Integer getClubId() {
		return clubId;
	}

	public void setClubId(Integer clubId) {
		this.clubId = clubId;
	}

	public int hashCode() {
        return new HashCodeBuilder()
            .append(getId())
            .toHashCode();
    }

    public boolean equals(Object obj) {
        if(obj instanceof Room == false) return false;
        if(this == obj) return true;
        Room other = (Room)obj;
        return new EqualsBuilder()
            .append(getId(),other.getId())
            .isEquals();
    }

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}


	public Integer getDiFen() {
		return diFen;
	}

	public void setDiFen(Integer diFen) {
		this.diFen = diFen;
	}

	public Integer getMaxPeople() {
		return maxPeople;
	}

	public void setMaxPeople(Integer maxPeople) {
		this.maxPeople = maxPeople;
	}

	public Integer getZhuangNum() {
		return zhuangNum;
	}

	public void setZhuangNum(Integer zhuangNum) {
		this.zhuangNum = zhuangNum;
	}

	public Integer getFanRule() {
		return fanRule;
	}

	public void setFanRule(Integer fanRule) {
		this.fanRule = fanRule;
	}

	public Integer getShunZi() {
		return shunZi;
	}

	public void setShunZi(Integer shunZi) {
		this.shunZi = shunZi;
	}

	public Integer getHuLu() {
		return huLu;
	}

	public void setHuLu(Integer huLu) {
		this.huLu = huLu;
	}

	public Integer getWuHua() {
		return wuHua;
	}

	public void setWuHua(Integer wuHua) {
		this.wuHua = wuHua;
	}

	public Integer getZhaDan() {
		return zhaDan;
	}

	public void setZhaDan(Integer zhaDan) {
		this.zhaDan = zhaDan;
	}

	public Integer getWuXiao() {
		return wuXiao;
	}

	public void setWuXiao(Integer wuXiao) {
		this.wuXiao = wuXiao;
	}

	public Integer getTongHua() {
		return tongHua;
	}

	public void setTongHua(Integer tongHua) {
		this.tongHua = tongHua;
	}

	public Integer getIsPlaying() {
		return isPlaying;
	}

	public void setIsPlaying(Integer isPlaying) {
		this.isPlaying = isPlaying;
	}

	public String getUserIds() {
		return userIds;
	}

	public void setUserIds(String userIds) {
		this.userIds = userIds;
	}


	public Integer getMaxQZhuang() {
		return maxQZhuang;
	}


	public void setMaxQZhuang(Integer maxQZhuang) {
		this.maxQZhuang = maxQZhuang;
	}



	
}

