/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.ui.trans.steps.jsoninput.getfields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.di.trans.steps.jsoninput.json.Configuration;
import org.pentaho.di.trans.steps.jsoninput.json.JsonSampler;
import org.pentaho.di.trans.steps.jsoninput.json.node.ArrayNode;
import org.pentaho.di.trans.steps.jsoninput.json.node.Node;
import org.pentaho.di.trans.steps.jsoninput.json.node.ObjectNode;

import java.io.InputStream;

/**
 * Created by bmorrise on 8/7/18.
 */
public class JsonSamplerTest {

  public static final String ITEMARR = "itemarr: [\n";
  public static final String ITEM_3_VALUE_3 = "item3: value3\n";
  public static final String ITEM_1_VALUE_1 = "item1: value1\n";
  public static final String ITEM_2_VALUE_2 = "item2: value2\n";
  public static final String ITEM_3 = "item3: {\n";

  @Test
  @Ignore("UI elements make this unit test unreliable.")
  public void testDedupeArray() throws Exception {
    JsonSampler jsonSampler = new JsonSampler();
    InputStream inputStream =
      this.getClass().getResourceAsStream( "/org/pentaho/di/ui/trans/steps/jsoninput/getfields/dedupe-test.json" );
    try {
      final Tree tree = new Tree( new Shell(), SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
      ArrayNode node = (ArrayNode) jsonSampler.sample( inputStream, tree );
      Assert.assertEquals( "[\n"
        + "{\n"
        + ITEMARR
        + "{\n"
        + ITEM_1_VALUE_1
        + ITEM_3_VALUE_3
        + ITEM_2_VALUE_2
        + "},\n"
        + "],\n"
        + ITEM_1_VALUE_1
        + ITEM_3
        + "item4: value4\n"
        + "item5: value5\n"
        + "item6: value6\n"
        + "},\n"
        + ITEM_2_VALUE_2
        + "},\n"
        + "],\n", node.toString() );
    } catch ( UnsatisfiedLinkError e ) {
      // Do nothing
    }
  }

  @Test
  @Ignore("UI elements make this unit test unreliable.")
  public void testDedupeObject() throws Exception {
    JsonSampler jsonSampler = new JsonSampler();
    InputStream inputStream =
      this.getClass().getResourceAsStream( "/org/pentaho/di/ui/trans/steps/jsoninput/getfields/dedupe-test2.json" );
    try {
      final Tree tree = new Tree( new Shell(), SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
      ObjectNode node = (ObjectNode) jsonSampler.sample( inputStream, tree );
      Assert.assertEquals( "{\n"
        + "data: [\n"
        + "{\n"
        + ITEMARR
        + "{\n"
        + ITEM_1_VALUE_1
        + ITEM_3_VALUE_3
        + ITEM_2_VALUE_2
        + "},\n"
        + "],\n"
        + ITEM_1_VALUE_1
        + ITEM_3
        + "item4: true\n"
        + "item5: 1.0\n"
        + "item6: value6\n"
        + "},\n"
        + ITEM_2_VALUE_2
        + "},\n"
        + "],\n"
        + "},\n", node.toString() );
    } catch ( NoClassDefFoundError | UnsatisfiedLinkError e ) {
      // Do nothing
    }
  }

  @Test
  @Ignore("UI elements make this unit test unreliable.")
  public void testDedupeLines() throws Exception {
    Configuration configuration = new Configuration();
    configuration.setLines( 10 );
    JsonSampler jsonSampler = new JsonSampler( configuration );
    InputStream inputStream =
      this.getClass().getResourceAsStream( "/org/pentaho/di/ui/trans/steps/jsoninput/getfields/dedupe-test2.json" );
    try {
      final Tree tree = new Tree( new Shell(), SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
      Node node = jsonSampler.sample( inputStream, tree );
      Assert.assertEquals( "{\n"
        + "data: [\n"
        + "{\n"
        + ITEMARR
        + "{\n"
        + ITEM_1_VALUE_1
        + ITEM_3_VALUE_3
        + "},\n"
        + "],\n"
        + ITEM_1_VALUE_1
        + ITEM_3
        + "item4: true\n"
        + "item5: 1.0\n"
        + "},\n"
        + ITEM_2_VALUE_2
        + "},\n"
        + "],\n"
        + "},\n", node.toString() );
    } catch ( UnsatisfiedLinkError | NoClassDefFoundError ex ) {
      // Do nothing
    }
  }


  @Test
  @Ignore("UI elements make this unit test unreliable.")
  public void testDedupeNestedArrays() throws Exception {
    Configuration configuration = new Configuration();
    JsonSampler jsonSampler = new JsonSampler( configuration );
    InputStream inputStream =
      this.getClass().getResourceAsStream( "/org/pentaho/di/ui/trans/steps/jsoninput/getfields/dedupe-test3.json" );
    try {
      //
      final Tree tree = new Tree( new Shell(), SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
      Node node = jsonSampler.sample( inputStream, tree );
      Assert.assertEquals( "[\n"
        + "{\n"
        + "item: [\n"
        + "{\n"
        + "five: five\n"
        + "one: two\n"
        + "seven: example\n"
        + "nine: [\n"
        + "[\n"
        + "{\n"
        + "test: airplane\n"
        + "example: {\n"
        + "why: none\n"
        + "},\n"
        + "},\n"
        + "],\n"
        + "],\n"
        + "},\n"
        + "],\n"
        + "},\n"
        + "],\n", node.toString() );
    } catch ( UnsatisfiedLinkError | NoClassDefFoundError ex) {
      // Do nothing
    }
  }

  @Test
  @Ignore("UI elements make this unit test unreliable.")
  public void testBigIntegerNode() throws Exception {
    Configuration configuration = new Configuration();
    JsonSampler jsonSampler = new JsonSampler( configuration );
    InputStream inputStream =
      this.getClass().getResourceAsStream( "/org/pentaho/di/ui/trans/steps/jsoninput/getfields/bigint-test.json" );
    try {
      final Tree tree = new Tree( new Shell(), SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
      Node node = jsonSampler.sample( inputStream, tree );
      Assert.assertEquals( "[\n"
        + "{\n"
        + ITEMARR
        + "{\n"
        + "itemId: 373208832580648960\n"
        + "},\n"
        + "],\n"
        + "item1: 12345\n"
        + ITEM_3
        + "item4: value4\n"
        + "},\n"
        + "},\n"
        + "],\n", node.toString() );
    } catch ( NoClassDefFoundError | UnsatisfiedLinkError e ) {
      // Do nothing
    }
  }
}
