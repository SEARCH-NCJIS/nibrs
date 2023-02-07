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
package org.search.nibrs.stagingdata.service;

import static org.search.nibrs.stagingdata.util.ObjectUtils.getInteger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.search.nibrs.model.ArresteeSegment;
import org.search.nibrs.model.GroupBArrestReport;
import org.search.nibrs.model.reports.SummaryReportRequest;
import org.search.nibrs.stagingdata.controller.BadRequestException;
import org.search.nibrs.stagingdata.model.Agency;
import org.search.nibrs.stagingdata.model.AgencyType;
import org.search.nibrs.stagingdata.model.ArrestReportSegmentWasArmedWith;
import org.search.nibrs.stagingdata.model.ArresteeWasArmedWithType;
import org.search.nibrs.stagingdata.model.DispositionOfArresteeUnder18Type;
import org.search.nibrs.stagingdata.model.EthnicityOfPersonType;
import org.search.nibrs.stagingdata.model.Owner;
import org.search.nibrs.stagingdata.model.RaceOfPersonType;
import org.search.nibrs.stagingdata.model.ResidentStatusOfPersonType;
import org.search.nibrs.stagingdata.model.SegmentActionTypeType;
import org.search.nibrs.stagingdata.model.SexOfPersonType;
import org.search.nibrs.stagingdata.model.TypeOfArrestType;
import org.search.nibrs.stagingdata.model.UcrOffenseCodeType;
import org.search.nibrs.stagingdata.model.search.IncidentDeleteRequest;
import org.search.nibrs.stagingdata.model.search.IncidentPointer;
import org.search.nibrs.stagingdata.model.search.IncidentSearchRequest;
import org.search.nibrs.stagingdata.model.segment.ArrestReportSegment;
import org.search.nibrs.stagingdata.repository.AdditionalJustifiableHomicideCircumstancesTypeRepository;
import org.search.nibrs.stagingdata.repository.AgencyRepository;
import org.search.nibrs.stagingdata.repository.AggravatedAssaultHomicideCircumstancesTypeRepository;
import org.search.nibrs.stagingdata.repository.ArresteeWasArmedWithTypeRepository;
import org.search.nibrs.stagingdata.repository.BiasMotivationTypeRepository;
import org.search.nibrs.stagingdata.repository.ClearedExceptionallyTypeRepository;
import org.search.nibrs.stagingdata.repository.DispositionOfArresteeUnder18TypeRepository;
import org.search.nibrs.stagingdata.repository.EthnicityOfPersonTypeRepository;
import org.search.nibrs.stagingdata.repository.LocationTypeRepository;
import org.search.nibrs.stagingdata.repository.MethodOfEntryTypeRepository;
import org.search.nibrs.stagingdata.repository.MultipleArresteeSegmentsIndicatorTypeRepository;
import org.search.nibrs.stagingdata.repository.OffenderSuspectedOfUsingTypeRepository;
import org.search.nibrs.stagingdata.repository.OfficerActivityCircumstanceTypeRepository;
import org.search.nibrs.stagingdata.repository.OfficerAssignmentTypeTypeRepository;
import org.search.nibrs.stagingdata.repository.PropertyDescriptionTypeRepository;
import org.search.nibrs.stagingdata.repository.RaceOfPersonTypeRepository;
import org.search.nibrs.stagingdata.repository.ResidentStatusOfPersonTypeRepository;
import org.search.nibrs.stagingdata.repository.SegmentActionTypeRepository;
import org.search.nibrs.stagingdata.repository.SexOfPersonTypeRepository;
import org.search.nibrs.stagingdata.repository.SuspectedDrugTypeTypeRepository;
import org.search.nibrs.stagingdata.repository.TypeDrugMeasurementTypeRepository;
import org.search.nibrs.stagingdata.repository.TypeInjuryTypeRepository;
import org.search.nibrs.stagingdata.repository.TypeOfArrestTypeRepository;
import org.search.nibrs.stagingdata.repository.TypeOfCriminalActivityTypeRepository;
import org.search.nibrs.stagingdata.repository.TypeOfVictimTypeRepository;
import org.search.nibrs.stagingdata.repository.TypeOfWeaponForceInvolvedTypeRepository;
import org.search.nibrs.stagingdata.repository.TypePropertyLossEtcTypeRepository;
import org.search.nibrs.stagingdata.repository.UcrOffenseCodeTypeRepository;
import org.search.nibrs.stagingdata.repository.VictimOffenderRelationshipTypeRepository;
import org.search.nibrs.stagingdata.repository.segment.ArrestReportSegmentRepository;
import org.search.nibrs.stagingdata.repository.segment.ArrestReportSegmentRepositoryCustom;
import org.search.nibrs.stagingdata.service.xml.XmlReportGenerator;
import org.search.nibrs.stagingdata.util.DateUtils;
import org.search.nibrs.util.CustomPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service to process Group B Arrest Report.  
 *
 */
