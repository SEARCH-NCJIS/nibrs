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
import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.search.nibrs.common.NIBRSError;
import org.search.nibrs.common.ParsedObject;
import org.search.nibrs.common.ReportSource;
import org.search.nibrs.model.GroupAIncidentReport;
import org.search.nibrs.model.OffenderSegment;
import org.search.nibrs.model.VictimSegment;
import org.search.nibrs.model.codes.ClearedExceptionallyCode;
import org.search.nibrs.model.codes.EthnicityCode;
import org.search.nibrs.model.codes.NIBRSErrorCode;
import org.search.nibrs.model.codes.RaceCode;
import org.search.nibrs.model.codes.RelationshipOfVictimToOffenderCode;
import org.search.nibrs.model.codes.SexCode;
import org.search.nibrs.validation.rules.Rule;

public class OffenderSegmentRulesFactoryTest {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(OffenderSegmentRulesFactoryTest.class);
	
	private OffenderSegmentRulesFactory rulesFactory = new OffenderSegmentRulesFactory();
	
	@Test
	public void testRule501() {
		Rule<OffenderSegment> rule = rulesFactory.getRule501();
		OffenderSegment os = buildBaseSegment();
		os.setOffenderSequenceNumber(ParsedObject.getMissingParsedObject());
		NIBRSError nibrsError = rule.apply(os);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._501, nibrsError.getNIBRSErrorCode());
		assertEquals("36", nibrsError.getDataElementIdentifier());
		@SuppressWarnings("unchecked")
		ParsedObject<Integer> value = (ParsedObject<Integer>) nibrsError.getValue();
		assertTrue(value.isMissing());
		os.setOffenderSequenceNumber(new ParsedObject<>(100));
		nibrsError = rule.apply(os);
		assertNotNull(nibrsError);
		os.setOffenderSequenceNumber(new ParsedObject<>(1));
		nibrsError = rule.apply(os);
		assertNull(nibrsError);
		os.setOffenderSequenceNumber(new ParsedObject<>(0));
		nibrsError = rule.apply(os);
		assertNull(nibrsError);
	}

	@Test
	public void testRule557() {
		Rule<OffenderSegment> rule = rulesFactory.getRule557();
		OffenderSegment os = buildBaseSegment();
		os.setOffenderSequenceNumber(new ParsedObject<>(0));
		GroupAIncidentReport incident = (GroupAIncidentReport) os.getParentReport();
		incident.setExceptionalClearanceCode(ClearedExceptionallyCode.A.code);
		NIBRSError nibrsError = rule.apply(os);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._557, nibrsError.getNIBRSErrorCode());
		assertEquals("36", nibrsError.getDataElementIdentifier());
		assertEquals(0, nibrsError.getValue());
	}
	
	@Test
	public void testRule556ForAgeOfOffender() {
		Rule<OffenderSegment> rule = rulesFactory.getRule556ForAgeOfOffender();
		OffenderSegment offenderSegment = buildBaseSegment();
		offenderSegment.setOffenderSequenceNumber(new ParsedObject<>(1));
		NIBRSError nibrsError = rule.apply(offenderSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._556, nibrsError.getNIBRSErrorCode());
		assertEquals("37", nibrsError.getDataElementIdentifier());
		assertNull(nibrsError.getValue());
		
		offenderSegment.setAgeString("00  ");
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
		offenderSegment.setAgeString("20  ");
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
		offenderSegment = buildBaseSegment();
		offenderSegment.setOffenderSequenceNumber(new ParsedObject<>(0));
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
		offenderSegment.setAgeString("2535");
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		offenderSegment.setAgeString("NB  ");
		nibrsError = rule.apply(offenderSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._556, nibrsError.getNIBRSErrorCode());
		assertEquals("NB  ", nibrsError.getValue());
		assertEquals("37", nibrsError.getDataElementIdentifier());
	}
