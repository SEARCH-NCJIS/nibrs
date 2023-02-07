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

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.search.nibrs.stagingdata.model.Agency;
import org.search.nibrs.stagingdata.model.ArrestReportSegmentWasArmedWith;
import org.search.nibrs.stagingdata.model.DateType;
import org.search.nibrs.stagingdata.model.DispositionOfArresteeUnder18Type;
import org.search.nibrs.stagingdata.model.EthnicityOfPersonType;
import org.search.nibrs.stagingdata.model.Owner;
import org.search.nibrs.stagingdata.model.RaceOfPersonType;
import org.search.nibrs.stagingdata.model.ResidentStatusOfPersonType;
import org.search.nibrs.stagingdata.model.SegmentActionTypeType;
import org.search.nibrs.stagingdata.model.SexOfPersonType;
import org.search.nibrs.stagingdata.model.Submission;
import org.search.nibrs.stagingdata.model.TypeOfArrestType;
import org.search.nibrs.stagingdata.model.UcrOffenseCodeType;
import org.search.nibrs.stagingdata.model.search.FbiSubmissionStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@NamedEntityGraph(name="allArrestReportSegmentJoins", attributeNodes = {
        @NamedAttributeNode("segmentActionType"),
        @NamedAttributeNode("agency"),
        @NamedAttributeNode("arrestDateType"),
        @NamedAttributeNode("typeOfArrestType"),
        @NamedAttributeNode("sexOfPersonType"),
        @NamedAttributeNode("raceOfPersonType"),
        @NamedAttributeNode("ethnicityOfPersonType"),
        @NamedAttributeNode("residentStatusOfPersonType"),
        @NamedAttributeNode("dispositionOfArresteeUnder18Type"),
        @NamedAttributeNode("ucrOffenseCodeType"),
        @NamedAttributeNode("arrestReportSegmentWasArmedWiths"),
        @NamedAttributeNode("owner"),
        @NamedAttributeNode("submission")
	})
