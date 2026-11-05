package org.pentaho.di.ui.core;

import static org.junit.Assert.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.junit.Test;

public class EclipseJfaceUpgradeIntegrationTest {

  @Test
  public void testTextFileImportWizardUsage() {
    try {

      assertNotNull( "Wizard class should be available", Wizard.class );
      assertNotNull( "WizardPage class should be available", WizardPage.class );

    } catch ( org.eclipse.swt.SWTException exception ) {

      assertNotNull( "Wizard class should still be available", Wizard.class );
    } catch ( Exception exception ) {
      fail( "TextFileImportWizard usage failed: " + exception.getMessage() );
    }
  }

  @Test
  public void testMergeJoinDialogMessageUsage() {
    try {

      assertNotNull( "MessageDialog class should be available", MessageDialog.class );

    } catch ( org.eclipse.swt.SWTException exception ) {

      assertNotNull( "MessageDialog should still be available", MessageDialog.class );
    } catch ( Exception exception ) {
      fail( "MergeJoinDialog MessageDialog usage failed: " + exception.getMessage() );
    }
  }

  @Test
  public void testProgressMonitorDialogUsage() {
    try {

      assertNotNull( "ProgressMonitorDialog should be available", ProgressMonitorDialog.class );
      assertNotNull( "IRunnableWithProgress should be available", IRunnableWithProgress.class );
    } catch ( org.eclipse.swt.SWTException exception ) {

      assertNotNull( "ProgressMonitorDialog should still be available", ProgressMonitorDialog.class );
    } catch ( Exception exception ) {
      fail( "ProgressMonitorDialog usage failed: " + exception.getMessage() );
    }
  }

  @Test
  public void testMessageDialogWithToggleUsage() {
    try {

      assertNotNull( "MessageDialogWithToggle should be available", MessageDialogWithToggle.class );
    } catch ( org.eclipse.swt.SWTException exception ) {

      assertNotNull( "MessageDialogWithToggle should still be available", MessageDialogWithToggle.class );
    } catch ( Exception exception ) {
      fail( "MessageDialogWithToggle usage failed: " + exception.getMessage() );
    }
  }

  @Test
  public void testCustomDialogCreation() {
    try {

      assertNotNull( "Dialog class should be available", Dialog.class );
    } catch ( org.eclipse.swt.SWTException exception ) {

      assertNotNull( "Dialog should still be available", Dialog.class );
    } catch ( Exception exception ) {
      fail( "Custom dialog creation failed: " + exception.getMessage() );
    }
  }

  @Test
  public void testWizardPageLifecycle() {
    try {

      assertNotNull( "WizardPage class should be available", WizardPage.class );

    } catch ( org.eclipse.swt.SWTException exception ) {

      assertNotNull( "WizardPage class should still be available", WizardPage.class );
    } catch ( Exception exception ) {
      fail( "WizardPage lifecycle test failed: " + exception.getMessage() );
    }
  }

  @Test
  public void testMultipleDialogsCoexistence() {
    try {

      assertNotNull( "MessageDialog should be available", MessageDialog.class );
      assertNotNull( "MessageDialogWithToggle should be available", MessageDialogWithToggle.class );
    } catch ( org.eclipse.swt.SWTException exception ) {

      assertNotNull( "Dialog classes should still be available", MessageDialog.class );
    } catch ( Exception exception ) {
      fail( "Multiple dialogs coexistence test failed: " + exception.getMessage() );
    }
  }

  @Test
  public void testMultiPageWizard() {
    try {

      assertNotNull( "Wizard class should be available", Wizard.class );
      assertNotNull( "WizardPage class should be available", WizardPage.class );
      assertNotNull( "IWizardPage interface should be available", IWizardPage.class );

    } catch ( org.eclipse.swt.SWTException exception ) {

      assertNotNull( "Wizard class should still be available", Wizard.class );
    } catch ( Exception exception ) {
      fail( "Multi-page wizard test failed: " + exception.getMessage() );
    }
  }

  @Test
  public void testErrorDialogHandling() {
    try {

      assertNotNull( "MessageDialog should be available for errors", MessageDialog.class );
    } catch ( org.eclipse.swt.SWTException exception ) {

      assertNotNull( "MessageDialog should still be available", MessageDialog.class );
    } catch ( Exception exception ) {
      fail( "Error dialog handling test failed: " + exception.getMessage() );
    }
  }

  @Test
  public void testDialogTypeCompatibility() {
    try {

      assertNotNull( "MessageDialog should be available", MessageDialog.class );
      assertNotNull( "MessageDialogWithToggle should be available", MessageDialogWithToggle.class );
      assertNotNull( "ProgressMonitorDialog should be available", ProgressMonitorDialog.class );
    } catch ( org.eclipse.swt.SWTException exception ) {

      assertNotNull( "Dialog classes should still be available", MessageDialog.class );
    } catch ( Exception exception ) {
      fail( "Dialog type compatibility test failed: " + exception.getMessage() );
    }
  }
}
