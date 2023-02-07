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
import org.search.nibrs.stagingdata.model.PropertyType;
import org.search.nibrs.stagingdata.model.SegmentActionTypeType;
import org.search.nibrs.stagingdata.model.SuspectedDrugType;
import org.search.nibrs.stagingdata.model.TypePropertyLossEtcType;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@JsonIdentityInfo(
	generator = ObjectIdGenerators.PropertyGenerator.class, 
	property = "propertySegmentId")
public class PropertySegment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer propertySegmentId;
	
	@ManyToOne
	@JoinColumn(name="segmentActionTypeTypeID") 
	private SegmentActionTypeType segmentActionType; 
	@ManyToOne
	@JoinColumn(name="administrativeSegmentId") 
	private AdministrativeSegment administrativeSegment; 
	@ManyToOne
	@JoinColumn(name="typePropertyLossEtcTypeId")
	private TypePropertyLossEtcType typePropertyLossEtcType;
	
	@OneToMany(mappedBy = "propertySegment", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private Set<SuspectedDrugType> suspectedDrugTypes = new HashSet<SuspectedDrugType>();

	@OneToMany(mappedBy = "propertySegment", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private Set<PropertyType> propertyTypes = new HashSet<PropertyType>();
	
	private Integer numberOfStolenMotorVehicles;
	private Integer numberOfRecoveredMotorVehicles;
	
	public PropertySegment() {
		super();
	}
	
	public String toString(){
		ReflectionToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE);
		String resultWithoutOffenseSegment = ReflectionToStringBuilder.toStringExclude(this, "administrativeSegment");
		int index = StringUtils.indexOf(resultWithoutOffenseSegment, ",");
		
		StringBuilder sb = new StringBuilder(resultWithoutOffenseSegment);
		sb.insert(index + 1, "administrativeSegmentId=" + administrativeSegment.getAdministrativeSegmentId() + ",");
		return sb.toString();
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
	public Integer getPropertySegmentId() {
		return propertySegmentId;
	}

	public void setPropertySegmentId(Integer propertySegmentId) {
		this.propertySegmentId = propertySegmentId;
	}

	public TypePropertyLossEtcType getTypePropertyLossEtcType() {
		return typePropertyLossEtcType;
	}

	public void setTypePropertyLossEtcType(TypePropertyLossEtcType typePropertyLossEtcType) {
		this.typePropertyLossEtcType = typePropertyLossEtcType;
	}

	public Integer getNumberOfStolenMotorVehicles() {
		return numberOfStolenMotorVehicles;
	}

	public void setNumberOfStolenMotorVehicles(Integer numberOfStolenMotorVehicles) {
		this.numberOfStolenMotorVehicles = numberOfStolenMotorVehicles;
	}

	public Integer getNumberOfRecoveredMotorVehicles() {
		return numberOfRecoveredMotorVehicles;
	}

	public void setNumberOfRecoveredMotorVehicles(Integer numberOfRecoveredMotorVehicles) {
		this.numberOfRecoveredMotorVehicles = numberOfRecoveredMotorVehicles;
	}

	public Set<SuspectedDrugType> getSuspectedDrugTypes() {
		return suspectedDrugTypes;
	}

	public void setSuspectedDrugTypes(Set<SuspectedDrugType> suspectedDrugTypes) {
		this.suspectedDrugTypes = suspectedDrugTypes;
	}

	public Set<PropertyType> getPropertyTypes() {
		return propertyTypes;
	}

	public void setPropertyTypes(Set<PropertyType> propertyTypes) {
		this.propertyTypes = propertyTypes;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((administrativeSegment == null) ? 0 : administrativeSegment.hashCode());
		result = prime * result
				+ ((numberOfRecoveredMotorVehicles == null) ? 0 : numberOfRecoveredMotorVehicles.hashCode());
		result = prime * result + ((numberOfStolenMotorVehicles == null) ? 0 : numberOfStolenMotorVehicles.hashCode());
		result = prime * result + ((propertySegmentId == null) ? 0 : propertySegmentId.hashCode());
		result = prime * result + ((propertyTypes == null) ? 0 : propertyTypes.hashCode());
		result = prime * result + ((segmentActionType == null) ? 0 : segmentActionType.hashCode());
		result = prime * result + ((suspectedDrugTypes == null) ? 0 : suspectedDrugTypes.hashCode());
		result = prime * result + ((typePropertyLossEtcType == null) ? 0 : typePropertyLossEtcType.hashCode());
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
		PropertySegment other = (PropertySegment) obj;
		if (administrativeSegment == null) {
			if (other.administrativeSegment != null)
				return false;
		} else if (!administrativeSegment.getIncidentNumber().equals(other.administrativeSegment.getIncidentNumber()))
			return false;
		if (numberOfRecoveredMotorVehicles == null) {
			if (other.numberOfRecoveredMotorVehicles != null)
				return false;
		} else if (!numberOfRecoveredMotorVehicles.equals(other.numberOfRecoveredMotorVehicles))
			return false;
		if (numberOfStolenMotorVehicles == null) {
			if (other.numberOfStolenMotorVehicles != null)
				return false;
		} else if (!numberOfStolenMotorVehicles.equals(other.numberOfStolenMotorVehicles))
			return false;
		if (propertySegmentId == null) {
			if (other.propertySegmentId != null)
				return false;
		} else if (!propertySegmentId.equals(other.propertySegmentId))
			return false;
		if (propertyTypes == null) {
			if (other.propertyTypes != null)
				return false;
		} else if (!propertyTypes.equals(other.propertyTypes))
			return false;
		if (segmentActionType == null) {
			if (other.segmentActionType != null)
				return false;
		} else if (!segmentActionType.equals(other.segmentActionType))
			return false;
		if (suspectedDrugTypes == null) {
			if (other.suspectedDrugTypes != null)
				return false;
		} else if (!suspectedDrugTypes.equals(other.suspectedDrugTypes))
			return false;
		if (typePropertyLossEtcType == null) {
			if (other.typePropertyLossEtcType != null)
				return false;
		} else if (!typePropertyLossEtcType.equals(other.typePropertyLossEtcType))
			return false;
		return true;
	}
}
