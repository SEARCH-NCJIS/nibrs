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
package org.search.nibrs.stagingdata.repository.segment;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.search.nibrs.model.GroupBArrestReport;
import org.search.nibrs.stagingdata.model.segment.ArrestReportSegment;
import org.search.nibrs.stagingdata.service.ArrestReportService;
import org.search.nibrs.stagingdata.util.BaselineIncidentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ArrestReportSegmentRepositoryTest {
	@SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(ArrestReportSegmentRepositoryTest.class);

	@Autowired
	public ArrestReportSegmentRepository arrestReportSegmentRepository; 
	@Autowired
	public ArrestReportService arrestReportService;
	
	@Before
	public void setup() {
		GroupBArrestReport groupBArrestReport = BaselineIncidentFactory.getBaselineGroupBArrestReport();
		arrestReportService.saveGroupBArrestReports(Arrays.asList(groupBArrestReport));
	}
	
	@Test
	@DirtiesContext
	public void testFindIdsByOriAndArrestDateAndFindAll() {
		List<Integer> arrestReportSegmentIds = arrestReportSegmentRepository
				.findIdsByOriAndArrestDate("agencyORI", 2017, 5, null);
		
		List<ArrestReportSegment> arrestReportSegments = arrestReportSegmentRepository
				.findAllById(arrestReportSegmentIds).stream().distinct().collect(Collectors.toList());
		
		assertThat(arrestReportSegmentIds.size(), equalTo(1));
		List<String> arrestTransactionNumbers = arrestReportSegments.stream()
				.map(ArrestReportSegment::getArrestTransactionNumber)
				.collect(Collectors.toList()); 
		assertTrue(arrestTransactionNumbers.containsAll(Arrays.asList("12345")));
		
		arrestReportSegmentIds = arrestReportSegmentRepository
				.findIdsByOriAndArrestDate("agencyORI", 2017, 6, null);
		assertThat(arrestReportSegmentIds.size(), equalTo(0));
		
	}
	
	@Test
	@DirtiesContext
	public void testCountByOriListAndSubmissionDateRange() {
		long count = arrestReportSegmentRepository
				.countByOriListAndSubmissionDateRange(null, null, null, null);
		
		assertThat(count, equalTo(1l));
	}
	
}
