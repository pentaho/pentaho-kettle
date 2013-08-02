package org.pentaho.di.ui.spoon.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TreeUtil;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreAttribute;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.stores.delegate.DelegatingMetaStore;

public class MetaStoreExplorerDialog {
  private static Class<?> PKG = MetaStoreExplorerDialog.class; // for i18n purposes, needed by Translator2
  
  private static LogChannelInterface log = LogChannel.GENERAL;

  private IMetaStore metaStore;

  private List<IMetaStore> metaStoreList;

  private Shell parent;

  private Shell shell;

  private Tree tree;

  private PropsUI props;

  private Button closeButton;

  public MetaStoreExplorerDialog(Shell parent, IMetaStore metaStore) {
    this.metaStore = metaStore;
    this.parent = parent;

    metaStoreList = new ArrayList<IMetaStore>();
    if (metaStore instanceof DelegatingMetaStore) {
      DelegatingMetaStore delegatingMetaStore = (DelegatingMetaStore) metaStore;
      log.logBasic("Exploring delegating meta store containing "+delegatingMetaStore.getMetaStoreList().size());
      for (IMetaStore delMetaStore : delegatingMetaStore.getMetaStoreList()) {
        try {
          log.logBasic(" --> delegated meta store "+delMetaStore.getName());
        } catch(Exception e) {
          log.logError(" --> Error acessing delegated meta store", e);
        }
      }
      metaStoreList.addAll(((DelegatingMetaStore) metaStore).getMetaStoreList());
    } else {
      metaStoreList.add(metaStore);
    }
    props = PropsUI.getInstance();
  }

  public void open() {
    Display display = parent.getDisplay();

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
    props.setLook(shell);
    shell.setImage(GUIResource.getInstance().getImageSpoon());

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "MetaStoreExplorerDialog.Dialog.Title"));

    int margin = Const.MARGIN;

    closeButton = new Button(shell, SWT.PUSH);
    closeButton.setText(BaseMessages.getString(PKG, "System.Button.Close"));

    BaseStepDialog.positionBottomButtons(shell, new Button[] { closeButton, }, margin, null);

    // Add listeners
    closeButton.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        close();
      }
    });
    
    tree = new Tree(shell, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
    props.setLook(tree);
    tree.setHeaderVisible(true);
    FormData treeFormData = new FormData();
    treeFormData.left   = new FormAttachment(0, 0); // To the right of the label
    treeFormData.top    = new FormAttachment(0, 0);
    treeFormData.right  = new FormAttachment(100, 0);
    treeFormData.bottom = new FormAttachment(closeButton, -margin*2);
    tree.setLayoutData(treeFormData);

    TreeColumn keyColumn = new TreeColumn(tree, SWT.LEFT);
    keyColumn.setText("Metastore, Namespace, Element type, element name");
    keyColumn.setWidth(300);

    TreeColumn valueColumn = new TreeColumn(tree, SWT.LEFT);
    valueColumn.setText("Description or value");
    valueColumn.setWidth(300);

    TreeColumn idColumn = new TreeColumn(tree, SWT.LEFT);
    idColumn.setText("id");
    idColumn.setWidth(300);
    
    tree.addMenuDetectListener(new MenuDetectListener() {
      
      @Override
      public void menuDetected(MenuDetectEvent event) {
        TreeItem treeItem = tree.getItem(new Point(event.x, event.y));
        if (treeItem!=null) {
          log.logBasic("hit item : "+treeItem.getText());
        }
      }
    });

    try {
      refreshTree();
    } catch(Exception e) {
      new ErrorDialog(shell, "Error", "Unexpected error displaying metastore information", e);
    }

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e) {
        close();
      }
    });

    BaseStepDialog.setSize(shell);

    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
  }

  private void close() {
    props.setScreen(new WindowProperty(shell));
    shell.dispose();
  }

  public void refreshTree() throws MetaStoreException {
    tree.removeAll();
    
    // Top level: the MetaStore
    //
    for (int m=0;m<metaStoreList.size();m++) {
      IMetaStore metaStore = metaStoreList.get(m);
      TreeItem metaStoreItem = new TreeItem(tree, SWT.NONE);
      
      metaStoreItem.setText(0, Const.NVL(metaStore.getName(), "metastore-"+(m+1)));
      metaStoreItem.setText(1, Const.NVL(metaStore.getDescription(), ""));
      
      // level: Namespace
      //
      List<String> namespaces = metaStore.getNamespaces();
      for (String namespace : namespaces) {
        TreeItem namespaceItem = new TreeItem(metaStoreItem, SWT.NONE);
        
        namespaceItem.setText(0, Const.NVL(namespace, ""));
            
        // level: element type
        //
        List<IMetaStoreElementType> elementTypes = metaStore.getElementTypes(namespace);
        for (IMetaStoreElementType elementType : elementTypes) {
          TreeItem elementTypeItem = new TreeItem(namespaceItem, SWT.NONE);
              
          elementTypeItem.setText(0, Const.NVL(elementType.getName(), ""));
          elementTypeItem.setText(1, Const.NVL(elementType.getDescription(), ""));

          // level: element
          //
          List<IMetaStoreElement> elements = metaStore.getElements(namespace, elementType);
          for (final IMetaStoreElement element : elements) {
            TreeItem elementItem = new TreeItem(elementTypeItem, SWT.NONE);
                
            elementItem.setText(0, Const.NVL(element.getName(), ""));
            elementItem.setText(2, Const.NVL(element.getId(), ""));
            
            elementItem.addListener(SWT.Selection, new Listener() {
              
              @Override
              public void handleEvent(Event event) {
                log.logBasic("Selected : "+element.getName());
              }
            });
           
            addAttributesToTree(elementItem, element);
          }

        }
      }
    }
    TreeUtil.setOptimalWidthOnColumns(tree);
  }

  private void addAttributesToTree(TreeItem parentItem, IMetaStoreAttribute parentAttribute) {
    for (IMetaStoreAttribute childAttribute : parentAttribute.getChildren()) {
      TreeItem treeItem = new TreeItem(parentItem, SWT.NONE);
      treeItem.setText(0, Const.NVL(childAttribute.getId(), ""));
      treeItem.setText(1, childAttribute.getValue()==null ? "" : childAttribute.getValue().toString());

      // Add more child attributes below
      //
      addAttributesToTree(treeItem, childAttribute);
    }
  }

  public IMetaStore getMetaStore() {
    return metaStore;
  }

  public void setMetaStore(IMetaStore metaStore) {
    this.metaStore = metaStore;
  }

  public List<IMetaStore> getMetaStoreList() {
    return metaStoreList;
  }

  public void setMetaStoreList(List<IMetaStore> metaStoreList) {
    this.metaStoreList = metaStoreList;
  }

}
