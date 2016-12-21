package com.ygg.webapp.service.Impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.ygg.webapp.dao.QqbsCashDao;
import com.ygg.webapp.entity.RewardEntity;
import com.ygg.webapp.service.QqbsCashService;
import com.ygg.webapp.util.MD5Util;
import com.ygg.webapp.util.RequestHandler;


@Service("qqbsCashService")
public class QqbsCashServiceImpl implements QqbsCashService {

    @Resource(name="qqbsCashDao")
    private QqbsCashDao qqbsCashDao;

    @Override
    public Map<String, Object> findListInfo(Map<String, Object> param)
        throws Exception
    {
        Map<String, Object> result = new HashMap<String, Object>();
        List<Map<String, Object>> infoList = qqbsCashDao.findListInfo(param);
        result.put("rows", infoList);
        result.put("total", qqbsCashDao.getCountByParam(param));
        return result;
    }
    
    
    /**
     * @param accountId
     * @param id
     * @param type 1打款 2拒绝
     * @throws Exception 
     * @see com.ygg.admin.service.qqbscash.QqbsCashService#updateLog(int, int, int)
     */
	public String updateLog(int id , int type) throws Exception{
		Map<String, Object> info = qqbsCashDao.getInfoByLogId(id);
		int accountId = Integer.valueOf(info.get("accountId")+"");
		float withdrawals = Float.valueOf(info.get("withdrawals")+"");
		String openId = info.get("openId")+"";
		int status = Integer.valueOf(info.get("status")+"");
		if(status == 1){
		    Map<String, Object> param = new HashMap<String, Object>();
	        param.put("id", id);
	        if(type == 1){
	            //打款,只是一个操作，用来更改打款状态的
	            RequestHandler reqHandler = new RequestHandler(null, null);
	            String nonce_str = MD5Util.getNonceStr();
	            
	            reqHandler.setParameter("mch_appid","wxa022d433d4a30207"); // 公众账号ID
	            reqHandler.setParameter("mchid","1348518501"); // 商户号
	            //随机字符串
	            reqHandler.setParameter("nonce_str", nonce_str); 
	            // 商家订单号
	            reqHandler.setParameter("partner_trade_no", MD5Util.getNonceStr());
	            //用户openid
	            reqHandler.setParameter("openid", openId);
	            //校验用户姓名选项
	            reqHandler.setParameter("check_name", "NO_CHECK");
	            //金额
	            reqHandler.setParameter("amount", (int)(withdrawals * 100) + "");
	            //企业付款描述信息
	            reqHandler.setParameter("desc", "行动派提现");
	            //Ip地址
	            reqHandler.setParameter("spbill_create_ip", "192.168.0.1");
	            
	             //key
	            reqHandler.setKey("0806a6b68fa695e33895c5dd924338ed");
	            
	            // 生成签名
	            /* String sign = reqHandler.createSign(reqHandler.getParameters());
	            reqHandler.setParameter("sign", sign);
	            String requestUrl = reqHandler.getRequestURL();
	            String cashUrl = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers";
	            String mchid = "1348518501";
	            String certPath = "C:\\Users\\apiclient_cert.p12";
	            JSONObject responseParams = CommonHttpClient.sendXmlRefundHTTP(cashUrl, mchid, certPath, requestUrl);	            
	            if ("SUCCESS".equals(responseParams.getString("return_code")) && "SUCCESS".equals(responseParams.getString("result_code")))
	            {
	                1.提现成功*/
	                param.put("status", 2);
	                RewardEntity hre = qqbsCashDao.getByAccountId(accountId);
	                if(hre != null){
	                    float newCash = hre.getAlreadyCash()+withdrawals;
	                    hre.setAlreadyCash(newCash);
	                    qqbsCashDao.updateHqbsReward(hre);
	                }
	           /* else if ("SUCCESS".equals(responseParams.getString("return_code")) && "FAIL".equals(responseParams.getString("result_code")))
	            {
	              logger.info(responseParams);
	              return responseParams.getString("err_code_des") + "";
	            }*/
	        }else if(type == 2){
	            //拒绝,提现失败
	            param.put("status", 3);
	            //修改奖励表,用户奖励信息
	            RewardEntity hre = qqbsCashDao.getByAccountId(accountId);
	            if(hre != null){
	                float newCash = hre.getWithdrawCash()+withdrawals;
	                hre.setWithdrawCash(newCash);
	                qqbsCashDao.updateHqbsReward(hre);
	            }
	        }
	        qqbsCashDao.updateLog(param);
		}else{
		    return "该记录已经打过款或已拒绝过,不再处理";
		}
    	return null;
    }
}
