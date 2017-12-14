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
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.search.nibrs.common.ParsedObject;

/**
 * Representation of an Arrestee reported within an Incident (either Group A or Group B) in a NIBRS report.
 *
 */
public class ArresteeSegment extends AbstractPersonSegment implements Identifiable, Serializable
{
    
	private static final long serialVersionUID = 8224865100768564724L;
	public static final int AUTOMATIC_WEAPON_INDICATOR_COUNT = 2;
	public static final int ARRESTEE_ARMED_WITH_COUNT = 2;
	public static final char GROUP_A_ARRESTEE_SEGMENT_TYPE_IDENTIFIER = '6';
	public static final char GROUP_B_ARRESTEE_SEGMENT_TYPE_IDENTIFIER = '7';
    public static final String GROUP_A_ARRESTEE_SEGMENT_LENGTH = "0110";
    public static final String GROUP_B_ARRESTEE_SEGMENT_LENGTH = "0066";

	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(ArresteeSegment.class);
	
	private ParsedObject<Integer> arresteeSequenceNumber;
    private String arrestTransactionNumber;
    private ParsedObject<Date> arrestDate;
    private String typeOfArrest;
    private String multipleArresteeSegmentsIndicator;
    private String ucrArrestOffenseCode;
    private String[] arresteeArmedWith;
    private String[] automaticWeaponIndicator;
    private String residentStatus;
    private String dispositionOfArresteeUnder18;

    public ArresteeSegment() {
		super();
	}

	public ArresteeSegment(AbstractPersonSegment p) {
		super(p);
	}

	public ArresteeSegment(char segmentType)
    {
        arresteeArmedWith = new String[ARRESTEE_ARMED_WITH_COUNT];
        automaticWeaponIndicator = new String[AUTOMATIC_WEAPON_INDICATOR_COUNT];
        this.segmentType = segmentType;
        arrestDate = new ParsedObject<>();
        arresteeSequenceNumber = new ParsedObject<>();
    }
    
    public ArresteeSegment(ArresteeSegment a) {
    	super(a);
    	this.arresteeSequenceNumber = a.arresteeSequenceNumber;
    	this.arrestTransactionNumber = a.arrestTransactionNumber;
    	this.arrestDate = a.arrestDate;
    	this.typeOfArrest = a.typeOfArrest;
    	this.multipleArresteeSegmentsIndicator = a.multipleArresteeSegmentsIndicator;
    	this.ucrArrestOffenseCode = a.ucrArrestOffenseCode;
    	this.residentStatus = a.residentStatus;
    	this.dispositionOfArresteeUnder18 = a.dispositionOfArresteeUnder18;
    	this.arresteeArmedWith = CopyUtils.copyArray(a.arresteeArmedWith);
    	this.automaticWeaponIndicator = CopyUtils.copyArray(a.automaticWeaponIndicator);
    	this.segmentType = a.segmentType;
    }
    
    @Override
    public String getIdentifier() {
    	return arrestTransactionNumber;
    }
    
    @Override
    protected void setParentReport(AbstractReport parentReport) {
    	super.setParentReport(parentReport);
    	if (parentReport instanceof GroupBArrestReport) {
    		segmentType = GROUP_B_ARRESTEE_SEGMENT_TYPE_IDENTIFIER;
    	} else {
    		segmentType = GROUP_A_ARRESTEE_SEGMENT_TYPE_IDENTIFIER;
    	}
    }
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((arrestDate == null) ? 0 : arrestDate.hashCode());
		result = prime * result + ((arrestTransactionNumber == null) ? 0 : arrestTransactionNumber.hashCode());
		result = prime * result + Arrays.hashCode(arresteeArmedWith);
		result = prime * result + ((arresteeSequenceNumber == null) ? 0 : arresteeSequenceNumber.hashCode());
		result = prime * result + Arrays.hashCode(automaticWeaponIndicator);
		result = prime * result + ((dispositionOfArresteeUnder18 == null) ? 0 : dispositionOfArresteeUnder18.hashCode());
		result = prime * result + ((multipleArresteeSegmentsIndicator == null) ? 0 : multipleArresteeSegmentsIndicator.hashCode());
		result = prime * result + ((residentStatus == null) ? 0 : residentStatus.hashCode());
		result = prime * result + ((typeOfArrest == null) ? 0 : typeOfArrest.hashCode());
		result = prime * result + ((ucrArrestOffenseCode == null) ? 0 : ucrArrestOffenseCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.hashCode() == hashCode();
	}

	public String getAutomaticWeaponIndicator(int position)
    {
        return automaticWeaponIndicator[position];
    }
    
	public String[] getAutomaticWeaponIndicator()
    {
        return automaticWeaponIndicator;
    }
    
