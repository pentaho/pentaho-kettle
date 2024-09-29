/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2019 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */

package org.pentaho.di.ui.core.widget;

import com.google.common.base.Preconditions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.pentaho.di.core.Const;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.floorDiv;
import static java.lang.Math.min;
import static org.pentaho.di.core.Const.MARGIN;

/**
 * Creates a label paired with an swt control immediately below it.  Call .attachBelow() to place
 * the paired label/control below another control in a form.
 * <p>
 * The {@link Series} inner class allows creating a series of labeled controls, each automatically attached the its
 * preceding control.  E.g.
 * +--------+
 * |Label1  |
 * +--------+
 * |Control1|
 * +--------+
 * |Label2  |
 * +--------+
 * |Control2|
 * +--------+
 * |Label3  |
 * +--------+
 * |Control3|
 * +--------+
 * <p>
 * or if using {@link Series#layout(int height, int width, int numcols)}, labeled controls will be layed
 * out top down / left right with mechanics similar to GridLayout.  E.g.
 * <p>
 * +--------+--------+--------+
 * |Wide Label1      |Label4  |
 * +--------+--------+--------+
 * |Wide Control     |Control4|
 * +--------+--------+--------+
 * |Label2  |Label3  |Label5  |
 * +--------+--------+--------+
 * |Control2|Control3|Control5|
 * +--------+--------+--------+
 * <p>
 * {@link Series#hideAll()}  and {@link Series#show(Control)} will update attachments automatically to
 * shift paired labels/controls up when preceding controls are hidden.
 */
@SuppressWarnings ( "unused" )
public class LabeledControl {

  private final int controlWidth;
  private Label label;
  private final Control control;
  private boolean visible = true;

  private static final int SPACING = 15;

  private LabeledControl( Composite parentComp, String labelText, Control control, int controlWidth, PropsUI props ) {
    label = new Label( parentComp, SWT.RIGHT );
    label.setText( labelText );
    this.controlWidth = controlWidth;
    this.control = control;
    props.setLook( label );
    props.setLook( control );
  }

  public static LabeledControl labeledControl(
    Composite parentComp, String labelText, Control control, int controlWidth, PropsUI props ) {
    return new LabeledControl( parentComp, labelText, control, controlWidth, props );
  }

  private Control control() {
    return control;
  }


  /**
   * Attaches this LabeledControl below control.  If null, attaches to the top of the container.
   */
  @SuppressWarnings ( "WeakerAccess" )
  public void attachBelow( Control control ) {
    attachBelow( control, 0 );
  }


  @SuppressWarnings ( "WeakerAccess" )
  public void attachBelow( Control control, int left ) {
    int leftPos = left > 0 ? left : MARGIN;
    if ( control == null ) {
      label.setLayoutData( new FormDataBuilder().left( 0, leftPos ).top( 0, MARGIN ).result() );
    } else {
      label.setLayoutData( new FormDataBuilder().left( 0, leftPos ).top( control, SPACING ).result() );
    }
    this.control
      .setLayoutData(
        new FormDataBuilder()
          .left( 0, leftPos )
          .top( label, Const.FORM_MARGIN )
          .width( controlWidth ).result() );
  }

  public static class Series {
    private final List<LabeledControl> labeledControlSequence;
    private final Control prevControl;
    private final Map<Control, LabeledControl> lookup;
    private final Composite parent;

    /**
     * Creates a series of LabeledControls, managing vertical layout between each.
     *
     * @param prevControl   The swt control above the series of LabeledControls
     * @param labelTexts    The list of label texts, required to be one text per control.
     * @param controls      The set of controls in the series
     * @param controlWidths The widths of each control in the series.
     * @param props         PropsUI to apply.
     */
    public Series( Composite parent, Control prevControl, List<String> labelTexts, List<Control> controls,
                   List<Integer> controlWidths,
                   PropsUI props ) {
      Preconditions.checkState( labelTexts.size() == controls.size()
        && labelTexts.size() == controlWidths.size() );
      this.prevControl = prevControl;
      this.parent = parent;

      labeledControlSequence = IntStream.range( 0, labelTexts.size() )
        .mapToObj( i -> new LabeledControl(
          parent, labelTexts.get( i ), controls.get( i ), controlWidths.get( i ), props ) )
        .collect( Collectors.toList() );

      lookup = labeledControlSequence.stream().collect( Collectors.toMap( LabeledControl::control, lc -> lc ) );
      layout();
    }


