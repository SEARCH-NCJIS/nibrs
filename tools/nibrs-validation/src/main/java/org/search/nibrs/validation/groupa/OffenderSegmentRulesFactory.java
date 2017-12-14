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

import java.util.ArrayList;
import java.util.List;

import org.search.nibrs.common.NIBRSError;
import org.search.nibrs.common.ParsedObject;
import org.search.nibrs.model.GroupAIncidentReport;
import org.search.nibrs.model.NIBRSAge;
import org.search.nibrs.model.OffenderSegment;
import org.search.nibrs.model.VictimSegment;
import org.search.nibrs.model.codes.ClearedExceptionallyCode;
import org.search.nibrs.model.codes.NIBRSErrorCode;
import org.search.nibrs.model.codes.RaceCode;
import org.search.nibrs.model.codes.RelationshipOfVictimToOffenderCode;
import org.search.nibrs.model.codes.SexCode;
import org.search.nibrs.validation.PersonSegmentRulesFactory;
import org.search.nibrs.validation.rules.AbstractBeanPropertyRule;
import org.search.nibrs.validation.rules.Rule;

public class OffenderSegmentRulesFactory {

	private static abstract class RelatedVictimAndOffenderRule implements Rule<OffenderSegment> {
		@Override
		public NIBRSError apply(OffenderSegment offenderSegment) {
			NIBRSError e = null;
			Integer offenderSequenceNumber = offenderSegment.getOffenderSequenceNumber().getValue();
			GroupAIncidentReport incident = (GroupAIncidentReport) offenderSegment.getParentReport();
			List<VictimSegment> victims = incident.getVictims();
			for (int i = 0; i < victims.size() && e == null; i++) {
				VictimSegment victimSegment = victims.get(i);
				List<ParsedObject<Integer>> relatedOffenders = victimSegment.getOffenderNumberRelatedList();
				for (int j = 0; j < relatedOffenders.size(); j++) {
					ParsedObject<Integer> relatedOffender = relatedOffenders.get(j);
					if (!(relatedOffender.isMissing() || relatedOffender.isInvalid()) && relatedOffender.getValue() == offenderSequenceNumber) {
						String relationship = victimSegment.getVictimOffenderRelationship()[j];
						if (relationship != null) {
							e = validateRelatedVictimAndOffender(offenderSegment, victimSegment, relationship);
						}
					}
				}
			}
			return e;
		}

		protected abstract NIBRSError validateRelatedVictimAndOffender(OffenderSegment offenderSegment, VictimSegment victimSegment, String relationship);
		
	}

	private static final class UnknownOffenderDemographicsRule extends AbstractBeanPropertyRule<OffenderSegment> {

		public UnknownOffenderDemographicsRule(String propertyName, String dataElementIdentifier) {
			super(propertyName, dataElementIdentifier, OffenderSegment.class, NIBRSErrorCode._552);
		}

		@Override
		protected boolean propertyViolatesRule(Object value, OffenderSegment subject) {
			ParsedObject<Integer> sequenceNumber = subject.getOffenderSequenceNumber();
			return !sequenceNumber.isInvalid() && !sequenceNumber.isMissing() && sequenceNumber.getValue() == 0 && value != null;
		}
		
	}
	
	private List<Rule<OffenderSegment>> rulesList;
	private PersonSegmentRulesFactory<OffenderSegment> personSegmentRulesFactory;
	
	public static OffenderSegmentRulesFactory instance(){
		return new OffenderSegmentRulesFactory();
	}
	
	OffenderSegmentRulesFactory() {
		personSegmentRulesFactory = new PersonSegmentRulesFactory<OffenderSegment>(OffenderSegment.class);
		rulesList = new ArrayList<Rule<OffenderSegment>>();
		initRulesList(rulesList);
	}
	
	private void initRulesList(List<Rule<OffenderSegment>> rulesList) {
		rulesList.add(getRule501());
		rulesList.add(getRule556ForAgeOfOffender());
		rulesList.add(getRule504ForSexOfOffender());
		rulesList.add(getRule504ForRaceOfOffender());
		rulesList.add(getRule504ForEthnicityOfOffender());
		rulesList.add(getRule509());
		rulesList.add(getRule510());
		rulesList.add(getRule522());
		rulesList.add(getRule550());
		rulesList.add(getRule552ForAge());
		rulesList.add(getRule552ForSex());
		rulesList.add(getRule552ForRace());
		rulesList.add(getRule552ForEthnicity());
		rulesList.add(getRule554());
		rulesList.add(getRule557());
		rulesList.add(getRule572());
	}

