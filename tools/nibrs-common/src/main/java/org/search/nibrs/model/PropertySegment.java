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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.search.nibrs.common.ParsedObject;
import org.search.nibrs.model.codes.OffenseCode;
import org.search.nibrs.model.codes.PropertyDescriptionCode;
import org.search.nibrs.model.codes.TypeOfPropertyLossCode;

/**
 * Representation of an article of property reported within an Incident in a NIBRS report.
 *
 */
public class PropertySegment extends AbstractSegment
{
	
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(PropertySegment.class);
    
	public static final int SUSPECTED_DRUG_TYPE_COUNT = 3;
	public static final int DATE_RECOVERED_COUNT = 10;
	public static final int VALUE_OF_PROPERTY_COUNT = 10;
	public static final int PROPERTY_DESCRIPTION_COUNT = 10;

	public static final char PROPERTY_SEGMENT_TYPE_IDENTIFIER = '3';
    public static final String PROPERTY_SEGMENT_LENGTH = "0307";
	
	private String typeOfPropertyLoss;
    private String[] propertyDescription;
    private ParsedObject<Integer>[] valueOfProperty;
    private ParsedObject<Date>[] dateRecovered;
    private ParsedObject<Integer> numberOfStolenMotorVehicles;
    private ParsedObject<Integer> numberOfRecoveredMotorVehicles;
    private String[] suspectedDrugType;
    private ParsedObject<Double>[] estimatedDrugQuantity;
    private String[] typeDrugMeasurement;

	private int populatedPropertyDescriptionCount;
    private int populatedSuspectedDrugTypeCount;

    public PropertySegment()
    {
    	super();
        propertyDescription = new String[PROPERTY_DESCRIPTION_COUNT];
		valueOfProperty = ParsedObject.initializeParsedObjectArray(VALUE_OF_PROPERTY_COUNT);
        dateRecovered = ParsedObject.initializeParsedObjectArray(DATE_RECOVERED_COUNT);
        suspectedDrugType = new String[SUSPECTED_DRUG_TYPE_COUNT];
        estimatedDrugQuantity = ParsedObject.initializeParsedObjectArray(SUSPECTED_DRUG_TYPE_COUNT);
        typeDrugMeasurement = new String[SUSPECTED_DRUG_TYPE_COUNT];
        segmentType = PROPERTY_SEGMENT_TYPE_IDENTIFIER;
        numberOfStolenMotorVehicles = new ParsedObject<Integer>();
        numberOfRecoveredMotorVehicles = new ParsedObject<Integer>();
    }

    public PropertySegment(PropertySegment p) {
    	super(p);
    	typeOfPropertyLoss = p.typeOfPropertyLoss;
    	numberOfRecoveredMotorVehicles = p.numberOfRecoveredMotorVehicles;
    	numberOfStolenMotorVehicles = p.numberOfStolenMotorVehicles;
    	populatedPropertyDescriptionCount = p.populatedPropertyDescriptionCount;
    	populatedSuspectedDrugTypeCount = p.populatedSuspectedDrugTypeCount;
    	propertyDescription = CopyUtils.copyArray(p.propertyDescription);
    	valueOfProperty = CopyUtils.copyArray(p.valueOfProperty);
    	dateRecovered = CopyUtils.copyArray(p.dateRecovered);
    	suspectedDrugType = CopyUtils.copyArray(p.suspectedDrugType);
    	estimatedDrugQuantity = CopyUtils.copyArray(p.estimatedDrugQuantity);
    	typeDrugMeasurement = CopyUtils.copyArray(p.typeDrugMeasurement);
        segmentType = PROPERTY_SEGMENT_TYPE_IDENTIFIER;
    }
    
    public boolean containsPropertyDescription(String descriptionCode) {
    	return Arrays.asList(getPropertyDescription()).contains(descriptionCode);
    }
    
	public ParsedObject<Integer>[] getValueOfProperty() {
		return valueOfProperty;
	}
	
    public ParsedObject<Double>[] getEstimatedDrugQuantity() {
		return estimatedDrugQuantity;
	}

	public ParsedObject<Date>[] getDateRecovered() {
		return dateRecovered;
	}

