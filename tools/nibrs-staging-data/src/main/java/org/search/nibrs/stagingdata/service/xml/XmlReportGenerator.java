
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

package org.search.nibrs.stagingdata.service.xml;

import static org.search.nibrs.xml.NibrsNamespaceContext.Namespace.CJIS;
import static org.search.nibrs.xml.NibrsNamespaceContext.Namespace.J;
import static org.search.nibrs.xml.NibrsNamespaceContext.Namespace.NC;
import static org.search.nibrs.xml.NibrsNamespaceContext.Namespace.NIBRS;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.search.nibrs.model.codes.BiasMotivationCode;
import org.search.nibrs.model.codes.PropertyDescriptionCode;
import org.search.nibrs.model.codes.RelationshipOfVictimToOffenderCode;
import org.search.nibrs.model.codes.TypeOfPropertyLossCode;
import org.search.nibrs.stagingdata.AppProperties;
import org.search.nibrs.stagingdata.model.AdditionalJustifiableHomicideCircumstancesType;
import org.search.nibrs.stagingdata.model.AggravatedAssaultHomicideCircumstancesType;
import org.search.nibrs.stagingdata.model.BiasMotivationType;
import org.search.nibrs.stagingdata.model.DispositionOfArresteeUnder18Type;
import org.search.nibrs.stagingdata.model.EthnicityOfPersonType;
import org.search.nibrs.stagingdata.model.MethodOfEntryType;
import org.search.nibrs.stagingdata.model.MultipleArresteeSegmentsIndicatorType;
import org.search.nibrs.stagingdata.model.OffenderSuspectedOfUsingType;
import org.search.nibrs.stagingdata.model.PropertyType;
import org.search.nibrs.stagingdata.model.RaceOfPersonType;
import org.search.nibrs.stagingdata.model.ResidentStatusOfPersonType;
import org.search.nibrs.stagingdata.model.SegmentActionTypeType;
import org.search.nibrs.stagingdata.model.SexOfPersonType;
import org.search.nibrs.stagingdata.model.SubmissionTrigger;
import org.search.nibrs.stagingdata.model.SuspectedDrugType;
import org.search.nibrs.stagingdata.model.TypeInjuryType;
import org.search.nibrs.stagingdata.model.TypeOfArrestType;
import org.search.nibrs.stagingdata.model.TypeOfCriminalActivityType;
import org.search.nibrs.stagingdata.model.TypeOfWeaponForceInvolved;
import org.search.nibrs.stagingdata.model.UcrOffenseCodeType;
import org.search.nibrs.stagingdata.model.VictimOffenderAssociation;
import org.search.nibrs.stagingdata.model.segment.AdministrativeSegment;
import org.search.nibrs.stagingdata.model.segment.ArrestReportSegment;
import org.search.nibrs.stagingdata.model.segment.ArresteeSegment;
import org.search.nibrs.stagingdata.model.segment.OffenderSegment;
import org.search.nibrs.stagingdata.model.segment.OffenseSegment;
import org.search.nibrs.stagingdata.model.segment.PropertySegment;
import org.search.nibrs.stagingdata.model.segment.VictimSegment;
import org.search.nibrs.stagingdata.repository.AgencyRepository;
import org.search.nibrs.stagingdata.repository.segment.AdministrativeSegmentRepository;
import org.search.nibrs.stagingdata.repository.segment.ArrestReportSegmentRepository;
import org.search.nibrs.stagingdata.service.AdministrativeSegmentService;
import org.search.nibrs.xml.NibrsNamespaceContext;
import org.search.nibrs.xml.NibrsNamespaceContext.Namespace;
import org.search.nibrs.xml.XmlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Service
public class XmlReportGenerator {

	private final Log log = LogFactory.getLog(this.getClass());
	
	static final NumberFormat MONTH_NUMBER_FORMAT = new DecimalFormat("00");
	static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	@Autowired
	public AdministrativeSegmentRepository administrativeSegmentRepository; 
	@Autowired
	public ArrestReportSegmentRepository arrestReportSegmentRepository; 

	@Autowired
	AdministrativeSegmentService administrativeSegmentService;
	@Autowired
	public AgencyRepository agencyRepository; 
	@Autowired
	private AppProperties appProperties;
	
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS");

	public long countTheIncidents(SubmissionTrigger submissionTrigger) {
		long groupAIncidentCount = administrativeSegmentRepository
				.countByOriListAndSubmissionDateRange(submissionTrigger.getOris(), 
						submissionTrigger.getStartDate(), 
						submissionTrigger.getEndDate(), 
						submissionTrigger.getAgencyIds());
		log.info("groupAIncidentCount: " + groupAIncidentCount);
		long groubBArrestReportCount = arrestReportSegmentRepository
				.countByOriListAndSubmissionDateRange(submissionTrigger.getOris(), 
						submissionTrigger.getStartDate(), 
						submissionTrigger.getEndDate(),
						submissionTrigger.getAgencyIds());
		log.info("groubBArrestReportCount: " + groubBArrestReportCount);
		return groupAIncidentCount + groubBArrestReportCount; 
	}

	@Async
	public void processSubmissionTrigger(SubmissionTrigger submissionTrigger) throws Exception{
		
	    File directorty = new File(appProperties.getNibrsNiemDocumentFolder()); 
	    if (!directorty.exists()){
	    	directorty.mkdirs(); 
	    }

	    writeGroupAIncidentReports(submissionTrigger);
	    writeGroupBIncidentReports(submissionTrigger);
	}
	
	@Async
	public void processGroupASubmission(Integer administrativeSegmentId) throws Exception{
		
		File directorty = new File(appProperties.getNibrsNiemDocumentFolder()); 
		if (!directorty.exists()){
			directorty.mkdirs(); 
		}

		writeGroupAIncidentReport(administrativeSegmentId);
	}
	
	private void writeGroupAIncidentReports(SubmissionTrigger submissionTrigger) throws Exception {
		
		List<Integer> ids = administrativeSegmentRepository.findIdsByOriListAndSubmissionDateRange(
				submissionTrigger.getOris(), submissionTrigger.getStartDate(), submissionTrigger.getEndDate(), submissionTrigger.getAgencyIds());
		
		for (Integer administrativeSegmentId : ids) {
			writeGroupAIncidentReport(administrativeSegmentId);
		}
	}

	private void writeGroupAIncidentReport(Integer administrativeSegmentId) throws Exception {
		log.info("Generating group A report for pkId " + administrativeSegmentId);
		AdministrativeSegment administrativeSegment = administrativeSegmentRepository.findByAdministrativeSegmentId(administrativeSegmentId);
		
		writeAdministrativeSegmentToXml(administrativeSegment, appProperties.getNibrsNiemDocumentFolder());
	}

