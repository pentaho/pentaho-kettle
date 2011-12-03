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
 */
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
