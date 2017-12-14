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

import java.util.Arrays;

import org.search.nibrs.common.ParsedObject;

/**
 * Representation of an OffenseSegment reported within an Incident in a NIBRS report.
 *  
 */
public class OffenseSegment extends AbstractSegment
{

	public static final int BIAS_MOTIVATION_COUNT = 5;
	public static final int AUTOMATIC_WEAPON_INDICATOR_COUNT = 3;
	public static final int TYPE_OF_WEAPON_FORCE_INVOLVED_COUNT = 3;
	public static final int TYPE_OF_CRIMINAL_ACTIVITY_COUNT = 3;
	public static final int OFFENDERS_SUSPECTED_OF_USING_COUNT = 3;

	public static final char OFFENSE_SEGMENT_TYPE_IDENTIFIER = '2';
    public static final String OFFENSE_SEGMENT_LENGTH = "0071";
	
	private String ucrOffenseCode;
    private String offenseAttemptedCompleted;
    private String[] offendersSuspectedOfUsing;
    private String locationType;
    private ParsedObject<Integer> numberOfPremisesEntered;
    private String methodOfEntry;
    private String[] typeOfCriminalActivity;
    private String[] typeOfWeaponForceInvolved;
    private String[] automaticWeaponIndicator;
    private String[] biasMotivation;
    private int populatedBiasMotivationCount;
    private int populatedTypeOfWeaponForceInvolvedCount;
    private int populatedTypeOfCriminalActivityCount;
    private int populatedOffendersSuspectedOfUsingCount;

    public OffenseSegment()
    {
    	super();
    	numberOfPremisesEntered = new ParsedObject<>();
        offendersSuspectedOfUsing = new String[OFFENDERS_SUSPECTED_OF_USING_COUNT];
        typeOfCriminalActivity = new String[TYPE_OF_CRIMINAL_ACTIVITY_COUNT];
        typeOfWeaponForceInvolved = new String[TYPE_OF_WEAPON_FORCE_INVOLVED_COUNT];
        automaticWeaponIndicator = new String[AUTOMATIC_WEAPON_INDICATOR_COUNT];
        biasMotivation = new String[BIAS_MOTIVATION_COUNT];
        segmentType = OFFENSE_SEGMENT_TYPE_IDENTIFIER;
    }
    
    public OffenseSegment(OffenseSegment o) {
    	super(o);
    	this.ucrOffenseCode = o.ucrOffenseCode;
    	this.offenseAttemptedCompleted = o.offenseAttemptedCompleted;
    	this.locationType = o.locationType;
    	this.numberOfPremisesEntered = o.numberOfPremisesEntered;
    	this.methodOfEntry = o.methodOfEntry;
    	this.populatedBiasMotivationCount = o.populatedBiasMotivationCount;
    	this.populatedTypeOfWeaponForceInvolvedCount = o.populatedTypeOfWeaponForceInvolvedCount;
    	this.populatedTypeOfCriminalActivityCount = o.populatedTypeOfCriminalActivityCount;
    	this.populatedOffendersSuspectedOfUsingCount = o.populatedOffendersSuspectedOfUsingCount;
    	offendersSuspectedOfUsing = CopyUtils.copyArray(o.offendersSuspectedOfUsing);
    	typeOfCriminalActivity = CopyUtils.copyArray(o.typeOfCriminalActivity);
    	typeOfWeaponForceInvolved = CopyUtils.copyArray(o.typeOfWeaponForceInvolved);
    	automaticWeaponIndicator = CopyUtils.copyArray(o.automaticWeaponIndicator);
    	biasMotivation = CopyUtils.copyArray(o.biasMotivation);
        segmentType = OFFENSE_SEGMENT_TYPE_IDENTIFIER;
    }
    
    public int getPopulatedOffendersSuspectedOfUsingCount() {
    	return populatedOffendersSuspectedOfUsingCount;
    }
    
    public int getPopulatedTypeOfCriminalActivityCount() {
    	return populatedTypeOfCriminalActivityCount;
    }
    
    public int getPopulatedTypeOfWeaponForceInvolvedCount() {
    	return populatedTypeOfWeaponForceInvolvedCount;
    }
    
    public int getPopulatedBiasMotivationCount() {
    	return populatedBiasMotivationCount;
    }
    
    public boolean getOffenseAttemptedIndicator() {
    	return "A".equals(getOffenseAttemptedCompleted());
    }

    public String getAutomaticWeaponIndicator(int position)
    {
        return automaticWeaponIndicator[position];
    }
    
    public String[] getAutomaticWeaponIndicator() {
    	return automaticWeaponIndicator;
    }

    @Override
    protected void setParentReport(AbstractReport parentReport) {
    	super.setParentReport(parentReport);
    }

    public void setAutomaticWeaponIndicator(int position, String value)
    {
        automaticWeaponIndicator[position] = value;
    }

    public String getTypeOfWeaponForceInvolved(int position)
    {
        return typeOfWeaponForceInvolved[position];
    }
    
    public String[] getTypeOfWeaponForceInvolved() {
    	return typeOfWeaponForceInvolved;
    }

    public void setTypeOfWeaponForceInvolved(int position, String value)
    {
        typeOfWeaponForceInvolved[position] = value;
        populatedTypeOfWeaponForceInvolvedCount = Math.max(populatedTypeOfWeaponForceInvolvedCount, position+1);
    }

    public String getTypeOfCriminalActivity(int position)
    {
        return typeOfCriminalActivity[position];
    }
    
