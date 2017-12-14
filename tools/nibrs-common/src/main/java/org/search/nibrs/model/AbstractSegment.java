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

import org.search.nibrs.common.NIBRSError;
import org.search.nibrs.common.ValidationTarget;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Base class for NIBRS segments
 */
public abstract class AbstractSegment implements ValidationTarget {
	
	private AbstractReport parentReport;
	protected char segmentType;
	
	public AbstractSegment() {
	}
	
	public AbstractSegment(AbstractSegment s) {
		this();
		segmentType = s.segmentType;
	}

	public NIBRSError getErrorTemplate() {
		NIBRSError ret = new NIBRSError();
		ret.setContext(getParentReport().getSource());
		ret.setReportUniqueIdentifier(getParentReport().getIdentifier());
		ret.setWithinSegmentIdentifier(getWithinSegmentIdentifier());
		ret.setSegmentType(getSegmentType());
		ret.setReport(getParentReport());
		return ret;
	}

	public abstract Object getWithinSegmentIdentifier();
	public abstract String getSegmentLength();

	protected void setParentReport(AbstractReport parentReport) {
		this.parentReport = parentReport;
	}

	@JsonIgnore
	public AbstractReport getParentReport() {
		return parentReport;
	}

	public final char getSegmentType() {
		return segmentType;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1; //super.hashCode();
		result = prime * result + segmentType;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.hashCode() == hashCode();
	}

}
