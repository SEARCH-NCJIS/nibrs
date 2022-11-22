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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.search.nibrs.common.NIBRSError;
import org.search.nibrs.common.ParsedObject;
import org.search.nibrs.common.ReportSource;
import org.search.nibrs.model.AbstractReport;
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
import org.search.nibrs.validation.rules.Rule;

public class OffenseSegmentRulesFactoryTest {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(OffenseSegmentRulesFactoryTest.class);
	
	private OffenseSegmentRulesFactory rulesFactory = new OffenseSegmentRulesFactory();
	
	@Test
	public void testRule065() {
		Rule<OffenseSegment> rule = rulesFactory.getRule065();
		OffenseSegment os1 = buildBaseSegment();
		os1.setUcrOffenseCode(OffenseCode._13A.code);
		GroupAIncidentReport incident = (GroupAIncidentReport) os1.getParentReport();
		incident.removeVictims();
		VictimSegment vs1 = new VictimSegment();
		incident.addVictim(vs1);
		VictimSegment vs2 = new VictimSegment();
		incident.addVictim(vs2);
		OffenseSegment os2 = new OffenseSegment();
		os2.setUcrOffenseCode(OffenseCode._13B.code);
		incident.addOffense(os2);
		vs1.setUcrOffenseCodeConnection(0, OffenseCode._13A.code);
		vs2.setUcrOffenseCodeConnection(0, OffenseCode._13A.code);
		NIBRSError e = rule.apply(os1);
		assertNull(e);
		e = rule.apply(os2);
		assertNotNull(e);
	}
	
	@Test
	public void testRule205() {
		Rule<OffenseSegment> rule = rulesFactory.getRule205();
		OffenseSegment o = buildBaseSegment();
		o.setUcrOffenseCode(null);
		o.setLocationType(LocationTypeCode._58.code);
		
		NIBRSError e = rule.apply(o);
		assertNotNull(e);
		assertEquals('2', e.getSegmentType());
		assertEquals("9", e.getDataElementIdentifier());
		assertNull(e.getValue());
		assertEquals(NIBRSErrorCode._205, e.getNIBRSErrorCode());

		List<OffenseCode> offenseCodeList = 
				Arrays.asList(
					OffenseCode._210, OffenseCode._250, OffenseCode._270,
					OffenseCode._280, OffenseCode._290, OffenseCode._370,
					OffenseCode._510, OffenseCode._26A, OffenseCode._26B,
					OffenseCode._26C, OffenseCode._26D, OffenseCode._26E,
					OffenseCode._26F, OffenseCode._26G, OffenseCode._39A,
					OffenseCode._39B, OffenseCode._39C, OffenseCode._13C,
					OffenseCode._35A, OffenseCode._35B, OffenseCode._520,
					OffenseCode._64A, OffenseCode._64B, OffenseCode._40A,
					OffenseCode._40B, OffenseCode._40C
				);
		
		for (OffenseCode offenseCode : OffenseCode.values()){
			o.setUcrOffenseCode(offenseCode.code);
			e = rule.apply(o);
			if (offenseCodeList.contains(offenseCode)){
				assertNull(e);
			}
			else{
				assertNotNull(e);
				assertEquals('2', e.getSegmentType());
				assertEquals("9", e.getDataElementIdentifier());
				assertEquals(offenseCode.code, e.getValue());
				assertEquals(NIBRSErrorCode._205, e.getNIBRSErrorCode());
			}
			
		}
		
		o.setLocationType(LocationTypeCode._02.code);

		for (OffenseCode offenseCode : OffenseCode.values()){
			o.setUcrOffenseCode(offenseCode.code);
			e = rule.apply(o);
			assertNull(e);
		}
		
	}
	
