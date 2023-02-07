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
package org.search.nibrs.model.codes;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * Enum for NIBRS OffenseSegment Codes.
 *
 */
public enum OffenseCode {
	
	_720("720", "Animal Cruelty", "A", CrimeAgainstCode.Society),
	/**
	 * Arson
	 */
	_200("200", "Arson", "A", CrimeAgainstCode.Property),
	_13A("13A", "Aggravated Assault", "A", CrimeAgainstCode.Person),
	_13B("13B", "Simple Assault", "A", CrimeAgainstCode.Person),
	_13C("13C", "Intimidation", "A", CrimeAgainstCode.Person),
	_510("510", "Bribery", "A", CrimeAgainstCode.Property),
	_220("220", "Burglary/Breaking & Entering", "A", CrimeAgainstCode.Property),
	_58A("58A", "Import Violations*", "A", CrimeAgainstCode.Society),
	_58B("58B", "Export Violations*", "A", CrimeAgainstCode.Society),
	_61A("61A", "Federal Liquor Offenses*", "A", CrimeAgainstCode.Society),
	_61B("61B", "Federal Tobacco Offenses*", "A", CrimeAgainstCode.Society),
	_620("620", "Wildlife Trafficking*", "A", CrimeAgainstCode.Society),
	_250("250", "Counterfeiting/Forgery", "A", CrimeAgainstCode.Property),
	_290("290", "Destruction/Damage/Vandalism of Property", "A", CrimeAgainstCode.Property),
	_35A("35A", "Drug/Narcotic Violations", "A", CrimeAgainstCode.Society),
	_35B("35B", "Drug Equipment Violations", "A", CrimeAgainstCode.Society),
	_270("270", "Embezzlement", "A", CrimeAgainstCode.Property),
	_103("103", "Espionage*", "A", CrimeAgainstCode.Society),
	_210("210", "Extortion/Blackmail", "A", CrimeAgainstCode.Property),
	_26A("26A", "False Pretenses/Swindle/Confidence Game", "A", CrimeAgainstCode.Property),
	_26B("26B", "Credit Card/Automated Teller Machine Fraud", "A", CrimeAgainstCode.Property),
	_26C("26C", "Impersonation", "A", CrimeAgainstCode.Property),
	_26D("26D", "Welfare Fraud", "A", CrimeAgainstCode.Property),
	_26E("26E", "Wire Fraud", "A", CrimeAgainstCode.Property),
	_26F("26F", "Identity Theft", "A", CrimeAgainstCode.Property),
	_26G("26G", "Fraud Offenses-Hacking/Computer Invasion", "A", CrimeAgainstCode.Property),
	_26H("26H", "Money Laundering*", "A", CrimeAgainstCode.Property),
	_39A("39A", "Betting/Wagering", "A", CrimeAgainstCode.Society),
	_39B("39B", "Operating/Promoting/Assisting Gambling", "A", CrimeAgainstCode.Society),
	_39C("39C", "Gambling Equipment Violations", "A", CrimeAgainstCode.Society),
	_39D("39D", "Sports Tampering", "A", CrimeAgainstCode.Society),
	_09A("09A", "Murder & Non-negligent Manslaughter", "A", CrimeAgainstCode.Person),
	_09B("09B", "Negligent Manslaughter", "A", CrimeAgainstCode.Person),
	/**
	 * Justifiable Homicide
	 */
	_09C("09C", "Justifiable Homicide", "A", CrimeAgainstCode.Person),
	_30A("30A", "Illegal Entry into the United States*", "A", CrimeAgainstCode.Society),
	_30B("30B", "False Citizenship*", "A", CrimeAgainstCode.Society),
	_30C("30C", "Smuggling Aliens*", "A", CrimeAgainstCode.Society),
	_30D("30D", "Re-entry after Deportation*", "A", CrimeAgainstCode.Society),
	_64A("64A", "Human Trafficking, Commercial Sex Acts", "A", CrimeAgainstCode.Person),
	_64B("64B", "Human Trafficking, Involuntary Servitude", "A", CrimeAgainstCode.Person),
	_100("100", "Kidnapping/Abduction Person", "A", CrimeAgainstCode.Person),
	_49A("49A", "Harboring Escapee/ Concealing from Arrest*", "A", CrimeAgainstCode.Society),
	_49B("49B", "Flight to Avoid Prosecution*", "A", CrimeAgainstCode.Society),
	_49C("49C", "Flight to Avoid Deportation*", "A", CrimeAgainstCode.Society),
	_23A("23A", "Pocket-picking", "A", CrimeAgainstCode.Property),
	_23B("23B", "Purse-snatching ", "A", CrimeAgainstCode.Property),
	_23C("23C", "Shoplifting ", "A", CrimeAgainstCode.Property),
	_23D("23D", "Theft From Building", "A", CrimeAgainstCode.Property),
	_23E("23E", "Theft From Coin-Operated Machine or Device ", "A", CrimeAgainstCode.Property),
	_23F("23F", "Theft From Motor Vehicle", "A", CrimeAgainstCode.Property),
	_23G("23G", "Theft of Motor Vehicle Parts or Accessories ", "A", CrimeAgainstCode.Property),
	_23H("23H", "All Other Larceny", "A", CrimeAgainstCode.Property),
	_240("240", "Motor Vehicle Theft", "A", CrimeAgainstCode.Property),
	_370("370", "Pornography/Obscene Material ", "A", CrimeAgainstCode.Society),
	_40A("40A", "Prostitution ", "A", CrimeAgainstCode.Society),
	_40B("40B", "Assisting or Promoting Prostitution ", "A", CrimeAgainstCode.Society),
	_40C("40C", "Purchasing Prostitution ", "A", CrimeAgainstCode.Society),
	_120("120", "Robbery ", "A", CrimeAgainstCode.Property),
	_11A("11A", "Rape ", "A", CrimeAgainstCode.Person),
	_11B("11B", "Sodomy ", "A", CrimeAgainstCode.Person),
	_11C("11C", "Sexual Assault With An Object", "A", CrimeAgainstCode.Person),
	_11D("11D", "Fondling ", "A", CrimeAgainstCode.Person),
	_36A("36A", "Incest ", "A", CrimeAgainstCode.Person),
	_36B("36B", "Statutory Rape ", "A", CrimeAgainstCode.Person),
	_280("280", "Stolen PropertySegment Offenses ", "A", CrimeAgainstCode.Property),
	_101("101", "Treason*", "A", CrimeAgainstCode.Society),
	_520("520", "Weapon Law Violations", "A", CrimeAgainstCode.Society),
	_521("521", "Violation of National Firearm Act of 1934*", "A", CrimeAgainstCode.Society),
	_522("522", "Weapons of Mass Destruction*", "A", CrimeAgainstCode.Society),
	_526("526", "Weapons of Mass Destruction*", "A", CrimeAgainstCode.Society),
	_90A("90A", "Bad Checks", "B", CrimeAgainstCode.Property),
	_90B("90B", "Curfew/Loitering/Vagrancy Violations", "B", CrimeAgainstCode.Society),
	_90C("90C", "Disorderly Conduct", "B", CrimeAgainstCode.Society),
	_90D("90D", "Driving Under the Influence", "B", CrimeAgainstCode.Society),
	_90E("90E", "Drunkenness", "B", CrimeAgainstCode.Society),
	_90F("90F", "Family Offenses, Nonviolent", "B", CrimeAgainstCode.Society),
	_90G("90G", "Liquor Law Violations", "B", CrimeAgainstCode.Society),
	_90H("90H", "Peeping Tom", "B", CrimeAgainstCode.Society),
	_90I("90I", "Runaway", "B", CrimeAgainstCode.Society), // No longer in the 2019 spec. 
	_90J("90J", "Trespass of Real Property", "B", CrimeAgainstCode.Society),
	_90K("90K", "Failure to Appear*", "B", CrimeAgainstCode.Society),
	_90L("90L", "Federal Resource Violations*", "B", CrimeAgainstCode.Society),
	_90M("90M", "Perjury*", "B", CrimeAgainstCode.Society),
	_90Z("90Z", "All Other Offenses", "B", CrimeAgainstCode.PersonPropertyOrSociety)
	;

