package com.ygg.webapp.dao.Impl;

import com.alibaba.fastjson.JSONObject;
import com.ygg.webapp.dao.OrderDao;
import com.ygg.webapp.entity.OrderDetailInfoForSeller;
import com.ygg.webapp.entity.OrderEntity;
import com.ygg.webapp.entity.OrderInfoForManage;
import com.ygg.webapp.entity.OrderLogistics;
import com.ygg.webapp.entity.OrderPayEntity;
import com.ygg.webapp.entity.ReceiveAddressEntity;
import com.ygg.webapp.exception.DaoException;
import com.ygg.webapp.util.DateTimeUtil;
import com.ygg.webapp.util.MathUtil;
import com.ygg.webapp.util.OrderEnum;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("orderDao")
public class OrderDaoImpl extends BaseDaoImpl implements OrderDao
{

    @Override
    public List<OrderEntity> findOrder(Map<String, Object> para)
            throws Exception
    {
        return this.getSqlSession().selectList("OrderMapper.findOrder", para);
    }

    @Override
    public OrderEntity findOrderById(int id)
            throws Exception
    {
        Map<String, Object> para = new HashMap<String, Object>();
        para.put("id", id);
        para.put("start", 0);
        para.put("max", 1);
        List<OrderEntity> orderList = findOrder(para);
        if (orderList != null && orderList.size() > 0)
        {
            return orderList.get(0);
        }
        else
        {
            return null;
        }
    }

    @Override
    public OrderEntity findOrderByNumber(String number)
            throws Exception
    {
        if (number == null)
            number = "";
        Map<String, Object> para = new HashMap<String, Object>();
        para.put("number", number.equals("") ? 0 : Long.valueOf(number));
        para.put("start", 0);
        para.put("max", 1);
        List<OrderEntity> orderList = findOrder(para);
        if (orderList != null && orderList.size() > 0)
        {
            return orderList.get(0);
        }
        else
        {
            return null;
        }
    }

    @Override
    public OrderEntity findOrderByNumber(long number)
        throws Exception
    {
        Map<String, Object> para = new HashMap<String, Object>();
        para.put("number", number);
        para.put("start", 0);
        para.put("max", 1);
        List<OrderEntity> orderList = findOrder(para);
        if (orderList != null && orderList.size() > 0)
        {
            return orderList.get(0);
        }
        else
        {
            return null;
        }
    }

    @Override
    public List<Integer> findOrderIdsByPara(Map<String, Object> para)
            throws Exception
    {
        List<Integer> reList = getSqlSession().selectList("OrderMapper.findOrderIdsByPara", para);
        return reList == null ? new ArrayList<Integer>() : reList;
    }

    @Override
    public int updateOrder(Map<String, Object> para)
            throws Exception
    {
        return this.getSqlSession().update("OrderMapper.updateOrder", para);
    }

    @Override
    public int countOrder(Map<String, Object> para)
            throws Exception
    {
        return this.getSqlSession().selectOne("OrderMapper.countOrder", para);
    }

    @Override
    public int saveOrderLogistics(Map<String, Object> para)
            throws Exception
    {
        return this.getSqlSession().insert("OrderMapper.saveOrderLogistics", para);
    }

    @Override
    public Map<String, Object> findOrderLogisticsByOrderId(int id)
            throws Exception
    {
        return this.getSqlSession().selectOne("OrderMapper.findOrderLogisticsByOrderId", id);
    }

    @Override
    public int updateOrderLogistics(Map<String, Object> para)
            throws Exception
    {
        return this.getSqlSession().update("OrderMapper.updateOrderLogisticsByOrderId", para);
    }

    @Override
    public OrderLogistics findOrderLogisticsBychannelAndNumber(Map<String, Object> para)
            throws Exception
    {
        return this.getSqlSession().selectOne("OrderMapper.findOrderLogisticsBychannelAndNumber", para);
    }

    @Override
    public int saveLogisticsDetail(Map<String, Object> para)
            throws Exception
    {
        return this.getSqlSession().insert("OrderMapper.saveLogisticsDetail", para);
    }

    @Override
    public int countLogisticsDetail(Map<String, Object> para)
            throws Exception
    {
        return this.getSqlSession().selectOne("OrderMapper.countLogisticsDetail", para);
    }

    @Override
    public int deleteLogisticsDetail(Map<String, Object> para)
            throws Exception
    {
        return this.getSqlSession().delete("OrderMapper.deleteLogisticsDetail", para);
    }

    @Override
    public List<OrderLogistics> findOrderLogistics(Map<String, Object> para)
            throws Exception
    {
        /*
         * 2016-2-24 吴灵修改
         * return this.getSqlSessionRead().selectList("OrderMapper.findOrderLogistics", para);
         */
        return this.getSqlSession().selectList("OrderMapper.findOrderLogistics", para);
    }

    @Override
    public int existsLogisticsDetail(Map<String, Object> para)
            throws Exception
    {
        return this.getSqlSession().selectOne("OrderMapper.existsLogisticsDetail", para);
    }

