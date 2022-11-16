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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;

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

public class XmlIncidentBuilderGroupBReportTest {
	private DefaultReportListener incidentListener;
	
	@Before
	public void setUp() throws Exception {
		InputStream inputStream = new FileInputStream(new File("src/test/resources/iep-sample/nibrs_GroupBArrest_Sample.xml"));
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
		assertThat(groupBArrestReport.getCityIndicator(), is("GAA7"));
		assertEquals('7', groupBArrestReport.getAdminSegmentLevel());
		assertEquals('A', groupBArrestReport.getReportActionType());
		assertEquals(Integer.valueOf(2), groupBArrestReport.getMonthOfTape());
		assertEquals(Integer.valueOf(2016), groupBArrestReport.getYearOfTape());
		
		assertThat(groupBArrestReport.getArresteeCount(), is(1));
		
		ArresteeSegment arrestee = groupBArrestReport.getArrestees().get(0);
		assertThat(arrestee.getAge().getAgeMin(), is(30));
		assertThat(arrestee.getRace(), is("W"));
		assertThat(arrestee.getEthnicity(), is("N"));
		assertThat(arrestee.getResidentStatus(), is("R"));
		assertThat(arrestee.getSex(), is("M"));
		assertThat(arrestee.getDispositionOfArresteeUnder18(), is("H"));
		assertThat(arrestee.getArresteeSequenceNumber().getValue(), is(1));
		
		assertThat(arrestee.getArrestDate().getValue(), is(LocalDate.of(2016, 2, 28)));
		assertThat(arrestee.getArrestTransactionNumber(), is("12345"));
		assertThat(arrestee.getTypeOfArrest(), is("O"));
		assertThat(arrestee.getUcrArrestOffenseCode(), is("64A"));
		
		assertThat(arrestee.getArresteeArmedWith(0), is("12"));
	}

}