	public void writeAdministrativeSegmentToXml(AdministrativeSegment administrativeSegment, String rootFolder) {
		try {
			Document document = this.createGroupAIncidentReport(administrativeSegment);
			
			String fileName = rootFolder + "/GroupAIncident" + administrativeSegment.getIncidentNumber() + "-" + LocalDateTime.now().format(formatter) + ".xml";
			log.info("Writing the XML report for GroupA Incident:\n " + administrativeSegment.getIncidentNumber() + " to " + fileName);
			FileUtils.writeStringToFile(new File(fileName), XmlUtils.nodeToString(document), "UTF-8");
		}
		catch (Exception e) {
			log.error("Failed to generate and write the report for GroupA Incident:\n " + administrativeSegment);
			log.error(e.getMessage());
			throw new RuntimeException(e); 
		}
	}
	
	public void writeGroupBIncidentReports(SubmissionTrigger submissionTrigger){
		
		List<Integer> ids = arrestReportSegmentRepository.findIdsByOriListAndSubmissionDateRange(
				submissionTrigger.getOris(), submissionTrigger.getStartDate(), submissionTrigger.getEndDate(), submissionTrigger.getAgencyIds());
		
		for (Integer arrestReportSegmentId : ids) {
			log.info("Generating arrest report for pkId " + arrestReportSegmentId);
			
			ArrestReportSegment arrestReportSegment = arrestReportSegmentRepository.findByArrestReportSegmentId(arrestReportSegmentId);
			
			writeArrestReportSegmentToXml(arrestReportSegment, appProperties.getNibrsNiemDocumentFolder());
		}
	}

