package me.asu.text;

import java.io.IOException;
import java.util.*;
import me.asu.util.Strings;

/**
 * Created by bruce on 1/15/15.
 */
public class SimilarTextUtils {
    public static double getSimilarity(String doc1, String doc2) throws IOException {
        if (doc1 != null && doc1.trim().length() > 0
                && doc2 != null && doc2.trim().length() > 0) {
            HashMap<Character, Integer[]> AlgorithmMap = new HashMap<Character, Integer[]>();
            //将两个字符串中的中文字符以及出现的总数封装到，AlgorithmMap中
            for (int i = 0; i < doc1.length(); i++) {
                char ch = doc1.charAt(i);
                if (isHanZi(ch)) {

                    Integer[] fq = null;
                    try {
                        fq = AlgorithmMap.get(ch);
                    } catch (Exception e) {
                    } finally {
                        if (fq != null && fq.length == 2) {
                            fq[0]++;
                        } else {
                            fq = new Integer[2];
                            fq[0] = 1;
                            fq[1] = 0;
                            AlgorithmMap.put(ch, fq);
                        }
                    }

                }
            }

            for (int i = 0; i < doc2.length(); i++) {
                char ch = doc2.charAt(i);
                if (isHanZi(ch)) {
                    Integer[] fq = null;
                    try {
                        fq = AlgorithmMap.get(ch);
                    } catch (Exception e) {
                    } finally {
                        if (fq != null && fq.length == 2) {
                            fq[1]++;
                        } else {
                            fq = new Integer[2];
                            fq[0] = 0;
                            fq[1] = 1;
                            AlgorithmMap.put(ch, fq);
                        }
                    }

                }
            }


            double sqdoc1 = 0;
            double sqdoc2 = 0;
            double denominator = 0;
            for (Map.Entry<Character, Integer[]> par : AlgorithmMap.entrySet()) {
                Integer[] c = par.getValue();
                denominator += c[0] * c[1];
                sqdoc1 += c[0] * c[0];
                sqdoc2 += c[1] * c[1];
            }
            return denominator / Math.sqrt(sqdoc1 * sqdoc2);
        } else {
            return 0;
        }
    }

    public static boolean isHanZi(char ch) {
        // 判断是否汉字
        return (ch >= 0x4E00 && ch <= 0x9FA5);
    }

    public static List<String> topSimilarString(String standardString, int topK,
            String ... testString) throws IOException {
        // input data check
        if (Strings.isBlank(standardString) ||testString == null || testString.length == 0) {
            return Collections.emptyList();
        }

        // adjust
        if (topK < 1) {
            topK = 1;
        }

        //System.out.println("compare " + standardString + " with: ");
        ArrayList<Map> result = new ArrayList<Map>();

        for (String test : testString) {
            //System.out.println(test);
            double x = getSimilarity(standardString, test);
            Map m = new HashMap();
            m.put("str", test);
            m.put("score", x);
            result.add(m);
            //System.out.printf("%s vs %s = %f \n", standardString, test, x);
        }
        // sort
        Collections.sort(result, new Comparator<Map>() {
            @Override
            public int compare(Map o1, Map o2) {
                double a = (Double) o1.get("score");
                double b = (Double) o2.get("score");
                double c = b - a;
                if (c > 0) {
                    return 1;
                } else if (c < 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        ArrayList<String> list = new ArrayList<String>(topK);
        for (int i=0; i<topK && i < result.size(); i++) {
            list.add((String)result.get(i).get("str"));
        }

        return list;
    }

    public static String removeTail(String str) {
        if (Strings.isBlank(str)) {
            return "";
        }
        String test = "号楼厦寺园苑馆寓"; //场院区街路道巷条弄里村乡镇州县盟郡市省
        int i=str.length()-1;
        for (; i>=0; i--) {
            char ch = str.charAt(i);
            if (test.contains(String.valueOf(ch))) {
                break;
            }
        }
        if (i >= 0) {
            return str.substring(0, i+1);
        } else {
            return "";
        }

    }
    public static void main(String[] args) throws IOException {

        String str1 = "北京市朝阳区周庄山水文园第7层";
        String[] strs = new String[]{
                "周庄山水文园201-1中信银行(北京十里河支行)",
                "周庄山水文园5-5渤海银行北京山水文园社区支行",
                "周庄山水文园1号楼底商04号蕾沃尔优质兔绒品牌服饰山水文园专卖店",
                "北京市朝阳区周庄山水文园1号8杏林春天中医养生会馆",
                "北京市朝阳区周庄山水文园201号2北京指墨画院",
                "周庄山水文园底商灵翠轩参茸商行",
                "北京市朝阳区周庄山水文园201号2人民艺术研究院",
                "周庄山水文园5号楼底商01茉莉美甲",
                "周庄山水文园5-1美联物业第六十六分公司",
                "北京市朝阳区周庄山水文园201号2众妙文化广场"
        };


//        String str1 = "深圳市罗湖区半岛大厦";
//        String[] strs = new String[]{
//                "深圳罗湖半岛大厦",
//                "深圳罗湖区半岛大厦",
//                "深圳市罗湖半岛大厦",
//                "深圳市罗湖新园路半岛大厦",
//                "深圳罗湖区新园路半岛大厦",
//                "深圳罗湖区新园路半鸟大厦",
//                "深圳罗湖区新园路半岛大楼",
//                "深圳罗湖区半岛大楼",
//                "深圳市罗湖半岛大楼"
//        };


//        String str1 = "罗湖(地王)深南东路5016号京基百纳广场购物中心4楼";
//        String[] strs = new String[]{
//                "深圳龙华新区龙华新区深南东路5016号京基百纳广场购物中心3楼NE & AR",
//                "深南东路5016号京基百纳空间KK - Mall购物广场B1楼楼B118号铺(近红宝路)一日三餐(京基百纳空间)",
//                "深圳罗湖区深南东路5016号京基百纳空间KK - Mall购物广场3楼(近大剧院)俏江南(京基百纳广场店)"
//        };

        ArrayList<Map> result = new ArrayList<Map>();

        for (String str2 : strs) {
            double x = getSimilarity(str1, str2);
            Map m = new HashMap();
            m.put("str", str2);
            m.put("score", x);
            result.add(m);
            System.out.printf("%s vs %s = %f \n", str1, str2, x);
        }
        // sort
        Collections.sort(result, new Comparator<Map>() {
            @Override
            public int compare(Map o1, Map o2) {
                double a = (Double) o1.get("score");
                double b = (Double) o2.get("score");
                double c = b - a;
                if (c > 0) {
                    return 1;
                } else if (c < 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        System.out.println(str1 + " similar with:");
        for (Map m : result) {
            System.out.println(m.get("score") + "\t" + m.get("str"));
        }

        str1 = removeTail(str1);
        HashSet<String> set = new HashSet<String>();
        for (String test : strs) {
            set.add(removeTail(test));
        }
        List<String> strings = topSimilarString(str1, 3, set.toArray(new String[0]));
        System.out.println(strings);
    }

}
