/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.di.trans.steps.hl7input.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ca.uhn.hl7v2.model.Group;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.util.SegmentFinder;
import ca.uhn.hl7v2.util.Terser;

public class HL7KettleParser {

  public static List<HL7Value> extractValues( Message message ) throws Exception {
    Terser terser = new Terser( message );
    SegmentFinder finder = terser.getFinder();

    List<HL7Value> values = new ArrayList<HL7Value>();

    int childNr = 1;

    while ( finder.hasNextChild() ) {

      // next group in the message (MSH, PID, EVN and so on)
      //
      finder.nextChild();
      Structure[] structures = finder.getCurrentChildReps();
      for ( int i = 0; i < structures.length; i++ ) {
        Structure structure = structures[i];
        parseStructure( values, message, terser, structure, Integer.toString( childNr ) );
      }

      childNr++;
    }

    return values;
  }

  private static void parseStructure( List<HL7Value> values, Message message, Terser terser, Structure structure,
      String structureNumber ) throws Exception {

    Map<String, List<String>> nameMap = NamesUtil.getInstance().getMap();

    if ( structure instanceof Segment ) {

      Segment segment = (Segment) structure;
      String[] names = segment.getNames();

      for ( int n = 1; n <= segment.numFields(); n++ ) {
        Type[] types = segment.getField( n );
        for ( int t = 0; t < types.length; t++ ) {
          int nrComponents = Terser.numComponents( types[t] );
          for ( int c = 1; c <= nrComponents; c++ ) {
            int nrSub = Terser.numSubComponents( types[t], c );
            for ( int sc = 1; sc <= nrSub; sc++ ) {
              String string = Terser.get( segment, n, t, c, sc );
              // Primitive primitive = Terser.getPrimitive(types[t], c, sc);

              String description = "?";
              List<String> list = nameMap.get( types[t].getName() );
              if ( list != null && c - 1 < list.size() ) {
                description = list.get( c - 1 );
              }

              Group group = structure.getParent();
              Group rootGroup = structure.getMessage();

              String coordinates = n + "." + ( t + 1 ) + "." + c + "." + sc;

              HL7Value value =
                  new HL7Value( message.getVersion(), rootGroup.getName(), group.getName(), structure.getName(),
                      structureNumber, names[n - 1], coordinates, types[t].getName(), description, string );
              values.add( value );
            }
          }
        }
      }

    } else if ( structure instanceof Group ) {
      Group group = (Group) structure;

      String[] names = group.getNames();

      for ( int n = 1; n <= names.length; n++ ) {
        String name = names[n - 1];
        Structure subStructure = group.get( name );
        parseStructure( values, message, terser, subStructure, structureNumber + "." + n );
      }
    } else {
      throw new Exception( "oops, not handled yet!" );
    }

  }

}
