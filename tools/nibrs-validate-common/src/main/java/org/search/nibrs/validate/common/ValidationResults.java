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
package org.search.nibrs.validate.common;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.search.nibrs.common.NIBRSError;
import org.search.nibrs.model.AbstractReport;
import org.search.nibrs.model.GroupAIncidentReport;
import org.search.nibrs.model.GroupBArrestReport;

public class ValidationResults{

	private final List<NIBRSError> errorList;
	private List<AbstractReport> reportsWithoutErrors;
	private List<GroupAIncidentReport> groupAIncidentReports = new ArrayList<>();
	private List<GroupBArrestReport> groupBArrestReports = new ArrayList<>();
	private Integer totalReportCount = 0;
	private List<AbstractReport> reportWithAllowableErrors;

	private Integer persistedCount = 0; 
	private List<AbstractReport> failedToPersist = new ArrayList<>();
	private LocalTime validateTimestamp;
	private String owner; 
	
	public ValidationResults() {
		super();
		errorList = new ArrayList<>();
		reportsWithoutErrors = new ArrayList<>();
	}

	public List<NIBRSError> getErrorList() {
		return errorList;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

	public List<AbstractReport> getReportsWithoutErrors() {
		return reportsWithoutErrors;
	}

	public void setReportsWithoutErrors(List<AbstractReport> reportsWithoutErrors) {
		this.reportsWithoutErrors = reportsWithoutErrors;
	}

	public Integer getTotalReportCount() {
		return totalReportCount;
	}

	public Integer getCountOfValidReport() {
		return reportsWithoutErrors.size();
	}
	
	public void increaseTotalReportCount() {
		this.totalReportCount ++;
	}

	public List<AbstractReport> getReportWithAllowableErrors() {
		return reportWithAllowableErrors;
	}

	public void setReportWithAllowableErrors(List<AbstractReport> reportWithAllowableErrors) {
		this.reportWithAllowableErrors = reportWithAllowableErrors;
	}

	public List<NIBRSError> getFilteredErrorList() {
		return this.getErrorList().stream()
				.filter(error->error.getReport() != null)
				.collect(Collectors.toList());
	}

	public Integer getPersistedCount() {
		return persistedCount;
	}

	public void increasePersistedCount() {
		this.persistedCount ++;
	}

	public List<AbstractReport> getFailedToPersist() {
		return failedToPersist;
	}

	public void addToFailedToPersist(AbstractReport report) {
		this.failedToPersist.add(report);
	}

	public List<GroupAIncidentReport> getGroupAIncidentReports() {
		return groupAIncidentReports;
	}

	public void setGroupAIncidentReports(List<GroupAIncidentReport> groupAIncidentReports) {
		this.groupAIncidentReports = groupAIncidentReports;
	}

	public List<GroupBArrestReport> getGroupBArrestReports() {
		return groupBArrestReports;
	}

	public void setGroupBArrestReports(List<GroupBArrestReport> groupBArrestReports) {
		this.groupBArrestReports = groupBArrestReports;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public LocalTime getValidateTimestamp() {
		return validateTimestamp;
	}

	public void setValidateTimestamp(LocalTime validateTimestamp) {
		this.validateTimestamp = validateTimestamp;
	}

}