package org.pentaho.di.www;


public class ServletTestUtils {
  public static final String BAD_STRING = "!@#$%^&*()<>/";

  public static String getInsideOfTag(String tag, String string) {
    String open = "<" + tag + ">";
    String close = "</" + tag + ">";
    return string.substring(string.indexOf(open) + open.length(), string.indexOf(close));
  }

  public static boolean hasBadText(String value) {
    return value.indexOf('!') != -1 || value.indexOf('@') != -1 || value.indexOf('$') != -1 || value.indexOf('%') != -1
        || value.indexOf('^') != -1 || value.indexOf('*') != -1 || value.indexOf('(') != -1 || value.indexOf(')') != -1
        || value.indexOf('<') != -1 || value.indexOf('>') != -1 || value.indexOf('/') != -1;
  }
}