@Service
public class ArrestReportService {
	private static final String BAD_DELETE_REQUEST = "The report action type should be 'D' and the arrest transaction number is required ";

	private static final String BAD_SAVE_REQUEST = "The Group B Report is not persisted or converted because it misses the arrestee info. ";

	private static final Log log = LogFactory.getLog(ArrestReportService.class);

	@Autowired
	ArrestReportSegmentRepository arrestReportSegmentRepository;
	@Autowired
	public AgencyRepository agencyRepository; 
	@Autowired
	public SegmentActionTypeRepository segmentActionTypeRepository; 
	@Autowired
	public ClearedExceptionallyTypeRepository clearedExceptionallyTypeRepository; 
	@Autowired
	public UcrOffenseCodeTypeRepository ucrOffenseCodeTypeRepository; 
	@Autowired
	public LocationTypeRepository locationTypeRepository; 
	@Autowired
	public MethodOfEntryTypeRepository methodOfEntryTypeRepository; 
	@Autowired
	public BiasMotivationTypeRepository biasMotivationTypeRepository; 
	@Autowired
	public TypeOfWeaponForceInvolvedTypeRepository typeOfWeaponForceInvolvedTypeRepository; 
	@Autowired
	public OffenderSuspectedOfUsingTypeRepository offenderSuspectedOfUsingTypeRepository; 
	@Autowired
	public TypeOfCriminalActivityTypeRepository typeOfCriminalActivityTypeRepository; 
	@Autowired
	public TypePropertyLossEtcTypeRepository typePropertyLossEtcTypeRepository; 
	@Autowired
	public TypeDrugMeasurementTypeRepository typeDrugMeasurementTypeRepository; 
	@Autowired
	public PropertyDescriptionTypeRepository propertyDescriptionTypeRepository; 
	@Autowired
	public SuspectedDrugTypeTypeRepository suspectedDrugTypeTypeRepository; 
	@Autowired
	public DispositionOfArresteeUnder18TypeRepository dispositionOfArresteeUnder18TypeRepository; 
	@Autowired
	public EthnicityOfPersonTypeRepository ethnicityOfPersonTypeRepository; 
	@Autowired
	public RaceOfPersonTypeRepository raceOfPersonTypeRepository; 
	@Autowired
	public SexOfPersonTypeRepository sexOfPersonTypeRepository; 
	@Autowired
	public TypeOfArrestTypeRepository typeOfArrestTypeRepository; 
	@Autowired
	public ResidentStatusOfPersonTypeRepository residentStatusOfPersonTypeRepository; 
	@Autowired
	public MultipleArresteeSegmentsIndicatorTypeRepository multipleArresteeSegmentsIndicatorTypeRepository; 
	@Autowired
	public ArresteeWasArmedWithTypeRepository arresteeWasArmedWithTypeRepository; 
	@Autowired
	public TypeOfVictimTypeRepository typeOfVictimTypeRepository; 
	@Autowired
	public OfficerActivityCircumstanceTypeRepository officerActivityCircumstanceTypeRepository; 
	@Autowired
	public OfficerAssignmentTypeTypeRepository officerAssignmentTypeTypeRepository; 
	@Autowired
	public AdditionalJustifiableHomicideCircumstancesTypeRepository additionalJustifiableHomicideCircumstancesTypeRepository; 
	@Autowired
	public TypeInjuryTypeRepository typeInjuryTypeRepository; 
	@Autowired
	public AggravatedAssaultHomicideCircumstancesTypeRepository aggravatedAssaultHomicideCircumstancesTypeRepository; 
	@Autowired
	public VictimOffenderRelationshipTypeRepository victimOffenderRelationshipTypeRepository; 
	@Autowired
	ArrestReportSegmentRepositoryCustom arrestReportSegmentRepositoryCustom;
	@Autowired
	public CodeTableService codeTableService; 
	@Autowired
	public XmlReportGenerator xmlReportGenerator; 
	
	@Transactional
	public ArrestReportSegment saveArrestReportSegment(ArrestReportSegment arrestReportSegment){
		return arrestReportSegmentRepository.save(arrestReportSegment);
	}
	
