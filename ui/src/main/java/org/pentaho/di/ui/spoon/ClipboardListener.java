package org.pentaho.di.ui.spoon;

public interface ClipboardListener {

  /**
   * Return the Id of the associated widget.
   * See {@link org.eclipse.rap.rwt.widgets.WidgetUtil#getId(Widget)}.
   * @return
   */
  String getWidgetId();
  
  void pasteListener( String text );

  void cutListener();

}
