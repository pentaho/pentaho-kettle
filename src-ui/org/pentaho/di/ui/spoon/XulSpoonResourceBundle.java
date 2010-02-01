package org.pentaho.di.ui.spoon;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import org.pentaho.di.i18n.BaseMessages;

/**
 * Static class wrapping Spoon's Messages class and Look and Feel bundle.
 * 
 * @author nbaker
 */
public class XulSpoonResourceBundle extends ResourceBundle{

  private static ResourceBundle lafBundle;
  private static Class PKG = Spoon.class;
  
  static{
    URL url = null;
    try{
      url = new File(".").toURL();
    } catch(MalformedURLException ex){}
    URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{url});
    
    lafBundle = ResourceBundle.getBundle("ui/laf", Locale.getDefault(), classLoader);
  }
  
  @Override
  public Enumeration<String> getKeys() {
    return null;
  }

  @Override
  protected Object handleGetObject(String key) {
    String result = BaseMessages.getString(PKG, key);
    if(result.indexOf('!') == 0 && result.lastIndexOf('!') == result.length() - 1){
      result = lafBundle.getString(key);
    }
    return result;
  }
  
}
