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

package org.pentaho.di.www;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pentaho.di.core.gui.AreaOwner;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.SwingGC;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPainter;
import org.pentaho.di.trans.step.StepMeta;

public class GetTransImageServlet extends BaseHttpServlet implements CartePluginInterface {

  private static final long serialVersionUID = -4365372274638005929L;

  private static Class<?> PKG = GetTransImageServlet.class; // for i18n purposes, needed by Translator2!!

  public static final String CONTEXT_PATH = "/kettle/transImage";

  /**
<div id="mindtouch">
    <h1>/kettle/transImage</h1>
    <a name="GET"></a>
    <h2>GET</h2>
    <p>Generates PNG image of the specified transformation currently present on Carte server.
  Transformation name and Carte transformation ID (optional) are used for specifying which
  transformation to get information for. Response is a binary of the PNG image.</p>

    <p><b>Example Request:</b><br />
    <pre function="syntax.xml">
    GET /kettle/transImage?name=dummy-trans
    </pre>

    </p>
    <h3>Parameters</h3>
    <table class="pentaho-table">
    <tbody>
    <tr>
      <th>name</th>
      <th>description</th>
      <th>type</th>
    </tr>
    <tr>
    <td>name</td>
    <td>Name of the transformation to be used for image generation.</td>
    <td>query</td>
    </tr>
    <tr>
    <td>id</td>
    <td>Carte id of the transformation to be used for image generation.</td>
    <td>query, optional</td>
    </tr>
    </tbody>
    </table>

  <h3>Response Body</h3>

  <table class="pentaho-table">
    <tbody>
      <tr>
        <td align="right">binary streak:</td>
        <td>image</td>
      </tr>
      <tr>
        <td align="right">media types:</td>
        <td>image/png</td>
      </tr>
    </tbody>
  </table>
  <p>A binary PNG image or empty response is presented if no transformation is found.</p>

    <h3>Status Codes</h3>
    <table class="pentaho-table">
  <tbody>
    <tr>
      <th>code</th>
      <th>description</th>
    </tr>
    <tr>
      <td>200</td>
      <td>Request was processed.</td>
    </tr>
    <tr>
      <td>500</td>
      <td>Internal server error occurs during request processing.</td>
    </tr>
  </tbody>
</table>
</div>
  */
  public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
    IOException {
    if ( isJettyMode() && !request.getContextPath().startsWith( CONTEXT_PATH ) ) {
      return;
    }

    if ( log.isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "GetTransImageServlet.Log.TransImageRequested" ) );
    }

    String transName = request.getParameter( "name" );
    String id = request.getParameter( "id" );

    // ID is optional...
    //
    Trans trans;
    CarteObjectEntry entry;
    if ( Utils.isEmpty( id ) ) {
      // get the first transformation that matches...
      //
      entry = getTransformationMap().getFirstCarteObjectEntry( transName );
      if ( entry == null ) {
        trans = null;
      } else {
        id = entry.getId();
        trans = getTransformationMap().getTransformation( entry );
      }
    } else {
      // Take the ID into account!
      //
      entry = new CarteObjectEntry( transName, id );
      trans = getTransformationMap().getTransformation( entry );
    }

    try {
      if ( trans != null ) {

        response.setStatus( HttpServletResponse.SC_OK );

        response.setCharacterEncoding( "UTF-8" );
        response.setContentType( "image/png" );

        // Generate xform image
        //
        BufferedImage image = generateTransformationImage( trans.getTransMeta() );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
          ImageIO.write( image, "png", os );
        } finally {
          os.flush();
        }
        response.setContentLength( os.size() );

        OutputStream out = response.getOutputStream();
        out.write( os.toByteArray() );

      }
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  private BufferedImage generateTransformationImage( TransMeta transMeta ) throws Exception {
    float magnification = 1.0f;
    Point maximum = transMeta.getMaximum();
    maximum.multiply( magnification );

    SwingGC gc = new SwingGC( null, maximum, 32, 0, 0 );
    TransPainter transPainter =
      new TransPainter(
        gc, transMeta, maximum, null, null, null, null, null, new ArrayList<AreaOwner>(),
        new ArrayList<StepMeta>(), 32, 1, 0, 0, true, "Arial", 10 );
    transPainter.setMagnification( magnification );
    transPainter.buildTransformationImage();

    BufferedImage image = (BufferedImage) gc.getImage();

    return image;
  }

  public String toString() {
    return "Trans Image Handler";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  public String getContextPath() {
    return CONTEXT_PATH;
  }

}
