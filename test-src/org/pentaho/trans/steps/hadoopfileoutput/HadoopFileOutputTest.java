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

package org.pentaho.trans.steps.hadoopfileoutput;

import org.junit.Test;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.hadoopfileoutput.HadoopFileOutputMeta;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class HadoopFileOutputTest {

   // for message resolution
   private static Class<?> MessagePKG = HadoopFileOutputMeta.class;
   
   /**
    *  Tests HadoopFileOutputMeta methods:
    *    1.  isFileAsCommand returns false
    *    2.  setFileAsCommand is not supported
    */
   @Test
   public void testFileAsCommandOption() {
      
      HadoopFileOutputMeta hadoopFileOutputMeta = new HadoopFileOutputMeta();
      
      //  we expect isFileAsCommand to be false
      assertFalse(hadoopFileOutputMeta.isFileAsCommand());
      
      // we expect setFileAsCommand(true or false) to be unsupported
      try {
         hadoopFileOutputMeta.setFileAsCommand(true);
      }
      catch (Exception e) {
         //  the expected message is  "class name":" message from the package that HadoopFileOutputMeta is in
         String expectedMessage = e.getClass().getName()+": "+BaseMessages.getString(MessagePKG, "HadoopFileOutput.MethodNotSupportedException.Message");
         assertTrue(e.getMessage().equals(expectedMessage));
      }     
   }
}