	public String code;
	public String description;
	public String group;
	private CrimeAgainstCode crimeAgainst;
	
	private static final Map<String,OffenseCode> ENUM_MAP;
	private static final Set<String> CODE_SET;  
	
	static {
		Map<String,OffenseCode> map = new HashMap<String, OffenseCode>();
		for (OffenseCode instance : OffenseCode.values()) {
		    map.put(instance.code.toLowerCase(),instance);
		}
		ENUM_MAP = Collections.unmodifiableMap(map);
		
		CODE_SET = Arrays.stream(values()).map(item->item.code)
				.collect(Collectors.toSet()); 
	}

	private OffenseCode(String code, String description, String group
			, CrimeAgainstCode crimeAgainstCode) {
		this.code = code;
		this.description = description;
		this.group = group;
		this.crimeAgainst = crimeAgainstCode;
	}
	
	public static final Set<OffenseCode> asSet() {
		return EnumSet.allOf(OffenseCode.class);
	}

	public static final Set<String> codeSet() {
		return CODE_SET;
	}
	
	public static final OffenseCode forCode(String code) {
		return ENUM_MAP.get(StringUtils.lowerCase(code));
	}
	
	public static final boolean isCrimeAgainstPersonCode(String code) {
		return Arrays.asList(_13A.code, _13B.code, _13C.code, 
		_09A.code, _09B.code, _09C.code, _64A.code, _64B.code, _100.code,
		_11A.code, _11B.code, _11C.code, _11D.code, _36A.code,
		_36B.code).contains(code);
	}
	
