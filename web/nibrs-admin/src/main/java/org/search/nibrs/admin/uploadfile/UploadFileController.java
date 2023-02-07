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
package org.search.nibrs.admin.uploadfile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.search.nibrs.admin.AppProperties;
import org.search.nibrs.admin.security.AuthUser;
import org.search.nibrs.admin.services.rest.RestService;
import org.search.nibrs.common.NIBRSError;
import org.search.nibrs.common.NIBRSJsonError;
import org.search.nibrs.importer.ReportListener;
import org.search.nibrs.model.AbstractReport;
import org.search.nibrs.model.GroupAIncidentReport;
import org.search.nibrs.model.GroupBArrestReport;
import org.search.nibrs.util.NibrsFileUtils;
import org.search.nibrs.validate.common.SubmissionFileValidator;
import org.search.nibrs.validate.common.ValidationResults;
import org.search.nibrs.validation.SubmissionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@SessionAttributes({"showUserInfoDropdown", "validationResults", "persistReportTask", "authUser", "privateSummaryReportSite", "validationToConvertResults","reportConversionProgress"})
@RequestMapping("/files")
public class UploadFileController {
	private final Log log = LogFactory.getLog(this.getClass());
	@Resource
	AppProperties appProperties;
	
	@Resource
	RestService restService;

    @Autowired
    private DownloadService downloadService;

    @Autowired
	SubmissionValidator submissionValidator;
	
	@Autowired
	SubmissionFileValidator submissionFileValidator;

	final List<String> acceptedFileTypes = 
			Arrays.asList("application/zip", "text/plain", "application/octet-stream", "text/xml", "application/xml", "application/x-zip-compressed");

	@GetMapping("/upload")
	public String getFileUploadForm(HttpServletRequest request, Map<String, Object> model) throws IOException {
		ReportProcessProgress persistReportTask = (ReportProcessProgress) model.get("persistReportTask");
		
		if (persistReportTask == null || persistReportTask.isAborted() || persistReportTask.isComplete() || !persistReportTask.isStarted()) {
			return "uploadForm::uploadForm";
		}
		else {
	        return "validationReport :: #content";
		}
	}
	
	@GetMapping("/uploadToConvertForm")
	public String getFileUploadToConvertForm(HttpServletRequest request, Map<String, Object> model) throws IOException {
		ReportProcessProgress reportConversionProgress = (ReportProcessProgress) model.get("reportConversionProgress");
		
		if (reportConversionProgress == null || reportConversionProgress.isAborted() || reportConversionProgress.isComplete() || !reportConversionProgress.isStarted()) {
			return "uploadToConvertForm::uploadForm";
		}
		else {
			return "validationToConvertReport :: #content";
		}
	}
	

    @PostMapping("/upload")
	public String handleFileUpload(@RequestParam("file") MultipartFile[] multipartFiles,
			RedirectAttributes redirectAttributes, Model model) throws IOException, ParserConfigurationException {

		log.info("processing file: " + multipartFiles.length);
		
		ValidationResults validationResults = getNibrsErrors(multipartFiles); 
		model.addAttribute("validationResults", validationResults);
		
		List<AbstractReport> validReports = validationResults.getReportsWithoutErrors(); 
		ReportProcessProgress persistReportTask = new ReportProcessProgress(validReports);
		model.addAttribute("persistReportTask", persistReportTask);

        return "validationReport :: #content";
    }

    @PostMapping("/uploadToConvert")
    public String handleFileUploadToConvert(@RequestParam("file") MultipartFile[] multipartFiles,
    		RedirectAttributes redirectAttributes, Model model) throws IOException, ParserConfigurationException {
    	
    	log.info("processing file: " + multipartFiles.length);
    	
    	ValidationResults validationToConvertResults = getNibrsErrors(multipartFiles); 
    	model.addAttribute("validationToConvertResults", validationToConvertResults);
    	
    	List<AbstractReport> validReports = validationToConvertResults.getReportsWithoutErrors(); 
    	ReportProcessProgress reportConversionProgress = new ReportProcessProgress(validReports);
    	model.addAttribute("reportConversionProgress", reportConversionProgress);
    	
    	return "validationToConvertReport :: #content";
    }
    
    @PostMapping("/json")
    public @ResponseBody List<NIBRSJsonError> getNibrsErrorInJson(@RequestParam("file") MultipartFile[] multipartFiles,
    		RedirectAttributes redirectAttributes, Model model) throws IOException, ParserConfigurationException {
    	
    	log.info("processing file: " + multipartFiles.length);
    	
    	List<NIBRSError> filteredErrorList = getNibrsErrors(multipartFiles).getFilteredErrorList(); 
    	
    	//Translate NIBRSError to a simplified JSON error class so it can be more easily consumed 
    	
    	//Create array here
    	List<NIBRSJsonError> jsonErrors = new ArrayList<NIBRSJsonError>();
    	
    	for (NIBRSError nibrsError : filteredErrorList)
    	{
    		NIBRSJsonError nibrsJsonError = createNibrsJsonError(nibrsError);
    		
    		jsonErrors.add(nibrsJsonError);
    	}	
    	
    	return jsonErrors;
    }

