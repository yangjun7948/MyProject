package com.zykj.file.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IORuntimeException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;

/**
 * @author junyang
 * @date 2019/9/9
 */
public class ImageUtil {
  /**
   * 生成水印
   * @param inputStream 输入图片流
   * @param x 水印X坐标
   * @param y 水印Y坐标
   * @param markWidth
   * @param markheight
   * @return
   */
  public static BufferedImage markImageWithDate(InputStream inputStream,Integer x,Integer y,Integer markWidth , Integer markheight) throws IOException {
    String dateStr = DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss");
    Color fontColor = new Color(255,255,255);
    Color backColor = new Color(0,0,0);
    Font font= new java.awt.Font("宋体", java.awt.Font.BOLD,14);

    Image srcImg = ImageIO.read(inputStream);
    BufferedImage buffImg = new BufferedImage(srcImg.getWidth(null),
            srcImg.getHeight(null), BufferedImage.TYPE_INT_RGB);
    // 1、得到画笔对象
    Graphics2D g = buffImg.createGraphics();
    // 2、设置对线段的锯齿状边缘处理
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.drawImage(srcImg.getScaledInstance(srcImg.getWidth(null),srcImg.getHeight(null),
            Image.SCALE_SMOOTH), 0, 0,null);
    //3、获取水印时间戳
    BufferedImage bufImage =ImageUtil.createImage(dateStr,font,backColor,fontColor);
    // 4 设置透明度
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.4f));
    // 5 绘制水印
    g.drawImage(bufImage, x,y,markWidth,markheight,null);
    // 6、释放资源
    g.dispose();
    // 7 生成图片
    return buffImg;

  }

  /**
   * 创建图片
   * @param str 图片文字
   * @param font 字体
   * @param backgroundColor 背景色
   * @param fontColor 字体颜色
   * @return BufferImage
   * @throws IORuntimeException
   */
  public static BufferedImage createImage(String str, Font font, Color backgroundColor, Color fontColor) throws IORuntimeException {
    Rectangle2D r = font.getStringBounds(str, new FontRenderContext(AffineTransform.getScaleInstance(1.0D, 1.0D), false, false));
    int unitHeight = (int)Math.floor(r.getHeight());
    int width = (int)Math.round(r.getWidth()) + 1;
    int height = unitHeight + 3;
    BufferedImage image = new BufferedImage(width, height, 4);
    Graphics g = image.getGraphics();
    g.setColor(backgroundColor);
    g.fillRect(0, 0, width, height);
    g.setColor(fontColor);
    g.setFont(font);
    g.drawString(str, 0, font.getSize());
    g.dispose();
    return image;
  }
}
