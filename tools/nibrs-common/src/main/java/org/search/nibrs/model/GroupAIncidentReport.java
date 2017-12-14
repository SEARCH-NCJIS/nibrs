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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.search.nibrs.common.ParsedObject;
import org.search.nibrs.model.codes.OffenseCode;
import org.search.nibrs.model.codes.PropertyDescriptionCode;
import org.search.nibrs.model.codes.TypeOfPropertyLossCode;

/**
 * Representation of an individual Group A incident in a NIBRS submission.
 *
 */
public class GroupAIncidentReport extends AbstractReport
{
    
	private static final long serialVersionUID = 1953997233584944586L;

	public static final char ADMIN_SEGMENT_TYPE_IDENTIFIER = '1';
	public static final String ADMIN_WITHOUT_CARGO_THEFT_SEGMENT_LENGTH = "0087";
	public static final String ADMIN_WITH_CARGO_THEFT_SEGMENT_LENGTH = "0088";

	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(GroupAIncidentReport.class);
	
	private String incidentNumber;
    private ParsedObject<Date> incidentDate;
    private String reportDateIndicator;
    private ParsedObject<Integer> incidentHour;
    private String exceptionalClearanceCode;
    private ParsedObject<Date> exceptionalClearanceDate;
    private String cargoTheftIndicator;
    private List<OffenseSegment> offenseSegmentList;
    private List<PropertySegment> propertySegmentList;
    private List<VictimSegment> victimSegmentList;
    private List<OffenderSegment> offenderSegmentList;
    private boolean includesLeoka = false;
    private boolean includesCargoTheft = false;

	public GroupAIncidentReport()
    {
		super(ADMIN_SEGMENT_TYPE_IDENTIFIER);
        removeOffenses();
        removeProperties();
        removeVictims();
        removeOffenders();
        incidentHour = new ParsedObject<>();
        incidentDate = new ParsedObject<>();
        exceptionalClearanceDate = new ParsedObject<>();
    }
	
	public GroupAIncidentReport(GroupAIncidentReport r) {
		super(r);
		this.incidentNumber = r.incidentNumber;
		this.incidentDate = r.incidentDate;
		this.reportDateIndicator = r.reportDateIndicator;
		this.incidentHour = r.incidentHour;
		this.exceptionalClearanceCode = r.exceptionalClearanceCode;
		this.exceptionalClearanceDate = r.exceptionalClearanceDate;
		this.cargoTheftIndicator = r.cargoTheftIndicator;
		this.includesLeoka = r.includesLeoka;
		offenseSegmentList = CopyUtils.copyList(r.offenseSegmentList);
		setParentReportOnChildSegments(offenseSegmentList);
		propertySegmentList = CopyUtils.copyList(r.propertySegmentList);
		setParentReportOnChildSegments(propertySegmentList);
		victimSegmentList = CopyUtils.copyList(r.victimSegmentList);
		setParentReportOnChildSegments(victimSegmentList);
		offenderSegmentList = CopyUtils.copyList(r.offenderSegmentList);
		setParentReportOnChildSegments(offenderSegmentList);
	}
	
	private final void setParentReportOnChildSegments(List<? extends AbstractSegment> segments) {
		for (AbstractSegment s : segments) {
			s.setParentReport(this);
		}
	}
	
	public boolean includesGamblingOffense() {
		boolean ret = false;
		for (OffenseSegment os : getOffenses()) {
			String offenseCode = os.getUcrOffenseCode();
			if (offenseCode != null && OffenseCode.isGamblingOffenseCode(offenseCode)) {
				ret = true;
				break;
			}
		}
		return ret;
	}
	
	public boolean includesPropertyCrime() {
		boolean ret = false;
		for (OffenseSegment os : getOffenses()) {
			String offenseCode = os.getUcrOffenseCode();
			if (offenseCode != null && OffenseCode.isCrimeAgainstPropertyCode(offenseCode)) {
				ret = true;
				break;
			}
		}
		return ret;
	}
	
