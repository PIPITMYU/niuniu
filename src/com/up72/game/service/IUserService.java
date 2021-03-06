/*
 * Powered By [up72-framework]
 * Web Site: http://www.up72.com
 * Since 2006 - 2017
 */

package com.up72.game.service;

import java.util.List;
import java.util.Map;

import com.up72.game.dto.resp.Feedback;
import com.up72.game.dto.resp.Player;
import com.up72.game.dto.resp.PlayerRecord;
import com.up72.game.model.PlayerMoneyRecord;
import com.up72.game.model.SystemMessage;
import com.up72.game.model.User;


/**
 * 接口
 * 
 * @author up72
 * @version 1.0
 * @since 1.0
 */
public interface IUserService {

    void save(Player user);

    void update(User user);
    
    void updateInfo(Player player);

    Player getById(java.lang.Long id);

    Player getByOpenId(String openId,String cid);

    List<PlayerRecord> findPlayerRecordByUserId(java.lang.Long userId,Integer start,Integer limit);

    void userFeedback(Feedback feedback);

    void updateMoney(Integer money,String userId,String cid);

    Integer isExistUserId(String userId);

    void updateUserAgree(Long userId,String cid);

    String getNotice(String cid);

    List<SystemMessage> getSystemMessage(java.lang.Long userId, Integer start, Integer limit);

    void insertPlayRecord(Map<String,String> playRecord);
    
    void insertPlayerMoneyRecord(PlayerMoneyRecord mr);
    
    String getConectUs();
    
    Integer getUserMoneyByUserId(Long userId,String cid);
    
    void updateIpAndLastTime(String openId,String ip,String cid);
    
    String findIpByUserId(Long userId);
    
    Integer findTotalGameNum(Long userId);
}
