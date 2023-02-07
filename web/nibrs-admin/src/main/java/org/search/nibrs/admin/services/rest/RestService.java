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
package org.search.nibrs.admin.services.rest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.search.nibrs.admin.AppProperties;
import org.search.nibrs.admin.security.AuthUser;
import org.search.nibrs.admin.uploadfile.ReportProcessProgress;
import org.search.nibrs.common.NIBRSError;
import org.search.nibrs.model.GroupAIncidentReport;
import org.search.nibrs.model.GroupBArrestReport;
import org.search.nibrs.stagingdata.model.Owner;
import org.search.nibrs.stagingdata.model.PreCertificationError;
import org.search.nibrs.stagingdata.model.SubmissionTrigger;
import org.search.nibrs.stagingdata.model.search.IncidentDeleteRequest;
import org.search.nibrs.stagingdata.model.search.IncidentSearchRequest;
import org.search.nibrs.stagingdata.model.search.IncidentSearchResult;
import org.search.nibrs.stagingdata.model.search.PrecertErrorSearchRequest;
import org.search.nibrs.stagingdata.model.search.SearchResult;
import org.search.nibrs.stagingdata.model.segment.AdministrativeSegment;
import org.search.nibrs.stagingdata.model.segment.ArrestReportSegment;
import org.search.nibrs.util.CustomPair;
import org.search.nibrs.validate.common.ValidationResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Profile({"incident-search"})
public class RestService{
	private final Log log = LogFactory.getLog(this.getClass());

	private final WebClient webClient;
	
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS");

	@Resource
	AppProperties appProperties;

	@Autowired
	public RestService(WebClient.Builder webClientBuilder, AppProperties appProperties) {
		this.webClient = webClientBuilder.baseUrl(appProperties.getRestServiceBaseUrl()).build();
	}
	
	public LinkedHashMap<String, Integer> getAgencies(Integer ownerId) {
		String ownerIdString = Objects.toString(ownerId, "");
		return this.webClient.get().uri("/codeTables/agencies/"+ownerIdString)
				.retrieve()
				.bodyToMono( new ParameterizedTypeReference<LinkedHashMap<String, Integer>>() {})
				.block();
	}
	
	public Map<String, Integer> getAgenciesNoChache(String ownerId) {
		return this.webClient.get().uri("/codeTables/agencies/"+ownerId)
				.header("Expires", "0")
				.header("Pragma", "no-cache")
				.header("Cache-Control", "private",  "no-store", "max-age=0")
				.retrieve()
				.bodyToMono( new ParameterizedTypeReference<LinkedHashMap<String, Integer>>() {})
				.block();
	}
	
	public Map<String, Integer> getAgenciesByOwnerAndState(Integer ownerId, String stateCode) {
		Map<String, Integer> map = 
				this.webClient.get().uri("/codeTables/states/" + stateCode + "/agencies/"+ Objects.toString(ownerId, ""))
				.header("Expires", "0")
				.header("Pragma", "no-cache")
				.header("Cache-Control", "private",  "no-store", "max-age=0")
				.retrieve()
				.bodyToMono( new ParameterizedTypeReference<LinkedHashMap<String, Integer>>() {})
				.block();
		return map; 
	}
	
	public Map<String, String> getOris(String ownerId) {
		return this.webClient.get().uri("/codeTables/agenciesHavingData/" + ownerId)
				.retrieve()
				.bodyToMono( new ParameterizedTypeReference<LinkedHashMap<String, String>>() {})
				.block();
	}
	
	public Map<Integer, String> getOffenseCodes() {
		return this.webClient.get().uri("/codeTables/offenseCodes")
				.retrieve()
				.bodyToMono( new ParameterizedTypeReference<LinkedHashMap<Integer, String>>() {})
				.block();
	}
	
