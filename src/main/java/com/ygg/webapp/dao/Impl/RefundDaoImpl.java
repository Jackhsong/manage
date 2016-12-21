package com.ygg.webapp.dao.Impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ygg.webapp.dao.RefundDao;
import com.ygg.webapp.entity.RefundTeackEntity;
import com.ygg.webapp.entity.RefundEntity;
import com.ygg.webapp.entity.RefundProportionEntity;


@Repository("refundDao")
public class RefundDaoImpl extends BaseDaoImpl implements RefundDao
{
    @Override
    public int saveRefund(RefundEntity refund)
        throws Exception
    {
        return getSqlSession().insert("RefundMapper.saveRefund", refund);
    }
    
    @Override
    public List<Integer> findAllOrderIdsByUserInfo(Map<String, Object> para)
        throws Exception
    {
        List<Integer> reList = getSqlSession().selectList("RefundMapper.findAllOrderIdsByUserInfo", para);
        return reList == null ? new ArrayList<Integer>() : reList;
    }
    
    @Override
    public List<RefundEntity> findAllRefundByPara(Map<String, Object> para)
        throws Exception
    {
        List<RefundEntity> reList = getSqlSession().selectList("RefundMapper.findAllRefundByPara", para);
        return reList == null ? new ArrayList<RefundEntity>() : reList;
    }
    
    @Override
    public int countAllRefundByPara(Map<String, Object> para)
        throws Exception
    {
        return getSqlSession().selectOne("RefundMapper.countAllRefundByPara", para);
    }
    
