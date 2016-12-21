package com.ygg.webapp.service.Impl;

import com.alibaba.fastjson.JSON;
import com.ygg.webapp.dao.AreaDao;
import com.ygg.webapp.dao.ProductBaseDao;
import com.ygg.webapp.dao.ProductDao;
import com.ygg.webapp.entity.ProductBaseEntity;
import com.ygg.webapp.entity.ProductCountEntity;
import com.ygg.webapp.entity.ProductEntity;
import com.ygg.webapp.entity.ResultEntity;
import com.ygg.webapp.service.ProductBaseService;
import com.ygg.webapp.util.CommonConstant;
import com.ygg.webapp.util.ImageTypeEnum;
import com.ygg.webapp.util.ImageUtil;
import com.ygg.webapp.util.ProductEnum;
import com.ygg.webapp.util.SellerEnum;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Pattern;

@Service("productBaseService")
public class ProductBaseServiceImpl implements ProductBaseService
{
    @Resource
    private ProductBaseDao productBaseDao;
    
    @Resource
    private ProductDao productDao;

    @Resource
    private AreaDao areaDao;
    
    private Logger logger = Logger.getLogger(ProductBaseServiceImpl.class);
    
    @Override
    public ResultEntity ajaxPageDataProductBase(Map<String, Object> para)
        throws Exception
    {
        List<Map<String, Object>> productInfoList = productBaseDao.queryAllProductBaseInfo(para);
        List<Integer> productIds = new ArrayList<>();
        for (Map<String, Object> mp:productInfoList)
        {
            productIds.add(Integer.parseInt(mp.get("productId").toString()));
        }

        Map<String, Object> params = new HashMap<>();
        params.put("productBaseIds", productIds);
        params.put("endTime", Integer.parseInt(DateTime.now().toString("yyyyMMddHH")));
        params.put("startTime", Integer.parseInt(DateTime.now().minusDays(7).toString("yyyyMMddHH")));
        List<Map<String, Object>> salesVolumeIn7 = new ArrayList<>();
        if (productIds.size() > 0)
        {
            salesVolumeIn7.addAll(productBaseDao.findProductBaseSalesVolumeByPara(params));
        }
        params.put("startTime", Integer.parseInt(DateTime.now().minusDays(30).toString("yyyyMMddHH")));
        List<Map<String, Object>> salesVolumeIn30 = new ArrayList<>();
        if (productIds.size() > 0)
        {
            salesVolumeIn30.addAll(productBaseDao.findProductBaseSalesVolumeByPara(params));//30日销量
        }

        Map<String, Object> salesVolumeIn7Map = new HashMap<>();
        for (Map<String, Object> it : salesVolumeIn7) {
            salesVolumeIn7Map.put(it.get("productBaseId").toString(), it.get("salesVolume"));
        }

        Map<String, Object> salesVolumeIn30Map = new HashMap<>();
        for (Map<String, Object> it : salesVolumeIn30) {
            salesVolumeIn30Map.put(it.get("productBaseId").toString(), it.get("salesVolume"));
        }

        for (Map<String, Object> curr : productInfoList)
        {
            int id = Integer.valueOf(curr.get("productId") + "").intValue();
            int sendType = Integer.valueOf(curr.get("sendType") + "");
            curr.put("sendType", SellerEnum.SellerTypeEnum.getDescByCode(sendType));
            if (CommonConstant.COMMON_NO == Integer.parseInt(curr.get("sellerIsAvailable").toString()))
            {
                curr.put("sellerName", curr.get("sellerName") + "<span style=\"color:red\">(已停用)</span>");
            }
            int submitType = Integer.parseInt(curr.get("submitType").toString());
            curr.put("submitType", "供货价");
            curr.put("submitContent", submitType == 1 ? curr.get("wholesalePrice") : submitType == 2 ? curr.get("deduction") + "%" : curr.get("selfPurchasePrice"));
            int freightType = Integer.parseInt(curr.get("freightType") + "");
            String type = "";
            switch (freightType)
            {
                case 1:
                    type = "包邮";
                    break;
                case 2:
                    type = "满" + curr.get("freightMoney") + "包邮";
                    break;
                case 3:
                    type = "不包邮";
                    break;
                case 4:
                    type = curr.get("freightOther") + "";
                    break;
            }
            curr.put("freightType", type);
            curr.put("isAvailable", (Integer.valueOf(curr.get("isAvailable") + "")) == 1 ? "可用" : "不可用");
            //组装分类名称，给导出用
            StringBuilder categoryName = new StringBuilder();
            curr.put("categoryName", categoryName.length() > 0 ? categoryName.substring(0, categoryName.length() - 1) : categoryName);
            if (curr.get("expireDate") == null)
            {
                curr.put("expireDate","");
            }
            else {
                curr.put("expireDate", curr.get("expireDate").toString());
            }
            curr.put("salesVolumeIn7", salesVolumeIn7Map.get(id + "") == null ? "0" : salesVolumeIn7Map.get(id + ""));
            curr.put("salesVolumeIn30", salesVolumeIn30Map.get(id + "") == null ? "0" : salesVolumeIn30Map.get(id + ""));
            curr.put("saleStatus", getProductBaseSaleStatus(id));
        }
        return ResultEntity.getResultList(productBaseDao.countProductBaseInfo(para), productInfoList);
    }

    
    
