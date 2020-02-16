package me.asu.util;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import me.asu.text.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repo.gif.AnimatedGifEncoder;

/**
 * 对图像操作的简化 API
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class ImageUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUtils.class);
    /**
     * 对一个图像进行旋转
     *
     * @param srcIm  原图像文件
     * @param taIm   转换后的图像文件
     * @param degree 旋转角度, 90 为顺时针九十度， -90 为逆时针九十度
     * @return 旋转后得图像对象
     */
    public static BufferedImage rotate(Object srcIm, File taIm, int degree) {
        BufferedImage im = ImageUtils.read(srcIm);
        BufferedImage im2 = ImageUtils.rotate(im, degree);
        ImageUtils.write(im2, taIm);
        return im2;
    }

    /**
     * 对一个图像进行旋转
     *
     * @param srcPath 原图像文件路径
     * @param taPath  转换后的图像文件路径
     * @param degree  旋转角度, 90 为顺时针九十度， -90 为逆时针九十度
     * @return 旋转后得图像对象
     */
    public static BufferedImage rotate(String srcPath, String taPath, int degree)
    throws IOException {
        File srcIm = new File(srcPath);
        if (null == srcIm) {
            throw Exceptions.makeThrow("Fail to find image file '%s'!", srcPath);
        }

        File taIm = Files.createFileIfNoExists(taPath);
        return rotate(srcIm, taIm, degree);
    }

    /**
     * 对一个图像进行旋转
     *
     * @param image  图像
     * @param degree 旋转角度, 90 为顺时针九十度， -90 为逆时针九十度
     * @return 旋转后得图像对象
     */
    public static BufferedImage rotate(BufferedImage image, int degree) {
        int iw = image.getWidth();// 原始图象的宽度
        int ih = image.getHeight();// 原始图象的高度
        int w = 0;
        int h = 0;
        int x = 0;
        int y = 0;
        degree = degree % 360;
        if (degree < 0) {
            degree = 360 + degree;// 将角度转换到0-360度之间
        }
        double ang = degree * 0.0174532925;// 将角度转为弧度

        /**
         * 确定旋转后的图象的高度和宽度
         */

        if (degree == 180 || degree == 0 || degree == 360) {
            w = iw;
            h = ih;
        } else if (degree == 90 || degree == 270) {
            w = ih;
            h = iw;
        } else {
            int d = iw + ih;
            w = (int) (d * Math.abs(Math.cos(ang)));
            h = (int) (d * Math.abs(Math.sin(ang)));
        }

        x = (w / 2) - (iw / 2);// 确定原点坐标
        y = (h / 2) - (ih / 2);
        BufferedImage rotatedImage = new BufferedImage(w, h, image.getType());
        Graphics gs = rotatedImage.getGraphics();
        gs.fillRect(0, 0, w, h);// 以给定颜色绘制旋转后图片的背景
        AffineTransform at = new AffineTransform();
        at.rotate(ang, w / 2, h / 2);// 旋转图象
        at.translate(x, y);
        AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        op.filter(image, rotatedImage);
        image = rotatedImage;
        return image;
    }

    /**
     * 自动等比缩放一个图片，并将其保存成目标图像文件<br />
     * 多余的部分，用给定背景颜色补上<br />
     * 如果参数中的宽度或高度为<b>-1</b>的话，着按照指定的高度或宽度对原图等比例缩放图片，不添加背景颜色
     * <p>
     * 图片格式支持 png | gif | jpg | bmp | wbmp
     *
     * @param srcIm   源图像文件对象
     * @param taIm    目标图像文件对象
     * @param w       宽度
     * @param h       高度
     * @param bgColor 背景颜色
     * @return 被转换前的图像对象
     * @throws java.io.IOException 当读写文件失败时抛出
     */
    public static BufferedImage zoomScale(Object srcIm, File taIm, int w, int h, Color bgColor)
    throws IOException {
        BufferedImage old = read(srcIm);
        BufferedImage im = ImageUtils.zoomScale(old, w, h, bgColor);
        write(im, taIm);
        return old;
    }

    /**
     * 自动等比缩放一个图片，并将其保存成目标图像文件<br />
     * 多余的部分，用给定背景颜色补上<br />
     * 如果参数中的宽度或高度为<b>-1</b>的话，着按照指定的高度或宽度对原图等比例缩放图片，不添加背景颜色
     * <p>
     * 图片格式支持 png | gif | jpg | bmp | wbmp
     *
     * @param srcPath 源图像路径
     * @param taPath  目标图像路径，如果不存在，则创建
     * @param w       宽度
     * @param h       高度
     * @param bgColor 背景颜色
     * @return 被转换前的图像对象
     * @throws java.io.IOException 当读写文件失败时抛出
     */
    public static BufferedImage zoomScale(String srcPath, String taPath, int w, int h,
                                          Color bgColor) throws IOException {
        File srcIm = new File(srcPath);
        if (null == srcIm) {
            throw Exceptions.makeThrow("Fail to find image file '%s'!", srcPath);
        }

        File taIm = Files.createFileIfNoExists(taPath);
        return zoomScale(srcIm, taIm, w, h, bgColor);
    }

    /**
     * 自动等比缩放一个图片，多余的部分，用给定背景颜色补上<br />
     * 如果参数中的宽度或高度为<b>-1</b>的话，着按照指定的高度或宽度对原图等比例缩放图片，不添加背景颜色
     *
     * @param im      图像对象
     * @param w       宽度
     * @param h       高度
     * @param bgColor 背景颜色
     * @return 被转换后的图像
     */
    public static BufferedImage zoomScale(BufferedImage im, int w, int h, Color bgColor) {
        if (w == -1 || h == -1) {
            return zoomScale(im, w, h);
        }

        // 检查背景颜色
        bgColor = null == bgColor ? Color.black : bgColor;
        // 获得尺寸
        int oW = im.getWidth();
        int oH = im.getHeight();
        float oR = (float) oW / (float) oH;
        float nR = (float) w / (float) h;

        int nW, nH, x, y;
        /*
         * 缩放
         */
        // 原图太宽，计算当原图与画布同高时，原图的等比宽度
        if (oR > nR) {
            nW = w;
            nH = (int) (((float) w) / oR);
            x = 0;
            y = (h - nH) / 2;
        }
        // 原图太长
        else if (oR < nR) {
            nH = h;
            nW = (int) (((float) h) * oR);
            x = (w - nW) / 2;
            y = 0;
        }
        // 比例相同
        else {
            nW = w;
            nH = h;
            x = 0;
            y = 0;
        }

        // 创建图像
        BufferedImage re = new BufferedImage(w, h, ColorSpace.TYPE_RGB);
        // 得到一个绘制接口
        Graphics gc = re.getGraphics();
        gc.setColor(bgColor);
        gc.fillRect(0, 0, w, h);
        gc.drawImage(im, x, y, nW, nH, bgColor, null);
        // 返回
        return re;
    }

    /**
     * 自动等比缩放一个图片
     *
     * @param im 图像对象
     * @param w  宽度
     * @param h  高度
     * @return 被转换后的图像
     */
    public static BufferedImage zoomScale(BufferedImage im, int w, int h) {
        // 获得尺寸
        int oW = im.getWidth();
        int oH = im.getHeight();

        int nW = w, nH = h;

        /*
         * 缩放
         */
        // 未指定图像高度，根据原图尺寸计算出高度
        if (h == -1) {
            nH = (int) ((float) w / oW * oH);
        }
        // 未指定图像宽度，根据原图尺寸计算出宽度
        else if (w == -1) {
            nW = (int) ((float) h / oH * oW);
        }

        // 创建图像
        BufferedImage re = new BufferedImage(nW, nH, ColorSpace.TYPE_RGB);
        re.getGraphics().drawImage(im, 0, 0, nW, nH, null);
        // 返回
        return re;
    }

    /**
     * 自动缩放剪切一个图片，令其符合给定的尺寸，并将其保存成目标图像文件
     * <p>
     * 图片格式支持 png | gif | jpg | bmp | wbmp
     *
     * @param srcIm 源图像文件对象
     * @param taIm  目标图像文件对象
     * @param w     宽度
     * @param h     高度
     * @return 被转换前的图像对象
     * @throws java.io.IOException 当读写文件失败时抛出
     */
    public static BufferedImage clipScale(Object srcIm, File taIm, int w, int h)
    throws IOException {
        BufferedImage old = read(srcIm);
        BufferedImage im = ImageUtils.clipScale(old, w, h);
        write(im, taIm);
        return old;
    }

    /**
     * 自动缩放剪切一个图片，令其符合给定的尺寸，并将其保存到目标图像路径
     * <p>
     * 图片格式支持 png | gif | jpg | bmp | wbmp
     *
     * @param srcPath 源图像路径
     * @param taPath  目标图像路径，如果不存在，则创建
     * @param w       宽度
     * @param h       高度
     * @return 被转换前的图像对象
     * @throws java.io.IOException 当读写文件失败时抛出
     */
    public static BufferedImage clipScale(String srcPath, String taPath, int w, int h)
    throws IOException {
        File srcIm = new File(srcPath);
        if (null == srcIm) {
            throw Exceptions.makeThrow("Fail to find image file '%s'!", srcPath);
        }

        File taIm = Files.createFileIfNoExists(taPath);
        return clipScale(srcIm, taIm, w, h);
    }

    /**
     * 根据给定的起始坐标点与结束坐标点来剪切一个图片，令其符合给定的尺寸，并将其保存成目标图像文件
     * <p>
     * 图片格式支持 png | gif | jpg | bmp | wbmp
     *
     * @param srcIm      源图像文件对象
     * @param taIm       目标图像文件对象
     * @param startPoint 起始坐标点，其值[x, y]为相对原图片左上角的坐标
     * @param endPoint   结束坐标点，其值[x, y]为相对原图片左上角的坐标
     * @return 被转换前的图像对象
     * @throws java.io.IOException 当读写文件失败时抛出
     */
    public static BufferedImage clipScale(Object srcIm, File taIm, int[] startPoint, int[] endPoint)
    throws IOException {
        // 计算给定坐标后的图片的尺寸
        int width = endPoint[0] - startPoint[0];
        int height = endPoint[1] - startPoint[1];

        BufferedImage old = read(srcIm);
        BufferedImage im = ImageUtils
                .clipScale(old.getSubimage(startPoint[0], startPoint[1], width, height), width,
                        height);

        write(im, taIm);
        return old;
    }

    /**
     * 根据给定的起始坐标点与结束坐标点来剪切一个图片，令其符合给定的尺寸，并将其保存成目标图像文件
     * <p>
     * 图片格式支持 png | gif | jpg | bmp | wbmp
     *
     * @param srcPath    源图像文件对象
     * @param taPath     目标图像文件对象
     * @param startPoint 起始坐标点，其值[x, y]为相对原图片左上角的坐标
     * @param endPoint   结束坐标点，其值[x, y]为相对原图片左上角的坐标
     * @return 被转换前的图像对象
     * @throws java.io.IOException 当读写文件失败时抛出
     */
    public static BufferedImage clipScale(String srcPath, String taPath, int[] startPoint,
                                          int[] endPoint) throws IOException {
        File srcIm = new File(srcPath);
        if (null == srcIm) {
            throw Exceptions.makeThrow("Fail to find image file '%s'!", srcPath);
        }

        File taIm = Files.createFileIfNoExists(taPath);
        return clipScale(srcIm, taIm, startPoint, endPoint);
    }

    /**
     * 自动缩放剪切一个图片，令其符合给定的尺寸
     * <p>
     * 如果图片太大，则将其缩小，如果图片太小，则将其放大，多余的部分被裁减
     *
     * @param im 图像对象
     * @param w  宽度
     * @param h  高度
     * @return 被转换后的图像
     */
    public static BufferedImage clipScale(BufferedImage im, int w, int h) {
        // 获得尺寸
        int oW = im.getWidth();
        int oH = im.getHeight();
        float oR = (float) oW / (float) oH;
        float nR = (float) w / (float) h;

        int nW, nH, x, y;
        /*
         * 裁减
         */
        // 原图太宽，计算当原图与画布同高时，原图的等比宽度
        if (oR > nR) {
            nW = (h * oW) / oH;
            nH = h;
            x = (w - nW) / 2;
            y = 0;
        }
        // 原图太长
        else if (oR < nR) {
            nW = w;
            nH = (w * oH) / oW;
            x = 0;
            y = (h - nH) / 2;
        }
        // 比例相同
        else {
            nW = w;
            nH = h;
            x = 0;
            y = 0;
        }
        // 创建图像
        BufferedImage re = new BufferedImage(w, h, ColorSpace.TYPE_RGB);
        re.getGraphics().drawImage(im, x, y, nW, nH, Color.black, null);
        // 返回
        return re;
    }

    /**
     * 将一个图片文件读入内存
     *
     * @param img 图片文件
     * @return 图片对象
     */
    public static BufferedImage read(Object img) {
        try {
            if (img instanceof CharSequence) {
                return ImageIO.read(new File(img.toString()));
            }
            if (img instanceof File) {
                return ImageIO.read((File) img);
            }

            if (img instanceof URL) {
                img = ((URL) img).openStream();
            }

            if (img instanceof InputStream) {
                File tmp = File.createTempFile("nutz_img", ".jpg");
                Files.write(tmp, img);
                try {
                    return read(tmp);
                } finally {
                    tmp.delete();
                }
            }
            throw Exceptions.makeThrow("Unkown img info!! --> " + img);
        } catch (IOException e) {
            try {
                InputStream in = null;
                if (img instanceof File) {
                    in = new FileInputStream((File) img);
                } else if (img instanceof URL) {
                    in = ((URL) img).openStream();
                } else if (img instanceof InputStream) {
                    in = (InputStream) img;
                }
                if (in != null) {
                    return readJpeg(in);
                }
            } catch (IOException e2) {
                e2.fillInStackTrace();
            }
            return null;
            // throw Exceptions.wrapThrow(e);
        }
    }

    /**
     * 将内存中一个图片写入目标文件
     *
     * @param im         图片对象
     * @param targetFile 目标文件，根据其后缀，来决定写入何种图片格式
     */
    public static void write(RenderedImage im, File targetFile) {
        try {
            ImageIO.write(im, Files.getSuffixName(targetFile), targetFile);
        } catch (IOException e) {
            throw Exceptions.wrapThrow(e);
        }
    }

    /**
     * 写入一个 JPG 图像
     *
     * @param im        图像对象
     * @param targetJpg 目标输出 JPG 图像文件
     * @param quality   质量 0.1f ~ 1.0f
     */
    public static void writeJpeg(RenderedImage im, Object targetJpg, float quality) {
        try {
            ImageWriter writer = ImageIO.getImageWritersBySuffix("jpg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            ImageOutputStream os = ImageIO.createImageOutputStream(targetJpg);
            writer.setOutput(os);
            writer.write(null, new IIOImage(im, null, null), param);
            os.flush();
            os.close();
        } catch (IOException e) {
            throw Exceptions.wrapThrow(e);
        }
    }

    /**
     * 尝试读取JPEG文件的高级方法,可读取32位的jpeg文件
     * <p/>
     * 来自:
     * http://stackoverflow.com/questions/2408613/problem-reading-jpeg-image-
     * using-imageio-readfile-file
     */
    private static BufferedImage readJpeg(InputStream in) throws IOException {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("JPEG");
        ImageReader reader = null;
        while (readers.hasNext()) {
            reader = readers.next();
            if (reader.canReadRaster()) {
                break;
            }
        }
        ImageInputStream input = ImageIO.createImageInputStream(in);
        reader.setInput(input);
        // Read the image raster
        Raster raster = reader.readRaster(0, null);
        BufferedImage image = createJPEG4(raster);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeJpeg(image, out, 1);
        out.flush();
        return read(new ByteArrayInputStream(out.toByteArray()));
    }

    /**
     * Java's ImageIO can't process 4-component images and Java2D can't apply
     * AffineTransformOp either, so convert raster data to RGB. Technique due to
     * MArk Stephens. Free for any use.
     */
    private static BufferedImage createJPEG4(Raster raster) {
        int w = raster.getWidth();
        int h = raster.getHeight();
        byte[] rgb = new byte[w * h * 3];

        float[] Y = raster.getSamples(0, 0, w, h, 0, (float[]) null);
        float[] Cb = raster.getSamples(0, 0, w, h, 1, (float[]) null);
        float[] Cr = raster.getSamples(0, 0, w, h, 2, (float[]) null);
        float[] K = raster.getSamples(0, 0, w, h, 3, (float[]) null);

        for (int i = 0, imax = Y.length, base = 0; i < imax; i++, base += 3) {
            float k = 220 - K[i], y = 255 - Y[i], cb = 255 - Cb[i], cr = 255 - Cr[i];

            double val = y + 1.402 * (cr - 128) - k;
            val = (val - 128) * .65f + 128;
            rgb[base] = val < 0.0 ? (byte) 0 : val > 255.0 ? (byte) 0xff : (byte) (val + 0.5);

            val = y - 0.34414 * (cb - 128) - 0.71414 * (cr - 128) - k;
            val = (val - 128) * .65f + 128;
            rgb[base + 1] = val < 0.0 ? (byte) 0 : val > 255.0 ? (byte) 0xff : (byte) (val + 0.5);

            val = y + 1.772 * (cb - 128) - k;
            val = (val - 128) * .65f + 128;
            rgb[base + 2] = val < 0.0 ? (byte) 0 : val > 255.0 ? (byte) 0xff : (byte) (val + 0.5);
        }

        raster = Raster.createInterleavedRaster(new DataBufferByte(rgb, rgb.length), w, h, w * 3, 3,
                new int[]{0, 1, 2}, null);

        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorModel cm = new ComponentColorModel(cs, false, true, Transparency.OPAQUE,
                DataBuffer.TYPE_BYTE);
        return new BufferedImage(cm, (WritableRaster) raster, true, null);
    }

    /**
     * 根据一堆图片生成一个gif图片
     *
     * @param targetFile 目标输出文件
     * @param frameFiles 组成动画的文件
     * @param delay      帧间隔
     * @return 是否生成成功
     */
    public static boolean writeGif(String targetFile, String[] frameFiles, int delay) {
        try {
            AnimatedGifEncoder e = new AnimatedGifEncoder();
            e.setRepeat(0);
            e.start(targetFile);
            for (String f : frameFiles) {
                e.setDelay(delay);
                e.addFrame(ImageIO.read(new File(f)));
            }
            return e.finish();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 生成该图片对应的 Base64 编码的字符串
     *
     * @param targetFile 图片文件
     * @return 图片对应的 Base64 编码的字符串
     */
    public static String encodeBase64(String targetFile) {
        return encodeBase64(new File(targetFile));
    }

    /**
     * 生成该图片对应的 Base64 编码的字符串
     *
     * @param targetFile 图片文件
     * @return 图片对应的 Base64 编码的字符串
     */
    public static String encodeBase64(File targetFile) {
        BufferedImage image = null;

        try {
            image = ImageIO.read(targetFile);
        } catch (IOException e) {
            throw Exceptions.wrapThrow(e);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(baos);
        image.flush();
        try {
            ImageIO.write(image, Files.getSuffixName(targetFile), bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            throw Exceptions.wrapThrow(e);
        }

        byte[] bImage = baos.toByteArray();

        return Base64.encodeToString(bImage, false);
    }
    /**
     * 几种常见的图片格式
     */
    public static String IMAGE_TYPE_GIF  = "gif";// 图形交换格式
    public static String IMAGE_TYPE_JPG  = "jpg";// 联合照片专家组
    public static String IMAGE_TYPE_JPEG = "jpeg";// 联合照片专家组
    public static String IMAGE_TYPE_BMP  = "bmp";// 英文Bitmap（位图）的简写，它是Windows操作系统中的标准图像文件格式
    public static String IMAGE_TYPE_PNG  = "png";// 可移植网络图形
    public static String IMAGE_TYPE_PSD  = "psd";// Photoshop的专用格式Photoshop

    // 创建一个随机数生成器类
    private static Random random = new Random();

    public static void showSupportedFormat() {
        System.out.println("支持写的图片格式:" + java.util.Arrays.toString(ImageIO.getWriterFormatNames()));
        System.out.println("支持读的图片格式:" + java.util.Arrays.toString(ImageIO.getReaderFormatNames()));
    }

    public static byte[] randCode(int length) {
        byte[] codeSequence = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
                'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
                'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        // 得到随机产生的验证码数字。
        byte[] chars = new byte[length];
        for (int i = 0; i < length; i++) {
            chars[i] = codeSequence[random.nextInt(36)];
        }
        return chars;
    }

    public static Map<String, byte[]> createCaptcher(int codeCount) {
        // 验证码字符个数
        if (codeCount < 1) {
            codeCount = 4;
        }

        // 验证码图片的宽度。
        int width = codeCount * 16 + 20;

        // 验证码图片的高度。
        int height = 20;

        int x = 0;

        // 字体高度
        int fontHeight;

        int codeY;

        x = width / (codeCount + 1);
        fontHeight = height - 2;
        codeY = height - 4;
        BufferedImage buffImg = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = buffImg.createGraphics();

        // 将图像填充为白色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // 创建字体，字体的大小应该根据图片的高度来定。
        Font font = new Font("Fixedsys", Font.PLAIN, fontHeight);
        // 设置字体。
        g.setFont(font);

        // 画边框。
        //        g.setColor(Color.BLACK);
        //        g.drawRect(0, 0, width - 1, height - 1);

        // 随机产生160条干扰线，使图象中的认证码不易被其它程序探测到。
        g.setColor(Color.BLACK);
        for (int i = 0; i < 1; i++) {
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            g.drawLine(x2, y2, x + xl, y2 + yl);
        }

        int red = 0, green = 0, blue = 0;
        byte[] codes = randCode(codeCount);

        // 随机产生codeCount数字的验证码。
        for (int i = 0; i < codeCount; i++) {
            String strRand = "" + (char) codes[i];
            // 产生随机的颜色分量来构造颜色值，这样输出的每位数字的颜色值都将不同。
            red = random.nextInt(255);
            green = random.nextInt(255);
            blue = random.nextInt(255);

            // 用随机产生的颜色将验证码绘制到图像中。
            g.setColor(new Color(red, green, blue));
            g.drawString(strRand, (i + 1) * x, codeY);
        }
        Map<String, byte[]> m = new HashMap<>(2);
        m.put("code", codes);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(buffImg, "jpeg", baos);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        m.put("img", baos.toByteArray());

        return m;
    }

    public static BufferedImage createImage(String text, int fontHeight) {
        if (fontHeight < 1) {
            fontHeight = 72;
        }
        // 验证码图片的宽度。
        int width = text.length() * fontHeight;

        // 验证码图片的高度。
        int height = fontHeight;

        BufferedImage buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = buffImg.createGraphics();

        // 将图像填充为白色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // 创建字体，字体的大小应该根据图片的高度来定。
        Font font = new Font("宋体", Font.PLAIN, fontHeight);
        // 设置字体。
        g.setColor(Color.BLACK);
        g.setFont(font);
        // 微调
        g.drawString(text, 0, height - 10);
        g.dispose();

        return buffImg;
    }

    public static BufferedImage createImage(char c, int fontHeight) {
        String text = String.valueOf(c);
        return createImage(text, fontHeight);
    }

    /**
     * @param srcImageFile 源图像文件地址
     * @return {@link BufferedImage}
     */
    public static BufferedImage readImg(String srcImageFile) throws IOException {
        return ImageIO.read(new File(srcImageFile));
    }

    public final static void scale(String srcImageFile, String result, int scale, boolean flag) {
        try {
            BufferedImage bi = scale(readImg(srcImageFile), scale, flag);
            // 输出到文件流
            ImageIO.write(bi, "JPEG", new File(result));
        } catch (IOException e) {
            LOGGER.warn("", e);
        }
    }

    /**
     * 缩放图像（按比例缩放）
     *
     * @param src 源图像文件
     * @param scale 缩放比例
     * @param flag 缩放选择:true 放大; false 缩小;
     */
    public final static BufferedImage scale(BufferedImage src, int scale, boolean flag) {
        // 得到源图宽
        int width = src.getWidth();
        // 得到源图长
        int height = src.getHeight();

        if (flag) {
            // 放大
            width = width * scale;
            height = height * scale;
        } else {
            // 缩小
            width = width / scale;
            height = height / scale;
        }
        Image image = src.getScaledInstance(width, height,
                Image.SCALE_DEFAULT);
        BufferedImage tag = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics g = tag.getGraphics();
        // 绘制缩小后的图
        g.drawImage(image, 0, 0, null);

        g.dispose();
        return tag;
    }

    /**
     * 缩放图像（按高度和宽度缩放）
     *
     * @param srcImageFile 源图像文件地址
     * @param result 缩放后的图像地址
     * @param height 缩放后的高度
     * @param width 缩放后的宽度
     * @param bb 比例不对时是否需要补白：true为补白; false为不补白;
     */
    public final static void scale(String srcImageFile, String result, int width, int height,
                                   boolean bb) {
        try {
            BufferedImage itemp = scale(readImg(srcImageFile), width, height, bb);
            ImageIO.write(itemp, "JPEG", new File(result));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 缩放图像（按高度和宽度缩放）
     *
     * @param bi 源图像
     * @param height 缩放后的高度
     * @param width 缩放后的宽度
     * @param bb 比例不对时是否需要补白：true为补白; false为不补白;
     * @return {@link BufferedImage}
     */
    public final static BufferedImage scale(BufferedImage bi, int width, int height, boolean bb) {
        // 缩放比例
        double ratio = 0.0;
        Image itemp = bi.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
        // 计算比例
        if ((bi.getHeight() > height) || (bi.getWidth() > width)) {
            if (bi.getHeight() > bi.getWidth()) {
                ratio = (new Integer(height)).doubleValue()
                        / bi.getHeight();
            } else {
                ratio = (new Integer(width)).doubleValue() / bi.getWidth();
            }
            AffineTransformOp op = new AffineTransformOp(AffineTransform
                    .getScaleInstance(ratio, ratio),
                    null);
            itemp = op.filter(bi, null);
        }
        if (bb) {
            //补白
            BufferedImage image = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.white);
            g.fillRect(0, 0, width, height);
            if (width == itemp.getWidth(null)) {
                g.drawImage(itemp, 0, (height - itemp.getHeight(null)) / 2,
                        itemp.getWidth(null), itemp.getHeight(null),
                        Color.white, null);
            } else {
                g.drawImage(itemp, (width - itemp.getWidth(null)) / 2, 0,
                        itemp.getWidth(null), itemp.getHeight(null),
                        Color.white, null);
            }
            g.dispose();
            itemp = image;
        }
        return (BufferedImage) itemp;
    }


    /**
     * 图像切割(按指定起点坐标和宽高切割)
     *
     * @param srcImageFile 源图像地址
     * @param result 切片后的图像地址
     * @param x 目标切片起点坐标X
     * @param y 目标切片起点坐标Y
     * @param width 目标切片宽度
     * @param height 目标切片高度
     */
    public final static void cut(String srcImageFile, String result, int x, int y, int width,
                                 int height) {
        try {
            // 读取源图像
            BufferedImage bi = ImageIO.read(new File(srcImageFile));
            // 源图宽度
            int srcWidth = bi.getHeight();
            // 源图高度
            int srcHeight = bi.getWidth();
            if (srcWidth > 0 && srcHeight > 0) {
                Image image = bi.getScaledInstance(srcWidth, srcHeight,
                        Image.SCALE_DEFAULT);
                // 四个参数分别为图像起点坐标和宽高
                // 即: CropImageFilter(int x,int y,int width,int height)
                ImageFilter cropFilter = new CropImageFilter(x, y, width, height);
                Image img = Toolkit.getDefaultToolkit().createImage(
                        new FilteredImageSource(image.getSource(),
                                cropFilter));
                BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics g = tag.getGraphics();
                g.drawImage(img, 0, 0, width, height, null); // 绘制切割后的图

                g.dispose();
                // 输出为文件
                ImageIO.write(tag, "JPEG", new File(result));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 图像切割（指定切片的行数和列数）
     *
     * @param srcImageFile 源图像地址
     * @param descDir 切片目标文件夹
     * @param rows 目标切片行数。默认2，必须是范围 [1, 20] 之内
     * @param cols 目标切片列数。默认2，必须是范围 [1, 20] 之内
     */
    public final static void cut(String srcImageFile, String descDir, int rows, int cols) {
        try {
            if (rows <= 0 || rows > 20) {
                rows = 2; // 切片行数
            }
            if (cols <= 0 || cols > 20) {
                cols = 2; // 切片列数
            }
            // 读取源图像

            BufferedImage bi = ImageIO.read(new File(srcImageFile));
            int srcWidth = bi.getHeight(); // 源图宽度
            int srcHeight = bi.getWidth(); // 源图高度
            if (srcWidth > 0 && srcHeight > 0) {
                Image img;
                ImageFilter cropFilter;
                Image image = bi.getScaledInstance(srcWidth, srcHeight, Image.SCALE_DEFAULT);
                // 每张切片的宽度
                int destWidth = srcWidth;
                // 每张切片的高度
                int destHeight = srcHeight;

                // 计算切片的宽度和高度
                if (srcWidth % cols == 0) {
                    destWidth = srcWidth / cols;
                } else {
                    destWidth = (int) Math.floor(srcWidth / cols) + 1;
                }
                if (srcHeight % rows == 0) {
                    destHeight = srcHeight / rows;
                } else {
                    destHeight = (int) Math.floor(srcWidth / rows) + 1;
                }
                // 循环建立切片
                // 改进的想法:是否可用多线程加快切割速度
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        // 四个参数分别为图像起点坐标和宽高
                        // 即: CropImageFilter(int x,int y,int width,int height)
                        cropFilter = new CropImageFilter(j * destWidth, i * destHeight,
                                destWidth, destHeight);
                        img = Toolkit.getDefaultToolkit().createImage(
                                new FilteredImageSource(image.getSource(),
                                        cropFilter));
                        BufferedImage tag = new BufferedImage(destWidth,
                                destHeight,
                                BufferedImage.TYPE_INT_RGB);
                        Graphics g = tag.getGraphics();
                        g.drawImage(img, 0, 0, null); // 绘制缩小后的图

                        g.dispose();
                        // 输出为文件

                        ImageIO.write(tag, "JPEG", new File(descDir
                                + "_r" + i + "_c" + j
                                + ".jpg"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 图像切割（指定切片的宽度和高度）
     *
     * @param srcImageFile 源图像地址
     * @param descDir 切片目标文件夹
     * @param destWidth 目标切片宽度。默认200
     * @param destHeight 目标切片高度。默认150
     */
    public final static void cutWithFixedSize(String srcImageFile, String descDir, int destWidth,
                                              int destHeight) {
        try {
            if (destWidth <= 0) {
                destWidth = 200; // 切片宽度
            }
            if (destHeight <= 0) {
                destHeight = 150; // 切片高度
            }
            // 读取源图像

            BufferedImage bi = ImageIO.read(new File(srcImageFile));
            int srcWidth = bi.getHeight(); // 源图宽度
            int srcHeight = bi.getWidth(); // 源图高度
            if (srcWidth > destWidth && srcHeight > destHeight) {
                Image img;
                ImageFilter cropFilter;
                Image image = bi.getScaledInstance(srcWidth, srcHeight, Image.SCALE_DEFAULT);
                int cols = 0; // 切片横向数量
                int rows = 0; // 切片纵向数量
                // 计算切片的横向和纵向数量
                if (srcWidth % destWidth == 0) {
                    cols = srcWidth / destWidth;
                } else {
                    cols = (int) Math.floor(srcWidth / destWidth) + 1;
                }
                if (srcHeight % destHeight == 0) {
                    rows = srcHeight / destHeight;
                } else {
                    rows = (int) Math.floor(srcHeight / destHeight) + 1;
                }
                // 循环建立切片
                // 改进的想法:是否可用多线程加快切割速度
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        // 四个参数分别为图像起点坐标和宽高
                        // 即: CropImageFilter(int x,int y,int width,int height)
                        cropFilter = new CropImageFilter(j * destWidth, i * destHeight,
                                destWidth, destHeight);
                        img = Toolkit.getDefaultToolkit().createImage(
                                new FilteredImageSource(image.getSource(),
                                        cropFilter));
                        BufferedImage tag = new BufferedImage(destWidth,
                                destHeight,
                                BufferedImage.TYPE_INT_RGB);
                        Graphics g = tag.getGraphics();
                        g.drawImage(img, 0, 0, null); // 绘制缩小后的图

                        g.dispose();
                        // 输出为文件

                        ImageIO.write(tag, "JPEG", new File(descDir
                                + "_r" + i + "_c" + j
                                + ".jpg"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 图像切割（指定切片的宽度和高度）
     *
     * @param bi 源图像
     * @param width 目标切片宽度。默认12
     * @param height 目标切片高度。默认12
     */
    public final static BufferedImage[][] cutWithFixedSize(BufferedImage bi, int width,
                                                           int height) {

        if (width <= 0) {
            width = 200; // 切片宽度
        }
        if (height <= 0) {
            height = 150; // 切片高度
        }
        // 源图宽度
        int srcWidth = bi.getHeight();
        // 源图高度
        int srcHeight = bi.getWidth();
        if (srcWidth > width && srcHeight > height) {
            Image img;
            ImageFilter cropFilter;
            Image image = bi.getScaledInstance(srcWidth, srcHeight, Image.SCALE_DEFAULT);
            // 切片横向数量
            int cols = 0;
            // 切片纵向数量
            int rows = 0;
            // 计算切片的横向和纵向数量
            if (srcWidth % width == 0) {
                cols = srcWidth / width;
            } else {
                cols = (int) Math.floor(srcWidth / width) + 1;
            }
            if (srcHeight % height == 0) {
                rows = srcHeight / height;
            } else {
                rows = (int) Math.floor(srcHeight / height) + 1;
            }
            BufferedImage[][] cuts = new BufferedImage[rows][cols];

            // 循环建立切片
            // 改进的想法:是否可用多线程加快切割速度
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    // 四个参数分别为图像起点坐标和宽高
                    // 即: CropImageFilter(int x,int y,int width,int height)
                    cropFilter = new CropImageFilter(j * width, i * height,
                            width, height);
                    img = Toolkit.getDefaultToolkit()
                                 .createImage(
                                         new FilteredImageSource(image.getSource(), cropFilter));
                    BufferedImage tag = new BufferedImage(width, height,
                            BufferedImage.TYPE_INT_RGB);
                    Graphics g = tag.getGraphics();
                    // 绘制缩小后的图
                    g.drawImage(img, 0, 0, null);
                    g.dispose();
                    cuts[i][j] = tag;
                }
            }
            return cuts;
        }
        return null;
    }


    /**
     * 图像类型转换：GIF->JPG、GIF->PNG、PNG->JPG、PNG->GIF(X)、BMP->PNG
     *
     * @param srcImageFile 源图像地址
     * @param formatName 包含格式非正式名称的 String：如JPG、JPEG、GIF等
     * @param destImageFile 目标图像地址
     */
    public final static void convert(String srcImageFile, String formatName, String destImageFile) {
        try {
            File f = new File(srcImageFile);
            f.canRead();
            f.canWrite();
            BufferedImage src = ImageIO.read(f);
            ImageIO.write(src, formatName, new File(destImageFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 彩色转为黑白
     *
     * @param srcImageFile 源图像地址
     * @param destImageFile 目标图像地址
     */
    public final static void gray(String srcImageFile, String destImageFile) {
        try {
            BufferedImage src = ImageIO.read(new File(srcImageFile));
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            ColorConvertOp op = new ColorConvertOp(cs, null);
            src = op.filter(src, null);
            ImageIO.write(src, "JPEG", new File(destImageFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 转灰度图像
     */
    public final static BufferedImage gray(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            return src;
        }
        // 图像转灰
        BufferedImage grayImage = new BufferedImage(src.getWidth(), src.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp op = new ColorConvertOp(cs, null);
        op.filter(src, grayImage);
        return grayImage;
    }

    /**
     * 给图片添加文字水印
     *
     * @param pressText 水印文字
     * @param srcImageFile 源图像地址
     * @param destImageFile 目标图像地址
     * @param fontName 水印的字体名称
     * @param fontStyle 水印的字体样式
     * @param color 水印的字体颜色
     * @param fontSize 水印的字体大小
     * @param x 修正值
     * @param y 修正值
     * @param alpha 透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     */
    public final static void pressText(String pressText,
                                       String srcImageFile, String destImageFile, String fontName,
                                       int fontStyle, Color color, int fontSize, int x,
                                       int y, float alpha) {
        try {
            File img = new File(srcImageFile);
            Image src = ImageIO.read(img);
            int width = src.getWidth(null);
            int height = src.getHeight(null);
            BufferedImage image = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.drawImage(src, 0, 0, width, height, null);
            g.setColor(color);
            g.setFont(new Font(fontName, fontStyle, fontSize));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
                    alpha));
            // 在指定坐标绘制水印文字

            g.drawString(pressText, (width - (getLength(pressText) * fontSize))
                    / 2 + x, (height - fontSize) / 2 + y);
            g.dispose();
            ImageIO.write((BufferedImage) image, "JPEG", new File(destImageFile));// 输出到文件流
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 给图片添加图片水印
     *
     * @param pressImg 水印图片
     * @param srcImageFile 源图像地址
     * @param destImageFile 目标图像地址
     * @param x 修正值。 默认在中间
     * @param y 修正值。 默认在中间
     * @param alpha 透明度：alpha 必须是范围 [0.0, 1.0] 之内（包含边界值）的一个浮点数字
     */
    public final static void pressImage(String pressImg, String srcImageFile, String destImageFile,
                                        int x, int y, float alpha) {
        try {
            File img = new File(srcImageFile);
            Image src = ImageIO.read(img);
            int wideth = src.getWidth(null);
            int height = src.getHeight(null);
            BufferedImage image = new BufferedImage(wideth, height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.drawImage(src, 0, 0, wideth, height, null);
            // 水印文件
            Image src_biao = ImageIO.read(new File(pressImg));
            int wideth_biao = src_biao.getWidth(null);
            int height_biao = src_biao.getHeight(null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
                    alpha));
            g.drawImage(src_biao, (wideth - wideth_biao) / 2,
                    (height - height_biao) / 2, wideth_biao, height_biao, null);
            // 水印文件结束
            g.dispose();
            ImageIO.write((BufferedImage) image, "JPEG", new File(destImageFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建图片缩略图(等比缩放 无失真缩放)
     *
     * @param src 源图片文件完整路径
     * @param dist 目标图片文件完整路径
     * @param width 缩放的宽度
     * @param height 缩放的高度
     * @param flag true 按照实际长宽输出  如果 false 按照比例进行无失真压缩
     */
    public static boolean createThumbnail(String src, String dist, float width, float height,
                                          boolean flag) {
        boolean flag1 = false;
        try {
            File srcfile = new File(src);
            if (!srcfile.exists()) {
                System.out.println("文件不存在");
                return flag1;
            }
            BufferedImage image = ImageIO.read(srcfile);

            // 获得缩放的比例

            double ratio = 1.0;
            // 判断如果高、宽都不大于设定值，则不处理
            if (image.getHeight() > height || image.getWidth() > width) {
                if (image.getHeight() > image.getWidth()) {
                    ratio = height / image.getHeight();
                } else {
                    ratio = width / image.getWidth();
                }
            }
            int newWidth = flag ? (int) width : (int) (image.getWidth() * ratio);
            int newHeight = flag ? (int) height : (int) (image.getHeight() * ratio);
            BufferedImage bfImage = new BufferedImage(newWidth, newHeight,
                    BufferedImage.TYPE_INT_RGB);
            flag1 = bfImage.getGraphics().drawImage(
                    image.getScaledInstance(newWidth, newHeight,
                            Image.SCALE_SMOOTH), 0, 0, null);
            if (flag1) {
                FileOutputStream output = new FileOutputStream(dist);
                ImageIO.write(bfImage, "PNG", output);
                output.close();
            }
        } catch (Exception e) {
            flag1 = false;
        }
        return flag1;
    }

    /**
     * 计算text的长度（一个中文算两个字符）
     */
    public final static int getLength(String text) {
        int length = 0;
        for (int i = 0; i < text.length(); i++) {
            if (new String(text.charAt(i) + "").getBytes().length > 1) {
                length += 2;
            } else {
                length += 1;
            }
        }
        return length / 2;
    }

    /**
     * 获取图片宽度\高度
     * add by jiang_yanyan 2015-01-04
     *
     * @param file 图片文件
     * @return 宽度, 高度
     */
    public static int[] getImgWidthHeight(File file) {
        InputStream is = null;
        BufferedImage src = null;
        int[] ret = new int[2];
        try {
            is = new FileInputStream(file);
            src = ImageIO.read(is);
            // 得到源图宽
            ret[0] = src.getWidth(null);
            // 得到源图高
            ret[1] = src.getHeight(null);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * 均值哈希实现图像指纹比较
     *
     * @author guyadong
     */
    public static final class FingerPrint {

        /**
         * 图像指纹的尺寸,将图像resize到指定的尺寸，来计算哈希数组
         */
        private static final int HASH_SIZE = 16;
        /**
         * 保存图像指纹的二值化矩阵
         */
        private final byte[] binaryzationMatrix;

        public FingerPrint(byte[] hashValue) {
            if (hashValue.length != HASH_SIZE * HASH_SIZE) {
                throw new IllegalArgumentException(
                        String.format("length of hashValue must be %d", HASH_SIZE * HASH_SIZE));
            }
            this.binaryzationMatrix = hashValue;
        }

        public FingerPrint(String hashValue) {
            this(toBytes(hashValue));
        }

        public FingerPrint(BufferedImage src) {
            this(hashValue(src));
        }

        private static byte[] hashValue(BufferedImage src) {
            BufferedImage hashImage = resize(src, HASH_SIZE, HASH_SIZE);
            byte[] matrixGray = (byte[]) gray(hashImage).getData()
                                                        .getDataElements(0, 0, HASH_SIZE, HASH_SIZE,
                                                                null);
            return binaryzation(matrixGray);
        }

        /**
         * 从压缩格式指纹创建{@link FingerPrint}对象
         */
        public static FingerPrint createFromCompact(byte[] compactValue) {
            return new FingerPrint(uncompact(compactValue));
        }

        public static boolean validHashValue(byte[] hashValue) {
            if (hashValue.length != HASH_SIZE) {
                return false;
            }
            for (byte b : hashValue) {
                if (0 != b && 1 != b) {
                    return false;
                }
            }
            return true;
        }

        public static boolean validHashValue(String hashValue) {
            if (hashValue.length() != HASH_SIZE) {
                return false;
            }
            for (int i = 0; i < hashValue.length(); ++i) {
                if ('0' != hashValue.charAt(i) && '1' != hashValue.charAt(i)) {
                    return false;
                }
            }
            return true;
        }

        public byte[] compact() {
            return compact(binaryzationMatrix);
        }

        /**
         * 指纹数据按位压缩
         */
        private static byte[] compact(byte[] hashValue) {
            byte[] result = new byte[(hashValue.length + 7) >> 3];
            byte b = 0;
            for (int i = 0; i < hashValue.length; ++i) {
                if (0 == (i & 7)) {
                    b = 0;
                }
                if (1 == hashValue[i]) {
                    b |= 1 << (i & 7);
                } else if (hashValue[i] != 0) {
                    throw new IllegalArgumentException(
                            "invalid hashValue,every element must be 0 or 1");
                }
                if (7 == (i & 7) || i == hashValue.length - 1) {
                    result[i >> 3] = b;
                }
            }
            return result;
        }

        /**
         * 压缩格式的指纹解压缩
         */
        private static byte[] uncompact(byte[] compactValue) {
            byte[] result = new byte[compactValue.length << 3];
            for (int i = 0; i < result.length; ++i) {
                if ((compactValue[i >> 3] & (1 << (i & 7))) == 0) {
                    result[i] = 0;
                } else {
                    result[i] = 1;
                }
            }
            return result;
        }

        /**
         * 字符串类型的指纹数据转为字节数组
         */
        private static byte[] toBytes(String hashValue) {
            hashValue = hashValue.replaceAll("\\s", "");
            byte[] result = new byte[hashValue.length()];
            for (int i = 0; i < result.length; ++i) {
                char c = hashValue.charAt(i);
                if ('0' == c) {
                    result[i] = 0;
                } else if ('1' == c) {
                    result[i] = 1;
                } else {
                    throw new IllegalArgumentException("invalid hashValue String");
                }
            }
            return result;
        }

        /**
         * 缩放图像到指定尺寸
         */
        private static BufferedImage resize(Image src, int width, int height) {
            BufferedImage result = new BufferedImage(width, height,
                    BufferedImage.TYPE_3BYTE_BGR);
            Graphics g = result.getGraphics();
            try {
                g.drawImage(src.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
            } finally {
                g.dispose();
            }
            return result;
        }

        /**
         * 计算均值
         */
        private static int mean(byte[] src) {
            long sum = 0;
            // 将数组元素转为无符号整数
            for (byte b : src) {
                sum += (long) b & 0xff;
            }
            return (int) (Math.round((float) sum / src.length));
        }

        /**
         * 二值化处理
         */
        private static byte[] binaryzation(byte[] src) {
            byte[] dst = src.clone();
            int mean = mean(src);
            for (int i = 0; i < dst.length; ++i) {
                // 将数组元素转为无符号整数再比较
                dst[i] = (byte) (((int) dst[i] & 0xff) >= mean ? 1 : 0);
            }
            return dst;

        }


        @Override
        public String toString() {
            return toString(true);
        }

        /**
         * @param multiLine 是否分行
         */
        public String toString(boolean multiLine) {
            StringBuffer buffer = new StringBuffer();
            int count = 0;
            for (byte b : this.binaryzationMatrix) {
                //buffer.append(0 == b ? '0' : '1');
                buffer.append(0 == b ? "●" : " ");
                if (multiLine && ++count % HASH_SIZE == 0) {
                    buffer.append('\n');
                }
            }
            return buffer.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof FingerPrint) {
                return Arrays
                        .equals(this.binaryzationMatrix, ((FingerPrint) obj).binaryzationMatrix);
            } else {
                return super.equals(obj);
            }
        }

        /**
         * 与指定的压缩格式指纹比较相似度
         *
         * @see #compare(FingerPrint)
         */
        public float compareCompact(byte[] compactValue) {
            return compare(createFromCompact(compactValue));
        }

        /**
         * @see #compare(FingerPrint)
         */
        public float compare(String hashValue) {
            return compare(new FingerPrint(hashValue));
        }

        /**
         * 与指定的指纹比较相似度
         *
         * @see #compare(FingerPrint)
         */
        public float compare(byte[] hashValue) {
            return compare(new FingerPrint(hashValue));
        }

        /**
         * 与指定图像比较相似度
         *
         * @see #compare(FingerPrint)
         */
        public float compare(BufferedImage image2) {
            return compare(new FingerPrint(image2));
        }

        /**
         * 比较指纹相似度
         *
         * @see #compare(byte[], byte[])
         */
        public float compare(FingerPrint src) {
            if (src.binaryzationMatrix.length != this.binaryzationMatrix.length) {
                throw new IllegalArgumentException("length of hashValue is mismatch");
            }
            return compare(binaryzationMatrix, src.binaryzationMatrix);
        }

        /**
         * 判断两个数组相似度，数组长度必须一致否则抛出异常
         *
         * @return 返回相似度(0.0~1.0)
         */
        private static float compare(byte[] f1, byte[] f2) {
            if (f1.length != f2.length) {
                throw new IllegalArgumentException("mismatch FingerPrint length");
            }
            int sameCount = 0;
            for (int i = 0; i < f1.length; ++i) {
                if (f1[i] == f2[i]) {
                    ++sameCount;
                }
            }
            return (float) sameCount / f1.length;
        }

        public static float compareCompact(byte[] f1, byte[] f2) {
            return compare(uncompact(f1), uncompact(f2));
        }

        public static float compare(BufferedImage image1, BufferedImage image2) {
            return new FingerPrint(image1).compare(new FingerPrint(image2));
        }
    }

    public static void main(String[] args) throws IOException {
        //showSupportedFormat();
        BufferedImage buffImg = createImage("服", 144);
        System.out.println(new FingerPrint(buffImg).toString());
        // 12 x 12
        BufferedImage[][] bufferedImages = cutWithFixedSize(buffImg, 12, 12);
        byte[][] chars = new byte[12][12];

        // 字符串由复杂到简单
        String base = "@#&$%*o!;.";
        byte[] bytes = base.getBytes();
        FingerPrint[] fps = new FingerPrint[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            BufferedImage x = createImage((char) bytes[i], 12);
            fps[i] = new FingerPrint(x);
        }

        // find the similar
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                BufferedImage x = bufferedImages[i][j];
                FingerPrint fp = new FingerPrint(x);

                byte ch = 0x20;
                float most = 0;
                for (int k = 0; k < fps.length; k++) {
                    float compare = fp.compare(fps[k]);
                    if (compare > most) {
                        most = compare;
                        ch = bytes[k];
                    }
                }
                chars[i][j] = ch;
            }
        }
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 12; j++) {
                System.out.print((char) chars[i][j]);
            }
            System.out.println();
        }

    }
}