	@Test
	public void testRule250() {
		Rule<OffenseSegment> rule = rulesFactory.getRule250();
		OffenseSegment o = buildBaseSegment();
		o.setUcrOffenseCode(null);
		NIBRSError e = rule.apply(o);
		assertNull(e);
		
		List<OffenseCode> offenseCodeList = 
				Arrays.asList(
						OffenseCode._26H, OffenseCode._101, OffenseCode._103, OffenseCode._30A,
						OffenseCode._30B, OffenseCode._30C, OffenseCode._30D,
						OffenseCode._360, OffenseCode._49A, OffenseCode._49B, OffenseCode._49C,
						OffenseCode._521, OffenseCode._522, OffenseCode._526,
						OffenseCode._58A, OffenseCode._58B, OffenseCode._61A,
						OffenseCode._61B, OffenseCode._620, OffenseCode._90K,
						OffenseCode._90L, OffenseCode._90M
						);		
		for (OffenseCode offenseCode : OffenseCode.values()){
			o.setUcrOffenseCode(offenseCode.code);
			e = rule.apply(o);
			if (offenseCodeList.contains(offenseCode)){
				assertNotNull(e);
				assertEquals('2', e.getSegmentType());
				assertEquals("6", e.getDataElementIdentifier());
				assertEquals(offenseCode.code, e.getValue());
				assertEquals(NIBRSErrorCode._250, e.getNIBRSErrorCode());			}
			else{
				assertNull(e);
			}
			
		}
		
		o.getParentReport().setFederalJucicialDistrictCode("002");
		
		for (OffenseCode offenseCode : OffenseCode.values()){
			o.setUcrOffenseCode(offenseCode.code);
			e = rule.apply(o);
			assertNull(e);
		}
		
	}
	
	@Test
	public void testRule270() {
		Rule<OffenseSegment> rule = rulesFactory.getRule270();
		OffenseSegment o = buildBaseSegment();
		o.setUcrOffenseCode(null);
		o.setBiasMotivation(0, null);
		o.setBiasMotivation(1, null);
		o.setBiasMotivation(2, null);
		NIBRSError e = rule.apply(o);
		assertNull(e);
		o.setUcrOffenseCode(OffenseCode._09C.code);
		e = rule.apply(o);
		assertNull(e);
		o.setBiasMotivation(0, BiasMotivationCode._88.code);
		e = rule.apply(o);
		assertNull(e);
		o.setBiasMotivation(0, BiasMotivationCode._11.code);
		e = rule.apply(o);
		assertNotNull(e);
		assertEquals('2', e.getSegmentType());
		assertEquals("8A", e.getDataElementIdentifier());
		assertEquals(BiasMotivationCode._11.code, e.getValue());
		o.setBiasMotivation(1, BiasMotivationCode._88.code);
		e = rule.apply(o);
		assertNotNull(e);
	}
	
	@Test
	public void testRule269() {
		Rule<OffenseSegment> rule = rulesFactory.getRule269();
		OffenseSegment o = buildBaseSegment();
		o.setUcrOffenseCode(null);
		o.setTypeOfWeaponForceInvolved(0, null);
		o.setTypeOfWeaponForceInvolved(1, null);
		o.setTypeOfWeaponForceInvolved(2, null);
		NIBRSError e = rule.apply(o);
		assertNull(e);
		o.setUcrOffenseCode(OffenseCode._13B.code);
		e = rule.apply(o);
		assertNull(e);
		o.setTypeOfWeaponForceInvolved(0, TypeOfWeaponForceCode._11.code);
		e = rule.apply(o);
		assertNotNull(e);
		assertEquals('2', e.getSegmentType());
		assertEquals("13", e.getDataElementIdentifier());
		assertEquals(TypeOfWeaponForceCode._11.code, e.getValue());
	}
	
	@Test
	public void testRule267() {
		Rule<OffenseSegment> rule = rulesFactory.getRule267();
		OffenseSegment o = buildBaseSegment();
		o.setUcrOffenseCode(null);
		o.setTypeOfWeaponForceInvolved(0, null);
		o.setTypeOfWeaponForceInvolved(1, null);
		o.setTypeOfWeaponForceInvolved(2, null);
		NIBRSError e = rule.apply(o);
		assertNull(e);
		o.setUcrOffenseCode(OffenseCode._09A.code);
		o.setTypeOfWeaponForceInvolved(0, TypeOfWeaponForceCode._11.code);
		e = rule.apply(o);
		assertNull(e);
		o.setUcrOffenseCode(OffenseCode._220.code);
		e = rule.apply(o);
		assertNull(e);
		o.setUcrOffenseCode(OffenseCode._09A.code);
		o.setTypeOfWeaponForceInvolved(0, TypeOfWeaponForceCode._99.code);
		e = rule.apply(o);
		assertNotNull(e);
		assertEquals('2', e.getSegmentType());
		assertEquals("13", e.getDataElementIdentifier());
		assertEquals(TypeOfWeaponForceCode._99.code, e.getValue());
	}
	
