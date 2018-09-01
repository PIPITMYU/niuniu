package com.up72.game.constant;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.up72.server.mina.utils.ProjectInfoPropertyUtil;

/**
 * 常量
 */
public class Cnst {
	
	// 获取项目版本信息
    public static final String version = ProjectInfoPropertyUtil.getProperty("project_version", "1.5");
    public static Boolean isTest = true;//是否是测试环境


    public static final String p_name = ProjectInfoPropertyUtil.getProperty("p_name", "wsw_X1");
    public static final String o_name = ProjectInfoPropertyUtil.getProperty("o_name", "u_consume");
    public static final String gm_url = ProjectInfoPropertyUtil.getProperty("gm_url", "");
    
    //回放配置
    public static final String BACK_FILE_PATH = ProjectInfoPropertyUtil.getProperty("backFilePath", "1.5");
    public static final String FILE_ROOT_PATH = ProjectInfoPropertyUtil.getProperty("fileRootPath", "1.5");
    public static String SERVER_IP = getLocalAddress();
    public static String HTTP_URL = "http://".concat(Cnst.SERVER_IP).concat(":").concat(ProjectInfoPropertyUtil.getProperty("httpUrlPort", "8086")).concat("/");
    public static String getLocalAddress(){
		String ip = "";
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ip;
	}
    
    
    public static final String cid = ProjectInfoPropertyUtil.getProperty("cid", "22");;
    //redis配置
    public static final String REDIS_HOST = ProjectInfoPropertyUtil.getProperty("redis.host", "");
    public static final String REDIS_PORT = ProjectInfoPropertyUtil.getProperty("redis.port", "");
    public static final String REDIS_PASSWORD = ProjectInfoPropertyUtil.getProperty("redis.password", "");

    //mina的端口
    public static final String MINA_PORT = ProjectInfoPropertyUtil.getProperty("mina.port", "");
    
    //用户服务地址
    public static final String GETUSER_URL = ProjectInfoPropertyUtil.getProperty("getUser_url","");
    //mina
    public static final int Session_Read_BufferSize = 2048 * 10;
    public static final int Session_life = 60;
    public static final int WriteTimeOut = 500;
    
    public static final String rootPath = ProjectInfoPropertyUtil.getProperty("rootPath", "");

    public static final long HEART_TIME = 9000;//心跳时间，前端定义为8s，避免网络问题延时，后端计算是以9s计算
    public static final int MONEY_INIT = 6;//初始赠送给用户的房卡数
    //开房选项中的是否
    public static final int YES = 1;
    public static final int NO = 0;
    

    public static final long ROOM_OVER_TIME = 5*60*60*1000;//房间定时5小时解散
    public static final long ROOM_CREATE_DIS_TIME = 40*60*1000;//创建房间之后，40分钟解散
    public static final long ROOM_DIS_TIME = 5*60*1000;//玩家发起解散房间之后，5分钟自动解散
    public static final String CLEAN_3 = "0 0 3 * * ?";
    public static final String CLEAN_EVERY_HOUR = "0 0 0/1 * * ?";
    public static final String COUNT_EVERY_TEN_MINUTE = "0 0/1 * * * ?";
    public static final long BACKFILE_STORE_TIME = 3*24*60*60*1000;//回放文件保存时间
   
    
    //玩家数据更新间隔时间
    public static long updateDiffTime =3*24*3600*1000;
    //测试时间
//    public static final long ROOM_OVER_TIME = 60*1000;//
//    public static final long ROOM_CREATE_DIS_TIME = 20*1000;
//    public static final long ROOM_DIS_TIME = 10*1000;
//	public static final String CLEAN_3 = "0/5 * * * * ?";
//	public static final String CLEAN_EVERY_HOUR = "0/5 * * * * ?";
//    public static final String COUNT_EVERY_TEN_MINUTE = "0/1 * * * * ?";
//    public static final long BACKFILE_STORE_TIME = 60*1000;//回放文件保存时间
    
    

