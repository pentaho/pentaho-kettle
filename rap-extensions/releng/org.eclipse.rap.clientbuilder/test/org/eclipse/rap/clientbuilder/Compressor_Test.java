/*******************************************************************************
 * Copyright (c) 2010, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.clientbuilder;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;


public class Compressor_Test {

  @Test
  public void testCompressEmpty() throws IOException {
    assertEquals( "", TestUtil.compress( "" ) );
  }

  @Test
  public void testCompressNumbers() throws IOException {
    assertEquals( "23;", TestUtil.compress( "23" ) );
    assertEquals( "23;", TestUtil.compress( " 23.0 " ) );
  }

  @Test
  public void testCompressStrings() throws IOException {
    assertEquals( "\"\";", TestUtil.compress( "''" ) );
    assertEquals( "\"a\";", TestUtil.compress( "'a'" ) );
  }

  @Test
  public void testCompressExpressions() throws IOException {
    assertEquals( "23+\"\";", TestUtil.compress( " 23 + ''" ) );
  }

  @Test
  public void testCompressEscapes() throws IOException {
    assertEquals( "\"\";", TestUtil.compress( "\"\"" ) );
    assertEquals( "\"\\\\\";", TestUtil.compress( "\"\\\\\"" ) );
    // Unicode characters are not escaped as the output file is in UTF-8
    assertEquals( "\"\u0416\";", TestUtil.compress( "\"\u0416\"" ) );
    // Unicode escapes are transformed into Unicode characters
    assertEquals( "\"\u0416\";", TestUtil.compress( "\"\\u0416\"" ) );
    assertEquals( "\"\u00CF\";", TestUtil.compress( "\"\\xCF\"" ) );
  }

}