	public List<IncidentPointer> findAllByCriteria(IncidentSearchRequest incidentSearchRequest){
		return arrestReportSegmentRepositoryCustom.findAllByCriteria(incidentSearchRequest);
	}
	
	public long countAllByCriteria(IncidentSearchRequest incidentSearchRequest){
		return arrestReportSegmentRepositoryCustom.countAllByCriteria(incidentSearchRequest);
	}
	
	public ArrestReportSegment findArrestReportSegment(Integer id){
		return arrestReportSegmentRepository.findByArrestReportSegmentId(id);
	}
	
	public List<ArrestReportSegment> findAllArrestReportSegment(){
		List<ArrestReportSegment> arrestReportSegments = new ArrayList<>();
		arrestReportSegmentRepository.findAll().forEach(arrestReportSegments::add);
		return arrestReportSegments;
	}
	
	public List<ArrestReportSegment> findArrestReportSegmentByOriAndArrestDate(String ori, Integer arrestYear, Integer arrestMonth, String ownerId){
		if ("StateWide".equalsIgnoreCase(ori)){
			ori = null;
		}
		List<Integer> ids = arrestReportSegmentRepository.findIdsByOriAndArrestDate(ori, arrestYear, arrestMonth, getInteger(ownerId));
		
		List<ArrestReportSegment> arrestReportSegments = 
				arrestReportSegmentRepository.findAllById(ids)
				.stream().distinct().collect(Collectors.toList());
		
		log.debug("ids size" + ids.size());
		log.debug("arrestReportSegments size" + arrestReportSegments.size());
		
		return arrestReportSegments; 
		
	}
	
	
	public List<ArrestReportSegment> findArrestReportSegmentByRequest(SummaryReportRequest summaryReportRequest){
		List<Integer> ids = arrestReportSegmentRepository.findIdsByStateAndAgencyAndArrestDate(
				summaryReportRequest.getStateCode(), 
				summaryReportRequest.getAgencyId(), 
				summaryReportRequest.getIncidentYear(), 
				summaryReportRequest.getIncidentMonth(), 
				summaryReportRequest.getOwnerId());
		
		List<ArrestReportSegment> arrestReportSegments = 
				arrestReportSegmentRepository.findAllById(ids)
				.stream().distinct().collect(Collectors.toList());
		
		log.debug("ids size" + ids.size());
		log.debug("arrestReportSegments size" + arrestReportSegments.size());
		
		return arrestReportSegments; 
		
	}
	
	
	public long deleteGroupBArrestReport(String identifier){
		if ( StringUtils.isBlank(identifier) ){
			log.error(BAD_DELETE_REQUEST); 
			throw new BadRequestException(BAD_DELETE_REQUEST);
		}
		return arrestReportSegmentRepository.deleteByArrestTransactionNumber(identifier);
	}
	
	public Iterable<ArrestReportSegment> saveGroupBArrestReports(List<GroupBArrestReport> groupBArrestReports){
		
		List<ArrestReportSegment> arrestReportSegments = getArrestReportSegments(true, groupBArrestReports);
		log.info("Persisting " + arrestReportSegments.size() + " Group B Arrest Reports." ); 
		
		return arrestReportSegmentRepository.saveAll(arrestReportSegments);
	}

