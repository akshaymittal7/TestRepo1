package cx_con_tools.utility;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMView;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core.LOVService;
import com.teamcenter.services.rac.core._2007_01.DataManagement.VecStruct;
import com.teamcenter.services.rac.core._2011_06.LOV.LOVAttachment;
import com.teamcenter.services.rac.core._2011_06.LOV.LOVAttachmentsInput;
import com.teamcenter.services.rac.core._2011_06.LOV.LOVAttachmentsResponse;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.exceptions.NotLoadedException;
import com.teamcenter.services.rac.core._2006_03.DataManagement.GenerateItemIdsAndInitialRevisionIdsProperties;
import com.teamcenter.services.rac.core._2006_03.DataManagement.GenerateItemIdsAndInitialRevisionIdsResponse;
import com.teamcenter.services.rac.core._2006_03.DataManagement.ItemIdsAndInitialRevisionIds;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.Property;


public class CONUtilityClass 
{
	private TCSession session_;
	private DataManagementService dmService_;
	private TCPreferenceService prefService_;
	private LOVService lovService_;

	public CONUtilityClass(TCSession tcSession) {
		session_ = tcSession;
		dmService_ = DataManagementService.getService(session_);
		prefService_ = session_.getPreferenceService();
		lovService_ = LOVService.getService(session_);
	}
	
	public String[] readPreferenceValues(String preferenceName)
	{
		@SuppressWarnings("deprecation")
		String[] prefernceValues = prefService_.getStringArray( TCPreferenceService.TC_preference_site, preferenceName);
		return prefernceValues;		
	}
	
	public int updateItemDetails(TCComponent currComp, String newItemId, String itemName, String itemDescription)
	{
		HashMap<String, VecStruct> propertyMap = new HashMap<String, VecStruct>();
		DataManagementService.VecStruct itemIdProperty = new DataManagementService.VecStruct();
		DataManagementService.VecStruct itemNamePropety = new DataManagementService.VecStruct();
		DataManagementService.VecStruct itemDescriptionProperty = new DataManagementService.VecStruct();
		itemIdProperty.stringVec = new String[] { newItemId };
		itemNamePropety.stringVec = new String[] { itemName };
		itemDescriptionProperty.stringVec = new String[] { itemDescription };
		if(currComp instanceof TCComponentItem)
		{
			propertyMap.put("item_id", itemIdProperty);
		}
		propertyMap.put("object_name", itemNamePropety);
		propertyMap.put("object_desc", itemDescriptionProperty);
		
		ServiceData sd = dmService_.setProperties(new TCComponent[] { currComp }, propertyMap);
		if(sd.sizeOfPartialErrors() > 0)
		{
			System.out.println("Error updating Item details");
			return -1;
		}
		else
		{
			System.out.println("Item details updated successfully");
			return 0;
		}
	}
	
	public String getItemID(TCComponent currComp)
	{
		ServiceData sd = dmService_.getProperties(new TCComponent[] {currComp}, new String[] {"item_id"});
		ModelObject datasetModelObject = sd.getPlainObject(0);
		String itemId = "";
		try 
		{
			Property prop = datasetModelObject.getPropertyObject("item_id");
			itemId = prop.getStringValue();
		} 
		catch (NotLoadedException e) 
		{
			e.printStackTrace();
		}
		return itemId;
	}
	
	public String getItemRevisionID(TCComponentItemRevision currComp)
	{
		ServiceData sd = dmService_.getProperties(new TCComponentItemRevision[] {currComp}, new String[] {"item_revision_id"});
		ModelObject datasetModelObject = sd.getPlainObject(0);
		String itemRevisionId = "";
		try 
		{
			Property prop = datasetModelObject.getPropertyObject("item_revision_id");
			itemRevisionId = prop.getStringValue();
		} 
		catch (NotLoadedException e) 
		{
			e.printStackTrace();
		}
		return itemRevisionId;
	}
	
