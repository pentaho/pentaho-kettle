/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.spinnerkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderClientListeners;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenModifyVerify;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListener;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;

import java.io.IOException;
import java.text.DecimalFormatSymbols;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Spinner;


public final class SpinnerLCA extends WidgetLCA<Spinner> {

  public static final SpinnerLCA INSTANCE = new SpinnerLCA();

  private static final String TYPE = "rwt.widgets.Spinner";
  private static final String[] ALLOWED_STYLES = { "READ_ONLY", "WRAP", "BORDER" };

  // Property names for preserveValues
  static final String PROP_MINIMUM = "minimum";
  static final String PROP_MAXIMUM = "maximum";
  static final String PROP_SELECTION = "selection";
  static final String PROP_DIGITS = "digits";
  static final String PROP_INCREMENT = "increment";
  static final String PROP_PAGE_INCREMENT = "pageIncrement";
  static final String PROP_TEXT_LIMIT = "textLimit";
  static final String PROP_DECIMAL_SEPARATOR = "decimalSeparator";
  static final String PROP_SELECTION_LISTENER = "Selection";
  static final String PROP_DEFAULT_SELECTION_LISTENER = "DefaultSelection";

  // Default values
  private static final int DEFAULT_MINIMUM = 0;
  private static final int DEFAULT_MAXIMUM = 100;
  private static final int DEFAULT_SELECTION = 0;
  private static final int DEFAULT_DIGITS = 0;
  private static final int DEFAULT_INCREMENT = 1;
  private static final int DEFAULT_PAGE_INCREMENT = 10;
  private static final String DEFAULT_DECIMAL_SEPARATOR = ".";

  @Override
  public void preserveValues( Spinner spinner ) {
    preserveProperty( spinner, PROP_MINIMUM, spinner.getMinimum() );
    preserveProperty( spinner, PROP_MAXIMUM, spinner.getMaximum() );
    preserveProperty( spinner, PROP_SELECTION, spinner.getSelection() );
    preserveProperty( spinner, PROP_DIGITS, spinner.getDigits() );
    preserveProperty( spinner, PROP_INCREMENT, spinner.getIncrement() );
    preserveProperty( spinner, PROP_PAGE_INCREMENT, spinner.getPageIncrement() );
    preserveProperty( spinner, PROP_TEXT_LIMIT, getTextLimit( spinner ) );
    preserveProperty( spinner, PROP_DECIMAL_SEPARATOR, getDecimalSeparator() );
  }

  @Override
  public void renderInitialization( Spinner spinner ) throws IOException {
    RemoteObject remoteObject = createRemoteObject( spinner, TYPE );
    remoteObject.setHandler( new SpinnerOperationHandler( spinner ) );
    remoteObject.set( "parent", getId( spinner.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( spinner, ALLOWED_STYLES ) ) );
  }

  @Override
  public void renderChanges( Spinner spinner ) throws IOException {
    ControlLCAUtil.renderChanges( spinner );
    WidgetLCAUtil.renderCustomVariant( spinner );
    renderMinimum( spinner );
    renderMaximum( spinner );
    renderSelection( spinner );
    renderDigits( spinner );
    renderIncrement( spinner );
    renderPageIncrement( spinner );
    renderTextLimit( spinner );
    renderDecimalSeparator( spinner );
    renderListenModifyVerify( spinner );
    renderListenSelection( spinner );
    renderClientListeners( spinner );
  }

  ///////////////////////////////////////////////////
  // Helping methods to render the changed properties

  private static void renderMinimum( Spinner spinner ) {
    renderProperty( spinner, PROP_MINIMUM, spinner.getMinimum(), DEFAULT_MINIMUM );
  }

  private static void renderMaximum( Spinner spinner ) {
    renderProperty( spinner, PROP_MAXIMUM, spinner.getMaximum(), DEFAULT_MAXIMUM );
  }

  private static void renderSelection( Spinner spinner ) {
    renderProperty( spinner, PROP_SELECTION, spinner.getSelection(), DEFAULT_SELECTION );
  }

  private static void renderDigits( Spinner spinner ) {
    renderProperty( spinner, PROP_DIGITS, spinner.getDigits(), DEFAULT_DIGITS );
  }

  private static void renderIncrement( Spinner spinner ) {
    renderProperty( spinner, PROP_INCREMENT, spinner.getIncrement(), DEFAULT_INCREMENT );
  }

  private static void renderPageIncrement( Spinner spinner ) {
    int defValue = DEFAULT_PAGE_INCREMENT;
    renderProperty( spinner, PROP_PAGE_INCREMENT, spinner.getPageIncrement(), defValue );
  }

  private static void renderTextLimit( Spinner spinner ) {
    renderProperty( spinner, PROP_TEXT_LIMIT, getTextLimit( spinner ), null );
  }

  private static void renderDecimalSeparator( Spinner spinner ) {
    String defValue = DEFAULT_DECIMAL_SEPARATOR;
    renderProperty( spinner, PROP_DECIMAL_SEPARATOR, getDecimalSeparator(), defValue );
  }

  private static void renderListenSelection( Spinner spinner ) {
    renderListener( spinner, SWT.Selection, PROP_SELECTION_LISTENER );
    renderListener( spinner, SWT.DefaultSelection, PROP_DEFAULT_SELECTION_LISTENER );
  }

  private static Integer getTextLimit( Spinner spinner ) {
    Integer result = null;
    int textLimit = spinner.getTextLimit();
    if( textLimit > 0 && textLimit != Spinner.LIMIT ) {
      result = Integer.valueOf( textLimit );
    }
    return result;
  }

  private static String getDecimalSeparator() {
    DecimalFormatSymbols symbols = new DecimalFormatSymbols( RWT.getLocale() );
    return String.valueOf( symbols.getDecimalSeparator() );
  }

  private SpinnerLCA() {
    // prevent instantiation
  }

}
