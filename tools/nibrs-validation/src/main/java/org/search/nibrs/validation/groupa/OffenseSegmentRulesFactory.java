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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.search.nibrs.common.NIBRSError;
import org.search.nibrs.common.ParsedObject;
import org.search.nibrs.model.GroupAIncidentReport;
import org.search.nibrs.model.OffenseSegment;
import org.search.nibrs.model.VictimSegment;
import org.search.nibrs.model.codes.AutomaticWeaponIndicatorCode;
import org.search.nibrs.model.codes.BiasMotivationCode;
import org.search.nibrs.model.codes.LocationTypeCode;
import org.search.nibrs.model.codes.MethodOfEntryCode;
import org.search.nibrs.model.codes.NIBRSErrorCode;
import org.search.nibrs.model.codes.OffenderSuspectedOfUsingCode;
import org.search.nibrs.model.codes.OffenseAttemptedCompletedCode;
import org.search.nibrs.model.codes.OffenseCode;
import org.search.nibrs.model.codes.TypeOfCriminalActivityCode;
import org.search.nibrs.model.codes.TypeOfWeaponForceCode;
import org.search.nibrs.util.ArrayUtils;
import org.search.nibrs.validation.rules.DuplicateCodedValueRule;
import org.search.nibrs.validation.rules.ExclusiveCodedValueRule;
import org.search.nibrs.validation.rules.NotAllBlankRule;
import org.search.nibrs.validation.rules.NotBlankRule;
import org.search.nibrs.validation.rules.NumericValueRule;
import org.search.nibrs.validation.rules.Rule;
import org.search.nibrs.validation.rules.ValidValueListRule;

public class OffenseSegmentRulesFactory {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(OffenseSegmentRulesFactory.class);
	
	private static final class Rule219 implements Rule<OffenseSegment> {
		
		private Set<String> activitySet1 = new HashSet<>();
		private Set<String> activitySet2 = new HashSet<>();
		private Set<String> activitySet3 = new HashSet<>();
		private Set<String> offenseSet1 = new HashSet<>();
		private Set<String> offenseSet2 = new HashSet<>();
		private Set<String> offenseSet3 = new HashSet<>();
		
		public Rule219() {
			
			activitySet1.addAll(Arrays.asList(new String[] {
				TypeOfCriminalActivityCode.B.code,
				TypeOfCriminalActivityCode.C.code,
				TypeOfCriminalActivityCode.D.code,
				TypeOfCriminalActivityCode.E.code,
				TypeOfCriminalActivityCode.O.code,
				TypeOfCriminalActivityCode.P.code,
				TypeOfCriminalActivityCode.T.code,
				TypeOfCriminalActivityCode.U.code
			}));
			
			offenseSet1.addAll(Arrays.asList(new String[] {
				OffenseCode._250.code,
				OffenseCode._280.code,
				OffenseCode._30C.code,
				OffenseCode._35A.code,
				OffenseCode._35B.code,
				OffenseCode._39C.code,
				OffenseCode._370.code,
				OffenseCode._49A.code,
				OffenseCode._520.code, 
				OffenseCode._521.code, 
				OffenseCode._522.code, 
				OffenseCode._526.code, 
				OffenseCode._58A.code, 
				OffenseCode._58B.code, 
				OffenseCode._61A.code, 
				OffenseCode._61B.code, 
				OffenseCode._620.code, 
			}));
			
			activitySet2.addAll(Arrays.asList(new String[] {
				TypeOfCriminalActivityCode.J.code,
				TypeOfCriminalActivityCode.G.code,
				TypeOfCriminalActivityCode.N.code
			}));
			
			offenseSet2.addAll(Arrays.asList(new String[] {
				OffenseCode._09A.code,
				OffenseCode._09B.code,
				OffenseCode._100.code,
				OffenseCode._11A.code,
				OffenseCode._11B.code,
				OffenseCode._11C.code,
				OffenseCode._11D.code,
				OffenseCode._120.code,
				OffenseCode._13A.code,
				OffenseCode._13B.code,
				OffenseCode._13C.code
			}));
			
			activitySet3.addAll(Arrays.asList(new String[] {
				TypeOfCriminalActivityCode.A.code,
				TypeOfCriminalActivityCode.F.code,
				TypeOfCriminalActivityCode.I.code,
				TypeOfCriminalActivityCode.S.code
			}));
			
			offenseSet3.addAll(Arrays.asList(new String[] {
					OffenseCode._720.code
			}));
			
		}

