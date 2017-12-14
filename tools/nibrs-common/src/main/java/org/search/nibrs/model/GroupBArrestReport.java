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

import java.util.Date;

/**
 * Representation of an individual Group B incident in a NIBRS submission.
 *
 */
public class GroupBArrestReport extends AbstractReport {
	
	private static final long serialVersionUID = 8378083693700302541L;

	public GroupBArrestReport() {
        super(ArresteeSegment.GROUP_B_ARRESTEE_SEGMENT_TYPE_IDENTIFIER);
	}
	
	public GroupBArrestReport(GroupBArrestReport r) {
		super(r);
	}
	
	@Override
	public boolean equals(Object o) {
		return o != null && o.hashCode() == hashCode();
	}
	
	@Override
	public int hashCode() {
		return 31*super.hashCode() + getClass().getName().hashCode();
	}
	
	@Override
	public String getUniqueReportDescription() {
    	return "Group B Arrest: ATN # " + getATNOrEmpty();
	}

	@Override
	public String getGloballyUniqueReportIdentifier() {
		return getOri() + "." + getATNOrEmpty();
	}
	
	@Override
	public String getIdentifier() {
		return getArresteeCount() == 0 ? null : getArrestees().get(0).getArrestTransactionNumber();
	}

	@Override
	public String getAdminSegmentLength()
	{
		return ArresteeSegment.GROUP_B_ARRESTEE_SEGMENT_LENGTH;
	}
	
	public Integer getArresteeSequenceNumber() {
		return getArresteeCount() == 0 ? null : getArrestees().get(0).getArresteeSequenceNumber().getValue();
	}
	
	public Date getArrestDate() {
		return getArresteeCount() == 0 ? null : getArrestees().get(0).getArrestDate().getValue();
	}
	
	public ArresteeSegment getArrestee() {
		return getArresteeCount() == 0 ? null : getArrestees().get(0);
	}
	
	private String getATNOrEmpty() {
		return getArresteeCount() == 0 ? "empty" : getArrestees().get(0).getArrestTransactionNumber();
	}

}
