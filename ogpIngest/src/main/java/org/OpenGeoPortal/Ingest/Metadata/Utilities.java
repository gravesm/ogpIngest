package org.OpenGeoPortal.Ingest.Metadata;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.OpenGeoPortal.Layer.AccessLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

public class Utilities {
	final static Logger logger = LoggerFactory.getLogger(Utilities.class.getName());

	public static String getLayerNameFromFileName(String fileName, String layerNamePrefix){
		String tempName = fileName;
		if (tempName.endsWith(".shp.xml"))
	     	    tempName = tempName.substring(0, tempName.length() - ".shp.xml".length());
		tempName = layerNamePrefix + tempName;
		tempName = tempName.toUpperCase();
		return tempName;
	}
	
	public static String getLayerNameFromFileName(String fileName){
		return getLayerNameFromFileName(fileName, "");
	}
	
	// run the passed command and return the results from it
	public static String runCommand(String command)
	{
	    ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
	    processBuilder.redirectErrorStream(true);
	    Process process = null;
	    try
	    {
	    	process = processBuilder.start();
	    }
	    catch (Exception ex)
	    {
		System.out.println("cound not create process for command  " + command + ".  error = " + ex.toString());
		return "Error: could not create process for command  " + command + ".  error = " + ex.toString();
	    }
	    try
	    {
		process.waitFor();
	    }
	    catch (InterruptedException e)
	    {
	    }
	    StringBuilder processLog = new StringBuilder();
	    BufferedReader processOutputReader = null;
	    try
	    {
	    	int character;
	    	processOutputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	    	while ((character = processOutputReader.read()) != -1)
	        {
	    	    processLog.append((char) character);
	    	}
	   }
	   catch (Exception ex)
	   {
	 	return "Error: could not read response for command " + command + " partial response = " + processLog.toString();
	   }
		
	   return processLog.toString();
	}
	
	public static DocumentFragment createContactFragment(Document metadataDocument, ContactInfo contactInfo){
		DocumentFragment contactFragment = metadataDocument.createDocumentFragment();
		 
		Node cntInfoNode = metadataDocument.createElement("cntinfo");
		contactFragment.appendChild(cntInfoNode);
		
		Node cntOrgNode = metadataDocument.createElement("cntorg");
		cntOrgNode.setTextContent(contactInfo.getContactOrg());
		
		Node cntPerNode = metadataDocument.createElement("cntper");
		cntPerNode.setTextContent(contactInfo.getContactPerson());
		
		Node cntOrgPNode = metadataDocument.createElement("cntorgp");
		cntOrgPNode.appendChild(cntOrgNode);
		cntOrgPNode.appendChild(cntPerNode);

		cntInfoNode.appendChild(cntOrgPNode);
		
		Node cntPosNode = metadataDocument.createElement("cntpos");
		cntPosNode.setTextContent(contactInfo.getContactPosition());
		
		cntInfoNode.appendChild(cntPosNode);

		Node cntaddrNode = metadataDocument.createElement("cntaddr");

		cntInfoNode.appendChild(cntaddrNode);

		Node addrtypeNode = metadataDocument.createElement("addrtype");
		addrtypeNode.setTextContent("mailing and physical address");
		cntaddrNode.appendChild(addrtypeNode);
		
		Node addressNode = metadataDocument.createElement("address");
		addressNode.setTextContent(contactInfo.getAddress());
		cntaddrNode.appendChild(addressNode);

		Node cityNode = metadataDocument.createElement("city");
		cityNode.setTextContent(contactInfo.getCity());
		cntaddrNode.appendChild(cityNode);
		
		Node stateNode = metadataDocument.createElement("state");
		stateNode.setTextContent(contactInfo.getState());
		cntaddrNode.appendChild(stateNode);

		Node postalNode = metadataDocument.createElement("postal");
		postalNode.setTextContent(contactInfo.getZip());
		cntaddrNode.appendChild(postalNode);
		
		Node countryNode = metadataDocument.createElement("country");
		countryNode.setTextContent(contactInfo.getCountry());
		cntaddrNode.appendChild(countryNode);

		Node cntemailNode = metadataDocument.createElement("cntemail");
		cntemailNode.setTextContent(contactInfo.getContactEmail());
		
		cntInfoNode.appendChild(cntemailNode);
	
		return contactFragment;
}

