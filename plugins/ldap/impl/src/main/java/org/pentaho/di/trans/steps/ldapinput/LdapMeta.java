/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.trans.steps.ldapinput;

import org.pentaho.di.trans.step.StepMetaInterface;

public interface LdapMeta extends StepMetaInterface {
  String getProtocol();

  String getHost();

  String getPort();

  String getDerefAliases();

  String getReferrals();

  boolean isUseCertificate();

  String getTrustStorePath();

  String getTrustStorePassword();

  boolean isTrustAllCertificates();
}
