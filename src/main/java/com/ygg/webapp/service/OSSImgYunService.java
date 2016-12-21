package com.ygg.webapp.service;

import java.io.IOException;
import java.util.Map;

/**
 * 阿里的oss上传图片
 */
public interface OSSImgYunService
{

    public Map<String, String> uploadFile(byte[] file, String fileName)
            throws IOException;

}
