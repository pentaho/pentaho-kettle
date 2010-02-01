package org.pentaho.di.ui.spoon;

/**
 * Registered implementations will be notified of Spoon startup and shutdown. This class will
 * most likely be registered as part of a SpoonPlugin.
 * 
 * @author nbaker
 *
 */
public interface SpoonLifecycleListener {
  public enum LifeCycleEvent{STARTUP, SHUTDOWN};
  void onEvent(LifeCycleEvent evt);
}
