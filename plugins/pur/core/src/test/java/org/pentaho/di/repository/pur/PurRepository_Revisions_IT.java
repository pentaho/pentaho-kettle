/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.repository.pur;

import org.junit.Test;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.VersionSummary;

import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Andrey Khayrutdinov
 */
public class PurRepository_Revisions_IT extends PurRepositoryTestBase {

  public PurRepository_Revisions_IT( Boolean lazyRepo ) {
    super( lazyRepo );
  }

  @Test
  public void onlyRevision_DataAndCommentAreSaved_Trans() throws Exception {
    testOnlyRevision_DateAndCommentAreSaved( new TransMeta() );
  }

  @Test
  public void onlyRevision_DataAndCommentAreSaved_Job() throws Exception {
    testOnlyRevision_DateAndCommentAreSaved( new JobMeta() );
  }

  private void testOnlyRevision_DateAndCommentAreSaved( RepositoryElementInterface transOrJob ) throws Exception {
    final String elementName = "onlyRevision_" + transOrJob.getRepositoryElementType();
    final String comment = "onlyRevision";
    final Calendar date = Calendar.getInstance();
    date.setTimeInMillis( 0 );

    transOrJob.setName( elementName );
    transOrJob.setRepositoryDirectory( purRepository.getDefaultSaveDirectory( transOrJob ) );
    purRepository.save( transOrJob, comment, date, null, false );

    assertCommentAndDate( transOrJob.getObjectRevision(), date, comment );

    List<VersionSummary> versions = assertExistsAndGetRevisions( transOrJob );
    assertEquals( 1, versions.size() );
    assertCommentAndDate( versions.get( 0 ), date, comment );
  }

  @Test
  public void onlyRevision_DataAndCommentAreNull_Trans() throws Exception {
    testOnlyRevision_DateAndCommentAreNull( new TransMeta() );
  }

  @Test
  public void onlyRevision_DataAndCommentAreNull_Job() throws Exception {
    testOnlyRevision_DateAndCommentAreNull( new JobMeta() );
  }

  private void testOnlyRevision_DateAndCommentAreNull( RepositoryElementInterface transOrJob ) throws Exception {
    final String elementName = "revisionWithOutComment_" + transOrJob.getRepositoryElementType();

    transOrJob.setName( elementName );
    transOrJob.setRepositoryDirectory( purRepository.getDefaultSaveDirectory( transOrJob ) );

    final long before = System.currentTimeMillis();
    purRepository.save( transOrJob, null, null, null, false );
    final long after = System.currentTimeMillis();

    assertNull( transOrJob.getObjectRevision().getComment() );

    final long revisionDate = transOrJob.getObjectRevision().getCreationDate().getTime();
    assertTrue( "Revision date should be inside 'before' and 'after' measurements", before <= revisionDate
        && revisionDate <= after );

    List<VersionSummary> versions = assertExistsAndGetRevisions( transOrJob );
    assertEquals( 1, versions.size() );
    assertNull( versions.get( 0 ).getMessage() );

    final long versionSummaryDate = versions.get( 0 ).getDate().getTime();
    assertTrue( "Revision date should be inside 'before' and 'after' measurements", before <= versionSummaryDate
        && versionSummaryDate <= after );
  }

  @Test
  public void twoRevisions_DataAndCommentAreSaved_Trans() throws Exception {
    testTwoRevisions_DateAndCommentAreSaved( new TransMeta() );
  }

  @Test
  public void twoRevisions_DataAndCommentAreSaved_Job() throws Exception {
    testTwoRevisions_DateAndCommentAreSaved( new JobMeta() );
  }

  private void testTwoRevisions_DateAndCommentAreSaved( RepositoryElementInterface transOrJob ) throws Exception {
    final String elementName = "twoRevisions_" + transOrJob.getRepositoryElementType();

    final String comment1 = "first";
    final Calendar date1 = Calendar.getInstance();
    date1.setTimeInMillis( 0 );

    final String comment2 = "second";
    final Calendar date2 = Calendar.getInstance();
    date2.setTimeInMillis( 100 );

    transOrJob.setName( elementName );
    transOrJob.setRepositoryDirectory( purRepository.getDefaultSaveDirectory( transOrJob ) );

    purRepository.save( transOrJob, comment1, date1, null, false );
    assertCommentAndDate( transOrJob.getObjectRevision(), date1, comment1 );

    purRepository.save( transOrJob, comment2, date2, null, false );
    assertCommentAndDate( transOrJob.getObjectRevision(), date2, comment2 );

    List<VersionSummary> versions = assertExistsAndGetRevisions( transOrJob );
    assertEquals( 2, versions.size() );
    assertCommentAndDate( versions.get( 0 ), date1, comment1 );
    assertCommentAndDate( versions.get( 1 ), date2, comment2 );
  }

  private List<VersionSummary> assertExistsAndGetRevisions( RepositoryElementInterface transOrJob ) {
    ObjectId id = transOrJob.getObjectId();
    assertNotNull( id );

    RepositoryFile file = unifiedRepository.getFileById( id.toString() );
    assertNotNull( file );
    return unifiedRepository.getVersionSummaries( id.toString() );
  }

  private void assertCommentAndDate( VersionSummary summary, Calendar expectedDate, String expectedComment ) {
    assertEquals( expectedDate.getTimeInMillis(), summary.getDate().getTime() );
    assertEquals( expectedComment, summary.getMessage() );
  }

  private void assertCommentAndDate( ObjectRevision revision, Calendar expectedDate, String expectedComment ) {
    assertEquals( expectedDate.getTimeInMillis(), revision.getCreationDate().getTime() );
    assertEquals( expectedComment, revision.getComment() );
  }
}