	public List<Rule<OffenderSegment>> getRulesList() {
		return rulesList;
	}	

	Rule<OffenderSegment> getRule501() {
		return new Rule<OffenderSegment>() {
			@Override
			public NIBRSError apply(OffenderSegment offenderSegment) {

				ParsedObject<Integer> offenderSequenceNumber = offenderSegment.getOffenderSequenceNumber();
				Integer offenderSequenceNumberValue = offenderSequenceNumber.getValue();
				NIBRSError e = null;

				if (offenderSequenceNumber.isMissing() || offenderSequenceNumber.isInvalid() || offenderSequenceNumberValue < 0 || offenderSequenceNumberValue > 99) {
					e = offenderSegment.getErrorTemplate();
					e.setNIBRSErrorCode(NIBRSErrorCode._501);
					e.setDataElementIdentifier("36");
					e.setValue(offenderSequenceNumber);
				}

				return e;

			}
		};
	}

	Rule<OffenderSegment> getRule557() {
		return new Rule<OffenderSegment>() {
			@Override
			public NIBRSError apply(OffenderSegment offenderSegment) {

				ParsedObject<Integer> offenderSequenceNumber = offenderSegment.getOffenderSequenceNumber();
				GroupAIncidentReport parent = (GroupAIncidentReport) offenderSegment.getParentReport();
				String exceptionalClearanceCode = parent.getExceptionalClearanceCode();
				
				NIBRSError e = null;

				if (!offenderSequenceNumber.isMissing() && !offenderSequenceNumber.isInvalid() && offenderSequenceNumber.getValue() == 0 &&
					!(exceptionalClearanceCode == null || ClearedExceptionallyCode.N.code.equals(exceptionalClearanceCode))) {
					e = offenderSegment.getErrorTemplate();
					e.setNIBRSErrorCode(NIBRSErrorCode._557);
					e.setDataElementIdentifier("36");
					e.setValue(offenderSequenceNumber.getValue());
				}

				return e;

			}
		};
	}

	Rule<OffenderSegment> getRule556ForAgeOfOffender() {
		return personSegmentRulesFactory.getAgeValidRule("37", NIBRSErrorCode._556, false);
	}

	Rule<OffenderSegment> getRule504ForSexOfOffender() {
		return personSegmentRulesFactory.getSexValidNonBlankRule("38", NIBRSErrorCode._504);
	}

	Rule<OffenderSegment> getRule504ForRaceOfOffender() {
		return personSegmentRulesFactory.getRaceValidNonBlankRule("39", NIBRSErrorCode._504);
	}

	Rule<OffenderSegment> getRule504ForEthnicityOfOffender() {
		return personSegmentRulesFactory.getEthnicityValidNonBlankRule("39A", NIBRSErrorCode._504, true);
	}
	
	Rule<OffenderSegment> getRule509() {
		return personSegmentRulesFactory.getAgeRangeLengthRule("37", NIBRSErrorCode._509);
	}
	
	Rule<OffenderSegment> getRule510() {
		return personSegmentRulesFactory.getProperAgeRangeRule("37", NIBRSErrorCode._510);
	}
	
	Rule<OffenderSegment> getRule522() {
		return personSegmentRulesFactory.getNonZeroAgeRangeMinimumRule("37", NIBRSErrorCode._522);
	}

	Rule<OffenderSegment> getRule550() {
		return new RelatedVictimAndOffenderRule() {
			@Override
			protected NIBRSError validateRelatedVictimAndOffender(OffenderSegment offenderSegment, VictimSegment victimSegment, String relationship) {
				NIBRSAge age = offenderSegment.getAge();
				NIBRSError e = null;
				if (RelationshipOfVictimToOffenderCode.SE.code.equals(relationship) && age != null && !age.isUnknown() && age.getError() == null 
						&& !age.hasInvalidLength() && age.getAgeMin() < 13) {
					e = offenderSegment.getErrorTemplate();
					e.setDataElementIdentifier("37");
					e.setValue(age);
					e.setNIBRSErrorCode(NIBRSErrorCode._550);
				}
				return e;
			}
		};
	}
	
	Rule<OffenderSegment> getRule552ForAge() {
		return new UnknownOffenderDemographicsRule("age", "37");
	}
	
	Rule<OffenderSegment> getRule552ForSex() {
		return new UnknownOffenderDemographicsRule("sex", "38");
	}
	
	Rule<OffenderSegment> getRule552ForRace() {
		return new UnknownOffenderDemographicsRule("race", "39");
	}
	
	Rule<OffenderSegment> getRule552ForEthnicity() {
		return new UnknownOffenderDemographicsRule("ethnicity", "39A");
	}