    private int getProductBaseSaleStatus(int id)
        throws Exception
    {
        Map<String, Object> param = new HashMap<>();
        param.put("productBaseId", id);
        List<ProductEntity> pes = productDao.findProductByPara(param);
        if (pes == null || pes.isEmpty())
        {
            return 1;
        }
        else
        {
            //停用、已经下架的商品是否考虑
            for (Iterator<ProductEntity> iterator = pes.iterator(); iterator.hasNext();)
            {
                if (iterator.next().getType() != ProductEnum.PRODUCT_TYPE.SALE.getCode())
                {
                    iterator.remove();
                }
            }
            if (pes.isEmpty())
            {
                //基本商品关联的商品只有商城，怎么算？？？
                return -1;
            }
            else
            {
                return 2;              
            }
        }
    }

    @Override
    public ProductBaseEntity queryProductBaseById(int editId)
        throws Exception
    {
        
        return productBaseDao.queryProductBaseById(editId);
    }
    
    @Override
    public int updateProductBase(ProductBaseEntity product, List<Map<String, Object>> mobileDetailsImageList, int saveType)
        throws Exception
    {
        // 更新商品基本信息
        logger.info("开始更新商品信息: ");
        product.setImage1(adjustImageSize(product.getImage1(), ImageUtil.getSuffix(ImageTypeEnum.v1product.ordinal())));
        product.setImage2(adjustImageSize(product.getImage2(), ImageUtil.getSuffix(ImageTypeEnum.v1product.ordinal())));
        product.setImage3(adjustImageSize(product.getImage3(), ImageUtil.getSuffix(ImageTypeEnum.v1product.ordinal())));
        product.setImage4(adjustImageSize(product.getImage4(), ImageUtil.getSuffix(ImageTypeEnum.v1product.ordinal())));
        product.setImage5(adjustImageSize(product.getImage5(), ImageUtil.getSuffix(ImageTypeEnum.v1product.ordinal())));
        product.setMediumImage(adjustImageSize(product.getMediumImage(), ImageUtil.getSuffix(ImageTypeEnum.v1brandProduct.ordinal())));
        product.setSmallImage(adjustImageSize(product.getSmallImage(), ImageUtil.getSuffix(ImageTypeEnum.v1cartProduct.ordinal())));
        if (product.getSellerDeliverAreaTemplateId() == 0)
        {
            product.setDeliverAreaType(1);
            product.setDeliverAreaDesc("");
        }
        productBaseDao.updateProductBase(product);
        logger.info("更新商品信息成功: ");
        Map<String, Object> paraMap = new HashMap<String, Object>();
        List<Integer> mobileDetailIds = new ArrayList<Integer>();
        paraMap.put("productId", product.getId());
        byte order = 22;
        for (Map<String, Object> curr : mobileDetailsImageList)
        {
            // 最多允许上传22张
            if (order < 0)
            {
                break;
            }
            String content = curr.get("content") + "";
            int id = Integer.valueOf(curr.get("id") + "");          
            if (id == 0)
            {                
            }
            else
            {
                mobileDetailIds.add(id);
                if ("".equals(content))
                {
                    // 删除
                    productBaseDao.deleteProductBaseMobileDetail(id);
                    logger.debug("删除MobileDetail信息成功");
                    
                }
            }
            order--;
        }
        if (mobileDetailIds.size() > 0)
        {
            paraMap.put("idList", mobileDetailIds);
            productBaseDao.deleteProductBaseMobileDetailIdInList(paraMap);
        }
        
        // 更新关联基本商品的特卖商品信息
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("brandId", product.getBrandId());
        map.put("sellerId", product.getSellerId());
        if (StringUtils.isEmpty(product.getCode()))
        {
            throw new Exception("商品编码为空");
        }
        map.put("code", product.getCode());
        map.put("barcode", StringUtils.isEmpty(product.getBarcode()) ? "" : product.getBarcode());
        map.put("netVolume", StringUtils.isEmpty(product.getNetVolume()) ? "" : product.getNetVolume());
        map.put("placeOfOrigin", StringUtils.isEmpty(product.getPlaceOfOrigin()) ? "" : product.getPlaceOfOrigin());
        map.put("manufacturerDate", StringUtils.isEmpty(product.getManufacturerDate()) ? "" : product.getManufacturerDate());
        map.put("storageMethod", StringUtils.isEmpty(product.getStorageMethod()) ? "" : product.getStorageMethod());
        map.put("durabilityPeriod", StringUtils.isEmpty(product.getDurabilityPeriod()) ? "" : product.getDurabilityPeriod());
        map.put("peopleFor", StringUtils.isEmpty(product.getPeopleFor()) ? "" : product.getPeopleFor());
        map.put("foodMethod", StringUtils.isEmpty(product.getFoodMethod()) ? "" : product.getFoodMethod());
        map.put("useMethod", StringUtils.isEmpty(product.getUseMethod()) ? "" : product.getUseMethod());
        map.put("baseId", product.getId());
        if (saveType == 2)
        {
            //同步基本商品主图到特卖和商城商品
            map.put("image1", product.getImage1());
            map.put("image2", product.getImage2());
            map.put("image3", product.getImage3());
            map.put("image4", product.getImage4());
            map.put("image5", product.getImage5());
            productBaseDao.updateProductCommonImage(product.getId(), product.getMediumImage(), product.getSmallImage());
        }
        productBaseDao.updateRelationProductByPara(map);
        // 更新分类信息
        boolean isUpdateCategory = false;
        if (isUpdateCategory)
        {
            List<Integer> categoryIdList = new ArrayList<Integer>();
            if (categoryIdList.size() > 0)
            {
                Map<String, Object> mp = new HashMap<String, Object>();
                mp.put("productBaseId", product.getId());
                mp.put("idList", categoryIdList);
            }
            
            // 更新关联的商品分类信息
            List<Integer> productIdList = productBaseDao.findProductIdByProductBaseId(product.getId(), ProductEnum.PRODUCT_TYPE.MALL.getCode());
            Map<String, Object> mp = new HashMap<String, Object>();
            mp.put("productBaseId", product.getId());
            
            List<Map<String, Object>> productCategoryList = new ArrayList<Map<String, Object>>();
            
        }
        
        // 更新配送地区
        List<Integer> productIdList = new ArrayList<Integer>();
        productIdList.add(product.getId());
        productBaseDao.deleteRelationProductBaseDeliverArea(productIdList);
        int templateId = product.getSellerDeliverAreaTemplateId();
        Map<String, Object> mp = new HashMap<String, Object>();
        mp.put("sellerDeliverAreaTemplateId", templateId);        
        mp.put("isExcept", 0);       
        mp.put("isExcept", 1);       
        return 1;
    }
    
