/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.logging;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith( PowerMockRunner.class )
public class LoggingRegistryTest {
  public static final String LOG_CHANEL_ID_PARENT = "parent-chanel-id";
  public static final String LOG_CHANEL_ID_CHILD = "child-chanel-id";
  public static final String STRING_DEFAULT = "<def>";

  @Test
  public void correctLogIdReturned_WhenLogObjectRegisteredAlready() {
    LoggingRegistry loggingRegistry = LoggingRegistry.getInstance();

    LoggingObject parent = new LoggingObject( new SimpleLoggingObject( "parent", LoggingObjectType.TRANS, null ) );
    parent.setLogChannelId( LOG_CHANEL_ID_PARENT );

    LoggingObject child = new LoggingObject( new SimpleLoggingObject( "child", LoggingObjectType.STEP, parent ) );
    child.setLogChannelId( LOG_CHANEL_ID_CHILD );

    loggingRegistry.getMap().put( STRING_DEFAULT, child );

    String logChanelId = loggingRegistry.registerLoggingSource( child );

    assertEquals( logChanelId, LOG_CHANEL_ID_CHILD );
  }

  @Test
  public void testRegisterFileWriter() {
    String id = "1";

    LoggingRegistry loggingRegistry = LoggingRegistry.getInstance();

    LogChannelFileWriterBuffer buffer = new LogChannelFileWriterBuffer( id );
    loggingRegistry.registerLogChannelFileWriterBuffer( buffer );

    assertNotNull( loggingRegistry.getLogChannelFileWriterBuffer( id ) );
  }

  @Test
  public void testFileWritersIds() {
    String id = "1";

    LoggingRegistry loggingRegistry = LoggingRegistry.getInstance();

    LogChannelFileWriterBuffer buffer = new LogChannelFileWriterBuffer( id );
    loggingRegistry.registerLogChannelFileWriterBuffer( buffer );

    assertNotNull( loggingRegistry.getLogChannelFileWriterBufferIds() );
  }

  @Test
  public void testRemoveFileWriter() {
    String id = "1";

    LoggingRegistry loggingRegistry = LoggingRegistry.getInstance();

    LogChannelFileWriterBuffer buffer = new LogChannelFileWriterBuffer( id );
    loggingRegistry.registerLogChannelFileWriterBuffer( buffer );

    loggingRegistry.removeLogChannelFileWriterBuffer( id );

    assertNull( loggingRegistry.getLogChannelFileWriterBuffer( id ) );
  }

  @Test
  public void getLogChannelFileWriterBufferTest() {
    LoggingRegistry loggingRegistry = LoggingRegistry.getInstance();

    Map<String, LogChannelFileWriterBuffer> fileWriterBuffers = getDummyFileWriterBuffers();

    Whitebox.setInternalState( loggingRegistry, "fileWriterBuffers", fileWriterBuffers );
    Whitebox.setInternalState( loggingRegistry, "childrenMap", getDummyChildrenMap() );

    assertEquals( loggingRegistry.getLogChannelFileWriterBuffer( "dcffc35f-c74f-4e37-b463-97313998ea20" ).getLogChannelId(), "dc8c1482-30ab-4d0f-b9f6-e4c32a627bf0" );

    //Switch the order of the writers
    fileWriterBuffers.remove( "7c1526bc-789e-4f5a-8d68-1f9c39488ceb" );
    fileWriterBuffers.put( "7c1526bc-789e-4f5a-8d68-1f9c39488ceb", new LogChannelFileWriterBuffer( "7c1526bc-789e-4f5a-8d68-1f9c39488ceb" ) );
    Whitebox.setInternalState( loggingRegistry, "fileWriterBuffers", fileWriterBuffers );

    //regardless of the order of the writers the correct the same should be selected
    assertEquals( loggingRegistry.getLogChannelFileWriterBuffer( "dcffc35f-c74f-4e37-b463-97313998ea20" ).getLogChannelId(), "dc8c1482-30ab-4d0f-b9f6-e4c32a627bf0" );
  }