	@Test
	public void testRule265() {
		Rule<OffenseSegment> rule = rulesFactory.getRule265();
		OffenseSegment o = buildBaseSegment();
		o.setUcrOffenseCode(null);
		o.setTypeOfWeaponForceInvolved(0, null);
		o.setTypeOfWeaponForceInvolved(1, null);
		o.setTypeOfWeaponForceInvolved(2, null);
		NIBRSError e = rule.apply(o);
		assertNull(e);
		o.setUcrOffenseCode(OffenseCode._13A.code);
		e = rule.apply(o);
		assertNull(e);
		o.setUcrOffenseCode(OffenseCode._13A.code);
		o.setTypeOfWeaponForceInvolved(0, TypeOfWeaponForceCode._11.code);
		e = rule.apply(o);
		assertNull(e);
		o.setUcrOffenseCode(OffenseCode._13B.code);
		o.setTypeOfWeaponForceInvolved(0, TypeOfWeaponForceCode._40.code);
		e = rule.apply(o);
		assertNull(e);
		o.setTypeOfWeaponForceInvolved(0, TypeOfWeaponForceCode._11.code);
		e = rule.apply(o);
		assertNotNull(e);
		assertEquals('2', e.getSegmentType());
		assertEquals("13", e.getDataElementIdentifier());
		assertEquals(TypeOfWeaponForceCode._11.code, e.getValue());
	}
	
	@Test
	public void testRule264() {
		Rule<OffenseSegment> rule = rulesFactory.getRule264();
		OffenseSegment o = buildBaseSegment();
		OffenseCode offenseCode = OffenseCode._09A;
		assertEquals(offenseCode.group, "A");
		o.setUcrOffenseCode(offenseCode.code);
		NIBRSError e = rule.apply(o);
		assertNull(e);
		o.setUcrOffenseCode(null);
		e = rule.apply(o);
		assertNull(e);
		offenseCode = OffenseCode._90J;
		assertEquals(offenseCode.group, "B");
		o.setUcrOffenseCode(offenseCode.code);
		e = rule.apply(o);
		assertNotNull(e);
		assertEquals('2', e.getSegmentType());
		assertEquals("6", e.getDataElementIdentifier());
		assertEquals(offenseCode.code, e.getValue());
	}
	
	@Test
	public void testRule258() {
		Rule<OffenseSegment> rule = rulesFactory.getRule258();
		OffenseSegment o = buildBaseSegment();
		o.setAutomaticWeaponIndicator(0, null);
		o.setAutomaticWeaponIndicator(1, null);
		o.setAutomaticWeaponIndicator(2, null);
		o.setTypeOfWeaponForceInvolved(0, null);
		o.setTypeOfWeaponForceInvolved(1, null);
		o.setTypeOfWeaponForceInvolved(2, null);
		NIBRSError e = rule.apply(o);
		assertNull(e);
		o.setTypeOfWeaponForceInvolved(0, TypeOfWeaponForceCode._20.code);
		e = rule.apply(o);
		assertNull(e);
		o.setAutomaticWeaponIndicator(0, AutomaticWeaponIndicatorCode._blank.code);
		e = rule.apply(o);
		assertNull(e);
		o.setTypeOfWeaponForceInvolved(0, TypeOfWeaponForceCode._11.code);
		e = rule.apply(o);
		assertNull(e);
		o.setAutomaticWeaponIndicator(0, AutomaticWeaponIndicatorCode._blank.code);
		e = rule.apply(o);
		assertNull(e);
		o.setAutomaticWeaponIndicator(0, AutomaticWeaponIndicatorCode.A.code);
		e = rule.apply(o);
		assertNull(e);
		o.setTypeOfWeaponForceInvolved(0, TypeOfWeaponForceCode._20.code);
		e = rule.apply(o);
		assertNotNull(e);
		assertEquals('2', e.getSegmentType());
		assertEquals("13", e.getDataElementIdentifier());
		assertEquals(TypeOfWeaponForceCode._20.code, e.getValue());
	}
	
	@Test
	public void testRule257() {
		Rule<OffenseSegment> rule = rulesFactory.getRule257();
		OffenseSegment o = buildBaseSegment();
		o.setNumberOfPremisesEntered(null);
		o.setUcrOffenseCode(OffenseCode._09A.code);
		o.setLocationType(LocationTypeCode._14.code);
		NIBRSError e = rule.apply(o);
		assertNull(e);
		o.setUcrOffenseCode(OffenseCode._220.code);
		o.setLocationType(LocationTypeCode._01.code);
		e = rule.apply(o);
		assertNull(e);
		o.setLocationType(LocationTypeCode._14.code);
		o.setNumberOfPremisesEntered(new ParsedObject<>(1));
		e = rule.apply(o);
		assertNull(e);
		o.setNumberOfPremisesEntered(ParsedObject.getMissingParsedObject());
		e = rule.apply(o);
		assertNotNull(e);
		assertNull(e.getValue());
		assertEquals('2', e.getSegmentType());
		assertEquals("10", e.getDataElementIdentifier());
	}
	