	private List<ArrestReportSegment> getArrestReportSegments(Boolean isToPersist, List<GroupBArrestReport> groupBArrestReports) {
		List<ArrestReportSegment> arrestReportSegments = new ArrayList<>(); 
		
		for(GroupBArrestReport groupBArrestReport : groupBArrestReports){
			ArresteeSegment arrestee = groupBArrestReport.getArrestee(); 
			if (arrestee == null || StringUtils.isBlank(groupBArrestReport.getIdentifier())){
				log.error(BAD_SAVE_REQUEST); 
//				throw new BadRequestException(BAD_SAVE_REQUEST);
				continue;
			}
			
			ArrestReportSegment arrestReportSegment = new ArrestReportSegment();
			
			if (groupBArrestReport.getOwnerId() != null) {
				Owner owner = new Owner(groupBArrestReport.getOwnerId());
				arrestReportSegment.setOwner(owner);
			}

			arrestReportSegment.setArrestTransactionNumber(groupBArrestReport.getIdentifier());
			arrestReportSegment.setAgency(agencyRepository.findFirstByAgencyOri(groupBArrestReport.getOri()));
			arrestReportSegment.setOri(groupBArrestReport.getOri());
			
			if (groupBArrestReport.getYearOfTape() != null && groupBArrestReport.getMonthOfTape() != null && isToPersist) {
				boolean havingNewerSubmission = arrestReportSegmentRepository.existsByArrestTransactionNumberAndOriAndSubmissionDateAndOwnerId
						(arrestReportSegment.getArrestTransactionNumber(), arrestReportSegment.getOri(), 
								DateUtils.getStartDate(groupBArrestReport.getYearOfTape(), 
										groupBArrestReport.getMonthOfTape()), groupBArrestReport.getOwnerId());
				
				if (havingNewerSubmission) {
					continue;
				}
			}
			
			String reportActionType = String.valueOf(groupBArrestReport.getReportActionType()).trim();
			if (!Objects.equals("D", reportActionType) && !Objects.equals("R", reportActionType)
					&& groupBArrestReport.getOwnerId() == null){
				if (arrestReportSegmentRepository
						.existsByArrestTransactionNumberAndOri(groupBArrestReport.getIdentifier(), groupBArrestReport.getOri())){
					reportActionType = "R"; 
				}
			}

			SegmentActionTypeType segmentActionType = codeTableService.getCodeTableType(reportActionType, 
					segmentActionTypeRepository::findFirstByStateCode, SegmentActionTypeType::new);
			arrestReportSegment.setSegmentActionType(segmentActionType);
			
			Optional<Integer> monthOfTape = Optional.ofNullable(groupBArrestReport.getMonthOfTape());
			monthOfTape.ifPresent( m-> {
				arrestReportSegment.setMonthOfTape(StringUtils.leftPad(String.valueOf(m), 2, '0'));
			});
			
			if (groupBArrestReport.getYearOfTape() != null){
				arrestReportSegment.setYearOfTape(String.valueOf(groupBArrestReport.getYearOfTape()));
			}
			
			arrestReportSegment.setCityIndicator(groupBArrestReport.getCityIndicator());
			arrestReportSegment.setStateCode(StringUtils.substring(groupBArrestReport.getOri(), 0, 2));
			Agency agency = codeTableService.getCodeTableType(groupBArrestReport.getOri(), 
					agencyRepository::findFirstByAgencyOri, Agency::new); 
			
			if (StringUtils.isNotBlank(groupBArrestReport.getOri()) && agency.getAgencyId() == 99998) {
				agency.setAgencyId(null);
				agency.setAgencyOri(groupBArrestReport.getOri());
				agency.setAgencyName(groupBArrestReport.getOri());
				agency.setAgencyType(new AgencyType(99999));
				agency.setStateCode(StringUtils.substring(groupBArrestReport.getOri(), 0, 2));
				agency.setStateName(StringUtils.substring(groupBArrestReport.getOri(), 0, 2));
				
				agency = agencyRepository.saveAndFlush(agency);
			}

			arrestReportSegment.setAgency(agency);
	
			arrestReportSegment.setArresteeSequenceNumber(groupBArrestReport.getArresteeSequenceNumber());
			
			arrestReportSegment.setArrestDate(groupBArrestReport.getArrestDate());
			arrestReportSegment.setArrestDateType(codeTableService.getDateType(DateUtils.asDate(groupBArrestReport.getArrestDate())));
			
			TypeOfArrestType typeOfArrestType = codeTableService.getCodeTableType(
					arrestee.getTypeOfArrest(), typeOfArrestTypeRepository::findFirstByStateCode, TypeOfArrestType::new);
			arrestReportSegment.setTypeOfArrestType(typeOfArrestType );
			
			if (arrestee.getAge() != null) {
				arrestReportSegment.setAgeOfArresteeMin(arrestee.getAge().getAgeMin());
				arrestReportSegment.setAgeOfArresteeMax(arrestee.getAge().getAgeMax());
				arrestReportSegment.setAgeNumArrestee(arrestee.getAge().getAverage());
				arrestReportSegment.setNonNumericAge(arrestee.getAge().getNonNumericAge());
			}
	
			SexOfPersonType sexOfPersonType = codeTableService.getCodeTableType(
					arrestee.getSex(), sexOfPersonTypeRepository::findFirstByStateCode, SexOfPersonType::new);
			arrestReportSegment.setSexOfPersonType(sexOfPersonType);
			
			RaceOfPersonType raceOfPersonType = codeTableService.getCodeTableType(
					arrestee.getRace(), raceOfPersonTypeRepository::findFirstByStateCode, RaceOfPersonType::new);
			arrestReportSegment.setRaceOfPersonType(raceOfPersonType);
			
			EthnicityOfPersonType ethnicityOfPersonType = codeTableService.getCodeTableType(
					arrestee.getEthnicity(), ethnicityOfPersonTypeRepository::findFirstByStateCode, EthnicityOfPersonType::new);
			arrestReportSegment.setEthnicityOfPersonType(ethnicityOfPersonType);
			
			ResidentStatusOfPersonType residentStatusOfPersonType = codeTableService.getCodeTableType(
					arrestee.getResidentStatus(), 
					residentStatusOfPersonTypeRepository::findFirstByStateCode, 
					ResidentStatusOfPersonType::new);
			arrestReportSegment.setResidentStatusOfPersonType(residentStatusOfPersonType);
			
			DispositionOfArresteeUnder18Type dispositionOfArresteeUnder18Type = codeTableService.getCodeTableType(
					arrestee.getDispositionOfArresteeUnder18(), 
					dispositionOfArresteeUnder18TypeRepository::findFirstByStateCode, 
					DispositionOfArresteeUnder18Type::new);
			arrestReportSegment.setDispositionOfArresteeUnder18Type(dispositionOfArresteeUnder18Type );
			
			UcrOffenseCodeType ucrOffenseCodeType = codeTableService.getCodeTableType(
					arrestee.getUcrArrestOffenseCode(), 
					ucrOffenseCodeTypeRepository::findFirstByStateCode, 
					UcrOffenseCodeType::new);;
			arrestReportSegment.setUcrOffenseCodeType(ucrOffenseCodeType);
			arrestReportSegment.setReportTimestamp(LocalDateTime.now());
			
			processArrestReportSegmentArmedWiths(arrestReportSegment, arrestee);
			
			arrestReportSegments.add(arrestReportSegment);
		}
		return arrestReportSegments;
	}

