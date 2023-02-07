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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.CoreMatchers.equalTo;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.search.nibrs.common.NIBRSError;
import org.search.nibrs.common.ParsedObject;
import org.search.nibrs.common.ReportSource;
import org.search.nibrs.model.GroupAIncidentReport;
import org.search.nibrs.model.NIBRSAge;
import org.search.nibrs.model.OffenderSegment;
import org.search.nibrs.model.OffenseSegment;
import org.search.nibrs.model.VictimSegment;
import org.search.nibrs.model.codes.AdditionalJustifiableHomicideCircumstancesCode;
import org.search.nibrs.model.codes.AggravatedAssaultHomicideCircumstancesCode;
import org.search.nibrs.model.codes.EthnicityCode;
import org.search.nibrs.model.codes.NIBRSErrorCode;
import org.search.nibrs.model.codes.OffenseCode;
import org.search.nibrs.model.codes.OfficerAssignmentType;
import org.search.nibrs.model.codes.RaceCode;
import org.search.nibrs.model.codes.RelationshipOfVictimToOffenderCode;
import org.search.nibrs.model.codes.ResidentStatusCode;
import org.search.nibrs.model.codes.SexCode;
import org.search.nibrs.model.codes.TypeInjuryCode;
import org.search.nibrs.model.codes.TypeOfOfficerActivityCircumstance;
import org.search.nibrs.model.codes.TypeOfVictimCode;
import org.search.nibrs.validation.ValidatorProperties;
import org.search.nibrs.validation.rules.Rule;

public class VictimSegmentRulesFactoryTest {
	
	private VictimSegmentRulesFactory victimRulesFactory = VictimSegmentRulesFactory.instance(new ValidatorProperties());
	
