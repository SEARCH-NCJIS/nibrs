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
package org.search.nibrs.admin.incident;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.search.nibrs.admin.AppProperties;
import org.search.nibrs.admin.security.AuthUser;
import org.search.nibrs.admin.services.rest.RestService;
import org.search.nibrs.stagingdata.model.search.FbiSubmissionStatus;
import org.search.nibrs.stagingdata.model.search.IncidentDeleteRequest;
import org.search.nibrs.stagingdata.model.search.IncidentPointer;
import org.search.nibrs.stagingdata.model.search.IncidentSearchRequest;
import org.search.nibrs.stagingdata.model.search.IncidentSearchResult;
import org.search.nibrs.stagingdata.model.segment.AdministrativeSegment;
import org.search.nibrs.stagingdata.model.segment.ArrestReportSegment;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes({"incidentSearchRequest", "offenseCodeMapping", "incidentSearchResult", "fbiSubmissionStatuses", "authUser", "stateCodeMappingByOwner", "ownerId"})
@RequestMapping("/incidents")
public class IncidentController {
	private final Log log = LogFactory.getLog(this.getClass());
	@Resource
	AppProperties appProperties;

	@Resource
	RestService restService;
	
    @ModelAttribute
    public void addModelAttributes(Map<String, Object> model) {
    	
    	log.info("Add ModelAtrributes");
		
		if (model.get("offenseCodeMapping")== null) {
			model.put("offenseCodeMapping", restService.getOffenseCodes());
		}
		if (model.get("fbiSubmissionStatuses") == null) {
			model.put("fbiSubmissionStatuses", FbiSubmissionStatus.values());
		}
		if (appProperties.getPrivateSummaryReportSite()) {
			model.put("agencyMapping", restService.getAgencies(null));
			model.put("stateCodeMappingByOwner", restService.getStatesNoChache(null));
		}
		else {
			AuthUser authUser =(AuthUser) model.get("authUser");   
			String ownerId = Objects.toString(authUser.getUserId(), ""); 
			model.put("agencyMapping", restService.getAgencies(authUser.getUserId()));
			model.put("stateCodeMappingByOwner", restService.getStatesNoChache(ownerId));
		}
		
    	log.info("Added ModelAtrributes");
		
    }
    
	@GetMapping("/searchForm")
	public String getSearchForm(Map<String, Object> model) throws IOException {
		IncidentSearchRequest incidentSearchRequest = (IncidentSearchRequest) model.get("incidentSearchRequest");
		
		if (incidentSearchRequest == null) {
			incidentSearchRequest = new IncidentSearchRequest();
		}
		
		model.put("incidentSearchRequest", incidentSearchRequest);
	    return "/incident/incidents::resultsPage";
	}
	
	@GetMapping("/searchForm/reset")
	public String resetSearchForm(Map<String, Object> model) throws IOException {
		IncidentSearchRequest incidentSearchRequest = new IncidentSearchRequest();
		model.put("incidentSearchRequest", incidentSearchRequest);
		return "/incident/incidentSearchForm::incidentSearchFormContent";
	}
	
	@PostMapping("/search")
	public String advancedSearch(HttpServletRequest request, @Valid @ModelAttribute IncidentSearchRequest 
			incidentSearchRequest, BindingResult bindingResult, 
			Map<String, Object> model) throws Throwable {
		
		log.info("incidentSearchRequest:" + incidentSearchRequest );
		if (bindingResult.hasErrors()) {
			log.info("has binding errors");
			log.info(bindingResult.getAllErrors());
			return "/incident/incidents::resultsPage";
		}
		
		getIncidentSearchResults(request, incidentSearchRequest, model);
		return "/incident/incidents::resultsPage";
	}

	private void getIncidentSearchResults(HttpServletRequest request, IncidentSearchRequest incidentSearchRequest,
			Map<String, Object> model) throws Throwable {
		if (!appProperties.getPrivateSummaryReportSite()) {
			AuthUser authUser =(AuthUser) model.get("authUser");  
			Integer ownerId = authUser.getUserId();
			incidentSearchRequest.setOwnerId(ownerId);
		}
		model.put("incidentSearchRequest", incidentSearchRequest); 
		IncidentSearchResult incidentSearchResult = restService.getIncidents(incidentSearchRequest);
		log.debug("incidentSearchResult returned count " + incidentSearchResult.getReturnedCount());
		log.debug("incidentSearchResult returned count " + incidentSearchResult.getTotalCount());
		model.put("incidentSearchResult", incidentSearchResult); 
	}	

