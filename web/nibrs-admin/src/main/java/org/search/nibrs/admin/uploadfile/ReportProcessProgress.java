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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.search.nibrs.model.AbstractReport;

public class ReportProcessProgress{

	private Integer totalCount;
	private Integer processedCount = 0;
	private List<String> failedToProcess = new ArrayList<>(); 
	private boolean aborted = false;
	private boolean started = false; 
	private String outputFolder;
	
	public ReportProcessProgress() {
		super();
	}

	public ReportProcessProgress(List<AbstractReport> reportsToProcess) {
		this();
		this.totalCount = reportsToProcess.size();
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}


	public void addFailedToProcess(String reportDescription) {
		this.failedToProcess.add(reportDescription);
	}
	
	public List<String> getFailedToProcess() {
		return failedToProcess;
	}

	public void addToFailedToProcess(String reportDescription) {
		this.failedToProcess.add(reportDescription);
	}

	public Integer getProcessedCount() {
		return processedCount;
	}

	public void increaseProcessedCount() {
		this.processedCount ++;
	}

	public void increaseProcessedCount(int count) {
		this.processedCount += count ;
	}
	
	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	public Integer getProgress() {
		return (int) Math.round(processedCount.doubleValue()/totalCount.doubleValue() * 100); 
	}

	public boolean isAborted() {
		return aborted;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}
	
	public boolean isComplete() {
		return this.processedCount.equals(this.totalCount);
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}
}