    @Override
    public List<Map<String, Object>> findAllLogisticsDetail(Map<String, Object> para)
            throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("OrderMapper.findAllLogisticsDetail", para);
        return reList == null ? new ArrayList<Map<String, Object>>() : reList;
    }

    @Override
    public List<Map<String, Object>> findAllOrderProductInfo(int id)
            throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("OrderMapper.findAllOrderProductInfo", id);
        for (Map<String, Object> map : reList)
        {
            int activityType = Integer.parseInt(map.get("isGroup") == null ? "0" : map.get("isGroup") + "");
            if (activityType == 1)
            {
                double groupPrice = Double.parseDouble(map.get("groupPrice") + "");
                map.put("price", groupPrice);
            }
        }
        return reList;
    }

    @Override
    public Map<String, Object> findOrderProductById(int id)
            throws Exception
    {
        Map<String, Object> re = getSqlSession().selectOne("OrderMapper.findOrderProductById", id);
        int activityType = Integer.parseInt(re.get("isGroup") == null ? "0" : re.get("isGroup") + "");
        if (activityType == 1)
        {
            double groupPrice = Double.parseDouble(re.get("groupPrice") + "");
            re.put("salesPrice", groupPrice);
        }
        return re;
    }

    @Override
    public List<Map<String, Object>> findOrderProductByOrderId(int orderId)
            throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("OrderMapper.findOrderProductByOrderId", orderId);
        for (Map<String, Object> map : reList)
        {
            int activityType = Integer.parseInt(map.get("isGroup") == null ? "0" : map.get("isGroup") + "");
            if (activityType == 1)
            {
                double groupPrice = Double.parseDouble(map.get("groupPrice") + "");
                map.put("salesPrice", groupPrice);
            }
        }
        return reList;
    }

    @Override
    public int updateOrderProduct(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().update("OrderMapper.updateOrderProduct", para);
    }

    @Override
    public List<OrderDetailInfoForSeller> findAllOrderInfoForSeller(Map<String, Object> map)
            throws Exception
    {
        List<OrderDetailInfoForSeller> reList = getSqlSession().selectList("OrderMapper.findAllOrderInfoForSeller", map);
        // 解冻订单操作
        Integer exportFreezeOrder = Integer.parseInt(map.get("exportFreezeOrder") == null ? "0" : map.get("exportFreezeOrder") + "");
        // if (reList.size() > 0)
        // {

        if (exportFreezeOrder == 1)
        {
            // 需要导出时间段内的解冻订单
            String startTimeBegin = map.get("startTimeBegin") + "";// 必须保证有该值
            String startTimeEnd = map.get("startTimeEnd") == null ? "" : map.get("startTimeEnd") + "";
            @SuppressWarnings("unchecked")
			List<Integer> sellerIdList = map.get("sellerIdList") == null ? null : (List<Integer>)map.get("sellerIdList");

            Map<String, Object> para = new HashMap<String, Object>();
            para.put("status", 2);// 2：已解冻
            if (!"".equals(startTimeBegin))
            {
                para.put("unfreezeTimeBegin", startTimeBegin);
            }
            if (!"".equals(startTimeEnd))
            {
                para.put("unfreezeTimeEnd", startTimeEnd);
            }
            List<Integer> freezeOrderIdList = findFreezeOrderIdByPara(para);
            List<Integer> secoundSearchOrderIdList = new ArrayList<Integer>();
            if (freezeOrderIdList.size() > 0)
            {
                // 导出商家发货表时存在解冻订单
                for (Integer it : freezeOrderIdList)
                {
                    boolean flag = true;
                    for (OrderDetailInfoForSeller curr : reList)
                    {
                        Integer currId = Integer.parseInt(curr.getoId());
                        if (currId == it)
                        {
                            flag = false;
                            break;
                        }
                    }
                    if (flag)
                    {
                        secoundSearchOrderIdList.add(it);
                    }
                }
            }
            if (secoundSearchOrderIdList.size() > 0)
            {
                // 存在需要再次查询的订单
                Map<String, Object> secondSearchPara = new HashMap<String, Object>();
                secondSearchPara.put("idList", secoundSearchOrderIdList);
                secondSearchPara.put("orderStatus", OrderEnum.ORDER_STATUS.REVIEW.getCode());
                secondSearchPara.put("isFreeze", 2);// 已解冻的订单 此处要区分order_freeze.status 的不同
                if (sellerIdList != null)
                {
                    secondSearchPara.put("sellerIdList", sellerIdList);
                }
                List<OrderDetailInfoForSeller> secReList = getSqlSession().selectList("OrderMapper.findAllOrderInfoForSeller", secondSearchPara);
                if (secReList.size() > 0)
                {
                    reList.addAll(secReList);
                    HttpSession session = map.get("session") == null ? null : (HttpSession)map.get("session");
                    if (session != null)
                    {
                        session.setAttribute("secoundSearchOrderIdList", secoundSearchOrderIdList);// 导出时需要判断的
                    }
                }
            }
        }
        // }
        for (OrderDetailInfoForSeller it : reList)
        {
            if (it.getIsGroup() == 1)
            {
                it.setSalesPrice(it.getGroupPrice());
            }
        }
        return reList;
    }

    @Override
    public int updateOrderOperationStatus(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().update("OrderMapper.updateOrderOperationStatus", para);
    }

    @Override
    public int updateOrderAddress(ReceiveAddressEntity entity)
            throws Exception
    {
        return getSqlSession().update("OrderMapper.updateOrderAddress", entity);
    }

    @Override
    public List<Map<String, Object>> findAllUnSendGoodInfo(Map<String, Object> para)
            throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("OrderMapper.findAllUnSendGoodsInfo", para);
        return reList == null ? new ArrayList<Map<String, Object>>() : reList;
    }

    @Override
    public List<Map<String, Object>> findAllUnSendGoodsDetail(Map<String, Object> para)
            throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("OrderMapper.findAllUnSendGoodsDetail", para);
        return reList == null ? new ArrayList<Map<String, Object>>() : reList;
    }

    @Override
    public int countAllUnSendGoodsDetail(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectOne("OrderMapper.countAllUnSendGoodsDetail", para);
    }

    // --------------------begin------------------------订单发货记录--------------------------------------------

    @Override
    public int insertOrderSendRecord(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().insert("OrderMapper.insertOrderSendRecord", para);
    }

    @Override
    public Map<String, Object> findOrderSendRecordByOrderId(Integer orderId)
            throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("orderId", orderId);
        map.put("start", 0);
        map.put("max", 1);
        List<Map<String, Object>> reList = findAllOrderSendRecordInfo(map);
        if (reList.size() > 0)
        {
            return reList.get(0);
        }
        else
        {
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> findAllOrderSendRecordInfo(Map<String, Object> para)
            throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("OrderMapper.findAllOrderSendRecord", para);
        return reList == null ? new ArrayList<Map<String, Object>>() : reList;
    }

    @Override
    public int updateOrderSendRecord(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().update("OrderMapper.updateOrderSendRecord", para);
    }

    // --------------------end------------------------订单发货记录--------------------------------------------
    @Override
    public Map<String, Object> findReceiveInfoById(int id)
            throws Exception
    {
        return getSqlSession().selectOne("OrderMapper.findReceiveInfoById", id);
    }

    // ----------------------------------------订单数据统计--------------------------------------------------------------

    @Override
    public List<Map<String, Object>> findOrderSalesRecord(Map<String, Object> para)
            throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("OrderMapper.findOrderSalesRecord", para);
        return reList == null ? new ArrayList<Map<String, Object>>() : reList;
    }

    @Override
    public List<Integer> findOrderAccountIdByPara(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findOrderAccountIdByPara", para);
    }

    @Override
    public boolean checkOrderIsOverseasOrder(int orderId)
            throws Exception
    {
        Integer id = getSqlSession().selectOne("OrderMapper.checkOrderIsOverseasOrder", orderId);
        if (id == null)
        {
            return false;
        }
        return true;
    }

    @Override
    public List<Map<String, Object>> findAllOrderChannel(Map<String, Object> para)
            throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("OrderMapper.findAllOrderChannel", para);
        return reList == null ? new ArrayList<Map<String, Object>>() : reList;
    }

    @Override
    public int countOrderChannel(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectOne("OrderMapper.countOrderChannel", para);
    }

    @Override
    public int saveOrderChannel(Map<String, Object> para)
            throws Exception
    {
        // 为了插入后自动赋值改字段
        para.put("id", 0);
        return getSqlSession().insert("OrderMapper.saveOrderChannel", para);
    }

    @Override
    public int updateOrderChannel(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().insert("OrderMapper.updateOrderChannel", para);
    }

    @Override
    public int deleteOrderChannel(int id)
            throws Exception
    {
        return getSqlSession().delete("OrderMapper.deleteOrderChannel", id);
    }

    @Override
    public int countOrderBySourceChannelId(int id)
            throws Exception
    {
        return getSqlSession().selectOne("OrderMapper.countOrderBySourceChannelId", id);
    }

    @Override
    public List<Integer> findProductIdListByOrderId(int id)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findProductIdListByOrderId", id);
    }

    @Override
    public List<Map<String, Object>> findAllTodaySaleRelOrderProduct(Map<String, Object> para)
            throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("OrderMapper.findAllTodaySaleRelOrderProduct", para);
        for (Map<String, Object> map : reList)
        {
            int activityType = Integer.parseInt(map.get("isGroup") == null ? "0" : map.get("isGroup") + "");
            if (activityType == 1)
            {
                double groupPrice = Double.parseDouble(map.get("groupPrice") + "");
                map.put("salesPrice", groupPrice);
            }
        }
        return reList;
    }

    @Override
    public List<Map<String, Object>> findProductStockAndNameByProductIdList(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findProductStockAndNameByProductIdList", para);
    }

    @Override
    public String sumOrderPrice(Integer orderId)
            throws Exception
    {
        // return getSqlSession().selectOne("OrderMapper.sumOrderPrice", orderId);
        List<Map<String, Object>> reList = findOrderProductByOrderId(orderId);
        double totalProductPrice = 0;
        for (Map<String, Object> it : reList)
        {
            double salesPrice = Double.parseDouble(it.get("salesPrice") + "");
            int productCount = Integer.parseInt(it.get("productCount") + "");
            totalProductPrice += (salesPrice * productCount);
        }
        return MathUtil.round(totalProductPrice, 2);
    }

    @Override
    public List<Map<String, Object>> findAccountSpending()
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findAccountSpending");
    }

    @Override
    public int countAllFakeOrder(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectOne("OrderMapper.countAllFakeOrder", para);
    }

    @Override
    public List<Map<String, Object>> queryAllFakeOrder(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.queryAllFakeOrder", para);
    }

    @Override
    public int orderLogisticsAgain(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().update("OrderMapper.orderLogisticsAgain", para);
    }

    @Override
    public int updateOrderSettlement(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().update("OrderMapper.updateOrderSettlement", para);
    }

    @Override
    public int updateOrderIsNeedSettlement(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().update("OrderMapper.updateOrderIsNeedSettlement", para);
    }

    @Override
    public List<Map<String, Object>> findProductInfoByOrderId(int id)
            throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("OrderMapper.findProductInfoByOrderId", id);
        for (Map<String, Object> map : reList)
        {
            int activityType = Integer.parseInt(map.get("isGroup") == null ? "0" : map.get("isGroup") + "");
            if (activityType == 1)
            {
                double groupPrice = Double.parseDouble(map.get("groupPrice") + "");
                map.put("salesPrice", groupPrice);
            }
        }
        return reList;
    }

    @Override
    public List<Map<String, Object>> findAllOrderFreeze(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findAllOrderFreeze", para);
    }

    @Override
    public int countOrderFreeze(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectOne("OrderMapper.countOrderFreeze", para);
    }

    @Override
    public Map<String, Object> findOrderFreezeByOrderId(int id)
            throws DaoException
    {
        return getSqlSession().selectOne("OrderMapper.findOrderFreezeByOrderId", id);
    }

    @Override
    public int insertOrderFreeze(int orderId)
            throws DaoException
    {
        return getSqlSession().insert("OrderMapper.insertOrderFreeze", orderId);
    }

    @Override
    public int updateOrderFreeze(int orderId, int isFreeze)
            throws DaoException
    {
        Map<String, Object> para = new HashMap<String, Object>();
        para.put("orderId", orderId);
        para.put("isFreeze", isFreeze);
        return getSqlSession().update("OrderMapper.updateOrderFreeze", para);
    }

    @Override
    public int updateOrderFreezeRecord(Map<String, Object> para)
            throws DaoException
    {
        return getSqlSession().update("OrderMapper.updateOrderFreezeRecord", para);
    }

    @Override
    public int updateOrderFreezeRecord(int orderFreezeRecordId)
            throws DaoException
    {
        Map<String, Object> para = new HashMap<String, Object>();
        para.put("id", orderFreezeRecordId);
        para.put("unfreezeTime", "0000-00-00 00:00:00");
        para.put("status", 1);
        return getSqlSession().update("OrderMapper.updateOrderFreezeRecord1", para);
    }

    @Override
    public List<Integer> findFreezeOrderIdByPara(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findFreezeOrderIdByPara", para);
    }

    @Override
    public int findOrderIdByNumber(long number)
            throws Exception
    {
        Integer id = getSqlSession().selectOne("OrderMapper.findOrderIdByNumber", number);
        return id == null ? -1 : id;
    }

    @Override
    public Map<String, Object> findOrderSettlementByNumber(long number)
            throws Exception
    {
        int orderId = findOrderIdByNumber(number);
        if (orderId == -1)
        {
            return null;
        }
        return getSqlSession().selectOne("OrderMapper.findOrderSettlementByOrderId", orderId);
    }

    @Override
    public int insertOrderSettlement(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().insert("OrderMapper.insertOrderSettlement", para);
    }

    @Override
    public int deleteOrderSettlement(int orderId)
            throws Exception
    {
        return getSqlSession().delete("OrderMapper.deleteOrderSettlement", orderId);
    }

    @Override
    public int updateOrderProductCost(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().update("OrderMapper.updateOrderProductCost", para);
    }

    @Override
    public String findPayTidOrderAliPay(int orderId)
            throws Exception
    {
        return getSqlSession().selectOne("OrderMapper.findPayTidOrderAliPay", orderId);
    }

    @Override
    public String findPayTidOrderWeixinPay(int orderId)
            throws Exception
    {
        return getSqlSession().selectOne("OrderMapper.findPayTidOrderWeixinPay", orderId);
    }

    @Override
    public String findPayTidOrderUnionPay(int orderId)
            throws Exception
    {
        return getSqlSession().selectOne("OrderMapper.findPayTidOrderUnionPay", orderId);
    }

    @Override
    public String findPayMarkTidOrderAliPay(int orderId)
            throws Exception
    {
        return getSqlSession().selectOne("OrderMapper.findPayMarkTidOrderAliPay", orderId);
    }

    @Override
    public String findPayMarkTidOrderWeixinPay(int orderId)
            throws Exception
    {
        return getSqlSession().selectOne("OrderMapper.findPayMarkTidOrderWeixinPay", orderId);
    }

    @Override
    public boolean alreadyConfirmOrderByOrderId(int orderId)
            throws Exception
    {
        Integer id = getSqlSession().selectOne("OrderMapper.findBirdexOrderConfirmByOrderId", orderId);
        return id != null;
    }

    @Override
    public Map<String, Object> findIdcardRealnameMappingByIdCard(String idCard)
            throws Exception
    {
        return getSqlSession().selectOne("OrderMapper.findIdcardRealnameMappingByIdCard", idCard);
    }

    @Override
    public List<Map<String, Object>> findOrderProductSettlementInfo(List<Integer> orderIdList)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findOrderProductSettlementInfo", orderIdList);
    }

    @Override
    public List<Map<String, Object>> userBehaviorAnalyze(Map<String, Object> para)
            throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("OrderMapper.userBehaviorAnalyze", para);
        return reList == null ? new ArrayList<Map<String, Object>>() : reList;
    }

    @Override
    public int countZeroBuyUser(Map<String, Object> para)
            throws Exception
    {
        return this.getSqlSession().selectOne("OrderMapper.countZeroBuyUser", para);
    }

    @Override
    public Map<String, Object> countNextBuyUser(Map<String, String> para)
            throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("OrderMapper.countNextBuyUser", para);
        return reList == null ? new HashMap<String, Object>() : reList.get(0);
    }

    @Override
    public List<Integer> findZeroBuyAccount(Map<String, Object> para)
            throws Exception
    {
        List<Integer> reList = getSqlSession().selectList("OrderMapper.findZeroBuyAccount", para);
        return reList == null ? new ArrayList<Integer>() : reList;
    }

    @Override
    public List<Map<String, Object>> findProductNameAndTypeByOrderId(String orderId)
            throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("OrderMapper.findProductNameAndTypeByOrderId", orderId);
        return reList == null ? new ArrayList<Map<String, Object>>() : reList;
    }

    @Override
    public String findReceiveMobileNumberByOrderId(String orderId)
            throws Exception
    {
        return this.getSqlSession().selectOne("OrderMapper.findReceiveMobileNumberByOrderId", orderId);
    }

    @Override
    public List<Map<String, Object>> findSaleDataByDate(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findSaleDataByDate", para);
    }

    @Override
    public List<Map<String, Object>> findQqbsSaleDataByDate(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findQqbsSaleDataByDate", para);
    }

    @Override
    public List<Map<String, Object>> findYWSaleDataByDate(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findYWSaleDataByDate", para);
    }

    @Override
    public String findHBOrderByParentNumber(String number)
            throws Exception
    {
        return getSqlSession().selectOne("OrderMapper.findAllHBOrderRecord", number);
    }

    @Override
    public List<OrderLogistics> findAllOrderLogisticsBychannelAndNumber(Map<String, String> searchMap)
            throws Exception
    {
        List<OrderLogistics> reList = this.getSqlSession().selectList("OrderMapper.findAllOrderLogisticsBychannelAndNumber", searchMap);
        return reList == null ? new ArrayList<OrderLogistics>() : reList;
    }

    @Override
    public int findOrderSignRecordIdByOrderId(int orderId)
            throws Exception
    {
        Integer id = getSqlSession().selectOne("OrderMapper.findOrderSignRecordIdByOrderId", orderId);
        if (id == null)
        {
            return -1;
        }
        return id;
    }

    @Override
    public int addOrderSignRecordIdByOrderId(int orderId)
            throws Exception
    {
        return getSqlSession().insert("OrderMapper.addOrderSignRecordIdByOrderId", orderId);
    }

    @Override
    public List<Map<String, Object>> findOrderPayRecordForSendTimeAnalyze(Map<String, Object> para)
            throws Exception
    {
        List<Map<String, Object>> reList = this.getSqlSession().selectList("OrderMapper.findOrderPayRecordForSendTimeAnalyze", para);
        return reList == null ? new ArrayList<Map<String, Object>>() : reList;
    }

    @Override
    public List<Map<String, Object>> findOrderPayRecordForLogisticAnalyze(Map<String, Object> para)
            throws Exception
    {
        List<Map<String, Object>> reList = this.getSqlSession().selectList("OrderMapper.findOrderPayRecordForLogisticAnalyze", para);
        return reList == null ? new ArrayList<Map<String, Object>>() : reList;
    }

    @Override
    public DateTime finOrderLogisticsTimeByChannelAndNumber(String channel, String number)
            throws Exception
    {
        DateTime dateTime = null;
        Map<String, String> para = new HashMap<String, String>();
        para.put("channel", channel);
        para.put("number", number);
        String createTime = this.getSqlSession().selectOne("OrderMapper.finOrderLogisticsTimeByChannelAndNumber", para);
        if (StringUtils.isNotEmpty(createTime))
        {
            dateTime = DateTimeUtil.string2DateTime(createTime, "yyyy-MM-dd HH:mm:ss.SSS");
        }
        return dateTime;
    }

    @Override
    public int countAllOrderSendTimeAnalyze(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectOne("OrderMapper.countAllOrderSendTimeAnalyze", para);
    }

    @Override
    public List<OrderInfoForManage> findAllOrderSendTimeAnalyze(Map<String, Object> para)
            throws Exception
    {
        List<OrderInfoForManage> reList = getSqlSession().selectList("OrderMapper.findAllOrderSendTimeAnalyze", para);
        return reList == null ? new ArrayList<OrderInfoForManage>() : reList;
    }

    @Override
    public int countAllOrderLogisticAnalyzeDetail(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectOne("OrderMapper.countAllOrderLogisticAnalyzeDetail", para);
    }

    @Override
    public List<OrderInfoForManage> findAllOrderLogisticAnalyzeDetail(Map<String, Object> para)
            throws Exception
    {
        List<OrderInfoForManage> reList = getSqlSession().selectList("OrderMapper.findAllOrderLogisticAnalyzeDetail", para);
        return reList == null ? new ArrayList<OrderInfoForManage>() : reList;
    }

    @Override
    public List<Map<String, Object>> findSimpleOrderInfoByPara(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findSimpleOrderInfoByPara", para);
    }

    @Override
    public List<Map<String, Object>> findAllOrderProductInfoByPara(Map<String, Object> para)
            throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("OrderMapper.findAllOrderProductInfoByPara", para);
        for (Map<String, Object> map : reList)
        {
            int activityType = Integer.parseInt(map.get("isGroup") == null ? "0" : map.get("isGroup") + "");
            if (activityType == 1)
            {
                double groupPrice = Double.parseDouble(map.get("groupPrice") + "");
                map.put("salesPrice", groupPrice);
            }
        }
        return reList;
    }

    @Override
    public List<Integer> findOldBuyAccountIds(String payTime)
            throws Exception
    {
        List<Integer> reList = this.getSqlSession().selectList("OrderMapper.findOldBuyAccountIds", payTime);
        return reList == null ? new ArrayList<Integer>() : reList;
    }

    @Override
    public List<Map<String, Object>> userFirstBehaviorAnalyze(Map<String, Object> para)
            throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("OrderMapper.userFirstBehaviorAnalyze", para);
        return reList == null ? new ArrayList<Map<String, Object>>() : reList;
    }

    @Override
    public int countFirstZeroBuyUser(Map<String, Object> para)
            throws Exception
    {
        return this.getSqlSession().selectOne("OrderMapper.countFirstZeroBuyUser", para);
    }

    @Override
    public Map<String, Object> countFirstNextBuyUser(Map<String, Object> seach)
            throws Exception
    {
        Map<String, Object> reList = getSqlSession().selectOne("OrderMapper.countFirstNextBuyUser", seach);
        return reList == null ? new HashMap<String, Object>() : reList;
    }

    @Override
    public List<Map<String, Object>> findOrderSalesRecordForMonthAnalyze(Map<String, Object> para)
            throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("OrderMapper.findOrderSalesRecordForMonthAnalyze", para);
        List<Map<String, Object>> reList2 = getSqlSession().selectList("OrderMapper.findOrderSalesRecordForMonthAnalyze2", para);
        reList.addAll(reList2);
        return reList;
    }

    @Override
    public List<Map<String, Object>> findOrderSalesRecordForPlatformMonthAnalyze(Map<String, Object> para) throws Exception {
        List<Map<String, Object>> reList = getSqlSession().selectList("OrderMapper.findOrderSalesRecordForPlatformMonthAnalyze", para);
        return reList;
    }

    @Override
    public int countAllProblemOrder(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectOne("OrderMapper.countAllProblemOrder", para);
    }

    @Override
    public List<Map<String, Object>> findAllProblemOrder(Map<String, Object> para)
            throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("OrderMapper.findAllProblemOrder", para);
        return reList == null ? new ArrayList<Map<String, Object>>() : reList;
    }

    @Override
    public int batchInsertOutOrderHbRecord(List<Map<String, Object>> insertList)
            throws Exception
    {
        return getSqlSession().insert("OrderMapper.batchInsertOutOrderHbRecord", insertList);
    }

    @Override
    public List<Map<String, Object>> findOutOrderHbRecordByHbNumberList(List<Long> numberList)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findOutOrderHbRecordByHbNumberList", numberList);
    }

    @Override
    public List<Integer> findOrderIdByCouponAccountId(int couponAccountId)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findOrderIdByCouponAccountId", couponAccountId);
    }

    @Override
    public List<Integer> findOrderReceiveAddressIdListByFullNameAndPhone(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findOrderReceiveAddressIdListByFullNameAndPhone", para);
    }

    @Override
    public List<Integer> findOrderIdListByOrderLogisticsNumber(String number)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findOrderIdListByOrderLogisticsNumber", number);
    }

    @Override
    public List<Map<String, Object>> findAllOrderInfoForList(Map<String, Object> para)
            throws Exception
    {
        /*
         * 2016-2-24 吴灵修改
         * if ("master".equals(para.get("datasource")))
        {
            return getSqlSession().selectList("OrderMapper.findAllOrderInfoForList", para);
        }
        else
        {*/
        return getSqlSession().selectList("OrderMapper.findAllOrderInfoForList", para);
        /*}*/
    }

    @Override
    public int countAllOrderInfoForList(Map<String, Object> para)
            throws Exception
    {
//        if ("master".equals(para.get("datasource")))
//        {
        return getSqlSession().selectOne("OrderMapper.countAllOrderInfoForList", para);
//        }
//        else
//        {
//            return getSqlSession().selectOne("OrderMapper.countAllOrderInfoForList", para);
//        }
    }

    @Override
    public List<Map<String, Object>> findAllOrderInfoForList2(Map<String, Object> para)
            throws Exception
    {
//        if ("master".equals(para.get("datasource")))
//        {
        return getSqlSession().selectList("OrderMapper.findAllOrderInfoForList2", para);
//        }
//        else
//        {
//            return getSqlSessionRead().selectList("OrderMapper.findAllOrderInfoForList2", para);
//        }
    }

    @Override
    public int countAllOrderInfoForList2(Map<String, Object> para)
            throws Exception
    {
//        if ("master".equals(para.get("datasource")))
//        {
        return getSqlSession().selectOne("OrderMapper.countAllOrderInfoForList2", para);
//        }
//        else
//        {
//            return getSqlSession().selectOne("OrderMapper.countAllOrderInfoForList2", para);
//        }
    }

    @Override
    public List<Integer> findAllOrderIdList(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findAllOrderIdList", para);
    }

    @Override
    public List<Integer> findAllOrderIdList2(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findAllOrderIdList2", para);
    }

    @Override
    public List<Map<String, Object>> findReceiveInfoByIdList(List<Integer> list)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findReceiveInfoByIdList", list);
    }

    @Override
    public List<Map<String, Object>> findSourceChannelInfoByIdList(List<Integer> list)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findSourceChannelInfoByIdList", list);
    }

    @Override
    public List<Map<String, Object>> findLogisticsInfoByIdList(List<Integer> list)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findLogisticsInfoByIdList", list);
    }

    @Override
    public List<Map<String, Object>> find_PayMarkTidOrderAliPay(int orderId)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.find_PayMarkTidOrderAliPay", orderId);
    }

    @Override
    public List<Map<String, Object>> find_PayMarkTidOrderWeixinPay(int orderId)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.find_PayMarkTidOrderWeixinPay", orderId);
    }

    @Override
    public List<Map<String, Object>> find_PayTidOrderUnionPay(int orderId)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.find_PayTidOrderUnionPay", orderId);
    }

    @Override
    public OrderEntity getOrderById(int id)
    {
        return getSqlSession().selectOne("OrderMapper.getOrderById", id);
    }

    @Override
    public List<JSONObject> findOrderAliPay(JSONObject j)
    {
        return getSqlSession().selectList("OrderMapper.findOrderAliPay", j);
    }

    @Override
    public List<JSONObject> findOrderWeixinPay(JSONObject j)
    {
        return getSqlSession().selectList("OrderMapper.findOrderWeixinPay", j);
    }

    @Override
    public List<JSONObject> findOrderUnionPay(JSONObject j)
    {
        return getSqlSession().selectList("OrderMapper.findOrderUnionPay", j);
    }

    @Override
    public int insertBirdexOrderConfirm(int orderId, int pushStatus)
            throws Exception
    {
        Map<String, Object> para = new HashMap<String, Object>();
        para.put("orderId", orderId);
        para.put("isPush", pushStatus);
        return getSqlSession().insert("OrderMapper.insertBirdexOrderConfirm", para);
    }

    @Override
    public int updateBirdexOrderPushStatus(int orderId, int pushStatus)
            throws Exception
    {
        Map<String, Object> para = new HashMap<String, Object>();
        para.put("orderId", orderId);
        para.put("isPush", pushStatus);
        return getSqlSession().update("OrderMapper.updateBirdexOrderPushStatus", para);
    }

    @Override
    public List<Map<String, Object>> findOrderProductCount(Map<String, Object> para)
            throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("OrderMapper.findOrderProductCount", para);
        return reList == null ? new ArrayList<Map<String, Object>>() : reList;
    }

    @Override
    public int addOrderEdbConfirm(int orderId, int isPush)
            throws Exception
    {
        Map<String, Object> para = new HashMap<>();
        para.put("orderId", orderId);
        para.put("isPush", isPush);
        return getSqlSession().insert("OrderMapper.addOrderEdbConfirm", para);
    }

    @Override
    public Map<String, Object> findOrderEdbConfirmByOrderId(int orderId)
            throws Exception
    {
        Map<String, Object> para = new HashMap<>();
        para.put("orderId", orderId);
        return getSqlSession().selectOne("OrderMapper.findOrderEdbConfirmByOrderId", para);
    }

    @Override
    public List<Map<String, Object>> findOrderEdbConfirmByOrderIdList(List<Integer> orderIdList)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findOrderEdbConfirmByOrderIdList", orderIdList);
    }

    @Override
    public List<Map<String, Object>> findEdbOrder(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findEdbOrder", para);
    }

    @Override
    public int countEdbOrder(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectOne("OrderMapper.countEdbOrder", para);
    }

    @Override
    public int updateOrderEdbConfirm(int orderId, int isPush)
            throws Exception
    {
        Map<String, Object> para = new HashMap<>();
        para.put("orderId", orderId);
        para.put("isPush", isPush);
        return getSqlSession().update("OrderMapper.updateOrderEdbConfirm", para);
    }

    @Override
    public List<Map<String, Object>> findOrderEdbSendRecordByOrderIdList(List<Integer> orderIdList)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findOrderEdbSendRecordByOrderIdList", orderIdList);
    }

    @Override
    public Map<String, Object> findOrderEdbSendRecordByOrderId(int orderId)
            throws Exception
    {
        List<Integer> orderIdList = new ArrayList<>();
        orderIdList.add(orderId);
        List<Map<String, Object>> list = getSqlSession().selectList("OrderMapper.findOrderEdbSendRecordByOrderIdList", orderIdList);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Map<String, Object>> findPushSuccessEdbOrder(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findPushSuccessEdbOrder", para);
    }

    @Override
    public int countPushSuccessEdbOrder(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectOne("OrderMapper.countPushSuccessEdbOrder", para);
    }

    @Override
    public List<Map<String, Object>> findPushErrorEdbOrder(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findPushErrorEdbOrder", para);
    }

    @Override
    public int countPushErrorEdbOrder(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectOne("OrderMapper.countPushErrorEdbOrder", para);
    }

    @Override
    public List<Map<String, Object>> findWaitPushEdbOrder(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findWaitPushEdbOrder", para);
    }

    @Override
    public int countWaitPushEdbOrder(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectOne("OrderMapper.countWaitPushEdbOrder", para);
    }

    @Override
    public List<Map<String, Object>> findNoPushEdbOrder(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findNoPushEdbOrder", para);
    }

    @Override
    public int countNoPushEdbOrder(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().selectOne("OrderMapper.countNoPushEdbOrder", para);
    }

    @Override
    public int countPushErrorEdbOrder()
            throws Exception
    {
        return getSqlSession().selectOne("OrderMapper.countPushErrorEdbOrder2");
    }

    @Override
    public List<Map<String, Object>> findOrderCheckListByOrderList(List<Integer> orderIdList)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findOrderCheckListByOrderList", orderIdList);
    }

    @Override
    public int saveOrderCheck(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().insert("OrderMapper.saveOrderCheck", para);
    }

    @Override
    public Map<String, Object> findLastLogisticsDetail(String channel, String number)
            throws Exception
    {
        Map<String, Object> para = new HashMap<>();
        para.put("channel", channel);
        para.put("number", number);
        return getSqlSession().selectOne("OrderMapper.findLastLogisticsDetail", para);
    }

    @Override
    public List<Map<String, Object>> findRefundForEveryday(Map<String, Object> param)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findRefundForEveryday", param);
    }

    @Override
    public List<Map<String, Object>> findCustomerRefundForEveryday(Map<String, Object> param)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findCustomerRefundForEveryday", param);
    }

    @Override
    public List<Map<String, Object>> findRefundForSeller(Map<String, Object> param)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findRefundForSeller", param);
    }

    @Override
    public List<Map<String, Object>> findCustomerRefundForSeller(Map<String, Object> param)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findCustomerRefundForSeller", param);
    }

    @Override
    public Map<String, Object> findOrderTimeoutSettlementByOrderId(int orderId)
            throws Exception
    {
        return getSqlSession().selectOne("OrderMapper.findOrderTimeoutSettlementByOrderId", orderId);
    }

    @Override
    public int saveOrderTimeoutSettlement(Map<String, Object> para)
            throws Exception
    {
        return getSqlSession().insert("OrderMapper.saveOrderTimeoutSettlement", para);
    }

    @Override
    public int deleteOrderTimeoutSettlement(int orderId)
            throws Exception
    {
        return getSqlSession().delete("OrderMapper.deleteOrderTimeoutSettlement", orderId);
    }

    @Override
    public List<Map<String, Object>> findOrderSalesRecordForQqbsMonthAnalyze(
            Map<String, Object> para) throws Exception
    {

        List<Map<String, Object>> reList = getSqlSession().selectList("OrderMapper.findOrderSalesRecordForQqbsMonthAnalyze", para);
        List<Map<String, Object>> reList2 = getSqlSession().selectList("OrderMapper.findOrderSalesRecordForQqbsMonthAnalyze2", para);
        reList.addAll(reList2);
        return reList;

    }


    @Override
    public List<Map<String, Object>> findOrderSalesRecordForYWMonthAnalyze(
            Map<String, Object> para) throws Exception
    {

        List<Map<String, Object>> reList = getSqlSession().selectList("OrderMapper.findOrderSalesRecordForYWMonthAnalyze", para);
        List<Map<String, Object>> reList2 = getSqlSession().selectList("OrderMapper.findOrderSalesRecordForYWMonthAnalyze2", para);
        reList.addAll(reList2);
        return reList;

    }


    @Override
    public List<Map<String, Object>> findQqbsAllTodaySaleRelOrderProduct(
            Map<String, Object> para) throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("OrderMapper.findQqbsAllTodaySaleRelOrderProduct", para);
        for (Map<String, Object> map : reList)
        {
            int activityType = Integer.parseInt(map.get("isGroup") == null ? "0" : map.get("isGroup") + "");
            if (activityType == 1)
            {
                double groupPrice = Double.parseDouble(map.get("groupPrice") + "");
                map.put("salesPrice", groupPrice);
            }
        }
        return reList;
    }


    @Override
    public List<Map<String, Object>> findYWAllTodaySaleRelOrderProduct(
            Map<String, Object> para) throws Exception
    {
        List<Map<String, Object>> reList = getSqlSession().selectList("OrderMapper.findYWAllTodaySaleRelOrderProduct", para);
        for (Map<String, Object> map : reList)
        {
            int activityType = Integer.parseInt(map.get("isGroup") == null ? "0" : map.get("isGroup") + "");
            if (activityType == 1)
            {
                double groupPrice = Double.parseDouble(map.get("groupPrice") + "");
                map.put("salesPrice", groupPrice);
            }
        }
        return reList;
    }

    @Override
    public List<OrderDetailInfoForSeller> findSellerUnSettleOrders(
            Map<String, Object> map) throws Exception
    {
        List<OrderDetailInfoForSeller> reList = getSqlSession().selectList("OrderMapper.findSellerUnSettleOrders", map);
        return reList;
    }

    @Override
    public List<OrderEntity> findOrdersBySameBatchNumber(String sameBatchNumber)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findOrdersBySameBatchNumber", sameBatchNumber);
    }

    @Override
    public List<Integer> findOrderIdsByWeiXinPayTid(String payTid)
            throws Exception
    {
        return getSqlSession().selectList("OrderMapper.findOrderIdsByWeiXinPayTid", payTid);
    }

    @Override
    public OrderPayEntity findOrderPayByOrderIdAndPayType(int orderId, int payType) throws Exception {

        if (payType == OrderEnum.PAY_CHANNEL.UNION.getCode())
        {
            return getSqlSession().selectOne("OrderMapper.findOrderUnionPayByOrderId",orderId);
        }
        else if (payType == OrderEnum.PAY_CHANNEL.ALIPAY.getCode())
        {
            return getSqlSession().selectOne("OrderMapper.findOrderAliPayByOrderId",orderId);
        }
        else if (payType == OrderEnum.PAY_CHANNEL.WEIXIN.getCode())
        {
            return getSqlSession().selectOne("OrderMapper.findOrderWeixinPayByOrderId",orderId);
        }
        else
        {
            return  null;
        }
    }


    @Override
    public List<Integer> findOrderIdsByPayTidAndPayType(String payTid, int payType) throws Exception {
        if (payType == OrderEnum.PAY_CHANNEL.UNION.getCode())
        {
            return getSqlSession().selectList("OrderMapper.findOrderIdsByUnionPayTid",payTid);
        }
        else if (payType == OrderEnum.PAY_CHANNEL.ALIPAY.getCode())
        {
            return getSqlSession().selectList("OrderMapper.findOrderIdsByAliPayTid",payTid);
        }
        else if (payType == OrderEnum.PAY_CHANNEL.WEIXIN.getCode())
        {
            return getSqlSession().selectList("OrderMapper.findOrderIdsByWeixinPayTid",payTid);
        }
        else
        {
            return  null;
        }
    }
    
    public int freezePointOfOrder(int orderId)throws Exception{
    	return getSqlSession().update("OrderMapper.freezePointOfOrder",orderId);
    }
}
