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
package org.search.nibrs.stagingdata.service.xml;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.search.nibrs.model.GroupAIncidentReport;
import org.search.nibrs.model.GroupBArrestReport;
import org.search.nibrs.stagingdata.model.segment.AdministrativeSegment;
import org.search.nibrs.stagingdata.model.segment.ArrestReportSegment;
import org.search.nibrs.stagingdata.repository.segment.AdministrativeSegmentRepository;
import org.search.nibrs.stagingdata.repository.segment.ArrestReportSegmentRepository;
import org.search.nibrs.stagingdata.service.AdministrativeSegmentFactory;
import org.search.nibrs.stagingdata.service.ArrestReportSegmentFactory;
import org.search.nibrs.stagingdata.service.ArrestReportService;
import org.search.nibrs.stagingdata.service.GroupAIncidentService;
import org.search.nibrs.stagingdata.util.BaselineIncidentFactory;
import org.search.nibrs.xml.NibrsNamespaceContext.Namespace;
import org.search.nibrs.xml.XmlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Document;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.ByNameAndTextRecSelector;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelector;
import org.xmlunit.diff.ElementSelectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class XmlReportGeneratorTest {
	private static final Log log = LogFactory.getLog(XmlReportGeneratorTest.class);

	@Autowired
	public AdministrativeSegmentRepository administrativeSegmentRepository; 
	@Autowired
	public ArrestReportSegmentRepository arrestReportSegmentRepository; 
	@Autowired
	public AdministrativeSegmentFactory administrativeSegmentFactory; 
	@Autowired
	public ArrestReportSegmentFactory arrestReportSegmentFactory; 
	@Autowired
	public GroupAIncidentService groupAIncidentService;
	@Autowired
	public ArrestReportService arrestReportService;
	@Autowired
	public XmlReportGenerator xmlReportGenerator;

	private List<String> ignorableNames = Arrays.asList(new String[]{"MessageDateTime", "MessageIdentification"}); ;
	

	@Test
	public void testCreateGroupAIncidentReportWithNormalAge() throws Exception {
		
		GroupAIncidentReport groupAIncidentReport = BaselineIncidentFactory.getBaselineIncident();
		groupAIncidentService.saveGroupAIncidentReports(groupAIncidentReport);
		
		List<AdministrativeSegment> administrativeSegments =  administrativeSegmentRepository.findByIncidentNumber("54236732");
		assertThat(administrativeSegments.size(), not(equalTo(0)));
		
		Document document = xmlReportGenerator.createGroupAIncidentReport(administrativeSegments.get(0));
		XmlUtils.printNode(document);
		
        compareGroupAIncident("src/test/resources/xmlInstances/groupAIncidentWithNormalAgeFromBaselineIncident.xml", document); 
	}

	@Test
	public void testCreateGroupAIncidentReportWithNonNumericAge() throws Exception {
		
		GroupAIncidentReport groupAIncidentReport = BaselineIncidentFactory.getBaselineIncidentWithNonNumericAges();
		groupAIncidentService.saveGroupAIncidentReports(groupAIncidentReport);
		
		List<AdministrativeSegment> administrativeSegments =  administrativeSegmentRepository.findByIncidentNumber("54236733");
		assertThat(administrativeSegments.size(), not(equalTo(0)));
		log.info(administrativeSegments);
		
		Document document = xmlReportGenerator.createGroupAIncidentReport(administrativeSegments.get(0));
		XmlUtils.printNode(document);
		
		compareGroupAIncident("src/test/resources/xmlInstances/groupAIncidentWithNonNumericAgeFromBaselineIncident.xml", document); 
	}
	
	private void compareGroupAIncident(String expectedFilePath, Document document) {
		
		Map<String, String> prefix2UriMap = new HashMap<>();
		Arrays.stream(Namespace.values()).forEach(i->prefix2UriMap.put(i.prefix, i.uri));
		
		ElementSelector es = ElementSelectors.conditionalBuilder()
                .whenElementIsNamed("Substance")
                .thenUse(new ByNameAndTextRecSelector())
                .whenElementIsNamed("Item")
                .thenUse(new ByNameAndTextRecSelector())
                .whenElementIsNamed("Offense")
                .thenUse(ElementSelectors.byXPath("./@s:id", prefix2UriMap, ElementSelectors.byNameAndText))
                .whenElementIsNamed("OffenseForce")
                .thenUse(new ByNameAndTextRecSelector())
                .elseUse(ElementSelectors.byName)
                .build();

		final Diff documentDiff = DiffBuilder
	            .compare(Input.fromFile( expectedFilePath))
	            .withTest(Input.fromDocument(document))
	            .normalizeWhitespace()
	            .ignoreComments()
	            .ignoreWhitespace()
	            .withNodeFilter(node -> !ignorableNames.contains(node.getLocalName()))
	            .withNodeMatcher(new DefaultNodeMatcher(es))
	            .checkForSimilar()
	            .build();
		documentDiff.getDifferences(); 
		assertThat(documentDiff.hasDifferences(), equalTo(false));
	}

	@Test
	public void testCreateGroupBArrestReport() throws Exception {
		GroupBArrestReport groupBArrestReport = BaselineIncidentFactory.getBaselineGroupBArrestReport();
		arrestReportService.saveGroupBArrestReports(Arrays.asList(groupBArrestReport));
		
		ArrestReportSegment arrestReportSegment = arrestReportSegmentFactory.getBasicArrestReportSegment();
		arrestReportSegment = arrestReportSegmentRepository.save(arrestReportSegment);
		
		List<ArrestReportSegment> arrestReportSegments =  arrestReportSegmentRepository.findByArrestTransactionNumber("12345");
		assertThat(arrestReportSegments.size(), not(equalTo(0)));
		log.info(arrestReportSegments);
		
		Document document = xmlReportGenerator.createGroupBArrestReport(arrestReportSegments.get(0));
		XmlUtils.printNode(document.getDocumentElement());
		
		compareGroupBArrestReport("src/test/resources/xmlInstances/groupBArrestFromBaselineIncident.xml", document);

		arrestReportSegments =  arrestReportSegmentRepository.findByArrestTransactionNumber("arrestTr");
		assertThat(arrestReportSegments.size(), not(equalTo(0)));
		log.info(arrestReportSegments);
		
		document = xmlReportGenerator.createGroupBArrestReport(arrestReportSegments.get(0));
		XmlUtils.printNode(document.getDocumentElement());
		compareGroupBArrestReport("src/test/resources/xmlInstances/groupBArrestFromArrestReportFactory.xml", document);
		
	}

	@Test
	public void testCreateGroupBArrestReportWithUnknownAge() throws Exception {
		GroupBArrestReport groupBArrestReport = BaselineIncidentFactory.getBaselineGroupBArrestReportWithUnknownAge();
		arrestReportService.saveGroupBArrestReports(Arrays.asList(groupBArrestReport));
		
		List<ArrestReportSegment> arrestReportSegments =  arrestReportSegmentRepository.findByArrestTransactionNumber("45678");
		assertThat(arrestReportSegments.size(), not(equalTo(0)));
		log.info(arrestReportSegments.size());
		
		Document document = xmlReportGenerator.createGroupBArrestReport(arrestReportSegments.get(0));
		XmlUtils.printNode(document.getDocumentElement());
		
		compareGroupBArrestReport("src/test/resources/xmlInstances/groupBArrestWithUnknownAgeFromBaselineIncident.xml", document);
		
	}
	
	private void compareGroupBArrestReport(String expectedFilePath, Document document) {
		ElementSelector es = ElementSelectors.conditionalBuilder()
                .whenElementIsNamed("ArresteeArmedWithCode")
                .thenUse(new ByNameAndTextRecSelector())
                .elseUse(ElementSelectors.byName)
                .build();
		final Diff documentDiff = DiffBuilder
	            .compare(Input.fromFile(expectedFilePath))
	            .withTest(Input.fromDocument(document))
	            .normalizeWhitespace()
	            .ignoreComments()
	            .ignoreWhitespace()
	            .withNodeFilter(node -> !ignorableNames.contains(node.getLocalName()))
	            .withNodeMatcher(new DefaultNodeMatcher(es))
	            .checkForSimilar()
	            .build();
		documentDiff.getDifferences(); 
		assertThat(documentDiff.hasDifferences(), equalTo(false));
	}
	
}