		@Override
		public NIBRSError apply(OffenseSegment subject) {
			NIBRSError ret = null;
			String offenseCode = subject.getUcrOffenseCode();
			for (String typeOfCriminalActivity : subject.getTypeOfCriminalActivity()) {
				if (typeOfCriminalActivity != null) {
					if (activitySet1.contains(typeOfCriminalActivity) && !offenseSet1.contains(offenseCode) ||
							activitySet2.contains(typeOfCriminalActivity) && !offenseSet2.contains(offenseCode) ||
							activitySet3.contains(typeOfCriminalActivity) && !offenseSet3.contains(offenseCode)) {
						ret = subject.getErrorTemplate();
						ret.setValue(typeOfCriminalActivity);
						ret.setDataElementIdentifier("12");
						ret.setNIBRSErrorCode(NIBRSErrorCode._219);
					}
				}
			}
			return ret;
		}

	}
	
	private List<Rule<OffenseSegment>> rulesList = new ArrayList<>();
	private Set<String> firearmCodes = new HashSet<>();
	
	public OffenseSegmentRulesFactory() {
		
		firearmCodes.add(TypeOfWeaponForceCode._11.code);
		firearmCodes.add(TypeOfWeaponForceCode._12.code);
		firearmCodes.add(TypeOfWeaponForceCode._13.code);
		firearmCodes.add(TypeOfWeaponForceCode._14.code);
		firearmCodes.add(TypeOfWeaponForceCode._15.code);
		
		rulesList.add(getRule201ForSingleStringProperty("ucrOffenseCode", "6"));
		rulesList.add(getRule201ForOffendersSuspectedOfUsing());
		rulesList.add(getRule201ForSingleStringProperty("offenseAttemptedCompleted", "7"));
		rulesList.add(getRule201ForSingleStringProperty("locationType", "9"));
		rulesList.add(getRule201ForStringArrayProperty("biasMotivation", "8A"));
		
		rulesList.add(getRule204ForValueList("biasMotivation", "8A", BiasMotivationCode.codeSet()));
		rulesList.add(getRule204ForValueList("offendersSuspectedOfUsing", "8", OffenderSuspectedOfUsingCode.codeSet()));
		rulesList.add(getRule204ForValueList("locationType", "9", LocationTypeCode.codeSet()));
		rulesList.add(getRule204ForValueList("methodOfEntry", "11", MethodOfEntryCode.codeSet()));
		rulesList.add(getRule204ForValueList("typeOfCriminalActivity", "12", TypeOfCriminalActivityCode.codeSet()));
		rulesList.add(getRule204ForValueList("typeOfWeaponForceInvolved", "13", TypeOfWeaponForceCode.codeSet()));
		rulesList.add(getRule204ForValueList("automaticWeaponIndicator", "13", AutomaticWeaponIndicatorCode.codeSet()));
		rulesList.add(getRule204ForValueList("automaticWeaponIndicator", "13", AutomaticWeaponIndicatorCode.codeSet()));
		rulesList.add(getRule204ForUCROffenseCode());
		rulesList.add(getRule204ForPremisesEntered());
		
		rulesList.add(getRule205());
				
		rulesList.add(getRule206("typeOfCriminalActivity", "12"));
		rulesList.add(getRule206("typeOfWeaponForceInvolved", "13"));
		rulesList.add(getRule206("offendersSuspectedOfUsing", "8"));
		rulesList.add(getRule206("biasMotivation", "8A"));
		
		rulesList.add(getRule207("typeOfCriminalActivity", "12", TypeOfCriminalActivityCode.noneOrUnknownValueCodeSet()));
		rulesList.add(getRule207("typeOfWeaponForceInvolved", "13", TypeOfWeaponForceCode.noneValueCodeSet()));
		rulesList.add(getRule207("offendersSuspectedOfUsing", "8", OffenderSuspectedOfUsingCode.noneOrUnknownValueCodeSet()));
		rulesList.add(getRule207("biasMotivation", "8A", BiasMotivationCode.noneOrUnknownValueCodeSet()));
		
		rulesList.add(getRule219());
		rulesList.add(getRule220());
		rulesList.add(getRule221());
		rulesList.add(getRule222());
		rulesList.add(getRule251());
		rulesList.add(getRule252());
		rulesList.add(getRule253());
		rulesList.add(getRule254());
		rulesList.add(getRule255());
		rulesList.add(getRule256());
		rulesList.add(getRule257());
		rulesList.add(getRule258());
		rulesList.add(getRule264());
		rulesList.add(getRule265());
		rulesList.add(getRule267());
		rulesList.add(getRule269());
		rulesList.add(getRule270());
		
		rulesList.add(getRule065());
		
	}
	