    @Override
    public int saveProductBase(ProductBaseEntity product, List<Map<String, Object>> mobileDetailsImageList)
        throws Exception
    {
        // 插入商品
        logger.debug("开始插入商品基本信息: ");
        product.setImage1(adjustImageSize(product.getImage1(), ImageUtil.getSuffix(ImageTypeEnum.v1product.ordinal())));
        product.setImage2(adjustImageSize(product.getImage2(), ImageUtil.getSuffix(ImageTypeEnum.v1product.ordinal())));
        product.setImage3(adjustImageSize(product.getImage3(), ImageUtil.getSuffix(ImageTypeEnum.v1product.ordinal())));
        product.setImage4(adjustImageSize(product.getImage4(), ImageUtil.getSuffix(ImageTypeEnum.v1product.ordinal())));
        product.setImage5(adjustImageSize(product.getImage5(), ImageUtil.getSuffix(ImageTypeEnum.v1product.ordinal())));
        product.setMediumImage(adjustImageSize(product.getMediumImage(), ImageUtil.getSuffix(ImageTypeEnum.v1brandProduct.ordinal())));
        product.setSmallImage(adjustImageSize(product.getSmallImage(), ImageUtil.getSuffix(ImageTypeEnum.v1cartProduct.ordinal())));
        product.setAvailableStock(product.getTotalStock());
        productBaseDao.saveProductBase(product);
        
        logger.debug("插入商品基本信息成功: ");

        byte order = 22;
        for (Map<String, Object> map : mobileDetailsImageList)
        {
            // 图片最多允许上传22张
            if (order < 0)
            {
                break;
            }
            order--;
        }

        // 设置配送地区
        if (product.getSellerDeliverAreaTemplateId() > 0)
        {
            int templateId = product.getSellerDeliverAreaTemplateId();
            Map<String, Object> mp = new HashMap<String, Object>();
            mp.put("sellerDeliverAreaTemplateId", templateId);            
            mp.put("isExcept", 0);  
            mp.put("isExcept", 1);
        }
        return 1;
    }
    
