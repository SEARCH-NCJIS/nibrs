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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
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
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.search.nibrs.model.codes.ClearedExceptionallyCode;
import org.search.nibrs.stagingdata.model.Agency;
import org.search.nibrs.stagingdata.model.CargoTheftIndicatorType;
import org.search.nibrs.stagingdata.model.ClearedExceptionallyType;
import org.search.nibrs.stagingdata.model.DateType;
import org.search.nibrs.stagingdata.model.Owner;
import org.search.nibrs.stagingdata.model.SegmentActionTypeType;
import org.search.nibrs.stagingdata.model.Submission;
import org.search.nibrs.stagingdata.model.search.FbiSubmissionStatus;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@JsonIdentityInfo(
	generator = ObjectIdGenerators.PropertyGenerator.class, 
	property = "administrativeSegmentId", scope = AdministrativeSegment.class)
@NamedEntityGraph(name="allAdministrativeSegmentJoins", attributeNodes = {
        @NamedAttributeNode("segmentActionType"),
        @NamedAttributeNode("offenseSegments"),
        @NamedAttributeNode("propertySegments"),
        @NamedAttributeNode("offenderSegments"),
        @NamedAttributeNode("victimSegments"),
        @NamedAttributeNode("arresteeSegments"),
        @NamedAttributeNode("agency"),
        @NamedAttributeNode("incidentDateType"),
        @NamedAttributeNode("exceptionalClearanceDateType"),
        @NamedAttributeNode("clearedExceptionallyType"),
        @NamedAttributeNode("cargoTheftIndicatorType"),
        @NamedAttributeNode("owner"),
        @NamedAttributeNode("submission")
	})
