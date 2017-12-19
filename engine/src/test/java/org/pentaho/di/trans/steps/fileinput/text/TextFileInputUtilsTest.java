/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.fileinput.text;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.steps.file.BaseFileField;

public class TextFileInputUtilsTest {
  @Test
  public void guessStringsFromLine() throws Exception {
    TextFileInputMeta inputMeta = Mockito.mock( TextFileInputMeta.class );
    inputMeta.content = new TextFileInputMeta.Content();
    inputMeta.content.fileType = "CSV";

    String line = "\"\\\\valueA\"|\"valueB\\\\\"|\"val\\\\ueC\""; // "\\valueA"|"valueB\\"|"val\\ueC"

    String[] strings = TextFileInputUtils
      .guessStringsFromLine( Mockito.mock( VariableSpace.class ), Mockito.mock( LogChannelInterface.class ),
        line, inputMeta, "|", "\"", "\\" );
    Assert.assertNotNull( strings );
    Assert.assertEquals( "\\valueA", strings[ 0 ] );
    Assert.assertEquals( "valueB\\", strings[ 1 ] );
    Assert.assertEquals( "val\\ueC", strings[ 2 ] );
  }

  @Test
  public void convertLineToStrings() throws Exception {
    TextFileInputMeta inputMeta = Mockito.mock( TextFileInputMeta.class );
    inputMeta.content = new TextFileInputMeta.Content();
    inputMeta.content.fileType = "CSV";
    inputMeta.inputFields = new BaseFileField[ 3 ];
    inputMeta.content.escapeCharacter = "\\";

    String line = "\"\\\\fie\\\\l\\dA\"|\"fieldB\\\\\"|\"fie\\\\ldC\""; // ""\\fie\\l\dA"|"fieldB\\"|"Fie\\ldC""

    String[] strings = TextFileInputUtils
      .convertLineToStrings( Mockito.mock( LogChannelInterface.class ), line, inputMeta, "|", "\"", "\\" );
    Assert.assertNotNull( strings );
    Assert.assertEquals( "\\fie\\l\\dA", strings[ 0 ] );
    Assert.assertEquals( "fieldB\\", strings[ 1 ] );
    Assert.assertEquals( "fie\\ldC", strings[ 2 ] );
  }

}