	Rule<OffenseSegment> getRule065() {
		return new Rule<OffenseSegment>() {
			@Override
			public NIBRSError apply(OffenseSegment subject) {
				NIBRSError e = null;
				String ucrCode = subject.getUcrOffenseCode();
				boolean found = false;
				for (VictimSegment vs : ((GroupAIncidentReport) subject.getParentReport()).getVictims()) {
					if (vs.getUcrOffenseCodeList().contains(ucrCode)) {
						found = true;
						break;
					}
				}
				if (!found) {
					e = subject.getErrorTemplate();
					e.setValue(ucrCode);
					e.setDataElementIdentifier("L 2");
					e.setNIBRSErrorCode(NIBRSErrorCode._065);
					e.setWithinSegmentIdentifier(null);
					e.setCrossSegment(true);
				}
				return e;
			}
		};
	}
	
	Rule<OffenseSegment> getRule270() {
		return new Rule<OffenseSegment>() {
			@Override
			public NIBRSError apply(OffenseSegment subject) {
				NIBRSError ret = null;
				String offenseCode = subject.getUcrOffenseCode();
				if (OffenseCode._09C.code.equals(offenseCode)) {
					String[] biasMotivations = subject.getBiasMotivation();
					for (String b : biasMotivations) {
						if (b != null && !BiasMotivationCode._88.code.equals(b)) {
							ret = subject.getErrorTemplate();
							ret.setDataElementIdentifier("8A");
							ret.setValue(b);
							ret.setNIBRSErrorCode(NIBRSErrorCode._270);
						}
					}
				}
				return ret;
			}
		};
	}
	
	Rule<OffenseSegment> getRule205() {
		return new Rule<OffenseSegment>() {
			@Override
			public NIBRSError apply(OffenseSegment subject) {
				NIBRSError ret = null;
				String offenseCode = subject.getUcrOffenseCode();
				if (LocationTypeCode._58.code.equals(subject.getLocationType()) && 
						!OffenseCode.isCrimeAllowingLocationTypeCyberspace(offenseCode)) {
					ret = subject.getErrorTemplate();
					ret.setDataElementIdentifier("9");
					ret.setValue(offenseCode);
					ret.setNIBRSErrorCode(NIBRSErrorCode._205);
				}
				return ret;
			}
		};
	}
	
	Rule<OffenseSegment> getRule269() {
		return new Rule<OffenseSegment>() {
			@Override
			public NIBRSError apply(OffenseSegment subject) {
				NIBRSError ret = null;
				
				String offenseCode = subject.getUcrOffenseCode();
				if (OffenseCode._13B.code.equals(offenseCode)) {
					String[] ww = subject.getTypeOfWeaponForceInvolved();
					for (String w : ww) {
						if (firearmCodes.contains(w)) {
							ret = subject.getErrorTemplate();
							ret.setValue(w);
							ret.setDataElementIdentifier("13");
							ret.setNIBRSErrorCode(NIBRSErrorCode._269);
						}
					}
				}
				return ret;
			}
		};
	}
	
	Rule<OffenseSegment> getRule267() {
		final Set<String> homicideCodes = new HashSet<>();
		homicideCodes.add(OffenseCode._09A.code);
		homicideCodes.add(OffenseCode._09B.code);
		homicideCodes.add(OffenseCode._09C.code);
		return new Rule<OffenseSegment>() {
			@Override
			public NIBRSError apply(OffenseSegment subject) {
				NIBRSError ret = null;
				String offenseCode = subject.getUcrOffenseCode();
				if (homicideCodes.contains(offenseCode)) {
					String[] ww = subject.getTypeOfWeaponForceInvolved();
					for (String w : ww) {
						if (TypeOfWeaponForceCode._99.code.equals(w)) {
							ret = subject.getErrorTemplate();
							ret.setValue(w);
							ret.setDataElementIdentifier("13");
							ret.setNIBRSErrorCode(NIBRSErrorCode._267);
						}
					}
				}
				return ret;
			}
		};
	}
	