	@Test
	public void testRule256() {
		Rule<OffenseSegment> rule = rulesFactory.getRule256();
		OffenseSegment o = buildBaseSegment();
		o.setOffenseAttemptedCompleted(null);
		o.setUcrOffenseCode(null);
		NIBRSError e = rule.apply(o);
		assertNull(e);
		o.setUcrOffenseCode(OffenseCode._100.code);
		o.setOffenseAttemptedCompleted(OffenseAttemptedCompletedCode.A.code);
		e = rule.apply(o);
		assertNull(e);
		o.setUcrOffenseCode(OffenseCode._09A.code);
		o.setOffenseAttemptedCompleted(OffenseAttemptedCompletedCode.C.code);
		e = rule.apply(o);
		assertNull(e);
		o.setOffenseAttemptedCompleted(OffenseAttemptedCompletedCode.A.code);
		e = rule.apply(o);
		assertNotNull(e);
		assertEquals('2', e.getSegmentType());
		assertEquals("7", e.getDataElementIdentifier());
		assertEquals(OffenseAttemptedCompletedCode.A.code, e.getValue());
	}
	
	@Test
	public void testRule255() {
		Rule<OffenseSegment> rule = rulesFactory.getRule255();
		OffenseSegment o = buildBaseSegment();
		o.setAutomaticWeaponIndicator(0, null);
		o.setAutomaticWeaponIndicator(1, null);
		o.setAutomaticWeaponIndicator(2, null);
		NIBRSError e = rule.apply(o);
		assertNull(e);
		o.setAutomaticWeaponIndicator(0, AutomaticWeaponIndicatorCode._blank.code);
		e = rule.apply(o);
		assertNull(e);
		o.setAutomaticWeaponIndicator(0, "XXX");
		e = rule.apply(o);
		assertNotNull(e);
		assertEquals('2', e.getSegmentType());
		assertEquals("13", e.getDataElementIdentifier());
		assertArrayEquals(new String[] {"XXX", null, null}, (String[]) e.getValue());
		o.setAutomaticWeaponIndicator(0, "XXX");
		o.setAutomaticWeaponIndicator(1, AutomaticWeaponIndicatorCode._blank.code);
		e = rule.apply(o);
		assertNotNull(e);
		assertArrayEquals(new String[] {"XXX", AutomaticWeaponIndicatorCode._blank.code, null}, (String[]) e.getValue());
	}
	
	@Test
	public void testRule254() {
		Rule<OffenseSegment> rule = rulesFactory.getRule254();
		OffenseSegment o = buildBaseSegment();
		o.setMethodOfEntry(null);
		o.setUcrOffenseCode(null);
		NIBRSError e = rule.apply(o);
		assertNull(e);
		o.setUcrOffenseCode(OffenseCode._09A.code);
		e = rule.apply(o);
		assertNull(e);
		o.setMethodOfEntry(MethodOfEntryCode.F.code);
		e = rule.apply(o);
		assertNotNull(e);
		assertEquals('2', e.getSegmentType());
		assertEquals("11", e.getDataElementIdentifier());
		assertEquals(MethodOfEntryCode.F.code, e.getValue());
	}
	
