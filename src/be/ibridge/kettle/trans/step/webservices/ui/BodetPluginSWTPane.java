package be.ibridge.kettle.trans.step.webservices.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;

import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.dialog.ErrorDialog;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepDialog;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.textfileinput.Messages;
import be.ibridge.kettle.trans.step.webservices.BodetPluginMeta;
import be.ibridge.kettle.trans.step.webservices.FieldLinkWebServiceField;
import be.ibridge.kettle.trans.step.webservices.wsdl.DOMParsing;
import be.ibridge.kettle.trans.step.webservices.wsdl.WSDLArgument;
import be.ibridge.kettle.trans.step.webservices.wsdl.WSDLOperation;
import be.ibridge.kettle.trans.step.webservices.wsdl.WSDLParameter;
import be.ibridge.kettle.trans.step.webservices.wsdl.WSDLService;
import be.ibridge.kettle.trans.step.webservices.wsdl.XsdType;

public class BodetPluginSWTPane extends BaseStepDialog implements StepDialogInterface
{
	private BodetPluginMeta meta;
	
	private Shell shell;
	
	private Display display;

	private Text urlWSTxt;
	
	private Text stepNameTxt;

	private Button loadWSBtn;

	private CTabFolder tabFolder;

	private TreeViewer treeWS;
	
	private Composite compositeTabWebService;
	
	/** Sauvegarde des widgets permettant la saisie des valeurs en entrées */
	private Text[] textsValueIn;
	
	/** Sauvegarde des widgets permettant la saisie des champs en entrées */
	private TableView fieldInTableView;
	
	/** Sauvegarde de la table permettant la saisie des champs en sortie*/
	private TableView fieldOutTableView;
	
	/** Composiste contenant les informations relatives au web service */
	private CTabItem tabItemWebService;

	/** Composite contenant les informations relatives aux paramètres en entrées */
	private CTabItem tabItemValueIn;

	/** Composite contenant les informations relatives aux champs en entrées */
	private CTabItem tabItemFieldIn;

	/** Composite contenant les informations relatives aux champs en sortie */
	private CTabItem tabItemFieldOut;
	
	/** Liste des services disponible dans le WSDL*/
	List wsdlServices;

	/**
	 * Initialisation du tab contenant les informations sur les opérations du
	 * web service
	 */
	private void addTabWebService() {

		// Ajout du tab contenant l'arborescence du web services
		compositeTabWebService = new Composite(tabFolder, SWT.NONE);
		compositeTabWebService.setLayoutData(new GridData(GridData.FILL_BOTH));
		compositeTabWebService.setLayout(new FillLayout());
		props.setLook(compositeTabWebService);
		
		tabItemWebService = new CTabItem(tabFolder, SWT.NONE);
		tabItemWebService.setText("Web Service");
		tabItemWebService.setControl(compositeTabWebService);

		// Ajout de l'arbre
		treeWS = new TreeViewer(compositeTabWebService, SWT.MULTI | SWT.BORDER);
		treeWS.setContentProvider(new WSDLTreeContentProvider());
		treeWS.setLabelProvider(new WSDLLabelProvider());
		props.setLook(treeWS.getTree());
	}
	