	public void writeArrestReportSegmentToXml(ArrestReportSegment arrestReportSegment, String rootFolder) {
		try {
			Document document = createGroupBArrestReport(arrestReportSegment);
			
			String fileName = rootFolder + "/GroupBArrestReport" + arrestReportSegment.getArrestTransactionNumber() + "-" + LocalDateTime.now().format(formatter) + ".xml";
			
			FileUtils.writeStringToFile(new File(fileName), XmlUtils.nodeToString(document), "UTF-8");			
		}
		catch (Exception e) {
			log.error("Failed to generate and write the report for Group B Arrest Report:\n " + arrestReportSegment);
			log.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public Document createGroupAIncidentReport(AdministrativeSegment administrativeSegment) throws ParserConfigurationException {
		Document document = XmlUtils.createNewDocument();
		Element submissionElement = XmlUtils.appendChildElement(document, NIBRS, "Submission");
		
		addMessageMetadataElement(administrativeSegment.getAdministrativeSegmentId(), submissionElement);
		
		Element reportElement = XmlUtils.appendChildElement(submissionElement, NIBRS, "Report"); 
		addReportHeaderElement(administrativeSegment, reportElement);
		addIncidentElement(administrativeSegment, reportElement);
		addOffenseElements(administrativeSegment, reportElement);
		addLocationElements(administrativeSegment, reportElement);
		addItemElements(administrativeSegment, reportElement);
		addSubstanceElements(administrativeSegment, reportElement);
		addPersonElements(administrativeSegment, reportElement);
		addEnforcementOfficialElements(administrativeSegment, reportElement);
		addVictimElements(administrativeSegment, reportElement);
		addSubjectElements(administrativeSegment, reportElement);
		addArresteeElements(administrativeSegment, reportElement);
		addArrestElements(administrativeSegment, reportElement);
		addArrestSubjectAssociationElements(administrativeSegment, reportElement);
		addOffenseLocationAssociationElements(administrativeSegment, reportElement);
		addOffenseVictimAssociationElements(administrativeSegment, reportElement);
		addSubjectVictimAssociationElements(administrativeSegment, reportElement);
		
		NibrsNamespaceContext namespaceContext = new NibrsNamespaceContext();
		namespaceContext.populateRootNamespaceDeclarations(document.getDocumentElement());
		return document;
	}

	public Document createGroupBArrestReport(ArrestReportSegment arrestReportSegment) throws ParserConfigurationException {
		Document document = XmlUtils.createNewDocument();
		Element submissionElement = XmlUtils.appendChildElement(document, NIBRS, "Submission");
		
		addMessageMetadataElement(arrestReportSegment.getArrestReportSegmentId(), submissionElement);
		
		Element reportElement = XmlUtils.appendChildElement(submissionElement, NIBRS, "Report"); 
		addReportHeaderElement(arrestReportSegment, reportElement);
		addPersonElements(arrestReportSegment, reportElement);
		
		addArresteeElements(arrestReportSegment, reportElement);
		addArrestElement(reportElement, arrestReportSegment.getArresteeSequenceNumber(), 
				arrestReportSegment.getArrestTransactionNumber(), 
				arrestReportSegment.getArrestDate(), 
				arrestReportSegment.getUcrOffenseCodeType(), 
				arrestReportSegment.getTypeOfArrestType());
		
		addArrestSubjectAssociationElement(reportElement, arrestReportSegment.getArresteeSequenceNumber());
		
		NibrsNamespaceContext namespaceContext = new NibrsNamespaceContext();
		namespaceContext.populateRootNamespaceDeclarations(document.getDocumentElement());
		return document;
	}
	
	private void addArresteeElements(ArrestReportSegment arrestReportSegment, Element reportElement) {
		Set<String> arresteeArmedWithTypeCodes = new HashSet<>(); 
		
		if (arrestReportSegment.getArrestReportSegmentWasArmedWiths() != null) {
			arresteeArmedWithTypeCodes =arrestReportSegment.getArrestReportSegmentWasArmedWiths().stream()
				.map(item-> item.getArresteeWasArmedWithType().getNibrsCode() + StringUtils.trimToEmpty(item.getAutomaticWeaponIndicator()))
				.collect(Collectors.toSet());
		}
		addArresteeElement(reportElement, arrestReportSegment.getArresteeSequenceNumber(), arresteeArmedWithTypeCodes, 
				arrestReportSegment.getDispositionOfArresteeUnder18Type(), null);
		
	}

	private void addPersonElements(ArrestReportSegment arrestReportSegment, Element reportElement) {
		Element arresteeElement = XmlUtils.appendChildElement(reportElement, Namespace.NC, "Person");
		XmlUtils.addAttribute(arresteeElement, Namespace.S, "id", "PersonArrestee-" + arrestReportSegment.getArresteeSequenceNumber());
		
		Integer ageMin = arrestReportSegment.getAgeOfArresteeMin();
		Integer ageMax = arrestReportSegment.getAgeOfArresteeMax();
		if ( ageMin != null && ageMin > 0) {
			addPersonAgeMeasure(arresteeElement, ageMin, ageMax);
		}
		else if (Objects.equals(arrestReportSegment.getNonNumericAge(), "00")){
			addPersonAgeMeasure(arresteeElement, "00");
		}
		
		addPersonInfo(arrestReportSegment.getEthnicityOfPersonType(), arrestReportSegment.getRaceOfPersonType(), 
				arrestReportSegment.getResidentStatusOfPersonType(), arrestReportSegment.getSexOfPersonType(), arresteeElement);
	}

	private void addIncidentElement(AdministrativeSegment administrativeSegment, Element reportElement) {
		Element incidentElement = XmlUtils.appendChildElement(reportElement, Namespace.NC, "Incident");
		
		appendIdentificationIdElement(incidentElement, NC, "ActivityIdentification", administrativeSegment.getIncidentNumber());
		
		LocalDate incidentDate = administrativeSegment.getIncidentDate();
		String incidentHour = administrativeSegment.getIncidentHour(); 
		if (incidentDate != null) {
			Element activityDate = XmlUtils.appendChildElement(incidentElement, Namespace.NC, "ActivityDate");
			
			if (StringUtils.isNotBlank(incidentHour)){
				String incidentHourString = "T" + StringUtils.leftPad(incidentHour, 2, '0') + ":00:00"; 
				Element element = XmlUtils.appendChildElement(activityDate, Namespace.NC, "DateTime");
				element.setTextContent(incidentDate + incidentHourString);
			}
			else {
				Element e = XmlUtils.appendChildElement(activityDate, Namespace.NC, "Date");
				e.setTextContent(incidentDate.toString());
			}
		}
		
		Element incidentAugmentation = XmlUtils.appendChildElement(incidentElement, Namespace.CJIS, "IncidentAugmentation");
		Boolean isReportDate = "R".equals(administrativeSegment.getReportDateIndicator()); 
		if (BooleanUtils.isTrue(isReportDate)) { 
			XmlUtils.appendElementAndValue(incidentAugmentation, Namespace.CJIS, "IncidentReportDateIndicator", 
					Boolean.TRUE.toString());
		}
		Element jIncidentAugElement = XmlUtils.appendChildElement(incidentElement, Namespace.J, "IncidentAugmentation");
		XmlUtils.appendElementAndValue(jIncidentAugElement, Namespace.J, "IncidentExceptionalClearanceCode", 
				administrativeSegment.getClearedExceptionallyType().getNibrsCode());
		
		Date exceptionalClearanceDate = administrativeSegment.getExceptionalClearanceDate();
		if (exceptionalClearanceDate != null) {
			Element incidentExceptionalClearanceDate = XmlUtils.appendChildElement(jIncidentAugElement, Namespace.J, "IncidentExceptionalClearanceDate");
			Element e = XmlUtils.appendChildElement(incidentExceptionalClearanceDate, Namespace.NC, "Date");
			e.setTextContent(DATE_FORMAT.format(exceptionalClearanceDate));
		}
	}

	private void addMessageMetadataElement(Integer messageId, Element submissionElement) {
		Element messageMetadata = XmlUtils.appendChildElement(submissionElement, CJIS, "MessageMetadata");
		Element messageDateTime = XmlUtils.appendChildElement(messageMetadata, CJIS, "MessageDateTime");
		messageDateTime.setTextContent(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss")));

		appendIdentificationIdElement(messageMetadata, CJIS, "MessageIdentification", messageId.toString());
		
		Element messageImplementationVersion = XmlUtils.appendChildElement(messageMetadata, CJIS, "MessageImplementationVersion"); 
		messageImplementationVersion.setTextContent("2019.1");
		
		Element messageSubmittingOrganization = XmlUtils.appendChildElement(messageMetadata, CJIS, "MessageSubmittingOrganization"); 
		Element organizationAugmentation = XmlUtils.appendChildElement(messageSubmittingOrganization, J, "OrganizationAugmentation");
		
		appendIdentificationIdElement(organizationAugmentation, J, "OrganizationORIIdentification", appProperties.getSubmittingAgencyOri());
	}

	private void appendIdentificationIdElement(Element parent, Namespace wrapperNamespace, String wrapperName, String id) {
		
		if (StringUtils.isNotBlank(id)){
			Element wrapperElement = XmlUtils.appendChildElement(parent, wrapperNamespace, wrapperName);
			Element identificationId = XmlUtils.appendChildElement(wrapperElement, NC, "IdentificationID");
			identificationId.setTextContent(id);
		}
	}

	private void addReportHeaderElement(AdministrativeSegment administrativeSegment, Element reportElement) {
		Element reportHeaderElement = XmlUtils.appendChildElement(reportElement, Namespace.NIBRS, "ReportHeader");
		Element nibrsReportCategoryCode = XmlUtils.appendChildElement(reportHeaderElement, Namespace.NIBRS, "NIBRSReportCategoryCode");
		nibrsReportCategoryCode.setTextContent("GROUP A INCIDENT REPORT");
		
		appendReportHeaderDetails(administrativeSegment.getSegmentActionType(), administrativeSegment.getYearOfTape(),  
				administrativeSegment.getMonthOfTape(), administrativeSegment.getOri(), administrativeSegment.getCityIndicator(),
				reportHeaderElement);
	}

	private void appendReportHeaderDetails(SegmentActionTypeType segmentActionTypeType,
			String yearOfTape, String monthOfTape, String ori, String cityIndicator,  
			Element reportHeaderElement) {
		XmlUtils.appendChildElement(reportHeaderElement, Namespace.NIBRS, "ReportActionCategoryCode")
			.setTextContent(segmentActionTypeType.getNibrsCode());
		
		Element reportDate = XmlUtils.appendChildElement(reportHeaderElement, Namespace.NIBRS, "ReportDate");
		Element yearMonthDate = XmlUtils.appendChildElement(reportDate, Namespace.NC, "YearMonthDate");
		yearMonthDate.setTextContent( yearOfTape + "-" + monthOfTape);
		
		if (StringUtils.isNotBlank(ori) || StringUtils.isNotBlank(cityIndicator)) {
			Element reportingAgency = XmlUtils.appendChildElement(reportHeaderElement, Namespace.NIBRS, "ReportingAgency");
			
			if (StringUtils.isNotBlank(ori)){
				Element organizationAugmentation = XmlUtils.appendChildElement(reportingAgency, J, "OrganizationAugmentation");
				appendIdentificationIdElement(organizationAugmentation, J, "OrganizationORIIdentification", ori);
			}
			if (StringUtils.isNotBlank(cityIndicator)){
				Element organizationAugmentation = XmlUtils.appendChildElement(reportingAgency, CJIS, "OrganizationAugmentation");
				appendIdentificationIdElement(organizationAugmentation, CJIS, "DirectReportingCityIdentification", cityIndicator);
			}
		}
	}

	private void addReportHeaderElement(ArrestReportSegment arrestReportSegment, Element reportElement) {
		Element reportHeaderElement = XmlUtils.appendChildElement(reportElement, Namespace.NIBRS, "ReportHeader");
		Element nibrsReportCategoryCode = XmlUtils.appendChildElement(reportHeaderElement, Namespace.NIBRS, "NIBRSReportCategoryCode");
		nibrsReportCategoryCode.setTextContent("GROUP B ARREST REPORT");
		appendReportHeaderDetails(arrestReportSegment.getSegmentActionType(), arrestReportSegment.getYearOfTape(),  
				arrestReportSegment.getMonthOfTape(), arrestReportSegment.getOri(), arrestReportSegment.getCityIndicator(),
				reportHeaderElement);
	}
	
	private void addOffenseElements(AdministrativeSegment administrativeSegment, Element reportElement) {
		for (OffenseSegment offense : administrativeSegment.getOffenseSegments()) {
			Element offenseElement = XmlUtils.appendChildElement(reportElement, Namespace.J, "Offense");
			XmlUtils.addAttribute(offenseElement, Namespace.S, "id", "Offense-" + offense.getUcrOffenseCodeType().getNibrsCode());
			XmlUtils.appendElementAndValue(offenseElement, Namespace.NIBRS, "OffenseUCRCode", offense.getUcrOffenseCodeType().getNibrsCode());
			
			for (TypeOfCriminalActivityType criminalActivityType: offense.getTypeOfCriminalActivityTypes()) {
				XmlUtils.appendElementAndValue(offenseElement, NIBRS, "CriminalActivityCategoryCode", criminalActivityType.getNibrsCode());
			}
			
			for (BiasMotivationType biasMotivationType: offense.getBiasMotivationTypes()) {
				if (StringUtils.isNotBlank(biasMotivationType.getNibrsCode())){
					XmlUtils.appendElementAndValue(offenseElement, J, "OffenseFactorBiasMotivationCode", 
							BiasMotivationCode.valueOfCode(biasMotivationType.getNibrsCode()).iepdCode);
				}
			}
			
			XmlUtils.appendElementAndValue(offenseElement, J, "OffenseStructuresEnteredQuantity", 
					Optional.ofNullable(offense.getNumberOfPremisesEntered()).map(String::valueOf).orElse(null));
			
			for (OffenderSuspectedOfUsingType offenderSuspectedOfUsingType: offense.getOffenderSuspectedOfUsingTypes()) {
				Element e = XmlUtils.appendChildElement(offenseElement, Namespace.J, "OffenseFactor");
				XmlUtils.appendChildElement(e, Namespace.J, "OffenseFactorCode").setTextContent(offenderSuspectedOfUsingType.getNibrsCode());
			}
			
			String methodOfEntry = Optional.ofNullable(offense.getMethodOfEntryType()).map(MethodOfEntryType::getNibrsCode)
					.orElse(null);
			if (StringUtils.isNotBlank(methodOfEntry)) {
				Element e = XmlUtils.appendChildElement(offenseElement, Namespace.J, "OffenseEntryPoint");
				XmlUtils.appendChildElement(e, Namespace.J, "PassagePointMethodCode").setTextContent(methodOfEntry);
			}
			
			for (TypeOfWeaponForceInvolved typeOfWeaponForceInvolved: offense.getTypeOfWeaponForceInvolveds()) {
				Element e = XmlUtils.appendChildElement(offenseElement, Namespace.J, "OffenseForce");
				XmlUtils.appendElementAndValue(e, Namespace.J, "ForceCategoryCode", 
						typeOfWeaponForceInvolved.getTypeOfWeaponForceInvolvedType().getNibrsCode() + 
						StringUtils.trimToEmpty(typeOfWeaponForceInvolved.getAutomaticWeaponIndicator()));
			}
			
			XmlUtils.appendElementAndValue(offenseElement, Namespace.J, "OffenseAttemptedIndicator", 
					BooleanUtils.toStringTrueFalse("A".equals(offense.getOffenseAttemptedCompleted())));
		}
	}
	
//	<!-- Element 9, Location Type -->
//	<nc:Location s:id="Location1">
//		<nibrs:LocationCategoryCode>16</nibrs:LocationCategoryCode>
//	</nc:Location>
	private void addLocationElements(AdministrativeSegment administrativeSegment, Element reportElement) {
		for (OffenseSegment offense : administrativeSegment.getOffenseSegments()) {
			Element locationElement = XmlUtils.appendChildElement(reportElement, Namespace.NC, "Location");
			XmlUtils.addAttribute(locationElement, Namespace.S, "id", "Location-" + offense.getUcrOffenseCodeType().getNibrsCode());
			XmlUtils.appendElementAndValue(locationElement, Namespace.NIBRS, "LocationCategoryCode", offense.getLocationType().getNibrsCode());
		}
	}

	private void addItemElements(AdministrativeSegment administrativeSegment, Element reportElement) {
		
		boolean contains35A = administrativeSegment.getOffenseSegments().stream()
				.anyMatch(offenseSegment-> "35A".equals(offenseSegment.getUcrOffenseCodeType().getNibrsCode()));
		
		List<PropertySegment> properties = administrativeSegment.getPropertySegments()
				.stream()
				.sorted(Comparator.comparing(PropertySegment::getPropertySegmentId, Comparator.nullsFirst(Comparator.naturalOrder())))
				.collect(Collectors.toList()); 
		for (PropertySegment property : properties) {
			if (("NONE".equalsIgnoreCase(property.getTypePropertyLossEtcType().getNibrsDescription()) 
					&& (property.getSuspectedDrugTypes() == null || property.getSuspectedDrugTypes().size() == 0))|| 
					"UNKNOWN".equalsIgnoreCase(property.getTypePropertyLossEtcType().getNibrsDescription())){
				
				Element itemElement = XmlUtils.appendChildElement(reportElement, Namespace.NC, "Item");
				Element itemStatus = XmlUtils.appendChildElement(itemElement, Namespace.NC, "ItemStatus");
				XmlUtils.appendElementAndValue(itemStatus, Namespace.CJIS, "ItemStatusCode", 
						property.getTypePropertyLossEtcType().getNibrsDescription().toUpperCase());
				continue;
			}
			
			List<PropertyType> sortedPropertyTypes = property.getPropertyTypes()
					.stream()
					.sorted(Comparator.comparing(PropertyType::getPropertyTypeId, Comparator.nullsFirst(Comparator.naturalOrder())))
					.collect(Collectors.toList()); 
			
			for (PropertyType propertyType : sortedPropertyTypes) {
				String nibrsCode = propertyType.getPropertyDescriptionType().getNibrsCode();
				if (nibrsCode != null && !("10".equals(nibrsCode) && contains35A) ) {
					Element itemElement = XmlUtils.appendChildElement(reportElement, Namespace.NC, "Item");
					
					addItemStatus(property, itemElement);
					
					addItemValueAndAmount(propertyType, itemElement);
					
					XmlUtils.appendElementAndValue(itemElement, Namespace.J, "ItemCategoryNIBRSPropertyCategoryCode", nibrsCode);
					
					if (PropertyDescriptionCode.isMotorVehicleCode(nibrsCode)){
						Integer rmv = property.getNumberOfRecoveredMotorVehicles();
						Integer smv = property.getNumberOfStolenMotorVehicles();
						if (rmv != null || smv != null) {
							XmlUtils.appendElementAndValue(itemElement, Namespace.NC, "ItemQuantity", String.valueOf(rmv != null ? rmv : smv));
						}
					}
				}
			}
		}

	}

	private void addItemValueAndAmount(PropertyType propertyType, Element parent) {
		String value = Optional.ofNullable(propertyType.getValueOfProperty())
				.map(item->item.intValue())
				.map(String::valueOf).orElse(null);
		if (value != null) {
			Element itemValue = XmlUtils.appendChildElement(parent, Namespace.NC, "ItemValue");
			Element itemValueAmount = XmlUtils.appendChildElement(itemValue, Namespace.NC, "ItemValueAmount");
			XmlUtils.appendElementAndValue(itemValueAmount, Namespace.NC, "Amount", value);
			
			Date dateRecovered = propertyType.getRecoveredDate();
			if (dateRecovered != null) {
				Element itemValueDate = XmlUtils.appendChildElement(itemValue, Namespace.NC, "ItemValueDate");
				XmlUtils.appendElementAndValue(itemValueDate, Namespace.NC, "Date", DATE_FORMAT.format(dateRecovered));
			}
		}
	}

	private void addItemStatus(PropertySegment property, Element parent) {
		Element itemStatus = XmlUtils.appendChildElement(parent, Namespace.NC, "ItemStatus");
		String typeOfPropertyLossNibrsCode = property.getTypePropertyLossEtcType().getNibrsCode();
		String typeOfPropertyLossIepdCode = Optional.ofNullable(TypeOfPropertyLossCode.valueOfCode(typeOfPropertyLossNibrsCode))
				.map(TypeOfPropertyLossCode::getIepdCode).orElse(null);
		XmlUtils.appendElementAndValue(itemStatus, Namespace.CJIS, "ItemStatusCode", typeOfPropertyLossIepdCode);
	}
	
	private void addSubstanceElements(AdministrativeSegment administrativeSegment, Element reportElement) {
		boolean contains35A = administrativeSegment.getOffenseSegments().stream()
				.anyMatch(offenseSegment-> "35A".equals(offenseSegment.getUcrOffenseCodeType().getNibrsCode()));

		if (!contains35A) {
			return;
		}
		List<PropertySegment> properties = administrativeSegment.getPropertySegments()
				.stream()
				.sorted(Comparator.comparing(PropertySegment::getPropertySegmentId, Comparator.nullsFirst(Comparator.naturalOrder())))
				.collect(Collectors.toList()); 
		for (PropertySegment property : properties) {
			List<PropertyType> sortedPropertyTypes = property.getPropertyTypes()
					.stream()
					.sorted(Comparator.comparing(PropertyType::getPropertyTypeId, Comparator.nullsFirst(Comparator.naturalOrder())))
					.collect(Collectors.toList()); 
			if ("NONE".equalsIgnoreCase(property.getTypePropertyLossEtcType().getNibrsDescription()) 
					&& property.getSuspectedDrugTypes() != null && property.getSuspectedDrugTypes().size()>0){
				for (SuspectedDrugType suspectedDrugType : property.getSuspectedDrugTypes()){
					Element substanceElement = XmlUtils.appendChildElement(reportElement, Namespace.NC, "Substance");
					addItemStatus(property, substanceElement);
					XmlUtils.appendElementAndValue(substanceElement, J, "DrugCategoryCode", 
							suspectedDrugType.getSuspectedDrugTypeType().getNibrsCode());
				}
				continue;
			}
			
			for (PropertyType propertyType : sortedPropertyTypes) {
				String description = propertyType.getPropertyDescriptionType().getNibrsCode();
				if ("10".equals(description)) {
					
					if (property.getSuspectedDrugTypes().size() > 0){
						for (SuspectedDrugType suspectedDrugType : property.getSuspectedDrugTypes()){
							Element substanceElement = XmlUtils.appendChildElement(reportElement, Namespace.NC, "Substance");
							
							addItemStatus(property, substanceElement);
							addItemValueAndAmount(propertyType, substanceElement);
							XmlUtils.appendElementAndValue(substanceElement, Namespace.J, "ItemCategoryNIBRSPropertyCategoryCode", description);
	
							XmlUtils.appendElementAndValue(substanceElement, J, "DrugCategoryCode", 
									suspectedDrugType.getSuspectedDrugTypeType().getNibrsCode());
							
							if (suspectedDrugType.getEstimatedDrugQuantity()!= null || (suspectedDrugType.getTypeDrugMeasurementType() != null && 
									suspectedDrugType.getTypeDrugMeasurementType().getTypeDrugMeasurementTypeId() != 99998)){
								Element substanceQuantityMeasure = XmlUtils.appendChildElement(substanceElement, Namespace.NC, "SubstanceQuantityMeasure");
								
								String estimatedDrugQuantityString = Optional.ofNullable(suspectedDrugType.getEstimatedDrugQuantity())
										.map(String::valueOf)
										.map(item->{return "1.0".equals(item)? "1":item;})
										.orElse(null); 
								XmlUtils.appendElementAndValue(substanceQuantityMeasure, Namespace.NC, "MeasureDecimalValue", estimatedDrugQuantityString);
								XmlUtils.appendElementAndValue(substanceQuantityMeasure, Namespace.J, "SubstanceUnitCode",
										suspectedDrugType.getTypeDrugMeasurementType().getNibrsCode());
							}
						}
					}
					else{
						Element substanceElement = XmlUtils.appendChildElement(reportElement, Namespace.NC, "Substance");
						
						addItemStatus(property, substanceElement);
						addItemValueAndAmount(propertyType, substanceElement);
						XmlUtils.appendElementAndValue(substanceElement, Namespace.J, "ItemCategoryNIBRSPropertyCategoryCode", description);
					}
				}
			}
		}

	}

	private void addPersonElements(AdministrativeSegment administrativeSegment, Element reportElement) {
		addVictimPersonElements(administrativeSegment, reportElement);
		addOffenderPersonElements(administrativeSegment, reportElement);
		addArresteePersonElements(administrativeSegment, reportElement);
	}

	private void addVictimPersonElements(AdministrativeSegment administrativeSegment, Element reportElement) {
		for (VictimSegment victim : administrativeSegment.getVictimSegments()) {
			String victimType = victim.getTypeOfVictimType().getNibrsCode();
			if ("L".equals(victimType) || "I".equals(victimType)) {
				Element victimElement = XmlUtils.appendChildElement(reportElement, Namespace.NC, "Person");
				XmlUtils.addAttribute(victimElement, Namespace.S, "id", "PersonVictim-" + victim.getVictimSequenceNumber());
				Integer ageMin = victim.getAgeOfVictimMin();
				Integer ageMax = victim.getAgeOfVictimMax();
				if ( ageMin != null && ageMin > 0) {
					addPersonAgeMeasure(victimElement, ageMin, ageMax);
				}
				else{
					if (BooleanUtils.toBoolean(victim.getAgeFirstWeekIndicator()) ||
						BooleanUtils.toBoolean(victim.getAgeFirstYearIndicator()) || 
						BooleanUtils.toBoolean(victim.getAgeNeonateIndicator()) ||
						Objects.equals(victim.getNonNumericAge(), "00")){
						addPersonAgeMeasure(victimElement, victim.getNonNumericAge());
					}
				}
				
				addPersonInfo(victim.getEthnicityOfPersonType(), victim.getRaceOfPersonType(), victim.getResidentStatusOfPersonType(), 
						victim.getSexOfPersonType(), victimElement);
			}
		}
	}

	private void addPersonInfo(EthnicityOfPersonType ethnicityOfPersonType, RaceOfPersonType raceOfPersonType, 
			ResidentStatusOfPersonType residentStatusOfPersonType, SexOfPersonType sexOfPersonType,  
			Element parent) {
		XmlUtils.appendElementAndValue(parent, Namespace.J, "PersonEthnicityCode", 
			ethnicityOfPersonType.getNibrsCode());
		XmlUtils.appendElementAndValue(parent, Namespace.J, "PersonRaceNDExCode", 
			raceOfPersonType.getNibrsCode());
		if (residentStatusOfPersonType != null){
			XmlUtils.appendElementAndValue(parent, Namespace.J, "PersonResidentCode", 
				residentStatusOfPersonType.getNibrsCode());
		}
		XmlUtils.appendElementAndValue(parent, Namespace.J, "PersonSexCode", 
			sexOfPersonType.getNibrsCode());
	}

	private void addPersonAgeMeasure(Element victimElement, Integer ageMin, Integer ageMax) {
		Element e = XmlUtils.appendChildElement(victimElement, Namespace.NC, "PersonAgeMeasure");
		if ( ageMax == null || (ageMax != null && ageMin.equals(ageMax))) {
			XmlUtils.appendElementAndValue(e, Namespace.NC, "MeasureIntegerValue", String.valueOf(ageMin));
		} else {
			e = XmlUtils.appendChildElement(e, Namespace.NC, "MeasureIntegerRange");
			XmlUtils.appendElementAndValue(e, Namespace.NC, "RangeMaximumIntegerValue", String.valueOf(ageMax));
			XmlUtils.appendElementAndValue(e, Namespace.NC, "RangeMinimumIntegerValue", String.valueOf(ageMin));
		}
	}
	
	private void addPersonAgeMeasure(Element victimElement, String nonNumericAge) {
		
		if (appProperties.getNonNumericAgeCodeMapping().containsKey(nonNumericAge)){
			Element e = XmlUtils.appendChildElement(victimElement, Namespace.NC, "PersonAgeMeasure");
			XmlUtils.appendElementAndValue(e, Namespace.NC, "MeasureValueText", 
					appProperties.getNonNumericAgeCodeMapping().get(nonNumericAge));
		}
	}

	private void addOffenderPersonElements(AdministrativeSegment administrativeSegment, Element reportElement) {
		for (OffenderSegment offender : administrativeSegment.getOffenderSegments()) {
			if (offender.getOffenderSequenceNumber() == 0) continue; 
			
			Element offenderElement = XmlUtils.appendChildElement(reportElement, Namespace.NC, "Person");
			XmlUtils.addAttribute(offenderElement, Namespace.S, "id", "PersonOffender-" + offender.getOffenderSequenceNumber());
			Integer ageMin = offender.getAgeOfOffenderMin();
			Integer ageMax = offender.getAgeOfOffenderMax();
			if ( ageMin != null && ageMin > 0) {
				addPersonAgeMeasure(offenderElement, ageMin, ageMax);
			}
			else if (Objects.equals(offender.getNonNumericAge(), "00")){
				addPersonAgeMeasure(offenderElement, offender.getNonNumericAge());
			}
			
			addPersonInfo(offender.getEthnicityOfPersonType(), offender.getRaceOfPersonType(), null, 
					offender.getSexOfPersonType(), offenderElement);
		}
	}

	private void addArresteePersonElements(AdministrativeSegment administrativeSegment, Element reportElement) {
		for (ArresteeSegment arrestee : administrativeSegment.getArresteeSegments()) {
			Element arresteeElement = XmlUtils.appendChildElement(reportElement, Namespace.NC, "Person");
			XmlUtils.addAttribute(arresteeElement, Namespace.S, "id", "PersonArrestee-" + arrestee.getArresteeSequenceNumber());
			
			Integer ageMin = arrestee.getAgeOfArresteeMin();
			Integer ageMax = arrestee.getAgeOfArresteeMax();
			if ( ageMin != null && ageMin > 0) {
				addPersonAgeMeasure(arresteeElement, ageMin, ageMax);
			}
			else if (Objects.equals(arrestee.getNonNumericAge(), "00")){
				addPersonAgeMeasure(arresteeElement, arrestee.getNonNumericAge());
			}
			
			addPersonInfo(arrestee.getEthnicityOfPersonType(), arrestee.getRaceOfPersonType(), arrestee.getResidentStatusOfPersonType(), 
					arrestee.getSexOfPersonType(), arresteeElement);
		}
	}
	
//<j:EnforcementOfficial>
//	<nc:RoleOfPerson s:ref="PersonVictim1"/>
//	<!-- Element 25A, Type of Activity (Officer)/ Circumstance -->
//	<j:EnforcementOfficialActivityCategoryCode>10</j:EnforcementOfficialActivityCategoryCode>
//	<!-- Element 25B, Assignment Type (Officer) -->
//	<j:EnforcementOfficialAssignmentCategoryCode>G</j:EnforcementOfficialAssignmentCategoryCode>
//	<j:EnforcementOfficialUnit>
//		<j:OrganizationAugmentation>
//			<j:OrganizationORIIdentification>
//				<!-- Element 25C, ORI-Other Jurisdiction (Officer) -->
//				<nc:IdentificationID>WVNDX01</nc:IdentificationID>
//			</j:OrganizationORIIdentification>
//		</j:OrganizationAugmentation>
//	</j:EnforcementOfficialUnit>
//</j:EnforcementOfficial>
	private void addEnforcementOfficialElements(AdministrativeSegment administrativeSegment, Element reportElement) {
		for (VictimSegment victim : administrativeSegment.getVictimSegments()) {
			String victimType = victim.getTypeOfVictimType().getNibrsCode();
			if ("L".equals(victimType)) {
				Element enforcementOfficialElement = XmlUtils.appendChildElement(reportElement, Namespace.J, "EnforcementOfficial");
				Element e = XmlUtils.appendChildElement(enforcementOfficialElement, Namespace.NC, "RoleOfPerson");
				XmlUtils.addAttribute(e, Namespace.S, "ref", "PersonVictim-" + victim.getVictimSequenceNumber());
				XmlUtils.appendElementAndValue(enforcementOfficialElement, Namespace.J, "EnforcementOfficialActivityCategoryCode", 
						victim.getOfficerActivityCircumstanceType().getNibrsCode());
				XmlUtils.appendElementAndValue(enforcementOfficialElement, Namespace.J, "EnforcementOfficialAssignmentCategoryCode", 
						victim.getOfficerAssignmentTypeType().getNibrsCode());
				
				String officerOtherJurisdictionOri = victim.getOfficerOtherJurisdictionOri();
				if (officerOtherJurisdictionOri != null) {
					e = XmlUtils.appendChildElement(enforcementOfficialElement, Namespace.J, "EnforcementOfficialUnit");
					e = XmlUtils.appendChildElement(e, Namespace.J, "OrganizationAugmentation");
					e = XmlUtils.appendChildElement(e, Namespace.J, "OrganizationORIIdentification");
					XmlUtils.appendElementAndValue(e, Namespace.NC, "IdentificationID", officerOtherJurisdictionOri);
				}
			}
		}
	}

	private void addVictimElements(AdministrativeSegment administrativeSegment, Element reportElement) {
		for (VictimSegment victim : administrativeSegment.getVictimSegments()) {
			Element victimElement = XmlUtils.appendChildElement(reportElement, Namespace.J, "Victim");
			XmlUtils.addAttribute(victimElement, Namespace.S, "id", "Victim-" + victim.getVictimSequenceNumber());
			
			String victimType = victim.getTypeOfVictimType().getNibrsCode();
			if ("L".equals(victimType) || "I".equals(victimType)) {
				Element roleOfPerson = XmlUtils.appendChildElement(victimElement, Namespace.NC, "RoleOfPerson");
				XmlUtils.addAttribute(roleOfPerson, Namespace.S, "ref", "PersonVictim-" + victim.getVictimSequenceNumber());
			}
			XmlUtils.appendElementAndValue(victimElement, Namespace.J, "VictimSequenceNumberText", String.valueOf(victim.getVictimSequenceNumber()));
			
			for (TypeInjuryType typeInjuryType :victim.getTypeInjuryTypes()){
				Element victimInjury = XmlUtils.appendChildElement(victimElement, J, "VictimInjury");
				XmlUtils.appendElementAndValue(victimInjury, J, "InjuryCategoryCode", typeInjuryType.getNibrsCode());
			}
			
			XmlUtils.appendElementAndValue(victimElement, Namespace.J, "VictimCategoryCode", victim.getTypeOfVictimType().getNibrsCode());
			for (AggravatedAssaultHomicideCircumstancesType homicideCircumstancesType : 
				victim.getAggravatedAssaultHomicideCircumstancesTypes()) {
				XmlUtils.appendElementAndValue(victimElement, Namespace.J, "VictimAggravatedAssaultHomicideFactorCode", 
						homicideCircumstancesType.getNibrsCode());
			}
			
			XmlUtils.appendElementAndValue(victimElement, Namespace.J, "VictimJustifiableHomicideFactorCode", 
					Optional.ofNullable(victim.getAdditionalJustifiableHomicideCircumstancesType())
					.map(AdditionalJustifiableHomicideCircumstancesType::getNibrsCode)
					.orElse(null));
		}		
	}

	private void addSubjectElements(AdministrativeSegment administrativeSegment, Element reportElement) {
		for (OffenderSegment offender : administrativeSegment.getOffenderSegments()) {
			Element offenderElement = XmlUtils.appendChildElement(reportElement, Namespace.J, "Subject");
			XmlUtils.addAttribute(offenderElement, Namespace.S, "id", "Offender-" + offender.getOffenderSequenceNumber());
			
			String offenderSequenceNumber = String.valueOf(offender.getOffenderSequenceNumber()); 
			if (offender.getOffenderSequenceNumber() == 0) {
				offenderSequenceNumber = StringUtils.leftPad(offenderSequenceNumber, 2, "0");
			}
			else{
				Element roleOfPerson = XmlUtils.appendChildElement(offenderElement, Namespace.NC, "RoleOfPerson");
				XmlUtils.addAttribute(roleOfPerson, Namespace.S, "ref", "PersonOffender-" + offender.getOffenderSequenceNumber());
			}
			
			XmlUtils.appendElementAndValue(offenderElement, Namespace.J, "SubjectSequenceNumberText", offenderSequenceNumber);
		}
	}

//<j:Arrestee s:id="Arrestee1">
//	<nc:RoleOfPerson s:ref="PersonArrestee1"/>
//	<!-- Element 40, Arrestee Sequence Number -->
//	<j:ArrestSequenceID>1</j:ArrestSequenceID>
//	<!-- Element 46, Arrestee Was Armed With -->
//	<j:ArresteeArmedWithCode>12</j:ArresteeArmedWithCode>
//    <j:ArresteeArmedWithCode>13A</j:ArresteeArmedWithCode>
//	<!-- Element 52, Disposition of Arrestee Under 18 -->
//	<j:ArresteeJuvenileDispositionCode>H</j:ArresteeJuvenileDispositionCode>
//	<!-- Element 44, Multiple Arrestee Segments Indicator -->
//	<j:ArrestSubjectCountCode>N</j:ArrestSubjectCountCode>
//</j:Arrestee>
	private void addArresteeElements(AdministrativeSegment administrativeSegment, Element reportElement) {
		for (ArresteeSegment arrestee : administrativeSegment.getArresteeSegments()) {
			
			Set<String> arresteeArmedWithTypeCodes = new HashSet<>(); 
			
			if (arrestee.getArresteeSegmentWasArmedWiths() != null) {
				arresteeArmedWithTypeCodes = arrestee.getArresteeSegmentWasArmedWiths().stream()
					.map(item -> item.getArresteeWasArmedWithType().getNibrsCode() + StringUtils.trimToEmpty(item.getAutomaticWeaponIndicator()))
					.collect(Collectors.toSet());
			}
			addArresteeElement(reportElement, arrestee.getArresteeSequenceNumber(), arresteeArmedWithTypeCodes, 
					arrestee.getDispositionOfArresteeUnder18Type(), arrestee.getMultipleArresteeSegmentsIndicatorType() );
		}
	}

	private void addArresteeElement(Element reportElement, Integer arresteeSequenceNumber, Set<String> arresteeArmedWithTypeCodes, 
			DispositionOfArresteeUnder18Type dispositionOfArresteeUnder18Type, MultipleArresteeSegmentsIndicatorType multipleArresteeSegmentsIndicatorType ) {
		Element arresteeElement = XmlUtils.appendChildElement(reportElement, Namespace.J, "Arrestee");
		XmlUtils.addAttribute(arresteeElement, Namespace.S, "id", "Arrestee-" + arresteeSequenceNumber);
		Element e = XmlUtils.appendChildElement(arresteeElement, Namespace.NC, "RoleOfPerson");
		XmlUtils.addAttribute(e, Namespace.S, "ref", "PersonArrestee-" + arresteeSequenceNumber);
		XmlUtils.appendElementAndValue(arresteeElement, Namespace.J, "ArrestSequenceID", String.valueOf(arresteeSequenceNumber));
		
		for (String armedWithCode: arresteeArmedWithTypeCodes){
			XmlUtils.appendElementAndValue(arresteeElement, J, "ArresteeArmedWithCode", armedWithCode);
		}
		
		if (dispositionOfArresteeUnder18Type!= null){
			XmlUtils.appendElementAndValue(arresteeElement, J, "ArresteeJuvenileDispositionCode", 
					dispositionOfArresteeUnder18Type.getNibrsCode());
		}
		
		if (multipleArresteeSegmentsIndicatorType != null){
			XmlUtils.appendElementAndValue(arresteeElement, J, "ArrestSubjectCountCode", 
					multipleArresteeSegmentsIndicatorType.getNibrsCode());
		}
	}
	
//<j:Arrest s:id="Arrest1">
//	<!-- Element 41, Arrest Transaction Number -->
//	<nc:ActivityIdentification>
//		<nc:IdentificationID>12345</nc:IdentificationID>
//	</nc:ActivityIdentification>
//	<!-- Element 42, Arrest Date -->
//	<nc:ActivityDate>
//		<nc:Date>2016-02-28</nc:Date>
//	</nc:ActivityDate>
//	<!-- Element 45, UCR Arrest Offense Code -->
//	<j:ArrestCharge>
//		<nibrs:ChargeUCRCode>64A</nibrs:ChargeUCRCode>
//	</j:ArrestCharge>
//	<!-- Element 43, Type Of Arrest -->
//	<j:ArrestCategoryCode>O</j:ArrestCategoryCode>
//</j:Arrest>
	private void addArrestElements(AdministrativeSegment administrativeSegment, Element reportElement) {
		for (ArresteeSegment arrestee : administrativeSegment.getArresteeSegments()) {
			addArrestElement(reportElement, arrestee.getArresteeSequenceNumber(), 
					arrestee.getArrestTransactionNumber(), 
					arrestee.getArrestDate(), 
					arrestee.getUcrOffenseCodeType(), 
					arrestee.getTypeOfArrestType());
		}		
	}

	private void addArrestElement(Element reportElement, Integer arresteeSequenceNumber, String arrestTransactionNumber, 
			LocalDate arrestDate, UcrOffenseCodeType ucrOffenseCodeType, TypeOfArrestType typeOfArrestType) {
		Element arrestElement = XmlUtils.appendChildElement(reportElement, Namespace.J, "Arrest");
		XmlUtils.addAttribute(arrestElement, Namespace.S, "id", "Arrest-" + arresteeSequenceNumber);
		appendIdentificationIdElement(arrestElement, NC, "ActivityIdentification", arrestTransactionNumber);
		
		if (arrestDate != null) {
			Element activityDate = XmlUtils.appendChildElement(arrestElement, Namespace.NC, "ActivityDate");
			XmlUtils.appendElementAndValue(activityDate, NC, "Date", arrestDate.toString());
		}
		
		Element arrestCharge = XmlUtils.appendChildElement(arrestElement, Namespace.J, "ArrestCharge");
		XmlUtils.appendElementAndValue(arrestCharge, Namespace.NIBRS, "ChargeUCRCode", ucrOffenseCodeType.getNibrsCode());
		XmlUtils.appendElementAndValue(arrestElement, Namespace.J, "ArrestCategoryCode", typeOfArrestType.getNibrsCode());
	}
	
//<j:ArrestSubjectAssociation>
//	<nc:Activity s:ref="Arrest1"/>
//	<j:Subject s:ref="Arrestee1"/>
//</j:ArrestSubjectAssociation>
	private void addArrestSubjectAssociationElements(AdministrativeSegment administrativeSegment, Element reportElement) {
		for (ArresteeSegment arrestee : administrativeSegment.getArresteeSegments()) {
			addArrestSubjectAssociationElement(reportElement, arrestee.getArresteeSequenceNumber());
		}
	}

	private void addArrestSubjectAssociationElement(Element reportElement, Integer arresteeSequenceNumber) {
		Element associationElement = XmlUtils.appendChildElement(reportElement, Namespace.J, "ArrestSubjectAssociation");
		Element e = XmlUtils.appendChildElement(associationElement, Namespace.NC, "Activity");
		XmlUtils.addAttribute(e, Namespace.S, "ref", "Arrest-" + arresteeSequenceNumber);
		e = XmlUtils.appendChildElement(associationElement, Namespace.J, "Subject");
		XmlUtils.addAttribute(e, Namespace.S, "ref", "Arrestee-" + arresteeSequenceNumber);
	}
	
//<j:OffenseLocationAssociation>
//	<j:Offense s:ref="Offense1"/>
//	<nc:Location s:ref="Location1"/>
//</j:OffenseLocationAssociation>
	private void addOffenseLocationAssociationElements(AdministrativeSegment administrativeSegment, Element reportElement) {
		for (OffenseSegment offense : administrativeSegment.getOffenseSegments()) {
			Element associationElement = XmlUtils.appendChildElement(reportElement, J, "OffenseLocationAssociation");
			Element e = XmlUtils.appendChildElement(associationElement, J, "Offense");
			XmlUtils.addAttribute(e, Namespace.S, "ref", "Offense-" + offense.getUcrOffenseCodeType().getNibrsCode());
			e = XmlUtils.appendChildElement(associationElement, Namespace.NC, "Location");
			XmlUtils.addAttribute(e, Namespace.S, "ref", "Location-" + offense.getUcrOffenseCodeType().getNibrsCode());
		}
	}

//<j:OffenseVictimAssociation>
//	<j:Offense s:ref="Offense1"/>
//	<j:Victim s:ref="Victim1"/>
//</j:OffenseVictimAssociation>
	private void addOffenseVictimAssociationElements(AdministrativeSegment administrativeSegment, Element reportElement) {
		for (VictimSegment victim : administrativeSegment.getVictimSegments()) {
			for (OffenseSegment offense:victim.getOffenseSegments()) {
				String ucrCode = offense.getUcrOffenseCodeType().getNibrsCode();
				Element associationElement = XmlUtils.appendChildElement(reportElement, Namespace.J, "OffenseVictimAssociation");
				Element e = XmlUtils.appendChildElement(associationElement, Namespace.J, "Offense");
				XmlUtils.addAttribute(e, Namespace.S, "ref", "Offense-" + ucrCode);
				e = XmlUtils.appendChildElement(associationElement, Namespace.J, "Victim");
				XmlUtils.addAttribute(e, Namespace.S, "ref", "Victim-" + victim.getVictimSequenceNumber());
			}
		}
	}
	
//<j:SubjectVictimAssociation s:id="SubjectVictimAssocSP1">
//	<!-- Element 35, Relationship(s) of Victim To Offender -->
//	<j:Subject s:ref="Subject1"/>
//	<j:Victim s:ref="Victim1"/>
//	<nibrs:VictimToSubjectRelationshipCode>Acquaintance</nibrs:VictimToSubjectRelationshipCode>
//</j:SubjectVictimAssociation>
	private void addSubjectVictimAssociationElements(AdministrativeSegment administrativeSegment, Element reportElement) {
		for (VictimSegment victim : administrativeSegment.getVictimSegments()) {
			for (VictimOffenderAssociation victimOffenderAssociation: victim.getVictimOffenderAssociations()) {
				Integer offenderSequenceNumber = victimOffenderAssociation.getOffenderSegment().getOffenderSequenceNumber();
				String relationshipTypeCode = victimOffenderAssociation.getVictimOffenderRelationshipType().getNibrsCode();
				
				Element associationElement = XmlUtils.appendChildElement(reportElement, Namespace.J, "SubjectVictimAssociation");
				Element e = XmlUtils.appendChildElement(associationElement, Namespace.J, "Subject");
				XmlUtils.addAttribute(e, Namespace.S, "ref", "Offender-" + offenderSequenceNumber);
				e = XmlUtils.appendChildElement(associationElement, Namespace.J, "Victim");
				XmlUtils.addAttribute(e, Namespace.S, "ref", "Victim-" + victim.getVictimSequenceNumber());
				
				if (StringUtils.isNotBlank(relationshipTypeCode)) {
					XmlUtils.appendElementAndValue(associationElement, NIBRS, "VictimToSubjectRelationshipCode", 
							RelationshipOfVictimToOffenderCode.valueOf(relationshipTypeCode).iepdCode);
				}
			}
		}
	}
	
}
