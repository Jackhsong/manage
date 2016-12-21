package com.ygg.webapp.controller;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Splitter;
import com.ygg.webapp.service.OSSImgYunService;

/**
 *上传照片到OSS管理
 */
@Controller
@RequestMapping("/pic")
public class PictureController {
	
	@Resource(name="ossImgYunService")
	private OSSImgYunService oSSImgYunService;

	Logger log = Logger.getLogger(PictureController.class);
	
	//上传图片管理
    @RequestMapping(value = "/fileUpLoad", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String fileUpLoad(
        @RequestParam("picFile") MultipartFile file, HttpServletRequest request,//
        @RequestParam(value = "limitSize", required = false, defaultValue = "1") int limitSize,
        @RequestParam(value = "needWidth", required = false, defaultValue = "0") int width,//限制宽度
        @RequestParam(value = "needHeight", required = false, defaultValue = "0") int height,//限制高度
        @RequestParam(value = "heightList", required = false, defaultValue = "") String heightList, //限制宽度2
        @RequestParam(value = "widthList", required = false, defaultValue = "") String widthList //限制高度2
    )
    {
        Map<String, Object> result = new HashMap<String, Object>();
        try
        {
            if (file.isEmpty())
            {
                result.put("status", 0);
                result.put("msg", "请选择文件后上传");
                return JSON.toJSONString(result);
            }
            if (file.getSize() > 409600 && limitSize == 1)
            {
                result.put("status", 0);
                result.put("msg", "图片不得大于400kb");
                return JSON.toJSONString(result);
            }
            List<Integer> heights = new ArrayList<Integer>();
            if(org.apache.commons.lang.StringUtils.isNotEmpty(heightList)) {
                List<String>  heightStrings = Splitter.on(",").splitToList(heightList);
                for(String s : heightStrings) {
                    heights.add(Integer.valueOf(s));
                }
            }
            List<Integer> widths = new ArrayList<Integer>();
            if(org.apache.commons.lang.StringUtils.isNotEmpty(widthList)) {
                List<String>  widthStrings = Splitter.on(",").splitToList(widthList);
                for(String s : widthStrings) {
                    widths.add(Integer.valueOf(s));
                }
            }

            //限制图片宽高
            if (width > 0)
            {
                BufferedImage img = ImageIO.read(file.getInputStream());
                int currWidth = img.getWidth();
                if (currWidth != width)
                {
                    result.put("status", 0);
                    result.put("msg", "图片尺寸不符合要求，要求宽度：" + width + "，实际宽度：" + currWidth + "。");
                    return JSON.toJSONString(result);
                }
                
            }
            if (height > 0)
            {
                BufferedImage img = ImageIO.read(file.getInputStream());
                int currHeight = img.getHeight();
                if (currHeight != height)
                {
                    result.put("status", 0);
                    result.put("msg", "图片尺寸不符合要求，要求高度：" + height + "，实际高度：" + currHeight + "。");
                    return JSON.toJSONString(result);
                }
            }
            if (CollectionUtils.isNotEmpty(widths))
            {
                BufferedImage img = ImageIO.read(file.getInputStream());
                int currWidth = img.getWidth();
                if (!widths.contains(currWidth))
                {
                    result.put("status", 0);
                    result.put("msg", "图片尺寸不符合要求，要求宽度：" + widths + "，实际宽度：" + currWidth + "。");
                    return JSON.toJSONString(result);
                }
            }
            if (CollectionUtils.isNotEmpty(heights))
            {
                BufferedImage img = ImageIO.read(file.getInputStream());
                int currHeight = img.getHeight();
                if (!heights.contains(currHeight))
                {
                    result.put("status", 0);
                    result.put("msg", "图片尺寸不符合要求，要求高度：" + heights + "，实际高度：" + currHeight + "。");
                    return JSON.toJSONString(result);
                }
            }
            Random random = new Random();
            String directory = "Product";//上传图片目录的目录
            byte[] bt = file.getBytes();
            String fileName = file.getOriginalFilename();
            int pointIndex = fileName.lastIndexOf(".");
            String fileExt = fileName.substring(pointIndex);
            String id = Long.toHexString(Long.valueOf(random.nextInt(10) + "" + System.currentTimeMillis() + "" + random.nextInt(10)));
            fileName = directory + id + fileExt.toLowerCase();
            result = toUP(bt, fileName);
        }
        catch (Exception e)
        {	log.error("-----------文件上传失败-----------------"+e);
            result.put("status", 0);
            result.put("msg", "文件上传失败");
        }
        return JSON.toJSONString(result);
    }

    
    //上传图片调用方法
    private Map<String, Object> toUP(byte[] bt, String fileName)
            throws Exception
        {
            Map<String, Object> result = new HashMap<String, Object>();
            Map<String, String> resultMap = null;
            resultMap = oSSImgYunService.uploadFile(bt, fileName);
            if (resultMap.get("status").equals("success"))
            {
                result.put("status", 1);
                String newUrlString = resultMap.get("url") + "";
                result.put("url", newUrlString);
            }
            else
            {   log.error("-----------文件上传失败-----------------");
                result.put("status", 0);
                result.put("msg", "文件上传失败");
            }
            return result;
        }
    
    
    
    
}