	private NIBRSJsonError createNibrsJsonError(NIBRSError nibrsError) {
		NIBRSJsonError nibrsJsonError = new NIBRSJsonError();
		
		nibrsJsonError.setSubmissionDate(nibrsError.getDateOfTape());
		
		nibrsJsonError.setSourceLocation(nibrsError.getContext().getSourceLocation());
		
		nibrsJsonError.setActionType(nibrsError.getReport().getReportActionType());
		
		nibrsJsonError.setOri(nibrsError.getReport().getOri());
		
		nibrsJsonError.setIncidentNumber(nibrsError.getReportUniqueIdentifier());
		
		nibrsJsonError.setSegment(nibrsError.getSegmentTypeOutput());
		
		nibrsJsonError.setWithinSegmentIdentifier(nibrsError.getOffenseSegmentIdentifier());
		
		nibrsJsonError.setWithinOffenderArrestVictim(nibrsError.getOffenderArresteeVictimSegmentIdentifier());
		
		nibrsJsonError.setWithinProperty(nibrsError.getPropertySegmentIdentifier());
		
		nibrsJsonError.setDataElement(nibrsError.getDataElementIdentifierOutput());
		
		nibrsJsonError.setErrorCode(nibrsError.getNIBRSErrorCode().getCode());
		
		//Add if statement here
		if (StringUtils.isNotBlank(nibrsError.getRuleNumber()))
		{
			if (nibrsError.getRuleNumber().equals("404") || nibrsError.getRuleNumber().equals("35") || nibrsError.getRuleNumber().equals("342"))
			{
				if (nibrsError.getValue() != null)
				{	
					nibrsJsonError.setRejectedValue(nibrsError.getValue().toString());
				}
			}
			else
			{
				nibrsJsonError.setRejectedValue(nibrsError.getOffendingValues());
			}	
		}
			
		nibrsJsonError.setErrorMessage(nibrsError.getErrorMessage());
		return nibrsJsonError;
	}
    
	private ValidationResults getNibrsErrors(MultipartFile[] multipartFiles)
			throws IOException, ParserConfigurationException {
		ValidationResults validationResults = new ValidationResults();
		ReportListener validatorListener = new ReportListener() {
			@Override
			public void newReport(AbstractReport report, List<NIBRSError> el) {
				validationResults.getErrorList().addAll(el);
				validationResults.getErrorList().addAll(submissionValidator.validateReport(report));
				addReportWithoutErrors(validationResults, report);
			}
		};
		
		for (MultipartFile multipartFile: multipartFiles){
			if (!acceptedFileTypes.contains(multipartFile.getContentType())){
				throw new IllegalArgumentException("The file type is not supported"); 
			}
			
			if (multipartFile.getContentType().equals("application/zip") || multipartFile.getContentType().equals("application/x-zip-compressed")){
				validateZippedFile( validatorListener, multipartFile.getInputStream());
			}
			else {
				submissionFileValidator.validateInputStream(
						validatorListener, multipartFile.getContentType(), multipartFile.getInputStream(), "console");
			}
			
		}
		
		return validationResults;
	}

	@GetMapping("/about")
	public String getAbout(Model model){
	
	    return "about";
	}
	
	@GetMapping("/resources")
	public String getResources(Model model){
		
		return "resources";
	}
	
	@GetMapping("/testFiles")
	public String getTestFiles(Model model){
		
		return "testFiles";
	}
	

	/**
	 * Get only the first entry of the zipped resource. 
	 * @param multipartFile
	 * @return
	 * @throws IOException
	 */
	private void validateZippedFile(ReportListener validatorlistener, InputStream inputStream) throws IOException {
		ZipInputStream zippedStream = new ZipInputStream(inputStream);

		ZipEntry zipEntry = zippedStream.getNextEntry();
		while ( zipEntry != null)
		{
			log.info("Unzipping " + zipEntry.getName());
	        ByteArrayOutputStream bout = new ByteArrayOutputStream();
	        for (int c = zippedStream.read(); c != -1; c = zippedStream.read()) {
	          bout.write(c);
	        }
	        
	        zippedStream.closeEntry();
			
			ByteArrayInputStream inStream = new ByteArrayInputStream( bout.toByteArray() );
			bout.close();
			
		    String mediaType = NibrsFileUtils.getMediaType(inStream, zipEntry.getName());

		    try {
		    	submissionFileValidator.validateInputStream(validatorlistener, mediaType, inStream, "console");
			} catch (ParserConfigurationException e) {
				log.error("Got exception while parsing the file " + zipEntry.getName(), e);
			}
			zipEntry = zippedStream.getNextEntry();
		}
		zippedStream.close();
        inputStream.close();
	}
	