    @SuppressWarnings ( { "WeakerAccess", "unused" } )
    public void hideAll() {
      labeledControlSequence.forEach( this::hideLabeledControl );
    }

    public void hide( Control control ) {
      hideLabeledControl( lookup.get( control ) );
    }

    public void show( Control control ) {
      showLabeledControl( lookup.get( control ) );
    }


    /**
     * Top/down layout
     */
    public void layout() {
      Control next = prevControl;
      for ( LabeledControl labeledControl : labeledControlSequence ) {
        if ( labeledControl.visible ) {
          labeledControl.attachBelow( next );
          next = labeledControl.control;
        }
      }
      parent.layout();
    }

    /**
     * performs layout in a grid assuming the available space is within given bounds.
     * Lays out top down, left to right.
     * <p>
     * This acts a lot like GridLayout, but plays nicely when embedded in a FormLayout.
     * Also accommodates wide controls that extend over multiple columns.
     */
    public void layout( int height, int width, int numcols ) {
      final int labeledControlHeight = 25;
      final int colwidth = width / numcols;
      final int numrows = height / labeledControlHeight;

      LabeledControl[][] grid = getLayoutGrid( numcols, colwidth, numrows );

      List<LabeledControl> addedControls = new ArrayList<>();
      for ( int column = 0; column < numcols; column++ ) {
        for ( int row = 0; row < numrows; row++ ) {
          LabeledControl curcontrol = grid[ row ][ column ];
          if ( curcontrol == null ) {
            continue;
          }
          if ( !addedControls.contains( curcontrol ) ) {
            addedControls.add( curcontrol );
            Control above = row == 0 ? null : grid[ row - 1 ][ column ].control;
            curcontrol.attachBelow( above, column * colwidth );
          }
        }
      }
      parent.layout();
    }

    // Returns the 2D grid structure
    private LabeledControl[][] getLayoutGrid( int numcols, int colwidth, int numrows ) {
      LabeledControl[][] grid = new LabeledControl[ numrows ][ numcols ];

      int curcolumn = 0;
      int currow = 0;

      for ( LabeledControl lc : labeledControlSequence ) {
        if ( !lc.visible ) {
          continue;
        }
        int numberOfColumnsWide = floorDiv( lc.controlWidth, colwidth ) + 1;
        int lastColumnForControl = min( curcolumn + numberOfColumnsWide, numcols );
        for ( int i = curcolumn; i < lastColumnForControl; i++ ) {
          while ( currow < numrows && grid[ currow ][ i ] != null ) {
            // previous control overhangs
            currow++;
          }
          if ( currow >= numrows ) {
            throw new IllegalStateException(
              String.format( "Control [%s] does not fit in column %s", lc.label.getText(), i ) );
          }
          grid[ currow ][ i ] = lc;
        }
        if ( ++currow >= numrows ) {
          curcolumn++;
          currow = 0;
        }
      }
      return grid;
    }


    @SuppressWarnings ( "unused" )
    public boolean containsControl( Control control ) {
      return lookup.containsKey( control );
    }

    private void showLabeledControl( LabeledControl lc ) {
      lc.control.setVisible( true );
      lc.label.setVisible( true );
      lc.visible = true;
    }

    private void hideLabeledControl( LabeledControl lc ) {
      lc.control.setVisible( false );
      lc.label.setVisible( false );
      lc.visible = false;
    }


  }


}

