package com.ygg.webapp.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;

import org.junit.Test;

public class VerifyCodeUtils
{
    
    public static final String VERIFY_CODES = "123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    
    public static final String VERIFY_zhongwen = "的一是在不了有和人这中大为上个国我以要他时来用们生到作地于出就分对成会可主发年动同" + "工也能下过子说产种面而方后多定行学法所民得经十三之进着等部度家电力里如水化高自二理起小物现实加量都两体制机当使点从业本"
        + "去把性好应开它合还因由其些然前外天政四日那社义事平形相全表间样与关各重新线内数正心反你明看原又么利比或但质气第向道命此变条" + "只没结解问意建月公无系军很情者最立代想已通并提直题党程展五果料象员革位入常文总次品式活设及管特件长求老头基资边流路级少图"
        + "山统接知较将组见计别她手角期根论运农指几九区强放决西被干做必战先回则任取据处队南给色光门即保治北造百规热领七海口东导器压" + "志世金增争济阶油思术极交受联什认六共权收证改清己美再采转更单风切打白教速花带安场身车例真务具万每目至达走积示议声报斗完"
        + "类八离华名确才科张信马节话米整空元况今集温传土许步群广石记需段研界拉林律叫且究观越织装影算低持音众书布复容儿须际商非验连断深" + "难近矿千周委素技备半办青省列习响约支般史感劳便团往酸历市克何除消构府称太准精值号率族维划选标写存候毛亲快效斯院查江型眼王按"
        + "格养易置派层片始却专状育厂京识适属圆包火住调满县局照参红细引听该铁价严";
    
    /**
     * 使用系统默认字符源生成验证码
     * 
     * @param verifySize 验证码长度
     * @return
     */
    public static String generateVerifyCode(int verifySize)
    {
        return generateVerifyCode(verifySize, VERIFY_CODES);
    }
    
    /**
     * 使用指定源生成验证码
     * 
     * @param verifySize 验证码长度
     * @param sources 验证码字符源
     * @return
     */
    public static String generateVerifyCode(int verifySize, String sources)
    {
        if (sources == null || sources.length() == 0)
        {
            sources = VERIFY_CODES;
        }
        int codesLen = sources.length();
        Random rand = new Random(System.currentTimeMillis());
        StringBuilder verifyCode = new StringBuilder(verifySize);
        for (int i = 0; i < verifySize; i++)
        {
            verifyCode.append(sources.charAt(rand.nextInt(codesLen - 1)));
        }
        return verifyCode.toString();
    }
    
    /**
     * 生成随机验证码文件,并返回验证码值
     * 
     * @param w
     * @param h
     * @param outputFile
     * @param verifySize
     * @return
     * @throws IOException
     */
    public static String outputVerifyImage(int w, int h, File outputFile, int verifySize)
        throws IOException
    {
        String verifyCode = generateVerifyCode(verifySize);
        outputImage(w, h, outputFile, verifyCode);
        return verifyCode;
    }
    
    /**
     * 输出随机验证码图片流,并返回验证码值
     * 
     * @param w
     * @param h
     * @param os
     * @param verifySize
     * @return
     * @throws IOException
     */
    public static String outputVerifyImage(int w, int h, OutputStream os, int verifySize)
        throws IOException
    {
        String verifyCode = generateVerifyCode(verifySize);
        outputImage(w, h, os, verifyCode);
        return verifyCode;
    }
    
    /**
     * 生成指定验证码图像文件
     * 
     * @param w
     * @param h
     * @param outputFile
     * @param code
     * @throws IOException
     */
    public static void outputImage(int w, int h, File outputFile, String code)
        throws IOException
    {
        if (outputFile == null)
        {
            return;
        }
        File dir = outputFile.getParentFile();
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        try
        {
            outputFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(outputFile);
            outputImage(w, h, fos, code);
            fos.close();
        }
        catch (IOException e)
        {
            throw e;
        }
    }
    
    /**
     * 输出指定验证码图片流
     * 
     * @param w
     * @param h
     * @param os
     * @param code
     * @throws IOException
     */
    public static void outputImage(int w, int h, OutputStream os, String code)
        throws IOException
    {
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
        ImageIO.write(image, "jpg", os);
    }
    
    public static void getVerifyImage()
        throws IOException
    {
        String verifyCode = generateVerifyCode(4);
        int w = 200, h = 80;
        
    }
    
    @Test
    public static void main(String[] args)
        throws IOException
    {
        File dir = new File("E:/verifies");
        int w = 200, h = 80;
        for (int i = 0; i < 2; i++)
        {
            String verifyCode = generateVerifyCode(4);
            File file = new File(dir, verifyCode + ".jpg");
            System.out.println(verifyCode);
            outputImage(w, h, file, verifyCode);
        }
    }
    
}