	@PostMapping("/validIncidentsAsync")
	public @ResponseBody String persistIncidentReportsAsync(Map<String, Object> model) {
		
		ValidationResults validationResults = (ValidationResults) model.get("validationResults");
		List<AbstractReport> validReports = validationResults.getReportsWithoutErrors(); 
		logCountsOfReports(validReports);
		
		ReportProcessProgress persistReportTask = (ReportProcessProgress) model.get("persistReportTask");
		
		AuthUser authUser = null; 
		if (!appProperties.getPrivateSummaryReportSite()) {
			authUser = (AuthUser) model.get("authUser"); 
		}
		restService.persistValidReportsAsync(persistReportTask, validationResults, authUser);
		
		log.info("called the aync method"); 
		return "Server processing the valid reports.";
	}

	@PostMapping("/validIncidentsConversionAsync")
	public @ResponseBody String convertIncidentReportsAsync(Map<String, Object> model) {
		
		ValidationResults validationToConvertResults = (ValidationResults) model.get("validationToConvertResults");
		List<AbstractReport> validReports = validationToConvertResults.getReportsWithoutErrors(); 
		logCountsOfReports(validReports);
		
		ReportProcessProgress reportConversionProgress = (ReportProcessProgress) model.get("reportConversionProgress");
		
		AuthUser authUser = null; 
		if (!appProperties.getPrivateSummaryReportSite()) {
			authUser = (AuthUser) model.get("authUser"); 
		}
		restService.convertValidReportsAsync(reportConversionProgress, validationToConvertResults, authUser);
		
		log.info("called the conversion aync method"); 
		return "Server processing the valid reports.";
	}
	
	@PostMapping("/preCertificationErrors")
	public @ResponseBody String persistPreCertificationErrors(Map<String, Object> model) {
		
		ValidationResults validationResults = (ValidationResults) model.get("validationResults");
		AuthUser authUser = (AuthUser) model.get("authUser"); 
		String response = restService.persistPreCertificationErrors(validationResults.getErrorList(), authUser);
		
		return response;
	}
	
	@GetMapping("/uploadStatus")
	public @ResponseBody ReportProcessProgress getUploadStatus(Map<String, Object> model) {
		
		ReportProcessProgress persistReportTask = (ReportProcessProgress) model.get("persistReportTask");
		return persistReportTask;
	}
	
	@GetMapping("/conversionStatus")
	public @ResponseBody ReportProcessProgress getConversionStatus(Map<String, Object> model) {
		
		ReportProcessProgress reportConversionProgress = (ReportProcessProgress) model.get("reportConversionProgress");
		return reportConversionProgress;
	}
	
	private void logCountsOfReports(List<AbstractReport> abstractReports) {
		Set<String> incidentNumbers = new HashSet<>();
		abstractReports.forEach(item->incidentNumbers.add(item.getIdentifier()));
		log.info("about to process " + abstractReports.size() + " reports with " + incidentNumbers.size() + " distinct identifiers. ");
	}	
	
	private void addReportWithoutErrors(ValidationResults validationResults, AbstractReport report) {
		validationResults.increaseTotalReportCount();
		if (validationResults.getErrorList().isEmpty()){
			validationResults.getReportsWithoutErrors().add(report);
			allocateTheReport(validationResults, report);
		}
		else{
			boolean isReportWithoutError = true; 
			for (int i=validationResults.getErrorList().size()-1; i>=0 ; i--) {
				NIBRSError nibrsError = validationResults.getErrorList().get(i);
				
				if (report.getIdentifier() != null && 
						!report.getIdentifier().equals(nibrsError.getReportUniqueIdentifier())) {
					break;
				}
				else if (nibrsError.isWarning()){
					continue; 
				}
				else {
					isReportWithoutError = false; 
					break;
				}
			}
			
			if (isReportWithoutError) {
				validationResults.getReportsWithoutErrors().add(report);
				allocateTheReport(validationResults, report);
			}
		}
	}


	private void allocateTheReport(ValidationResults validationResults, AbstractReport report) {
		if (report instanceof GroupAIncidentReport) {
			validationResults.getGroupAIncidentReports().add((GroupAIncidentReport) report);
		}
		else if(report instanceof GroupBArrestReport) {
			validationResults.getGroupBArrestReports().add((GroupBArrestReport) report);
		}
	}
	
    @GetMapping("/downloadZipFile")
    public void downloadZipFile(HttpServletResponse response, Map<String, Object> model) {
    	ReportProcessProgress reportConversionProgress = (ReportProcessProgress) model.get("reportConversionProgress");
    	
        downloadService.downloadZipFolder(response, reportConversionProgress.getOutputFolder());
    }
	
}

