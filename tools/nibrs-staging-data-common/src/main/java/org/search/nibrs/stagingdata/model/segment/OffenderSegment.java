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
package org.search.nibrs.stagingdata.model.segment;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.search.nibrs.stagingdata.model.EthnicityOfPersonType;
import org.search.nibrs.stagingdata.model.RaceOfPersonType;
import org.search.nibrs.stagingdata.model.SegmentActionTypeType;
import org.search.nibrs.stagingdata.model.SexOfPersonType;
import org.search.nibrs.stagingdata.model.VictimOffenderAssociation;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@JsonIdentityInfo(
	generator = ObjectIdGenerators.PropertyGenerator.class, 
	property = "offenderSegmentId", 
	scope = OffenderSegment.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OffenderSegment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer offenderSegmentId;
	
	@ManyToOne
	@JoinColumn(name="administrativeSegmentId") 
	private AdministrativeSegment administrativeSegment; 
	@ManyToOne
	@JoinColumn(name="segmentActionTypeTypeID") 
	private SegmentActionTypeType segmentActionType; 
	private Integer offenderSequenceNumber; 

	private Integer ageOfOffenderMin; 
	private Integer ageOfOffenderMax;
	private Integer ageNumOffender;
	private String nonNumericAge; 
	@ManyToOne
	@JoinColumn(name="sexOfPersonTypeId") 
	private SexOfPersonType sexOfPersonType; 
	@ManyToOne
	@JoinColumn(name="raceOfPersonTypeId") 
	private RaceOfPersonType raceOfPersonType;
	@ManyToOne
	@JoinColumn(name="ethnicityOfPersonTypeId")
	private EthnicityOfPersonType ethnicityOfPersonType;
	
	@OneToMany(mappedBy = "offenderSegment", fetch=FetchType.EAGER, cascade = CascadeType.PERSIST, orphanRemoval = true)
	private Set<VictimOffenderAssociation> victimOffenderAssociations = new HashSet<VictimOffenderAssociation>();
	
	public String toString(){
		ReflectionToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE);
		String resultWithoutParentSegment = 
				ReflectionToStringBuilder.toStringExclude(this, "administrativeSegment");
		int index = StringUtils.indexOf(resultWithoutParentSegment, ",");
		
		StringBuilder sb = new StringBuilder(resultWithoutParentSegment);
		sb.insert(index + 1, "administrativeSegmentId=" + administrativeSegment.getAdministrativeSegmentId() + ",");
		return sb.toString();
	}
	public EthnicityOfPersonType getEthnicityOfPersonType() {
		return ethnicityOfPersonType;
	}
	public void setEthnicityOfPersonType(EthnicityOfPersonType ethnicityOfPersonType) {
		this.ethnicityOfPersonType = ethnicityOfPersonType;
	}
	public RaceOfPersonType getRaceOfPersonType() {
		return raceOfPersonType;
	}
	public void setRaceOfPersonType(RaceOfPersonType raceOfPersonType) {
		this.raceOfPersonType = raceOfPersonType;
	}
	public SexOfPersonType getSexOfPersonType() {
		return sexOfPersonType;
	}
	public void setSexOfPersonType(SexOfPersonType sexOfPersonType) {
		this.sexOfPersonType = sexOfPersonType;
	}
	public SegmentActionTypeType getSegmentActionType() {
		return segmentActionType;
	}
	public void setSegmentActionType(SegmentActionTypeType segmentActionType) {
		this.segmentActionType = segmentActionType;
	}
	public AdministrativeSegment getAdministrativeSegment() {
		return administrativeSegment;
	}
	public void setAdministrativeSegment(AdministrativeSegment administrativeSegment) {
		this.administrativeSegment = administrativeSegment;
	}
	public Integer getOffenderSequenceNumber() {
		return offenderSequenceNumber;
	}
	public void setOffenderSequenceNumber(Integer offenderSequenceNumber) {
		this.offenderSequenceNumber = offenderSequenceNumber;
	}
	public Integer getAgeOfOffenderMin() {
		return ageOfOffenderMin;
	}
	public void setAgeOfOffenderMin(Integer ageOfOffenderMin) {
		this.ageOfOffenderMin = ageOfOffenderMin;
	}
	public Integer getAgeOfOffenderMax() {
		return ageOfOffenderMax;
	}
	public void setAgeOfOffenderMax(Integer ageOfOffenderMax) {
		this.ageOfOffenderMax = ageOfOffenderMax;
	}
	public Integer getOffenderSegmentId() {
		return offenderSegmentId;
	}
	public void setOffenderSegmentId(Integer offenderSegmentId) {
		this.offenderSegmentId = offenderSegmentId;
	}
	public Set<VictimOffenderAssociation> getVictimOffenderAssociations() {
		return victimOffenderAssociations;
	}
	public void setVictimOffenderAssociations(Set<VictimOffenderAssociation> victimOffenderAssociations) {
		this.victimOffenderAssociations = victimOffenderAssociations;
	}
	@JsonIgnore
    public boolean isJuvenile() {
    	boolean ret = false; 
    	if ( !isAgeUnknown() ) {
    		ret = ageOfOffenderMax < 18 || (ageOfOffenderMin < 18 && getAverageAge() < 18);
    	}
    	return ret;
    }
    
	@JsonIgnore
    public boolean isAdult() {
    	boolean ret = false; 
    	if ( !isAgeUnknown() ) {
    		ret = ageOfOffenderMin >= 18 || (ageOfOffenderMax >= 18 && getAverageAge() >= 18);
    	}
    	return ret;
    }
    
	@JsonIgnore
	public Integer getAverageAge() {
		Integer ret = null;
		if (!isAgeUnknown() && ageOfOffenderMin != null) {
			double min = ageOfOffenderMin.doubleValue();
			double max = ageOfOffenderMax.doubleValue();
			double average = (min + max) / 2.0;
			ret = new Integer((int) average);
		}
		return ret;
	}

	@JsonIgnore
    public boolean isAgeUnknown() {
    	// set forth in rule for data element 52
    	return ageOfOffenderMax == null && ageOfOffenderMin == null && Objects.equals(nonNumericAge, "00");
    }
	public String getNonNumericAge() {
		return nonNumericAge;
	}
	public void setNonNumericAge(String nonNumericAge) {
		this.nonNumericAge = nonNumericAge;
	}

	@JsonIgnore
	public String getAgeString() {
		String ageString = StringUtils.EMPTY;
		
		Integer averageAge = getAverageAge();
		if (averageAge != null) {
			ageString = StringUtils.leftPad(averageAge.toString(), 2, '0'); 
		}
		else if (nonNumericAge != null) {
			ageString = nonNumericAge;
		}
		return ageString;
	}
	public Integer getAgeNumOffender() {
		return ageNumOffender;
	}
	public void setAgeNumOffender(Integer ageNumOffender) {
		this.ageNumOffender = ageNumOffender;
	}
}
