package me.asu.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.asu.util.Strings;

/**
 * Created by bruce on 12/31/14.
 */
public abstract class HtmlUtils {
    /**
     * HTML字符转义
     * @return String 过滤后的字符串
     */
    public static String htmlEscape(String input) {
        if(Strings.isEmpty(input)){
            return input;
        }
        input = input.replaceAll("&", "&amp;");
        input = input.replaceAll("<", "&lt;");
        input = input.replaceAll(">", "&gt;");
        input = input.replaceAll(" ", "&nbsp;");
        input = input.replaceAll("'", "&#39;");   //IE暂不支持单引号的实体名称,而支持单引号的实体编号,故单引号转义成实体编号,其它字符转义成实体名称
        input = input.replaceAll("\"", "&quot;"); //双引号也需要转义，所以加一个斜线对其进行转义
        input = input.replaceAll("\n", "<br/>");  //不能把\n的过滤放在前面，因为还要对<和>过滤，这样就会导致<br/>失效了

        return input;
    }


    public static String htmlUnEscape(String input) {
        if(Strings.isEmpty(input)){
            return input;
        }
        input = input.replaceAll("&amp;", "&");
        input = input.replaceAll("&lt;", "<");
        input = input.replaceAll("&gt;", ">");
        input = input.replaceAll("&nbsp;", " ");
        input = input.replaceAll("&#39;", "'");
        input = input.replaceAll("&quot;", "\"");
        input = input.replaceAll("<br/>", "\n");

        Pattern p = Pattern.compile("&#x(\\w{4});");
        Matcher matcher = p.matcher(input);
        StringBuffer buffer = new StringBuffer();
        while(matcher.find()) {
            String group = matcher.group(1);
            int i = Integer.parseInt(group, 16);
            String s = String.valueOf((char)i);
            matcher.appendReplacement(buffer, s);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /***
     *
     * @param content 内容String
     * @return @tale:
     */
    public static String getNoScriptString(String content){

        if(null==content) return "";

        java.util.regex.Pattern p_script;
        java.util.regex.Matcher m_script;

        try {
            //定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script> }
            String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";

            p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
            m_script = p_script.matcher(content);
            content = m_script.replaceAll(""); //过滤script标签

        }catch(Exception e) {
            return "";
        }

        return content;
    }

    public static String getNoStyleString(String content){

        if(null==content) return "";

        java.util.regex.Pattern p_style;
        java.util.regex.Matcher m_style;

        try {
            //定义style的正则表达式{或<style[^>]*?>[\\s\\S]*?<\\/style> }
            String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>";

            p_style = Pattern.compile(regEx_style,Pattern.CASE_INSENSITIVE);
            m_style = p_style.matcher(content);
            content = m_style.replaceAll(""); //过滤style标签

        }catch(Exception e) {
            return "";
        }

        return content;
    }
}