	public PropertySegment getStolenPropertySegment() {
		PropertySegment ret = null;
		for (PropertySegment ps : getProperties()) {
			if (TypeOfPropertyLossCode._7.code.equals(ps.getTypeOfPropertyLoss())) {
				ret = ps;
				break;
			}
		}
		return ret;
	}
	
	public PropertySegment getRecoveredPropertySegment() {
		PropertySegment ret = null;
		for (PropertySegment ps : getProperties()) {
			if (TypeOfPropertyLossCode._5.code.equals(ps.getTypeOfPropertyLoss())) {
				ret = ps;
				break;
			}
		}
		return ret;
	}
	
	public OffenderSegment getOffenderForSequenceNumber(Integer sequenceNumber) {
		OffenderSegment ret = null;
		if (sequenceNumber == null) {
			return null;
		}
		for (OffenderSegment o : offenderSegmentList) {
			if (sequenceNumber.equals(o.getOffenderSequenceNumber().getValue())) {
				ret = o;
				break;
			}
		}
		return ret;
	}
	
	public OffenseSegment getOffenseForOffenseCode(String ucrOffenseCode) {
		OffenseSegment ret = null;
		if (ucrOffenseCode == null) {
			return null;
		}
		for (OffenseSegment os : offenseSegmentList) {
			if (ucrOffenseCode.equals(os.getUcrOffenseCode())) {
				ret = os;
				break;
			}
		}
		return ret;
	}
	
	public List<VictimSegment> getVictimsOfOffender(OffenderSegment os) {
		List<VictimSegment> ret = new ArrayList<>();
		for (VictimSegment vs : getVictims()) {
			if (vs.isVictimOfOffender(os)) {
				ret.add(vs);
			}
		}
		return ret;
	}
    
	public List<OffenderSegment> getOffendersOfVictim(VictimSegment vs) {
		List<OffenderSegment> ret = new ArrayList<>();
		for (OffenderSegment os : getOffenders()) {
			if (os.isOffenderOfVictim(vs)) {
				ret.add(os);
			}
		}
		return ret;
	}

	@Override
	public String getAdminSegmentLength()
	{
		return includesCargoTheft()
				? ADMIN_WITH_CARGO_THEFT_SEGMENT_LENGTH
				: ADMIN_WITHOUT_CARGO_THEFT_SEGMENT_LENGTH;
	}

	public boolean includesCargoTheft() {
		return includesCargoTheft;
	}

	public void setIncludesCargoTheft(boolean includesCargoTheft) {
		this.includesCargoTheft = includesCargoTheft;
	}

    public boolean includesLeoka() {
		return includesLeoka;
	}

	public void setIncludesLeoka(boolean includesLeoka) {
		this.includesLeoka = includesLeoka;
	}

    public String getUniqueReportDescription() {
    	return "Group A Incident: Incident # " + incidentNumber;
    }
    
    public String getGloballyUniqueReportIdentifier() {
    	return getOri() + "." + incidentNumber;
    }
    
    public String getIdentifier() {
    	return incidentNumber;
    }
    
    public void removeOffender(int index) {
		offenderSegmentList.remove(index);
	}

	public void removeOffenders() {
		offenderSegmentList = new ArrayList<OffenderSegment>();
	}

	public void removeVictim(int index) {
		victimSegmentList.remove(index);
	}

	public void removeVictims() {
		victimSegmentList = new ArrayList<VictimSegment>();
	}

	public void removeProperty(int index) {
		propertySegmentList.remove(index);
	}

	public void removeProperties() {
		propertySegmentList = new ArrayList<PropertySegment>();
	}

	public void removeOffense(int index) {
		offenseSegmentList.remove(index);
	}
    
	public void removeOffenses() {
		offenseSegmentList = new ArrayList<OffenseSegment>();
	}
    
    public String getCargoTheftIndicator() {
		return cargoTheftIndicator;
	}

	public void setCargoTheftIndicator(String cargoTheftIndicator) {
		this.cargoTheftIndicator = cargoTheftIndicator;
	}

	public void addOffense(OffenseSegment offense)
    {
        offenseSegmentList.add(offense);
        offense.setParentReport(this);
    }
    
