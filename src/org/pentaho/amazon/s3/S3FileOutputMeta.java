/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.amazon.s3;

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;

import org.pentaho.amazon.AmazonSpoonPlugin;

@Step(id = "S3FileOutputPlugin", image = "S3O.png", name = "S3 File Output", description = "Create files in an S3 location", categoryDescription = "Output")
public class S3FileOutputMeta extends TextFileOutputMeta {

  private String accessKey = null;
  private String secretKey = null;

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

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
//    if (retval.startsWith(AmazonSpoonPlugin.S3_SCHEME)) {
//      String authPart = retval.substring(AmazonSpoonPlugin.S3_SCHEME.length() + 3, retval.indexOf("@s3")).replaceAll("\\+", "%2B").replaceAll("/", "%2F");
//      retval = AmazonSpoonPlugin.S3_SCHEME + "://" + authPart + "@s3" + retval.substring(retval.indexOf("@s3")+3);
//    }
    return retval;
  }

}