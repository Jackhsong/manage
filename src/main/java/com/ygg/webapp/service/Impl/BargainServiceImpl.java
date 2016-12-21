package com.ygg.webapp.service.Impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.ygg.webapp.dao.BargainDao;
import com.ygg.webapp.exception.ServiceException;
import com.ygg.webapp.service.BargainService;

@Service("bargainService")
public class BargainServiceImpl implements BargainService
{

    Logger logger = Logger.getLogger(BargainServiceImpl.class);

    @Resource(name="bargainDao")
    private BargainDao bargainDao;

	@Override
	public List<Map<String, Object>> selectListBargainByPara(Map<String, Object> para) throws ServiceException {
		List<Map<String, Object>> list=bargainDao.selectListBargainByPara(para);
		
		return list;
	}

	@Override
	public Integer updateStatusById(Map<String, Object> para) throws ServiceException {
		Integer result=bargainDao.updateStatusById(para);
		
		return result;
	}

	@Override
	public Integer updateActivityBargain(Map<String, Object> para) throws ServiceException {
		Integer result=bargainDao.updateActivityBargain(para);
		
		return result;
	}

	@Override
	public Integer updateProductById(Map<String, Object> para) throws ServiceException {
		Integer result=bargainDao.updateProductById(para);
		
		return result;
	}

	@Override
	public Integer insertProduct(Map<String, Object> para) throws ServiceException {
		Integer result=bargainDao.insertProduct(para);
				
		return result;
	}

	@Override
	public Integer insertProductBargain(Map<String, Object> para) throws ServiceException {
		Integer result=bargainDao.insertProductBargain(para);
				
		return result;
	}

	@Override
	public Integer insertActivityBargain(Map<String, Object> para) throws ServiceException {
		Integer result=bargainDao.insertActivityBargain(para);
				
		return result;
	}

	@Override
	public Integer selectTheLastId() throws ServiceException {
		Integer result=bargainDao.selectTheLastId();
		
		return result;
	}

	@Override
	public Integer deleteProductById(Integer id) throws ServiceException {
		Integer result=bargainDao.deleteProductById(id);
		
		return result;
	}

	@Override
	public Integer deleteProductBargainById(Integer id) throws ServiceException {
		Integer result=bargainDao.deleteProductBargainById(id);
				
		return result;
	}

	@Override
	public Integer deleteActivityByProductId(Integer id) throws ServiceException {
		Integer result=bargainDao.deleteActivityByProductId(id);
				
		return result;
	}
    
    

   
}
