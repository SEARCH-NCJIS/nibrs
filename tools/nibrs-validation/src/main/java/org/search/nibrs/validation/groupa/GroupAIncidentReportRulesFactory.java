/*
 * Copyright 2016 Research Triangle Institute
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
package org.search.nibrs.validation.groupa;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.search.nibrs.common.NIBRSError;
import org.search.nibrs.common.ParsedObject;
import org.search.nibrs.model.AbstractSegment;
import org.search.nibrs.model.ArresteeSegment;
import org.search.nibrs.model.GroupAIncidentReport;
import org.search.nibrs.model.NIBRSAge;
import org.search.nibrs.model.OffenderSegment;
import org.search.nibrs.model.OffenseSegment;
import org.search.nibrs.model.PropertySegment;
import org.search.nibrs.model.VictimSegment;
import org.search.nibrs.model.codes.AggravatedAssaultHomicideCircumstancesCode;
import org.search.nibrs.model.codes.CargoTheftIndicatorCode;
import org.search.nibrs.model.codes.ClearedExceptionallyCode;
import org.search.nibrs.model.codes.NIBRSErrorCode;
import org.search.nibrs.model.codes.OffenseCode;
import org.search.nibrs.model.codes.PropertyDescriptionCode;
import org.search.nibrs.model.codes.RaceCode;
import org.search.nibrs.model.codes.RelationshipOfVictimToOffenderCode;
import org.search.nibrs.model.codes.SexCode;
import org.search.nibrs.model.codes.TypeOfPropertyLossCode;
import org.search.nibrs.validation.ValidatorProperties;
import org.search.nibrs.validation.rules.BlankRightFillStringRule;
import org.search.nibrs.validation.rules.NotBlankRule;
import org.search.nibrs.validation.rules.NumericValueRule;
import org.search.nibrs.validation.rules.Rule;
import org.search.nibrs.validation.rules.StringValueRule;
import org.search.nibrs.validation.rules.ValidNIBRSIdentifierFormatRule;
import org.search.nibrs.validation.rules.ValidValueListRule;

/**
 * Factory class that provides Rule implementations to validate the elements contained on the Group A report administrative segment.
 */
public class GroupAIncidentReportRulesFactory {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(GroupAIncidentReportRulesFactory.class);
	
	private static final class DuplicateSegmentIdentifierRule<T extends AbstractSegment> implements Rule<GroupAIncidentReport> {
		
		private Function<GroupAIncidentReport, List<T>> segmentProducer;
		private String dataElementIdentifier;
		private NIBRSErrorCode errorCode;
		

		public DuplicateSegmentIdentifierRule(Function<GroupAIncidentReport, List<T>> segmentProducer, String dataElementIdentifier, NIBRSErrorCode errorCode) {
			this.segmentProducer = segmentProducer;
			this.dataElementIdentifier = dataElementIdentifier;
			this.errorCode = errorCode;
		}
		
		@Override
		public NIBRSError apply(GroupAIncidentReport subject) {
			NIBRSError ret = null;
			Set<Object> identifiers = new HashSet<>();
			Set<Object> dups = new HashSet<>();
			for (T s : segmentProducer.apply(subject)) {
				Object identifier = s.getWithinSegmentIdentifier();
				if (identifiers.contains(identifier)) {
					dups.add(identifier);
				} else {
					identifiers.add(identifier);
				}
			}
			if (!dups.isEmpty()) {
				ret = subject.getErrorTemplate();
				ret.setValue(dups);
				ret.setDataElementIdentifier(dataElementIdentifier);
				ret.setNIBRSErrorCode(errorCode);
			}
			return ret;
		}
		
	}

	private static abstract class IncidentDateRule implements Rule<GroupAIncidentReport> {
		@Override
		public NIBRSError apply(GroupAIncidentReport subject) {
			NIBRSError ret = null;
			Integer month = subject.getMonthOfTape();
			Integer year = subject.getYearOfTape();
			if (month != null && month > 0 && month < 13 && year != null) {
				ParsedObject<LocalDate> incidentDatePO = subject.getIncidentDate();
				if (!(incidentDatePO.isMissing() || incidentDatePO.isInvalid())) {
					ret = compareIncidentDateToTape(month, year, incidentDatePO.getValue(), subject.getErrorTemplate());
				}
			}
			return ret;
		}

		protected abstract NIBRSError compareIncidentDateToTape(Integer month, Integer year, LocalDate incidentDate, NIBRSError errorTemplate);
		
	}

	private List<Rule<GroupAIncidentReport>> rulesList = new ArrayList<>();
	private Set<String> cargoTheftOffenses = new HashSet<>();
	private Set<String> trueExceptionalClearanceCodes = new HashSet<>();
	private ValidatorProperties validatorProperties;

	public static GroupAIncidentReportRulesFactory instance(ValidatorProperties validatorProperties) {
		return new GroupAIncidentReportRulesFactory(validatorProperties);
	}
	
	private GroupAIncidentReportRulesFactory() {
		
		cargoTheftOffenses.add(OffenseCode._120.code);
		cargoTheftOffenses.add(OffenseCode._210.code);
		cargoTheftOffenses.add(OffenseCode._220.code);
		cargoTheftOffenses.add(OffenseCode._23D.code);
		cargoTheftOffenses.add(OffenseCode._23F.code);
		cargoTheftOffenses.add(OffenseCode._23H.code);
		cargoTheftOffenses.add(OffenseCode._240.code);
		cargoTheftOffenses.add(OffenseCode._26A.code);
		cargoTheftOffenses.add(OffenseCode._26B.code);
		cargoTheftOffenses.add(OffenseCode._26C.code);
		cargoTheftOffenses.add(OffenseCode._26E.code);
		cargoTheftOffenses.add(OffenseCode._26F.code);
		cargoTheftOffenses.add(OffenseCode._26G.code);
		cargoTheftOffenses.add(OffenseCode._510.code);
		cargoTheftOffenses.add(OffenseCode._270.code);
		
		trueExceptionalClearanceCodes = ClearedExceptionallyCode.codeSet();
		trueExceptionalClearanceCodes.remove(ClearedExceptionallyCode.N.code);
		
		rulesList.add(getRule101("ori", "1"));
		rulesList.add(getRule101("incidentNumber", "2"));
		rulesList.add(getRule101("yearOfTape", "Year of Tape"));
		rulesList.add(getRule101("monthOfTape", "Month of Tape"));
		rulesList.add(getRule101("incidentDate", "3"));
		rulesList.add(getRule101("exceptionalClearanceCode", "4"));
		rulesList.add(getRule104("reportDateIndicator"));
		rulesList.add(getRule104("yearOfTape"));
		rulesList.add(getRule104("monthOfTape"));
		rulesList.add(getRule104("cargoTheftIndicator"));
		rulesList.add(getRule105IncidentDate());
		rulesList.add(getRule105ExceptionalClearanceDate());
		rulesList.add(getRule106());
		rulesList.add(getRule115());
		rulesList.add(getRule117());
		rulesList.add(getRule119());
		rulesList.add(getRule122());
		rulesList.add(getRule152());
		rulesList.add(getRule153());
		rulesList.add(getRule155());
		rulesList.add(getRule156());
		rulesList.add(getRule170());
		rulesList.add(getRule172());
		rulesList.add(getRule072());
		rulesList.add(getRule071());
		rulesList.add(getRule073());
		rulesList.add(getRule074());
		rulesList.add(getRule075());
		rulesList.add(getRule076());
		rulesList.add(getRule077());
		rulesList.add(getRule078());
		rulesList.add(getRule080());
		rulesList.add(getRule081());
		rulesList.add(getRule084());
		rulesList.add(getRule262());
		rulesList.add(getRule376());
		rulesList.add(getRule451());
		rulesList.add(getRule551());
		rulesList.add(getRule661());
		rulesList.add(getRule263());
		rulesList.add(getRule266());
		rulesList.add(getRule268());
		rulesList.add(getRule382());
		rulesList.add(getRule404VictimSequenceNumber());
		rulesList.add(getRule466());
		rulesList.add(getRule470());
		rulesList.add(getRule474());
		rulesList.add(getRule480());
		rulesList.add(getRule555());
		rulesList.add(getRule558());
		rulesList.add(getRule559());
		rulesList.add(getRule669());
		rulesList.add(getRule656());
		rulesList.add(getRule560());
		
	}
	