	Rule<OffenseSegment> getRule265() {
		final Set<String> simpleAssaultWeapons = new HashSet<>();
		simpleAssaultWeapons.add(TypeOfWeaponForceCode._40.code);
		simpleAssaultWeapons.add(TypeOfWeaponForceCode._90.code);
		simpleAssaultWeapons.add(TypeOfWeaponForceCode._95.code);
		simpleAssaultWeapons.add(TypeOfWeaponForceCode._99.code);
		return new Rule<OffenseSegment>() {
			@Override
			public NIBRSError apply(OffenseSegment subject) {
				NIBRSError ret = null;
				String offenseCode = subject.getUcrOffenseCode();
				if (offenseCode != null && OffenseCode._13B.code.equals(offenseCode)) {
					String[] ww = subject.getTypeOfWeaponForceInvolved();
					for (String w : ww) {
						if (w != null && !simpleAssaultWeapons.contains(w)) {
							ret = subject.getErrorTemplate();
							ret.setValue(w);
							ret.setDataElementIdentifier("13");
							ret.setNIBRSErrorCode(NIBRSErrorCode._265);
						}
					}
				}
				return ret;
			}
		};
	}
	
	Rule<OffenseSegment> getRule264() {
		return new Rule<OffenseSegment>() {
			@Override
			public NIBRSError apply(OffenseSegment subject) {
				NIBRSError ret = null;
				OffenseCode code = OffenseCode.forCode(subject.getUcrOffenseCode());
				if (code != null && !code.group.equals("A")) {
					ret = subject.getErrorTemplate();
					ret.setValue(code.code);
					ret.setDataElementIdentifier("6");
					ret.setNIBRSErrorCode(NIBRSErrorCode._264);
					ret.setWithinSegmentIdentifier(null);
				}
				return ret;
			}
		};
	}
	
	Rule<OffenseSegment> getRule258() {
		return new Rule<OffenseSegment>() {
			@Override
			public NIBRSError apply(OffenseSegment subject) {
				NIBRSError ret = null;
				String[] awi = subject.getAutomaticWeaponIndicator();
				for (int i=0;i < awi.length && ret == null;i++) {
					String weapon = subject.getTypeOfWeaponForceInvolved(i);
					if (AutomaticWeaponIndicatorCode.A.code.equals(awi[i]) && weapon != null && !firearmCodes.contains(weapon)) {
						ret = subject.getErrorTemplate();
						ret.setValue(weapon);
						ret.setDataElementIdentifier("13");
						ret.setNIBRSErrorCode(NIBRSErrorCode._258);
					}
				}
				return ret;
			}
		};
	}
	
	Rule<OffenseSegment> getRule257() {
		return new Rule<OffenseSegment>() {
			@Override
			public NIBRSError apply(OffenseSegment subject) {
				NIBRSError ret = null;
				String offenseCode = subject.getUcrOffenseCode();
				ParsedObject<Integer> numberOfPremises = subject.getNumberOfPremisesEntered();
				String locationType = subject.getLocationType();
				if (offenseCode != null && OffenseCode._220.code.equals(offenseCode) &&
						locationType != null && (LocationTypeCode._14.code.equals(locationType) || LocationTypeCode._19.code.equals(locationType)) &&
						(numberOfPremises.isMissing() || numberOfPremises.isInvalid())) {
					ret = subject.getErrorTemplate();
					ret.setValue(null);
					ret.setDataElementIdentifier("10");
					ret.setNIBRSErrorCode(NIBRSErrorCode._257);
				}
				return ret;
			}
		};
	}
	
	Rule<OffenseSegment> getRule256() {
		final Set<String> assaultOrHomicideCodeSet = new HashSet<>();
		assaultOrHomicideCodeSet.add(OffenseCode._09A.code);
		assaultOrHomicideCodeSet.add(OffenseCode._09B.code);
		assaultOrHomicideCodeSet.add(OffenseCode._09C.code);
		assaultOrHomicideCodeSet.add(OffenseCode._13A.code);
		assaultOrHomicideCodeSet.add(OffenseCode._13B.code);
		assaultOrHomicideCodeSet.add(OffenseCode._13C.code);
		assaultOrHomicideCodeSet.add(OffenseCode._360.code);
		return new Rule<OffenseSegment>() {
			@Override
			public NIBRSError apply(OffenseSegment subject) {
				NIBRSError ret = null;
				String offenseCode = subject.getUcrOffenseCode();
				String attemptedCompletedCode = subject.getOffenseAttemptedCompleted();
				if (attemptedCompletedCode != null && !OffenseAttemptedCompletedCode.C.code.equals(attemptedCompletedCode) && 
						offenseCode != null && assaultOrHomicideCodeSet.contains(offenseCode)) {
					ret = subject.getErrorTemplate();
					ret.setValue(attemptedCompletedCode);
					ret.setDataElementIdentifier("7");
					ret.setNIBRSErrorCode(NIBRSErrorCode._256);
				}
				return ret;
			}
		};
	}
	
