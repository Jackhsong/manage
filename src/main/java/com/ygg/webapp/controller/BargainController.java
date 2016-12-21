package com.ygg.webapp.controller;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.ygg.webapp.service.BargainService;

//@Controller
//@RequestMapping("/backBargain")
public class BargainController {

	Logger logger = Logger.getLogger(BargainController.class);
	
	@Resource(name="bargainService")
    private BargainService bargainService;
	
	/*
	 * 跳转砍价商品页面
	 */
	@RequestMapping("/toBargainList")
	public ModelAndView toBargainList(){
		ModelAndView mv=new ModelAndView();
		mv.setViewName("/bargain/bargainList");
		
		return mv;
	}
	
	
	@RequestMapping("/list")
	@ResponseBody
	public String list(
		@RequestParam(value = "page", required = false, defaultValue = "1") int page,
		@RequestParam(value = "rows", required = false, defaultValue = "10") int rows, 
		@RequestParam(value = "id", required = false, defaultValue = "0") Integer id, 
        @RequestParam(value = "productId", required = false, defaultValue = "0") Integer productId, 
        @RequestParam(value = "name", required = false, defaultValue = "") String name, 
        @RequestParam(value = "startTime", required = false, defaultValue = "") String startTime, 
        @RequestParam(value = "endTime", required = false, defaultValue = "") String endTime,
        @RequestParam(value = "cutType", required = false, defaultValue = "0") Integer cutType, 
        @RequestParam(value = "cutStatus", required = false, defaultValue = "0") Integer cutStatus
		) throws Exception{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Map<String, Object> para = new HashMap<String, Object>();
	    if (id > 0)
        {
            para.put("id", id);
        }    
        if (!"".equals(name))
        {
            para.put("name", name);
        }
        if (!"".equals(startTime))
        {
            para.put("startTime", sdf.parse(startTime));
        }
        if (!"".equals(endTime))
        {
            para.put("endTime", sdf.parse(endTime));
        }
        if (productId > 0)
        {
            para.put("productId", productId);
        }
        if (cutType > 0)
        {
            para.put("type", cutType);
        }
        if (cutStatus== 1)
        {   
            para.put("status", cutStatus);
        }else if(cutStatus==2){
        	para.put("status", 0);
        }

		List<Map<String,Object>> list=bargainService.selectListBargainByPara(para);	
		for(Map<String,Object> map:list){
			Long sellerId=(Long) map.get("sellerId");
			Long brandId=(Long) map.get("brandId");
			if(sellerId==1){
				map.put("sellerName", "杭州慈露科技有限公司");
			}if(brandId==1){
				map.put("brandName", "慈露");
			}
			map.put("startTime", sdf.format(map.get("startTime")));
			map.put("endTime", sdf.format(map.get("endTime")));
		}
		Map<String, Object> result=new HashMap<String, Object>();
		if (page==0) {
			page=1;
		}
        int start = (page - 1) * rows;
		int end =( page *rows >= list.size()) ? list.size(): page *rows;
		result.put("total", list.size());
		result.put("rows", list.subList(start, end) );
		
		return JSON.toJSONString(result);	
	}
	
	
	@RequestMapping("/updateStatus")
	public ModelAndView updateStatus(
			@RequestParam(value = "updateId", required = false, defaultValue = "0") int id,
			@RequestParam(value = "updateType", required = false, defaultValue = "0") int updateType
		){
		ModelAndView mv=new ModelAndView();
		mv.setViewName("redirect:/backBargain/toBargainList");
		Map<String, Object> para = new HashMap<String, Object>();
		if(id > 0){
			para.put("id", id);
		}
		if(updateType==1){
			para.put("status", 1);
		}else if(updateType==2){
			para.put("status", 0);
		}
		int result=bargainService.updateStatusById(para);
		if(result!=1){
			logger.error("----------更改活动状态失败-----------");
		}
		return mv;		
	}
	
	
	@RequestMapping("/saveEasyUpdate")
	public ModelAndView saveEasyUpdate(
			@RequestParam(value = "updatehigh", required = false, defaultValue = "0") int highDiscount,
			@RequestParam(value = "updatelow", required = false, defaultValue = "0") int lowDiscount,
			@RequestParam(value = "updatelowPrice", required = false, defaultValue = "0") int lowPrice,
			@RequestParam(value = "updatetimes", required = false, defaultValue = "0") int times,
			@RequestParam(value = "updateleftcount", required = false, defaultValue = "0") int leftCount,
			@RequestParam(value = "updatetotalcount", required = false, defaultValue = "0") int stock,
			@RequestParam(value = "easyupdateId", required = false, defaultValue = "0") int id,
			@RequestParam(value = "easyupdateProductId", required = false, defaultValue = "0") int productId
		){
		ModelAndView mv=new ModelAndView();
		mv.setViewName("redirect:/backBargain/toBargainList");		
		Map<String, Object> para = new HashMap<String, Object>();
		Map<String, Object> param = new HashMap<String, Object>();
		if(highDiscount > 0){
			para.put("highDiscount", highDiscount);
		}
		if(lowDiscount > 0){
			para.put("lowDiscount", lowDiscount);
		}
		if(lowPrice > 0){
			para.put("lowPrice", lowPrice);
		}
		if(times> 0){
			para.put("times", times);
		}
		if(id > 0){
			para.put("id", id);
		}			
		int result1=bargainService.updateStatusById(para);
		if(result1!=1){
			logger.error("----------更改砍价折扣信息失败-----------");
		}
		if(leftCount > 0){
			param.put("leftCount", leftCount);
		}
		if(stock > 0){
			param.put("stock", stock);
		}
		if(productId > 0){
			param.put("productId", productId);
		}
		int result2=bargainService.updateActivityBargain(param);
		if(result2!=1){
			logger.error("----------更改砍价商品库存信息失败-----------");
		}
		return mv;		
	}
	
	
	@RequestMapping("/updatedetail")
	public ModelAndView updatedetail(Integer id){
		ModelAndView mv=new ModelAndView();
		mv.setViewName("/bargain/bargainUpdate");
		Map<String, Object> para = new HashMap<String, Object>();
	    if (id > 0)
        {
            para.put("id", id);
        }	    
	    List<Map<String,Object>> list=bargainService.selectListBargainByPara(para);
	    Map<String,Object> map=list.get(0);
	    int type=(int) map.get("type");
	    int status=(int) map.get("status");
	    if(type==1){
	    	map.put("typeName", "一元砍");
	    }else{
	    	map.put("typeName", "七刀砍");
	    }
	    if(status==1){
	    	map.put("statusName", "进行中");
	    }else{
	    	map.put("statusName","已结束");
	    }
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    map.put("startTime", sdf.format(map.get("startTime")));
		map.put("endTime", sdf.format(map.get("endTime")));
	    mv.addObject("data",map);    
		return mv;
	}
	
	
	@RequestMapping("/saveupdate")
	public ModelAndView saveupdate(
			@RequestParam(value = "updateorsave", required = false, defaultValue = "0") Integer updateorsave,
			@RequestParam(value = "id", required = false, defaultValue = "0") Integer id, 
	        @RequestParam(value = "productId", required = false, defaultValue = "0") Integer productId,
	        @RequestParam(value = "cutType", required = false, defaultValue = "0") Integer type,
	        @RequestParam(value = "highDiscount", required = false, defaultValue = "0") Float highDiscount, 
	        @RequestParam(value = "lowDiscount", required = false, defaultValue = "0") Float lowDiscount, 
	        @RequestParam(value = "lowPrice", required = false, defaultValue = "0") Float lowPrice,      
	        @RequestParam(value = "times", required = false, defaultValue = "0") Integer times,
	        @RequestParam(value = "cutstatus", required = false, defaultValue = "0") Integer cutstatus,
	        @RequestParam(value = "name", required = false, defaultValue = "") String name, 
	        @RequestParam(value = "startTime", required = false, defaultValue = "") String startTime, 
	        @RequestParam(value = "endTime", required = false, defaultValue = "") String endTime, 
	        @RequestParam(value = "sellingPoint", required = false, defaultValue = "0") Integer sellingPoint, 
	        @RequestParam(value = "desc", required = false, defaultValue = "") String desc,
	        @RequestParam(value = "marketPrice", required = false, defaultValue = "0") Float marketPrice, 
	        @RequestParam(value = "salesPrice", required = false, defaultValue = "0") Float salesPrice, 
	        @RequestParam(value = "netVolume", required = false, defaultValue = "") String netVolume, 
	        @RequestParam(value = "placeOfOrigin", required = false, defaultValue = "") String placeOfOrigin, 
	        @RequestParam(value = "storageMethod", required = false, defaultValue = "") String storageMethod, 
	        @RequestParam(value = "peopleFor", required = false, defaultValue = "") String peopleFor,
	        @RequestParam(value = "foodMethod", required = false, defaultValue = "") String foodMethod, 
	        @RequestParam(value = "useMethod", required = false, defaultValue = "") String useMethod, 
	        @RequestParam(value = "durabilityPeriod", required = false, defaultValue = "") String durabilityPeriod, 
	        @RequestParam(value = "tip", required = false, defaultValue = "") String tip, 
	        @RequestParam(value = "remark", required = false, defaultValue = "") String remark, 
	        @RequestParam(value = "description", required = false, defaultValue = "") String description,
	        @RequestParam(value = "title", required = false, defaultValue = "") String title, 
	        @RequestParam(value = "leftCount", required = false, defaultValue = "0") Integer leftCount, 
	        @RequestParam(value = "stock", required = false, defaultValue = "0") Integer stock, 
	        @RequestParam(value = "image1", required = false, defaultValue = "") String image1, 
	        @RequestParam(value = "image2", required = false, defaultValue = "") String image2, 
	        @RequestParam(value = "image3", required = false, defaultValue = "") String image3,
	        @RequestParam(value = "image4", required = false, defaultValue = "") String image4, 
	        @RequestParam(value = "image5", required = false, defaultValue = "") String image5, 
	        @RequestParam(value = "pcDetail", required = false, defaultValue = "") String pcDetail
			) throws Exception{
		
		ModelAndView mv=new ModelAndView();
		mv.setViewName("redirect:/backBargain/toBargainList");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//product表格的更改信息
		Map<String, Object> para1 = new HashMap<String, Object>();
		//product_bargain表格的更改信息
		Map<String, Object> para2 = new HashMap<String, Object>();
		//activity_bargain表格的更改信息
		Map<String, Object> para3 = new HashMap<String, Object>();

	    if (productId > 0){para1.put("id", productId);}
	    if (!"".equals(name)){para1.put("name", name);}
	    if (!"".equals(startTime)){para1.put("startTime", sdf.parse(startTime));}
	    if (!"".equals(endTime)){para1.put("endTime", sdf.parse(endTime));}
	    if (sellingPoint > 0){para1.put("sellingPoint", sellingPoint);}
	    if (!"".equals(desc)){para1.put("desc", desc);}
	    if (marketPrice > 0){para1.put("marketPrice", marketPrice);}
	    if (salesPrice > 0){para1.put("salesPrice", salesPrice);}
	    if (!"".equals(netVolume)){para1.put("netVolume", netVolume);}
	    if (!"".equals(placeOfOrigin)){para1.put("placeOfOrigin", placeOfOrigin);}
	    if (!"".equals(storageMethod)){para1.put("storageMethod", storageMethod);}
	    if (!"".equals(peopleFor)){para1.put("peopleFor", peopleFor);}
	    if (!"".equals(foodMethod)){para1.put("foodMethod", foodMethod);}
	    if (!"".equals(useMethod)){para1.put("useMethod", useMethod);}
	    if (!"".equals(durabilityPeriod)){para1.put("durabilityPeriod", durabilityPeriod);}
	    if (!"".equals(tip)){para1.put("tip", tip);}
	    if (!"".equals(remark)){para1.put("remark", remark);}
	    if (!"".equals(image1)){para1.put("image1", image1);}
	    if (!"".equals(image2)){para1.put("image2", image2);}
	    if (!"".equals(image3)){para1.put("image3", image3);}
	    if (!"".equals(image4)){para1.put("image4", image4);}
	    if (!"".equals(image5)){para1.put("image5", image5);}
        if (!"".equals(pcDetail)){para1.put("pcDetail", pcDetail);}
	    		
	    if (id > 0){para2.put("id", id);} 
	    if (productId > 0){para2.put("productId", productId);} 	    
	    if (highDiscount > 0){para2.put("highDiscount", highDiscount);}
	    if (lowDiscount > 0){para2.put("lowDiscount", lowDiscount);}
	    if (lowPrice > 0){para2.put("lowPrice", lowPrice);}
	    if (times > 0){para2.put("times", times);}
	    if(updateorsave==1){
	    	if (type > 0){para2.put("type", type);}
	    	if (cutstatus > 0){
		    	if(cutstatus==1){
		    		para2.put("status", 1);
		    	}else{
		    		para2.put("status", 0);
		    	}
		    }
	    }
      	    
	    if (productId > 0){para3.put("productId", productId);}
	    if (!"".equals(description)){para3.put("description", description);}
	    if (!"".equals(title)){para3.put("title", title);}
	    if (leftCount > 0){para3.put("leftCount", leftCount);}
	    if (stock > 0){para3.put("stock", stock);}
	    //增加新商品
	    if(updateorsave==1){
	    	int result1=bargainService.insertProduct(para1);
	    	if(result1==1){
	    		int productNewId=bargainService.selectTheLastId();
	    		if (productNewId > 0){para2.put("productId", productNewId);}
		    	if (productNewId > 0){para3.put("productId", productNewId);}
		    	int result2=bargainService.insertProductBargain(para2);	    	
		    	int result3=bargainService.insertActivityBargain(para3);
			    if(result1!=1||result2!=1||result3!=1){
			    	mv.setViewName("redirect:/backBargain/toadd");
			    	logger.error("-----------更改砍价商品信息失败--------------");
			    }
	    	}	    	
	    }
	    //修改商品信息
	    if(updateorsave==2){
		    int result1=bargainService.updateProductById(para1);
		    int result2=bargainService.updateStatusById(para2);
		    int result3=bargainService.updateActivityBargain(para3);
		    if(result1!=1||result2!=1||result3!=1){
		    	mv.setViewName("redirect:/backBargain/updatedetail?id="+id);
		    	logger.error("-----------更改砍价商品信息失败--------------");
		    }
	    }
	
		return mv;
	}
	
	
	@RequestMapping("/toadd")
	public ModelAndView toadd(){
		ModelAndView mv=new ModelAndView();
		mv.setViewName("/bargain/bargainAdd");
		
		return mv;
	}
	
	
	@RequestMapping("/delete")
	public ModelAndView delete(Integer productId){
		ModelAndView mv=new ModelAndView();
		mv.setViewName("redirect:/backBargain/toBargainList");
		//删除的时候要确保三个都能删除，不能前面表格删除了，后面表格没有删除，暂时没设置
		if(productId>0){
			int result1=bargainService.deleteProductById(productId);
			int result2=bargainService.deleteProductBargainById(productId);
			int result3=bargainService.deleteActivityByProductId(productId);
			if(result1!=1||result2!=1||result3!=1){
		    	logger.error("-----------删除砍价商品信息失败--------------");
		    }
		}
		return mv;
	}
	
	
}











