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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.search.nibrs.common.NIBRSError;
import org.search.nibrs.common.ParsedObject;
import org.search.nibrs.common.ReportSource;
import org.search.nibrs.model.GroupAIncidentReport;
import org.search.nibrs.model.OffenseSegment;
import org.search.nibrs.model.PropertySegment;
import org.search.nibrs.model.codes.NIBRSErrorCode;
import org.search.nibrs.model.codes.OffenseAttemptedCompletedCode;
import org.search.nibrs.model.codes.OffenseCode;
import org.search.nibrs.model.codes.PropertyDescriptionCode;
import org.search.nibrs.model.codes.SuspectedDrugTypeCode;
import org.search.nibrs.model.codes.TypeOfDrugMeasurementCode;
import org.search.nibrs.model.codes.TypeOfPropertyLossCode;
import org.search.nibrs.validation.rules.Rule;

public class PropertySegmentRulesFactoryTest {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(PropertySegmentRulesFactoryTest.class);
	
	private PropertySegmentRulesFactory rulesFactory = new PropertySegmentRulesFactory();
	
	private static final String[] getStringArrayWithValues(int length, String... values) {
		String[] ret = new String[length];
		for (int i=0; i < values.length; i++) {
			ret[i] = values[i];
		}
		return ret;
	}
	
