/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.job.entries.ssh2put;

import com.trilead.ssh2.KnownHosts;
import com.trilead.ssh2.ServerHostKeyVerifier;

/**
 * This example hostkey verifier is used by the
 * UsingKnownHosts.java example.
 *  
 * @author Christian Plattner, plattner@trilead.com
 * @version $Id: SimpleVerifier.java,v 1.4 2007/10/15 12:49:57 cplattne Exp $
 */
class SimpleVerifier implements ServerHostKeyVerifier
{
	KnownHosts database;

	public SimpleVerifier(KnownHosts database)
	{
		if (database == null)
			throw new IllegalArgumentException();

		this.database = database;
	}

	public boolean verifyServerHostKey(String hostname, int port, String serverHostKeyAlgorithm, byte[] serverHostKey)
			throws Exception
	{
		int result = database.verifyHostkey(hostname, serverHostKeyAlgorithm, serverHostKey);

		switch (result)
		{
		case KnownHosts.HOSTKEY_IS_OK:

			return true; // We are happy

		case KnownHosts.HOSTKEY_IS_NEW:

			// Unknown host? Blindly accept the key and put it into the cache.
			// Well, you definitely can do better (e.g., ask the user).

			// The following call will ONLY put the key into the memory cache!
			// To save it in a known hosts file, also call "KnownHosts.addHostkeyToFile(...)"
			database.addHostkey(new String[] { hostname }, serverHostKeyAlgorithm, serverHostKey);

			return true;

		case KnownHosts.HOSTKEY_HAS_CHANGED:

			// The connection if the hostkey has changed.
			// put the key into the memory cache!
			database.addHostkey(new String[] { hostname }, serverHostKeyAlgorithm, serverHostKey);
			
			return true;

		default:
			throw new IllegalStateException();
		}
	}
}