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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.search.nibrs.stagingdata.model.BiasMotivationType;
import org.search.nibrs.stagingdata.model.LocationType;
import org.search.nibrs.stagingdata.model.MethodOfEntryType;
import org.search.nibrs.stagingdata.model.OffenderSuspectedOfUsingType;
import org.search.nibrs.stagingdata.model.SegmentActionTypeType;
import org.search.nibrs.stagingdata.model.TypeOfCriminalActivityType;
import org.search.nibrs.stagingdata.model.TypeOfWeaponForceInvolved;
import org.search.nibrs.stagingdata.model.UcrOffenseCodeType;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@JsonIdentityInfo(
	generator = ObjectIdGenerators.PropertyGenerator.class, 
	property = "offenseSegmentId", 
	scope=OffenseSegment.class)
public class OffenseSegment implements Serializable{
	private static final long serialVersionUID = 6355433666143334140L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer offenseSegmentId;
	
	@ManyToOne
	@JoinColumn(name="segmentActionTypeTypeID") 
	private SegmentActionTypeType segmentActionType; 
	@ManyToOne
	@JoinColumn(name="administrativeSegmentId")
	private AdministrativeSegment administrativeSegment; 
	@ManyToOne
	@JoinColumn(name="ucrOffenseCodeTypeId")
	private UcrOffenseCodeType ucrOffenseCodeType;
	
	private String offenseAttemptedCompleted;
	
	@ManyToOne
	@JoinColumn(name="locationTypeTypeId")
	private LocationType locationType;
	
	private Integer numberOfPremisesEntered;
	
	@ManyToOne
	@JoinColumn(name="methodOfEntryTypeID")
	private MethodOfEntryType methodOfEntryType;
	