    public void setAutomaticWeaponIndicator(int position, String value)
    {
        automaticWeaponIndicator[position] = value;
    }
    
    public String[] getArresteeArmedWith() {
    	return arresteeArmedWith;
    }
    
    public String getArresteeArmedWith(int position)
    {
        return arresteeArmedWith[position];
    }
    
    public void setArresteeArmedWith(int position, String value)
    {
        arresteeArmedWith[position] = value;
    }
    
    public ParsedObject<Date> getArrestDate()
    {
        return arrestDate;
    }
    public void setArrestDate(ParsedObject<Date> arrestDate)
    {
        this.arrestDate = arrestDate;
    }
    public ParsedObject<Integer> getArresteeSequenceNumber()
    {
        return arresteeSequenceNumber;
    }
    public void setArresteeSequenceNumber(ParsedObject<Integer> arresteeSequenceNumber)
    {
        this.arresteeSequenceNumber = arresteeSequenceNumber;
    }
    public String getArrestTransactionNumber()
    {
        return arrestTransactionNumber;
    }
    public void setArrestTransactionNumber(String arrestTransactionNumber)
    {
        this.arrestTransactionNumber = arrestTransactionNumber;
    }
    public String getDispositionOfArresteeUnder18()
    {
        return dispositionOfArresteeUnder18;
    }
    public void setDispositionOfArresteeUnder18(String dispositionOfArresteeUnder18)
    {
        this.dispositionOfArresteeUnder18 = dispositionOfArresteeUnder18;
    }
    public String getMultipleArresteeSegmentsIndicator()
    {
        return multipleArresteeSegmentsIndicator;
    }
    public void setMultipleArresteeSegmentsIndicator(String multipleArresteeSegmentsIndicator)
    {
        this.multipleArresteeSegmentsIndicator = multipleArresteeSegmentsIndicator;
    }
    public String getResidentStatus()
    {
        return residentStatus;
    }
    public void setResidentStatus(String residentStatus)
    {
        this.residentStatus = residentStatus;
    }
    public String getTypeOfArrest()
    {
        return typeOfArrest;
    }
    public void setTypeOfArrest(String typeOfArrest)
    {
        this.typeOfArrest = typeOfArrest;
    }
    public String getUcrArrestOffenseCode()
    {
        return ucrArrestOffenseCode;
    }
    public void setUcrArrestOffenseCode(String ucrArrestOffenseCode)
    {
        this.ucrArrestOffenseCode = ucrArrestOffenseCode;
    }
    
    public boolean isGroupA() {
    	return getSegmentType() == GROUP_A_ARRESTEE_SEGMENT_TYPE_IDENTIFIER;
    }

    public boolean isGroupB() {
    	return getSegmentType() == GROUP_B_ARRESTEE_SEGMENT_TYPE_IDENTIFIER;
    }
    
    public boolean isJuvenile() {
    	// set forth in rule for data element 52
    	NIBRSAge age = getAge();
    	boolean ret = false;
    	if (age != null && !age.isUnknown() && age.getError() == null) {
    		ret = age.isNonNumeric() || age.getAgeMax() < 18 || (age.getAgeMin() < 18 && age.getAverage() < 18);
    	}
    	return ret;
    }

    public boolean isNotJuvenile() {
    	// set forth in rule for data element 52
    	NIBRSAge age = getAge();
    	boolean ret = false;
    	if (age != null && !age.isUnknown() && age.getError() == null) {
    		ret = !(age.isNonNumeric() || age.getAgeMax() < 18 || (age.getAgeMin() < 18 && age.getAverage() < 18));
    	}
    	return ret;
    }
    
	@Override
	public Object getWithinSegmentIdentifier() {
		return arresteeSequenceNumber.getValue();
	}

    @Override
    public String getSegmentLength()
    {
        return isGroupA() ? GROUP_A_ARRESTEE_SEGMENT_LENGTH : GROUP_B_ARRESTEE_SEGMENT_LENGTH;
    }
	
	/**
	 * All arrestees are people
	 */
	@Override
	public boolean isPerson() {
		return true;
	}

	/**
	 * Arrestees cannot be unknown
	 */
	@Override
	public boolean isUnknown() {
		return false;
	}
	
	public boolean containsArresteeArmedWith(){
		return Arrays.asList(arresteeArmedWith)
				.stream()
				.filter(Objects::nonNull)
				.count() > 0;
	}
	
	public char getReportActionType() {
		char reportActionType = ' ';
		
		if (isGroupA()){
			reportActionType = ((GroupAIncidentReport) this.getParentReport()).getReportActionType();
		}
		else{
			reportActionType = ((GroupBArrestReport) this.getParentReport()).getReportActionType();
		}
		return reportActionType;
	}
	

	@Override
	public String toString(){
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