	// Removed with Tech Spec 3.1
	/*
	Rule<OffenderSegment> getRule553() {
		return new RelatedVictimAndOffenderRule() {
			// note:  Per FBI, this rule will soon be changed
			@Override
			protected NIBRSError validateRelatedVictimAndOffender(OffenderSegment offenderSegment, VictimSegment victimSegment, String relationship) {
				String offenderSex = offenderSegment.getSex();
				String victimSex = victimSegment.getSex();
				NIBRSError e = null;
				if ((RelationshipOfVictimToOffenderCode.HR.code.equals(relationship) && offenderSex != null && !offenderSex.equals(victimSex)) ||
						(RelationshipOfVictimToOffenderCode.BG.code.equals(relationship) && offenderSex != null && offenderSex.equals(victimSex)) ||
						(RelationshipOfVictimToOffenderCode.SE.code.equals(relationship) && offenderSex != null && offenderSex.equals(victimSex)) ||
						(RelationshipOfVictimToOffenderCode.CS.code.equals(relationship) && offenderSex != null && offenderSex.equals(victimSex)) ||
						(RelationshipOfVictimToOffenderCode.XS.code.equals(relationship) && offenderSex != null && offenderSex.equals(victimSex))) {
					e = offenderSegment.getErrorTemplate();
					e.setDataElementIdentifier("38");
					e.setValue(offenderSex);
					e.setNIBRSErrorCode(NIBRSErrorCode._553);
				}
				return e;
			}
		};
	}
	*/
	
	Rule<OffenderSegment> getRule554() {
		return new RelatedVictimAndOffenderRule() {
			@Override
			protected NIBRSError validateRelatedVictimAndOffender(OffenderSegment offenderSegment, VictimSegment victimSegment, String relationship) {
				NIBRSAge offenderAge = offenderSegment.getAge();
				NIBRSAge victimAge = victimSegment.getAge();
				NIBRSError e = null;
				if (((RelationshipOfVictimToOffenderCode.PA.code.equals(relationship) ||  RelationshipOfVictimToOffenderCode.GP.code.equals(relationship)) &&
						offenderAge != null && victimAge != null && !offenderAge.isUnknown() && !victimAge.isUnknown() && !victimAge.isOlderThan(offenderAge, true)) ||
					((RelationshipOfVictimToOffenderCode.CH.code.equals(relationship) ||  RelationshipOfVictimToOffenderCode.GC.code.equals(relationship)) &&
						offenderAge != null && victimAge != null && !offenderAge.isUnknown() && !victimAge.isUnknown() && !victimAge.isYoungerThan(offenderAge, true))) {
					e = offenderSegment.getErrorTemplate();
					e.setDataElementIdentifier("37");
					e.setValue(offenderAge);
					e.setNIBRSErrorCode(NIBRSErrorCode._554);
				}
				return e;
			}
		};
	}
	
//	Rule<OffenderSegment> getRule556() {
//		return new Rule<OffenderSegment>() {
//			@Override
//			public NIBRSError apply(OffenderSegment offenderSegment) {
//				// note: numeric range 00-99 requirement is handled by rule 501
//				NIBRSError e = null;
//				NIBRSAge age = offenderSegment.getAge();
//				if (age != null && !age.isUnknown() && (age.isNonNumeric() || age.hasInvalidLength())) {
//					e = offenderSegment.getErrorTemplate();
//					e.setDataElementIdentifier("37");
//					e.setNIBRSErrorCode(NIBRSErrorCode._556);
//					e.setValue(age);
//				}
//				return e;
//			}
//		};
//	}
//	
	Rule<OffenderSegment> getRule572() {
		return new RelatedVictimAndOffenderRule() {
			@Override
			protected NIBRSError validateRelatedVictimAndOffender(OffenderSegment offenderSegment, VictimSegment victimSegment, String relationship) {
				NIBRSError e = null;
				NIBRSAge age = offenderSegment.getAge();
				String sex = offenderSegment.getSex();
				String race = offenderSegment.getRace();
				if (!RelationshipOfVictimToOffenderCode.RU.code.equals(relationship) &&
						((age != null && age.isUnknown() && SexCode.U.code.equals(sex) && RaceCode.U.code.equals(race)) ||
						 (age == null && sex == null && race == null))) {
					e = offenderSegment.getErrorTemplate();
					e.setDataElementIdentifier("35");
					e.setValue(relationship);
					e.setNIBRSErrorCode(NIBRSErrorCode._572);
				}
				return e;
			}
		};
	}
	
}
