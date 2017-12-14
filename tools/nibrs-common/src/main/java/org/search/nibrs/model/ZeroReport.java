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
package org.search.nibrs.model;

/**
 * Representation of an individual "Zero Report" in a NIBRS submission.  A zero report is submitted by an agency to signify that no crime occurred in that month in that agency.
 *
 */
public class ZeroReport extends AbstractReport {

	public static final char ZERO_REPORT_TYPE_IDENTIFIER = '0';
	public static final String ZERO_REPORT_SEGMENT_LENGTH = "0043";

	private String incidentNumber;

	public ZeroReport() {
        super(ZERO_REPORT_TYPE_IDENTIFIER);
	}
	
	public ZeroReport(ZeroReport z) {
		super(z);
	}
	
	@Override
	public boolean equals(Object o) {
		return o != null && o.hashCode() == hashCode();
	}
	
	@Override
	public int hashCode() {
		return 31*super.hashCode() + getClass().getName().hashCode();
	}

	@Override
	public String getUniqueReportDescription() {
    	return "Zero Report for ORI " + getOri();
	}

	@Override
	public String getGloballyUniqueReportIdentifier() {
		return getOri() + "." + getIncidentNumber();
	}

	@Override
	public String getAdminSegmentLength()
	{
		return ZERO_REPORT_SEGMENT_LENGTH;
	}
	
	@Override
	public String getIdentifier() {
		return getIncidentNumber();
	}

	public String getIncidentNumber() {
		return incidentNumber;
	}

	public void setIncidentNumber(String incidentNumber) {
		this.incidentNumber = incidentNumber;
	}

	@Override
	public String getIdentifier() {
		return getIncidentNumber();
	}
	
}