  @Test
  public void getLogChannelFileWriterBufferOnlyOnePossibilityTest() {
    LoggingRegistry loggingRegistry = LoggingRegistry.getInstance();

    Map<String, LogChannelFileWriterBuffer> fileWriterBuffers = getDummyFileWriterBuffers();

    fileWriterBuffers.remove( "7c1526bc-789e-4f5a-8d68-1f9c39488ceb" );

    Whitebox.setInternalState( loggingRegistry, "fileWriterBuffers", fileWriterBuffers );
    Whitebox.setInternalState( loggingRegistry, "childrenMap", getDummyChildrenMap() );

    assertEquals( loggingRegistry.getLogChannelFileWriterBuffer( "dcffc35f-c74f-4e37-b463-97313998ea20" ).getLogChannelId(), "dc8c1482-30ab-4d0f-b9f6-e4c32a627bf0" );
  }

  @Test
  public void getLogChannelFileWriterBufferNoPossibilityTest() {
    LoggingRegistry loggingRegistry = LoggingRegistry.getInstance();

    Map<String, LogChannelFileWriterBuffer> fileWriterBuffers = getDummyFileWriterBuffers();

    fileWriterBuffers.clear();

    Whitebox.setInternalState( loggingRegistry, "fileWriterBuffers", fileWriterBuffers );
    Whitebox.setInternalState( loggingRegistry, "childrenMap", getDummyChildrenMap() );

    assertNull( loggingRegistry.getLogChannelFileWriterBuffer( "dcffc35f-c74f-4e37-b463-97313998ea20" ) );
  }

  private Map<String, LogChannelFileWriterBuffer> getDummyFileWriterBuffers() {
    Map<String, LogChannelFileWriterBuffer> dummyFileWriterBuffers = new LinkedHashMap<>(  );

    dummyFileWriterBuffers.put( "7c1526bc-789e-4f5a-8d68-1f9c39488ceb", new LogChannelFileWriterBuffer( "7c1526bc-789e-4f5a-8d68-1f9c39488ceb" ) );
    dummyFileWriterBuffers.put( "dc8c1482-30ab-4d0f-b9f6-e4c32a627bf0", new LogChannelFileWriterBuffer( "dc8c1482-30ab-4d0f-b9f6-e4c32a627bf0" ) );

    return dummyFileWriterBuffers;
  }

