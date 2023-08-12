package com.wangpan.entity;

import java.awt.image.BufferedImage;
import java.util.Random;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;

/**
 * @author fangyixin
 * @date 2023/8/11 16:36
 */
public class CreateImageCode {
    //图片长宽
    private int width = 160;
    private int height = 40;

    //验证码字符个数
    private int codeCount = 4;
    //验证码干扰个数
    private int lineCount = 20;
    //验证码
    private String code = null;
    //验证码图片Buffer
    private BufferedImage buffImg = null;
    Random random = new Random();

    public CreateImageCode() {
        creatImage();
    }

    public CreateImageCode(int width, int height) {
        this.width = width;
        this.height = height;
        creatImage();
    }

    public CreateImageCode(int width, int height, int codeCount) {
        this.width = width;
        this.height = height;
        this.codeCount = codeCount;
        creatImage();
    }

    public CreateImageCode(int width, int height, int codeCount, int lineCount) {
        this.width = width;
        this.height = height;
        this.codeCount = codeCount;
        this.lineCount = lineCount;
        creatImage();
    }

    //生成图片
    private void creatImage() {
        int fontWidth = width / codeCount; //字体宽度
        int fontHeight = height - 5; //字体高度
        int codeY = height - 8;

        //图像buffer
        buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = buffImg.getGraphics();
        //设置背景色
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, width, height);
        //设置字体
        Font font = new Font("Fixedsys", Font.BOLD, fontHeight);
        g.setFont(font);

        //设置干扰线
        for (int i = 0; i < lineCount; i++) {
            int xs = random.nextInt(width);
            int ys = random.nextInt(height);
            int xe = xs + random.nextInt(width);
            int ye = ys + random.nextInt(height);
            g.setColor(getRandColor(1, 255));
            g.drawLine(xs, ys, xe, ye);
        }

        //添加噪点
        float yawpRate = 0.01f; //噪声率
        int area = (int) (yawpRate * width * height);
        for (int i = 0; i < area; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            buffImg.setRGB(x, y, random.nextInt(255));
        }

        String str1 = randomStr(codeCount);//得到随机字符
        this.code = str1;
        for (int i = 0; i < codeCount; i++) {
            String strRand = str1.substring(i, i + 1);
            g.setColor(getRandColor(1, 255));
            g.drawString(strRand, i * fontWidth + 3, codeY);
        }
    }

    //得到随机字符
    private String randomStr(int n) {
        String str1 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz123456789";
        String str2 = "";
        int len = str1.length() - 1;
        double r;
        for (int i = 0; i < n; i++) {
            r = (Math.random()) * len;
            str2 += str1.charAt((int) r);
        }
        return str2;
    }

    //给定范围得到随机颜色
    private Color getRandColor(int fc, int bc) {
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    public void write(OutputStream sos) throws IOException {
        ImageIO.write(buffImg,"png",sos);
        sos.close();
    }

    public BufferedImage getBuffImg(){
        return buffImg;
    }

    public String getCode(){return code.toLowerCase();}

}