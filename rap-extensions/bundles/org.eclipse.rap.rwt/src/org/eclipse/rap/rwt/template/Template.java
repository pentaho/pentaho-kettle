/*******************************************************************************
 * Copyright (c) 2013, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.template;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.Adaptable;
import org.eclipse.rap.rwt.internal.template.TemplateSerializer;


/**
 * Templates are used to describe how a given data item is presented by a widget.
 * <p>
 * Templates can be applied to multiple widgets within the same UI session.
 * </p>
 * @see org.eclipse.rap.rwt.RWT#ROW_TEMPLATE
 *
 * @since 2.2
 */
public class Template implements Serializable, Adaptable {

  private final List<Cell<?>> cells;

  /**
   * Constructs an empty template.
   */
  public Template() {
    cells = new ArrayList<>();
  }

  void addCell( Cell<?> cell ) {
    cells.add( cell );
  }

  /**
   * Returns the list of cells included in this template. Modifications to the returned list will
   * not change the template.
   *
   * @return the list of cells in this template, may be empty but never <code>null</code>
   */
  public List<Cell<?>> getCells() {
    return new ArrayList<>( cells );
  }

  /**
   * <strong>IMPORTANT:</strong> This method is <em>not</em> part of the RWT public API. It is
   * marked public only so that it can be shared within the packages provided by RWT. It should
   * never be accessed from application code.
   *
   * @noreference This method is not intended to be referenced by clients.
   */
  @Override
  @SuppressWarnings( "unchecked" )
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == TemplateSerializer.class ) {
      return ( T )new TemplateSerializer() {
        @Override
        public JsonValue toJson() {
          JsonArray jsonArray = new JsonArray();
          for( Cell<?> cell : getCells() ) {
            jsonArray.add( cell.toJson() );
          }
          return jsonArray;
        }
      };
    }
    return null;
  }

}
