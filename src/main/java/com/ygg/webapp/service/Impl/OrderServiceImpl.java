package com.ygg.webapp.service.Impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ygg.webapp.dao.AccountDao;
import com.ygg.webapp.dao.AreaDao;
import com.ygg.webapp.dao.OrderDao;
import com.ygg.webapp.dao.ProductDao;
import com.ygg.webapp.dao.ReceiveAddressDao;
import com.ygg.webapp.dao.RefundDao;
import com.ygg.webapp.dao.SellerDao;
import com.ygg.webapp.entity.AccountEntity;
import com.ygg.webapp.entity.CityEntity;
import com.ygg.webapp.entity.DistrictEntity;
import com.ygg.webapp.entity.OrderDetailInfoForSeller;
import com.ygg.webapp.entity.OrderEntity;
import com.ygg.webapp.entity.OrderInfoForManage;
import com.ygg.webapp.entity.OrderReceiveAddress;
import com.ygg.webapp.entity.ProvinceEntity;
import com.ygg.webapp.entity.ReceiveAddressEntity;
import com.ygg.webapp.entity.RefundEntity;
import com.ygg.webapp.entity.SellerEntity;
import com.ygg.webapp.exception.ServiceException;
import com.ygg.webapp.service.OrderService;
import com.ygg.webapp.util.CommonConstant;
import com.ygg.webapp.util.CommonEnum;
import com.ygg.webapp.util.CommonUtil;
import com.ygg.webapp.util.DateTimeUtil;
import com.ygg.webapp.util.MathUtil;
import com.ygg.webapp.util.OrderEnum;
import com.ygg.webapp.util.POIUtil;
import com.ygg.webapp.util.SellerEnum;
import com.ygg.webapp.util.UtilProperties;
import com.ygg.webapp.util.KdCompanyEnum;

@Service("orderService")
public class OrderServiceImpl implements OrderService
{
    
    @Resource(name = "orderDao")
    private OrderDao orderDao;
    
    @Resource
    private AccountDao accountDao;
    
    @Resource
    private SellerDao sellerDao;
    
    @Resource
    private ReceiveAddressDao receiveAddressDao;
    
    
    @Resource
    private AreaDao areaDao;
    
    @Resource
    private RestTemplate restTemplate;
    
    @Resource
    private RefundDao refundDao;
    
    @Resource
    private ProductDao productDao;

    Logger log = Logger.getLogger(OrderServiceImpl.class);
    
    @Override
    public String jsonOrderInfo(Map<String, Object> para)
        throws Exception
    {
        List<OrderEntity> orderList = orderDao.findOrder(para);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        int total = 0;
        if (orderList != null && orderList.size() > 0)
        {
            for (OrderEntity order : orderList)
            {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("id", order.getId());
                map.put("status", order.getStatus());
                map.put("number", order.getNumber());
                map.put("totalPrice", order.getTotalPrice());
                map.put("createTime", order.getCreateTime());
                map.put("payTime", order.getPayTime());
                map.put("sendTime", order.getSendTime());
                resultList.add(map);
            }
            total = orderDao.countOrder(para);
        }
        resultMap.put("rows", resultList);
        resultMap.put("total", total);
        return JSON.toJSONString(resultMap);
    }
    
    List<Map<String, Object>> packageOrderInfo(List<Map<String, Object>> info, int checkOrder)
        throws Exception
    {
        List<Integer> orderIdList = new ArrayList<>();
        List<Integer> receiveAddressIdList = new ArrayList<>();
        Set<Integer> sellerIdList = new HashSet<>();
        Set<Integer> sourceChannelIdList = new HashSet<>();
        
        for (Map<String, Object> it : info)
        {
            int id = Integer.valueOf(it.get("id") + "");
            int sourceChannelId = Integer.valueOf(it.get("sourceChannelId") + "");
            int receiveAddressId = Integer.valueOf(it.get("receiveAddressId") + "");
            int sellerId = Integer.valueOf(it.get("sellerId") + "");
            orderIdList.add(id);
            receiveAddressIdList.add(receiveAddressId);
            sellerIdList.add(sellerId);
            sourceChannelIdList.add(sourceChannelId);
        }
        
        // 查询收货人信息
        Map<String, Map<String, Object>> receiveInfoMap = new HashMap<>();
        if (receiveAddressIdList.size() > 0)
        {
            List<Map<String, Object>> list = orderDao.findReceiveInfoByIdList(receiveAddressIdList);
            for (Map<String, Object> it : list)
            {
                String id = it.get("id") + "";
                receiveInfoMap.put(id, it);
            }
        }
        
        // 查询商家信息
        Map<String, Map<String, Object>> sellerInfoMap = new HashMap<>();
        if (sellerIdList.size() > 0)
        {
            Map<String, Object> _sp = new HashMap<>();
            _sp.put("idList", CommonUtil.setToList(sellerIdList));
            List<Map<String, Object>> sellerList = sellerDao.findSellerInfoBySellerIdList(_sp);
            for (Map<String, Object> it : sellerList)
            {
                String id = it.get("id") + "";
                sellerInfoMap.put(id, it);
            }
        }
        
        // 查询order_source_channel
        Map<String, Map<String, Object>> sourceChannelInfoMap = new HashMap<>();
        if (sourceChannelIdList.size() > 0)
        {
            List<Map<String, Object>> list = orderDao.findSourceChannelInfoByIdList(CommonUtil.setToList(sourceChannelIdList));
            for (Map<String, Object> it : list)
            {
                String id = it.get("id") + "";
                sourceChannelInfoMap.put(id, it);
            }
        }
        
        // 查询发货信息
        Map<String, Map<String, Object>> logisticsInfoMap = new HashMap<>();
        if (orderIdList.size() > 0)
        {
            List<Map<String, Object>> list = orderDao.findLogisticsInfoByIdList(orderIdList);
            for (Map<String, Object> it : list)
            {
                String id = it.get("orderId") + "";
                logisticsInfoMap.put(id, it);
            }
        }
        // 查询订单审核信息
        Map<String, Map<String, Object>> orderCheckInfoMap = new HashMap<>();
        if (orderIdList.size() > 0 && checkOrder == 1)
        {
            List<Map<String, Object>> list = orderDao.findOrderCheckListByOrderList(orderIdList);
            for (Map<String, Object> it : list)
            {
                String id = it.get("orderId") + "";
                orderCheckInfoMap.put(id, it);
            }
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> it : info)
        {
            int status = Integer.valueOf(it.get("status") + "");
            String number = it.get("number") + "";
            it.put("createTime", it.get("createTime").toString());
            it.put("expireTime", it.get("expireTime") == null ? "" : it.get("expireTime").toString());
            it.put("payTime", it.get("payTime") == null ? "" : it.get("payTime").toString());
            it.put("sendTime", it.get("sendTime") == null ? "" : it.get("sendTime").toString());
            it.put("payChannel", OrderEnum.PAY_CHANNEL.getDescByCode(Integer.valueOf(it.get("payChannel") + "")));
            it.put("oStatusDescripton", OrderEnum.ORDER_STATUS.getDescByCode(status));
            int id = Integer.valueOf(it.get("id") + "");
            List<Map<String, Object>> orderProducts = orderDao.findAllOrderProductInfo(id);
            String orderDetail = "";
            for (Map<String, Object> map : orderProducts)
            {
            	orderDetail += map.get("pName")+"("+map.get("price")+"*"+map.get("count")+")";
            }
            it.put("orderDetail",orderDetail);
            List<Map<String, Object>> refundMap = refundDao.selectRefundByOrderId(id);
            for(Map<String, Object> map:refundMap){
            	if(refundMap!=null){
            		int type = Integer.parseInt(map.get("status") == null ? "0" : map.get("status") + "");
                	if(type==1){
                		it.put("oStatusDescripton", "退款申请中");
                	}else if(type==2){
                		it.put("oStatusDescripton", "待退货");
                	}else if(type==3){
                		it.put("oStatusDescripton", "待退款");
                	}else if(type==4){
                		it.put("oStatusDescripton", "退款成功");
                	}else if(type==5){
                		it.put("oStatusDescripton", "退款关闭");
                	}else if(type==6){
                		it.put("oStatusDescripton", "退款取消");
                	}
            	}
            }
            
            it.put("totalPrice", MathUtil.round(it.get("totalPrice") + "", 2));
            it.put("realPrice", MathUtil.round(it.get("realPrice") + "", 2));
            // 尾1:app,2:wap
            int appChannel = Integer.valueOf(it.get("appChannel") == null ? "0" : it.get("appChannel") + "");
            int orderType = Integer.valueOf(it.get("type") == null ? "1" : it.get("type") + "");
            if (number.endsWith(CommonConstant.ORDER_SUFFIX_APP))
            {
                String appVersion = it.get("appVersion") + "";
                appVersion = "".equals(appVersion) ? "" : "(" + appVersion + ")";
                it.put("orderChannel", CommonEnum.OrderAppChannelEnum.getDescriptionByOrdinal(appChannel) + appVersion);
                
            }
            else if (number.endsWith(CommonConstant.ORDER_SUFFIX_WAP))
            {
                if (orderType == OrderEnum.ORDER_TYPE.HUANQIUBUSHOU.getCode())
                {
                    it.put("orderChannel", "心动慈露");
                }
                else if (orderType == OrderEnum.ORDER_TYPE.YANWANG.getCode())
                {
                    it.put("orderChannel", "燕网");
                }
                else
                {
                    it.put("orderChannel", "网页");
                }
            }
            else if (number.endsWith(CommonConstant.ORDER_SUFFIX_GROUP))
            {
                it.put("orderChannel", CommonEnum.OrderAppChannelEnum.getDescriptionByOrdinal(appChannel));
            }
            
            if (Integer.valueOf(it.get("type") + "") == 1)
            {
                it.put("orderType", "心动慈露");
                if (appChannel == CommonEnum.OrderAppChannelEnum.QUAN_QIU_BU_SHOU.ordinal())
                {
                    it.put("orderType", "心动慈露");
                }
            }
            else if (Integer.valueOf(it.get("type") + "") == 2)
            {
                it.put("orderType", "心动慈露");
            }
            else if (Integer.valueOf(it.get("type") + "") == OrderEnum.ORDER_TYPE.HUANQIUBUSHOU.getCode())
            {
                it.put("orderType", "心动慈露");
            }
            else
            {
                it.put("orderType", "心动慈露");
            }
            
            int isSettlement = Integer.valueOf(it.get("isSettlement") + "");
            it.put("isSettlement", isSettlement == 1 ? "已结算" : "未结算");
            
            // 需要查询收货人信息
            String receiveAddressId = it.get("receiveAddressId") + "";
            Map<String, Object> r = receiveInfoMap.get(receiveAddressId);
            it.put("raFullName", r != null ? r.get("fullName") : "");
            it.put("raMobileNumber", r != null ? r.get("mobileNumber") : "");
            
            // 需要查询商家信息
            String sellerId = it.get("sellerId") + "";
            Map<String, Object> s = sellerInfoMap.get(sellerId);
            it.put("sSellerName", s != null ? s.get("realSellerName") : "");
            it.put("sSendAddress", s != null ? s.get("sendAddress") : "");
            
            // 需要查询order_source_channel
            String sourceId = it.get("sourceChannelId") + "";
            Map<String, Object> s1 = sourceChannelInfoMap.get(sourceId);
            it.put("orderSource", s1 != null ? s1.get("name") : "");
            
            // 查询发货信息
            String orderId = it.get("id") + "";
            Map<String, Object> o = logisticsInfoMap.get(orderId);
            it.put("ologChannel", o != null ? o.get("channel") : "");
            it.put("ologNumber", o != null ? o.get("number") : "");
            it.put("ologMoney", o != null ? o.get("money") : "" + "");
            
            int isFreeze = Integer.valueOf(it.get("isFreeze") + "");
            if (isFreeze == 1)
            {
                it.put("refundStatus", "退款冻结");
            }
            else if (isFreeze == 2)
            {
                it.put("refundStatus", "");
            }
            else
            {
                it.put("refundStatus", "");
            }
            
            // 订单超时信息
            Map<String, Object> timeoutMap =null;// timeoutOrderInfoMap.get(orderId);
            it.put("timeOutReason", timeoutMap == null ? "" : timeoutMap.get("reason") == null ? "" : timeoutMap.get("reason") + "");
            it.put("timeOutResult", timeoutMap == null ? "未申诉" : OrderEnum.TIMEOUT_COMPLAIN_RESULT.getNameByCode(Integer.parseInt(timeoutMap.get("status").toString())));
            it.put("timeOutResultStatus", timeoutMap == null ? "0" : timeoutMap.get("status") + "");
            it.put("timeOutReasonId", timeoutMap == null ? "0" : timeoutMap.get("reasonId") == null ? 0 : timeoutMap.get("reasonId") + "");
            
            // 订单审核信息
            if (checkOrder == 1)
            {
                Map<String, Object> map = orderCheckInfoMap.get(orderId);
                String checkTime = map == null ? "" : (map.get("checkTime") == null ? "" : DateTimeUtil.timestampObjectToWebString(map.get("checkTime")));
                String checkStatusDesc = OrderEnum.ORDER_CHECK_STATUS.getDescByCode(Integer.valueOf(it.get("checkStatus") + ""));
                it.put("checkTime", checkTime);
                it.put("checkStatusDesc", checkStatusDesc);
            }
            it.put("isSendTimeoutDeal", 0);
            rows.add(it);
        }
        info.clear();
        return rows;
    }
    
