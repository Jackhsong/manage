package com.ygg.webapp.dao.Impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ygg.webapp.dao.ManagerDao;
import com.ygg.webapp.entity.ManagerEntity;
import com.ygg.webapp.exception.DaoException;

@Repository("managerDao")
public class ManagerDaoImpl  extends BaseDaoImpl implements ManagerDao{

	@Override
	public int insertManager(ManagerEntity record) throws DaoException {
		
		return this.getSqlSession().insert("ManagerMapper.insertManager", record);
	}

	@Override
	public ManagerEntity selectByName(String name) throws DaoException {
		
		return this.getSqlSession().selectOne("ManagerMapper.selectByName", name);
	}

	@Override
	public int updateByName(ManagerEntity record) throws DaoException {
		
		return this.getSqlSession().update("ManagerMapper.updateByName",record);
	}

    @Override
    public List<Map<String, Object>> findAllMenuByPara(Map<String, Object> para) throws DaoException{
    	
        return this.getSqlSession().selectList("ManagerMapper.findAllMenuByPara", para);
    }

}
