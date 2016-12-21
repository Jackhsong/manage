package com.ygg.webapp.service.Impl;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.ygg.webapp.dao.AreaDao;
import com.ygg.webapp.dao.SellerDao;
import com.ygg.webapp.entity.BrandEntity;
import com.ygg.webapp.entity.SellerEntity;
import com.ygg.webapp.service.SellerService;
import com.ygg.webapp.util.SellerEnum;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service("sellerService")
public class SellerServiceImpl implements SellerService
{
    
    Logger logger = Logger.getLogger(SellerServiceImpl.class);
    
    @Resource
    private SellerDao sellerDao = null;

    @Resource
    private AreaDao areaDao;
    
    @SuppressWarnings("unchecked")
    @Override
    public int saveOrUpdate(Map<String, Object> map)
        throws Exception
    {
        int resultStatus = -1;
        SellerEntity seller = (SellerEntity)map.get("seller");
        if (seller.getId() <= 0)
        {
            // 修改
            logger.info("修改商家信息：" + seller.toString());
            Map<String, Object> sellerBeanMap = new BeanMap(seller);
            logger.debug(sellerBeanMap);
            resultStatus = sellerDao.updateSellerByPara(sellerBeanMap);                        
        }
        
        //商家店铺网址，先删除后新增
        sellerDao.deleteSellerOnlineStoreAddress(seller.getId());
        
        return resultStatus;
    }
    
    @Override
    public String jsonSellerInfo(Map<String, Object> para)
        throws Exception
    {
        List<SellerEntity> sellerList = sellerDao.findAllSellerByPara(para);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        int total = 0;
        if (sellerList.size() > 0)
        {
            for (SellerEntity seller : sellerList)
            {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("id", seller.getId());
                map.put("sellerName", seller.getSellerName());
                //类目
                List<Map<String, Object>> categories = sellerDao.findSellerCategoryByRelation(seller.getId());
                Set<String> names = new LinkedHashSet<>();
                if (CollectionUtils.isEmpty(categories)) {  // 如果商家没有添加类目 则从商家的基本商品中查找 并自动存储商家类别
                    categories = sellerDao.findSellerCategoryByBaseProduct(seller.getId());
                    Map<String, Object> insertPara = new HashMap<>();
                    insertPara.put("sellerId", seller.getId());
                    if (CollectionUtils.isNotEmpty(categories)) {
                        for(Map<String, Object> cat : categories) {
                            insertPara.put("categoryFirstId", cat.get("id"));
                            sellerDao.saveSellerCategoryRelation(insertPara);
                            names.add(StringUtils.trimToNull((String) cat.get("name")));
                        }
                        map.put("categories", Joiner.on(",").skipNulls().join(names));
                    }
                } else {
                    for(Map<String, Object> cat : categories) {
                        names.add(StringUtils.trimToNull((String) cat.get("name")));
                    }
                    map.put("categories", Joiner.on(",").skipNulls().join(names));
                }
                
                map.put("createTime", seller.getCreateTime().toString());
                String remark = SellerEnum.SellerSendCodeTypeEnum.getDescByCode( seller.getSendCodeType());
                if("其他".equalsIgnoreCase(remark)) {
                    remark = seller.getSendCodeRemark();
                }
                map.put("sendTypeDesc", remark);
                map.put("realSellerName", seller.getRealSellerName());
                map.put("companyName", seller.getCompanyName());
                map.put("sellerTypeString", SellerEnum.SellerTypeEnum.getDescByCode(seller.getSellerType()));
                map.put("sendAddress", seller.getSendAddress());
                map.put("responsibilityPerson", seller.getResponsibilityPerson());
                map.put("warehouse", seller.getWarehouse());
                map.put("isAvailable", seller.getIsAvailable());
                map.put("weekendRemark", SellerEnum.WeekendSendTypeEnum.getDescByCode(seller.getIsSendWeekend()));
                map.put("isOwner", seller.getIsOwner());
                resultList.add(map);
            }
            total = sellerDao.countSellerByPara(para);
        }
        resultMap.put("rows", resultList);
        resultMap.put("total", total);
        return JSON.toJSONString(resultMap);
    }
   
    @Override
    public List<SellerEntity> findSellerIsAvailable()
        throws Exception
    {
        Map<String, Object> para = new HashMap<String, Object>();
        para.put("isAvailable", 1);
        para.put("start", 0);
        para.put("max", 1000);
        return sellerDao.findAllSellerByPara(para);
    }
    
    
    @Override
    public List<BrandEntity> findSellerBrandBysid(int id)
        throws Exception
    {
        return sellerDao.findSellerBrandBysid(id);
    }
       

	@Override
	public List<SellerEntity> findAllSeller(Map<String, Object> para) throws Exception {
		
		return sellerDao.findAllSellerByPara(para);
	}

    
}