	public GroupAIncidentReportRulesFactory(ValidatorProperties validatorProperties) {
		this();
		this.validatorProperties = validatorProperties; 
	}

	Rule<GroupAIncidentReport> getRule560() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				
				List<VictimSegment> victimsOfRape = subject.getVictims().stream()
						.filter(VictimSegment::isVictimOfRape)
						.collect(Collectors.toList());
				
				if (victimsOfRape.size() > 0) {
					for (VictimSegment vs : victimsOfRape) {
						List<Integer> validRelatedOffenderNumbers = vs.getDistinctValidRelatedOffenderNumberList(); 
						if (validRelatedOffenderNumbers.size() > 0 
							 && !validRelatedOffenderNumbers.contains(Integer.valueOf(0))){;
							String victimSex = vs.getSex();
							Set<String> relatedOffenderSexes = 
									subject.getOffenders().stream()
									.filter(offense->vs.isVictimOfOffender(offense))
									.map(offense->offense.getSex())
									.collect(Collectors.toSet()); 
							relatedOffenderSexes.remove(victimSex);
							if (victimSex != null && relatedOffenderSexes.size() == 0) {
								ret = subject.getErrorTemplate();
								ret.setNIBRSErrorCode(NIBRSErrorCode._560);
								ret.setDataElementIdentifier("38");
								ret.setValue(victimSex);
								ret.setCrossSegment(true);
								break;
							}
						}
					}
				}
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule656() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				NIBRSError template = subject.getErrorTemplate();
				template.setNIBRSErrorCode(NIBRSErrorCode._656);
				template.setDataElementIdentifier("L 6");
				template.setValue(null);
				template.setCrossSegment(true);
				int offenderCount = subject.getOffenderCount();
				int arresteeCount = subject.getArresteeCount();
				if (offenderCount == 1) {
					OffenderSegment offender = subject.getOffenders().get(0);
					ParsedObject<Integer> offenderSequenceNumberPO = offender.getOffenderSequenceNumber();
					if (arresteeCount > 0 && (offenderSequenceNumberPO.isInvalid() || offenderSequenceNumberPO.isMissing() || offenderSequenceNumberPO.getValue() == 0)) {
						ret = template;
					}
				}
				if (arresteeCount > offenderCount) {
					ret = template;
					ret.setValue(offenderCount + 1);
				}
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule669() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				int offenseCount = subject.getOffenseCount();
				if (offenseCount > 0) {
					boolean noJustifiableHomicide = true;
					for (int i=0;i < offenseCount && noJustifiableHomicide;i++) {
						OffenseSegment os = subject.getOffenses().get(i);
						noJustifiableHomicide = !(OffenseCode._09C.code.equals(os.getUcrOffenseCode()));
					}
					if (!noJustifiableHomicide && subject.getArresteeCount() > 0) {
						ret = subject.getErrorTemplate();
						ret.setNIBRSErrorCode(NIBRSErrorCode._669);
						ret.setDataElementIdentifier("6");
						ret.setValue(OffenseCode._09C.code);
						ret.setCrossSegment(true);
					}
				}
				return ret;
			}
		};
	}
	
	private static abstract class MinimalOffenderInfoRule<T> implements Rule<GroupAIncidentReport> {
		
		private NIBRSErrorCode nibrsErrorCode;
		
		public MinimalOffenderInfoRule(NIBRSErrorCode nibrsErrorCode) {
			this.nibrsErrorCode = nibrsErrorCode;
		}
		
		@Override
		public final NIBRSError apply(GroupAIncidentReport subject) {
			NIBRSError ret = null;
			int offenderCount = subject.getOffenderCount();
			if (offenderCount > 0) {
				boolean someOffenderHasAllInfo = false;
				NIBRSError potentialError = null;
				for (int i = 0; i < offenderCount; i++) {
					OffenderSegment os = subject.getOffenders().get(i);
					Object value = null;
					String dataElementIdentifier = null;
					String race = os.getRace();
					NIBRSAge age = os.getAge();
					String sex = os.getSex();
					if (race == null || RaceCode.U.code.equals(race)) {
						value = race;
						dataElementIdentifier = "39";
					}
					if (sex == null || SexCode.U.code.equals(sex)) {
						value = sex;
						dataElementIdentifier = "38";
					}
					if (age == null || age.isUnknown()) {
						value = age;
						dataElementIdentifier = "37";
					}
					if (dataElementIdentifier != null) {
						potentialError = os.getErrorTemplate();
						potentialError.setValue(value);
						potentialError.setDataElementIdentifier(dataElementIdentifier);
						potentialError.setNIBRSErrorCode(nibrsErrorCode);
					}
					someOffenderHasAllInfo |= dataElementIdentifier == null;
				}
				if (!someOffenderHasAllInfo && violatesRule(subject)) {
					ret = potentialError;
				}
			}
			return ret;
		}
		
		protected abstract boolean violatesRule(GroupAIncidentReport subject);
		
	}
	
	Rule<GroupAIncidentReport> getRule559() {
		return new MinimalOffenderInfoRule<GroupAIncidentReport>(NIBRSErrorCode._559) {
			@Override
			protected boolean violatesRule(GroupAIncidentReport subject) {
				boolean ret = false;
				int offenseCount = subject.getOffenseCount();
				for (int i=0;i < offenseCount && !ret;i++) {
					OffenseSegment os = subject.getOffenses().get(i);
					ret = OffenseCode._09C.code.equals(os.getUcrOffenseCode());
				}
				return ret;
			}
			
		};
	}
	
	Rule<GroupAIncidentReport> getRule558() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				
				String exceptionalClearanceCode = subject.getExceptionalClearanceCode();
				if (subject.getReportActionType() != 'D' 
						&& ClearedExceptionallyCode.applicableCodeSet().contains(exceptionalClearanceCode)){
					long allKnownOffenderCount = subject.getOffenders()
							.stream()
							.filter(item -> item.isIdentifyingInfoComplete(validatorProperties.getStateToFbiRaceCodeMapping()) )
							.count();
					if (allKnownOffenderCount == 0){
						ret = subject.getErrorTemplate();
						ret.setDataElementIdentifier("L 5");
						ret.setNIBRSErrorCode(NIBRSErrorCode._558);
						ret.setWithinSegmentIdentifier(null);
						ret.setCrossSegment(true);
					}
				}

				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule555() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				int offenderCount = subject.getOffenderCount();
				if (offenderCount > 1) {
					for (int i=0;i < offenderCount && ret == null;i++) {
						ParsedObject<Integer> offenderSequenceNumber = subject.getOffenders().get(i).getOffenderSequenceNumber();
						if (!offenderSequenceNumber.isMissing() && !offenderSequenceNumber.isInvalid() && offenderSequenceNumber.getValue() == 0) {
							ret = subject.getErrorTemplate();
							ret.setValue(0);
							ret.setDataElementIdentifier("36");
							ret.setNIBRSErrorCode(NIBRSErrorCode._555);
						}
					}
				}
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule480() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				
				boolean contains08AggravatedAssaultHomicideCircumstancesCode = subject.getVictims().stream()
						.anyMatch(victim -> 
							victim.getAggravatedAssaultHomicideCircumstancesList().contains(AggravatedAssaultHomicideCircumstancesCode._08.code));
				
				int victimCount = subject.getVictimCount();
				int offenseCount = subject.getOffenseCount();
				if (contains08AggravatedAssaultHomicideCircumstancesCode && victimCount < 2 && offenseCount < 2) {
					ret = subject.getVictims().get(0).getErrorTemplate();
					ret.setValue(AggravatedAssaultHomicideCircumstancesCode._08.code);
					ret.setDataElementIdentifier("31");
					ret.setNIBRSErrorCode(NIBRSErrorCode._480);
					ret.setCrossSegment(true);
				}
				return ret;
			}
		};
	}
	

	
	Rule<GroupAIncidentReport> getRule474() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				int victimCount = subject.getVictimCount();
				List<Integer> priorVictimOffenders = new ArrayList<>();
				if (victimCount > 1) {
					for (int i=0;i < victimCount && ret == null;i++) {
						VictimSegment vs = subject.getVictims().get(i);
						for (int j=0;j < VictimSegment.OFFENDER_NUMBER_RELATED_COUNT && ret == null;j++) {
							if (RelationshipOfVictimToOffenderCode.VO.code.equals(vs.getVictimOffenderRelationship(j))) {
								Integer offender = vs.getOffenderNumberRelated(j).getValue();
								if (priorVictimOffenders.contains(offender)) {
									ret = vs.getErrorTemplate();
									ret.setValue(RelationshipOfVictimToOffenderCode.VO.code);
									ret.setDataElementIdentifier("35");
									ret.setNIBRSErrorCode(NIBRSErrorCode._474);
								} else {
									priorVictimOffenders.add(offender);
								}
							}
						}
					}
				}
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule470() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				int victimCount = subject.getVictimCount();
				int offenderCount = subject.getOffenderCount();
				if (victimCount <= 1 || offenderCount <= 1) {
					for (int i=0;i < victimCount && ret == null;i++) {
						VictimSegment vs = subject.getVictims().get(i);
						List<String> relationships = vs.getVictimOffenderRelationshipList();
						if (relationships.contains(RelationshipOfVictimToOffenderCode.VO.code)) {
							ret = vs.getErrorTemplate();
							ret.setValue(RelationshipOfVictimToOffenderCode.VO.code);
							ret.setDataElementIdentifier("35");
							ret.setNIBRSErrorCode(NIBRSErrorCode._470);
							ret.setCrossSegment(true);
						}
					}
				}
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule404VictimSequenceNumber() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				int victimCount = subject.getVictimCount();
				
				long validVictimSequenceNumberCount = subject.getVictims().stream()
					.map(victim -> victim.getVictimSequenceNumber())
					.map(sequnceNumber -> sequnceNumber.getValue())
					.filter(Objects::nonNull)
					.filter(item -> item > 0)
					.distinct()
					.count(); 
							
				
				if (victimCount > 0 && victimCount != validVictimSequenceNumberCount ) {
					ret = subject.getVictims().get(0).getErrorTemplate();
					ret.setDataElementIdentifier("23");
					ret.setNIBRSErrorCode(NIBRSErrorCode._404);
					ret.setCrossSegment(true);
				}
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule466() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				int victimCount = subject.getVictimCount();
				int offenseCount = subject.getOffenseCount();
				if (victimCount > 0 && offenseCount > 0) {
					for (int i=0;i < victimCount && ret == null;i++) {
						VictimSegment vs = subject.getVictims().get(i);
						for (String offenseCode : vs.getUcrOffenseCodeList()) {
							if (offenseCode != null && subject.getOffenseForOffenseCode(offenseCode) == null) {
								ret = vs.getErrorTemplate();
								ret.setValue(offenseCode);
								ret.setDataElementIdentifier("24");
								ret.setNIBRSErrorCode(NIBRSErrorCode._466);
								ret.setCrossSegment(false);
							}
						}
					}
				}
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule382() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				int propertyCount = subject.getPropertyCount();
				if (propertyCount > 0 && subject.getOffenseCount() > 0) {
					boolean hasDrugsNarcoticViolationOffense = false;
					for (OffenseSegment os : subject.getOffenses()) {
						String offenseCode = os.getUcrOffenseCode();
						if (OffenseCode._35A.code.equals(offenseCode)) {
							hasDrugsNarcoticViolationOffense = true;
							break;
						}
					}
					if (!hasDrugsNarcoticViolationOffense) {
						boolean hasUnvaluedDrugs = false;
						for (int i=0;i < propertyCount && !hasUnvaluedDrugs;i++) {
							PropertySegment ps = subject.getProperties().get(i);
							for (int j=0;j < PropertySegment.PROPERTY_DESCRIPTION_COUNT && !hasUnvaluedDrugs;j++) {
								hasUnvaluedDrugs = PropertyDescriptionCode._10.code.equals(ps.getPropertyDescription(j)) && ps.getValueOfProperty(j).getValue() == null;
							}
						}
						if (hasUnvaluedDrugs) {
							ret = subject.getErrorTemplate();
							ret.setValue(null);
							ret.setDataElementIdentifier("15");
							ret.setNIBRSErrorCode(NIBRSErrorCode._382);
							ret.setCrossSegment(true);
						}
					}
				}
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule268() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				int propertyCount = subject.getPropertyCount();
				if (propertyCount > 0 && subject.getOffenseCount() > 0) {
					boolean containsLarcency = subject.getOffenses()
							.stream()
							.filter(Objects::nonNull)
							.anyMatch(offense->OffenseCode.isLarcenyOffenseCode(offense.getUcrOffenseCode()));
					boolean hasOtherStolenVehicleCrime = false;
					for (OffenseSegment os : subject.getOffenses()) {
						String offenseCode = os.getUcrOffenseCode();
						if (OffenseCode.isCrimeAgainstStolenVehiclePropertyCode(offenseCode) 
								&& !OffenseCode.isLarcenyOffenseCode(offenseCode)) {
							if (!os.getOffenseAttemptedIndicator()){
								hasOtherStolenVehicleCrime = true;
								break;
							}
						}
					}
					boolean stolenMotorVehicleSubmitted = false;
					String propertyDescriptionValue = null;
					for (int i=0;i < propertyCount && !stolenMotorVehicleSubmitted;i++) {
						PropertySegment ps = subject.getProperties().get(i);
						boolean isStolen = TypeOfPropertyLossCode._7.code.equals(ps.getTypeOfPropertyLoss());
						
						if (isStolen){
							for (int j=0;j < PropertySegment.PROPERTY_DESCRIPTION_COUNT && !stolenMotorVehicleSubmitted;j++) {
								stolenMotorVehicleSubmitted = PropertyDescriptionCode.isMotorVehicleCode(ps.getPropertyDescription(j));
								propertyDescriptionValue = ps.getPropertyDescription(j);
							}
						}
					}
					if (containsLarcency && !hasOtherStolenVehicleCrime && stolenMotorVehicleSubmitted) {
						ret = subject.getErrorTemplate();
						ret.setValue(propertyDescriptionValue);
						ret.setDataElementIdentifier("15");
						ret.setNIBRSErrorCode(NIBRSErrorCode._268);
						ret.setCrossSegment(true);
					}
				}
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule266() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				Set<String> offenseCodes = new HashSet<>();
				for (OffenseSegment os : subject.getOffenses()) {
					String offenseCode = os.getUcrOffenseCode();
					if (offenseCode != null) {
						offenseCodes.add(offenseCode);
					}
				}
				if (offenseCodes.contains(OffenseCode._09C.code) && offenseCodes.size() > 1) {
					offenseCodes.remove(OffenseCode._09C.code);
					ret = subject.getErrorTemplate();
					ret.setValue(offenseCodes);
					ret.setDataElementIdentifier("6");
					ret.setNIBRSErrorCode(NIBRSErrorCode._266);
				}
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule263() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				if (subject.getOffenseCount() > 10) {
					ret = subject.getErrorTemplate();
					ret.setValue(subject.getOffenseCount());
					ret.setDataElementIdentifier(null);
					ret.setNIBRSErrorCode(NIBRSErrorCode._263);
				}
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule661() {
		return new DuplicateSegmentIdentifierRule<ArresteeSegment>(subject -> subject.getArrestees(), "40", NIBRSErrorCode._661);
	}
	
	Rule<GroupAIncidentReport> getRule551() {
		return new DuplicateSegmentIdentifierRule<OffenderSegment>(subject -> subject.getOffenders(), "36", NIBRSErrorCode._551);
	}
	
	Rule<GroupAIncidentReport> getRule451() {
		return new DuplicateSegmentIdentifierRule<VictimSegment>(subject -> subject.getVictims(), "23", NIBRSErrorCode._451);
	}
	
	Rule<GroupAIncidentReport> getRule376() {
		return new DuplicateSegmentIdentifierRule<PropertySegment>(subject -> subject.getProperties(), "14", NIBRSErrorCode._376);
	}
	
	Rule<GroupAIncidentReport> getRule262() {
		return new DuplicateSegmentIdentifierRule<OffenseSegment>(subject -> subject.getOffenses(), "6", NIBRSErrorCode._262);
	}
	
	Rule<GroupAIncidentReport> getRule080() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;

				boolean containsCrimesAgainstSociety = subject.getOffenses().stream()
						.anyMatch(offense -> OffenseCode.isCrimeAgainstSocietyCode(offense.getUcrOffenseCode())) ;
				
				Long crimeAgainsSocietyVictimCount = subject.getVictims()
						.stream()
						.filter(VictimSegment::isVictimOfCrimeAgainstSociety)
						.count();
						
				if (containsCrimesAgainstSociety && crimeAgainsSocietyVictimCount > 1) {
					ret = subject.getErrorTemplate();
					ret.setSegmentType('0');
					ret.setNIBRSErrorCode(NIBRSErrorCode._080);
				}
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule075() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				if (subject.getReportActionType() != 'D' 
					&& (subject.getOffenders().isEmpty() || subject.getOffenses().isEmpty() || subject.getVictims().isEmpty())) {
					ret = subject.getErrorTemplate();
					ret.setValue(null);
					ret.setDataElementIdentifier(null);
					ret.setNIBRSErrorCode(NIBRSErrorCode._075);
				}
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule076() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				
				boolean isPropertySegmentAllowable = subject.getOffenses()
						.stream().map(OffenseSegment::getUcrOffenseCode)
						.anyMatch(code-> OffenseCode.isCrimeRequirePropertySegement(code)
								|| OffenseCode.isCommerceViolations(code)
								|| Objects.equals(code, OffenseCode._521.code)
								|| Objects.equals(code, OffenseCode._522.code)
								|| Objects.equals(code, OffenseCode._526.code)); 
				
				if ( !isPropertySegmentAllowable && !subject.getProperties().isEmpty()) {
					ret = subject.getErrorTemplate();
					ret.setValue(null);
					ret.setDataElementIdentifier("L 3");
					ret.setNIBRSErrorCode(NIBRSErrorCode._076);
					ret.setCrossSegment(true);
				}
						
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule077() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				
				String qualifiedUcrCode = subject.getOffenses().stream()
						.filter(offense -> (offense.getOffenseAttemptedIndicator() 
								&& (OffenseCode.isCrimeAgainstPropertyCode(offense.getUcrOffenseCode()) 
										|| OffenseCode.isGamblingOffenseCode(offense.getUcrOffenseCode())
										|| OffenseCode._100.code.equals(offense.getUcrOffenseCode()) 
										|| OffenseCode._35A.code.equals(offense.getUcrOffenseCode())) ))
						.limit(1)
						.map(i->i.getUcrOffenseCode())
						.reduce("", String::concat);
										
				if (StringUtils.isNotBlank(qualifiedUcrCode)){
					boolean hasNonOrUnknowPropertyLoss = subject.getProperties()
								.stream()
								.filter(i -> TypeOfPropertyLossCode.noneOrUnknownValueCodeSet().contains( i.getTypeOfPropertyLoss()))
								.count() > 0 ;
							
					if (!hasNonOrUnknowPropertyLoss){
						ret = subject.getErrorTemplate();
						ret.setValue(qualifiedUcrCode);
						ret.setDataElementIdentifier("14");
						ret.setNIBRSErrorCode(NIBRSErrorCode._077);
						ret.setCrossSegment(true);
					}
				}
				
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule071() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError e = null;
				String exceptionalClearanceCode = subject.getExceptionalClearanceCode();
				ParsedObject<LocalDate> exceptionalClearanceDatePO = subject.getExceptionalClearanceDate();
				if (subject.getReportActionType() == 'I' 
						&& exceptionalClearanceCode != null 
						&& !ClearedExceptionallyCode.N.code.equals(exceptionalClearanceCode) 
						&& !exceptionalClearanceDatePO.isMissing() 
						&& !exceptionalClearanceDatePO.isInvalid()
						&& exceptionalClearanceDatePO.getValue() != null) {
					boolean containingInvalidArrests = 
							subject.getArrestees().stream()
							.anyMatch(item->
								item.getArrestDate() != null 
								&& item.getArrestDate().getValue() != null 
								&& !item.getArrestDate().getValue().isAfter(exceptionalClearanceDatePO.getValue()));
					if (containingInvalidArrests) {
						e = subject.getErrorTemplate();
						e.setNIBRSErrorCode(NIBRSErrorCode._071);
						e.setDataElementIdentifier("04");
						e.setValue(exceptionalClearanceCode);
						e.setCrossSegment(true);
					}
				}
				return e;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule078() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				
				List<OffenseSegment> qualifiedOffenses = subject.getOffenses().stream()
						.filter(offense -> ("C".equals(offense.getOffenseAttemptedCompleted()) 
								&& (OffenseCode.isCrimeAgainstPropertyCode(offense.getUcrOffenseCode()) 
										|| OffenseCode.isGamblingOffenseCode(offense.getUcrOffenseCode())
										|| OffenseCode._35A.code.equals(offense.getUcrOffenseCode())
										|| OffenseCode._100.code.equals(offense.getUcrOffenseCode())) ))
						.collect(Collectors.toList());
				
				if (qualifiedOffenses.size() > 0){
					
					List<PropertySegment> properties = subject.getProperties();
					List<String> existingPropertyLosses = 
							subject.getProperties().stream()
								.map(item -> item.getTypeOfPropertyLoss())
								.distinct()
								.collect(Collectors.toList());
					for ( OffenseSegment offense: qualifiedOffenses){
						List<String> validPropertyLossCodes = getValidPropertyLossCodes(offense);
						if ( properties == null ||  properties.isEmpty() || 
							!CollectionUtils.containsAny(existingPropertyLosses, validPropertyLossCodes)){
							ret = subject.getErrorTemplate();
							ret.setValue(offense.getUcrOffenseCode());
							ret.setDataElementIdentifier("14");
							ret.setNIBRSErrorCode(NIBRSErrorCode._078);
							ret.setCrossSegment(true);
						}
					}
				}
				
				return ret;
			}
		};
	}

	Rule<GroupAIncidentReport> getRule081() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				
				List<OffenseSegment> qualifiedOffenses = subject.getOffenses().stream()
						.filter(offense -> 
							("C".equals(offense.getOffenseAttemptedCompleted()) 
								&& (OffenseCode.isCrimeAgainstPropertyCode(offense.getUcrOffenseCode()) 
									|| OffenseCode.isGamblingOffenseCode(offense.getUcrOffenseCode())
									|| OffenseCode._35A.code.equals(offense.getUcrOffenseCode())
									|| OffenseCode._35B.code.equals(offense.getUcrOffenseCode())
									|| OffenseCode._100.code.equals(offense.getUcrOffenseCode()))
							|| (offense.getOffenseAttemptedIndicator() 
								&& (OffenseCode.isCrimeAgainstPropertyCode(offense.getUcrOffenseCode()) 
									|| OffenseCode.isGamblingOffenseCode(offense.getUcrOffenseCode())
									|| OffenseCode._100.code.equals(offense.getUcrOffenseCode()) 
									|| OffenseCode.isDrugNarcoticOffense(offense.getUcrOffenseCode()))
							)))
						.collect(Collectors.toList()); 
									
				
				List<String> existingPropertyLosses = 
						subject.getProperties().stream()
							.map(item -> item.getTypeOfPropertyLoss())
							.filter(Objects::nonNull)
							.distinct()
							.collect(Collectors.toList());
				
				if (qualifiedOffenses.size() > 0){
					existingPropertyLosses.removeAll(getValidPropertyLossCodes(qualifiedOffenses));
					ret = setError081(subject, ret, existingPropertyLosses);
				}
				
				else if (subject.getOffenses().stream().anyMatch(Objects::nonNull)){
					existingPropertyLosses.removeAll(TypeOfPropertyLossCode.noneOrUnknownValueCodeSet());
					ret = setError081(subject, ret, existingPropertyLosses);
				}
				
				return ret;
			}

			private NIBRSError setError081(GroupAIncidentReport subject,
					NIBRSError ret, List<String> existingPropertyLosses) {
				if (existingPropertyLosses.size() > 0){
					ret = subject.getErrorTemplate();
					ret.setValue(existingPropertyLosses);
					ret.setDataElementIdentifier("14");
					ret.setNIBRSErrorCode(NIBRSErrorCode._081);
					ret.setCrossSegment(true);
				}
				return ret;
			}
		};
	}
	
	protected Set<String> getValidPropertyLossCodes(List<OffenseSegment> offenses){
		Set<String> validPropertyCodes = new HashSet<>(); 
		
		offenses.stream()
			.forEach(offense -> validPropertyCodes.addAll(getValidPropertyLossCodes(offense)));
		return validPropertyCodes;
	}
	
	/**
	 * ucrOffenseCode are among criminal against property/kidnaping/gambling/35A offense codes. 
	 * @param ucrOffenseCode
	 * @return list of valid property loss codes
	 * 
	 */
	protected List<String> getValidPropertyLossCodes(OffenseSegment offense) {

		switch(offense.getOffenseAttemptedCompleted()){
		case "A":
			return new ArrayList<>(TypeOfPropertyLossCode.noneOrUnknownValueCodeSet());
		case "C": 
			switch(offense.getUcrOffenseCode()){
			case "280": 
				return Arrays.asList(
						TypeOfPropertyLossCode._1.code, 
						TypeOfPropertyLossCode._5.code);
			case "220": 
			case "100": 
			case "510": 
				return Arrays.asList(
						TypeOfPropertyLossCode._1.code, 
						TypeOfPropertyLossCode._5.code, 
						TypeOfPropertyLossCode._7.code, 
						TypeOfPropertyLossCode._8.code);
			case "35A": 
			case "35B": 
				return Arrays.asList(
						TypeOfPropertyLossCode._1.code, 
						TypeOfPropertyLossCode._6.code);
			case "200": 
				return Arrays.asList(TypeOfPropertyLossCode._2.code);
			case "250": 
				return Arrays.asList(
						TypeOfPropertyLossCode._3.code, 
						TypeOfPropertyLossCode._5.code, 
						TypeOfPropertyLossCode._6.code);
			case "290":
				return Arrays.asList(TypeOfPropertyLossCode._4.code);
			case "120": 
			case "210": 
			case "23A": 
			case "23B": 
			case "23C": 
			case "23D": 
			case "23E": 
			case "23F": 
			case "23G": 
			case "23H": 
			case "240": 
			case "26A": 
			case "26B": 
			case "26C": 
			case "26D": 
			case "26E": 
			case "26F": 
			case "26G": 
			case "270": 
				return Arrays.asList(
						TypeOfPropertyLossCode._5.code, 
						TypeOfPropertyLossCode._7.code);
			case "39A": 
			case "39B":
			case "39C":
			case "39D":
				return Arrays.asList(
						TypeOfPropertyLossCode._6.code);
			}
		}
		return new ArrayList<String>(TypeOfPropertyLossCode.codeSet());
	}

	Rule<GroupAIncidentReport> getRule084() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				
				boolean hasStolenAndRecoverdPropertySegments = subject.getProperties()
						.stream()
						.filter(item -> item.isStolenPropertySegment() || item.isRecoveredPropertySegment())
						.map(item -> item.getTypeOfPropertyLoss())
						.distinct()
						.count() == 2;
				 
				
				if ( hasStolenAndRecoverdPropertySegments ){
					Map<String, Integer> stolenPropertyValueMap = 
							subject.getProperties().stream()
							.filter(item ->item.isStolenPropertySegment())
							.map(item -> item.getPropertyDescriptionValueMap())
							.findFirst()
							.get();
					Map<String, Integer> recoveredPropertyValueMap = 
							subject.getProperties().stream()
							.filter(item ->item.isRecoveredPropertySegment())
							.map(item -> item.getPropertyDescriptionValueMap())
							.findFirst()
							.get();
					
					Integer recoveredVehiclePartsValue =  Optional.ofNullable(recoveredPropertyValueMap.get(PropertyDescriptionCode._38.code))
							.orElse(0).intValue(); 
					Integer stolenVehiclePartsValue =  Optional.ofNullable(stolenPropertyValueMap.get(PropertyDescriptionCode._38.code))
							.orElse(0).intValue(); 
					
					if (stolenPropertyValueMap != null && recoveredPropertyValueMap != null) {
						for (Map.Entry<String, Integer> entry : stolenPropertyValueMap.entrySet()) {
							if (entry.getValue() != null 
									&& entry.getValue() >= 0 
									&& ((recoveredPropertyValueMap.get(entry.getKey()) != null
												&& recoveredPropertyValueMap.get(entry.getKey()) > entry.getValue())
										|| (PropertyDescriptionCode.isMotorVehicleCode(entry.getKey())
												&& recoveredPropertyValueMap.get(entry.getKey()) != null
												&& (recoveredVehiclePartsValue + recoveredPropertyValueMap.get(entry.getKey())) 
													> (entry.getValue() + stolenVehiclePartsValue))
										|| (PropertyDescriptionCode.isMotorVehicleCode(entry.getKey())
												&& recoveredPropertyValueMap.get(entry.getKey()) == null
												&& recoveredVehiclePartsValue > entry.getValue()))
									){
								ret = subject.getErrorTemplate();
								ret.setValue(entry.getKey());
								ret.setNIBRSErrorCode(NIBRSErrorCode._084);
								ret.setDataElementIdentifier("16");
								ret.setCrossSegment(true);
								break;
							}
						}
					}
				}
				
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule074() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				String offenseCode = subject.getOffenses().stream()
						.map(item -> item.getUcrOffenseCode())
						.filter(Objects::nonNull)
						.filter(item -> OffenseCode._100.code.equals(item) 
								|| OffenseCode._35A.code.equals(item)
								|| OffenseCode.isGamblingOffenseCode(item)
								|| OffenseCode.isCrimeAgainstPropertyCode(item))
								.limit(1)
								.reduce("", String::concat);
				if (StringUtils.isNotBlank(offenseCode) 
								&& subject.getProperties().isEmpty()) {
					
					ret = subject.getErrorTemplate();
					ret.setValue(offenseCode);
					ret.setDataElementIdentifier("L 3");
					ret.setNIBRSErrorCode(NIBRSErrorCode._074);
					ret.setCrossSegment(true);
				}
						
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule073() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				PropertySegment recoveredSegment = subject.getRecoveredPropertySegment();
				PropertySegment stolenSegment = subject.getStolenPropertySegment();
				if (subject.getReportActionType() == 'I' && recoveredSegment != null 
					&& recoveredSegment.getNumberOfRecoveredMotorVehicles().getValue() != null 
					&& recoveredSegment.getNumberOfRecoveredMotorVehicles().getValue() > 0
					&& (stolenSegment == null 
						|| stolenSegment.getNumberOfStolenMotorVehicles().isMissing() 
						|| stolenSegment.getNumberOfStolenMotorVehicles().isInvalid()
						|| stolenSegment.getNumberOfStolenMotorVehicles().getValue() == null 
						|| stolenSegment.getNumberOfStolenMotorVehicles().getValue() < recoveredSegment.getNumberOfRecoveredMotorVehicles().getValue())) {
					ret = subject.getErrorTemplate();
					ret.setValue(recoveredSegment.getNumberOfRecoveredMotorVehicles().getValue());
					ret.setSegmentType('0');
					ret.setNIBRSErrorCode(NIBRSErrorCode._073);
				}
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule072() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				PropertySegment recoveredSegment = subject.getRecoveredPropertySegment();
				PropertySegment stolenSegment = subject.getStolenPropertySegment();
				if (recoveredSegment != null 
						&& subject.getOffenseForOffenseCode(OffenseCode._250.code) == null 
						&& subject.getOffenseForOffenseCode(OffenseCode._280.code) == null) {
					List<String> recoveredPropertyTypes = new ArrayList<>();
					recoveredPropertyTypes.addAll(Arrays.asList(recoveredSegment.getPropertyDescription()));
					recoveredPropertyTypes.removeIf(element -> element == null);
					List<String> stolenPropertyDescriptionList = stolenSegment == null ? new ArrayList<String>() : Arrays.asList(stolenSegment.getPropertyDescription());
					recoveredPropertyTypes.removeAll(stolenPropertyDescriptionList);
					if (PropertyDescriptionCode.containsMotorVehicleCode(stolenPropertyDescriptionList)) {
						recoveredPropertyTypes.remove(PropertyDescriptionCode._38.code);
					}
					
					if (stolenPropertyDescriptionList.contains(PropertyDescriptionCode._13.code)){
						recoveredPropertyTypes.remove(PropertyDescriptionCode._59.code);
					}
					if (!recoveredPropertyTypes.isEmpty()) {
						ret = subject.getErrorTemplate();
						ret.setNIBRSErrorCode(NIBRSErrorCode._072);
					}
				}
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule155() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				ParsedObject<LocalDate> exceptionalClearanceDatePO = subject.getExceptionalClearanceDate();
				ParsedObject<LocalDate> incidentDatePO = subject.getIncidentDate();
				if (!exceptionalClearanceDatePO.isMissing() && !exceptionalClearanceDatePO.isInvalid()
						&& !incidentDatePO.isInvalid() && !incidentDatePO.isMissing()) {
					if (exceptionalClearanceDatePO.getValue().isBefore(incidentDatePO.getValue())) {
						ret = subject.getErrorTemplate();
						ret.setValue(subject.getExceptionalClearanceDate().getValue());
						ret.setDataElementIdentifier("5");
						ret.setNIBRSErrorCode(NIBRSErrorCode._155);
					}
				}
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule156() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				ParsedObject<LocalDate> exceptionalClearanceDatePO = subject.getExceptionalClearanceDate();
				if ((exceptionalClearanceDatePO.isMissing() || exceptionalClearanceDatePO.isInvalid()) && trueExceptionalClearanceCodes.contains(subject.getExceptionalClearanceCode())) {
					ret = subject.getErrorTemplate();
					ret.setValue(subject.getExceptionalClearanceCode());
					ret.setDataElementIdentifier("5");
					ret.setNIBRSErrorCode(NIBRSErrorCode._156);
				}
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule105ExceptionalClearanceDate() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				ParsedObject<LocalDate> exceptionalClearanceDatePO = subject.getExceptionalClearanceDate();
				if ((!exceptionalClearanceDatePO.isMissing() && !exceptionalClearanceDatePO.isInvalid()) && exceptionalClearanceDatePO.getValue().isAfter(LocalDate.now())) {
					ret = subject.getErrorTemplate();
					ret.setValue(exceptionalClearanceDatePO.getValue());
					ret.setDataElementIdentifier("5");
					ret.setNIBRSErrorCode(NIBRSErrorCode._105);
				}
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule105IncidentDate() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				ParsedObject<LocalDate> incidentDatePo = subject.getIncidentDate();
				if ((!incidentDatePo.isMissing() && !incidentDatePo.isInvalid()) && incidentDatePo.getValue().isAfter(LocalDate.now())) {
					ret = subject.getErrorTemplate();
					ret.setValue(incidentDatePo.getValue());
					ret.setDataElementIdentifier("3");
					ret.setNIBRSErrorCode(NIBRSErrorCode._105);
				}
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule153() {
		return new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				NIBRSError ret = null;
				ParsedObject<LocalDate> exceptionalClearanceDatePO = subject.getExceptionalClearanceDate();
				if (!exceptionalClearanceDatePO.isMissing() && ! exceptionalClearanceDatePO.isInvalid() && "N".equals(subject.getExceptionalClearanceCode())) {
					ret = subject.getErrorTemplate();
					ret.setValue(subject.getExceptionalClearanceCode());
					ret.setDataElementIdentifier("4");
					ret.setNIBRSErrorCode(NIBRSErrorCode._153);
				}
				return ret;
			}
		};
	}
	
	Rule<GroupAIncidentReport> getRule172() {
		
		return new IncidentDateRule() {
			protected NIBRSError compareIncidentDateToTape(Integer month, Integer year, LocalDate incidentDate, NIBRSError errorTemplate) {
				LocalDate fbiNIBRSStartDate = LocalDate.of(1991, 1, 1);
				NIBRSError e = null;
				if (incidentDate.isBefore(fbiNIBRSStartDate)) {
					e = errorTemplate;
					e.setDataElementIdentifier("3");
					e.setNIBRSErrorCode(NIBRSErrorCode._172);
					e.setValue(incidentDate);
				}
				return e;
			}
		};
		
	}
	
	/**
	 * Removed from specs v3-1. Remove it from rule list. 
	 * @return
	 */
	Rule<GroupAIncidentReport> getRule171() {
		
		return new IncidentDateRule() {
			protected NIBRSError compareIncidentDateToTape(Integer month, Integer year, LocalDate incidentDate, NIBRSError errorTemplate) {
				LocalDate priorYearStartDate = LocalDate.of(year-1, 1, 1);
				NIBRSError e = null;
				if (incidentDate.isBefore(priorYearStartDate)) {
					e = errorTemplate;
					e.setDataElementIdentifier("3");
					e.setNIBRSErrorCode(NIBRSErrorCode._171);
					e.setValue(incidentDate);
				}
				return e;
			}
		};
		
	}
	
	Rule<GroupAIncidentReport> getRule170() {
		
		return new IncidentDateRule() {
			protected NIBRSError compareIncidentDateToTape(Integer month, Integer year, LocalDate incidentDate, NIBRSError errorTemplate) {
				LocalDate submissionDate = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1);
				NIBRSError e = null;
				if (incidentDate.isAfter(submissionDate)) {
					e = errorTemplate;
					e.setDataElementIdentifier("3");
					e.setNIBRSErrorCode(NIBRSErrorCode._170);
					e.setValue(incidentDate);
				}
				return e;
			}
		};
		
	}
	
	Rule<GroupAIncidentReport> getRule119() {
		
		Rule<GroupAIncidentReport> ret = new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				if (subject.getReportActionType() == 'D') {
					return null; 
				}
				
				List<OffenseSegment> offenses = subject.getOffenses();
				boolean cargoTheftIncident = offenses.stream()
						.anyMatch(offense -> cargoTheftOffenses.contains(offense.getUcrOffenseCode()));
				NIBRSError ret = null;
				if (StringUtils.isNotBlank(subject.getCargoTheftIndicator())
						&& !cargoTheftIncident ) {
					ret = subject.getErrorTemplate();
					ret.setValue(subject.getCargoTheftIndicator());
					ret.setDataElementIdentifier("2A");
					ret.setNIBRSErrorCode(NIBRSErrorCode._119);
				}
				return ret;
			}
		};
		
		return ret;
		
	}
	
	Rule<GroupAIncidentReport> getRule122() {
		
		Rule<GroupAIncidentReport> ret = new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				List<OffenseSegment> offenses = subject.getOffenses();
				
				if (!subject.includesCargoTheft()){
					return null;
				}
				
				boolean cargoTheftIncident = offenses.stream()
						.anyMatch(offense -> cargoTheftOffenses.contains(offense.getUcrOffenseCode()));
				
				NIBRSError ret = null;
				if (cargoTheftIncident &&  
						!Arrays.asList("Y", "N").contains(subject.getCargoTheftIndicator())) {
					ret = subject.getErrorTemplate();
					ret.setValue(subject.getCargoTheftIndicator());
					ret.setDataElementIdentifier("2A");
					ret.setNIBRSErrorCode(NIBRSErrorCode._122);
				}
				return ret;
			}
		};
		
		return ret;
	}
	
	Rule<GroupAIncidentReport> getRule106() {
		
		Rule<GroupAIncidentReport> ret = new Rule<GroupAIncidentReport>() {
			@Override
			public NIBRSError apply(GroupAIncidentReport subject) {
				List<OffenseSegment> offenses = subject.getOffenses();
				
				boolean containCrimeRequiresIncidentHour = offenses.stream()
						.anyMatch(offense -> OffenseCode.isCrimeRequireIncidentHour(offense.getUcrOffenseCode()));
				boolean hasLeoVictims = subject.getVictims().stream().anyMatch(VictimSegment::isLawEnforcementOfficer);
				
				NIBRSError ret = null;
				ParsedObject<Integer> incidentHour = subject.getIncidentHour();
				if (containCrimeRequiresIncidentHour &&  hasLeoVictims
						&& (incidentHour.isInvalid() || incidentHour.isMissing()
								|| incidentHour.getValue() == null 
								|| incidentHour.getValue() < 0 
								|| incidentHour.getValue() > 23) ) {
					ret = subject.getErrorTemplate();
					ret.setValue(subject.getIncidentHour().getValue());
					ret.setDataElementIdentifier("3");
					ret.setNIBRSErrorCode(NIBRSErrorCode._106);
				}
				return ret;
			}
		};
		
		return ret;
	}
	
	Rule<GroupAIncidentReport> getRule117() {
		return new ValidNIBRSIdentifierFormatRule<>("2", NIBRSErrorCode._117);
	}

	Rule<GroupAIncidentReport> getRule104(String propertyName) {
		Rule<GroupAIncidentReport> ret = null;
		if ("reportDateIndicator".equals(propertyName)) {
			ret = new StringValueRule<>(
					subject -> {
						return subject.getReportDateIndicator();
					},
					(value, target) -> {
						NIBRSError e = null;
						if (value != null && (!value.equals("R"))) {
							e = target.getErrorTemplate();
							e.setNIBRSErrorCode(NIBRSErrorCode._104);
							e.setDataElementIdentifier("3");
							e.setValue(value);
						}
						return e;
					});
		} else if ("yearOfTape".equals(propertyName)) {
			ret = new NumericValueRule<>(
					subject -> {
						return subject.getYearOfTape();
					},
					(value, target) -> {
						NIBRSError e = null;
						if (value != null && 1991 > value.intValue()) {
							e = target.getErrorTemplate();
							e.setNIBRSErrorCode(NIBRSErrorCode._104);
							e.setDataElementIdentifier("Year of Tape");
							e.setValue(value);
						}
						return e;
					});
		} else if ("monthOfTape".equals(propertyName)) {
			ret = new NumericValueRule<>(
					subject -> {
						return subject.getMonthOfTape();
					},
					(value, target) -> {
						NIBRSError e = null;
						if (value != null && (1 > value.intValue() || 12 < value.intValue())) {
							e = target.getErrorTemplate();
							e.setNIBRSErrorCode(NIBRSErrorCode._104);
							e.setDataElementIdentifier("Month of Tape");
							e.setValue(value);
						}
						return e;
					});
		} else if ("cargoTheftIndicator".equals(propertyName)) {
			ret = new ValidValueListRule<GroupAIncidentReport>(propertyName, "2A", GroupAIncidentReport.class, NIBRSErrorCode._104, CargoTheftIndicatorCode.codeSet(), true) {
				protected boolean ignore(GroupAIncidentReport r) {
					return !r.includesCargoTheft();
				}
			};
		}
		return ret;
	}
	
	Rule<GroupAIncidentReport> getRule152() {
		return new NumericValueRule<>(subject -> {
			return subject.getIncidentHour().getValue();
		} , (value, target) -> {
			NIBRSError e = null;
			if (value != null && (0 > value.intValue() || 23 < value.intValue())) {
				e = target.getErrorTemplate();
				e.setNIBRSErrorCode(NIBRSErrorCode._152);
				e.setDataElementIdentifier("3");
				e.setValue(value);
			}
			return e;
		});
	}
	
	Rule<GroupAIncidentReport> getRule101(String propertyName, String dataElementIdentifier) {
		if ("exceptionalClearanceCode".equals(propertyName)) {
			return new ValidValueListRule<GroupAIncidentReport>(propertyName, dataElementIdentifier, GroupAIncidentReport.class,
					NIBRSErrorCode._101, ClearedExceptionallyCode.codeSet(), false);
		}
		return new NotBlankRule<>(propertyName, dataElementIdentifier, GroupAIncidentReport.class, NIBRSErrorCode._101);
	}

	Rule<GroupAIncidentReport> getRule115() {
		return new BlankRightFillStringRule<>("2", NIBRSErrorCode._115);
	}

	/**
	 * Get the list of rules for the administrative segment.
	 * @return the list of rules
	 */
	public List<Rule<GroupAIncidentReport>> getRulesList() {
		return Collections.unmodifiableList(rulesList);
	}

}
