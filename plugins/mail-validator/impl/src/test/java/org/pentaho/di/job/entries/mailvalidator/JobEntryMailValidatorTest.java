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

package org.pentaho.di.job.entries.mailvalidator;

import static org.junit.Assert.assertNotNull;
import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;


public class JobEntryMailValidatorTest extends JobEntryLoadSaveTestSupport<JobEntryMailValidator> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryMailValidator> getJobEntryClass() {
    return JobEntryMailValidator.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList(
        "smtpCheck",
        "timeout",
        "defaultSMTP",
        "emailSender",
        "emailAddress" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap(
        "smtpCheck", "isSMTPCheck",
        "timeout", "getTimeOut",
        "defaultSMTP", "getDefaultSMTP",
        "emailSender", "geteMailSender",
        "emailAddress", "getEmailAddress" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap(
        "smtpCheck", "setSMTPCheck",
        "timeout", "setTimeOut",
        "defaultSMTP", "setDefaultSMTP",
        "emailSender", "seteMailSender",
        "emailAddress", "setEmailAddress" );
  }

  @Test
  public void testExecute() {
    KettleLogStore.init();
    Result previousResult = new Result();
    JobEntryMailValidator validator = new JobEntryMailValidator();
    Result result = validator.execute( previousResult, 0 );
    assertNotNull( result );
  }

}
