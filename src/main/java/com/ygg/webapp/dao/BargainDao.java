package com.ygg.webapp.dao;

import java.util.List;
import java.util.Map;

import com.ygg.webapp.exception.DaoException;

public interface BargainDao
{
	
	List<Map<String,Object>> selectListBargainByPara(Map<String,Object> para) throws DaoException;
	
    Integer updateStatusById(Map<String,Object> para) throws DaoException;
    
    Integer updateActivityBargain(Map<String,Object> para) throws DaoException;
    
    Integer updateProductById(Map<String,Object> para) throws DaoException;
    
    Integer insertProduct(Map<String,Object> para) throws DaoException;
    
    Integer insertProductBargain(Map<String,Object> para) throws DaoException;
    
    Integer insertActivityBargain(Map<String,Object> para) throws DaoException;
    
    Integer selectTheLastId() throws DaoException;
    
    Integer deleteProductById(Integer id) throws DaoException;
    
    Integer deleteProductBargainById(Integer id) throws DaoException;
    
    Integer deleteActivityByProductId(Integer id) throws DaoException;
    
    
}
