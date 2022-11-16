/*
 * Copyright 2016 SEARCH-The National Consortium for Justice Information and Statistics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.search.nibrs.xmlfile.importer;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.search.nibrs.common.NIBRSError;
import org.search.nibrs.common.ParsedObject;
import org.search.nibrs.common.ReportSource;
import org.search.nibrs.importer.AbstractIncidentBuilder;
import org.search.nibrs.importer.ReportListener;
import org.search.nibrs.model.AbstractReport;
import org.search.nibrs.model.AbstractSegment;
import org.search.nibrs.model.ArresteeSegment;
import org.search.nibrs.model.GroupAIncidentReport;
import org.search.nibrs.model.GroupBArrestReport;
import org.search.nibrs.model.NIBRSAge;
import org.search.nibrs.model.OffenderSegment;
import org.search.nibrs.model.OffenseSegment;
import org.search.nibrs.model.PropertySegment;
import org.search.nibrs.model.VictimSegment;
import org.search.nibrs.model.ZeroReport;
import org.search.nibrs.model.codes.AutomaticWeaponIndicatorCode;
import org.search.nibrs.model.codes.BiasMotivationCode;
import org.search.nibrs.model.codes.FederalJudicialDistrictCode;
import org.search.nibrs.model.codes.NIBRSErrorCode;
import org.search.nibrs.model.codes.PropertyDescriptionCode;
import org.search.nibrs.model.codes.RelationshipOfVictimToOffenderCode;
import org.search.nibrs.model.codes.TypeOfPropertyLossCode;
import org.search.nibrs.xml.XmlUtils;
import org.search.nibrs.xmlfile.NibrsXmlFileProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Builder class that constructs incidents from a stream of NIBRS report data.
 * Incidents are broadcast to listeners as events; this keeps the class as
 * memory-unintensive as possible (NIBRS report streams can be rather large).
 * <br/>
 * At some point, if other report elements than Incidents are desired, this will
 * need to be modified. Currently, it only broadcasts Incident "add" records.
 * 
 */