	public void replaceItemIdInDatasetOrFormName(TCComponent currComp, String oldItemId, String newItemId)
	{
		ServiceData sd = dmService_.getProperties(new TCComponent[] {currComp}, new String[] {"object_name"});
		ModelObject datasetOrFormModelObject = sd.getPlainObject(0);
		try 
		{
			Property prop = datasetOrFormModelObject.getPropertyObject("object_name");
			String datasetName = prop.getStringValue();
			String newDatasetName = datasetName.replaceAll(oldItemId, newItemId);
			
			HashMap<String, VecStruct> propertyMap = new HashMap<String, VecStruct>();
			DataManagementService.VecStruct datasetOrFormNameProperty = new DataManagementService.VecStruct();
			datasetOrFormNameProperty.stringVec = new String[] { newDatasetName };
			propertyMap.put("object_name", datasetOrFormNameProperty);
			dmService_.setProperties(new TCComponent[] { currComp }, propertyMap);
			System.out.println("Component details updated successfully");
		} 
		catch (NotLoadedException e) 
		{
			e.printStackTrace();
		}
	}
	
	public String getItemName(TCComponent currComp)
	{
		ServiceData sd = dmService_.getProperties(new TCComponent[] {currComp}, new String[] {"object_name"});
		ModelObject itemModelObject = sd.getPlainObject(0);
		Property prop = null;
		try 
		{
			prop = itemModelObject.getPropertyObject("object_name");
		} 
		catch (NotLoadedException e) 
		{
			e.printStackTrace();
		}
		String currentItemName = prop.getStringValue();
		
		return currentItemName;
	}
	
	public ItemIdsAndInitialRevisionIds[] generateItemIds(int numberOfIds, String type)
            throws ServiceException
    {
		GenerateItemIdsAndInitialRevisionIdsProperties[] properties = new GenerateItemIdsAndInitialRevisionIdsProperties[1];
		GenerateItemIdsAndInitialRevisionIdsProperties property = new GenerateItemIdsAndInitialRevisionIdsProperties();

        property.count = numberOfIds;
        property.itemType = type;
        property.item = null;
        properties[0] = property;

        GenerateItemIdsAndInitialRevisionIdsResponse response = dmService_.generateItemIdsAndInitialRevisionIds(properties);


        if (response.serviceData.sizeOfPartialErrors() > 0)
        {
            throw new ServiceException( "DataManagementService.generateItemIdsAndInitialRevisionIds returned a partial error.");
        }

        BigInteger bIkey = new BigInteger("0");
        
        @SuppressWarnings("unchecked")
        Map<BigInteger,ItemIdsAndInitialRevisionIds[]> allNewIds = response.outputItemIdsAndInitialRevisionIds;
        ItemIdsAndInitialRevisionIds[] newIds = allNewIds.get(bIkey);

        return newIds;
    }
	
	public String[] getStringLOVForProperty(TCComponentItem object, String property)
	{
		String[] listOfValuesString = null;
		LOVAttachmentsInput[] serviceInput = new LOVAttachmentsInput[1];
		LOVAttachmentsInput input = new LOVAttachmentsInput();
		input.objects = new TCComponent[]{object};
		input.properties = new String [] {property};
		
		serviceInput[0] = input;
		
		try 
		{
			LOVAttachmentsResponse response = lovService_.getLOVAttachments(serviceInput);
			if (response.serviceData.sizeOfPartialErrors() > 0)
		    {
		        throw new ServiceException( "lovService_.getLOVAttachments returned a partial error.");
		    }
			
			@SuppressWarnings("unchecked")
			Map<TCComponentItem, LOVAttachment[]> lov = response.lovAttachments;
			
			TCComponentListOfValues lovValues = (lov.get(object))[0].lov;
			if(lovValues != null)
			{
				listOfValuesString = lovValues.getListOfValues().getLOVDisplayValues();
			}
			
		} 
		catch (ServiceException e) 
		{
			e.printStackTrace();
		} 
		catch (TCException e) 
		{
			e.printStackTrace();
		} 
		
		return listOfValuesString;
	}
}
