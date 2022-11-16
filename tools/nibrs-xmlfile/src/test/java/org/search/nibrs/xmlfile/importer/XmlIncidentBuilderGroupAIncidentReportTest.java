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
package org.search.nibrs.xmlfile.importer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.search.nibrs.common.NIBRSError;
import org.search.nibrs.importer.DefaultReportListener;
import org.search.nibrs.model.ArresteeSegment;
import org.search.nibrs.model.GroupAIncidentReport;
import org.search.nibrs.model.GroupBArrestReport;
import org.search.nibrs.model.OffenderSegment;
import org.search.nibrs.model.OffenseSegment;
import org.search.nibrs.model.PropertySegment;
import org.search.nibrs.model.VictimSegment;
import org.search.nibrs.model.ZeroReport;
import org.search.nibrs.model.codes.AutomaticWeaponIndicatorCode;
import org.search.nibrs.xmlfile.NibrsXmlFileProperties;

public class XmlIncidentBuilderGroupAIncidentReportTest {
	
	@SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(XmlIncidentBuilderGroupAIncidentReportTest.class);
	
	private DefaultReportListener incidentListener;
	
	@Before
	public void setUp() throws Exception {
		InputStream inputStream = new FileInputStream(new File("src/test/resources/iep-sample/nibrs_AllFields_Sample.xml"));
		incidentListener = new DefaultReportListener();
		
		XmlIncidentBuilder incidentBuilder = new XmlIncidentBuilder( new NibrsXmlFileProperties());
		incidentBuilder.addIncidentListener(incidentListener);
		incidentBuilder.buildIncidents(inputStream, getClass().getName());
		List<NIBRSError> errorList = incidentListener.getErrorList();
		assertEquals(0, errorList.size());
	}

