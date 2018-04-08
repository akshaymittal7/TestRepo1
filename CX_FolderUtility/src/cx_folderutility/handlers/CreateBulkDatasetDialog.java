package cx_folderutility.handlers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import cx_folderutility.Activator;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;


public class CreateBulkDatasetDialog extends Dialog {

	protected Object result_;
	protected Shell shellFolderDatasetUtility_;
	private Text folderPath_;
	protected TCSession tcSession_;
	protected UtilityClass utilObject_;
	private InterfaceAIFComponent currComp_;
	protected String[] preferenceValues_;
	protected Map<String, String> fileDatasetMap_;
	protected String folderTypeFromPreference_;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public CreateBulkDatasetDialog(Shell parent, int style) {
		super(parent, style);
		setText("SWT Dialog");
		AbstractAIFUIApplication localAbstractAIFUIApplication = AIFUtility.getCurrentApplication();
        tcSession_ = (TCSession) localAbstractAIFUIApplication.getSession();
        utilObject_ = new UtilityClass(tcSession_);
        currComp_ = AIFUtility.getCurrentApplication().getTargetComponent();
        preferenceValues_ = utilObject_.readPreferenceValues("CREATE_BULK_DATASET_UTILITY_MAPPING");
        fileDatasetMap_ = createFileDatasetTypeMappingFromPreference();
        folderTypeFromPreference_ = utilObject_.readPreferenceValue("CREATE_BULK_DATASET_UTILITY_FOLDER_TYPE");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shellFolderDatasetUtility_.open();
		shellFolderDatasetUtility_.layout();
		Display display = getParent().getDisplay();
		while (!shellFolderDatasetUtility_.isDisposed()) {
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
		shellFolderDatasetUtility_ = new Shell(getParent(), SWT.DIALOG_TRIM);
		shellFolderDatasetUtility_.setSize(519, 173);
		shellFolderDatasetUtility_.setText("Folder Dataset Utility");
		
		Image dialogIcon = Activator.getImageDescriptor("icons/teamcenter_app_16.gif").createImage();
		shellFolderDatasetUtility_.setImage(dialogIcon);
		
		Label lblSelectFolder = new Label(shellFolderDatasetUtility_, SWT.NONE);
		lblSelectFolder.setBounds(10, 31, 62, 20);
		lblSelectFolder.setText("Folder:");
		
		folderPath_ = new Text(shellFolderDatasetUtility_, SWT.BORDER);
		folderPath_.setBounds(78, 28, 320, 26);
		
		Button okButton = new Button(shellFolderDatasetUtility_, SWT.NONE);
		okButton.setBounds(134, 79, 90, 30);
		okButton.setText("OK");
		okButton.setToolTipText("Button will be enabled only when a Folder is selected");
		okButton.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				String folderPathString = folderPath_.getText();
				//MessageDialog.openInformation(getParent(),"Selected folder is...",folderPathString);
				if(currComp_ instanceof TCComponentFolder)
				{
					listFilesForFolder(new File(folderPathString), (TCComponentFolder) currComp_);
				}
				if(currComp_ instanceof TCComponentItem)
				{
					listFilesForFolder(new File(folderPathString), (TCComponentItem) currComp_);
				}
				if(currComp_ instanceof TCComponentItemRevision)
				{
					listFilesForFolder(new File(folderPathString), (TCComponentItemRevision) currComp_);
				}
				
				shellFolderDatasetUtility_.close();
			}
		});
		
		Button cancelButton = new Button(shellFolderDatasetUtility_, SWT.NONE);
		cancelButton.setBounds(230, 79, 90, 30);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() 
		{
			public void widgetSelected(SelectionEvent e) 
			{
				shellFolderDatasetUtility_.close();
			}
		});
		
		Button browseButton = new Button(shellFolderDatasetUtility_, SWT.NONE);
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				 DirectoryDialog dlg = new DirectoryDialog(getParent());

			        dlg.setFilterPath(folderPath_.getText());
			        dlg.setText("Create Bulk Dataset Utility");
			        dlg.setMessage("Select a directory");
			        String dir = dlg.open();
			        if (dir != null) 
			        {
			          folderPath_.setText(dir);
			        }
					//MessageDialog.openInformation(getParent(),"Selected folder is...",folderPath.getText());
			}
		});
		browseButton.setBounds(414, 26, 90, 30);
		browseButton.setText("Browse");
		
		if (currComp_ instanceof TCComponentFolder || currComp_ instanceof TCComponentItem 
				|| currComp_ instanceof TCComponentItemRevision)
		{
			okButton.setEnabled(true);
		}
		else
		{
			okButton.setEnabled(false);
		}
	}
	
	public void listFilesForFolder(File folder, TCComponentFolder parentFolder) 
	{
		File[] listOfItemsInFolder = folder.listFiles();
		TCComponentFolder currentCreatedFolder;
		
		for(int count=0; count < listOfItemsInFolder.length; count++)
		{
			if(listOfItemsInFolder[count].isDirectory())
			{
				currentCreatedFolder = utilObject_.createFolder(listOfItemsInFolder[count].getName(), folderTypeFromPreference_);
				try 
				{
					parentFolder.add("contents", currentCreatedFolder);
				} 
				catch (TCException e) 
				{
					e.printStackTrace();
				}
				listFilesForFolder(listOfItemsInFolder[count], currentCreatedFolder);
			}
			else
			{
				String mapValue = "";
				String datasetType ="";
				String namedReferenceType = "";
				String fileName = listOfItemsInFolder[count].getName();
				String extension = fileName.substring(fileName.indexOf("."));
						
				mapValue = fileDatasetMap_.get(extension);
				
				if(mapValue != null && !mapValue.equals(""))
				{
					StringTokenizer tokens = new StringTokenizer(mapValue, "=");
					if(tokens!= null && tokens.hasMoreTokens())
					{
						datasetType = tokens.nextToken();
						namedReferenceType = tokens.nextToken();
					}
					
					if(datasetType != null && !datasetType.equals(""))
					{
						utilObject_.createDatasetAndUploadFile(datasetType, namedReferenceType,  listOfItemsInFolder[count], parentFolder);
					}
				}
			}
		}
	}
	
	public void listFilesForFolder(File folder, TCComponent component) 
	{
		File[] listOfItemsInFolder = folder.listFiles();
		//TCComponentFolder currentCreatedFolder;
		
		for(int count=0; count < listOfItemsInFolder.length; count++)
		{
			if(listOfItemsInFolder[count].isDirectory())
			{
				listFilesForFolder(listOfItemsInFolder[count], component);
			}
			else
			{
				String mapValue = "";
				String datasetType ="";
				String namedReferenceType = "";
				String fileName = listOfItemsInFolder[count].getName();
				String extension = fileName.substring(fileName.indexOf("."));
						
				mapValue = fileDatasetMap_.get(extension);
				
				if(mapValue != null && !mapValue.equals(""))
				{
					StringTokenizer tokens = new StringTokenizer(mapValue, "=");
					if(tokens!= null && tokens.hasMoreTokens())
					{
						datasetType = tokens.nextToken();
						namedReferenceType = tokens.nextToken();
					}
					
					if(datasetType != null && !datasetType.equals(""))
					{
						utilObject_.createDatasetAndUploadFile(datasetType, namedReferenceType,  listOfItemsInFolder[count], component);
					}
				}
			}
		}
	}
	
	public Map<String, String> createFileDatasetTypeMappingFromPreference()
	{
		String key = "";
		String value = "";
		Map<String, String> fileDatasetMap = new HashMap<String, String>();
		for(int count=0; count<preferenceValues_.length; count++)
		{
			StringTokenizer tokens = new StringTokenizer(preferenceValues_[count], "=");
			if(tokens!= null && tokens.hasMoreTokens())
			{
				key = tokens.nextToken();
				value = tokens.nextToken() + "=" + tokens.nextToken();
				fileDatasetMap.put(key, value);
			}
		}
		return fileDatasetMap;
	}
}
