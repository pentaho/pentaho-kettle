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
package org.pentaho.di.core.logging;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.apache.log4j.Appender;
import org.apache.log4j.spi.Filter;
import org.junit.Test;

public abstract class Log4jAppenderTest {

  public abstract Appender getAppender();

  @Test
  public void addFilter() {
    Appender appender = getAppender();
    Filter filter = mock( Filter.class );
    appender.addFilter( filter );
    assertThat( appender.getFilter(), is( filter ) );
  }

  @Test
  public void clearFilters() {
    Appender appender = getAppender();
    appender.addFilter( mock( Filter.class ) );
    appender.clearFilters();
    assertNull( appender.getFilter() );
  }

}