	private void processArrestReportSegmentArmedWiths(ArrestReportSegment arrestReportSegment, ArresteeSegment arrestee) {
		
		Set<ArrestReportSegmentWasArmedWith> armedWiths = Optional.ofNullable(arrestReportSegment.getArrestReportSegmentWasArmedWiths())
				.orElseGet(HashSet::new);
		armedWiths.clear();
		if (arrestee.containsArresteeArmedWith()){
			for (int i = 0; i < ArresteeSegment.ARRESTEE_ARMED_WITH_COUNT; i++){
				String arresteeArmedWithCode = 
						StringUtils.trimToNull(arrestee.getArresteeArmedWith(i));
				String automaticWeaponIndicator = 
						StringUtils.trimToEmpty(arrestee.getAutomaticWeaponIndicator(i));
				
				if (StringUtils.isNotBlank(arresteeArmedWithCode)){
					Optional<ArresteeWasArmedWithType> arresteeWasArmedWithType = 
							Optional.ofNullable(codeTableService.getCodeTableType(arresteeArmedWithCode,
									arresteeWasArmedWithTypeRepository::findFirstByStateCode, 
									null));
					arresteeWasArmedWithType.ifPresent( type ->
						armedWiths.add(new ArrestReportSegmentWasArmedWith(
								null, arrestReportSegment, type, automaticWeaponIndicator))
					);
				}
			}
		}
		
		if (!armedWiths.isEmpty()){
			arrestReportSegment.setArrestReportSegmentWasArmedWiths(armedWiths);
		}
	}
	
	@Transactional
	public int deleteByOriAndSubmissionDate(String ori, String yearOfTape, String monthOfTape) {
		return arrestReportSegmentRepositoryCustom.deleteByOriAndSubmissionDate(ori, yearOfTape, monthOfTape);
	}
	
	@Transactional
	public int deleteIncidentDeleteRequest(IncidentDeleteRequest incidentDeleteRequest) {
		return arrestReportSegmentRepositoryCustom.deleteByIncidentDeleteRequest(incidentDeleteRequest);
	}

	public void convertAndWriteGroupBArrestReports(CustomPair<String, List<GroupBArrestReport>> groupBArrestReportsPair) {
		List<ArrestReportSegment> arrestReportSegments = 
				getArrestReportSegments(false, groupBArrestReportsPair.getValue());
		for (ArrestReportSegment arrestReportSegment: arrestReportSegments) {
			arrestReportSegment.setArrestReportSegmentId(222222); 
			xmlReportGenerator.writeArrestReportSegmentToXml(arrestReportSegment, groupBArrestReportsPair.getKey());
		}
		
	}
}
