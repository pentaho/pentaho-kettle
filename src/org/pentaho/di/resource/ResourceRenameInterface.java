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
 */
package org.pentaho.di.resource;

/**
 * The purpose of this interface is to provide a re-callable interface for re-naming
 * resources during job or transformation export.
 * 
 * @created Jul 10, 2007
 * @author Marc Batchelor
 *
 */
public interface ResourceRenameInterface {


  /**
   * Generate a new resource name using the provided extension. The
   * extension is appended to the end of the generated name.
   * @param extension the resource extension (like .xml or .ktr)
   * @return Newly generated resource name
   */
  public String generateResourceName(String extension);
  
  /**
   * Generates a new resource name using the provided prefix and
   * extension. The extension is appended to the end of the
   * generated name. The prefix will be pre-pended to the generated
   * name.
   * @param extension the resource extension (like .ktr or .kjb)
   * @param prefix the resource prefix (like zip://somefile! or C:/temp/export/)
   * @return The constructed new file name.
   */
  public String generateResourceName(String extension, String prefix);
  
}