	public String[] getTypeDrugMeasurement() {
		return typeDrugMeasurement;
	}
	public String[] getPropertyDescription() {
		return propertyDescription;
	}
	
	public List<String> getPropertyDescriptionList(){
		return Arrays.stream(propertyDescription)
				.filter(StringUtils::isNotBlank)
				.collect(Collectors.toList());
	}
	
	public boolean containsVehiclePropertyCodes(){
		return getPropertyDescriptionList()
				.stream()
				.anyMatch(PropertyDescriptionCode::isMotorVehicleCode);
	}
	
	public Optional<String> getFirstVehiclePropertyCode(){
		return getPropertyDescriptionList()
				.stream()
				.filter(PropertyDescriptionCode::isMotorVehicleCode)
				.findFirst();
	}

	public String[] getSuspectedDrugType() {
		return suspectedDrugType;
	}

    public int getPopulatedSuspectedDrugTypeCount() {
    	return populatedSuspectedDrugTypeCount;
    }
    
    public int getPopulatedPropertyDescriptionCount() {
    	return populatedPropertyDescriptionCount;
    }
    
    public ParsedObject<Double> getEstimatedDrugQuantity(int position)
    {
        return estimatedDrugQuantity[position];
    }
    
    public void setEstimatedDrugQuantity(int position, ParsedObject<Double> value)
    {
        estimatedDrugQuantity[position] = value;
    }

    public String getTypeDrugMeasurement(int position)
    {
        return typeDrugMeasurement[position];
    }
    
    public void setTypeDrugMeasurement(int position, String value)
    {
        typeDrugMeasurement[position] = value;
    }

    public String getSuspectedDrugType(int position)
    {
        return suspectedDrugType[position];
    }
    
    public void setSuspectedDrugType(int position, String value)
    {
        suspectedDrugType[position] = value;
        populatedSuspectedDrugTypeCount = Math.max(populatedSuspectedDrugTypeCount, position+1);
    }

    public ParsedObject<Date> getDateRecovered(int position)
    {
        return dateRecovered[position];
    }
    
    public void setDateRecovered(int position, ParsedObject<Date> value)
    {
        dateRecovered[position] = value;
    }

    public ParsedObject<Integer> getValueOfProperty(int position)
    {
        return valueOfProperty[position];
    }
    
    public void setValueOfProperty(int position, ParsedObject<Integer> value)
    {
        valueOfProperty[position] = value;
    }

    public String getPropertyDescription(int position)
    {
        return propertyDescription[position];
    }
    
    public void setPropertyDescription(int position, String value)
    {
        propertyDescription[position] = value;
        populatedPropertyDescriptionCount = Math.max(populatedPropertyDescriptionCount, position+1);
    }

    public ParsedObject<Integer> getNumberOfRecoveredMotorVehicles()
    {
        return numberOfRecoveredMotorVehicles;
    }
    
    public void setNumberOfRecoveredMotorVehicles(ParsedObject<Integer> numberOfRecoveredMotorVehicles)
    {
    	this.numberOfRecoveredMotorVehicles = numberOfRecoveredMotorVehicles;
    }
    public ParsedObject<Integer> getNumberOfStolenMotorVehicles()
    {
        return numberOfStolenMotorVehicles;
    }
    public void setNumberOfStolenMotorVehicles(ParsedObject<Integer> numberOfStolenMotorVehicles)
    {
        this.numberOfStolenMotorVehicles = numberOfStolenMotorVehicles;
    }
    
    public String getTypeOfPropertyLoss()
    {
        return typeOfPropertyLoss;
    }
    public void setTypeOfPropertyLoss(String typeOfPropertyLoss)
    {
        this.typeOfPropertyLoss = typeOfPropertyLoss;
    }
    
    public boolean isLossTypeRecovered()
    {
        return NIBRSRules.propertyLossTypeIsRecovered(getTypeOfPropertyLoss());
    }
    
    public boolean isPropertyTypeDrug(int position)
    {
        return NIBRSRules.propertyDescriptionCodeIsDrug(getPropertyDescription(position));
    }

