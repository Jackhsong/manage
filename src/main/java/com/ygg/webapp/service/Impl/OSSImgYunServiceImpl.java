package com.ygg.webapp.service.Impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import com.ygg.webapp.service.OSSImgYunService;
import com.ygg.webapp.util.FileOperateUtil;

/**
 * ali OSSYun图片存储服务
 *
 */
@Service("ossImgYunService")
public class OSSImgYunServiceImpl implements OSSImgYunService{
    
    //private String accessKeyId = UtilProperties.getInstance("yggCommon.properties").getProperties("ali_oss_accessKeyId");
    private String accessKeyId="rvCyNiasyIq1JGHD";
    //private String accessKeySecret = UtilProperties.getInstance("yggCommon.properties").getProperties("ali_oss_accessKeySecret");
    private String accessKeySecret="VzKWi0s3y3CXSzqKr7b8E1NBmYGNgo";
    //private String bucketName = UtilProperties.getInstance("yggCommon.properties").getProperties("ali_oss_bucketName");
    private String bucketName="bucket20160601";
    //private String domainName = UtilProperties.getInstance("yggCommon.properties").getProperties("ali_oss_domainName");
    private String domainName="bucket20160601.oss-cn-hangzhou.aliyuncs.com";
    //private String endpoint = UtilProperties.getInstance("yggCommon.properties").getProperties("ali_oss_endpoint"); // oss-cn-hangzhou-internal.aliyuncs.com
    private String endpoint="http://oss-cn-hangzhou.aliyuncs.com";
    private static OSSClient ossClient = null;
    
    /** 绑定的域名 */
    private final String URL = "http://" + bucketName + "." + domainName; // download base地址
    
    //private final String baseUrl = UtilProperties.getInstance("yggCommon.properties").getProperties("ali_oss_baseUrl");
    private final String baseUrl="http://bucket20160601.oss-cn-hangzhou.aliyuncs.com/";
    /** 根目录 */
    private static final String DIR_ROOT = "/";
    
    public OSSImgYunServiceImpl()
    {
        ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret, getClientConfiguration());
        
        if (!ossClient.doesBucketExist(bucketName))
            ossClient.createBucket(bucketName);
        // 设置Bucket ACL
        ossClient.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
    }
    
    private ClientConfiguration getClientConfiguration()
    {
        ClientConfiguration conf = new ClientConfiguration();
        // 设置HTTP最大连接数为10
        conf.setMaxConnections(10);
        // 设置TCP连接超时为5000毫秒
        conf.setConnectionTimeout(5000);
        // 设置最大的重试次数为3
        conf.setMaxErrorRetry(3);
        // 设置Socket传输数据超时的时间为30000毫秒
        conf.setSocketTimeout(30000);
        
        return conf;
    }
    
    @Override
    public Map<String, String> uploadFile(byte[] file, String fileName)
        throws IOException
    {
        
        InputStream is = new FileInputStream(FileOperateUtil.byte2File(file));
        ObjectMetadata objectMeta = new ObjectMetadata();
        objectMeta.setContentLength(file.length);
        // 可以在metadata中标记文件类型
        objectMeta.setContentType("image/jpeg");
        String key = fileName;
        String filePath = DIR_ROOT + key;
        try
        {
            ossClient.putObject(bucketName, key, is, objectMeta);
            
            return getUploadResult(true, filePath);
        }
        catch (Exception e)
        {
            return getUploadResult(false, filePath);
        }
        finally
        {
            if (is != null)
                is.close();
        }
    }
    
    private Map<String, String> getUploadResult(boolean result, String filePath)
    {
        Map<String, String> map = new HashMap<String, String>();
        if (result)
        {
            map.put("status", "success");
            map.put("url", baseUrl + filePath);
        }
        else
        {
            map.put("status", "failure");
        }
        return map;
    }


    
}