    @Override
    public Map<String, Object> ajaxOrderInfo(Map<String, Object> para)
        throws Exception
    {
        // 为1代表来自订单审核列表
        int checkOrder = Integer.valueOf(para.get("checkOrder") == null ? "0" : para.get("checkOrder") + "");
        
        // 若有订单来源，先查询来源ID
        String orderSource = para.get("orderSource") == null ? null : para.get("orderSource") + "";
        if (orderSource != null)
        {
            Map<String, Object> sourcePara = new HashMap<>();
            sourcePara.put("name", orderSource);
            List<Map<String, Object>> reList = orderDao.findAllOrderChannel(sourcePara);
            if (reList.size() > 0)
            {
                para.put("orderSourceId", reList.get(0).get("id"));
            }
            else
            {
                Map<String, Object> _result = new HashMap<>();
                _result.put("total", 0);
                _result.put("rows", new ArrayList<>());
                return _result;
            }
        }
        
        // 商家发货类型
        int sellerType = Integer.valueOf(para.get("sellerType") == null ? "0" : para.get("sellerType") + "");
        if (sellerType != 0)
        {
            List<Integer> sellerIdList = sellerDao.findIdListBySellerType(sellerType);
            if (sellerIdList.size() > 0)
            {
                para.put("sellerIdList", sellerIdList);
            }
            else
            {
                Map<String, Object> _result = new HashMap<>();
                _result.put("total", 0);
                _result.put("rows", new ArrayList<>());
                return _result;
            }
        }
        
        // 用户名
        String accountName = para.get("accountName") == null ? null : para.get("accountName") + "";
        if (accountName != null)
        {
            List<Integer> accountIdList = accountDao.findIdListByName(accountName);
            if (accountIdList.size() > 0)
            {
                para.put("accountIdList", accountIdList);
            }
            else
            {
                Map<String, Object> _result = new HashMap<>();
                _result.put("total", 0);
                _result.put("rows", new ArrayList<>());
                return _result;
            }
        }
        
        // 收货信息
        String mobileNumber = para.get("mobileNumber") == null ? null : para.get("mobileNumber") + "";
        String fullName = para.get("fullName") == null ? null : para.get("fullName") + "";
        if (mobileNumber != null || fullName != null)
        {
            Map<String, Object> spara = new HashMap<>();
            if (fullName != null)
            {
                spara.put("fullName", fullName);
            }
            if (mobileNumber != null)
            {
                spara.put("mobileNumber", mobileNumber);
            }
            List<Integer> receiveAddressIdList = orderDao.findOrderReceiveAddressIdListByFullNameAndPhone(para);
            if (receiveAddressIdList.size() > 0)
            {
                para.put("receiveAddressIdList", receiveAddressIdList);
            }
            else
            {
                Map<String, Object> _result = new HashMap<>();
                _result.put("total", 0);
                _result.put("rows", new ArrayList<>());
                return _result;
            }
        }
        
        // 物流信息
        String logisticsNumber = para.get("logisticsNumber") == null ? null : para.get("logisticsNumber") + "";
        if (logisticsNumber != null)
        {
            List<Integer> orderIdList = orderDao.findOrderIdListByOrderLogisticsNumber(logisticsNumber);
            if (orderIdList.size() > 0)
            {
                para.put("orderIdList", orderIdList);
            }
            else
            {
                Map<String, Object> _result = new HashMap<>();
                _result.put("total", 0);
                _result.put("rows", new ArrayList<>());
                return _result;
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        int total = 0;
        int sqlType = Integer.valueOf(para.get("sqlType") == null ? "1" : para.get("sqlType") + "");
        if (sqlType == 1)
        {
            total = orderDao.countAllOrderInfoForList(para);
            List<Map<String, Object>> info = orderDao.findAllOrderInfoForList(para);
            rows = packageOrderInfo(info, checkOrder);
        }
        else
        {
            if (sqlType == 12)
            {
                String productId = para.get("productId") == null ? null : para.get("productId") + "";
                List<Integer> productIdList = new ArrayList<>();
                productIdList.add(Integer.valueOf(productId));
                para.put("productIdList", productIdList);
            }
            else
            {
                // 包含商品信息
                String productId = para.get("productId") == null ? null : para.get("productId") + "";
                if (productId == null)
                {
                    Map<String, Object> _cpara = new HashMap<>();
                    String productName = para.get("productName") == null ? null : para.get("productName") + "";
                    String productRemark = para.get("productRemark") == null ? null : para.get("productRemark") + "";
                    String code = para.get("code") == null ? null : para.get("code").toString().trim();
                    if (productName != null)
                    {
                        _cpara.put("productName", productName);
                    }
                    if (productRemark != null)
                    {
                        _cpara.put("productRemark", productRemark);
                    }
                    if (StringUtils.isNotEmpty(code))
                    {
                        _cpara.put("code", code);
                    }
                    List<Integer> productIdList = productDao.findProductIdByNameAndRemark(_cpara);
                    if (productIdList.size() > 0)
                    {
                        para.put("productIdList", productIdList);
                    }
                    else
                    {
                        Map<String, Object> _result = new HashMap<>();
                        _result.put("total", 0);
                        _result.put("rows", new ArrayList<>());
                        return _result;
                    }
                }
                else
                {
                    List<Integer> productIdList = new ArrayList<>();
                    productIdList.add(Integer.valueOf(productId));
                    para.put("productIdList", productIdList);
                }
            }
            
            // 查询数据库
            total = orderDao.countAllOrderInfoForList2(para);
            List<Map<String, Object>> info = orderDao.findAllOrderInfoForList2(para);
            rows = packageOrderInfo(info, checkOrder);
            info.clear();
        }
        
        result.put("total", total);
        result.put("rows", rows);
        return result;
    }
    
    
    @Override
    public int getExportOrderNums(Map<String, Object> para)
        throws Exception
    {
        // 若有订单来源，先查询来源ID
        String orderSource = para.get("orderSource") == null ? null : para.get("orderSource") + "";
        if (orderSource != null)
        {
            Map<String, Object> sourcePara = new HashMap<>();
            sourcePara.put("name", orderSource);
            List<Map<String, Object>> reList = orderDao.findAllOrderChannel(sourcePara);
            if (reList.size() > 0)
            {
                para.put("orderSourceId", reList.get(0).get("id"));
            }
            else
            {
                return 0;
            }
        }
        
        // 商家发货类型
        int sellerType = Integer.valueOf(para.get("sellerType") == null ? "0" : para.get("sellerType") + "");
        if (sellerType != 0)
        {
            List<Integer> sellerIdList = sellerDao.findIdListBySellerType(sellerType);
            if (sellerIdList.size() > 0)
            {
                para.put("sellerIdList", sellerIdList);
            }
            else
            {
                return 0;
            }
        }
        
        // 用户名
        String accountName = para.get("accountName") == null ? null : para.get("accountName") + "";
        if (accountName != null)
        {
            List<Integer> accountIdList = accountDao.findIdListByName(accountName);
            if (accountIdList.size() > 0)
            {
                para.put("accountIdList", accountIdList);
            }
            else
            {
                return 0;
            }
        }
        
        // 收货信息
        String mobileNumber = para.get("mobileNumber") == null ? null : para.get("mobileNumber") + "";
        String fullName = para.get("fullName") == null ? null : para.get("fullName") + "";
        if (mobileNumber != null || fullName != null)
        {
            Map<String, Object> spara = new HashMap<>();
            if (fullName != null)
            {
                spara.put("fullName", fullName);
            }
            if (mobileNumber != null)
            {
                spara.put("mobileNumber", mobileNumber);
            }
            List<Integer> receiveAddressIdList = orderDao.findOrderReceiveAddressIdListByFullNameAndPhone(para);
            if (receiveAddressIdList.size() > 0)
            {
                para.put("receiveAddressIdList", receiveAddressIdList);
            }
            else
            {
                return 0;
            }
        }
        
        // 物流信息
        String logisticsNumber = para.get("logisticsNumber") == null ? null : para.get("logisticsNumber") + "";
        if (logisticsNumber != null)
        {
            List<Integer> orderIdList = orderDao.findOrderIdListByOrderLogisticsNumber(logisticsNumber);
            if (orderIdList.size() > 0)
            {
                para.put("orderIdList", orderIdList);
            }
            else
            {
                return 0;
            }
        }
        
        int total = 0;
        int sqlType = Integer.valueOf(para.get("sqlType") == null ? "1" : para.get("sqlType") + "");
        if (sqlType == 1)
        {
            total = orderDao.countAllOrderInfoForList(para);
        }
        else
        {
            if (sqlType == 12)
            {
                String productId = para.get("productId") == null ? null : para.get("productId") + "";
                List<Integer> productIdList = new ArrayList<>();
                productIdList.add(Integer.valueOf(productId));
                para.put("productIdList", productIdList);
            }
            else
            {
                // 包含商品信息
                String productId = para.get("productId") == null ? null : para.get("productId") + "";
                if (productId == null)
                {
                    Map<String, Object> _cpara = new HashMap<>();
                    String productName = para.get("productName") == null ? null : para.get("productName") + "";
                    String productRemark = para.get("productRemark") == null ? null : para.get("productRemark") + "";
                    String code = para.get("code") == null ? null : para.get("code").toString().trim();
                    if (productName != null)
                    {
                        _cpara.put("productName", productName);
                    }
                    if (productRemark != null)
                    {
                        _cpara.put("productRemark", productRemark);
                    }
                    if (StringUtils.isNotEmpty(code))
                    {
                        _cpara.put("code", code);
                    }
                    List<Integer> productIdList = productDao.findProductIdByNameAndRemark(_cpara);
                    if (productIdList.size() > 0)
                    {
                        para.put("productIdList", productIdList);
                    }
                    else
                    {
                        return 0;
                    }
                }
                else
                {
                    List<Integer> productIdList = new ArrayList<>();
                    productIdList.add(Integer.valueOf(productId));
                    para.put("productIdList", productIdList);
                }
            }
            // 查询数据库
            total = orderDao.countAllOrderInfoForList2(para);
        }
        return total;
    }
    
    @Override
    public String exportAllStatusOrder(Map<String, Object> para)
        throws Exception
    {
        List<Integer> allIdList = findAllOrderIdList(para);
        if (allIdList.size() <= 0)
        {
            return null;
        }
        
        // 写入xls
        String nowStr = DateTime.now().toString("yyyy-MM-dd_HH_mm_ss");
        File fileDir = new File(UtilProperties.getInstance("config.properties").getProperties("order_zip_download_dir"));
        File dir = new File(fileDir, nowStr + "_" + new Random().nextInt(10000) + "orderDetail");
        dir.mkdir();
        
        // 循环
        int pageSize = 30000;
        int maxPage = allIdList.size() % pageSize == 0 ? allIdList.size() / pageSize : allIdList.size() / pageSize + 1;
        for (int page = 1; page <= maxPage; page++)
        {
            int begin = (page - 1) * pageSize;
            int end = begin + pageSize;
            end = end > allIdList.size() ? allIdList.size() : end;
            writeXlsFromOrderIdList(allIdList.subList(begin, end), page, dir);
        }
        return dir.getAbsolutePath();
    }
    
    /**
     * 根据订单IDList 导出订单明细
     *
     * @param idList
     * @throws Exception
     */
    private void writeXlsFromOrderIdList(List<Integer> idList, int page, File dir)
        throws Exception
    {
        Map<String, Object> para = new HashMap<>();
        para.put("idList", idList);
        List<OrderDetailInfoForSeller> reList = orderDao.findAllOrderInfoForSeller(para);
        if (reList.size() > 0)
        {
            Map<String, Map<String, String>> payPidMap = new HashMap<>();
            List<Long> orderAliPay = new ArrayList<>();
            List<Long> orderUnionPay = new ArrayList<>();
            List<Long> orderWeixinPay = new ArrayList<>();
            
            for (OrderDetailInfoForSeller curr : reList)
            {
                curr.setoStatusDescripton(OrderEnum.ORDER_STATUS.getDescByCode(curr.getoStatus()));
                String address = curr.getProvince() + curr.getCity() + curr.getDistrict() + curr.getDetailAddress();
                curr.setAddress(address);
                BigDecimal bigDecimal1 = new BigDecimal(curr.getSalesPrice());
                BigDecimal bigDecimal2 = new BigDecimal(curr.getProductCount());
                curr.setSmailTotalPrice(bigDecimal1.multiply(bigDecimal2).doubleValue());
                
                // 支付信息
                int payChannel = Integer.parseInt(curr.getPayChannel());
                if (payChannel == OrderEnum.PAY_CHANNEL.UNION.getCode())
                {
                    orderUnionPay.add(Long.valueOf(curr.getoId()));
                }
                else if (payChannel == OrderEnum.PAY_CHANNEL.ALIPAY.getCode())
                {
                    orderAliPay.add(Long.valueOf(curr.getoId()));
                }
                else if (payChannel == OrderEnum.PAY_CHANNEL.WEIXIN.getCode())
                {
                    orderWeixinPay.add(Long.valueOf(curr.getoId()));
                }
            }
            
            if (orderUnionPay.size() > 0)
            {
                Map<String, Object> unionPara = new HashMap<>();
                unionPara.put("idList", orderUnionPay);
            }
            if (orderWeixinPay.size() > 0)
            {
                Map<String, Object> weixinPara = new HashMap<>();
                weixinPara.put("idList", orderWeixinPay);
            }
            if (orderAliPay.size() > 0)
            {
                Map<String, Object> aliPara = new HashMap<>();
                aliPara.put("idList", orderAliPay);
            }
            
            SXSSFWorkbook workbook = null;
            OutputStream fOut = null;
            try
            {
                String[] str = {"订单编号", "订单状态", "创建时间", "付款日期", "付款渠道", "支付号", "收货人", "身份证号码", "收货地址", "省", "市", "区", "详细地址", "联系电话", "商品编号", "商品名称", "件数", "单价", "总价", "运费",
                    "订单总价", "实付金额", "积分优惠", "优惠券优惠", "客服调价", "买家备注", "卖家备注", "发货时间", "物流公司", "物流单号", "商家", "下单用户名", "客服备注", "冻结状态"};
                workbook = POIUtil.createSXSSFWorkbookTemplate(str);
                Sheet sheet = workbook.getSheetAt(0);
                for (int i = 0; i < reList.size(); i++)
                {
                    int cellIndex = 0;
                    OrderDetailInfoForSeller curr = reList.get(i);
                    Row r = sheet.createRow(i + 1);
                    r.createCell(cellIndex++).setCellValue(curr.getoNumber() + "");
                    r.createCell(cellIndex++).setCellValue(curr.getoStatusDescripton() + "");
                    r.createCell(cellIndex++).setCellValue(curr.getoCreateTime() + "");
                    r.createCell(cellIndex++).setCellValue(curr.getoPayTime() + "");
                    
                    // 支付信息
                    Map<String, String> payInfo = payPidMap.get(curr.getoId());
                    String excelPayTid = "";
                    String excelPayChannel = "";
                    if (payInfo != null && payInfo.get("payTid") != null && !"".equals(payInfo.get("payTid")))
                    {
                        excelPayTid = payInfo.get("payTid") + "";
                        excelPayChannel = payInfo.get("payChannel") + "";
                    }
                    else
                    {
                        excelPayTid = "系统中不存在。";
                        excelPayChannel = "";
                    }
                    r.createCell(cellIndex++).setCellValue(excelPayChannel);
                    r.createCell(cellIndex++).setCellValue(excelPayTid);
                    
                    r.createCell(cellIndex++).setCellValue(curr.getRaFullName() + "");
                    r.createCell(cellIndex++).setCellValue(curr.getRaIdCard() + "");
                    r.createCell(cellIndex++).setCellValue(curr.getAddress() + "");
                    r.createCell(cellIndex++).setCellValue(curr.getProvince() + "");
                    r.createCell(cellIndex++).setCellValue(curr.getCity() + "");
                    r.createCell(cellIndex++).setCellValue(curr.getDistrict() + "");
                    r.createCell(cellIndex++).setCellValue(curr.getDetailAddress() + "");
                    r.createCell(cellIndex++).setCellValue(curr.getRaMobileNumber() + "");
                    String code = curr.getProductCode();
                    Integer productCount = Integer.valueOf(curr.getProductCount() + "");
                    Integer _num = 1;
                    int index = code.lastIndexOf("%");
                    boolean isdiv = false;
                    if ((index > -1) && (index < code.length() - 1))
                    {
                        String num = code.substring(index + 1);
                        if (StringUtils.isNumeric(num))
                        {
                            code = code.substring(0, index);
                            productCount = productCount * Integer.valueOf(num);
                            isdiv = true;
                            _num = Integer.parseInt(num);
                        }
                    }
                    r.createCell(cellIndex++).setCellValue(code);
                    r.createCell(cellIndex++).setCellValue(curr.getProductName() + "");
                    r.createCell(cellIndex++).setCellValue(productCount);
                    if (isdiv)
                    {
                        r.createCell(cellIndex++).setCellValue(new DecimalFormat("0.00").format(curr.getSalesPrice() / _num));
                    }
                    else
                    {
                        r.createCell(cellIndex++).setCellValue(curr.getSalesPrice() + "");
                    }
                    r.createCell(cellIndex++).setCellValue(curr.getSmailTotalPrice() + "");
                    r.createCell(cellIndex++).setCellValue(curr.getoFreightMoney());
                    r.createCell(cellIndex++).setCellValue(curr.getoTotalPrice());
                    r.createCell(cellIndex++).setCellValue(new DecimalFormat("0.00").format(curr.getoRealPrice()));
                    r.createCell(cellIndex++).setCellValue(new DecimalFormat("0.00").format(curr.getoAccountPoint() / 100.00));
                    r.createCell(cellIndex++).setCellValue(new DecimalFormat("0.00").format(curr.getoCouponPrice()));
                    r.createCell(cellIndex++).setCellValue(new DecimalFormat("0.00").format(curr.getoAdjustPrice()));
                    // 买家备注，暂停用
                    r.createCell(cellIndex++).setCellValue("");
                    // 卖家备注，
                    r.createCell(cellIndex++).setCellValue(curr.getSellerMarks());
                    r.createCell(cellIndex++).setCellValue(curr.getoSendTime() + "");
                    r.createCell(cellIndex++).setCellValue(curr.getOlogChannel() + "");
                    r.createCell(cellIndex++).setCellValue(curr.getOlogNumber() + "");
                    r.createCell(cellIndex++).setCellValue(curr.getSellerName() + "-" + curr.getsId());
                    r.createCell(cellIndex++).setCellValue(curr.getaName());
                    // 客服备注
                    r.createCell(cellIndex++).setCellValue(curr.getKefuRemark());
                    // 冻结状态
                    if (curr.getIsFreeze() == 1)
                    {
                        r.createCell(cellIndex++).setCellValue("冻结中");
                    }
                    else
                    {
                        r.createCell(cellIndex++).setCellValue("未冻结");
                    }
                    
                }
                File file = new File(dir, "all-" + page + ".xlsx");
                file.createNewFile();
                fOut = new FileOutputStream(file);
                workbook.write(fOut);
                fOut.flush();
            }
            catch (Exception e)
            {
                log.error("导出订单详细excel出错", e);
                throw new Exception(e);
            }
            finally
            {
                if (fOut != null)
                {
                    try
                    {
                        fOut.close();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                if (workbook != null)
                {
                    try
                    {
                        workbook.close();
                        workbook.dispose();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    /**
     * 获取导出明细所有将导出的订单编号
     *
     * @param para
     * @return
     * @throws Exception
     */
    private List<Integer> findAllOrderIdList(Map<String, Object> para)
        throws Exception
    {
        List<Integer> list = new ArrayList<>();
        // 若有订单来源，先查询来源ID
        String orderSource = para.get("orderSource") == null ? null : para.get("orderSource") + "";
        if (orderSource != null)
        {
            Map<String, Object> sourcePara = new HashMap<>();
            sourcePara.put("name", orderSource);
            List<Map<String, Object>> reList = orderDao.findAllOrderChannel(sourcePara);
            if (reList.size() > 0)
            {
                para.put("orderSourceId", reList.get(0).get("id"));
            }
            else
            {
                return list;
            }
        }
        
        // 商家发货类型
        int sellerType = Integer.valueOf(para.get("sellerType") == null ? "0" : para.get("sellerType") + "");
        if (sellerType != 0)
        {
            List<Integer> sellerIdList = sellerDao.findIdListBySellerType(sellerType);
            if (sellerIdList.size() > 0)
            {
                para.put("sellerIdList", sellerIdList);
            }
            else
            {
                return list;
            }
        }
        
        // 用户名
        String accountName = para.get("accountName") == null ? null : para.get("accountName") + "";
        if (accountName != null)
        {
            List<Integer> accountIdList = accountDao.findIdListByName(accountName);
            if (accountIdList.size() > 0)
            {
                para.put("accountIdList", accountIdList);
            }
            else
            {
                return list;
            }
        }
        
        // 收货信息
        String mobileNumber = para.get("mobileNumber") == null ? null : para.get("mobileNumber") + "";
        String fullName = para.get("fullName") == null ? null : para.get("fullName") + "";
        if (mobileNumber != null || fullName != null)
        {
            Map<String, Object> spara = new HashMap<>();
            if (fullName != null)
            {
                spara.put("fullName", fullName);
            }
            if (mobileNumber != null)
            {
                spara.put("mobileNumber", mobileNumber);
            }
            List<Integer> receiveAddressIdList = orderDao.findOrderReceiveAddressIdListByFullNameAndPhone(para);
            if (receiveAddressIdList.size() > 0)
            {
                para.put("receiveAddressIdList", receiveAddressIdList);
            }
            else
            {
                return list;
            }
        }
        
        // 物流信息
        String logisticsNumber = para.get("logisticsNumber") == null ? null : para.get("logisticsNumber") + "";
        if (logisticsNumber != null)
        {
            List<Integer> orderIdList = orderDao.findOrderIdListByOrderLogisticsNumber(logisticsNumber);
            if (orderIdList.size() > 0)
            {
                para.put("orderIdList", orderIdList);
            }
            else
            {
                return list;
            }
        }
        
        int sqlType = Integer.valueOf(para.get("sqlType") == null ? "1" : para.get("sqlType") + "");
        if (sqlType == 1)
        {
            list = orderDao.findAllOrderIdList(para);
        }
        else
        {
            if (sqlType == 12)
            {
                String productId = para.get("productId") == null ? null : para.get("productId") + "";
                List<Integer> productIdList = new ArrayList<>();
                productIdList.add(Integer.valueOf(productId));
                para.put("productIdList", productIdList);
            }
            else
            {
                // 包含商品信息
                String productId = para.get("productId") == null ? null : para.get("productId") + "";
                if (productId == null)
                {
                    Map<String, Object> _cpara = new HashMap<>();
                    String productName = para.get("productName") == null ? null : para.get("productName") + "";
                    String productRemark = para.get("productRemark") == null ? null : para.get("productRemark") + "";
                    String code = para.get("code") == null ? null : para.get("code").toString().trim();
                    if (productName != null)
                    {
                        _cpara.put("productName", productName);
                    }
                    if (productRemark != null)
                    {
                        _cpara.put("productRemark", productRemark);
                    }
                    if (StringUtils.isNotEmpty(code))
                    {
                        _cpara.put("code", code);
                    }
                    List<Integer> productIdList = productDao.findProductIdByNameAndRemark(_cpara);
                    if (productIdList.size() > 0)
                    {
                        para.put("productIdList", productIdList);
                    }
                    else
                    {
                        return list;
                    }
                }
                else
                {
                    List<Integer> productIdList = new ArrayList<>();
                    productIdList.add(Integer.valueOf(productId));
                    para.put("productIdList", productIdList);
                }
            }
            
            // 查询数据库
            list = orderDao.findAllOrderIdList2(para);
        }
        return list;
    }
    
    @Override
    public Map<String, List<OrderDetailInfoForSeller>> findAllOrderInfoForSeller(Map<String, Object> para)
        throws Exception
    {
        int from = Integer.valueOf(para.get("from") == null ? "1" : para.get("from") + "");
        Map<String, List<OrderDetailInfoForSeller>> returnMap = new HashMap<>();
        List<OrderDetailInfoForSeller> reList = orderDao.findAllOrderInfoForSeller(para);
        if (reList.size() > 0)
        {
            Set<Integer> orderIdList = new HashSet<>();
            for (OrderDetailInfoForSeller curr : reList)
            {
                orderIdList.add(Integer.valueOf(curr.getoId()));
                
                String cuKey = curr.getSellerName() + "_" + curr.getSendAddress() + "_" + curr.getWarehouse() + "_" + curr.getsId();
                List<OrderDetailInfoForSeller> newList = returnMap.get(cuKey);
                if (newList == null)
                {
                    newList = new ArrayList<>();
                    returnMap.put(cuKey, newList);
                }
                curr.setoStatusDescripton(OrderEnum.ORDER_STATUS.getDescByCode(curr.getoStatus()));
                String address = curr.getProvince() + curr.getCity() + curr.getDistrict() + curr.getDetailAddress();
                curr.setAddress(address);
                BigDecimal bigDecimal1 = new BigDecimal(curr.getSalesPrice());
                BigDecimal bigDecimal2 = new BigDecimal(curr.getProductCount());
                curr.setSmailTotalPrice(bigDecimal1.multiply(bigDecimal2).doubleValue());
                newList.add(curr);
            }
            if ((orderIdList.size() > 0) && from == 2)
            {
                // 商家导出，修改导出状态，用于在退款退货处判断
                updateOrderOperationStatus(orderIdList);
            }
        }
        return returnMap;
    }
    
    /**
     * 模拟订单发货，发现各种意外情况。
     */
    @Override
    public boolean sendOrderTest(Map<String, Object> para)
        throws Exception
    {
        List<Integer> notOrderList = new ArrayList<>();
        String orderNumber = para.get("orderNumber") + "";
        String channel = para.get("channel") + "";
        String number = para.get("number") + "";
        String sendTime = para.get("sendTime") + "";
        @SuppressWarnings("unchecked")
		List<Integer> sellerIdList = para.get("sellerIdList") == null ? null : (List<Integer>)para.get("sellerIdList");
        /*
         * 1. 该订单不存在 这里包括 合并订单 以HB开头 2. 该订单已发货 3. 物流公司不存在
         */
        @SuppressWarnings("unchecked")
		List<Map<String, Object>> iList = (List<Map<String, Object>>)para.get("iList");
        Map<String, Object> sendStatusMap = new HashMap<String, Object>();
        
        // 查询物流公司是否填写是否正确
        KdCompanyEnum kdEnum = KdCompanyEnum.getKdCompanyEnumByCompanyName(channel);
        if (kdEnum == null || "".equals(number.trim()))
        {
            sendStatusMap.put("status", "失败");
            sendStatusMap.put("msg", "物流公司不存在或者物流单号不符合格式");
            sendStatusMap.put("errorCode", 1);
            sendStatusMap.put("orderNumber", orderNumber);
            sendStatusMap.put("createTime", "");
            sendStatusMap.put("payTime", "");
            sendStatusMap.put("sendTime", sendTime);
            sendStatusMap.put("channel", channel);
            sendStatusMap.put("number", number);
            iList.add(sendStatusMap);
            return false;
        }
        
        if (orderNumber.startsWith("HB"))
        {
            // 合并订单，查询合并订单是否存在，子订单是符合要求
            Map<String, Object> hbPara = new HashMap<String, Object>();
            hbPara.put("hbNumber", "GGJ" + orderNumber);
            
        }
        else
        {
            // 查询订单信息
            OrderEntity order = orderDao.findOrderByNumber(orderNumber);
            if (order == null)
            {
                sendStatusMap.put("status", "失败");
                sendStatusMap.put("msg", "订单不存在");
                sendStatusMap.put("errorCode", 1);
                sendStatusMap.put("orderNumber", orderNumber);
                sendStatusMap.put("createTime", "");
                sendStatusMap.put("payTime", "");
                sendStatusMap.put("sendTime", sendTime);
                sendStatusMap.put("channel", channel);
                sendStatusMap.put("number", number);
                iList.add(sendStatusMap);
                return false;
            }
            if (notOrderList.contains(order.getId()))
            {
                sendStatusMap.put("status", "失败");
                sendStatusMap.put("msg", "异常订单不允许发货");
                sendStatusMap.put("errorCode", 1);
                sendStatusMap.put("orderNumber", orderNumber);
                sendStatusMap.put("createTime", "");
                sendStatusMap.put("payTime", "");
                sendStatusMap.put("sendTime", sendTime);
                sendStatusMap.put("channel", channel);
                sendStatusMap.put("number", number);
                iList.add(sendStatusMap);
                return false;
            }
            
            // 存在商家ID则
            if (sellerIdList != null && !sellerIdList.contains(order.getSellerId()))
            {
                sendStatusMap.put("status", "失败");
                sendStatusMap.put("msg", "订单不属于商家");
                sendStatusMap.put("errorCode", 1);
                sendStatusMap.put("orderNumber", orderNumber);
                sendStatusMap.put("createTime", "");
                sendStatusMap.put("payTime", "");
                sendStatusMap.put("sendTime", sendTime);
                sendStatusMap.put("channel", channel);
                sendStatusMap.put("number", number);
                iList.add(sendStatusMap);
                return false;
            }
            
            // 查询订单状态是否符合要求
            if (order.getStatus() != (byte)2 && order.getStatus() != (byte)3)
            {
                sendStatusMap.put("status", "失败");
                sendStatusMap.put("msg", "该订单状态不符合发货要求");
                sendStatusMap.put("errorCode", 1);
                sendStatusMap.put("orderNumber", orderNumber);
                sendStatusMap.put("createTime", order.getCreateTime());
                sendStatusMap.put("payTime", order.getPayTime());
                sendStatusMap.put("sendTime", sendTime);
                sendStatusMap.put("channel", channel);
                sendStatusMap.put("number", number);
                iList.add(sendStatusMap);
                return false;
            }
            // 查询订单物流信息
            Map<String, Object> orderLogisticsMap = orderDao.findOrderLogisticsByOrderId(order.getId());
            if (orderLogisticsMap != null)
            {
                sendStatusMap.put("status", "失败");
                sendStatusMap.put("msg", "该订单已发货");
                sendStatusMap.put("errorCode", 2);
                sendStatusMap.put("orderNumber", orderNumber);
                sendStatusMap.put("createTime", order.getCreateTime());
                sendStatusMap.put("payTime", order.getPayTime());
                sendStatusMap.put("sendTime", sendTime);
                sendStatusMap.put("channel", channel);
                sendStatusMap.put("number", number);
                iList.add(sendStatusMap);
                return false;
            }
        }
        sendStatusMap.put("status", "成功");
        sendStatusMap.put("msg", "");
        sendStatusMap.put("errorCode", 0);
        sendStatusMap.put("orderNumber", orderNumber);
        sendStatusMap.put("createTime", "");
        sendStatusMap.put("payTime", "");
        sendStatusMap.put("sendTime", sendTime);
        sendStatusMap.put("channel", channel);
        sendStatusMap.put("number", number);
        iList.add(sendStatusMap);
        return true;
    }
    
    @Override
    public int sendOrderHB(Map<String, Object> para)
        throws Exception
    {
        String orderNumber = para.get("orderNumber") + "";
        Map<String, Object> hbPara = new HashMap<String, Object>();
        hbPara.put("hbNumber", "GGJ" + orderNumber);
        return 1;
    }
    
    @Override
    public int sendOrderWithOutLogistics(Map<String, Object> para)
        throws Exception
    {
        // 更新订单状态和发货时间
        Map<String, Object> orderPara = new HashMap<String, Object>();
        orderPara.put("id", (int)para.get("orderId"));
        orderPara.put("status", 3);
        orderPara.put("sendTime", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
        int status = orderDao.updateOrder(orderPara);
        if (status == 1)
        {
            para.put("createTime", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
            para.put("isSubscribed", 0);
            status = orderDao.saveOrderLogistics(para);
        }
        return status;
    }
    
    
    @Override
    public int sendOrder(Map<String, Object> para)
        throws Exception
    {
        int orderId = Integer.valueOf(para.get("orderId") == null ? "0" : para.get("orderId") + "").intValue();
        if (orderId == 0)
        {
            // 来自批量发货
            Map<String, Object> map1 = new HashMap<>();
            String orderNumber = para.get("orderNumber") + "";
            OrderEntity nowOrder = orderDao.findOrderByNumber(orderNumber);
            map1.put("orderId", nowOrder.getId());
            map1.put("number", para.get("number"));
            map1.put("channel", para.get("channel"));
            map1.put("money", 0);
            para = map1; // 重新赋值，防止合并订单多次发货出现问题
            orderId = nowOrder.getId();
        }
        
        String number = para.get("number") + "";// 物流单号
        String companyName = para.get("channel") + "";// 物流公司名称
        
        // 检出订单是否符合发货条件 status 2：待发货，3：已发货 才能发货
        OrderEntity currOrderEntity = orderDao.findOrderById(orderId);
        if (currOrderEntity.getStatus() != (byte)OrderEnum.ORDER_STATUS.REVIEW.getCode() && currOrderEntity.getStatus() != (byte)OrderEnum.ORDER_STATUS.SENDGOODS.getCode())
        {
            log.warn("订单状态不符合发货条件，拦截。订单ID：" + orderId);
            return 0;
        }
        // 查询订单是否已有物流信息
        Map<String, Object> orderLogisticsMap = orderDao.findOrderLogisticsByOrderId(orderId);
        boolean isEdit = false;
        if (orderLogisticsMap != null)
        {
            isEdit = true;
        }
        if (!isEdit)
        {
            // 插入订单物流信息
            para.put("createTime", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
            para.put("isSubscribed", 0);
            int resultStatus = orderDao.saveOrderLogistics(para);
            if (resultStatus != 1)
            {
                return 0;
            }
            
            // 更新订单状态和发货时间
            Map<String, Object> orderPara = new HashMap<String, Object>();
            orderPara.put("id", (int)para.get("orderId"));
            orderPara.put("status", OrderEnum.ORDER_STATUS.SENDGOODS.getCode());
            orderPara.put("sendTime", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
            orderDao.updateOrder(orderPara);
        }
        else
        {
            // 更新 订单物流信息
            orderDao.updateOrderLogistics(para);
            // 更新订单状态和发货时间
            Map<String, Object> orderPara = new HashMap<String, Object>();
            orderPara.put("id", (int)para.get("orderId"));
            orderPara.put("status", OrderEnum.ORDER_STATUS.SENDGOODS.getCode());
            orderDao.updateOrder(orderPara);
        }
        
        boolean toDingyue = true;
        if (isEdit)
        {
            // 如果是修改，判断是否修改了物流信息，若修改了物流信息，则继续向下走订阅逻辑
            String oldChannel = (String)orderLogisticsMap.get("channel");
            String oldNumber = (String)orderLogisticsMap.get("number");
            String newChannel = (String)para.get("channel");
            String newNumber = (String)para.get("number");
            if (oldChannel.equals(newChannel) && oldNumber.equals(newNumber))
            {
                toDingyue = false;
            }
            log.info("修改订单物流信息，订单id：" + (int)para.get("orderId"));
        }
        
        log.info(KdCompanyEnum.getKdCompanyEnumByCompanyName(companyName));
        log.info(companyName + "---订单ID：" + orderId);
        Map<String, Object> detail = new HashMap<String, Object>();
        detail.put("logisticsChannel", companyName);
        detail.put("logisticsNumber", number);
        detail.put("operateTime", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
        detail.put("createTime", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
        int existsNum = orderDao.existsLogisticsDetail(detail);
        if (existsNum == 0)
        {
            // 不存在，插入
            orderDao.saveLogisticsDetail(detail);
        }
        else
        {
            log.debug("已经存在物流信息信息，跳过插入");
        }
        // 插入订单发货记录 修改物流信息不发短信
        if (!isEdit)
        {
            if (!isUseful(orderDao.findOrderSendRecordByOrderId(currOrderEntity.getId())))
            {
                // 在不发短信提醒表中查询商品是否存在
                List<Integer> notSendMsgProductIdList = null;
                // 如果商品不存在不发短信提醒表中，则不插入
                if (notSendMsgProductIdList == null || notSendMsgProductIdList.size() == 0)
                {
                    insertOrderSendRecord(currOrderEntity.getId(), currOrderEntity.getAccountId(), currOrderEntity.getReceiveAddressId(), para.get("channel") + "",
                        para.get("number") + "", 0);
                }
                
            }
        }
        return 1;
    }
    
    
    public static Boolean isUseful(Object o)
            throws Exception
        {
            if (o == null)
            {
                return false;
            }
            if (o instanceof Byte)
            {
                Byte b = (Byte)o;
                if (b.byteValue() == 0)
                {
                    return false;
                }
            }
            else if (o instanceof Short)
            {
                Short s = (Short)o;
                if (s.shortValue() == 0)
                {
                    return false;
                }
            }
            else if (o instanceof Integer)
            {
                Integer b = (Integer)o;
                if (b.intValue() == 0)
                {
                    return false;
                }
            }
            else if (o instanceof Long)
            {
                Long b = (Long)o;
                if (b.longValue() == 0)
                {
                    return false;
                }
            }
            else if (o instanceof Float)
            {
                Float b = (Float)o;
                if (b.floatValue() == 0)
                {
                    return false;
                }
            }
            else if (o instanceof Double)
            {
                Double b = (Double)o;
                if (b.doubleValue() == 0)
                {
                    return false;
                }
            }
            else if (o instanceof Character)
            {
                Character c = (Character)o;
                if ('\0' == c)
                {
                    return false;
                }
            }
            else if (o instanceof String)
            {
                String b = (String)o;
                if ("".equals(b))
                {
                    return false;
                }
            }
            else if (o instanceof Map)
            {
                Map m = (Map)o;
                if (m.keySet().size() == 0)
                {
                    return false;
                }
            }
            else if (o instanceof List)
            {
                List l = (List)o;
                if (l.size() == 0)
                {
                    return false;
                }
            }
            return true;
        }
    
    
    /**
     * 插入订单发货记录
     */
    private void insertOrderSendRecord(int orderId, int accountId, int receiveAddressId, String channel, String number, int status)
        throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("orderId", orderId);
        map.put("accountId", accountId);
        map.put("receiveAddressId", receiveAddressId);
        map.put("channel", channel);
        map.put("number", number);
        map.put("status", status);
        orderDao.insertOrderSendRecord(map);
    }
    
    @Override
    public boolean kd100CallBack(String param, String orderId)
        throws Exception
    {
    	return false;
    }
    
    @Override
    public Map<String, Object> findOrderDetailInfo(int orderId, int from)
        throws Exception
    {
        // 订单
        OrderEntity order = orderDao.findOrderById(orderId);
        if (order == null)
        {
            return new HashMap<>();
        }
        
        // 买家
        AccountEntity account = accountDao.findAccountById(order.getAccountId());
        account = account == null ? new AccountEntity() : account;
        // 商家
        SellerEntity seller = sellerDao.findSellerById(order.getSellerId());
        seller = seller == null ? new SellerEntity() : seller;
        OrderReceiveAddress receiveAddress = receiveAddressDao.findOrderReceiveAddressById(order.getReceiveAddressId());
        receiveAddress = receiveAddress == null ? new OrderReceiveAddress() : receiveAddress;
        
        // 物流信息 - 根据订单ID查询订单物流信息
        Map<String, Object> orderLogisticsMap = orderDao.findOrderLogisticsByOrderId(orderId);
        boolean hasOrderLogistics = true;
        if (orderLogisticsMap == null)
        {
            hasOrderLogistics = false;
            orderLogisticsMap = new HashMap<>();
        }
        Map<String, Object> para = new HashMap<>();
        para.put("logisticsChannel", orderLogisticsMap.get("channel"));
        para.put("logisticsNumber", orderLogisticsMap.get("number"));
        para.put("start", 0);
        para.put("max", 3);
        List<Map<String, Object>> logisticsList = orderDao.findAllLogisticsDetail(para);
        
        // 将数据组装成Map返回
        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getId());
        map.put("orderType", OrderEnum.ORDER_TYPE.getDescByCode(order.getType()));
        map.put("number", order.getNumber());
        map.put("status", OrderEnum.ORDER_STATUS.getDescByCode(order.getStatus()));
        map.put("statusCode", order.getStatus());
        String operationStatus = null;
        if (seller.getIsOwner() == 1)
        {
            if (order.getStatus() == OrderEnum.ORDER_STATUS.REVIEW.getCode()) // 待发货
            {
                if ((order.getOperationStatus() == 1))
                {
                    operationStatus = "该订单已被商家导出，操作订单时请核实信息。";
                }
                else
                {
                    operationStatus = "订单未被商家导出。";
                }
            }
        }
        map.put("operationStatus", operationStatus);
        Integer isFreeze = order.getIsFreeze();
        int freezeCode = 0;
        if (isFreeze == 1)
        {
            Map<String, Object> freezeMap = orderDao.findOrderFreezeByOrderId(orderId);
            if (freezeMap != null)
            {
                int freezeStatus = Integer.parseInt(freezeMap.get("status") + "");
                if (freezeStatus != OrderEnum.ORDER_FREEZE_STATUS.UN_FREEZE.getCode())
                {
                    freezeCode = freezeStatus;
                }
            }
        }
        
        map.put("freezeCode", freezeCode);
        map.put("createTime", order.getCreateTime());
        map.put("payTime", order.getPayTime());
        map.put("sendTime", order.getSendTime());
        map.put("receiveTime", order.getReceiveTime());
        map.put("freightMoney", order.getFreightMoney() + "");
        map.put("remark", order.getRemark());
        map.put("remark2", order.getRemark2());
        map.put("totalPrice", new DecimalFormat("0.00").format(order.getTotalPrice() + order.getAdjustPrice()));
        map.put("realPrice", new DecimalFormat("0.00").format(order.getRealPrice()));
        map.put("couponPrice", new DecimalFormat("0.00").format(order.getCouponPrice()));
        map.put("accountPoint", new DecimalFormat("0.00").format(order.getAccountPoint() / 100.00));
        if (order.getIsMemberOrder() == 1)
        {
            map.put("accountPoint", map.get("accountPoint") + "(积分换购)");
        }
        map.put("adjustPrice", new DecimalFormat("0.00").format(order.getAdjustPrice()));
        map.put("activitiesPrice", new DecimalFormat("0.00").format(order.getActivitiesPrice()));
        map.put("activitiesOptionalPartPrice", new DecimalFormat("0.00").format(order.getActivitiesOptionalPartPrice()));
        
        // 参与其他活动
        String otherActivity = "";
        map.put("otherActivity", otherActivity);
        // 订单商品总价
        String totalProductPrice = orderDao.sumOrderPrice(order.getId());
        map.put("totalProductPrice", totalProductPrice);
        map.put("payChannel", OrderEnum.PAY_CHANNEL.getDescByCode(order.getPayChannel()));
        // 商家备注 暂不实现
        map.put("marks", "");
        map.put("accountType", "微信用户");
        map.put("accountName", account.getName());
        map.put("accountId", account.getId());
        map.put("receiveName", receiveAddress.getFullName());
        map.put("receiveMobile", receiveAddress.getMobileNumber());
        map.put("receiveIdCart", receiveAddress.getIdCard());
        if (receiveAddress.getConfirmFullName() == null || receiveAddress.getConfirmFullName().equals(""))
        {
            map.put("modifyReceiveName", "");
        }
        else
        {
            map.put("modifyReceiveName", "备注：用户已更改真实姓名为：" + receiveAddress.getConfirmFullName());
        }
        // 详细地址
        StringBuffer sb = new StringBuffer("");
        if (receiveAddress.getProvince() != null && !"".equals(receiveAddress.getProvince()))
        {
            sb.append(areaDao.findProvinceNameByProvinceId(Integer.valueOf(receiveAddress.getProvince()))).append(" ");
        }
        if(receiveAddress.getProvince() != null && !"".equals(receiveAddress.getProvince())){
            map.put("provinceId", Integer.valueOf(receiveAddress.getProvince()));
        }
        // 所有省份信息
        List<ProvinceEntity> provinceList = areaDao.findAllProvince();
        map.put("provinceList", provinceList);
        if (receiveAddress.getCity() != null && !"".equals(receiveAddress.getCity()))
        {
            sb.append(areaDao.findCityNameByCityId(Integer.valueOf(receiveAddress.getCity()))).append(" ");
        }
        if(receiveAddress.getProvince() != null && !"".equals(receiveAddress.getProvince())){
        	 map.put("cityId", Integer.valueOf(receiveAddress.getCity()));
        	 // 对应省份下的所有城市信息
             List<CityEntity> cityList = areaDao.findAllCityByProvinceId(Integer.valueOf(receiveAddress.getProvince()));
             map.put("cityList", cityList);
        }
       
        
        if (receiveAddress.getDistrict() != null && !"".equals(receiveAddress.getDistrict()))
        {
            sb.append(areaDao.findDistrictNameByDistrictId(Integer.valueOf(receiveAddress.getDistrict()))).append(" ");
        }
        if(receiveAddress.getProvince() != null && !"".equals(receiveAddress.getProvince())){
        	 map.put("districtId", Integer.valueOf(receiveAddress.getDistrict()));
        	// 对应城市下的所有区信息
             List<DistrictEntity> districtList = areaDao.findAllDistrictByCityId(Integer.valueOf(receiveAddress.getCity()));
             map.put("districtList", districtList);
       }
       
        
        if (receiveAddress.getDetailAddress() != null && !"".equals(receiveAddress.getDetailAddress()))
        {
            if (from == 2 && order.getStatus() == OrderEnum.ORDER_STATUS.REVIEW.getCode())
            {
                sb.append("<span style=\"color:red\">点“发货”填写正确物流单号后显示详细地址。</span>");
            }
            else
            {
                sb.append(receiveAddress.getDetailAddress());
            }
        }
        if(receiveAddress.getProvince() != null && !"".equals(receiveAddress.getProvince())){
        	map.put("detailAddress", receiveAddress.getDetailAddress());
       }
        
        map.put("address", sb.toString());
        map.put("sellerName", seller.getRealSellerName());
        StringBuffer strB = new StringBuffer(SellerEnum.SellerTypeEnum.getDescByCode(seller.getSellerType()));
        if (SellerEnum.SellerTypeEnum.HONG_KONG.getCode() == seller.getSellerType())
        {
            if (seller.getIsNeedIdCardImage() == 1)
            {
                strB.append("(身份证照片)");
            }
            else
            {
                strB.append("(仅身份证号)");
            }
        }
        map.put("sellerType", strB);
        map.put("sendAddress", seller.getSendAddress());
        map.put("sellerId", seller.getId() + "");
        
        // 物流信息
        map.put("hasOrderLogistics", hasOrderLogistics ? 1 : null);
        map.put("logisticsChannel", orderLogisticsMap.get("channel"));
        map.put("logisticsNumber", orderLogisticsMap.get("number"));
        map.put("logisticsMoney", orderLogisticsMap.get("money"));
        map.put("logisticsList", logisticsList);
        //String logisticsUrl = "http://cilu.waibao58.com/ygg/order/logistic/" + order.getId() + "/0";
        map.put("logisticsUrl", null);
        
        // 订单详情页，加上订单交易号
        String payTid = "";
        
        String weixinMark = "";
        String zhifubaoMark = "";
        String yinlianMark = "";
        List<JSONObject> orderNumberList = new ArrayList<JSONObject>();
        JSONArray orderNumberJsonArray = new JSONArray();
        if (order.getPayChannel() == OrderEnum.PAY_CHANNEL.ALIPAY.getCode())
        {
            payTid = orderDao.findPayTidOrderAliPay(orderId);
            List<Map<String, Object>> list = orderDao.find_PayMarkTidOrderAliPay(orderId);
            for (Iterator<Map<String, Object>> it = list.iterator(); it.hasNext();)
            {
                Map<String, Object> m = it.next();
                zhifubaoMark += m.get("payMark") + ",";
                
            }
            zhifubaoMark = zhifubaoMark.substring(0, zhifubaoMark.length() - 1);
            if (StringUtils.isNotEmpty(payTid))
            {
                JSONObject j = new JSONObject();
                j.put("payTid", payTid);
                orderNumberList = orderDao.findOrderAliPay(j);
            }
        }
        else if (order.getPayChannel() == OrderEnum.PAY_CHANNEL.WEIXIN.getCode())
        {
            payTid = orderDao.findPayTidOrderWeixinPay(orderId);
            List<Map<String, Object>> list = orderDao.find_PayMarkTidOrderWeixinPay(orderId);
            for (Iterator<Map<String, Object>> it = list.iterator(); it.hasNext();)
            {
                Map<String, Object> m = it.next();
                weixinMark += m.get("payMark") + ",";
                
            }
            weixinMark = weixinMark.length() > 0 ? weixinMark.substring(0, weixinMark.length() - 1) : "";
            if (StringUtils.isNotEmpty(payTid))
            {
                JSONObject j = new JSONObject();
                j.put("payTid", payTid);
                orderNumberList = orderDao.findOrderWeixinPay(j);
            }
        }
        else if (order.getPayChannel() == OrderEnum.PAY_CHANNEL.UNION.getCode())
        {
            payTid = orderDao.findPayTidOrderUnionPay(orderId);
            if(payTid==null){
            	return null;
            }
            List<Map<String, Object>> list = orderDao.find_PayTidOrderUnionPay(orderId);
            for (Iterator<Map<String, Object>> it = list.iterator(); it.hasNext();)
            {
                Map<String, Object> m = it.next();
                yinlianMark += m.get("payMark") + ",";
            }
            if (!yinlianMark.isEmpty())
            {
                yinlianMark = yinlianMark.substring(0, yinlianMark.length() - 1);
            }
            if (StringUtils.isNotEmpty(payTid))
            {
                JSONObject j = new JSONObject();
                j.put("payTid", payTid);
                orderNumberList = orderDao.findOrderUnionPay(j);
            }
        }
        
        if (order.getPayChannel() != OrderEnum.PAY_CHANNEL.ALIPAY.getCode())
        {
            
            List<Map<String, Object>> list = orderDao.find_PayMarkTidOrderAliPay(orderId);
            for (Iterator<Map<String, Object>> it = list.iterator(); it.hasNext();)
            {
                Map<String, Object> m = it.next();
                zhifubaoMark += m.get("payMark") + ",";
            }
            zhifubaoMark = zhifubaoMark.length() > 0 ? zhifubaoMark.substring(0, zhifubaoMark.length() - 1) : "";
        }
        
        if (order.getPayChannel() != OrderEnum.PAY_CHANNEL.WEIXIN.getCode())
        {
            
            List<Map<String, Object>> list = orderDao.find_PayMarkTidOrderWeixinPay(orderId);
            for (Iterator<Map<String, Object>> it = list.iterator(); it.hasNext();)
            {
                Map<String, Object> m = it.next();
                weixinMark += m.get("payMark") + ",";
            }
            weixinMark = weixinMark.length() > 0 ? weixinMark.substring(0, weixinMark.length() - 1) : "";
        }
        
        if (order.getPayChannel() != OrderEnum.PAY_CHANNEL.UNION.getCode())
        {
            
            List<Map<String, Object>> list = orderDao.find_PayTidOrderUnionPay(orderId);
            for (Iterator<Map<String, Object>> it = list.iterator(); it.hasNext();)
            {
                Map<String, Object> m = it.next();
                yinlianMark += m.get("payMark") + ",";
            }
            yinlianMark = yinlianMark.length() > 0 ? yinlianMark.substring(0, yinlianMark.length() - 1) : "";
        }
        
        for (Iterator<JSONObject> it = orderNumberList.iterator(); it.hasNext();)
        {
            JSONObject j = it.next();
            int oId = j.getInteger("orderId");
            if (orderId != oId)
            {
                JSONObject jsonObject = new JSONObject();
                OrderEntity orderEntity = orderDao.getOrderById(oId);
                jsonObject.put("id", oId + "");
                jsonObject.put("number", orderEntity.getNumber());
                orderNumberJsonArray.add(jsonObject);
            }
        }
        
        map.put("payTid", payTid);
        map.put("orderNumberJsonArray", orderNumberJsonArray);
        map.put("weixinMark", weixinMark);
        map.put("zhifubaoMark", zhifubaoMark);
        map.put("yinlianMark", yinlianMark);
        
        // 订单发货时效相关
        map.put("sellerSendTime",
            SellerEnum.SellerSendTimeTypeEnum.getDescByCode(seller.getSendTimeType()) + "(" + SellerEnum.WeekendSendTypeEnum.getDescByCode(seller.getIsSendWeekend()) + ")");
        map.put("sendTimeoutStatus", order.getIsTimeOut() == 1 ? "超时" : "未超时");
        Map<String, Object> sendTimeComplainResult =null;// timeoutOrderDao.findOrderTimeoutComplainResultByOId(orderId);
        map.put("orderSendTimeoutComplain",
            sendTimeComplainResult == null ? "未申诉"
                : sendTimeComplainResult.get("status") == null ? "未申诉"
                    : OrderEnum.TIMEOUT_COMPLAIN_RESULT.getNameByCode(Integer.parseInt(sendTimeComplainResult.get("status").toString())));
        
        // 订单物流时效相关
        String logisticsTime = "";
        if (seller.getSellerType() == SellerEnum.SellerTypeEnum.CHINA.getCode())
        {
            logisticsTime = "订单发货后24小时有物流信息";
        }
        else
        {
            if (seller.getBondedNumberType() == SellerEnum.BondedNumberTypeEnum.BONDED_TYPE_1.getCode())
            {
                logisticsTime = "订单发货后48小时有物流信息";
            }
            else
            {
                logisticsTime = "订单发货后24小时有物流信息";
            }
        }
        map.put("logisticsTime", logisticsTime);
        return map;
    }
    
    @Override
    public String orderProductJsonStr(int id)
        throws Exception
    {
        List<Map<String, Object>> products = orderDao.findAllOrderProductInfo(id);
        for (Map<String, Object> map : products)
        {
            int sellerType = (int)map.get("sellerType");
            int isNeedIdcardImage = (int)map.get("isNeedIdcardImage");
            StringBuffer sb = new StringBuffer();
            sb.append(SellerEnum.SellerTypeEnum.getDescByCode(sellerType));
            if (SellerEnum.SellerTypeEnum.HONG_KONG.getCode() == sellerType)
            {
                if (isNeedIdcardImage == 1)
                {
                    sb.append("(身份证照片)");
                }
                else
                {
                    sb.append("(仅身份证号)");
                }
            }
            map.put("sellerType", sb.toString());
            double price = Double.valueOf(map.get("price") + "").doubleValue();
            BigDecimal bigDecimal = new BigDecimal(price);
            int count = (int)map.get("count");
            BigDecimal couBigDecimal = new BigDecimal(count);
            map.put("totalPrice", bigDecimal.multiply(couBigDecimal).doubleValue());
            int sendGoodsCount = count + Integer.parseInt(map.get("adjustCount") + "");
            map.put("sendGoodsCount", sendGoodsCount);
            if (sendGoodsCount > 0)
            {
                map.put("sendGoodsStatus", "发货");
            }
            else
            {
                map.put("sendGoodsStatus", "不发货");
            }
            // 查询退款退货情况
            Map<String, Object> refundPara = new HashMap<String, Object>();
            refundPara.put("statusList", Arrays.asList(CommonEnum.RefundStatusEnum.APPLY.ordinal(), CommonEnum.RefundStatusEnum.WAIT_RETURN_OF_GOODS.ordinal(),
                CommonEnum.RefundStatusEnum.WAIT_RETURN_OF_MONEY.ordinal(), CommonEnum.RefundStatusEnum.SUCCESS.ordinal()));
            refundPara.put("orderProductId", Integer.parseInt(map.get("orderProductId") + ""));
            List<RefundEntity> res = refundDao.findAllRefundByPara(refundPara);
            if (res.size() > 0)
            {
                if (res.get(0).getType() == 1)
                {
                    map.put("refundStatus", "仅退款");
                }
                else
                {
                    map.put("refundStatus", "退款并退货");
                }
                map.put("refundDetailStatus", CommonEnum.RefundStatusEnum.getDescriptionByOrdinal(res.get(0).getStatus()));
            }
            else
            {
                map.put("refundStatus", "未退款");
                map.put("refundDetailStatus", "");
            }
        }
        return JSON.toJSONString(products);
    }
    
    
    @Override
    public String exportForSeller(Map<String, Object> para)
        throws Exception
    {
        // 若有订单来源，先查询来源ID
        String orderSource = (String)para.get("orderSource");
        if (orderSource != null)
        {
            Map<String, Object> sourcePara = new HashMap<>();
            sourcePara.put("name", orderSource);
            List<Map<String, Object>> reList = orderDao.findAllOrderChannel(sourcePara);
            if (reList.size() > 0)
            {
                para.put("orderSourceId", reList.get(0).get("id"));
            }
        }
        Map<String, List<OrderDetailInfoForSeller>> resultMap = findAllOrderInfoForSeller(para);
        String nowStr = DateTime.now().toString("yyyy-MM-dd_HH_mm_ss");
        File fileDir = new File(UtilProperties.getInstance("config.properties").getProperties("order_zip_download_dir"));
        File newDir = new File(fileDir, nowStr + "_" + new Random().nextInt(10000) + "send");
        newDir.mkdir();
        // 导出参数选择
        HttpSession session = para.get("session") == null ? null : (HttpSession)para.get("session");
        @SuppressWarnings("unchecked")
		List<Integer> secoundSearchOrderIdList =
            session.getAttribute("secoundSearchOrderIdList") == null ? new ArrayList<Integer>() : (List<Integer>)session.getAttribute("secoundSearchOrderIdList");
        SellerEntity se = null;
        for (Map.Entry<String, List<OrderDetailInfoForSeller>> entry : resultMap.entrySet())
        {
            if (se != null)
            {
                writeToExcel(newDir, entry.getKey() + ".xls", entry.getValue(), secoundSearchOrderIdList, 1);
            }
            else
            {
                writeToExcel(newDir, entry.getKey() + ".xlsx", entry.getValue(), secoundSearchOrderIdList, 2);
            }
        }
        return newDir.getAbsolutePath();
    }
    
    /**
     * 导出发货Excel
     *
     * @param dir
     * @param fileName
     * @param resultList
     * @param secoundSearchOrderIdList
     * @param xlsType 1 : 2003版本；2:2010版本
     * @throws Exception
     */
    private void writeToExcel(File dir, String fileName, List<OrderDetailInfoForSeller> resultList, List<Integer> secoundSearchOrderIdList, int xlsType)
        throws Exception
    {
        OutputStream fOut = null;
        Workbook workbook = null;
        try
        {
            Map<String, Map<String, String>> payPidMap = findPayTid1(resultList);
            
            if (xlsType == 1)
            {
                String[] arr = {"订单编号", "订单状态", "订单类型", "创建时间", "付款日期", "收货人", "身份证号码", "收货地址", "省", "市", "区", "详细地址", "联系电话", "商品编号", "商品名称", "件数", "单价", "总价", "运费", "订单总价",
                    "买家备注", "卖家备注", "发货时间", "物流公司", "物流单号", "冻结状态", "发件人", "联系方式", "支付公司", "支付交易号"};
                workbook = POIUtil.createHSSFWorkbookTemplate(arr);
            }
            else
            {
                String[] arr = {"订单编号", "订单状态", "创建时间", "付款日期", "收货人", "身份证号码", "收货地址", "省", "市", "区", "详细地址", "联系电话", "商品编号", "商品名称", "件数", "单价", "总价", "运费", "订单总价", "买家备注",
                    "卖家备注", "发货时间", "物流公司", "物流单号", "冻结状态", "发件人", "联系方式", "支付公司", "支付交易号"};
                workbook = POIUtil.createXSSFWorkbookTemplate(arr);
            }
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 0; i < resultList.size(); i++)
            {
                int cellIndex = 0;
                OrderDetailInfoForSeller curr = resultList.get(i);
                
                // 支付信息
                Map<String, String> payInfo = payPidMap.get(curr.getoId());
                String excelPayTid = "";
                String excelPayChannel = "";
                // if (!oNumber.startsWith("GGJ"))
                // {
                if (payInfo != null && payInfo.get("payTid") != null && !"".equals(payInfo.get("payTid")))
                {
                    excelPayTid = payInfo.get("payTid") + "";
                    excelPayChannel = payInfo.get("payChannel") + "";
                }
                else
                {
                    excelPayTid = "系统中不存在。";
                    excelPayChannel = "";
                }
                
                Row r = sheet.createRow(i + 1);
                r.createCell(cellIndex++).setCellValue(curr.getoNumber() + "");
                r.createCell(cellIndex++).setCellValue(curr.getoStatusDescripton() + "");
                if (xlsType == 1)
                {
                    r.createCell(cellIndex++).setCellValue(OrderEnum.ORDER_TYPE.getDescByCode(curr.getOrderType()));
                }
                r.createCell(cellIndex++).setCellValue(curr.getoCreateTime() + "");
                r.createCell(cellIndex++).setCellValue(curr.getoPayTime() + "");
                r.createCell(cellIndex++).setCellValue(curr.getRaFullName() + "");
                r.createCell(cellIndex++).setCellValue(curr.getRaIdCard() + "");
                r.createCell(cellIndex++).setCellValue(curr.getAddress() + "");
                r.createCell(cellIndex++).setCellValue(curr.getProvince() + "");
                r.createCell(cellIndex++).setCellValue(curr.getCity() + "");
                r.createCell(cellIndex++).setCellValue(curr.getDistrict() + "");
                r.createCell(cellIndex++).setCellValue(curr.getDetailAddress() + "");
                r.createCell(cellIndex++).setCellValue(curr.getRaMobileNumber() + "");
                String code = curr.getProductCode();
                Integer productCount = Integer.valueOf(curr.getProductCount() + "");
                Integer _num = 1;
                int index = code.lastIndexOf("%");
                boolean isdiv = false;
                if ((index > -1) && (index < code.length() - 1))
                {
                    String num = code.substring(index + 1);
                    if (StringUtils.isNumeric(num))
                    {
                        code = code.substring(0, index);
                        productCount = productCount * Integer.valueOf(num);
                        isdiv = true;
                        _num = Integer.parseInt(num);
                    }
                }
                r.createCell(cellIndex++).setCellValue(code);
                r.createCell(cellIndex++).setCellValue(curr.getProductName() + "");
                r.createCell(cellIndex++).setCellValue(productCount);
                if (isdiv)
                {
                    r.createCell(cellIndex++).setCellValue(new DecimalFormat("0.00").format(curr.getSalesPrice() / _num));
                }
                else
                {
                    r.createCell(cellIndex++).setCellValue(curr.getSalesPrice() + "");
                }
                r.createCell(cellIndex++).setCellValue(curr.getSmailTotalPrice() + "");
                r.createCell(cellIndex++).setCellValue(0);
                r.createCell(cellIndex++).setCellValue(new DecimalFormat("0.00").format(curr.getoTotalPrice() - curr.getoFreightMoney()));
                // 买家备注，暂停用
                r.createCell(cellIndex++).setCellValue("");
                // 卖家备注，
                r.createCell(cellIndex++).setCellValue(curr.getSellerMarks());
                r.createCell(cellIndex++).setCellValue(curr.getoSendTime() + "");
                r.createCell(cellIndex++).setCellValue(curr.getOlogChannel() + "");
                r.createCell(cellIndex++).setCellValue(curr.getOlogNumber() + "");
                Integer id = Integer.parseInt(curr.getoId() + "");
                if (secoundSearchOrderIdList.contains(id))
                {
                    r.createCell(cellIndex++).setCellValue("解冻订单");
                }
                else
                {
                    r.createCell(cellIndex++).setCellValue("普通订单");
                }
                if (curr.getOrderType() == OrderEnum.ORDER_TYPE.GEGETUAN.getCode())
                {
                    r.createCell(cellIndex++).setCellValue("心动慈露");
                }
                else if (curr.getAppChannel() == CommonEnum.OrderAppChannelEnum.QUAN_QIU_BU_SHOU.ordinal())
                {
                    r.createCell(cellIndex++).setCellValue("心动慈露");
                }
                else
                {
                    r.createCell(cellIndex++).setCellValue("心动慈露");
                }
                r.createCell(cellIndex++).setCellValue("0571-86888702");
                r.createCell(cellIndex++).setCellValue(excelPayChannel);
                r.createCell(cellIndex++).setCellValue(excelPayTid);
            }
            File file = new File(dir, fileName);
            file.createNewFile();
            fOut = new FileOutputStream(file);
            workbook.write(fOut);
            fOut.flush();
            resultList.clear();
        }
        catch (Exception e)
        {
            log.error("导出发货excel出错", e);
            throw new Exception(e);
        }
        finally
        {
            if (fOut != null)
            {
                try
                {
                    fOut.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            if (workbook != null)
            {
                try
                {
                    workbook.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    
    @Override
    public void updateOrderOperationStatus(Set<Integer> orderIds)
        throws Exception
    {
        List<Integer> orderList = new ArrayList<Integer>();
        for (Integer id : orderIds)
        {
            orderList.add(id);
        }
        if (orderList.size() > 0)
        {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("operationStatus", 1);
            map.put("idList", orderList);
            orderDao.updateOrderOperationStatus(map);
        }
    }
    
    @Override
    public OrderEntity findOrderByNumber(String number)
        throws Exception
    {
        return orderDao.findOrderByNumber(number);
    }
    
    @Override
    public String updateOrderStatus(int id, String number, int oldStatus, int newStatus, String marks)
        throws Exception
    {
        Map<String, Object> resultMap = new HashMap<>();
        if (OrderEnum.ORDER_STATUS.getOrderStatusEnum(newStatus) == null)
        {
            resultMap.put("status", "0");
            resultMap.put("msg", "修改失败，未知订单状态");
            return JSON.toJSONString(resultMap);
        }
        
        boolean isLogger = false;
        if (newStatus == OrderEnum.ORDER_STATUS.REVIEW.getCode())
        {
            if (number.endsWith(CommonConstant.ORDER_SUFFIX_GROUP))
            {
                // 心动慈露订单只能从OrderEnum.OrderStatusEnum.CREATE.getCode()——>OrderEnum.OrderStatusEnum.REVIEW.getCode()
                if (oldStatus != OrderEnum.ORDER_STATUS.CREATE.getCode())
                {
                    resultMap.put("status", "0");
                    resultMap.put("msg", "心动慈露订单只能从未付款改为待发货");
                    return JSON.toJSONString(resultMap);
                }
                else
                {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("id", id);
                    map.put("status", newStatus);
                    map.put("payTime", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
                    map.put("isGroup", CommonConstant.COMMON_NO);
                    orderDao.updateOrder(map);
                    isLogger = true;
                }
            }
            else
            {
                // 心动慈露订单只能从OrderEnum.OrderStatusEnum.TIMEOUT.getCode()或OrderEnum.OrderStatusEnum.USER_CANCEL.getCode()——>OrderEnum.OrderStatusEnum.REVIEW.getCode()
                if (oldStatus != OrderEnum.ORDER_STATUS.TIMEOUT.getCode() && oldStatus != OrderEnum.ORDER_STATUS.USER_CANCEL.getCode())
                {
                    resultMap.put("status", "0");
                    resultMap.put("msg", "心动慈露订单只能从【用户取消】或【超时取消】改为待发货");
                    return JSON.toJSONString(resultMap);
                }
                else
                {
                    // url = http://121.40.73.86:8080/ygg/order/modifynopayorderstatus?orderIds=+id
                    String url = UtilProperties.getInstance("config.properties").getProperties("update_status_url") + id;
                    Object result = restTemplate.getForObject(url, String.class);
                    log.info("调用" + url + "，返回结果：" + result);
                    if (result != null)
                    {
                        JSONObject jsonResult = JSON.parseObject(String.valueOf(result).toString());
                        if (jsonResult.getIntValue("status") == CommonConstant.COMMON_YES)
                        {
                            isLogger = true;
                        }
                        else
                        {
                            resultMap.put("status", "0");
                            resultMap.put("msg", "修改失败");
                            return JSON.toJSONString(resultMap);
                        }
                    }
                }
            }
        }
        else
        {
            if (number.endsWith(CommonConstant.ORDER_SUFFIX_GROUP))
            {
                // 团购订单用户取消状态为6
                newStatus = OrderEnum.ORDER_STATUS.TIMEOUT.getCode();
            }
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("id", id);
            map.put("status", newStatus);
            orderDao.updateOrder(map);
            isLogger = true;
        }
        resultMap.put("status", "1");
        resultMap.put("msg", "修改成功");
        return JSON.toJSONString(resultMap);
    }
    
    @Override
    public String jsonOrderOperationInfo(Map<String, Object> para)
        throws Exception
    {
    	return "";
    }
    
    @Override
    public int updateOrder(Map<String, Object> para)
        throws Exception
    {
        return orderDao.updateOrder(para);
    }
    
    @Override
    public int updatePrice(Map<String, Object> para)
        throws Exception
    {
        // 先判断订单状态是否符合要求
        Integer id = (Integer)para.get("id");
        OrderEntity order = orderDao.findOrderById(id);
        if (order.getStatus() != 1)
        {
            return 0;
        }
        int status = orderDao.updateOrder(para);
        if (status == 1)
        {
            log.warn("管理员修改订单价格，订单ID：" + id + "，订单原价格：" + order.getTotalPrice() + ",修改后的价格：" + para.get("totalPrice"));
        }
        return status;
    }
    
    @Override
    public String jsonAllCityByProvinceId(int id)
        throws Exception
    {
        return JSON.toJSONString(areaDao.findAllCityByProvinceId(id));
    }
    
    @Override
    public String jsonAllDistrictByCityId(int id)
        throws Exception
    {
        return JSON.toJSONString(areaDao.findAllDistrictByCityId(id));
    }
    
    @Override
    public Map<String, Object> updateAddress(ReceiveAddressEntity entity, int tradeId)
        throws Exception
    {
        Map<String, Object> map = new HashMap<String, Object>();
        OrderEntity order = orderDao.findOrderById(tradeId);
        
        Map<String, Object> address_map = orderDao.findReceiveInfoById(order.getReceiveAddressId());
        
        entity.setId(order.getReceiveAddressId());
        int status = orderDao.updateOrderAddress(entity);
        String msg = "";
        if (order.getOperationStatus() == 1 && (order.getStatus() != 1))
        {
            msg = "该订单已导出，且可能已经通知给商家，请找相关招商负责人确认。";
        }
        map.put("status", status);
        map.put("msg", msg);
        
        if (status == 1)
        {
            //String username = SecurityUtils.getSubject().getPrincipal() + "";
            StringBuffer sb = new StringBuffer("用户【】手动修改了订单收货地址，订单Id=" + tradeId + " ");
            String fullName = address_map.get("fullName").toString();
            String mobileNumber = address_map.get("mobileNumber").toString();
            String detailAddress = address_map.get("detailAddress").toString();
            String idCard = address_map.get("idCard").toString();
            String province = address_map.get("province").toString();
            String city = address_map.get("city").toString();
            String district = address_map.get("district").toString();
            
            if (!fullName.equals(entity.getFullName()))
            {
                sb.append("将 " + fullName + "修改为 " + entity.getFullName());
            }
            if (!mobileNumber.equals(entity.getMobileNumber()))
            {
                sb.append(" 将 " + mobileNumber + "修改为 " + entity.getMobileNumber());
            }
            if (!detailAddress.equals(entity.getDetailAddress()))
            {
                sb.append(" 将 " + detailAddress + "修改为 " + entity.getDetailAddress());
            }
            if (!idCard.equals(entity.getIdCard()))
            {
                sb.append(" 将 " + idCard + "修改为 " + entity.getIdCard());
            }
            if (!province.equals(entity.getProvince()))
            {
                sb.append(" 将 " + province + "修改为 " + entity.getProvince());
            }
            if (!city.equals(entity.getCity()))
            {
                sb.append(" 将 " + city + "修改为 " + entity.getCity());
            }
            if (!district.equals(entity.getDistrict()))
            {
                sb.append(" 将 " + district + "修改为 " + entity.getDistrict());
            }
            
            if (sb.toString().indexOf("将") > 0)
            {
                Map<String, Object> logPara = new HashMap<String, Object>();
                //logPara.put("username", username);
                logPara.put("businessType", CommonEnum.BusinessTypeEnum.ORDER_MANAGEMENT.ordinal());
                logPara.put("operationType", CommonEnum.OperationTypeEnum.MODIFY_ORDER_ADDRESS.ordinal());
                logPara.put("content", sb.toString());
                logPara.put("level", CommonEnum.LogLevelEnum.LEVEL_ONE.ordinal());
                //logDao.logger(logPara);
            }
            
        }
        return map;
    }
    
    @Override
    public List<Map<String, Object>> findAllUnSendGoodInfo()
        throws Exception
    {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Map<String, Object> para = new HashMap<String, Object>();
        // 查询 24hour <= payTime < 48小时
        para.put("payTimeBegin", DateTime.now().plusDays(-2).toString("yyyy-MM-dd HH:mm:ss"));
        para.put("payTimeEnd", DateTime.now().plusDays(-1).toString("yyyy-MM-dd HH:mm:ss"));
        List<Map<String, Object>> reList_24_to_48 = orderDao.findAllUnSendGoodInfo(para);
        
        // 查询 payTime <= 48hour
        para.remove("payTimeBegin");
        para.put("payTimeEnd", DateTime.now().plusDays(-2).toString("yyyy-MM-dd HH:mm:ss"));
        List<Map<String, Object>> reList_upper_48 = orderDao.findAllUnSendGoodInfo(para);
        
        // 查询15点前的
        para.put("payTimeBegin", DateTime.now().withHourOfDay(1).withMinuteOfHour(0).withSecondOfMinute(0).toString("yyyy-MM-dd HH:mm:ss"));
        para.put("payTimeEnd", DateTime.now().withHourOfDay(15).withMinuteOfHour(0).withSecondOfMinute(0).toString("yyyy-MM-dd HH:mm:ss"));
        List<Map<String, Object>> reList_before15 = orderDao.findAllUnSendGoodInfo(para);
        
        Set<Integer> sellerIds = new HashSet<Integer>();
        for (Map<String, Object> currMap : reList_24_to_48)
        {
            sellerIds.add(Integer.valueOf(currMap.get("id") + ""));
        }
        for (Map<String, Object> currMap : reList_upper_48)
        {
            sellerIds.add(Integer.valueOf(currMap.get("id") + ""));
        }
        for (Integer id : sellerIds)
        {
            Map<String, Object> cu = new HashMap<String, Object>();
            Map<String, Object> _24_to_48 = getMapBySellerId(reList_24_to_48, id.toString());
            Map<String, Object> _upper_48 = getMapBySellerId(reList_upper_48, id.toString());
            cu.put("id", id.toString());
            if (_24_to_48 != null)
            {
                cu.put("sellerName", _24_to_48.get("sellerName"));
                cu.put("responsibilityPerson", _24_to_48.get("responsibilityPerson"));
                cu.put("sendAddress", _24_to_48.get("sendAddress"));
                cu.put("warehouse", _24_to_48.get("warehouse"));
            }
            else
            {
                cu.put("sellerName", _upper_48.get("sellerName"));
                cu.put("responsibilityPerson", _upper_48.get("responsibilityPerson"));
                cu.put("sendAddress", _upper_48.get("sendAddress"));
                cu.put("warehouse", _upper_48.get("warehouse"));
            }
            
            if (_24_to_48 != null)
            {
                cu.put("countIds_24_to_48", _24_to_48.get("countIds"));
                cu.put("sumTotal_24_to_48", _24_to_48.get("sumTotal"));
            }
            if (_upper_48 != null)
            {
                cu.put("countIds_upper_48", _upper_48.get("countIds"));
                cu.put("sumTotal_upper_48", _upper_48.get("sumTotal"));
            }
            
            result.add(cu);
        }
        List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> map : reList_before15)
        {
            String sellerId = map.get("id") + "";
            Map<String, Object> tmp = getMapBySellerId(result, sellerId);
            if (tmp != null)
            {
                for (Map<String, Object> currMap : result)
                {
                    if (sellerId.equals(currMap.get("id") + ""))
                    {
                        currMap.put("countIds_before_15", map.get("countIds"));
                        currMap.put("sumTotal_before_15", map.get("sumTotal"));
                    }
                }
            }
            else
            {
                tmp = new HashMap<String, Object>();
                tmp.put("id", sellerId.toString());
                tmp.put("sellerName", map.get("sellerName"));
                tmp.put("responsibilityPerson", map.get("responsibilityPerson"));
                tmp.put("sendAddress", map.get("sendAddress"));
                tmp.put("warehouse", map.get("warehouse"));
                tmp.put("countIds_before_15", map.get("countIds"));
                tmp.put("sumTotal_before_15", map.get("sumTotal"));
                tempList.add(tmp);
            }
        }
        if (tempList.size() > 0)
        {
            result.addAll(tempList);
        }
        return result;
    }
    
    private Map<String, Object> getMapBySellerId(List<Map<String, Object>> reList, String id)
    {
        Map<String, Object> result = null;
        for (Map<String, Object> currMap : reList)
        {
            if (id.equals(currMap.get("id") + ""))
            {
                result = currMap;
                break;
            }
        }
        return result;
    }
    
    @Override
    public Map<String, Object> findAllUnSendGoodsDetail(Map<String, Object> para)
        throws Exception
    {
        Map<String, Object> result = new HashMap<String, Object>();
        List<Map<String, Object>> reList = orderDao.findAllUnSendGoodsDetail(para);
        int total = 0;
        if (reList.size() > 0)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (Map<String, Object> currMap : reList)
            {
                currMap.put("createTimeStr", sdf.format((Timestamp)currMap.get("createTime")));
                currMap.put("payTimeStr", sdf.format((Timestamp)currMap.get("payTime")));
                if ((currMap.get("number") + "").endsWith("1"))
                {
                    currMap.put("orderChannel", CommonEnum.OrderAppChannelEnum.getDescriptionByOrdinal(Integer.valueOf(currMap.get("appChannel") + "")));
                }
                else if ((currMap.get("number") + "").endsWith("2"))
                {
                    currMap.put("orderChannel", "网页");
                }
                currMap.put("statusStr", OrderEnum.ORDER_STATUS.getDescByCode(Integer.valueOf(currMap.get("status") + "")));
                // number 链接
                String number = currMap.get("number") + "";
                String hrefNumber = "<a target='_balnk' href='/yggManager/order/detail/" + Integer.valueOf(currMap.get("id") + "") + "'>" + number + "</a>";
                currMap.put("hrefNumber", hrefNumber);
            }
            total = orderDao.countAllUnSendGoodsDetail(para);
        }
        result.put("rows", reList);
        result.put("total", total);
        return result;
    }
    
    @Override
    public Map<String, Object> findAllOtherSource(Map<String, Object> para)
        throws Exception
    {
        List<Map<String, Object>> reList = orderDao.findAllOrderChannel(para);
        int total = 0;
        if (reList.size() > 0)
        {
            total = orderDao.countOrderChannel(para);
            for (Map<String, Object> currMap : reList)
            {
                currMap.put("osc", "m.gegejia.com?s=<span style='color:red'>" + currMap.get("mark") + "<span>");
            }
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("rows", reList);
        result.put("total", total);
        return result;
    }
    
    @Override
    public List<Map<String, Object>> searchOtherSourceNeedsInfo(int type)
        throws Exception
    {
        /**
         * type: 1:参数 ; 2:渠道 ; 3:负责人
         */
        Map<String, Object> para = new HashMap<String, Object>();
        List<Map<String, Object>> reList = orderDao.findAllOrderChannel(para);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        String code = "";
        String text = "";
        if (type == 1)
        {
            code = "mark";
            text = "mark";
        }
        else if (type == 2)
        {
            code = "name";
            text = "name";
        }
        else
        {
            code = "responsibilityPerson";
            text = "responsibilityPerson";
        }
        Map<String, Object> all = new HashMap<String, Object>();
        all.put("code", "");
        all.put("text", "全部");
        result.add(all);
        for (Map<String, Object> currMap : reList)
        {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("code", currMap.get(code));
            map.put("text", currMap.get(text));
            result.add(map);
        }
        return result;
    }
    
    @Override
    public Map<String, Object> saveOrUpdateChannel(Integer id, String name, String person)
        throws Exception
    {
        try
        {
            if (id == null)
            {
                // 新增
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("name", name);
                List<Map<String, Object>> reList = orderDao.findAllOrderChannel(map);
                if (reList.size() < 1)
                {
                    // String mark = CommonUtil.generateCode();
                    Map<String, Object> para = new HashMap<String, Object>();
                    para.put("name", name);
                    para.put("responsibilityPerson", person);
                    // para.put("mark", mark);
                    orderDao.saveOrderChannel(para);
                    Integer newId = (Integer)para.get("id");
                    // 更新 mark == newId
                    Map<String, Object> markPara = new HashMap<String, Object>();
                    markPara.put("id", newId);
                    markPara.put("mark", newId);
                    orderDao.updateOrderChannel(markPara);
                }
                else
                {
                    Map<String, Object> result = new HashMap<String, Object>();
                    result.put("status", 0);
                    result.put("msg", "已存在该渠道");
                    return result;
                }
            }
            else
            {
                // 更新
                Map<String, Object> para = new HashMap<String, Object>();
                para.put("id", id);
                para.put("name", name);
                para.put("responsibilityPerson", person);
                orderDao.updateOrderChannel(para);
            }
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("status", 1);
            return result;
        }
        catch (Exception e)
        {
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("status", 0);
            result.put("msg", "保存失败");
            return result;
        }
    }
    
    @Override
    public Map<String, Object> deleteOrderChannel(int id)
        throws Exception
    {
        try
        {
            int count = orderDao.countOrderBySourceChannelId(id);
            Map<String, Object> result = new HashMap<String, Object>();
            if (count > 0)
            {
                result.put("status", 0);
                result.put("msg", "该来源正在使用中");
                return result;
            }
            int status = orderDao.deleteOrderChannel(id);
            result.put("status", status);
            if (status != 1)
            {
                result.put("msg", "保存失败");
            }
            return result;
        }
        catch (Exception e)
        {
            Map<String, Object> result = new HashMap<String, Object>();
            result.put("status", 0);
            result.put("msg", "删除失败");
            return result;
        }
    }
    
    @Override
    public OrderEntity findOrderById(int id)
        throws Exception
    {
        
        return orderDao.findOrderById(id);
    }
    
    @Override
    public List<Map<String, Object>> findAccountSpending()
        throws Exception
    {
        List<Map<String, Object>> result = orderDao.findAccountSpending();
        return result == null ? new ArrayList<Map<String, Object>>() : result;
    }
    
    @Override
    public Map<String, Object> getFakeOrderJsonInfo(Map<String, Object> para)
        throws Exception
    {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime nowTime = formatter.parseDateTime(DateTime.now().toString("yyyy-MM-dd"));
        Map<String, Object> result = new HashMap<String, Object>();
        List<Map<String, Object>> reList = orderDao.queryAllFakeOrder(para);
        int total = 0;
        if (reList != null && reList.size() > 0)
        {
            for (Map<String, Object> currMap : reList)
            {
                int sellerType = Integer.valueOf(currMap.get("sellerType") + "");
                int isNeedIdcardNumber = Integer.valueOf(currMap.get("isNeedIdcardNumber") + "");
                int isNeedIdcardImage = Integer.valueOf(currMap.get("isNeedIdcardImage") + "");
                if (sellerType == SellerEnum.SellerTypeEnum.HONG_KONG.getCode())
                {
                    currMap.put("sellerType",
                        SellerEnum.SellerTypeEnum.getDescByCode(sellerType) + (isNeedIdcardImage == 1 ? "(身份证照片)" : (isNeedIdcardNumber == 1 ? "(身份证号)" : "")));
                }
                else
                {
                    currMap.put("sellerType", SellerEnum.SellerTypeEnum.getDescByCode(sellerType));
                }
                DateTime beginTime = DateTimeUtil.string2DateTime(currMap.get("createTime") + "", "yyyy-MM-dd");
                currMap.put("days", Days.daysBetween(beginTime, nowTime).getDays());
                currMap.put("isFakeOrder", Integer.valueOf(currMap.get("isFakeOrder") + "") == 1 ? "是" : "否");
            }
            total = orderDao.countAllFakeOrder(para);
        }
        result.put("rows", reList);
        result.put("total", total);
        return result;
    }
    
    @Override
    public int orderLogisticsAgain(Map<String, Object> para)
        throws Exception
    {
        if (para == null || para.size() == 0)
        {
            return 0;
        }
        if (para.get("id") == null && para.get("idList") == null)
        {
            return 0;
        }
        return orderDao.orderLogisticsAgain(para);
    }
    
    @Override
    public boolean importTest(Map<String, Object> para)
        throws Exception
    {
        List<Map<String, Object>> resultList = (List<Map<String, Object>>)para.get("resultList");
        Map<String, Object> sendStatusMap = new HashMap<String, Object>();
        String orderNumber = para.get("orderNumber") + "";
        if (orderNumber.startsWith("HB"))
        {
        	return false;
        }
        else
        {
            if (!StringUtils.isNumeric(orderNumber))
            {
                sendStatusMap.put("status", "失败");
                sendStatusMap.put("msg", "非法订单号");
                sendStatusMap.put("orderNumber", orderNumber);
                sendStatusMap.put("code", 1);
                resultList.add(sendStatusMap);
                return false;
            }
            String settlementDate = para.get("settlementDate") + "";
            int nType = CommonUtil.estimateOrderNumber(orderNumber);
            if (nType == 1)
            {
                
            	return false;
            }
            else
            {
                // 正常订单
                OrderEntity order = orderDao.findOrderByNumber(orderNumber);
                if (order == null)
                {
                    sendStatusMap.put("status", "失败");
                    sendStatusMap.put("msg", "订单号不存在");
                    sendStatusMap.put("orderNumber", orderNumber);
                    sendStatusMap.put("code", 1);
                    resultList.add(sendStatusMap);
                    return false;
                }
                else
                {
                    if (order.getIsSettlement() == 1)
                    {
                        sendStatusMap.put("status", "失败");
                        sendStatusMap.put("msg", "订单已结算");
                        sendStatusMap.put("orderNumber", orderNumber);
                        sendStatusMap.put("code", 2);
                        resultList.add(sendStatusMap);
                        return false;
                    }
                    else if (order.getIsNeedSettlement() == 0)
                    {
                        sendStatusMap.put("status", "失败");
                        sendStatusMap.put("msg", "订单无需结算");
                        sendStatusMap.put("orderNumber", orderNumber);
                        sendStatusMap.put("code", 5);
                        resultList.add(sendStatusMap);
                        return false;
                    }
                    else if (order.getIsSettlement() == 0)
                    {
                        sendStatusMap.put("status", "成功");
                        sendStatusMap.put("msg", "ok");
                        sendStatusMap.put("orderNumber", orderNumber);
                        sendStatusMap.put("code", 3);
                        resultList.add(sendStatusMap);
                        return true;
                    }
                    else
                    {
                        sendStatusMap.put("status", "失败");
                        sendStatusMap.put("msg", "未知错误");
                        sendStatusMap.put("orderNumber", orderNumber);
                        sendStatusMap.put("code", 4);
                        resultList.add(sendStatusMap);
                        return false;
                    }
                }
            }
        }
    }
    
    @Override
    public int updateOrderSettlement(Map<String, Object> para)
        throws Exception
    {
        List<Map<String, Object>> resultList = (List<Map<String, Object>>)para.get("resultList");
        int status = 0;
        int isNeedSettlement = Integer.valueOf(para.get("isNeedSettlement") == null ? "1" : para.get("isNeedSettlement") + "");
        int updateIsNeedSettlement = Integer.valueOf(para.get("updateIsNeedSettlement") == null ? "0" : para.get("updateIsNeedSettlement") + "");
        String orderNumber = para.get("orderNumber") + "";
        String isSettlement = para.get("isSettlement") + "";
        String settlementDate = para.get("settlementDate") + "";
        if (orderNumber.startsWith("HB"))
        {
           
        }
        else
        {
            int nType = CommonUtil.estimateOrderNumber(orderNumber);
            if (nType == 1)
            {
               
            }
            else
            {
                Map<String, Object> upPara = new HashMap<String, Object>();
                upPara.put("orderNumber", orderNumber);
                if (updateIsNeedSettlement == 1)
                {
                    // 修改订单是否需要结算
                    upPara.put("isNeedSettlement", isNeedSettlement);
                    status = orderDao.updateOrderIsNeedSettlement(upPara);
                }
                else
                {
                    upPara.put("isSettlement", isSettlement);
                    upPara.put("settlementDate", settlementDate);
                    status = orderDao.updateOrderSettlement(upPara);
                }
            }
        }
        Map<String, Object> sendStatusMap = new HashMap<>();
        sendStatusMap.put("status", status == 1 ? "成功" : "失败");
        sendStatusMap.put("msg", "");
        sendStatusMap.put("orderNumber", orderNumber);
        resultList.add(sendStatusMap);
        return status;
    }
    
    @Override
    public Map<String, Object> getOrderRefundInfo(Long number)
        throws Exception
    {
        Map<String, Object> info = new HashMap<String, Object>();
        info.put("status", 1);
        OrderEntity entity = orderDao.findOrderByNumber(number + "");
        if (entity == null)
        {
            info.put("status", 0);
            return info;
        }
        if (entity.getStatus() == OrderEnum.ORDER_STATUS.CREATE.getCode() || entity.getStatus() == OrderEnum.ORDER_STATUS.USER_CANCEL.getCode()
            || entity.getStatus() == OrderEnum.ORDER_STATUS.TIMEOUT.getCode())
        {
            info.put("status", 2);
            return info;
        }
        info.put("orderId", entity.getId());
        info.put("accountId", entity.getAccountId());
        // 获取收货地址相关信息
        Map<String, Object> receiveInfo = orderDao.findReceiveInfoById(entity.getReceiveAddressId());
        info.put("receiveInfo", receiveInfo);
        // 获取订单商品相关信息
        List<Map<String, Object>> orderProductList = orderDao.findProductInfoByOrderId(entity.getId());
        info.put("orderProduct", orderProductList);
        // 获取订单用户对应账号卡号信息
        Map<String, Object> accountCardPara = new HashMap<String, Object>();
        accountCardPara.put("accountId", entity.getAccountId());
        List<Map<String, Object>> cardList = accountDao.findAllAccountCard(accountCardPara);
        List<Map<String, Object>> accountCardList = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> it : cardList)
        {
            Map<String, Object> itmap = new HashMap<String, Object>();
            itmap.put("id", it.get("acId"));
            int type = Integer.valueOf(it.get("type") + "");
            int bankType = Integer.valueOf(it.get("bankType") + "");
            String name = "";
            if (type == 1)
            {
                name = "银行-" + CommonEnum.BankTypeEnum.getDescriptionByOrdinal(bankType) + "-" + it.get("cardNumber");
            }
            else
            {
                name = "支付宝-" + it.get("cardNumber");
            }
            itmap.put("name", name);
            accountCardList.add(itmap);
        }
        info.put("accountCardList", accountCardList);
        return info;
    }
    
    @Override
    public Map<String, Object> calOrderRefundInfoByOrderNumber(long number, int selectProductCount, int orderProductId, int from)
        throws Exception
    {
        // 计算分摊优惠券、分摊积分、理论申请退款金额 、理论应返还积分、 理论应扣除积分
        OrderEntity order = orderDao.findOrderByNumber(number + "");
        Map<String, Object> result = calOrderProduct(order, selectProductCount, orderProductId, from == 1);
        return result;
    }
    
    @Override
    public Map<String, Object> calOrderRefundInfoByOrderId(int orderId, int selectProductCount, int orderProductId)
        throws Exception
    {
        // 计算分摊优惠券、分摊积分、理论申请退款金额 、理论应返还积分、 理论应扣除积分
        OrderEntity order = orderDao.findOrderById(orderId);
        Map<String, Object> result = calOrderProduct(order, selectProductCount, orderProductId, false);
        return result;
    }
    
    public Map<String, Object> calOrderProduct(OrderEntity order, int selectProductCount, int orderProductId, boolean minus)
        throws Exception
    {
        
        double adjustPrice = order.getAdjustPrice();
        double freightMoney = order.getFreightMoney();
        double totalPrice = order.getTotalPrice() + order.getAdjustPrice() - freightMoney; // 订单商品总金额
        double couponPrice = order.getCouponPrice(); // 订单优惠券金额
        double activitiesPrice = order.getActivitiesPrice(); // 订单满减金额
        double activitiesOptionalPartPrice = order.getActivitiesOptionalPartPrice(); // 订单N元任选金额
        int accountPoint = order.getAccountPoint(); // 订单使用积分
        
        // 获取订单商品相关信息
        List<Map<String, Object>> orderProductList = orderDao.findProductInfoByOrderId(order.getId());
        double salesPrice = 0;
        int productCount = 0;
        for (Map<String, Object> it : orderProductList)
        {
            Integer itId = Integer.parseInt(it.get("id") + "");
            if (itId == orderProductId)
            {
                salesPrice = Float.parseFloat(it.get("salesPrice") + "");
                productCount = Integer.parseInt(it.get("productCount") + "");
            }
        }
        
        Map<String, Object> result = new HashMap<String, Object>();
        int status = 0;
        if (salesPrice > 0 && productCount > 0)
        {
            status = 1;
            double selectPrice = selectProductCount * salesPrice;
            double adjustProportion = Double.parseDouble(MathUtil.round((selectPrice / totalPrice) * adjustPrice, 2));// 分摊客服改价
            double activitiesProportion = Double.parseDouble(MathUtil.round((selectPrice / totalPrice) * activitiesPrice, 2));// 分摊满减金额
            double activitiesOptionalPartProportion = Double.parseDouble(MathUtil.round((selectPrice / totalPrice) * activitiesOptionalPartPrice, 2));// 分摊N元任选优惠金额
            double freightMoneyProportion = Double.parseDouble(MathUtil.round((selectPrice / totalPrice) * freightMoney, 2));// 分摊邮费
            // double freightMoneyProportion = 0;//分摊邮费
            double couponProportion = Double.parseDouble(MathUtil.round((selectPrice / totalPrice) * couponPrice, 2));// 分摊优惠券金额
            double pointProportion = Double.parseDouble(MathUtil.round((selectPrice / totalPrice) * accountPoint / 100.0, 2));// 分摊积分抵扣
            double logicApplyPrice = Double.parseDouble(MathUtil
                .round(selectPrice + freightMoneyProportion - adjustProportion - couponProportion - pointProportion - activitiesProportion - activitiesOptionalPartProportion, 2));// 理论申请退款金额
                
            // 假如订单下所有商品退款，最后一个商品的理论申请退款金额用差额来计算
            double alreadyApplyMoney = 0;
            if (orderProductList.size() > 1)
            {
                for (Map<String, Object> it : orderProductList)
                {
                    Integer itId = Integer.parseInt(it.get("id") + "");
                    if (itId == orderProductId)
                    {
                        continue;
                    }
                    Map<String, Object> refundPara = new HashMap<>();
                    refundPara.put("statusList", Arrays.asList(CommonEnum.RefundStatusEnum.SUCCESS.ordinal(), CommonEnum.RefundStatusEnum.WAIT_RETURN_OF_MONEY.ordinal(),
                        CommonEnum.RefundStatusEnum.WAIT_RETURN_OF_GOODS.ordinal()));
                    refundPara.put("orderProductId", itId);
                    List<RefundEntity> res = refundDao.findAllRefundByPara(refundPara);
                    if (res.size() == 0)
                    {
                        alreadyApplyMoney = 0;
                        break;
                    }
                    else
                    {
                        RefundEntity re = res.get(0);
                        alreadyApplyMoney += re.getRealMoney();
                    }
                }
            }
            if (alreadyApplyMoney == 0 && minus)
            {
                // 查询正在申请中的退款
                if (orderProductList.size() > 1)
                {
                    for (Map<String, Object> it : orderProductList)
                    {
                        Integer itId = Integer.parseInt(it.get("id") + "");
                        if (itId == orderProductId)
                        {
                            continue;
                        }
                        Map<String, Object> refundPara = new HashMap<>();
                        refundPara.put("statusList", Arrays.asList(CommonEnum.RefundStatusEnum.APPLY.ordinal()));
                        refundPara.put("orderProductId", itId);
                        List<RefundEntity> res = refundDao.findAllRefundByPara(refundPara);
                        if (res.size() == 0)
                        {
                            alreadyApplyMoney = 0;
                            break;
                        }
                        else
                        {
                            RefundEntity re = res.get(0);
                            alreadyApplyMoney += re.getApplyMoney();
                        }
                    }
                }
            }
            
            if (alreadyApplyMoney > 0)
            {
                logicApplyPrice = Double.valueOf(MathUtil.round(order.getRealPrice() - alreadyApplyMoney, 2));
                logicApplyPrice = logicApplyPrice <= 0 ? 0 : logicApplyPrice;
            }
            
            long logicGiveAccountPoint = Math.round(pointProportion * 100);// 理论应返还积分
            long logicRemoveAccountPoint = Math.round(logicApplyPrice);// 理论应扣除积分 == 理论申请退款金额
            result.put("selectProductCount", selectProductCount + "");
            result.put("selectPrice", MathUtil.round(selectPrice, 2));
            result.put("freightMoneyProportion", freightMoneyProportion + "");
            result.put("adjustProportion", adjustProportion + "");
            result.put("activitiesProportion", activitiesProportion + "");
            result.put("activitiesOptionalPartProportion", activitiesOptionalPartProportion + "");
            result.put("couponProportion", couponProportion + "");
            result.put("pointProportion", pointProportion + "");
            result.put("logicApplyPrice", logicApplyPrice + "");
            result.put("logicGiveAccountPoint", logicGiveAccountPoint + "");
            result.put("logicRemoveAccountPoint", logicRemoveAccountPoint + "");
            
            if (order.getType() == OrderEnum.ORDER_TYPE.GEGETUAN.getCode())
            {
                result.put("logicRemoveAccountPoint", "0");
            }
        }
        result.put("status", status);
        result.put("msg", status == 1 ? "请求数据成功" : "请求数据失败");
        return result;
    }
    
    @Override
    public Map<String, Object> findAllOrderFreeze(Map<String, Object> para)
        throws Exception
    {
        Map<String, Object> result = new HashMap<String, Object>();
        List<Map<String, Object>> freezeList = orderDao.findAllOrderFreeze(para);
        int total = 0;
        if (freezeList.size() > 0)
        {
            for (Map<String, Object> it : freezeList)
            {
                it.put("freezeStatusStr", OrderEnum.ORDER_FREEZE_STATUS.getDescByCode(Integer.parseInt(it.get("freezeStatus") + "")));
                it.put("orderStatusStr", OrderEnum.ORDER_STATUS.getDescByCode(Integer.parseInt(it.get("orderStatus") + "")));
                it.put("freezeTime", DateTimeUtil.timestampObjectToString(it.get("freezeTime")));
                it.put("unfreezeTime", DateTimeUtil.timestampObjectToString(it.get("unfreezeTime")));
                it.put("payTime", DateTimeUtil.timestampObjectToString(it.get("payTime")));
            }
            total = orderDao.countOrderFreeze(para);
        }
        result.put("rows", freezeList);
        result.put("total", total);
        return result;
    }
    
    @Override
    public Map<String, Object> orderSendTimeAnalyzeDetail(Map<String, Object> para)
        throws Exception
    {
        String beginHour = para.get("beginHour") + "";
        String endHour = para.get("endHour") + "";
        if ("0".equals(beginHour) && "0".equals(endHour))
        {
            para.remove("beginHour");
            para.remove("endHour");
        }
        if (StringUtils.isEmpty(beginHour))
        {
            para.remove("beginHour");
        }
        if (StringUtils.isEmpty(endHour))
        {
            para.remove("endHour");
        }
        if ("-1".equals(beginHour) && "-1".equals(endHour))
        {
            para.put("sendTime", "0000-00-00 00:00:00");
            para.remove("beginHour");
            para.remove("endHour");
        }
        // 若有订单来源，先查询来源ID
        String orderSource = (String)para.get("orderSource");
        if (orderSource != null)
        {
            Map<String, Object> sourcePara = new HashMap<String, Object>();
            sourcePara.put("name", orderSource);
            List<Map<String, Object>> reList = orderDao.findAllOrderChannel(sourcePara);
            if (reList.size() > 0)
            {
                para.put("orderSourceId", reList.get(0).get("id"));
            }
        }
        List<OrderInfoForManage> reList = orderDao.findAllOrderSendTimeAnalyze(para);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        int total = 0;
        if (reList.size() > 0)
        {
            for (OrderInfoForManage curr : reList)
            {
                Map<String, Object> map = curr.getMapValue();
                resultList.add(map);
            }
            total = orderDao.countAllOrderSendTimeAnalyze(para);
        }
        resultMap.put("rows", resultList);
        resultMap.put("total", total);
        return resultMap;
    }
    
    @Override
    public Map<String, Object> orderLogisticAnalyzeDetail(Map<String, Object> para)
        throws Exception
    {
        int hour = Integer.parseInt(para.get("hour") + "");
        List<Map<String, Object>> orderInfoList = orderDao.findOrderPayRecordForLogisticAnalyze(para);
        Map<String, List<Map<String, Object>>> groupByDayMap = new HashMap<String, List<Map<String, Object>>>();
        Timestamp sendTime = null;
        for (Map<String, Object> it : orderInfoList)
        {
            sendTime = (Timestamp)it.get("sendTime");
            int day = new DateTime(sendTime.getTime()).getDayOfMonth();
            String key = day + "";
            List<Map<String, Object>> dayList = groupByDayMap.get(key);
            if (dayList == null)
            {
                dayList = new ArrayList<Map<String, Object>>();
                groupByDayMap.put(key, dayList);
            }
            dayList.add(it);
        }
        List<Integer> dayOfHalfOrderIdList = new ArrayList<Integer>();// 0.5天有物流信息订单Id
        List<Integer> dayOf1OrderIdList = new ArrayList<Integer>();// 1天有物流信息订单Id
        List<Integer> dayOf2OrderIdList = new ArrayList<Integer>();// 2天有物流信息订单Id
        List<Integer> dayOf3OrderIdList = new ArrayList<Integer>();// 3天有物流信息订单Id
        List<Integer> dayOf4OrderIdList = new ArrayList<Integer>();// 4天有物流信息订单Id
        List<Integer> dayOf5OrderIdList = new ArrayList<Integer>();// 5天有物流信息订单Id
        List<Integer> noLogisticsOrderIdList = new ArrayList<Integer>();// 未发货订单Id
        for (Map.Entry<String, List<Map<String, Object>>> entry : groupByDayMap.entrySet())
        {
            List<Map<String, Object>> dayList = entry.getValue();
            for (Map<String, Object> it : dayList)
            {
                int orderId = Integer.parseInt(it.get("orderId") + "");
                DateTime sendDateTime = new DateTime((Timestamp)it.get("sendTime"));
                String channel = it.get("channel").toString();
                String number = it.get("number").toString();
                DateTime logisticsTime = orderDao.finOrderLogisticsTimeByChannelAndNumber(channel, number);
                if (logisticsTime == null)
                {
                    noLogisticsOrderIdList.add(orderId);
                    
                }
                else
                {
                    int hourMius = Hours.hoursBetween(sendDateTime, logisticsTime).getHours();
                    if (hourMius <= 12)
                    {
                        dayOfHalfOrderIdList.add(orderId);
                        
                    }
                    else if (hourMius <= 24)
                    {
                        dayOf1OrderIdList.add(orderId);
                        
                    }
                    else if (hourMius <= 48)
                    {
                        dayOf2OrderIdList.add(orderId);
                        
                    }
                    else if (hourMius <= 72)
                    {
                        dayOf3OrderIdList.add(orderId);
                    }
                    else if (hourMius <= 96)
                    {
                        dayOf4OrderIdList.add(orderId);
                    }
                    else
                    {
                        dayOf5OrderIdList.add(orderId);
                    }
                }
            }
        }
        if (hour == -1)
        {
            para.put("idList", noLogisticsOrderIdList);
            
        }
        else if (hour == 0)
        {
            ;
        }
        else if (hour == 12)
        {
            para.put("idList", dayOfHalfOrderIdList);
        }
        else if (hour == 24)
        {
            para.put("idList", dayOf1OrderIdList);
        }
        else if (hour == 48)
        {
            para.put("idList", dayOf2OrderIdList);
        }
        else if (hour == 72)
        {
            para.put("idList", dayOf3OrderIdList);
        }
        else if (hour == 96)
        {
            para.put("idList", dayOf4OrderIdList);
        }
        else if (hour == 120)
        {
            para.put("idList", dayOf5OrderIdList);
        }
        List<OrderInfoForManage> reList = orderDao.findAllOrderLogisticAnalyzeDetail(para);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        int total = 0;
        if (reList.size() > 0)
        {
            for (OrderInfoForManage curr : reList)
            {
                Map<String, Object> map = curr.getMapValue();
                resultList.add(map);
            }
            total = orderDao.countAllOrderLogisticAnalyzeDetail(para);
        }
        resultMap.put("rows", resultList);
        resultMap.put("total", total);
        return resultMap;
    }
    
    @Override
    public Map<String, Object> findAllProblemOrderList(Map<String, Object> para)
        throws Exception
    {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        DateTime nowTime = formatter.parseDateTime(DateTime.now().toString("yyyy-MM-dd"));
        Map<String, Object> result = new HashMap<String, Object>();
        List<Map<String, Object>> reList = orderDao.findAllProblemOrder(para);
        int total = 0;
        if (reList != null && reList.size() > 0)
        {
            for (Map<String, Object> currMap : reList)
            {
                int sellerType = Integer.valueOf(currMap.get("sellerType") + "");
                int isNeedIdcardNumber = Integer.valueOf(currMap.get("isNeedIdcardNumber") + "");
                int isNeedIdcardImage = Integer.valueOf(currMap.get("isNeedIdcardImage") + "");
                int logisticsStatus = Integer.parseInt(currMap.get("logisticsStatus") + "");
                if (sellerType == SellerEnum.SellerTypeEnum.HONG_KONG.getCode())
                {
                    currMap.put("sellerType",
                        SellerEnum.SellerTypeEnum.getDescByCode(sellerType) + (isNeedIdcardImage == 1 ? "(身份证照片)" : (isNeedIdcardNumber == 1 ? "(身份证号)" : "")));
                }
                else
                {
                    currMap.put("sellerType", SellerEnum.SellerTypeEnum.getDescByCode(sellerType));
                }
                DateTime beginTime = DateTimeUtil.string2DateTime(currMap.get("createTime") + "", "yyyy-MM-dd");
                currMap.put("days", Days.daysBetween(beginTime, nowTime).getDays());
                currMap.put("logisticsStatus", CommonEnum.LogisticsStateEnum.getDescription(logisticsStatus));
            }
            total = orderDao.countAllProblemOrder(para);
        }
        result.put("rows", reList);
        result.put("total", total);
        return result;
    }
    
    @Override
    public Workbook saveOrderHBFromBeiBeiForBirdex(List<Map<String, Object>> data, String[] exportTitle)
        throws Exception
    {
        Workbook workbook = POIUtil.createHSSFWorkbookTemplate(exportTitle);
        String errorMsg = "";
        try
        {
            // 将所有记录按 收货人 + 收货地址 分组
            /*
             * 0: 订单编号 1: 交易id 2: 买家备注 3: 商家名称 4: 省份 5: 市区 6: 乡县 7: 地址 8: 身份证号码 9: 订单来源 10: 专场id 11: 最终价格 12: 创建时间 13:
             * 支付时间 14: 发票类型 15: 发票抬头 16: 收件人 17: 收件人手机 18: 收件地址 19: 卖家备注 20: 运费 21: 商品总数 22: 支付渠道 23: 支付号信息 24: 商品id
             * 25: 商家编码 26: 货号 27: 商品名称 28: 品牌名称 29: 商品价格 30: 款式 31: 数量 32: 小计 33: SKU属性 34: 创建时间
             */
            Map<String, List<Map<String, Object>>> rowGroupByBuyerKey = new HashMap<>();
            int breakIndex = 0;
            for (Map<String, Object> it : data)
            {
                if (it.get("cell16") == null || "".equals(it.get("cell16")) || it.get("cell18") == null || "".equals(it.get("cell18")))
                {
                    // 遇到连续空行的话直接跳出
                    if (breakIndex > 10)
                    {
                        errorMsg = "文件中存在许多空行，请检查。";
                        throw new ServiceException("贝贝网转笨鸟订单失败！");
                    }
                    else
                    {
                        breakIndex++;
                        continue;
                    }
                }
                breakIndex = 0;
                
                String key = it.get("cell16") + "_" + it.get("cell18");
                List<Map<String, Object>> currList = rowGroupByBuyerKey.get(key);
                if (currList == null)
                {
                    currList = new ArrayList<>();
                    rowGroupByBuyerKey.put(key, currList);
                }
                currList.add(it);
            }
            List<Map<String, Object>> exportMapList = new ArrayList<>();
            List<Map<String, Object>> insertMapList = new ArrayList<>();
            Set<String> productCodeSet = new HashSet<>();
            for (Map.Entry<String, List<Map<String, Object>>> e : rowGroupByBuyerKey.entrySet())
            {
                List<Map<String, Object>> value = e.getValue();
                
                // 每个合并订单不大于500
                double totalPrice = 0;
                List<Map<String, Object>> tempList = new ArrayList<>();
                
                List<List<Map<String, Object>>> group500List = new ArrayList<>();
                for (Map<String, Object> it : value)
                {
                    int productCount = Integer.valueOf((it.get("cell31") + "").trim());
                    double productPrice = Double.valueOf((it.get("cell29") + "").trim());
                    totalPrice += (productPrice * productCount);
                    if (totalPrice > 500)
                    {
                        List<Map<String, Object>> cclist = tempList;
                        group500List.add(cclist);
                        tempList = new ArrayList<>();
                        totalPrice = productPrice * productCount;
                    }
                    tempList.add(it);
                }
                if (tempList.size() > 0)
                {
                    group500List.add(tempList);
                }
                
                for (List<Map<String, Object>> list : group500List)
                {
                    long number = CommonUtil.generateOutNumber();
                    for (Map<String, Object> it : list)
                    {
                        // 处理编码、单价、数量
                        int productCount = Integer.valueOf((it.get("cell31") + "").trim());
                        double productPrice = Double.valueOf((it.get("cell29") + "").trim());
                        String code = (it.get("cell25") + "").trim();
                        int index = code.lastIndexOf("%");
                        if ((index > -1) && (index < code.length() - 1))
                        {
                            // 有效
                            String num = code.substring(index + 1);
                            if (StringUtils.isNumeric(num))
                            {
                                // 数量*num
                                productCount = productCount * Integer.valueOf(num);
                                productPrice = productPrice / Integer.valueOf(num);
                                code = code.substring(0, index);
                            }
                        }
                        
                        productCodeSet.add(code);
                        
                        String outNumber = it.get("cell0") + "";
                        Map<String, Object> insertMap = new HashMap<>();
                        insertMap.put("hbNumber", number);
                        insertMap.put("sonNumber", outNumber);
                        insertMapList.add(insertMap);
                        
                        Map<String, Object> exportMap = new HashMap<>();
                        exportMap.put("number", number);
                        exportMap.put("outNumber", outNumber);
                        exportMap.put("receivePeople", it.get("cell16"));
                        exportMap.put("idCard", it.get("cell8"));
                        exportMap.put("mobileNumber", it.get("cell17"));
                        exportMap.put("province", it.get("cell4"));
                        exportMap.put("city", it.get("cell5"));
                        exportMap.put("distinct", it.get("cell6"));
                        exportMap.put("detailAddress", it.get("cell7"));
                        exportMap.put("productCode", code);
                        exportMap.put("productCount", productCount + "");
                        exportMap.put("productPrice", MathUtil.round(productPrice, 2));
                        exportMap.put("payChannel", it.get("cell22"));
                        exportMap.put("payTime", it.get("cell13"));
                        exportMap.put("payId", it.get("cell23"));
                        exportMap.put("payPrice", it.get("cell11"));
                        exportMap.put("remark", it.get("cell2") == null ? "" : it.get("cell2") + "");
                        exportMapList.add(exportMap);
                    }
                }
                if (insertMapList.size() > 1000)
                {
                    // 批量插入数据库
                    if (orderDao.batchInsertOutOrderHbRecord(insertMapList) > 0)
                    {
                        log.info("批量插入外部订单合并记录成功!");
                        insertMapList = new ArrayList<>();
                    }
                }
            }
            
            if (insertMapList.size() > 0)
            {
                // 批量插入数据库
                if (orderDao.batchInsertOutOrderHbRecord(insertMapList) > 0)
                {
                    log.info("批量插入外部订单合并记录成功!");
                }
            }
            
            // 查询品牌信息
            Map<String, Object> brandMapByCodeKey = new HashMap<>();
            if (productCodeSet.size() > 0)
            {
                List<Map<String, Object>> brandList = productDao.findProductBrandsByCodeList(CommonUtil.setToList(productCodeSet));
                for (Map<String, Object> it : brandList)
                {
                    String code = it.get("code") + "";
                    String name = it.get("name") + "";
                    brandMapByCodeKey.put(code, name);
                }
            }
            
            // 导出excel发货表
            Sheet sheet = workbook.getSheetAt(0);
            int rIndex = 1;
            for (Map<String, Object> it : exportMapList)
            {
                int cellIndex = 0;
                Row r = sheet.createRow(rIndex++);
                r.createCell(cellIndex++).setCellValue(it.get("number") + "");
                r.createCell(cellIndex++).setCellValue("香港天水围仓-HKT");
                r.createCell(cellIndex++).setCellValue("香港标准服务-HKBDX");
                r.createCell(cellIndex++).setCellValue(it.get("productCode") + "");
                r.createCell(cellIndex++).setCellValue(it.get("productCount") + "");
                r.createCell(cellIndex++).setCellValue(it.get("productPrice") + "");
                r.createCell(cellIndex++).setCellValue("人民币");
                r.createCell(cellIndex++).setCellValue(it.get("receivePeople") + "");
                r.createCell(cellIndex++).setCellValue(it.get("mobileNumber") + "");
                r.createCell(cellIndex++).setCellValue("");//
                r.createCell(cellIndex++).setCellValue(it.get("province") + "");
                r.createCell(cellIndex++).setCellValue(it.get("city") + "");
                r.createCell(cellIndex++).setCellValue(it.get("distinct") + "");
                r.createCell(cellIndex++).setCellValue(it.get("detailAddress") + "");
                r.createCell(cellIndex++).setCellValue("000000");//
                r.createCell(cellIndex++).setCellValue(it.get("idCard") + "");
                r.createCell(cellIndex++).setCellValue("Y");
                r.createCell(cellIndex++).setCellValue("");//
                r.createCell(cellIndex++).setCellValue("");//
                r.createCell(cellIndex++).setCellValue(it.get("payChannel") + "");
                r.createCell(cellIndex++).setCellValue(it.get("payTime") + "");
                r.createCell(cellIndex++).setCellValue(it.get("payId") + "");
                r.createCell(cellIndex++).setCellValue(it.get("payPrice") + "");
                r.createCell(cellIndex++).setCellValue("人民币");
                r.createCell(cellIndex++).setCellValue("N");
                r.createCell(cellIndex++).setCellValue("N");
                r.createCell(cellIndex++).setCellValue("贝贝网");
                r.createCell(cellIndex++).setCellValue("使用贝贝网盒子打包");
                r.createCell(cellIndex++).setCellValue(it.get("outNumber") + "");
            }
        }
        catch (Exception e)
        {
            log.error("贝贝网订单转笨鸟订单失败！OrderServiceImpl", e);
            if ("".equals(errorMsg))
            {
                errorMsg = "转换失败！";
            }
            workbook = POIUtil.createHSSFWorkbookTemplate(exportTitle);
            Sheet sheet = workbook.getSheetAt(0);
            Row r = sheet.createRow(1);
            r.createCell(0).setCellValue(errorMsg);
        }
        return workbook;
    }
    
    @Override
    public Workbook saveOrderSendGoodsFromBirdexForBeiBei(List<Map<String, Object>> data, String[] exportTitle)
        throws Exception
    {
        Workbook workbook = POIUtil.createHSSFWorkbookTemplate(exportTitle);
        String errorMsg = "";
        try
        {
            Map<String, Object> logisticsMap = new HashMap<>();
            Set<Long> hbNumberSet = new HashSet<>();
            for (Map<String, Object> it : data)
            {
                if (it.get("cell0") == null || "".equals(it.get("cell0")) || it.get("cell1") == null || "".equals(it.get("cell1")))
                {
                    continue;
                }
                String number = it.get("cell0") + "";
                String logisticsNumber = it.get("cell1") + "";
                logisticsMap.put(number, logisticsNumber);
                hbNumberSet.add(Long.valueOf(number));
            }
            
            // 查询订单合并信息
            if (hbNumberSet.size() > 0)
            {
                List<Map<String, Object>> hbRecordList = orderDao.findOutOrderHbRecordByHbNumberList(CommonUtil.setToList(hbNumberSet));
                if (hbRecordList.size() > 0)
                {
                    Sheet sheet = workbook.getSheetAt(0);
                    int rIndex = 1;
                    for (Map<String, Object> it : hbRecordList)
                    {
                        String hbNumber = it.get("hbNumber") + "";
                        String sonNumber = it.get("sonNumber") + "";
                        Object logisticsNumber = logisticsMap.get(hbNumber);
                        if (logisticsNumber != null && !"".equals(logisticsNumber))
                        {
                            int cellIndex = 0;
                            Row r = sheet.createRow(rIndex++);
                            r.createCell(cellIndex++).setCellValue(sonNumber);
                            r.createCell(cellIndex++).setCellValue(logisticsNumber + "");
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error("贝贝网订单发货导出失败！OrderServiceImpl", e);
            if ("".equals(errorMsg))
            {
                errorMsg = "转换失败！";
            }
            workbook = POIUtil.createHSSFWorkbookTemplate(exportTitle);
            Sheet sheet = workbook.getSheetAt(0);
            Row r = sheet.createRow(1);
            r.createCell(0).setCellValue(errorMsg);
        }
        return workbook;
    }
    
    @Override
    public Workbook exportSendGoodsInfoByNumbers(List<Map<String, Object>> data, String[] exportTitle)
        throws Exception
    {
        Workbook workbook = POIUtil.createHSSFWorkbookTemplate(exportTitle);
        String errorMsg = "";
        try
        {
            Set<Long> normalOrderList = new HashSet<>();
            int breakIndex = 0;
            int rowNumber = 1;
            for (Map<String, Object> it : data)
            {
                rowNumber++;
                if (it.get("cell0") == null)
                {
                    // 遇到连续空行的话直接跳出
                    if (breakIndex > 10)
                    {
                        errorMsg = "文件中存在许多空行，请检查。";
                        throw new ServiceException("根据订单编号导出发货文件失败！");
                    }
                    else
                    {
                        breakIndex++;
                        continue;
                    }
                }
                breakIndex = 0;
                
                String _number = (it.get("cell0") + "").trim();
                if (_number.indexOf("GGJHB") > -1 || _number.indexOf("HB") > -1)
                {
                    errorMsg = "文件包括合并订单号。请自行拆分后再操作。";
                    throw new ServiceException("根据订单编号导出发货文件失败！");
                }
                else if (_number.indexOf("GGJ") > -1)
                {
                    normalOrderList.add(Long.valueOf(_number.substring(3)));
                }
                else if (StringUtils.isNumeric(_number))
                {
                    normalOrderList.add(Long.valueOf(_number));
                }
                else
                {
                    errorMsg = "第" + rowNumber + "行数据异常，请检查。";
                    throw new ServiceException("根据订单编号导出发货文件失败！");
                }
            }
        }
        catch (Exception e)
        {
            log.error("根据订单编号导出发货文件失败！OrderServiceImpl", e);
            if ("".equals(errorMsg))
            {
                errorMsg = "转换失败！";
            }
            workbook = POIUtil.createHSSFWorkbookTemplate(exportTitle);
            Sheet sheet = workbook.getSheetAt(0);
            Row r = sheet.createRow(1);
            r.createCell(0).setCellValue(errorMsg);
        }
        return workbook;
    }
    
    Map<String, Map<String, String>> findPayTid(List<Map<String, Object>> data)
        throws Exception
    {
        Map<String, Map<String, String>> payPidMap = new HashMap<>();
        List<Long> orderAliPay = new ArrayList<>();
        List<Long> orderUnionPay = new ArrayList<>();
        List<Long> orderWeixinPay = new ArrayList<>();
        for (Map<String, Object> it : data)
        {
            int payChannel = Integer.parseInt(it.get("payChannel") + "");
            if (payChannel == OrderEnum.PAY_CHANNEL.UNION.getCode())
            {
                orderUnionPay.add(Long.valueOf(it.get("oId") + ""));
            }
            else if (payChannel == OrderEnum.PAY_CHANNEL.ALIPAY.getCode())
            {
                orderAliPay.add(Long.valueOf(it.get("oId") + ""));
            }
            else if (payChannel == OrderEnum.PAY_CHANNEL.WEIXIN.getCode())
            {
                orderWeixinPay.add(Long.valueOf(it.get("oId") + ""));
            }
        }
        return payPidMap;
    }
    
    Map<String, Map<String, String>> findPayTid1(List<OrderDetailInfoForSeller> resultList)
        throws Exception
    {
        Map<String, Map<String, String>> payPidMap = new HashMap<>();
        List<Long> orderAliPay = new ArrayList<>();
        List<Long> orderUnionPay = new ArrayList<>();
        List<Long> orderWeixinPay = new ArrayList<>();
        for (OrderDetailInfoForSeller it : resultList)
        {
            int payChannel = Integer.parseInt(it.getPayChannel());
            if (payChannel == OrderEnum.PAY_CHANNEL.UNION.getCode())
            {
                orderUnionPay.add(Long.valueOf(it.getoId()));
            }
            else if (payChannel == OrderEnum.PAY_CHANNEL.ALIPAY.getCode())
            {
                orderAliPay.add(Long.valueOf(it.getoId()));
            }
            else if (payChannel == OrderEnum.PAY_CHANNEL.WEIXIN.getCode())
            {
                orderWeixinPay.add(Long.valueOf(it.getoId()));
            }
        }
        return payPidMap;
    }
    
    @Override
    public Map<String, Object> findEdbOrderConfirmed(Map<String, Object> para)
        throws Exception
    {
        // 加入注册E店宝商家
        if (para.get("sellerIdList") == null)
        {
            List<Integer> sellerIdList = sellerDao.findEdbSellerIdList();
            if (sellerIdList.size() > 0)
            {
                para.put("sellerIdList", sellerIdList);
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        int total = 0;
        int pushStatus = Integer.valueOf(para.get("pushStatus") + "");
        Integer receiveStatus = para.get("receiveStatus") == null ? null : Integer.valueOf(para.get("receiveStatus") + "");
        if (pushStatus == 0)
        {
            // 全部状态
            para.put("confirmed", "1");
            rows = orderDao.findEdbOrder(para);
            total = orderDao.countEdbOrder(para);
        }
        else if (pushStatus == 1)
        {
            // 推送成功
            rows = orderDao.findPushSuccessEdbOrder(para);
            total = orderDao.countPushSuccessEdbOrder(para);
        }
        else if (pushStatus == 2)
        {
            if (receiveStatus == null)
            {
                // 推送失败
                rows = orderDao.findPushErrorEdbOrder(para);
                total = orderDao.countPushErrorEdbOrder(para);
            }
        }
        else if (pushStatus == 3)
        {
            if (receiveStatus == null)
            {
                // 等待推送
                rows = orderDao.findWaitPushEdbOrder(para);
                total = orderDao.countWaitPushEdbOrder(para);
            }
        }
        else if (pushStatus == 4)
        {
            if (receiveStatus == null)
            {
                // 不推送
                rows = orderDao.findNoPushEdbOrder(para);
                total = orderDao.countNoPushEdbOrder(para);
            }
        }
        
        if (rows.size() > 0)
        {
            List<Integer> orderIdList = new ArrayList<>();
            for (Map<String, Object> map : rows)
            {
                orderIdList.add(Integer.valueOf(map.get("id") + ""));
            }
            
            // 确认记录
            List<Map<String, Object>> confirmRecordList = orderDao.findOrderEdbConfirmByOrderIdList(orderIdList);
            Map<String, Map<String, Object>> confirmRecordMapByOrderId = new HashMap<>();
            for (Map<String, Object> it : confirmRecordList)
            {
                confirmRecordMapByOrderId.put(it.get("orderId") + "", it);
            }
            
            // 推送记录
            List<Map<String, Object>> sendRecordList = orderDao.findOrderEdbSendRecordByOrderIdList(orderIdList);
            Map<String, Map<String, Object>> sendRecordMapByOrderId = new HashMap<>();
            for (Map<String, Object> it : sendRecordList)
            {
                sendRecordMapByOrderId.put(it.get("orderId") + "", it);
            }
            
            for (Map<String, Object> map : rows)
            {
                String orderId = map.get("id") + "";
                Map<String, Object> confirmRecord = confirmRecordMapByOrderId.get(orderId); // 确认记录
                if (confirmRecord == null)
                {
                    continue;
                }
                boolean isPush = Integer.valueOf(confirmRecord.get("isPush") + "") == 1;
                Map<String, Object> sendRecord = sendRecordMapByOrderId.get(orderId);
                if (sendRecord != null)
                {
                    int isPushSuccess = Integer.valueOf(sendRecord.get("isPushSuccess") + "");
                    String errorContent = sendRecord.get("errorContent") + "";
                    map.put("pushTime", DateTimeUtil.timestampObjectToWebString(sendRecord.get("createTime")));
                    map.put("pushStatus", isPushSuccess == 1 ? "推送成功" : "推送失败");
                    map.put("pushError", errorContent);
                    int canUnSend = isPush ? (isPushSuccess == 1 ? 0 : 1) : 0;
                    map.put("canUnSend", canUnSend);
                }
                else
                {
                    map.put("pushTime", "");
                    map.put("pushStatus", isPush ? "等待推送" : "不推送");
                    map.put("pushError", "");
                    map.put("canUnSend", isPush ? 1 : 0);
                }
                map.put("payTime", DateTimeUtil.timestampObjectToWebString(map.get("payTime")));
                map.put("sendStatus", isPush ? "已确认推送" : "已确认不推送");
                map.put("orderStatus", OrderEnum.ORDER_STATUS.getDescByCode(Integer.valueOf(map.get("status") + "")));
            }
        }
        result.put("rows", rows);
        result.put("total", total);
        return result;
    }
    
    @Override
    public Map<String, Object> findEdbOrderUnconfirmed(Map<String, Object> para)
        throws Exception
    {
        Map<String, Object> result = new HashMap<>();
        para.put("status", 2);
        para.put("unconfirmed", "1");
        if (para.get("sellerIdList") == null)
        {
            List<Integer> sellerIdList = sellerDao.findEdbSellerIdList();
            if (sellerIdList.size() > 0)
            {
                para.put("sellerIdList", sellerIdList);
            }
        }
        List<Map<String, Object>> orderList = orderDao.findEdbOrder(para);
        int total = 0;
        if (orderList.size() > 0)
        {
            for (Map<String, Object> map : orderList)
            {
                int canSend = 1;
                int isFreeze = Integer.valueOf(map.get("isFreeze") + "");
                if (isFreeze == 1)
                {
                    canSend = 0;
                }
                map.put("canSend", canSend);
                map.put("payTime", DateTimeUtil.timestampObjectToWebString(map.get("payTime")));
                map.put("freezeStatus", isFreeze == 1 ? "已冻结" : "未冻结");
                map.put("sendStatus", "未确认推送");
                map.put("orderStatus", OrderEnum.ORDER_STATUS.getDescByCode(Integer.valueOf(map.get("status") + "")));
            }
            total = orderDao.countEdbOrder(para);
        }
        result.put("rows", orderList);
        result.put("total", total);
        return result;
    }
    
    @Override
    public Map<String, Object> confirmEdbOrder(List<Integer> idList, int isPush)
        throws Exception
    {
        Map<String, Object> result = new HashMap<>();
        int status = 1;
        int rightNum = 0;
        int wrongNum = 0;
        int freezeNum = 0;
        String msg = "";
        for (int orderId : idList)
        {
            OrderEntity oe = orderDao.findOrderById(orderId);
            if (oe.getIsFreeze() != 1 || isPush == 0)
            {
                Map<String, Object> confirmedEdbRecord = orderDao.findOrderEdbConfirmByOrderId(orderId);
                if (confirmedEdbRecord == null)
                {
                    // 未曾确认
                    if (orderDao.addOrderEdbConfirm(orderId, isPush) == 1)
                    {
                        rightNum++;
                    }
                    else
                    {
                        wrongNum++;
                    }
                }
                else
                {
                    if (orderDao.updateOrderEdbConfirm(orderId, isPush) == 1)
                    {
                        rightNum++;
                    }
                    else
                    {
                        wrongNum++;
                    }
                }
            }
            else
            {
                freezeNum++;
            }
        }
        
        if (isPush == 1)
        {
            if (rightNum > 0)
            {
                msg += "成功确认了" + rightNum + "个订单。";
            }
            if (wrongNum > 0)
            {
                msg += "失败了" + wrongNum + "个订单。";
            }
            if (freezeNum > 0)
            {
                msg += "冻结订单无法确认" + freezeNum + "个。";
            }
        }
        else
        {
            if (rightNum > 0)
            {
                msg += "成功取消确认了" + rightNum + "个订单。";
            }
            if (wrongNum > 0)
            {
                msg += "失败了" + wrongNum + "个订单。";
            }
            if (freezeNum > 0)
            {
                msg += "冻结订单无法取消" + freezeNum + "个。";
            }
        }
        status = rightNum > 0 ? 1 : 0;
        result.put("dialog", (rightNum == idList.size()) ? 0 : 1);
        result.put("msg", msg);
        result.put("status", status);
        result.put("rightNum", rightNum);
        result.put("wrongNum", wrongNum);
        return result;
    }
    
    @Override
    public Map<String, Object> getConfirmedCountInfo()
        throws Exception
    {
        Map<String, Object> result = new HashMap<>();
        result.put("errorNums", orderDao.countPushErrorEdbOrder());
        return result;
    }
    
    @Override
    public Map<String, Object> saveCheckOrder(int orderId, int checkStatus, String remark)
        throws Exception
    {
        if ("".equals(remark))
        {
            Subject currentUser = SecurityUtils.getSubject();
            if (currentUser != null)
            {
                remark = currentUser.getPrincipal() + "将订单ID：" + orderId + "审核" + (checkStatus == 2 ? "通过" : "不通过");
            }
        }
        
        String checkTime = DateTimeUtil.now();
        Map<String, Object> updateOrderPara = new HashMap<>();
        updateOrderPara.put("id", orderId);
        updateOrderPara.put("checkStatus", checkStatus);
        if (OrderEnum.ORDER_CHECK_STATUS.CHECK_PASS.getCode() == checkStatus)
        {
            updateOrderPara.put("expireTime", getOrderExpireTime(orderId, checkTime));
        }
        int status = 0;
        if (orderDao.updateOrder(updateOrderPara) == 1)
        {
            status = 1;
            Map<String, Object> savePara = new HashMap<>();
            savePara.put("orderId", orderId);
            savePara.put("checkTime", checkTime);
            savePara.put("remark", remark);
            orderDao.saveOrderCheck(savePara);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("status", status);
        result.put("msg", status == 1 ? "保存成功" : "保存失败");
        return result;
    }
    
    private String getOrderExpireTime(int orderId, String checkTime)
    {
        String expireTime = null;
        try
        {
            OrderEntity order = orderDao.findOrderById(orderId);
            DateTime beginTime = new DateTime(CommonUtil.string2Date(checkTime, "yyyy-MM-dd HH:mm:ss").getTime());
            DateTime endTime = null;
            SellerEntity seller = sellerDao.findSellerById(order.getSellerId());
            if (seller != null)
            {
                
                // 当天15点前订单当天打包并提供物流单号，24小时内有物流信息，超时时间为当天24点
                if (seller.getSendTimeType() == SellerEnum.SellerSendTimeTypeEnum.IN_15_HOUR.getCode())
                {
                    int plusDay = 0;
                    int dayOfHour = beginTime.getHourOfDay();
                    int dayOfWeek = beginTime.getDayOfWeek();
                    
                    if (seller.getIsSendWeekend() == SellerEnum.WeekendSendTypeEnum.SEND_ON_SUNDAY.getCode())
                    {
                        // 周六不发货
                        if (dayOfWeek == 5)
                        {
                            if (dayOfHour >= 15)
                            {
                                plusDay += 2;
                            }
                        }
                        else if (dayOfWeek == 6)
                        {
                            plusDay += 1;
                        }
                        else
                        {
                            if (dayOfHour >= 15)
                            {
                                plusDay += 1;
                            }
                        }
                    }
                    else if (seller.getIsSendWeekend() == SellerEnum.WeekendSendTypeEnum.SEND_ON_SATURDAY.getCode())
                    {
                        // 周天不发货
                        if (dayOfWeek == 6)
                        {
                            if (dayOfHour >= 15)
                            {
                                plusDay += 2;
                            }
                        }
                        else if (dayOfWeek == 7)
                        {
                            plusDay += 1;
                        }
                        else
                        {
                            if (dayOfHour >= 15)
                            {
                                plusDay += 1;
                            }
                        }
                    }
                    else if (seller.getIsSendWeekend() == SellerEnum.WeekendSendTypeEnum.NOT_SEND_ON_WEEKEND.getCode())
                    {
                        // 周末不发货
                        if (dayOfWeek == 5)
                        {
                            if (dayOfHour >= 15)
                            {
                                plusDay += 3;
                            }
                        }
                        else if (dayOfWeek == 6)
                        {
                            plusDay += 2;
                        }
                        else if (dayOfWeek == 7)
                        {
                            plusDay += 1;
                        }
                        else
                        {
                            if (dayOfHour >= 15)
                            {
                                plusDay += 1;
                            }
                        }
                    }
                    else
                    {
                        if (dayOfHour >= 15)
                        {
                            plusDay += 1;
                        }
                    }
                    endTime = beginTime.withTimeAtStartOfDay().plusDays(plusDay).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59);
                }
                else
                {
                    int plusHour = 0;
                    if (seller.getSendTimeType() == SellerEnum.SellerSendTimeTypeEnum.IN_24_HOUR.getCode())
                    {
                        if (order.getType() == OrderEnum.ORDER_TYPE.GEGEJIA.getCode())
                        {
                            plusHour = 24;
                        }
                        else if (order.getType() == OrderEnum.ORDER_TYPE.GEGETUAN.getCode())
                        {
                            plusHour = 48;
                            
                        }
                    }
                    else if (seller.getSendTimeType() == SellerEnum.SellerSendTimeTypeEnum.IN_48_HOUR.getCode())
                    {
                        plusHour = 48;
                    }
                    else if (seller.getSendTimeType() == SellerEnum.SellerSendTimeTypeEnum.IN_72_HOUR.getCode())
                    {
                        plusHour = 72;
                    }
                    endTime = beginTime.plusHours(plusHour);
                    while (!beginTime.isAfter(endTime))
                    {
                        if (beginTime.getDayOfWeek() == 6)
                        {
                            if (seller.getIsSendWeekend() == SellerEnum.WeekendSendTypeEnum.SEND_ON_SUNDAY.getCode()
                                || seller.getIsSendWeekend() == SellerEnum.WeekendSendTypeEnum.NOT_SEND_ON_WEEKEND.getCode())
                            {
                                endTime = endTime.plusHours(24);
                            }
                        }
                        if (beginTime.getDayOfWeek() == 7)
                        {
                            if (seller.getIsSendWeekend() == SellerEnum.WeekendSendTypeEnum.SEND_ON_SATURDAY.getCode()
                                || seller.getIsSendWeekend() == SellerEnum.WeekendSendTypeEnum.NOT_SEND_ON_WEEKEND.getCode())
                            {
                                endTime = endTime.plusHours(24);
                            }
                        }
                        beginTime = beginTime.plusDays(1);
                    }
                }
            }
            if (endTime != null)
            {
                expireTime = endTime.toString("yyyy-MM-dd HH:mm:ss");
            }
            else
            {
                expireTime = "0000-00-00 00:00:00";
            }
        }
        catch (Exception e)
        {
            log.error("审单更新订单发货超时时间出错,orderId=" + orderId, e);
            expireTime = "0000-00-00 00:00:00";
        }
        return expireTime;
        
    }
    
    @Override
    public void updateEdbIsPushStatus()
        throws Exception
    {
        Map<String, Object> para = new HashMap<>();
        // 加入注册E店宝商家
        List<Integer> sellerIdList = sellerDao.findEdbSellerIdList();
        if (sellerIdList.size() > 0)
        {
            para.put("sellerIdList", sellerIdList);
            for (Map<String, Object> m : orderDao.findWaitPushEdbOrder(para))
            {
                int id = Integer.valueOf(m.get("id") + "");
                int status = Integer.valueOf(m.get("status") + "");
                if (status != 2)
                {
                    if (orderDao.updateOrderEdbConfirm(id, 0) == 1)
                    {
                        log.info("成功将订单: " + id + "更新为不推送EDB");
                    }
                }
            }
        }
    }
    
    @Override
    public String exportForSellerUnSettleOrders(Map<String, Object> para)
        throws Exception
    {
        Map<String, List<OrderDetailInfoForSeller>> resultMap = findSellerUnSettleOrders(para);
        String nowStr = DateTime.now().toString("yyyy-MM-dd_HH_mm_ss");
        File fileDir = new File(UtilProperties.getInstance("config.properties").getProperties("order_zip_download_dir"));
        File newDir = new File(fileDir, nowStr + "_" + new Random().nextInt(10000) + "unsettle");
        newDir.mkdir();
        // 导出参数选择
        for (Map.Entry<String, List<OrderDetailInfoForSeller>> entry : resultMap.entrySet())
        {
            writeToExcel(newDir, entry.getKey() + ".xls", entry.getValue());
        }
        return newDir.getAbsolutePath();
    }
    
    @Override
    public Map<String, List<OrderDetailInfoForSeller>> findSellerUnSettleOrders(Map<String, Object> para)
        throws Exception
    {
        Map<String, List<OrderDetailInfoForSeller>> returnMap = new HashMap<>();
        List<OrderDetailInfoForSeller> reList = orderDao.findSellerUnSettleOrders(para);
        if (CollectionUtils.isNotEmpty(reList))
        {
            for (OrderDetailInfoForSeller curr : reList)
            {
                String cuKey = curr.getSellerName() + "_" + curr.getSendAddress() + "_" + curr.getWarehouse() + "_" + curr.getsId();
                List<OrderDetailInfoForSeller> newList = returnMap.get(cuKey);
                if (newList == null)
                {
                    newList = new ArrayList<>();
                    returnMap.put(cuKey, newList);
                }
                curr.setoStatusDescripton(OrderEnum.ORDER_STATUS.getDescByCode(curr.getoStatus()));
                //curr.setProvince(AreaCache.getInstance().getProvinceName(curr.getProvince()));
                // 添加心动慈露判断 28==appChannel
                int appChannel = curr.getAppChannel();
                boolean isQqbs = 28 == appChannel;
                curr.setOrderTypeDescripton(isQqbs ? CommonEnum.OrderAppChannelEnum.getDescriptionByOrdinal(appChannel) : OrderEnum.ORDER_TYPE.getDescByCode(curr.getOrderType()));
                // todo 增强 根据商品类型 判断心动慈露

                newList.add(curr);
            }
        }
        return returnMap;
    }
    
    private void writeToExcel(File dir, String fileName, List<OrderDetailInfoForSeller> resultList)
        throws Exception
    {
        OutputStream fOut = null;
        Workbook workbook = null;
        try
        {
            // "供货总价", "配送省份", "运费"为空
            String[] arr = {"下单时间", "付款时间", "发货时间", "订单编号 ", "订单状态", "商品编码", "商品名称", "数量", "销售渠道", "销售单价", "供货单价", "供货总价", "配送省份", "运费", "物流公司", "物流单号"};
            workbook = POIUtil.createHSSFWorkbookTemplate(arr);
            Sheet sheet = workbook.getSheetAt(0);
            // 设置column的width
            for (int i = 0; i < arr.length; i++)
            {
                sheet.setColumnWidth(i, 5500);
            }
            for (int i = 0; i < resultList.size(); i++)
            {
                
                int cellIndex = 0;
                OrderDetailInfoForSeller curr = resultList.get(i);
                Row r = sheet.createRow(i + 1);
                String productCode = curr.getProductCode();
                Integer productCount = Integer.valueOf(curr.getProductCount() + "");
                int index = StringUtils.lastIndexOf(productCode, "%");
                Integer _num = 1;
                boolean isdiv = false;
                if ((index > -1) && (index < productCode.length() - 1))
                {
                    String num = productCode.substring(index + 1);
                    if (StringUtils.isNumeric(num))
                    {
                        productCode = productCode.substring(0, index);
                        productCount = productCount * Integer.valueOf(num);
                        isdiv = true;
                        _num = Integer.parseInt(num);
                    }
                }
                r.createCell(cellIndex++).setCellValue(curr.getoCreateTime() + "");
                r.createCell(cellIndex++).setCellValue(curr.getoPayTime() + "");
                r.createCell(cellIndex++).setCellValue(curr.getoSendTime() + "");
                r.createCell(cellIndex++).setCellValue(curr.getoNumber() + "");
                r.createCell(cellIndex++).setCellValue(curr.getoStatusDescripton() + "");
                r.createCell(cellIndex++).setCellValue(productCode + "");
                r.createCell(cellIndex++).setCellValue(curr.getProductName() + "");
                r.createCell(cellIndex++).setCellValue(productCount + "");
                r.createCell(cellIndex++).setCellValue(curr.getOrderTypeDescripton());
                r.createCell(cellIndex++).setCellValue(new DecimalFormat("0.00").format(isdiv ? curr.getSalesPrice() / _num : curr.getSalesPrice()) + "");
                r.createCell(cellIndex++).setCellValue("");
                r.createCell(cellIndex++).setCellValue("");
                r.createCell(cellIndex++).setCellValue(curr.getProvince() + "");
                r.createCell(cellIndex++).setCellValue("");
                r.createCell(cellIndex++).setCellValue(curr.getOlogChannel() + "");
                r.createCell(cellIndex++).setCellValue(curr.getOlogNumber() + "");
            }
            File file = new File(dir, fileName);
            file.createNewFile();
            fOut = new FileOutputStream(file);
            workbook.write(fOut);
            fOut.flush();
            resultList.clear();
        }
        catch (Exception e)
        {
            log.error("导出结算excel出错", e);
            throw new Exception(e);
        }
        finally
        {
            if (fOut != null)
            {
                try
                {
                    fOut.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            if (workbook != null)
            {
                try
                {
                    workbook.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    
}
