package com.ygg.webapp.dao.Impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ygg.webapp.dao.QqbsCashDao;
import com.ygg.webapp.dao.Impl.BaseDaoImpl;
import com.ygg.webapp.entity.RewardEntity;

@Repository("qqbsCashDao")
public class QqbsCashDaoImpl extends BaseDaoImpl implements QqbsCashDao {
	
	@Override
    public List<Map<String, Object>> findListInfo(Map<String, Object> param)
        throws Exception{
        return getSqlSession().selectList("QqbsCashMapper.findListByParam", param);
    }
	
	@Override
    public int getCountByParam(Map<String, Object> param)
        throws Exception
    {
        return getSqlSession().selectOne("QqbsCashMapper.getCountByParam", param);
    } 
	 
    
    @Override
    public int updateLog(Map<String, Object> param)
    {
        return getSqlSession().update("QqbsCashMapper.updateLog", param);
    }
    
    public int updateHqbsReward(RewardEntity hqbsRewardEntity){
		return this.getSqlSession().update("QqbsCashMapper.updateHqbsReward", hqbsRewardEntity);
	}
    
    /**
	 * 获取用户奖励信息
	 * @param accountId
	 * @return
	 * @throws Exception
	 */
	public RewardEntity getByAccountId(int accountId){
		RewardEntity ae = this.getSqlSession().selectOne("QqbsCashMapper.getByAccountId", accountId);
        return ae;
    }
    
	/**
	 * 获取用户奖励信息
	 * @param accountId
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getInfoByLogId(int logId){
		return this.getSqlSession().selectOne("QqbsCashMapper.getInfoByLogId", logId);
	}
}
