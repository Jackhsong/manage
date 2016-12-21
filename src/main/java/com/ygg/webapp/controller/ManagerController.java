package com.ygg.webapp.controller;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.ygg.webapp.entity.ManagerEntity;
import com.ygg.webapp.service.ManagerService;
import com.ygg.webapp.util.CommonUtil;
import com.ygg.webapp.util.VerifyCodeUtils;

/**
 *管理员登陆页面
 */
@Controller
@RequestMapping("/admin")
public class ManagerController {
	
    Logger log = Logger.getLogger(ManagerController.class);
    
    @Resource(name="managerService")
    private ManagerService managerService;
 

	/**
	 * 登陆跳转
	 */
	@RequestMapping("/login")
	public ModelAndView tologin(){
		ModelAndView mv=new ModelAndView();
		mv.setViewName("/manager/login");
		
		return mv;	
	}
	

    /**
     * 验证码图片获取
     */
    @RequestMapping(value = "/getCode")
    public void export(HttpServletRequest request, HttpServletResponse response, HttpSession session)
        throws Exception
    {        
        String code = VerifyCodeUtils.generateVerifyCode(4);
        request.getSession().setAttribute("verifyCode", code.toLowerCase());
        int w = 80, h = 40;
        
        int verifySize = code.length();
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Random rand = new Random();
        Graphics2D g2 = image.createGraphics();
        Color[] colors = new Color[5];
        Color[] colorSpaces = new Color[] {Color.WHITE, Color.CYAN, Color.GRAY, Color.LIGHT_GRAY, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.YELLOW};
        float[] fractions = new float[colors.length];
        for (int i = 0; i < colors.length; i++)
        {
            colors[i] = colorSpaces[rand.nextInt(colorSpaces.length)];
            fractions[i] = rand.nextFloat();
        }
        Arrays.sort(fractions);
        Paint linearPaint = new LinearGradientPaint(0, 0, w, h, fractions, colors);
        Paint linearPaint2 =
            new LinearGradientPaint(0, 0, w, h, new float[] {0.3f, .6f, .8f, .9f}, new Color[] {Color.BLUE, Color.BLACK, Color.GREEN, Color.BLUE});
        // 设置图片背景为白色
        g2.setPaint(Color.WHITE);
        g2.fillRect(0, 0, w, h);
        // 设置图片渐变背景
        g2.setPaint(linearPaint);
        g2.fillRoundRect(0, 0, w, h, 5, 5);
        
        g2.setPaint(linearPaint2);
        int fontSize = (int)(Math.min(w / verifySize, h));
        Font font = new Font("微软雅黑", Font.BOLD, fontSize);
        g2.setFont(font);
        char[] chars = code.toCharArray();
        for (int i = 0; i < verifySize; i++)
        {
            AffineTransform affine = new AffineTransform();
            affine.setToRotation(Math.PI / 4 * rand.nextDouble() * (rand.nextBoolean() ? 1 : -1), (w / verifySize) * i + fontSize / 2, h / 2);
            g2.setTransform(affine);
            g2.drawChars(chars, i, 1, (w / verifySize) * i, h / 2 + fontSize / 2);
        }
        g2.dispose();
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        // 将图像输出到Servlet输出流中。
        ServletOutputStream sos = response.getOutputStream();
        ImageIO.write(image, "jpeg", sos);
        sos.close();
    }
	
	
	/**
	 * 验证管理员信息
	 */
    @RequestMapping(value = "/checkManager", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String shiroLogin(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "name", required = true) String name,
        @RequestParam(value = "pwd", required = true) String pwd, @RequestParam(value = "code", required = true) String code)
        throws Exception
    {
        Object sessionCode = request.getSession().getAttribute("verifyCode");
        if (sessionCode == null || !(sessionCode + "").equalsIgnoreCase(code))
        {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("status", 0);
            map.put("msg", "验证码错误!");
            return JSON.toJSONString(map);
        }
        pwd = CommonUtil.strToMD5(pwd + name);
        ManagerEntity me=managerService.selectByName(name);
        if(me==null){
        	 //用户名不存在
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("status", 0);  
            map.put("msg", "用户名不存在！");
            return JSON.toJSONString(map);
        }
        String savePwd=me.getPwd();
        if (!pwd.equals(savePwd))
        {
            // 登陆失败
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("status", 0);
            map.put("msg", "密码错误!");
            return JSON.toJSONString(map);
        }
        // 登陆成功
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("status", 1);
        result.put("msg", "登陆成功");
        //登陆成功，添加用户信息，保存20分钟
        request.getSession().setAttribute("Manager", me);
        return JSON.toJSONString(result);
    }
	
	
    /**
     * 登陆成功进入首页
     */
    @RequestMapping("/index")
    public ModelAndView index(){
    	  ModelAndView mv = new ModelAndView();

          mv.setViewName("redirect:/productBase/list");
          return mv;
    }
    
    
    /**
    * 退出
    */
   @RequestMapping("/logout")
   public ModelAndView logout(HttpServletRequest request)
       throws Exception
   {
       ModelAndView mv = new ModelAndView();
       //退出的时候清除session
       request.getSession().invalidate();
       mv.setViewName("redirect:/admin/login");
       return mv;
   }
    
	
   /**
    * 菜单管理
    */
   @RequestMapping("/menu")
   @ResponseBody
   public String chooseMenu(HttpServletRequest request) throws Exception{
	   
	   List<Map<String, Object>> menuList = new ArrayList<>();

	   String nodes = request.getParameter("nodes");
       List<Integer> stateList = new ArrayList<>();
       if (nodes != null && !nodes.equals("0"))
       {
           String[] msArr = nodes.split("-");
           for (String string : msArr){
               stateList.add(Integer.valueOf(string));
           }
       }

       Map<String,Object> para = new HashMap<>();
       para.put("pid", 0);
       List<Map<String, Object>> firstMenuList = managerService.findAllMenuByPara(para);     

       for (Map<String, Object> firstMenu : firstMenuList){       	   
           int id = Integer.valueOf(firstMenu.get("id") + "");           
           List<Map<String, Object>> children = loadChild(id);

           if (children.size() > 0){
               Map<String,Object> currMenu = new HashMap<>();
               currMenu.put("children", children);
               currMenu.put("id", id + "");
               currMenu.put("text", firstMenu.get("text"));
               if (stateList.contains(id)){
                   currMenu.put("state", "open");
               }else{
                   currMenu.put("state", "closed");
               }
               // 自定义属性放这个map
               Map<String, Object> attributes = new HashMap<>();
               attributes.put("url", firstMenu.get("url") + "");
               currMenu.put("attributes", attributes);
               menuList.add(currMenu);
           }
       }
	return JSON.toJSONString(menuList);
   }


