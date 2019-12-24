package io.elasticjob.lite.spring.job.util;

/**
 * StringUtils.
 **/
public class StringUtils {
    
    /**
     * Returns true if the given string is null or is the empty string.
     *
     * @param string a string reference to check
     * @return true if the string is null or is the empty string
     */
    public static boolean isNullOrEmpty(final String string) {
        return string == null || string.length() == 0;
    }
}
