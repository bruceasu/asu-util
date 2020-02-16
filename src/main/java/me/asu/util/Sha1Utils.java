package me.asu.util;

import java.io.File;
import java.io.InputStream;

public class Sha1Utils extends AbsDigest
{

    static String SHA1 = "SHA1";

    /**
     * 获取指定文件的 SHA1 值
     *
     * @param f 文件
     * @return 指定文件的 SHA1 值
     * @see #digest(String, File)
     */
    public static String sha1(File f)
    {
        return digest(SHA1, f);
    }

    /**
     * 获取指定输入流的 SHA1 值
     *
     * @param ins 输入流
     * @return 指定输入流的 SHA1 值
     * @see #digest(String, InputStream)
     */
    public static String sha1(InputStream ins)
    {
        return digest(SHA1, ins);
    }

    /**
     * 获取指定字符串的 SHA1 值
     *
     * @param cs 字符串
     * @return 指定字符串的 SHA1 值
     * @see #digest(String, String)
     */
    public static String sha1(String cs)
    {
        return digest(SHA1, cs);
    }

    /**
     * 获取指定字符串的 SHA1 值
     *
     * @param data 数据
     * @return 指定字符串的 SHA1 值
     * @see #digest(String, String)
     */
    public static String sha1(byte[] data)
    {
        return digest(SHA1, data);
    }


}
