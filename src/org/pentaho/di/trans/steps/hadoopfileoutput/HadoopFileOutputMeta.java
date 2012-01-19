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

package org.pentaho.di.trans.steps.hadoopfileoutput;

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;

@Step(id = "HadoopFileOutputPlugin", image = "HDO.png", name = "Hadoop File Output", description="Create files in an HDFS location", categoryDescription="Big Data")
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