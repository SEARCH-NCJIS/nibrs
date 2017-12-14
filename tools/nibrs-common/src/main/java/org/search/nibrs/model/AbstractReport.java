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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.search.nibrs.common.NIBRSError;
import org.search.nibrs.common.ReportSource;
import org.search.nibrs.common.ValidationTarget;

/**
 * Abstract class of objects representing types of "reports" in NIBRS...  Group A incident reports, Group B arrest reports, and Zero Reports.
 *
 */
public abstract class AbstractReport implements ValidationTarget, Identifiable, Serializable {

	private static final long serialVersionUID = 8536244849582893682L;

	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(AbstractReport.class);

	private Integer monthOfTape;
	private Integer yearOfTape;
	private String cityIndicator;
	private String ori;
	protected char adminSegmentLevel;
	private char reportActionType;
	private boolean hasUpstreamErrors;
	private List<ArresteeSegment> arresteeSegmentList;
	private ReportSource source;
	
	public AbstractReport(char adminSegmentLevel) {
		this.adminSegmentLevel = adminSegmentLevel;
		removeArrestees();
	}
	
	public AbstractReport(AbstractReport r) {
		this.monthOfTape = r.monthOfTape;
		this.yearOfTape = r.yearOfTape;
		this.cityIndicator = r.cityIndicator;
		this.ori = r.ori;
		this.adminSegmentLevel = r.adminSegmentLevel;
		this.reportActionType = r.reportActionType;
		this.hasUpstreamErrors = r.hasUpstreamErrors;
		arresteeSegmentList = CopyUtils.copyList(r.arresteeSegmentList);
		for (ArresteeSegment s : arresteeSegmentList) {
			s.setParentReport(this);
		}
		this.source = new ReportSource(r.source);
	}
	
	public abstract String getUniqueReportDescription();
	public abstract String getGloballyUniqueReportIdentifier();
	public abstract String getAdminSegmentLength();

	public char getReportActionType() {
		return reportActionType;
	}
	public void setReportActionType(char reportActionType) {
		this.reportActionType = reportActionType;
	}
	
	public char getAdminSegmentLevel() {
		return adminSegmentLevel;
	}

	public String getCityIndicator() {
	    return cityIndicator;
	}

	public void setCityIndicator(String cityIndicator) {
	    this.cityIndicator = cityIndicator;
	}

	public Integer getMonthOfTape() {
	    return monthOfTape;
	}

	public void setMonthOfTape(Integer monthOfTape) {
		this.monthOfTape = monthOfTape;
	}

	public String getOri() {
	    return ori;
	}

	public void setOri(String ori) {
	    this.ori = ori;
	}

	public Integer getYearOfTape() {
	    return yearOfTape;
	}

	public void setYearOfTape(Integer yearOfTape) {
	    this.yearOfTape = yearOfTape;
	}

	public boolean getHasUpstreamErrors() {
		return hasUpstreamErrors;
	}

	public void setHasUpstreamErrors(boolean hasUpstreamErrors) {
		this.hasUpstreamErrors = hasUpstreamErrors;
	}
	public void removeArrestee(int index) {
		arresteeSegmentList.remove(index);
	}
	public void removeArrestees() {
		arresteeSegmentList = new ArrayList<ArresteeSegment>();
	}
	public void addArrestee(ArresteeSegment arrestee) {
	    arresteeSegmentList.add(arrestee);
	    arrestee.setParentReport(this);
	}
	public int getArresteeCount() {
	    return arresteeSegmentList.size();
	}
	public Iterator<ArresteeSegment> arresteeIterator() {
	    return getArrestees().iterator();
	}
	public List<ArresteeSegment> getArrestees() {
		return Collections.unmodifiableList(arresteeSegmentList);
	}
	public void setArrestees(List<ArresteeSegment> arresteeSegmentList) {
		this.arresteeSegmentList = arresteeSegmentList;
	}
	public ReportSource getSource() {
		return source;
	}
	public void setSource(ReportSource source) {
		this.source = source;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("[monthOfTape=" + monthOfTape + ", yearOfTape=" + yearOfTape + ", cityIndicator=" + cityIndicator + ", ori=" + ori + ", adminSegmentLevel=" + adminSegmentLevel
				+ ", reportActionType=" + reportActionType + ", hasUpstreamErrors=" + hasUpstreamErrors + "]");
		sb.append("\n").append(arresteeSegmentList.size() + " Arrestee Segments:\n");
		for (ArresteeSegment a : arresteeSegmentList) {
			sb.append("\t").append(a.toString()).append("\n");
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + adminSegmentLevel;
		result = prime * result + ((arresteeSegmentList == null) ? 0 : arresteeSegmentList.hashCode());
		result = prime * result + ((cityIndicator == null) ? 0 : cityIndicator.hashCode());
		result = prime * result + (hasUpstreamErrors ? 1231 : 1237);
		result = prime * result + ((monthOfTape == null) ? 0 : monthOfTape.hashCode());
		result = prime * result + ((ori == null) ? 0 : ori.hashCode());
		result = prime * result + reportActionType;
		result = prime * result + ((yearOfTape == null) ? 0 : yearOfTape.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.hashCode() == hashCode();
	}
	
	@Override
	public NIBRSError getErrorTemplate() {
		NIBRSError ret = new NIBRSError();
		ret.setContext(getSource());
		ret.setReportUniqueIdentifier(getIdentifier());
		ret.setSegmentType(getAdminSegmentLevel());
		ret.setReport(this);
		return ret;
	}

}
