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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.search.nibrs.stagingdata.model.AdditionalJustifiableHomicideCircumstancesType;
import org.search.nibrs.stagingdata.model.AggravatedAssaultHomicideCircumstancesType;
import org.search.nibrs.stagingdata.model.EthnicityOfPersonType;
import org.search.nibrs.stagingdata.model.OfficerActivityCircumstanceType;
import org.search.nibrs.stagingdata.model.OfficerAssignmentTypeType;
import org.search.nibrs.stagingdata.model.RaceOfPersonType;
import org.search.nibrs.stagingdata.model.ResidentStatusOfPersonType;
import org.search.nibrs.stagingdata.model.SegmentActionTypeType;
import org.search.nibrs.stagingdata.model.SexOfPersonType;
import org.search.nibrs.stagingdata.model.TypeInjuryType;
import org.search.nibrs.stagingdata.model.TypeOfVictimType;
import org.search.nibrs.stagingdata.model.VictimOffenderAssociation;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
@JsonIdentityInfo(
	generator = ObjectIdGenerators.PropertyGenerator.class, 
	property = "victimSegmentId", scope=VictimSegment.class)
@JsonSerialize(as=VictimSegment.class)
public class VictimSegment implements Serializable{
	private static final long serialVersionUID = 1947393606928402316L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer victimSegmentId;
	
	@ManyToOne
	@JoinColumn(name="segmentActionTypeTypeId") 
	private SegmentActionTypeType segmentActionType; 
	@ManyToOne
	@JoinColumn(name="administrativeSegmentId") 
	private AdministrativeSegment administrativeSegment; 
	private Integer victimSequenceNumber; 
	
	@ManyToOne
	@JoinColumn(name="typeOfVictimTypeId") 
	private TypeOfVictimType typeOfVictimType;
	
	@ManyToOne
	@JoinColumn(name="officerActivityCircumstanceTypeId") 
	private OfficerActivityCircumstanceType officerActivityCircumstanceType;
	
	@ManyToOne
	@JoinColumn(name="officerAssignmentTypeTypeId") 
	private OfficerAssignmentTypeType officerAssignmentTypeType;
	
	private String officerOtherJurisdictionOri; 
	private Integer ageOfVictimMin; 
	private Integer ageOfVictimMax; 
	private Integer ageNumVictim; 
	private Integer ageNeonateIndicator;
	private Integer ageFirstWeekIndicator;
	private Integer ageFirstYearIndicator;
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
	@ManyToOne
	@JoinColumn(name="residentStatusOfPersonTypeId") 
	private ResidentStatusOfPersonType residentStatusOfPersonType;
	@ManyToOne
	@JoinColumn(name="additionalJustifiableHomicideCircumstancesTypeId")
	private AdditionalJustifiableHomicideCircumstancesType additionalJustifiableHomicideCircumstancesType;
	
	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(name = "typeInjury", 
	joinColumns = @JoinColumn(name = "victimSegmentId", referencedColumnName = "victimSegmentId"), 
	inverseJoinColumns = @JoinColumn(name = "typeInjuryTypeId", referencedColumnName = "typeInjuryTypeId"))
	private Set<TypeInjuryType> typeInjuryTypes = new HashSet<TypeInjuryType>();     
	
	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(name = "victimOffenseAssociation", 
	joinColumns = @JoinColumn(name = "victimSegmentId", referencedColumnName = "victimSegmentId"), 
	inverseJoinColumns = @JoinColumn(name = "offenseSegmentId", referencedColumnName = "offenseSegmentId"))
	private Set<OffenseSegment> offenseSegments = new HashSet<OffenseSegment>();     
	
	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(name = "aggravatedAssaultHomicideCircumstances", 
	joinColumns = @JoinColumn(name = "victimSegmentId", referencedColumnName = "victimSegmentId"), 
	inverseJoinColumns = @JoinColumn(name = "aggravatedAssaultHomicideCircumstancesTypeId", 
				referencedColumnName = "aggravatedAssaultHomicideCircumstancesTypeId"))
	private Set<AggravatedAssaultHomicideCircumstancesType> aggravatedAssaultHomicideCircumstancesTypes = new HashSet<AggravatedAssaultHomicideCircumstancesType>();     
	
