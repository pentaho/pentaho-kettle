package org.pentaho.di.core.gui;

import org.eclipse.swt.graphics.Image;
import org.pentaho.di.trans.TransMeta;

public interface SpoonInterface {

    public boolean addSpoonBrowser(String name, String urlString);

    public void addTransGraph(TransMeta transMeta);

    public Object[] messageDialogWithToggle( String dialogTitle, Image image, String message, int dialogImageType, String buttonLabels[], int defaultIndex, String toggleMessage, boolean toggleState );


}
