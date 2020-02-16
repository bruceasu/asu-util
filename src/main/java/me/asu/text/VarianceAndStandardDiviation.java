package me.asu.text;

/**
 * VarianceAndStandardDiviation.
 * <p>2017 Suk All rights reserved.</p>
 *
 * @author Suk
 * @version 1.0.0
 * @since 2017-10-14 17:34
 */

import java.text.DecimalFormat;
import java.util.Random;

public class VarianceAndStandardDiviation {


    public static void main(String[] args) {
        Random random = new Random();
        double dmax = 999;//Double.MAX_VALUE;//Double类型的最大值，太大的double值，相乘会达到无穷大
        double dmin = Double.MIN_VALUE;//Double类型的最小值

        int n = 100;//假设求取100个doubl数的方差和标准差
        double[] x = new double[n];
        for (int i = 0; i < n; i++) {//随机生成n个double数
            x[i] = Double.valueOf(Math.floor(random.nextDouble() * (dmax - dmin)));
            System.out.println(x[i]);
        }
        //设置doubl字符串输出格式，不以科学计数法输出
        DecimalFormat df = new DecimalFormat("#,##0.00");//格式化设置
        //计算方差
        double dV = variance(x);
        System.out.println("方差=" + df.format(dV));
        //计算标准差
        double dS = standardDiviation(x);
        System.out.println("标准差=" + df.format(dS));
    }

    /**
     * 方差s^2=[(x1-x)^2 +...(xn-x)^2]/n
     */
    public static double variance(double[] x) {
        int m = x.length;
        double sum = 0;
        for (int i = 0; i < m; i++) {//求和
            sum += x[i];
        }
        double dAve = sum / m;//求平均值
        double dVar = 0;
        for (int i = 0; i < m; i++) {//求方差
            dVar += (x[i] - dAve) * (x[i] - dAve);
        }
        return dVar / m;
    }

    public static double variance(Double[] x) {
        int m = x.length;
        double sum = 0;
        for (int i = 0; i < m; i++) {//求和
            sum += x[i];
        }
        double dAve = sum / m;//求平均值
        double dVar = 0;
        for (int i = 0; i < m; i++) {//求方差
            dVar += (x[i] - dAve) * (x[i] - dAve);
        }
        return dVar / m;
    }


    /**
     * 标准差σ=sqrt(s^2)
     */
    public static double standardDiviation(double[] x) {
        int m = x.length;
        double sum = 0;
        for (int i = 0; i < m; i++) {//求和
            sum += x[i];
        }
        double dAve = sum / m;//求平均值
        double dVar = 0;
        for (int i = 0; i < m; i++) {//求方差
            dVar += (x[i] - dAve) * (x[i] - dAve);
        }
        return Math.sqrt(dVar / m);
    }

    public static double standardDiviation(Double[] x) {
        int m = x.length;
        double sum = 0;
        for (int i = 0; i < m; i++) {//求和
            sum += x[i];
        }
        double dAve = sum / m;//求平均值
        double dVar = 0;
        for (int i = 0; i < m; i++) {//求方差
            dVar += (x[i] - dAve) * (x[i] - dAve);
        }
        return Math.sqrt(dVar / m);
    }
}