	/**
	 * 菜单调用方法
	 */
   private List<Map<String, Object>> loadChild(int pid)throws Exception{
	        List<Map<String, Object>> menuInfoList = new ArrayList<>();
	        Map<String,Object> para = new HashMap<>();
	        para.put("pid", pid);
	        List<Map<String, Object>> currMenuList =managerService.findAllMenuByPara(para);  
	        for (Map<String, Object> it : currMenuList)
	        {
	            String url = it.get("url") + ""; 
	            Map<String,Object> childMenu = null;
	            if ("null".equals(url) || "".equals(url))
	            {
	                // 是目录
	                Integer _id = Integer.valueOf(it.get("id") + "");
	                List<Map<String, Object>> nextChildren = loadChild(_id);
	                if (nextChildren.size() > 0)
	                {
	                    childMenu = new HashMap<>();
	                    childMenu.put("state", "open");
	                    childMenu.put("children", nextChildren);
	                }
	            }
	            else
	            {
	                    childMenu = new HashMap<>();
	            }
	            if (childMenu != null)
	            {
	                childMenu.put("id", it.get("id"));
	                childMenu.put("text", it.get("text"));
	                Map<String, Object> attributes = new HashMap<>();
	                attributes.put("url", url);
	                childMenu.put("attributes", attributes);
	                menuInfoList.add(childMenu);
	            }
	        }
	        return menuInfoList;
	    }
	
	
	

}


