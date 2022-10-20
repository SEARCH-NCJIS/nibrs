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

import static org.search.nibrs.util.ArrayUtils.allNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.search.nibrs.common.NIBRSError;
import org.search.nibrs.common.ParsedObject;
import org.search.nibrs.model.GroupAIncidentReport;
import org.search.nibrs.model.NIBRSAge;
import org.search.nibrs.model.OffenderSegment;
import org.search.nibrs.model.VictimSegment;
import org.search.nibrs.model.codes.AggravatedAssaultHomicideCircumstancesCode;
import org.search.nibrs.model.codes.NIBRSErrorCode;
import org.search.nibrs.model.codes.OffenseCode;
import org.search.nibrs.model.codes.OfficerAssignmentType;
import org.search.nibrs.model.codes.RaceOfOffenderCode;
import org.search.nibrs.model.codes.RelationshipOfVictimToOffenderCode;
import org.search.nibrs.model.codes.SexCode;
import org.search.nibrs.model.codes.SexOfOffenderCode;
import org.search.nibrs.model.codes.TypeInjuryCode;
import org.search.nibrs.model.codes.TypeOfOfficerActivityCircumstance;
import org.search.nibrs.model.codes.TypeOfVictimCode;
import org.search.nibrs.util.ArrayUtils;
import org.search.nibrs.validation.PersonSegmentRulesFactory;
import org.search.nibrs.validation.ValidationConstants;
import org.search.nibrs.validation.ValidatorProperties;
import org.search.nibrs.validation.rules.AbstractBeanPropertyRule;
import org.search.nibrs.validation.rules.DuplicateCodedValueRule;
import org.search.nibrs.validation.rules.NotAllBlankRule;
import org.search.nibrs.validation.rules.NotBlankRule;
import org.search.nibrs.validation.rules.NullObjectRule;
import org.search.nibrs.validation.rules.Rule;
import org.search.nibrs.validation.rules.ValidValueListRule;

public class VictimSegmentRulesFactory {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(VictimSegmentRulesFactory.class);

	private List<Rule<VictimSegment>> rulesList__2_1;
	private List<Rule<VictimSegment>> rulesList__3_1;
	private List<Rule<VictimSegment>> rulesList__2019;
	private PersonSegmentRulesFactory<VictimSegment> personSegmentRulesFactory;
	
	public static VictimSegmentRulesFactory instance(ValidatorProperties validatorProperties) {
		return new VictimSegmentRulesFactory(validatorProperties);
	}
	
	static final List<String> INJURY_OFFENSE_LIST = Arrays.asList(
			OffenseCode._100.code,
			OffenseCode._11A.code,
			OffenseCode._11B.code,
			OffenseCode._11C.code,
			OffenseCode._11D.code,
			OffenseCode._120.code,
			OffenseCode._13A.code,
			OffenseCode._13B.code,
			OffenseCode._210.code,
			OffenseCode._64A.code,
			OffenseCode._64B.code
			);
	
	private static final class PersonVictimNotBlankRule<T extends VictimSegment> extends AbstractBeanPropertyRule<VictimSegment> {
		
		private String typeCode;
		
		public PersonVictimNotBlankRule(String propertyName, String dataElementIdentifier, NIBRSErrorCode errorCode, String typeCode) {
			super(propertyName, dataElementIdentifier, VictimSegment.class, errorCode);
			this.typeCode = typeCode;
		}

		@Override
		protected boolean propertyViolatesRule(Object value, VictimSegment subject) {
			return (typeCode.equals(subject.getTypeOfVictim()) && value == null);
		}

	}

	private static final class NonPersonVictimBlankRule<T extends VictimSegment> extends AbstractBeanPropertyRule<VictimSegment> {
		
		public NonPersonVictimBlankRule(String propertyName, String dataElementIdentifier, NIBRSErrorCode errorCode) {
			super(propertyName, dataElementIdentifier, VictimSegment.class, errorCode);
		}

		@Override
		protected boolean propertyViolatesRule(Object value, VictimSegment victimSegment) {
			return (!victimSegment.isPerson() && value != null);
		}

	}

	private VictimSegmentRulesFactory(ValidatorProperties validatorProperties) {
		personSegmentRulesFactory = new PersonSegmentRulesFactory<VictimSegment>(VictimSegment.class, validatorProperties);
		initRules();
	}
	
	private void initRules(){
		
		rulesList__2_1 = new ArrayList<Rule<VictimSegment>>();
		rulesList__2_1.add(getRule401ForAggravatedAssaultHomicideCircumstances());
		rulesList__2_1.add(getRule401ForSequenceNumber());
		rulesList__2_1.add(getRule401ForVictimConnectedToUcrOffenseCode());
		rulesList__2_1.add(getRule401ForTypeOfVictim());
		rulesList__2_1.add(getRule401ForTypeOfInjury());
		rulesList__2_1.add(getRule401OffenderNumberToBeRelated());
		rulesList__2_1.add(getRule404ForTypeOfOfficerActivityCircumstance());
		rulesList__2_1.add(getRule404ForOfficerAssignmentType());
		rulesList__2_1.add(getRule404ForVictimConnectedToUcrOffenseCode());
		rulesList__2_1.add(getRule404ForOfficerOriOtherJurisdiction());
		rulesList__2_1.add(getRule404ForAgeOfVictim());		
		rulesList__2_1.add(getRule404ForSexOfVictim());		
		rulesList__2_1.add(getRule404ForRaceOfVictim());		
		rulesList__2_1.add(getRule404ForEthnicityOfVictim());		
		rulesList__2_1.add(getRule404ForResidentStatusOfVictim());		
		rulesList__2_1.add(getRule404ForTypeOfVictim());
		rulesList__2_1.add(getRule404ForAggravatedAssaultHomicideCircumstances());
		rulesList__2_1.add(getRule404OffenderNumberToBeRelated());
		rulesList__2_1.add(getRule404ForTypeOfInjury());
		rulesList__2_1.add(getRule404ForRelationshipOfVictimToOffender());		
		rulesList__2_1.add(getRule404ForAdditionalJustifiableHomicideCircsumstances());
		rulesList__2_1.add(getRule406ForTypeOfInjury());		
		rulesList__2_1.add(getRule406ForAggravatedAssaultHomicideCircumstances());		
		rulesList__2_1.add(getRule406ForVictimConnectedToUcrOffenseCode());
		rulesList__2_1.add(getRule406OffenderNumberToBeRelated());
		rulesList__2_1.add(getRule407());
		rulesList__2_1.add(getRule409());
		rulesList__2_1.add(getRule410ForAgeOfVictim());
		rulesList__2_1.add(getRule419ForAggravatedAssaultHomicideCircumstances());
		rulesList__2_1.add(getRule419ForTypeOfInjury());
		rulesList__2_1.add(getRule422ForAgeOfVictim());
		Rule<VictimSegment> rule450 = getRule450ForAgeOfVictim__2_1();
		rulesList__2_1.add(rule450);				
		rulesList__2_1.add(getRule453ForAgeOfVictim());
		rulesList__2_1.add(getRule453ForSexOfVictim());
		rulesList__2_1.add(getRule453ForRaceOfVictim());
		rulesList__2_1.add(getRule454ForTypeOfOfficerActivityCircumstance());
		rulesList__2_1.add(getRule454ForSexOfVictim());
		rulesList__2_1.add(getRule454ForRaceOfVictim());
		rulesList__2_1.add(getRule454ForAgeOfVictim());
		rulesList__2_1.add(getRule454ForOfficerAssignmentType());
		rulesList__2_1.add(getRule455ForAdditionalJustifiableHomicideCircsumstances());	
		rulesList__2_1.add(getRule456());
		rulesList__2_1.add(getRule457ForAdditionalJustifiableHomicideCircsumstances());
		rulesList__2_1.add(getRule458ForSexOfVictim());	
		rulesList__2_1.add(getRule458ForResidentStatusOfVictim());				
		rulesList__2_1.add(getRule458ForOffenderNumberToBeRelated());	
		rulesList__2_1.add(getRule458ForAgeOfVictim());
		rulesList__2_1.add(getRule458ForEthnicityOfVictim());
		rulesList__2_1.add(getRule458ForRaceOfVictim());
		rulesList__2_1.add(getRule458ForTypeOfInjury());
		rulesList__2_1.add(getRule459ForOffenderNumberToBeRelated());
		rulesList__2_1.add(getRule460ForRelationshipOfVictimToOffender());
		rulesList__2_1.add(getRule462());
		rulesList__2_1.add(getRule463());
		rulesList__2_1.add(getRule464ForTypeOfVictim());
		rulesList__2_1.add(getRule465ForTypeOfVictim());
		rulesList__2_1.add(getRule467ForTypeOfVictim());		
		rulesList__2_1.add(getRule469ForSexOfVictim());
		rulesList__2_1.add(getRule468ForRelationshipOfVictimToOffender());
		rulesList__2_1.add(getRule471());
		rulesList__2_1.add(getRule472());
		rulesList__2_1.add(getRule475());
		rulesList__2_1.add(getRule476());
		rulesList__2_1.add(getRule477());
		rulesList__2_1.add(getRule478());
		rulesList__2_1.add(getRule479());
		rulesList__2_1.add(getRule481ForAgeOfVictim());		
		rulesList__2_1.add(getRule482ForTypeOfVictim());
		rulesList__2_1.add(getRule483ForTypeOfOfficerActivity());
		rulesList__2_1.add(getRule483ForOfficerAssignmentType());
		rulesList__2_1.add(getRule483ForOfficerOtherJurisdictionORI());

		rulesList__2_1.add(getRule070());
		rulesList__2_1.add(getRule085());

		rulesList__3_1 = new ArrayList<Rule<VictimSegment>>();
		rulesList__3_1.addAll(rulesList__2_1);
		rulesList__3_1.remove(rule450);
		rulesList__3_1.add(getRule450ForAgeOfVictim__3_1());
		
		rulesList__2019 = new ArrayList<Rule<VictimSegment>>();
		rulesList__2019.addAll(rulesList__3_1);
		rulesList__2019.add(getRule452());
		rulesList__2019.add(getRule461ForTypeOfVictim());
		rulesList__2019.remove(getRule459ForOffenderNumberToBeRelated());
		
	}
		
