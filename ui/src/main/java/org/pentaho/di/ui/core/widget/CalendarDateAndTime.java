/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.util.DateDetector;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.gui.GUIResource;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class CalendarDateAndTime extends Composite {

  private Label wlDateLabel;
  private TextVar wDateString;
  private Label calendarImage;
  private Label wlTimeLabel;
  private ComboVar wHour;
  private ComboVar wMinute;
  private ComboVar wSecond;
  private Shell calendarShell;

  public CalendarDateAndTime( Composite composite, int i, VariableSpace variableSpace ) {
    super( composite, i );

    FormLayout formLayout = new FormLayout();
    formLayout.marginBottom = 0;
    formLayout.marginHeight = 0;
    formLayout.marginWidth = 0;
    formLayout.marginTop = 0;
    this.setLayout( formLayout );

    wlDateLabel = new Label( this, SWT.LEFT );
    wlDateLabel.setText( "Date:" );
    wlDateLabel.setLayoutData( new FormDataBuilder().top().left().result() );

    wlTimeLabel = new Label( this, SWT.LEFT );
    wlTimeLabel.setText( "Time:" );
    wlTimeLabel.setLayoutData( new FormDataBuilder().top().left( wlDateLabel, 135 ).result() );

    wDateString = new TextVar( variableSpace, this, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wDateString.setLayoutData( new FormDataBuilder().top( wlDateLabel, 5 ).left().width( 100 ).result() );

    calendarImage = new Label( this, SWT.LEFT | SWT.BOTTOM );
    calendarImage.setImage( GUIResource.getInstance().getImageCalendar() );
    calendarImage.setLayoutData( new FormDataBuilder().left( wDateString, 5 ).top( 50, 0 ).result() );
    calendarImage.addMouseListener( new MouseListener() {
      @Override
      public void mouseDoubleClick( MouseEvent mouseEvent ) {
        mouseDown( mouseEvent );
      }

      @Override
      public void mouseDown( MouseEvent mouseEvent ) {
        //only allow one to appear
        if ( calendarShell == null || calendarShell.isDisposed() ) {
          showCalendarWidget();
        }
      }

      @Override
      public void mouseUp( MouseEvent mouseEvent ) {
        //nothing to do here
      }
    } );

    wHour = new ComboVar( variableSpace, this, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    setDropDownValues( wHour, 0, 23 );
    wHour.setLayoutData( new FormDataBuilder().top( wlTimeLabel ).left( calendarImage, 50 ).result() );
    wHour.setText( "00" );

    wMinute = new ComboVar( variableSpace, this, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    setDropDownValues( wMinute, 0, 59 );
    wMinute.setLayoutData( new FormDataBuilder().top( wlTimeLabel ).left( wHour, ConstUI.SMALL_MARGIN ).result() );
    wMinute.setText( "00" );

    wSecond = new ComboVar( variableSpace, this, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    setDropDownValues( wSecond, 0, 59 );
    wSecond.setLayoutData( new FormDataBuilder().top( wlTimeLabel ).left( wMinute, ConstUI.SMALL_MARGIN ).result() );
    wSecond.setText( "00" );
  }

  private void setDropDownValues( ComboVar c, int start, int end ) {
    for ( int i = start; i <= end; i++ ) {
      String value = String.valueOf( i );
      value = value.length() == 1 ? 0 + value : value;
      c.add( value );
    }
  }

  public void setDateAndTime( Date d ) {
    if ( d == null ) {
      d = Calendar.getInstance().getTime();
    }
    setDateString( String.format( "%1$tY/%1$tm/%1$td", d ) );
    setTime( d );
  }

  public Date getDate() throws ParseException {
    return DateDetector.getDateFromString( getDateString() + " " + getTimeString() );
  }

  public String getTimeString() {
    return wHour.getText() + ":" + wMinute.getText() + ":" + wSecond.getText();
  }

  public void setDateString( String dateString ) {
    wDateString.setText( dateString );
  }

  public String getDateString() {
    return wDateString.getText();
  }

  public void setTime( Date widgetDate ) {
    wHour.setText( String.format( "%TH", widgetDate ) );
    wMinute.setText( String.format( "%TM", widgetDate ) );
    wSecond.setText( String.format( "%TS", widgetDate ) );
  }

  private void showCalendarWidget() {
    calendarShell = new Shell( this.getShell(), SWT.DIALOG_TRIM );

    FormLayout formLayout = new FormLayout();
    formLayout.marginBottom = 0;
    formLayout.marginHeight = 0;
    formLayout.marginWidth = 0;
    formLayout.marginTop = 0;
    calendarShell.setLayout( formLayout );

    DateTime calendarWidget = new DateTime( calendarShell, SWT.CALENDAR | SWT.BORDER );
    calendarWidget.setLayoutData( new FormDataBuilder().top().left().result() );

    // try to set the date in the calendar from the value in the text field, if it's a valid date string
    try {
      Date dateFromField = DateDetector.getDateFromString( wDateString.getText() );
      Calendar c = Calendar.getInstance();
      c.setTime( dateFromField );
      calendarWidget.setDate( c.get( Calendar.YEAR ), c.get( Calendar.MONTH ), c.get( Calendar.DAY_OF_MONTH ) );
    } catch ( ParseException e ) {
      // do nothing; just ignore
    }

    Button okButton = new Button( calendarShell, SWT.PUSH );
    Button cancelButton = new Button( calendarShell, SWT.PUSH );

    cancelButton.setText( "CANCEL" );
    cancelButton.setLayoutData( new FormDataBuilder().top( calendarWidget ).right().result() );

    okButton.setText( "OK" );
    okButton.setLayoutData( new FormDataBuilder()
      .top( calendarWidget ).right( cancelButton, -ConstUI.SMALL_MARGIN ).result() );

    okButton.addSelectionListener( new SelectionListener() {
      @Override
      public void widgetSelected( SelectionEvent selectionEvent ) {
        widgetDefaultSelected( selectionEvent );
      }

      @Override
      public void widgetDefaultSelected( SelectionEvent selectionEvent ) {
        //note: getMonth is indexed from 0, but days and years are from 1
        setDateString( calendarWidget.getYear() + "/" + ( calendarWidget.getMonth() + 1 ) + "/" + calendarWidget.getDay() );
        calendarShell.dispose();
      }
    } );

    cancelButton.addSelectionListener( new SelectionListener() {
      @Override
      public void widgetSelected( SelectionEvent selectionEvent ) {
        widgetDefaultSelected( selectionEvent );
      }

      @Override
      public void widgetDefaultSelected( SelectionEvent selectionEvent ) {
        calendarShell.dispose();
      }
    } );

    calendarShell.pack();
    calendarShell.open();
  }

}
