/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.ui.spoon;

import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.w3c.dom.Node;

import java.util.Locale;

public interface FileListener {

  public boolean open( Node transNode, String fname, boolean importfile ) throws KettleMissingPluginsException;

  public boolean save( EngineMetaInterface meta, String fname, boolean isExport );

  public void syncMetaName( EngineMetaInterface meta, String name );

  public boolean accepts( String fileName );

  public boolean acceptsXml( String nodeName );

  public String[] getSupportedExtensions();

  public String[] getFileTypeDisplayNames( Locale locale );

  public String getRootNodeName();
}