	private void selectWSDLOperation(String anOperationName)
	{
		WSDLOperation vOperation = null;
		for(Iterator vIterator = wsdlServices.iterator(); vIterator.hasNext() && vOperation == null;)
		{
			WSDLService vWSDLService = (WSDLService) vIterator.next();
			for(Iterator vItOperation = vWSDLService.getOperations().iterator(); vItOperation.hasNext() && vOperation == null;)
			{
				WSDLOperation vCurrentOperation = (WSDLOperation) vItOperation.next();
				if(vCurrentOperation.getName().equals(anOperationName))
				{
					vOperation = vCurrentOperation;
				}
			}
		}
		
		//Si on a changé d'opération
		if(anOperationName != null && !anOperationName.equalsIgnoreCase(meta.getOperationNameWebService()))
		{
			if(vOperation != null)
			{
				meta.setOperationNameWebService(vOperation.getName());
				// Données en entrée
				meta.getFieldInWebService().clear();
				meta.getValueInWebService().clear();
				meta.getValueInLinkWebServiceFieldList().clear();
				// On tri la liste des arguments par ordre
				// alphabetique
				Collections.sort(vOperation.getArguments(), new Comparator/*<WSDLArgument>*/() {
					public int compare(Object o1, Object o2) {
						return ((WSDLArgument)o1).getName().compareTo(((WSDLArgument)o2).getName());
					}
				});

                for (Iterator iter = vOperation.getArguments().iterator(); iter.hasNext();)
                {
                    WSDLArgument vArgument = (WSDLArgument) iter.next();

					if (vArgument.isMultiple()) {

						// On tri par order alphabetique les paramètres
						Collections.sort(vArgument.getParameters(), new Comparator() {
							public int compare(Object o1, Object o2) {
								return ((WSDLParameter )o1).getName().compareTo(((WSDLParameter)o2).getName());
							}
						});

						meta.setInFieldArgumentNameWebService(vArgument.getName());
						// Si c'est une données multiple on doit se
						// baser sur les champs précédents
                        for (Iterator iterator = vArgument.getParameters().iterator(); iterator.hasNext();) {
                            WSDLParameter vParameter = (WSDLParameter) iterator.next();
                            meta.getFieldInWebService().add(vParameter);
                        }
					} else {
						meta.setInValueArgumentNameWebService(vArgument.getName());
						// Sinon on doit passé en paramètres des
						// valeurs fixes
                        for (Iterator iterator = vArgument.getParameters().iterator(); iterator.hasNext();) {
                            WSDLParameter vParameter = (WSDLParameter) iterator.next();
                            meta.getValueInWebService().add(vParameter);
                        }
					}
				}
				// Données en sortie
				meta.getFieldOutWebService().clear();
				meta.getFieldInLinkWebServiceFieldList().clear();
				for (Iterator iter = vOperation.getReturns().iterator(); iter.hasNext();)
                {
                    WSDLArgument vArgument = (WSDLArgument) iter.next();
					if (vArgument.isMultiple()) {
						// On tri par order alphabetique les
						// paramètres
						Collections.sort(vArgument.getParameters(), new Comparator() {
							public int compare(Object o1, Object o2) {
								return ((WSDLParameter)o1).getName().compareTo(((WSDLParameter)o2).getName());
							}
						});
						meta.setOutFieldArgumentNameWebService(vArgument.getName());
                        for (Iterator iterator = vArgument.getParameters().iterator(); iterator.hasNext();) {
                            WSDLParameter vParameter = (WSDLParameter) iterator.next();
							meta.getFieldOutWebService().add(vParameter);
							meta.getFieldOutLinkWebServiceFieldList().add(new FieldLinkWebServiceField(new Value(vParameter.getName(), XsdType.xdsTypeToKettleType(vParameter.getType())), vParameter.getName()));
						}
					}
				}
			}
		}
		
		//GESTION DES ONGLETS !
		
		//On supprime tous les tabs, on est obligé de les reconstruire pour chargé les bonnes données !
		removeTabValueIn();
		boolean valuesIn = false;
		removeTabFieldIn();
		boolean fieldsIn = false;
		removeTabFieldOut();
		boolean fieldsOut = false;
		
		if(vOperation != null)
		{
            for (Iterator iter = vOperation.getArguments().iterator(); iter.hasNext();) {
                WSDLArgument vArgument = (WSDLArgument) iter.next();
				if (vArgument.isMultiple()) 
				{
					fieldsIn = vArgument.getParameters() != null && vArgument.getParameters().size() > 0;
				} else {
					
					valuesIn = vArgument.getParameters() != null && vArgument.getParameters().size() > 0;
				}
			}
            for (Iterator iter = vOperation.getReturns().iterator(); iter.hasNext();) {
                WSDLArgument vArgument = (WSDLArgument) iter.next();
				if (vArgument.isMultiple()) 
				{
					fieldsOut = true;
				}
			}
		}
	
		//Affichage des bon tabs suivant l'opérations
//		if(valuesIn)
//		{
//			addTabValueIn();
//		}
		if(fieldsIn || valuesIn)
		{
			addTabFieldIn(valuesIn);
		}
		if(fieldsOut)
		{
			addTabFieldOut();
		}
		
	}
	
	
	/**
	 * Initialisation de l'arbre : 
	 * 	- construction par rapport à l'URL du WS
	 *  - ajout du listener de sélection sur l'arbre
	 *
	 */
	private void initTreeTabWebService(String anURI)
	{
		anURI = StringUtil.environmentSubstitute(anURI);
		DOMParsing vDomParsing = new DOMParsing();
		
		//Parcours des services pour les mettres à jour
		try
		{
			wsdlServices = new ArrayList/*<WSDLService>*/(vDomParsing.parse(anURI));	
		}
		catch(Exception e)
		{
			//TODO afficher un message d'erreur qui va bien
			wsdlServices = new ArrayList/*<WSDLService>*/();
			new ErrorDialog(shell, "URI unreachable", "Unable to load " + anURI, e);
			
			log.logError("Unable to load " + anURI, e.getMessage());
		}
		
		treeWS.setInput(wsdlServices);
		
		treeWS.getTree().addSelectionListener(new SelectionAdapter(){
			// @Override
			public void widgetSelected(SelectionEvent arg0) {
				if(arg0.item.getData() instanceof WSDLOperation)
				{
					selectWSDLOperation(((WSDLOperation)arg0.item.getData()).getName());
				}
			}
			
		});
		treeWS.refresh();
		treeWS.expandAll();
	}

