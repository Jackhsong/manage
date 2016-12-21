package com.ygg.webapp.service.Impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.ygg.webapp.dao.ManagerDao;
import com.ygg.webapp.entity.ManagerEntity;
import com.ygg.webapp.exception.ServiceException;
import com.ygg.webapp.service.ManagerService;

@Service("managerService")
public class ManagerServiceImpl implements ManagerService{

	@Resource(name="managerDao")
	private ManagerDao managerDao;

	@Override
	public int insertManager(ManagerEntity record) throws ServiceException {
		int resultnumber=managerDao.insertManager(record);
		
		return resultnumber;
	}

	@Override
	public ManagerEntity selectByName(String name) throws ServiceException {
		ManagerEntity managerEntity=managerDao.selectByName(name);
		
		return managerEntity;
	}

	@Override
	public int updateByName(ManagerEntity record) throws ServiceException {
		int resultnumber=managerDao.updateByName(record);
		
		return resultnumber;
	}

	@Override
	public List<Map<String, Object>> findAllMenuByPara(Map<String, Object> para) throws ServiceException {
		List<Map<String, Object>> list=managerDao.findAllMenuByPara(para);
		
		return list;
	}
	
	
	
}