	@Test
	public void testRule253() {
		Rule<OffenseSegment> rule = rulesFactory.getRule253();
		OffenseSegment o = buildBaseSegment();
		o.setMethodOfEntry(null);
		NIBRSError e = rule.apply(o);
		assertNull(e);
		o.setUcrOffenseCode(OffenseCode._220.code);
		o.setMethodOfEntry(MethodOfEntryCode.F.code);
		e = rule.apply(o);
		assertNull(e);
		o.setMethodOfEntry(null);
		e = rule.apply(o);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._253, e.getNIBRSErrorCode());
		assertEquals('2', e.getSegmentType());
		assertEquals("11", e.getDataElementIdentifier());
		assertEquals(null, e.getValue());
	}
	
	@Test
	public void testRule252() {
		Rule<OffenseSegment> rule = rulesFactory.getRule252();
		OffenseSegment o = buildBaseSegment();
		o.setNumberOfPremisesEntered(ParsedObject.getMissingParsedObject());
		NIBRSError e = rule.apply(o);
		assertNull(e);
		o.setNumberOfPremisesEntered(new ParsedObject<>(2));
		o.setUcrOffenseCode(OffenseCode._220.code);
		o.setLocationType(LocationTypeCode._14.code);
		e = rule.apply(o);
		assertNull(e);
		o.setUcrOffenseCode(OffenseCode._09A.code);
		e = rule.apply(o);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._252, e.getNIBRSErrorCode());
		assertEquals('2', e.getSegmentType());
		assertEquals("10", e.getDataElementIdentifier());
		assertEquals("220", e.getValue());
		o.setUcrOffenseCode(OffenseCode._220.code);
		o.setLocationType(LocationTypeCode._01.code);
		e = rule.apply(o);
		assertNotNull(e);
	}
	
	@Test
	public void testRule251() {
		Rule<OffenseSegment> rule = rulesFactory.getRule251();
		OffenseSegment o = buildBaseSegment();
		o.setOffenseAttemptedCompleted(null);
		NIBRSError e = rule.apply(o);
		assertNull(e);
		o.setOffenseAttemptedCompleted(OffenseAttemptedCompletedCode.A.code);
		e = rule.apply(o);
		assertNull(e);
		o.setOffenseAttemptedCompleted("X");
		e = rule.apply(o);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._251, e.getNIBRSErrorCode());
		assertEquals('2', e.getSegmentType());
		assertEquals("7", e.getDataElementIdentifier());
		assertEquals("X", e.getValue());
		assertEquals(o.getUcrOffenseCode(), e.getWithinSegmentIdentifier());
	}
	
	@Test
	public void testRule221() {
		
		Rule<OffenseSegment> rule = rulesFactory.getRule221();
		OffenseSegment o = buildBaseSegment();
		o.setTypeOfWeaponForceInvolved(0, null);
		o.setTypeOfWeaponForceInvolved(1, null);
		o.setTypeOfWeaponForceInvolved(2, null);
		o.setUcrOffenseCode(null);
		assertNull(rule.apply(o));
		o.setUcrOffenseCode(OffenseCode._250.code);
		assertNull(rule.apply(o));
		
		o.setUcrOffenseCode(OffenseCode._09A.code);
		NIBRSError e = rule.apply(o);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._221, e.getNIBRSErrorCode());
		assertEquals('2', e.getSegmentType());
		assertEquals("13", e.getDataElementIdentifier());
		assertArrayEquals(new String[3], (String[]) e.getValue());
		assertEquals(OffenseCode._09A.code, e.getWithinSegmentIdentifier());

		o.setTypeOfWeaponForceInvolved(0, TypeOfWeaponForceCode._11.code);
		assertNull(rule.apply(o));
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testRule222() {
		
		Rule<OffenseSegment> rule = rulesFactory.getRule222();
		OffenseSegment o = buildBaseSegment();
		o.setTypeOfWeaponForceInvolved(0, null);
		o.setTypeOfWeaponForceInvolved(1, null);
		o.setTypeOfWeaponForceInvolved(2, null);
		o.setUcrOffenseCode(null);
		assertNull(rule.apply(o));
		o.setUcrOffenseCode(OffenseCode._250.code);
		assertNull(rule.apply(o));
		
		List<String>  offenseCodes = Arrays.asList(
				OffenseCode._09A.code,
				OffenseCode._09B.code,
				OffenseCode._09C.code,
				OffenseCode._100.code,
				OffenseCode._11A.code,
				OffenseCode._11B.code,
				OffenseCode._11C.code,
				OffenseCode._11D.code,
				OffenseCode._120.code,
				OffenseCode._13A.code,
				OffenseCode._13B.code,
				OffenseCode._210.code,
				OffenseCode._520.code,
				OffenseCode._64A.code,
				OffenseCode._64B.code);
		
		NIBRSError e; 
		for (String offenseCode : offenseCodes) {
			o.setUcrOffenseCode(offenseCode);
			e = rule.apply(o);
			assertNull(e);
		}

		o.setUcrOffenseCode(OffenseCode._250.code);
		o.setTypeOfWeaponForceInvolved(0, TypeOfWeaponForceCode._11.code);
		e = rule.apply(o);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._222, e.getNIBRSErrorCode());
		assertEquals('2', e.getSegmentType());
		assertEquals("13", e.getDataElementIdentifier());
		assertTrue(Arrays.asList("11").containsAll((List<String>) e.getValue()));
		
	}
	
	@Test
	public void testRule220() {
		
		Rule<OffenseSegment> rule = rulesFactory.getRule220();
		OffenseSegment o = buildBaseSegment();
		o.setTypeOfCriminalActivity(0, null);
		o.setTypeOfCriminalActivity(1, null);
		o.setTypeOfCriminalActivity(2, null);
		o.setUcrOffenseCode(null);
		assertNull(rule.apply(o));
		o.setUcrOffenseCode(OffenseCode._09A.code);
		assertNull(rule.apply(o));
		
		o.setUcrOffenseCode(OffenseCode._250.code);
		NIBRSError e = rule.apply(o);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._220, e.getNIBRSErrorCode());
		assertEquals('2', e.getSegmentType());
		assertEquals("12", e.getDataElementIdentifier());
		assertArrayEquals(new String[3], (String[]) e.getValue());
		assertEquals(OffenseCode._250.code, e.getWithinSegmentIdentifier());

		o.setTypeOfCriminalActivity(0, TypeOfCriminalActivityCode.A.code);
		assertNull(rule.apply(o));
		
	}
	
	@Test
	public void testRule219() {
		
		Rule<OffenseSegment> rule = rulesFactory.getRule219();
		OffenseSegment o = buildBaseSegment();
		o.setTypeOfCriminalActivity(0, null);
		o.setTypeOfCriminalActivity(1, null);
		o.setTypeOfCriminalActivity(2, null);
		assertNull(rule.apply(o));
		
		o.setTypeOfCriminalActivity(0, TypeOfCriminalActivityCode.B.code);
		o.setUcrOffenseCode(OffenseCode._09A.code);
		NIBRSError e = rule.apply(o);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._219, e.getNIBRSErrorCode());
		assertEquals('2', e.getSegmentType());
		assertEquals("12", e.getDataElementIdentifier());
		assertEquals(TypeOfCriminalActivityCode.B.code, e.getValue());
		
		o.setUcrOffenseCode(OffenseCode._250.code);
		assertNull(rule.apply(o));

	}
	
	@Test
	public void testRule207() {
		
		Rule<OffenseSegment> rule207 = rulesFactory.getRule207("biasMotivation", "8A", BiasMotivationCode.noneOrUnknownValueCodeSet());
		OffenseSegment o = buildBaseSegment();
		o.setBiasMotivation(0, null);
		NIBRSError e = rule207.apply(o);
		assertNull(e);
		o.setBiasMotivation(0, BiasMotivationCode._11.code);
		e = rule207.apply(o);
		assertNull(e);
		o.setBiasMotivation(1, BiasMotivationCode._12.code);
		e = rule207.apply(o);
		assertNull(e);
		o.setBiasMotivation(2, BiasMotivationCode._88.code);
		e = rule207.apply(o);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._207, e.getNIBRSErrorCode());
		assertEquals('2', e.getSegmentType());
		assertEquals("8A", e.getDataElementIdentifier());

	}
	
	@Test
	public void testRule206() {
		
		Rule<OffenseSegment> rule206 = rulesFactory.getRule206("biasMotivation", "8A");
		OffenseSegment o = buildBaseSegment();
		o.setBiasMotivation(0, null);
		NIBRSError e = rule206.apply(o);
		assertNull(e);
		o.setBiasMotivation(0, BiasMotivationCode._11.code);
		e = rule206.apply(o);
		assertNull(e);
		o.setBiasMotivation(1, BiasMotivationCode._12.code);
		e = rule206.apply(o);
		assertNull(e);
		o.setBiasMotivation(2, BiasMotivationCode._12.code);
		e = rule206.apply(o);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._206, e.getNIBRSErrorCode());
		assertEquals('2', e.getSegmentType());
		assertEquals("8A", e.getDataElementIdentifier());

	}
	
	@Test
	public void testRule204_biasMotivation() {
		
		Rule<OffenseSegment> rule = rulesFactory.getRule204ForValueList("biasMotivation", "8A", BiasMotivationCode.codeSet());
		OffenseSegment o = buildBaseSegment();
		o.setBiasMotivation(0, null);
		NIBRSError e = rule.apply(o);
		assertNull(e);
		o.setBiasMotivation(0, BiasMotivationCode._11.code);
		e = rule.apply(o);
		assertNull(e);
		o.setBiasMotivation(0, "XXX");
		e = rule.apply(o);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._204, e.getNIBRSErrorCode());
		assertEquals('2', e.getSegmentType());
		assertEquals("8A", e.getDataElementIdentifier());
		
	}
		
	@Test
	public void testRule204_offendersSuspectedOfUsing() {
		
		Rule<OffenseSegment> rule = rulesFactory.getRule204ForValueList("offendersSuspectedOfUsing", "8", OffenderSuspectedOfUsingCode.codeSet());
		OffenseSegment o = buildBaseSegment();
		o.setOffendersSuspectedOfUsing(0, null);
		NIBRSError e = rule.apply(o);
		assertNull(e);
		o.setOffendersSuspectedOfUsing(0, OffenderSuspectedOfUsingCode.A.code);
		e = rule.apply(o);
		assertNull(e);
		o.setOffendersSuspectedOfUsing(0, "X");
		e = rule.apply(o);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._204, e.getNIBRSErrorCode());
		assertEquals('2', e.getSegmentType());
		assertEquals("8", e.getDataElementIdentifier());
		
	}
	
	@Test
	public void testRule204_methodOfEntry() {
			
		Rule<OffenseSegment> rule = rulesFactory.getRule204ForValueList("methodOfEntry", "11", MethodOfEntryCode.codeSet());
		OffenseSegment o = buildBaseSegment();
		o.setMethodOfEntry(null);
		NIBRSError e = rule.apply(o);
		assertNull(e);
		o.setMethodOfEntry(MethodOfEntryCode.F.code);
		e = rule.apply(o);
		assertNull(e);
		o.setMethodOfEntry("XXX");
		e = rule.apply(o);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._204, e.getNIBRSErrorCode());
		assertEquals('2', e.getSegmentType());
		assertEquals("11", e.getDataElementIdentifier());
		
	}
	
	@Test
	public void testRule204_locationType() {

		Rule<OffenseSegment> rule = rulesFactory.getRule204ForValueList("locationType", "9", LocationTypeCode.codeSet());
		OffenseSegment o = buildBaseSegment();
		o.setLocationType(null);
		NIBRSError e = rule.apply(o);
		assertNull(e);
		o.setLocationType(LocationTypeCode._01.code);
		e = rule.apply(o);
		assertNull(e);
		o.setLocationType("XXX");
		e = rule.apply(o);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._204, e.getNIBRSErrorCode());
		assertEquals('2', e.getSegmentType());
		assertEquals("9", e.getDataElementIdentifier());
		
	}

	@Test
	public void testRule204_numberOfPremisesEntered() {

		Rule<OffenseSegment> rule = rulesFactory.getRule204ForPremisesEntered();
		OffenseSegment o = buildBaseSegment();
		o.setNumberOfPremisesEntered(ParsedObject.getMissingParsedObject());
		NIBRSError e = rule.apply(o);
		assertNull(e);
		o.setNumberOfPremisesEntered(new ParsedObject<>(5));
		e = rule.apply(o);
		assertNull(e);
		o.setNumberOfPremisesEntered(new ParsedObject<>(500));
		e = rule.apply(o);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._204, e.getNIBRSErrorCode());
		assertEquals('2', e.getSegmentType());
		assertEquals("10", e.getDataElementIdentifier());
		assertEquals(500, e.getValue());
		assertEquals(o.getUcrOffenseCode(), e.getWithinSegmentIdentifier());

	}
	
	@Test
	public void testRule201ForOffendersSuspectedOfUsing() {
		
		Rule<OffenseSegment> rule201 = rulesFactory.getRule201ForOffendersSuspectedOfUsing();
		OffenseSegment o = buildBaseSegment();
		AbstractReport report = o.getParentReport();
		o.setOffendersSuspectedOfUsing(0, null);
		o.setOffendersSuspectedOfUsing(1, null);
		o.setOffendersSuspectedOfUsing(2, null);
		NIBRSError e = rule201.apply(o);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._201, e.getNIBRSErrorCode());
		assertEquals('2', e.getSegmentType());
		assertEquals("8", e.getDataElementIdentifier());
		String[] value = (String[]) e.getValue();
		assertEquals(3, value.length);
		assertEquals(report.getSource(), e.getContext());
		o.setOffendersSuspectedOfUsing(0, OffenderSuspectedOfUsingCode.A.code);
		o.setOffendersSuspectedOfUsing(1, null);
		o.setOffendersSuspectedOfUsing(2, null);
		e = rule201.apply(o);
		assertNull(e);
		o.setOffendersSuspectedOfUsing(0, "XXX");
		e = rule201.apply(o);
		assertNotNull(e);

	}

	@Test
	public void testRule204ForOffenseCode() {
		
		Rule<OffenseSegment> rule201 = rulesFactory.getRule204ForUCROffenseCode();
		OffenseSegment o = buildBaseSegment();
		AbstractReport report = o.getParentReport();
		o.setUcrOffenseCode(null);
		NIBRSError e = rule201.apply(o);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._204, e.getNIBRSErrorCode());
		assertEquals('2', e.getSegmentType());
		assertEquals("6", e.getDataElementIdentifier());
		assertEquals(null, e.getValue());
		assertEquals(report.getSource(), e.getContext());
		assertEquals(o.getUcrOffenseCode(), e.getWithinSegmentIdentifier());
		o.setUcrOffenseCode(OffenseCode._09A.code);
		e = rule201.apply(o);
		assertNull(e);
		o.setUcrOffenseCode("XXX");
		e = rule201.apply(o);
		assertNotNull(e);
		assertEquals(o.getUcrOffenseCode(), e.getWithinSegmentIdentifier());
		
	}

	@Test
	public void testRule201_offenseAttemptedCompleted() {
			
		Rule<OffenseSegment> rule201 = rulesFactory.getRule201ForSingleStringProperty("offenseAttemptedCompleted", "7");
		OffenseSegment o = buildBaseSegment();
		AbstractReport report = o.getParentReport();
		o.setOffenseAttemptedCompleted(null);
		NIBRSError e = rule201.apply(o);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._201, e.getNIBRSErrorCode());
		assertEquals('2', e.getSegmentType());
		assertEquals("7", e.getDataElementIdentifier());
		assertEquals(null, e.getValue());
		assertEquals(report.getSource(), e.getContext());
		assertEquals(o.getUcrOffenseCode(), e.getWithinSegmentIdentifier());
		o.setOffenseAttemptedCompleted(OffenseAttemptedCompletedCode.A.code);
		e = rule201.apply(o);
		assertNull(e);
		
	}
	
	@Test
	public void testRule201_locationType() {
			
		Rule<OffenseSegment> rule201 = rulesFactory.getRule201ForSingleStringProperty("locationType", "9");
		OffenseSegment o = buildBaseSegment();
		AbstractReport report = o.getParentReport();
		o.setLocationType(null);
		NIBRSError e = rule201.apply(o);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._201, e.getNIBRSErrorCode());
		assertEquals('2', e.getSegmentType());
		assertEquals("9", e.getDataElementIdentifier());
		assertEquals(null, e.getValue());
		assertEquals(report.getSource(), e.getContext());
		o.setLocationType(LocationTypeCode._01.code);
		e = rule201.apply(o);
		assertNull(e);
		
	}
		
	@Test
	public void testRule201_offendersSuspectedOfUsing() {
			
		Rule<OffenseSegment> rule201 = rulesFactory.getRule201ForStringArrayProperty("offendersSuspectedOfUsing", "8");
		OffenseSegment o = buildBaseSegment();
		AbstractReport report = o.getParentReport();
		o.setOffendersSuspectedOfUsing(0, null);
		NIBRSError e = rule201.apply(o);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._201, e.getNIBRSErrorCode());
		assertEquals('2', e.getSegmentType());
		assertEquals("8", e.getDataElementIdentifier());
		assertTrue(stringArrayAllNull(e.getValue(), 3));
		assertEquals(report.getSource(), e.getContext());
		o.setOffendersSuspectedOfUsing(0, OffenderSuspectedOfUsingCode.N.code);
		e = rule201.apply(o);
		assertNull(e);
		
	}
		
	@Test
	public void testRule201_biasMotivation() {
			
		Rule<OffenseSegment> rule201 = rulesFactory.getRule201ForStringArrayProperty("biasMotivation", "8A");
		OffenseSegment o = buildBaseSegment();
		AbstractReport report = o.getParentReport();
		o.setBiasMotivation(0, null);
		NIBRSError e = rule201.apply(o);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._201, e.getNIBRSErrorCode());
		assertEquals('2', e.getSegmentType());
		assertEquals("8A", e.getDataElementIdentifier());
		assertTrue(stringArrayAllNull(e.getValue(), 3));
		assertEquals(report.getSource(), e.getContext());
		o.setBiasMotivation(0, BiasMotivationCode._11.code);
		e = rule201.apply(o);
		assertNull(e);
		
	}
	
	private boolean stringArrayAllNull(Object o, int arrayLength) {
		boolean ret = true;
		if (o != null && o instanceof String[]) {
			String[] ss = (String[]) o;
			if (ss.length == arrayLength) {
				for (String s : ss) {
					if (s != null) {
						ret = false;
						break;
					}
				}
			}
		}
		return ret;
	}
	
	private OffenseSegment buildBaseSegment() {
		GroupAIncidentReport report = new GroupAIncidentReport();
		ReportSource source = new ReportSource();
		source.setSourceLocation(getClass().getName());
		source.setSourceName(getClass().getName());
		report.setSource(source);
		OffenseSegment o = new OffenseSegment();
		o.setUcrOffenseCode(OffenseCode._09A.code);
		report.addOffense(o);
		return o;
	}

}