	/*
	 * Initialisation du tab contenant les informations sur les opérations du
	 * web services
	private void addTabValueIn() {
		//Ajout de l'affichage
		Composite vCompositeTabValueIn = new Composite(tabFolder, SWT.NONE);
		GridLayout vLayout = new GridLayout();
		vLayout.numColumns = 2;
		vCompositeTabValueIn.setLayout(vLayout);
		props.setLook(vCompositeTabValueIn);

		tabItemValueIn = new CTabItem(tabFolder, SWT.NONE);
		tabItemValueIn.setText("Parameters");
		tabItemValueIn.setControl(vCompositeTabValueIn);
		
		textsValueIn = new Text[meta.getValueInWebService().size()];
		
		int i = 0;
        for (Iterator iter = meta.getValueInWebService().iterator(); iter.hasNext();)
        {
            String vValueName = (String) iter.next();
			Label vLabel = new Label(vCompositeTabValueIn, SWT.NONE);
			GridData vGridData = new GridData();
			vGridData.grabExcessHorizontalSpace = true;
			vGridData.grabExcessVerticalSpace = true;
			vLabel.setLayoutData(vGridData);
			vLabel.setText(vValueName);
			props.setLook(vLabel);
			textsValueIn[i] = new Text(vCompositeTabValueIn, SWT.BORDER);
			vGridData = new GridData(GridData.FILL_HORIZONTAL);
			vGridData.grabExcessHorizontalSpace = true;
			vGridData.grabExcessVerticalSpace = true;
			textsValueIn[i].setLayoutData(vLayout);
			if(((FieldLinkWebServiceField)meta.getValueInLinkWebServiceFieldList().get(i)).getField().getString() != null)
			{
				textsValueIn[i].setText(((FieldLinkWebServiceField)meta.getValueInLinkWebServiceFieldList().get(i)).getField().getString());	
			}
			else
			{
				textsValueIn[i].setText("");
			}
			textsValueIn[i].setLayoutData(vGridData);
			++i;
		}
	}
	*/

	private void removeTabValueIn() {
		if(tabItemValueIn != null)
		{
			tabItemValueIn.dispose();
			tabItemValueIn = null;
		}
	}

