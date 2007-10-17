package org.pentaho.di.ui.spoon;

import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.w3c.dom.Node;

public class TransFileListener implements FileListener {

    public boolean open(Node transNode, String fname, boolean importfile)
    {
    	Spoon spoon = Spoon.getInstance();
        try
        {
            TransMeta transMeta = new TransMeta();
            transMeta.loadXML(transNode, spoon.getRepository(), true);
            spoon.getProperties().addLastFile(LastUsedFile.FILE_TYPE_TRANSFORMATION, fname, null, false, null);
            spoon.addMenuLast();
            if (!importfile) transMeta.clearChanged();
            transMeta.setFilename(fname);
            spoon.addTransGraph(transMeta);

            spoon.refreshTree();
            spoon.refreshHistory();
            return true;
            
        }
        catch(KettleException e)
        {
            new ErrorDialog(spoon.getShell(), Messages.getString("Spoon.Dialog.ErrorOpening.Title"), Messages.getString("Spoon.Dialog.ErrorOpening.Message")+fname, e);
        }
        return false;
    }

    public boolean save(EngineMetaInterface meta, String fname,boolean export) {
    	Spoon spoon = Spoon.getInstance();
    	EngineMetaInterface lmeta;
    	if (export)
    	{
    		lmeta = (TransMeta)((TransMeta)meta).realClone(false);
    	}
    	else
    		lmeta = meta;
    	return spoon.saveMeta(lmeta, fname);
    }

}
