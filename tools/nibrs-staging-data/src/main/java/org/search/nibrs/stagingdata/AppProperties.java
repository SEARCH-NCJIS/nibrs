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
package org.search.nibrs.stagingdata;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("staging.data")
public class AppProperties {
	
	private String nibrsNiemDocumentFolder = "/tmp/nibrs/niemSubmission/input";
    private String submittingAgencyOri = "SUBORI123";
	private Map<String, String> nonNumericAgeCodeMapping = new HashMap<>();
	private Map<String, String> propertyCodeMapping = new HashMap<>();
	private Map<String, String> locationCodeMapping = new HashMap<>();
	private Map<String, Integer> stateRaceCodeMapping = new HashMap<>();
	private Integer reportSearchResultsLimit = 10000;
	private Integer summaryReportProcessingBatchSize = 500;
	/*
	 * Segment action type matters to update records
	 */
	private boolean toUpdateSegmentActionType = true;

	public AppProperties() {
		super();
		getNonNumericAgeCodeMapping().put("NN", "NEONATAL");
		getNonNumericAgeCodeMapping().put("NB", "NEWBORN");
		getNonNumericAgeCodeMapping().put("BB", "BABY");
		getNonNumericAgeCodeMapping().put("00", "UNKNOWN");
		
		getPropertyCodeMapping().put("01", "MISCELLANEOUS");
		getPropertyCodeMapping().put("02", "CONSUMABLE_GOODS");
		getPropertyCodeMapping().put("03", "LOCALLY_STOLEN_MOTOR_VEHICLES");
		getPropertyCodeMapping().put("04", "MISCELLANEOUS");
		getPropertyCodeMapping().put("05", "LOCALLY_STOLEN_MOTOR_VEHICLES");
		getPropertyCodeMapping().put("06", "CLOTHING_FURS");
		getPropertyCodeMapping().put("07", "OFFICE_EQUIPMENT");
		getPropertyCodeMapping().put("08", "CONSUMABLE_GOODS");
		getPropertyCodeMapping().put("09", "MISCELLANEOUS");
		getPropertyCodeMapping().put("10", "CONSUMABLE_GOODS");
		getPropertyCodeMapping().put("11", "MISCELLANEOUS");
		getPropertyCodeMapping().put("12", "MISCELLANEOUS");
		getPropertyCodeMapping().put("13", "FIREARMS");
		getPropertyCodeMapping().put("14", "MISCELLANEOUS");
		getPropertyCodeMapping().put("15", "MISCELLANEOUS");
		getPropertyCodeMapping().put("16", "HOUSEHOLD_GOODS");
		getPropertyCodeMapping().put("17", "JEWELRY_AND_PRECIOUS_METALS");
		getPropertyCodeMapping().put("18", "LIVESTOCK");
		getPropertyCodeMapping().put("19", "MISCELLANEOUS");
		getPropertyCodeMapping().put("20", "CURRENCY_NOTES_ETC");
		getPropertyCodeMapping().put("21", "CURRENCY_NOTES_ETC");
		getPropertyCodeMapping().put("22", "MISCELLANEOUS");
		getPropertyCodeMapping().put("23", "OFFICE_EQUIPMENT");
		getPropertyCodeMapping().put("24", "LOCALLY_STOLEN_MOTOR_VEHICLES");
		getPropertyCodeMapping().put("25", "CLOTHING_FURS");
		getPropertyCodeMapping().put("26", "TELEVISIONS_RADIOS_STEREOS_ETC");
		getPropertyCodeMapping().put("27", "TELEVISIONS_RADIOS_STEREOS_ETC");
		getPropertyCodeMapping().put("28", "LOCALLY_STOLEN_MOTOR_VEHICLES");
		getPropertyCodeMapping().put("29", "MISCELLANEOUS");
		getPropertyCodeMapping().put("30", "MISCELLANEOUS");
		getPropertyCodeMapping().put("31", "MISCELLANEOUS");
		getPropertyCodeMapping().put("32", "MISCELLANEOUS");
		getPropertyCodeMapping().put("33", "MISCELLANEOUS");
		getPropertyCodeMapping().put("34", "MISCELLANEOUS");
		getPropertyCodeMapping().put("35", "MISCELLANEOUS");
		getPropertyCodeMapping().put("36", "MISCELLANEOUS");
		getPropertyCodeMapping().put("37", "LOCALLY_STOLEN_MOTOR_VEHICLES");
		getPropertyCodeMapping().put("38", "MISCELLANEOUS");
		getPropertyCodeMapping().put("39", "MISCELLANEOUS");
		getPropertyCodeMapping().put("40", "MISCELLANEOUS");
		getPropertyCodeMapping().put("41", "MISCELLANEOUS");
		getPropertyCodeMapping().put("42", "MISCELLANEOUS");
		getPropertyCodeMapping().put("43", "MISCELLANEOUS");
		getPropertyCodeMapping().put("44", "MISCELLANEOUS");
		getPropertyCodeMapping().put("45", "MISCELLANEOUS");
		getPropertyCodeMapping().put("46", "MISCELLANEOUS");
		getPropertyCodeMapping().put("47", "CONSUMABLE_GOODS");
		getPropertyCodeMapping().put("48", "MISCELLANEOUS");
		getPropertyCodeMapping().put("49", "MISCELLANEOUS");
		getPropertyCodeMapping().put("50", "MISCELLANEOUS");
		getPropertyCodeMapping().put("51", "MISCELLANEOUS");
		getPropertyCodeMapping().put("52", "MISCELLANEOUS");
		getPropertyCodeMapping().put("53", "MISCELLANEOUS");
		getPropertyCodeMapping().put("54", "MISCELLANEOUS");
		getPropertyCodeMapping().put("55", "MISCELLANEOUS");
		getPropertyCodeMapping().put("56", "MISCELLANEOUS");
		getPropertyCodeMapping().put("57", "MISCELLANEOUS");
		getPropertyCodeMapping().put("58", "MISCELLANEOUS");
		getPropertyCodeMapping().put("59", "MISCELLANEOUS");
		getPropertyCodeMapping().put("64", "CONSUMABLE_GOODS");
		getPropertyCodeMapping().put("65", "MISCELLANEOUS");
		getPropertyCodeMapping().put("66", "MISCELLANEOUS");
		getPropertyCodeMapping().put("67", "MISCELLANEOUS");
		getPropertyCodeMapping().put("68", "MISCELLANEOUS");
		getPropertyCodeMapping().put("69", "MISCELLANEOUS");
		getPropertyCodeMapping().put("70", "MISCELLANEOUS");
		getPropertyCodeMapping().put("71", "MISCELLANEOUS");
		getPropertyCodeMapping().put("72", "MISCELLANEOUS");
		getPropertyCodeMapping().put("73", "MISCELLANEOUS");
		getPropertyCodeMapping().put("74", "TELEVISIONS_RADIOS_STEREOS_ETC");
		getPropertyCodeMapping().put("75", "MISCELLANEOUS");
		getPropertyCodeMapping().put("76", "MISCELLANEOUS");
		getPropertyCodeMapping().put("77", "MISCELLANEOUS");
		getPropertyCodeMapping().put("78", "MISCELLANEOUS");
		getPropertyCodeMapping().put("79", "MISCELLANEOUS");
		getPropertyCodeMapping().put("80", "MISCELLANEOUS");
		getPropertyCodeMapping().put("88", "MISCELLANEOUS");
		
		getLocationCodeMapping().put("01", "MISCELLANEOUS");
		getLocationCodeMapping().put("02", "BANK");
		getLocationCodeMapping().put("03", "COMMERCIAL_HOUSE");
		getLocationCodeMapping().put("04", "MISCELLANEOUS");
		getLocationCodeMapping().put("05", "COMMERCIAL_HOUSE");
		getLocationCodeMapping().put("06", "MISCELLANEOUS");
		getLocationCodeMapping().put("07", "CONVENIENCE_STORE");
		getLocationCodeMapping().put("08", "COMMERCIAL_HOUSE");
		getLocationCodeMapping().put("09", "COMMERCIAL_HOUSE");
		getLocationCodeMapping().put("10", "MISCELLANEOUS");
		getLocationCodeMapping().put("11", "MISCELLANEOUS");
		getLocationCodeMapping().put("12", "COMMERCIAL_HOUSE");
		getLocationCodeMapping().put("13", "HIGHWAY");
		getLocationCodeMapping().put("14", "COMMERCIAL_HOUSE");
		getLocationCodeMapping().put("15", "MISCELLANEOUS");
		getLocationCodeMapping().put("16", "MISCELLANEOUS");
		getLocationCodeMapping().put("17", "COMMERCIAL_HOUSE");
		getLocationCodeMapping().put("18", "MISCELLANEOUS");
		getLocationCodeMapping().put("19", "MISCELLANEOUS");
		getLocationCodeMapping().put("20", "RESIDENCE");
		getLocationCodeMapping().put("21", "COMMERCIAL_HOUSE");
		getLocationCodeMapping().put("22", "MISCELLANEOUS");
		getLocationCodeMapping().put("23", "GAS_OR_SERVICE_STATION");
		getLocationCodeMapping().put("24", "COMMERCIAL_HOUSE");
		getLocationCodeMapping().put("25", "MISCELLANEOUS");
		getLocationCodeMapping().put("37", "MISCELLANEOUS");
		getLocationCodeMapping().put("38", "MISCELLANEOUS");
		getLocationCodeMapping().put("39", "MISCELLANEOUS");
		getLocationCodeMapping().put("40", "MISCELLANEOUS");
		getLocationCodeMapping().put("41", "MISCELLANEOUS");
		getLocationCodeMapping().put("42", "MISCELLANEOUS");
		getLocationCodeMapping().put("43", "MISCELLANEOUS");
		getLocationCodeMapping().put("44", "MISCELLANEOUS");
		getLocationCodeMapping().put("45", "MISCELLANEOUS");
		getLocationCodeMapping().put("46", "MISCELLANEOUS");
		getLocationCodeMapping().put("47", "MISCELLANEOUS");
		getLocationCodeMapping().put("48", "MISCELLANEOUS");
		getLocationCodeMapping().put("49", "MISCELLANEOUS");
		getLocationCodeMapping().put("50", "MISCELLANEOUS");
		getLocationCodeMapping().put("51", "MISCELLANEOUS");
		getLocationCodeMapping().put("52", "MISCELLANEOUS");
		getLocationCodeMapping().put("53", "MISCELLANEOUS");
		getLocationCodeMapping().put("54", "MISCELLANEOUS"); // according to the conversion of NIBRS Data to Summary Data.pdf
		getLocationCodeMapping().put("55", "MISCELLANEOUS");
		getLocationCodeMapping().put("56", "MISCELLANEOUS");
		getLocationCodeMapping().put("57", "MISCELLANEOUS");
		getLocationCodeMapping().put("58", "MISCELLANEOUS");
	}

