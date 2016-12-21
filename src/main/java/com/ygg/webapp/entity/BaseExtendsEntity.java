package com.ygg.webapp.entity;

/**
 * @author wuhy
 * @date 创建时间：2016年5月16日 下午7:34:19
 */
public abstract class BaseExtendsEntity
{
    public static final int NOT_EXIST_ACCOUNT_ID = 0;
    
    private int start;
    
    public int getStart()
    {
        return start;
    }
    
    public void setStart(int start)
    {
        this.start = start;
    }
    
    public int getMax()
    {
        return max;
    }
    
    public void setMax(int max)
    {
        this.max = max;
    }
    
    private int max;
    
}