	@Test
	public void test() {
		List<GroupBArrestReport> groupBArrestReports = incidentListener.getGroupBIncidentList();
		assertThat(groupBArrestReports.size(), is(0));
		List<ZeroReport> zeroIncidentList = incidentListener.getZeroReportList();
		assertEquals(0, zeroIncidentList.size());
		
		List<GroupAIncidentReport> groupAIncidentList = incidentListener.getGroupAIncidentList();
		assertThat(groupAIncidentList.size(), is(1));
		
		GroupAIncidentReport groupAIncident = groupAIncidentList.get(0); 
		assertNotNull(groupAIncident);
		
		
		assertThat(groupAIncident.getFederalJucicialDistrictCode(), is("002"));
		assertThat(groupAIncident.getAdminSegmentLevel(), is('1'));
		assertThat(groupAIncident.getIncidentNumber(), is("54236732")); 
		assertThat(groupAIncident.getOri(), is("WVNDX0100"));
		assertThat(groupAIncident.getCityIndicator(), is("GAA7"));
		assertThat(groupAIncident.getIncidentDate().getValue(), is(LocalDate.of(2016, 2, 19)));
		assertThat(groupAIncident.getIncidentHour().getValue(), is(Integer.valueOf(10)));
		assertThat(groupAIncident.getReportDateIndicator(), is(nullValue()));
		assertThat(groupAIncident.getCargoTheftIndicator(), is("Y"));
		
		assertThat(groupAIncident.getExceptionalClearanceCode(), is("A"));
		assertThat(groupAIncident.getExceptionalClearanceDate().getValue(), is(LocalDate.of(2016, 2, 25)));
		
		assertThat(groupAIncident.getOffenseCount(), is(1));
		OffenseSegment offenseSegment = groupAIncident.getOffenses().get(0);
		assertThat(offenseSegment.getUcrOffenseCode(), is("240"));
		assertThat(offenseSegment.getTypeOfCriminalActivity()[0], is("N"));
		assertThat(offenseSegment.getBiasMotivation()[0], is("88"));
		assertThat(offenseSegment.getNumberOfPremisesEntered().getValue(), is(1));
		assertThat(offenseSegment.getOffendersSuspectedOfUsing()[0], is("N"));
		assertThat(offenseSegment.getMethodOfEntry(), is("F"));
		assertThat(offenseSegment.getTypeOfWeaponForceInvolved(0), is("11"));
		assertThat(offenseSegment.getAutomaticWeaponIndicator(0), is("A"));
		assertThat(offenseSegment.getOffenseAttemptedIndicator(), is(true));
		assertThat(offenseSegment.getOffenseAttemptedCompleted(), is("A"));
		assertThat(offenseSegment.getLocationType(), is("58"));
		assertThat(offenseSegment.getPopulatedBiasMotivationCount(), is(1));
		assertThat(offenseSegment.getPopulatedTypeOfCriminalActivityCount(), is(1));
		assertThat(offenseSegment.getPopulatedOffendersSuspectedOfUsingCount(), is(1));
		assertThat(offenseSegment.getPopulatedTypeOfWeaponForceInvolvedCount(), is(1));
		
		assertThat(groupAIncident.getOffenderCount(), is(1));
		OffenderSegment offenderSegment = groupAIncident.getOffenders().get(0);
		assertThat(offenderSegment.getOffenderSequenceNumber().getValue(), is(1));
		assertThat(offenderSegment.getAge().getAgeMin(), is(25));
		assertThat(offenderSegment.getAge().getAgeMax(), is(30));
		assertThat(offenderSegment.getEthnicity(), is("N"));
		assertThat(offenderSegment.getRace(), is("W"));
		assertThat(offenderSegment.getSex(), is("F"));

		assertThat(groupAIncident.getVictimCount(), is(2));
		VictimSegment victimSegment1 = groupAIncident.getVictims().get(0);
		assertThat(victimSegment1.getVictimSequenceNumber().getValue(), is(1));
		assertThat(victimSegment1.getTypeOfInjury()[0], is("N"));
		assertThat(victimSegment1.getTypeOfVictim(), is("L"));
		assertThat(victimSegment1.getAggravatedAssaultHomicideCircumstances(0), is("01"));
		assertThat(victimSegment1.getAdditionalJustifiableHomicideCircumstances(), is("C"));
		assertThat(victimSegment1.getAge().getAgeMax(), is(32));
		assertThat(victimSegment1.getAge().getAgeMin(), is(32));
		assertThat(victimSegment1.getEthnicity(), is("N"));
		assertThat(victimSegment1.getRace(), is("B"));
		assertThat(victimSegment1.getResidentStatus(), is("R"));
		assertThat(victimSegment1.getSex(), is("M"));
		
		assertThat(victimSegment1.getOffenderNumberRelated(0).getValue(), is(1));
		assertThat(victimSegment1.getPopulatedOffenderNumberRelatedCount(), is(1));
		assertThat(victimSegment1.getVictimOffenderRelationship(0), is("AQ"));
		assertThat(victimSegment1.getUcrOffenseCodeConnection(0), is("240")); 
		assertThat(victimSegment1.getPopulatedUcrOffenseCodeConnectionCount(), is(1));
		
		assertThat(victimSegment1.getOfficerOtherJurisdictionORI(), is("WVNDX01"));
		assertThat(victimSegment1.getOfficerAssignmentType(), is("G"));
		assertThat(victimSegment1.getTypeOfOfficerActivityCircumstance(), is("10"));

		VictimSegment victimSegment2 = groupAIncident.getVictims().get(1);
		assertThat(victimSegment2.getVictimSequenceNumber().getValue(), is(2));
		assertThat(victimSegment2.getTypeOfInjury()[0], is("N"));
		assertThat(victimSegment2.getTypeOfVictim(), is("I"));
		assertThat(victimSegment2.getAggravatedAssaultHomicideCircumstances(0), is("10"));
		assertThat(victimSegment2.getAdditionalJustifiableHomicideCircumstances(), is("G"));

		assertThat(victimSegment2.getAge().getAgeMax(), is(0));
		assertThat(victimSegment2.getAge().getAgeMin(), is(0));
		assertThat(victimSegment2.getAge().getNonNumericAge(), is("BB"));
		assertThat(victimSegment2.getEthnicity(), is("U"));
		assertThat(victimSegment2.getRace(), is("W"));
		assertThat(victimSegment2.getResidentStatus(), is("R"));
		assertThat(victimSegment2.getSex(), is("M"));
		
		assertThat(victimSegment2.getOffenderNumberRelated(0).getValue(), is(1));
		assertThat(victimSegment2.getPopulatedOffenderNumberRelatedCount(), is(1));
		assertThat(victimSegment2.getVictimOffenderRelationship(0), is("ST"));
		assertThat(victimSegment2.getUcrOffenseCodeConnection(0), is("240")); 
		assertThat(victimSegment2.getPopulatedUcrOffenseCodeConnectionCount(), is(1));

		assertThat(groupAIncident.getArresteeCount(), is(1));
		ArresteeSegment arrestee = groupAIncident.getArrestees().get(0);
		assertThat(arrestee.getAge().getAgeMin(), is(25));
		assertThat(arrestee.getRace(), is("W"));
		assertThat(arrestee.getEthnicity(), is("N"));
		assertThat(arrestee.getResidentStatus(), is("R"));
		assertThat(arrestee.getSex(), is("F"));
		assertThat(arrestee.getDispositionOfArresteeUnder18(), is("H"));
		assertThat(arrestee.getArresteeSequenceNumber().getValue(), is(1));
		
		assertThat(arrestee.getArrestDate().getValue(), is(LocalDate.of(2016, 2, 28)));
		assertThat(arrestee.getArrestTransactionNumber(), is("12345"));
		assertThat(arrestee.getTypeOfArrest(), is("O"));
		assertThat(arrestee.getUcrArrestOffenseCode(), is("64A"));
		
		assertThat(arrestee.getArresteeArmedWith(0), is("12"));
		assertThat(arrestee.getAutomaticWeaponIndicator(0), is(AutomaticWeaponIndicatorCode._blank.code));
		assertThat(arrestee.getArresteeArmedWith(1), is("13"));
		assertThat(arrestee.getAutomaticWeaponIndicator(1), is("A"));
		assertThat(arrestee.getMultipleArresteeSegmentsIndicator(), is("N"));
		
		assertThat(groupAIncident.getPropertyCount(), is(5));
		List<PropertySegment> propertySegments = groupAIncident.getProperties(); 
		assertThat(propertySegments.get(0).getTypeOfPropertyLoss(), is("1"));
		assertThat(propertySegments.get(0).getPopulatedPropertyDescriptionCount(), is(1));
		assertThat(propertySegments.get(0).getPropertyDescription(0), is("01"));
		assertThat(propertySegments.get(0).getValueOfProperty(0).getValue(), is(12001));
		assertThat(propertySegments.get(0).getDateRecovered(0).getValue(), is(LocalDate.of(2016, 2, 21)));
		assertThat(propertySegments.get(0).getPopulatedSuspectedDrugTypeCount(), is(0));
		
		assertThat(propertySegments.get(1).getTypeOfPropertyLoss(), is("2"));
		assertThat(propertySegments.get(1).getPopulatedPropertyDescriptionCount(), is(1));
		assertThat(propertySegments.get(1).getPropertyDescription(0), is("01"));
		assertThat(propertySegments.get(1).getValueOfProperty(0).getValue(), is(12002));
		assertThat(propertySegments.get(1).getDateRecovered(0).getValue(), is(LocalDate.of(2016, 2, 22)));
		assertThat(propertySegments.get(1).getPopulatedSuspectedDrugTypeCount(), is(0));
		
		assertThat(propertySegments.get(2).getTypeOfPropertyLoss(), is("3"));
		assertThat(propertySegments.get(2).getPopulatedPropertyDescriptionCount(), is(1));
		assertThat(propertySegments.get(2).getPropertyDescription(0), is("01"));
		assertThat(propertySegments.get(2).getValueOfProperty(0).getValue(), is(12003));
		assertThat(propertySegments.get(2).getDateRecovered(0).getValue(), is(LocalDate.of(2016, 2, 23)));
		assertThat(propertySegments.get(2).getPopulatedSuspectedDrugTypeCount(), is(0));
		assertThat(propertySegments.get(2).getNumberOfRecoveredMotorVehicles().isMissing(), is(true));
		assertThat(propertySegments.get(2).getNumberOfStolenMotorVehicles().isMissing(), is(true));
		
		assertThat(propertySegments.get(3).getTypeOfPropertyLoss(), is("4"));
		assertThat(propertySegments.get(3).getPopulatedPropertyDescriptionCount(), is(1));
		assertThat(propertySegments.get(3).getPropertyDescription(0), is("01"));
		assertThat(propertySegments.get(3).getValueOfProperty(0).getValue(), is(12004));
		assertThat(propertySegments.get(3).getDateRecovered(0).getValue(), is(LocalDate.of(2016, 2, 24)));
		assertThat(propertySegments.get(3).getPopulatedSuspectedDrugTypeCount(), is(0));
		assertThat(propertySegments.get(3).getNumberOfRecoveredMotorVehicles().isMissing(), is(true));
		assertThat(propertySegments.get(3).getNumberOfStolenMotorVehicles().isMissing(), is(true));
		
		assertThat(propertySegments.get(4).getTypeOfPropertyLoss(), is("5"));
		assertThat(propertySegments.get(4).getPopulatedPropertyDescriptionCount(), is(4));
		assertThat(propertySegments.get(4).getPropertyDescription(0), is("01"));
		assertThat(propertySegments.get(4).getValueOfProperty(0).getValue(), is(12005));
		assertThat(propertySegments.get(4).getDateRecovered(0).getValue(), is(LocalDate.of(2016, 2, 25)));
		assertThat(propertySegments.get(4).getPropertyDescription(1), is("05"));
		assertThat(propertySegments.get(4).getValueOfProperty(1).getValue(), is(12006));
		assertThat(propertySegments.get(4).getDateRecovered(1).getValue(), is(LocalDate.of(2016, 2, 26)));
		assertThat(propertySegments.get(4).getPropertyDescription(2), is("03"));
		assertThat(propertySegments.get(4).getValueOfProperty(2).getValue(), is(12007));
		assertThat(propertySegments.get(4).getDateRecovered(2).getValue(), is(LocalDate.of(2016, 2, 27)));
		assertThat(propertySegments.get(4).getPropertyDescription(3), is("10"));
		assertThat(propertySegments.get(4).getPopulatedPropertyDescriptionCount(), is(4));
		assertThat(propertySegments.get(4).getValueOfProperty(3).getValue(), is(12008));
		assertThat(propertySegments.get(4).getDateRecovered(3).getValue(), is(LocalDate.of(2016, 2, 28)));
		assertThat(propertySegments.get(4).getPopulatedSuspectedDrugTypeCount(), is(1));
		assertThat(propertySegments.get(4).getSuspectedDrugType(0), is("X"));
		assertThat(propertySegments.get(4).getEstimatedDrugQuantity(0).getValue(), is(Double.valueOf("1.5")));
		assertThat(propertySegments.get(4).getTypeDrugMeasurement(0), is("XX"));
		assertThat(propertySegments.get(4).getNumberOfRecoveredMotorVehicles().getValue(), is(45));
		assertThat(propertySegments.get(4).getNumberOfRecoveredMotorVehicles().isMissing(), is(false));
		assertThat(propertySegments.get(4).getNumberOfRecoveredMotorVehicles().isInvalid(), is(false));
		assertThat(propertySegments.get(4).getNumberOfStolenMotorVehicles().isMissing(), is(true));
		
	}

}
