/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created July 10, 2007 
 * @author Marc Batchelor
 * 
 */
package org.pentaho.di.resource;

import org.pentaho.di.core.util.UUIDUtil;

public class UUIDResourceRename implements ResourceRenameInterface {

  public String generateResourceName(String extension) {
    return generateResourceName(extension, null);
  }

  public String generateResourceName(String extension, String prefix) {
    String uuid = UUIDUtil.getUUIDAsString();
    return (prefix != null ? prefix : "") +  //$NON-NLS-1$
           uuid + 
           ( ( extension != null && extension.length() > 0 ) ? (extension.charAt(0) == '.' ? extension : "." + extension) : ""); //$NON-NLS-1$ //$NON-NLS-2$
  }

}