    public String[] getTypeOfCriminalActivity() {
    	return typeOfCriminalActivity;
    }

    public void setTypeOfCriminalActivity(int position, String value)
    {
        typeOfCriminalActivity[position] = value;
        populatedTypeOfCriminalActivityCount = Math.max(populatedTypeOfCriminalActivityCount, position+1);
    }

    public String getOffendersSuspectedOfUsing(int position)
    {
        return offendersSuspectedOfUsing[position];
    }
    
    public String[] getOffendersSuspectedOfUsing() {
    	return offendersSuspectedOfUsing;
    }

    public void setOffendersSuspectedOfUsing(int position, String value)
    {
        offendersSuspectedOfUsing[position] = value;
        populatedOffendersSuspectedOfUsingCount = Math.max(populatedOffendersSuspectedOfUsingCount, position+1);
    }

    public String getBiasMotivation(int position)
    {
        return biasMotivation[position];
    }
    
    public String[] getBiasMotivation() {
    	return biasMotivation;
    }

    public void setBiasMotivation(int position, String value)
    {
        biasMotivation[position] = value;
        populatedBiasMotivationCount = Math.max(populatedBiasMotivationCount, position+1);
    }

    public String getLocationType()
    {
        return locationType;
    }

    public void setLocationType(String locationType)
    {
        this.locationType = locationType;
    }

    public String getMethodOfEntry()
    {
        return methodOfEntry;
    }

    public void setMethodOfEntry(String methodOfEntry)
    {
        this.methodOfEntry = methodOfEntry;
    }

    public ParsedObject<Integer> getNumberOfPremisesEntered()
    {
        return numberOfPremisesEntered;
    }

    public void setNumberOfPremisesEntered(ParsedObject<Integer> numberOfPremisesEntered)
    {
        this.numberOfPremisesEntered = numberOfPremisesEntered;
    }

    public String getOffenseAttemptedCompleted()
    {
        return offenseAttemptedCompleted;
    }

    public void setOffenseAttemptedCompleted(String offenseAttemptedCompleted)
    {
        this.offenseAttemptedCompleted = offenseAttemptedCompleted;
    }

    public String getUcrOffenseCode()
    {
        return ucrOffenseCode;
    }

    public void setUcrOffenseCode(String ucrOffenseCode)
    {
        this.ucrOffenseCode = ucrOffenseCode;
    }

    public boolean isWeaponForceFirearm(int position)
    {
        return NIBRSRules.weaponForceTypeCodeIsFirearm(getTypeOfWeaponForceInvolved(position));
    }

    public boolean isWeaponForceWeapon(int position)
    {
        return NIBRSRules.weaponForceTypeCodeIsWeapon(getTypeOfWeaponForceInvolved(position));
    }
    
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(automaticWeaponIndicator);
		result = prime * result + Arrays.hashCode(biasMotivation);
		result = prime * result + ((locationType == null) ? 0 : locationType.hashCode());
		result = prime * result + ((methodOfEntry == null) ? 0 : methodOfEntry.hashCode());
		result = prime * result + ((numberOfPremisesEntered == null) ? 0 : numberOfPremisesEntered.hashCode());
		result = prime * result + Arrays.hashCode(offendersSuspectedOfUsing);
		result = prime * result + ((offenseAttemptedCompleted == null) ? 0 : offenseAttemptedCompleted.hashCode());
		result = prime * result + populatedBiasMotivationCount;
		result = prime * result + populatedOffendersSuspectedOfUsingCount;
		result = prime * result + populatedTypeOfCriminalActivityCount;
		result = prime * result + populatedTypeOfWeaponForceInvolvedCount;
		result = prime * result + Arrays.hashCode(typeOfCriminalActivity);
		result = prime * result + Arrays.hashCode(typeOfWeaponForceInvolved);
		result = prime * result + ((ucrOffenseCode == null) ? 0 : ucrOffenseCode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.hashCode() == hashCode();
	}

	@Override
	public String toString() {
		return "OffenseSegment [ucrOffenseCode=" + ucrOffenseCode + ", offenseAttemptedCompleted=" + offenseAttemptedCompleted + ", offendersSuspectedOfUsing=" + Arrays.toString(offendersSuspectedOfUsing)
				+ ", locationType=" + locationType + ", numberOfPremisesEntered=" + numberOfPremisesEntered + ", methodOfEntry=" + methodOfEntry + ", typeOfCriminalActivity="
				+ Arrays.toString(typeOfCriminalActivity) + ", typeOfWeaponForceInvolved=" + Arrays.toString(typeOfWeaponForceInvolved) + ", automaticWeaponIndicator="
				+ Arrays.toString(automaticWeaponIndicator) + ", biasMotivation=" + Arrays.toString(biasMotivation) + ", populatedBiasMotivationCount=" + populatedBiasMotivationCount
				+ ", populatedTypeOfWeaponForceInvolvedCount=" + populatedTypeOfWeaponForceInvolvedCount + ", populatedTypeOfCriminalActivityCount=" + populatedTypeOfCriminalActivityCount
				+ ", populatedOffendersSuspectedOfUsingCount=" + populatedOffendersSuspectedOfUsingCount + "]";
	}

	@Override
	public Object getWithinSegmentIdentifier() {
		return ucrOffenseCode;
	}

    @Override
    public String getSegmentLength()
    {
        return OFFENSE_SEGMENT_LENGTH;
    }

}