  private Map<String, List<String>> getDummyChildrenMap() {
    Map<String, List<String>> childrenMap = new ConcurrentHashMap<>(  );

    childrenMap.put( "4d345a50-4dc7-4d90-97d2-4123aa43d28f", new ArrayList<>( Arrays.asList( "23c0f002-7071-4c08-9b4a-cc179d206540", "d4bb8bbf-c765-49b6-9b85-f03c8621ba32" ) ) );
    childrenMap.put( "bf3eb602-01c3-48d7-b357-7c28362fe4b9", new ArrayList<>( Arrays.asList( "2f1d7562-1aab-4a5d-aa90-ff1f410b9f52", "dc8c1482-30ab-4d0f-b9f6-e4c32a627bf0" ) ) );
    childrenMap.put( "267315ad-d616-4d1a-8116-747dcb381ed4", new ArrayList<>( Arrays.asList( "0ac4993d-ebae-453c-850b-7e733c35b28a", "486da3f1-38d5-4151-b9c4-fb5b4978afe9" ) ) );
    childrenMap.put( "1c33fa43-9a14-4191-8124-6f929421d744", new ArrayList<>( Arrays.asList( "7f1c7986-6373-4cd1-8638-cff7a6b1e7ec" ) ) );
    childrenMap.put( "92d7d8f7-0cb8-416c-b728-a1d7fb998b8f", new ArrayList<>( Arrays.asList( "6095cfa7-8086-4fd0-83bf-8b191d85e16f" ) ) );
    childrenMap.put( "9f1a8771-4934-4567-8d55-1deb7f0f3640", new ArrayList<>( Arrays.asList( "f4a87ce6-ce8f-4e96-87f5-9cbfef8a0676" ) ) );
    childrenMap.put( "d4bb8bbf-c765-49b6-9b85-f03c8621ba32", new ArrayList<>( Arrays.asList( "dc2cc8c4-bdd6-4ebe-ac9e-8794bea3f9ef" ) ) );
    childrenMap.put( "689cd0aa-96c7-4e79-9d04-e5f81f9d6aaf", new ArrayList<>( Arrays.asList( "267315ad-d616-4d1a-8116-747dcb381ed4" ) ) );
    childrenMap.put( "dc2cc8c4-bdd6-4ebe-ac9e-8794bea3f9ef", new ArrayList<>( Arrays.asList( "cbe78766-f334-46eb-8863-9be04f379b2e", "689cd0aa-96c7-4e79-9d04-e5f81f9d6aaf","5f8cade2-c847-4fb8-8994-f0d4410c5591" ) ) );
    childrenMap.put( "5f8cade2-c847-4fb8-8994-f0d4410c5591", new ArrayList<>( Arrays.asList( "8a7d5e5e-ef7b-4c5d-9e41-92f46aa84a79" ) ) );
    childrenMap.put( "f443db9e-e486-4a48-aa6f-073b53729ac3", new ArrayList<>( Arrays.asList( "305365f2-617c-4fc3-b01d-49472fd2a947", "61cfe589-7895-4030-8e15-bd1590f9888f" ) ) );
    childrenMap.put( "6276b8a5-6fcd-40ab-a112-f97fd2175150", new ArrayList<>( Arrays.asList( "f59c3faa-e953-4d8c-888d-f364588ac2a7", "7c1526bc-789e-4f5a-8d68-1f9c39488ceb" ) ) );
    childrenMap.put( "17bce546-76a3-42c1-a587-b49fc99dd585", new ArrayList<>( Arrays.asList( "76a0cf8e-1f16-4b70-8c26-e35fbe0e5480" ) ) );
    childrenMap.put( "7c1526bc-789e-4f5a-8d68-1f9c39488ceb", new ArrayList<>( Arrays.asList( "bf3eb602-01c3-48d7-b357-7c28362fe4b9" ) ) );
    childrenMap.put( "f4a87ce6-ce8f-4e96-87f5-9cbfef8a0676", new ArrayList<>( Arrays.asList( "a49e997f-c3f6-4b12-a781-570cce18ffaf", "72b88d04-9c02-41e7-8085-c81b033bcffd", "1c33fa43-9a14-4191-8124-6f929421d744" ) ) );
    childrenMap.put( "19e59a1f-62f9-454b-86ab-b422a44382b0", new ArrayList<>( Arrays.asList( "6d22adfd-0a33-43dc-9892-193e1cc9dc5e", "9f1a8771-4934-4567-8d55-1deb7f0f3640" ) ) );
    childrenMap.put( "72b88d04-9c02-41e7-8085-c81b033bcffd", new ArrayList<>( Arrays.asList( "f443db9e-e486-4a48-aa6f-073b53729ac3" ) ) );
    childrenMap.put( "dc8c1482-30ab-4d0f-b9f6-e4c32a627bf0", new ArrayList<>( Arrays.asList( "dcffc35f-c74f-4e37-b463-97313998ea20" ) ) );
    childrenMap.put( "7f1c7986-6373-4cd1-8638-cff7a6b1e7ec", new ArrayList<>( Arrays.asList( "3f1c7d85-c4f7-4c34-9e17-c97f4579361e", "17bce546-76a3-42c1-a587-b49fc99dd585" ) ) );
    childrenMap.put( "8a7d5e5e-ef7b-4c5d-9e41-92f46aa84a79", new ArrayList<>( Arrays.asList( "ee723784-6875-4055-8be9-504f0c4bfb9e", "92d7d8f7-0cb8-416c-b728-a1d7fb998b8f" ) ) );

    return childrenMap;
  }
}
