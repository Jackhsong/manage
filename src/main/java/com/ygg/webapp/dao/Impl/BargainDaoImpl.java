package com.ygg.webapp.dao.Impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ygg.webapp.dao.BargainDao;
import com.ygg.webapp.exception.DaoException;

@Repository("bargainDao")
public class BargainDaoImpl extends BaseDaoImpl implements BargainDao
{

	@Override
	public List<Map<String, Object>> selectListBargainByPara(Map<String, Object> para) throws DaoException {
		
		return this.getSqlSession().selectList("BargainMapper.selectListBargainByPara",para);
	}

	@Override
	public Integer updateStatusById(Map<String, Object> para) throws DaoException {
		
		return this.getSqlSession().update("BargainMapper.updateStatusById",para);
	}

	@Override
	public Integer updateActivityBargain(Map<String, Object> para) throws DaoException {
		
		return this.getSqlSession().update("BargainMapper.updateActivityBargain",para);
	}

	@Override
	public Integer updateProductById(Map<String, Object> para) throws DaoException {
		
		return this.getSqlSession().update("BargainMapper.updateProductById",para);
	}

	@Override
	public Integer insertProduct(Map<String, Object> para) throws DaoException {
		
		return this.getSqlSession().insert("BargainMapper.insertProduct", para);
	}

	@Override
	public Integer insertProductBargain(Map<String, Object> para) throws DaoException {
		
		return this.getSqlSession().insert("BargainMapper.insertProductBargain", para);
	}

	@Override
	public Integer insertActivityBargain(Map<String, Object> para) throws DaoException {
		
		return this.getSqlSession().insert("BargainMapper.insertActivityBargain", para);
	}

	@Override
	public Integer selectTheLastId() throws DaoException {
		
		return this.getSqlSession().selectOne("BargainMapper.selectTheLastId");
	}

	@Override
	public Integer deleteProductById(Integer id) throws DaoException {
		
		return this.getSqlSession().delete("BargainMapper.deleteProductById",id);
	}

	@Override
	public Integer deleteProductBargainById(Integer id) throws DaoException {
		
		return this.getSqlSession().delete("BargainMapper.deleteProductBargainById",id);
	}

	@Override
	public Integer deleteActivityByProductId(Integer id) throws DaoException {
		
		return this.getSqlSession().delete("BargainMapper.deleteActivityByProductId",id);
	}

   
}
