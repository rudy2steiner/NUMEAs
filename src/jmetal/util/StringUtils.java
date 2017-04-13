package jmetal.util;

/**
 * Created by Rudy Steiner on 2017/4/10.
 */
public class StringUtils {
    public static String combine(String... args){
        StringBuilder sb=new StringBuilder();
        for(String s:args){
            sb.append(s);
        }
        return sb.toString();
    }
}