    public static final int ROOM_LIFE_TIME_CREAT = (int) ((ROOM_OVER_TIME/1000)+200);//创建时，5小时，redis用
    public static final int ROOM_LIFE_TIME_DIS = (int) ((ROOM_DIS_TIME/1000)+200);//解散房间时，300s，redis用
    public static final int ROOM_LIFE_TIME_COMMON = (int) ((ROOM_CREATE_DIS_TIME/1000)+200);//正常开局存活时间，redis用
    public static final int OVERINFO_LIFE_TIME_COMMON = (int) (10*60);//大结算 overInfo 存活时间
    public static final int PLAYOVER_LIFE_TIME =3*24*60*60;//战绩保存时间
    public static final int HUIFANG_LIFE_TIME = 30*60;//30分钟 回放写入文件后删除
    
    public static final int DIS_ROOM_RESULT = 1;

    public static final int DIS_ROOM_TYPE_1 = 1;//创建房间40分钟解散类型
    public static final int DIS_ROOM_TYPE_2 = 2;//玩家点击解散房间类型

    public static final int PAGE_SIZE = 10;
    



    public static final String USER_SESSION_USER_ID = "user_id";
    public static final String USER_SESSION_IP = "ip";
    public static final String USER_SESSION_CID = "cid";
    
    

    //房间信息中的state
    // 1等待玩家入坐；2游戏中；3小结算
    public static final int ROOM_STATE_CREATED = 1;
    public static final int ROOM_STATE_GAMIING = 2;
    public static final int ROOM_STATE_XJS = 3;
    public static final int ROOM_STATE_YJS = 4;
    //房间中的playState state = 2时有效
    public static final int ROOM_PALYSTATE_QIANGZHUANG = 1;//抢庄
    public static final int ROOM_PALYSTATE_YAZHU = 2;//押注
    public static final int ROOM_PALYSTATE_LIANGPAI = 3;//亮牌

    //房间类型
    public static final int ROOM_TYPE_1 = 1;//房主模式
    public static final int ROOM_TYPE_2 = 2;//自由模式
    public static final int ROOM_TYPE_3 = 3;//AA

    public static final int ROOM_PALYTYPE_NIUNIU = 1; //牛牛上庄
    public static final int ROOM_PALYTYPE_GUDING = 2; //固定上庄
    public static final int ROOM_PALYTYPE_ZIYOU = 3;//自由
    public static final int ROOM_PALYTYPE_MINGPAI = 4;//明牌
    public static final int ROOM_PALYTYPE_TONGBI = 5;//通比


    //开房的局数对应消耗的房卡数
    public static final Map<Integer,Integer> moneyMap = new HashMap<>();
    static {
        moneyMap.put(10,3);
        moneyMap.put(20,6);
    }
    //玩家在线状态 state 
    public static final int PLAYER_LINE_STATE_INLINE = 1;//"inline"
    public static final int PLAYER_LINE_STATE_OUT = 2;//"out"
    
    //玩家进入或退出代开房间
    public static final int PLAYER_EXTRATYPE_ADDROOM = 1;//进入
    public static final int PLAYER_EXTRATYPE_EXITROOM = 2;//退出
    public static final int PLAYER_EXTRATYPE_JIESANROOM = 3;//解散
    //玩家状态
    public static final int PLAYER_STATE_DATING = 1;//"dating"
    public static final int PLAYER_STATE_IN = 2;//"in"
    public static final int PLAYER_STATE_PREPARED = 3;//"prepared"
    public static final int PLAYER_STATE_GAME = 4;//"game"
    public static final int PLAYER_STATE_OVER = 5;//"over"
    public static final int PLAYER_STATE_XJS = 6;//"xjs"
    