	private void addTabFieldIn(boolean valuesIn) {
		
		//Ajout du composant
		Composite vCompositeTabFieldIn = new Composite(tabFolder, SWT.NONE);
		GridLayout vGridLayout = new GridLayout();
		vGridLayout.numColumns = 1;
		vCompositeTabFieldIn.setLayout(vGridLayout);
		props.setLook(vCompositeTabFieldIn);

		tabItemFieldIn = new CTabItem(tabFolder, SWT.NONE);
		if(meta.getInFieldArgumentNameWebService() != null)
		{
			tabItemFieldIn.setText(meta.getInFieldArgumentNameWebService() + " in ");	
		}
		else if(meta.getInValueArgumentNameWebService() != null)
		{
			tabItemFieldIn.setText(meta.getInValueArgumentNameWebService() + " in ");
		}
		else
		{
			tabItemFieldIn.setText(" in ");
		}
		
		tabItemFieldIn.setControl(vCompositeTabFieldIn);

		String[] vFieldsName = null;
		if(valuesIn)
		{
			vFieldsName = new String[meta.getValueInWebService().size()];
			int i = 0;
            for (Iterator iter = meta.getValueInWebService().iterator(); iter.hasNext();)
            {
                String vParameter = ((WSDLParameter)iter.next()).getName();
				vFieldsName[i] = vParameter;
				++i;
			}
		}
		else
		{
			vFieldsName = new String[meta.getFieldInWebService().size()];
			int i = 0;
            for (Iterator iter = meta.getFieldInWebService().iterator(); iter.hasNext();)
            {
                WSDLParameter vParameter = (WSDLParameter) iter.next();
				vFieldsName[i] = vParameter.getName();
				++i;
			}
			
		}
		
		ColumnInfo[] colinf=new ColumnInfo[]
               {
                new ColumnInfo(Messages.getString("TextFileInputDialog.NameColumn.Column"),       ColumnInfo.COLUMN_TYPE_TEXT,    false),
                new ColumnInfo("WSDL Field",       ColumnInfo.COLUMN_TYPE_CCOMBO,  vFieldsName, true ),
               };
        
        fieldInTableView = new TableView(vCompositeTabFieldIn, 
                SWT.FULL_SELECTION | SWT.MULTI, 
                colinf, 
                0,  
                new ModifyListener(){

					public void modifyText(ModifyEvent arg0) {
						//On ne fait rien
					}
        		},
                Props.getInstance()
                );
        
        GridData vGridData = new GridData(GridData.FILL_BOTH);
        fieldInTableView.setLayoutData(vGridData);
        
        Button vButton = new Button(vCompositeTabFieldIn, SWT.NONE);
        vButton.setText("Champs précédent");
        vGridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        vButton.setLayoutData(vGridData);
        vButton.addMouseListener(new MouseAdapter(){
        	// @Override
        	public void mouseDown(MouseEvent arg0) {
        		//On doit récupérer les champs de l'étape précédente !
        		try {
        			Row vRow = transMeta.getPrevStepFields(stepname);
        			if (vRow != null) {
        				for (int i = 0; i < vRow.size(); ++i) {
        					fieldInTableView.add(new String[]{vRow.getValue(i).getName(), ""});
        				}
        			}
        		} catch (KettleStepException e) {
        			e.printStackTrace();
        		}
        	}
        });
        
        fieldInTableView.table.removeAll();
        //Si on a des valeurs en entrées on les utilises
        if(valuesIn)
        {
            for (Iterator iter = meta.getValueInLinkWebServiceFieldList().iterator(); iter.hasNext();)
            {
                FieldLinkWebServiceField vFields = (FieldLinkWebServiceField) iter.next();
        		TableItem vTableItem = new TableItem(fieldInTableView.table, SWT.NONE);
        		vTableItem.setText(1, vFields.getField().getName());
        		vTableItem.setText(2, vFields.getWebServiceField());
        	}
        }
        else
        {
            //Ajout des données
            for (Iterator iter = meta.getFieldInLinkWebServiceFieldList().iterator(); iter.hasNext();)
            {
                FieldLinkWebServiceField vFields = (FieldLinkWebServiceField) iter.next();
            	TableItem vTableItem = new TableItem(fieldInTableView.table, SWT.NONE);
            	vTableItem.setText(1, vFields.getField().getName());
            	vTableItem.setText(2, vFields.getWebServiceField());
            }
        }
        fieldInTableView.setRowNums();
	}
	
	private void removeTabFieldIn()
	{
		if(tabItemFieldIn != null)
		{
			tabItemFieldIn.dispose();
			tabItemFieldIn = null;
		}
	}
	
	private void addTabFieldOut()
	{
		//Initialisation de l'affichage
		Composite vCompositeTabFieldOut = new Composite(tabFolder, SWT.NONE);
		GridLayout vGridLayout = new GridLayout();
		vGridLayout.numColumns = 1;
		vCompositeTabFieldOut.setLayout(vGridLayout);
		props.setLook(vCompositeTabFieldOut);
		
		tabItemFieldOut = new CTabItem(tabFolder, SWT.NONE);
		tabItemFieldOut.setText(meta.getOutFieldArgumentNameWebService() + " out");
		tabItemFieldOut.setControl(vCompositeTabFieldOut);
		
		ColumnInfo[] colinf=new ColumnInfo[]
                   {
                    new ColumnInfo(Messages.getString("TextFileInputDialog.NameColumn.Column"),       ColumnInfo.COLUMN_TYPE_TEXT, false, true),
                    new ColumnInfo(Messages.getString("TextFileInputDialog.TypeColumn.Column"),       ColumnInfo.COLUMN_TYPE_TEXT, false, true),
                   };
            
        fieldOutTableView = new TableView(vCompositeTabFieldOut, 
                SWT.FULL_SELECTION | SWT.MULTI, 
                colinf, 
                0,  
                new ModifyListener(){
					public void modifyText(ModifyEvent arg0) {
						//TODO je ne sais pas trop à quoi cela sert
						System.out.println("Modification");
					}
        		},
                Props.getInstance()
                );
	                            
        GridData vGridData = new GridData(GridData.FILL_BOTH);
        fieldOutTableView.setLayoutData(vGridData);
        
        //Ajout des données
        fieldOutTableView.table.removeAll();
        for (Iterator iter = meta.getFieldOutWebService().iterator(); iter.hasNext();)
        {
            WSDLParameter vParameter = (WSDLParameter) iter.next();
        	TableItem vTableItem = new TableItem(fieldOutTableView.table, SWT.NONE);
        	vTableItem.setText(1, vParameter.getName());
        	vTableItem.setText(2, vParameter.getType());
        	//fieldOutTableView.add(new String[]{vParameter.getName(), vParameter.getType()});
        }
        fieldOutTableView.setRowNums();
	}
	