	public static final boolean containsCrimeAgainstPersonCode(Collection<String> codes) {
		return codes.stream().anyMatch(code -> isCrimeAgainstPersonCode(code));
	}

	public static final boolean isCrimeAgainstSocietyCode(String code) {
		return Arrays.asList(_720.code,
		_35A.code, _35B.code, _39A.code, _39B.code,
		_39C.code, _39D.code, _370.code, _40A.code,
		_40B.code, _40C.code, _520.code, _90A.code, 
		_90B.code, _90C.code, _90D.code, _90E.code,
		_90F.code, _90G.code, _90H.code, _90J.code,
		_90Z.code).contains(code);
	}
	
	public static final boolean isCommerceViolations(String code) {
		return Arrays.asList(
				_58A.code, _58B.code, _61A.code, _61B.code,
				_620.code).contains(code);
	}
	
	public static final boolean containsCrimeAgainstSocietyCode(Collection<String> codes) {
		return codes.stream().anyMatch(code -> isCrimeAgainstSocietyCode(code));
	}

	public static final boolean isCrimeAgainstPropertyCode(String code) {
		return Arrays.asList(_200.code, _26H.code, 
				_510.code, _220.code, _250.code,
				_290.code, _270.code, _210.code,
				_26A.code, _26B.code, _26C.code,
				_26D.code, _26E.code, _26F.code,
				_26G.code, _23A.code, _23B.code,
				_23C.code, _23D.code, _23E.code,
				_23F.code, _23G.code, _23H.code,
				_240.code, _120.code, _280.code					
		).contains(code);
	}
	