    /**
     * 修改所需图片的尺寸
     * 
     * @param imageUrl
     * @param postfix
     * @return
     */
    private String adjustImageSize(String imageUrl, String postfix)
    {
    	return imageUrl;
    }
    /**
     * 复制商品
     */
    @Override
    public ResultEntity copyProduct(int id, String code)
        throws Exception
    {
        
        ProductBaseEntity src = productBaseDao.queryProductBaseById(id);
        if (src == null)
        {
            return ResultEntity.getFailResult(String.format("ID=%d的基本商品不存在", id));
        }
        
        if (StringUtils.isEmpty(code))
        {
            return ResultEntity.getFailResult("商品编码不能为空");
        }
        
        //判断商品同一个商家是否存在相同编码，因为之前的老数据存在较多重复，故不能用建唯一约束
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("sellerId", src.getSellerId());
        param.put("code", code);
        List<Integer> idList = productBaseDao.checkCodeAndBarCode(param);
        if (!idList.isEmpty())
        {
            return ResultEntity.getFailResult(String.format("编码=%s的商品已经存在", code));
        }
        
        if (code.indexOf("%") > -1)
        {
            String suffix = code.substring(code.lastIndexOf("%") + 1);
            if (!suffix.matches("^\\d+$"))
            {
                return ResultEntity.getFailResult("商品编码含有%时，%后面必须是数字");
            }
        }
        
        logger.info(String.format("开始从基本商品Id=%d复制信息...>>>", id));
        
        //复制基本信息
        src.setId(0);
        src.setCode(code);
        src.setRemark("从商品:" + id + " 复制而来");
        src.setCreateTime(DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
        src.setTotalStock(0);
        src.setSaleStock(0);
        src.setMallStock(0);
        src.setDistributionStock(0);
        src.setAvailableStock(0);
        productBaseDao.saveProductBase(src);
        return ResultEntity.getSuccessResult(src.getId());
    }
    
    @Override
    public List<ProductBaseEntity> queryAllProductBase(Map<String, Object> para)
        throws Exception
    {
        List<ProductBaseEntity> list = productBaseDao.queryAllProductBase(para);
        return list == null ? new ArrayList<ProductBaseEntity>() : list;
    }
    
    @Override
    public int forAvailable(Map<String, Object> para)
        throws Exception
    {
        return productBaseDao.forAvailable(para);
    }
    
    @Override
    public List<Map<String, Object>> querySaleProductInfoByBaseId(int id)
        throws Exception
    {
        Map<String, Object> para = new HashMap<String, Object>();
        para.put("id", id);
        List<Map<String, Object>> resultList = productBaseDao.querySaleProductInfoByBaseId(para);
        if (resultList != null)
        {
            for (Map<String, Object> map : resultList)
            {
                map.put("type", "特卖商品");
            }
        }
        else
        {
            resultList = new ArrayList<Map<String, Object>>();
        }
        return resultList;
    }
    
    @Override
    public String adjustStock(int productBaseId, int productId, int productType, int changeStock)
        throws Exception
    {
        ProductBaseEntity base = productBaseDao.findProductBaseByIdForUpdate(productBaseId);
        if (base == null)
        {
            return JSON.toJSONString(ResultEntity.getFailResult("基本商品不存在"));
        }
        if (base.getAvailableStock() - changeStock < 0)
        {
            return JSON.toJSONString(ResultEntity.getFailResult("基本商品可用库存不足"));
        }
        
        ProductCountEntity pce = productDao.findProductCountByProductId(productId);
        if (pce == null)
        {
            return JSON.toJSONString(ResultEntity.getFailResult(ProductEnum.PRODUCT_TYPE.getDescByCode(productType) + "不存在"));
        }
        if (pce.getStock() + changeStock < pce.getLock())
        {
            return JSON.toJSONString(ResultEntity.getFailResult("调整的库存数量必须大于或等于可用库存"));
        }
        
        //先调商品库存，再条基本商品库存，productType=1需要更新productBase中的sale_stock,productType=2需要更新productBase中的mall_stock
        if (productDao.updateProductStock(productId, pce.getStock(), changeStock) > 0)
        {
            int status = 0;
            Map<String, Object> para = new HashMap<>();
            para.put("changeStock", changeStock);
            para.put("oldAvailableStock", base.getAvailableStock());
            para.put("id", productBaseId);
            if (productType == ProductEnum.PRODUCT_TYPE.SALE.getCode())
            {
                status = productBaseDao.adjustSaleStock(para);
            }
            else if (productType == ProductEnum.PRODUCT_TYPE.MALL.getCode())
            {
                status = productBaseDao.adjustMallStock(para);
            }
            if (status > 0)
            {
                return JSON.toJSONString(ResultEntity.getSuccessResult());
            }
            else
            {
                return JSON.toJSONString(ResultEntity.getFailResult("库存调整失败，请稍后再试"));
            }
        }
        else
        {
            return JSON.toJSONString(ResultEntity.getFailResult("库存调整失败，请稍后再试"));
        }
    }
    
    @Override
    public int addTotalStock(Map<String, Object> map)
        throws Exception
    {
        return productBaseDao.addTotalStock(map);
    }
    
    @Override
    public int findMaxProductId()
        throws Exception
    {
        
        int id = productBaseDao.findMaxProductId();
        return id + 1;
    }
    
    @Override
    public List<Integer> checkCodeAndBarCode(String code)
        throws Exception
    {
        Map<String, Object> para = new HashMap<String, Object>();
        para.put("code", code);
        return productBaseDao.checkCodeAndBarCode(para);
    }
    
    @Override
    public Map<String, Object> findJsonProductInfoBybaseId(Map<String, Object> para)
        throws Exception
    {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        List<Map<String, Object>> reList = productBaseDao.querySaleProductInfoByBaseId(para);
        int total = 0;
        
        for (Map<String, Object> map : reList)
        {
            map.put("type", (int)map.get("typeCode") == 1 ? "特卖商品" : "商城商品");
            map.put("available", Integer.valueOf(map.get("stock") + "").intValue() - Integer.valueOf(map.get("lock") + "").intValue());
            map.put("isAvailable", (int)map.get("isAvailable") == 1 ? "可用" : "停用");
            if ((int)map.get("typeCode") == 1)
            {
                map.put("time", (Timestamp)map.get("startTime") + "~" + (Timestamp)map.get("endTime"));
            }
            else
            {
                map.put("time", "-");
            }
        }
        total = productBaseDao.countSaleProductInfoByBaseId(para);
        resultMap.put("rows", reList);
        resultMap.put("total", total);
        return resultMap;
    }
    
    @Override
    public int checkIsInUse(int id)
        throws Exception
    {
        return productBaseDao.checkIsInUse(id);
    }
    
    @Override
    public int checkBatchUpdateProductCostPrice(String productBaseId, String submitType, String wholesalePrice, String deduction, String proposalPrice, String selfPurchasePrice,
        List<Map<String, Object>> simulationList)
        throws Exception
    {
        Map<String, Object> result = new HashMap<String, Object>();
        int status = 0;
        String msg = "成功";
        
        ProductBaseEntity pb = null;
        if (StringUtils.isNumeric(productBaseId))
        {
            pb = productBaseDao.queryProductBaseById(Integer.parseInt(productBaseId));
        }
        if (pb == null)
        {
            msg = "商品id不存在";
        }
        else if (!"供货价".equals(submitType) && !"扣点".equals(submitType) && !"自营采购价".equals(submitType))
        {
            msg = "结算类型不存在";
        }
        else if ("扣点".equals(submitType) && !Pattern.matches("[0-9]+(\\.?)[0-9]*", deduction))
        {
            msg = "扣点不正确";
        }
        else if ("扣点".equals(submitType) && !Pattern.matches("[0-9]+(\\.?)[0-9]*", proposalPrice))
        {
            msg = "建议价不正确";
        }
        else if ("供货价".equals(submitType) && !Pattern.matches("[0-9]+(\\.?)[0-9]*", wholesalePrice))
        {
            msg = "供货价不正确";
        }
        else if ("自营采购价".equals(submitType) && !Pattern.matches("[0-9]+(\\.?)[0-9]*", selfPurchasePrice))
        {
            msg = "采购价不正确";
        }
        else
        {
            status = 1;
            // 覆盖or新增
            if (pb.getSubmitType() == 1 && pb.getWholesalePrice() == 0)
            {
                msg = "新增";
            }
            else
            {
                msg = "覆盖";
            }
        }
        result.put("status", status == 1 ? "成功" : "失败");
        result.put("msg", msg);
        result.put("productBaseId", productBaseId);
        result.put("submitType", submitType);
        result.put("wholesalePrice", wholesalePrice);
        result.put("deduction", deduction);
        result.put("proposalPrice", proposalPrice);
        result.put("selfPurchasePrice", selfPurchasePrice);
        simulationList.add(result);
        return status;
    }
    
    @Override
    public int saveBatchUpdateProductCostPrice(String productBaseId, String submitType, String wholesalePrice, String deduction, String proposalPrice, String selfPurchasePrice,
        List<Map<String, Object>> confirmList)
        throws Exception
    {
        Map<String, Object> para = new HashMap<String, Object>();
        para.put("id", productBaseId);
        if ("扣点".equals(submitType))
        {
            para.put("submitType", 2);
            para.put("deduction", deduction);
            para.put("proposalPrice", proposalPrice);
        }
        else if ("供货价".equals(submitType))
        {
            para.put("submitType", 1);
            para.put("wholesalePrice", wholesalePrice);
        }
        else if ("自营采购价".equals(submitType))
        {
            para.put("submitType", 3);
            para.put("selfPurchasePrice", selfPurchasePrice);
        }
        int status = productBaseDao.updateProductBaseCost(para);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("status", status == 1 ? "成功" : "失败");
        result.put("msg", "");
        result.put("productBaseId", productBaseId);
        result.put("submitType", submitType);
        result.put("wholesalePrice", wholesalePrice);
        result.put("deduction", deduction);
        result.put("proposalPrice", proposalPrice);
        result.put("selfPurchasePrice", selfPurchasePrice);
        confirmList.add(result);
        return status;
    }
    
    @Override
    public Map<String, Object> jsonQualityPromiseInfo(Map<String, Object> para)
        throws Exception
    {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        List<Map<String, Object>> reList = productBaseDao.findAllQualityPromise(para);
        int total = 0;
        for (Map<String, Object> map : reList)
        {
            int type = Integer.valueOf(map.get("type") + "").intValue();
            map.put("typeDesc", ProductEnum.QUALITY_PROMISE_TYPE.getDescByCode(type));
        }
        total = productBaseDao.countQualityPromise(para);
        resultMap.put("rows", reList);
        resultMap.put("total", total);
        return resultMap;
    }
    
    @Override
    public int saveOrUpdateQualityPromise(Map<String, Object> para)
        throws Exception
    {
        int id = (int)para.get("id");
        /*
         * Map<String, Object> imageMap = ImageUtil.getProperty(para.get("image") + ""); int width =
         * Integer.valueOf(imageMap.get("width") + "").intValue(); int height = Integer.valueOf(imageMap.get("height") +
         * "").intValue(); if (width != 333 && height != 333) { return 2; }
         */
        boolean isExist = productBaseDao.IsExistQualityPromise(para);
        if (isExist)
        {
            return 3;
        }
        if (id == 0)
        {
            return productBaseDao.insertQualityPromise(para);
        }
        else
        {
            return productBaseDao.updateQualityPromise(para);
        }
    }
    
    
    @Override
    public Map<String, Object> findAllExpireProduct(Map<String, Object> para)
        throws Exception
    {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        List<Map<String, Object>> productInfoList = productBaseDao.findAllExpireProduct(para);
        int total = 0;
        if (productInfoList.size() > 0)
        {
            for (Map<String, Object> map : productInfoList)
            {
                int remainDay = Integer.parseInt(map.get("remainDay") == null ? "0" : map.get("remainDay") + "");
                if (remainDay < 0)
                {
                    map.put("remainDay", "已过期" + (-remainDay) + "天");
                }
                map.put("expireDate", map.get("expireDate").toString());
            }
            total = productBaseDao.countAllExpireProduct(para);
        }
        resultMap.put("rows", productInfoList);
        resultMap.put("total", total);
        return resultMap;
    }
    
    @Override
    public int findAllottedStockById(int productBaseId)
        throws Exception
    {
        return productBaseDao.findAllottedStockById(productBaseId);
    }
    
    @Override
    public List<Map<String, Object>> findProductBaseRelationInfoByProductBaseId(List<Integer> productBaseIds)
        throws Exception
    {
        return productBaseDao.findProductBaseRelationInfoByProductBaseId(productBaseIds);
    }
    
    @Override
    public int insertWholesalePriceUpdatelog(int id, float oldPrice, float newPrice)
        throws Exception
    {
        Map<String, Object> params = new HashMap<>();
        params.put("productBaseId", id);
        params.put("oldPrice", oldPrice);
        params.put("newPrice", newPrice);
        //params.put("username", commonService.getCurrentRealName());
        return productBaseDao.insertWholesalePriceUpdatelog(params);
    }
    
    @Override
    public ResultEntity jsonWholeSalePriceHistory(Map<String, Object> para)
        throws Exception
    {
        return null;//ResultEntity.getResultList(productBaseDao.countWholeSalePriceLogByPara(para), productBaseDao.findWholeSalePriceLogByPara(para));
    }
    
    @Override
    public List<Map<String, Object>> findProductBaseIdBySellerProductId(List<Object> sellerProductIds)
        throws Exception 
    {
        return productBaseDao.findProductBaseIdBySellerProductId(sellerProductIds);
    }

    @Override
    public Object previewPicture(int id) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("status", 1);
        ProductBaseEntity pbe = productBaseDao.queryProductBaseById(id);
        if (pbe == null) {
            resultMap.put("status", 0);
            return resultMap;
        }

        List<Map<String, Object>> mainImages = new ArrayList<>();
        if (StringUtils.isNotEmpty(pbe.getImage1())) {
            Map<String, Object> map = ImageUtil.getProperty(pbe.getImage1());
            map.put("url", pbe.getImage1());
            mainImages.add(map);
        }
        if (StringUtils.isNotEmpty(pbe.getImage2())) {
            Map<String, Object> map = ImageUtil.getProperty(pbe.getImage2());
            map.put("url", pbe.getImage2());
            mainImages.add(map);
        }
        if (StringUtils.isNotEmpty(pbe.getImage3())) {
            Map<String, Object> map = ImageUtil.getProperty(pbe.getImage3());
            map.put("url", pbe.getImage3());
            mainImages.add(map);
        }
        if (StringUtils.isNotEmpty(pbe.getImage4())) {
            Map<String, Object> map = ImageUtil.getProperty(pbe.getImage4());
            map.put("url", pbe.getImage4());
            mainImages.add(map);
        }
        if (StringUtils.isNotEmpty(pbe.getImage5())) {
            Map<String, Object> map = ImageUtil.getProperty(pbe.getImage5());
            map.put("url", pbe.getImage5());
            mainImages.add(map);
        }

        List<Map<String, Object>> detailImages = new ArrayList<>();
        resultMap.put("mainImages", mainImages);
        resultMap.put("detailImages", detailImages);
        return resultMap;
    }


