package org.pentaho.di.ui.spoon;

/**
 * Implementations can be registered with SpoonPerspectives to receive notification of state changes.
 * 
 * @author nbaker
 *
 */
public interface SpoonPerspectiveListener {
  void onActivation();
  void onDeactication();
}