	public Map<Integer, String> getNibrsErrorCodes() {
		return this.webClient.get().uri("/codeTables/nibrsErrorCodes")
				.retrieve()
				.bodyToMono( new ParameterizedTypeReference<LinkedHashMap<Integer, String>>() {})
				.block();
	}
	
	public IncidentSearchResult getIncidents(IncidentSearchRequest incidentSearchRequest){
		return this.webClient.post().uri("/reports/search")
				.body(BodyInserters.fromValue(incidentSearchRequest))
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<IncidentSearchResult>() {})
				.block();
	}
	
	public Owner getSavedUser(Owner webUser){
		return this.webClient.post().uri("/codeTables/user")
				.body(BodyInserters.fromValue(webUser))
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<Owner>() {})
				.block();
	}
	
	public AdministrativeSegment getAdministrativeSegment(String id){
		return this.webClient.get().uri("/reports/A/" + id)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<AdministrativeSegment>() {})
				.block();
	}
	
	public ArrestReportSegment getArrestReportSegment(String id){
		return this.webClient.get().uri("/reports/B/" + id)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<ArrestReportSegment>() {})
				.block();
	}
	
	public void persistGroupBReport(List<GroupBArrestReport> groupBArrestReports, ReportProcessProgress persistReportTask) {
		try{
			webClient.post().uri("/arrestReports")
			.body(BodyInserters.fromValue(groupBArrestReports))
			.retrieve()
			.bodyToMono(String.class)
			.block();
			
			persistReportTask.increaseProcessedCount(groupBArrestReports.size());
			log.info("Progress: " + persistReportTask.getProcessedCount() + "/" + persistReportTask.getTotalCount());
		}
		catch(ResourceAccessException rae){
			List<String> identifiers = groupBArrestReports.stream()
					.map(GroupBArrestReport::getIdentifier)
					.collect(Collectors.toList());
			log.error("Failed to connect to the rest service to process the Group B Arrest Reports " + 
					" with Identifiers " + identifiers);
			persistReportTask.setAborted(true);
			throw rae;
		}
		catch(Exception e){
			persistReportTask.increaseProcessedCount(groupBArrestReports.size());
			groupBArrestReports.stream()
				.map(GroupBArrestReport::getUniqueReportDescription)
				.forEach(item->persistReportTask.addFailedToProcess(item));
			List<String> identifiers = groupBArrestReports.stream()
					.map(GroupBArrestReport::getIdentifier)
					.collect(Collectors.toList());
			log.warn("Failed to persist incident " + identifiers);
			log.error(e);
			log.info("Progress: " + persistReportTask.getProcessedCount() + "/" + persistReportTask.getTotalCount());
		}
	}
	
	public void persistGroupAReport(List<GroupAIncidentReport> groupAIncidentReports, ReportProcessProgress persistReportTask) {
		
		try {
			log.info("About to post for group A incident report " + groupAIncidentReports.size());
			webClient.post().uri("/groupAIncidentReports")
				.body(BodyInserters.fromValue(groupAIncidentReports))
				.retrieve()
				.bodyToMono(String.class)
				.block();
			persistReportTask.increaseProcessedCount(groupAIncidentReports.size());
			log.info("Progress: " + persistReportTask.getProcessedCount() + "/" + persistReportTask.getTotalCount());
		}
		catch(ResourceAccessException rae){
			List<String> identifiers = groupAIncidentReports.stream()
					.map(GroupAIncidentReport::getIncidentNumber)
					.collect(Collectors.toList());
			log.error("Failed to connect to the rest service to process the group A reports " + 
					"  Identifiers " + identifiers);
			persistReportTask.setAborted(true);
			throw rae;
		}
		catch(Exception e){
			persistReportTask.increaseProcessedCount(groupAIncidentReports.size());
			
			groupAIncidentReports.stream()
				.map(GroupAIncidentReport::getUniqueReportDescription)
				.forEach(item->persistReportTask.addFailedToProcess(item));
			List<String> identifiers = groupAIncidentReports.stream()
					.map(GroupAIncidentReport::getIncidentNumber)
					.collect(Collectors.toList());
			log.warn("Failed to persist incident " + identifiers);
			log.error(e);
			log.info("Progress: " + persistReportTask.getProcessedCount() + "/" + persistReportTask.getTotalCount());
		}
	}
	
	public String generateSubmissionFiles(IncidentSearchRequest incidentSearchRequest) {
		SubmissionTrigger submissionTrigger = new SubmissionTrigger(incidentSearchRequest);
		log.info("submissionTrigger: " + submissionTrigger);
		log.info("submissionIncidentSearchRequest: " + incidentSearchRequest);
		
		String response = ""; 
		try { 
			response = webClient.post().uri("/submissions/trigger")
				.body(BodyInserters.fromValue(submissionTrigger))
				.retrieve()
				.bodyToMono(String.class)
				.block();
		}
		catch(Throwable e) {
			log.error("Got error when calling the service /submissions/trigger", e);
			response = "Failed to process the request, please report the error or check back later."; 
		}
		
		return response; 
	}
	
	public String deleteByIncidentDeleteRequest(IncidentDeleteRequest incidentDeleteRequest) {
		
		String response = ""; 
		String deleteGroupAIncidentsResponse = "";
		String deleteGroupBArrestsResponse = "";
		try { 
			deleteGroupAIncidentsResponse = webClient.method(HttpMethod.DELETE)
					.uri("/groupAIncidentReports")
					.body(BodyInserters.fromValue(incidentDeleteRequest))
					.retrieve()
					.bodyToMono(String.class)
					.block();
			deleteGroupBArrestsResponse = webClient.method(HttpMethod.DELETE)
					.uri("/arrestReports")
					.body(BodyInserters.fromValue(incidentDeleteRequest))
					.retrieve()
					.bodyToMono(String.class)
					.block();
			
			response = deleteGroupAIncidentsResponse + "\n" + deleteGroupBArrestsResponse;
		}
		catch(Throwable e) {
			log.error("Got error when calling the delete services of /groupAIncidentReports or /arrestReports ", e);
			response = "Failed to process the request, please report the error or check back later."; 
		}
		
		return response; 
	}
	
	@Async
	public void persistValidReportsAsync(ReportProcessProgress persistReportTask, ValidationResults validationResults, AuthUser authUser) {
		log.info("Execute method asynchronously. "
			      + Thread.currentThread().getName());
		persistReportTask.setStarted(true);
		for(List<GroupAIncidentReport> groupAIncidentReports: ListUtils.partition(validationResults.getGroupAIncidentReports(), 30)){
			if (authUser != null) {
				groupAIncidentReports.forEach(report-> report.setOwnerId(authUser.getUserId()));
			}
			this.persistGroupAReport(groupAIncidentReports, persistReportTask);
		}
		
		List<List<GroupBArrestReport>> groupBArrestReportsSublists = ListUtils.partition(validationResults.getGroupBArrestReports(), 30); 
		for(List<GroupBArrestReport> groupBArrestReports: groupBArrestReportsSublists){
			if (authUser != null) {
				groupBArrestReports.forEach(report-> report.setOwnerId(authUser.getUserId()));
			}
			this.persistGroupBReport(groupBArrestReports, persistReportTask);
		}
		
	}
	
	public String persistPreCertificationErrors(List<NIBRSError> nibrsErrors, AuthUser authUser) {
		log.info("Execute method asynchronously. "
				+ Thread.currentThread().getName());
		
		List<PreCertificationError> preCertificationErrors = nibrsErrors.stream()
				.map(nibrsError -> new PreCertificationError(nibrsError))
				.collect(Collectors.toList());
		
		if (authUser != null) {
			preCertificationErrors.forEach(error -> error.setOwnerId(authUser.getUserId()));
		}
		
		Integer savedCount = webClient.post().uri("/preCertificationErrors")
			.body(BodyInserters.fromValue(preCertificationErrors))
			.retrieve()
			.bodyToMono(Integer.class)
			.block();
		
		return savedCount + " errors are persisted."; 
		
	}

	public SearchResult<PreCertificationError> getPrecertErrors(PrecertErrorSearchRequest precertErrorSearchRequest) {
		return this.webClient.post().uri("/preCertificationErrors/search")
				.body(BodyInserters.fromValue(precertErrorSearchRequest))
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<SearchResult<PreCertificationError>>() {})
				.block();
	}

	public Map<String, String> getStatesNoChache(String ownerId) {
		
		String ownerIdString = Objects.toString(ownerId, "");
		return this.webClient.get().uri("/codeTables/states/"+ownerIdString)
				.header("Expires", "0")
				.header("Pragma", "no-cache")
				.header("Cache-Control", "private",  "no-store", "max-age=0")
				.retrieve()
				.bodyToMono( new ParameterizedTypeReference<LinkedHashMap<String, String>>() {})
				.block();
	}

	public List<Integer> getYearsByStateCode(String stateCode, String ownerId) {
		return this.webClient.get().uri("/codeTables/state/years/"+ ownerId + "/" + stateCode)
				.retrieve()
				.bodyToMono( new ParameterizedTypeReference<List<Integer>>() {})
				.block();
	}
	
	
	public List<Integer> getYears(Integer agencyId, String ownerId) {
		return this.webClient.get().uri("/codeTables/years/"+ ownerId + "/" + agencyId)
				.retrieve()
				.bodyToMono( new ParameterizedTypeReference<List<Integer>>() {})
				.block();
	}

	public List<Integer> getMonths(Integer agencyId, Integer year, String ownerId) {
		return this.webClient.get().uri("/codeTables/months/" + year + "/" + ownerId + "/"+ agencyId)
				.retrieve()
				.bodyToMono( new ParameterizedTypeReference<List<Integer>>() {})
				.block();
	}
	
	public List<Integer> getMonthsByStateCode(String stateCode, Integer year, String ownerId) {
		return this.webClient.get().uri("/codeTables/state/months/" + year + "/" + ownerId + "/"+ stateCode)
				.retrieve()
				.bodyToMono( new ParameterizedTypeReference<List<Integer>>() {})
				.block();
	}

	@Async
	public void convertValidReportsAsync(ReportProcessProgress reportConversionProgress,
			ValidationResults validationToConvertResults, AuthUser authUser) {
		log.info("Execute conversion method asynchronously. "
			      + Thread.currentThread().getName());
		reportConversionProgress.setStarted(true);
		
		String outputFolder = getRootFolderPath(authUser);
		reportConversionProgress.setOutputFolder(outputFolder);

		for(List<GroupAIncidentReport> groupAIncidentReports: ListUtils.partition(validationToConvertResults.getGroupAIncidentReports(), 50)){
			CustomPair<String, List<GroupAIncidentReport>> groupAIncidentsToConvert = 
					new CustomPair<String, List<GroupAIncidentReport>>(outputFolder, groupAIncidentReports); 
			this.convertGroupAReport(groupAIncidentsToConvert, reportConversionProgress);
		}
		
		List<List<GroupBArrestReport>> groupBArrestReportsSublists = ListUtils.partition(validationToConvertResults.getGroupBArrestReports(), 50);
		for(List<GroupBArrestReport> groupBArrestReports: groupBArrestReportsSublists){
			CustomPair<String, List<GroupBArrestReport>> groupBArrestsToConvert = 
					new CustomPair<String, List<GroupBArrestReport>>(outputFolder, groupBArrestReports); 
			this.convertGroupBReport(groupBArrestsToConvert, reportConversionProgress);
		}
		
		
	}

	private String getRootFolderPath(AuthUser authUser) {
		StringBuilder sb = new StringBuilder(200);
		sb.append(appProperties.getXmlDocumentDownloadRootFolder()); 
		sb.append("/");
		if (authUser != null) {
			sb.append(authUser.getUsername()); 
			sb.append("-");
		}
		sb.append(LocalDateTime.now().format(formatter));
		
		String outputFolder = sb.toString();
		return outputFolder;
	}

	private void convertGroupBReport(CustomPair<String, List<GroupBArrestReport>> groupBArrestsToConvert,
			ReportProcessProgress reportConversionProgress) {
		
		List<GroupBArrestReport> groupBArrestReports = groupBArrestsToConvert.getValue();
		try{
			webClient.post().uri("/arrestReportsToXml")
			.body(BodyInserters.fromValue(groupBArrestsToConvert))
			.retrieve()
			.bodyToMono(String.class)
			.block();
			
			reportConversionProgress.increaseProcessedCount(groupBArrestReports.size());
			log.info("Progress: " + reportConversionProgress.getProcessedCount() + "/" + reportConversionProgress.getTotalCount());
		}
		catch(ResourceAccessException rae){
			List<String> identifiers = groupBArrestReports.stream()
					.map(GroupBArrestReport::getIdentifier)
					.collect(Collectors.toList());
			log.error("Failed to connect to the rest service to process the Group B Arrest Reports " + 
					" with Identifiers " + identifiers);
			reportConversionProgress.setAborted(true);
			throw rae;
		}
		catch(Exception e){
			reportConversionProgress.increaseProcessedCount(groupBArrestReports.size());
			groupBArrestReports.stream()
				.map(GroupBArrestReport::getUniqueReportDescription)
				.forEach(item->reportConversionProgress.addFailedToProcess(item));
			List<String> identifiers = groupBArrestReports.stream()
					.map(GroupBArrestReport::getIdentifier)
					.collect(Collectors.toList());
			log.warn("Failed to persist incident " + identifiers);
			log.error(e);
			log.info("Progress: " + reportConversionProgress.getProcessedCount() + "/" + reportConversionProgress.getTotalCount());
		}
		
	}

	private void convertGroupAReport(CustomPair<String, List<GroupAIncidentReport>> groupAToConvertPair,
			ReportProcessProgress reportConversionProgress) {
		List<GroupAIncidentReport> groupAIncidentReports = groupAToConvertPair.getValue(); 
		try {
			log.info("About to post for group A incident report " + groupAIncidentReports.size());
			webClient.post().uri("/groupAIncidentReportsToXml")
				.body(BodyInserters.fromValue(groupAToConvertPair))
				.retrieve()
				.bodyToMono(String.class)
				.block();
			reportConversionProgress.increaseProcessedCount(groupAIncidentReports.size());
			log.info("Progress: " + reportConversionProgress.getProcessedCount() + "/" + reportConversionProgress.getTotalCount());
		}
		catch(ResourceAccessException rae){
			List<String> identifiers = groupAIncidentReports.stream()
					.map(GroupAIncidentReport::getIncidentNumber)
					.collect(Collectors.toList());
			log.error("Failed to connect to the rest service to process the group A reports " + 
					"  Identifiers " + identifiers);
			reportConversionProgress.setAborted(true);
			throw rae;
		}
		catch(Exception e){
			reportConversionProgress.increaseProcessedCount(groupAIncidentReports.size());
			
			groupAIncidentReports.stream()
				.map(GroupAIncidentReport::getUniqueReportDescription)
				.forEach(item->reportConversionProgress.addFailedToProcess(item));
			List<String> identifiers = groupAIncidentReports.stream()
					.map(GroupAIncidentReport::getIncidentNumber)
					.collect(Collectors.toList());
			log.warn("Failed to persist incident " + identifiers);
			log.error(e);
			log.info("Progress: " + reportConversionProgress.getProcessedCount() + "/" + reportConversionProgress.getTotalCount());
		}
		
	}
	
}