	Rule<OffenseSegment> getRule255() {
		return new ValidValueListRule<>("automaticWeaponIndicator", "13", OffenseSegment.class, NIBRSErrorCode._255, AutomaticWeaponIndicatorCode.codeSet());
	}
	
	Rule<OffenseSegment> getRule254() {
		return new Rule<OffenseSegment>() {
			@Override
			public NIBRSError apply(OffenseSegment subject) {
				NIBRSError ret = null;
				String offenseCode = subject.getUcrOffenseCode();
				if (subject.getMethodOfEntry() != null && offenseCode != null && !OffenseCode._220.code.equals(offenseCode)) {
					ret = subject.getErrorTemplate();
					ret.setValue(subject.getMethodOfEntry());
					ret.setDataElementIdentifier("11");
					ret.setNIBRSErrorCode(NIBRSErrorCode._254);
				}
				return ret;
			}
		};
	}
	
	Rule<OffenseSegment> getRule253() {
		return new NotBlankRule<OffenseSegment>("methodOfEntry", "11", OffenseSegment.class, NIBRSErrorCode._253) {
			@Override
			public boolean ignore(OffenseSegment o) {
				return o != null && !OffenseCode._220.code.equals(o.getUcrOffenseCode());
			}
		};
	}
	
	Rule<OffenseSegment> getRule252() {
		return new Rule<OffenseSegment>() {
			@Override
			public NIBRSError apply(OffenseSegment subject) {
				NIBRSError ret = null;
				if (!(subject.getNumberOfPremisesEntered().isMissing() || subject.getNumberOfPremisesEntered().isInvalid())) {
					String offenseCode = subject.getUcrOffenseCode();
					String locationType = subject.getLocationType();
					if (!(OffenseCode._220.code.equals(offenseCode) && (LocationTypeCode._14.code.equals(locationType) || LocationTypeCode._19.code.equals(locationType)))) {
						ret = subject.getErrorTemplate();
						ret.setValue(OffenseCode._220.code);
						ret.setDataElementIdentifier("10");
						ret.setNIBRSErrorCode(NIBRSErrorCode._252);
						ret.setWithinSegmentIdentifier(null);
					}
				}
				return ret;
			}
		};
	}
	
	Rule<OffenseSegment> getRule221() {
		return new NotAllBlankRule<OffenseSegment>("typeOfWeaponForceInvolved", "13", OffenseSegment.class, NIBRSErrorCode._221) {
			@Override
			public boolean ignore(OffenseSegment o) {
				String offenseCode = o.getUcrOffenseCode();
				return offenseCode == null || !OffenseCode.isCrimeRequiringTypeOfWeaponForceInvolved(offenseCode);
			}
		};
	}
	
	Rule<OffenseSegment> getRule222() {
		return new Rule<OffenseSegment>() {
			@Override
			public NIBRSError apply(OffenseSegment subject) {
				NIBRSError ret = null;
				String offenseCode = subject.getUcrOffenseCode();
				if (ArrayUtils.notAllNull(subject.getTypeOfWeaponForceInvolved()) 
						&& !OffenseCode.isCrimeRequiringTypeOfWeaponForceInvolved(offenseCode)) {
					List<String> typeOfWeaponForceInvovled  = 
							Arrays.stream(subject.getTypeOfWeaponForceInvolved())
							.filter(Objects::nonNull)
							.collect(Collectors.toList());
					ret = subject.getErrorTemplate();
					ret.setValue(typeOfWeaponForceInvovled);
					ret.setDataElementIdentifier("13");
					ret.setNIBRSErrorCode(NIBRSErrorCode._222);
				}
				return ret;
			}
		};
	}
	