	private void removeTabFieldOut()
	{
		if(tabItemFieldOut != null)
		{
			tabItemFieldOut.dispose();
			tabItemFieldOut = null;
		}
	}
	
	/**
	 * Gestion du chargement des données à l'affichage de la fenêtre
	 *
	 */
	private void load()
	{
		stepNameTxt.setText(stepname);
		
		urlWSTxt.setText(meta.getUrlWebService() == null ? "" : meta.getUrlWebService());
		if(urlWSTxt.getText() != null && !"".equals(urlWSTxt.getText()))
		{
			initTreeTabWebService(urlWSTxt.getText());
			selectWSDLOperation(meta.getOperationNameWebService());
			//Sélection dans l'arbre de l'élément qui va bien
			TreeItem[] vItems = treeWS.getTree().getItems();
			TreeItem vSelectedItem = null;
			for(int i = 0; i < vItems.length && vSelectedItem == null; ++i)
			{
				for(int j = 0; j < vItems[i].getItems().length && vSelectedItem == null; ++j)
				{
					if(vItems[i].getItems()[j].getData() instanceof WSDLOperation)
					{
						WSDLOperation vOperation = (WSDLOperation)vItems[i].getItems()[j].getData();
						if(vOperation.getName().equals(meta.getOperationNameWebService()))
						{
							vSelectedItem = vItems[i].getItems()[j];
						}
					}
				}
			}
			if(vSelectedItem != null)
			{
				treeWS.getTree().setSelection(vSelectedItem);	
			}
		}
	}
	
	/**
	 * Gestion de la sauvegarde des données à la fermeture de la fenêtre
	 *
	 */
	private void save()
	{
		//Sauvegarde du méta
		meta.setUrlWebService(urlWSTxt.getText());
		//Sauvegarde des values in
		if(tabItemValueIn != null)
		{
			int i = 0;
            for (Iterator iter = meta.getValueInLinkWebServiceFieldList().iterator(); iter.hasNext();)
            {
                FieldLinkWebServiceField vField = (FieldLinkWebServiceField) iter.next();
				vField.getField().setValue(textsValueIn[i].getText());
				++i;
			}
		}
		
		//Sauvegarde des fields in
		if(tabItemFieldIn != null)
		{
			int nbRow = fieldInTableView.nrNonEmpty();
			//Utilisation des valeurs en entrées
			if(meta.getValueInWebService() != null && meta.getValueInWebService().size() > 0)
			{
				meta.getValueInLinkWebServiceFieldList().clear();
				for(int i = 0; i < nbRow; ++i)
				{
					TableItem vTableItem = fieldInTableView.getNonEmpty(i);
					FieldLinkWebServiceField vFieldLinkWebServiceField = new FieldLinkWebServiceField(new Value(vTableItem.getText(1)), vTableItem.getText(2));
					meta.getValueInLinkWebServiceFieldList().add(vFieldLinkWebServiceField);
				}
			}
			//Sinon utilisation des champs en entrées
			else
			{
				meta.getFieldInLinkWebServiceFieldList().clear();
				for(int i = 0; i < nbRow; ++i)
				{
					TableItem vTableItem = fieldInTableView.getNonEmpty(i);
					FieldLinkWebServiceField vFieldLinkWebServiceField = new FieldLinkWebServiceField(new Value(vTableItem.getText(1)), vTableItem.getText(2));
					meta.getFieldInLinkWebServiceFieldList().add(vFieldLinkWebServiceField);
				}
			}
		}
		
		//Sauvegarde des fields out : on ne fait rien c'est seulement de la consultation.
	}

