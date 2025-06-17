package org.pentaho.di.ui.spoon.tree.extension;

import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.core.exception.KettleException;

import java.util.ArrayList;
import java.util.List;

/**
 * The class manages the list of UIExtensions.
 */
public class UIExtensionManager {

  private final List<UIExtension> extensions = new ArrayList<>();

  public void addUIExtension( UIExtension extension ) {
    extensions.add( extension );
  }

  /**
   * It loops through the list of extensions and builds each of the extensions
   * @param main
   * @throws KettleException
   */
  public void buildUIExtensions( Composite main ) throws KettleException {
    try {
      for ( UIExtension extension : extensions ) {
        extension.buildExtension( main );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Exception building UI extensions", e );
    }
  }
}
