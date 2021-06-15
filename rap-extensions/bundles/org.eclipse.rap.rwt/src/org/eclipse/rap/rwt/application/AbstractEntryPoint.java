/*******************************************************************************
 * Copyright (c) 2013, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.application;

import static org.eclipse.rap.rwt.RWT.getClient;
import static org.eclipse.rap.rwt.internal.service.ContextProvider.getApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.rap.rwt.client.service.StartupParameters;
import org.eclipse.rap.rwt.internal.lifecycle.RWTLifeCycle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


/**
 * This class provides a skeletal implementation of the <code>EntryPoint</code> interface, to
 * minimize the effort required to implement this interface.
 * <p>
 * By default, this implementation creates a maximized main shell without any trimmings. Subclasses
 * must implement <code>createContents</code> to create the contents of the main shell. In case a
 * different type of main shell is required, subclasses may also override <code>createShell</code>.
 * </p>
 * <p>
 * This class is compatible with all operation modes of RWT. It is recommended to extend this base
 * class rather than to implement the EntryPoint interface itself.
 * </p>
 *
 * @since 2.0
 */
public abstract class AbstractEntryPoint implements EntryPoint, StartupParameters {

  private Display display;
  private Shell shell;

  /**
   * This method is called by the framework to initialize the UI. Subclasses should implement
   * {@link #createContents(Composite)} instead of overriding this method.
   *
   * @nooverride This method is not intended to be re-implemented or extended by clients.
   */
  @Override
  public int createUI() {
    display = new Display();
    shell = createShell( display );
    shell.setLayout( new GridLayout( 1, false ) );
    createContents( shell );
    if( shell.getMaximized() ) {
      shell.layout();
    } else {
      shell.pack();
    }
    shell.open();
    if( getApplicationContext().getLifeCycleFactory().getLifeCycle() instanceof RWTLifeCycle ) {
      while( !shell.isDisposed() ) {
        if( !display.readAndDispatch() ) {
          display.sleep();
        }
      }
      display.dispose();
    }
    return 0;
  }

  /**
   * Returns the names of the entrypoint startup parameters.
   *
   * @return a (possibly empty) collection of parameter names
   * @since 3.0
   */
  @Override
  public Collection<String> getParameterNames() {
    StartupParameters service = getClient().getService( StartupParameters.class );
    return service == null ? new ArrayList<String>() : service.getParameterNames();
  }

  /**
   * Returns the value of a named entrypoint startup parameter. You should only use this method
   * when you are sure the parameter has only one value. If the parameter might have more than one
   * value, use {@link #getParameterValues}.
   *
   * If you use this method with a multivalued parameter, the value returned is equal to the first
   * value in the list returned by <code>getParameterValues</code>.
   *
   * @param name the name of the parameter
   * @return the value of the parameter, or <code>null</code> if the parameter does not exist
   * @since 3.0
   */
  @Override
  public String getParameter( String name ) {
    StartupParameters service = getClient().getService( StartupParameters.class );
    return service == null ? null : service.getParameter( name );
  }

  /**
   * Returns a list with values of a named entrypoint startup parameter.
   *
   * If the parameter has a single value, the list has a size of 1.
   *
   * @param name the name of the parameter
   * @return the values of the parameter, or <code>null</code> if the parameter does not exist
   * @since 3.0
   */
  @Override
  public List<String> getParameterValues( String name ) {
    StartupParameters service = getClient().getService( StartupParameters.class );
    return service == null ? null : service.getParameterValues( name );
  }

  /**
   * Returns the main shell for this entrypoint.
   *
   * @return the main shell, or <code>null</code> if the shell is not created yet
   */
  protected Shell getShell() {
    return shell;
  }

  /**
   * Creates the controls that constitute the UI for this entrypoint. Subclasses must implement this
   * method and set the parent's layout as needed.
   * <p>
   * An implementation must not create an SWT event loop. This is done by the base class as needed.
   * </p>
   *
   * @param parent the parent composite to contain the content
   */
  protected abstract void createContents( Composite parent );

  /**
   * Creates the main shell for this entrypoint. The default implementation creates a maximized
   * shell without any trimmings. Subclasses may override this method in order to create a different
   * kind of shell.
   *
   * @param display the display to create the shell on
   * @return the created shell
   */
  protected Shell createShell( Display display ) {
    Shell shell = new Shell( display, SWT.NO_TRIM );
    shell.setMaximized( true );
    return shell;
  }

}
