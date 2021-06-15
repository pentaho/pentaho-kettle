/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.layout;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Scrollable;

/* package */class LayoutGenerator {

    /**
     * Default size for controls with varying contents
     */
    private static final Point defaultSize = new Point(150, 150);

    /**
     * Default wrapping size for wrapped labels
     */
    private static final int wrapSize = 350;

    private static final GridDataFactory nonWrappingLabelData = GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).grab(false, false);

    private static boolean hasStyle(Control c, int style) {
        return (c.getStyle() & style) != 0;
    }

    /**
     * Generates a GridLayout for the given composite by examining its child
     * controls and attaching layout data to any immediate children that do not
     * already have layout data.
     * 
     * @param toGenerate
     *            composite to generate a layout for
     */
    public static void generateLayout(Composite toGenerate) {
        Control[] children = toGenerate.getChildren();

        for (int i = 0; i < children.length; i++) {
            Control control = children[i];

            // Skip any children that already have layout data
            if (control.getLayoutData() != null) {
                continue;
            }

            applyLayoutDataTo(control);
        }
    }

    private static void applyLayoutDataTo(Control control) {
    	defaultsFor(control).applyTo(control);
    }
    
    /**
     * Creates default factory for this control types:
     * <ul>
     * 	<li>{@link Button} with {@link SWT#CHECK}</li>
     * 	<li>{@link Button}</li>
     * 	<li>{@link Composite}</li>
     * </ul>
     * @param control the control the factory is search for
     * @return a default factory for the control
     */
    public static GridDataFactory defaultsFor(Control control) {
        if (control instanceof Button) {
            Button button = (Button) control;

            if (hasStyle(button, SWT.CHECK)) {
                return nonWrappingLabelData.copy();
            }
            return GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).hint(Geometry.max(button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true), LayoutConstants.getMinButtonSize()));
        }

        if (control instanceof Scrollable) {
            Scrollable scrollable = (Scrollable) control;

            if (scrollable instanceof Composite) {
                Composite composite = (Composite) control;

                Layout theLayout = composite.getLayout();
                if (theLayout instanceof GridLayout) {
                    boolean growsHorizontally = false;
                    boolean growsVertically = false;

                    Control[] children = composite.getChildren();
                    for (int i = 0; i < children.length; i++) {
                        Control child = children[i];

                        GridData data = (GridData) child.getLayoutData();

                        if (data != null) {
                            if (data.grabExcessHorizontalSpace) {
                                growsHorizontally = true;
                            }
                            if (data.grabExcessVerticalSpace) {
                                growsVertically = true;
                            }
                        }
                    }

                    return GridDataFactory.fillDefaults().grab(growsHorizontally, growsVertically);
                }
            }
        }

        boolean wrapping = hasStyle(control, SWT.WRAP);

        // Assume any control with the H_SCROLL or V_SCROLL flags are
        // horizontally or vertically
        // scrollable, respectively.
        boolean hScroll = hasStyle(control, SWT.H_SCROLL);
        boolean vScroll = hasStyle(control, SWT.V_SCROLL);

        boolean containsText = hasMethod(control, "setText", new Class[] { String.class }); //$NON-NLS-1$

        // If the control has a setText method, an addModifyListener method, and
        // does not have
        // the SWT.READ_ONLY flag, assume it contains user-editable text.
        boolean userEditable = !hasStyle(control, SWT.READ_ONLY) && containsText && hasMethod(control, "addModifyListener", new Class[] { ModifyListener.class }); //$NON-NLS-1$

        // For controls containing user-editable text...
        if (userEditable) {
            if (hasStyle(control, SWT.MULTI)) {
                vScroll = true;
            }

            if (!wrapping) {
                hScroll = true;
            }
        }

        // Compute the horizontal hint
        int hHint = SWT.DEFAULT;
        boolean grabHorizontal = hScroll;

        // For horizontally-scrollable controls, override their horizontal
        // preferred size
        // with a constant
        if (hScroll) {
            hHint = defaultSize.x;
        } else {
            // For wrapping controls, there are two cases.
            // 1. For controls that contain text (like wrapping labels,
            // read-only text boxes,
            // etc.) override their preferred size with the preferred wrapping
            // point and
            // make them grab horizontal space.
            // 2. For non-text controls (like wrapping toolbars), assume that
            // their non-wrapped
            // size is best.

            if (wrapping) {
                if (containsText) {
                    hHint = wrapSize;
                    grabHorizontal = true;
                }
            }
        }

        int vAlign = SWT.FILL;

        // Heuristic for labels: Controls that contain non-wrapping read-only
        // text should be
        // center-aligned rather than fill-aligned
        if (!vScroll && !wrapping && !userEditable && containsText) {
            vAlign = SWT.CENTER;
        }

        return GridDataFactory.fillDefaults().grab(grabHorizontal, vScroll).align(SWT.FILL, vAlign).hint(hHint, vScroll ? defaultSize.y : SWT.DEFAULT);
    }

    private static boolean hasMethod(Control control, String name, Class[] parameterTypes) {
        Class c = control.getClass();
        try {
            return c.getMethod(name, parameterTypes) != null;
        } catch (SecurityException e) {
            return false;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
