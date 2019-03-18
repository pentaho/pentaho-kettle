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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
 * {@link Series#hideAll()}  and {@link Series#show(Control)} will update attachments automatically to
 * shift paired labels/controls up when preceding controls are hidden.
 */
public class LabeledControl {

  private final int controlWidth;
  private Label label;
  private final Control control;
  private boolean visible = true;

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

  @SuppressWarnings( "WeakerAccess" )
  public void attachBelow( Control prevControl ) {
    label.setLayoutData( new FormDataBuilder().left( 0, MARGIN ).top( prevControl, MARGIN ).result() );
    control
      .setLayoutData(
        new FormDataBuilder()
          .left( 0, MARGIN )
          .top( label, Const.FORM_MARGIN )
          .width( controlWidth ).result() );
  }


  public static class Series {
    private final List<LabeledControl> labeledControlSequence;
    private final Control prevControl;
    private final Map<Control, LabeledControl> lookup;

    /**
     * Creates a series of LabeledControls, managing vertical layout between each.
     *
     * @param prevControl   The swt control above the series of LabeledControls
     * @param labelTexts    The list of label texts, required to be one text per control.
     * @param controls      The set of controls in the series
     * @param controlWidths The widths of each control in the series.
     * @param props         PropsUI to apply.
     */
    public Series( Control prevControl, List<String> labelTexts, List<Control> controls, List<Integer> controlWidths,
                   PropsUI props ) {
      Preconditions.checkState( labelTexts.size() == controls.size()
        && labelTexts.size() == controlWidths.size() );
      this.prevControl = prevControl;

      labeledControlSequence = IntStream.range( 0, labelTexts.size() )
        .mapToObj( i -> new LabeledControl(
          prevControl.getParent(), labelTexts.get( i ), controls.get( i ), controlWidths.get( i ), props ) )
        .collect( Collectors.toList() );

      lookup = labeledControlSequence.stream().collect( Collectors.toMap( LabeledControl::control, lc -> lc ) );
      layout();
    }


    @SuppressWarnings( "WeakerAccess" )
    public void hideAll() {
      labeledControlSequence.forEach( this::hideLabeledControl );
      layout();
    }

    public void hide( Control control ) {
      hideLabeledControl( lookup.get( control ) );
      layout();
    }

    public void show( Control control ) {
      showLabeledControl( lookup.get( control ) );
      layout();
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

    private void layout() {
      Control next = prevControl;
      for ( LabeledControl labeledControl : labeledControlSequence ) {
        if ( labeledControl.visible ) {
          labeledControl.attachBelow( next );
          next = labeledControl.control;
        }
      }
      prevControl.getParent().layout();
    }

  }


}

