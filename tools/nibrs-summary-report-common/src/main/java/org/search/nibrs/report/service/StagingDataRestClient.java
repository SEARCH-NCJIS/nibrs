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
package org.search.nibrs.report.service;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.buf.StringUtils;
import org.search.nibrs.model.reports.ReturnAForm;
import org.search.nibrs.model.reports.ReturnARecordCardReport;
import org.search.nibrs.model.reports.SummaryReportRequest;
import org.search.nibrs.model.reports.arson.ArsonReport;
import org.search.nibrs.model.reports.asr.AsrReports;
import org.search.nibrs.model.reports.cargotheft.CargoTheftReport;
import org.search.nibrs.model.reports.humantrafficking.HumanTraffickingForm;
import org.search.nibrs.model.reports.supplementaryhomicide.SupplementaryHomicideReport;
import org.search.nibrs.report.SummaryReportProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class StagingDataRestClient {

	private static final Log log = LogFactory.getLog(StagingDataRestClient.class);

	private final WebClient webClient;
	@Autowired
	private SummaryReportProperties appProperties;

	@Autowired
	public StagingDataRestClient(WebClient.Builder webClientBuilder, SummaryReportProperties appProperties) {
		this.webClient = webClientBuilder.baseUrl(appProperties.getStagingDataRestServiceBaseUrl()).build();
	}
	
	public ReturnAForm getReturnAFormByRequest(SummaryReportRequest summaryReportRequest) {
		
		log.info("Getting the ReturnAForm from the url " + appProperties.getStagingDataRestServiceBaseUrl() + "/returnAForm");
		
		ReturnAForm returnAForm =
				webClient.post().uri("/returnAForm")
				.body(BodyInserters.fromValue(summaryReportRequest))
				.retrieve()
				.bodyToMono(ReturnAForm.class)
				.block();

		log.debug("returnAForm: " + returnAForm);
		return returnAForm;
	}
	
	public AsrReports getAsrReportsByRequest(SummaryReportRequest summaryReportRequest) {

		List<String> urlParts = Arrays.asList(appProperties.getStagingDataRestServiceBaseUrl(), 
				"asrReports"); 
		String url = StringUtils.join(urlParts, '/');
		log.info("Getting the ASR Report object from the url " + url);
		
		AsrReports asrReports =
				webClient.post().uri("/asrReports")
				.body(BodyInserters.fromValue(summaryReportRequest))
				.retrieve()
				.bodyToMono(AsrReports.class)
				.block();

		log.info("asrReports: " + asrReports);
		return asrReports;
	}

	public ArsonReport getArsonReportByRequest(SummaryReportRequest summaryReportRequest) {
		
		List<String> urlParts = Arrays.asList(appProperties.getStagingDataRestServiceBaseUrl(), 
				"arsonReport"); 
		String url = StringUtils.join(urlParts, '/');
		log.info("Getting the Arson Report object from the url " + url);
		
		ArsonReport arsonReport = 
				webClient.post().uri("/arsonReport")
				.body(BodyInserters.fromValue(summaryReportRequest))
				.retrieve()
				.bodyToMono(ArsonReport.class)
				.block();
		log.info("arsonReport: " + arsonReport);
		return arsonReport;
	}
	
	public HumanTraffickingForm getHumanTraffickingFormByRequest(SummaryReportRequest summaryReportRequest) {
		List<String> urlParts = Arrays.asList(appProperties.getStagingDataRestServiceBaseUrl(), 
				"humanTraffickingReport"); 
		String url = StringUtils.join(urlParts, '/');
		log.info("Getting the humanTraffickingForm object from the url " + url);
		
		HumanTraffickingForm humanTraffickingForm = 
				webClient.post().uri("/humanTraffickingReport")
				.body(BodyInserters.fromValue(summaryReportRequest))
				.retrieve()
				.bodyToMono(HumanTraffickingForm.class)
				.block();
		log.info("humanTraffickingForm: " + humanTraffickingForm);
		return humanTraffickingForm;
	}

	public SupplementaryHomicideReport getSupplementaryHomicideReportByRequest(
			SummaryReportRequest summaryReportRequest) {
		List<String> urlParts = Arrays.asList(appProperties.getStagingDataRestServiceBaseUrl(), 
				"shrReports"); 
		String url = StringUtils.join(urlParts, '/');
		log.info("Getting the SupplementaryHomicideReport object from the url " + url);
		
		SupplementaryHomicideReport supplementaryHomicideReport = 
				webClient.post().uri("/shrReports")
				.body(BodyInserters.fromValue(summaryReportRequest))
				.retrieve()
				.bodyToMono(SupplementaryHomicideReport.class)
				.block();
		log.info("supplementaryHomicideReport: " + supplementaryHomicideReport);
		return supplementaryHomicideReport;
	}

	public CargoTheftReport getCargoTheftReportByRequest(SummaryReportRequest summaryReportRequest) {
		List<String> urlParts = Arrays.asList(appProperties.getStagingDataRestServiceBaseUrl(), 
				"cargoTheftReport"); 
		String url = StringUtils.join(urlParts, '/');
		log.info("Getting the CargoTheftReport object from the url " + url);
		
		CargoTheftReport cargoTheftReport = 
				webClient.post().uri("/cargoTheftReport")
				.body(BodyInserters.fromValue(summaryReportRequest))
				.retrieve()
				.bodyToMono(CargoTheftReport.class)
				.block();
		log.info("cargoTheftReport: " + cargoTheftReport);
		return cargoTheftReport;
	}

	public ReturnARecordCardReport getReturnARecordCardByRequest(SummaryReportRequest summaryReportRequest) {
		log.info("Getting the ReturnARecordCard from the url " + appProperties.getStagingDataRestServiceBaseUrl() + "/returnARecordCard");
		
		ReturnARecordCardReport returnARecordCardReport =
				webClient.post().uri("/returnARecordCard")
				.body(BodyInserters.fromValue(summaryReportRequest))
				.retrieve()
				.bodyToMono(ReturnARecordCardReport.class)
				.block();

		log.debug("ReturnARecordCardReport: " + returnARecordCardReport);
		return returnARecordCardReport;
	}
	
//	private List<HttpMessageConverter<?>> getMessageConverters() {
//	    List<HttpMessageConverter<?>> converters = 
//	      new ArrayList<HttpMessageConverter<?>>();
//	    converters.add(new MappingJackson2HttpMessageConverter());
//	    return converters;
//	}

}
