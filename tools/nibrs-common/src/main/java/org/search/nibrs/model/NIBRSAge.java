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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.search.nibrs.common.NIBRSError;
import org.search.nibrs.model.codes.NIBRSErrorCode;

/**
 * The class of objects representing an expression of a person's age in NIBRS.  An age expression can be a non-numeric code (e.g., for newborns), a single
 * integer value, or a range of integer values.  If the age is a single value, the min and max will be equal.
 *
 */
public class NIBRSAge {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(NIBRSAge.class);
	
	private Integer ageMin;
	private Integer ageMax;
	private String nonNumericAge;
	private NIBRSError error;
	private String ageString;
	private boolean invalidValue;
	private boolean invalidLength;
	
	public NIBRSAge() {
	}
	
	public NIBRSAge(NIBRSAge a) {
		this.ageMin = a.ageMin;
		this.ageMax = a.ageMax;
		this.nonNumericAge = a.nonNumericAge;
		this.error = a.error == null ? null : new NIBRSError(a.error);
		this.ageString = a.ageString;
		this.invalidValue = a.invalidValue;
		this.invalidLength = a.invalidLength;
	}
	
	String getAgeString() {
		return ageString;
	}
	
	void setError(NIBRSError error) {
		this.error = error;
	}
	
	void setAgeString(String ageString, char segmentContext) {
		nonNumericAge = null;
		ageMin = null;
		ageMax = null;
		error = null;
		invalidValue = false;
		invalidLength = false;
		if (ageString != null) {
			String ageStringTrim = ageString.trim();
			if (ageStringTrim.length() == 4) {
				try {
					ageMin = Integer.parseInt(ageStringTrim.substring(0, 2));
				} catch (NumberFormatException nfe) {
					setNonNumericAgeError(segmentContext, ageString);
					
				}
				try {
					ageMax = Integer.parseInt(ageStringTrim.substring(2, 4));
				} catch (NumberFormatException nfe) {
					error = new NIBRSError();
					error.setValue(ageString);
					error.setNIBRSErrorCode(NIBRSErrorCode.valueOf("_" + segmentContext + "09"));
					nonNumericAge = ageString;
					invalidValue = true;
				}
			} else if (ageStringTrim.length() == 2) {
				if ("NN".equals(ageStringTrim) || "NB".equals(ageStringTrim) || "BB".equals(ageStringTrim)) {
					nonNumericAge = ageStringTrim;
					ageMin = 0;
					ageMax = 0;
					
					if (segmentContext != '4'){
						setNonNumericAgeError(segmentContext, ageString);
					}
				} else if ("00".equals(ageStringTrim)) {
					nonNumericAge = ageStringTrim;
				} else {
					try {
						ageMin = Integer.parseInt(ageStringTrim.substring(0, 2));
						ageMax = ageMin;
					} catch (NumberFormatException nfe) {
						nonNumericAge = ageStringTrim; 
						setNonNumericAgeError(segmentContext, ageString);
						invalidValue = true;
					}
				}
			} else {
				invalidLength = true;
				nonNumericAge = ageStringTrim; 
				if (ageStringTrim.length() == 3){
					error = new NIBRSError();
					error.setValue(ageString);
					error.setNIBRSErrorCode(NIBRSErrorCode.valueOf("_" + segmentContext + "09"));
				}
				else{
					setNonNumericAgeError(segmentContext, ageString);
				}
			}
		}
	}

	private void setNonNumericAgeError(char segmentContext, String ageString) {
		error = new NIBRSError();
		error.setValue(ageString);
		nonNumericAge = ageString.trim();
	
		switch (segmentContext){
		case '4':
			error.setNIBRSErrorCode(NIBRSErrorCode.valueOf("_" + segmentContext + "04"));
			break; 
		case '5': 
			error.setNIBRSErrorCode(NIBRSErrorCode.valueOf("_" + segmentContext + "56"));
			break; 
		case '6': 
			error.setNIBRSErrorCode(NIBRSErrorCode.valueOf("_" + segmentContext + "64"));
			break; 
		case '7': 
			error.setNIBRSErrorCode(NIBRSErrorCode.valueOf("_" + segmentContext + "57"));
			break; 
		}
	}