	@Test
	public void testRule367() {
		Rule<PropertySegment> rule = rulesFactory.getRule367();
		PropertySegment p = buildBaseSegment();
		setAllNull(p.getSuspectedDrugType());
		setAllNull(p.getTypeDrugMeasurement());
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setTypeDrugMeasurement(0, TypeOfDrugMeasurementCode._DU.code);
		p.setSuspectedDrugType(0, SuspectedDrugTypeCode._A.code);
		e = rule.apply(p);
		assertNull(e);
		p.setTypeDrugMeasurement(0, TypeOfDrugMeasurementCode._NP.code);
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._367, e.getNIBRSErrorCode());
		assertEquals("22", e.getDataElementIdentifier());
		assertEquals(SuspectedDrugTypeCode._A.code, e.getValue());
	}
	
	@Test
	public void testRule365() {
		Rule<PropertySegment> rule = rulesFactory.getRule365();
		PropertySegment p = buildBaseSegment();
		setAllNull(p.getSuspectedDrugType());
		setAllNull(p.getPropertyDescription());
		p.setTypeOfPropertyLoss(null);
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setSuspectedDrugType(0, SuspectedDrugTypeCode._A.code);
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._365, e.getNIBRSErrorCode());
		assertEquals("20", e.getDataElementIdentifier());
		assertEquals(SuspectedDrugTypeCode._A.code, e.getValue());
		
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._1.code);
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._365, e.getNIBRSErrorCode());
		assertEquals("20", e.getDataElementIdentifier());
		assertEquals(SuspectedDrugTypeCode._A.code, e.getValue());
		
		GroupAIncidentReport incident = (GroupAIncidentReport) p.getParentReport();
		OffenseSegment o = new OffenseSegment();
		incident.addOffense(o);
		o.setUcrOffenseCode(null);
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._365, e.getNIBRSErrorCode());
		assertEquals("20", e.getDataElementIdentifier());
		assertEquals(SuspectedDrugTypeCode._A.code, e.getValue());
		o.setUcrOffenseCode(OffenseCode._35A.code);
		e = rule.apply(p);
		assertNull(e);
	
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._6.code);
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._365, e.getNIBRSErrorCode());
		assertEquals("20", e.getDataElementIdentifier());
		assertEquals(SuspectedDrugTypeCode._A.code, e.getValue());
		
		p.setPropertyDescription(0, "20");
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._365, e.getNIBRSErrorCode());
		assertEquals("20", e.getDataElementIdentifier());
		assertEquals(SuspectedDrugTypeCode._A.code, e.getValue());
		
		p.setPropertyDescription(0, "10");
		e = rule.apply(p);
		assertNull(e);
	}
	
	@Test
	public void testRule364ForMeasurement() {
		testRule364_common(rulesFactory.getRule364forMeasurement(), "22");
	}

	@Test
	public void testRule364ForQuantity() {
		testRule364_common(rulesFactory.getRule364forQuantity(), "21");
	}

	private void testRule364_common(Rule<PropertySegment> rule, String expectedDataElementIdentifier) {
		PropertySegment p = buildBaseSegment();
		GroupAIncidentReport incident = (GroupAIncidentReport) p.getParentReport();
		OffenseSegment o = new OffenseSegment();
		incident.addOffense(o);
		o.setUcrOffenseCode(null);
		p.setTypeOfPropertyLoss(null);
		setAllNull(p.getSuspectedDrugType());
		NIBRSError e = rule.apply(p);
		assertNull(e);
		o.setUcrOffenseCode(OffenseCode._35A.code);
		e = rule.apply(p);
		assertNull(e);
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._6.code);
		e = rule.apply(p);
		assertNull(e);
		p.setPropertyDescription(0, PropertyDescriptionCode._10.code);
		e = rule.apply(p);
		assertNull(e);
		p.setSuspectedDrugType(0, SuspectedDrugTypeCode._A.code);
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._364, e.getNIBRSErrorCode());
		assertEquals(expectedDataElementIdentifier, e.getDataElementIdentifier());
		assertNull(e.getValue());
		p.setPropertyDescription(1, PropertyDescriptionCode._01.code);
		e = rule.apply(p);
		assertNotNull(e);
	}
	
	@Test
	public void testRule363ForMeasurement() {
		Rule<PropertySegment> rule = rulesFactory.getRule363forMeasurement();
		PropertySegment p = buildBaseSegment();
		setAllNull(p.getSuspectedDrugType());
		setAllNull(p.getEstimatedDrugQuantity());
		setAllNull(p.getTypeDrugMeasurement());
		NIBRSError e;
		e = rule.apply(p);
		assertNull(e);
		p.setSuspectedDrugType(0, SuspectedDrugTypeCode._A.code);
		p.setSuspectedDrugType(1, SuspectedDrugTypeCode._B.code);
		p.setSuspectedDrugType(2, SuspectedDrugTypeCode._X.code);
		e = rule.apply(p);
		assertNull(e);
		p.setTypeDrugMeasurement(2, TypeOfDrugMeasurementCode._DU.code);
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._363, e.getNIBRSErrorCode());
		assertEquals("22", e.getDataElementIdentifier());
		assertEquals(TypeOfDrugMeasurementCode._DU.code, e.getValue());
	}
	
	@Test
	public void testRule363ForQuantity() {
		Rule<PropertySegment> rule = rulesFactory.getRule363forQuantity();
		PropertySegment p = buildBaseSegment();
		setAllNull(p.getSuspectedDrugType());
		setAllNull(p.getEstimatedDrugQuantity());
		setAllNull(p.getTypeDrugMeasurement());
		NIBRSError e;
		e = rule.apply(p);
		assertNull(e);
		p.setSuspectedDrugType(0, SuspectedDrugTypeCode._A.code);
		p.setSuspectedDrugType(1, SuspectedDrugTypeCode._B.code);
		p.setSuspectedDrugType(2, SuspectedDrugTypeCode._X.code);
		e = rule.apply(p);
		assertNull(e);
		p.setEstimatedDrugQuantity(2, new ParsedObject<>(10.0));
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._363, e.getNIBRSErrorCode());
		assertEquals("21", e.getDataElementIdentifier());
		assertEquals(10.0, e.getValue());
	}
	
	@Test
	public void testRule362() {
		Rule<PropertySegment> rule = rulesFactory.getRule362();
		PropertySegment p = buildBaseSegment();
		setAllNull(p.getSuspectedDrugType());
		NIBRSError e;
		e = rule.apply(p);
		assertNull(e);
		p.setSuspectedDrugType(0, SuspectedDrugTypeCode._A.code);
		p.setSuspectedDrugType(1, SuspectedDrugTypeCode._B.code);
		p.setSuspectedDrugType(2, SuspectedDrugTypeCode._X.code);
		e = rule.apply(p);
		assertNull(e);
		p.setSuspectedDrugType(1, SuspectedDrugTypeCode._X.code);
		p.setSuspectedDrugType(2, SuspectedDrugTypeCode._B.code);
		e = rule.apply(p);
		assertNull(e);
		p.setSuspectedDrugType(2, null);
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._362, e.getNIBRSErrorCode());
		assertEquals("20", e.getDataElementIdentifier());
		assertArrayEquals(new String[] {SuspectedDrugTypeCode._A.code, SuspectedDrugTypeCode._X.code, null}, (String[]) e.getValue());
	}
	
	@Test
	public void testRule359() {
		Rule<PropertySegment> rule = rulesFactory.getRule359();
		PropertySegment p = buildBaseSegment();
		setAllNull(p.getPropertyDescription());
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setPropertyDescription(0, PropertyDescriptionCode._01.code);
		e = rule.apply(p);
		assertNull(e);
		p.setNumberOfStolenMotorVehicles(new ParsedObject<>(0));
		e = rule.apply(p);
		assertNull(e);
		p.setNumberOfRecoveredMotorVehicles(new ParsedObject<>(0));
		e = rule.apply(p);
		assertNull(e);
		p.setNumberOfStolenMotorVehicles(new ParsedObject<>(1));
		p.setPropertyDescription(0, null);
		e = rule.apply(p);
		assertNull(e);
		
		GroupAIncidentReport incident = (GroupAIncidentReport) p.getParentReport();
		OffenseSegment o = new OffenseSegment();
		incident.addOffense(o);
		o.setUcrOffenseCode(OffenseCode._240.code);
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._359, e.getNIBRSErrorCode());
		assertEquals("18", e.getDataElementIdentifier());
		assertThat(e.getValue(), is(1));
		p.setPropertyDescription(0, PropertyDescriptionCode._01.code);
		e = rule.apply(p);
		assertNotNull(e);
		p.setNumberOfStolenMotorVehicles(ParsedObject.getMissingParsedObject());
		p.setNumberOfRecoveredMotorVehicles(new ParsedObject<>(1));
		e = rule.apply(p);
		assertNotNull(e);
		p.setPropertyDescription(0, PropertyDescriptionCode._03.code);
		e = rule.apply(p);
		assertNull(e);
	}
	
	@Test
	public void testRule361() {
		Rule<PropertySegment> rule = rulesFactory.getRule361();
		PropertySegment p = buildBaseSegment();
		GroupAIncidentReport incident = (GroupAIncidentReport) p.getParentReport();
		OffenseSegment o = new OffenseSegment();
		incident.addOffense(o);
		o.setUcrOffenseCode(null);
		o.setOffenseAttemptedCompleted(null);
		p.setTypeOfPropertyLoss(null);
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._1.code);
		o.setUcrOffenseCode(OffenseCode._240.code);
		o.setOffenseAttemptedCompleted(OffenseAttemptedCompletedCode.C.code);
		e = rule.apply(p);
		assertNull(e);
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._5.code);
		o.setUcrOffenseCode(OffenseCode._09A.code);
		e = rule.apply(p);
		assertNull(e);
		o.setUcrOffenseCode(OffenseCode._240.code);
		p.setNumberOfRecoveredMotorVehicles(new ParsedObject<>(1));
		e = rule.apply(p);
		assertNull(e);
		
		p.setNumberOfRecoveredMotorVehicles(new ParsedObject<Integer>());
		e = rule.apply(p);
		assertNull(e);
		
		p.setPropertyDescription(0, PropertyDescriptionCode._03.code);
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._361, e.getNIBRSErrorCode());
		assertEquals("19", e.getDataElementIdentifier());
		assertNull(e.getValue());
		p.setNumberOfRecoveredMotorVehicles(new ParsedObject<>(0));
		e = rule.apply(p);
		assertNull(e);
		
	}
	
	@Test
	public void testRule389() {
		Rule<PropertySegment> rule = rulesFactory.getRule389();
		PropertySegment p = buildBaseSegment();
		GroupAIncidentReport incident = (GroupAIncidentReport) p.getParentReport();
		OffenseSegment o = new OffenseSegment();
		incident.addOffense(o);
		o.setUcrOffenseCode(null);
		o.setOffenseAttemptedCompleted(null);
		p.setTypeOfPropertyLoss(null);
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._1.code);
		o.setUcrOffenseCode(OffenseCode._240.code);
		o.setOffenseAttemptedCompleted(OffenseAttemptedCompletedCode.C.code);
		e = rule.apply(p);
		assertNull(e);
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._5.code);
		o.setUcrOffenseCode(OffenseCode._09A.code);
		e = rule.apply(p);
		assertNull(e);
		o.setUcrOffenseCode(OffenseCode._240.code);
		p.setNumberOfRecoveredMotorVehicles(new ParsedObject<>(1));
		e = rule.apply(p);
		assertNull(e);
		
		p.setNumberOfRecoveredMotorVehicles(new ParsedObject<Integer>());
		e = rule.apply(p);
		assertNull(e);
		
		p.setPropertyDescription(0, PropertyDescriptionCode._03.code);
		e = rule.apply(p);
		assertNull(e); 
		
		p.setPropertyDescription(1, PropertyDescriptionCode._05.code);
		e = rule.apply(p);
		assertNull(e);
		
		p.setNumberOfRecoveredMotorVehicles(new ParsedObject<Integer>(0));
		e = rule.apply(p);
		assertNull(e);
		
		p.setNumberOfRecoveredMotorVehicles(new ParsedObject<Integer>(1));
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._389, e.getNIBRSErrorCode());
		assertEquals("19", e.getDataElementIdentifier());
		assertEquals("1", e.getValue().toString());
		
		p.setNumberOfRecoveredMotorVehicles(new ParsedObject<Integer>(2));
		e = rule.apply(p);
		assertNull(e);
	}
	
	@Test
	public void testRule358() {
		Rule<PropertySegment> rule = rulesFactory.getRule358();
		PropertySegment p = buildBaseSegment();
		GroupAIncidentReport incident = (GroupAIncidentReport) p.getParentReport();
		OffenseSegment o = new OffenseSegment();
		incident.addOffense(o);
		o.setUcrOffenseCode(null);
		o.setOffenseAttemptedCompleted(null);
		p.setNumberOfStolenMotorVehicles(ParsedObject.getMissingParsedObject());
		p.setTypeOfPropertyLoss(null);
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setNumberOfStolenMotorVehicles(ParsedObject.getMissingParsedObject());
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._1.code);
		o.setUcrOffenseCode(OffenseCode._240.code);
		o.setOffenseAttemptedCompleted(OffenseAttemptedCompletedCode.C.code);
		e = rule.apply(p);
		assertNull(e);
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._7.code);
		o.setUcrOffenseCode(OffenseCode._09A.code);
		e = rule.apply(p);
		assertNull(e);
		o.setUcrOffenseCode(OffenseCode._240.code);
		o.setOffenseAttemptedCompleted(OffenseAttemptedCompletedCode.A.code);
		e = rule.apply(p);
		assertNull(e);
		
		o.setOffenseAttemptedCompleted(OffenseAttemptedCompletedCode.C.code);
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._358, e.getNIBRSErrorCode());
		assertEquals("18", e.getDataElementIdentifier());
		assertEquals("null", e.getValue().toString());
		
	}
	
	@Test
	public void testRule360() {
		Rule<PropertySegment> rule = rulesFactory.getRule360();
		PropertySegment p = buildBaseSegment();
		GroupAIncidentReport incident = (GroupAIncidentReport) p.getParentReport();
		OffenseSegment o = new OffenseSegment();
		incident.addOffense(o);
		o.setUcrOffenseCode(null);
		o.setOffenseAttemptedCompleted(null);
		p.setTypeOfPropertyLoss(null);
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setNumberOfRecoveredMotorVehicles(new ParsedObject<>(1));
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._1.code);
		o.setUcrOffenseCode(OffenseCode._240.code);
		o.setOffenseAttemptedCompleted(OffenseAttemptedCompletedCode.C.code);
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._360, e.getNIBRSErrorCode());
		assertEquals("19", e.getDataElementIdentifier());
		assertEquals("1", e.getValue().toString());
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._5.code);
		o.setUcrOffenseCode(OffenseCode._09A.code);
		o.setOffenseAttemptedCompleted(OffenseAttemptedCompletedCode.C.code);
		e = rule.apply(p);
		assertNotNull(e);
		o.setUcrOffenseCode(OffenseCode._240.code);
		o.setOffenseAttemptedCompleted(OffenseAttemptedCompletedCode.A.code);
		e = rule.apply(p);
		assertNotNull(e);
		o.setOffenseAttemptedCompleted(OffenseAttemptedCompletedCode.C.code);
		e = rule.apply(p);
		assertNull(e);
	}
	
	@Test
	public void testRule357() {
		Rule<PropertySegment> rule = rulesFactory.getRule357();
		PropertySegment p = buildBaseSegment();
		GroupAIncidentReport incident = (GroupAIncidentReport) p.getParentReport();
		OffenseSegment o = new OffenseSegment();
		incident.addOffense(o);
		o.setUcrOffenseCode(null);
		o.setOffenseAttemptedCompleted(null);
		p.setNumberOfStolenMotorVehicles(ParsedObject.getMissingParsedObject());
		p.setTypeOfPropertyLoss(null);
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setNumberOfStolenMotorVehicles(new ParsedObject<>(1));
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._1.code);
		o.setUcrOffenseCode(OffenseCode._240.code);
		o.setOffenseAttemptedCompleted(OffenseAttemptedCompletedCode.C.code);
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(NIBRSErrorCode._357, e.getNIBRSErrorCode());
		assertEquals("18", e.getDataElementIdentifier());
		assertEquals("1", e.getValue());
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._7.code);
		o.setUcrOffenseCode(OffenseCode._09A.code);
		o.setOffenseAttemptedCompleted(OffenseAttemptedCompletedCode.C.code);
		e = rule.apply(p);
		assertNotNull(e);
		o.setUcrOffenseCode(OffenseCode._240.code);
		o.setOffenseAttemptedCompleted(OffenseAttemptedCompletedCode.A.code);
		e = rule.apply(p);
		assertNotNull(e);
		o.setOffenseAttemptedCompleted(OffenseAttemptedCompletedCode.C.code);
		e = rule.apply(p);
		assertNull(e);
	}
	
	@Test
	public void testRule355() {
		Rule<PropertySegment> rule = rulesFactory.getRule355();
		PropertySegment p = buildBaseSegment();
		//setAllNull(p.getDateRecovered());
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._5.code);
		LocalDate d = LocalDate.now();
		p.setDateRecovered(0, new ParsedObject<>(d));
		e = rule.apply(p);
		assertNull(e);
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._1.code);
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(d, e.getValue());
		assertEquals(NIBRSErrorCode._355, e.getNIBRSErrorCode());
		assertEquals("17", e.getDataElementIdentifier());
	}
	
	@Test
	public void testRule354() {
		Rule<PropertySegment> rule = rulesFactory.getRule354();
		PropertySegment p = buildBaseSegment();
		setAllNull(p.getPropertyDescription());
		for (int i= 0; i< PropertySegment.VALUE_OF_PROPERTY_COUNT; i++){
			p.setValueOfProperty(i, new ParsedObject<Integer>());
		}
		
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setValueOfProperty(0, new ParsedObject<>(10));
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(10, e.getValue());
		assertEquals(NIBRSErrorCode._354, e.getNIBRSErrorCode());
		assertEquals("16", e.getDataElementIdentifier());
	}
	
	@Test
	public void testRule353() {
		Rule<PropertySegment> rule = rulesFactory.getRule353();
		PropertySegment p = buildBaseSegment();
		setAllNull(p.getPropertyDescription());
		setAllNull(p.getValueOfProperty());
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setPropertyDescription(0, PropertyDescriptionCode._88.code);
		p.setValueOfProperty(0, new ParsedObject<>(1));
		e = rule.apply(p);
		assertNull(e);
		p.setValueOfProperty(0, new ParsedObject<>(10));
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(10, e.getValue());
		assertEquals(NIBRSErrorCode._353, e.getNIBRSErrorCode());
		assertEquals("16", e.getDataElementIdentifier());
	}
	
	@Test
	public void testRule352() {
		
		PropertySegment p = buildBaseSegment();
		GroupAIncidentReport incident = (GroupAIncidentReport) p.getParentReport();
		OffenseSegment o = new OffenseSegment();
		incident.addOffense(o);
		o.setUcrOffenseCode(OffenseCode._09A.code);
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._8.code);
		
		setAllNull(p.getPropertyDescription());
		setAllNull(p.getValueOfProperty());
		setAllNull(p.getDateRecovered());
		setAllNull(p.getSuspectedDrugType());
		setAllNull(p.getEstimatedDrugQuantity());
		setAllNull(p.getTypeDrugMeasurement());

		testRule252Common(p, TypeOfPropertyLossCode._8.code);
		testRule352DrugElements(p);
		
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._1.code);
		
		setAllNull(p.getPropertyDescription());
		setAllNull(p.getValueOfProperty());
		setAllNull(p.getDateRecovered());
		setAllNull(p.getSuspectedDrugType());
		setAllNull(p.getEstimatedDrugQuantity());
		setAllNull(p.getTypeDrugMeasurement());

		
		testRule252Common(p, TypeOfPropertyLossCode._1.code);
		testRule352DrugElements(p);
		
		setAllNull(p.getPropertyDescription());
		setAllNull(p.getValueOfProperty());
		setAllNull(p.getDateRecovered());
		setAllNull(p.getSuspectedDrugType());
		setAllNull(p.getEstimatedDrugQuantity());
		setAllNull(p.getTypeDrugMeasurement());

		o.setUcrOffenseCode(OffenseCode._35A.code);
		p.setSuspectedDrugType(0, SuspectedDrugTypeCode._A.code);
		testRule252Common(p, TypeOfPropertyLossCode._1.code);
		
		p.setSuspectedDrugType(0, null);
		Rule<PropertySegment> rule = rulesFactory.getRule352();
		NIBRSError e = rule.apply(p);
		assertNotNull(e);
		
	}

	@Test
	public void testRule383() {
		
		PropertySegment p = buildBaseSegment();
		GroupAIncidentReport incident = (GroupAIncidentReport) p.getParentReport();
		OffenseSegment o = new OffenseSegment();
		incident.addOffense(o);
		o.setUcrOffenseCode(OffenseCode._35A.code);
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._8.code);
		
		setAllNull(p.getPropertyDescription());
		setAllNull(p.getValueOfProperty());
		
		p.setPropertyDescription(0, PropertyDescriptionCode._10.code);
		
		Rule<PropertySegment> rule = rulesFactory.getRule383();
		NIBRSError e = rule.apply(p);
		assertNull(e);
		
		p.setValueOfProperty(0, new ParsedObject<>(33));
		e = rule.apply(p);
		assertNotNull(e);
		assertThat(e.getDataElementIdentifier(), is("16"));
		assertThat(e.getValue(), is(p.getValueOfProperty(0).getValue()));
		assertThat(e.getNIBRSErrorCode(), is(NIBRSErrorCode._383));

		OffenseSegment offense2 = new OffenseSegment();
		offense2.setUcrOffenseCode(OffenseCode._35B.code);
		incident.addOffense(offense2);
		e = rule.apply(p);
		assertNull(e);
		
	}
	
	@Test
	public void testRule372() {
		
		PropertySegment p = buildBaseSegment();
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._8.code);
		
		setAllNull(p.getPropertyDescription());
		setAllNull(p.getValueOfProperty());
		setAllNull(p.getDateRecovered());
		setAllNull(p.getSuspectedDrugType());
		setAllNull(p.getEstimatedDrugQuantity());
		setAllNull(p.getTypeDrugMeasurement());
		
		Rule<PropertySegment> rule = rulesFactory.getRule372();
		NIBRSError e = rule.apply(p);
		assertNull(e);
		
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._1.code);
		e = rule.apply(p);
		assertNull(e);
		
		for (String lossCode : TypeOfPropertyLossCode.requirePropertyDescriptionValueCodeSet()) {
			p.setTypeOfPropertyLoss(lossCode);
			e = rule.apply(p);
			assertNotNull(e);
			assertThat(e.getDataElementIdentifier(), is("14"));
			assertThat(e.getValue(), is(lossCode));
			assertThat(e.getNIBRSErrorCode(), is(NIBRSErrorCode._372));

		}
		
		p.setPropertyDescription(0, PropertyDescriptionCode._01.code);
		e = rule.apply(p);
		assertNotNull(e);
		assertThat(e.getDataElementIdentifier(), is("14"));
		assertThat(e.getValue(), is(TypeOfPropertyLossCode._7.code));
		assertThat(e.getNIBRSErrorCode(), is(NIBRSErrorCode._372));
		
		p.setValueOfProperty(0, new ParsedObject<Integer>(30));
		e = rule.apply(p);
		assertNull(e);
		setAllNull(p.getValueOfProperty());
		setAllNull(p.getPropertyDescription());
		
		p.setDateRecovered(0, new ParsedObject<LocalDate>(LocalDate.now()));
		e = rule.apply(p);
		assertNull(e);
		setAllNull(p.getDateRecovered());

		p.setSuspectedDrugType(0, SuspectedDrugTypeCode._A.code);
		e = rule.apply(p);
		assertNull(e);
		setAllNull(p.getSuspectedDrugType());

		p.setEstimatedDrugQuantity(0, new ParsedObject<Double>(Double.valueOf("33")));
		e = rule.apply(p);
		assertNull(e);
		setAllNull(p.getEstimatedDrugQuantity());
		
		p.setTypeDrugMeasurement(0 , TypeOfDrugMeasurementCode._DU.code);
		e = rule.apply(p);
		assertNull(e);
		setAllNull(p.getTypeDrugMeasurement());

		e = rule.apply(p);
		assertNotNull(e);
		assertThat(e.getDataElementIdentifier(), is("14"));
		assertThat(e.getValue(), is(p.getTypeOfPropertyLoss()));
		assertThat(e.getNIBRSErrorCode(), is(NIBRSErrorCode._372));
		
	}
	
	@Test
	public void testRule387() {
		
		Rule<PropertySegment> rule = rulesFactory.getRule387();
		PropertySegment p = buildBaseSegment();
		GroupAIncidentReport incident = (GroupAIncidentReport) p.getParentReport();
		OffenseSegment o = new OffenseSegment();
		incident.addOffense(o);
		o.setUcrOffenseCode(OffenseCode._35A.code);
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._6.code);
		
		setAllNull(p.getPropertyDescription());
		setAllNull(p.getValueOfProperty());
		setAllNull(p.getDateRecovered());
		setAllNull(p.getSuspectedDrugType());
		setAllNull(p.getEstimatedDrugQuantity());
		setAllNull(p.getTypeDrugMeasurement());
		
		NIBRSError e = rule.apply(p);
		assertNull(e);

		p.setPropertyDescription(0, PropertyDescriptionCode._11.code);
		e = rule.apply(p);
		assertNotNull(e);
		assertThat(e.getDataElementIdentifier(), is("15"));
		assertThat(e.getValue(), is("11"));
		assertThat(e.getNIBRSErrorCode(), is(NIBRSErrorCode._387));
		
		OffenseSegment _35B = new OffenseSegment();
		_35B.setUcrOffenseCode(OffenseCode._35B.code);
		incident.addOffense(_35B);
		e = rule.apply(p);
		assertNull(e);
		
		incident.removeOffense(0);
		e = rule.apply(p);
		assertNull(e);
		p.setPropertyDescription(0, PropertyDescriptionCode._10.code);
		e = rule.apply(p);
		assertNotNull(e);
		assertThat(e.getDataElementIdentifier(), is("15"));
		assertThat(e.getValue(), is("10"));
		assertThat(e.getNIBRSErrorCode(), is(NIBRSErrorCode._387));
		
	}
	
	private void testRule352DrugElements(PropertySegment p) {
		
		Rule<PropertySegment> rule = rulesFactory.getRule352();
		
		p.setSuspectedDrugType(0, SuspectedDrugTypeCode._A.code);
		NIBRSError e = rule.apply(p);
		assertNotNull(e);
		
		p.setSuspectedDrugType(0, null);
		p.setEstimatedDrugQuantity(0, new ParsedObject<Double>(10.0));
		e = rule.apply(p);
		assertNotNull(e);
		
		p.setEstimatedDrugQuantity(0, null);
		p.setTypeDrugMeasurement(0, TypeOfDrugMeasurementCode._DU.code);
		e = rule.apply(p);
		assertNotNull(e);
		
		p.setTypeDrugMeasurement(0, null);
		
	}

	private void testRule252Common(PropertySegment p, String expectedErrorValue) {
		
		Rule<PropertySegment> rule = rulesFactory.getRule352();
		
		NIBRSError e = rule.apply(p);
		assertNull(e);
		
		p.setPropertyDescription(0, PropertyDescriptionCode._01.code);
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(expectedErrorValue, e.getValue());
		assertEquals(NIBRSErrorCode._352, e.getNIBRSErrorCode());
		assertEquals("14", e.getDataElementIdentifier());
		
		p.setPropertyDescription(0, null);
		p.setValueOfProperty(0, new ParsedObject<>(10));
		e = rule.apply(p);
		assertNotNull(e);
		
		p.setValueOfProperty(0, null);
		p.setDateRecovered(0, new ParsedObject<>(LocalDate.now()));
		e = rule.apply(p);
		assertNotNull(e);
		p.setDateRecovered(0, null);
		
	}
	
	private static final void setAllNull(Object[] array) {
		for (int i=0;i < array.length;i++) {
			array[i] = null;
		}
	}
	
	@Test
	public void testRule391() {
		Rule<PropertySegment> rule = rulesFactory.getRule391();
		PropertySegment p = buildBaseSegment();
		for (int i=0;i < 10;i++) {
			p.setValueOfProperty(i, ParsedObject.getMissingParsedObject());
		}
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setValueOfProperty(0, new ParsedObject<>(10));
		p.setPropertyDescription(0, PropertyDescriptionCode._01.code);
		e = rule.apply(p);
		assertNull(e);
		p.setPropertyDescription(0, PropertyDescriptionCode._09.code);
		p.setValueOfProperty(0, new ParsedObject<>(0));
		e = rule.apply(p);
		assertNull(e);
		p.setValueOfProperty(0, new ParsedObject<>(10));
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("16", e.getDataElementIdentifier());
		assertEquals(10, e.getValue());
		assertEquals(NIBRSErrorCode._391, e.getNIBRSErrorCode());
		p.setPropertyDescription(1, PropertyDescriptionCode._99.code);
		p.setValueOfProperty(1, new ParsedObject<>(0));
		e = rule.apply(p);
		assertNotNull(e);
	}
	
	@Test
	public void testRule351() {
		Rule<PropertySegment> rule = rulesFactory.getRule351();
		PropertySegment p = buildBaseSegment();
		for (int i=0;i < 10;i++) {
			p.setValueOfProperty(i, ParsedObject.getMissingParsedObject());
		}
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setValueOfProperty(0, new ParsedObject<>(0));
		p.setPropertyDescription(0, PropertyDescriptionCode._09.code);
		e = rule.apply(p);
		assertNull(e);
		p.setPropertyDescription(0, PropertyDescriptionCode._01.code);
		p.setValueOfProperty(0, new ParsedObject<>(10));
		e = rule.apply(p);
		assertNull(e);
		p.setValueOfProperty(0, new ParsedObject<>(0));
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("16", e.getDataElementIdentifier());
		assertEquals("0", e.getValue());
		assertEquals(NIBRSErrorCode._351, e.getNIBRSErrorCode());
		p.setPropertyDescription(1, PropertyDescriptionCode._99.code);
		p.setValueOfProperty(1, new ParsedObject<>(0));
		e = rule.apply(p);
		assertNotNull(e);
	}
	
	@Test
	public void testRule356() {
		Rule<PropertySegment> rule = rulesFactory.getRule356();
		PropertySegment p = buildBaseSegment();
		for (int i=0;i < 10;i++) {
			p.setValueOfProperty(i, ParsedObject.getMissingParsedObject());
		}
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setPropertyDescription(0, PropertyDescriptionCode._09.code);
		e = rule.apply(p);
		assertNull(e);
		p.setDateRecovered(0, new ParsedObject<>(LocalDate.now()));
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("17", e.getDataElementIdentifier());
		assertNull(e.getValue());
		assertEquals(NIBRSErrorCode._356, e.getNIBRSErrorCode());
		
		
		p.setValueOfProperty(0, new ParsedObject<>(10));
		e = rule.apply(p);
		assertNull(e);
		
		p.setPropertyDescription(0, null);
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("17", e.getDataElementIdentifier());
		assertNull(e.getValue());
		assertEquals(NIBRSErrorCode._356, e.getNIBRSErrorCode());

	}
	
	@Test
	public void testRule342() {
		Rule<PropertySegment> rule = rulesFactory.getRule342();
		PropertySegment p = buildBaseSegment();
		for (int i=0;i < 10;i++) {
			p.setValueOfProperty(i, ParsedObject.getMissingParsedObject());
		}
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setValueOfProperty(0, new ParsedObject<>(10));
		e = rule.apply(p);
		assertNull(e);
		p.setValueOfProperty(1, new ParsedObject<>(10000000));
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("16", e.getDataElementIdentifier());
		assertEquals(Arrays.asList("010000000"), e.getValue());
		assertEquals(NIBRSErrorCode._342, e.getNIBRSErrorCode());
		assertTrue(e.isWarning());
	}
	
	@Test
	public void testRule306() {
		Rule<PropertySegment> rule = rulesFactory.getRule306();
		PropertySegment p = buildBaseSegment();
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setSuspectedDrugType(0, SuspectedDrugTypeCode._A.code);
		p.setTypeDrugMeasurement(0, TypeOfDrugMeasurementCode._DU.code);
		p.setSuspectedDrugType(1, SuspectedDrugTypeCode._B.code);
		p.setTypeDrugMeasurement(1, TypeOfDrugMeasurementCode._DU.code);
		e = rule.apply(p);
		assertNull(e);
		p.setSuspectedDrugType(1, SuspectedDrugTypeCode._A.code);
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("20", e.getDataElementIdentifier());
		assertEquals(SuspectedDrugTypeCode._A.code, e.getValue());
		assertEquals(NIBRSErrorCode._306, e.getNIBRSErrorCode());
		p.setTypeDrugMeasurement(1, TypeOfDrugMeasurementCode._FO.code);
		e = rule.apply(p);
		assertNull(e);
		p.setSuspectedDrugType(0, SuspectedDrugTypeCode._U.code);
		e = rule.apply(p);
		assertNull(e);
		p.setSuspectedDrugType(0, SuspectedDrugTypeCode._A.code);
		p.setSuspectedDrugType(1, SuspectedDrugTypeCode._U.code);
		e = rule.apply(p);
		assertNull(e);
	}
	
	@Test
	public void testRule320() {
		testThatRecoveredDateLaterThanIncidentDate(rulesFactory.getRule320(), NIBRSErrorCode._320);
	}
	
	@Test
	public void testRule305_forIncidentDate() {
		testThatRecoveredDateLaterThanIncidentDate(rulesFactory.getRule305(), NIBRSErrorCode._305);
	}

	private NIBRSError testThatRecoveredDateLaterThanIncidentDate(Rule<PropertySegment> rule, NIBRSErrorCode errorCode) {
		PropertySegment p = buildBaseSegment();
		GroupAIncidentReport parent = (GroupAIncidentReport) p.getParentReport();
		parent.setIncidentDate(ParsedObject.getMissingParsedObject());
		for (int i=0;i < 10;i++) {
			p.setDateRecovered(i, ParsedObject.getMissingParsedObject());
		}
		NIBRSError e = rule.apply(p);
		assertNull(e);
		ParsedObject<LocalDate> incidentDate = parent.getIncidentDate();
		incidentDate.setValue(LocalDate.of(2016, 1, 2));
		incidentDate.setMissing(false);
		incidentDate.setInvalid(false);
		parent.setIncidentDate(incidentDate);
		p.setDateRecovered(0, new ParsedObject<>(LocalDate.of(2016, 1, 2)));
		e = rule.apply(p);
		assertNull(e);
		p.setDateRecovered(0, new ParsedObject<>(LocalDate.of(2016, 1, 1)));
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("17", e.getDataElementIdentifier());
		assertEquals(LocalDate.of(2016, 1, 1), e.getValue());
		assertEquals(errorCode, e.getNIBRSErrorCode());
		p.setDateRecovered(1, new ParsedObject<>(LocalDate.of(2016, 1, 10)));
		e = rule.apply(p);
		assertNotNull(e);
		return e;
	}
	
	@Test
	public void testRule305_forSubmissionDate() {
		Rule<PropertySegment> rule = rulesFactory.getRule305();
		PropertySegment p = buildBaseSegment();
		GroupAIncidentReport parent = (GroupAIncidentReport) p.getParentReport();
		parent.setYearOfTape(null);
		parent.setMonthOfTape(null);
		for (int i=0;i < 10;i++) {
			p.setDateRecovered(i, ParsedObject.getMissingParsedObject());
		}
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setDateRecovered(0, new ParsedObject<>(LocalDate.of(2016, 1, 1)));
		parent.setYearOfTape(2016);
		parent.setMonthOfTape(1);
		e = rule.apply(p);
		assertNull(e);
		p.setDateRecovered(0, new ParsedObject<>(LocalDate.of(2016, 2, 1)));
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("17", e.getDataElementIdentifier());
		assertEquals(LocalDate.of(2016, 2, 1), e.getValue());
		assertEquals(NIBRSErrorCode._305, e.getNIBRSErrorCode());
		p.setDateRecovered(1, new ParsedObject<>(LocalDate.of(2016, 1, 1)));
		e = rule.apply(p);
		assertNotNull(e);
	}
	
	@Test
	public void testRule304_valueOfProperty() {
		Rule<PropertySegment> rule = rulesFactory.getRule304ForPropertyValue();
		PropertySegment p = buildBaseSegment();
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setValueOfProperty(0, new ParsedObject<>(10));
		e = rule.apply(p);
		assertNull(e);
		p.setValueOfProperty(0, new ParsedObject<>(-10));
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("16", e.getDataElementIdentifier());
		assertEquals(-10, e.getValue());
		assertEquals(NIBRSErrorCode._304, e.getNIBRSErrorCode());
		p.setValueOfProperty(0, new ParsedObject<>(1000000000));
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(1000000000, e.getValue());
		p.setValueOfProperty(1, new ParsedObject<>(10));
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(1000000000, e.getValue());
	}
	
	@Test
	public void testRule304_estimatedDrugQuantity() {
		Rule<PropertySegment> rule = rulesFactory.getRule304ForDrugQuantity();
		PropertySegment p = buildBaseSegment();
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setEstimatedDrugQuantity(0, new ParsedObject<>(10.0));
		e = rule.apply(p);
		assertNull(e);
		p.setEstimatedDrugQuantity(0, new ParsedObject<>(-10.0));
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("21", e.getDataElementIdentifier());
		assertEquals(-10.0, e.getValue());
		assertEquals(NIBRSErrorCode._304, e.getNIBRSErrorCode());
		p.setEstimatedDrugQuantity(0, new ParsedObject<>(10000000000.0));
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(10000000000.0, e.getValue());
		p.setEstimatedDrugQuantity(1, new ParsedObject<>(10.0));
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals(10000000000.0, e.getValue());
	}
	
	@Test
	public void testRule366() {
		Rule<PropertySegment> rule = rulesFactory.getRule366();
		PropertySegment p = buildBaseSegment();
		
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setEstimatedDrugQuantity(0, new ParsedObject<>(10.0));
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("21", e.getDataElementIdentifier());
		assertEquals(NIBRSErrorCode._366, e.getNIBRSErrorCode());
		
		p.setSuspectedDrugType(0, SuspectedDrugTypeCode._E.code);
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("21", e.getDataElementIdentifier());
		assertEquals(NIBRSErrorCode._366, e.getNIBRSErrorCode());
		
		p.setTypeDrugMeasurement(0, TypeOfDrugMeasurementCode._FO.code);;
		e = rule.apply(p);
		assertNull(e);
	}
	
	@Test
	public void testRule368() {
		Rule<PropertySegment> rule = rulesFactory.getRule368();
		PropertySegment p = buildBaseSegment();
		
		NIBRSError e = rule.apply(p);
		assertNull(e);
		
		p.setTypeDrugMeasurement(0, TypeOfDrugMeasurementCode._FO.code);;
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("22", e.getDataElementIdentifier());
		assertEquals(NIBRSErrorCode._368, e.getNIBRSErrorCode());
		
		p.setEstimatedDrugQuantity(0, new ParsedObject<>(10.0));
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("22", e.getDataElementIdentifier());
		assertEquals(NIBRSErrorCode._368, e.getNIBRSErrorCode());
		
		p.setSuspectedDrugType(0, SuspectedDrugTypeCode._E.code);
		e = rule.apply(p);
		assertNull(e);
		
		p.setEstimatedDrugQuantity(0, ParsedObject.getInvalidParsedObject());
		e = rule.apply(p);
		assertNull(e);
		
		p.setEstimatedDrugQuantity(0, ParsedObject.getMissingParsedObject());
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("22", e.getDataElementIdentifier());
		assertEquals(NIBRSErrorCode._368, e.getNIBRSErrorCode());
	}
	
	@Test
	public void testRule384() {
		Rule<PropertySegment> rule = rulesFactory.getRule384();
		PropertySegment p = buildBaseSegment();
		
		NIBRSError e = rule.apply(p);
		assertNull(e);
		
		p.setTypeDrugMeasurement(0, TypeOfDrugMeasurementCode._FO.code);;
		e = rule.apply(p);
		assertNull(e);
		
		p.setTypeDrugMeasurement(0, TypeOfDrugMeasurementCode._XX.code);;
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("21", e.getDataElementIdentifier());
		assertEquals(NIBRSErrorCode._384, e.getNIBRSErrorCode());
		
		p.setEstimatedDrugQuantity(0, new ParsedObject<>(10.0));
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("21", e.getDataElementIdentifier());
		assertEquals(NIBRSErrorCode._384, e.getNIBRSErrorCode());
		
		p.setEstimatedDrugQuantity(0, ParsedObject.getInvalidParsedObject());
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("21", e.getDataElementIdentifier());
		assertEquals(NIBRSErrorCode._384, e.getNIBRSErrorCode());
		
		p.setEstimatedDrugQuantity(0, ParsedObject.getMissingParsedObject());
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("21", e.getDataElementIdentifier());
		assertEquals(NIBRSErrorCode._384, e.getNIBRSErrorCode());
		
		p.setEstimatedDrugQuantity(0, new ParsedObject<>(1.0));
		e = rule.apply(p);
		assertNull(e);
	}
	
	@Test
	public void testRule304_stolenMotorVehicles() {
		Rule<PropertySegment> rule = rulesFactory.getRule304ForStolenMotorVehicleCount();
		PropertySegment p = buildBaseSegment();
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setNumberOfStolenMotorVehicles(new ParsedObject<>(2));
		e = rule.apply(p);
		assertNull(e);
		p.setNumberOfStolenMotorVehicles(new ParsedObject<>(-2));
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("18", e.getDataElementIdentifier());
		assertEquals(-2, e.getValue());
		p.setNumberOfStolenMotorVehicles(new ParsedObject<>(200));
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("18", e.getDataElementIdentifier());
		assertEquals(200, e.getValue());
		assertEquals(NIBRSErrorCode._304, e.getNIBRSErrorCode());
	}
	
	@Test
	public void testRule304_recoveredMotorVehicles() {
		Rule<PropertySegment> rule = rulesFactory.getRule304ForRecoveredMotorVehicleCount();
		PropertySegment p = buildBaseSegment();
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setNumberOfRecoveredMotorVehicles(new ParsedObject<>(2));
		e = rule.apply(p);
		assertNull(e);
		p.setNumberOfRecoveredMotorVehicles(new ParsedObject<>(-2));
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("19", e.getDataElementIdentifier());
		assertEquals(-2, e.getValue());
		assertEquals(NIBRSErrorCode._304, e.getNIBRSErrorCode());
		p.setNumberOfRecoveredMotorVehicles(new ParsedObject<>(200));
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("19", e.getDataElementIdentifier());
		assertEquals(200, e.getValue());
		assertEquals(NIBRSErrorCode._304, e.getNIBRSErrorCode());
	}
	
	@Test
	public void testRule304_typeDrugMeasurement() {
		Rule<PropertySegment> rule = rulesFactory.getRule304ForListBoundElement("typeDrugMeasurement", "22", PropertyDescriptionCode.codeSet());
		PropertySegment p = buildBaseSegment();
		NIBRSError e = rule.apply(p);
		assertNull(e);
		for (int i=0;i < 3;i++) {
			p.setTypeDrugMeasurement(i, null);
		}
		e = rule.apply(p);
		assertNull(e);
		p.setTypeDrugMeasurement(0, "XXX");
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("22", e.getDataElementIdentifier());
		assertArrayEquals(getStringArrayWithValues(3, "XXX"), (String[]) e.getValue());
		assertEquals(NIBRSErrorCode._304, e.getNIBRSErrorCode());
		p.setTypeDrugMeasurement(1, TypeOfDrugMeasurementCode._OZ.code);
		e = rule.apply(p);
		assertNotNull(e);
		assertArrayEquals(getStringArrayWithValues(3, "XXX", TypeOfDrugMeasurementCode._OZ.code), (String[]) e.getValue());
		assertEquals(NIBRSErrorCode._304, e.getNIBRSErrorCode());
	}
	
	@Test
	public void testRule304_suspectedDrugType() {
		Rule<PropertySegment> rule = rulesFactory.getRule304ForListBoundElement("suspectedDrugType", "20", PropertyDescriptionCode.codeSet());
		PropertySegment p = buildBaseSegment();
		NIBRSError e = rule.apply(p);
		assertNull(e);
		for (int i=0;i < 3;i++) {
			p.setSuspectedDrugType(i, null);
		}
		e = rule.apply(p);
		assertNull(e);
		p.setSuspectedDrugType(0, "XXX");
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("20", e.getDataElementIdentifier());
		assertArrayEquals(getStringArrayWithValues(3, "XXX"), (String[]) e.getValue());
		assertEquals(NIBRSErrorCode._304, e.getNIBRSErrorCode());
		p.setSuspectedDrugType(1, SuspectedDrugTypeCode._A.code);
		e = rule.apply(p);
		assertNotNull(e);
		assertArrayEquals(getStringArrayWithValues(3, "XXX", SuspectedDrugTypeCode._A.code), (String[]) e.getValue());
		assertEquals(NIBRSErrorCode._304, e.getNIBRSErrorCode());
	}
	
	@Test
	public void testRule304_propertyDescription() {
		Rule<PropertySegment> rule = rulesFactory.getRule304ForListBoundElement("propertyDescription", "15", PropertyDescriptionCode.codeSet());
		PropertySegment p = buildBaseSegment();
		NIBRSError e = rule.apply(p);
		assertNull(e);
		for (int i=0;i < 10;i++) {
			p.setPropertyDescription(i, null);
		}
		e = rule.apply(p);
		assertNull(e);
		p.setPropertyDescription(0, "XXX");
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("15", e.getDataElementIdentifier());
		assertArrayEquals(getStringArrayWithValues(10, "XXX"), (String[]) e.getValue());
		assertEquals(NIBRSErrorCode._304, e.getNIBRSErrorCode());
		p.setPropertyDescription(1, PropertyDescriptionCode._01.code);
		e = rule.apply(p);
		assertNotNull(e);
		assertArrayEquals(getStringArrayWithValues(10, "XXX", PropertyDescriptionCode._01.code), (String[]) e.getValue());
		assertEquals(NIBRSErrorCode._304, e.getNIBRSErrorCode());
	}
	
	@Test
	public void testRule375() {
		Rule<PropertySegment> rule = rulesFactory.getRule375();
		PropertySegment p = buildBaseSegment();
		NIBRSError e = rule.apply(p);
		assertNull(e);
		
		for (int i=0;i < 10;i++) {
			p.setPropertyDescription(i, null);
		}
		e = rule.apply(p);
		assertNull(e);
		
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._1.code);
		e = rule.apply(p);
		assertNull(e);
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._8.code);
		e = rule.apply(p);
		assertNull(e);
		
		for (String typeOfPropertyLossCode: TypeOfPropertyLossCode.requirePropertyDescriptionValueCodeSet()){
			p.setTypeOfPropertyLoss(typeOfPropertyLossCode);
			e = rule.apply(p);
			assertNotNull(e);
			assertEquals('3', e.getSegmentType());
			assertEquals("15", e.getDataElementIdentifier());
			assertEquals(NIBRSErrorCode._375, e.getNIBRSErrorCode());
		}
		
		p.setPropertyDescription(0, "18");
		e = rule.apply(p);
		assertNull(e);
	}
	
	@Test
	public void testRule304_typeOfPropertyLoss() {
		Rule<PropertySegment> rule = rulesFactory.getRule304ForTypePropertyLoss();
		PropertySegment p = buildBaseSegment();
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setTypeOfPropertyLoss(null);
		e = rule.apply(p);
		assertNull(e);

		p.setTypeOfPropertyLoss("9");
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("14", e.getDataElementIdentifier());
		assertEquals(NIBRSErrorCode._304, e.getNIBRSErrorCode());
		assertEquals("9", e.getValue());
	}
	
	@Test
	public void testRule301_typeOfPropertyLoss() {
		Rule<PropertySegment> rule = rulesFactory.getRule301ForTypePropertyLoss();
		PropertySegment p = buildBaseSegment();
		
		GroupAIncidentReport incident = (GroupAIncidentReport) p.getParentReport();
		OffenseSegment o = new OffenseSegment();
		incident.addOffense(o);
		o.setUcrOffenseCode(null);

		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setTypeOfPropertyLoss(null);
		e = rule.apply(p);
		assertNull(e);
		p.setTypeOfPropertyLoss("");
		e = rule.apply(p);
		assertNull(e);
		
		o.setUcrOffenseCode("100");
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("14", e.getDataElementIdentifier());
		assertEquals(NIBRSErrorCode._301, e.getNIBRSErrorCode());
		assertThat(e.getValue(), is(""));
		
		p.setTypeOfPropertyLoss("1");
		e = rule.apply(p);
		assertNull(e);
	}
	
	private PropertySegment buildBaseSegment() {
		GroupAIncidentReport report = new GroupAIncidentReport();
		ReportSource source = new ReportSource();
		source.setSourceLocation(getClass().getName());
		source.setSourceName(getClass().getName());
		report.setSource(source);
		PropertySegment p = new PropertySegment();
		p.setTypeOfPropertyLoss(TypeOfPropertyLossCode._1.code);
		report.addProperty(p);
		return p;
	}

	@Test
	public void testRule388() {
		Rule<PropertySegment> rule = rulesFactory.getRule388();
		PropertySegment p = buildBaseSegment();
		NIBRSError e = rule.apply(p);
		assertNull(e);
		p.setNumberOfStolenMotorVehicles(new ParsedObject<>(1));
		e = rule.apply(p);
		assertNull(e);
		p.setPropertyDescription(0, "03");
		p.setPropertyDescription(1, "05");
		e = rule.apply(p);
		assertNotNull(e);
		assertEquals('3', e.getSegmentType());
		assertEquals("18", e.getDataElementIdentifier());
		assertEquals("01", e.getValue().toString());
		assertEquals(NIBRSErrorCode._388, e.getNIBRSErrorCode());
	}
	
}
