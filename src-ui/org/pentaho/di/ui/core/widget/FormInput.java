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

package org.pentaho.di.ui.core.widget;

import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;

import java.lang.reflect.Method;

/**
 * This class defines the fairly generic FormInput.
 * This class is simply a convenience utility, containing
 * the primary information required to build an input 
 * for a FormLayout.  
 *
 * This template requires one to define the type of contained control.
 *
 * ex:
 *      FormInput<Text> input = new FormInput<Text>( new Label( shell, SWT.NONE ),
 *					             new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER) );	
 *	input.setText( "Hello", FormInput.Widget.LABEL );
 *	input.setText( "World", FormInput.Widget.INPUT );
 *	input.setToolTip( "To whom do you want to send a shout out?", FormInput.Widget.INPUT );
 *	input.setPosition( 0, 47, FormInput.Widget.LABEL, FormInput.Position.LEFT );
 *	input.setPosition( 0, 130, FormInput.Widget.LABEL, FormInput.Position.RIGHT );
 *	input.setPosition( input.getLabel( ), 10, FormInput.Widget.INPUT, FormInput.Position.LEFT );
 *
 * @author Robert D. Rice
 */
public class FormInput<C extends Control> extends Object /*implements ConstantsIF*/ {
    public static final String vc_id = "$Id: FormInput.java 1672 2009-05-20 20:12:26Z robert $";

    /** enumeration of available positioning elements */
    public enum Position { LEFT, RIGHT, TOP, BOTTOM }

    /** enumeration of the contained widgets */
    public enum Widget { LABEL, INPUT }

    /** attributes */
    protected Label label = null;
    protected C input = null;
    protected FormData labelFD = new FormData( );
    protected FormData inputFD = new FormData( );

    /**
     * Constructor.
     * @param label
     * @param control input
     */
    public FormInput( Label label, C input ) {
	super( );
	setLabel( label );
	setInput( input );
    }

    /**
     * getter for the label
     * @return label
     */
    public Label getLabel( ) {
	return label;
    }

    /**
     * setter for the label
     * @param label
     */
    public void setLabel( Label label ) {
	this.label = label;
	this.label.setLayoutData( getLabelFD( ) );
    }

    /**
     * getter for the input
     * @return input
     */
    public C getInput( ) {
	return input;
    }

    /**
     * setter for the input
     * @param input
     */
    public void setInput( C input ) {
	this.input = input;
	this.input.setLayoutData( getInputFD( ) );
    }

    /**
     * getter for the labelFD
     * @return labelFD
     */
    public FormData getLabelFD( ) {
	return labelFD;
    }

    /**
     * setter for the labelFD
     * @param labelFD
     */
    public void setLabelFD( FormData labelFD ) {
	this.labelFD = labelFD;
    }

    /**
     * getter for the inputFD
     * @return inputFD
     */
    public FormData getInputFD( ) {
	return inputFD;
    }

    /**
     * setter for the inputFD
     * @param inputFD
     */
    public void setInputFD( FormData inputFD ) {
	this.inputFD = inputFD;
    }

    /**
     * setter for the element position
     * @param numerator
     * @param offset
     * @param widget to set position, [ lable, input ]
     * @param position side, [ left, right, top, bottom ]
     */
    public void setPosition( int numerator, int offset, 
			     Widget widget, Position side ) {
	setPosition( new FormAttachment( numerator, offset ), widget, side );
    }

    /**
     * setter for the element position
     * @param Control
     * @param offset
     * @param widget to set position, [ lable, input ]
     * @param position side, [ left, right, top, bottom ]
     */
    public void setPosition( Control control, int offset, 
			     Widget widget, Position side ) {
	setPosition( new FormAttachment( control, offset ), widget, side );
    }

    /**
     * setter for the element position
     * @param FormAttachment position
     * @param widget to set position, [ lable, input ]
     * @param position side, [ left, right, top, bottom ]
     */
    public void setPosition( FormAttachment position, 
			     Widget widget, Position side ) {
	FormData layout = widget == Widget.LABEL ? getLabelFD( ) : getInputFD( );

	switch (side) {
	case LEFT: layout.left = position; break;
	case RIGHT: layout.right = position; break;
	case TOP: layout.top = position; break;
	case BOTTOM: layout.bottom = position; break;
	default: break;
        }
    }

    /**
     * setter for the widget text
     * @param string text
     * @param widget to set text on
     */
    public void setText( String text, Widget widget ) {
	Control control = widget == Widget.LABEL ? getLabel( ) : getInput( );
	Class<?>[] params = { String.class };

	try {
	    Method method = control.getClass( ).getDeclaredMethod( "setText", params );
	    method.invoke( control, text );
	} catch ( Exception ex ) {
	    ; // oops
	}
    }

    /**
     * getter for the widget text
     * @param widget to retrieve the text from
     * @return string text
     */
    public String getText( Widget widget ) {
	String text = null;
	Control control = widget == Widget.LABEL ? getLabel( ) : getInput( );
	
	try {
	    Method method = control.getClass( ).getDeclaredMethod( "getText" );
	    text = (String)method.invoke( control );
	} catch ( Exception ex ) {
	    ; // oops
	}
	
	return text;
    }
    
    /**
     * setter for the tooltip
     * @param string text
     */
    public void setToolTip( String text, Widget widget ) {
	switch (widget) {
	case LABEL: getLabel( ).setToolTipText( text ); break;
	case INPUT: getInput( ).setToolTipText( text ); break;
	default: break;
	}
    }
} 