    @Override
    public Map<String, Map<String, Object>> findAllRefundIsReceiveStatusByIds(Map<String, Object> para)
        throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("RefundMapper.findAllRefundIsReceiveStatusByIds", para);
        if (reList == null || reList.size() < 1)
        {
            return new HashMap<>();
        }
        // refundId为key ，放入map
        Map<String, Map<String, Object>> result = new HashMap<>();
        for (Map<String, Object> currMap : reList)
        {
            String id = currMap.get("refundId") + "";
            result.put(id, currMap);
        }
        return result;
    }
    
    @Override
    public Map<String, Map<String, Object>> findAllOrderProductInfoByIds(Map<String, Object> para)
        throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("RefundMapper.findAllOrderProductInfoByIds", para);
        if (reList == null || reList.size() < 1)
        {
            return null;
        }
        for (Map<String, Object> map : reList)
        {
            int activityType = Integer.parseInt(map.get("isGroup") == null ? "0" : map.get("isGroup") + "");
            if (activityType == 1)
            {
                double groupPrice = Double.parseDouble(map.get("groupPrice") + "");
                map.put("salesPrice", groupPrice);
            }
        }
        // id为key ，放入map
        Map<String, Map<String, Object>> result = new HashMap<String, Map<String, Object>>();
        for (Map<String, Object> currMap : reList)
        {
            String id = currMap.get("id") + "";
            result.put(id, currMap);
        }
        return result;
    }
    
    @Override
    public Map<String, Map<String, Object>> findAllOrderReceiveInfoByIds(Map<String, Object> para)
        throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("RefundMapper.findAllOrderReceiveInfoByIds", para);
        if (reList == null || reList.size() < 1)
        {
            return null;
        }
        // orderId为key ，放入map
        Map<String, Map<String, Object>> result = new HashMap<String, Map<String, Object>>();
        for (Map<String, Object> currMap : reList)
        {
            String id = currMap.get("orderId") + "";
            result.put(id, currMap);
        }
        return result;
    }
    
    @Override
    public RefundEntity findRefundById(int id)
        throws Exception
    {
        Map<String, Object> para = new HashMap<String, Object>();
        para.put("id", id);
        List<RefundEntity> reList = findAllRefundByPara(para);
        if (reList.size() > 0)
        {
            return reList.get(0);
        }
        return null;
    }
    
    @Override
    public Map<String, Object> findOrderProductInfoByOrderProductId(int id)
    {
        Map<String, Object> map = getSqlSession().selectOne("RefundMapper.findOrderProductInfoByOrderProductId", id);
        int activityType = Integer.parseInt(map.get("isGroup") == null ? "0" : map.get("isGroup") + "");
        if (activityType == 1)
        {
            double groupPrice = Double.parseDouble(map.get("groupPrice") + "");
            map.put("salesPrice", groupPrice);
        }
        return map;
    }
    
    @Override
    public Map<String, Object> findAccountCardById(int id)
    {
        return getSqlSession().selectOne("RefundMapper.findAccountCardById", id);
    }
    
    @Override
    public int updateRefund(Map<String, Object> para)
        throws Exception
    {
        return getSqlSession().update("RefundMapper.updateRefund", para);
    }
    
    @Override
    public int updateOrderProductRefundTeack(RefundTeackEntity entity)
        throws Exception
    {
        return getSqlSession().update("RefundMapper.updateOrderProductRefundTeack", entity);
    }
    
    @Override
    public int saveOrderProductRefundTeack(RefundTeackEntity entity)
        throws Exception
    {
        return getSqlSession().insert("RefundMapper.saveOrderProductRefundTeack", entity);
    }
    
    @Override
    public List<RefundTeackEntity> findOrderProductRefundTeack(Map<String, Object> para)
        throws Exception
    {
        List<RefundTeackEntity> reList = getSqlSession().selectList("RefundMapper.findOrderProductRefundTeack", para);
        return reList == null ? new ArrayList<RefundTeackEntity>() : reList;
    }
    
    @Override
    public int saveFinancialAffairsCard(Map<String, Object> para)
        throws Exception
    {
        return getSqlSession().insert("RefundMapper.saveFinancialAffairsCard", para);
    }
    
    @Override
    public int deleteFinancialAffairsCardByIds(List<Integer> idList)
        throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("idList", idList);
        return getSqlSession().delete("RefundMapper.deleteFinancialAffairsCardById", map);
    }
    
    @Override
    public List<Map<String, Object>> findAllFinancialAffairsCard(Map<String, Object> para)
        throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("RefundMapper.findAllFinancialAffairsCard", para);
        return reList == null ? new ArrayList<Map<String, Object>>() : reList;
    }
    
    @Override
    public int countAllFinancialAffairsCard(Map<String, Object> para)
        throws Exception
    {
        return getSqlSession().selectOne("RefundMapper.countAllFinancialAffairsCard", para);
    }
    
    @Override
    public Map<String, Object> findRefundLogisticsByRefundId(int id)
        throws Exception
    {
        return getSqlSession().selectOne("RefundMapper.findRefundLogisticsByRefundId", id);
    }
    
    @Override
    public int saveRefundLogistics(Map<String, Object> para)
        throws Exception
    {
        return getSqlSession().insert("RefundMapper.saveRefundLogistics", para);
    }
    
    @Override
    public List<Map<String, Object>> findRefundLogisticsByPara(Map<String, Object> para)
        throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("RefundMapper.findRefundLogisticsByPara", para);
        return reList == null ? new ArrayList<Map<String, Object>>() : reList;
    }
    
    @Override
    public int updateRefundLogistics(Map<String, Object> para)
        throws Exception
    {
        return getSqlSession().update("RefundMapper.updateRefundLogistics", para);
    }
    
    @Override
    public List<Map<String, Object>> findAllFinancialAffairsCardById(int transferAccount)
        throws Exception
    {
        return getSqlSession().selectList("RefundMapper.findAllFinancialAffairsCardById", transferAccount);
    }


    @Override
    public RefundProportionEntity findRefundProportionByRefundId(int refundId)
        throws Exception
    {
        return getSqlSession().selectOne("RefundMapper.findRefundProportionByRefundId", refundId);
    }
    
    @Override
    public int updateRefundProportionByRefundId(Map<String, Object> para)
        throws Exception
    {
        return getSqlSession().update("RefundMapper.updateRefundProportionByRefundId", para);
    }
    
    @Override
    public int saveRefundProportionByRefundId(Map<String, Object> para)
        throws Exception
    {
        return getSqlSession().insert("RefundMapper.saveRefundProportionByRefundId", para);
    }
    
    @Override
    public Map<String, Object> findOrderProductCostById(int id)
        throws Exception
    {
        return getSqlSession().selectOne("RefundMapper.findOrderProductCostById", id);
    }
    
    @Override
    public List<RefundProportionEntity> findAllRefundProportionByPara(Map<String, Object> para)
        throws Exception
    {
        return getSqlSession().selectList("RefundMapper.findAllRefundProportionByPara", para);
    }
    
    @Override
    public int findOtherNotExistsRefund(Map<String, Object> para)
        throws Exception
    {
        return getSqlSession().selectOne("RefundMapper.findOtherNotExistsRefund", para);
    }
    
    @Override
    public int findOtherNotExistsRefundForCancelOrder(Map<String, Object> para)
        throws Exception
    {
        return getSqlSession().selectOne("RefundMapper.findOtherNotExistsRefundForCancelOrder", para);
    }
    
    @Override
    public int findOtherNotExistsRefundForCancelOrderForStep1(Map<String, Object> para)
        throws Exception
    {
        return getSqlSession().selectOne("RefundMapper.findOtherNotExistsRefundForCancelOrderForStep1", para);
    }
    
    @Override
    public int countRefundByOrderProductId(int id)
        throws Exception
    {
        return getSqlSession().selectOne("RefundMapper.countRefundByOrderProductId", id);
    }
    
    @Override
    public List<Map<String, Object>> findRefundForEveryday(Map<String, Object> param)
        throws Exception
    {
        return getSqlSession().selectList("RefundMapper.findRefundForEveryday", param);
    }
    
    @Override
    public List<Map<String, Object>> findRefundSellerIdForSeller(Map<String, Object> param)
        throws Exception
    {
        return getSqlSession().selectList("RefundMapper.findRefundSellerIdForSeller", param);
    }
    
    @Override
    public int countRefundSellerIdForSeller(Map<String, Object> param)
        throws Exception
    {
        Integer result = getSqlSession().selectOne("RefundMapper.countRefundSellerIdForSeller", param);
        return result == null ? 0 : result;
    }
    
    @Override
    public List<Map<String, Object>> findRefundForSeller(Map<String, Object> param)
        throws Exception
    {
        return getSqlSession().selectList("RefundMapper.findRefundForSeller", param);
    }

	@Override
	public List<Map<String, Object>> selectRefundByOrderId(int orderId) throws Exception {
		List<Map<String, Object>> map = getSqlSession().selectList("RefundMapper.selectRefundByOrderId", orderId);
        return map;
	}
}