	@Test
	public void testRule070() {
		
		Rule<VictimSegment> rule = victimRulesFactory.getRule070();
		VictimSegment victimSegment = getBasicVictimSegment();
		OffenderSegment offenderSegment = new OffenderSegment();
		GroupAIncidentReport parent = (GroupAIncidentReport) victimSegment.getParentReport();
		parent.addOffender(offenderSegment);
		offenderSegment.setOffenderSequenceNumber(new ParsedObject<>(1));
		victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(1));
		NIBRSError e = rule.apply(victimSegment);
		assertNull(e);
		victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(2));
		e = rule.apply(victimSegment);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._070, e.getNIBRSErrorCode());
		assertEquals("34", e.getDataElementIdentifier());
		assertEquals(2, e.getValue());
	}
	
	@Test
	public void testRule085() {
		
		Rule<VictimSegment> rule = victimRulesFactory.getRule085();
		VictimSegment victimSegment = getBasicVictimSegment();
		GroupAIncidentReport parent = (GroupAIncidentReport) victimSegment.getParentReport();
		parent.getOffenders().get(0).setOffenderSequenceNumber(new ParsedObject<>(1));
		victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(1));
		NIBRSError e = rule.apply(victimSegment);
		assertNull(e);
		
		OffenderSegment offenderSegment2 = new OffenderSegment();
		parent.addOffender(offenderSegment2);
		offenderSegment2.setOffenderSequenceNumber(new ParsedObject<>(2));
		
		victimSegment.setTypeOfVictim("I");
		e = rule.apply(victimSegment);
		assertNull(e);
		
		List<String> applicableOffenses = OffenseCode.codeSet()
				.stream()
				.filter(code->OffenseCode.isCrimeAgainstPersonCode(code) || OffenseCode.isCrimeAgainstPropertyCode(code))
				.collect(Collectors.toList()); 
		
		applicableOffenses.forEach(code->{
			victimSegment.setTypeOfVictim("I");
			victimSegment.setUcrOffenseCodeConnection(0, code);;
			assertEquals(2, parent.getOffenderCount());
			NIBRSError error = rule.apply(victimSegment);
			assertNotNull(error);
			assertEquals(NIBRSErrorCode._085, error.getNIBRSErrorCode());
			assertEquals("34", error.getDataElementIdentifier());
			assertNull(error.getValue());
			
			victimSegment.setTypeOfVictim("L");
			error = rule.apply(victimSegment);
			assertNotNull(error);
			assertEquals(NIBRSErrorCode._085, error.getNIBRSErrorCode());
			assertEquals("34", error.getDataElementIdentifier());
			assertNull(error.getValue());
			
			victimSegment.setTypeOfVictim(TypeOfVictimCode.B.code);
			error = rule.apply(victimSegment);
			assertNull(error);
		});
		
		OffenseCode.codeSet()
			.stream()
			.filter(code -> !OffenseCode.isCrimeAgainstPersonCode(code) && !OffenseCode.isCrimeAgainstPropertyCode(code))
			.forEach(code->{
				victimSegment.setTypeOfVictim("I");
				victimSegment.setUcrOffenseCodeConnection(0, code);;
				assertEquals(2, parent.getOffenderCount());
				NIBRSError error = rule.apply(victimSegment);
				assertNull(error);
				
				victimSegment.setTypeOfVictim("L");
				error = rule.apply(victimSegment);
				assertNull(error);
				
				victimSegment.setTypeOfVictim(TypeOfVictimCode.B.code);
				error = rule.apply(victimSegment);
				assertNull(error);
			});
	}
	
	@Test
	public void testRule401ForSequenceNumber(){
				
		Rule<VictimSegment> rule401 = victimRulesFactory.getRule401ForSequenceNumber();
		
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setVictimSequenceNumber(new ParsedObject<>(0));
		NIBRSError nibrsError = rule401.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._401, nibrsError.getNIBRSErrorCode());
		assertEquals("23", nibrsError.getDataElementIdentifier());
		assertEquals(0, nibrsError.getValue());
		
		victimSegment.setVictimSequenceNumber(new ParsedObject<>(1000));
		nibrsError = rule401.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._401, nibrsError.getNIBRSErrorCode());
		assertEquals(1000, nibrsError.getValue());
		
		victimSegment.setVictimSequenceNumber(ParsedObject.getMissingParsedObject());
		nibrsError = rule401.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._401, nibrsError.getNIBRSErrorCode());
		assertNull(nibrsError.getValue());
		
	}
	
	@Test
	public void testRule401ForVictimConnectedToUcrOffenseCode() {
		
		Rule<VictimSegment> rule401 = victimRulesFactory.getRule401ForVictimConnectedToUcrOffenseCode();
		
		VictimSegment victimSegment = getBasicVictimSegment();
		OffenseSegment offenseSegment = ((GroupAIncidentReport) victimSegment.getParentReport()).getOffenses().get(0);
		offenseSegment.setUcrOffenseCode(OffenseCode._09A.code);
		
		NIBRSError nibrsError = rule401.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._401, nibrsError.getNIBRSErrorCode());
		assertEquals("24", nibrsError.getDataElementIdentifier());
		assertNull(nibrsError.getValue());
		
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09B.code);
		nibrsError = rule401.apply(victimSegment);
		assertNull(nibrsError);

	}
	@Test
	public void testRule404ForVictimConnectedToUcrOffenseCode() {
		
		Rule<VictimSegment> rule404 = victimRulesFactory.getRule404ForVictimConnectedToUcrOffenseCode();
		
		VictimSegment victimSegment = getBasicVictimSegment();
		OffenseSegment offenseSegment = ((GroupAIncidentReport) victimSegment.getParentReport()).getOffenses().get(0);
		offenseSegment.setUcrOffenseCode(OffenseCode._09A.code);
		
		NIBRSError nibrsError = rule404.apply(victimSegment);
		assertNull(nibrsError);
		
		
		victimSegment.setUcrOffenseCodeConnection(0, "999");
		nibrsError = rule404.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._404, nibrsError.getNIBRSErrorCode());
		assertEquals("24", nibrsError.getDataElementIdentifier());
		
		List<String> compList = new ArrayList<>();
		compList.add("999");
		assertEquals(compList, nibrsError.getValue());
		
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09A.code);
		nibrsError = rule404.apply(victimSegment);
		assertNull(nibrsError);
		
	}
	
	@Test
	public void testRule401ForTypeOfVictim(){
		
		Rule<VictimSegment> rule401 = victimRulesFactory.getRule401ForTypeOfVictim();

		VictimSegment victimSegment = getBasicVictimSegment();
		
		// test null value
		victimSegment.setTypeOfVictim(null);
		
		NIBRSError nibrsError = rule401.apply(victimSegment);
		
		assertNotNull(nibrsError);
		
		assertEquals(NIBRSErrorCode._401, nibrsError.getNIBRSErrorCode());
		
		// test invalid code
		victimSegment.setTypeOfVictim("Z");
		
		nibrsError = rule401.apply(victimSegment);
		
		assertNull(nibrsError);
		
	}
	
	@Test
	public void testRule404ForTypeOfVictim(){
		
		Rule<VictimSegment> rule404 = victimRulesFactory.getRule404ForTypeOfVictim();
		
		VictimSegment victimSegment = getBasicVictimSegment();
		
		// test null value
		victimSegment.setTypeOfVictim(null);
		
		NIBRSError nibrsError = rule404.apply(victimSegment);
		
		assertNotNull(nibrsError);
		
		assertEquals(NIBRSErrorCode._404, nibrsError.getNIBRSErrorCode());
		
		// test invalid code
		victimSegment.setTypeOfVictim("Z");
		
		nibrsError = rule404.apply(victimSegment);
		
		assertNotNull(nibrsError);
		
		assertEquals(NIBRSErrorCode._404, nibrsError.getNIBRSErrorCode());
	}
	
	
	@Test
	public void testRule404ForAgeOfVictim(){
		Rule<VictimSegment> ageRule404 = victimRulesFactory.getRule404ForAgeOfVictim();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		NIBRSError nibrsError = ageRule404.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._404, nibrsError.getNIBRSErrorCode());
		assertEquals("26", nibrsError.getDataElementIdentifier());
		assertNull(nibrsError.getValue());
		victimSegment.setAge(null);
		nibrsError = ageRule404.apply(victimSegment);
		assertNotNull(nibrsError);
		victimSegment.setAge(NIBRSAge.getUnknownAge());
		victimSegment.setTypeOfVictim(TypeOfVictimCode.S.code);
		nibrsError = ageRule404.apply(victimSegment);
		assertNull(nibrsError);
	}

	@Test
	public void testRule410ForAgeOfVictim() {

		Rule<VictimSegment> rule = victimRulesFactory.getRule410ForAgeOfVictim();

		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setAge(NIBRSAge.getAge(30, 20));
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._410, nibrsError.getNIBRSErrorCode());
		assertEquals("26", nibrsError.getDataElementIdentifier());
		assertEquals(victimSegment.getAge(), nibrsError.getValue());

		victimSegment.setAge(NIBRSAge.getAge(20, 30));
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
	}

	@Test
	public void testRule450ForAgeOfVictim() {

		Rule<VictimSegment> rule = victimRulesFactory.getRule450ForAgeOfVictim__3_1();

		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setAge(NIBRSAge.getAge(9, 9));
		victimSegment.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.SE.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._450__3_1, nibrsError.getNIBRSErrorCode());
		assertEquals("26", nibrsError.getDataElementIdentifier());
		assertEquals(victimSegment.getAge(), nibrsError.getValue());

		victimSegment.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.AQ.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);

		victimSegment.setAge(NIBRSAge.getAge(13, 13));
		victimSegment.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.SE.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
	}

	@Test
	public void testRule453ForAgeOfVictim() {

		Rule<VictimSegment> rule = victimRulesFactory.getRule453ForAgeOfVictim();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		victimSegment.setAge(null);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._453, nibrsError.getNIBRSErrorCode());
		assertEquals("26", nibrsError.getDataElementIdentifier());
		assertNull(nibrsError.getValue());

		victimSegment.setAge(NIBRSAge.getAge(30, 33));
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
	}

	@Test
	public void testRule453ForSexOfVictim() {

		Rule<VictimSegment> rule = victimRulesFactory.getRule453ForSexOfVictim();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		victimSegment.setSex(null);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._453, nibrsError.getNIBRSErrorCode());
		assertEquals("27", nibrsError.getDataElementIdentifier());
		assertNull(nibrsError.getValue());
		
		victimSegment.setSex(SexCode.M.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}

	@Test
	public void testRule453ForRaceOfVictim() {

		Rule<VictimSegment> rule = victimRulesFactory.getRule453ForRaceOfVictim();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setTypeOfVictim(RaceCode.I.code);
		victimSegment.setRace(null);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._453, nibrsError.getNIBRSErrorCode());
		assertEquals("28", nibrsError.getDataElementIdentifier());
		assertNull(nibrsError.getValue());

		victimSegment.setTypeOfVictim(RaceCode.P.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);

	}

	@Test
	public void testRule404ForEthnicityOfVictim() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule404ForEthnicityOfVictim();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		victimSegment.setEthnicity(null);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setEthnicity("invalid");
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._404, nibrsError.getNIBRSErrorCode());
		assertEquals("29", nibrsError.getDataElementIdentifier());
		assertEquals("invalid", nibrsError.getValue());
		victimSegment.setEthnicity(EthnicityCode.N.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}
	
	@Test
	public void testRule404ForResidentStatus() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule404ForResidentStatusOfVictim();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		victimSegment.setResidentStatus(null);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setResidentStatus("invalid");
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._404, nibrsError.getNIBRSErrorCode());
		assertEquals("30", nibrsError.getDataElementIdentifier());
		assertEquals("invalid", nibrsError.getValue());
		victimSegment.setResidentStatus(ResidentStatusCode.N.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRule404ForAggravatedAssaultHomicideCircumstances() {

		Rule<VictimSegment> rule = victimRulesFactory.getRule404ForAggravatedAssaultHomicideCircumstances();

		VictimSegment victimSegment = getBasicVictimSegment();

		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._200.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);

		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09C.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
		victimSegment.setAggravatedAssaultHomicideCircumstances(0, "30");
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._404, nibrsError.getNIBRSErrorCode());
		assertEquals("31", nibrsError.getDataElementIdentifier());
		assertThat((List<String>)nibrsError.getValue(), equalTo(Arrays.asList("30")));
		
		victimSegment.setAggravatedAssaultHomicideCircumstances(0, "20");
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);

	}

	@Test
	public void testRule401ForAggravatedAssaultHomicideCircumstances() {
		
		Rule<VictimSegment> rule = victimRulesFactory.getRule401ForAggravatedAssaultHomicideCircumstances();
		
		VictimSegment victimSegment = getBasicVictimSegment();
		
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._200.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09C.code);
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._401, nibrsError.getNIBRSErrorCode());
		assertEquals("31", nibrsError.getDataElementIdentifier());
		
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09A.code);
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._401, nibrsError.getNIBRSErrorCode());
		assertEquals("31", nibrsError.getDataElementIdentifier());
		
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09B.code);
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._401, nibrsError.getNIBRSErrorCode());
		assertEquals("31", nibrsError.getDataElementIdentifier());
		
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._13A.code);
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._401, nibrsError.getNIBRSErrorCode());
		assertEquals("31", nibrsError.getDataElementIdentifier());
		
	}
	
	@Test
	public void testRule404OffenderNumberToBeRelated(){
		
		Rule<VictimSegment> offender404Rule = victimRulesFactory.getRule404OffenderNumberToBeRelated();
		
		VictimSegment victimSegment = getBasicVictimSegment();
		
		NIBRSError nibrsError = offender404Rule.apply(victimSegment);
		assertNull(nibrsError);
		
		OffenderSegment offenderSegment = ((GroupAIncidentReport) victimSegment.getParentReport()).getOffenders().get(0);
		offenderSegment.setOffenderSequenceNumber(new ParsedObject<>(1));
		
		victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(2));
		
		nibrsError = offender404Rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._404, nibrsError.getNIBRSErrorCode());
		List<Integer> compSet = new ArrayList<>();
		compSet.add(2);
		assertEquals(compSet, nibrsError.getValue());
		
		victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(1));
		nibrsError = offender404Rule.apply(victimSegment);
		assertNull(nibrsError);
		
		victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(0));
		nibrsError = offender404Rule.apply(victimSegment);
		assertNull(nibrsError);
		
	}
	
	@Test
	public void testRule404ForTypeOfficerActivityCircumstance() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule404ForTypeOfOfficerActivityCircumstance();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setTypeOfOfficerActivityCircumstance(null);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setTypeOfOfficerActivityCircumstance(TypeOfOfficerActivityCircumstance._01.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setTypeOfOfficerActivityCircumstance("invalid");
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._404, nibrsError.getNIBRSErrorCode());
		assertEquals("25A", nibrsError.getDataElementIdentifier());
		assertEquals("invalid", nibrsError.getValue());
	}

	@Test
	public void testRule454ForTypeOfOfficerActivityCircumstance() {

		Rule<VictimSegment> rule = victimRulesFactory.getRule454ForTypeOfOfficerActivityCircumstance();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setTypeOfOfficerActivityCircumstance(null);
		victimSegment.setTypeOfVictim(TypeOfVictimCode.L.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._454, nibrsError.getNIBRSErrorCode());
		assertEquals("25A", nibrsError.getDataElementIdentifier());

		victimSegment.setTypeOfOfficerActivityCircumstance(TypeOfOfficerActivityCircumstance._01.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
		victimSegment.setTypeOfOfficerActivityCircumstance(null);
		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);

	}

	@Test
	public void testRule454ForOfficerAssignmentType() {

		Rule<VictimSegment> rule = victimRulesFactory.getRule454ForOfficerAssignmentType();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setOfficerAssignmentType(null);
		victimSegment.setTypeOfVictim(TypeOfVictimCode.L.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(victimSegment);
		assertEquals(NIBRSErrorCode._454, nibrsError.getNIBRSErrorCode());
		assertEquals("25B", nibrsError.getDataElementIdentifier());
		assertNull(nibrsError.getValue());

		victimSegment.setOfficerAssignmentType(OfficerAssignmentType.F.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
		victimSegment.setOfficerAssignmentType(null);
		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);

	}

	@Test
	public void testRule454ForSexOfVictim() {

		Rule<VictimSegment> rule = victimRulesFactory.getRule454ForSexOfVictim();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setTypeOfVictim(TypeOfVictimCode.L.code);
		victimSegment.setSex(null);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._454, nibrsError.getNIBRSErrorCode());
		assertEquals("27", nibrsError.getDataElementIdentifier());

		victimSegment.setSex(SexCode.M.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
		victimSegment.setSex(null);
		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);

	}

	@Test
	public void testRule454ForRaceOfVictim() {

		Rule<VictimSegment> rule = victimRulesFactory.getRule454ForRaceOfVictim();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setTypeOfVictim(TypeOfVictimCode.L.code);
		victimSegment.setRace(null);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._454, nibrsError.getNIBRSErrorCode());
		assertEquals("28", nibrsError.getDataElementIdentifier());

		victimSegment.setRace(RaceCode.A.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
		victimSegment.setRace(null);
		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);

	}

	@Test
	public void testRule404ForOfficerAssignmentType() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule404ForOfficerAssignmentType();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setOfficerAssignmentType(null);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setOfficerAssignmentType(OfficerAssignmentType.F.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setOfficerAssignmentType("invalid");
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._404, nibrsError.getNIBRSErrorCode());
		assertEquals("25B", nibrsError.getDataElementIdentifier());
		assertEquals("invalid", nibrsError.getValue());
	}
	
	@Test
	public void testRule404ForOfficerOriOtherJurisdiction() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule404ForOfficerOriOtherJurisdiction();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setOfficerOtherJurisdictionORI(null);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setOfficerOtherJurisdictionORI("AB12345");
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}
	
	
	@Test
	public void testRule404ForSexOfVictim() {
		
		Rule<VictimSegment> sexOfVictim404Rule = victimRulesFactory.getRule404ForSexOfVictim();

		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		victimSegment.setSex(null);
		NIBRSError nibrsError = sexOfVictim404Rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._404, nibrsError.getNIBRSErrorCode());
		assertEquals("27", nibrsError.getDataElementIdentifier());
		assertNull(nibrsError.getValue());
		
		victimSegment.setSex("invalid");
		nibrsError = sexOfVictim404Rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals("invalid", nibrsError.getValue());
		
		victimSegment.setSex(SexCode.F.code);
		nibrsError = sexOfVictim404Rule.apply(victimSegment);
		assertNull(nibrsError);
		
		victimSegment = getBasicVictimSegment();
		victimSegment.setTypeOfVictim(TypeOfVictimCode.S.code);
		victimSegment.setSex(null);
		nibrsError = sexOfVictim404Rule.apply(victimSegment);
		assertNull(nibrsError);

	}

	@Test
	public void testRule404ForRaceOfVictim() {
		
		Rule<VictimSegment> raceOfVictim404Rule = victimRulesFactory.getRule404ForRaceOfVictim();
	
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		victimSegment.setRace(null);
		NIBRSError nibrsError = raceOfVictim404Rule.apply(victimSegment);
		assertNotNull(nibrsError);	
		assertEquals(NIBRSErrorCode._404, nibrsError.getNIBRSErrorCode());
		assertEquals("28", nibrsError.getDataElementIdentifier());
		assertEquals(null, nibrsError.getValue());
		
		victimSegment.setRace("invalid");
		nibrsError = raceOfVictim404Rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals("invalid", nibrsError.getValue());
		
		victimSegment.setRace(RaceCode.A.code);
		nibrsError = raceOfVictim404Rule.apply(victimSegment);
		assertNull(nibrsError);
		
	}
	
	@Test
	public void testRule404ForAdditionalJustifiableHomicideCircsumstances() {
		
		Rule<VictimSegment> rule = victimRulesFactory.getRule404ForAdditionalJustifiableHomicideCircsumstances();

		VictimSegment victimSegment = getBasicVictimSegment();

		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._200.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);

		victimSegment.setUcrOffenseCodeConnection(1, OffenseCode._09C.code);
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._404, nibrsError.getNIBRSErrorCode());
		assertEquals("32", nibrsError.getDataElementIdentifier());
		
		victimSegment.setAdditionalJustifiableHomicideCircumstances(AdditionalJustifiableHomicideCircumstancesCode.A.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);

	}

	@Test
	public void testRule455ForAdditionalJustifiableHomicideCircsumstances() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule455ForAdditionalJustifiableHomicideCircsumstances();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setAggravatedAssaultHomicideCircumstances(0, AggravatedAssaultHomicideCircumstancesCode._20.code);
		victimSegment.setAdditionalJustifiableHomicideCircumstances(null);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._455, nibrsError.getNIBRSErrorCode());
		assertEquals("32", nibrsError.getDataElementIdentifier());
		assertEquals(Arrays.asList(new String[] {AggravatedAssaultHomicideCircumstancesCode._20.code}), nibrsError.getValue());
		victimSegment.setAdditionalJustifiableHomicideCircumstances(AdditionalJustifiableHomicideCircumstancesCode.A.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}

	@Test
	public void testRule457ForAdditionalJustifiableHomicideCircsumstances() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule457ForAdditionalJustifiableHomicideCircsumstances();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setAggravatedAssaultHomicideCircumstances(0, AggravatedAssaultHomicideCircumstancesCode._10.code);
		victimSegment.setAdditionalJustifiableHomicideCircumstances(AdditionalJustifiableHomicideCircumstancesCode.A.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._457, nibrsError.getNIBRSErrorCode());
		assertEquals("32", nibrsError.getDataElementIdentifier());
		assertEquals(AdditionalJustifiableHomicideCircumstancesCode.A.code, nibrsError.getValue());
		victimSegment.setAggravatedAssaultHomicideCircumstances(0, AggravatedAssaultHomicideCircumstancesCode._20.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}
	
	@Test
	public void testRule419ForTypeOfInjury() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule419ForTypeOfInjury();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setTypeOfInjury(0, TypeInjuryCode.B.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._419, nibrsError.getNIBRSErrorCode());
		assertEquals("33", nibrsError.getDataElementIdentifier());
		assertEquals(Arrays.asList(new String[] {TypeInjuryCode.B.code}), nibrsError.getValue());
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._200.code);
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		victimSegment.setTypeOfInjury(0, null);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setTypeOfInjury(0, TypeInjuryCode.B.code);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._100.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}

	@Test
	public void testRule419ForAggravatedAssaultHomicideCircumstances() {

		Rule<VictimSegment> rule = victimRulesFactory.getRule419ForAggravatedAssaultHomicideCircumstances();

		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setAggravatedAssaultHomicideCircumstances(0, AggravatedAssaultHomicideCircumstancesCode._01.code);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._11A.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._419, nibrsError.getNIBRSErrorCode());
		assertEquals("31", nibrsError.getDataElementIdentifier());
		assertEquals(Arrays.asList(new String[] {AggravatedAssaultHomicideCircumstancesCode._01.code}), nibrsError.getValue());

		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09A.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
	}

	@Test
	public void testRule458ForTypeOfInjury() {

		Rule<VictimSegment> rule = victimRulesFactory.getRule458ForTypeOfInjury();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09A.code);
		victimSegment.setTypeOfVictim(TypeOfVictimCode.R.code);
		victimSegment.setTypeOfInjury(0, TypeInjuryCode.B.code);

		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._458, nibrsError.getNIBRSErrorCode());
		assertEquals("33", nibrsError.getDataElementIdentifier());

		victimSegment.setTypeOfInjury(0, null);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		victimSegment.setTypeOfInjury(0, TypeInjuryCode.B.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		victimSegment.setTypeOfInjury(0, TypeInjuryCode.B.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
	}

	@Test
	public void testRule458ForEthnicityOfVictim() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule458ForEthnicityOfVictim();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09A.code);
		victimSegment.setTypeOfVictim(TypeOfVictimCode.R.code);
		victimSegment.setEthnicity(EthnicityCode.H.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._458, nibrsError.getNIBRSErrorCode());
		assertEquals("29", nibrsError.getDataElementIdentifier());
		assertEquals(EthnicityCode.H.code, nibrsError.getValue());
		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._200.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09A.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}

	@Test
	public void testRule458ForResidentStatusOfVictim() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule458ForResidentStatusOfVictim();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09A.code);
		victimSegment.setTypeOfVictim(TypeOfVictimCode.R.code);
		victimSegment.setResidentStatus(ResidentStatusCode.N.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._458, nibrsError.getNIBRSErrorCode());
		assertEquals("30", nibrsError.getDataElementIdentifier());
		assertEquals(ResidentStatusCode.N.code, nibrsError.getValue());
		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._200.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09A.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}

	@Test
	public void testRule458ForOffenderNumberToBeRelated() {

		Rule<VictimSegment> rule = victimRulesFactory.getRule458ForOffenderNumberToBeRelated();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09A.code);
		victimSegment.setTypeOfVictim(TypeOfVictimCode.R.code);
		victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(1));

		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._458, nibrsError.getNIBRSErrorCode());
		assertEquals("34", nibrsError.getDataElementIdentifier());
		assertEquals(Arrays.asList(new Integer[] { 1 }), nibrsError.getValue());

		victimSegment.setOffenderNumberRelated(0, ParsedObject.getMissingParsedObject());
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);

		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(1));
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);

		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(1));
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._200.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);

	}

	@Test
	public void testRule458ForSexOfVictim() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule458ForSexOfVictim();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09A.code);
		victimSegment.setTypeOfVictim(TypeOfVictimCode.R.code);
		victimSegment.setSex(SexCode.F.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._458, nibrsError.getNIBRSErrorCode());
		assertEquals("27", nibrsError.getDataElementIdentifier());
		assertEquals(SexCode.F.code, nibrsError.getValue());
		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._200.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09A.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}

	@Test
	public void testRule458ForRaceOfVictim() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule458ForRaceOfVictim();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09A.code);
		victimSegment.setTypeOfVictim(TypeOfVictimCode.R.code);
		victimSegment.setRace(RaceCode.A.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._458, nibrsError.getNIBRSErrorCode());
		assertEquals("28", nibrsError.getDataElementIdentifier());
		assertEquals(RaceCode.A.code, nibrsError.getValue());
		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._200.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09A.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}

	@Test
	public void testRule458ForAgeOfVictim() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule458ForAgeOfVictim();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09A.code);		
		victimSegment.setTypeOfVictim(TypeOfVictimCode.R.code);
		victimSegment.setAge(NIBRSAge.getAge(25, null));
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._458, nibrsError.getNIBRSErrorCode());
		assertEquals("26", nibrsError.getDataElementIdentifier());
		assertEquals(victimSegment.getAge(), nibrsError.getValue());
		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._200.code);	
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09A.code);		
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}
	
	@Test
	public void testRule459ForOffenderNumberToBeRelated() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule459ForOffenderNumberToBeRelated();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(1));
		
		OffenseCode.codeSet()
			.stream()
			.filter(code->OffenseCode.isCrimeAgainstPersonCode(code) || OffenseCode.isCrimeAgainstPropertyCode(code))
			.forEach(code -> {
				victimSegment.setUcrOffenseCodeConnection(0, code);
				NIBRSError nibrsError = rule.apply(victimSegment);
				assertNull(nibrsError);
			});
		
		
		OffenseCode.codeSet()
		.stream()
		.filter(code->!OffenseCode.isCrimeAgainstPersonCode(code) && !OffenseCode.isCrimeAgainstPropertyCode(code))
		.forEach(code -> {
			victimSegment.setUcrOffenseCodeConnection(0, code);
			victimSegment.setOffenderNumberRelated(0, new ParsedObject<Integer>());
			NIBRSError nibrsError = rule.apply(victimSegment);
			assertNull(nibrsError);
			
			victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(1));
			nibrsError = rule.apply(victimSegment);
			assertNotNull(nibrsError);
			assertEquals(NIBRSErrorCode._459, nibrsError.getNIBRSErrorCode());
			assertEquals("34", nibrsError.getDataElementIdentifier());
			assertEquals(Arrays.asList(new Integer[] {1}), nibrsError.getValue());
		});
	}

	@Test
	public void testRule460() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule460ForRelationshipOfVictimToOffender();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(1));
		victimSegment.setVictimOffenderRelationship(0, null);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._460, nibrsError.getNIBRSErrorCode());
		assertEquals("35", nibrsError.getDataElementIdentifier());
		assertNull(nibrsError.getValue());
		victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(0));
		victimSegment.setVictimOffenderRelationship(0, null);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(1));
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		victimSegment.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.AQ.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}

	@Test
	public void testRule468() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule468ForRelationshipOfVictimToOffender();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(0));
		victimSegment.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.AQ.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._468, nibrsError.getNIBRSErrorCode());
		assertEquals("35", nibrsError.getDataElementIdentifier());
		assertEquals(RelationshipOfVictimToOffenderCode.AQ.code, nibrsError.getValue());
		victimSegment.setVictimOffenderRelationship(0, null);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}
	
	@Test
	public void testRule469() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule469ForSexOfVictim();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setSex(SexCode.U.code);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._11A.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._469, nibrsError.getNIBRSErrorCode());
		assertEquals("27", nibrsError.getDataElementIdentifier());
		assertEquals(SexCode.U.code, nibrsError.getValue());
		victimSegment.setSex(SexCode.M.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}

	@Test
	public void testRule481() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule481ForAgeOfVictim();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setAge(NIBRSAge.getAge(19, null));
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._36B.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._481, nibrsError.getNIBRSErrorCode());
		assertEquals("26", nibrsError.getDataElementIdentifier());
		assertEquals(victimSegment.getAge(), nibrsError.getValue());
		victimSegment.setAge(NIBRSAge.getAge(17, null));
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setAge(NIBRSAge.getAge(19, null));
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09A.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}
	
	@Test
	public void testRule482() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule482ForTypeOfVictim();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setTypeOfVictim(TypeOfVictimCode.L.code);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._120.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._482, nibrsError.getNIBRSErrorCode());
		assertEquals("25", nibrsError.getDataElementIdentifier());
		assertEquals(TypeOfVictimCode.L.code, nibrsError.getValue());
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._13A.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}

	@Test
	public void testRule483() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule483ForOfficerAssignmentType();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		victimSegment.setOfficerAssignmentType(OfficerAssignmentType.F.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._483, nibrsError.getNIBRSErrorCode());
		assertEquals("25B", nibrsError.getDataElementIdentifier());
		assertEquals(OfficerAssignmentType.F.code, nibrsError.getValue());
		victimSegment.setTypeOfVictim(TypeOfVictimCode.L.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}
	
	@Test
	public void testRule462() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule462();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._13A.code);
		victimSegment.setAggravatedAssaultHomicideCircumstances(0, AggravatedAssaultHomicideCircumstancesCode._07.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._462, nibrsError.getNIBRSErrorCode());
		assertEquals("31", nibrsError.getDataElementIdentifier());
		victimSegment.setUcrOffenseCodeConnection(1, OffenseCode._120.code);
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		victimSegment.setAggravatedAssaultHomicideCircumstances(1, AggravatedAssaultHomicideCircumstancesCode._01.code);
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._120.code);
		victimSegment.setUcrOffenseCodeConnection(1, null);
		victimSegment.setAggravatedAssaultHomicideCircumstances(1, null);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
		victimSegment = getBasicVictimSegment();
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._13A.code);
		victimSegment.setAggravatedAssaultHomicideCircumstances(0, AggravatedAssaultHomicideCircumstancesCode._10.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setAggravatedAssaultHomicideCircumstances(1, null);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
	}

	@Test
	public void testRule463() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule463();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09C.code);
		victimSegment.setAggravatedAssaultHomicideCircumstances(0, AggravatedAssaultHomicideCircumstancesCode._01.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._463, nibrsError.getNIBRSErrorCode());
		assertEquals("31", nibrsError.getDataElementIdentifier());
		victimSegment.setAggravatedAssaultHomicideCircumstances(0, AggravatedAssaultHomicideCircumstancesCode._20.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}

	@Test
	public void testRule461() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule461ForTypeOfVictim();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setTypeOfVictim(TypeOfVictimCode.S.code);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._220.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._461, nibrsError.getNIBRSErrorCode());
		assertEquals("25", nibrsError.getDataElementIdentifier());
		assertEquals(TypeOfVictimCode.S.code, nibrsError.getValue());
		victimSegment.setUcrOffenseCodeConnection(1, OffenseCode._120.code);
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._120.code);
		victimSegment.setUcrOffenseCodeConnection(1, null);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setTypeOfVictim(TypeOfVictimCode.B.code);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._220.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}

	@Test
	public void testRule464() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule464ForTypeOfVictim();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setTypeOfVictim(TypeOfVictimCode.S.code);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09A.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._464, nibrsError.getNIBRSErrorCode());
		assertEquals("25", nibrsError.getDataElementIdentifier());
		assertEquals(OffenseCode._09A.code, nibrsError.getValue());
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._120.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09A.code);
		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}

	@Test
	public void testRule467() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule467ForTypeOfVictim();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setTypeOfVictim(TypeOfVictimCode.S.code);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._510.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._467, nibrsError.getNIBRSErrorCode());
		assertEquals("25", nibrsError.getDataElementIdentifier());
		assertEquals(TypeOfVictimCode.S.code, nibrsError.getValue());
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09A.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._510.code);
		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}

	@Test
	public void testRule401ForTypeOfInjury() {

		Rule<VictimSegment> rule = victimRulesFactory.getRule401ForTypeOfInjury();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._200.code);

		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);

		VictimSegmentRulesFactory.INJURY_OFFENSE_LIST.forEach(code->{
			victimSegment.setTypeOfVictim(null);
			victimSegment.setTypeOfInjury(0, null);
			victimSegment.setUcrOffenseCodeConnection(0, code);
			NIBRSError error = rule.apply(victimSegment);
			assertNull(error);
			
			victimSegment.setTypeOfVictim(TypeOfVictimCode.L.code);
			error = rule.apply(victimSegment);
			if ("120".equals(code)) {
				assertNull(error);
			}
			else {
				assertNotNull(error);
				assertEquals(NIBRSErrorCode._401, error.getNIBRSErrorCode());
				assertEquals("33", error.getDataElementIdentifier());
			}
			
			victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
			error = rule.apply(victimSegment);
			assertNotNull(error);
			assertEquals(NIBRSErrorCode._401, error.getNIBRSErrorCode());
			assertEquals("33", error.getDataElementIdentifier());
			
			victimSegment.setTypeOfInjury(0, TypeInjuryCode.I.code);
			error = rule.apply(victimSegment);
			assertNull(nibrsError);
			
			victimSegment.setTypeOfInjury(0, "invalid");
			error = rule.apply(victimSegment);
			assertNull(nibrsError);
		}); 

	}

	@Test
	public void testRule401OffenderNumberToBeRelated() {
		
		
		Rule<VictimSegment> rule = victimRulesFactory.getRule401OffenderNumberToBeRelated();
		VictimSegment victimSegment = getBasicVictimSegment();
		
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._220.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
		victimSegment.setTypeOfVictim(TypeOfVictimCode.F.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
		victimSegment.setTypeOfVictim(TypeOfVictimCode.I.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._120.code);
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._401, nibrsError.getNIBRSErrorCode());
		assertEquals("34", nibrsError.getDataElementIdentifier());
		
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._11D.code);
		victimSegment.setTypeOfVictim(TypeOfVictimCode.L.code);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._401, nibrsError.getNIBRSErrorCode());
		assertEquals("34", nibrsError.getDataElementIdentifier());
		
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._520.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._11D.code);
		victimSegment.setOffenderNumberRelated(0, new ParsedObject<Integer>(1));;
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
	}
	
	@Test
	public void testRule404ForTypeOfInjury() {

		Rule<VictimSegment> rule = victimRulesFactory.getRule404ForTypeOfInjury();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._200.code);

		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);

		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._100.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
		victimSegment.setTypeOfInjury(0, "invalid");
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._404, nibrsError.getNIBRSErrorCode());
		assertEquals("33", nibrsError.getDataElementIdentifier());
		
		victimSegment.setTypeOfInjury(0,"N");
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
		victimSegment.setTypeOfVictim("I");
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
		victimSegment.setTypeOfInjury(0, null);
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._404, nibrsError.getNIBRSErrorCode());
		assertEquals("33", nibrsError.getDataElementIdentifier());

		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._100.code);
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._404, nibrsError.getNIBRSErrorCode());
		assertEquals("33", nibrsError.getDataElementIdentifier());
		
		victimSegment.setTypeOfVictim("L");
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._404, nibrsError.getNIBRSErrorCode());
		assertEquals("33", nibrsError.getDataElementIdentifier());
		
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._120.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
}

	@SuppressWarnings("unchecked")
	@Test
	public void testRule404ForRelationshipOfVictimToOffender(){
		
		Rule<VictimSegment> rule = victimRulesFactory.getRule404ForRelationshipOfVictimToOffender();
	
		VictimSegment victimSegment = getBasicVictimSegment();
		
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
		victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(0));
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
		victimSegment.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.AQ.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
		victimSegment.setVictimOffenderRelationship(0, "invalid");
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._404, nibrsError.getNIBRSErrorCode());
		assertEquals(Arrays.asList("invalid") , ((List<String>) nibrsError.getValue()));
		assertEquals("35", nibrsError.getDataElementIdentifier());
		
		OffenseCode.codeSet().stream()
			.filter(code->OffenseCode.isCrimeAgainstPersonCode(code) || OffenseCode.isCrimeAgainstPropertyCode(code))
			.forEach(code->{
				/*
				 * Not mandatory
				 */
				victimSegment.setUcrOffenseCodeConnection(0, null);
				victimSegment.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.AQ.code);
				NIBRSError error = rule.apply(victimSegment);
				assertNull(error);
				
				victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(0));
				error = rule.apply(victimSegment);
				assertNull(error);
				
				victimSegment.setVictimOffenderRelationship(0, "invalid");
				error = rule.apply(victimSegment);
				assertNotNull(error);
				assertEquals(NIBRSErrorCode._404, error.getNIBRSErrorCode());
				assertEquals(Arrays.asList("invalid") , ((List<String>) error.getValue()));
				assertEquals("35", error.getDataElementIdentifier());
				
				victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(1));
				error = rule.apply(victimSegment);
				assertNotNull(error);
				assertEquals(NIBRSErrorCode._404, error.getNIBRSErrorCode());
				assertEquals(Arrays.asList("invalid") , ((List<String>) error.getValue()));
				assertEquals("35", error.getDataElementIdentifier());
				
				victimSegment.setVictimOffenderRelationship(0, null);
				error = rule.apply(victimSegment);
				assertNull(error);
				
				/*
				 * mandatory
				 */
				victimSegment.setUcrOffenseCodeConnection(0, code);
				error = rule.apply(victimSegment);
				assertNotNull(error);
				assertEquals(NIBRSErrorCode._404, error.getNIBRSErrorCode());
				assertEquals(Arrays.asList("") , ((List<String>) error.getValue()));
				assertEquals("35", error.getDataElementIdentifier());
				
				victimSegment.setVictimOffenderRelationship(0, "invalid");
				error = rule.apply(victimSegment);
				assertNotNull(error);
				assertEquals(NIBRSErrorCode._404, error.getNIBRSErrorCode());
				assertEquals(Arrays.asList("invalid") , ((List<String>) error.getValue()));
				assertEquals("35", error.getDataElementIdentifier());
				
				RelationshipOfVictimToOffenderCode.codeSet().forEach(relationShip -> {
					victimSegment.setVictimOffenderRelationship(0, relationShip);
					NIBRSError error1 = rule.apply(victimSegment);
					assertNull(error1);
				});

			});
		
		OffenseCode.codeSet().stream()
		.filter(code->!OffenseCode.isCrimeAgainstPersonCode(code) && !OffenseCode.isCrimeAgainstPropertyCode(code))
		.forEach(code->{
			/*
			 * Not mandatory
			 */
			victimSegment.setUcrOffenseCodeConnection(0, code);
			victimSegment.setUcrOffenseCodeConnection(0, null);
			
			RelationshipOfVictimToOffenderCode.codeSet().forEach(relationShip -> {
				victimSegment.setVictimOffenderRelationship(0, relationShip);
				NIBRSError error1 = rule.apply(victimSegment);
				assertNull(error1);
			});


			victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(0));
			NIBRSError error = rule.apply(victimSegment);
			assertNull(error);
			
			victimSegment.setVictimOffenderRelationship(0, "invalid");
			error = rule.apply(victimSegment);
			assertNotNull(error);
			assertEquals(NIBRSErrorCode._404, error.getNIBRSErrorCode());
			assertEquals(Arrays.asList("invalid") , ((List<String>) error.getValue()));
			assertEquals("35", error.getDataElementIdentifier());
			
			victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(1));
			error = rule.apply(victimSegment);
			assertNotNull(error);
			assertEquals(NIBRSErrorCode._404, error.getNIBRSErrorCode());
			assertEquals(Arrays.asList("invalid") , ((List<String>) error.getValue()));
			assertEquals("35", error.getDataElementIdentifier());
			
			victimSegment.setVictimOffenderRelationship(0, null);
			error = rule.apply(victimSegment);
			assertNull(error);
			
		});
		
		
	}
	
	@Test
	public void testRule406ForVictimConnectedToUcrOffenseCode() {

		Rule<VictimSegment> offenseCode401Rule = victimRulesFactory.getRule406ForVictimConnectedToUcrOffenseCode();

		VictimSegment victimSegment = getBasicVictimSegment();

		victimSegment.setUcrOffenseCodeConnection(new String[] { OffenseCode._09A.code, OffenseCode._09A.code });
		NIBRSError nibrsError = offenseCode401Rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._406, nibrsError.getNIBRSErrorCode());

		victimSegment.setUcrOffenseCodeConnection(new String[] { OffenseCode._09A.code, OffenseCode._09B.code });
		nibrsError = offenseCode401Rule.apply(victimSegment);
		assertNull(nibrsError);

	}

	@Test
	public void testRule406ForAggravatedAssaultHomicideCircumstances() {

		Rule<VictimSegment> assault406Rule = victimRulesFactory.getRule406ForAggravatedAssaultHomicideCircumstances();

		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setAggravatedAssaultHomicideCircumstances(new String[] {
				AggravatedAssaultHomicideCircumstancesCode._01.code, AggravatedAssaultHomicideCircumstancesCode._01.code });

		NIBRSError nibrsError = assault406Rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._406, nibrsError.getNIBRSErrorCode());

		victimSegment.setAggravatedAssaultHomicideCircumstances(new String[] {
				AggravatedAssaultHomicideCircumstancesCode._01.code, AggravatedAssaultHomicideCircumstancesCode._02.code });

		nibrsError = assault406Rule.apply(victimSegment);
		assertNull(nibrsError);
	}

	@Test
	public void testRule406OffenderNumberToBeRelated() {

		Rule<VictimSegment> rule = victimRulesFactory.getRule406OffenderNumberToBeRelated();

		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(1));
		victimSegment.setOffenderNumberRelated(1, new ParsedObject<>(1));
		NIBRSError nibrsError = null;
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._406, nibrsError.getNIBRSErrorCode());
		assertEquals("34", nibrsError.getDataElementIdentifier());

		// demonstrates what happens if missing/invalid is improperly set on a ParsedObject
		victimSegment.setOffenderNumberRelated(1, new ParsedObject<>(1));
		victimSegment.getOffenderNumberRelated(1).setMissing(true);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		
		victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(1));
		victimSegment.setOffenderNumberRelated(1, new ParsedObject<>(2));
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);

	}
	
	@Test
	public void testRule406ForTypeOfInjury() {
	
		Rule<VictimSegment> rule = victimRulesFactory.getRule406ForTypeOfInjury();

		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setTypeOfInjury(new String[] {TypeInjuryCode.B.code, TypeInjuryCode.B.code});
		
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._406, nibrsError.getNIBRSErrorCode());
		assertEquals("33", nibrsError.getDataElementIdentifier());
		
		victimSegment.setTypeOfInjury(new String[] {TypeInjuryCode.B.code, TypeInjuryCode.I.code});
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}
	
	@Test
	public void testRule471() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule471();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(1));
		victimSegment.setOffenderNumberRelated(1, new ParsedObject<>(2));
		victimSegment.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.VO.code);
		victimSegment.setVictimOffenderRelationship(1, RelationshipOfVictimToOffenderCode.VO.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals("34", nibrsError.getDataElementIdentifier());
		assertEquals(RelationshipOfVictimToOffenderCode.VO.code, nibrsError.getValue());
		victimSegment.setOffenderNumberRelated(1, ParsedObject.getMissingParsedObject());
		victimSegment.setVictimOffenderRelationship(1, null);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}
	
	@Test
	public void testRule472() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule472();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(1));
		victimSegment.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.AQ.code);
		OffenderSegment os = ((GroupAIncidentReport) victimSegment.getParentReport()).getOffenders().get(0);
		os.setSex(null);
		os.setOffenderSequenceNumber(new ParsedObject<>(1));
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals("35", nibrsError.getDataElementIdentifier());
		assertEquals(NIBRSErrorCode._472, nibrsError.getNIBRSErrorCode());
		assertEquals(RelationshipOfVictimToOffenderCode.AQ.code, nibrsError.getValue());
		victimSegment.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.RU.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}
	
	@Test
	public void testRule475() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule475();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.SE.code);
		victimSegment.setVictimOffenderRelationship(1, RelationshipOfVictimToOffenderCode.SE.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals("34", nibrsError.getDataElementIdentifier());
		assertEquals(NIBRSErrorCode._475, nibrsError.getNIBRSErrorCode());
		assertEquals(RelationshipOfVictimToOffenderCode.SE.code, nibrsError.getValue());
		victimSegment.setVictimOffenderRelationship(1, RelationshipOfVictimToOffenderCode.AQ.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}
	
	@Test
	public void testRule477() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule477();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setAggravatedAssaultHomicideCircumstances(0, AggravatedAssaultHomicideCircumstancesCode._20.code);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09A.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals("31", nibrsError.getDataElementIdentifier());
		assertEquals(NIBRSErrorCode._477, nibrsError.getNIBRSErrorCode());
		assertEquals(Arrays.asList(new String[] {AggravatedAssaultHomicideCircumstancesCode._20.code}), nibrsError.getValue());
		victimSegment.setAggravatedAssaultHomicideCircumstances(0, AggravatedAssaultHomicideCircumstancesCode._01.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09B.code);
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		victimSegment.setAggravatedAssaultHomicideCircumstances(0, AggravatedAssaultHomicideCircumstancesCode._30.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setAggravatedAssaultHomicideCircumstances(1, AggravatedAssaultHomicideCircumstancesCode._31.code);
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		victimSegment.setAggravatedAssaultHomicideCircumstances(1, null);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09C.code);
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		victimSegment.setAggravatedAssaultHomicideCircumstances(0, AggravatedAssaultHomicideCircumstancesCode._20.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}
	
	@Test
	public void testRule478() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule478();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._09A.code);
		victimSegment.setUcrOffenseCodeConnection(1, OffenseCode._13A.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals("24", nibrsError.getDataElementIdentifier());
		assertEquals(NIBRSErrorCode._478, nibrsError.getNIBRSErrorCode());
		//assertEquals(Arrays.asList(new String[] {OffenseCode._09A.code, OffenseCode._13A.code}), nibrsError.getValue());
		assertNull(nibrsError.getValue());
		victimSegment.setUcrOffenseCodeConnection(1, OffenseCode._36A.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		// certainly could test all permutations, but...
	}
	
	@Test
	public void testRule479() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule479();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setTypeOfInjury(0, TypeInjuryCode.B.code);
		victimSegment.setUcrOffenseCodeConnection(0, OffenseCode._13B.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		
		assertNotNull(nibrsError);
		assertEquals("33", nibrsError.getDataElementIdentifier());
		assertEquals(NIBRSErrorCode._479, nibrsError.getNIBRSErrorCode());
		assertEquals(Arrays.asList(new String[] {TypeInjuryCode.B.code}), nibrsError.getValue());
		
		victimSegment.setUcrOffenseCodeConnection(1,  OffenseCode._13A.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setUcrOffenseCodeConnection(1,  null);
		victimSegment.setTypeOfInjury(0, TypeInjuryCode.M.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setTypeOfInjury(0, TypeInjuryCode.N.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setTypeOfInjury(0, null);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}
	
	@Test
	public void testRule456() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule456();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setAggravatedAssaultHomicideCircumstances(0, AggravatedAssaultHomicideCircumstancesCode._10.code);
		victimSegment.setAggravatedAssaultHomicideCircumstances(1, AggravatedAssaultHomicideCircumstancesCode._01.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		assertEquals("31", nibrsError.getDataElementIdentifier());
		assertEquals(NIBRSErrorCode._456, nibrsError.getNIBRSErrorCode());
		assertEquals(Arrays.asList(new String[] {AggravatedAssaultHomicideCircumstancesCode._10.code, AggravatedAssaultHomicideCircumstancesCode._01.code}), nibrsError.getValue());
		victimSegment.setAggravatedAssaultHomicideCircumstances(0, AggravatedAssaultHomicideCircumstancesCode._20.code);
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		victimSegment.setAggravatedAssaultHomicideCircumstances(0, AggravatedAssaultHomicideCircumstancesCode._02.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}
	
	@Test
	public void testRule407() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule407();
		VictimSegment victimSegment = getBasicVictimSegment();
		victimSegment.setTypeOfInjury(0, TypeInjuryCode.M.code);
		victimSegment.setTypeOfInjury(1, TypeInjuryCode.B.code);
		NIBRSError nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
		victimSegment.setTypeOfInjury(0, TypeInjuryCode.N.code);
		nibrsError = rule.apply(victimSegment);
		assertNotNull(nibrsError);
		victimSegment.setTypeOfInjury(0, TypeInjuryCode.I.code);
		nibrsError = rule.apply(victimSegment);
		assertNull(nibrsError);
	}
	
	@Test
	public void testRule476() {
		Rule<VictimSegment> rule = victimRulesFactory.getRule476();
		VictimSegment victimSegment1 = getBasicVictimSegment();
		NIBRSError nibrsError = rule.apply(victimSegment1);
		assertNull(nibrsError);
		GroupAIncidentReport incident = (GroupAIncidentReport) victimSegment1.getParentReport();
		OffenderSegment offenderSegment = new OffenderSegment();
		offenderSegment.setOffenderSequenceNumber(new ParsedObject<>(1));
		incident.addOffender(offenderSegment);
		offenderSegment = new OffenderSegment();
		offenderSegment.setOffenderSequenceNumber(new ParsedObject<>(2));
		incident.addOffender(offenderSegment);
		VictimSegment victimSegment2 = new VictimSegment();
		incident.addVictim(victimSegment2);
		victimSegment1.setOffenderNumberRelated(0, new ParsedObject<>(1));
		victimSegment1.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.SE.code);
		victimSegment2.setOffenderNumberRelated(0, new ParsedObject<>(1));
		victimSegment2.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.AQ.code);
		nibrsError = rule.apply(victimSegment1);
		assertNull(nibrsError);
		victimSegment2.setOffenderNumberRelated(0, new ParsedObject<>(2));
		victimSegment2.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.SE.code);
		nibrsError = rule.apply(victimSegment1);
		assertNull(nibrsError);
		victimSegment2.setOffenderNumberRelated(0, new ParsedObject<>(1));
		nibrsError = rule.apply(victimSegment1);
		assertNotNull(nibrsError);
		assertEquals("35", nibrsError.getDataElementIdentifier());
		assertEquals(NIBRSErrorCode._476, nibrsError.getNIBRSErrorCode());
	}
	
	private VictimSegment getBasicVictimSegment() {
		
		GroupAIncidentReport report = new GroupAIncidentReport();
		ReportSource source = new ReportSource();
		source.setSourceLocation(getClass().getName());
		source.setSourceName(getClass().getName());
		report.setSource(source);		
	
		VictimSegment victimSegment = new VictimSegment();
		report.addVictim(victimSegment);
		
		OffenseSegment offenseSegment = new OffenseSegment();
		report.addOffense(offenseSegment);

		OffenderSegment offenderSegment = new OffenderSegment();
		report.addOffender(offenderSegment);
		
		return victimSegment;
	}

}
