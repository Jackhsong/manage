package com.ygg.webapp.service;

import java.util.List;
import java.util.Map;

import com.ygg.webapp.entity.BrandEntity;
import com.ygg.webapp.entity.SellerEntity;


public interface SellerService
{
    
    /**
     * 保存商家信息
     * 
     * @param para
     * @return
     * @throws Exception
     */
    int saveOrUpdate(Map<String, Object> para)
        throws Exception;
    
    /**
     * 根据para查询商家，并封装成json字符串返回
     * 
     * @param para
     * @return
     * @throws Exception
     */
    String jsonSellerInfo(Map<String, Object> para)
        throws Exception;

    
    /**
     * 查询可用的商家信息
     * 
     * @return
     * @throws Exception
     */
    List<SellerEntity> findSellerIsAvailable()
        throws Exception;
    
    /**
     * 查询全部商家信息
     * 
     * @return
     * @throws Exception
     */
    List<SellerEntity> findAllSeller(Map<String, Object> para)
        throws Exception;

    
    /**
     * 根据商家Id查找商家所售品牌
     * @param id
     * @return
     * @throws Exception
     */
    List<BrandEntity> findSellerBrandBysid(int id)
        throws Exception;

}