	public BodetPluginSWTPane(Shell aShell, BaseStepMeta in, TransMeta transMeta, String sname) {
		super(aShell, in, transMeta, sname);
		
		meta = (BodetPluginMeta)in;
		
		Shell parent = getParent();
		display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 		props.setLook(shell);
		
		// Identification du layout
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		shell.setLayout(layout);

		//Titre pour le nom du step
		Label vStepNameLabel = new Label(shell, SWT.NONE);
		vStepNameLabel.setText("Step name");
		GridData vGridData = new GridData(GridData.FILL_HORIZONTAL);
		vGridData.grabExcessHorizontalSpace = false;
		vStepNameLabel.setLayoutData(vGridData);
		props.setLook(vStepNameLabel);
		
		//Saisie du nom de l'application
		stepNameTxt = new Text(shell, SWT.BORDER);
		vGridData = new GridData(GridData.FILL_HORIZONTAL);
		vGridData.horizontalSpan = 2;
		stepNameTxt.setLayoutData(vGridData);
		
		// titre de l'application
		Label titleLabel = new Label(shell, SWT.NONE);
		titleLabel.setText("URL");
		vGridData = new GridData(GridData.FILL_HORIZONTAL);
		vGridData.grabExcessHorizontalSpace = false;
		titleLabel.setLayoutData(vGridData);
		props.setLook(titleLabel);

		// url du web services
		urlWSTxt = new Text(shell, SWT.BORDER);
		vGridData = new GridData(GridData.FILL_HORIZONTAL);
		urlWSTxt.setLayoutData(vGridData);

		// Ajout du bouton de chargement du web services
		loadWSBtn = new Button(shell, SWT.PUSH);
		loadWSBtn.setText("Load");
		vGridData = new GridData();
		loadWSBtn.setLayoutData(vGridData);
		//Gestion du chargement
		loadWSBtn.addMouseListener(new MouseAdapter(){
			public void mouseDown(MouseEvent arg0) {
				//Si l'url est renseigné, on essaye de toujours charger
				if(urlWSTxt.getText() != null && !"".equals(urlWSTxt.getText()))
				{
					initTreeTabWebService(urlWSTxt.getText());
					removeTabValueIn();
					removeTabFieldIn();
					removeTabFieldOut();
				}
			}
		});
		
		// Ajout du tab folder
		tabFolder = new CTabFolder(shell, SWT.NONE);
		vGridData = new GridData(GridData.FILL_BOTH);
		vGridData.horizontalSpan = 3;
		tabFolder.setLayoutData(vGridData);
		props.setLook(tabFolder);
		
		// Ajout du tab contenant les informations sur les web services
		addTabWebService();
		
		tabFolder.setSelection(tabItemWebService);

		// Ajout des boutons ok et annuler
		Composite vComposite = new Composite(shell, SWT.NONE);
		vGridData = new GridData(GridData.FILL_HORIZONTAL);
		vGridData.horizontalSpan = 3;
		vComposite.setLayoutData(vGridData);
		props.setLook(vComposite);
		
		GridLayout vCompositeLayout = new GridLayout();
		vCompositeLayout.numColumns = 2;
		vComposite.setLayout(vCompositeLayout);

		Button vOkBtn = new Button(vComposite, SWT.NONE);
		vGridData = new GridData(GridData.FILL_HORIZONTAL);
		vGridData.horizontalAlignment = GridData.END;
		vOkBtn.setLayoutData(vGridData);
		vOkBtn.setText("Ok");
		vOkBtn.addMouseListener(new MouseAdapter(){
			// @Override
			public void mouseDown(MouseEvent arg0){
				stepname = stepNameTxt.getText();
				save();
				shell.dispose();
			}
		});
		props.setLook(vOkBtn);
		

		Button vCancelBtn = new Button(vComposite, SWT.NONE);
		vGridData = new GridData(GridData.FILL_HORIZONTAL);
		vGridData.horizontalAlignment = GridData.BEGINNING;
		vCancelBtn.setLayoutData(vGridData);
		vCancelBtn.setText("Cancel");
		vCancelBtn.addMouseListener(new MouseAdapter(){
			// @Override
			public void mouseDown(MouseEvent arg0) {
				stepname = null;
				shell.dispose();
			}
		});
		props.setLook(vCancelBtn);
		load();
	}
	
	public void dispose() {
		urlWSTxt.dispose();
		loadWSBtn.dispose();
	}

	public String open() 
	{
		shell.open();
		
		while (!shell.isDisposed())
		{
				if (!display.readAndDispatch()) display.sleep();
		}
		
		return stepname;
	}
}
