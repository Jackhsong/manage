package com.ygg.webapp.util;

import org.apache.commons.lang.StringUtils;

/**
 * 快递枚举
 */
public enum KdCompanyEnum
{    
    
    /** 申通快递 */
    SHENTONG("申通快递", 3, "shentong", "shentong","STO", ""),
    
    /** 包裹/平邮 */
    YOUZHENGGUONEI("包裹/平邮(邮政国内小包)", 3, "youzhengguonei", "youzhengguonei", "YZPY",""),
    
    /** 圆通速递 */
    YUANTONG("圆通速递", 3, "yuantong", "yuantong","YTO", ""),
    
    /** 中通速递 */
    ZHONGTONG("中通速递", 3, "zhongtong", "zhongtong","ZTO", ""),
    
    /** 顺丰速运 */
    SHUNFENG("顺丰速运", 3, "shunfeng", "shunfeng","SF", ""),

    /** 韵达快运 */
    YUNDA("韵达快运", 3, "yunda", "yunda","YD", ""),

    /** EMS */
    EMS("EMS", 3, "ems", "EMS", "EMS",""),
    
    /** 德邦物流 */
    DEBANGWULIU("德邦物流", 3, "debangwuliu", "debangwuliu", "DBL",""),
    
    /** 全峰快递 */
    QUANFENGKUAIDI("全峰快递", 3, "quanfengkuaidi", "quanfengkuaidi","QFKD", "");

    /** 快递公司名称 */
    final String companyName;
    
    /** 支持的快递服务商：0：都不支持，1：快递100支持，2：快递吧支持，3：都支持*/
    final int serviceType;
    
    /** 快递100编码*/
    final String kd100Code;
    
    /** 快递吧编码*/
    final String kd8Code;
    
    /** 快递鸟编码*/
    final String kdnCode;
    
    /** 不支持快递detail*/
    final String queryURL;
    
    private KdCompanyEnum(String companyName, int serviceType, String kd100Code, String kd8Code, String kdnCode, String queryURL)
    {
        this.companyName = companyName;
        this.serviceType = serviceType;
        this.kd100Code = kd100Code;
        this.kd8Code = kd8Code;
        this.kdnCode = kdnCode;
        this.queryURL = queryURL;
    }
    
    public String getCompanyName()
    {
        return companyName;
    }
    
    public String getQueryURL()
    {
        return queryURL;
    }
    
    public static String getCompanyNameByKd100Code(String kd100Code)
    {
        if (StringUtils.isEmpty(kd100Code))
        {
            return "";
        }
        for (KdCompanyEnum e : KdCompanyEnum.values())
        {
            if (e.kd100Code.equals(kd100Code))
            {
                return e.companyName;
            }
        }
        return "";
    }
    
    public int getServiceType()
    {
        return serviceType;
    }
    
    public String getKd100Code()
    {
        return kd100Code;
    }
    
    public String getKd8Code()
    {
        return kd8Code;
    }
    
    public String getKdnCode()
    {
        return kdnCode;
    }
    public static String getKd100CodeByCompanyName(String companyName)
    {
        if (StringUtils.isEmpty(companyName))
        {
            return "";
        }
        for (KdCompanyEnum e : KdCompanyEnum.values())
        {
            if (companyName.equals(e.companyName))
            {
                return e.kd100Code;
            }
        }
        return "";
    }
    
    public static String getKd8CodeByCompanyName(String companyName)
    {
        if (StringUtils.isEmpty(companyName))
        {
            return "";
        }
        for (KdCompanyEnum e : KdCompanyEnum.values())
        {
            if (companyName.equals(e.companyName))
            {
                return e.kd8Code;
            }
        }
        return "";
    }
    
    public static String getKdnCodeByCompanyName(String companyName)
    {
        if (StringUtils.isEmpty(companyName))
        {
            return "";
        }
        for (KdCompanyEnum e : KdCompanyEnum.values())
        {
            if (companyName.equals(e.companyName))
            {
                return e.kdnCode;
            }
        }
        return "";
    }
    
    public static KdCompanyEnum getKdCompanyEnumByCompanyName(String companyName)
    {
        if (StringUtils.isEmpty(companyName))
        {
            return null;
        }
        for (KdCompanyEnum e : KdCompanyEnum.values())
        {
            if (companyName.equals(e.companyName))
            {
                return e;
            }
        }
        return null;
    }
    
    public static String getCompanyNameByKd8Code(String channel)
    {
        if (StringUtils.isEmpty(channel))
        {
            return null;
        }
        for (KdCompanyEnum e : KdCompanyEnum.values())
        {
            if (channel.equals(e.kd8Code))
            {
                return e.companyName;
            }
        }
        return null;
    }
    
    public static String getCompanyNameByKdnCode(String channel)
    {
        if (StringUtils.isEmpty(channel))
        {
            return null;
        }
        for (KdCompanyEnum e : KdCompanyEnum.values())
        {
            if (channel.equals(e.kdnCode))
            {
                return e.companyName;
            }
        }
        return null;
    }
    
}