/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.ui.spoon;

import java.util.Locale;

import org.pentaho.di.core.EngineMetaInterface;
import org.w3c.dom.Node;

public interface FileListener {

  public boolean open(Node transNode, String fname, boolean importfile);

  public boolean save(EngineMetaInterface meta, String fname,boolean isExport);
  
  public void syncMetaName(EngineMetaInterface meta,String name);
  
  public boolean accepts(String fileName);
  
  public boolean acceptsXml(String nodeName);
  
  public String[] getSupportedExtensions();
  
  public String[] getFileTypeDisplayNames(Locale locale);
  
  public String getRootNodeName();
}