    @Override
    public Map<String, Object> findHistorySalesVolumeById(int id, String startTime, String endTime)
        throws Exception
    {
        Map<String, Object> para = new HashMap<>();
        para.put("productBaseId", id);
        if (StringUtils.isNotEmpty(startTime))
        {
            para.put("startTime", DateTime.parse(startTime, DateTimeFormat.forPattern("yyyy-MM-dd")).withHourOfDay(0).toString("yyyyMMddHH"));
        }
        if (StringUtils.isNotEmpty(endTime))
        {
            para.put("endTime", DateTime.parse(endTime, DateTimeFormat.forPattern("yyyy-MM-dd")).withHourOfDay(23).toString("yyyyMMddHH"));
        }
        
        int totalSales = 0;
        List<Map<String, Object>> salesVolumes = productBaseDao.findProductBaseHistorySalesVolume(para);
        TreeMap<Integer, Integer> groupByDay = new TreeMap<>();
        for (Map<String, Object> it : salesVolumes)
        {
            Integer day = Integer.parseInt(it.get("sales_day_hour").toString().substring(0, 8));
            int salesVolume = Integer.parseInt(it.get("sales_volume").toString());
            if (groupByDay.get(day) == null)
            {
                groupByDay.put(day, salesVolume);
            }
            else
            {
                groupByDay.put(day, groupByDay.get(day) + salesVolume);
            }
            totalSales += salesVolume;
        }
        
        List<String> labels = new ArrayList<>();
        for (Integer day : groupByDay.keySet())
        {
            labels.add(DateTime.parse(day + "", DateTimeFormat.forPattern("yyyyMMdd")).toString("yyyy年MM月dd日"));
        }
        if (labels.isEmpty())
        {
            DateTime start = DateTime.now().minusDays(30);
            DateTime end = DateTime.now();
            while (end.isAfter(start))
            {
                labels.add(start.toString("yyyy年MM月dd日"));
                start = start.plusDays(1);
            }
        }
        List<Integer> data = new ArrayList<>(groupByDay.values());
        Map<String, Object> result = new HashMap<>();
        result.put("productName", productBaseDao.queryProductBaseById(id).getName());
        result.put("labels", labels);
        result.put("data", data);
        result.put("totalSales", totalSales);
        result.put("begin", labels.get(0));
        result.put("end", labels.get(labels.size() - 1));
        return result;
    }
}
