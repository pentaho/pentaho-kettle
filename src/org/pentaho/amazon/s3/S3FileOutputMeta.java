/* Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
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
/*
 * @author Michael D'Amour
 */
package org.pentaho.amazon.s3;

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;

import org.pentaho.amazon.AmazonSpoonPlugin;

@Step(id = "S3FileOutputPlugin", image = "S3O.png", name = "S3 File Output", description = "Create files in an S3 location", categoryDescription = "Output")
public class S3FileOutputMeta extends TextFileOutputMeta {

  @Override
  public void setDefault() {
    // call the base classes method
    super.setDefault();

    // now set the default for the
    // filename to an empty string
    setFileName("");
  }

  public String buildFilename(String filename, String extension, VariableSpace space, int stepnr, String partnr, int splitnr, boolean ziparchive,
      TextFileOutputMeta meta) {
    String retval = super.buildFilename(filename, extension, space, stepnr, partnr, splitnr, ziparchive, meta);
    if (retval.startsWith(AmazonSpoonPlugin.S3_SCHEME)) {
      String authPart = retval.substring(AmazonSpoonPlugin.S3_SCHEME.length() + 3, retval.indexOf("@s3")).replaceAll("\\+", "%2B").replaceAll("/", "%2F");
      retval = AmazonSpoonPlugin.S3_SCHEME + "://" + authPart + "@s3" + retval.substring(retval.indexOf("@s3")+3);
    }
    return retval;
  }

}