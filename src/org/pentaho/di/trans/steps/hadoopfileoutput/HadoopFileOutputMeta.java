/*
 * Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @author Michael D'Amour
 */

package org.pentaho.di.trans.steps.hadoopfileoutput;

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;

@Step(id = "HadoopFileOutputPlugin", image = "HDO.png", name = "Hadoop File Output", description="Create files in an HDFS location", categoryDescription="Hadoop")
public class HadoopFileOutputMeta extends TextFileOutputMeta {

    // for message resolution
    private static Class<?> PKG = HadoopFileOutputMeta.class;
   
    @Override
    public void setDefault()
    {
        //  call the base classes method
        super.setDefault();
        
        // now set the default for the 
        // filename to an empty string
        setFileName("");
        super.setFileAsCommand(false);
    }
    
    @Override
    public void setFileAsCommand(boolean fileAsCommand) {
      //  Don't do anything.  We want to keep this property as false
      //  Throwing a KettleStepException would be desirable but then we 
      //  need to change the base class' method which is 
      //  open source.
       
      throw new RuntimeException(new RuntimeException(BaseMessages.getString(PKG, "HadoopFileOutput.MethodNotSupportedException.Message")));
    }
    
}