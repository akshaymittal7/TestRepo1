package cx_con_tools.dialog;

import cx_con_tools.Activator;
import cx_con_tools.utility.CONUtilityClass;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.graphics.Image;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMView;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ItemIdsAndInitialRevisionIds;


public class ItemRenameDialog extends Dialog {

	protected Object result_;
	protected Shell shellItemRenameUtility_;
	protected TCSession tcSession_;
	protected CONUtilityClass utilObject_;
	private InterfaceAIFComponent currComp_;
	private Text newItemIdText_;
	private Combo itemNameComboText_;
	private Text descriptionText_;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ItemRenameDialog(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
		AbstractAIFUIApplication localAbstractAIFUIApplication = AIFUtility.getCurrentApplication();
        tcSession_ = (TCSession) localAbstractAIFUIApplication.getSession();
        utilObject_ = new CONUtilityClass(tcSession_);
        currComp_ = AIFUtility.getCurrentApplication().getTargetComponent();
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shellItemRenameUtility_.open();
		shellItemRenameUtility_.layout();
		Display display = getParent().getDisplay();
		while (!shellItemRenameUtility_.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result_;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shellItemRenameUtility_ = new Shell(getParent(), SWT.DIALOG_TRIM);
		shellItemRenameUtility_.setSize(632, 195);
		shellItemRenameUtility_.setText("Rename Item");
		
		Image dialogIcon = Activator.getImageDescriptor("icons/teamcenter_app_16.gif").createImage();
		shellItemRenameUtility_.setImage(dialogIcon);
		
		Label newItemIdLabel = new Label(shellItemRenameUtility_, SWT.NONE);
		newItemIdLabel.setBounds(10, 10, 89, 20);
		newItemIdLabel.setText("New Item Id:");
		
		newItemIdText_ = new Text(shellItemRenameUtility_, SWT.BORDER);
		newItemIdText_.setBounds(105, 7, 203, 26);
		
		Button assignButton = new Button(shellItemRenameUtility_, SWT.NONE);
		assignButton.setBounds(314, 5, 90, 30);
		assignButton.setText("Assign");
		assignButton.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				String itemType = "";
				TCComponentItem currentItem = (TCComponentItem) currComp_;
				itemType = currentItem.getType();
				try 
				{
					ItemIdsAndInitialRevisionIds[] itemIdsAndInitialRevisionIds = utilObject_.generateItemIds(1, itemType);
					newItemIdText_.setText(itemIdsAndInitialRevisionIds[0].newItemId);
				} 
				catch (ServiceException e1) 
				{
					e1.printStackTrace();
				}
			}
		});
		
		Label newItemNameLabel = new Label(shellItemRenameUtility_, SWT.NONE);
		newItemNameLabel.setBounds(10, 42, 70, 20);
		newItemNameLabel.setText("Name:");
		
		itemNameComboText_ = new Combo(shellItemRenameUtility_, SWT.NONE);
		itemNameComboText_.setBounds(105, 38, 343, 28);
		String [] lovValues = utilObject_.getStringLOVForProperty((TCComponentItem) currComp_, "object_name");
		if(lovValues != null)
		{
			String currentItemName = utilObject_.getItemName((TCComponentItem) currComp_);
			if(lovValues.length > 0)
			{
				itemNameComboText_.setItems(lovValues);
			}
			else
			{
				itemNameComboText_.setItems(new String[] {currentItemName});
			}
			
			itemNameComboText_.setText(currentItemName);
		}
		else
		{
			String currentItemName = utilObject_.getItemName((TCComponentItem) currComp_);
			itemNameComboText_.setItems(new String[] {currentItemName});
			itemNameComboText_.setText(currentItemName);
		}
		
		Label descriptionLabel = new Label(shellItemRenameUtility_, SWT.NONE);
		descriptionLabel.setBounds(10, 75, 89, 20);
		descriptionLabel.setText("Description:");
		
		descriptionText_ = new Text(shellItemRenameUtility_, SWT.BORDER);
		descriptionText_.setBounds(105, 72, 511, 26);
		
		final Button okButton = new Button(shellItemRenameUtility_, SWT.NONE);
		okButton.setBounds(190, 120, 90, 30);
		okButton.setText("OK");
		okButton.setEnabled(false);
		okButton.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				try 
				{
					int returnValue = 1;
					newItemIdText_.setEnabled(false);
					//Finding all the related objects
					TCComponent[] relatedComponents = ((TCComponent) currComp_).getRelatedComponents();
					//Finding the BOM Views as well
					TCComponent[] bomViews = ((TCComponent) currComp_).getTCProperty("bom_view_tags").getReferenceValueArray();
					//Finding and storing the old Item id
					String oldItemId = utilObject_.getItemID((TCComponent) currComp_);
					//Updating the item detais
					returnValue = utilObject_.updateItemDetails((TCComponent) currComp_, newItemIdText_.getText(), itemNameComboText_.getText(), descriptionText_.getText());
					if(returnValue != 0)
					{
						shellItemRenameUtility_.close();
						MessageDialog.openError(getParent(),"ERROR", "Item Rename is not successful. \nPlease check the input.");
						return;
					}
					//Updating the Item revisions and the Datasets
					System.out.println("Updating the related components");
					for(int count = 0; count < relatedComponents.length; count++)
					{
						if(relatedComponents[count] instanceof TCComponentItemRevision)
						{
							String itemRevisionId = utilObject_.getItemRevisionID((TCComponentItemRevision)relatedComponents[count]);
							//Find all the related objects from the Revision
							TCComponent[] relatedDatasetsToRevision = relatedComponents[count].getRelatedComponents();
							//Finding all the BOM View revisions
							TCComponent[] bomViewRevisions = relatedComponents[count].getTCProperty("structure_revisions").getReferenceValueArray();
							//Updating the item revisions
							utilObject_.updateItemDetails(relatedComponents[count], newItemIdText_.getText(), itemNameComboText_.getText(), descriptionText_.getText());
							
							for(int subCount=0; subCount < relatedDatasetsToRevision.length; subCount++)
							{
								//Update the dataset which are attached to the revision
								if(relatedDatasetsToRevision[subCount] instanceof TCComponentDataset ||
										relatedDatasetsToRevision[subCount] instanceof TCComponentForm)
								{
									utilObject_.replaceItemIdInDatasetOrFormName(relatedDatasetsToRevision[subCount], oldItemId, newItemIdText_.getText());
								}
							}
							
							for(int subCount=0; subCount < bomViewRevisions.length; subCount++)
							{
								//Update the BVRs that are attached to the revision
								if(bomViewRevisions[subCount] instanceof TCComponentBOMViewRevision)
								{
									System.out.println("Updating the BOM View Revision");
									//The name in the BOM View is different
									utilObject_.updateItemDetails(bomViewRevisions[subCount], newItemIdText_.getText(), newItemIdText_.getText()+ "/" + itemRevisionId +"/View", 
											descriptionText_.getText());
								}
							}
						}
						if(relatedComponents[count] instanceof TCComponentForm)
						{
							utilObject_.replaceItemIdInDatasetOrFormName(relatedComponents[count], oldItemId, newItemIdText_.getText());
						}
					}
					
					for(int count = 0; count < bomViews.length; count++)
					{
						if(bomViews[count] instanceof TCComponentBOMView)
						{
							System.out.println("Updating the BOM View");
							//The name in the BOM View is different
							utilObject_.updateItemDetails(bomViews[count], newItemIdText_.getText(), newItemIdText_.getText()+"/View", descriptionText_.getText());
						}
					}
				} 
				catch (TCException e1) 
				{
					e1.printStackTrace();
				}
				shellItemRenameUtility_.close();
				MessageDialog.openInformation(getParent(),"Success", "Item Rename done successfully.");
			}
		});
		
		Button cancelButton = new Button(shellItemRenameUtility_, SWT.NONE);
		cancelButton.setBounds(297, 120, 90, 30);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				shellItemRenameUtility_.close();
			}
		});
		
		newItemIdText_.addModifyListener(new ModifyListener() 
		{
			@Override
			public void modifyText(ModifyEvent arg0) 
			{
				if (!newItemIdText_.getText().equals("") && !itemNameComboText_.getText().equals("")) 
				{
					okButton.setEnabled(true);
				}
				else 
				{
					okButton.setEnabled(false);
				}
			}
		});
		
		itemNameComboText_.addModifyListener(new ModifyListener() 
		{
			@Override
			public void modifyText(ModifyEvent arg0) 
			{
				if (!newItemIdText_.getText().equals("") && !itemNameComboText_.getText().equals("")) 
				{
					okButton.setEnabled(true);
				}
				else 
				{
					okButton.setEnabled(false);
				}
			}
		});
	}
}
