package com.ygg.webapp.controller;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.ygg.webapp.annotation.ControllerLog;
import com.ygg.webapp.entity.BrandEntity;
import com.ygg.webapp.entity.ProductEntity;
import com.ygg.webapp.entity.ProductInfoForGroupSale;
import com.ygg.webapp.entity.QqbsSaleWindowEntity;
import com.ygg.webapp.entity.SellerEntity;
import com.ygg.webapp.service.ActivitiesCommonService;
import com.ygg.webapp.service.ProductService;
import com.ygg.webapp.service.QqbsSaleWindowServcie;
import com.ygg.webapp.util.CommonEnum;
import com.ygg.webapp.util.ImageUtil;
import com.ygg.webapp.service.SellerService;
import com.ygg.webapp.service.BrandService;

/**
 * 首页特卖管理
 * 
 * @author Administrator
 * 
 */
@Controller
@RequestMapping("/qqbsSale")
public class SaleController
{
    
    @Resource(name = "activitiesCommonService")
    private ActivitiesCommonService activitiesCommonService;
    
    @Resource(name = "productService")
    private ProductService productService;
    
    @Resource(name = "qqbsSaleWindowServcie")
    private QqbsSaleWindowServcie qqbsSaleWindowServcie;
    
    @Resource(name = "sellerService")
    private SellerService sellerService;
    
    @Resource(name = "brandService")
    private BrandService brandService;
    
    private Logger log = Logger.getLogger(SaleController.class);
    
    /**
     * 转发到特卖添加页面
     * 
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/addSale")
    public ModelAndView addSale(HttpServletRequest request)
        throws Exception
    {
        ModelAndView mv = new ModelAndView();
        QqbsSaleWindowEntity saleWindow = new QqbsSaleWindowEntity();
        mv.addObject("saleWindow", saleWindow);
        
        // 获取标签数据
        Map<String, Object> tagPara = new HashMap<String, Object>();
        tagPara.put("available", "1");
        tagPara.put("start", 0);
        tagPara.put("max", 100);
        
        mv.addObject("flagId", "0");
        
        mv.setViewName("sale/saleForm");
        return mv;
    }
    
    /**
     * @param request
     * @param id
     * @return
     * @throws Exception
     */
    @RequestMapping("/edit/{id}")
    public ModelAndView edit(HttpServletRequest request, @PathVariable("id") int id)
        throws Exception
    {
        ModelAndView mv = new ModelAndView();
        QqbsSaleWindowEntity saleWindow = qqbsSaleWindowServcie.findSaleWindowById(id);
        if (saleWindow == null)
        {
            mv.setViewName("forward:/error/404");
            return mv;
        }
        mv.addObject("saleWindow", saleWindow);
        String startTime = saleWindow.getStartTime();
        String endTime = saleWindow.getEndTime();
        mv.addObject("startTime", startTime.substring(0, startTime.length() - 2));
        mv.addObject("endTime", endTime.substring(0, endTime.length() - 2));
        
        // 组合特卖ID
        mv.addObject("id", saleWindow.getDisplayId());
        mv.addObject("flagId", saleWindow.getSaleFlagId() + "");
        
        mv.setViewName("sale/saleForm");
        return mv;
    }
    
    @RequestMapping("/list")
    public ModelAndView list(HttpServletRequest request)
        throws Exception
    {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("sale/saleManage");
        return mv;
    }
    
