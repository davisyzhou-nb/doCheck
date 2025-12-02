package zlian.netgap.util;

public class StringUtils {

    public static boolean isEmpty(Object obj){
        String str = String.valueOf(obj);
        return str==null||str.length()==0;
    }
}
