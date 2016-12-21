package com.ygg.webapp.dao.Impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ygg.webapp.dao.QqbsSaleWindowDao;
import com.ygg.webapp.entity.QqbsSaleWindowEntity;


@Repository("qqbsSaleWindowDao")
public class QqbsSaleWindowDaoImpl extends BaseDaoImpl implements QqbsSaleWindowDao
{
    
    @Override
    public int save(QqbsSaleWindowEntity qqbsSaleWindow)
        throws Exception
    {
        return this.getSqlSession().insert("QqbsSaleWindowMapper.save", qqbsSaleWindow);
    }
    
    @Override
    public int update(QqbsSaleWindowEntity qqbsSaleWindow)
        throws Exception
    {
        return this.getSqlSession().update("QqbsSaleWindowMapper.update", qqbsSaleWindow);
    }
    
    @Override
    public int countSaleWindow(Map<String, Object> para)
        throws Exception
    {
        return this.getSqlSession().selectOne("QqbsSaleWindowMapper.countSaleWindow", para);
    }
    
    @Override
    public List<QqbsSaleWindowEntity> findAllSaleWindow(Map<String, Object> para)
        throws Exception
    {
        return this.getSqlSession().selectList("QqbsSaleWindowMapper.findAllSaleWindow", para);
    }
    
    @Override
    public QqbsSaleWindowEntity findSaleWindowById(int id)
        throws Exception
    {
        return this.getSqlSession().selectOne("QqbsSaleWindowMapper.findSaleWindowById", id);
    }
    
    @Override
    public int updateDisplayCode(Map<String, Object> para)
        throws Exception
    {
        return this.getSqlSession().update("QqbsSaleWindowMapper.updateDisplayCode", para);
    }
    
    @Override
    public List<Map<String, Object>> findAllSaleWindowForSellerPeriod(Map<String, Object> para)
        throws Exception
    {
        List<Map<String, Object>> result = this.getSqlSession().selectList("QqbsSaleWindowMapper.findAllSaleWindowForSellerPeriod", para);
        return result == null ? new ArrayList<Map<String, Object>>() : result;
    }
    
    @Override
    public int hideSaleWindow(Map<String, Object> para)
        throws Exception
    {
        return this.getSqlSession().update("QqbsSaleWindowMapper.hideSaleWindow", para);
    }
    
    @Override
    public int countAllSaleWindowForSellerPeriod(Map<String, Object> para)
        throws Exception
    {
        return this.getSqlSession().selectOne("QqbsSaleWindowMapper.countAllSaleWindowForSellerPeriod", para);
    }
    
    @Override
    public int countSingleSaleWindow(Map<String, Object> para)
        throws Exception
    {
        return this.getSqlSession().selectOne("QqbsSaleWindowMapper.countSingleSaleWindow", para);
    }
    
    @Override
    public List<QqbsSaleWindowEntity> findAllSingleSaleWindow(Map<String, Object> para)
        throws Exception
    {
        List<QqbsSaleWindowEntity> reList = this.getSqlSession().selectList("QqbsSaleWindowMapper.findAllSingleSaleWindow", para);
        return reList == null ? new ArrayList<QqbsSaleWindowEntity>() : reList;
    }
    
    @Override
    public int countGroupSaleWinodw(Map<String, Object> para)
        throws Exception
    {
        return this.getSqlSession().selectOne("QqbsSaleWindowMapper.countGroupSaleWinodw", para);
    }
    
    @Override
    public List<QqbsSaleWindowEntity> findAllGroupSaleWinodw(Map<String, Object> para)
        throws Exception
    {
        List<QqbsSaleWindowEntity> reList = this.getSqlSession().selectList("QqbsSaleWindowMapper.findAllGroupSaleWinodw", para);
        return reList == null ? new ArrayList<QqbsSaleWindowEntity>() : reList;
    }
    
    public List<QqbsSaleWindowEntity> findAllByParam(Map<String, Object> para)
        throws Exception
    {
        List<QqbsSaleWindowEntity> reList = this.getSqlSession().selectList("QqbsSaleWindowMapper.findAllByParam", para);
        return reList == null ? new ArrayList<QqbsSaleWindowEntity>() : reList;
    }
    
    public int countAllByParam(Map<String, Object> para)
        throws Exception
    {
        return this.getSqlSession().selectOne("QqbsSaleWindowMapper.countAllByParam", para);
    }

    @Override
    public List<QqbsSaleWindowEntity> findAllSaleWindowByParam(Map<String, Object> para)
    {
        return this.getSqlSession().selectList("QqbsSaleWindowMapper.findAllSaleWindowByParam", para);
    }

    @Override
    public int countAllSaleWindowByParam(Map<String, Object> para)
    {
        return this.getSqlSession().selectOne("QqbsSaleWindowMapper.countAllSaleWindowByParam", para);
    }

    @Override
    public int updateOrder(int id, int order)
    {
        Map<String, Object> para = new HashMap<String, Object>();
        para.put("id", id);
        para.put("order", order);
        return this.getSqlSession().update("QqbsSaleWindowMapper.updateOrder", para);
    }
}