public class AdministrativeSegment implements Comparable<AdministrativeSegment>, Serializable{
	private static final long serialVersionUID = -3998248086687831675L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer administrativeSegmentId;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="segmentActionTypeTypeID") 
	private SegmentActionTypeType segmentActionType;
	private String stateCode; 
	private String monthOfTape; 
	private String yearOfTape; 
	private String cityIndicator;
	private String ori;
	
    @OneToMany(mappedBy = "administrativeSegment", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval=true)
    private Set<OffenseSegment> offenseSegments = new HashSet<OffenseSegment>();
	
    @OneToMany(mappedBy = "administrativeSegment", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval=true)
    private Set<PropertySegment> propertySegments = new HashSet<PropertySegment>();
    
    @OneToMany(mappedBy = "administrativeSegment", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval=true)
    private Set<ArresteeSegment> arresteeSegments = new HashSet<ArresteeSegment>();
    
    @OneToMany(mappedBy = "administrativeSegment", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval=true)
    private Set<OffenderSegment> offenderSegments = new HashSet<OffenderSegment>();
    
    @OneToMany(mappedBy = "administrativeSegment", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval=true)
    private Set<VictimSegment> victimSegments = new HashSet<VictimSegment>();
    
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="agencyId")
	private Agency agency; 
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ownerId")
	private Owner owner; 
	
	private String incidentNumber; 
	
	private LocalDate incidentDate;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="incidentDateId")
	private DateType incidentDateType; 

	private Date exceptionalClearanceDate;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="exceptionalClearanceDateId")
	private DateType exceptionalClearanceDateType; 
	
	private String reportDateIndicator; 
	private String incidentHour;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="clearedExceptionallyTypeId") 
	private ClearedExceptionallyType clearedExceptionallyType; 
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="cargoTheftIndicatorTypeId") 
	private CargoTheftIndicatorType cargoTheftIndicatorType; 
	
	@OneToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "submissionId", nullable = true)
    private Submission submission;
	
	private LocalDateTime reportTimestamp;
	
    public Set<OffenseSegment> getOffenseSegments() {
        return offenseSegments;
    }

    public void setOffenseSegments(Set<OffenseSegment> offenseSegments) {
    	this.offenseSegments = offenseSegments;
    }
    
	
	public String getMonthOfTape() {
		return monthOfTape;
	}
	public void setMonthOfTape(String monthOfTape) {
		this.monthOfTape = monthOfTape;
	}
	public String getYearOfTape() {
		return yearOfTape;
	}
	public void setYearOfTape(String yearOfTape) {
		this.yearOfTape = yearOfTape;
	}
	public String getCityIndicator() {
		return cityIndicator;
	}
	public void setCityIndicator(String cityIndicator) {
		this.cityIndicator = cityIndicator;
	}
	public String getOri() {
		return ori;
	}
	public void setOri(String ori) {
		this.ori = ori;
	}
	public Agency getAgency() {
		return agency;
	}
	public void setAgency(Agency agency) {
		this.agency = agency;
	}
	public SegmentActionTypeType getSegmentActionType() {
		return segmentActionType;
	}
	public void setSegmentActionType(SegmentActionTypeType segmentActionType) {
		this.segmentActionType = segmentActionType;
	}
	public String getReportDateIndicator() {
		return reportDateIndicator;
	}
	public void setReportDateIndicator(String reportDateIndicator) {
		this.reportDateIndicator = reportDateIndicator;
	}
	public String getIncidentHour() {
		return incidentHour;
	}
	public void setIncidentHour(String incidentHour) {
		this.incidentHour = incidentHour;
	}
	public Integer getAdministrativeSegmentId() {
		return administrativeSegmentId;
	}
	public void setAdministrativeSegmentId(Integer administrativeSegmentId) {
		this.administrativeSegmentId = administrativeSegmentId;
	}
	public String getIncidentNumber() {
		return incidentNumber;
	}
	public void setIncidentNumber(String incidentNumber) {
		this.incidentNumber = incidentNumber;
	}
	public DateType getIncidentDateType() {
		return incidentDateType;
	}
	public void setIncidentDateType(DateType incidentDateType) {
		this.incidentDateType = incidentDateType;
	}
	public ClearedExceptionallyType getClearedExceptionallyType() {
		return clearedExceptionallyType;
	}
	public void setClearedExceptionallyType(ClearedExceptionallyType clearedExceptionallyType) {
		this.clearedExceptionallyType = clearedExceptionallyType;
	}

	public Set<PropertySegment> getPropertySegments() {
		return propertySegments;
	}

	public void setPropertySegments(Set<PropertySegment> propertySegments) {
		this.propertySegments = propertySegments;
	}

	public Set<ArresteeSegment> getArresteeSegments() {
		return arresteeSegments;
	}

	public void setArresteeSegments(Set<ArresteeSegment> arresteeSegments) {
		this.arresteeSegments = arresteeSegments;
	}

	public Set<OffenderSegment> getOffenderSegments() {
		return offenderSegments;
	}

	public void setOffenderSegments(Set<OffenderSegment> offenderSegments) {
		this.offenderSegments = offenderSegments;
	}

	public Set<VictimSegment> getVictimSegments() {
		return victimSegments;
	}

	public void setVictimSegments(Set<VictimSegment> victimSegments) {
		this.victimSegments = victimSegments;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((administrativeSegmentId == null) ? 0 : administrativeSegmentId.hashCode());
		result = prime * result + ((agency == null) ? 0 : agency.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		result = prime * result + ((cargoTheftIndicatorType == null) ? 0 : cargoTheftIndicatorType.hashCode());
		result = prime * result + ((cityIndicator == null) ? 0 : cityIndicator.hashCode());
		result = prime * result + ((clearedExceptionallyType == null) ? 0 : clearedExceptionallyType.hashCode());
		result = prime * result + ((exceptionalClearanceDate == null) ? 0 : exceptionalClearanceDate.hashCode());
		result = prime * result
				+ ((exceptionalClearanceDateType == null) ? 0 : exceptionalClearanceDateType.hashCode());
		result = prime * result + ((getIncidentDate() == null) ? 0 : getIncidentDate().hashCode());
		result = prime * result + ((incidentDateType == null) ? 0 : incidentDateType.hashCode());
		result = prime * result + ((incidentHour == null) ? 0 : incidentHour.hashCode());
		result = prime * result + ((incidentNumber == null) ? 0 : incidentNumber.hashCode());
		result = prime * result + ((monthOfTape == null) ? 0 : monthOfTape.hashCode());
		result = prime * result + ((ori == null) ? 0 : ori.hashCode());
		result = prime * result + ((reportDateIndicator == null) ? 0 : reportDateIndicator.hashCode());
		result = prime * result + ((segmentActionType == null) ? 0 : segmentActionType.hashCode());
		result = prime * result + ((yearOfTape == null) ? 0 : yearOfTape.hashCode());
		return result;
	}

	public CargoTheftIndicatorType getCargoTheftIndicatorType() {
		return cargoTheftIndicatorType;
	}

	public void setCargoTheftIndicatorType(CargoTheftIndicatorType cargoTheftIndicatorType) {
		this.cargoTheftIndicatorType = cargoTheftIndicatorType;
	}

	public DateType getExceptionalClearanceDateType() {
		return exceptionalClearanceDateType;
	}

	public void setExceptionalClearanceDateType(DateType exceptionalClearanceDateType) {
		this.exceptionalClearanceDateType = exceptionalClearanceDateType;
	}

	public Date getExceptionalClearanceDate() {
		return exceptionalClearanceDate;
	}

	public void setExceptionalClearanceDate(Date exceptionalClearanceDate) {
		this.exceptionalClearanceDate = exceptionalClearanceDate;
	}

	@Override
	public String toString() {
		return "AdministrativeSegment [administrativeSegmentId=" + administrativeSegmentId==null?"null":administrativeSegmentId + ", segmentActionType="
				+ segmentActionType + ", stateCode =" + stateCode + ", monthOfTape=" + monthOfTape + ", "
				+ "yearOfTape=" + yearOfTape + ", cityIndicator="
				+ cityIndicator + ", ori=" + ori + ", offenseSegments=" + offenseSegments + ", propertySegments="
				+ propertySegments + ", arresteeSegments=" + arresteeSegments + ", offenderSegments=" + offenderSegments
				+ ", victimSegments=" + victimSegments + ", agency=" + agency + ", owner=" + owner==null?"null": owner
				+ ",incidentNumber=" + incidentNumber
				+ ", incidentDate=" + getIncidentDate() + ", incidentDateType=" + incidentDateType
				+ ", exceptionalClearanceDate=" + exceptionalClearanceDate + ", exceptionalClearanceDateType="
				+ exceptionalClearanceDateType + ", reportDateIndicator=" + reportDateIndicator + ", incidentHour="
				+ incidentHour + ", clearedExceptionallyType=" + clearedExceptionallyType + ", cargoTheftIndicatorType="
				+ cargoTheftIndicatorType + "submission=" + submission== null? "null":submission + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AdministrativeSegment other = (AdministrativeSegment) obj;
		if (administrativeSegmentId == null) {
			if (other.administrativeSegmentId != null)
				return false;
		} else if (!administrativeSegmentId.equals(other.administrativeSegmentId))
			return false;
		if (agency == null) {
			if (other.agency != null)
				return false;
		} else if (!agency.equals(other.agency))
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		if (arresteeSegments == null) {
			if (other.arresteeSegments != null)
				return false;
		} else if (!arresteeSegments.equals(other.arresteeSegments))
			return false;
		if (cargoTheftIndicatorType == null) {
			if (other.cargoTheftIndicatorType != null)
				return false;
		} else if (!cargoTheftIndicatorType.equals(other.cargoTheftIndicatorType))
			return false;
		if (cityIndicator == null) {
			if (other.cityIndicator != null)
				return false;
		} else if (!cityIndicator.equals(other.cityIndicator))
			return false;
		if (clearedExceptionallyType == null) {
			if (other.clearedExceptionallyType != null)
				return false;
		} else if (!clearedExceptionallyType.equals(other.clearedExceptionallyType))
			return false;
		if (exceptionalClearanceDate == null) {
			if (other.exceptionalClearanceDate != null)
				return false;
		} else if (!exceptionalClearanceDate.equals(other.exceptionalClearanceDate))
			return false;
		if (exceptionalClearanceDateType == null) {
			if (other.exceptionalClearanceDateType != null)
				return false;
		} else if (!exceptionalClearanceDateType.equals(other.exceptionalClearanceDateType))
			return false;
		if (getIncidentDate() == null) {
			if (other.getIncidentDate() != null)
				return false;
		} else if (!getIncidentDate().equals(other.getIncidentDate()))
			return false;
		if (incidentDateType == null) {
			if (other.incidentDateType != null)
				return false;
		} else if (!incidentDateType.equals(other.incidentDateType))
			return false;
		if (incidentHour == null) {
			if (other.incidentHour != null)
				return false;
		} else if (!incidentHour.equals(other.incidentHour))
			return false;
		if (incidentNumber == null) {
			if (other.incidentNumber != null)
				return false;
		} else if (!incidentNumber.equals(other.incidentNumber))
			return false;
		if (monthOfTape == null) {
			if (other.monthOfTape != null)
				return false;
		} else if (!monthOfTape.equals(other.monthOfTape))
			return false;
		if (offenderSegments == null) {
			if (other.offenderSegments != null)
				return false;
		} else if (!offenderSegments.equals(other.offenderSegments))
			return false;
		if (offenseSegments == null) {
			if (other.offenseSegments != null)
				return false;
		} else if (!offenseSegments.equals(other.offenseSegments))
			return false;
		if (ori == null) {
			if (other.ori != null)
				return false;
		} else if (!ori.equals(other.ori))
			return false;
		if (propertySegments == null) {
			if (other.propertySegments != null)
				return false;
		} else if (!propertySegments.equals(other.propertySegments))
			return false;
		if (reportDateIndicator == null) {
			if (other.reportDateIndicator != null)
				return false;
		} else if (!reportDateIndicator.equals(other.reportDateIndicator))
			return false;
		if (segmentActionType == null) {
			if (other.segmentActionType != null)
				return false;
		} else if (!segmentActionType.equals(other.segmentActionType))
			return false;
		if (victimSegments == null) {
			if (other.victimSegments != null)
				return false;
		} else if (!victimSegments.equals(other.victimSegments))
			return false;
		if (yearOfTape == null) {
			if (other.yearOfTape != null)
				return false;
		} else if (!yearOfTape.equals(other.yearOfTape))
			return false;
		return true;
	}

	public LocalDateTime getReportTimestamp() {
		return reportTimestamp;
	}

	public void setReportTimestamp(LocalDateTime reportTimestamp) {
		this.reportTimestamp = reportTimestamp;
	}

	public String getStateCode() {
		return stateCode;
	}

	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}
	
	@JsonIgnore
	public boolean isClearanceInvolvingOnlyJuvenile() {
		boolean isClearanceInvolvingOnlyJuvenile = false; 
		if (ClearedExceptionallyCode.applicableCodeSet().contains(this.getClearedExceptionallyType().getNibrsCode())){
			Set<OffenderSegment> offenders = this.getOffenderSegments();
			isClearanceInvolvingOnlyJuvenile = 
					offenders.stream().anyMatch(offender -> offender.isJuvenile())
					&& offenders.stream().noneMatch(offender -> offender.isAdult()); 
		}
		else {
			Set<ArresteeSegment> arrestees = this.getArresteeSegments();
			isClearanceInvolvingOnlyJuvenile = arrestees.stream().anyMatch(arrestee -> arrestee.isJuvenile())
					&& arrestees.stream().noneMatch(arrestee -> arrestee.isAdult()); 
		}
		return isClearanceInvolvingOnlyJuvenile;
	}

	public LocalDate getIncidentDate() {
		return incidentDate;
	}

	public void setIncidentDate(LocalDate incidentDate) {
		this.incidentDate = incidentDate;
	}

	@Override
	public int compareTo(AdministrativeSegment o) {
		return this.getAdministrativeSegmentId().compareTo(o.getAdministrativeSegmentId());
	}

	public Submission getSubmission() {
		return submission;
	}

	public void setSubmission(Submission submission) {
		this.submission = submission;
	}

	@JsonIgnore
	public FbiSubmissionStatus getFbiStatus() {
		if (submission == null) return FbiSubmissionStatus.NOT_SUBMITTED; 
		else if (submission.getAcceptedIndicator()) {
			return FbiSubmissionStatus.ACCEPTED; 
		}
		else {
			return FbiSubmissionStatus.REJECTED; 
		}
	}

	public Owner getOwner() {
		return owner;
	}

	public void setOwner(Owner owner) {
		this.owner = owner;
	}

}
