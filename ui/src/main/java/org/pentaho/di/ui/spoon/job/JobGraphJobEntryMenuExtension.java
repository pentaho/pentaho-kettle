/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon.job;

import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.dom.Document;

public class JobGraphJobEntryMenuExtension {

  public Document doc;
  public JobMeta jobMeta;
  public JobGraph jobGraph;
  public JobEntryCopy jobEntry;
  public XulDomContainer xulDomContainer;

  public JobGraphJobEntryMenuExtension( XulDomContainer xulDomContainer, Document doc, JobMeta jobMeta,
    JobEntryCopy jobEntry, JobGraph jobGraph ) {
    this.xulDomContainer = xulDomContainer;
    this.doc = doc;
    this.jobMeta = jobMeta;
    this.jobEntry = jobEntry;
    this.jobGraph = jobGraph;
  }

}
