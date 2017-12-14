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

import org.search.nibrs.common.ParsedObject;
import org.search.nibrs.model.codes.RaceCode;
import org.search.nibrs.model.codes.SexCode;

/**
 * Representation of an OffenderSegment reported within an Incident in a NIBRS report.
 *
 */
public class OffenderSegment extends AbstractPersonSegment
{
    
	public static final char OFFENDER_SEGMENT_TYPE_IDENTIFIER = '5';
	public static final String OFFENDER_WITH_ETHNICITY_SEGMENT_LENGTH = "0046";
	private ParsedObject<Integer> offenderSequenceNumber;
    
    public OffenderSegment() {
    	super();
    	segmentType = OFFENDER_SEGMENT_TYPE_IDENTIFIER;
    	offenderSequenceNumber = new ParsedObject<>();
    }
    
    public OffenderSegment(OffenderSegment o) {
    	super(o);
    	offenderSequenceNumber = o.offenderSequenceNumber;
    	segmentType = OFFENDER_SEGMENT_TYPE_IDENTIFIER;
    }
    
    public ParsedObject<Integer> getOffenderSequenceNumber()
    {
        return offenderSequenceNumber;
    }
    public void setOffenderSequenceNumber(ParsedObject<Integer> offenderSequenceNumber)
    {
        this.offenderSequenceNumber = offenderSequenceNumber;
        if (!(offenderSequenceNumber.isMissing() || offenderSequenceNumber.isInvalid()) && offenderSequenceNumber.getValue() == 0) {
        }
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((offenderSequenceNumber == null) ? 0 : offenderSequenceNumber.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.hashCode() == hashCode();
	}

	@Override
	public String toString() {
		return "OffenderSegment [" + super.toString() + ", offenderSequenceNumber=" + offenderSequenceNumber + "]";
	}

	@Override
	public Object getWithinSegmentIdentifier() {
		return offenderSequenceNumber.getValue();
	}

	@Override
	public String getSegmentLength()
	{
		return OFFENDER_WITH_ETHNICITY_SEGMENT_LENGTH;
	}

	public boolean isOffenderOfVictim(VictimSegment vs) {
		return vs.isVictimOfOffender(this);
	}
    
	/**
	 * All offenders are people
	 */
	@Override
	public boolean isPerson() {
		return true;
	}

	/**
	 * An offender with a sequence number of 00 is unknown
	 */
	@Override
	public boolean isUnknown() {
		return !(offenderSequenceNumber.isMissing() || offenderSequenceNumber.isInvalid()) && offenderSequenceNumber.getValue() == 0;
	}

	public boolean isIdentifyingInfoComplete() {
		return getRace() != null && RaceCode.knownCodeSet().contains(getRace()) 
				&& getSex() != null && SexCode.knownCodeSet().contains(getSex()) 
				&& getAge() != null && !getAge().isUnknown();
	}
	
}
