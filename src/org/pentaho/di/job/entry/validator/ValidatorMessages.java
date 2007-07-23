package org.pentaho.di.job.entry.validator;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class for getting formatted strings from validator resource bundle.
 *
 * @author mlowery
 */
public class ValidatorMessages {

  private static final String BUNDLE_NAME = "org.pentaho.di.job.entry.messages.validator"; //$NON-NLS-1$

  public static String getString(final String key, final Object... params) {
    return getStringFromBundle(BUNDLE_NAME, key, params);
  }

  public static String getStringFromBundle(final String bundleName, final String key, final Object... params) {
    ResourceBundle bundle = null;
    try {
      bundle = ResourceBundle.getBundle(bundleName, Locale.getDefault());
    } catch (MissingResourceException e) {
      return "??? missing resource ???"; //$NON-NLS-1$
    } catch (NullPointerException e) {
      return "??? baseName null ???"; //$NON-NLS-1$
    }
    String unformattedString = null;
    try {
      unformattedString = bundle.getString(key);
    } catch (Exception e) {
      return "??? " + key + " ???"; //$NON-NLS-1$ //$NON-NLS-2$
    }
    String formattedString = MessageFormat.format(unformattedString, params);
    return formattedString;
  }

}
