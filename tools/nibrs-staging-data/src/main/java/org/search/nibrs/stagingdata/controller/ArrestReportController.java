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
package org.search.nibrs.stagingdata.controller;

import java.util.List;

import org.search.nibrs.model.GroupBArrestReport;
import org.search.nibrs.stagingdata.model.search.IncidentDeleteRequest;
import org.search.nibrs.stagingdata.model.segment.ArrestReportSegment;
import org.search.nibrs.stagingdata.service.ArrestReportService;
import org.search.nibrs.stagingdata.util.BaselineIncidentFactory;
import org.search.nibrs.util.CustomPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArrestReportController {
	@Autowired
	private ArrestReportService arrestReportService;
	
	@RequestMapping("/arrestReports")
	public List<ArrestReportSegment> getAllArrestReport(){
		return arrestReportService.findAllArrestReportSegment();
	}
	
	@RequestMapping("/groupBArrestReport")
	public GroupBArrestReport getGroupBReport(){
		return BaselineIncidentFactory.getBaselineGroupBArrestReport();
	}
	
	@RequestMapping(value="/arrestReports", method=RequestMethod.POST)
	public void saveArrestReports(@RequestBody List<GroupBArrestReport> groupBArrestReports){
		arrestReportService.saveGroupBArrestReports(groupBArrestReports);
	}
	
	@RequestMapping(value="/arrestReportsToXml", method=RequestMethod.POST)
	public void convertArrestReports(@RequestBody CustomPair<String, List<GroupBArrestReport>> groupBArrestReportsPair){
		arrestReportService.convertAndWriteGroupBArrestReports(groupBArrestReportsPair);
	}
	
	@RequestMapping(value="/arrestReports/{identifier}", method=RequestMethod.DELETE)
	public void deleteArrestReport(@PathVariable("identifier") String identifier){
		arrestReportService.deleteGroupBArrestReport(identifier);
	}
	
	@RequestMapping(value="/arrestReports/{ori}/{yearOfTape}/{monthOfTape}", method=RequestMethod.DELETE)
	public @ResponseBody String deleteByOriAndSubmissionDate(@PathVariable("ori") String ori, @PathVariable("yearOfTape") String yearOfTape,
			@PathVariable("monthOfTape") String monthOfTape){
		int deletedCount = arrestReportService.deleteByOriAndSubmissionDate(ori, yearOfTape, monthOfTape);
		return String.valueOf(deletedCount) + " arrest reports are deleted. ";
	}
	
	@RequestMapping(value="/arrestReports", method=RequestMethod.DELETE)
	public @ResponseBody String deleteByIncidentDeleteRequest(@RequestBody IncidentDeleteRequest incidentDeleteRequest){
		int deletedCount = arrestReportService.deleteIncidentDeleteRequest(incidentDeleteRequest);
		return String.valueOf(deletedCount) + " arrest reports are deleted. ";
	}
	
}