	@OneToMany(mappedBy = "victimSegment", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private Set<VictimOffenderAssociation> VictimOffenderAssociations = new HashSet<VictimOffenderAssociation>();
	
	public String toString(){
		ReflectionToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE);
		String resultWithoutParentSegment = ReflectionToStringBuilder.toStringExclude(this, "administrativeSegment", "victimSegments");
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
	public ResidentStatusOfPersonType getResidentStatusOfPersonType() {
		return residentStatusOfPersonType;
	}
	public void setResidentStatusOfPersonType(ResidentStatusOfPersonType residentStatusOfPersonType) {
		this.residentStatusOfPersonType = residentStatusOfPersonType;
	}
	public AdministrativeSegment getAdministrativeSegment() {
		return administrativeSegment;
	}
	public void setAdministrativeSegment(AdministrativeSegment administrativeSegment) {
		this.administrativeSegment = administrativeSegment;
	}
	public Integer getVictimSequenceNumber() {
		return victimSequenceNumber;
	}
	public void setVictimSequenceNumber(Integer victimSequenceNumber) {
		this.victimSequenceNumber = victimSequenceNumber;
	}
	public TypeOfVictimType getTypeOfVictimType() {
		return typeOfVictimType;
	}
	public void setTypeOfVictimType(TypeOfVictimType typeOfVictimType) {
		this.typeOfVictimType = typeOfVictimType;
	}
	public OfficerActivityCircumstanceType getOfficerActivityCircumstanceType() {
		return officerActivityCircumstanceType;
	}
	public void setOfficerActivityCircumstanceType(OfficerActivityCircumstanceType officerActivityCircumstanceType) {
		this.officerActivityCircumstanceType = officerActivityCircumstanceType;
	}
	public OfficerAssignmentTypeType getOfficerAssignmentTypeType() {
		return officerAssignmentTypeType;
	}
	public void setOfficerAssignmentTypeType(OfficerAssignmentTypeType officerAssignmentTypeType) {
		this.officerAssignmentTypeType = officerAssignmentTypeType;
	}
	public Integer getAgeOfVictimMin() {
		return ageOfVictimMin;
	}
	public void setAgeOfVictimMin(Integer ageOfVictimMin) {
		this.ageOfVictimMin = ageOfVictimMin;
	}
	public Integer getAgeOfVictimMax() {
		return ageOfVictimMax;
	}
	public void setAgeOfVictimMax(Integer ageOfVictimMax) {
		this.ageOfVictimMax = ageOfVictimMax;
	}
	public Integer getAgeNeonateIndicator() {
		return ageNeonateIndicator;
	}
	public void setAgeNeonateIndicator(Integer ageNeonateIndicator) {
		this.ageNeonateIndicator = ageNeonateIndicator;
	}
	public Integer getAgeFirstWeekIndicator() {
		return ageFirstWeekIndicator;
	}
	public void setAgeFirstWeekIndicator(Integer ageFirstWeekIndicator) {
		this.ageFirstWeekIndicator = ageFirstWeekIndicator;
	}
	public Integer getAgeFirstYearIndicator() {
		return ageFirstYearIndicator;
	}
	public void setAgeFirstYearIndicator(Integer ageFirstYearIndicator) {
		this.ageFirstYearIndicator = ageFirstYearIndicator;
	}
	public AdditionalJustifiableHomicideCircumstancesType getAdditionalJustifiableHomicideCircumstancesType() {
		return additionalJustifiableHomicideCircumstancesType;
	}
	public void setAdditionalJustifiableHomicideCircumstancesType(
			AdditionalJustifiableHomicideCircumstancesType additionalJustifiableHomicideCircumstancesType) {
		this.additionalJustifiableHomicideCircumstancesType = additionalJustifiableHomicideCircumstancesType;
	}
	public Set<TypeInjuryType> getTypeInjuryTypes() {
		return typeInjuryTypes;
	}
	public void setTypeInjuryTypes(Set<TypeInjuryType> typeInjuryTypes) {
		this.typeInjuryTypes = typeInjuryTypes;
	}
	public Set<OffenseSegment> getOffenseSegments() {
		return offenseSegments;
	}
	public void setOffenseSegments(Set<OffenseSegment> offenseSegments) {
		this.offenseSegments = offenseSegments;
	}
	public Set<AggravatedAssaultHomicideCircumstancesType> getAggravatedAssaultHomicideCircumstancesTypes() {
		return aggravatedAssaultHomicideCircumstancesTypes;
	}
	public void setAggravatedAssaultHomicideCircumstancesTypes(Set<AggravatedAssaultHomicideCircumstancesType> aggravatedAssaultHomicideCircumstancesTypes) {
		this.aggravatedAssaultHomicideCircumstancesTypes = aggravatedAssaultHomicideCircumstancesTypes;
	}
	public Integer getVictimSegmentId() {
		return victimSegmentId;
	}
	public void setVictimSegmentId(Integer victimSegmentId) {
		this.victimSegmentId = victimSegmentId;
	}
	public Set<VictimOffenderAssociation> getVictimOffenderAssociations() {
		return VictimOffenderAssociations;
	}
	public void setVictimOffenderAssociations(Set<VictimOffenderAssociation> victimOffenderAssociations) {
		VictimOffenderAssociations = victimOffenderAssociations;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((VictimOffenderAssociations == null) ? 0 : VictimOffenderAssociations.hashCode());
		result = prime * result + ((additionalJustifiableHomicideCircumstancesType == null) ? 0
				: additionalJustifiableHomicideCircumstancesType.hashCode());
		result = prime * result + ((administrativeSegment == null) ? 0 : administrativeSegment.hashCode());
		result = prime * result + ((ageFirstWeekIndicator == null) ? 0 : ageFirstWeekIndicator.hashCode());
		result = prime * result + ((ageFirstYearIndicator == null) ? 0 : ageFirstYearIndicator.hashCode());
		result = prime * result + ((ageNeonateIndicator == null) ? 0 : ageNeonateIndicator.hashCode());
		result = prime * result + ((ageOfVictimMax == null) ? 0 : ageOfVictimMax.hashCode());
		result = prime * result + ((ageOfVictimMin == null) ? 0 : ageOfVictimMin.hashCode());
		result = prime * result + ((nonNumericAge == null) ? 0 : nonNumericAge.hashCode());
		result = prime * result + ((aggravatedAssaultHomicideCircumstancesTypes == null) ? 0
				: aggravatedAssaultHomicideCircumstancesTypes.hashCode());
		result = prime * result + ((ethnicityOfPersonType == null) ? 0 : ethnicityOfPersonType.hashCode());
		result = prime * result + ((offenseSegments == null) ? 0 : offenseSegments.hashCode());
		result = prime * result
				+ ((officerActivityCircumstanceType == null) ? 0 : officerActivityCircumstanceType.hashCode());
		result = prime * result + ((officerAssignmentTypeType == null) ? 0 : officerAssignmentTypeType.hashCode());
		result = prime * result + ((raceOfPersonType == null) ? 0 : raceOfPersonType.hashCode());
		result = prime * result + ((residentStatusOfPersonType == null) ? 0 : residentStatusOfPersonType.hashCode());
		result = prime * result + ((segmentActionType == null) ? 0 : segmentActionType.hashCode());
		result = prime * result + ((sexOfPersonType == null) ? 0 : sexOfPersonType.hashCode());
		result = prime * result + ((typeInjuryTypes == null) ? 0 : typeInjuryTypes.hashCode());
		result = prime * result + ((typeOfVictimType == null) ? 0 : typeOfVictimType.hashCode());
		result = prime * result + ((victimSegmentId == null) ? 0 : victimSegmentId.hashCode());
		result = prime * result + ((victimSequenceNumber == null) ? 0 : victimSequenceNumber.hashCode());
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
		VictimSegment other = (VictimSegment) obj;
		if (VictimOffenderAssociations == null) {
			if (other.VictimOffenderAssociations != null)
				return false;
		} else if (!VictimOffenderAssociations.equals(other.VictimOffenderAssociations))
			return false;
		if (additionalJustifiableHomicideCircumstancesType == null) {
			if (other.additionalJustifiableHomicideCircumstancesType != null)
				return false;
		} else if (!additionalJustifiableHomicideCircumstancesType
				.equals(other.additionalJustifiableHomicideCircumstancesType))
			return false;
		if (administrativeSegment == null) {
			if (other.administrativeSegment != null)
				return false;
		} else if (!administrativeSegment.equals(other.administrativeSegment))
			return false;
		if (ageFirstWeekIndicator == null) {
			if (other.ageFirstWeekIndicator != null)
				return false;
		} else if (!ageFirstWeekIndicator.equals(other.ageFirstWeekIndicator))
			return false;
		if (ageFirstYearIndicator == null) {
			if (other.ageFirstYearIndicator != null)
				return false;
		} else if (!ageFirstYearIndicator.equals(other.ageFirstYearIndicator))
			return false;
		if (ageNeonateIndicator == null) {
			if (other.ageNeonateIndicator != null)
				return false;
		} else if (!ageNeonateIndicator.equals(other.ageNeonateIndicator))
			return false;
		if (ageOfVictimMax == null) {
			if (other.ageOfVictimMax != null)
				return false;
		} else if (!ageOfVictimMax.equals(other.ageOfVictimMax))
			return false;
		if (ageOfVictimMin == null) {
			if (other.ageOfVictimMin != null)
				return false;
		} else if (!ageOfVictimMin.equals(other.ageOfVictimMin))
			return false;
		if (aggravatedAssaultHomicideCircumstancesTypes == null) {
			if (other.aggravatedAssaultHomicideCircumstancesTypes != null)
				return false;
		} else if (!aggravatedAssaultHomicideCircumstancesTypes
				.equals(other.aggravatedAssaultHomicideCircumstancesTypes))
			return false;
		if (ethnicityOfPersonType == null) {
			if (other.ethnicityOfPersonType != null)
				return false;
		} else if (!ethnicityOfPersonType.equals(other.ethnicityOfPersonType))
			return false;
		if (offenseSegments == null) {
			if (other.offenseSegments != null)
				return false;
		} else if (!offenseSegments.equals(other.offenseSegments))
			return false;
		if (officerActivityCircumstanceType == null) {
			if (other.officerActivityCircumstanceType != null)
				return false;
		} else if (!officerActivityCircumstanceType.equals(other.officerActivityCircumstanceType))
			return false;
		if (officerAssignmentTypeType == null) {
			if (other.officerAssignmentTypeType != null)
				return false;
		} else if (!officerAssignmentTypeType.equals(other.officerAssignmentTypeType))
			return false;
		if (raceOfPersonType == null) {
			if (other.raceOfPersonType != null)
				return false;
		} else if (!raceOfPersonType.equals(other.raceOfPersonType))
			return false;
		if (residentStatusOfPersonType == null) {
			if (other.residentStatusOfPersonType != null)
				return false;
		} else if (!residentStatusOfPersonType.equals(other.residentStatusOfPersonType))
			return false;
		if (segmentActionType == null) {
			if (other.segmentActionType != null)
				return false;
		} else if (!segmentActionType.equals(other.segmentActionType))
			return false;
		if (sexOfPersonType == null) {
			if (other.sexOfPersonType != null)
				return false;
		} else if (!sexOfPersonType.equals(other.sexOfPersonType))
			return false;
		if (typeInjuryTypes == null) {
			if (other.typeInjuryTypes != null)
				return false;
		} else if (!typeInjuryTypes.equals(other.typeInjuryTypes))
			return false;
		if (typeOfVictimType == null) {
			if (other.typeOfVictimType != null)
				return false;
		} else if (!typeOfVictimType.equals(other.typeOfVictimType))
			return false;
		if (victimSegmentId == null) {
			if (other.victimSegmentId != null)
				return false;
		} else if (!victimSegmentId.equals(other.victimSegmentId))
			return false;
		if (victimSequenceNumber == null) {
			if (other.victimSequenceNumber != null)
				return false;
		} else if (!victimSequenceNumber.equals(other.victimSequenceNumber))
			return false;
		return true;
	}
	public String getOfficerOtherJurisdictionOri() {
		return officerOtherJurisdictionOri;
	}
	public void setOfficerOtherJurisdictionOri(String officerOtherJurisdictionOri) {
		this.officerOtherJurisdictionOri = officerOtherJurisdictionOri;
	}
	public String getNonNumericAge() {
		return nonNumericAge;
	}
	public void setNonNumericAge(String nonNumericAge) {
		this.nonNumericAge = nonNumericAge;
	}
	
	@Transient
	public List<String> getConnectedOffenseCodes(){
		return this.getOffenseSegments().stream()
				.map(offenseSegment->offenseSegment.getUcrOffenseCodeType().getNibrsCode())
				.collect(Collectors.toList());
	}
	
	@Transient
	public Integer getAverageAge() {
		Integer ret = null;
		if (getAgeOfVictimMin() != null && getAgeOfVictimMax() != null) {
			double min = getAgeOfVictimMin().doubleValue();
			double max = getAgeOfVictimMax().doubleValue();
			double average = (min + max) / 2.0;
			ret = new Integer((int) average);
		}
		return ret;
	}
	
	@Transient
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
	public Integer getAgeNumVictim() {
		return ageNumVictim;
	}
	public void setAgeNumVictim(Integer ageNumVictim) {
		this.ageNumVictim = ageNumVictim;
	}

}
