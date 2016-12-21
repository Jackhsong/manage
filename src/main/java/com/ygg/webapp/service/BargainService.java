package com.ygg.webapp.service;

import java.util.List;
import java.util.Map;

import com.ygg.webapp.exception.ServiceException;

public interface BargainService
{
   
	List<Map<String,Object>> selectListBargainByPara(Map<String,Object> para) throws ServiceException;
	
	Integer updateStatusById(Map<String,Object> para) throws ServiceException;
	
	Integer updateActivityBargain(Map<String,Object> para)  throws ServiceException;
	
	Integer updateProductById(Map<String, Object> para)  throws ServiceException;
	
	Integer insertProduct(Map<String,Object> para)  throws ServiceException;
	    
    Integer insertProductBargain(Map<String,Object> para)   throws ServiceException;
    
    Integer insertActivityBargain(Map<String,Object> para)  throws ServiceException;

    Integer selectTheLastId()  throws ServiceException;
    
    Integer deleteProductById(Integer id) throws ServiceException;
    
    Integer deleteProductBargainById(Integer id) throws ServiceException;
    
    Integer deleteActivityByProductId(Integer id) throws ServiceException;
    
    
}
