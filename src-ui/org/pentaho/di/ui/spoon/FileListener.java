package org.pentaho.di.ui.spoon;

import org.pentaho.di.core.EngineMetaInterface;
import org.w3c.dom.Node;

public interface FileListener {

    public boolean open(Node transNode, String fname, boolean importfile);

    public boolean save(EngineMetaInterface meta, String fname,boolean isExport);

}