    //请求状态
    public static final int REQ_STATE_FUYI = -1;//敬请期待
    public static final int REQ_STATE_0 = 0;//非法请求
    public static final int REQ_STATE_1 = 1;//正常
    public static final int REQ_STATE_2 = 2;//余额不足
    public static final int REQ_STATE_3 = 3;//已经在其他房间中
    public static final int REQ_STATE_4 = 4;//房间不存在
    public static final int REQ_STATE_5 = 5;//房间人员已满
    public static final int REQ_STATE_6 = 6;//游戏中，不能退出房间
    public static final int REQ_STATE_7 = 7;//有玩家拒绝解散房间
    public static final int REQ_STATE_8 = 8;//玩家不存在（代开模式中，房主踢人用的）
    public static final int REQ_STATE_9 = 9;//接口id不符合，需请求大接口
    public static final int REQ_STATE_10 = 10;//代开房间创建成功
    public static final int REQ_STATE_11 = 11;//已经代开过10个了，不能再代开了
    public static final int REQ_STATE_12 = 12;//房间存在超过24小时解散的提示
    public static final int REQ_STATE_13 = 13;//房间40分钟未开局解散提示
    public static final int REQ_STATE_14 = 14;//ip不一致


    //退出类型
    public static final int EXIST_TYPE_EXIST = 1;//"exist"
    public static final int EXIST_TYPE_DISSOLVE = 2;//"dissolve";

    // 项目根路径
    public static String ROOTPATH = "";
    
    
    public static final String splitStr = "_";
    
    public static final String REDIS_PREFIX = ProjectInfoPropertyUtil.getProperty("redis.prefix","");
    /**
     * 房间信息
     */
    public static final String REDIS_PREFIX_ROOMMAP = REDIS_PREFIX + "_ROOM_MAP_";//房间信息  
    
    public static String get_REDIS_PREFIX_ROOMMAP(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_PREFIX_ROOMMAP.concat(cId).concat(splitStr);
    }
    
    /**
     * openId - userId 对照表
     */
    public static final String REDIS_PREFIX_OPENIDUSERMAP = REDIS_PREFIX + "_OPENID_USERID_MAP_";//openId-user数据
    
    public static String get_REDIS_PREFIX_OPENIDUSERMAP(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	
    	return REDIS_PREFIX_OPENIDUSERMAP.concat(cId).concat(splitStr);
    }
    
    /**
     * userId  - Player对象对照表
     */
    public static final String REDIS_PREFIX_USER_ID_USER_MAP = REDIS_PREFIX + "_USER_ID_USER_MAP_";//通过userId获取用户
    
    public static String get_REDIS_PREFIX_USER_ID_USER_MAP(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_PREFIX_USER_ID_USER_MAP.concat(cId).concat(splitStr);
    }
    
    
    /**
     * 通知  没有对照
     */
    public static final String NOTICE_KEY = REDIS_PREFIX + "_NOTICE_KEY";
    
    public static String get_NOTICE_KEY(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return NOTICE_KEY.concat(splitStr).concat(cId);
    }
    
    /**
     * 所有需要重启以后删除的对照表
     */
    public static final String PROJECT_PREFIX = REDIS_PREFIX + "_*";
    
    /**
     * IP - NUM 对应IP在线人数
     */
    public static final String REDIS_ONLINE_NUM_COUNT = REDIS_PREFIX + "_ONLINE_NUM_";
    
    public static String get_REDIS_ONLINE_NUM_COUNT(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_ONLINE_NUM_COUNT.concat(cId).concat(splitStr);
    }
    
    /**
     * hash结构 全服一个  filed - value 是 userId - timer 玩家ID和时间戳对照表
     */
    public static final String REDIS_HEART_PREFIX = REDIS_PREFIX + "_HEART_USERS_MAP_";
    
    public static String get_REDIS_HEART_PREFIX(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_HEART_PREFIX.concat(cId);
    }
    