	public String getSubmittingAgencyOri() {
		return submittingAgencyOri;
	}

	public void setSubmittingAgencyOri(String submittingAgencyOri) {
		this.submittingAgencyOri = submittingAgencyOri;
	}

	public Map<String, String> getNonNumericAgeCodeMapping() {
		return nonNumericAgeCodeMapping;
	}

	public String getNibrsNiemDocumentFolder() {
		return nibrsNiemDocumentFolder;
	}

	public void setNibrsNiemDocumentFolder(String nibrsNiemDocumentFolder) {
		this.nibrsNiemDocumentFolder = nibrsNiemDocumentFolder;
	}

	public Map<String, String> getLocationCodeMapping() {
		return locationCodeMapping;
	}

	public void setLocationCodeMapping(Map<String, String> locationCodeMapping) {
		this.locationCodeMapping = locationCodeMapping;
	}

	public Map<String, String> getPropertyCodeMapping() {
		return propertyCodeMapping;
	}

	public void setPropertyCodeMapping(Map<String, String> propertyCodeMapping) {
		this.propertyCodeMapping = propertyCodeMapping;
	}

	public Integer getReportSearchResultsLimit() {
		return reportSearchResultsLimit;
	}

	public void setReportSearchResultsLimit(Integer reportSearchResultsLimit) {
		this.reportSearchResultsLimit = reportSearchResultsLimit;
	}

	public Integer getSummaryReportProcessingBatchSize() {
		return summaryReportProcessingBatchSize;
	}

	public void setSummaryReportProcessingBatchSize(Integer summaryReportProcessingBatchSize) {
		this.summaryReportProcessingBatchSize = summaryReportProcessingBatchSize;
	}

	public Map<String, Integer> getStateRaceCodeMapping() {
		return stateRaceCodeMapping;
	}

	public void setStateRaceCodeMapping(Map<String, Integer> stateRaceCodeMapping) {
		this.stateRaceCodeMapping = stateRaceCodeMapping;
	}

	public boolean isToUpdateSegmentActionType() {
		return toUpdateSegmentActionType;
	}

	public void setToUpdateSegmentActionType(boolean toUpdateSegmentActionType) {
		this.toUpdateSegmentActionType = toUpdateSegmentActionType;
	}

}