
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

package org.search.nibrs.stagingdata.service.summary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.search.nibrs.model.codes.LocationTypeCode;
import org.search.nibrs.model.codes.OffenseCode;
import org.search.nibrs.model.codes.PropertyDescriptionCode;
import org.search.nibrs.model.codes.TypeOfPropertyLossCode;
import org.search.nibrs.model.reports.PropertyStolenByClassification;
import org.search.nibrs.model.reports.PropertyStolenByClassificationRowName;
import org.search.nibrs.model.reports.PropertyTypeValueRowName;
import org.search.nibrs.model.reports.ReturnAForm;
import org.search.nibrs.model.reports.ReturnAFormRow;
import org.search.nibrs.model.reports.ReturnARecordCard;
import org.search.nibrs.model.reports.ReturnARecordCardReport;
import org.search.nibrs.model.reports.ReturnARecordCardRow;
import org.search.nibrs.model.reports.ReturnARecordCardRowName;
import org.search.nibrs.model.reports.ReturnARowName;
import org.search.nibrs.model.reports.SummaryReportRequest;
import org.search.nibrs.stagingdata.AppProperties;
import org.search.nibrs.stagingdata.model.Agency;
import org.search.nibrs.stagingdata.model.PropertyType;
import org.search.nibrs.stagingdata.model.TypeOfWeaponForceInvolved;
import org.search.nibrs.stagingdata.model.TypeOfWeaponForceInvolvedType;
import org.search.nibrs.stagingdata.model.segment.AdministrativeSegment;
import org.search.nibrs.stagingdata.model.segment.OffenseSegment;
import org.search.nibrs.stagingdata.model.segment.PropertySegment;
import org.search.nibrs.stagingdata.model.segment.VictimSegment;
import org.search.nibrs.stagingdata.repository.AgencyRepository;
import org.search.nibrs.stagingdata.repository.segment.AdministrativeSegmentRepository;
import org.search.nibrs.stagingdata.service.AdministrativeSegmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReturnAFormService {

	private final Log log = LogFactory.getLog(this.getClass());
	@Autowired
	AdministrativeSegmentService administrativeSegmentService;
	@Autowired
	AdministrativeSegmentRepository administrativeSegmentRepository;
	@Autowired
	public AgencyRepository agencyRepository; 
	@Autowired
	public HumanTraffickingFormService humanTraffickingFormService; 
	@Autowired
	public AppProperties appProperties; 
	
	private Map<String, Integer> partIOffensesMap; 
	private Map<String, Integer> larcenyOffenseImportanceMap; 
	private Map<String, PropertyStolenByClassificationRowName> larcenyOffenseByNatureMap; 

//	private List<Integer> propertyTypeIds; 
//	private List<String> incidentNumbers; 
	public ReturnAFormService() {
		partIOffensesMap = new HashMap<>();
		partIOffensesMap.put("09A", 1); 
		partIOffensesMap.put("09B", 2); 
		partIOffensesMap.put("11A", 3); 
		partIOffensesMap.put("11B", 4); 
		partIOffensesMap.put("11C", 5); 
		partIOffensesMap.put("120", 6); 
		partIOffensesMap.put("13A", 7); 
		partIOffensesMap.put("13B", 8); 
		partIOffensesMap.put("13C", 9); 
		partIOffensesMap.put("220", 10); 
		partIOffensesMap.put("23B", 11); 
		partIOffensesMap.put("23A", 11); 
		partIOffensesMap.put("23C", 11); 
		partIOffensesMap.put("23D", 11); 
		partIOffensesMap.put("23E", 11); 
		partIOffensesMap.put("23F", 11); 
		partIOffensesMap.put("23G", 11); 
		partIOffensesMap.put("23H", 11); 
		partIOffensesMap.put("240", 12); 
				
		larcenyOffenseByNatureMap = new HashMap<>();
		larcenyOffenseByNatureMap.put("23B", PropertyStolenByClassificationRowName.LARCENY_PURSE_SNATCHING);  // Purse-snatching
		larcenyOffenseByNatureMap.put("23A", PropertyStolenByClassificationRowName.LARCENY_POCKET_PICKING);  // Pocket-picking
		larcenyOffenseByNatureMap.put("23C", PropertyStolenByClassificationRowName.LARCENY_SHOPLIFTING); // Shoplifting
		larcenyOffenseByNatureMap.put("23D", PropertyStolenByClassificationRowName.LARCENY_FROM_BUILDING); // Theft from building
		larcenyOffenseByNatureMap.put("23G", PropertyStolenByClassificationRowName.LARCENY_MOTOR_VEHICLE_PARTS_AND_ACCESSORIES);  // Theft of Motor Vehicle Parts or Accessories
		larcenyOffenseByNatureMap.put("23H38", PropertyStolenByClassificationRowName.LARCENY_MOTOR_VEHICLE_PARTS_AND_ACCESSORIES); // Theft of Motor Vehicle Parts or Accessories
		larcenyOffenseByNatureMap.put("23F", PropertyStolenByClassificationRowName.LARCENY_FROM_MOTOR_VEHICLES);  // Theft from motor Vehicles
		larcenyOffenseByNatureMap.put("23E", PropertyStolenByClassificationRowName.LARCENY_FROM_COIN_OPERATED_MACHINES);  // Theft from Coin Operated machines and device
		larcenyOffenseByNatureMap.put("23H04", PropertyStolenByClassificationRowName.LARCENY_BICYCLES); // Bicycles 
		larcenyOffenseByNatureMap.put("23H", PropertyStolenByClassificationRowName.LARCENY_ALL_OTHER);  // All Other
		
		larcenyOffenseImportanceMap = new HashMap<>();
		larcenyOffenseImportanceMap.put("23B", 1);
		larcenyOffenseImportanceMap.put("23A", 2);
		larcenyOffenseImportanceMap.put("23C", 3);
		larcenyOffenseImportanceMap.put("23D", 4);
		larcenyOffenseImportanceMap.put("23G", 5);
		larcenyOffenseImportanceMap.put("23H38", 5);
		larcenyOffenseImportanceMap.put("23F", 6);
		larcenyOffenseImportanceMap.put("23E", 7);
		larcenyOffenseImportanceMap.put("23H04", 8);
		larcenyOffenseImportanceMap.put("23H", 9);
	}
	
	public ReturnAForm createReturnASummaryReportByRequest(SummaryReportRequest summaryReportRequest) {
		
		log.info("summaryReportRequest for Return A form: " + summaryReportRequest);
		ReturnAForm returnAForm = new ReturnAForm(summaryReportRequest.getIncidentYear(), summaryReportRequest.getIncidentMonth()); 
		
//		propertyTypeIds = new ArrayList<>();
//		incidentNumbers = new ArrayList<>();
		if (summaryReportRequest.getAgencyId() != null){
			Optional<Agency> agency = agencyRepository.findById(summaryReportRequest.getAgencyId());
			if (agency.isPresent()){
				returnAForm.setAgencyName(agency.get().getAgencyName());
				returnAForm.setOri(agency.get().getAgencyOri());
				returnAForm.setStateName(agency.get().getStateName());
				returnAForm.setStateCode(agency.get().getStateCode());
				returnAForm.setPopulation(agency.get().getPopulation());
			}
			else{
				return returnAForm; 
			}
		}
		else{
			Agency agency = agencyRepository.findFirstByStateCode(summaryReportRequest.getStateCode());
			returnAForm.setAgencyName("");
			returnAForm.setStateName(agency.getStateName());
			returnAForm.setStateCode(agency.getStateCode());
			returnAForm.setPopulation(null);
		}
		
		processReportedOffenses(summaryReportRequest, returnAForm);
		processOffenseClearances(summaryReportRequest, returnAForm);
		
		fillTheForcibleRapeTotalRow(returnAForm);
		fillTheRobberyTotalRow(returnAForm);
		fillTheAssaultTotalRow(returnAForm);
		fillTheBurglaryTotalRow(returnAForm);
		fillTheMotorVehicleTheftTotalRow(returnAForm);
		fillTheGrandTotalRow(returnAForm);
		
		log.info("returnAForm: " + returnAForm);
		return returnAForm;
	}
	
	public ReturnARecordCardReport createReturnARecordCardReportByRequest(SummaryReportRequest summaryReportRequest) {
		
		log.info("summaryReportRequest for Return A Record Card: " + summaryReportRequest);
		ReturnARecordCardReport returnARecordCardReport = new ReturnARecordCardReport(summaryReportRequest.getIncidentYear()); 
		
//		propertyTypeIds = new ArrayList<>();
//		incidentNumbers = new ArrayList<>();
		
		processReportedOffenses(summaryReportRequest, returnARecordCardReport);
		processOffenseClearances(summaryReportRequest, returnARecordCardReport);
		
		for (ReturnARecordCard returnARecordCard: returnARecordCardReport.getReturnARecordCards().values()) {
			fillTheMurderSubtotalRow(returnARecordCard);
			fillTheRapeSubtotalRow(returnARecordCard);
			fillTheRobberySubtotalRow(returnARecordCard);
			fillTheAssaultSubtotalRow(returnARecordCard);
			fillTheViolentTotalRow(returnARecordCard);
			fillTheBurglarySubotalRow(returnARecordCard);
			fillTheLarcenySubotalRow(returnARecordCard);
			fillTheMotorVehicleTheftSubotalRow(returnARecordCard);
			fillThePropertyTotalRow(returnARecordCard);
			fillTheReportedOffenseGrandTotalRow(returnARecordCard);
		}
		
		processArsonReportedOffenses(summaryReportRequest, returnARecordCardReport); 
		processArsonOffenseClearances(summaryReportRequest, returnARecordCardReport); 
		
		processHumanTraffickingReportedOffenses(summaryReportRequest, returnARecordCardReport); 
		processHumanTraffickingOffenseClearances(summaryReportRequest, returnARecordCardReport); 
		
		log.debug("returnARecordCardReport: " + returnARecordCardReport);
		return returnARecordCardReport;
	}
	
	private void processHumanTraffickingOffenseClearances(SummaryReportRequest summaryReportRequest,
			ReturnARecordCardReport returnARecordCardReport) {
		List<AdministrativeSegment> administrativeSegments = 
				administrativeSegmentService.findHumanTraffickingIncidentByRequestAndClearanceDate(summaryReportRequest);
		for (AdministrativeSegment administrativeSegment: administrativeSegments){
			if (administrativeSegment.getOffenseSegments().size() == 0) continue;
			ReturnARecordCard returnARecordCard = getReturnARecordCard(returnARecordCardReport, administrativeSegment);
			
			boolean isClearanceInvolvingOnlyJuvenile = administrativeSegment.isClearanceInvolvingOnlyJuvenile();
			
			OffenseSegment offense = humanTraffickingFormService.getHumanTraffickingOffense(administrativeSegment);
			ReturnARecordCardRow returnARecordCardRow = null; 
			int offenseCount = 1; 
			switch (OffenseCode.forCode(offense.getUcrOffenseCodeType().getNibrsCode())){
			case _64A:
				returnARecordCardRow = returnARecordCard.getHumanTraffickingFormRows()[0];
				offenseCount = getOffenseCountByConnectedVictim(administrativeSegment, offense);
				break; 
			case _64B: 
				returnARecordCardRow = returnARecordCard.getHumanTraffickingFormRows()[1];
				offenseCount = getOffenseCountByConnectedVictim(administrativeSegment, offense);
				break; 
			default: 
			}
			
			if (returnARecordCardRow != null){
				returnARecordCardRow.increaseClearedOffenses(offenseCount);
				if (isClearanceInvolvingOnlyJuvenile){
					returnARecordCardRow.increaseClearanceInvolvingOnlyJuvenile(offenseCount);
				}
			}
		}
	}

	private void processHumanTraffickingReportedOffenses(SummaryReportRequest summaryReportRequest,
			ReturnARecordCardReport returnARecordCardReport) {
		List<AdministrativeSegment> administrativeSegments = 
				administrativeSegmentService.findHumanTraffickingIncidentByRequest(summaryReportRequest);
		for (AdministrativeSegment administrativeSegment: administrativeSegments){
			if (administrativeSegment.getOffenseSegments().size() == 0) continue; 
			ReturnARecordCard returnARecordCard = getReturnARecordCard(returnARecordCardReport, administrativeSegment);
			int incidentMonth = administrativeSegment.getIncidentDate().getMonthValue(); 

			OffenseSegment offense = humanTraffickingFormService.getHumanTraffickingOffense(administrativeSegment); 
			ReturnARecordCardRow returnARecordCardRow = null; 
			int offenseCount = 1; 
			OffenseCode offenseCode = OffenseCode.forCode(offense.getUcrOffenseCodeType().getNibrsCode()); 
			switch (offenseCode){
			case _64A:
				returnARecordCardRow = returnARecordCard.getHumanTraffickingFormRows()[0];
				offenseCount = getOffenseCountByConnectedVictim(administrativeSegment, offense);
				break; 
			case _64B: 
				returnARecordCardRow = returnARecordCard.getHumanTraffickingFormRows()[1];
				offenseCount = getOffenseCountByConnectedVictim(administrativeSegment, offense);
				break; 
			default: 
			}
			
			if (returnARecordCardRow != null){
				increaseRecordCardRowCount(returnARecordCardRow, incidentMonth, offenseCount);
			}
			
		}
	}

	private void processArsonOffenseClearances(SummaryReportRequest summaryReportRequest,
			ReturnARecordCardReport returnARecordCardReport) {
		List<Integer> ids = administrativeSegmentRepository.findArsonIdsByStateCodeAndOriAndClearanceDate(
				summaryReportRequest.getStateCode(), summaryReportRequest.getAgencyId(), 
				summaryReportRequest.getIncidentYear(), summaryReportRequest.getIncidentMonth(), 
				summaryReportRequest.getOwnerId());
		List<AdministrativeSegment> administrativeSegments = administrativeSegmentRepository.findAllById(ids);
		for (AdministrativeSegment administrativeSegment: administrativeSegments) {
			ReturnARecordCard returnARecordCard = getReturnARecordCard(returnARecordCardReport, administrativeSegment);
			
			ReturnARecordCardRow arsonRow = returnARecordCard.getArsonRow(); 
			if (administrativeSegment.isClearanceInvolvingOnlyJuvenile()) {
				arsonRow.increaseClearanceInvolvingOnlyJuvenile(1);
			}
			
			arsonRow.increaseClearedOffenses(1);
		}
	}

	private void processArsonReportedOffenses(SummaryReportRequest summaryReportRequest,
			ReturnARecordCardReport returnARecordCardReport) {
		List<Integer> ids = administrativeSegmentRepository.findArsonIdsBySummaryReportRequest(
						summaryReportRequest.getStateCode(), summaryReportRequest.getAgencyId(), 
						summaryReportRequest.getIncidentYear(), summaryReportRequest.getIncidentMonth(), 
						summaryReportRequest.getOwnerId());
		List<AdministrativeSegment> administrativeSegments = administrativeSegmentRepository.findAllById(ids);
		for (AdministrativeSegment administrativeSegment: administrativeSegments) {
			ReturnARecordCard returnARecordCard = getReturnARecordCard(returnARecordCardReport, administrativeSegment);
			int incidentMonth = administrativeSegment.getIncidentDate().getMonthValue(); 
			
			ReturnARecordCardRow arsonRow = returnARecordCard.getArsonRow(); 
			increaseRecordCardRowCount(arsonRow, incidentMonth, 1);
		}
		
	}

	private void processOffenseClearances(SummaryReportRequest summaryReportRequest,
			ReturnARecordCardReport returnARecordCardReport) {
		List<Integer> administrativeSegmentIds = 
				administrativeSegmentRepository.findIdsByStateCodeAndOriAndClearanceDateAndOffenses(
						summaryReportRequest.getStateCode(), summaryReportRequest.getAgencyId(), 
						summaryReportRequest.getIncidentYear(), summaryReportRequest.getIncidentMonth(), 
						summaryReportRequest.getOwnerId(), new ArrayList(partIOffensesMap.keySet()));
		int i; 
		int batchSize = appProperties.getSummaryReportProcessingBatchSize();
		log.info(administrativeSegmentIds.size() + " offense Clearances to process.");
		for (i = 0; i+ batchSize < administrativeSegmentIds.size(); i+=batchSize ) {
			log.info("processing offense Clearances " + i + " to " + String.valueOf(i+batchSize));
			getRecordCardOffenseClearanceRows(returnARecordCardReport, administrativeSegmentIds.subList(i, i+batchSize));
		}
		
		if (i < administrativeSegmentIds.size()) {
			log.info("processing offense Clearances " + i + " to " + administrativeSegmentIds.size());
			getRecordCardOffenseClearanceRows(returnARecordCardReport, administrativeSegmentIds.subList(i, administrativeSegmentIds.size()));
		}
		
	}

	private void getRecordCardOffenseClearanceRows(ReturnARecordCardReport returnARecordCardReport,
			List<Integer> administrativeSegmentIds) {
		List<AdministrativeSegment> administrativeSegments = 
				administrativeSegmentRepository.findAllById(administrativeSegmentIds)
					.stream().distinct().collect(Collectors.toList());; 
		for (AdministrativeSegment administrativeSegment: administrativeSegments){
			
			if (administrativeSegment.getOffenseSegments().size() == 0) continue;
			ReturnARecordCard returnARecordCard = getReturnARecordCard(returnARecordCardReport, administrativeSegment);
			countClearedOffenses(returnARecordCard.getReturnAFormRows(), administrativeSegment, ReturnARecordCardRowName.class);

		}
		administrativeSegments.clear();
		
	}

	private void fillTheReportedOffenseGrandTotalRow(ReturnARecordCard returnARecordCard) {
		fillRecordCardTotalRow(returnARecordCard, ReturnARecordCardRowName.GRAND_TOTAL, 
				ReturnARecordCardRowName.VIOLENT_TOTAL,
				ReturnARecordCardRowName.PROPERTY_TOTAL);
	}

	private void fillThePropertyTotalRow(ReturnARecordCard returnARecordCard) {
		fillRecordCardTotalRow(returnARecordCard, ReturnARecordCardRowName.PROPERTY_TOTAL, 
				ReturnARecordCardRowName.BURGLARY_SUBTOTAL,
				ReturnARecordCardRowName.LARCENY_THEFT_TOTAL, 
				ReturnARecordCardRowName.MOTOR_VEHICLE_THEFT_SUBTOTAL);
	}

	private void fillTheMotorVehicleTheftSubotalRow(ReturnARecordCard returnARecordCard) {
		fillRecordCardTotalRow(returnARecordCard, ReturnARecordCardRowName.MOTOR_VEHICLE_THEFT_SUBTOTAL, 
				ReturnARecordCardRowName.AUTOS_THEFT,
				ReturnARecordCardRowName.TRUCKS_BUSES_THEFT, 
				ReturnARecordCardRowName.OTHER_VEHICLES_THEFT);
	}

	private void fillTheLarcenySubotalRow(ReturnARecordCard returnARecordCard) {
		fillRecordCardTotalRow(returnARecordCard, ReturnARecordCardRowName.LARCENY_THEFT, 
				ReturnARecordCardRowName.LARCENY_THEFT_TOTAL);
	}

	private void fillTheBurglarySubotalRow(ReturnARecordCard returnARecordCard) {
		fillRecordCardTotalRow(returnARecordCard, ReturnARecordCardRowName.BURGLARY_SUBTOTAL, 
				ReturnARecordCardRowName.FORCIBLE_ENTRY_BURGLARY, 
				ReturnARecordCardRowName.UNLAWFUL_ENTRY_NO_FORCE_BURGLARY, 
				ReturnARecordCardRowName.ATTEMPTED_FORCIBLE_ENTRY_BURGLARY);
	}

	private void fillTheViolentTotalRow(ReturnARecordCard returnARecordCard) {
		fillRecordCardTotalRow(returnARecordCard, ReturnARecordCardRowName.VIOLENT_TOTAL, 
				ReturnARecordCardRowName.MURDER_SUBTOTAL, 
				ReturnARecordCardRowName.RAPE_SUBTOTAL, 
				ReturnARecordCardRowName.ROBBERY_SUBTOTAL, 
				ReturnARecordCardRowName.ASSAULT_SUBTOTAL);
	}

	private void fillTheAssaultSubtotalRow(ReturnARecordCard returnARecordCard) {
		fillRecordCardTotalRow(returnARecordCard, ReturnARecordCardRowName.ASSAULT_SUBTOTAL, 
				ReturnARecordCardRowName.FIREARM_ASSAULT, 
				ReturnARecordCardRowName.KNIFE_CUTTING_INSTRUMENT_ASSAULT, 
				ReturnARecordCardRowName.OTHER_DANGEROUS_WEAPON_ASSAULT, 
				ReturnARecordCardRowName.HANDS_FISTS_FEET_AGGRAVATED_INJURY_ASSAULT);
	}

	private void fillTheRobberySubtotalRow(ReturnARecordCard returnARecordCard) {
		fillRecordCardTotalRow(returnARecordCard, ReturnARecordCardRowName.ROBBERY_SUBTOTAL, 
				ReturnARecordCardRowName.FIREARM_ROBBERY, 
				ReturnARecordCardRowName.KNIFE_CUTTING_INSTRUMENT_ROBBERY, 
				ReturnARecordCardRowName.OTHER_DANGEROUS_WEAPON_ROBBERY, 
				ReturnARecordCardRowName.STRONG_ARM_ROBBERY);
	}

	private void fillTheRapeSubtotalRow(ReturnARecordCard returnARecordCard) {
		fillRecordCardTotalRow(returnARecordCard, ReturnARecordCardRowName.RAPE_SUBTOTAL, 
				ReturnARecordCardRowName.RAPE_BY_FORCE, 
				ReturnARecordCardRowName.ATTEMPTS_TO_COMMIT_FORCIBLE_RAPE);
	}

	private void fillTheMurderSubtotalRow(ReturnARecordCard returnARecordCard) {
		fillRecordCardTotalRow(returnARecordCard, ReturnARecordCardRowName.MURDER_SUBTOTAL, ReturnARecordCardRowName.MURDER_MURDER);
	}

	private void processReportedOffenses(SummaryReportRequest summaryReportRequest,
			ReturnARecordCardReport returnARecordCardReport) {
		List<String> offenseCodes = new ArrayList(partIOffensesMap.keySet()); 
//		offenseCodes.remove("09B");
//		offenseCodes.remove("13B");
//		offenseCodes.remove("13C");
		List<Integer> administrativeSegmentIds = 
				administrativeSegmentRepository.findIdsBySummaryReportRequestAndOffenses(
						summaryReportRequest.getStateCode(), summaryReportRequest.getAgencyId(), 
						summaryReportRequest.getIncidentYear(), 0, 
						summaryReportRequest.getOwnerId(), offenseCodes);
		int i ; 
		int batchSize = appProperties.getSummaryReportProcessingBatchSize();
		log.info(administrativeSegmentIds.size() + " Reported offenses to process ");
		for (i = 0; i+batchSize < administrativeSegmentIds.size(); i+=batchSize ) {
			log.info("processing Reported offenses " + i + " to " + String.valueOf(i+batchSize));
			getRecordCardReportedOffenseRows(returnARecordCardReport, administrativeSegmentIds.subList(i, i+batchSize));
		}
		
		if (i < administrativeSegmentIds.size()) {
			log.info("processing Reported offenses " + i + " to " + administrativeSegmentIds.size());
			getRecordCardReportedOffenseRows(returnARecordCardReport, administrativeSegmentIds.subList(i, administrativeSegmentIds.size()));
		}
	}

	private void getRecordCardReportedOffenseRows(ReturnARecordCardReport returnARecordCardReport, List<Integer> administrativeSegmentIds) {
		List<AdministrativeSegment> administrativeSegments = 
				administrativeSegmentRepository.findAllById(administrativeSegmentIds)
					.stream().distinct().collect(Collectors.toList());; 
		for (AdministrativeSegment administrativeSegment: administrativeSegments){
			if (administrativeSegment.getOffenseSegments().size() == 0) continue; 
			ReturnARecordCard returnARecordCard = getReturnARecordCard(returnARecordCardReport, administrativeSegment);
			
			int incidentMonth = administrativeSegment.getIncidentDate().getMonthValue(); 
			List<OffenseSegment> offensesToReport = getReturnAOffenses(administrativeSegment); 
			for (OffenseSegment offense: offensesToReport){
				
				ReturnARecordCardRowName returnARecordCardRowName = null; 
				int offenseCount = 1; 
				OffenseCode offenseCode = OffenseCode.forCode(offense.getUcrOffenseCodeType().getNibrsCode()); 
				switch (offenseCode){
				case _09A:
					returnARecordCardRowName = ReturnARecordCardRowName.MURDER_MURDER;
					offenseCount = getOffenseCountByConnectedVictim(administrativeSegment, offense);
					break; 
				case _11A:
				case _11B:
				case _11C:
					returnARecordCardRowName = getRowNameFor11AOffense(administrativeSegment, offense, ReturnARecordCardRowName.class);
					offenseCount = getOffenseCountByConnectedVictim(administrativeSegment, "11A", "11B", "11C");
					break;
				case _120:
					returnARecordCardRowName = getRowNameForRobbery(offense, ReturnARecordCardRowName.class);
					break; 
				case _13A:
					returnARecordCardRowName = getRowNameForAssault(offense, ReturnARecordCardRowName.class);
					offenseCount = getOffenseCountByConnectedVictim(administrativeSegment, offense);
					break;
				case _13B: 
				case _13C: 
					returnARecordCardRowName = getRowNameFor13B13COffense(offense, ReturnARecordCardRowName.class);
					offenseCount = getOffenseCountByConnectedVictim(administrativeSegment, "13B", "13C");
				case _220: 
					countRecordCardBurglaryOffense(returnARecordCard, offense, incidentMonth);
					break;
				case _23A: 
				case _23B:
				case _23C: 
				case _23D: 
				case _23E: 
				case _23F: 
				case _23G: 
				case _23H: 
					returnARecordCardRowName = ReturnARecordCardRowName.LARCENY_THEFT_TOTAL;
					break; 
				case _240: 
					countRecordCardMotorVehicleTheftOffense(returnARecordCard, offense, incidentMonth);
					break; 
				default: 
				}
				
				if (returnARecordCardRowName != null){
					increaseRecordCardRowCount(returnARecordCard.getRows()[returnARecordCardRowName.ordinal()], incidentMonth, offenseCount);
				}
			}
			
		}
		administrativeSegments.clear();
		
	}

	private ReturnARecordCard getReturnARecordCard(ReturnARecordCardReport returnARecordCardReport,
			AdministrativeSegment administrativeSegment) {
		
		Agency agency = administrativeSegment.getAgency(); 
		
		if (StringUtils.isBlank(returnARecordCardReport.getStateName())) {
			returnARecordCardReport.setStateName(agency.getStateName());
		}
		
		ReturnARecordCard returnARecordCard = returnARecordCardReport.getReturnARecordCards().get(agency.getAgencyId()); 
		
		if (returnARecordCard == null) {
			returnARecordCard = 
					new ReturnARecordCard(agency.getAgencyOri(), administrativeSegment.getIncidentDateType().getYearNum());
			returnARecordCard.setAgencyName(agency.getAgencyName());
			returnARecordCard.setPopulation(agency.getPopulation());
			returnARecordCard.setStateCode(agency.getStateCode());
			returnARecordCard.setStateName(agency.getStateName());
			
			returnARecordCardReport.getReturnARecordCards().put(agency.getAgencyId(), returnARecordCard);
		}
		return returnARecordCard;
	}

	private void increaseRecordCardRowCount(ReturnARecordCardRow row, int incidentMonth, int offenseCount) {
		row.increaseTotal(offenseCount);
		row.increaseMonthNumber(offenseCount, incidentMonth -1);
		
		if (incidentMonth <= 6) {
			row.increaseFirstHalfSubtotal(offenseCount);
		}
		else {
			row.increaseSecondHalfSubtotal(offenseCount);
		}
	}

	private boolean countRecordCardMotorVehicleTheftOffense(ReturnARecordCard returnARecordCard,
			OffenseSegment offense, int incidentMonth) {
		
		int totalOffenseCount = 0;
		if ("A".equals(offense.getOffenseAttemptedCompleted())){
			increaseRecordCardRowCount(returnARecordCard.getRows()[ReturnARecordCardRowName.AUTOS_THEFT.ordinal()], incidentMonth, 1);
			totalOffenseCount = 1;
		}
		else {
			List<PropertySegment> properties =  offense.getAdministrativeSegment().getPropertySegments()
					.stream().filter(property->TypeOfPropertyLossCode._7.code.equals(property.getTypePropertyLossEtcType().getNibrsCode()))
					.collect(Collectors.toList());
			
			for (PropertySegment property: properties){
				int offenseCountInThisProperty = 0;
				List<String> motorVehicleCodes = property.getPropertyTypes().stream()
						.map(propertyType -> propertyType.getPropertyDescriptionType().getNibrsCode())
						.filter(code -> PropertyDescriptionCode.isMotorVehicleCode(code))
						.collect(Collectors.toList()); 
				
				int numberOfStolenMotorVehicles = Optional.ofNullable(property.getNumberOfStolenMotorVehicles()).orElse(0);
				
//				log.info("offense.getOffenseAttemptedCompleted():" + offense.getOffenseAttemptedCompleted()); 
				if ( numberOfStolenMotorVehicles > 0){
					offenseCountInThisProperty += numberOfStolenMotorVehicles;
					if (motorVehicleCodes.contains(PropertyDescriptionCode._03.code)){
						for (String code: motorVehicleCodes){
							switch (code){
							case "05":
							case "28": 
							case "37": 
								numberOfStolenMotorVehicles --; 
								increaseRecordCardRowCount(returnARecordCard.getRows()[ReturnARecordCardRowName.TRUCKS_BUSES_THEFT.ordinal()], incidentMonth, 1);
								break; 
							case "24": 
								numberOfStolenMotorVehicles --; 
								increaseRecordCardRowCount(returnARecordCard.getRows()[ReturnARecordCardRowName.OTHER_VEHICLES_THEFT.ordinal()], incidentMonth, 1);
								break; 
							}
						}
						
						if (numberOfStolenMotorVehicles > 0){
							increaseRecordCardRowCount(returnARecordCard.getRows()[ReturnARecordCardRowName.AUTOS_THEFT.ordinal()], incidentMonth, numberOfStolenMotorVehicles);
						}
					}
					else if (CollectionUtils.containsAny(motorVehicleCodes, 
							Arrays.asList(PropertyDescriptionCode._05.code, PropertyDescriptionCode._28.code, PropertyDescriptionCode._37.code))){
						int countOfOtherVehicles = Long.valueOf(motorVehicleCodes.stream()
								.filter(code -> code.equals(PropertyDescriptionCode._24.code)).count()).intValue();
						numberOfStolenMotorVehicles -= countOfOtherVehicles;
						increaseRecordCardRowCount(returnARecordCard.getRows()[ReturnARecordCardRowName.OTHER_VEHICLES_THEFT.ordinal()], incidentMonth, countOfOtherVehicles);
						
						if (numberOfStolenMotorVehicles > 0){
							increaseRecordCardRowCount(returnARecordCard.getRows()[ReturnARecordCardRowName.TRUCKS_BUSES_THEFT.ordinal()], incidentMonth, numberOfStolenMotorVehicles);
						}
					}
					else if (motorVehicleCodes.contains(PropertyDescriptionCode._24.code)){
						increaseRecordCardRowCount(returnARecordCard.getRows()[ReturnARecordCardRowName.OTHER_VEHICLES_THEFT.ordinal()], incidentMonth, numberOfStolenMotorVehicles);
					}
				}
				totalOffenseCount += offenseCountInThisProperty;
				
//				if (offenseCountInThisProperty > 0){
//					double valueOfStolenProperty = getStolenPropertyValue(offense.getAdministrativeSegment(), 0);
//					returnAForm.getPropertyStolenByClassifications()
//						[PropertyStolenByClassificationRowName.MOTOR_VEHICLE_THEFT.ordinal()]
//							.increaseMonetaryValue(valueOfStolenProperty);
//					returnAForm.getPropertyStolenByClassifications()
//						[PropertyStolenByClassificationRowName.GRAND_TOTAL.ordinal()]
//						.increaseMonetaryValue(valueOfStolenProperty);
//				}
	
			}
		}		
//		returnAForm.getPropertyStolenByClassifications()
//			[PropertyStolenByClassificationRowName.MOTOR_VEHICLE_THEFT.ordinal()]
//					.increaseNumberOfOffenses(totalOffenseCount);
//		returnAForm.getPropertyStolenByClassifications()
//			[PropertyStolenByClassificationRowName.GRAND_TOTAL.ordinal()]
//				.increaseNumberOfOffenses(totalOffenseCount);
		return totalOffenseCount > 0; 
	}

	private int countRecordCardBurglaryOffense(ReturnARecordCard returnARecordCard, OffenseSegment offense, int incidentMonth) {
		ReturnARecordCardRowName rowName = getBurglaryRow(offense, ReturnARecordCardRowName.class);
		
		int burglaryOffenseCount = 0; 
//		If there is an entry in Data Element 10 (Number of Premises Entered) and an entry of 19 
//		(Rental Storage Facility) in Data Element 9 (Location Type), use the number of premises 
//		listed in Data Element 10 as the number of burglaries to be counted.
		if (rowName != null){
			int numberOfPremisesEntered = Optional.ofNullable(offense.getNumberOfPremisesEntered()).orElse(0);
//			log.info("numberOfPremisesEntered:" + numberOfPremisesEntered);
//			log.info("LocationTypeCode._19.code.equals(offense.getLocationType().getNibrsCode()):" + LocationTypeCode._19.code.equals(offense.getLocationType().getNibrsCode()));
			if ( numberOfPremisesEntered > 0 
					&& LocationTypeCode._19.code.equals(offense.getLocationType().getNibrsCode())){
				burglaryOffenseCount = offense.getNumberOfPremisesEntered();
			}
			else {
				burglaryOffenseCount = 1; 
			}
			
			increaseRecordCardRowCount(returnARecordCard.getRows()[rowName.ordinal()], incidentMonth, burglaryOffenseCount);
		}
		
		return burglaryOffenseCount; 
	}

	private void processOffenseClearances(SummaryReportRequest summaryReportRequest, ReturnAForm returnAForm) {
		List<Integer> administrativeSegmentIds = 
				administrativeSegmentRepository.findIdsByStateCodeAndOriAndClearanceDateAndOffenses(
						summaryReportRequest.getStateCode(), summaryReportRequest.getAgencyId(), 
						summaryReportRequest.getIncidentYear(), summaryReportRequest.getIncidentMonth(), 
						summaryReportRequest.getOwnerId(), new ArrayList(partIOffensesMap.keySet()));
		int i; 
		int batchSize = appProperties.getSummaryReportProcessingBatchSize();
		log.info(administrativeSegmentIds.size() + " offense Clearances to process.");
		for (i = 0; i+ batchSize < administrativeSegmentIds.size(); i+=batchSize ) {
			log.info("processing offense Clearances " + i + " to " + String.valueOf(i+batchSize));
			getOffenseClearanceRows(returnAForm.getRows(), administrativeSegmentIds.subList(i, i+batchSize), ReturnARowName.class);
		}
		
		if (i < administrativeSegmentIds.size()) {
			log.info("processing offense Clearances " + i + " to " + administrativeSegmentIds.size());
			getOffenseClearanceRows(returnAForm.getRows(), administrativeSegmentIds.subList(i, administrativeSegmentIds.size()), ReturnARowName.class);
		}
	}

	private void processReportedOffenses(SummaryReportRequest summaryReportRequest, ReturnAForm returnAForm) {
		List<Integer> administrativeSegmentIds = 
				administrativeSegmentRepository.findIdsBySummaryReportRequestAndOffenses(
						summaryReportRequest.getStateCode(), summaryReportRequest.getAgencyId(), 
						summaryReportRequest.getIncidentYear(), summaryReportRequest.getIncidentMonth(), 
						summaryReportRequest.getOwnerId(), new ArrayList(partIOffensesMap.keySet()));
		int i ; 
		int batchSize = appProperties.getSummaryReportProcessingBatchSize();
		log.info(administrativeSegmentIds.size() + " Reported offenses to process. ");
		for (i = 0; i+batchSize < administrativeSegmentIds.size(); i+=batchSize ) {
			log.info("processing Reported offenses " + i + " to " + String.valueOf(i+batchSize));
			getReportedOffenseRows(returnAForm, administrativeSegmentIds.subList(i, i+batchSize), summaryReportRequest);
		}
		
		if (i < administrativeSegmentIds.size()) {
			log.info("processing Reported offenses " + i + " to " + administrativeSegmentIds.size());
			getReportedOffenseRows(returnAForm, administrativeSegmentIds.subList(i, administrativeSegmentIds.size()), summaryReportRequest);
		}
	}

	private <T extends Enum<T>> void getOffenseClearanceRows(ReturnAFormRow[] rows, 
			List<Integer> administrativeSegmentIds, Class<T> enumType) {
		List<AdministrativeSegment> administrativeSegments = 
				administrativeSegmentRepository.findAllById(administrativeSegmentIds)
					.stream().distinct().collect(Collectors.toList());; 
		for (AdministrativeSegment administrativeSegment: administrativeSegments){
			
			if (administrativeSegment.getOffenseSegments().size() == 0) continue;
			
			countClearedOffenses(rows, administrativeSegment, enumType);

		}
		administrativeSegments.clear();
	}

	private <T extends Enum<T>> void countClearedOffenses(ReturnAFormRow[] rows, AdministrativeSegment administrativeSegment,
			Class<T> enumType) {
		boolean isClearanceInvolvingOnlyJuvenile = administrativeSegment.isClearanceInvolvingOnlyJuvenile();
		List<OffenseSegment> offenses = getClearedOffenses(administrativeSegment);
		for (OffenseSegment offense: offenses){

			T rowName = null; 
			int offenseCount = 1; 
			switch (OffenseCode.forCode(offense.getUcrOffenseCodeType().getNibrsCode())){
			case _09A:
				if (enumType == ReturnARowName.class) {
					rowName = Enum.valueOf(enumType, "MURDER_NONNEGLIGENT_HOMICIDE");
				}
				else {
					rowName = Enum.valueOf(enumType, "MURDER_MURDER");
				}
				offenseCount = getOffenseCountByConnectedVictim(administrativeSegment, offense);
				break; 
			case _09B: 
				if (enumType == ReturnARowName.class) {
					rowName = Enum.valueOf(enumType, "MANSLAUGHTER_BY_NEGLIGENCE"); 
					offenseCount = getOffenseCountByConnectedVictim(administrativeSegment, offense);
				}
				break; 
			case _11A: 
			case _11B:
			case _11C:
				rowName = getRowNameFor11AOffense(administrativeSegment, offense, enumType);
				offenseCount = getOffenseCountByConnectedVictim(administrativeSegment, "11A", "11B", "11C");
				break;
			case _120:
				rowName = getRowNameForRobbery(offense, enumType);
				break; 
			case _13A:
				rowName = getRowNameForAssault(offense, enumType);
				offenseCount = getOffenseCountByConnectedVictim(administrativeSegment, offense);
				break;
			case _13B: 
			case _13C: 
				rowName = getRowNameFor13B13COffense(offense, enumType);
				offenseCount = getOffenseCountByConnectedVictim(administrativeSegment, offense);
			case _220: 
				countClearedBurglaryOffense(rows, offense, isClearanceInvolvingOnlyJuvenile, enumType);
				break;
			case _23A: 
			case _23B:
			case _23C: 
			case _23D: 
			case _23E: 
			case _23F: 
			case _23G: 
			case _23H: 
				rowName = Enum.valueOf(enumType, "LARCENY_THEFT_TOTAL"); 
				break; 
			case _240: 
				countClearedMotorVehicleTheftOffense(rows, offense, isClearanceInvolvingOnlyJuvenile, enumType );
				break; 
			default: 
			}
			
			if (rowName != null){
				rows[rowName.ordinal()].increaseClearedOffenses(offenseCount);
				
				if (isClearanceInvolvingOnlyJuvenile){
					rows[rowName.ordinal()].increaseClearanceInvolvingOnlyJuvenile(offenseCount);
				}
			}
		}
	}

	private <T extends Enum<T>> void countClearedMotorVehicleTheftOffense(ReturnAFormRow[] rows, OffenseSegment offense,
			boolean isClearanceInvolvingOnlyJuvenile, Class<T> enumType) {
		List<PropertySegment> properties =  offense.getAdministrativeSegment().getPropertySegments()
				.stream().filter(property->TypeOfPropertyLossCode._7.code.equals(property.getTypePropertyLossEtcType().getNibrsCode()))
				.collect(Collectors.toList());
		
		for (PropertySegment property: properties){
			List<String> motorVehicleCodes = property.getPropertyTypes().stream()
					.map(propertyType -> propertyType.getPropertyDescriptionType().getNibrsCode())
					.filter(code -> PropertyDescriptionCode.isMotorVehicleCode(code))
					.collect(Collectors.toList()); 
			
			int numberOfStolenMotorVehicles = Optional.ofNullable(property.getNumberOfStolenMotorVehicles()).orElse(0);
			if ("A".equals(offense.getOffenseAttemptedCompleted())){
				rows[Enum.valueOf(enumType, "AUTOS_THEFT").ordinal()].increaseReportedOffenses(motorVehicleCodes.size());
			}
			else if (numberOfStolenMotorVehicles > 0){
				
				if (motorVehicleCodes.contains(PropertyDescriptionCode._03.code)){
					for (String code: motorVehicleCodes){
						switch (code){
						case "05":
						case "28": 
						case "37": 
							numberOfStolenMotorVehicles --; 
							rows[Enum.valueOf(enumType, "TRUCKS_BUSES_THEFT").ordinal()].increaseClearedOffenses(1);
							
							if(isClearanceInvolvingOnlyJuvenile){
								rows[Enum.valueOf(enumType, "TRUCKS_BUSES_THEFT").ordinal()].increaseClearanceInvolvingOnlyJuvenile(1);
							}
							break; 
						case "24": 
							numberOfStolenMotorVehicles --; 
							rows[Enum.valueOf(enumType, "OTHER_VEHICLES_THEFT").ordinal()].increaseClearedOffenses(1);
							if(isClearanceInvolvingOnlyJuvenile){
								rows[Enum.valueOf(enumType, "OTHER_VEHICLES_THEFT").ordinal()].increaseClearanceInvolvingOnlyJuvenile(1);
							}
							break; 
						}
					}
					
					if (numberOfStolenMotorVehicles > 0){
						rows[Enum.valueOf(enumType, "AUTOS_THEFT").ordinal()].increaseClearedOffenses(numberOfStolenMotorVehicles);
						if(isClearanceInvolvingOnlyJuvenile){
							rows[Enum.valueOf(enumType, "AUTOS_THEFT").ordinal()].increaseClearanceInvolvingOnlyJuvenile(1);
						}
					}
				}
				else if (CollectionUtils.containsAny(motorVehicleCodes, 
						Arrays.asList(PropertyDescriptionCode._05.code, PropertyDescriptionCode._28.code, PropertyDescriptionCode._37.code))){
					int countOfOtherVehicles = Long.valueOf(motorVehicleCodes.stream()
							.filter(code -> code.equals(PropertyDescriptionCode._24.code)).count()).intValue();
					numberOfStolenMotorVehicles -= Long.valueOf(countOfOtherVehicles).intValue();
					
					rows[Enum.valueOf(enumType, "OTHER_VEHICLES_THEFT").ordinal()].increaseClearedOffenses(countOfOtherVehicles);
					if(isClearanceInvolvingOnlyJuvenile){
						rows[Enum.valueOf(enumType, "OTHER_VEHICLES_THEFT").ordinal()].increaseClearanceInvolvingOnlyJuvenile(countOfOtherVehicles);
					}
					
					if (numberOfStolenMotorVehicles > 0){
						rows[Enum.valueOf(enumType, "TRUCKS_BUSES_THEFT").ordinal()].increaseClearedOffenses(numberOfStolenMotorVehicles);
						if(isClearanceInvolvingOnlyJuvenile){
							rows[Enum.valueOf(enumType, "TRUCKS_BUSES_THEFT").ordinal()].increaseClearanceInvolvingOnlyJuvenile(numberOfStolenMotorVehicles);
						}
					}
				}
				else if (motorVehicleCodes.contains(PropertyDescriptionCode._24.code)){
					rows[Enum.valueOf(enumType, "OTHER_VEHICLES_THEFT").ordinal()].increaseClearedOffenses(numberOfStolenMotorVehicles);
					if(isClearanceInvolvingOnlyJuvenile){
						rows[Enum.valueOf(enumType, "OTHER_VEHICLES_THEFT").ordinal()].increaseClearanceInvolvingOnlyJuvenile(numberOfStolenMotorVehicles);
					}
				}
			}
		}
		
	}

	private <T extends Enum<T>> void countClearedBurglaryOffense(ReturnAFormRow[] rows, OffenseSegment offense, 
			boolean isClearanceInvolvingOnlyJuvenile, Class<T> enumType) {
		T rowName = getBurglaryRow(offense, enumType);
		
//		If there is an entry in Data Element 10 (Number of Premises Entered) and an entry of 19 
//		(Rental Storage Facility) in Data Element 9 (Location Type), use the number of premises 
//		listed in Data Element 10 as the number of burglaries to be counted.
		
		if (rowName != null){
			
			int increment = 1;
			int numberOfPremisesEntered = Optional.ofNullable(offense.getNumberOfPremisesEntered()).orElse(0);
			if (numberOfPremisesEntered > 0 && "19".equals(offense.getLocationType().getNibrsCode())){
				increment = offense.getNumberOfPremisesEntered(); 
			}
			
			rows[rowName.ordinal()].increaseClearedOffenses(increment);
			
			if (isClearanceInvolvingOnlyJuvenile){
				rows[rowName.ordinal()].increaseClearanceInvolvingOnlyJuvenile(increment);
			}
		}
	}

	private <T extends Enum<T>> T getBurglaryRow(OffenseSegment offense, Class<T> enumType) {
		T rowName = null; 
		if ("C".equals(offense.getOffenseAttemptedCompleted())){
			if (offense.getMethodOfEntryType().getNibrsCode().equals("F")){
				rowName = Enum.valueOf(enumType, "FORCIBLE_ENTRY_BURGLARY"); 
			}
			else if (offense.getMethodOfEntryType().getNibrsCode().equals("N")){
				rowName = Enum.valueOf(enumType, "UNLAWFUL_ENTRY_NO_FORCE_BURGLARY"); 
			}
		}
		else if ("A".equals(offense.getOffenseAttemptedCompleted()) && 
				Arrays.asList("N", "F").contains(offense.getMethodOfEntryType().getNibrsCode())){
			rowName = Enum.valueOf(enumType, "ATTEMPTED_FORCIBLE_ENTRY_BURGLARY"); 
		}
		return rowName;
	}

	private List<OffenseSegment> getClearedOffenses(AdministrativeSegment administrativeSegment) {
		//TODO need to handle the Time-Window submission types and Time-Window offenses  
		List<OffenseSegment> offenses = new ArrayList<>(); 
		
		OffenseSegment reportingOffense = null; 
		Integer reportingOffenseValue = 99; 
		for (OffenseSegment offense: administrativeSegment.getOffenseSegments()){
			if (!Arrays.asList("A", "C").contains(offense.getOffenseAttemptedCompleted())){
				continue;
			}
			
			if (offense.getUcrOffenseCodeType().getNibrsCode().equals(OffenseCode._200.code)){
				offenses.add(offense);
				continue;
			}
			Integer offenseValue = Optional.ofNullable(partIOffensesMap.get(offense.getUcrOffenseCodeType().getNibrsCode())).orElse(99); 
			
			if (offenseValue < reportingOffenseValue){
				reportingOffense = offense; 
				reportingOffenseValue = offenseValue; 
			}
		}
		
		if (reportingOffense != null){
			offenses.add(reportingOffense);
		}
		return offenses;
	}

	private void getReportedOffenseRows(ReturnAForm returnAForm, List<Integer> administrativeSegmentIds, 
			SummaryReportRequest summaryReportRequest) {
		PropertyStolenByClassification[] stolenProperties = returnAForm.getPropertyStolenByClassifications();
		
		List<AdministrativeSegment> administrativeSegments = 
				administrativeSegmentRepository.findAllById(administrativeSegmentIds)
					.stream().distinct().collect(Collectors.toList());; 
		for (AdministrativeSegment administrativeSegment: administrativeSegments){
			if (administrativeSegment.getOffenseSegments().size() == 0) continue; 
			
			List<OffenseSegment> offensesToReport = getReturnAOffenses(administrativeSegment); 
			for (OffenseSegment offense: offensesToReport){
				
				ReturnARowName returnARowName = null; 
				int burglaryOffenseCount = 0; 
				int offenseCount = 1; 
				boolean hasMotorVehicleTheftOffense = false; 
//				double stolenPropertyValue = 0.0;
				OffenseCode offenseCode = OffenseCode.forCode(offense.getUcrOffenseCodeType().getNibrsCode()); 
				switch (offenseCode){
				case _09A:
					returnARowName = ReturnARowName.MURDER_NONNEGLIGENT_HOMICIDE;
					offenseCount = getOffenseCountByConnectedVictim(administrativeSegment, offense);
					processStolenProperties(stolenProperties, administrativeSegment, PropertyStolenByClassificationRowName.MURDER_AND_NONNEGLIGENT_MANSLAUGHTER, offenseCount);	
					sumPropertyValuesByType(returnAForm, administrativeSegment);
					break; 
				case _09B: 
					returnARowName = ReturnARowName.MANSLAUGHTER_BY_NEGLIGENCE; 
					offenseCount = getOffenseCountByConnectedVictim(administrativeSegment, offense);
//					stolenPropertyValue = getStolenPropertyValue(administrativeSegment, 0);
					//log.info("09B offense stolen property value: " + stolenPropertyValue); 
					break; 
				//case _09C: // TODO  Not finding anything about 09C in the "Conversion of NIBRS Data to Summary Data" document. comment out this block -hw 20190110
				//	returnARowName = ReturnARowName.MURDER_NONNEGLIGENT_HOMICIDE; 
				//	returnAForm.getRows()[returnARowName.ordinal()].increaseUnfoundedOffenses(1); ///?why
				//	break; 
				case _11A:
				case _11B:
				case _11C:
					returnARowName = getRowNameFor11AOffense(administrativeSegment, offense, ReturnARowName.class);
					offenseCount = getOffenseCountByConnectedVictim(administrativeSegment, "11A", "11B", "11C");
					if (returnARowName != null){
						processStolenProperties(stolenProperties, administrativeSegment, PropertyStolenByClassificationRowName.RAPE, offenseCount);
						sumPropertyValuesByType(returnAForm, administrativeSegment);
					}
					break;
				case _120:
					returnARowName = getRowNameForRobbery(offense, ReturnARowName.class);
					if (returnARowName != null){
						processRobberyStolenPropertyByLocation(stolenProperties, offense);
						sumPropertyValuesByType(returnAForm, administrativeSegment);
					}
					break; 
				case _13A:
					returnARowName = getRowNameForAssault(offense, ReturnARowName.class);
					offenseCount = getOffenseCountByConnectedVictim(administrativeSegment, offense);
					break;
				case _13B: 
				case _13C: 
					returnARowName = getRowNameFor13B13COffense(offense, ReturnARowName.class);
					offenseCount = getOffenseCountByConnectedVictim(administrativeSegment, "13B", "13C");
//					log.debug("return A row name is 13B or 13C: " + returnARowName != null?returnARowName:"null");
//					if (returnARowName != null) {
//						log.debug("returnAForm.getRows()[returnARowName.ordinal()]: " + returnAForm.getRows()[returnARowName.ordinal()].getReportedOffenses());
//						log.debug("13B13C count increase:" + offenseCount);
//					}
					break;
				case _220: 
					burglaryOffenseCount = countBurglaryOffense(returnAForm, offense);
					break;
				case _23A: 
				case _23B:
				case _23C: 
				case _23D: 
				case _23E: 
				case _23F: 
				case _23G: 
				case _23H: 
					returnARowName = ReturnARowName.LARCENY_THEFT_TOTAL; 
					processLarcenyStolenPropertyByValue(stolenProperties, administrativeSegment);
					processLarcenyStolenPropertyByNature(stolenProperties, offenseCode, administrativeSegment);
					sumPropertyValuesByType(returnAForm, administrativeSegment);
					break; 
				case _240: 
					hasMotorVehicleTheftOffense = countMotorVehicleTheftOffense(returnAForm, offense);
					processRecoveredVehicleTotal(stolenProperties, administrativeSegment, summaryReportRequest);	
					break; 
				default: 
				}
				
				if (returnARowName != null){
					returnAForm.getRows()[returnARowName.ordinal()].increaseReportedOffenses(offenseCount);
				}
				
				if ( burglaryOffenseCount > 0 || hasMotorVehicleTheftOffense){
					sumPropertyValuesByType(returnAForm, administrativeSegment);
				}
				
//				log.info("ReturnA property by type stolen total: " + returnAForm.getPropertyTypeValues()[PropertyTypeValueRowName.TOTAL.ordinal()].getStolen());
//				log.info("ReturnA property by classification stolen total: " + stolenProperties[PropertyStolenByClassificationRowName.GRAND_TOTAL.ordinal()].getMonetaryValue());
//				log.info("debug"); 
			}
			
		}
		administrativeSegments.clear();
	}

	private void processRecoveredVehicleTotal(PropertyStolenByClassification[] stolenProperties,
			AdministrativeSegment administrativeSegment,
			SummaryReportRequest summaryReportRequest) {
		int totalRecoveredCount = 0;
		
		List<PropertySegment> properties =  administrativeSegment.getPropertySegments()
				.stream().filter(property->TypeOfPropertyLossCode._5.code.equals(property.getTypePropertyLossEtcType().getNibrsCode()))
				.collect(Collectors.toList());
		
		for (PropertySegment property: properties){
			boolean containsMotorVehicleCodes = property.getPropertyTypes().stream()
					.filter(propertyType -> propertyType.getRecoveredDate() != null && 
							Objects.equals(propertyType.getRecoveredDateType().getYearNum(),summaryReportRequest.getIncidentYear())
							&& (summaryReportRequest.getIncidentMonth() == null || summaryReportRequest.getIncidentMonth() == 0 ||
								Objects.equals(propertyType.getRecoveredDateType().getMonthNum(),summaryReportRequest.getIncidentMonth())))
					.map(propertyType -> propertyType.getPropertyDescriptionType().getNibrsCode())
					.anyMatch(code -> PropertyDescriptionCode.isMotorVehicleCode(code));
			
			int numberOfRecoveredMotorVehicles = Optional.ofNullable(property.getNumberOfRecoveredMotorVehicles()).orElse(0);
			
			if ( numberOfRecoveredMotorVehicles > 0 && containsMotorVehicleCodes){
				totalRecoveredCount += numberOfRecoveredMotorVehicles;
			}
		}
		
		stolenProperties[PropertyStolenByClassificationRowName.MOTOR_VEHICLES_TOTAL_LOCALLY_STOLEN_MOTOR_VEHICLES_RECOVERED.ordinal()]
			.increaseNumberOfOffenses(totalRecoveredCount);
		
	}

	private int getOffenseCountByConnectedVictim(AdministrativeSegment administrativeSegment, OffenseSegment offense) {
		long offenseCount = administrativeSegment.getVictimSegments()
				.stream()
				.filter(victim->victim.getOffenseSegments().contains(offense))
				.count();
		return Long.valueOf(offenseCount).intValue();
	}

	private int getOffenseCountByConnectedVictim(AdministrativeSegment administrativeSegment, String... offenseCodes) {
		int offenseCount = 0; 
		
		
		for (VictimSegment victimSegment: administrativeSegment.getVictimSegments()) {
			List<String> victimConnectedOffenseCodes = victimSegment.getConnectedOffenseCodes();
			
			boolean connectedToRape = CollectionUtils.containsAny(victimConnectedOffenseCodes, Arrays.asList(offenseCodes));
			if (connectedToRape) {
				offenseCount += 1; 
			}
			
		}
		
		return offenseCount;
	}
	
	private void processLarcenyStolenPropertyByNature(PropertyStolenByClassification[] stolenProperties, OffenseCode offenseCode, 
			AdministrativeSegment administrativeSegment) {
		
//		List<String> offenseCodes =  administrativeSegment.getOffenseSegments()
//				.stream()
//				.map(i -> i.getUcrOffenseCodeType().getNibrsCode())
//				.collect(Collectors.toList()); 
		List<String> larcenyOffenseCodes = administrativeSegment.getOffenseSegments()
				.stream()
				.map(i -> i.getUcrOffenseCodeType().getNibrsCode())
				.filter(OffenseCode::isLarcenyOffenseCode)
				.collect(Collectors.toList());
		
		List<String> convertedLarcenyOffenseCodes = 
				larcenyOffenseCodes.stream().map(i-> convert23H(i, administrativeSegment)).collect(Collectors.toList());
		
//		log.info("convertedLarcenyOffenseCodes:" + convertedLarcenyOffenseCodes);
		String offenseCodeString = StringUtils.EMPTY; 
		Integer larcenyOffenseImportance = 99; 
		for (String code: convertedLarcenyOffenseCodes) {
			Integer importanceOfCode = larcenyOffenseImportanceMap.get(code); 
			if (importanceOfCode != null && importanceOfCode < larcenyOffenseImportance) {
				larcenyOffenseImportance = importanceOfCode; 
				offenseCodeString = code; 
			}
		}
		
		
		PropertyStolenByClassificationRowName propertyStolenByClassificationRowName = larcenyOffenseByNatureMap.get(offenseCodeString);
//		if ("23D".equals(offenseCodeString)) {
//		if (propertyStolenByClassificationRowName == PropertyStolenByClassificationRowName.LARCENY_MOTOR_VEHICLE_PARTS_AND_ACCESSORIES) {
//			log.info("offenseCodes: " + offenseCodes);
//		}
		
		stolenProperties[propertyStolenByClassificationRowName.ordinal()].increaseNumberOfOffenses(1);
		stolenProperties[PropertyStolenByClassificationRowName.LARCENIES_TOTAL_BY_NATURE.ordinal()].increaseNumberOfOffenses(1);

		double stolenPropertyValue = getStolenPropertyValue(administrativeSegment, 0);
		stolenProperties[propertyStolenByClassificationRowName.ordinal()].increaseMonetaryValue(stolenPropertyValue);
//		if ("23D".equals(offenseCodeString)) {
//			log.info("propertyTypes:" + administrativeSegment.getPropertySegments()
//						.stream()
//						.filter(propertySegment -> propertySegment.getTypePropertyLossEtcType().getNibrsCode().equals("7"))
//						.flatMap(i->i.getPropertyTypes().stream())
//						.map(i->i.getValueOfProperty())
//						.collect(Collectors.toList()));
//			log.info("stolenPropertyValue: " + stolenPropertyValue);
//			log.info("stolenProperties[LARCENY_FROM_BUILDING]: " + stolenProperties[propertyStolenByClassificationRowName.ordinal()].getMonetaryValue());
//			incidentNumbers.add(administrativeSegment.getIncidentNumber());
//			log.info("23D incidentNumbers: " + StringUtils.join(incidentNumbers, ","));
//			propertyTypeIds.add(administrativeSegment.getAdministrativeSegmentId()); 
//			log.info("23D administrativeSegmentIds: " + StringUtils.join(propertyTypeIds, ","));
//		}
//		if (propertyStolenByClassificationRowName == PropertyStolenByClassificationRowName.LARCENY_MOTOR_VEHICLE_PARTS_AND_ACCESSORIES) {
//			log.debug("propertyTypes:" + administrativeSegment.getPropertySegments()
//						.stream()
//						.filter(propertySegment -> propertySegment.getTypePropertyLossEtcType().getNibrsCode().equals("7"))
//						.flatMap(i->i.getPropertyTypes().stream())
//						.map(i->i.getValueOfProperty())
//						.collect(Collectors.toList()));
//			log.debug("stolenPropertyValue: " + stolenPropertyValue);
//			log.debug("stolenProperties[LARCENY_MOTOR_VEHICLE_PARTS_AND_ACCESSORIES]: " + stolenProperties[propertyStolenByClassificationRowName.ordinal()].getMonetaryValue());
//			incidentNumbers.add(administrativeSegment.getIncidentNumber());
//			log.debug("23H38 23G incidentNumbers: " + StringUtils.join(incidentNumbers, ","));
//			propertyTypeIds.add(administrativeSegment.getAdministrativeSegmentId()); 
//			log.debug("23H38 23G administrativeSegmentIds: " + StringUtils.join(propertyTypeIds, ","));
//		}
		stolenProperties[PropertyStolenByClassificationRowName.LARCENIES_TOTAL_BY_NATURE.ordinal()].increaseMonetaryValue(stolenPropertyValue);
	}

	private String convert23H(String offenseCodeString, AdministrativeSegment administrativeSegment) {
		if ("23H".equals(offenseCodeString)){
			List<PropertyType> stolenPropertyTypes =  administrativeSegment.getPropertySegments()
					.stream()
					.filter(propertySegment -> propertySegment.getTypePropertyLossEtcType().getNibrsCode().equals("7"))
					.flatMap(i->i.getPropertyTypes().stream())
					.filter(i->i.getValueOfProperty() != null &&  i.getValueOfProperty() > 0)
					.collect(Collectors.toList());
			
			if (stolenPropertyTypes.size() > 0){
				PropertyType propertyTypeWithMaxValue = Collections.max(stolenPropertyTypes, Comparator.comparing(PropertyType::getValueOfProperty));
				if ("38".equals(propertyTypeWithMaxValue.getPropertyDescriptionType().getNibrsCode())
						|| "04".equals(propertyTypeWithMaxValue.getPropertyDescriptionType().getNibrsCode())){
					offenseCodeString += propertyTypeWithMaxValue.getPropertyDescriptionType().getNibrsCode(); 
				}
			}
		}
		return offenseCodeString;
	}

	private void processLarcenyStolenPropertyByValue(PropertyStolenByClassification[] stolenProperties, AdministrativeSegment administrativeSegment) {
		double stolenPropertyValue = getStolenPropertyValue(administrativeSegment, 0);
		PropertyStolenByClassificationRowName propertyStolenByClassificationRowName = null;
		
		if (stolenPropertyValue >= 200.0){
			propertyStolenByClassificationRowName = PropertyStolenByClassificationRowName.LARCENY_200_PLUS;
		}
		else if (stolenPropertyValue >= 50 ){
			propertyStolenByClassificationRowName = PropertyStolenByClassificationRowName.LARCENY_50_199;
		}
		else{
			propertyStolenByClassificationRowName = PropertyStolenByClassificationRowName.LARCENY_UNDER_50;
		}
		
		stolenProperties[propertyStolenByClassificationRowName.ordinal()].increaseNumberOfOffenses(1);
		stolenProperties[propertyStolenByClassificationRowName.ordinal()].increaseMonetaryValue(stolenPropertyValue);
		
//		if (stolenPropertyValue >= 200.0){
//			log.info("administrativeSegment ID: " + administrativeSegment.getAdministrativeSegmentId());
//			log.info("offenses: " + administrativeSegment.getOffenseSegments().stream().map(i->i.getUcrOffenseCodeType().getNibrsCode()).collect(Collectors.toList()));
//			log.info("$200 AND OVER total: " + stolenProperties[propertyStolenByClassificationRowName.ordinal()].getMonetaryValue());
//		}
		
		stolenProperties[PropertyStolenByClassificationRowName.LARCENY_TOTAL.ordinal()].increaseNumberOfOffenses(1);
		stolenProperties[PropertyStolenByClassificationRowName.LARCENY_TOTAL.ordinal()].increaseMonetaryValue(stolenPropertyValue);
		stolenProperties[PropertyStolenByClassificationRowName.GRAND_TOTAL.ordinal()].increaseNumberOfOffenses(1);
		stolenProperties[PropertyStolenByClassificationRowName.GRAND_TOTAL.ordinal()].increaseMonetaryValue(stolenPropertyValue);
	}

	private void processRobberyStolenPropertyByLocation(PropertyStolenByClassification[] stolenProperties,
			OffenseSegment offenseSegment) {
		String locationType = appProperties.getLocationCodeMapping().get(offenseSegment.getLocationType().getNibrsCode());
		if ( StringUtils.isNotBlank(locationType)){
			PropertyStolenByClassificationRowName rowName = PropertyStolenByClassificationRowName.valueOf("ROBBERY_" + locationType);
			stolenProperties[rowName.ordinal()].increaseNumberOfOffenses(1);
			stolenProperties[PropertyStolenByClassificationRowName.ROBBERY_TOTAL.ordinal()].increaseNumberOfOffenses(1);
			stolenProperties[PropertyStolenByClassificationRowName.GRAND_TOTAL.ordinal()].increaseNumberOfOffenses(1);
			
			Double stolenPropertyValue = getStolenPropertyValue(offenseSegment.getAdministrativeSegment(), 0);
			stolenProperties[rowName.ordinal()].increaseMonetaryValue(stolenPropertyValue);
			stolenProperties[PropertyStolenByClassificationRowName.ROBBERY_TOTAL.ordinal()].increaseMonetaryValue(stolenPropertyValue);
			stolenProperties[PropertyStolenByClassificationRowName.GRAND_TOTAL.ordinal()].increaseMonetaryValue(stolenPropertyValue);
		}
	}

	private void processStolenProperties(PropertyStolenByClassification[] stolenProperties,
			AdministrativeSegment administrativeSegment, PropertyStolenByClassificationRowName propertyStolenByClassificationRowName, int offenseCount) {
		double stolenPropertyValue;
		stolenProperties[propertyStolenByClassificationRowName.ordinal()].increaseNumberOfOffenses(offenseCount);
		stolenProperties[PropertyStolenByClassificationRowName.GRAND_TOTAL.ordinal()].increaseNumberOfOffenses(offenseCount);
		stolenPropertyValue = getStolenPropertyValue(administrativeSegment, 0);
		stolenProperties[propertyStolenByClassificationRowName.ordinal()].increaseMonetaryValue(stolenPropertyValue);
		stolenProperties[PropertyStolenByClassificationRowName.GRAND_TOTAL.ordinal()].increaseMonetaryValue(stolenPropertyValue);
	}

	private Double getStolenPropertyValue(AdministrativeSegment administrativeSegment, int lowerLimit) {
		return administrativeSegment.getPropertySegments()
				.stream()
				.filter(propertySegment -> propertySegment.getTypePropertyLossEtcType().getNibrsCode().equals("7"))
				.flatMap(i->i.getPropertyTypes().stream())
				.filter(i-> i.getValueOfProperty() != null && i.getValueOfProperty()> lowerLimit)
				.map(PropertyType::getValueOfProperty)
				.reduce(Double::sum).orElse(0.0);
	}

	private void sumPropertyValuesByType(ReturnAForm returnAForm, AdministrativeSegment administrativeSegment) {
		for (PropertySegment propertySegment: administrativeSegment.getPropertySegments()){
			List<PropertyType> propertyTypes = propertySegment.getPropertyTypes()
					.stream()
					.filter(propertyType -> propertyType.getValueOfProperty() != null)
					.collect(Collectors.toList()); 
			
			if (propertyTypes.size() > 0){
				for (PropertyType propertyType: propertyTypes){
					String propertyDescription = appProperties.getPropertyCodeMapping().get(propertyType.getPropertyDescriptionType().getNibrsCode());
					if (StringUtils.isNotBlank(propertyDescription)) {
						PropertyTypeValueRowName rowName = PropertyTypeValueRowName.valueOf(propertyDescription); 
						switch (propertySegment.getTypePropertyLossEtcType().getNibrsCode()){
						case "7":
							returnAForm.getPropertyTypeValues()[rowName.ordinal()].increaseStolen(propertyType.getValueOfProperty());
							returnAForm.getPropertyTypeValues()[PropertyTypeValueRowName.TOTAL.ordinal()].increaseStolen(propertyType.getValueOfProperty());
							break; 
						case "5":
							returnAForm.getPropertyTypeValues()[rowName.ordinal()].increaseRecovered(propertyType.getValueOfProperty());
//							if (rowName == PropertyTypeValueRowName.CURRENCY_NOTES_ETC) {
//								log.info("***********************************************************");
//								log.info("TypePropertyLossEtcType: " + propertySegment.getTypePropertyLossEtcType().getNibrsCode());
//								log.info("administrativeSegmentId: " + administrativeSegment.getAdministrativeSegmentId());
//								log.info("incidentNumber: " + administrativeSegment.getIncidentNumber());
//								log.info("incidentDate: " + administrativeSegment.getIncidentDate());
//								log.info("propertySegmentId: " + propertySegment.getPropertySegmentId());
//								log.info("propertyTypeId: " + propertyType.getPropertyTypeId());
//								log.info("property description: " + propertyType.getPropertyDescriptionType().getNibrsCode());
//								log.info("valueOfProperty(): " + propertyType.getValueOfProperty());
//								log.info("recovered Date: " + propertyType.getRecoveredDate());
//								log.info("recovered currency amount total: " + returnAForm.getPropertyTypeValues()[rowName.ordinal()].getRecovered());
//							}
//							if (rowName == PropertyTypeValueRowName.CONSUMABLE_GOODS) {
//								propertyTypeIds.add(propertyType.getPropertyTypeId());
//								log.info("***********************************************************");
//								log.info("incidentNumber: " + administrativeSegment.getIncidentNumber());
//								log.info("property description: " + propertyType.getPropertyDescriptionType().getNibrsCode());
//								log.info("valueOfProperty(): " + propertyType.getValueOfProperty());
//								log.info("incidentDate: " + administrativeSegment.getIncidentDate());
//								log.info("TypePropertyLossEtcType: " + propertySegment.getTypePropertyLossEtcType().getNibrsCode());
//								log.info("administrativeSegmentId: " + administrativeSegment.getAdministrativeSegmentId());
//								log.info("propertySegmentId: " + propertySegment.getPropertySegmentId());
//								log.info("propertyTypeId: " + propertyType.getPropertyTypeId());
//								log.info("recovered Date: " + propertyType.getRecoveredDate());
//								log.info("recovered currency amount total: " + returnAForm.getPropertyTypeValues()[rowName.ordinal()].getRecovered());
//								log.info("propertyIds so far: " + StringUtils.join(propertyTypeIds, ",")); 
//							}
							returnAForm.getPropertyTypeValues()[PropertyTypeValueRowName.TOTAL.ordinal()].increaseRecovered(propertyType.getValueOfProperty());
							break; 
						default:
						}
					}
				}
			}
			
		}
	}

	private void fillTheMotorVehicleTheftTotalRow(ReturnAForm returnAForm) {
		ReturnARowName totalRow = ReturnARowName.MOTOR_VEHICLE_THEFT_TOTAL; 
		
		fillTheTotalRow(returnAForm, totalRow, ReturnARowName.AUTOS_THEFT, 
				ReturnARowName.TRUCKS_BUSES_THEFT,
				ReturnARowName.OTHER_VEHICLES_THEFT);
	}

	private void fillTheBurglaryTotalRow(ReturnAForm returnAForm) {
		ReturnARowName totalRow = ReturnARowName.BURGLARY_TOTAL; 
		
		fillTheTotalRow(returnAForm, totalRow, ReturnARowName.FORCIBLE_ENTRY_BURGLARY, 
				ReturnARowName.UNLAWFUL_ENTRY_NO_FORCE_BURGLARY,
				ReturnARowName.ATTEMPTED_FORCIBLE_ENTRY_BURGLARY);
	}

	private void fillTheAssaultTotalRow(ReturnAForm returnAForm) {
		ReturnARowName totalRow = ReturnARowName.ASSAULT_TOTAL; 
		
		fillTheTotalRow(returnAForm, totalRow, ReturnARowName.FIREARM_ASSAULT, 
				ReturnARowName.KNIFE_CUTTING_INSTRUMENT_ASSAULT,
				ReturnARowName.OTHER_DANGEROUS_WEAPON_ASSAULT, 
				ReturnARowName.HANDS_FISTS_FEET_AGGRAVATED_INJURY_ASSAULT, 
				ReturnARowName.OTHER_ASSAULT_NOT_AGGRAVATED);
	}

	private void fillTheGrandTotalRow(ReturnAForm returnAForm) {
		ReturnARowName totalRow = ReturnARowName.GRAND_TOTAL; 
		
		fillTheTotalRow(returnAForm, totalRow, ReturnARowName.MURDER_NONNEGLIGENT_HOMICIDE, 
				ReturnARowName.MANSLAUGHTER_BY_NEGLIGENCE,
				ReturnARowName.FORCIBLE_RAPE_TOTAL, 
				ReturnARowName.ROBBERY_TOTAL, 
				ReturnARowName.ASSAULT_TOTAL, 
				ReturnARowName.BURGLARY_TOTAL, 
				ReturnARowName.LARCENY_THEFT_TOTAL, 
				ReturnARowName.MOTOR_VEHICLE_THEFT_TOTAL);
		
	}

	private void fillTheTotalRow(ReturnAForm returnAForm, ReturnARowName totalRow, ReturnARowName... rowsArray) {
		List<ReturnARowName> rows = Arrays.asList(rowsArray);
		int totalReportedOffense = 
				rows.stream()
					.mapToInt(row -> returnAForm.getRows()[row.ordinal()].getReportedOffenses())
					.sum(); 
		returnAForm.getRows()[totalRow.ordinal()].setReportedOffenses(totalReportedOffense);
		
		int totalUnfoundedOffense = 
				rows.stream()
				.mapToInt(row -> returnAForm.getRows()[row.ordinal()].getUnfoundedOffenses())
				.sum(); 
		returnAForm.getRows()[totalRow.ordinal()].setUnfoundedOffenses(totalUnfoundedOffense);
		
		int totalClearedOffense = 
				rows.stream()
				.mapToInt(row -> returnAForm.getRows()[row.ordinal()].getClearedOffenses())
				.sum(); 
		returnAForm.getRows()[totalRow.ordinal()].setClearedOffenses(totalClearedOffense);
		
		int totalClearanceInvolvingJuvenile = 
				rows.stream()
				.mapToInt(row -> returnAForm.getRows()[row.ordinal()].getClearanceInvolvingOnlyJuvenile())
				.sum(); 
		returnAForm.getRows()[totalRow.ordinal()].setClearanceInvolvingOnlyJuvenile(totalClearanceInvolvingJuvenile);
	}

	private void fillRecordCardTotalRow(ReturnARecordCard returnARecordCard, ReturnARecordCardRowName totalRow, 
			ReturnARecordCardRowName... rowsArray) {
		List<ReturnARecordCardRowName> rows = Arrays.asList(rowsArray);
		int totalReportedOffense = 
				rows.stream()
				.mapToInt(row -> returnARecordCard.getRows()[row.ordinal()].getTotal())
				.sum(); 
		returnARecordCard.getRows()[totalRow.ordinal()].setTotal(totalReportedOffense);
		returnARecordCard.getReturnAFormRows()[totalRow.ordinal()].setReportedOffenses(totalReportedOffense);
		
		int firstHalfTotalReportedOffense = 
				rows.stream()
				.mapToInt(row -> returnARecordCard.getRows()[row.ordinal()].getFirstHalfSubtotal())
				.sum(); 
		returnARecordCard.getRows()[totalRow.ordinal()].setFirstHalfSubtotal(firstHalfTotalReportedOffense);
		
		int secondHalfTotalReportedOffense = 
				rows.stream()
				.mapToInt(row -> returnARecordCard.getRows()[row.ordinal()].getSecondHalfSubtotal())
				.sum(); 
		returnARecordCard.getRows()[totalRow.ordinal()].setSecondHalfSubtotal(secondHalfTotalReportedOffense);
		
		for (int i=0; i<12; i++) {
			final int j = i; 
			int monthTotal = 
					rows.stream()
					.mapToInt(row -> returnARecordCard.getRows()[row.ordinal()].getMonths()[j])
					.sum();  
			returnARecordCard.getRows()[totalRow.ordinal()].getMonths()[j] = monthTotal;
		}

		int totalClearedOffenses = rows.stream()
				.mapToInt(row -> returnARecordCard.getReturnAFormRows()[row.ordinal()].getClearedOffenses())
				.sum(); 
		returnARecordCard.getReturnAFormRows()[totalRow.ordinal()].setClearedOffenses(totalClearedOffenses);

		int totalClearedJuvenilOffenses = rows.stream()
				.mapToInt(row -> returnARecordCard.getReturnAFormRows()[row.ordinal()].getClearanceInvolvingOnlyJuvenile())
				.sum(); 
		returnARecordCard.getReturnAFormRows()[totalRow.ordinal()].setClearanceInvolvingOnlyJuvenile(totalClearedJuvenilOffenses);
		
	}
	
	private void fillTheRobberyTotalRow(ReturnAForm returnAForm) {
		ReturnARowName totalRow = ReturnARowName.ROBBERY_TOTAL; 
		
		fillTheTotalRow(returnAForm, totalRow, ReturnARowName.FIREARM_ROBBERY, 
				ReturnARowName.KNIFE_CUTTING_INSTRUMENT_ROBBERY,
				ReturnARowName.OTHER_DANGEROUS_WEAPON_ROBBERY,
				ReturnARowName.STRONG_ARM_ROBBERY);
	}

	private void fillTheForcibleRapeTotalRow(ReturnAForm returnAForm) {
		ReturnARowName totalRow = ReturnARowName.FORCIBLE_RAPE_TOTAL; 
		
		fillTheTotalRow(returnAForm, totalRow, ReturnARowName.RAPE_BY_FORCE, 
				ReturnARowName.ATTEMPTS_TO_COMMIT_FORCIBLE_RAPE);
	}

	private boolean countMotorVehicleTheftOffense(ReturnAForm returnAForm, OffenseSegment offense) {
		
		int totalOffenseCount = 0;
		if ("A".equals(offense.getOffenseAttemptedCompleted())){
			returnAForm.getRows()[ReturnARowName.AUTOS_THEFT.ordinal()].increaseReportedOffenses(1);
			totalOffenseCount = 1;
		}
		else {
			List<PropertySegment> properties =  offense.getAdministrativeSegment().getPropertySegments()
					.stream().filter(property->TypeOfPropertyLossCode._7.code.equals(property.getTypePropertyLossEtcType().getNibrsCode()))
					.collect(Collectors.toList());
			
			for (PropertySegment property: properties){
				int offenseCountInThisProperty = 0;
				List<String> motorVehicleCodes = property.getPropertyTypes().stream()
						.map(propertyType -> propertyType.getPropertyDescriptionType().getNibrsCode())
						.filter(code -> PropertyDescriptionCode.isMotorVehicleCode(code))
						.collect(Collectors.toList()); 
				
				int numberOfStolenMotorVehicles = Optional.ofNullable(property.getNumberOfStolenMotorVehicles()).orElse(0);
				
//				log.info("offense.getOffenseAttemptedCompleted():" + offense.getOffenseAttemptedCompleted()); 
				if ( numberOfStolenMotorVehicles > 0){
					offenseCountInThisProperty += numberOfStolenMotorVehicles;
					if (motorVehicleCodes.contains(PropertyDescriptionCode._03.code)){
						for (String code: motorVehicleCodes){
							switch (code){
							case "05":
							case "28": 
							case "37": 
								numberOfStolenMotorVehicles --; 
								returnAForm.getRows()[ReturnARowName.TRUCKS_BUSES_THEFT.ordinal()].increaseReportedOffenses(1);
								break; 
							case "24": 
								numberOfStolenMotorVehicles --; 
								returnAForm.getRows()[ReturnARowName.OTHER_VEHICLES_THEFT.ordinal()].increaseReportedOffenses(1);
								break; 
							}
						}
						
						if (numberOfStolenMotorVehicles > 0){
							returnAForm.getRows()[ReturnARowName.AUTOS_THEFT.ordinal()].increaseReportedOffenses(numberOfStolenMotorVehicles);
						}
					}
					else if (CollectionUtils.containsAny(motorVehicleCodes, 
							Arrays.asList(PropertyDescriptionCode._05.code, PropertyDescriptionCode._28.code, PropertyDescriptionCode._37.code))){
						int countOfOtherVehicles = Long.valueOf(motorVehicleCodes.stream()
								.filter(code -> code.equals(PropertyDescriptionCode._24.code)).count()).intValue();
						numberOfStolenMotorVehicles -= countOfOtherVehicles;
						returnAForm.getRows()[ReturnARowName.OTHER_VEHICLES_THEFT.ordinal()].increaseReportedOffenses(countOfOtherVehicles);
						
						if (numberOfStolenMotorVehicles > 0){
							returnAForm.getRows()[ReturnARowName.TRUCKS_BUSES_THEFT.ordinal()].increaseReportedOffenses(numberOfStolenMotorVehicles);
						}
					}
					else if (motorVehicleCodes.contains(PropertyDescriptionCode._24.code)){
						returnAForm.getRows()[ReturnARowName.OTHER_VEHICLES_THEFT.ordinal()].increaseReportedOffenses(numberOfStolenMotorVehicles);
					}
				}
				totalOffenseCount += offenseCountInThisProperty;
				
				if (offenseCountInThisProperty > 0){
					double valueOfStolenProperty = getStolenPropertyValue(offense.getAdministrativeSegment(), 0);
					returnAForm.getPropertyStolenByClassifications()
						[PropertyStolenByClassificationRowName.MOTOR_VEHICLE_THEFT.ordinal()]
							.increaseMonetaryValue(valueOfStolenProperty);
					returnAForm.getPropertyStolenByClassifications()
						[PropertyStolenByClassificationRowName.GRAND_TOTAL.ordinal()]
						.increaseMonetaryValue(valueOfStolenProperty);
				}
	
			}
		}		
		returnAForm.getPropertyStolenByClassifications()
			[PropertyStolenByClassificationRowName.MOTOR_VEHICLE_THEFT.ordinal()]
					.increaseNumberOfOffenses(totalOffenseCount);
		returnAForm.getPropertyStolenByClassifications()
			[PropertyStolenByClassificationRowName.GRAND_TOTAL.ordinal()]
				.increaseNumberOfOffenses(totalOffenseCount);
		return totalOffenseCount > 0; 
	}

	private int countBurglaryOffense(ReturnAForm returnAForm, OffenseSegment offense) {
		ReturnARowName returnARowName = getBurglaryRow(offense, ReturnARowName.class);
		
		int burglaryOffenseCount = 0; 
//		If there is an entry in Data Element 10 (Number of Premises Entered) and an entry of 19 
//		(Rental Storage Facility) in Data Element 9 (Location Type), use the number of premises 
//		listed in Data Element 10 as the number of burglaries to be counted.
		if (returnARowName != null){
			int numberOfPremisesEntered = Optional.ofNullable(offense.getNumberOfPremisesEntered()).orElse(0);
//			log.info("numberOfPremisesEntered:" + numberOfPremisesEntered);
//			log.info("LocationTypeCode._19.code.equals(offense.getLocationType().getNibrsCode()):" + LocationTypeCode._19.code.equals(offense.getLocationType().getNibrsCode()));
			if ( numberOfPremisesEntered > 0 
					&& LocationTypeCode._19.code.equals(offense.getLocationType().getNibrsCode())){
				burglaryOffenseCount = offense.getNumberOfPremisesEntered();
			}
			else {
				burglaryOffenseCount = 1; 
			}
			
			returnAForm.getRows()[returnARowName.ordinal()].increaseReportedOffenses(burglaryOffenseCount);
		}
		
		if (burglaryOffenseCount > 0){
			PropertyStolenByClassificationRowName propertyStolenByClassificationRowName = 
					getPropertyStolenByClassificationBurglaryRowName(offense.getLocationType().getNibrsCode(), offense.getAdministrativeSegment().getIncidentHour());
			returnAForm.getPropertyStolenByClassifications()[propertyStolenByClassificationRowName.ordinal()]
					.increaseNumberOfOffenses(burglaryOffenseCount);
			returnAForm.getPropertyStolenByClassifications()[PropertyStolenByClassificationRowName.BURGLARY_TOTAL.ordinal()]
					.increaseNumberOfOffenses(burglaryOffenseCount);
			returnAForm.getPropertyStolenByClassifications()[PropertyStolenByClassificationRowName.GRAND_TOTAL.ordinal()]
					.increaseNumberOfOffenses(burglaryOffenseCount);
			
			double stolenPropertyValue = getStolenPropertyValue(offense.getAdministrativeSegment(), 0);
			returnAForm.getPropertyStolenByClassifications()[propertyStolenByClassificationRowName.ordinal()]
					.increaseMonetaryValue(stolenPropertyValue);
			returnAForm.getPropertyStolenByClassifications()[PropertyStolenByClassificationRowName.BURGLARY_TOTAL.ordinal()]
					.increaseMonetaryValue(stolenPropertyValue);
			returnAForm.getPropertyStolenByClassifications()[PropertyStolenByClassificationRowName.GRAND_TOTAL.ordinal()]
					.increaseMonetaryValue(stolenPropertyValue);
		}
		return burglaryOffenseCount; 
	}

	private PropertyStolenByClassificationRowName getPropertyStolenByClassificationBurglaryRowName(String locationCode,
			String incidentHour) {
		PropertyStolenByClassificationRowName propertyStolenByClassificationRowName = null;
		if (LocationTypeCode._20.code.equals(locationCode)){
			if (StringUtils.isBlank(incidentHour)){
				propertyStolenByClassificationRowName = PropertyStolenByClassificationRowName.BURGLARY_RESIDENCE_UNKNOWN;
			}
			else if (Integer.valueOf(incidentHour) >= 6 && Integer.valueOf(incidentHour) < 18){
				propertyStolenByClassificationRowName = PropertyStolenByClassificationRowName.BURGLARY_RESIDENCE_DAY; 
			}
			else{
				propertyStolenByClassificationRowName = PropertyStolenByClassificationRowName.BURGLARY_RESIDENCE_NIGHT; 
			}
		}
		else{
			if (StringUtils.isBlank(incidentHour)){
				propertyStolenByClassificationRowName = PropertyStolenByClassificationRowName.BURGLARY_NON_RESIDENCE_UNKNOWN;
			}
			else if (Integer.valueOf(incidentHour) >= 6 && Integer.valueOf(incidentHour) < 18){
				propertyStolenByClassificationRowName = PropertyStolenByClassificationRowName.BURGLARY_NON_RESIDENCE_DAY; 
			}
			else{
				propertyStolenByClassificationRowName = PropertyStolenByClassificationRowName.BURGLARY_NON_RESIDENCE_NIGHT; 
			}
		}
		return propertyStolenByClassificationRowName;
	}

	private <T extends Enum<T>> T getRowNameFor13B13COffense(OffenseSegment offense, Class<T> enumType) {
		
		List<String> typeOfWeaponForceInvolved = offense.getTypeOfWeaponForceInvolveds()
				.stream().map(TypeOfWeaponForceInvolved::getTypeOfWeaponForceInvolvedType)
				.map(TypeOfWeaponForceInvolvedType::getNibrsCode)
				.collect(Collectors.toList());
//		log.debug("TypeOfWeaponForceInvolveds:" + typeOfWeaponForceInvolved);
		T rowName = null; 
		boolean containsValidWeaponForceType = 
				offense.getTypeOfWeaponForceInvolveds()
				.stream()
				.filter(type -> Arrays.asList("40", "90", "95", "99", " ").contains(type.getTypeOfWeaponForceInvolvedType().getNibrsCode()))
				.count() > 0 || typeOfWeaponForceInvolved.isEmpty();
				
		if (containsValidWeaponForceType){
			rowName = Enum.valueOf(enumType, "OTHER_ASSAULT_NOT_AGGRAVATED");
		}
		return rowName;
	}
	
	private <T extends Enum<T>> T getRowNameForRobbery(OffenseSegment offense, Class<T> enumType) {
		List<String> typeOfWeaponInvolvedCodes = offense.getTypeOfWeaponForceInvolveds()
				.stream()
				.map(TypeOfWeaponForceInvolved::getTypeOfWeaponForceInvolvedType)
				.map(TypeOfWeaponForceInvolvedType::getNibrsCode)
				.collect(Collectors.toList()); 

		if (CollectionUtils.containsAny(typeOfWeaponInvolvedCodes, Arrays.asList("11", "12", "13", "14", "15"))){
			return Enum.valueOf(enumType, "FIREARM_ROBBERY"); 
		}
		else if (typeOfWeaponInvolvedCodes.contains("20")){
			return Enum.valueOf(enumType, "KNIFE_CUTTING_INSTRUMENT_ROBBERY"); 
		}
		else if (CollectionUtils.containsAny(typeOfWeaponInvolvedCodes, 
				Arrays.asList("30", "35", "50", "60", "65", "70", "85", "90", "95"))){
			return Enum.valueOf(enumType, "OTHER_DANGEROUS_WEAPON_ROBBERY"); 
		}
		else if (CollectionUtils.containsAny(typeOfWeaponInvolvedCodes, 
				Arrays.asList("40", "99"))){
			return Enum.valueOf(enumType, "STRONG_ARM_ROBBERY"); 
		}
			
		return null;
	}

	private <T extends Enum<T>> T getRowNameForAssault(OffenseSegment offense, Class<T> enumType) {
		List<String> typeOfWeaponInvolvedCodes = offense.getTypeOfWeaponForceInvolveds()
				.stream()
				.map(TypeOfWeaponForceInvolved::getTypeOfWeaponForceInvolvedType)
				.map(TypeOfWeaponForceInvolvedType::getNibrsCode)
				.collect(Collectors.toList()); 
		
		if (CollectionUtils.containsAny(typeOfWeaponInvolvedCodes, Arrays.asList("11", "12", "13", "14", "15"))){
			return Enum.valueOf(enumType, "FIREARM_ASSAULT");
		}
		else if (typeOfWeaponInvolvedCodes.contains("20")){
			return Enum.valueOf(enumType, "KNIFE_CUTTING_INSTRUMENT_ASSAULT"); 
		}
		else if (CollectionUtils.containsAny(typeOfWeaponInvolvedCodes, 
				Arrays.asList("30", "35", "50", "60", "65", "70", "85", "90", "95"))){
			return Enum.valueOf(enumType, "OTHER_DANGEROUS_WEAPON_ASSAULT"); 
		}
		else if (CollectionUtils.containsAny(typeOfWeaponInvolvedCodes, 
				Arrays.asList("40", "99"))){
			return Enum.valueOf(enumType, "HANDS_FISTS_FEET_AGGRAVATED_INJURY_ASSAULT"); 
		}
		
		return null;
	}
	
	private <T extends Enum<T>> T getRowNameFor11AOffense(AdministrativeSegment administrativeSegment,
			OffenseSegment offense, Class<T> enumType) {
		
		T rowName = null;
		boolean containsCompletedRapeOffense = administrativeSegment.getOffenseSegments()
				.stream()
				.filter(item -> OffenseCode.isReturnARapeCode(item.getUcrOffenseCodeType().getNibrsCode()))
				.anyMatch(item->"C".equals(item.getOffenseAttemptedCompleted()));
		boolean containsAttemptedRapeOffense = administrativeSegment.getOffenseSegments()
				.stream()
				.filter(item -> OffenseCode.isReturnARapeCode(item.getUcrOffenseCodeType().getNibrsCode()))
				.anyMatch(item->"A".equals(item.getOffenseAttemptedCompleted()));
		List<VictimSegment> victimSegments = administrativeSegment.getVictimSegments()
			.stream().filter(victim->CollectionUtils.containsAny(victim.getConnectedOffenseCodes(), Arrays.asList("11A", "11B", "11C")))
			.filter(victim->Arrays.asList("F", "M").contains(victim.getSexOfPersonType().getNibrsCode()))
			.collect(Collectors.toList());
		if (victimSegments.size() > 0){
			if (containsCompletedRapeOffense){
				rowName = Enum.valueOf(enumType, "RAPE_BY_FORCE");
			}
			else if (containsAttemptedRapeOffense){
				rowName = Enum.valueOf(enumType, "ATTEMPTS_TO_COMMIT_FORCIBLE_RAPE");
			} 
		}
		
		return rowName;
	}

	
	private List<OffenseSegment> getReturnAOffenses(AdministrativeSegment administrativeSegment) {
		List<OffenseSegment> offenses = new ArrayList<>(); 
		
		OffenseSegment reportingOffense = null; 
		Integer reportingOffenseValue = 99; 
		
//		List<String> offenseCodes = administrativeSegment.getOffenseSegments()
//				.stream().map(offense->offense.getUcrOffenseCodeType().getNibrsCode())
//				.collect(Collectors.toList());
		for (OffenseSegment offense: administrativeSegment.getOffenseSegments()){
//			if (offense.getUcrOffenseCodeType().getNibrsCode().startsWith("23")
//					&& CollectionUtils.containsAny(offenseCodes, Arrays.asList("09A", "09B", "11A", "11B", "11C", "120", "13A", "13B", "13C", "220" ))) {
//				log.info("Larcency Offense Not Added");
//				log.info("OffenseCodes: " + offenseCodes);
//				log.info("offense.getOffenseAttemptedCompleted():" + offense.getOffenseAttemptedCompleted());
//			}
			if (!Arrays.asList("A", "C").contains(offense.getOffenseAttemptedCompleted())){
				continue;
			}
			
			if (offense.getUcrOffenseCodeType().getNibrsCode().equals(OffenseCode._09C.code)){
				offenses.add(offense);
				continue;
			}
			Integer offenseValue = Optional.ofNullable(partIOffensesMap.get(offense.getUcrOffenseCodeType().getNibrsCode())).orElse(99); 
			
			if (offenseValue < reportingOffenseValue){
//				if (reportingOffense!= null && reportingOffense.getUcrOffenseCodeType().getNibrsCode().equals("220")) {
//					log.info("220 added against the rule");
//					int numberOfPremisesEntered = Optional.ofNullable(reportingOffense.getNumberOfPremisesEntered()).orElse(0);
//					log.info("numberOfPremisesEntered: " + numberOfPremisesEntered);
//					log.info("LocationTypeCode._19.code.equals(offense.getLocationType().getNibrsCode()): " + LocationTypeCode._19.code.equals(reportingOffense.getLocationType().getNibrsCode()));
//					offenses.add(reportingOffense);
//					log.info("reportingOffense: " + offense.getUcrOffenseCodeType().getNibrsCode());
//				}
				reportingOffense = offense; 
				reportingOffenseValue = offenseValue; 
			}
//			else if (offense.getUcrOffenseCodeType().getNibrsCode().equals("220")) {
//				offenses.add(offense);
////				log.info("administrativeSegmentID: " + offense.getAdministrativeSegment().getAdministrativeSegmentId());
//				int numberOfPremisesEntered = Optional.ofNullable(offense.getNumberOfPremisesEntered()).orElse(0);
//				log.info("220 added against the rule");
//				log.info("numberOfPremisesEntered: " + numberOfPremisesEntered);
//				log.info("LocationTypeCode._19.code.equals(offense.getLocationType().getNibrsCode()): " + LocationTypeCode._19.code.equals(offense.getLocationType().getNibrsCode()));
//				log.info("reportingOffense: " + reportingOffense.getUcrOffenseCodeType().getNibrsCode());
//			}
		}
		
		if (reportingOffense != null){
			offenses.add(reportingOffense);
		}
		return offenses;
	}
	
	
}
