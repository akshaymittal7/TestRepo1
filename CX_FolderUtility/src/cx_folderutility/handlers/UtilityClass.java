package cx_folderutility.handlers;

import java.io.File;
import java.util.ArrayList;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.loose.core._2006_03.FileManagement.DatasetFileInfo;
import com.teamcenter.services.loose.core._2006_03.FileManagement.GetDatasetWriteTicketsInputData;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core.PropDescriptorService;
import com.teamcenter.services.rac.core.SessionService;
import com.teamcenter.services.rac.core._2006_03.DataManagement.CreateDatasetsResponse;
import com.teamcenter.services.rac.core._2006_03.DataManagement.DatasetProperties;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.services.rac.core._2008_06.PropDescriptor.CreateDescResponse;
import com.teamcenter.services.rac.core._2008_06.PropDescriptor.PropDesc;
import com.teamcenter.soa.client.FileManagementUtility;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.services.rac.core._2006_03.DataManagement.CreateRelationsResponse;
import com.teamcenter.services.rac.core._2006_03.DataManagement.Relationship;

public class UtilityClass 
{
	private TCSession session_;
	private DataManagementService dmService_;
	private PropDescriptorService pdService_;
	private TCPreferenceService prefService_;
	private SessionService sessionService_;

	public UtilityClass(TCSession tcSession) {
		session_ = tcSession;
		dmService_ = DataManagementService.getService(session_);
		pdService_ = PropDescriptorService.getService(session_);
		prefService_ = session_.getPreferenceService();
		sessionService_ = SessionService.getService(tcSession);
	}

	public PropDesc[] getAllCreateAttrs(String boType) {
		return getCreateAttrs(boType, false);
	}

	public PropDesc[] getRequiredCreateAttrs(String boType) {
		return getCreateAttrs(boType, true);
	}

