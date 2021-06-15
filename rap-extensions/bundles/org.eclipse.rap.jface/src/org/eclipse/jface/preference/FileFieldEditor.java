// RAP [bm]: FileDialog - we could use upload instead
///*******************************************************************************
// * Copyright (c) 2000, 2007 IBM Corporation and others.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     IBM Corporation - initial API and implementation
// *******************************************************************************/
//package org.eclipse.jface.preference;
//
//import java.io.File;
//
//import org.eclipse.jface.resource.JFaceResources;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.FileDialog;
//
///**
// * A field editor for a file path type preference. A standard file 
// * dialog appears when the user presses the change button.
// */
//public class FileFieldEditor extends StringButtonFieldEditor {
//
//    /**
//     * List of legal file extension suffixes, or <code>null</code>
//     * for system defaults.
//     */
//    private String[] extensions = null;
//
//    /**
//     * Indicates whether the path must be absolute;
//     * <code>false</code> by default.
//     */
//    private boolean enforceAbsolute = false;
//
//    /**
//     * Creates a new file field editor 
//     */
//    protected FileFieldEditor() {
//    }
//
//    /**
//     * Creates a file field editor.
//     * 
//     * @param name the name of the preference this field editor works on
//     * @param labelText the label text of the field editor
//     * @param parent the parent of the field editor's control
//     */
//    public FileFieldEditor(String name, String labelText, Composite parent) {
//        this(name, labelText, false, parent);
//    }
//    
//    /**
//     * Creates a file field editor.
//     * 
//     * @param name the name of the preference this field editor works on
//     * @param labelText the label text of the field editor
//     * @param enforceAbsolute <code>true</code> if the file path
//     *  must be absolute, and <code>false</code> otherwise
//     * @param parent the parent of the field editor's control
//     */
//    public FileFieldEditor(String name, String labelText, boolean enforceAbsolute, Composite parent) {
//        this(name, labelText, enforceAbsolute, VALIDATE_ON_FOCUS_LOST, parent);
//    }
//    /**
//     * Creates a file field editor.
//     * 
//     * @param name the name of the preference this field editor works on
//     * @param labelText the label text of the field editor
//     * @param enforceAbsolute <code>true</code> if the file path
//     *  must be absolute, and <code>false</code> otherwise
//     * @param validationStrategy either {@link StringButtonFieldEditor#VALIDATE_ON_KEY_STROKE}
//     *  to perform on the fly checking, or {@link StringButtonFieldEditor#VALIDATE_ON_FOCUS_LOST}
//     *  (the default) to perform validation only after the text has been typed in
//     * @param parent the parent of the field editor's control.
//     * @since 1.1
//     * @see StringButtonFieldEditor#VALIDATE_ON_KEY_STROKE
//     * @see StringButtonFieldEditor#VALIDATE_ON_FOCUS_LOST
//     */
//    public FileFieldEditor(String name, String labelText,
//            boolean enforceAbsolute, int validationStrategy, Composite parent) {
//        init(name, labelText);
//        this.enforceAbsolute = enforceAbsolute;
//        setErrorMessage(JFaceResources
//                .getString("FileFieldEditor.errorMessage"));//$NON-NLS-1$
//        setChangeButtonText(JFaceResources.getString("openBrowse"));//$NON-NLS-1$
//        setValidateStrategy(validationStrategy);
//        createControl(parent);
//    }
//
//    /* (non-Javadoc)
//     * Method declared on StringButtonFieldEditor.
//     * Opens the file chooser dialog and returns the selected file.
//     */
//    protected String changePressed() {
//        File f = new File(getTextControl().getText());
//        if (!f.exists()) {
//			f = null;
//		}
//        File d = getFile(f);
//        if (d == null) {
//			return null;
//		}
//
//        return d.getAbsolutePath();
//    }
//
//    /* (non-Javadoc)
//     * Method declared on StringFieldEditor.
//     * Checks whether the text input field specifies an existing file.
//     */
//    protected boolean checkState() {
//
//        String msg = null;
//
//        String path = getTextControl().getText();
//        if (path != null) {
//			path = path.trim();
//		} else {
//			path = "";//$NON-NLS-1$
//		}
//        if (path.length() == 0) {
//            if (!isEmptyStringAllowed()) {
//				msg = getErrorMessage();
//			}
//        } else {
//            File file = new File(path);
//            if (file.isFile()) {
//                if (enforceAbsolute && !file.isAbsolute()) {
//					msg = JFaceResources
//                            .getString("FileFieldEditor.errorMessage2");//$NON-NLS-1$
//				}
//            } else {
//                msg = getErrorMessage();
//            }
//        }
//
//        if (msg != null) { // error
//            showErrorMessage(msg);
//            return false;
//        }
//
//        // OK!
//        clearErrorMessage();
//        return true;
//    }
//
//    /**
//     * Helper to open the file chooser dialog.
//     * @param startingDirectory the directory to open the dialog on.
//     * @return File The File the user selected or <code>null</code> if they
//     * do not.
//     */
//    private File getFile(File startingDirectory) {
//
//        FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
//        if (startingDirectory != null) {
//			dialog.setFileName(startingDirectory.getPath());
//		}
//        if (extensions != null) {
//			dialog.setFilterExtensions(extensions);
//		}
//        String file = dialog.open();
//        if (file != null) {
//            file = file.trim();
//            if (file.length() > 0) {
//				return new File(file);
//			}
//        }
//
//        return null;
//    }
//
//    /**
//     * Sets this file field editor's file extension filter.
//     *
//     * @param extensions a list of file extension, or <code>null</code> 
//     * to set the filter to the system's default value
//     */
//    public void setFileExtensions(String[] extensions) {
//        this.extensions = extensions;
//    }
//}