	public List<Rule<VictimSegment>> getRulesList() {
		return getRulesList(ValidationConstants.SPEC__LATEST);
	}	

	public List<Rule<VictimSegment>> getRulesList(String specVersion) {
		if (ValidationConstants.SPEC__2_1.equals(specVersion)) {
			return rulesList__2_1;
		} else if (ValidationConstants.SPEC__3_1.equals(specVersion)) {
			return rulesList__3_1;
		} else if (ValidationConstants.SPEC__2019.contentEquals(specVersion)) {
			return rulesList__2019;
		}
		throw new IllegalArgumentException("Invalid spec version: " + specVersion);
	}	

	Rule<VictimSegment> getRule070() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment subject) {
				NIBRSError e = null;
				GroupAIncidentReport parent = (GroupAIncidentReport) subject.getParentReport();
				List<Integer> related = new ArrayList<>();
				related.addAll(subject.getDistinctValidRelatedOffenderNumberList());
				related.removeIf(element -> element == null);
				for (Integer offenderNumber : related) {
					OffenderSegment matchedOffender = parent.getOffenderForSequenceNumber(offenderNumber);
					if (matchedOffender == null) {
						e = subject.getErrorTemplate();
						e.setNIBRSErrorCode(NIBRSErrorCode._070);
						e.setDataElementIdentifier("34");
						e.setValue(offenderNumber);
						e.setCrossSegment(true);
						e.setWithinSegmentIdentifier(null); 
						break;
					}
				}
				return e;
			}
		};
	}
	
	Rule<VictimSegment> getRule085_31() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment subject) {
				NIBRSError e = null;
				GroupAIncidentReport parent = (GroupAIncidentReport) subject.getParentReport();
				
				if (parent.getOffenderCount() >= 3 
						&& subject.isPerson()
						&& (subject.getUcrOffenseCodeList().contains(OffenseCode._120.code)||
							OffenseCode.containsCrimeAgainstPersonCode(subject.getUcrOffenseCodeList()))
						){
					long relatedOffenderCount = subject.getOffenderNumberRelatedList().stream()
							.filter(item->!(item.isInvalid() || item.isMissing() ||item.getValue() == null)).count();
					if (relatedOffenderCount < 2){
						e = subject.getErrorTemplate();
						e.setNIBRSErrorCode(NIBRSErrorCode._085);
						e.setDataElementIdentifier("34");
						e.setCrossSegment(true);
						e.setWithinSegmentIdentifier(null); 
					}
				}
					
				return e;
			}
		};
	}
	
	Rule<VictimSegment> getRule085() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment subject) {
				NIBRSError e = null;
				GroupAIncidentReport parent = (GroupAIncidentReport) subject.getParentReport();
				
				if (parent.getOffenderCount() >= 2 
						&& subject.isPerson()
						&& (OffenseCode.containsCrimeAgainstPropertyCode(subject.getUcrOffenseCodeList())||
							OffenseCode.containsCrimeAgainstPersonCode(subject.getUcrOffenseCodeList()))
						){
					long relatedOffenderCount = subject.getOffenderNumberRelatedList().stream()
							.filter(item->!(item.isInvalid() || item.isMissing() ||item.getValue() == null)).count();
					if (relatedOffenderCount < 2){
						e = subject.getErrorTemplate();
						e.setNIBRSErrorCode(NIBRSErrorCode._085);
						e.setDataElementIdentifier("34");
						e.setCrossSegment(true);
						e.setWithinSegmentIdentifier(null); 
					}
				}
					
				return e;
			}
		};
	}
			
			
	Rule<VictimSegment> getRule401ForSequenceNumber() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment subject) {

				ParsedObject<Integer> victimSequenceNumberPO = subject.getVictimSequenceNumber();
				Integer victimSequenceNumber = victimSequenceNumberPO.getValue();
				NIBRSError e = null;
				
				if(victimSequenceNumberPO.isMissing() || victimSequenceNumberPO.isInvalid() || victimSequenceNumber < 1 || victimSequenceNumber > 999) {
					e = subject.getErrorTemplate();
					e.setNIBRSErrorCode(NIBRSErrorCode._401);
					e.setDataElementIdentifier("23");
					e.setValue(victimSequenceNumber);
				}	
				
				return e;
				
			}
		};
	}
	
	Rule<VictimSegment> getRule401ForVictimConnectedToUcrOffenseCode() {
		return new Rule<VictimSegment>() {

			@Override
			public NIBRSError apply(VictimSegment victimSegment) {

				NIBRSError e = null;
				NIBRSError errorTemplate = victimSegment.getErrorTemplate();
				errorTemplate.setDataElementIdentifier("24");
				errorTemplate.setNIBRSErrorCode(NIBRSErrorCode._401);

				Set<String> offenseCodeSet = new HashSet<>();
				offenseCodeSet.addAll(victimSegment.getUcrOffenseCodeList());
				offenseCodeSet.removeIf(Objects::isNull);

				if (offenseCodeSet.isEmpty()) {
					
					e = errorTemplate;
					e.setValue(null);
					
				} 

				return e;

			}
		};
	}	
	Rule<VictimSegment> getRule404ForVictimConnectedToUcrOffenseCode() {
		return new Rule<VictimSegment>() {
			
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				
				NIBRSError e = null;
				NIBRSError errorTemplate = victimSegment.getErrorTemplate();
				errorTemplate.setDataElementIdentifier("24");
				errorTemplate.setNIBRSErrorCode(NIBRSErrorCode._404);
				
				Set<String> offenseCodeSet = new HashSet<>();
				offenseCodeSet.addAll(victimSegment.getUcrOffenseCodeList());
				offenseCodeSet.removeIf(Objects::isNull);
				
				if (!offenseCodeSet.isEmpty()) {
					
					offenseCodeSet.removeAll(OffenseCode.codeSet());
					
					if (!offenseCodeSet.isEmpty()) {
						e = errorTemplate;
						e.setValue(new ArrayList<String>(offenseCodeSet));
					}
				} 
				
				return e;
				
			}
		};
	}	
	
	Rule<VictimSegment> getRule404OffenderNumberToBeRelated() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {

				NIBRSError e = null;
				NIBRSError errorTemplate = victimSegment.getErrorTemplate();
				errorTemplate.setDataElementIdentifier("34");
				errorTemplate.setNIBRSErrorCode(NIBRSErrorCode._404);

				Set<Integer> victimRelatedOffenderNumberSet = new HashSet<>();
				victimRelatedOffenderNumberSet.addAll(victimSegment.getDistinctValidRelatedOffenderNumberList());
				victimRelatedOffenderNumberSet.removeIf(item -> item == null);

				GroupAIncidentReport parent = (GroupAIncidentReport) victimSegment.getParentReport();
				Set<Integer> offenderSegmentSequenceNumbers = new HashSet<>();
				for (OffenderSegment os : parent.getOffenders()) {
					offenderSegmentSequenceNumbers.add(os.getOffenderSequenceNumber().getValue());
				}

				victimRelatedOffenderNumberSet.removeAll(offenderSegmentSequenceNumbers);
				// for whatever reason, empirical evidence suggests that FBI does not count zero-related offenders as invalid
				victimRelatedOffenderNumberSet.removeIf(item -> item == 0);

				if (!victimRelatedOffenderNumberSet.isEmpty()) {
					e = errorTemplate;
					e.setValue(new ArrayList<Integer>(victimRelatedOffenderNumberSet));
				}

				return e;

			}
		};
	}

	Rule<VictimSegment> getRule401OffenderNumberToBeRelated() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				
				NIBRSError e = null;
				boolean victimRelatedOffenderMissing = allNull(victimSegment.getOffenderNumberRelated());
				boolean crimeAgainstPersonAndRobberyOffense = 
						victimSegment.getUcrOffenseCodeList().contains(OffenseCode._120.code) 
						|| OffenseCode.containsCrimeAgainstPersonCode(victimSegment.getUcrOffenseCodeList());

				if ( victimRelatedOffenderMissing 
						&& victimSegment.isPerson() 
						&& crimeAgainstPersonAndRobberyOffense ) {
					NIBRSError errorTemplate = victimSegment.getErrorTemplate();
					errorTemplate.setDataElementIdentifier("34");
					errorTemplate.setNIBRSErrorCode(NIBRSErrorCode._401);
					e = errorTemplate;
				}
				
				return e;
				
			}
		};
	}
	
	Rule<VictimSegment> getRule401ForTypeOfVictim() {
		return new NotBlankRule<VictimSegment>("typeOfVictim", "25", VictimSegment.class, NIBRSErrorCode._401) ;
	}

	Rule<VictimSegment> getRule404ForTypeOfVictim() {
		return new ValidValueListRule<VictimSegment>("typeOfVictim", "25", VictimSegment.class, NIBRSErrorCode._404, TypeOfVictimCode.codeSet(), false);
	}
	
	Rule<VictimSegment> getRule404ForOfficerOriOtherJurisdiction() {
		// note:  we cannot check if an ORI is in the FBI's database.  And the field is always optional, so nothing to test here
		return new NullObjectRule<>();
	}

	Rule<VictimSegment> getRule404ForOfficerAssignmentType() {
		return new ValidValueListRule<VictimSegment>("officerAssignmentType", "25B", VictimSegment.class, NIBRSErrorCode._404, OfficerAssignmentType.codeSet(), true);
	}

	Rule<VictimSegment> getRule404ForTypeOfOfficerActivityCircumstance() {
		return new ValidValueListRule<VictimSegment>("typeOfOfficerActivityCircumstance", "25A", VictimSegment.class, NIBRSErrorCode._404, TypeOfOfficerActivityCircumstance.codeSet(), true);
	}

	Rule<VictimSegment> getRule401ForTypeOfInjury(){
		return new NotAllBlankRule<VictimSegment>("typeOfInjury", "33", VictimSegment.class, NIBRSErrorCode._401) {
			@Override
			public boolean ignore(VictimSegment o) {
				List<String> offenseCodeList = o.getUcrOffenseCodeList()
					.stream()
					.filter(INJURY_OFFENSE_LIST::contains)
					.distinct()
					.collect(Collectors.toList());
				
				return !(((offenseCodeList.size() == 1 && offenseCodeList.contains("120") && "I".equals(o.getTypeOfVictim())))
						||( (offenseCodeList.size() > 1 || ( offenseCodeList.size() == 1 && !offenseCodeList.contains("120")) )&& o.isPerson())); 
			}
		};
	}

	Rule<VictimSegment> getRule404ForTypeOfInjury(){
		return new ValidValueListRule<VictimSegment>("typeOfInjury", "33", VictimSegment.class, NIBRSErrorCode._404, TypeInjuryCode.codeSet(), false) {
			@Override
			public boolean ignore(VictimSegment o) {
				List<String> offenseCodeList = o.getUcrOffenseCodeList()
					.stream()
					.filter(INJURY_OFFENSE_LIST::contains)
					.distinct()
					.collect(Collectors.toList());
				
				return ArrayUtils.allNull(o.getTypeOfInjury()) && 
						!(((offenseCodeList.size() == 1 && offenseCodeList.contains("120") && "I".equals(o.getTypeOfVictim())))
						||( (offenseCodeList.size() > 1 || ( offenseCodeList.size() == 1 && !offenseCodeList.contains("120")) )&& o.isPerson())); 
			}
		};
	}

	Rule<VictimSegment> getRule404ForSexOfVictim(){
		return personSegmentRulesFactory.getSexValidNonBlankRule("27", NIBRSErrorCode._404);
	}
	
	Rule<VictimSegment> getRule404ForEthnicityOfVictim(){
		return personSegmentRulesFactory.getEthnicityValidNonBlankRule("29", NIBRSErrorCode._404, true);
	}
	
	Rule<VictimSegment> getRule404ForResidentStatusOfVictim(){
		return personSegmentRulesFactory.getResidentStatusValidNonBlankRule("30", NIBRSErrorCode._404, true);
	}
	
	Rule<VictimSegment> getRule404ForRaceOfVictim(){
		return personSegmentRulesFactory.getRaceValidNonBlankRule("28", NIBRSErrorCode._404);
	}
	
	Rule<VictimSegment> getRule404ForAgeOfVictim() {
		return personSegmentRulesFactory.getAgeValidRule("26", NIBRSErrorCode._404, false);
	}

	Rule<VictimSegment> getRule401ForAggravatedAssaultHomicideCircumstances() {
		return new NotAllBlankRule<VictimSegment>("aggravatedAssaultHomicideCircumstances", "31", VictimSegment.class, NIBRSErrorCode._401) {
			@Override
			public boolean ignore(VictimSegment o) {
				return !o.isAggravatedAssaultHomicideCircumstancesMandatory();
			}
		};

	}

	Rule<VictimSegment> getRule404ForAggravatedAssaultHomicideCircumstances() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				
				NIBRSError e = null;

				List<String> aahcList = new ArrayList<>();
				aahcList.addAll(victimSegment.getAggravatedAssaultHomicideCircumstancesList());
				aahcList.removeIf(item -> item == null);
				List<String> applicableOffenses = victimSegment.getUcrOffenseCodeList().stream()
						.filter(Objects::nonNull)
						.filter(OffenseCode::isAggravatedAssaultHomicideCircumstancesOffense)
						.collect(Collectors.toList());
				boolean isMandatory = victimSegment.isAggravatedAssaultHomicideCircumstancesMandatory();
				if (isMandatory) {
					Set<String> validAggravatedAssaultHomicideCodes = new HashSet<>();
					
					for (String offense : applicableOffenses) {
						switch (offense) {
						case "13A":
						case "09A": 
							validAggravatedAssaultHomicideCodes.add("01"); 
							validAggravatedAssaultHomicideCodes.add("02"); 
							validAggravatedAssaultHomicideCodes.add("03"); 
							validAggravatedAssaultHomicideCodes.add("04"); 
							validAggravatedAssaultHomicideCodes.add("05"); 
							validAggravatedAssaultHomicideCodes.add("06"); 
							validAggravatedAssaultHomicideCodes.add("07"); 
							validAggravatedAssaultHomicideCodes.add("08"); 
							validAggravatedAssaultHomicideCodes.add("09"); 
							validAggravatedAssaultHomicideCodes.add("10");
							break; 
						case "09B":
							validAggravatedAssaultHomicideCodes.add("30");
							validAggravatedAssaultHomicideCodes.add("31");
							validAggravatedAssaultHomicideCodes.add("32");
							validAggravatedAssaultHomicideCodes.add("33");
							validAggravatedAssaultHomicideCodes.add("34");
							break;
						case "09C": 
							validAggravatedAssaultHomicideCodes.add("20");
							validAggravatedAssaultHomicideCodes.add("21");
							break; 
						}
					}
					
					if (!aahcList.isEmpty() && !validAggravatedAssaultHomicideCodes.containsAll(aahcList)) {
						e = victimSegment.getErrorTemplate();
						e.setNIBRSErrorCode(NIBRSErrorCode._404);
						e.setDataElementIdentifier("31");
						List<String> invalidValues = aahcList.stream()
								.filter(item-> !validAggravatedAssaultHomicideCodes.contains(item))
								.collect(Collectors.toList());
						e.setValue(invalidValues);
					}
				}

				return e;
				
			}
		};
	}

	Rule<VictimSegment> getRule404ForRelationshipOfVictimToOffender() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				
				NIBRSError e = null;
				
				List<ParsedObject<Integer>> relatedOffenderNumbers = victimSegment.getOffenderNumberRelatedList();
				List<String> relationships = victimSegment.getVictimOffenderRelationshipList();
				List<String> invalidRelationships = new ArrayList<>();
				
				for (int i= 0; i<relatedOffenderNumbers.size(); i++){
					ParsedObject<Integer> offenderNumber = relatedOffenderNumbers.get(i);
					String relationship = relationships.get(i);
					boolean isMandatory = !isEmpty(offenderNumber) &&
							(OffenseCode.containsCrimeAgainstPersonCode(victimSegment.getUcrOffenseCodeList())
									|| OffenseCode.containsCrimeAgainstPropertyCode(victimSegment.getUcrOffenseCodeList())); 
							
					if ((isMandatory && (StringUtils.isBlank(relationship) || !RelationshipOfVictimToOffenderCode.codeSet().contains(relationship)))
						||(!isMandatory && (StringUtils.isNotBlank(relationship)) && !RelationshipOfVictimToOffenderCode.codeSet().contains(relationship))) {
						invalidRelationships.add(StringUtils.trimToEmpty(relationship));
					}
				}
				
				if ( !invalidRelationships.isEmpty() ){
					e = victimSegment.getErrorTemplate();
					e.setNIBRSErrorCode(NIBRSErrorCode._404);
					e.setDataElementIdentifier("35");
					e.setValue(invalidRelationships);
				}

				return e;
				
			}

			private boolean isEmpty(ParsedObject<Integer> offenderNumber) {
				return offenderNumber.getValue() == null || offenderNumber.getValue() == 0;
			}
		};
	}
	
	Rule<VictimSegment> getRule404ForRelationshipOfVictimToOffender_31() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				
				NIBRSError e = null;
				
				List<ParsedObject<Integer>> relatedOffenderNumbers = victimSegment.getOffenderNumberRelatedList();
				List<String> relationships = victimSegment.getVictimOffenderRelationshipList();
				List<String> invalidRelationships = new ArrayList<>();
				
				for (int i= 0; i<relatedOffenderNumbers.size(); i++){
					ParsedObject<Integer> offenderNumber = relatedOffenderNumbers.get(i);
					String relationship = relationships.get(i);
					boolean isMandatory = !isEmpty(offenderNumber) &&
							(OffenseCode.containsCrimeAgainstPersonCode(victimSegment.getUcrOffenseCodeList())
									|| OffenseCode.containsCrimeAgainstPropertyCode(victimSegment.getUcrOffenseCodeList())); 
							
					if ((isMandatory && (StringUtils.isBlank(relationship) || (!RelationshipOfVictimToOffenderCode.codeSet().contains(relationship) && !"HR".equalsIgnoreCase(relationship))))
						||(!isMandatory && (StringUtils.isNotBlank(relationship)) && !RelationshipOfVictimToOffenderCode.codeSet().contains(relationship) && !"HR".equalsIgnoreCase(relationship))) {
						invalidRelationships.add(StringUtils.trimToEmpty(relationship));
					}
				}
				
				if ( !invalidRelationships.isEmpty() ){
					e = victimSegment.getErrorTemplate();
					e.setNIBRSErrorCode(NIBRSErrorCode._404);
					e.setDataElementIdentifier("35");
					e.setValue(invalidRelationships);
				}

				return e;
				
			}

			private boolean isEmpty(ParsedObject<Integer> offenderNumber) {
				return offenderNumber.getValue() == null || offenderNumber.getValue() == 0;
			}
		};
	}

	Rule<VictimSegment> getRule404ForAdditionalJustifiableHomicideCircsumstances() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {

				NIBRSError e = null;

				String ajhc = victimSegment.getAdditionalJustifiableHomicideCircumstances();
				List<String> offenseCodeList = victimSegment.getUcrOffenseCodeList();
				if (offenseCodeList.contains(OffenseCode._09C.code) && ajhc == null) {
					e = victimSegment.getErrorTemplate();
					e.setNIBRSErrorCode(NIBRSErrorCode._404);
					e.setDataElementIdentifier("32");
					e.setValue(null);
				}

				return e;

			}
		};
	}

	Rule<VictimSegment> getRule455ForAdditionalJustifiableHomicideCircsumstances() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				NIBRSError e = null;
				List<String> assaultCircList = victimSegment.getAggravatedAssaultHomicideCircumstancesList();
				if ((assaultCircList.contains(AggravatedAssaultHomicideCircumstancesCode._20.code) ||
						assaultCircList.contains(AggravatedAssaultHomicideCircumstancesCode._21.code))
						&& victimSegment.getAdditionalJustifiableHomicideCircumstances() == null) {
					e = victimSegment.getErrorTemplate();
					e.setDataElementIdentifier("32");
					e.setNIBRSErrorCode(NIBRSErrorCode._455);
					e.setValue(assaultCircList);
				}
				return e;
			}
		};
	}

	Rule<VictimSegment> getRule457ForAdditionalJustifiableHomicideCircsumstances() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				
				NIBRSError e = null;

				String ajhc = victimSegment.getAdditionalJustifiableHomicideCircumstances();
				List<String> validJustHomicideList = Arrays.asList(AggravatedAssaultHomicideCircumstancesCode._09C.code,
						AggravatedAssaultHomicideCircumstancesCode._20.code,
						AggravatedAssaultHomicideCircumstancesCode._21.code);

				if (ajhc != null &&
						!CollectionUtils.containsAny(victimSegment.getAggravatedAssaultHomicideCircumstancesList(), validJustHomicideList)) {
					e = victimSegment.getErrorTemplate();
					e.setDataElementIdentifier("32");
					e.setNIBRSErrorCode(NIBRSErrorCode._457);
					e.setValue(ajhc);
				}

				return e;
				
			}
		};
	}

	Rule<VictimSegment> getRule406ForVictimConnectedToUcrOffenseCode() {
		return new DuplicateCodedValueRule<>("ucrOffenseCodeConnection", "24", VictimSegment.class, NIBRSErrorCode._406);
	}

	Rule<VictimSegment> getRule406ForAggravatedAssaultHomicideCircumstances() {
		return new DuplicateCodedValueRule<VictimSegment>("aggravatedAssaultHomicideCircumstances", "31", VictimSegment.class, NIBRSErrorCode._406);
	}

	Rule<VictimSegment> getRule406OffenderNumberToBeRelated() {
		return new DuplicateCodedValueRule<VictimSegment>("offenderNumberRelated", "34", VictimSegment.class, NIBRSErrorCode._406);
	}

	Rule<VictimSegment> getRule406ForTypeOfInjury() {
		return new DuplicateCodedValueRule<VictimSegment>("typeOfInjury", "33", VictimSegment.class, NIBRSErrorCode._406);
	}
	
	Rule<VictimSegment> getRule407() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				NIBRSError e = null;
				NIBRSError errorTemplate = victimSegment.getErrorTemplate();
				errorTemplate.setDataElementIdentifier("33");
				errorTemplate.setNIBRSErrorCode(NIBRSErrorCode._407);
				Set<String> noneDisallowed = new HashSet<>();
				noneDisallowed.addAll(TypeInjuryCode.codeSet());
				noneDisallowed.remove(TypeInjuryCode.N.code);
				List<String> injuryList = new ArrayList<>();
				injuryList.addAll(victimSegment.getTypeOfInjuryList());
				injuryList.removeIf(item -> item == null);
				errorTemplate.setValue(injuryList);
				if (injuryList.contains(TypeInjuryCode.N.code) && CollectionUtils.containsAny(injuryList, noneDisallowed)) {
					e = errorTemplate;
				}
				return e;
			}
		};
	}
	
	Rule<VictimSegment> getRule409() {
		return personSegmentRulesFactory.getAgeRangeLengthRule("26", NIBRSErrorCode._409);
	}
	
	Rule<VictimSegment> getRule410ForAgeOfVictim() {
		return personSegmentRulesFactory.getProperAgeRangeRule("26", NIBRSErrorCode._410);
	}

	Rule<VictimSegment> getRule419ForTypeOfInjury() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {

				NIBRSError e = null;
				List<String> offenseList = victimSegment.getUcrOffenseCodeList();
				List<String> typeOfInjuryList = new ArrayList<>();
				typeOfInjuryList.addAll(victimSegment.getTypeOfInjuryList());
				typeOfInjuryList.removeIf(item -> item == null);
				
				List<String> allowedOffenseCodes = 
						Arrays.asList(OffenseCode._100.code, OffenseCode._11A.code, OffenseCode._11B.code, OffenseCode._11C.code, 
						OffenseCode._11D.code, OffenseCode._120.code, OffenseCode._13A.code, OffenseCode._13B.code,  
						OffenseCode._210.code, OffenseCode._64A.code, OffenseCode._64B.code);
				boolean containsTypeOfInjuryOffense = offenseList.stream().anyMatch(i->allowedOffenseCodes.contains(i)); 
				boolean containsAggravatedAssaultHomicideCircumstancesOffense = offenseList.stream().anyMatch(OffenseCode::isAggravatedAssaultHomicideCircumstancesOffense); 
				if ((!typeOfInjuryList.isEmpty() && !containsTypeOfInjuryOffense)
						|| (ArrayUtils.notAllNull(victimSegment.getAggravatedAssaultHomicideCircumstances()) && 
								!containsAggravatedAssaultHomicideCircumstancesOffense)) {
					e = victimSegment.getErrorTemplate();
					e.setDataElementIdentifier("33");
					e.setNIBRSErrorCode(NIBRSErrorCode._419);
					e.setValue(typeOfInjuryList);
				}

				return e;
				
			}
		};
	}

	Rule<VictimSegment> getRule419ForAggravatedAssaultHomicideCircumstances() {
		return new Rule<VictimSegment>() {

			@Override
			public NIBRSError apply(VictimSegment victimSegment) {

				NIBRSError e = null;

				List<String> aahc = new ArrayList<>();
				aahc.addAll(victimSegment.getAggravatedAssaultHomicideCircumstancesList());
				aahc.removeIf(item -> item == null);

				List<String> ucrOffenseList = new ArrayList<>();
				ucrOffenseList.addAll(victimSegment.getUcrOffenseCodeList());
				ucrOffenseList.removeIf(item -> item == null);

				List<String> assaultHomicideOffenses = Arrays.asList(OffenseCode._09A.code, OffenseCode._09B.code, OffenseCode._09C.code, OffenseCode._13A.code);

				if (!aahc.isEmpty() && !CollectionUtils.containsAny(ucrOffenseList, assaultHomicideOffenses)) {
					e = victimSegment.getErrorTemplate();
					e.setNIBRSErrorCode(NIBRSErrorCode._419);
					e.setDataElementIdentifier("31");
					e.setValue(aahc);
				}

				return e;
			}
		};
	}

	Rule<VictimSegment> getRule422ForAgeOfVictim() {
		return personSegmentRulesFactory.getNonZeroAgeRangeMinimumRule("26", NIBRSErrorCode._422);
	}

	Rule<VictimSegment> getRule450ForAgeOfVictim__3_1() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				NIBRSError e = null;
				if (victimSegment.getVictimOffenderRelationshipList().contains(RelationshipOfVictimToOffenderCode.SE.code)) {
					NIBRSAge nibrsAge = victimSegment.getAge();
					if (nibrsAge != null) {
						Integer minAge = nibrsAge.getAgeMin();
						if (minAge != null && minAge < 13) {
							e = victimSegment.getErrorTemplate();
							e.setDataElementIdentifier("26");
							e.setNIBRSErrorCode(NIBRSErrorCode._450__3_1);
							e.setValue(nibrsAge);
						}
					}
				}
				return e;
			}
		};
	}

	Rule<VictimSegment> getRule450ForAgeOfVictim__2_1() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				NIBRSError e = null;
				if (victimSegment.getVictimOffenderRelationshipList().contains(RelationshipOfVictimToOffenderCode.SE.code)) {
					NIBRSAge nibrsAge = victimSegment.getAge();
					if (nibrsAge != null) {
						Integer minAge = nibrsAge.getAgeMin();
						if (minAge != null && minAge < 10) {
							e = victimSegment.getErrorTemplate();
							e.setDataElementIdentifier("26");
							e.setNIBRSErrorCode(NIBRSErrorCode._450__2_1);
							e.setValue(nibrsAge);
						}
					}
				}
				return e;
			}
		};
	}

	Rule<VictimSegment> getRule453ForAgeOfVictim() {
		return new PersonVictimNotBlankRule<>("age", "26", NIBRSErrorCode._453, TypeOfVictimCode.I.code);
	}

	Rule<VictimSegment> getRule453ForSexOfVictim() {
		return new PersonVictimNotBlankRule<>("sex", "27", NIBRSErrorCode._453, TypeOfVictimCode.I.code);
	}

	Rule<VictimSegment> getRule453ForRaceOfVictim() {
		return new PersonVictimNotBlankRule<>("race", "28", NIBRSErrorCode._453, TypeOfVictimCode.I.code);
	}
	
	// TODO: need to be able to configure the factory for enforcing leoka rules, or not (for these next two rules)

	Rule<VictimSegment> getRule454ForTypeOfOfficerActivityCircumstance() {
		return new PersonVictimNotBlankRule<>("typeOfOfficerActivityCircumstance", "25A", NIBRSErrorCode._454, TypeOfVictimCode.L.code);
	}

	Rule<VictimSegment> getRule454ForOfficerAssignmentType() {
		return new PersonVictimNotBlankRule<>("officerAssignmentType", "25B", NIBRSErrorCode._454, TypeOfVictimCode.L.code);
	}

	Rule<VictimSegment> getRule454ForSexOfVictim() {
		return new PersonVictimNotBlankRule<>("sex", "27", NIBRSErrorCode._454, TypeOfVictimCode.L.code);
	}
	
	Rule<VictimSegment> getRule454ForRaceOfVictim() {
		return new PersonVictimNotBlankRule<>("race", "28", NIBRSErrorCode._454, TypeOfVictimCode.L.code);
	}
	
	Rule<VictimSegment> getRule454ForAgeOfVictim() {
		return new PersonVictimNotBlankRule<>("age", "26", NIBRSErrorCode._454, TypeOfVictimCode.L.code);
	}
	
	Rule<VictimSegment> getRule458ForAgeOfVictim() {
		return new NonPersonVictimBlankRule<>("age", "26", NIBRSErrorCode._458);
	}

	Rule<VictimSegment> getRule458ForEthnicityOfVictim() {
		return new NonPersonVictimBlankRule<>("ethnicity", "29", NIBRSErrorCode._458);
	}

	Rule<VictimSegment> getRule458ForSexOfVictim() {
		return new NonPersonVictimBlankRule<>("sex", "27", NIBRSErrorCode._458);
	}

	Rule<VictimSegment> getRule458ForRaceOfVictim() {
		return new NonPersonVictimBlankRule<>("race", "28", NIBRSErrorCode._458);
	}

	Rule<VictimSegment> getRule458ForResidentStatusOfVictim() {
		return new NonPersonVictimBlankRule<>("residentStatus", "30", NIBRSErrorCode._458);
	}

	Rule<VictimSegment> getRule458ForTypeOfInjury() {
		return new Rule<VictimSegment>() {

			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				
				NIBRSError e = null;
				
				List<String> typeOfInjuryList = new ArrayList<>();
				typeOfInjuryList.addAll(victimSegment.getTypeOfInjuryList());
				typeOfInjuryList.removeIf(Objects::isNull);

				if (!victimSegment.isPerson() && !typeOfInjuryList.isEmpty()) {
					e = victimSegment.getErrorTemplate();
					e.setDataElementIdentifier("33");
					e.setNIBRSErrorCode(NIBRSErrorCode._458);
				}
				
				return e;
				
			}
		};
	}

	Rule<VictimSegment> getRule458ForOffenderNumberToBeRelated() {
		return new Rule<VictimSegment>() {

			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				
				NIBRSError e = null;
				
				List<String> offenseList = new ArrayList<>();
				offenseList.addAll(victimSegment.getUcrOffenseCodeList());
				offenseList.removeIf(item -> item == null);

				List<Integer> offenderList = new ArrayList<>();
				offenderList.addAll(victimSegment.getDistinctValidRelatedOffenderNumberList());
				offenderList.removeIf(item -> item == null);

				if (!victimSegment.isPerson() && !offenderList.isEmpty()) {
					e = victimSegment.getErrorTemplate();
					e.setDataElementIdentifier("34");
					e.setNIBRSErrorCode(NIBRSErrorCode._458);
					e.setValue(offenderList);
				}
				
				return e;
				
			}
		};
	}

	Rule<VictimSegment> getRule459ForOffenderNumberToBeRelated() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {

				NIBRSError e = null;

				List<Integer> offenderNumberList = new ArrayList<>();
				offenderNumberList.addAll(victimSegment.getDistinctValidRelatedOffenderNumberList());
				offenderNumberList.removeIf(item -> item == null);

				List<String> offenseCodeList = new ArrayList<>();
				offenseCodeList.addAll(victimSegment.getUcrOffenseCodeList());
				offenseCodeList.removeIf(item -> item == null);

				if (!offenderNumberList.isEmpty() && !(OffenseCode.containsCrimeAgainstPersonCode(offenseCodeList) 
						|| OffenseCode.containsCrimeAgainstPropertyCode(offenseCodeList))) {
					e = victimSegment.getErrorTemplate();
					e.setDataElementIdentifier("34");
					e.setNIBRSErrorCode(NIBRSErrorCode._459);
					e.setValue(offenderNumberList);
				}
				
				return e;
				
			}
		};
	}

	Rule<VictimSegment> getRule460ForRelationshipOfVictimToOffender() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {

				NIBRSError e = null;
				List<String> victimOffenderRelationshipList = victimSegment.getVictimOffenderRelationshipList();
				List<ParsedObject<Integer>> offenderNumRelatedList = victimSegment.getOffenderNumberRelatedList();
				
				for (int i=0;i < offenderNumRelatedList.size() && e == null;i++) {
					ParsedObject<Integer> offenderNumber = offenderNumRelatedList.get(i);
					if (!offenderNumber.isInvalid() && offenderNumber.getValue() != null && offenderNumber.getValue() > 0) {
						String rel = victimOffenderRelationshipList.get(i);
						if (rel == null ) {
							e = victimSegment.getErrorTemplate();
							e.setDataElementIdentifier("35");
							e.setNIBRSErrorCode(NIBRSErrorCode._460);
							e.setValue(null);
						}
					}
				}

				return e;
				
			}
		};
	}

	/**
	 * Removed in specs v3-1. Remove it from the rule list. 
	 * @return
	 */
	Rule<VictimSegment> getRule461ForTypeOfVictim() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				NIBRSError e = null;

				boolean containsCrimeAgainstGovernment = victimSegment.getUcrOffenseCodeList()
						.stream()
						.anyMatch(OffenseCode::isCrimeAgainstGovernmentCode); 
						
				if (containsCrimeAgainstGovernment && !TypeOfVictimCode.G.code.equals(victimSegment.getTypeOfVictim())) {
					e = victimSegment.getErrorTemplate();
					e.setNIBRSErrorCode(NIBRSErrorCode._461);
					e.setDataElementIdentifier("25");
					e.setValue(victimSegment.getTypeOfVictim());
				}
				return e;
			}
		};
	}
	
	Rule<VictimSegment> getRule476() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				NIBRSError e = null;
				GroupAIncidentReport parent = (GroupAIncidentReport) victimSegment.getParentReport();
				int spouseCount = 0;
				ParsedObject<Integer>[] offenderNumbersRelated = victimSegment.getOffenderNumberRelated();
				for (int i=0; i < offenderNumbersRelated.length;i++) {
					String relationship = victimSegment.getVictimOffenderRelationship(i);
					ParsedObject<Integer> offenderSequenceNumberPO = offenderNumbersRelated[i];
					if (!(offenderSequenceNumberPO.isMissing() || offenderSequenceNumberPO.isInvalid()) && RelationshipOfVictimToOffenderCode.SE.code.equals(relationship)) {
						OffenderSegment os = parent.getOffenderForSequenceNumber(offenderSequenceNumberPO.getValue());
						List<VictimSegment> siblingVictims = new ArrayList<>();
						siblingVictims.addAll(parent.getVictimsOfOffender(os));
						siblingVictims.remove(victimSegment);
						for (VictimSegment siblingVictim : siblingVictims) {
							ParsedObject<Integer>[] siblingOffenderNumbersRelated = siblingVictim.getOffenderNumberRelated();
							for (int j=0;j < siblingOffenderNumbersRelated.length;j++) {
								if (offenderSequenceNumberPO.equals(siblingOffenderNumbersRelated[j])) {
									if (RelationshipOfVictimToOffenderCode.SE.code.equals(siblingVictim.getVictimOffenderRelationship(j))) {
										spouseCount++;
									}
								}
							}
						}
					}
				}
				if (spouseCount > 0) {
					e = victimSegment.getErrorTemplate();
					e.setNIBRSErrorCode(NIBRSErrorCode._476);
					e.setDataElementIdentifier("35");
					e.setValue(spouseCount);
				}
				return e;
			}
		};
	}

	Rule<VictimSegment> getRule462() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				NIBRSError e = null;
				Set<String> allowedValues = new HashSet<>();
				allowedValues.add(AggravatedAssaultHomicideCircumstancesCode._01.code);
				allowedValues.add(AggravatedAssaultHomicideCircumstancesCode._02.code);
				allowedValues.add(AggravatedAssaultHomicideCircumstancesCode._03.code);
				allowedValues.add(AggravatedAssaultHomicideCircumstancesCode._04.code);
				allowedValues.add(AggravatedAssaultHomicideCircumstancesCode._05.code);
				allowedValues.add(AggravatedAssaultHomicideCircumstancesCode._06.code);
				allowedValues.add(AggravatedAssaultHomicideCircumstancesCode._08.code);
				allowedValues.add(AggravatedAssaultHomicideCircumstancesCode._09.code);
				allowedValues.add(AggravatedAssaultHomicideCircumstancesCode._10.code);
				List<String> aahc = new ArrayList<>();
				aahc.addAll(victimSegment.getAggravatedAssaultHomicideCircumstancesList());
				aahc.removeAll(allowedValues);
				aahc.removeIf(item -> item == null);
				if (victimSegment.getUcrOffenseCodeList().contains(OffenseCode._13A.code) && aahc.size() > 0) {
					e = victimSegment.getErrorTemplate();
					e.setNIBRSErrorCode(NIBRSErrorCode._462);
					e.setDataElementIdentifier("31");
					e.setValue(aahc);
				}
				return e;
			}
		};
	}

	Rule<VictimSegment> getRule463() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				NIBRSError e = null;
				List<String> aahc = victimSegment.getAggravatedAssaultHomicideCircumstancesList();
				if (victimSegment.getUcrOffenseCodeList().contains(OffenseCode._09C.code) &&
						!(aahc.contains(AggravatedAssaultHomicideCircumstancesCode._20.code) || aahc.contains(AggravatedAssaultHomicideCircumstancesCode._21.code))) {
					e = victimSegment.getErrorTemplate();
					e.setNIBRSErrorCode(NIBRSErrorCode._463);
					e.setDataElementIdentifier("31");
					e.setValue(aahc);
				}
				return e;
			}
		};
	}

	Rule<VictimSegment> getRule464ForTypeOfVictim() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				NIBRSError e = null;
				List<String> offenseCodeList = new ArrayList<>();
				offenseCodeList.addAll(victimSegment.getUcrOffenseCodeList());
				offenseCodeList.removeIf(item -> !OffenseCode.isCrimeAgainstPersonCode(item));
				if (!offenseCodeList.isEmpty() && !victimSegment.isPerson()) {
					e = victimSegment.getErrorTemplate();
					e.setDataElementIdentifier("25");
					e.setNIBRSErrorCode(NIBRSErrorCode._464);
					e.setValue(offenseCodeList.get(0));
				}
				return e;
			}
		};
	}

	Rule<VictimSegment> getRule465ForTypeOfVictim() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				NIBRSError e = null;
				if (OffenseCode.containsCrimeAgainstSocietyCode(victimSegment.getUcrOffenseCodeList()) && !TypeOfVictimCode.S.code.equals(victimSegment.getTypeOfVictim())) {
					e = victimSegment.getErrorTemplate();
					e.setNIBRSErrorCode(NIBRSErrorCode._465);
					e.setDataElementIdentifier("25");
					e.setValue(victimSegment.getTypeOfVictim());
					e.setWithinSegmentIdentifier(null);
				}
				return e;
			}
		};
	}

	Rule<VictimSegment> getRule467ForTypeOfVictim() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				NIBRSError e = null;
				if (TypeOfVictimCode.S.code.equals(victimSegment.getTypeOfVictim()) && OffenseCode.containsCrimeAgainstPropertyCode(victimSegment.getUcrOffenseCodeList())) {
					e = victimSegment.getErrorTemplate();
					e.setDataElementIdentifier("25");
					e.setNIBRSErrorCode(NIBRSErrorCode._467);
					e.setValue(TypeOfVictimCode.S.code);
				}
				return e;
			}
		};
	}
	
	Rule<VictimSegment> getRule468ForRelationshipOfVictimToOffender() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {

				NIBRSError e = null;
				List<String> victimOffenderRelationshipList = victimSegment.getVictimOffenderRelationshipList();
				List<Integer> offenderNumRelatedList = victimSegment.getDistinctValidRelatedOffenderNumberList();
				
				for (int i=0;i < offenderNumRelatedList.size() && e == null;i++) {
					Integer offenderNumber = offenderNumRelatedList.get(i);
					if (offenderNumber != null && offenderNumber == 0) {
						String rel = victimOffenderRelationshipList.get(i);
						if (rel != null) {
							e = victimSegment.getErrorTemplate();
							e.setDataElementIdentifier("35");
							e.setNIBRSErrorCode(NIBRSErrorCode._468);
							e.setValue(rel);
						}
					}
				}

				return e;
				
			}
		};
	}

	Rule<VictimSegment> getRule469ForSexOfVictim() {
		return new Rule<VictimSegment>() {

			@Override
			public NIBRSError apply(VictimSegment victimSegment) {

				NIBRSError e = null;
				
				List<String> offenses = Arrays.asList(new String[] {OffenseCode._36B.code, OffenseCode._11A.code});
				List<String> sexes = Arrays.asList(new String[] {SexCode.F.code, SexCode.M.code});

				if (CollectionUtils.containsAny(victimSegment.getUcrOffenseCodeList(), offenses) 
						&& !sexes.contains(victimSegment.getSex())) {
					e = victimSegment.getErrorTemplate();
					e.setDataElementIdentifier("27");
					e.setNIBRSErrorCode(NIBRSErrorCode._469);
					e.setValue(victimSegment.getSex());
				}
				
				return e;
				
			}
		};
	}

	Rule<VictimSegment> getRule481ForAgeOfVictim() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {

				NIBRSError e = null;
				NIBRSAge age = victimSegment.getAge();

				if (victimSegment.getUcrOffenseCodeList().contains(OffenseCode._36B.code) 
						&& age != null 
						&& ((age.getAgeMax() != null && age.getAgeMax() >= 18)
								|| (Objects.equals(age.getNonNumericAge(), "00"))) ) {
					e = victimSegment.getErrorTemplate();
					e.setDataElementIdentifier("26");
					e.setNIBRSErrorCode(NIBRSErrorCode._481);
					e.setValue(age);
				}

				return e;

			}
		};
	}

	Rule<VictimSegment> getRule452() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				
				NIBRSError e = null;
				NIBRSAge age = victimSegment.getAge();
				
				if ( "L".equals(victimSegment.getTypeOfVictim()) 
						&& (age == null 
						|| age.isNonNumeric() || age.getAverage() < 17 || age.getAverage() > 98)) {
					e = victimSegment.getErrorTemplate();
					e.setDataElementIdentifier("26");
					e.setNIBRSErrorCode(NIBRSErrorCode._452);
					e.setValue(age);
				}
				
				return e;
				
			}
		};
	}
	
	Rule<VictimSegment> getRule482ForTypeOfVictim() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {

				NIBRSError e = null;
				List<String> allowedOffenseList = Arrays.asList(OffenseCode._09A.code, OffenseCode._13A.code, OffenseCode._13B.code, OffenseCode._13C.code);

				if (TypeOfVictimCode.L.code.equals(victimSegment.getTypeOfVictim()) && !CollectionUtils.containsAny(victimSegment.getUcrOffenseCodeList(), allowedOffenseList)) {
					e = victimSegment.getErrorTemplate();
					e.setDataElementIdentifier("25");
					e.setNIBRSErrorCode(NIBRSErrorCode._482);
					e.setValue(victimSegment.getTypeOfVictim());
				}
				return e;
			}
		};
	}

	Rule<VictimSegment> getRule483ForTypeOfOfficerActivity() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				NIBRSError e = null;
				if (victimSegment.getTypeOfOfficerActivityCircumstance() != null && !TypeOfVictimCode.L.code.equals(victimSegment.getTypeOfVictim())) {
					e = victimSegment.getErrorTemplate();
					e.setDataElementIdentifier("25A");
					e.setNIBRSErrorCode(NIBRSErrorCode._483);
					e.setValue(victimSegment.getOfficerAssignmentType());
				}
				return e;
			}
		};
	}
	
	Rule<VictimSegment> getRule483ForOfficerAssignmentType() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				NIBRSError e = null;
				if (victimSegment.getOfficerAssignmentType() != null && !TypeOfVictimCode.L.code.equals(victimSegment.getTypeOfVictim())) {
					e = victimSegment.getErrorTemplate();
					e.setDataElementIdentifier("25B");
					e.setNIBRSErrorCode(NIBRSErrorCode._483);
					e.setValue(victimSegment.getOfficerAssignmentType());
				}
				return e;
			}
		};
	}
	
	Rule<VictimSegment> getRule483ForOfficerOtherJurisdictionORI() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				NIBRSError e = null;
				if (victimSegment.getOfficerOtherJurisdictionORI() != null && !TypeOfVictimCode.L.code.equals(victimSegment.getTypeOfVictim())) {
					e = victimSegment.getErrorTemplate();
					e.setDataElementIdentifier("25C");
					e.setNIBRSErrorCode(NIBRSErrorCode._483);
					e.setValue(victimSegment.getOfficerAssignmentType());
				}
				return e;
			}
		};
	}
	
	Rule<VictimSegment> getRule471() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				NIBRSError e = null;
				long voCount = victimSegment.getVictimOffenderRelationshipList().stream()
						.filter(Objects::nonNull)
						.filter(item->RelationshipOfVictimToOffenderCode.VO.code.equals(item))
						.count(); 
				if (voCount > 1) {
					e = victimSegment.getErrorTemplate();
					e.setDataElementIdentifier("34");
					e.setNIBRSErrorCode(NIBRSErrorCode._471);
					e.setValue(RelationshipOfVictimToOffenderCode.VO.code);
				}
				return e;
			}
		};
	}

	Rule<VictimSegment> getRule472() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				NIBRSError e = null;
				GroupAIncidentReport incident = (GroupAIncidentReport) victimSegment.getParentReport();
				List<Integer> offenderNumberList = Arrays.stream(victimSegment.getOffenderNumberRelated())
						.filter(Objects::nonNull)
						.filter(item->item.getValue() != null)
						.map(item->item.getValue())
						.collect(Collectors.toList());
				List<String> relationshipList = victimSegment.getVictimOffenderRelationshipList();
				for (int i=0;i < offenderNumberList.size() && e == null;i++) {
					Integer offenderNumber = offenderNumberList.get(i);
					String relationship = relationshipList.get(i);
					if (offenderNumber != null) {
						OffenderSegment os = incident.getOffenderForSequenceNumber(offenderNumber);
						if (os != null 
							 && (os.getAge() == null || os.getAge().isUnknown()) 
							 && (os.getSex() == null || os.getSex().equals(SexOfOffenderCode.U.code)) 
							 && (os.getRace() == null || os.getRace().equals(RaceOfOffenderCode.U.code)) 
							 && (relationship != null && !RelationshipOfVictimToOffenderCode.RU.code.equals(relationship))) {
							e = victimSegment.getErrorTemplate();
							e.setDataElementIdentifier("35");
							e.setNIBRSErrorCode(NIBRSErrorCode._472);
							e.setValue(relationship);
						}
					}
				}
				return e;
			}
		};
	}
	
	Rule<VictimSegment> getRule475() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				NIBRSError e = null;
				List<String> relationshipList = new ArrayList<>();
				relationshipList.addAll(victimSegment.getVictimOffenderRelationshipList());
				relationshipList.removeIf(item -> item == null);
				if (relationshipList.size() > 1) {
					int spouse = 0;
					for (String rel : relationshipList) {
						if (RelationshipOfVictimToOffenderCode.SE.code.equals(rel)) {
							spouse++;
						}
					}
					if (spouse > 1) {
						e = victimSegment.getErrorTemplate();
						e.setDataElementIdentifier("34");
						e.setNIBRSErrorCode(NIBRSErrorCode._475);
						e.setValue(RelationshipOfVictimToOffenderCode.SE.code);
					}
				}
				return e;
			}
		};
	}
	
	Rule<VictimSegment> getRule477() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				NIBRSError e = null;
				NIBRSError errorTemplate = victimSegment.getErrorTemplate();
				errorTemplate.setNIBRSErrorCode(NIBRSErrorCode._477);
				errorTemplate.setDataElementIdentifier("31");
				List<String> offenseList = new ArrayList<>();
				offenseList.addAll(victimSegment.getUcrOffenseCodeList());
				offenseList.removeIf(item -> item == null);
				List<String> aahc = new ArrayList<>();
				aahc.addAll(victimSegment.getAggravatedAssaultHomicideCircumstancesList());
				aahc.removeIf(item -> item == null);
				if (offenseList.contains(OffenseCode._13A.code) || offenseList.contains(OffenseCode._09A.code)) {
					Set<String> allowedSet = new HashSet<>();
					allowedSet.add(AggravatedAssaultHomicideCircumstancesCode._01.code);
					allowedSet.add(AggravatedAssaultHomicideCircumstancesCode._02.code);
					allowedSet.add(AggravatedAssaultHomicideCircumstancesCode._03.code);
					allowedSet.add(AggravatedAssaultHomicideCircumstancesCode._04.code);
					allowedSet.add(AggravatedAssaultHomicideCircumstancesCode._05.code);
					allowedSet.add(AggravatedAssaultHomicideCircumstancesCode._06.code);
					allowedSet.add(AggravatedAssaultHomicideCircumstancesCode._07.code);
					allowedSet.add(AggravatedAssaultHomicideCircumstancesCode._08.code);
					allowedSet.add(AggravatedAssaultHomicideCircumstancesCode._09.code);
					allowedSet.add(AggravatedAssaultHomicideCircumstancesCode._10.code);
					List<String> aahc2 = new ArrayList<>();
					aahc2.addAll(aahc);
					aahc2.removeAll(allowedSet);
					if (aahc.size() > 2 || aahc2.size() > 0) {
						e = errorTemplate;
						e.setValue(aahc2);
					}
				} else if (offenseList.contains(OffenseCode._09B.code)) {
					Set<String> allowedSet = new HashSet<>();
					allowedSet.add(AggravatedAssaultHomicideCircumstancesCode._30.code);
					allowedSet.add(AggravatedAssaultHomicideCircumstancesCode._31.code);
					allowedSet.add(AggravatedAssaultHomicideCircumstancesCode._32.code);
					allowedSet.add(AggravatedAssaultHomicideCircumstancesCode._33.code);
					allowedSet.add(AggravatedAssaultHomicideCircumstancesCode._34.code);
					List<String> aahc2 = new ArrayList<>();
					aahc2.addAll(aahc);
					aahc2.removeAll(allowedSet);
					if (aahc.size() > 1 || aahc2.size() > 0) {
						e = errorTemplate;
						e.setValue(aahc2);
					}
				} else if (offenseList.contains(OffenseCode._09C.code)) {
					Set<String> allowedSet = new HashSet<>();
					allowedSet.add(AggravatedAssaultHomicideCircumstancesCode._20.code);
					allowedSet.add(AggravatedAssaultHomicideCircumstancesCode._21.code);
					List<String> aahc2 = new ArrayList<>();
					aahc2.addAll(aahc);
					aahc2.removeAll(allowedSet);
					if (aahc.size() > 1 || aahc2.size() > 0) {
						e = errorTemplate;
						e.setValue(aahc2);
					}
				}
				return e;
			}
		};
	}
	
	Rule<VictimSegment> getRule478() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				NIBRSError e = null;
				NIBRSError errorTemplate = victimSegment.getErrorTemplate();
				errorTemplate.setDataElementIdentifier("24");
				errorTemplate.setNIBRSErrorCode(NIBRSErrorCode._478);
				errorTemplate.setValue(null);
				
				List<String> offenseList = victimSegment.getUcrOffenseCodeList()
						.stream()
						.distinct()
						.filter(Objects::nonNull)
						.collect(Collectors.toList());

				if (offenseList.size() > 1){
					for (int i=0; i< offenseList.size(); i++){
						
						Rule478OffenseCode offenseI = Rule478OffenseCode.valueOfAny(offenseList.get(i)); 
						if (offenseI == null){
							continue; 
						}
						
						for (int j = i + 1; j < offenseList.size(); j++ ){
							Rule478OffenseCode offenseJ = Rule478OffenseCode.valueOfAny(offenseList.get(j));
							if (offenseJ != null && rule478ExclusionTable[offenseI.ordinal()][offenseJ.ordinal()]){
								e = errorTemplate;
								break;
							}
						}
					}
				}
				return e;
			}
		};
	}
	
	Rule<VictimSegment> getRule479() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				NIBRSError e = null;
				List<String> typeOfInjuryList = victimSegment.getTypeOfInjuryList()
						.stream()
						.filter(Objects::nonNull)
						.collect(Collectors.toList());
				boolean containsOnly13B = victimSegment.getUcrOffenseCodeList().stream()
						.filter(Objects::nonNull)
						.distinct()
						.allMatch(item->Objects.equals(item, OffenseCode._13B.code));
				boolean containsOnlyMinorInjuries = typeOfInjuryList
						.stream()
						.allMatch(injury->Objects.equals(injury, TypeInjuryCode.N.code) ||
								Objects.equals(injury, TypeInjuryCode.M.code));
				if (containsOnly13B && !containsOnlyMinorInjuries) {
					e = victimSegment.getErrorTemplate();
					e.setDataElementIdentifier("33");
					e.setNIBRSErrorCode(NIBRSErrorCode._479);
					List<String> il = new ArrayList<>();
					il.addAll(typeOfInjuryList);
					il.removeIf(item -> item == null);
					e.setValue(il);
				}
				return e;
			}
		};
	}
	
	Rule<VictimSegment> getRule456() {
		return new Rule<VictimSegment>() {
			@Override
			public NIBRSError apply(VictimSegment victimSegment) {
				NIBRSError e = null;
				List<String> aahc = new ArrayList<>();
				aahc.addAll(victimSegment.getAggravatedAssaultHomicideCircumstancesList());
				aahc.removeIf(item -> item == null);
				NIBRSError errorTemplate = victimSegment.getErrorTemplate();
				errorTemplate.setDataElementIdentifier("31");
				errorTemplate.setNIBRSErrorCode(NIBRSErrorCode._456);
				errorTemplate.setValue(aahc);
				if (aahc.contains(AggravatedAssaultHomicideCircumstancesCode._10.code) && aahc.size() > 1) {
					e = errorTemplate;
				} else if (aahc.size() > 1) {
					Set<String> categorySet = new HashSet<>();
					for (String s : aahc) {
						categorySet.add(s.substring(0, 1));
					}
					if (categorySet.size() > 1) {
						e = errorTemplate;
					}
				}
				return e;
			}
		};
	}

	private enum Rule478OffenseCode{
		_09A, _09B, _11A, _11B, _11C, _11D, _120, _13A, _13B, _13C, 
		_23A, _23B, _23C, _23D, _23E, _23F, _23G, _23H, _240, _36A, 
		_36B;
		
		@Override
		public String toString(){
			return StringUtils.substringAfter(this.name(), "_");
		}
		
		public static Rule478OffenseCode valueOfAny(String code){
			code = StringUtils.prependIfMissing(StringUtils.trimToEmpty(code), "_");
			
			if (Rule478OffenseCode.codeSet().contains(code)){
				return Rule478OffenseCode.valueOf(code);
			}
			else {
				return null;
			}
		}
		
		public static List<String> codeSet(){
			return Arrays.asList(Rule478OffenseCode.values())
					.stream()
					.map(item->item.name())
					.collect(Collectors.toList());
		}
	}
	
	private boolean[][] rule478ExclusionTable = new boolean[][]{
			{true, true, false, false, false, false, false, true, true, true, false, false, false, false, false, false, false, false, false, false, false}, 
			{true, true, false, false, false, false, false, true, true, true, false, false, false, false, false, false, false, false, false, false, false}, 
			{false, false, true, false, false, true, false, true, true, true, false, false, false, false, false, false, false, false, false, true, true}, 
			{false, false, false, true, false, true, false, true, true, true, false, false, false, false, false, false, false, false, false, true, true}, 
			{false, false, false, false, true, true, false, true, true, true, false, false, false, false, false, false, false, false, false, true, true}, 
			{false, false, true, true, true, true, false, false, true, true, false, false, false, false, false, false, false, false, false, true, true}, 
			{false, false, false, false, false, false, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false}, 
			{true, true, true, true, true, false, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false}, 
			{true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false}, 
			{true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false}, 
			{false, false, false, false, false, false, true, false, false, false, true, false, false, false, false, false, false, false, false, false, false}, 
			{false, false, false, false, false, false, true, false, false, false, false, true, false, false, false, false, false, false, false, false, false}, 
			{false, false, false, false, false, false, true, false, false, false, false, false, true, false, false, false, false, false, false, false, false}, 
			{false, false, false, false, false, false, true, false, false, false, false, false, false, true, false, false, false, false, false, false, false}, 
			{false, false, false, false, false, false, true, false, false, false, false, false, false, false, true, false, false, false, false, false, false}, 
			{false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, true, false, false, false, false, false}, 
			{false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, true, false, false, false, false}, 
			{false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, true, false, false, false}, 
			{false, false, false, false, false, false, true, false, false, false, false, false, false, false, false, false, false, false, true, false, false}, 
			{false, false, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, true, false}, 
			{false, false, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true}, 
		}; 
}