    public int getOffenseCount()
    {
        return offenseSegmentList.size();
    }
    
    public Iterator<OffenseSegment> offenseIterator()
    {
        return getOffenses().iterator();
    }
    
    public List<OffenseSegment> getOffenses() {
    	return Collections.unmodifiableList(offenseSegmentList);
    }
    
    public void setOffenses(List<OffenseSegment> offenseSegmentList) {
    	this.offenseSegmentList = offenseSegmentList;
    }
    
    public void addProperty(PropertySegment property)
    {
        propertySegmentList.add(property);
        property.setParentReport(this);
    }
    
    public int getPropertyCount()
    {
        return propertySegmentList.size();
    }
    
    public Iterator<PropertySegment> propertyIterator()
    {
        return getProperties().iterator();
    }
    
    public List<PropertySegment> getProperties() {
		return Collections.unmodifiableList(propertySegmentList);
	}

    public void setProperties(List<PropertySegment> properties) {
    	this.propertySegmentList = properties;
    }
    
	public void addVictim(VictimSegment victim)
    {
        victimSegmentList.add(victim);
        victim.setParentReport(this);
    }
    
    public int getVictimCount()
    {
        return victimSegmentList.size();
    }
    
    public Iterator<VictimSegment> victimIterator()
    {
        return getVictims().iterator();
    }

	public List<VictimSegment> getVictims() {
		return Collections.unmodifiableList(victimSegmentList);
	}

    public void setVictims(List<VictimSegment> victims) {
    	this.victimSegmentList = victims;
    }
    
    public void addOffender(OffenderSegment offender)
    {
        offenderSegmentList.add(offender);
        offender.setParentReport(this);
    }
    
    public int getOffenderCount()
    {
        return offenderSegmentList.size();
    }
    
    public Iterator<OffenderSegment> offenderIterator()
    {
        return getOffenders().iterator();
    }

    public List<OffenderSegment> getOffenders() {
		return Collections.unmodifiableList(offenderSegmentList);
	}

    public void setOffenders(List<OffenderSegment> offenders) {
    	this.offenderSegmentList = offenders;
    }
    
    public String getExceptionalClearanceCode()
    {
        return exceptionalClearanceCode;
    }
    public void setExceptionalClearanceCode(String exceptionalClearanceCode)
    {
        this.exceptionalClearanceCode = exceptionalClearanceCode;
    }
    public ParsedObject<Date> getExceptionalClearanceDate()
    {
        return exceptionalClearanceDate;
    }
    public void setExceptionalClearanceDate(ParsedObject<Date> exceptionalClearanceDate)
    {
        this.exceptionalClearanceDate = exceptionalClearanceDate;
    }
    public ParsedObject<Date> getIncidentDate()
    {
        return incidentDate;
    }
    public void setIncidentDate(ParsedObject<Date> incidentDate)
    {
        this.incidentDate = incidentDate;
    }
    public ParsedObject<Integer> getIncidentHour()
    {
        return incidentHour;
    }
    public void setIncidentHour(ParsedObject<Integer> incidentHour)
    {
        this.incidentHour = incidentHour;
    }
    public String getIncidentNumber()
    {
        return incidentNumber;
    }
    public void setIncidentNumber(String incidentNumber)
    {
        this.incidentNumber = incidentNumber;
    }
    public String getReportDateIndicator()
    {
        return reportDateIndicator;
    }
    public void setReportDateIndicator(String reportDateIndicator)
    {
        this.reportDateIndicator = reportDateIndicator;
    }

    public boolean isDrugOffenseInvolved() {
		return this.getOffenses().stream()
			.filter(Objects::nonNull)
			.anyMatch(item->OffenseCode.isDrugNarcoticOffense(item.getUcrOffenseCode()));
    }

    public boolean isOffenseInvolved( OffenseCode offensecode) {
		return this.getOffenses().stream()
			.filter(Objects::nonNull)
			.anyMatch(item->offensecode.code.equals(item.getUcrOffenseCode()));
    }
    
