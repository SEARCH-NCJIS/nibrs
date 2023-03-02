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
package org.search.nibrs.report;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("summary.report")
public class SummaryReportProperties {

	private String stagingDataRestServiceBaseUrl = "http://localhost:9080";
	private String summaryReportOutputPath = ".";
	private List<String> stateRaceCodeTitles = new ArrayList<>();
	private int yellowRaceCodeColumnCount = 3;
	
	public String getStagingDataRestServiceBaseUrl() {
		return stagingDataRestServiceBaseUrl;
	}

	public void setStagingDataRestServiceBaseUrl(String stagingDataRestServiceBaseUrl) {
		this.stagingDataRestServiceBaseUrl = stagingDataRestServiceBaseUrl;
	}

	public String getSummaryReportOutputPath() {
		return summaryReportOutputPath;
	}

	public void setSummaryReportOutputPath(String summaryReportOutputPath) {
		this.summaryReportOutputPath = summaryReportOutputPath;
	}

	@Override
	public String toString() {
		return "SummaryReportProperties [stagingDataRestServiceBaseUrl=" + stagingDataRestServiceBaseUrl
				+ ", summaryReportOutputPath=" + summaryReportOutputPath + "]";
	}

	public List<String> getStateRaceCodeTitles() {
		return stateRaceCodeTitles;
	}

	public void setStateRaceCodeTitles(List<String> stateRaceCodeTitles) {
		this.stateRaceCodeTitles = stateRaceCodeTitles;
	}

	public int getYellowRaceCodeColumnCount() {
		return yellowRaceCodeColumnCount;
	}

	public void setYellowRaceCodeColumnCount(int yellowRaceCodeColumnCount) {
		this.yellowRaceCodeColumnCount = yellowRaceCodeColumnCount;
	}

}