	@RequestMapping(value="/pointers", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Data<IncidentPointer> getIncidentPointers(
			Map<String, Object> model) throws Throwable {
		IncidentSearchResult incidentSearchResult = (IncidentSearchResult) model.get("incidentSearchResult");
		log.debug("incidentSearchResult returned count " + incidentSearchResult.getReturnedCount());
		log.debug("incidentSearchResult returned count " + incidentSearchResult.getTotalCount());
		
		return new Data<IncidentPointer>(incidentSearchResult.getIncidentPointers());
	}	
	
	@GetMapping("/{reportType}/{id}")
	public String getReportDetail(HttpServletRequest request, @PathVariable String id, 
			@PathVariable String reportType, Map<String, Object> model) throws Throwable {
		
		if ("GroupA".equals(reportType)) { 
			AdministrativeSegment administrativeSegment = restService.getAdministrativeSegment(id);
			model.put("administrativeSegment", administrativeSegment);
			log.debug("administrativeSegment: " + administrativeSegment);
			return "incident/groupAIncidentDetail::detail";
		}
		else {
			ArrestReportSegment arrestReportSegment = restService.getArrestReportSegment(id);
			model.put("arrestReportSegment", arrestReportSegment);
			log.debug("ArrestReportSegment: " + arrestReportSegment);
			return "incident/groupBArrestDetail::detail";
		}
	}

	@GetMapping("/styled/{reportType}/{id}")
	public String getReportStyledDetail(HttpServletRequest request, @PathVariable String id, 
			@PathVariable String reportType, Map<String, Object> model) throws Throwable {
		
		if ("GroupA".equals(reportType)) { 
			AdministrativeSegment administrativeSegment = restService.getAdministrativeSegment(id);
			model.put("administrativeSegment", administrativeSegment);
			log.debug("administrativeSegment: " + administrativeSegment);
			return "incident/groupAIncidentDetail";
		}
		else {
			ArrestReportSegment arrestReportSegment = restService.getArrestReportSegment(id);
			model.put("arrestReportSegment", arrestReportSegment);
			log.debug("ArrestReportSegment: " + arrestReportSegment);
			return "incident/groupBArrestDetail";
		}
	}
	
	@GetMapping("/incidentDeleteForm")
	public String getIncidentDeleteForm(Map<String, Object> model) throws IOException {
		AuthUser authUser =(AuthUser) model.get("authUser");  
		Integer ownerId = authUser.getUserId();
		
		model.put("stateCodeMappingByOwner", restService.getStatesNoChache(Objects.toString(authUser.getUserId())));
		
		IncidentDeleteRequest incidentDeleteRequest = new IncidentDeleteRequest();
		incidentDeleteRequest.setOwnerId(ownerId);
		
		model.put("incidentDeleteRequest", incidentDeleteRequest);
	    return "/incident/incidentDeleteForm::incidentDeleteFormPage";
	}
	

	@GetMapping("/incidentDeleteForm/reset")
	public String resetIncidentDeleteForm(Map<String, Object> model) throws IOException {
		IncidentDeleteRequest incidentDeleteRequest = new IncidentDeleteRequest();
		model.put("incidentDeleteRequest", incidentDeleteRequest);
		AuthUser authUser =(AuthUser) model.get("authUser");  
		Integer ownerId = authUser.getUserId();
		incidentDeleteRequest.setOwnerId(ownerId);
		model.put("agencyMapping", restService.getAgenciesNoChache(Objects.toString(authUser.getUserId())));
		return "/incident/incidentDeleteForm::incidentDeleteFormPage";
	}
	
	@PostMapping("/delete")
	public @ResponseBody String delete(HttpServletRequest request, @Valid @ModelAttribute IncidentDeleteRequest 
			incidentDeleteRequest, BindingResult bindingResult, 
			Map<String, Object> model) throws Throwable {
		
		String response = "The feature is not available for the setting.";
		if (!appProperties.getPrivateSummaryReportSite()) {
			AuthUser authUser =(AuthUser) model.get("authUser");  
			Integer ownerId = authUser.getUserId();
			incidentDeleteRequest.setOwnerId(ownerId);
		
			log.info("incidentDeleteRequest:" + incidentDeleteRequest );
			
			response = restService.deleteByIncidentDeleteRequest(incidentDeleteRequest);
			
			model.put("agencyMapping", restService.getAgenciesNoChache(Objects.toString(authUser.getUserId())));
			
		}
		
		return response;
	}
	
	@GetMapping("/agencies")
	public @ResponseBody Map<String, Integer> getAgencyIdMapping(@RequestParam(required = false) String stateCode, Map<String, Object> model) throws IOException{
		Integer ownerId = null;
		
		boolean privateSummaryReportSite = (boolean) model.get("privateSummaryReportSite");
		if (BooleanUtils.isFalse(privateSummaryReportSite)) {
			ownerId = (Integer) model.get("ownerId");
		}
		
		if (StringUtils.isBlank(stateCode)) {
			return restService.getAgencies(ownerId); 
		}
		else {
			return restService.getAgenciesByOwnerAndState(ownerId, stateCode);
		}
		
	}


}