    /**
     * roomId - 回放记录    对照表
     */
    public static final String REDIS_HUIFANG = REDIS_PREFIX + "_HUIFANG_STRING_";
    
    public static String get_REDIS_HUIFANG(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_HUIFANG.concat(cId).concat(splitStr);
    }
    
    
    /**
     * 玩家代开的记录  hashMap 结构 key为 房间ID value为1 没有意义
     */
    public static final String ROOM_DAIKAI_KEY = REDIS_PREFIX + "_DAIKAI_KEY_";
	
	public static String get_ROOM_DAIKAI_KEY(String cId){
		if(cId == null || "".equals(cId))
    		return null;
		return ROOM_DAIKAI_KEY.concat(cId).concat(splitStr);
	}
    //NIUNIU
    public static final String REDIS_RECORD_PREFIX = ProjectInfoPropertyUtil.getProperty("redis.record_prefix","");
    
    //这个字段不清理，存放玩家战绩，定时任务定期清理内容
    /**
     * 战绩记录 key为room-createTime  value为整个房间记录
     */
    public static final String REDIS_PLAY_RECORD_PREFIX = REDIS_RECORD_PREFIX + "_PLAY_RECORD_";//房间战绩
    
    
    public static String get_REDIS_PLAY_RECORD_PREFIX(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_PLAY_RECORD_PREFIX.concat(cId).concat(splitStr);
    }
    
    /**
     * list结构  key为 userId value是列表 表示自己玩过的房间-createTime
     */
    public static final String REDIS_PLAY_RECORD_PREFIX_ROE_USER = REDIS_RECORD_PREFIX + "_PLAY_RECORD_FOR_USER_";//玩家字段
    
    public static String get_REDIS_PLAY_RECORD_PREFIX_ROE_USER(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_PLAY_RECORD_PREFIX_ROE_USER.concat(cId).concat(splitStr);
    }
    /**
     * list 结构 key为userId value roomId-createTime 
     */
    public static final String REDIS_PLAY_RECORD_PREFIX_ROE_DAIKAI = REDIS_RECORD_PREFIX + "_PLAY_RECORD_FOR_DAIKAI_";//代开房间
    
    public static String get_REDIS_PLAY_RECORD_PREFIX_ROE_DAIKAI(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_PLAY_RECORD_PREFIX_ROE_DAIKAI.concat(cId).concat(splitStr);
    }
    
    /**
     * 临时存储大结算数据 key为roomId-createTime
     */
    public static final String REDIS_PLAY_RECORD_PREFIX_OVERINFO = REDIS_RECORD_PREFIX + "_PLAY_RECORD_OVERINFO_";//大结算
    
    public static String get_REDIS_PLAY_RECORD_PREFIX_OVERINFO(String cId){
    	if(cId == null || "".equals(cId))
    		return null;
    	return REDIS_PLAY_RECORD_PREFIX_OVERINFO.concat(cId).concat(splitStr);
    }
    

    
   