	public Integer getAgeMin() {
		return ageMin;
	}

	public Integer getAgeMax() {
		return ageMax;
	}
	
	public Integer getAverage() {
		Integer ret = null;
		if (!isNonNumeric()) {
			double min = getAgeMin().doubleValue();
			double max = getAgeMax().doubleValue();
			double average = (min + max) / 2.0;
			ret = new Integer((int) average);
		}
		return ret;
	}

	public String getNonNumericAge() {
		return nonNumericAge;
	}

	public NIBRSError getError() {
		return error;
	}
	
	public boolean isNonNumeric() {
		return error == null && nonNumericAge != null;
	}
	
	public boolean isUnknown() {
		return "00".equals(nonNumericAge);
	}
	
	/**
	 * 1–6 Days Old
	 * @return true if "NN" is the value
	 */
	public boolean isNN() {
		return "NN".equals(nonNumericAge);
	}
	
	/**
	 * 1-6 days old 
	 * @return true if "NB" is the value 
	 */
	public boolean isNB() {
		return "NB".equals(nonNumericAge);
	}
	
	/**
	 * 7–364 Days Old
	 * @return true if "BB" is the value 
	 */
	public boolean isBB() {
		return "BB".equals(nonNumericAge);
	}
	
	public boolean isAgeRange() {
		boolean isAgeRange = false;
		if (error == null && !isNonNumeric()) {
			if (ageMin != null && ageMax != null) {
				isAgeRange = !ageMin.equals(ageMax);
			}
		}
		return isAgeRange;
	}
	
	public boolean containsBadAgeRange() {
		if ( nonNumericAge != null && nonNumericAge.length() > 2 ){
			return true;
		}
		return false;
	}
	
	public boolean hasInvalidValue() {
		return invalidValue;
	}
	
	public boolean hasInvalidLength() {
		return invalidLength;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ageString == null) ? 0 : ageString.hashCode());
		//LOG.info("hashCode=" + result);
		result = prime * result + ((error == null) ? 0 : error.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.hashCode() == hashCode();
	}
	
	@Override
	public String toString() {
		NIBRSError e = getError();
		if (e != null) {
			return "Invalid age: " + getAgeString();
		}
		return isNonNumeric() ? getNonNumericAge() : (isAgeRange() ? ageMin + "" + ageMax : ageMin.toString());
	}

	public boolean isYoungerThan(NIBRSAge age, boolean lenientRange) {
		if (age == null) {
			throw new IllegalArgumentException("Cannot compare to null age");
		}
		int[] thisDays = convertAgeToDays();
		int[] thatDays = age.convertAgeToDays();
		int thatComp = lenientRange ? thatDays[1] : thatDays[0];
		int thisComp = lenientRange ? thisDays[0] : thisDays[1];
		return thisComp < thatComp;
	}

	public boolean isOlderThan(NIBRSAge age, boolean lenientRange) {
		if (age == null) {
			throw new IllegalArgumentException("Cannot compare to null age");
		}
		int[] thisDays = convertAgeToDays();
		int[] thatDays = age.convertAgeToDays();
		int thatComp = lenientRange ? thatDays[0] : thatDays[1];
		int thisComp = lenientRange ? thisDays[1] : thisDays[0];
		return thisComp > thatComp;
	}
	
	int[] convertAgeToDays() {
		int[] ret = new int[2];
		if (isNonNumeric()) {
			if ("NN".equals(nonNumericAge)) {
				ret[0] = 0;
				ret[1] = 0;
			} else if ("NB".equals(nonNumericAge)) {
				ret[0] = 1;
				ret[1] = 1;
			} else if ("BB".equals(nonNumericAge)) {
				ret[0] = 7;
				ret[1] = 7;
			}
		} else {
			ret[0] = ageMin*365;
			ret[1] = ageMax*365;
		}
		return ret;
	}

}