	/**
	 * Returns the Property Descriptors for the Create Attributes for the given
	 * business object type
	 * 
	 * @param boType
	 * @param RequiredOnly
	 * @return
	 */
	private PropDesc[] getCreateAttrs(String boType, boolean RequiredOnly) {
		ArrayList<PropDesc> reqAttrs = new ArrayList<PropDesc>();

		try {
			String[] boTypes = { boType };
			CreateDescResponse createResponse = pdService_
					.getCreateDesc(boTypes);
			for (int i = 0; i < createResponse.createDescs[0].propDescs.length; i++) {
				if (RequiredOnly) {
					if (createResponse.createDescs[0].propDescs[i].isRequired)
						reqAttrs.add(createResponse.createDescs[0].propDescs[i]);
				} else
					reqAttrs.add(createResponse.createDescs[0].propDescs[i]);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return (PropDesc[]) reqAttrs.toArray(new PropDesc[reqAttrs.size()]);
	}

	/**
	 * Creates an Folder using the DataManagementService createObject call.
	 * 
	 * @param folder_name
	 * @return
	 */
	public TCComponentFolder createFolder(String folder_name, String folderType) {

		TCComponentFolder tcFolder = null;
		try {

			CreateInput folderIn = new CreateInput();
			folderIn.boName = folderType;

			folderIn.stringProps.put("object_name", folder_name);

			tcFolder = (TCComponentFolder) createObject(folderIn);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return tcFolder;
	}

	public TCComponent createObject(CreateInput input) throws Exception {
		TCComponent[] objs = null;
		CreateInput[] inputs = new CreateInput[1];
		inputs[0] = input;
		objs = createObjects(inputs);
		if (objs != null & objs.length > 0)
			return objs[0];
		else
			return null;
	}

	public TCComponent[] createObjects(CreateInput[] inputs) throws Exception {
		TCComponent[] objs = null;
		CreateIn[] createIns = new CreateIn[inputs.length];
		for (int i = 0; i < inputs.length; i++) {
			createIns[i] = new CreateIn();
			createIns[i].data = inputs[i];
		}

		CreateResponse cResponse = null;
		try {
			cResponse = dmService_.createObjects(createIns);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (cResponse.output.length > 0) {
			objs = cResponse.output[0].objects;
			System.out.println("Objects created!!");
		} else
			System.out.println("Objects not created.");

		for (int i = 0; i < cResponse.serviceData.sizeOfPartialErrors(); i++) {
			String msgs[] = cResponse.serviceData.getPartialError(i)
					.getMessages();
			String msg = "";
			for (int j = 0; j < msgs.length; j++) {
				System.out.println(msgs[j]);
				msg += msgs[j] + "\n";
			}
			if (msgs.length > 0)
				throw new Exception(msg);
		}
		return objs;
	}
	
	public String[] readPreferenceValues(String preferenceName)
	{
		@SuppressWarnings("deprecation")
		String[] prefernceValues = prefService_.getStringArray( TCPreferenceService.TC_preference_site, preferenceName);
		return prefernceValues;		
	}
	
	public String readPreferenceValue(String preferenceName)
	{
		@SuppressWarnings("deprecation")
		String prefernceValues = prefService_.getString(TCPreferenceService.TC_preference_site, preferenceName);;
		
		return prefernceValues;		
	}
	
	
	public void createDatasetAndUploadFile(String datasetType, String namedReferenceType, File fileToUpload, TCComponent component)
	{
		FileManagementUtility fMSFileManagement = new FileManagementUtility(session_.getSoaConnection());
			
		GetDatasetWriteTicketsInputData[] inputs  = new GetDatasetWriteTicketsInputData[1];
		inputs[0] = getGetDatasetWriteTicketsInputData(datasetType, namedReferenceType, fileToUpload, component);
			
		ServiceData response = fMSFileManagement.putFiles(inputs);
			
		if (response.sizeOfPartialErrors() > 0)
		{
		    System.out.println("FileManagementService upload returned partial errors: " + response.sizeOfPartialErrors());
		}
			
		// Close FMS connection since done
		fMSFileManagement.term();
	}
	
	private GetDatasetWriteTicketsInputData  getGetDatasetWriteTicketsInputData(String datasetType, String namedReferenceType, File fileToUpload, TCComponent component)
	{
		DatasetProperties props = new DatasetProperties();
		props.clientId = "datasetWriteTixTestClientId";
		props.type = datasetType;
		props.name = fileToUpload.getName();
		props.description = fileToUpload.getName();
			
		DatasetProperties[] currProps = {props};
			
		//create a datset
		CreateDatasetsResponse resp =  dmService_.createDatasets(currProps);
			
		//get the dataset
		TCComponentDataset dataset = resp.output[0].dataset;
		
		if(component instanceof TCComponentFolder)
		{
			try 
			{
				component.add("contents", dataset);
			} 
			catch (TCException e) 
			{
				e.printStackTrace();
			}
		}
		if(component instanceof TCComponentItem || component instanceof TCComponentItemRevision)
		{
			try 
			{
				String defaultPasteRelation = component.getDefaultPasteRelation();
				
				CreateRelationsResponse respRel = null;

				Relationship[] relation = new Relationship[1];
				relation[0] = new Relationship();

				relation[0].primaryObject = component;
				relation[0].secondaryObject = dataset;
				relation[0].relationType = defaultPasteRelation;
				respRel = dmService_.createRelations(relation);
				if (respRel.serviceData.sizeOfPartialErrors() > 0) {

					System.out.println(respRel.serviceData.getPartialError(0)
							.getMessages()[0]);
				}
			} 
			catch (TCException e) 
			{
				e.printStackTrace();
			}
		}
			
		//create a file to associate with dataset
		DatasetFileInfo fileInfo = new DatasetFileInfo();
		DatasetFileInfo[] fileInfos = new DatasetFileInfo[1];
			
		// assume this file is in current dir
			
		fileInfo.clientId            = fileToUpload.getName();
		fileInfo.fileName            = fileToUpload.getAbsolutePath();
		fileInfo.namedReferencedName = namedReferenceType;
		if(datasetType.equals("Text"))
		{
			fileInfo.isText = true;
		}
		else
		{
			 fileInfo.isText  = false;
		}
		fileInfo.allowReplace        = true;
		fileInfos[0] = fileInfo;
			
		GetDatasetWriteTicketsInputData inputData = new GetDatasetWriteTicketsInputData();
		inputData.dataset = dataset;
		inputData.createNewVersion = false;
		inputData.datasetFileInfos = fileInfos;
			
		return inputData;
	}
}