//	@Test
//	public void testRule556() {
//		Rule<OffenderSegment> rule = rulesFactory.getRule556();
//		OffenderSegment offenderSegment = buildBaseSegment();
//		NIBRSError nibrsError = rule.apply(offenderSegment);
//		assertNull(nibrsError);
//		offenderSegment.setAgeString("20  ");
//		nibrsError = rule.apply(offenderSegment);
//		assertNull(nibrsError);
//		offenderSegment.setAgeString("00  ");
//		nibrsError = rule.apply(offenderSegment);
//		assertNull(nibrsError);
//		offenderSegment.setAgeString("2535");
//		nibrsError = rule.apply(offenderSegment);
//		assertNull(nibrsError);
//		offenderSegment.setAgeString("NB  ");
//		nibrsError = rule.apply(offenderSegment);
//		assertNotNull(nibrsError);
//		assertEquals(NIBRSErrorCode._556, nibrsError.getNIBRSErrorCode());
//		assertEquals(offenderSegment.getAge(), nibrsError.getValue());
//		assertEquals("37", nibrsError.getDataElementIdentifier());
//	}

	@Test
	public void testRule504ForSexOfOffender() {
		
		Rule<OffenderSegment> rule = rulesFactory.getRule504ForSexOfOffender();

		OffenderSegment offenderSegment = buildBaseSegment();
		offenderSegment.setOffenderSequenceNumber(new ParsedObject<>(1));
		offenderSegment.setSex(null);
		NIBRSError nibrsError = rule.apply(offenderSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._504, nibrsError.getNIBRSErrorCode());
		assertEquals("38", nibrsError.getDataElementIdentifier());
		assertNull(nibrsError.getValue());
		
		offenderSegment.setSex("invalid");
		nibrsError = rule.apply(offenderSegment);
		assertNotNull(nibrsError);
		assertEquals("invalid", nibrsError.getValue());
		
		offenderSegment.setSex(SexCode.F.code);
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
		offenderSegment = buildBaseSegment();
		offenderSegment.setOffenderSequenceNumber(new ParsedObject<>(0));
		offenderSegment.setSex(null);
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);

	}

	@Test
	public void testRule504ForRaceOfOffender() {
		
		Rule<OffenderSegment> rule = rulesFactory.getRule504ForRaceOfOffender();
	
		OffenderSegment offenderSegment = buildBaseSegment();
		offenderSegment.setOffenderSequenceNumber(new ParsedObject<>(1));
		offenderSegment.setRace(null);
		NIBRSError nibrsError = rule.apply(offenderSegment);
		assertNotNull(nibrsError);	
		assertEquals(NIBRSErrorCode._504, nibrsError.getNIBRSErrorCode());
		assertEquals("39", nibrsError.getDataElementIdentifier());
		assertEquals(null, nibrsError.getValue());
		
		offenderSegment.setRace("invalid");
		nibrsError = rule.apply(offenderSegment);
		assertNotNull(nibrsError);
		assertEquals("invalid", nibrsError.getValue());
		
		offenderSegment.setRace(RaceCode.A.code);
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
		offenderSegment = buildBaseSegment();
		offenderSegment.setOffenderSequenceNumber(new ParsedObject<>(0));
		offenderSegment.setRace(null);
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
	}
	
	@Test
	public void testRule504ForEthnicityOfOffender() {
		
		Rule<OffenderSegment> rule = rulesFactory.getRule504ForEthnicityOfOffender();
	
		OffenderSegment offenderSegment = buildBaseSegment();
		offenderSegment.setEthnicity(null);
		NIBRSError nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);

		offenderSegment.setEthnicity("invalid");
		nibrsError = rule.apply(offenderSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._504, nibrsError.getNIBRSErrorCode());
		assertEquals("39A", nibrsError.getDataElementIdentifier());
		assertEquals("invalid", nibrsError.getValue());
		
		offenderSegment.setEthnicity(EthnicityCode.H.code);
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
	}
	
	@Test
	public void testRule510() {

		Rule<OffenderSegment> rule = rulesFactory.getRule510();

		OffenderSegment offenderSegment = buildBaseSegment();
		offenderSegment.setAgeString("3020");
		NIBRSError nibrsError = rule.apply(offenderSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._510, nibrsError.getNIBRSErrorCode());
		assertEquals("37", nibrsError.getDataElementIdentifier());
		assertEquals(offenderSegment.getAge(), nibrsError.getValue());

		offenderSegment.setAgeString("2030");
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
	}
	
	@Test
	public void testRule509() {

		Rule<OffenderSegment> rule = rulesFactory.getRule509();

		OffenderSegment offenderSegment = buildBaseSegment();
		offenderSegment.setAgeString("123");
		NIBRSError nibrsError = rule.apply(offenderSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._509, nibrsError.getNIBRSErrorCode());
		assertEquals("37", nibrsError.getDataElementIdentifier());
		assertEquals("123", nibrsError.getValue());

		offenderSegment.setAgeString("2030");
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
	}
	
	@Test
	public void testRule522() {

		Rule<OffenderSegment> rule = rulesFactory.getRule522();

		OffenderSegment offenderSegment = buildBaseSegment();
		offenderSegment.setAgeString("0020");
		NIBRSError nibrsError = rule.apply(offenderSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._522, nibrsError.getNIBRSErrorCode());
		assertEquals("37", nibrsError.getDataElementIdentifier());
		assertEquals(offenderSegment.getAge(), nibrsError.getValue());

		offenderSegment.setAgeString("2030");
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
	}
	
	@Test
	public void testRule550() {
		
		Rule<OffenderSegment> rule = rulesFactory.getRule550();
		
		OffenderSegment offenderSegment = buildBaseSegment();
		GroupAIncidentReport incident = (GroupAIncidentReport) offenderSegment.getParentReport();
		VictimSegment vs = new VictimSegment();
		incident.addVictim(vs);
		
		NIBRSError nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
		offenderSegment.setOffenderSequenceNumber(new ParsedObject<>(1));
		vs.setOffenderNumberRelated(0, new ParsedObject<>(1));
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
		vs.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.AQ.code);
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
		offenderSegment.setAgeString("25  ");
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
		offenderSegment.setAgeString("08  ");
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
		vs.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.SE.code);
		nibrsError = rule.apply(offenderSegment);
		assertNotNull(nibrsError);
		assertEquals("37", nibrsError.getDataElementIdentifier());
		assertEquals(NIBRSErrorCode._550, nibrsError.getNIBRSErrorCode());
		assertEquals(offenderSegment.getAge(), nibrsError.getValue());
		
		vs.setOffenderNumberRelated(1, new ParsedObject<>(2));
		vs.setVictimOffenderRelationship(1, RelationshipOfVictimToOffenderCode.AQ.code);
		nibrsError = rule.apply(offenderSegment);
		assertNotNull(nibrsError);
		
	}
	
	@Test
	public void testRule552ForAge() {
		Rule<OffenderSegment> rule = rulesFactory.getRule552ForAge();
		OffenderSegment offenderSegment = buildBaseSegment();
		offenderSegment.setOffenderSequenceNumber(new ParsedObject<>(0));
		offenderSegment.setAgeString("29  ");
		NIBRSError e = rule.apply(offenderSegment);
		assertNotNull(e);
		assertEquals("37", e.getDataElementIdentifier());
		assertEquals(NIBRSErrorCode._552, e.getNIBRSErrorCode());
		offenderSegment.setAgeString(null);
		e = rule.apply(offenderSegment);
		assertNull(e);
	}
	
	@Test
	public void testRule552ForSex() {
		Rule<OffenderSegment> rule = rulesFactory.getRule552ForSex();
		OffenderSegment offenderSegment = buildBaseSegment();
		offenderSegment.setOffenderSequenceNumber(new ParsedObject<>(0));
		offenderSegment.setSex("M");
		NIBRSError e = rule.apply(offenderSegment);
		assertNotNull(e);
		assertEquals("38", e.getDataElementIdentifier());
		assertEquals(NIBRSErrorCode._552, e.getNIBRSErrorCode());
		offenderSegment.setSex(null);
		e = rule.apply(offenderSegment);
		assertNull(e);
	}

	// Removed with Tech Spec 3.1
	/*
	@Test
	public void testRule553() {
		
		Rule<OffenderSegment> rule = rulesFactory.getRule553();
		
		OffenderSegment offenderSegment = buildBaseSegment();
		GroupAIncidentReport incident = (GroupAIncidentReport) offenderSegment.getParentReport();
		VictimSegment victimSegment = new VictimSegment();
		incident.addVictim(victimSegment);
		
		NIBRSError nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);

		offenderSegment.setOffenderSequenceNumber(new ParsedObject<>(1));
		victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(1));
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
		victimSegment.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.HR.code);
		victimSegment.setSex(SexCode.M.code);
		offenderSegment.setSex(SexCode.F.code);
		nibrsError = rule.apply(offenderSegment);
		assertNotNull(nibrsError);
		assertEquals("38", nibrsError.getDataElementIdentifier());
		assertEquals(SexCode.F.code, nibrsError.getValue());
		assertEquals(NIBRSErrorCode._553, nibrsError.getNIBRSErrorCode());
		
		offenderSegment.setSex(SexCode.M.code);
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
		victimSegment.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.SE.code);
		nibrsError = rule.apply(offenderSegment);
		assertNotNull(nibrsError);
		
		victimSegment.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.BG.code);
		nibrsError = rule.apply(offenderSegment);
		assertNotNull(nibrsError);
		
		victimSegment.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.XS.code);
		nibrsError = rule.apply(offenderSegment);
		assertNotNull(nibrsError);
		
		victimSegment.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.AQ.code);
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
	}
	*/
	
	@Test
	public void testRule554() {
		
		Rule<OffenderSegment> rule = rulesFactory.getRule554();
		
		OffenderSegment offenderSegment = buildBaseSegment();
		GroupAIncidentReport incident = (GroupAIncidentReport) offenderSegment.getParentReport();
		VictimSegment victimSegment = new VictimSegment();
		incident.addVictim(victimSegment);
		
		NIBRSError nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);

		offenderSegment.setOffenderSequenceNumber(new ParsedObject<>(1));
		victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(1));
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
		victimSegment.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.PA.code);
		victimSegment.setAgeString("00  ");
		offenderSegment.setAgeString("00  ");
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);

		victimSegment.setAgeString("20  ");
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		victimSegment.setAgeString("00  ");
		offenderSegment.setAgeString("20  ");
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);

		victimSegment.setAgeString("50  ");
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
		victimSegment.setAgeString("10  ");
		nibrsError = rule.apply(offenderSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._554, nibrsError.getNIBRSErrorCode());
		assertEquals("37", nibrsError.getDataElementIdentifier());
		assertEquals(offenderSegment.getAge(), nibrsError.getValue());
		
		offenderSegment.setAgeString("2030");
		victimSegment.setAgeString("1015");
		nibrsError = rule.apply(offenderSegment);
		assertNotNull(nibrsError);
		victimSegment.setAgeString("1025");
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
	}
	
	@Test
	public void testRule572() {
		
		Rule<OffenderSegment> rule = rulesFactory.getRule572();
		
		OffenderSegment offenderSegment = buildBaseSegment();
		GroupAIncidentReport incident = (GroupAIncidentReport) offenderSegment.getParentReport();
		VictimSegment victimSegment = new VictimSegment();
		incident.addVictim(victimSegment);
		
		NIBRSError nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);

		offenderSegment.setOffenderSequenceNumber(new ParsedObject<>(1));
		victimSegment.setOffenderNumberRelated(0, new ParsedObject<>(1));
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
		victimSegment.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.PA.code);
		nibrsError = rule.apply(offenderSegment);
		assertNotNull(nibrsError);
		assertEquals(NIBRSErrorCode._572, nibrsError.getNIBRSErrorCode());
		assertEquals(RelationshipOfVictimToOffenderCode.PA.code, nibrsError.getValue());
		assertEquals("35", nibrsError.getDataElementIdentifier());

		victimSegment.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.RU.code);
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
		victimSegment.setVictimOffenderRelationship(0, RelationshipOfVictimToOffenderCode.PA.code);
		offenderSegment.setSex(SexCode.M.code);
		nibrsError = rule.apply(offenderSegment);
		assertNull(nibrsError);
		
	}
	
	private OffenderSegment buildBaseSegment() {
		GroupAIncidentReport report = new GroupAIncidentReport();
		ReportSource source = new ReportSource();
		source.setSourceLocation(getClass().getName());
		source.setSourceName(getClass().getName());
		report.setSource(source);
		OffenderSegment o = new OffenderSegment();
		report.addOffender(o);
		return o;
	}

}