    /**
     *
     * @param page
     * @param rows
     * @param type：特卖位类型：1单品，2组合，3自定义特卖
     * @param saleStatus：特卖状态：1等待开始，2进行中，3已结束
     * @param name：特卖名称
     * @param isDisplay：展现状态，1展现，0不展现
     * @param searchStartTime：开售时间，开售时间
     * @param productId：商品Id
     * @param productName：商品名称
     * @return
     */
    @RequestMapping(value = "/jsonSaleWindowInfo", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String jsonSaleWindowInfo(@RequestParam(value = "page", required = false, defaultValue = "1") int page, //
        @RequestParam(value = "rows", required = false, defaultValue = "10") int rows, //
        @RequestParam(value = "type", required = false, defaultValue = "-1") int type, //
        @RequestParam(value = "saleStatus", required = false, defaultValue = "-1") int saleStatus, //
        @RequestParam(value = "name", required = false, defaultValue = "") String name, //
        @RequestParam(value = "isDisplay", required = false, defaultValue = "-1") int isDisplay, //
        @RequestParam(value = "startTime", required = false, defaultValue = "") String searchStartTime, //
        @RequestParam(value = "productId", required = false, defaultValue = "0") int productId, //
        @RequestParam(value = "pId", required = false, defaultValue = "0") int pId, //
        @RequestParam(value = "brandId", required = false, defaultValue = "-1") int brandId, //
        @RequestParam(value = "productName", required = false, defaultValue = "") String productName, //
        @RequestParam(value = "sellerId", required = false, defaultValue = "-1") int sellerId, HttpServletResponse response, String isExport)
    {
        Map<String, Object> para = new HashMap<String, Object>();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try
        {
            if (page == 0)
            {
                page = 1;
            }           
            // 导出时不分页
            if (StringUtils.isBlank(isExport))
            {
                para.put("start", rows * (page - 1));
                para.put("max", rows);
            }
            para.put("type", type);
            if (isDisplay != -1)
            {
                para.put("isDisplay", isDisplay);
            }
            para.put("status", saleStatus);
            para.put("order", 1);
            
            if (!"".equals(name))
            {
                para.put("name", "%" + name + "%");
            }
            if (!"".equals(searchStartTime))
            {
                para.put("startTime", searchStartTime);
            }
            if (productId != 0)
            {
                para.put("productId", productId);
            }
            if (pId != 0)
            {
                para.put("pId", pId);
            }
            if (!"".equals(productName))
            {
                para.put("productName", "%" + productName + "%");
            }
            if (brandId != -1)
            {
                para.put("brandId", brandId);
            }
            if (sellerId != -1)
            {
                para.put("sellerId", sellerId);
            }
            
            resultMap = qqbsSaleWindowServcie.findAllSaleWindowNew(para);
            // 导出
            if (StringUtils.isNotBlank(isExport))
            {  
                exportQqbsSaleWindow(response, resultMap);
                return null;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            resultMap.put("rows", new ArrayList<Map<String, Object>>());
            resultMap.put("total", 0);
        }
        return JSON.toJSONString(resultMap);
    }
    
    @SuppressWarnings("unchecked")
    private void exportQqbsSaleWindow(HttpServletResponse response, Map<String, Object> resultInfo)
    {
        String displayName = "心动慈露特卖管理导出";
        String[] headContent = {"特卖ID", "组特或单品ID", "展现状态", "特卖状态", "排序值", "场次", "特卖类型", "关联商家", "名称", "特卖描述", "库存数量", "开始时间", "结束时间"};
        excel(response, displayName, headContent, map2arr((List<Map<String, Object>>)resultInfo.get("rows")));
    }
    
    private List<Object[]> map2arr(List<Map<String, Object>> result)
    {
        List<Object[]> rowContents = new ArrayList<Object[]>();
        if (result != null && result.size() > 0)
        {
            for (Map<String, Object> map : result)
            {
                int i = 0;
                Object[] obj = new Object[13];
                obj[i++] = map.get("id");
                obj[i++] = map.get("displayId");
                obj[i++] = map.get("isDisplay");
                obj[i++] = map.get("saleStatus");
                obj[i++] = map.get("order");
                obj[i++] = map.get("saleTimeTypeStr");
                obj[i++] = map.get("type");
                obj[i++] = map.get("relaRealSellerName");
                obj[i++] = map.get("name");
                obj[i++] = map.get("desc");
                obj[i++] = map.get("stock");
                obj[i++] = map.get("startTime");
                obj[i++] = map.get("endTime");
                rowContents.add(obj);
            }
        }
        return rowContents;
    }
    
    private void excel(HttpServletResponse response, String displayName, String[] headContent, List<Object[]> rowContents)
    {
        HSSFWorkbook workbook = null;
        OutputStream fOut = null;
        try
        {
            // 进行转码，使其支持中文文件名
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("content-disposition", "attachment;filename=" + new String(displayName.getBytes("UTF-8"), "ISO8859-1") + ".xls");
            // 产生工作簿对象
            workbook = new HSSFWorkbook();
            // 产生工作表对象
            HSSFSheet sheet = workbook.createSheet();
            int rowCount = 0;
            // 写入excel文件头
            if (headContent != null && headContent.length > 0)
            {
                Row row = sheet.createRow(rowCount++);
                for (int i = 0; i < headContent.length; i++)
                {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(headContent[i]);
                }
            }
            // 写入excel内容
            if (rowContents != null && rowContents.size() > 0)
            {
                for (Object[] rowContent : rowContents)
                {
                    Row row = sheet.createRow(rowCount++);
                    for (int i = 0; i < rowContent.length; i++)
                    {
                        Cell cell = row.createCell(i, Cell.CELL_TYPE_STRING);
                        cell.setCellValue(String.valueOf(rowContent[i]));
                    }
                }
            }
            fOut = response.getOutputStream();
            workbook.write(fOut);
            fOut.flush();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
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
    
    /**
     * 查询商品 特卖类型管理的商品
     * 
     * @param request
     * @param page
     * @param rows
     * @param type
     * @param subjectId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/jsonProductInfo", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String jsonProductInfo(HttpServletRequest request, @RequestParam(value = "page", required = false, defaultValue = "1") int page,
        @RequestParam(value = "rows", required = false, defaultValue = "10") int rows, @RequestParam(value = "type", required = true) int type, // 1单品；2组合特卖；3自定义特卖
        @RequestParam(value = "subjectId", required = false, defaultValue = "0") int subjectId)
        throws Exception
    {
        Map<String, Object> para = new HashMap<String, Object>();
        if (page == 0)
        {
            page = 1;
        }
        para.put("start", rows * (page - 1));
        para.put("max", rows);
        para.put("id", subjectId);
        
        Map<String, Object> resultMap = new HashMap<String, Object>();
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        
        if (type == 1 && subjectId != 0)
        {
            ProductEntity product = productService.findProductById(subjectId);
            if (product != null)
            {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("id", product.getId());
                map.put("showId", product.getId());
                map.put("code", product.getCode());
                map.put("name", product.getName());
                map.put("startTime", product.getStartTime());
                map.put("endTime", product.getEndTime());
                resultList.add(map);
                resultMap.put("rows", resultList);
                resultMap.put("total", 1);
            }
            else
            {
                resultMap.put("rows", resultList);
                resultMap.put("total", 0);
            }
        }
        else if (type == 2 && subjectId != 0)
        {
            List<ProductInfoForGroupSale> pList = activitiesCommonService.findProductInfoForGroupSaleByActivitiesCommonId(para);
            for (ProductInfoForGroupSale productInfoForGroupSale : pList)
            {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("showId", productInfoForGroupSale.getProductId());
                map.put("id", productInfoForGroupSale.getProductId());
                map.put("code", productInfoForGroupSale.getCode());
                map.put("name", productInfoForGroupSale.getName());
                map.put("startTime", productInfoForGroupSale.getStartTime());
                map.put("endTime", productInfoForGroupSale.getEndTime());
                resultList.add(map);
            }
            resultMap.put("rows", resultList);
            int total = activitiesCommonService.countProductNumByActivitiesCommonId(subjectId);
            resultMap.put("total", total);
        }
        else
        {
            
        }
        return JSON.toJSONString(resultMap);
    }
    
    /**
     * 修改商品时间
     * 
     * @param request
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/updateProductTime", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    @ControllerLog(description = "特卖位管理-修改商品时间")
    public String updateProductTime(HttpServletRequest request, @RequestParam(value = "productIds", required = true) String productIds,
        @RequestParam(value = "startTime", required = true) String startTime, @RequestParam(value = "endTime", required = true) String endTime,
        @RequestParam(value = "type", required = false) int type, @RequestParam(value = "saleId", required = false, defaultValue = "0") int saleId)
        throws Exception
    {
        
        List<Integer> idList = new ArrayList<>();
        if (productIds.indexOf(",") > 0)
        {
            String[] arr = productIds.split(",");
            for (String cur : arr)
            {
                idList.add(Integer.valueOf(cur));
            }
        }
        else
        {
            idList.add(Integer.valueOf(productIds));
        }
        for (Integer currId : idList)
        {
            ProductEntity pEntity = new ProductEntity();
            pEntity.setId(currId);
            pEntity.setStartTime(startTime);
            pEntity.setEndTime(endTime);
            ProductEntity pe = productService.findProductById(currId);
            
            productService.updateProductTime(pEntity);
            // 记录日志
            try
            {
                Map<String, Object> logInfoMap = new HashMap<>();
                logInfoMap.put("businessType", CommonEnum.BusinessTypeEnum.SELL_MANAGEMENT.ordinal());
                if (type == 1)// banner修改商品销售时间
                {
                    logInfoMap.put("operationType", CommonEnum.OperationTypeEnum.MODIFY_PRODUCT_TIME.ordinal());
                }
                if (type == 2)// 特卖修改商品时间
                {
                    logInfoMap.put("saleId", saleId);
                    logInfoMap.put("operationType", CommonEnum.OperationTypeEnum.MODIFY_SELL_PRODUCT_TIME.ordinal());
                }
                logInfoMap.put("level", CommonEnum.LogLevelEnum.LEVEL_TWO.ordinal());
                String newTime = startTime + "~" + endTime;
                String oldTime = pe.getStartTime().substring(0, pe.getStartTime().lastIndexOf(".")) + "~" + pe.getEndTime().substring(0, pe.getEndTime().lastIndexOf("."));
                logInfoMap.put("objectId", currId);
                logInfoMap.put("oldTime", oldTime);
                logInfoMap.put("newTime", newTime);
                //logService.logger(logInfoMap);
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
        
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("status", 1);
        resultMap.put("msg", "修改成功");
        return JSON.toJSONString(resultMap);
    }
    
    /**
     * 新增特卖
     * 
     * @param request
     * @param editId
     * @param name
     * @param desc
     * @param image
     * @return
     * @throws Exception
     */
    @RequestMapping("/save")
    @ControllerLog(description = "特卖位管理-新增特卖位")
    public ModelAndView save(HttpServletRequest request, @RequestParam(value = "editId", required = false, defaultValue = "0") int editId,
        @RequestParam(value = "type", required = true) byte type, // 1单品；2组合特卖；3自定义特卖
        @RequestParam(value = "name", required = true) String name, //
        @RequestParam(value = "saleTimeType", required = true) int saleTimeType, // 1：早场，2：晚场
        @RequestParam(value = "desc", required = false, defaultValue = "") String desc, //
        @RequestParam(value = "image", required = false, defaultValue = "") String image,//
        @RequestParam(value = "order", required = false, defaultValue = "0") short order, @RequestParam(value = "code", required = false, defaultValue = "0") int code, // 商品ID
        @RequestParam(value = "groupSale", required = false, defaultValue = "0") int groupSale,//
        @RequestParam(value = "startTime", required = true) String startTime,//
        @RequestParam(value = "endTime", required = true) String endTime, //
        @RequestParam(value = "isDisplay", required = true) byte isDisplay,//
        @RequestParam(value = "saleFlagId", required = false, defaultValue = "0") int saleFlagId,//
        @RequestParam(value = "isDisplayFlag", required = false, defaultValue = "1") int isDisplayFlag)
        throws Exception
    {
        ModelAndView mv = new ModelAndView();
        if (image.indexOf(ImageUtil.getPrefix()) > 0)
        {
            image = image.substring(0, image.indexOf(ImageUtil.getPrefix()));
        }
        QqbsSaleWindowEntity saleWindow = new QqbsSaleWindowEntity();
        saleWindow.setType(type);
        saleWindow.setName(name);
        saleWindow.setDesc(desc);
        saleWindow.setOrder(order);
        saleWindow.setImage(image);
        saleWindow.setIsDisplay(isDisplay);
        saleWindow.setSaleFlagId(saleFlagId);
        saleWindow.setIsDisplayFlag(isDisplayFlag);
        saleWindow.setSaleTimeType(saleTimeType);
        if (type == 1)
        {
            saleWindow.setDisplayId(code);
        }
        else if (type == 2)
        {
            saleWindow.setDisplayId(groupSale);
        }
        saleWindow.setStartTime(startTime);
        saleWindow.setEndTime(endTime);
        
        if (editId != 0)
        {
            saleWindow.setId(editId);
            qqbsSaleWindowServcie.update(saleWindow);
        }
        else
        {
            // insert 新增默认排序值在最前面
            qqbsSaleWindowServcie.save(saleWindow);
        }
        
        mv.setViewName("redirect:/sale/list");
        return mv;
    }
    
    @RequestMapping(value = "/updateDisplayCode", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    @ControllerLog(description = "特卖位管理-修改特卖位展现状态")
    public String updateDisplayCode(HttpServletRequest request, @RequestParam(value = "id", required = true) int id, @RequestParam(value = "code", required = true) int code)
        throws Exception
    {
        Map<String, Object> para = new HashMap<>();
        para.put("id", id);
        para.put("isDisplay", code == 1 ? 0 : 1);
        int resultStatus = qqbsSaleWindowServcie.updateDisplayCode(para);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if (resultStatus != 1)
        {
            resultMap.put("status", 0);
            resultMap.put("msg", "修改失败");
        }
        else
        {
            resultMap.put("status", 1);
        }
        return JSON.toJSONString(resultMap);
    }
    
    /**
     * 检查商品的特卖时间 并且检查商品的分销供货价是否大于或等于售价*60%并且小于等于售价
     * 
     * @param request
     * @param type
     * @param subjectId
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/checkProductTime", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String checkProductTime(HttpServletRequest request,
        @RequestParam(value = "type", required = true) int type, // 1单品；2组合特卖；3自定义特卖
        @RequestParam(value = "subjectId", required = true) int subjectId, @RequestParam(value = "startTime", required = false) String startTime,
        @RequestParam(value = "endTime", required = false) String endTime)
        throws Exception
    {
        Map<String, Object> para = new HashMap<>();
        Map<String, Object> resultMap = new HashMap<>();
        para.put("type", type);
        para.put("subjectId", subjectId);
        boolean isExist = qqbsSaleWindowServcie.checkIsExist(para);
        if (!isExist)
        {
            resultMap.put("status", 0);
            String tip = "单品";
            switch (type)
            {
                case 1:
                    tip = "单品";
                    break;
                case 2:
                    tip = "组合特卖";
                    break;
                default:
                    break;
            }
            resultMap.put("msg", "id=" + subjectId + "的" + tip + "不存在");
            return JSON.toJSONString(resultMap);
        }
        if (startTime != null)
        {
            para.put("startTime", startTime);
        }
        if (endTime != null)
        {
            para.put("endTime", endTime);
        }
        resultMap = qqbsSaleWindowServcie.checkProductTime(para);
        return JSON.toJSONString(resultMap);
        
    }
    
    @RequestMapping(value = "/checkNameAndDescLength", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String checkNameAndDescLength(@RequestParam(value = "name", required = true, defaultValue = "") String name,
        @RequestParam(value = "desc", required = true, defaultValue = "") String desc)
        throws Exception
    {
        Map<String, Object> map = new HashMap<>();
        float count = 0.0f;
        if (!"".equals(name))
        {
            name = name.replaceAll(" ", "");
            for (int i = 0; i < name.length(); i++)
            {
                char ch = name.charAt(i);
                if (ch >= 'a' && ch <= 'z')
                {
                    count += 0.5;
                }
                else if (ch >= '0' && ch <= '9')
                {
                    count += 0.5;
                }
                else
                {
                    count += 1;
                }
            }
        }
        if (!"".equals(desc))
        {
            desc = desc.replaceAll(" ", "");
            for (int i = 0; i < desc.length(); i++)
            {
                char ch = desc.charAt(i);
                if (ch >= 'a' && ch <= 'z')
                {
                    count += 0.5;
                }
                else if (ch >= '0' && ch <= '9')
                {
                    count += 0.5;
                }
                else
                {
                    count += 1;
                }
            }
        }
        if ((int)count >= 22)
        {
            map.put("status", 0);
            map.put("msg", "名称+描述不得超过22个字符");
        }
        else
        {
            map.put("status", 1);
        }
        return JSON.toJSONString(map);
    }
    
    @RequestMapping(value = "/updateOrder")
    @ResponseBody
    public Object updateOrder(int id, int order)
        throws Exception
    {
        try
        {
            Map<String, Object> map = new HashMap<>();
            map.put("status", 1);
            map.put("data", qqbsSaleWindowServcie.updateOrder(id, order));
            return map;
        }
        catch (Exception ex)
        {
            Map<String, Object> map = new HashMap<>();
            map.put("status", 0);
            map.put("msg", ex.getMessage());
            return map;
        }
    }
    
    
    /**
     * 得到品牌信息以json格式返回
     * 
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/jsonBrandCode", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String jsonBrandCode(HttpServletRequest request, @RequestParam(value = "isAvailable", required = false, defaultValue = "-1") int isAvailable,
        @RequestParam(value = "brandId", required = false, defaultValue = "-1") int brandId,
        @RequestParam(value = "mallCountryId", required = false, defaultValue = "-1") int mallCountryId)
        throws Exception
    {
        Map<String, Object> para = new HashMap<String, Object>();
        if (isAvailable != -1)
        {
            para.put("isAvailable", isAvailable);
        }
        List<BrandEntity> brandList = brandService.findAllBrand(para);
        List<Map<String, String>> codeList = new ArrayList<Map<String, String>>();
        Map<String, String> mapAll = new HashMap<String, String>();
        if (isAvailable == -1)
        {
            mapAll.put("code", "0");
            mapAll.put("text", "全部");
            codeList.add(mapAll);
        }
        for (BrandEntity entity : brandList)
        {
            Map<String, String> map = new HashMap<String, String>();
            map.put("code", entity.getId() + "");
            map.put("text", entity.getName());
            if (brandId == entity.getId())
            {
                map.put("selected", "true");
            }
            codeList.add(map);
        }
        return JSON.toJSONString(codeList);
    }
    
    
    /**
     * 得到商家信息以json格式返回
     * 
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/jsonSellerCode", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String jsonSellerCode(HttpServletRequest request,
        @RequestParam(value = "id", required = false, defaultValue = "0") int id)
        throws Exception
    {
        List<SellerEntity> sellerList =sellerService.findAllSeller(null);
        List<Map<String, String>> codeList = new ArrayList<Map<String, String>>();
        for (SellerEntity entity : sellerList)
        {
            Map<String, String> map = new HashMap<String, String>();
            map.put("code", entity.getId() + "");
            String p = entity.getIsAvailable() == 1 ? "" : "(已停用)";
            map.put("text", p + entity.getRealSellerName() + "(" + entity.getSendAddress() + "-" + entity.getWarehouse() + ")");
            if (entity.getId() == id)
            {
                map.put("selected", "true");
            }
            codeList.add(map);
        }
        return JSON.toJSONString(codeList);
    }
    
    
}