    public boolean drugMeasurementIsNotReported(int position)
    {
        return NIBRSRules.drugMeasurementIsNotReported(getTypeDrugMeasurement(position));
    }
    
    public boolean isStolenPropertySegment(){
    	return TypeOfPropertyLossCode._7.code.equals(typeOfPropertyLoss);
    }

    public boolean isRecoveredPropertySegment(){
    	return TypeOfPropertyLossCode._5.code.equals(typeOfPropertyLoss);
    }
    
	@Override
	public String toString() {
		return "PropertySegment [typeOfPropertyLoss=" + typeOfPropertyLoss + ", propertyDescription=" + Arrays.toString(propertyDescription) + ", valueOfProperty=" + Arrays.toString(valueOfProperty)
				+ ", dateRecovered=" + Arrays.toString(dateRecovered) + ", numberOfStolenMotorVehicles=" + numberOfStolenMotorVehicles + ", numberOfRecoveredMotorVehicles="
				+ numberOfRecoveredMotorVehicles + ", suspectedDrugType=" + Arrays.toString(suspectedDrugType) + ", estimatedDrugQuantity=" + Arrays.toString(estimatedDrugQuantity)
				+ ", typeDrugMeasurement=" + Arrays.toString(typeDrugMeasurement) + ", populatedPropertyDescriptionCount=" + populatedPropertyDescriptionCount + ", populatedSuspectedDrugTypeCount="
				+ populatedSuspectedDrugTypeCount + "]";
	}
    
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(dateRecovered);
		result = prime * result + Arrays.hashCode(estimatedDrugQuantity);
		result = prime * result + ((numberOfRecoveredMotorVehicles == null) ? 0 : numberOfRecoveredMotorVehicles.hashCode());
		result = prime * result + ((numberOfStolenMotorVehicles == null) ? 0 : numberOfStolenMotorVehicles.hashCode());
		result = prime * result + populatedPropertyDescriptionCount;
		result = prime * result + populatedSuspectedDrugTypeCount;
		result = prime * result + Arrays.hashCode(propertyDescription);
		result = prime * result + Arrays.hashCode(suspectedDrugType);
		result = prime * result + Arrays.hashCode(typeDrugMeasurement);
		result = prime * result + ((typeOfPropertyLoss == null) ? 0 : typeOfPropertyLoss.hashCode());
		result = prime * result + Arrays.hashCode(valueOfProperty);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.hashCode() == hashCode();
	}

	@Override
	public Object getWithinSegmentIdentifier() {
		return typeOfPropertyLoss;
	}

    @Override
    public String getSegmentLength()
    {
        return PROPERTY_SEGMENT_LENGTH;
    }
	
	public Map<String, Integer> getPropertyDescriptionValueMap(){
		Map<String, Integer> descriptionValueMap = new HashMap<String, Integer>();
		for (int i=0; i<PROPERTY_DESCRIPTION_COUNT; i++ ){
			if (StringUtils.isNotBlank(this.getPropertyDescription(i))){
				descriptionValueMap.put(StringUtils.trim(this.getPropertyDescription(i)), this.getValueOfProperty(i).getValue());
			}
		}
		
		return descriptionValueMap;
	}
	
	public boolean isSuspectedDrugTypeMandatory() {
		GroupAIncidentReport parent = (GroupAIncidentReport) this.getParentReport();
		boolean drugOffense = parent.isOffenseInvolved(OffenseCode._35A);
		boolean isRule392Exception2 = parent.isRule392Exception2();
		boolean otherOffenseRequirePropertySegment = parent.containsOtherCrimeRequirePropertySegment(OffenseCode._35A.code);

		String typeOfPropertyLoss = this.getTypeOfPropertyLoss();
		boolean containsPropertyDescription10 = this.containsPropertyDescription(PropertyDescriptionCode._10.code);

		boolean mandatoryField = !otherOffenseRequirePropertySegment && drugOffense 
				&& ((Objects.equals(typeOfPropertyLoss, TypeOfPropertyLossCode._6.code) && containsPropertyDescription10 ) 
					|| (Objects.equals(typeOfPropertyLoss, TypeOfPropertyLossCode._1.code) && !isRule392Exception2) );
		return mandatoryField;
	}
	


}