public class ArrestReportSegment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer arrestReportSegmentId;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="segmentActionTypeTypeID") 
	private SegmentActionTypeType segmentActionType; 
	private String stateCode;
	private String monthOfTape; 
	private String yearOfTape; 
	private String cityIndicator;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="agencyId")
	private Agency agency; 
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ownerId")
	private Owner owner; 

	private String ori; 
	private String arrestTransactionNumber; 
	private Integer arresteeSequenceNumber; 
	private LocalDate arrestDate;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="arrestDateId")
	private DateType arrestDateType; 

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="typeOfArrestTypeId") 
	private TypeOfArrestType typeOfArrestType; 
	private Integer ageOfArresteeMin; 
	private Integer ageOfArresteeMax;
	private Integer ageNumArrestee;
	private String nonNumericAge;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="sexOfPersonTypeId") 
	private SexOfPersonType sexOfPersonType;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="raceOfPersonTypeId") 
	private RaceOfPersonType raceOfPersonType;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ethnicityOfPersonTypeId") 
	private EthnicityOfPersonType ethnicityOfPersonType;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="residentStatusOfPersonTypeId") 
	private ResidentStatusOfPersonType residentStatusOfPersonType;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="dispositionOfArresteeUnder18TypeId") 
	private DispositionOfArresteeUnder18Type dispositionOfArresteeUnder18Type;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ucrOffenseCodeTypeId")
	private UcrOffenseCodeType ucrOffenseCodeType;
	
	@OneToMany(mappedBy = "arrestReportSegment", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ArrestReportSegmentWasArmedWith> arrestReportSegmentWasArmedWiths = new HashSet<ArrestReportSegmentWasArmedWith>();

	@OneToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "submissionId", nullable = true)
    private Submission submission;

	private LocalDateTime reportTimestamp;

	public Integer getArrestReportSegmentId() {
		return arrestReportSegmentId;
	}
	public void setArrestReportSegmentId(Integer arrestReportSegmentId) {
		this.arrestReportSegmentId = arrestReportSegmentId;
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
	public String getArrestTransactionNumber() {
		return arrestTransactionNumber;
	}
	public void setArrestTransactionNumber(String arrestTransactionNumber) {
		this.arrestTransactionNumber = arrestTransactionNumber;
	}
	public Integer getArresteeSequenceNumber() {
		return arresteeSequenceNumber;
	}
	public void setArresteeSequenceNumber(Integer arresteeSequenceNumber) {
		this.arresteeSequenceNumber = arresteeSequenceNumber;
	}
	public LocalDate getArrestDate() {
		return arrestDate;
	}
	public void setArrestDate(LocalDate arrestDate) {
		this.arrestDate = arrestDate;
	}
	public Integer getAgeOfArresteeMin() {
		return ageOfArresteeMin;
	}
	public void setAgeOfArresteeMin(Integer ageOfArresteeMin) {
		this.ageOfArresteeMin = ageOfArresteeMin;
	}
	public Integer getAgeOfArresteeMax() {
		return ageOfArresteeMax;
	}
	public void setAgeOfArresteeMax(Integer ageOfArresteeMax) {
		this.ageOfArresteeMax = ageOfArresteeMax;
	}
	public UcrOffenseCodeType getUcrOffenseCodeType() {
		return ucrOffenseCodeType;
	}
	public void setUcrOffenseCodeType(UcrOffenseCodeType ucrOffenseCodeType) {
		this.ucrOffenseCodeType = ucrOffenseCodeType;
	}
	public DateType getArrestDateType() {
		return arrestDateType;
	}
	public void setArrestDateType(DateType arrestDateType) {
		this.arrestDateType = arrestDateType;
	}
	public DispositionOfArresteeUnder18Type getDispositionOfArresteeUnder18Type() {
		return dispositionOfArresteeUnder18Type;
	}
	public void setDispositionOfArresteeUnder18Type(DispositionOfArresteeUnder18Type dispositionOfArresteeUnder18Type) {
		this.dispositionOfArresteeUnder18Type = dispositionOfArresteeUnder18Type;
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
	public TypeOfArrestType getTypeOfArrestType() {
		return typeOfArrestType;
	}
	public void setTypeOfArrestType(TypeOfArrestType typeOfArrestType) {
		this.typeOfArrestType = typeOfArrestType;
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
	public ResidentStatusOfPersonType getResidentStatusOfPersonType() {
		return residentStatusOfPersonType;
	}
	public void setResidentStatusOfPersonType(ResidentStatusOfPersonType residentStatusOfPersonType) {
		this.residentStatusOfPersonType = residentStatusOfPersonType;
	}
	public Set<ArrestReportSegmentWasArmedWith> getArrestReportSegmentWasArmedWiths() {
		return arrestReportSegmentWasArmedWiths;
	}
	public void setArrestReportSegmentWasArmedWiths(Set<ArrestReportSegmentWasArmedWith> arrestReportSegmentWasArmedWiths) {
		this.arrestReportSegmentWasArmedWiths = arrestReportSegmentWasArmedWiths;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ageOfArresteeMax == null) ? 0 : ageOfArresteeMax.hashCode());
		result = prime * result + ((ageOfArresteeMin == null) ? 0 : ageOfArresteeMin.hashCode());
		result = prime * result + ((agency == null) ? 0 : agency.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		result = prime * result + ((arrestDate == null) ? 0 : arrestDate.hashCode());
		result = prime * result + ((arrestDateType == null) ? 0 : arrestDateType.hashCode());
		result = prime * result + ((arrestReportSegmentId == null) ? 0 : arrestReportSegmentId.hashCode());
		result = prime * result + ((arrestTransactionNumber == null) ? 0 : arrestTransactionNumber.hashCode());
		result = prime * result + ((arresteeSequenceNumber == null) ? 0 : arresteeSequenceNumber.hashCode());
		result = prime * result + ((cityIndicator == null) ? 0 : cityIndicator.hashCode());
		result = prime * result
				+ ((dispositionOfArresteeUnder18Type == null) ? 0 : dispositionOfArresteeUnder18Type.hashCode());
		result = prime * result + ((ethnicityOfPersonType == null) ? 0 : ethnicityOfPersonType.hashCode());
		result = prime * result + ((monthOfTape == null) ? 0 : monthOfTape.hashCode());
		result = prime * result + ((ori == null) ? 0 : ori.hashCode());
		result = prime * result + ((raceOfPersonType == null) ? 0 : raceOfPersonType.hashCode());
		result = prime * result + ((residentStatusOfPersonType == null) ? 0 : residentStatusOfPersonType.hashCode());
		result = prime * result + ((segmentActionType == null) ? 0 : segmentActionType.hashCode());
		result = prime * result + ((sexOfPersonType == null) ? 0 : sexOfPersonType.hashCode());
		result = prime * result + ((typeOfArrestType == null) ? 0 : typeOfArrestType.hashCode());
		result = prime * result + ((ucrOffenseCodeType == null) ? 0 : ucrOffenseCodeType.hashCode());
		result = prime * result + ((yearOfTape == null) ? 0 : yearOfTape.hashCode());
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
		ArrestReportSegment other = (ArrestReportSegment) obj;
		if (ageOfArresteeMax == null) {
			if (other.ageOfArresteeMax != null)
				return false;
		} else if (!ageOfArresteeMax.equals(other.ageOfArresteeMax))
			return false;
		if (ageOfArresteeMin == null) {
			if (other.ageOfArresteeMin != null)
				return false;
		} else if (!ageOfArresteeMin.equals(other.ageOfArresteeMin))
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
		if (arrestDate == null) {
			if (other.arrestDate != null)
				return false;
		} else if (!arrestDate.equals(other.arrestDate))
			return false;
		if (arrestDateType == null) {
			if (other.arrestDateType != null)
				return false;
		} else if (!arrestDateType.equals(other.arrestDateType))
			return false;
		if (arrestReportSegmentId == null) {
			if (other.arrestReportSegmentId != null)
				return false;
		} else if (!arrestReportSegmentId.equals(other.arrestReportSegmentId))
			return false;
		else if (!arrestReportSegmentWasArmedWiths.equals(other.arrestReportSegmentWasArmedWiths))
			return false;
		if (arrestTransactionNumber == null) {
			if (other.arrestTransactionNumber != null)
				return false;
		} else if (!arrestTransactionNumber.equals(other.arrestTransactionNumber))
			return false;
		if (arresteeSequenceNumber == null) {
			if (other.arresteeSequenceNumber != null)
				return false;
		} else if (!arresteeSequenceNumber.equals(other.arresteeSequenceNumber))
			return false;
		if (cityIndicator == null) {
			if (other.cityIndicator != null)
				return false;
		} else if (!cityIndicator.equals(other.cityIndicator))
			return false;
		if (dispositionOfArresteeUnder18Type == null) {
			if (other.dispositionOfArresteeUnder18Type != null)
				return false;
		} else if (!dispositionOfArresteeUnder18Type.equals(other.dispositionOfArresteeUnder18Type))
			return false;
		if (ethnicityOfPersonType == null) {
			if (other.ethnicityOfPersonType != null)
				return false;
		} else if (!ethnicityOfPersonType.equals(other.ethnicityOfPersonType))
			return false;
		if (monthOfTape == null) {
			if (other.monthOfTape != null)
				return false;
		} else if (!monthOfTape.equals(other.monthOfTape))
			return false;
		if (ori == null) {
			if (other.ori != null)
				return false;
		} else if (!ori.equals(other.ori))
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
		if (typeOfArrestType == null) {
			if (other.typeOfArrestType != null)
				return false;
		} else if (!typeOfArrestType.equals(other.typeOfArrestType))
			return false;
		if (ucrOffenseCodeType == null) {
			if (other.ucrOffenseCodeType != null)
				return false;
		} else if (!ucrOffenseCodeType.equals(other.ucrOffenseCodeType))
			return false;
		if (yearOfTape == null) {
			if (other.yearOfTape != null)
				return false;
		} else if (!yearOfTape.equals(other.yearOfTape))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "ArrestReportSegment [arrestReportSegmentId=" + arrestReportSegmentId + ", segmentActionType="
				+ segmentActionType + ", stateCode=" + getStateCode() + ", monthOfTape=" + monthOfTape + ", yearOfTape=" + yearOfTape + ", cityIndicator="
				+ cityIndicator + ", agency=" + agency + "owner=" + owner==null?"null":owner + ", ori=" + ori + ", arrestTransactionNumber="
				+ arrestTransactionNumber + ", arresteeSequenceNumber=" + arresteeSequenceNumber + ", arrestDate="
				+ arrestDate + ", arrestDateType=" + arrestDateType + ", typeOfArrestType=" + typeOfArrestType
				+ ", ageOfArresteeMin=" + ageOfArresteeMin + ", ageOfArresteeMax=" + ageOfArresteeMax
				+ ", nonNumericAge=" + Objects.toString(nonNumericAge) 
				+ ", sexOfPersonType=" + sexOfPersonType + ", raceOfPersonType=" + raceOfPersonType
				+ ", ethnicityOfPersonType=" + ethnicityOfPersonType + ", residentStatusOfPersonType="
				+ residentStatusOfPersonType + ", dispositionOfArresteeUnder18Type=" + dispositionOfArresteeUnder18Type
				+ ", ucrOffenseCodeType=" + ucrOffenseCodeType + "submission=" + submission==null?"null":submission + "]";
	}
	public String getNonNumericAge() {
		return nonNumericAge;
	}
	public void setNonNumericAge(String nonNumericAge) {
		this.nonNumericAge = nonNumericAge;
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
	
	public Integer getAverageAge() {
		Integer ret = null;
		if (!isAgeUnknown()) {
			double min = ageOfArresteeMin.doubleValue();
			double max = ageOfArresteeMax.doubleValue();
			double average = (min + max) / 2.0;
			ret = new Integer((int) average);
		}
		return ret;
	}
	
    public boolean isAgeUnknown() {
    	// set forth in rule for data element 52
    	return ageOfArresteeMax == null && ageOfArresteeMin == null && Objects.equals(nonNumericAge, "00");
    }

    public boolean isJuvenile() {
    	boolean ret = false; 
    	if ( !isAgeUnknown() ) {
    		ret = ageOfArresteeMax < 18 || (ageOfArresteeMin < 18 && getAverageAge() < 18);
    	}
    	return ret;
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
	public Integer getAgeNumArrestee() {
		return ageNumArrestee;
	}
	public void setAgeNumArrestee(Integer ageNumArrestee) {
		this.ageNumArrestee = ageNumArrestee;
	}

}
