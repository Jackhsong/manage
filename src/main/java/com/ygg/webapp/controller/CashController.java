package com.ygg.webapp.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;

import com.ygg.webapp.annotation.ControllerLog;
import com.ygg.webapp.service.QqbsCashService;

@Controller
@RequestMapping("/qqbsCash")
public class CashController {
	
	Logger logger = Logger.getLogger(CashController.class);
	
	@Resource(name="qqbsCashService")
	private QqbsCashService qqbsCashService;

	@RequestMapping("/list")
	public ModelAndView list() {
		ModelAndView mv = new ModelAndView("cash/list");
		return mv;
	}
	
	/**
	 * 列表查询
	 * @param status
	 * @param isDisplay
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/listInfo")
	@ResponseBody
	public Object listInfo(String name,
			@RequestParam(value = "accountId", defaultValue = "-1", required = false) int accountId,
			@RequestParam(value = "page", required = false, defaultValue = "1") int page,
			@RequestParam(value = "rows", required = false, defaultValue = "10") int rows) {
		try {
			page = page == 0 ? 1 : page;
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("accountId", accountId);
			if(StringUtils.isNotBlank(name)){
				param.put("name", "%" + name + "%");
			}
			param.put("start", rows * (page - 1));
			param.put("size", rows);
			return JSON.toJSONString(qqbsCashService.findListInfo(param));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("status", 0);
			resultMap.put("msg", e.getMessage());
			return resultMap;
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/refuse", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    @ControllerLog(description = "拒绝")
    public String refuse(HttpServletRequest request, @RequestParam(value = "id", required = true) int id,
    		 @RequestParam(value = "type", required = true) int type)
    {
        Map resultMap = new HashMap();
        try{
            String fla = qqbsCashService.updateLog(id,type);
            if(StringUtils.isNotBlank(fla)){
                resultMap.put("status", 0);
                resultMap.put("msg", "ID="+id+" 拒绝失败 原因:"+fla);
            }else{
                resultMap.put("status", 1);
                resultMap.put("msg", "拒绝成功");
            }
        }catch(Exception e){
        	logger.error("拒绝失败",e);
        	resultMap.put("status", 0);
            resultMap.put("msg", "拒绝失败");
        }
        return JSON.toJSONString(resultMap);
    }
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/accept", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    @ControllerLog(description = "打款")
    public String accept(HttpServletRequest request, @RequestParam(value = "id", required = true) int id,
    		 @RequestParam(value = "type", required = true) int type)
    {
        Map resultMap = new HashMap();
        try{
        	String fla = qqbsCashService.updateLog(id,type);
        	if(StringUtils.isNotBlank(fla)){
        		resultMap.put("status", 0);
                resultMap.put("msg", "ID="+id+" 打款失败 原因:"+fla);
        	}else{
        		resultMap.put("status", 1);
                resultMap.put("msg", "打款成功");
        	}
        }catch(Exception e){
        	logger.error("打款失败",e);
        	resultMap.put("status", 0);
            resultMap.put("msg", "打款失败");
        }
        return JSON.toJSONString(resultMap);
    }

}
