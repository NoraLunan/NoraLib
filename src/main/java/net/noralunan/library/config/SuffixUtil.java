package net.noralunan.library.config;

import javax.annotation.Nullable;

public class SuffixUtil
{
    public static String defSuffix(Object value)
    {
        return " [" + format(value, null) + "]";
    }

    public static String defNewRow(Object value)
    {
        return format(value, null);
    }

    public static String format(Object value, @Nullable String range)
    {
        if (range == null)
        {
            return String.format("Default: %s", value);
        }
        return String.format("Range: %s, Default: %s", range, value);
    }
}