	public static Document setAccessInfo(Document metadataDocument, AccessLevel access, String institution, String accessConstraintsText, String constraintsText){
		return setAccessInfo(metadataDocument, access.toString(), institution,  accessConstraintsText, constraintsText);
	}
	
public static Document setAccessInfo(Document metadataDocument, String access, String institution, String accessConstraintsText, String constraintsText){
	String restrictedText = "Restricted Access Online: Access granted to Licensee only. Available only to " + institution.toUpperCase() + " affiliates.";
	String publicText = "Unrestricted Access Online.";

	accessConstraintsText = (accessConstraintsText != null) ?
		accessConstraintsText.trim() : "";

	if (access.equalsIgnoreCase("restricted")) {
		accessConstraintsText = restrictedText + " " + accessConstraintsText;
	} else {
		accessConstraintsText = publicText + " " + accessConstraintsText;
	}

	accessConstraintsText = accessConstraintsText.trim();

	Node accessNode = metadataDocument.getElementsByTagName("accconst").item(0);

	if (accessNode == null) {
		Element accessElement = metadataDocument.createElement("accconst");

		accessElement.appendChild(metadataDocument.createTextNode(
			accessConstraintsText));

		NodeList keywords = metadataDocument.getElementsByTagName("keywords");
		Node keywordsNextSib = keywords.item(keywords.getLength() - 1).getNextSibling();
		keywordsNextSib.getParentNode().insertBefore(accessElement, keywordsNextSib);

	} else {
		String accessText = accessNode.getTextContent().trim();
		String newAccessText;

		if (accessText.isEmpty() || accessText.equalsIgnoreCase("none")) {
			newAccessText = accessConstraintsText;
		} else {
			newAccessText = accessConstraintsText + " " + accessText;
		}

		accessNode.setTextContent(newAccessText.trim());
	}

	constraintsText = (constraintsText != null) ? constraintsText.trim() : "";

	Node useNode = metadataDocument.getElementsByTagName("useconst").item(0);

	if (useNode == null) {
		Element useElement = metadataDocument.createElement("useconst");

		useElement.appendChild(metadataDocument.createTextNode(constraintsText));

		accessNode = metadataDocument.getElementsByTagName("accconst").item(0);
		Node accessNextSib = accessNode.getNextSibling();
		accessNextSib.getParentNode().insertBefore(useElement, accessNextSib);
	} else {
		String useText = useNode.getTextContent().trim();
		String newUseText;

		if (useText.isEmpty() || useText.equalsIgnoreCase("none")) {
			newUseText = constraintsText;
		} else {
			newUseText = constraintsText + " " + useText;
		}

		useNode.setTextContent(newUseText.trim());
	}

	return metadataDocument;
}

public static Document handleFtname(Document metadataDocument, String filename){
	//check to see if ftname exists
		Node ftNameNode = metadataDocument.getElementsByTagName("ftname").item(0);
		String ftName = ftNameNode.getTextContent().trim();
		if (ftName.toUpperCase().startsWith("SDE_DATA.")){
			ftName = ftName.substring(9);
		}
		ftNameNode.setTextContent(ftName.toUpperCase());
		return metadataDocument;
}

/**
 * This ensures that contact info exists for both the distribution and metadata
 * reference sections. No contact information is replaced. Local contact info
 * is always added to any exisiting contacts in the distribution section.
 *
 * @param metadataDocument source document
 * @param contactInfo local contact info
 * @return modified source document
 */
public static Document setContactInfo(Document metadataDocument, ContactInfo contactInfo){

	NodeList metaContact = metadataDocument.getElementsByTagName("metc");

	if (metaContact.getLength() == 0) {
		
		NodeList metaInfoList = metadataDocument.getElementsByTagName("metainfo");
		
		if (metaInfoList.getLength() > 0){

			Node metainfoNode = metaInfoList.item(0);
			Node metcNode = metadataDocument.createElement("metc");

			metainfoNode.appendChild(metcNode);
			metcNode.appendChild(createContactFragment(metadataDocument, contactInfo));
		} else {
			//probably can't recover from this
		}
	}

	Node rootNode = metadataDocument.getDocumentElement();
	Node distinfoNode = metadataDocument.createElement("distinfo");

	rootNode.appendChild(distinfoNode);

	Node distribNode = metadataDocument.createElement("distrib");

	distinfoNode.appendChild(distribNode);
	distribNode.appendChild(createContactFragment(metadataDocument, contactInfo));


	return metadataDocument;
}

public static Document handleOnlink (Document metadataDocument, String onlinkText){
		// idinfo/citation/citeinfo/onlink
		// Server=arrowsmith.mit.edu; Service=5150; Database=oracle
		/*
		'This section should cycle through onlink tags, find and keep tags with string http, and delete others
    'After that it should find links, if they exist, from SDS_DATA_LIST in Online_Linkage and insert them
    'Then insert the standard arrowsmith link
		*/
		//make sure we get the right onlink tag
		List<Node> onlinkNodeList = new ArrayList<Node>();
		Node onlinkParentNode = null;
		Node idinfoNode = metadataDocument.getElementsByTagName("idinfo").item(0);
	NodeList idinfoChildNodes = idinfoNode.getChildNodes();
	for (int j = 0; j < idinfoChildNodes.getLength(); j++){
		Node currentNode = idinfoChildNodes.item(j);
		if (currentNode.getNodeName().equals("citation")){
			NodeList citationNodes = currentNode.getChildNodes();
			for (int k = 0; k < citationNodes.getLength(); k++){
				Node currentCitationNode = citationNodes.item(k);
				if (currentCitationNode.getNodeName().equals("citeinfo")){
					onlinkParentNode = currentCitationNode;
					NodeList citeinfoNodes = currentCitationNode.getChildNodes();
					for (int l = 0; l < citeinfoNodes.getLength(); l++){
						Node currentCiteinfoNode = citeinfoNodes.item(l);
						if (currentCiteinfoNode.getNodeName().equals("onlink")){
							onlinkNodeList.add(currentCiteinfoNode);
						}
					}

				}

			}
		}
	}
	if (onlinkNodeList.isEmpty()){
		//add an onlink node
		//add server info
		logger.info("nodelist is empty");
		Node newOnlinkNode = metadataDocument.createElement("onlink");
		newOnlinkNode.setTextContent(onlinkText);
		onlinkParentNode.appendChild(newOnlinkNode);
	} else {
		Boolean onlinkPopulated = false;
		for (Node onlinkNode : onlinkNodeList){
			logger.info("onlink content: " + onlinkNode.getTextContent());
			if (onlinkNode.getTextContent().equals(onlinkText)){
				onlinkPopulated = true;
			} else if (!onlinkNode.getTextContent().contains("http")){
				onlinkNode.getParentNode().removeChild(onlinkNode);
			} 
		}
		
		if (!onlinkPopulated){
			//add server info
			logger.info("onlink not populated");
			Node newOnlinkNode = metadataDocument.createElement("onlink");
			newOnlinkNode.setTextContent(onlinkText);
			onlinkParentNode.appendChild(newOnlinkNode);
		}
	}
	return metadataDocument;
}

	public static Document setStylesheet(Document doc) {
		Element root = doc.getDocumentElement();
		ProcessingInstruction stylesheet = doc.createProcessingInstruction(
			"xml-stylesheet", "type=\"text/xsl\" href=\"fgdc.xml\"");
		root.getParentNode().insertBefore(stylesheet, root);

		return doc;
	}
	
}
