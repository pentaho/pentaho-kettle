/**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

package org.pentaho.di.ui.spoon;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.TabItemInterface;

/**
 * This class handles the display of help information like the welcome page and JDBC info in an embedded browser.
 * 
 * @author Matt
 * @since November 2006
 * 
 */

public class SpoonBrowser implements TabItemInterface
{
    private Shell            shell;
    private Spoon            spoon;
    private String           stringUrl;
    private Composite        composite;
    
    private static Browser browser;

    public SpoonBrowser(Composite parent, final Spoon spoon, final String stringUrl) throws SWTError
    {
        this.shell = parent.getShell();
        this.spoon = spoon;
        this.stringUrl = stringUrl;
        
        composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        
        Composite compTools = new Composite(composite, SWT.NONE);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        compTools.setLayoutData(data);
        compTools.setLayout(new GridLayout(2, false));
        ToolBar navBar = new ToolBar(compTools, SWT.NONE);
        navBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END));
        final ToolItem back = new ToolItem(navBar, SWT.PUSH);
        back.setText("Back");
        back.setEnabled(false);
        final ToolItem forward = new ToolItem(navBar, SWT.PUSH);
        forward.setText("Forward");
        forward.setEnabled(false);
        
        final Composite comp = new Composite(composite, SWT.NONE);
        data = new GridData(GridData.FILL_BOTH);
        comp.setLayoutData(data);
        comp.setLayout(new FillLayout());

        browser = new Browser(comp, SWT.NONE);

        back.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                browser.back();
            }
        });
        forward.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                browser.forward();
            }
        });
        LocationListener locationListener = new LocationListener() {
            public void changed(LocationEvent event) {
                  Browser browser = (Browser)event.widget;
                  back.setEnabled(browser.isBackEnabled());
                  forward.setEnabled(browser.isForwardEnabled());
               }
            public void changing(LocationEvent event) {
               }
            };
            
        browser.addLocationListener(locationListener);
         
        comp.addKeyListener(spoon.defKeys);
        composite.addKeyListener(spoon.defKeys);
        browser.addKeyListener(spoon.defKeys);
                 
        // Set the text
        browser.setUrl(stringUrl);
    }

    /**
     * @return the browser
     */
    public Browser getBrowser()
    {
        return browser;
    }

    /**
     * @return the shell
     */
    public Shell getShell()
    {
        return shell;
    }

    /**
     * @return the spoon
     */
    public Spoon getSpoon()
    {
        return spoon;
    }

    /**
     * @param spoon the spoon to set
     */
    public void setSpoon(Spoon spoon)
    {
        this.spoon = spoon;
    }

    public boolean applyChanges()
    {
        return true;
    }

    public boolean canBeClosed()
    {
        return true;
    }

    public Object getManagedObject()
    {
        return stringUrl;
    }

    public boolean hasContentChanged()
    {
        return false;
    }

    public int showChangedWarning()
    {
        return 0;
    }

    /**
     * @return the composite
     */
    public Composite getComposite()
    {
        return composite;
    }

    /**
     * @param composite the composite to set
     */
    public void setComposite(Composite composite)
    {
        this.composite = composite;
    }

    public EngineMetaInterface getMeta() {
    	return null;
    }

    
}
