package com.ygg.webapp.service;

import java.util.List;
import java.util.Map;

import com.ygg.webapp.entity.ManagerEntity;
import com.ygg.webapp.exception.ServiceException;

public interface ManagerService {

	    int insertManager(ManagerEntity record) throws ServiceException;

	    ManagerEntity selectByName(String name) throws ServiceException;

	    int updateByName(ManagerEntity record) throws ServiceException;
	    
	    List<Map<String, Object>> findAllMenuByPara(Map<String, Object> para) throws ServiceException;

}