	Rule<OffenseSegment> getRule220() {
		return new NotAllBlankRule<OffenseSegment>("typeOfCriminalActivity", "12", OffenseSegment.class, NIBRSErrorCode._220) {
			@Override
			public boolean ignore(OffenseSegment o) {
				Set<String> applicableOffenses = new HashSet<>();
				applicableOffenses.addAll(Arrays.asList(new String[] {
					OffenseCode._250.code,
					OffenseCode._280.code,
					OffenseCode._30C.code,
					OffenseCode._35A.code,
					OffenseCode._35B.code,
					OffenseCode._39C.code,
					OffenseCode._49A.code,
					OffenseCode._370.code,
					OffenseCode._520.code, 
					OffenseCode._521.code, 
					OffenseCode._522.code, 
					OffenseCode._526.code, 
					OffenseCode._58A.code, 
					OffenseCode._58B.code, 
					OffenseCode._61A.code, 
					OffenseCode._61B.code, 
					OffenseCode._620.code, 
					OffenseCode._720.code
				}));
				String offenseCode = o.getUcrOffenseCode();
				return offenseCode == null || !applicableOffenses.contains(offenseCode);
			}
		};
	}
	
	Rule<OffenseSegment> getRule219() {
		return new Rule219();
	}
	
	Rule<OffenseSegment> getRule207(String propertyName, String dataElementIdentifier, Set<String> exclusiveValueSet) {
		return new ExclusiveCodedValueRule<>(propertyName, dataElementIdentifier, OffenseSegment.class, NIBRSErrorCode._207, exclusiveValueSet);
	}
	
	Rule<OffenseSegment> getRule206(String propertyName, String dataElementIdentifier) {
		return new DuplicateCodedValueRule<>(propertyName, dataElementIdentifier, OffenseSegment.class, NIBRSErrorCode._206);
	}
	
	Rule<OffenseSegment> getRule204ForUCROffenseCode() {
		return new ValidValueListRule<>("ucrOffenseCode", "6", OffenseSegment.class, NIBRSErrorCode._204, OffenseCode.codeSet(), false);
	}
	
	Rule<OffenseSegment> getRule201ForOffendersSuspectedOfUsing() {
		// weird that the 2.1 spec calls this a 201 error, not 204 like the rest of the "must be a valid value" ones, but there you go...
		return new ValidValueListRule<>("offendersSuspectedOfUsing", "8", OffenseSegment.class, NIBRSErrorCode._201, OffenderSuspectedOfUsingCode.codeSet(), false);
	}
	
	Rule<OffenseSegment> getRule201ForSingleStringProperty(String propertyName, String dataElementIdentifier) {
		return new NotBlankRule<>(propertyName, dataElementIdentifier, OffenseSegment.class, NIBRSErrorCode._201);
	}
	
	Rule<OffenseSegment> getRule201ForStringArrayProperty(String propertyName, String dataElementIdentifier) {
		return new NotAllBlankRule<>(propertyName, dataElementIdentifier, OffenseSegment.class, NIBRSErrorCode._201);
	}
	
	Rule<OffenseSegment> getRule204ForValueList(String propertyName, String dataElementIdentifier, Set<String> allowedValueSet) {
		return new ValidValueListRule<>(propertyName, dataElementIdentifier, OffenseSegment.class, NIBRSErrorCode._204, allowedValueSet);
	}
	
	Rule<OffenseSegment> getRule251() {
		return new ValidValueListRule<>("offenseAttemptedCompleted", "7", OffenseSegment.class, NIBRSErrorCode._251, OffenseAttemptedCompletedCode.codeSet(), true);
	}
	
	Rule<OffenseSegment> getRule204ForPremisesEntered() {
		Rule<OffenseSegment> ret = new NumericValueRule<>(
				subject -> {
					return subject.getNumberOfPremisesEntered().getValue();
				},
				(value, target) -> {
					NIBRSError e = null;
					if (value != null && (0 > value.intValue() || 99 < value.intValue())) {
						e = target.getErrorTemplate();
						e.setNIBRSErrorCode(NIBRSErrorCode._204);
						e.setDataElementIdentifier("10");
						e.setValue(value);
					}
					return e;
				});
		return ret;
	}
	
	/**
	 * Get the list of rules for the offense segment.
	 * @return the list of rules
	 */
	public List<Rule<OffenseSegment>> getRulesList() {
		return Collections.unmodifiableList(rulesList);
	}

}
