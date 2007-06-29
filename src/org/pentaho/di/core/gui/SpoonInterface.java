package org.pentaho.di.core.gui;

import org.pentaho.di.trans.TransMeta;

public interface SpoonInterface {

    public boolean addSpoonBrowser(String name, String urlString);

    public void addTransGraph(TransMeta transMeta);


}
