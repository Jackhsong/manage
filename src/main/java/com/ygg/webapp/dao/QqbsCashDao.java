package com.ygg.webapp.dao;

import java.util.List;
import java.util.Map;

import com.ygg.webapp.entity.RewardEntity;

public interface QqbsCashDao {
	/**
     * 查询推荐列表
     * @param param
     * @return
     * @throws Exception
     */
    List<Map<String, Object>> findListInfo(Map<String, Object> param)
        throws Exception;
    
    /**
     * 统计条数
     * @param param
     * @return
     * @throws Exception
     */
    int getCountByParam(Map<String, Object> param)
        throws Exception;
    
    public int updateLog(Map<String, Object> param);
    
    public int updateHqbsReward(RewardEntity hqbsRewardEntity);
    /**
	 * 获取用户奖励信息
	 * @param accountId
	 * @return
	 * @throws Exception
	 */
	public RewardEntity getByAccountId(int accountId);
	
	
	/**
     * 获取用户奖励信息
     * @param accountId
     * @return
     * @throws Exception
     */
    public Map<String, Object> getInfoByLogId(int logId);
}