	@OneToMany(mappedBy = "offenseSegment", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private Set<TypeOfWeaponForceInvolved> typeOfWeaponForceInvolveds = new HashSet<>();
	
	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(name = "offenderSuspectedOfUsing", 
	joinColumns = @JoinColumn(name = "offenseSegmentId", referencedColumnName = "offenseSegmentId"), 
	inverseJoinColumns = @JoinColumn(name = "offenderSuspectedOfUsingTypeId", referencedColumnName = "offenderSuspectedOfUsingTypeId"))
	private Set<OffenderSuspectedOfUsingType> offenderSuspectedOfUsingTypes = new HashSet<>();     
	
	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(name = "typeCriminalActivity", 
	joinColumns = @JoinColumn(name = "offenseSegmentId", referencedColumnName = "offenseSegmentId"), 
	inverseJoinColumns = @JoinColumn(name = "typeOfCriminalActivityTypeId", referencedColumnName = "typeOfCriminalActivityTypeId"))
	private Set<TypeOfCriminalActivityType> typeOfCriminalActivityTypes = new HashSet<>();  
	
	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(name = "biasMotivation", 
	joinColumns = @JoinColumn(name = "offenseSegmentId", referencedColumnName = "offenseSegmentId"), 
	inverseJoinColumns = @JoinColumn(name = "biasMotivationTypeId", referencedColumnName = "biasMotivationTypeId"))
	private Set<BiasMotivationType> biasMotivationTypes = new HashSet<>();  
	
	@ManyToMany(mappedBy = "offenseSegments", fetch=FetchType.EAGER)
	private Set<VictimSegment> victimSegments = new HashSet<>();
	
	public OffenseSegment() {
		super();
	}
	public String toString(){
		ReflectionToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE);
		String resultWithoutParentSegment = ReflectionToStringBuilder.toStringExclude(this, "administrativeSegment", "victimSegments");
		int index = StringUtils.indexOf(resultWithoutParentSegment, ",");
		
		StringBuilder sb = new StringBuilder(resultWithoutParentSegment);
		sb.insert(index + 1, "administrativeSegmentId=" + administrativeSegment.getAdministrativeSegmentId() + ",");
		return sb.toString();
	}
	public SegmentActionTypeType getSegmentActionType() {
		return segmentActionType;
	}
	public void setSegmentActionType(SegmentActionTypeType segmentActionType) {
		this.segmentActionType = segmentActionType;
	}
	public String getOffenseAttemptedCompleted() {
		return offenseAttemptedCompleted;
	}
	public void setOffenseAttemptedCompleted(String offenseAttemptedCompleted) {
		this.offenseAttemptedCompleted = offenseAttemptedCompleted;
	}
	public Integer getOffenseSegmentId() {
		return offenseSegmentId;
	}
	public void setOffenseSegmentId(Integer offenseSegmentId) {
		this.offenseSegmentId = offenseSegmentId;
	}
	public AdministrativeSegment getAdministrativeSegment() {
		return administrativeSegment;
	}
	public void setAdministrativeSegment(AdministrativeSegment administrativeSegment) {
		this.administrativeSegment = administrativeSegment;
	}
	public UcrOffenseCodeType getUcrOffenseCodeType() {
		return ucrOffenseCodeType;
	}
	public void setUcrOffenseCodeType(UcrOffenseCodeType ucrOffenseCodeType) {
		this.ucrOffenseCodeType = ucrOffenseCodeType;
	}
	public LocationType getLocationType() {
		return locationType;
	}
	public void setLocationType(LocationType locationType) {
		this.locationType = locationType;
	}
	public Integer getNumberOfPremisesEntered() {
		return numberOfPremisesEntered;
	}
	public void setNumberOfPremisesEntered(Integer numberOfPremisesEntered) {
		this.numberOfPremisesEntered = numberOfPremisesEntered;
	}
	public MethodOfEntryType getMethodOfEntryType() {
		return methodOfEntryType;
	}
	public void setMethodOfEntryType(MethodOfEntryType methodOfEntryType) {
		this.methodOfEntryType = methodOfEntryType;
	}
	public Set<OffenderSuspectedOfUsingType> getOffenderSuspectedOfUsingTypes() {
		return offenderSuspectedOfUsingTypes;
	}
	public void setOffenderSuspectedOfUsingTypes(Set<OffenderSuspectedOfUsingType> offenderSuspectedOfUsingTypes) {
		this.offenderSuspectedOfUsingTypes = offenderSuspectedOfUsingTypes;
	}
	public Set<TypeOfCriminalActivityType> getTypeOfCriminalActivityTypes() {
		return typeOfCriminalActivityTypes;
	}
	public void setTypeOfCriminalActivityTypes(Set<TypeOfCriminalActivityType> typeOfCriminalActivityTypes) {
		this.typeOfCriminalActivityTypes = typeOfCriminalActivityTypes;
	}
	public Set<TypeOfWeaponForceInvolved> getTypeOfWeaponForceInvolveds() {
		return typeOfWeaponForceInvolveds;
	}
	public void setTypeOfWeaponForceInvolveds(Set<TypeOfWeaponForceInvolved> typeOfWeaponForceInvolveds) {
		this.typeOfWeaponForceInvolveds = typeOfWeaponForceInvolveds;
	}
	public Set<BiasMotivationType> getBiasMotivationTypes() {
		return biasMotivationTypes;
	}
	public void setBiasMotivationTypes(Set<BiasMotivationType> biasMotivationTypes) {
		this.biasMotivationTypes = biasMotivationTypes;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((administrativeSegment == null) ? 0 : administrativeSegment.hashCode());
		result = prime * result + ((biasMotivationTypes == null) ? 0 : biasMotivationTypes.hashCode());
		result = prime * result + ((locationType == null) ? 0 : locationType.hashCode());
		result = prime * result + ((methodOfEntryType == null) ? 0 : methodOfEntryType.hashCode());
		result = prime * result + ((numberOfPremisesEntered == null) ? 0 : numberOfPremisesEntered.hashCode());
		result = prime * result
				+ ((offenderSuspectedOfUsingTypes == null) ? 0 : offenderSuspectedOfUsingTypes.hashCode());
		result = prime * result + ((offenseAttemptedCompleted == null) ? 0 : offenseAttemptedCompleted.hashCode());
		result = prime * result + ((offenseSegmentId == null) ? 0 : offenseSegmentId.hashCode());
		result = prime * result + ((segmentActionType == null) ? 0 : segmentActionType.hashCode());
		result = prime * result + ((typeOfCriminalActivityTypes == null) ? 0 : typeOfCriminalActivityTypes.hashCode());
		result = prime * result + ((typeOfWeaponForceInvolveds == null) ? 0 : typeOfWeaponForceInvolveds.hashCode());
		result = prime * result + ((ucrOffenseCodeType == null) ? 0 : ucrOffenseCodeType.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OffenseSegment other = (OffenseSegment) obj;
		if (administrativeSegment == null) {
			if (other.administrativeSegment != null)
				return false;
		} else if (!administrativeSegment.equals(other.administrativeSegment))
			return false;
		if (biasMotivationTypes == null) {
			if (other.biasMotivationTypes != null)
				return false;
		} else if (!biasMotivationTypes.equals(other.biasMotivationTypes))
			return false;
		if (locationType == null) {
			if (other.locationType != null)
				return false;
		} else if (!locationType.equals(other.locationType))
			return false;
		if (methodOfEntryType == null) {
			if (other.methodOfEntryType != null)
				return false;
		} else if (!methodOfEntryType.equals(other.methodOfEntryType))
			return false;
		if (numberOfPremisesEntered == null) {
			if (other.numberOfPremisesEntered != null)
				return false;
		} else if (!numberOfPremisesEntered.equals(other.numberOfPremisesEntered))
			return false;
		if (offenderSuspectedOfUsingTypes == null) {
			if (other.offenderSuspectedOfUsingTypes != null)
				return false;
		} else if (!offenderSuspectedOfUsingTypes.equals(other.offenderSuspectedOfUsingTypes))
			return false;
		if (offenseAttemptedCompleted == null) {
			if (other.offenseAttemptedCompleted != null)
				return false;
		} else if (!offenseAttemptedCompleted.equals(other.offenseAttemptedCompleted))
			return false;
		if (offenseSegmentId == null) {
			if (other.offenseSegmentId != null)
				return false;
		} else if (!offenseSegmentId.equals(other.offenseSegmentId))
			return false;
		if (segmentActionType == null) {
			if (other.segmentActionType != null)
				return false;
		} else if (!segmentActionType.equals(other.segmentActionType))
			return false;
		if (typeOfCriminalActivityTypes == null) {
			if (other.typeOfCriminalActivityTypes != null)
				return false;
		} else if (!typeOfCriminalActivityTypes.equals(other.typeOfCriminalActivityTypes))
			return false;
		if (typeOfWeaponForceInvolveds == null) {
			if (other.typeOfWeaponForceInvolveds != null)
				return false;
		} else if (!typeOfWeaponForceInvolveds.equals(other.typeOfWeaponForceInvolveds))
			return false;
		if (ucrOffenseCodeType == null) {
			if (other.ucrOffenseCodeType != null)
				return false;
		} else if (!ucrOffenseCodeType.equals(other.ucrOffenseCodeType))
			return false;
		if (victimSegments == null) {
			if (other.victimSegments != null)
				return false;
		} else if (!victimSegments.equals(other.victimSegments))
			return false;
		return true;
	}
}
