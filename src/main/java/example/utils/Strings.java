package example.utils;

import org.apache.commons.lang.builder.StandardToStringStyle;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Strings {

    private static ToStringStyle style;

    public static ToStringStyle style() {
        if (style == null) {
            StandardToStringStyle standard = new StandardToStringStyle();
            standard.setUseIdentityHashCode(false);
            standard.setUseShortClassName(true);
            style = standard;
        }
        return style;
    }

    public static String toString(Object obj) {
        return ToStringBuilder.reflectionToString(obj, style());
    }

    public static String[] array(String... items) {
        return items;
    }
}
