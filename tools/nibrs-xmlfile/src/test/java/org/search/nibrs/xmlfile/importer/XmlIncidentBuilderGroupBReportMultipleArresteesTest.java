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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.search.nibrs.common.NIBRSError;
import org.search.nibrs.importer.DefaultReportListener;
import org.search.nibrs.model.ArresteeSegment;
import org.search.nibrs.model.GroupAIncidentReport;
import org.search.nibrs.model.GroupBArrestReport;
import org.search.nibrs.model.ZeroReport;
import org.search.nibrs.xmlfile.NibrsXmlFileProperties;

public class XmlIncidentBuilderGroupBReportMultipleArresteesTest {
	private DefaultReportListener incidentListener;
	
	@Before
	public void setUp() throws Exception {
		InputStream inputStream = new FileInputStream(new File("src/test/resources/iep-sample/nibrs_GroupBArrest_Sample_multipleArrestees.xml"));
		incidentListener = new DefaultReportListener();
		
		XmlIncidentBuilder incidentBuilder = new XmlIncidentBuilder(new NibrsXmlFileProperties());
		incidentBuilder.addIncidentListener(incidentListener);
		incidentBuilder.buildIncidents(inputStream, getClass().getName());
		List<NIBRSError> errorList = incidentListener.getErrorList();
		assertEquals(0, errorList.size());
	}

	@Test
	public void test() {
		List<GroupAIncidentReport> groupAIncidentList = incidentListener.getGroupAIncidentList();
		assertEquals(0, groupAIncidentList.size());
		List<ZeroReport> zeroIncidentList = incidentListener.getZeroReportList();
		assertEquals(0, zeroIncidentList.size());
		
		List<GroupBArrestReport> groupBArrestReports = incidentListener.getGroupBIncidentList();
		assertEquals(1, groupBArrestReports.size());
		
		GroupBArrestReport groupBArrestReport = groupBArrestReports.get(0); 
		assertNotNull(groupBArrestReport);
		assertEquals("12345", groupBArrestReport.getIdentifier());
		assertEquals("WVNDX0100", groupBArrestReport.getOri());
		assertThat(groupBArrestReport.getCityIndicator(), equalTo("GAA7"));
		assertEquals('7', groupBArrestReport.getAdminSegmentLevel());
		assertEquals('A', groupBArrestReport.getReportActionType());
		assertEquals(Integer.valueOf(2), groupBArrestReport.getMonthOfTape());
		assertEquals(Integer.valueOf(2016), groupBArrestReport.getYearOfTape());
		
		assertThat(groupBArrestReport.getArresteeCount(), equalTo(2));
		
		ArresteeSegment arrestee = groupBArrestReport.getArrestees().get(0);
		assertThat(arrestee.getAge().getAgeMin(), equalTo(30));
		assertThat(arrestee.getRace(), equalTo("W"));
		assertThat(arrestee.getEthnicity(), equalTo("N"));
		assertThat(arrestee.getResidentStatus(), equalTo("R"));
		assertThat(arrestee.getSex(), equalTo("M"));
		assertThat(arrestee.getDispositionOfArresteeUnder18(), equalTo("H"));
		assertThat(arrestee.getArresteeSequenceNumber().getValue(), equalTo(1));
		
		assertThat(arrestee.getArrestDate().getValue(), equalTo(LocalDate.of(2016, 2, 28)));
		assertThat(arrestee.getArrestTransactionNumber(), equalTo("12345"));
		assertThat(arrestee.getTypeOfArrest(), equalTo("O"));
		assertThat(arrestee.getUcrArrestOffenseCode(), equalTo("64A"));
		
		assertThat(arrestee.getArresteeArmedWith(0), equalTo("12"));
		
		ArresteeSegment arrestee1 = groupBArrestReport.getArrestees().get(1);
		assertThat(arrestee1.getAge().getAgeMin(), equalTo(30));
		assertThat(arrestee1.getRace(), equalTo("W"));
		assertThat(arrestee1.getEthnicity(), equalTo("N"));
		assertThat(arrestee1.getResidentStatus(), equalTo("R"));
		assertThat(arrestee1.getSex(), equalTo("M"));
		assertThat(arrestee1.getDispositionOfArresteeUnder18(), equalTo("H"));
		assertThat(arrestee1.getArresteeSequenceNumber().getValue(), equalTo(2));
		
		assertThat(arrestee1.getArrestDate().getValue(), equalTo(LocalDate.of(2016, 2, 28)));
		assertThat(arrestee1.getArrestTransactionNumber(), equalTo("12345"));
		assertThat(arrestee1.getTypeOfArrest(), equalTo("O"));
		assertThat(arrestee1.getUcrArrestOffenseCode(), equalTo("64A"));
		
		assertThat(arrestee1.getArresteeArmedWith(0), equalTo("12"));
	}

}