@Component
@Scope(scopeName = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class XmlIncidentBuilder extends AbstractIncidentBuilder{
	private static final Log log = LogFactory.getLog(XmlIncidentBuilder.class);
	
	NibrsXmlFileProperties nibrsXmlFileProperties;

	private DocumentBuilder documentBuilder; 
	private List<String> automaticWeaponCodes = Arrays.asList("11A", "12A", "13A", "14A", "15A");

	public XmlIncidentBuilder() throws ParserConfigurationException {
		super();
		setDateFormat(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		initDocumentBuilder();
	}

	@Autowired
	public XmlIncidentBuilder(NibrsXmlFileProperties nibrsXmlFileProperties) throws ParserConfigurationException {
		this();
		this.nibrsXmlFileProperties = nibrsXmlFileProperties;
	}
	
	private void initDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

		documentBuilderFactory.setValidating(false);
		documentBuilderFactory.setIgnoringComments(false);
		documentBuilderFactory.setIgnoringElementContentWhitespace(true);
		documentBuilderFactory.setNamespaceAware(true);

		documentBuilder = documentBuilderFactory.newDocumentBuilder();
		documentBuilder.setEntityResolver(new NullResolver());
	}

	public void addIncidentListener(ReportListener listener) {
		getListeners().add(listener);
	}

	public void removeIncidentListener(ReportListener listener) {
		getListeners().remove(listener);
	}

	/**
	 * Read NIBRS incidents in the XML format from the input stream.
	 * @param reader the source of the data
	 * @throws Exception 
	 */
	public void buildIncidents(InputStream inputStream, String readerLocationName) {

		AbstractReport currentReport = null;
		
		log.info("Processing NIBRS XML file");
		
		List<NIBRSError> errorList = new ArrayList<NIBRSError>();
		
		Document document;
		
		try {
			document = documentBuilder.parse(inputStream);
			NodeList reportElements = (NodeList) XmlUtils.xPathNodeListSearch(document, "nibrs:Submission/nibrs:Report");
			
			for(int i=0; i < reportElements.getLength(); i++){
				ReportSource reportSource = new ReportSource();
				reportSource.setSourceLocation(String.valueOf(i+1));
				reportSource.setSourceName(readerLocationName);
				
				Element reportNode = (Element)reportElements.item(i);
				
				ReportBaseData reportBaseData = new ReportBaseData();
				List<NIBRSError> reportBaseDataErrors = reportBaseData.setData(reportSource, reportNode);
				errorList.addAll(reportBaseDataErrors);

				if (reportBaseDataErrors.isEmpty()){
					currentReport = buildReport(errorList, reportNode, readerLocationName, reportBaseData);
					errorList = new ArrayList<NIBRSError>();
					handleNewReport(currentReport, errorList);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
		
		
		log.info("finished processing file");
		log.info("Encountered " + getLogListener().errorCount + " error(s).");
		log.info("Created " + getLogListener().reportCount + " incident(s).");

	}

	AbstractReport buildReport(List<NIBRSError> errorList, Element reportElement, String readerLocationName, ReportBaseData reportBaseData) throws Exception {
		int errorListSize = errorList.size();
		AbstractReport ret = null;
		
		String nibrsReportCategoryCode = XmlUtils.xPathStringSearch(reportElement, "nibrs:ReportHeader/nibrs:NIBRSReportCategoryCode"); 
		
		switch (nibrsReportCategoryCode){
		case "GROUP A INCIDENT REPORT":
			ret = buildGroupAIncidentReport(reportBaseData, errorList); 
			break; 
		case "GROUP B ARREST REPORT": 
			ret = buildGroupBArrestReport(reportBaseData, errorList);
			break; 
		case "ZERO REPORT": 
			ret = buildZeroReport(reportBaseData, errorList); 
			break;
		}
		
		if (errorList.size() > errorListSize) {
			ret.setHasUpstreamErrors(true);
		}
		ret.setSource(reportBaseData.getReportSource());
		return ret;
	}

	private AbstractReport buildZeroReport(ReportBaseData reportBaseData, List<NIBRSError> errorList) throws Exception {
		ZeroReport ret = new ZeroReport();
		ret.setOri(reportBaseData.getOri());
		ret.setReportActionType(reportBaseData.getActionType());
		ret.setIncidentNumber(reportBaseData.getIncidentNumber());
		
		List<NIBRSError> newErrorList = getSubmissionYearMonth(reportBaseData, ret, NIBRSErrorCode._001);
		
		//TODO find out Zero Report Year and Zero report Month xPath.
			
		for (NIBRSError e : newErrorList) {
			e.setReport(ret);
		}
		
		errorList.addAll(newErrorList);
		
		return ret;
	}

	private List<NIBRSError> getSubmissionYearMonth(ReportBaseData reportBaseData, 
			AbstractReport ret, NIBRSErrorCode nibrsErrorCode) {
		List<NIBRSError> errorList = new ArrayList<>();
		String submissionDateString = ""; 
		
		try {
			
			submissionDateString = XmlUtils.xPathStringSearch(reportBaseData.getReportElement(), "nibrs:ReportHeader/nibrs:ReportDate/nc:YearMonthDate");
			if (StringUtils.isNotBlank(submissionDateString) && submissionDateString.length() == 7){
				YearMonth submissionDate = YearMonth.parse(submissionDateString);
				ret.setYearOfTape(submissionDate.getYear());
				ret.setMonthOfTape(submissionDate.getMonth().getValue());
			}
		}
		catch (Exception e){
			log.info(e);
			NIBRSError nibrsError = new NIBRSError();
			nibrsError.setContext(reportBaseData.getReportSource());
			nibrsError.setReportUniqueIdentifier(reportBaseData.getIncidentNumber());
			nibrsError.setNIBRSErrorCode(nibrsErrorCode);
			nibrsError.setValue(submissionDateString);
			nibrsError.setSegmentType('0');
			errorList.add(nibrsError);
			log.debug("Error in DateTimeParse conversion: position=" + reportBaseData.getReportSource()
				+ ", xPath = 'nibrs:ReportHeader/nibrs:ReportDate/nc:YearMonthDate'"
				+ ", value=" + StringUtils.trimToEmpty(submissionDateString));

		}
		
		return errorList;
	}

	private AbstractReport buildGroupBArrestReport(ReportBaseData reportBaseData, List<NIBRSError> errorList) {
		List<NIBRSError> newErrorList = new ArrayList<>();
		GroupBArrestReport ret = new GroupBArrestReport();
		
		Element reportElement = reportBaseData.getReportElement();
		ret.setOri(reportBaseData.getOri());
		ret.setReportActionType(reportBaseData.getActionType());
		
		String cityIndicator = XmlUtils.xPathStringSearch(reportElement, 
				"nibrs:ReportHeader/nibrs:ReportingAgency/cjis:OrganizationAugmentation/cjis:DirectReportingCityIdentification/nc:IdentificationID");
		ret.setCityIndicator(cityIndicator);

		String judicialDistrictCode = XmlUtils.xPathStringSearch(reportElement, "nc:Location/nc:LocationLocale/ cjis:JudicialDistrictCode");
		if (StringUtils.isNotBlank(judicialDistrictCode)) {
			ret.setFederalJucicialDistrictCode(FederalJudicialDistrictCode.valueOfIepdCode(judicialDistrictCode).code);
		}

		newErrorList.addAll(getSubmissionYearMonth(reportBaseData,  ret, NIBRSErrorCode._701));

		buildGroupBArresteeSegments(reportBaseData, newErrorList, ret, reportElement);
		errorList.addAll(newErrorList);
	
		return ret;
	}

	private void buildGroupBArresteeSegments(ReportBaseData reportBaseData, List<NIBRSError> newErrorList, GroupBArrestReport ret,
			Element reportElement) {
		
		NodeList arresteeElements = (NodeList) XmlUtils.xPathNodeListSearch(reportElement, "j:Arrestee");
		for(int i=0; i < arresteeElements.getLength() ; i++){
			Element arresteeElement = (Element) arresteeElements.item(i);
			ArresteeSegment newArrestee = new ArresteeSegment(ArresteeSegment.GROUP_B_ARRESTEE_SEGMENT_TYPE_IDENTIFIER);
			
			ReportSource reportSource = new ReportSource(reportBaseData.getReportSource());;
			String arresteeId = XmlUtils.xPathStringSearch(arresteeElement, "@s:id");
			reportSource.setSourceLocation(arresteeId);
			
			ParsedObject<Integer> sequenceNumber = newArrestee.getArresteeSequenceNumber();
			sequenceNumber.setMissing(false);
			sequenceNumber.setInvalid(false);
			String sequenceNumberString = XmlUtils.xPathStringSearch(arresteeElement, "j:ArrestSequenceID");
			if (sequenceNumberString == null) {
				sequenceNumber.setMissing(true);
				sequenceNumber.setValue(null);
			} else {
				getIntegerValue(newErrorList, sequenceNumber, sequenceNumberString, NIBRSErrorCode._701, "40", reportBaseData );
			}
			
			newArrestee.setArresteeSequenceNumber(sequenceNumber);
			
			Node arrestNode = XmlUtils.xPathNodeSearch(reportElement, "j:Arrest[@s:id = ../j:ArrestSubjectAssociation[j:Subject/@s:ref='"+ arresteeId +  "']/nc:Activity/@s:ref]");
			
			if (arrestNode != null){
				newArrestee.setArrestTransactionNumber(XmlUtils.xPathStringSearch(arrestNode, "nc:ActivityIdentification/nc:IdentificationID"));
				
				ParsedObject<LocalDate> arrestDate = newArrestee.getArrestDate();
				arrestDate.setMissing(false);
				arrestDate.setInvalid(false);
				String arrestDateString = XmlUtils.xPathStringSearch(arrestNode, "nc:ActivityDate/nc:Date");
				if (arrestDateString == null) {
					arrestDate.setMissing(true);
					arrestDate.setValue(null);
				} else {
					try {
						LocalDate d = LocalDate.parse(arrestDateString, getDateFormat());
						arrestDate.setValue(d);
					} catch (Exception pe) {
						NIBRSError e = new NIBRSError();
						reportSource.setSourceLocation((String)XmlUtils.xPathStringSearch(reportElement, "j:Arrest/@s:id"));
						e.setContext(reportBaseData.getReportSource());
						e.setReportUniqueIdentifier(reportBaseData.getIncidentNumber());
						e.setSegmentType(reportBaseData.getSegmentType());
						e.setValue(arrestDateString);
						e.setNIBRSErrorCode(NIBRSErrorCode._705);
						e.setDataElementIdentifier("42");
						newErrorList.add(e);
						arrestDate.setInvalid(true);
						arrestDate.setValidationError(e);
					}
				}
				newArrestee.setArrestDate(arrestDate);
				newArrestee.setUcrArrestOffenseCode(XmlUtils.xPathStringSearch(arrestNode, "j:ArrestCharge/nibrs:ChargeUCRCode"));
				newArrestee.setTypeOfArrest(XmlUtils.xPathStringSearch(arrestNode, "j:ArrestCategoryCode"));
			}
			
			newArrestee.setMultipleArresteeSegmentsIndicator(XmlUtils.xPathStringSearch(arresteeElement, "j:ArrestSubjectCountCode"));
			
			NodeList arresteeArmedWithElements = (NodeList) XmlUtils.xPathNodeListSearch(arresteeElement, "j:ArresteeArmedWithCode");
			
			for(int j=0; j < arresteeArmedWithElements.getLength() && j < 2; j++){
				Element arresteeArmedWithElement = (Element)arresteeArmedWithElements.item(j);
				String arresteeArmedWithCode = arresteeArmedWithElement.getTextContent();
				setArmedWithAndAutomaticIndicator(newArrestee, j, arresteeArmedWithCode);
			}
			
			String personRef = XmlUtils.xPathStringSearch(arresteeElement, "nc:RoleOfPerson/@s:ref");
			Node personNode = XmlUtils.xPathNodeSearch(reportElement, "nc:Person[@s:id ='" + personRef + "']");
			
			newArrestee.setAge(parseAgeNode(personNode, newArrestee));
			newArrestee.setSex(XmlUtils.xPathStringSearch(personNode, "j:PersonSexCode"));
			newArrestee.setRace(XmlUtils.xPathStringSearch(personNode, nibrsXmlFileProperties.getRaceCodeXPath()));
			newArrestee.setEthnicity(XmlUtils.xPathStringSearch(personNode, "j:PersonEthnicityCode"));
			newArrestee.setResidentStatus(XmlUtils.xPathStringSearch(personNode, "j:PersonResidentCode"));
			
			newArrestee.setDispositionOfArresteeUnder18(XmlUtils.xPathStringSearch(arresteeElement, "j:ArresteeJuvenileDispositionCode"));

			ret.addArrestee(newArrestee);
		}	
		
	}

	private void setArmedWithAndAutomaticIndicator(ArresteeSegment arrestee, int i, String arresteeArmedWithCode) {
		arrestee.setArresteeArmedWith(i , StringUtils.removeEnd(arresteeArmedWithCode, "A"));
		arrestee.setAutomaticWeaponIndicator(i, getAutomaticWeaponIndicator(arresteeArmedWithCode));
	}

	private NIBRSAge parseAgeNode(Node personNode, AbstractSegment segmentContext) {
		
		NIBRSAge ret = null;

		String ageString = XmlUtils.xPathStringSearch(personNode, "nc:PersonAgeMeasure/nc:MeasureIntegerValue|nc:PersonAgeMeasure/nc:MeasureValueText");
		ageString = StringUtils.leftPad(ageString, 2); 
		
		if (StringUtils.isBlank(ageString)){
			String ageMinString = XmlUtils.xPathStringSearch(personNode, "nc:PersonAgeMeasure/nc:MeasureIntegerRange/nc:RangeMinimumIntegerValue"); 
			String ageMaxString = XmlUtils.xPathStringSearch(personNode, "nc:PersonAgeMeasure/nc:MeasureIntegerRange/nc:RangeMaximumIntegerValue");
			ageString = StringUtils.join(StringUtils.leftPad(ageMinString, 2), StringUtils.leftPad(ageMaxString, 2)); 
		}
		
		if (!StringUtils.isBlank(ageString)) {

			ret = new NIBRSAge();

			switch (ageString) {
			case "NEONATAL":
				ret = NIBRSAge.getNeonateAge();
				break;
			case "NEWBORN":
				ret = NIBRSAge.getNewbornAge();
				break;
			case "BABY":
				ret = NIBRSAge.getBabyAge();
				break;
			case "UNKNOWN":
				ret = NIBRSAge.getUnknownAge();
				break;
			default:
				log.info(ageString);
				ret = NIBRSAgeBuilder.buildAgeFromRawString(ageString, segmentContext);
			}

		}
		
		return ret;
	}
	
	private void getIntegerValue(List<NIBRSError> errorList, ParsedObject<Integer> parsedObject,
			String stringValue, NIBRSErrorCode nibrsErrorCode,  String dataElementId, ReportBaseData reportBaseData) {
		try {
			Integer sequenceNumberI = Integer.parseInt(stringValue);
			parsedObject.setValue(sequenceNumberI);
		} catch (NumberFormatException nfe) {
			NIBRSError e = new NIBRSError();
			e.setContext(reportBaseData.getReportSource());
			e.setReportUniqueIdentifier(reportBaseData.getIncidentNumber());
			e.setSegmentType(reportBaseData.getSegmentType());
			e.setValue(stringValue);
			e.setNIBRSErrorCode(nibrsErrorCode);
			e.setDataElementIdentifier(dataElementId);
			errorList.add(e);
			parsedObject.setInvalid(true);
			parsedObject.setValidationError(e);
		}
	}

	private AbstractReport buildGroupAIncidentReport(ReportBaseData reportBaseData, List<NIBRSError> errorList) {
		List<NIBRSError> newErrorList = new ArrayList<>();
		GroupAIncidentReport newIncident = new GroupAIncidentReport();
		newIncident.setSource(reportBaseData.getReportSource());
		
		newIncident.setIncidentNumber(reportBaseData.getIncidentNumber());
		newIncident.setOri(reportBaseData.getOri());
		newIncident.setReportActionType(reportBaseData.getActionType());
		Element reportElement = reportBaseData.getReportElement();
		newErrorList.addAll(getSubmissionYearMonth(reportBaseData,  newIncident, NIBRSErrorCode._101));

		String cityIndicator = XmlUtils.xPathStringSearch(reportElement, 
				"nibrs:ReportHeader/nibrs:ReportingAgency/cjis:OrganizationAugmentation/cjis:DirectReportingCityIdentification/nc:IdentificationID");
		newIncident.setCityIndicator(cityIndicator);
		
		String judicialDistrictCode = XmlUtils.xPathStringSearch(reportElement, "nc:Location/nc:LocationLocale/ cjis:JudicialDistrictCode");
		if (StringUtils.isNotBlank(judicialDistrictCode)) {
			newIncident.setFederalJucicialDistrictCode(FederalJudicialDistrictCode.valueOfIepdCode(judicialDistrictCode).code);
		}
		
		ParsedObject<LocalDate> incidentDate = newIncident.getIncidentDate();
		incidentDate.setMissing(false);
		incidentDate.setInvalid(false);
		String incidentDateString = XmlUtils.xPathStringSearch(reportElement, "nc:Incident/nc:ActivityDate/nc:Date");
		String incidentDatetimeString = XmlUtils.xPathStringSearch(reportElement, "nc:Incident/nc:ActivityDate/nc:DateTime");
		if (StringUtils.isBlank(incidentDateString) && StringUtils.isBlank(incidentDatetimeString)) {
			incidentDate.setMissing(true);
			incidentDate.setValue(null);
		} else {
			try {
				
				if (StringUtils.isNotBlank(incidentDateString)){
					LocalDate d = LocalDate.parse(incidentDateString, getDateFormat());
					incidentDate.setValue(d);
				}
				else {
					LocalDateTime d = LocalDateTime.parse(StringUtils.substring(incidentDatetimeString, 0, 19), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
					incidentDate.setValue(d.toLocalDate());
				}
			} catch (Exception pe) {
				NIBRSError e = new NIBRSError();
				e.setContext(reportBaseData.getReportSource());
				e.setReportUniqueIdentifier(reportBaseData.getIncidentNumber());
				e.setSegmentType(reportBaseData.getSegmentType());
				e.setValue(incidentDateString);
				e.setNIBRSErrorCode(NIBRSErrorCode._105);
				e.setDataElementIdentifier("3");
				newErrorList.add(e);
				incidentDate.setInvalid(true);
				incidentDate.setValidationError(e);
			}
		}
		newIncident.setIncidentDate(incidentDate);
			
		String reportDateIncidator = XmlUtils.xPathStringSearch(reportElement, "nc:Incident/cjis:IncidentAugmentation/cjis:IncidentReportDateIndicator");
		
		newIncident.setReportDateIndicator(BooleanUtils.toString(BooleanUtils.toBoolean(reportDateIncidator), "R", null));

		String hourString = null;
		if (StringUtils.isNotBlank(incidentDatetimeString) && incidentDatetimeString.length() > 11){
			hourString = StringUtils.substringBefore(StringUtils.substringAfter(incidentDatetimeString, "T"), ":"); 
		}
		
		ParsedObject<Integer> hour = newIncident.getIncidentHour();
		hour.setMissing(false);
		hour.setInvalid(false);
		if (StringUtils.isNotBlank(hourString)) {
			try {
				if (hourString.length() != 2){
					throw new NumberFormatException(); 
				}
				Integer hourI = new Integer(hourString);
				hour.setValue(hourI);
			} catch(NumberFormatException nfe) {
				NIBRSError e152 = new NIBRSError();
				e152.setContext(reportBaseData.getReportSource());
				e152.setReportUniqueIdentifier(reportBaseData.getIncidentNumber());
				e152.setSegmentType(reportBaseData.getSegmentType());
				e152.setValue(hourString);
				e152.setNIBRSErrorCode(NIBRSErrorCode._152);
				e152.setDataElementIdentifier("3");
				newErrorList.add(e152);
				
				NIBRSError e = new NIBRSError();
				e.setContext(reportBaseData.getReportSource());
				e.setReportUniqueIdentifier(reportBaseData.getIncidentNumber());
				e.setSegmentType(reportBaseData.getSegmentType());
				e.setValue(hourString);
				e.setNIBRSErrorCode(NIBRSErrorCode._104);
				e.setDataElementIdentifier("3");
				newErrorList.add(e);
				hour.setInvalid(true);
				hour.setValidationError(e);
			}
		} else {
			hour.setMissing(true);
		}
			
		newIncident.setExceptionalClearanceCode(XmlUtils.xPathStringSearch(reportElement, "nc:Incident/j:IncidentAugmentation/j:IncidentExceptionalClearanceCode"));
		
		ParsedObject<LocalDate> clearanceDate = newIncident.getExceptionalClearanceDate();
		clearanceDate.setMissing(false);
		clearanceDate.setInvalid(false);
		String clearanceDateString = XmlUtils.xPathStringSearch(reportElement, "nc:Incident/j:IncidentAugmentation/j:IncidentExceptionalClearanceDate/nc:Date");
		if (clearanceDateString == null) {
			clearanceDate.setMissing(true);
			clearanceDate.setValue(null);
		} else {
			try {
				LocalDate d = LocalDate.parse(clearanceDateString, getDateFormat());
				clearanceDate.setValue(d);
			} catch (Exception pe) {
				NIBRSError e = new NIBRSError();
				e.setContext(reportBaseData.getReportSource());
				e.setReportUniqueIdentifier(reportBaseData.getIncidentNumber());
				e.setSegmentType(reportBaseData.getSegmentType());
				e.setValue(clearanceDateString);
				e.setNIBRSErrorCode(NIBRSErrorCode._105);
				e.setDataElementIdentifier("5");
				newErrorList.add(e);
				clearanceDate.setInvalid(true);
				clearanceDate.setValidationError(e);
			}
		}
		newIncident.setExceptionalClearanceDate(clearanceDate);
		
		String cargoTheftYN = XmlUtils.xPathStringSearch(reportElement, "nc:Incident/cjis:IncidentAugmentation/j:OffenseCargoTheftIndicator");
		
		if (StringUtils.isNotBlank(cargoTheftYN)){
			String cargoTheft = BooleanUtils.toString(BooleanUtils.toBoolean(cargoTheftYN), "Y", "N", "N"); 
			newIncident.setCargoTheftIndicator(cargoTheft);
			newIncident.setIncludesCargoTheft(true);
		}
		
		for (NIBRSError e : newErrorList) {
			e.setReport(newIncident);
		}
		errorList.addAll(newErrorList);
		
		buildOffenseSegments(reportElement, newIncident, errorList);
		buildPropertySegments(reportElement, newIncident, errorList);
		buildVictimSegments(reportElement, newIncident, errorList);
		buildOffenderSegments(reportElement, newIncident, errorList);
		buildGroupAArresteeSegments(reportElement, newIncident, errorList);
		return newIncident;
	}

	private void buildGroupAArresteeSegments(Element reportElement, GroupAIncidentReport incident,
			List<NIBRSError> errorList) {
		char segmentType = '6';
		NodeList arresteeElements = (NodeList) XmlUtils.xPathNodeListSearch(reportElement, "j:Arrestee");
		for(int i=0; i < arresteeElements.getLength() ; i++){
			Element arresteeElement = (Element) arresteeElements.item(i);
			ArresteeSegment newArrestee = new ArresteeSegment(segmentType);
			
			ReportSource reportSource = new ReportSource(incident.getSource());
			String arresteeId = XmlUtils.xPathStringSearch(arresteeElement, "@s:id");
			reportSource.setSourceLocation(arresteeId);
			
			ParsedObject<Integer> sequenceNumber = newArrestee.getArresteeSequenceNumber();
			sequenceNumber.setMissing(false);
			sequenceNumber.setInvalid(false);
			String sequenceNumberString = XmlUtils.xPathStringSearch(arresteeElement, "j:ArrestSequenceID");
			if (sequenceNumberString == null) {
				sequenceNumber.setMissing(true);
				sequenceNumber.setValue(null);
			} else {
				try {
					Integer sequenceNumberI = Integer.parseInt(sequenceNumberString);
					sequenceNumber.setValue(sequenceNumberI);
				} catch (NumberFormatException nfe) {
					NIBRSError e = new NIBRSError();
					e.setContext(reportSource);
					e.setReportUniqueIdentifier(incident.getIncidentNumber());
					e.setSegmentType(segmentType);
					e.setValue(sequenceNumberString);
					e.setNIBRSErrorCode(NIBRSErrorCode._601);
					e.setDataElementIdentifier("40");
					errorList.add(e);
					sequenceNumber.setInvalid(true);
					sequenceNumber.setValidationError(e);
				}
			}
			
			newArrestee.setArresteeSequenceNumber(sequenceNumber);
			
			Node arrestNode = XmlUtils.xPathNodeSearch(reportElement, "j:Arrest[@s:id = ../j:ArrestSubjectAssociation[j:Subject/@s:ref='"+ arresteeId +  "']/nc:Activity/@s:ref]");
			
			if (arrestNode != null){
				newArrestee.setArrestTransactionNumber(XmlUtils.xPathStringSearch(arrestNode, "nc:ActivityIdentification/nc:IdentificationID"));
				
				ParsedObject<LocalDate> arrestDate = newArrestee.getArrestDate();
				arrestDate.setMissing(false);
				arrestDate.setInvalid(false);
				String arrestDateString = XmlUtils.xPathStringSearch(arrestNode, "nc:ActivityDate/nc:Date");
				if (arrestDateString == null) {
					arrestDate.setMissing(true);
					arrestDate.setValue(null);
				} else {
					try {
						LocalDate d = LocalDate.parse(arrestDateString, getDateFormat());
						arrestDate.setValue(d);
					} catch (Exception pe) {
						NIBRSError e = new NIBRSError();
						e.setContext(reportSource);
						e.setReportUniqueIdentifier(incident.getIncidentNumber());
						e.setSegmentType(segmentType);
						e.setValue(arrestDateString);
						e.setNIBRSErrorCode(NIBRSErrorCode._705);
						e.setDataElementIdentifier("42");
						errorList.add(e);
						arrestDate.setInvalid(true);
						arrestDate.setValidationError(e);
					}
				}
				newArrestee.setArrestDate(arrestDate);
				newArrestee.setUcrArrestOffenseCode(XmlUtils.xPathStringSearch(arrestNode, "j:ArrestCharge/nibrs:ChargeUCRCode"));
				newArrestee.setTypeOfArrest(XmlUtils.xPathStringSearch(arrestNode, "j:ArrestCategoryCode"));
			}
			
			newArrestee.setMultipleArresteeSegmentsIndicator(XmlUtils.xPathStringSearch(arresteeElement, "j:ArrestSubjectCountCode"));
			
			NodeList arresteeArmedWithElements = (NodeList) XmlUtils.xPathNodeListSearch(arresteeElement, "j:ArresteeArmedWithCode");
			
			for(int j=0; j < arresteeArmedWithElements.getLength() && j < 2; j++){
				Element arresteeArmedWithElement = (Element)arresteeArmedWithElements.item(j);
				String arresteeArmedWithCode = arresteeArmedWithElement.getTextContent();
				setArmedWithAndAutomaticIndicator(newArrestee, j, arresteeArmedWithCode);
			}
			
			String personRef = XmlUtils.xPathStringSearch(arresteeElement, "nc:RoleOfPerson/@s:ref");
			Node personNode = XmlUtils.xPathNodeSearch(reportElement, "nc:Person[@s:id ='" + personRef + "']");
			
			newArrestee.setAge(parseAgeNode(personNode, newArrestee));
			newArrestee.setSex(XmlUtils.xPathStringSearch(personNode, "j:PersonSexCode"));
			newArrestee.setRace(XmlUtils.xPathStringSearch(personNode, nibrsXmlFileProperties.getRaceCodeXPath()));
			newArrestee.setEthnicity(XmlUtils.xPathStringSearch(personNode, "j:PersonEthnicityCode"));
			newArrestee.setResidentStatus(XmlUtils.xPathStringSearch(personNode, "j:PersonResidentCode"));
			
			newArrestee.setDispositionOfArresteeUnder18(XmlUtils.xPathStringSearch(arresteeElement, "j:ArresteeJuvenileDispositionCode"));

			incident.addArrestee(newArrestee);
		}		
	}

	private void buildOffenderSegments(Element reportElement, GroupAIncidentReport incident,
			List<NIBRSError> errorList) {
		char segmentType = '5';
		NodeList offenderElements = (NodeList) XmlUtils.xPathNodeListSearch(reportElement, "j:Subject");
		for(int i=0; i < offenderElements.getLength(); i++){
			Element offenderElement = (Element) offenderElements.item(i);
			OffenderSegment newOffender = new OffenderSegment();
			
			ReportSource reportSource = new ReportSource(incident.getSource());
			String offenderId = XmlUtils.xPathStringSearch(offenderElement, "@s:id");
			reportSource.setSourceLocation(offenderId);
			
			ParsedObject<Integer> sequenceNumber = newOffender.getOffenderSequenceNumber();
			sequenceNumber.setMissing(false);
			sequenceNumber.setInvalid(false);
			String sequenceNumberString = XmlUtils.xPathStringSearch(offenderElement, "j:SubjectSequenceNumberText");
			if (sequenceNumberString == null) {
				sequenceNumber.setMissing(true);
				sequenceNumber.setValue(null);
			} else {
				try {
					Integer sequenceNumberI = Integer.parseInt(sequenceNumberString);
					sequenceNumber.setValue(sequenceNumberI);
				} catch (NumberFormatException nfe) {
					NIBRSError e = new NIBRSError();
					e.setContext(reportSource);
					e.setReportUniqueIdentifier(incident.getIncidentNumber());
					e.setSegmentType(segmentType);
					e.setValue(sequenceNumberString);
					e.setNIBRSErrorCode(NIBRSErrorCode._501);
					e.setDataElementIdentifier("36");
					errorList.add(e);
					sequenceNumber.setInvalid(true);
					sequenceNumber.setValidationError(e);
				}
			}
			
			String personRef = XmlUtils.xPathStringSearch(offenderElement, "nc:RoleOfPerson/@s:ref");
			Node personNode = XmlUtils.xPathNodeSearch(reportElement, "nc:Person[@s:id ='" + personRef + "']");
			
			newOffender.setAge(parseAgeNode(personNode, newOffender));
			newOffender.setSex(XmlUtils.xPathStringSearch(personNode, "j:PersonSexCode"));
			newOffender.setRace(XmlUtils.xPathStringSearch(personNode, nibrsXmlFileProperties.getRaceCodeXPath()));
			newOffender.setEthnicity(XmlUtils.xPathStringSearch(personNode, "j:PersonEthnicityCode"));
			
			incident.addOffender(newOffender);
		}
	}

	private void buildVictimSegments(Element reportElement, GroupAIncidentReport incident,
			List<NIBRSError> errorList) {
		char segmentType = '4';
		NodeList victimElements = (NodeList) XmlUtils.xPathNodeListSearch(reportElement, "j:Victim");
		for(int i=0; i < victimElements.getLength(); i++){
			Element victimElement = (Element) victimElements.item(i);
			VictimSegment newVictim = new VictimSegment();
			
			ReportSource reportSource = new ReportSource(incident.getSource());
			String victimId = XmlUtils.xPathStringSearch(victimElement, "@s:id");
			reportSource.setSourceLocation(victimId);

			Integer sequenceNumberI = null;
			ParsedObject<Integer> sequenceNumber = newVictim.getVictimSequenceNumber();
			sequenceNumber.setMissing(false);
			sequenceNumber.setInvalid(false);
			String sequenceNumberString = XmlUtils.xPathStringSearch(victimElement, "j:VictimSequenceNumberText");
			if (sequenceNumberString == null) {
				sequenceNumber.setMissing(true);
				sequenceNumber.setValue(null);
				NIBRSError e = new NIBRSError();
				e.setContext(reportSource);
				e.setReportUniqueIdentifier(incident.getIncidentNumber());
				e.setSegmentType(segmentType);
				e.setNIBRSErrorCode(NIBRSErrorCode._401);
				e.setDataElementIdentifier("23");
				errorList.add(e);
			} else {
				try {
					sequenceNumberI = Integer.parseInt(sequenceNumberString);
					sequenceNumber.setValue(sequenceNumberI);
				} catch (NumberFormatException nfe) {
					NIBRSError e = new NIBRSError();
					e.setContext(reportSource);
					e.setReportUniqueIdentifier(incident.getIncidentNumber());
					e.setSegmentType(segmentType);
					e.setValue(sequenceNumberString);
					e.setNIBRSErrorCode(NIBRSErrorCode._402);
					e.setDataElementIdentifier("23");
					errorList.add(e);
					sequenceNumber.setInvalid(true);
					sequenceNumber.setValidationError(e);
				}
			}

			NodeList offenseUcrCodeNodes = XmlUtils.xPathNodeListSearch(reportElement, "j:Offense[@s:id = ../j:OffenseVictimAssociation[j:Victim/@s:ref = '" 
						+ victimId + "']/j:Offense/@s:ref]/nibrs:OffenseUCRCode");
			for (int j = 0; j < offenseUcrCodeNodes.getLength() && j < VictimSegment.UCR_OFFENSE_CODE_CONNECTION_COUNT; j++) {
				Element offenseUcrCode = (Element) offenseUcrCodeNodes.item(j);
				newVictim.setUcrOffenseCodeConnection(j, offenseUcrCode.getTextContent());
			}
			
			NodeList subjectVictimAssociations = XmlUtils.xPathNodeListSearch(reportElement, "j:SubjectVictimAssociation[j:Victim/@s:ref = '"+ victimId + "']"); 
			for (int j = 0; j < subjectVictimAssociations.getLength() && j < VictimSegment.OFFENDER_NUMBER_RELATED_COUNT; j++){
				Element subjectVictimAssociation = ( Element ) subjectVictimAssociations.item(j); 
				String offenderRef = XmlUtils.xPathStringSearch(subjectVictimAssociation, "j:Subject/@s:ref");
				
				String offenderNumberRelatedString = 
						XmlUtils.xPathStringSearch(reportElement, "j:Subject[@s:id='"+ offenderRef + "']/j:SubjectSequenceNumberText");
				
				ParsedObject<Integer> offenderNumberRelated = newVictim.getOffenderNumberRelated(j);
				offenderNumberRelated.setInvalid(false);
				offenderNumberRelated.setMissing(false);
				if (offenderNumberRelatedString == null) {
					offenderNumberRelated.setMissing(true);
					offenderNumberRelated.setInvalid(false);
				} else {
					try {
						Integer offenderNumberRelatedValue = Integer.parseInt(offenderNumberRelatedString);
						offenderNumberRelated.setValue(offenderNumberRelatedValue);
					} catch (NumberFormatException nfe) {
						NIBRSError e = new NIBRSError();
						e.setContext(reportSource);
						e.setReportUniqueIdentifier( incident.getIncidentNumber() );
						e.setSegmentType(segmentType);
						e.setValue(offenderNumberRelatedString);
						e.setNIBRSErrorCode(NIBRSErrorCode._402);
						e.setWithinSegmentIdentifier(sequenceNumberI);
						e.setDataElementIdentifier("34");
						errorList.add(e);
						offenderNumberRelated.setMissing(false);
						offenderNumberRelated.setInvalid(true);
					}
				}

				newVictim.setOffenderNumberRelated(j, offenderNumberRelated);
				String victimOffenderRelationshipIepdCode = XmlUtils.xPathStringSearch(subjectVictimAssociation, "nibrs:VictimToSubjectRelationshipCode");
				String victimOffenderRelationshipNibrsCode = Optional
						.ofNullable(RelationshipOfVictimToOffenderCode.valueOfIepdCode(victimOffenderRelationshipIepdCode))
						.map(item->item.code).orElse(null);
						
				newVictim.setVictimOffenderRelationship(j, victimOffenderRelationshipNibrsCode);
			}
			

			newVictim.setTypeOfVictim(XmlUtils.xPathStringSearch(victimElement, "j:VictimCategoryCode"));
			
			String personRef = XmlUtils.xPathStringSearch(victimElement, "nc:RoleOfPerson/@s:ref");
			Node personNode = XmlUtils.xPathNodeSearch(reportElement, "nc:Person[@s:id ='" + personRef + "']");
			
			newVictim.setAge(parseAgeNode(personNode, newVictim));
			newVictim.setSex(XmlUtils.xPathStringSearch(personNode, "j:PersonSexCode"));
			newVictim.setRace(XmlUtils.xPathStringSearch(personNode, nibrsXmlFileProperties.getRaceCodeXPath()));
			newVictim.setEthnicity(XmlUtils.xPathStringSearch(personNode, "j:PersonEthnicityCode"));
			newVictim.setResidentStatus(XmlUtils.xPathStringSearch(personNode, "j:PersonResidentCode"));
			newVictim.setAggravatedAssaultHomicideCircumstances(0, XmlUtils.xPathStringSearch(victimElement, "j:VictimAggravatedAssaultHomicideFactorCode"));
			newVictim.setAdditionalJustifiableHomicideCircumstances(XmlUtils.xPathStringSearch(victimElement, "j:VictimJustifiableHomicideFactorCode"));

			NodeList victimInjuries = XmlUtils.xPathNodeListSearch(victimElement, "j:VictimInjury/j:InjuryCategoryCode");
			for (int j = 0; j < victimInjuries.getLength() && j < VictimSegment.TYPE_OF_INJURY_COUNT; j++) {
				Node injuryCategoryCode = victimInjuries.item(j);
				newVictim.setTypeOfInjury(j, injuryCategoryCode.getTextContent());
			}

			Node enforcementOfficialNode = XmlUtils.xPathNodeSearch(reportElement, "j:EnforcementOfficial[nc:RoleOfPerson/@s:ref = '" + personRef + "']"); 
			
			if (enforcementOfficialNode != null){
				newVictim.setTypeOfOfficerActivityCircumstance(XmlUtils.xPathStringSearch(enforcementOfficialNode, 
						"j:EnforcementOfficialActivityCategoryCode"));
				newVictim.setOfficerAssignmentType(XmlUtils.xPathStringSearch(enforcementOfficialNode, 
						"j:EnforcementOfficialAssignmentCategoryCode"));
				newVictim.setOfficerOtherJurisdictionORI(XmlUtils.xPathStringSearch(enforcementOfficialNode, 
						"j:EnforcementOfficialUnit/j:OrganizationAugmentation/j:OrganizationORIIdentification/nc:IdentificationID"));
			}
			
			incident.setIncludesLeoka(StringUtils.isNotBlank(newVictim.getOfficerOtherJurisdictionORI()));
			incident.addVictim(newVictim);
		}
	}

	private void buildPropertySegments(Element reportElement, GroupAIncidentReport incident,
			List<NIBRSError> errorList) {

		NodeList itemElements = (NodeList) XmlUtils.xPathNodeListSearch(reportElement, "nc:Item");
		
		Map<String, PropertySegment> typeOfLossPropertySegments = new LinkedHashMap<>();
		for(int i=0; i < itemElements.getLength(); i++){
			ReportSource reportSource = new ReportSource(incident.getSource());
			reportSource.setSourceLocation("item" + StringUtils.leftPad(String.valueOf(i+1), 2, '0'));
			Element itemElement = (Element) itemElements.item(i);
			
			String typeOfPropertyLossCode = getTypeOfPropertyLossCode(itemElement);
			
			PropertySegment propertySegment = typeOfLossPropertySegments.get(typeOfPropertyLossCode);
			
			if (propertySegment == null){
				propertySegment = new PropertySegment(); 
				typeOfLossPropertySegments.put(typeOfPropertyLossCode, propertySegment);
				propertySegment.setTypeOfPropertyLoss(typeOfPropertyLossCode);
			}
			
			int index = propertySegment.getPopulatedPropertyDescriptionCount(); 
			
			String propertyDescription = XmlUtils.xPathStringSearch(itemElement, "j:ItemCategoryNIBRSPropertyCategoryCode");
			propertySegment.setPropertyDescription(index, propertyDescription);
			
			parsePropertyValue(incident, errorList, reportSource, itemElement, propertySegment, index);

			parseRecoveredDate(incident, errorList, reportSource, itemElement, propertySegment, index);

			parseNumberOfStolenOrRecoveredVehicles(itemElement, typeOfPropertyLossCode, propertySegment, propertyDescription);

		}
		
		NodeList substanceElements = (NodeList) XmlUtils.xPathNodeListSearch(reportElement, "nc:Substance");
		for(int i=0; i < substanceElements.getLength(); i++){
			ReportSource reportSource = new ReportSource(incident.getSource());
			reportSource.setSourceLocation("sbstnc" + StringUtils.leftPad(String.valueOf(i+1), 2, '0'));
			Element substanceElement = (Element) substanceElements.item(i);
			
			String typeOfPropertyLossCode = getTypeOfPropertyLossCode(substanceElement);
			
			PropertySegment propertySegment = typeOfLossPropertySegments.get(typeOfPropertyLossCode);
			
			if (propertySegment == null){
				propertySegment = new PropertySegment(); 
				typeOfLossPropertySegments.put(typeOfPropertyLossCode, propertySegment);
				propertySegment.setTypeOfPropertyLoss(typeOfPropertyLossCode);
			}
			
			int drugIndex = propertySegment.getPopulatedSuspectedDrugTypeCount(); 
			int propertyDescriptionIndex = propertySegment.getPopulatedPropertyDescriptionCount();
			String propertyDescription = XmlUtils.xPathStringSearch(substanceElement, "j:ItemCategoryNIBRSPropertyCategoryCode");
			if (!(PropertyDescriptionCode._10.code.equals(propertyDescription) && propertySegment.containsPropertyDescription(propertyDescription))){
				propertySegment.setPropertyDescription(propertyDescriptionIndex, propertyDescription);
			}
			else{
				for (int j=0; j < propertySegment.getPopulatedPropertyDescriptionCount(); j++){
					if (PropertyDescriptionCode._10.code.equals(propertySegment.getPropertyDescription(j))){
						propertyDescriptionIndex = j;
					}
				}
			}

			parsePropertyValue(incident, errorList, reportSource, substanceElement, propertySegment, propertyDescriptionIndex); 
			parseRecoveredDate(incident, errorList, reportSource, substanceElement, propertySegment, propertyDescriptionIndex);
			
			String drugCategoryCode = XmlUtils.xPathStringSearch(substanceElement, "j:DrugCategoryCode");
			propertySegment.setSuspectedDrugType(drugIndex, drugCategoryCode);
			
			String drugQuantityFullValueString = XmlUtils.xPathStringSearch(substanceElement, "nc:SubstanceQuantityMeasure/nc:MeasureDecimalValue");
			if (StringUtils.isNotBlank(drugQuantityFullValueString)){
				try{
					Double doubleValue = new Double(drugQuantityFullValueString);
					propertySegment.setEstimatedDrugQuantity(i, new ParsedObject<Double>(doubleValue));
				}
				catch (NumberFormatException ne){
					log.error(ne);
					ParsedObject<Double> estimatedDrugQuantity = ParsedObject.getInvalidParsedObject();
					propertySegment.setEstimatedDrugQuantity(i, estimatedDrugQuantity);
					NIBRSError e = new NIBRSError();
					e.setContext(reportSource);
					e.setReportUniqueIdentifier(incident.getIncidentNumber());
					e.setSegmentType(propertySegment.getSegmentType());
					e.setValue(drugQuantityFullValueString);
					e.setNIBRSErrorCode(NIBRSErrorCode._302);
					e.setWithinSegmentIdentifier(null);
					e.setDataElementIdentifier("21");
					errorList.add(e);
					estimatedDrugQuantity.setValidationError(e);
				}
			}
			else{
				propertySegment.setEstimatedDrugQuantity(drugIndex, ParsedObject.getMissingParsedObject());
			}
			
			String drugMeasurementType = XmlUtils.xPathStringSearch(substanceElement, 
					"nc:SubstanceQuantityMeasure/j:SubstanceUnitCode");
			propertySegment.setTypeDrugMeasurement(drugIndex, drugMeasurementType );
		}

		typeOfLossPropertySegments.entrySet().forEach(entry -> incident.addProperty(entry.getValue()));
	}

	private void parseRecoveredDate(GroupAIncidentReport incident, List<NIBRSError> errorList, 
			ReportSource reportSource, Element itemElement, PropertySegment propertySegment, int index) {
		String recoveredDateString = XmlUtils.xPathStringSearch(itemElement, "nc:ItemValue/nc:ItemValueDate/nc:Date"); 
		
		ParsedObject<LocalDate> d = propertySegment.getDateRecovered(index);
		d.setMissing(false);
		d.setInvalid(false);
		if (recoveredDateString == null) {
			d.setMissing(true);
			d.setValue(null);
		} else {
			try {
				LocalDate recoveredDate = LocalDate.parse(recoveredDateString);
				d.setValue(recoveredDate);
			} catch (DateTimeParseException pe) {
				NIBRSError e = new NIBRSError();
				e.setContext(reportSource);
				e.setReportUniqueIdentifier(incident.getIncidentNumber());
				e.setSegmentType(propertySegment.getSegmentType());
				e.setValue(recoveredDateString);
				e.setNIBRSErrorCode(NIBRSErrorCode._305);
				e.setDataElementIdentifier("17");
				errorList.add(e);
				d.setInvalid(true);
				d.setValidationError(e);
			}
		}
	}

	private void parsePropertyValue(GroupAIncidentReport incident, List<NIBRSError> errorList, 
			ReportSource reportSource, Element parentElement, PropertySegment propertySegment, int index) {
		String propertyValueString = XmlUtils.xPathStringSearch(parentElement, "nc:ItemValue/nc:ItemValueAmount/nc:Amount"); 
		
		ParsedObject<Integer> propertyValue = propertySegment.getValueOfProperty(index);
		if (propertyValue.getValue() == null){
			propertyValue.setInvalid(false);
			propertyValue.setMissing(false);
		}
		
		if (propertyValueString == null) {
			if (propertyValue.getValue() == null){
				propertyValue.setValue(null);
				propertyValue.setInvalid(false);
				propertyValue.setMissing(true);
			}
		} else {
			try {
				String valueOfPropertyPattern = "\\d{1,9}";
				if (propertyValueString.matches(valueOfPropertyPattern)){
					Integer propertyValueI = Integer.parseInt(propertyValueString);
					propertyValue.setValue(propertyValueI + Optional.ofNullable(propertyValue.getValue()).orElse(0));
				}
				else{
					throw new NumberFormatException(); 
				}
			} catch (NumberFormatException nfe) {
				NIBRSError e = new NIBRSError();

				e.setContext(reportSource);
				e.setReportUniqueIdentifier(incident.getIncidentNumber());
				e.setSegmentType(propertySegment.getSegmentType());
				e.setValue(org.apache.commons.lang3.StringUtils.leftPad(propertyValueString, 9));
				e.setNIBRSErrorCode(NIBRSErrorCode._302);
				e.setWithinSegmentIdentifier(null);
				e.setDataElementIdentifier("16");
				errorList.add(e);
				propertyValue.setMissing(false);
				propertyValue.setInvalid(true);
			}
		}
	}

	private void parseNumberOfStolenOrRecoveredVehicles(Element itemElement, String typeOfPropertyLossCode, PropertySegment propertySegment,
			String propertyDescription) {
		if (PropertyDescriptionCode.isMotorVehicleCode(propertyDescription)
				&& (TypeOfPropertyLossCode._7.code.equals(typeOfPropertyLossCode) 
						|| TypeOfPropertyLossCode._5.code.equals(typeOfPropertyLossCode) )){
			
			String itemQuantityString = XmlUtils.xPathStringSearch(itemElement, "nc:ItemQuantity");
			ParsedObject<Integer> itemQuantity = null;
			if (TypeOfPropertyLossCode._7.code.equals(typeOfPropertyLossCode)){
				itemQuantity = propertySegment.getNumberOfStolenMotorVehicles();
			}
			else if (TypeOfPropertyLossCode._5.code.equals(typeOfPropertyLossCode)){
				itemQuantity = propertySegment.getNumberOfRecoveredMotorVehicles();
			}
			
			if (StringUtils.isBlank(itemQuantityString)) {
				if (itemQuantity.isMissing()){
					itemQuantity.setValue(null);
				}
			} else {
				try {
					Integer thisItemQuantity = Integer.parseInt(itemQuantityString);
					if (itemQuantity.isMissing() || itemQuantity.isInvalid()){
						itemQuantity.setValue(thisItemQuantity);
						itemQuantity.setMissing(false);
						itemQuantity.setInvalid(false);
					}
					else{
						itemQuantity.setValue(itemQuantity.getValue() + thisItemQuantity);
					}
				} catch (NumberFormatException nfe) {
					
					if (itemQuantity.isMissing() || itemQuantity.isInvalid()){
						itemQuantity.setInvalid(true);
					}
				}
			}
		}
	}

	private String getTypeOfPropertyLossCode(Element itemElement) {
		String typeOfPropertyLoss = XmlUtils.xPathStringSearch(itemElement, "nc:ItemStatus/cjis:ItemStatusCode");
		String typeOfPropertyLossCode = Optional.ofNullable(TypeOfPropertyLossCode.valueOfIepdCode(typeOfPropertyLoss))
				.map(item->item.code).orElse(null); 
		if (typeOfPropertyLossCode == null){
			typeOfPropertyLossCode = TypeOfPropertyLossCode.valueOfIepdCode("UNKNOWN").code;
		}
		return typeOfPropertyLossCode;
	}

	private void buildOffenseSegments(Element reportElement, GroupAIncidentReport incident,
			List<NIBRSError> errorList) {
		
		char segmentType = '2';
		
		NodeList offenseElements = (NodeList) XmlUtils.xPathNodeListSearch(reportElement, "j:Offense");
		for(int i=0; i < offenseElements.getLength(); i++){
			Element offenseElement = (Element) offenseElements.item(i);
			OffenseSegment newOffense = new OffenseSegment();
			
			ReportSource reportSource = new ReportSource(incident.getSource());
			String offenseId = XmlUtils.xPathStringSearch(offenseElement, "@s:id");
			reportSource.setSourceLocation(offenseId);
			
			NodeList offenseFactorBiasMotivationCodes = 
					XmlUtils.xPathNodeListSearch(offenseElement, "j:OffenseFactorBiasMotivationCode");
			
			if (offenseFactorBiasMotivationCodes.getLength() > 0){
				
				for(int j=0; j < offenseFactorBiasMotivationCodes.getLength(); j++){
					Element offenseFactorBiasMotivationCode = 
							(Element) offenseFactorBiasMotivationCodes.item(j);
					newOffense.setBiasMotivation(j, BiasMotivationCode.valueOfIepdCode(offenseFactorBiasMotivationCode.getTextContent()).code);
				}
				
			}
			else {
				NIBRSError e = new NIBRSError();
				e.setContext(reportSource);
				e.setReportUniqueIdentifier(incident.getIncidentNumber());
				e.setSegmentType(segmentType);
				e.setNIBRSErrorCode(NIBRSErrorCode._201);
				errorList.add(e);
				
				break; 
			}
			
			newOffense.setUcrOffenseCode(XmlUtils.xPathStringSearch(offenseElement, "nibrs:OffenseUCRCode")); 
			String offenseAttemptedIndicatorString = XmlUtils.xPathStringSearch(offenseElement, "j:OffenseAttemptedIndicator"); 
			Boolean offenseAttemptedIndicator = BooleanUtils.toBooleanObject(offenseAttemptedIndicatorString); 
			newOffense.setOffenseAttemptedCompleted(BooleanUtils.toString(offenseAttemptedIndicator, "A", "C", null));
			newOffense.setLocationType(XmlUtils.xPathStringSearch(reportElement, "nc:Location[@s:id = ../j:OffenseLocationAssociation[j:Offense/@s:ref = '" + offenseId + "']/nc:Location/@s:ref]/nibrs:LocationCategoryCode"));
			
			String premisesEnteredString = XmlUtils.xPathStringSearch(offenseElement, "j:OffenseStructuresEnteredQuantity");
			ParsedObject<Integer> premisesEntered = newOffense.getNumberOfPremisesEntered();
			
			if (premisesEnteredString == null) {
				premisesEntered.setMissing(true);
				premisesEntered.setInvalid(false);
				premisesEntered.setValue(null);
			} else {
				
				try {
					Integer value = Integer.parseInt(premisesEnteredString);
					premisesEntered.setValue(value);
					premisesEntered.setMissing(false);
					premisesEntered.setInvalid(false);
				} catch (NumberFormatException nfe) {
					NIBRSError e = new NIBRSError();
					e.setContext(reportSource);
					e.setReportUniqueIdentifier(incident.getIncidentNumber());
					e.setSegmentType(segmentType);
					e.setValue(premisesEnteredString);
					e.setNIBRSErrorCode(NIBRSErrorCode._204);
					e.setDataElementIdentifier("10");
					errorList.add(e);
					premisesEntered.setInvalid(true);
					premisesEntered.setValidationError(e);
				}
				
			}
			
			newOffense.setMethodOfEntry(XmlUtils.xPathStringSearch(offenseElement, "j:OffenseEntryPoint/j:PassagePointMethodCode"));
			
			parseOffendersSuspectedOfUsings(offenseElement, newOffense);

			parseTypesOfCriminalActivities(offenseElement, newOffense);
			
			parseTypesWeaponForceInvolved(offenseElement, newOffense);
			
			incident.addOffense(newOffense); 
			
		}
	}
	
	private void parseTypesWeaponForceInvolved(Element offenseElement, OffenseSegment newOffense) {
		NodeList typesWeaponForceInvolved = 
				XmlUtils.xPathNodeListSearch(offenseElement, "j:OffenseForce/j:ForceCategoryCode");
		
		for( int j = 0; j < typesWeaponForceInvolved.getLength() && j < OffenseSegment.TYPE_OF_WEAPON_FORCE_INVOLVED_COUNT; j++ ){
			Element typeWeaponForceInvolved = 
					(Element) typesWeaponForceInvolved.item(j);
			String typeWeaponForceInvolvedCode = typeWeaponForceInvolved.getTextContent();
			newOffense.setTypeOfWeaponForceInvolved(j, StringUtils.removeEnd(typeWeaponForceInvolvedCode, "A"));
			
			String automaticWeaponIndicator = getAutomaticWeaponIndicator(typeWeaponForceInvolvedCode);
			newOffense.setAutomaticWeaponIndicator(j, automaticWeaponIndicator);
		}
	}

	private String getAutomaticWeaponIndicator(String typeWeaponForceInvolvedCode) {
		return BooleanUtils.toString(automaticWeaponCodes.contains(typeWeaponForceInvolvedCode), "A", 
				AutomaticWeaponIndicatorCode._blank.code);
	}

	private void parseTypesOfCriminalActivities(Element offenseElement, OffenseSegment newOffense) {
		NodeList typesOfCriminalActivities = 
				XmlUtils.xPathNodeListSearch(offenseElement, "nibrs:CriminalActivityCategoryCode");
		
		for( int j = 0; j < typesOfCriminalActivities.getLength() && j < OffenseSegment.TYPE_OF_CRIMINAL_ACTIVITY_COUNT; j++ ){
			Element typeOfCriminalActivity = 
					(Element) typesOfCriminalActivities.item(j);
			newOffense.setTypeOfCriminalActivity(j, typeOfCriminalActivity.getTextContent());
		}
	}

	private void parseOffendersSuspectedOfUsings(Element offenseElement, OffenseSegment newOffense) {
		NodeList offendersSuspectedOfUsings = 
				XmlUtils.xPathNodeListSearch(offenseElement, "j:OffenseFactor/j:OffenseFactorCode");
		
		for( int j = 0; j < offendersSuspectedOfUsings.getLength() && j < OffenseSegment.OFFENDERS_SUSPECTED_OF_USING_COUNT; j++ ){
			Element offendersSuspectedOfUsing = 
					(Element) offendersSuspectedOfUsings.item(j);
			newOffense.setOffendersSuspectedOfUsing(j, offendersSuspectedOfUsing.getTextContent());
		}
	}

	private final void handleNewReport(AbstractReport newReport, List<NIBRSError> errorList) {
		if (newReport != null) {
			for (Iterator<ReportListener> it = getListeners().iterator(); it.hasNext();) {
				ReportListener listener = it.next();
				listener.newReport(newReport, errorList);
			}
		}
	}


	class NullResolver implements EntityResolver {
		public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
			return new InputSource(new StringReader(""));
		}
	}

}