    public boolean isCompleteOffenseInvolved( OffenseCode offensecode) {
		return this.getOffenses().stream()
			.filter(Objects::nonNull)
			.anyMatch(item->(offensecode.code.equals(item.getUcrOffenseCode()) 
					&& !item.getOffenseAttemptedIndicator()));
    }

    public boolean isAgainstPropertyCrimeInvolved() {
    	return this.getOffenses().stream()
    			.filter(Objects::nonNull)
    			.anyMatch(item->OffenseCode.isCrimeAgainstPropertyCode(item.getUcrOffenseCode()));
    }
    
    public boolean containsOtherCrimeRequirePropertySegment(String... excludedOffenseCodes) {
    	return this.getOffenses().stream()
    			.filter(Objects::nonNull)
    			.filter(item->!Arrays.asList(excludedOffenseCodes).contains(item.getUcrOffenseCode()))
    			.anyMatch(item->OffenseCode.isCrimeRequirePropertySegement(item.getUcrOffenseCode()));
    }
    
    public boolean isRule392Exception2() {
    	return getPropertyCount() > 1 && 
    			this.getProperties().stream()
    			.filter(Objects::nonNull)
    			.anyMatch(item-> TypeOfPropertyLossCode._6.code.equals(item.getTypeOfPropertyLoss())
    					&& item.containsPropertyDescription(PropertyDescriptionCode._10.code));
    }
    
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((cargoTheftIndicator == null) ? 0 : cargoTheftIndicator.hashCode());
		result = prime * result + ((exceptionalClearanceCode == null) ? 0 : exceptionalClearanceCode.hashCode());
		result = prime * result + ((exceptionalClearanceDate == null) ? 0 : exceptionalClearanceDate.hashCode());
		result = prime * result + ((incidentDate == null) ? 0 : incidentDate.hashCode());
		result = prime * result + ((incidentHour == null) ? 0 : incidentHour.hashCode());
		result = prime * result + ((incidentNumber == null) ? 0 : incidentNumber.hashCode());
		result = prime * result + (includesLeoka ? 1231 : 1237);
		result = prime * result + ((offenderSegmentList == null) ? 0 : offenderSegmentList.hashCode());
		result = prime * result + ((offenseSegmentList == null) ? 0 : offenseSegmentList.hashCode());
		result = prime * result + ((propertySegmentList == null) ? 0 : propertySegmentList.hashCode());
		result = prime * result + ((reportDateIndicator == null) ? 0 : reportDateIndicator.hashCode());
		result = prime * result + ((victimSegmentList == null) ? 0 : victimSegmentList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.hashCode() == hashCode();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(1024);
		sb.append("GroupAIncidentReport:\n");
		sb.append(super.toString());
		sb.append("[incidentNumber=" + incidentNumber + ", incidentDate=" + incidentDate + ", reportDateIndicator=" + reportDateIndicator + ", incidentHour=" + incidentHour
				+ ", exceptionalClearanceCode=" + exceptionalClearanceCode + ", exceptionalClearanceDate=" + exceptionalClearanceDate + ", cargoTheftIndicator=" + cargoTheftIndicator + ", includesLeoka=" + includesLeoka);
		sb.append("\n").append(offenseSegmentList.size() + " OffenseSegment Segments:\n");
		for (OffenseSegment o : offenseSegmentList) {
			sb.append("\t").append(o.toString()).append("\n");
		}
		sb.append("\n").append(offenderSegmentList.size() + " OffenderSegment Segments:\n");
		for (OffenderSegment o : offenderSegmentList) {
			sb.append("\t").append(o.toString()).append("\n");
		}
		sb.append("\n").append(propertySegmentList.size() + " PropertySegment Segments:\n");
		for (PropertySegment p : propertySegmentList) {
			sb.append("\t").append(p.toString()).append("\n");
		}
		sb.append("\n").append(victimSegmentList.size() + " VictimSegment Segments:\n");
		for (VictimSegment v : victimSegmentList) {
			sb.append("\t").append(v.toString()).append("\n");
		}
		
		return sb.toString();
	}
}
