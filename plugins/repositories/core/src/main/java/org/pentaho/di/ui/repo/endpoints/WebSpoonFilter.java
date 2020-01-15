/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
 * Copyright (C) 2016-2019 by Hitachi America, Ltd., R&D : http://www.hitachi-america.us/rd/
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

package org.pentaho.di.ui.repo.endpoints;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.eclipse.rap.rwt.service.UISession;
import org.pentaho.di.core.WebSpoonUtils;

@Provider
public class WebSpoonFilter implements ContainerRequestFilter {

  @Override
  public void filter( ContainerRequestContext requestContext ) throws IOException {
    MultivaluedMap<String, String> params = requestContext.getUriInfo().getQueryParameters();
    String cid = params.getFirst( "cid" );
    UISession uiSession = WebSpoonUtils.getUISession( cid );
    WebSpoonUtils.setUISession( uiSession );
  }

}
