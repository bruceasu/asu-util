package me.asu.util;


/**
 * 驼峰式 vs 蛇行式 .
 * <p>2017 Suk All rights reserved.</p>
 *
 * @author Suk
 * @version 1.0.0
 * @since 2017-09-29 17:49
 */
public class NameStyleUtils {

    /**
     * 将驼峰式命名的字符串转换为下划线蛇形方式。
     * 如果转换前的驼峰式命名的字符串为空，则返回空字符串。</br>
     * 例如：helloWorld->hello_world
     *
     * @param name 转换前的驼峰式命名的字符串
     * @return 转换后下划线大写方式命名的字符串
     */
    public static String snakeName(String name) {
        if (Strings.isBlank(name)) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        char[] chars = name.toCharArray();
        // 循环处理其余字符
        for (int i = 0; i < name.length(); i++) {
            char ch = chars[i];
            // 在大写字母前添加下划线
            if (Character.isUpperCase(ch)) {
                result.append("_");
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    /**
     * 将下划线蛇形方式命名的字符串转换为驼峰式。
     * 如果转换前的下划线蛇形方式命名的字符串为空，则返回空字符串。</br>
     * 例如：HELLO_WORLD->HelloWorld
     *
     * @param name 转换前的下划线大写方式命名的字符串
     * @return 转换后的驼峰式命名的字符串
     */
    public static String camelName(String name) {
        if (Strings.isBlank(name)) {
            return "";
        } else if (!name.contains("_")) {
            // 不含下划线
            return name;
        }
        StringBuilder result = new StringBuilder();
        // 用下划线将原始字符串分割
        char[] chars = name.toCharArray();
        // 循环处理其余字符
        for (int i = 0; i < name.length(); i++) {
            char ch = chars[i];
            // 在大写字母前添加下划线
            if (ch == '_') {
                i++;
                if (i == name.length()) {
                    break;
                }
                ch = chars[i];
                result.append(Character.toUpperCase(ch));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }
}
