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
package org.search.nibrs.stagingdata.model.search;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class IncidentSearchResult implements Serializable {
	private static final long serialVersionUID = 7916910066665545067L;
	private Integer primaryKey; 
	private ReportType reportType; 
	private String incidentIdentifier; 
	private Integer agencyId; 
	private String agencyName; 
	private LocalDate incidentDate; 
	private Integer ucrOffenseCodeTypeId; 
	private String offenseCode; 
	private String submissionMonth; 
	private String submissionYear; 
	private Boolean fbiSubmission;
	private LocalDateTime reportTimestamp; 
	
	public IncidentSearchResult() {
		super();
	}
	
	public IncidentSearchResult(Integer primaryKey,
			String reportType,
			String incidentIdentifier, 
			Integer agencyId, 
			String agencyName,
			LocalDate incidentDate,
			Integer ucrOffenseCodeTypeId,
			String offenseCode,
			String submissionMonth, 
			String submissionYear, 
			LocalDateTime reportTimestamp) {
		this();
		this.primaryKey = primaryKey;
		this.reportType = ReportType.valueOf(reportType);
		this.incidentIdentifier = incidentIdentifier;
		this.agencyId = agencyId;
		this.agencyName = agencyName; 
		this.incidentDate = incidentDate;
		this.ucrOffenseCodeTypeId = ucrOffenseCodeTypeId;
		this.offenseCode = offenseCode; 
		this.submissionMonth = submissionMonth;
		this.submissionYear = submissionYear;
		this.reportTimestamp = reportTimestamp;
	}
	public IncidentSearchResult(Integer primaryKey, String incidentIdentifier) {
		this();
		this.primaryKey = primaryKey;
		this.incidentIdentifier = incidentIdentifier;
	}
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	public String getIncidentIdentifier() {
		return incidentIdentifier;
	}
	public void setIncidentIdentifier(String incidentIdentifier) {
		this.incidentIdentifier = incidentIdentifier;
	}
	public Integer getAgencyId() {
		return agencyId;
	}
	public void setAgencyId(Integer agencyId) {
		this.agencyId = agencyId;
	}
	public LocalDate getIncidentDate() {
		return incidentDate;
	}
	public void setIncidentDate(LocalDate incidentDate) {
		this.incidentDate = incidentDate;
	}
	public Integer getUcrOffenseCodeTypeId() {
		return ucrOffenseCodeTypeId;
	}
	public void setUcrOffenseCodeTypeId(Integer ucrOffenseCodeTypeId) {
		this.ucrOffenseCodeTypeId = ucrOffenseCodeTypeId;
	}
	public String getSubmissionMonth() {
		return submissionMonth;
	}
	public void setSubmissionMonth(String submissionMonth) {
		this.submissionMonth = submissionMonth;
	}
	public String getSubmissionYear() {
		return submissionYear;
	}
	public void setSubmissionYear(String submissionYear) {
		this.submissionYear = submissionYear;
	}
	public Boolean getFbiSubmission() {
		return fbiSubmission;
	}
	public void setFbiSubmission(Boolean fbiSubmission) {
		this.fbiSubmission = fbiSubmission;
	}
	public String getAgencyName() {
		return agencyName;
	}
	public void setAgencyName(String agencyName) {
		this.agencyName = agencyName;
	}
	public String getOffenseCode() {
		return offenseCode;
	}
	public void setOffenseCode(String offenseCode) {
		this.offenseCode = offenseCode;
	} 

	public ReportType getReportType() {
		return reportType;
	}
	public void setReportType(ReportType reportType) {
		this.reportType = reportType;
	}
	public Integer getPrimaryKey() {
		return primaryKey;
	}
	public void setPrimaryKey(Integer primaryKey) {
		this.primaryKey = primaryKey;
	}
	public LocalDateTime getReportTimestamp() {
		return reportTimestamp;
	}
	public void setReportTimestamp(LocalDateTime reportTimestamp) {
		this.reportTimestamp = reportTimestamp;
	}
}
