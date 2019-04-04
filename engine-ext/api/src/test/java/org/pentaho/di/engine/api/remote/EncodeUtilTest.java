/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.engine.api.remote;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Test class for the basic functionality of EncodeUtil.
 *
 * @author Joe Rice 1/29/2018
 */
public class EncodeUtilTest implements Serializable {
  @Test
  public void testDecodeBase64Zipped() throws Exception {
    TestClass testClass = new TestClass();
    testClass.setTestProp1( "testPropValue1" );
    testClass.setTestProp2( "testPropValue2" );

    String base64ZippedString = this.encode( testClass );

    TestClass reconstructedTestClass = (TestClass) this.decode( base64ZippedString );

    Assert.assertNotNull( reconstructedTestClass );
    Assert.assertEquals( reconstructedTestClass.getTestProp1(), testClass.getTestProp1() );
    Assert.assertEquals( reconstructedTestClass.getTestProp2(), testClass.getTestProp2() );
  }

  @Test
  public void testEncodeBase64Zipped() throws Exception {
    TestClass testClass = new TestClass();
    testClass.setTestProp1( "testPropValue1" );
    testClass.setTestProp2( "testPropValue2" );

    String base64ZippedString = this.encode( testClass );

    Assert.assertNotNull( base64ZippedString );
    Assert.assertTrue( !base64ZippedString.trim().isEmpty() );
  }

  private Object decode( String string ) throws Exception {
    byte[] data = EncodeUtil.decodeBase64Zipped( string );
    InputStream is = new ByteArrayInputStream( data );
    ObjectInputStream ois = new ObjectInputStream( is );
    Object o = ois.readObject();
    ois.close();
    return o;
  }

  public String encode( Object object ) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream( baos );
    oos.writeObject( object );
    oos.close();
    return EncodeUtil.encodeBase64Zipped( baos.toByteArray() );
  }

  private class TestClass implements Serializable {
    private String testProp1;
    private String testProp2;

    public void setTestProp1( String testProp1 ) {
      this.testProp1 = testProp1;
    }

    public void setTestProp2( String testProp2 ) {
      this.testProp2 = testProp2;
    }

    public String getTestProp1() {
      return testProp1;
    }

    public String getTestProp2() {
      return testProp2;
    }
  }

}
