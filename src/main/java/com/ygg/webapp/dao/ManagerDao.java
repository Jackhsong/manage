package com.ygg.webapp.dao;

import java.util.List;
import java.util.Map;

import com.ygg.webapp.entity.ManagerEntity;
import com.ygg.webapp.exception.DaoException;

public interface ManagerDao {

    int insertManager(ManagerEntity record) throws DaoException;

    ManagerEntity selectByName(String name) throws DaoException;

    int updateByName(ManagerEntity record) throws DaoException;
    
    List<Map<String, Object>> findAllMenuByPara(Map<String, Object> para) throws DaoException;

}