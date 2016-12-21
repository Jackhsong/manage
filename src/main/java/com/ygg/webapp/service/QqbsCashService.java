package com.ygg.webapp.service;

import java.util.Map;

public interface QqbsCashService {

	/**
	 * 查询推荐列表
	 * @param param
	 * @return Map<String, Object>
	 * @throws Exception
	 */
	Map<String, Object> findListInfo(Map<String, Object> param) throws Exception;
	
	
	/**
	 * 更新提现日志
	 * @param id
	 * @param type
	 */
	public String updateLog(int id , int type)throws Exception;
	
}