    public static Map<String,String> ROUTE_MAP = new ConcurrentHashMap<String, String>();
    static{
    	ROUTE_MAP.put("a","interfaceId");
    	ROUTE_MAP.put("b","state");
    	ROUTE_MAP.put("c","message");
    	ROUTE_MAP.put("d","info");
    	ROUTE_MAP.put("e","others");
    	ROUTE_MAP.put("f","page");
    	ROUTE_MAP.put("g","infos");
    	ROUTE_MAP.put("h","pages");
    	ROUTE_MAP.put("i","connectionInfo");
    	ROUTE_MAP.put("j","help");
    	ROUTE_MAP.put("k","userId");
    	ROUTE_MAP.put("l","content");
    	ROUTE_MAP.put("m","tel");
    	ROUTE_MAP.put("n","roomType");
    	ROUTE_MAP.put("o","type");
    	ROUTE_MAP.put("p","diFen");
    	ROUTE_MAP.put("q","maxPeople");
    	ROUTE_MAP.put("r","zhuangNum");
    	ROUTE_MAP.put("s","fanRule");
    	ROUTE_MAP.put("t","shunZi");
    	ROUTE_MAP.put("u","huLu");
    	ROUTE_MAP.put("v","wuHua");
    	ROUTE_MAP.put("w","zhaDan");
    	ROUTE_MAP.put("x","wuXiao");
    	ROUTE_MAP.put("y","tongHua");
    	ROUTE_MAP.put("z","roomSn");
    	ROUTE_MAP.put("A","roomId");
    	ROUTE_MAP.put("B","reqState");
    	ROUTE_MAP.put("C","playerNum");
    	ROUTE_MAP.put("D","money");
    	ROUTE_MAP.put("E","playStatus");
    	ROUTE_MAP.put("F","position");
    	ROUTE_MAP.put("G","userInfo");
    	ROUTE_MAP.put("H","wsw_sole_main_id");
    	ROUTE_MAP.put("I","wsw_sole_action_id");
    	ROUTE_MAP.put("J","roomInfo");
    	ROUTE_MAP.put("K","lastNum");
    	ROUTE_MAP.put("L","totalNum");
    	ROUTE_MAP.put("M","roomIp");
    	ROUTE_MAP.put("N","ip");
    	ROUTE_MAP.put("O","xjst");
    	ROUTE_MAP.put("P","score");
    	ROUTE_MAP.put("Q","userName");
    	ROUTE_MAP.put("R","userImg");
    	ROUTE_MAP.put("S","joinIndex");
    	ROUTE_MAP.put("T","gender");
    	ROUTE_MAP.put("U","createTime");
    	ROUTE_MAP.put("V","circleNum");
    	ROUTE_MAP.put("W","playerInfo");
    	ROUTE_MAP.put("X","openId");
    	ROUTE_MAP.put("Y","cId");
    	ROUTE_MAP.put("Z","currentUser");
    	ROUTE_MAP.put("aa","anotherUsers");
    	ROUTE_MAP.put("ab","version");
    	ROUTE_MAP.put("ac","userAgree");
    	ROUTE_MAP.put("ad","notice");
    	ROUTE_MAP.put("ae","yaZhu");
    	ROUTE_MAP.put("af","qiangZhuang");
    	ROUTE_MAP.put("ag","pais");
    	ROUTE_MAP.put("ai","zhuangPlayer");
    	ROUTE_MAP.put("aj","dissolveTime");
    	ROUTE_MAP.put("ak","othersAgree");
    	ROUTE_MAP.put("al","dissolveRoom");
    	ROUTE_MAP.put("am","xiaoJuNum");
    	ROUTE_MAP.put("an","continue");
    	ROUTE_MAP.put("ao","niuNum");
    	ROUTE_MAP.put("ap","agree");
    	ROUTE_MAP.put("aq","idx");
    	ROUTE_MAP.put("ar", "maxQZhuang");
    	ROUTE_MAP.put("as", "finalScore");
    	ROUTE_MAP.put("at", "extraType");
    	ROUTE_MAP.put("au", "nextAction");
    	ROUTE_MAP.put("av", "oldPai");
    	ROUTE_MAP.put("aw", "newPai");
    	
    	ROUTE_MAP.put("interfaceId","a");
    	ROUTE_MAP.put("state","b");
    	ROUTE_MAP.put("message","c");
    	ROUTE_MAP.put("info","d");
    	ROUTE_MAP.put("others","e");
    	ROUTE_MAP.put("page","f");
    	ROUTE_MAP.put("infos","g");
    	ROUTE_MAP.put("pages","h");
    	ROUTE_MAP.put("connectionInfo","i");
    	ROUTE_MAP.put("help","j");
    	ROUTE_MAP.put("userId","k");
    	ROUTE_MAP.put("content","l");
    	ROUTE_MAP.put("tel","m");
    	ROUTE_MAP.put("roomType","n");
    	ROUTE_MAP.put("type","o");
    	ROUTE_MAP.put("diFen","p");
    	ROUTE_MAP.put("maxPeople","q");
    	ROUTE_MAP.put("zhuangNum","r");
    	ROUTE_MAP.put("fanRule","s");
    	ROUTE_MAP.put("shunZi","t");
    	ROUTE_MAP.put("huLu","u");
    	ROUTE_MAP.put("wuHua","v");
    	ROUTE_MAP.put("zhaDan","w");
    	ROUTE_MAP.put("wuXiao","x");
    	ROUTE_MAP.put("tongHua","y");
    	ROUTE_MAP.put("roomSn","z");
    	ROUTE_MAP.put("roomId","A");
    	ROUTE_MAP.put("reqState","B");
    	ROUTE_MAP.put("playerNum","C");
    	ROUTE_MAP.put("money","D");
    	ROUTE_MAP.put("playStatus","E");
    	ROUTE_MAP.put("position","F");
    	ROUTE_MAP.put("userInfo","G");
    	ROUTE_MAP.put("wsw_sole_main_id","H");
    	ROUTE_MAP.put("wsw_sole_action_id","I");
    	ROUTE_MAP.put("roomInfo","J");
    	ROUTE_MAP.put("lastNum","K");
    	ROUTE_MAP.put("totalNum","L");
    	ROUTE_MAP.put("roomIp","M");
    	ROUTE_MAP.put("ip","N");
    	ROUTE_MAP.put("xjst","O");
    	ROUTE_MAP.put("score","P");
    	ROUTE_MAP.put("userName","Q");
    	ROUTE_MAP.put("userImg","R");
    	ROUTE_MAP.put("joinIndex","S");
    	ROUTE_MAP.put("gender","T");
    	ROUTE_MAP.put("createTime","U");
    	ROUTE_MAP.put("circleNum","V");
    	ROUTE_MAP.put("playerInfo","W");
    	ROUTE_MAP.put("openId","X");
    	ROUTE_MAP.put("cId","Y");
    	ROUTE_MAP.put("currentUser","Z");
    	ROUTE_MAP.put("anotherUsers","aa");
    	ROUTE_MAP.put("version","ab");
    	ROUTE_MAP.put("userAgree","ac");
    	ROUTE_MAP.put("notice","ad");
    	ROUTE_MAP.put("yaZhu","ae");
    	ROUTE_MAP.put("qiangZhuang","af");
    	ROUTE_MAP.put("pais","ag");
    	ROUTE_MAP.put("zhuangPlayer","ai");
    	ROUTE_MAP.put("dissolveTime","aj");
    	ROUTE_MAP.put("othersAgree","ak");
    	ROUTE_MAP.put("dissolveRoom","al");
    	ROUTE_MAP.put("xiaoJuNum","am");
    	ROUTE_MAP.put("continue","an");
    	ROUTE_MAP.put("niuNum","ao");
    	ROUTE_MAP.put("agree","ap");
    	ROUTE_MAP.put("idx","aq");
    	ROUTE_MAP.put("maxQZhuang", "ar");
    	ROUTE_MAP.put("finalScore", "as");
    	ROUTE_MAP.put("extraType", "at");
    	ROUTE_MAP.put("nextAction", "au");
    	ROUTE_MAP.put("oldPai","av");
    	ROUTE_MAP.put("newPai","aw");
    }

    

    public final static int[] CARD_ARRAY = { 101, 201, 301, 401, 102, 202, 302, 402, 103, 203, 303,
    	403, 104, 204, 304, 404, 105, 205, 305, 405, 106, 206, 306, 406, 107, 207, 307, 407, 108, 208,
    	308, 408, 109, 209, 309, 409, 110, 210, 310, 410, 111, 211, 311, 411, 112, 212, 312, 412, 113, 
    	213, 313, 413 };
    
}