	public static final boolean isCrimeAgainstStolenVehiclePropertyCode(String code) {
		return Arrays.asList(
				_510.code, _220.code, 
				_270.code, _210.code,
				_26A.code, _26B.code, _26C.code,
				_26D.code, _26E.code, _26F.code,
				_26G.code, _23A.code, _23B.code,
				_23C.code, _23D.code, _23E.code,
				_23F.code, _23G.code, _23H.code,
				_240.code, _120.code					
				).contains(code);
	}
	
	public static final boolean isCrimeAllowingLocationTypeCyberspace(String code) {
		return Arrays.asList(
				_210.code, _250.code, _270.code, 
				_280.code, _290.code, _370.code,  
				_510.code, _26A.code, _26B.code,  
				_26C.code, _26D.code, _26E.code, 
				_26F.code, _26G.code, _39A.code, 
				_39B.code, _39C.code, _13C.code, 
				_35A.code, _35B.code, _520.code, 
				_64A.code, _64B.code, _40A.code,  
				_40B.code, _40C.code					
				).contains(code);
	}
	
	public static final boolean isDrugNarcoticOffense(String code) {
		return Arrays.asList(_35A.code, _35B.code).contains(code);
	}
	
	public static final boolean isAggravatedAssaultHomicideCircumstancesOffense(String code) {
		return Arrays.asList(_09A.code, _09B.code, _09C.code, _13A.code).contains(code);
	}
	
	public static final boolean containsCrimeAgainstPropertyCode(Collection<String> codes) {
		return codes.stream().anyMatch(code -> isCrimeAgainstPropertyCode(code));
	}

	public static final boolean containsCrimeRequirePropertySegement(Collection<String> codes) {
		return codes.stream().anyMatch(OffenseCode::isCrimeRequirePropertySegement);
	}
	
	public static final boolean isCrimeRequirePropertySegement(String code) {
		return isCrimeAgainstPropertyCode(code)
				|| _100.code.equals(code)
				|| isGamblingOffenseCode(code)
				|| isDrugNarcoticOffense(code);
	}
	
	public static final boolean isCrimeRequireIncidentHour(String code) {
		return Arrays.asList(_09A.code, _13A.code, _13B.code, _13C.code).contains(code);
	}
	
	public static final boolean isCrimeRequiringTypeOfWeaponForceInvolved(String code) {
		return Arrays.asList(
				_09A.code,
				_09B.code,
				_09C.code,
				_100.code,
				_11A.code,
				_11B.code,
				_11C.code,
				_11D.code,
				_120.code,
				_13A.code,
				_13B.code,
				_210.code,
				_520.code,
				_64A.code,
				_64B.code).contains(code);
	}
	
	public static final boolean isGamblingOffenseCode(String code) {
		return codeMatchesRegex(code, "39[ABCD]");
	}
	public static final boolean isReturnARapeCode(String code) {
		return codeMatchesRegex(code, "11[ABC]");
	}

	private static boolean codeMatchesRegex(String code, String regex) {
		return code!= null && Pattern.compile(regex).matcher(code).matches();
	}
	
	public static final boolean containsGamblingOffenseCode(Collection<String> codes) {
		return codes.stream().anyMatch(code -> isGamblingOffenseCode(code));
	}

	public static final boolean isLarcenyOffenseCode(String code) {
		return codeMatchesRegex(code, "23[ABCDEFGH]");
	}
	
	public static final boolean containsLarcenyOffenseCode(Collection<String> codes) {
		return codes.stream().anyMatch(code -> isLarcenyOffenseCode(code));
	}
	
	public static final boolean isOffenseHavingIllogicalPropertyDescriptions(String code){
		return Arrays.asList(
				_220.code, _240.code, _23A.code, _23B.code,
				_23C.code, _23D.code, _23E.code,
				_23F.code, _23G.code, _23H.code
		).contains(code);

	}

	public CrimeAgainstCode getCrimeAgainst() {
		return crimeAgainst;
	}

	public void setCrimeAgainst(CrimeAgainstCode crimeAgainst) {
		this.crimeAgainst = crimeAgainst;
	}
	
}