package be.ibridge.kettle.test.transexec;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import be.ibridge.kettle.cluster.ClusterSchema;
import be.ibridge.kettle.cluster.SlaveServer;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.util.EnvUtil;
import be.ibridge.kettle.trans.TransExecutionConfiguration;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.dialog.TransExecutionConfigurationDialog;

public class TestTransExecConfigDialog
{
    public static void main(String[] args)
    {
        Display display = new Display();
        Props.init(display, Props.TYPE_PROPERTIES_SPOON);
        EnvUtil.environmentInit();
        Shell shell = new Shell();
        
        TransExecutionConfiguration configuration = new TransExecutionConfiguration();
        configuration.setExecutingClustered(true);
        
        TransMeta transMeta = generateTransMeta();
        
        TransExecutionConfigurationDialog dialog = new TransExecutionConfigurationDialog(shell, configuration, transMeta);
        dialog.open();
        
        Props.getInstance().saveProps();
        display.dispose();
    }

    private static TransMeta generateTransMeta()
    {
        TransMeta transMeta = new TransMeta();
        
        SlaveServer slaveServer = new SlaveServer("sam", "8080", "cluster", "cluster");
        List slaveServers = new ArrayList();
        slaveServers.add(slaveServer);
        ClusterSchema clusterSchema = new ClusterSchema("Local cluster", slaveServers);
        transMeta.getClusterSchemas().add(clusterSchema);
        
        return transMeta;
    }
}   
