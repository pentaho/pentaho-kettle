/*! ****************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.trans.steps.avro;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AvroFunctionalTest {

  @Test
  public void shouldSerializeAndDeserializeValidSchema() throws Exception {
    String schemaStr = """
        {
          "type": "record",
          "name": "User",
          "fields": [
            {"name": "name", "type": "string"},
            {"name": "age", "type": "int"}
          ]
        }
        """;

    Schema schema = new Schema.Parser().parse(schemaStr);
    GenericRecord record = new GenericData.Record(schema);

    record.put("name", "Alice");
    record.put("age", 30);

    assertEquals("Alice", record.get("name").toString());
    assertEquals(30, record.get("age"));
  }
}

