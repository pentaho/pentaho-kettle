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
