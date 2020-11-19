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
 * Copyright (c) 2002-2020 Hitachi Vantara..  All rights reserved.
 */

package org.hitachivantara.importer.utility;

import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.DefaultCaret;
import java.io.File;
import java.util.List;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;

public class ImporterUtility {

  private JList sourcesList;
  private DefaultListModel listModel;
  private JButton removeSelectedButton;
  private JButton removeAllButton;
  private JButton executeButton;
  private JTextArea log;
  private XMLProcess xmlProcess;
  private JPanel mainPanel;
  private JFrame mainWindow;

  public static void main( String[] args ) {
    new ImporterUtility();
  }

  public ImporterUtility() {
    mainWindow = new JFrame();
    xmlProcess = new XMLProcess();
    mainWindow.setSize( 750, 500 );
    mainWindow.setTitle( "Hitachi Vantara - Update Jobs and Transformations for import into Data Flow Manager" );
    mainWindow.setLocationRelativeTo( null );
    mainWindow.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

    mainPanel = new JPanel( new BorderLayout() );
    JToolBar toolBar = new JToolBar();
    JButton browseButton = new JButton( new ImageIcon( getClass().getClassLoader().getResource( "Add.png" ) ) );
    browseButton.setToolTipText( "Add Files" );
    browseButton.addActionListener( e -> {
      File file = openFileChooser();
      selectFiles( listModel, file );
    } );
    toolBar.add( browseButton );

    removeAllButton = new JButton( new ImageIcon( getClass().getClassLoader().getResource( "remove_all.png" ) ) );
    removeAllButton.setToolTipText( "Remove All Files" );
    removeAllButton.setEnabled( false );
    removeAllButton.addActionListener( e -> listModel.clear() );
    toolBar.add( removeAllButton );

    removeSelectedButton =
      new JButton( new ImageIcon( getClass().getClassLoader().getResource( "remove_single.png" ) ) );
    removeSelectedButton.setToolTipText( "Remove Selected File" );
    removeSelectedButton.setEnabled( false );
    removeSelectedButton.addActionListener( e -> {
      List<File> files = sourcesList.getSelectedValuesList();
      for ( File file : files ) {
        listModel.removeElement( file );
      }
    } );
    toolBar.add( removeSelectedButton );

    mainPanel.add( toolBar, BorderLayout.NORTH );

    JPanel centerPanel = new JPanel( new GridLayout( 2, 1 ) );
    listModel = new DefaultListModel();
    listModel.addListDataListener( new ListDataListener() {
      public void intervalAdded( ListDataEvent e ) {
        removeAllButton.setEnabled( !listModel.isEmpty() );
        executeButton.setEnabled( !listModel.isEmpty() );
      }

      public void intervalRemoved( ListDataEvent e ) {
        executeButton.setEnabled( !listModel.isEmpty() );
        removeAllButton.setEnabled( !listModel.isEmpty() );
      }

      public void contentsChanged( ListDataEvent e ) {
        throw new UnsupportedOperationException();
      }
    } );
    sourcesList = new JList( listModel );
    sourcesList.addListSelectionListener( e -> removeSelectedButton.setEnabled( !sourcesList.isSelectionEmpty() ) );
    JPanel sourcesListPanel = new JPanel( new BorderLayout() );
    sourcesListPanel.setBorder( BorderFactory
      .createTitledBorder( BorderFactory.createEtchedBorder(), "Files", TitledBorder.LEFT, TitledBorder.TOP ) );
    sourcesListPanel.add( new JScrollPane( sourcesList ), BorderLayout.CENTER );
    centerPanel.add( sourcesListPanel );

    log = new JTextArea();
    DefaultCaret caret = (DefaultCaret) log.getCaret();
    caret.setUpdatePolicy( DefaultCaret.ALWAYS_UPDATE );
    JPanel logPanel = new JPanel( new BorderLayout() );
    logPanel.setBorder( BorderFactory
      .createTitledBorder( BorderFactory.createEtchedBorder(), "Log", TitledBorder.LEFT, TitledBorder.TOP ) );
    logPanel.add( new JScrollPane( log ), BorderLayout.CENTER );

    centerPanel.add( logPanel );
    mainPanel.add( centerPanel, BorderLayout.CENTER );

    JPanel southPanel = new JPanel( new BorderLayout() );
    southPanel.setBorder( new EmptyBorder( 0, 0, 3, 3 ) );
    executeButton = new JButton( new ImageIcon( getClass().getClassLoader().getResource( "run.png" ) ) );
    executeButton.setToolTipText( "Process Files" );
    executeButton.setEnabled( false );
    executeButton.addActionListener( e -> {
      log.setText( xmlProcess.process( listModel.elements() ) );
      JOptionPane
        .showMessageDialog( mainPanel, xmlProcess.getCount() + " of " + listModel.size() + " files processed." );
    } );
    southPanel.add( executeButton, BorderLayout.EAST );
    mainPanel.add( southPanel, BorderLayout.SOUTH );

    mainWindow.add( mainPanel );
    mainWindow.setResizable( true );
    mainWindow.setVisible( true );
  }

  private File openFileChooser() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setAcceptAllFileFilterUsed( false );
    fileChooser.setPreferredSize( new Dimension( 750, 400 ) );
    fileChooser.setCurrentDirectory( new File( "." ) );
    fileChooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
    fileChooser.addChoosableFileFilter( new FileFilter() {
      public String getDescription() {
        return "Kettle Transformations (*.ktr) and Kettle Jobs (*.kjb)";
      }

      public boolean accept( File file ) {
        if ( file.isDirectory() ) {
          return true;
        } else {
          return file.getName().toLowerCase().endsWith( ".ktr" ) || file.getName().toLowerCase().endsWith( ".kjb" );
        }
      }
    } );
    return fileChooser.showOpenDialog( mainWindow ) == JFileChooser.APPROVE_OPTION ? fileChooser.getSelectedFile()
      : null;
  }

  private void selectFiles( DefaultListModel listModel, File selectedFile ) {
    if ( selectedFile != null ) {
      if ( selectedFile.isDirectory() ) {
        File[] files = selectedFile.listFiles();
        for ( File file : files ) {
          selectFiles( listModel, file );
        }
      } else {
        if ( ( selectedFile.getName().toLowerCase().endsWith( ".ktr" ) || selectedFile.getName().toLowerCase()
          .endsWith( ".kjb" ) ) && ( !listModel.contains( selectedFile ) ) ) {
          listModel.addElement( selectedFile );
        }
      }
    }
  }
}
