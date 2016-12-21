package com.ygg.webapp.dao.Impl;

import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.ygg.webapp.dao.BaseDao;

@Repository("baseDao")
public class BaseDaoImpl /* extends SqlSessionDaoSupport */implements BaseDao
{
    
    @Resource
    private SqlSessionTemplate sqlSessionTemplate;
    
    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate)
    {
        this.sqlSessionTemplate = sqlSessionTemplate;
        
    }
    
    protected SqlSessionTemplate getSqlSession()
    {
        return sqlSessionTemplate;
    }
}
