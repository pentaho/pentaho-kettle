package org.pentaho.di.core.row;

public class ValueDataUtil
{
    public static final String leftTrim(String string)
    {
         StringBuffer buffer = new StringBuffer(string);
         while (buffer.length()>0 && buffer.charAt(0)==' ') buffer.deleteCharAt(0);
         return buffer.toString();
    }
    
    public static final String rightTrim(String string)
    {
         StringBuffer buffer = new StringBuffer(string);
         while (buffer.length()>0 && buffer.charAt(buffer.length()-1)==' ') buffer.deleteCharAt(buffer.length()-1);
         return buffer.toString();
    }

    /**
     * Determines whether or not a character is considered a space.
     * A character is considered a space in Kettle if it is a space, a tab, a newline or a cariage return.
     * @param c The character to verify if it is a space.
     * @return true if the character is a space. false otherwise. 
     */
    public static final boolean isSpace(char c)
    {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n';
    }
    
    /**
     * Trims a string: removes the leading and trailing spaces of a String.
     * @param string The string to trim
     * @return The trimmed string.
     */
    public static final String trim(String string)
    {
        int max = string.length() - 1;
        int min = 0;    

        while (min <= max && isSpace(string.charAt(min)))
            min++;
        while (max >= 0 && isSpace(string.charAt(max)))
            max--;

        if (max < min)
            return "";

        return string.substring(min, max + 1);
    }
}
