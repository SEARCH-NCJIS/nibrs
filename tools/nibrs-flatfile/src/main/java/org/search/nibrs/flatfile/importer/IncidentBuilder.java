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
package org.search.nibrs.flatfile.importer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.search.nibrs.common.NIBRSError;
import org.search.nibrs.common.ParsedObject;
import org.search.nibrs.common.ReportSource;
import org.search.nibrs.flatfile.FlatfileConstants;
import org.search.nibrs.flatfile.NIBRSAgeBuilder;
import org.search.nibrs.flatfile.util.StringUtils;
import org.search.nibrs.importer.AbstractIncidentBuilder;
import org.search.nibrs.importer.ReportListener;
import org.search.nibrs.model.AbstractReport;
import org.search.nibrs.model.ArresteeSegment;
import org.search.nibrs.model.BadSegmentLevelReport;
import org.search.nibrs.model.GroupAIncidentReport;
import org.search.nibrs.model.GroupBArrestReport;
import org.search.nibrs.model.NIBRSAge;
import org.search.nibrs.model.OffenderSegment;
import org.search.nibrs.model.OffenseSegment;
import org.search.nibrs.model.PropertySegment;
import org.search.nibrs.model.VictimSegment;
import org.search.nibrs.model.ZeroReport;
import org.search.nibrs.model.codes.NIBRSErrorCode;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * Builder class that constructs incidents from a stream of NIBRS report data.
 * Incidents are broadcast to listeners as events; this keeps the class as
 * memory-unintensive as possible (NIBRS report streams can be rather large).
 * <br/>
 * At some point, if other report elements than Incidents are desired, this will
 * need to be modified. Currently, it only broadcasts Incident "add" records.
 * 
 */
@Component
@Scope(scopeName = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class IncidentBuilder extends AbstractIncidentBuilder {
	
	private static final Log log = LogFactory.getLog(IncidentBuilder.class);;

	public IncidentBuilder() {
		super();
	}

	public void addIncidentListener(ReportListener listener) {
		getListeners().add(listener);
	}

	public void removeIncidentListener(ReportListener listener) {
		getListeners().remove(listener);
	}

	/**
	 * Read NIBRS incidents from the flatfile format exposed by the specified Reader
	 * @param reader the source of the data
	 * @throws IOException exception encountered in addressing the Reader
	 */
	@Override
	public void buildIncidents(Reader reader, String readerLocationName) throws IOException {

		BufferedReader br = null;
		
		// we buffer to improve performance in reading big files
		if (!(reader instanceof BufferedReader)) {
			br = new BufferedReader(reader);
		} else {
			br = (BufferedReader) reader;
		}
		
		String line = null;
		AbstractReport currentReport = null;
		int lineNumber = 1;
		
		log.info("Processing NIBRS flat file");
		
		List<NIBRSError> errorList = new ArrayList<NIBRSError>();
		
		while ((line = br.readLine()) != null && org.apache.commons.lang3.StringUtils.isNotBlank(line)) {
			Segment s = new Segment();
			ReportSource reportSource = new ReportSource();
			reportSource.setSourceLocation(String.valueOf(lineNumber));
			reportSource.setSourceName(readerLocationName);
			List<NIBRSError> segmentErrors = s.setData(reportSource, line);
			errorList.addAll(segmentErrors);
			if (segmentErrors.isEmpty()) {
				char level = s.getSegmentLevel();
				if (level == ZeroReport.ZERO_REPORT_TYPE_IDENTIFIER 
						|| level == GroupAIncidentReport.ADMIN_SEGMENT_TYPE_IDENTIFIER 
						|| level == ArresteeSegment.GROUP_B_ARRESTEE_SEGMENT_TYPE_IDENTIFIER 
						|| !Objects.equals(currentReport.getIdentifier(), s.getSegmentUniqueIdentifier())) {
					handleNewReport(currentReport, errorList);
					errorList = new ArrayList<NIBRSError>();
					currentReport = buildReport(errorList, s, readerLocationName);
				} else {
					int errorListSize = errorList.size();
					if (currentReport instanceof GroupAIncidentReport){
						addSegmentToIncident((GroupAIncidentReport) currentReport, s, errorList);
					}
					if (errorList.size() > errorListSize && currentReport != null) {
						currentReport.setHasUpstreamErrors(true);
					}
				}
			}
			lineNumber++;
		}
		
		handleNewReport(currentReport, errorList);

		log.info("finished processing file, read " + (lineNumber - 1) + " lines.");
		log.info("Encountered " + getLogListener().errorCount + " error(s).");
		log.info("Created " + getLogListener().reportCount + " incident(s).");

	}

	AbstractReport buildReport(List<NIBRSError> errorList, Segment s, String readerLocationName) {
		int errorListSize = errorList.size();
		AbstractReport ret = null;
		char level = s.getSegmentLevel();
		if (level == GroupAIncidentReport.ADMIN_SEGMENT_TYPE_IDENTIFIER) {
			ret = buildGroupAIncidentSegment(s, errorList);
		} else if (level == ArresteeSegment.GROUP_B_ARRESTEE_SEGMENT_TYPE_IDENTIFIER) {
			ret = buildGroupBIncidentReport(s, errorList);
		} else if (level == ZeroReport.ZERO_REPORT_TYPE_IDENTIFIER) {
			ret = buildZeroReport(s, errorList);
		} else {
			ret = buildBadSegmentLevelIncidentSegment(s, errorList);
		}
		if (errorList.size() > errorListSize) {
			ret.setHasUpstreamErrors(true);
		}
		ret.setSource(s.getReportSource());
		return ret;
	}

	private ZeroReport buildZeroReport(Segment s, List<NIBRSError> errorList) {
		
		List<NIBRSError> newErrorList = new ArrayList<>();
		ZeroReport ret = new ZeroReport();
		ret.setOri(s.getOri());
		ret.setReportActionType(s.getActionType());
		int length = s.getSegmentLength();
		
		if (length == 43) {
			ret.setMonthOfTape(getIntValueFromSegment(s, 7, 8, newErrorList, NIBRSErrorCode._001));
			ret.setYearOfTape(getIntValueFromSegment(s, 9, 12, newErrorList, NIBRSErrorCode._001));
			ret.setCityIndicator(StringUtils.getStringBetween(13, 16, s.getData()));
		} else {
			NIBRSError e = new NIBRSError();
			e.setContext(s.getReportSource());
			e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
			e.setSegmentType(s.getSegmentType());
			e.setValue(length);
			e.setNIBRSErrorCode(NIBRSErrorCode._001);
			newErrorList.add(e);
		}
		
		for (NIBRSError e : newErrorList) {
			e.setReport(ret);
		}
		
		errorList.addAll(newErrorList);
		
		return ret;
		
	}

	private AbstractReport buildGroupBIncidentReport(Segment s, List<NIBRSError> errorList) {
		List<NIBRSError> newErrorList = new ArrayList<>();
		GroupBArrestReport ret = new GroupBArrestReport();
		ArresteeSegment arrestee = new ArresteeSegment(ArresteeSegment.GROUP_B_ARRESTEE_SEGMENT_TYPE_IDENTIFIER);
		arrestee.setParentReport(ret);
		String segmentData = s.getData();
		ret.setOri(s.getOri());
		ret.setReportActionType(s.getActionType());
		int length = s.getSegmentLength();
		if (length == 66 || length == 69) {
			if (length == 69) {
				String federalJucicialDistrictCode = StringUtils.getStringBetweenNoTrim(67, 69, segmentData);
				ret.setFederalJucicialDistrictCode(federalJucicialDistrictCode);
			}
			ret.setMonthOfTape(getIntValueFromSegment(s, 7, 8, newErrorList, NIBRSErrorCode._701));
			ret.setYearOfTape(getIntValueFromSegment(s, 9, 12, newErrorList, NIBRSErrorCode._701));
			ret.setCityIndicator(StringUtils.getStringBetween(13, 16, segmentData));
			
			ParsedObject<Integer> sequenceNumber = arrestee.getArresteeSequenceNumber();
			sequenceNumber.setMissing(false);
			sequenceNumber.setInvalid(false);
			String sequenceNumberString = StringUtils.getStringBetween(38, 39, segmentData);
			if (sequenceNumberString == null) {
				sequenceNumber.setMissing(true);
				sequenceNumber.setValue(null);
			} else {
				try {
					Integer sequenceNumberI = Integer.parseInt(sequenceNumberString);
					sequenceNumber.setValue(sequenceNumberI);
				} catch (NumberFormatException nfe) {
					NIBRSError e = new NIBRSError();
					e.setContext(s.getReportSource());
					e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
					e.setSegmentType(s.getSegmentType());
					e.setValue(sequenceNumberString);
					e.setNIBRSErrorCode(NIBRSErrorCode._701);
					e.setDataElementIdentifier("40");
					errorList.add(e);
					sequenceNumber.setInvalid(true);
					sequenceNumber.setValidationError(e);
				}
			}
			
			arrestee.setArresteeSequenceNumber(sequenceNumber);
			
			arrestee.setArrestTransactionNumber(StringUtils.getStringBetween(26, 37, segmentData));
			
			ParsedObject<LocalDate> arrestDate = arrestee.getArrestDate();
			arrestDate.setMissing(false);
			arrestDate.setInvalid(false);
			String arrestDateString = StringUtils.getStringBetween(40, 47, segmentData);
			if (arrestDateString == null) {
				arrestDate.setMissing(true);
				arrestDate.setValue(null);
			} else {
				try {
					LocalDate d = LocalDate.parse(arrestDateString, getDateFormat());
					arrestDate.setValue(d);
				} catch (Exception pe) {
					NIBRSError e = new NIBRSError();
					e.setContext(s.getReportSource());
					e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
					e.setSegmentType(s.getSegmentType());
					e.setValue(arrestDateString);
					e.setNIBRSErrorCode(NIBRSErrorCode._705);
					e.setDataElementIdentifier("42");
					newErrorList.add(e);
					arrestDate.setInvalid(true);
					arrestDate.setValidationError(e);
				}
			}
			arrestee.setArrestDate(arrestDate);
			
			arrestee.setTypeOfArrest(StringUtils.getStringBetween(48, 48, segmentData));
			arrestee.setUcrArrestOffenseCode(StringUtils.getStringBetween(49, 51, segmentData));
			for (int i = 0; i < 2; i++) {
				arrestee.setArresteeArmedWith(i, StringUtils.getStringBetween(52 + 3 * i, 53 + 3 * i, segmentData));
				arrestee.setAutomaticWeaponIndicator(i, StringUtils.getStringBetween(54 + 3 * i, 54 + 3 * i, segmentData));
			}
			NIBRSAge arresteeAge = NIBRSAgeBuilder.buildAgeFromRawString(StringUtils.getStringBetween(58, 61, segmentData), arrestee);
			arrestee.setAge(arresteeAge);
			arrestee.setSex(StringUtils.getStringBetween(62, 62, segmentData));
			arrestee.setRace(StringUtils.getStringBetween(63, 63, segmentData));
			arrestee.setEthnicity(StringUtils.getStringBetween(64, 64, segmentData));
			arrestee.setResidentStatus(StringUtils.getStringBetween(65, 65, segmentData));
			arrestee.setDispositionOfArresteeUnder18(StringUtils.getStringBetween(66, 66, segmentData));
		} else {
			NIBRSError e = new NIBRSError();
			e.setContext(s.getReportSource());
			e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
			e.setSegmentType(s.getSegmentType());
			e.setValue(length);
			e.setNIBRSErrorCode(NIBRSErrorCode._701);
			newErrorList.add(e);
		}
		
		for (NIBRSError e : newErrorList) {
			e.setReport(ret);
		}
		
		ret.addArrestee(arrestee);
		errorList.addAll(newErrorList);
	
		return ret;
	}

	private final void handleNewReport(AbstractReport newReport, List<NIBRSError> errorList) {
		if (newReport != null) {
			for (Iterator<ReportListener> it = getListeners().iterator(); it.hasNext();) {
				ReportListener listener = it.next();
				listener.newReport(newReport, errorList);
			}
		}
	}

	private final AbstractReport buildGroupAIncidentSegment(Segment s, List<NIBRSError> errorList) {
		List<NIBRSError> newErrorList = new ArrayList<>();
		GroupAIncidentReport newIncident = new GroupAIncidentReport();
		newIncident.setIncidentNumber(s.getSegmentUniqueIdentifier());
		newIncident.setOri(s.getOri());
		newIncident.setReportActionType(s.getActionType());
		String segmentData = s.getData();
		int length = s.getSegmentLength();
		if (length == 87 || length == 88 || length==91) {
			newIncident.setMonthOfTape(getIntValueFromSegment(s, 7, 8, newErrorList, NIBRSErrorCode._101));
			newIncident.setYearOfTape(getIntValueFromSegment(s, 9, 12, newErrorList, NIBRSErrorCode._101));
			newIncident.setCityIndicator(StringUtils.getStringBetween(13, 16, segmentData));
			ParsedObject<LocalDate> incidentDate = newIncident.getIncidentDate();
			incidentDate.setMissing(false);
			incidentDate.setInvalid(false);
			String incidentDateString = StringUtils.getStringBetween(38, 45, segmentData);
			if (incidentDateString == null) {
				incidentDate.setMissing(true);
				incidentDate.setValue(null);
			} else {
				try {
					LocalDate d = LocalDate.parse(incidentDateString, getDateFormat());
					incidentDate.setValue(d);
				} catch (Exception pe) {
					NIBRSError e = new NIBRSError();
					e.setContext(s.getReportSource());
					e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
					e.setSegmentType(s.getSegmentType());
					e.setValue(incidentDateString);
					e.setNIBRSErrorCode(NIBRSErrorCode._105);
					e.setDataElementIdentifier("3");
					newErrorList.add(e);
					incidentDate.setInvalid(true);
					incidentDate.setValidationError(e);
				}
			}
			newIncident.setIncidentDate(incidentDate);
			
			newIncident.setReportDateIndicator(StringUtils.getStringBetween(46, 46, segmentData));
			
			String hourString = StringUtils.getStringBetween(47, 48, segmentData);
			ParsedObject<Integer> hour = newIncident.getIncidentHour();
			hour.setMissing(false);
			hour.setInvalid(false);
			if (hourString != null && hourString.trim().length() > 0) {
				try {
					
					if (hourString.length() != 2){
						throw new NumberFormatException(); 
					}
					Integer hourI = new Integer(hourString);
					hour.setValue(hourI);
				} catch(NumberFormatException nfe) {
					
					NIBRSError e152 = new NIBRSError();
					e152.setContext(s.getReportSource());
					e152.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
					e152.setSegmentType(s.getSegmentType());
					e152.setValue(hourString);
					e152.setNIBRSErrorCode(NIBRSErrorCode._152);
					e152.setDataElementIdentifier("3");
					newErrorList.add(e152);
					
					NIBRSError e = new NIBRSError();
					e.setContext(s.getReportSource());
					e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
					e.setSegmentType(s.getSegmentType());
					e.setValue(hourString);
					e.setNIBRSErrorCode(NIBRSErrorCode._104);
					e.setDataElementIdentifier("3");
					newErrorList.add(e);
					hour.setInvalid(true);
					hour.setValidationError(e);
				}
			} else {
				hour.setMissing(true);
			}
			
			newIncident.setExceptionalClearanceCode(StringUtils.getStringBetween(49, 49, segmentData));
			
			ParsedObject<LocalDate> clearanceDate = newIncident.getExceptionalClearanceDate();
			clearanceDate.setMissing(false);
			clearanceDate.setInvalid(false);
			String clearanceDateString = StringUtils.getStringBetween(50, 57, segmentData);
			if (clearanceDateString == null) {
				clearanceDate.setMissing(true);
				clearanceDate.setValue(null);
			} else {
				try {
					LocalDate d = LocalDate.parse(clearanceDateString, getDateFormat());
					clearanceDate.setValue(d);
				} catch (Exception pe) {
					NIBRSError e = new NIBRSError();
					e.setContext(s.getReportSource());
					e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
					e.setSegmentType(s.getSegmentType());
					e.setValue(clearanceDateString);
					e.setNIBRSErrorCode(NIBRSErrorCode._105);
					e.setDataElementIdentifier("5");
					newErrorList.add(e);
					clearanceDate.setInvalid(true);
					clearanceDate.setValidationError(e);
				}
			}
			newIncident.setExceptionalClearanceDate(clearanceDate);
			
			boolean cargoTheft = (length == 88 || length == 91);
			if (cargoTheft) {
				String cargoTheftYN = StringUtils.getStringBetweenNoTrim(88, 88, segmentData);
				
				if (cargoTheftYN != null){
					newIncident.setCargoTheftIndicator(cargoTheftYN);
					newIncident.setIncludesCargoTheft(true);
				}
				else{
					//TODO comment out temporarily  --hw
//					NIBRSError e = new NIBRSError();
//					e.setContext(s.getReportSource());
//					e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
//					e.setSegmentType(s.getSegmentType());
//					e.setDataElementIdentifier("2A");
//					e.setNIBRSErrorCode(NIBRSErrorCode._101);
//					newErrorList.add(e);
				}
				
				if (length == 91) {
					String federalJucicialDistrictCode = StringUtils.getStringBetweenNoTrim(89, 91, segmentData);
					newIncident.setFederalJucicialDistrictCode(federalJucicialDistrictCode);
				}
			}
			
		} else {
			NIBRSError e = new NIBRSError();
			e.setContext(s.getReportSource());
			e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
			e.setSegmentType(s.getSegmentType());
			e.setValue(length);
			e.setNIBRSErrorCode(NIBRSErrorCode._178);
			newErrorList.add(e);
		}
		for (NIBRSError e : newErrorList) {
			e.setReport(newIncident);
		}
		errorList.addAll(newErrorList);
		return newIncident;
	}
	private final AbstractReport buildBadSegmentLevelIncidentSegment(Segment s, List<NIBRSError> errorList) {
		List<NIBRSError> newErrorList = new ArrayList<>();
		BadSegmentLevelReport newIncident = new BadSegmentLevelReport();
		newIncident.setIncidentNumber(s.getSegmentUniqueIdentifier());
		newIncident.setOri(s.getOri());
		newIncident.setReportActionType(s.getActionType());
		String segmentData = s.getData();
		int length = s.getSegmentLength();
		if (length >=38 ) {
			newIncident.setMonthOfTape(getIntValueFromSegment(s, 7, 8, newErrorList, NIBRSErrorCode._101));
			newIncident.setYearOfTape(getIntValueFromSegment(s, 9, 12, newErrorList, NIBRSErrorCode._101));
			newIncident.setCityIndicator(StringUtils.getStringBetween(13, 16, segmentData));
		}
		
		NIBRSError e = new NIBRSError();
		e.setContext(s.getReportSource());
		e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
		e.setNIBRSErrorCode(NIBRSErrorCode._050);
		e.setCrossSegment(true);
		newErrorList.add(e);
		
		e.setReport(newIncident);
		errorList.addAll(newErrorList);
		return newIncident;
	}

	private Integer getIntValueFromSegment(Segment s, int startPos, int endPos, List<NIBRSError> errorList, NIBRSErrorCode errorCode) {
		String sv = StringUtils.getStringBetween(startPos, endPos, s.getData());
		Integer i = null;
		try {
			i = new Integer(sv);
		} catch (NumberFormatException nfe) {
			NIBRSError e = new NIBRSError();
			e.setContext(s.getReportSource());
			e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
			e.setNIBRSErrorCode(errorCode);
			e.setValue(sv);
			e.setSegmentType(s.getSegmentType());
			errorList.add(e);
			log.debug("Error in int conversion: lineNumber=" + s.getReportSource() + ", value=" + sv);
		}
		return i;
	}

	private final void addSegmentToIncident(GroupAIncidentReport currentIncident, Segment s, List<NIBRSError> errorList) {
		if (Objects.isNull(currentIncident)) return; 
		
		List<NIBRSError> newErrorList = new ArrayList<>();
		char segmentType = s.getSegmentType();
		switch (segmentType) {
		case OffenseSegment.OFFENSE_SEGMENT_TYPE_IDENTIFIER:
			currentIncident.addOffense(buildOffenseSegment(s, currentIncident, newErrorList));
			break;
		case PropertySegment.PROPERTY_SEGMENT_TYPE_IDENTIFIER:
			currentIncident.addProperty(buildPropertySegment(s, currentIncident, newErrorList));
			break;
		case VictimSegment.VICTIM_SEGMENT_TYPE_IDENTIFIER:
			currentIncident.addVictim(buildVictimSegment(s, currentIncident, newErrorList));
			break;
		case OffenderSegment.OFFENDER_SEGMENT_TYPE_IDENTIFIER:
			currentIncident.addOffender(buildOffenderSegment(s, currentIncident, newErrorList));
			break;
		case ArresteeSegment.GROUP_A_ARRESTEE_SEGMENT_TYPE_IDENTIFIER:
			currentIncident.addArrestee(buildGroupAArresteeSegment(s, currentIncident, newErrorList));
			break;
		case '8':
			log.info("Skip the State Specific segment 8");
			break; 
		case '9':
			log.info("Skip the State Specific segment 9");
			break; 
		default:
			NIBRSError error = new NIBRSError();
			error.setContext(s.getReportSource());
			error.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
			error.setNIBRSErrorCode(NIBRSErrorCode._051);
			error.setValue(segmentType);
			newErrorList.add(error);
		}
		for (NIBRSError e : newErrorList) {
			e.setReport(currentIncident);
		}
		errorList.addAll(newErrorList);
	}

	private ArresteeSegment buildGroupAArresteeSegment(Segment s, GroupAIncidentReport parent, List<NIBRSError> errorList) {
		ArresteeSegment newArrestee = new ArresteeSegment(ArresteeSegment.GROUP_A_ARRESTEE_SEGMENT_TYPE_IDENTIFIER);
		newArrestee.setParentReport(parent);
		String segmentData = s.getData();
		int length = s.getSegmentLength();
		if (length == FlatfileConstants.GROUP_A_ARRESTEE_SEGMENT_LENGTH) {
			
			ParsedObject<Integer> sequenceNumber = newArrestee.getArresteeSequenceNumber();
			sequenceNumber.setMissing(false);
			sequenceNumber.setInvalid(false);
			String sequenceNumberString = StringUtils.getStringBetween(38, 39, segmentData);
			if (sequenceNumberString == null) {
				sequenceNumber.setMissing(true);
				sequenceNumber.setValue(null);
			} else {
				try {
					Integer sequenceNumberI = Integer.parseInt(sequenceNumberString);
					sequenceNumber.setValue(sequenceNumberI);
				} catch (NumberFormatException nfe) {
					NIBRSError e = new NIBRSError();
					e.setContext(s.getReportSource());
					e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
					e.setSegmentType(s.getSegmentType());
					e.setValue(sequenceNumberString);
					e.setNIBRSErrorCode(NIBRSErrorCode._601);
					e.setDataElementIdentifier("40");
					errorList.add(e);
					sequenceNumber.setInvalid(true);
					sequenceNumber.setValidationError(e);
				}
			}
			
			newArrestee.setArresteeSequenceNumber(sequenceNumber);
			
			newArrestee.setArrestTransactionNumber(StringUtils.getStringBetween(40, 51, segmentData));
			
			ParsedObject<LocalDate> arrestDate = newArrestee.getArrestDate();
			arrestDate.setMissing(false);
			arrestDate.setInvalid(false);
			String arrestDateString = StringUtils.getStringBetween(52, 59, segmentData);
			if (arrestDateString == null) {
				arrestDate.setMissing(true);
				arrestDate.setValue(null);
			} else {
				try {
					LocalDate d = LocalDate.parse(arrestDateString, getDateFormat());
					arrestDate.setValue(d);
				} catch (Exception pe) {
					NIBRSError e = new NIBRSError();
					e.setContext(s.getReportSource());
					e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
					e.setSegmentType(s.getSegmentType());
					e.setValue(arrestDateString);
					e.setNIBRSErrorCode(NIBRSErrorCode._705);
					e.setDataElementIdentifier("42");
					errorList.add(e);
					arrestDate.setInvalid(true);
					arrestDate.setValidationError(e);
				}
			}
			newArrestee.setArrestDate(arrestDate);
			
			newArrestee.setTypeOfArrest(StringUtils.getStringBetween(60, 60, segmentData));
			newArrestee.setMultipleArresteeSegmentsIndicator(StringUtils.getStringBetween(61, 61, segmentData));
			newArrestee.setUcrArrestOffenseCode(StringUtils.getStringBetween(62, 64, segmentData));
			for (int i = 0; i < ArresteeSegment.ARRESTEE_ARMED_WITH_COUNT; i++) {
				newArrestee.setArresteeArmedWith(i, StringUtils.getStringBetween(65 + 3 * i, 66 + 3 * i, segmentData));
			}
			for (int i = 0; i < ArresteeSegment.AUTOMATIC_WEAPON_INDICATOR_COUNT; i++) {
				newArrestee.setAutomaticWeaponIndicator(i, StringUtils.getStringBetween(67 + 3 * i, 67 + 3 * i, segmentData));
			}
			NIBRSAge arresteeAge = NIBRSAgeBuilder.buildAgeFromRawString(StringUtils.getStringBetween(71, 74, segmentData), newArrestee);
			newArrestee.setAge(arresteeAge);
			newArrestee.setSex(StringUtils.getStringBetween(75, 75, segmentData));
			newArrestee.setRace(StringUtils.getStringBetween(76, 76, segmentData));
			newArrestee.setEthnicity(StringUtils.getStringBetween(77, 77, segmentData));
			newArrestee.setResidentStatus(StringUtils.getStringBetween(78, 78, segmentData));
			newArrestee.setDispositionOfArresteeUnder18(StringUtils.getStringBetween(79, 79, segmentData));
		} else {
			NIBRSError e = new NIBRSError();
			e.setContext(s.getReportSource());
			e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
			e.setSegmentType(s.getSegmentType());
			e.setValue(length);
			e.setNIBRSErrorCode(NIBRSErrorCode._601);
			errorList.add(e);
		}
		return newArrestee;
	}

	private OffenderSegment buildOffenderSegment(Segment s, GroupAIncidentReport parent, List<NIBRSError> errorList) {
		OffenderSegment newOffender = new OffenderSegment();
		newOffender.setParentReport(parent);
		String segmentData = s.getData();
		int length = s.getSegmentLength();
		if (length == FlatfileConstants.OFFENDER_WITHOUT_ETHNICITY_SEGMENT_LENGTH || length == FlatfileConstants.OFFENDER_WITH_ETHNICITY_SEGMENT_LENGTH) {
			
			ParsedObject<Integer> sequenceNumber = newOffender.getOffenderSequenceNumber();
			sequenceNumber.setMissing(false);
			sequenceNumber.setInvalid(false);
			String sequenceNumberString = StringUtils.getStringBetween(38, 39, segmentData);
			if (sequenceNumberString == null) {
				sequenceNumber.setMissing(true);
				sequenceNumber.setValue(null);
			} else {
				try {
					Integer sequenceNumberI = Integer.parseInt(sequenceNumberString);
					sequenceNumber.setValue(sequenceNumberI);
				} catch (NumberFormatException nfe) {
					NIBRSError e = new NIBRSError();
					e.setContext(s.getReportSource());
					e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
					e.setSegmentType(s.getSegmentType());
					e.setValue(sequenceNumberString);
					e.setNIBRSErrorCode(NIBRSErrorCode._501);
					e.setDataElementIdentifier("36");
					errorList.add(e);
					sequenceNumber.setInvalid(true);
					sequenceNumber.setValidationError(e);
				}
			}
			
			NIBRSAge offenderAge = NIBRSAgeBuilder.buildAgeFromRawString(StringUtils.getStringBetween(40, 43, segmentData), newOffender);
			newOffender.setAge(offenderAge);
			newOffender.setSex(StringUtils.getStringBetween(44, 44, segmentData));
			newOffender.setRace(StringUtils.getStringBetween(45, 45, segmentData));
			boolean hasOffenderEthnicity = length == FlatfileConstants.OFFENDER_WITH_ETHNICITY_SEGMENT_LENGTH;
			if (hasOffenderEthnicity) {
				newOffender.setEthnicity(StringUtils.getStringBetween(46, 46, segmentData));
			}
		} else {
			NIBRSError e = new NIBRSError();
			e.setContext(s.getReportSource());
			e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
			e.setSegmentType(s.getSegmentType());
			e.setValue(length);
			e.setNIBRSErrorCode(NIBRSErrorCode._584);
			errorList.add(e);
		}
		return newOffender;
	}

	private VictimSegment buildVictimSegment(Segment s, GroupAIncidentReport parentIncident, List<NIBRSError> errorList) {

		VictimSegment newVictim = new VictimSegment();
		newVictim.setParentReport(parentIncident);
		String segmentData = s.getData();
		int length = s.getSegmentLength();

		if (length == 129 || length >= 141) {

			Integer sequenceNumberI = null;
			ParsedObject<Integer> sequenceNumber = newVictim.getVictimSequenceNumber();
			sequenceNumber.setMissing(false);
			sequenceNumber.setInvalid(false);
			String sequenceNumberString = StringUtils.getStringBetween(38, 40, segmentData);
			if (sequenceNumberString == null) {
				sequenceNumber.setMissing(true);
				sequenceNumber.setValue(null);
				NIBRSError e = new NIBRSError();
				e.setContext(s.getReportSource());
				e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
				e.setSegmentType(s.getSegmentType());
				e.setNIBRSErrorCode(NIBRSErrorCode._401);
				e.setDataElementIdentifier("23");
				errorList.add(e);
			} else {
				try {
					sequenceNumberI = Integer.parseInt(sequenceNumberString);
					sequenceNumber.setValue(sequenceNumberI);
				} catch (NumberFormatException nfe) {
					NIBRSError e = new NIBRSError();
					e.setContext(s.getReportSource());
					e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
					e.setSegmentType(s.getSegmentType());
					e.setValue(sequenceNumberString);
					e.setNIBRSErrorCode(NIBRSErrorCode._402);
					e.setDataElementIdentifier("23");
					errorList.add(e);
					sequenceNumber.setInvalid(true);
					sequenceNumber.setValidationError(e);
				}
			}

			for (int i = 0; i < VictimSegment.UCR_OFFENSE_CODE_CONNECTION_COUNT; i++) {
				newVictim.setUcrOffenseCodeConnection(i, StringUtils.getStringBetween(41 + 3 * i, 43 + 3 * i, segmentData));
			}
			for (int i = 0; i < VictimSegment.OFFENDER_NUMBER_RELATED_COUNT; i++) {
				String offenderNumberRelatedString = StringUtils.getStringBetween(90 + 4 * i, 91 + 4 * i, segmentData);
				ParsedObject<Integer> offenderNumberRelated = newVictim.getOffenderNumberRelated(i);
				offenderNumberRelated.setInvalid(false);
				offenderNumberRelated.setMissing(false);
				if (offenderNumberRelatedString == null) {
					offenderNumberRelated.setMissing(true);
					offenderNumberRelated.setInvalid(false);
				} else {
					try {
						Integer offenderNumberRelatedValue = Integer.parseInt(offenderNumberRelatedString);
						offenderNumberRelated.setValue(offenderNumberRelatedValue);
						newVictim.setOffenderNumberRelated(i, offenderNumberRelated);
					} catch (NumberFormatException nfe) {
						NIBRSError e = new NIBRSError();
						e.setContext(s.getReportSource());
						e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
						e.setSegmentType(s.getSegmentType());
						e.setValue(StringUtils.getStringBetween(90 + 4 * i, 91 + 4 * i, segmentData));
						e.setNIBRSErrorCode(NIBRSErrorCode._402);
						e.setWithinSegmentIdentifier(sequenceNumberI);
						e.setDataElementIdentifier("34");
						errorList.add(e);
						offenderNumberRelated.setMissing(false);
						offenderNumberRelated.setInvalid(true);
					}
				}
			}
			for (int i = 0; i < VictimSegment.OFFENDER_NUMBER_RELATED_COUNT; i++) {
				newVictim.setVictimOffenderRelationship(i, StringUtils.getStringBetween(92 + 4 * i, 93 + 4 * i, segmentData));
			}

			newVictim.setTypeOfVictim(StringUtils.getStringBetween(71, 71, segmentData));
			
			NIBRSAge victimAge = NIBRSAgeBuilder.buildAgeFromRawString(StringUtils.getStringBetween(72, 75, segmentData), newVictim);
			newVictim.setAge(victimAge);
			
			newVictim.setSex(StringUtils.getStringBetween(76, 76, segmentData));
			newVictim.setRace(StringUtils.getStringBetween(77, 77, segmentData));
			newVictim.setEthnicity(StringUtils.getStringBetween(78, 78, segmentData));
			newVictim.setResidentStatus(StringUtils.getStringBetween(79, 79, segmentData));
			newVictim.setAggravatedAssaultHomicideCircumstances(0, StringUtils.getStringBetween(80, 81, segmentData));
			newVictim.setAggravatedAssaultHomicideCircumstances(1, StringUtils.getStringBetween(82, 83, segmentData));
			newVictim.setAdditionalJustifiableHomicideCircumstances(StringUtils.getStringBetween(84, 84, segmentData));

			for (int i = 0; i < VictimSegment.TYPE_OF_INJURY_COUNT; i++) {
				newVictim.setTypeOfInjury(i, StringUtils.getStringBetween(85 + i, 85 + i, segmentData));
			}

			boolean leoka = length == 141;

			if (leoka) {
				newVictim.setTypeOfOfficerActivityCircumstance(StringUtils.getStringBetween(130, 131, segmentData));
				newVictim.setOfficerAssignmentType(StringUtils.getStringBetween(132, 132, segmentData));
				newVictim.setOfficerOtherJurisdictionORI(StringUtils.getStringBetween(133, 141, segmentData));
			}
			
			parentIncident.setIncludesLeoka(leoka);
		} else {
			NIBRSError e = new NIBRSError();
			e.setContext(s.getReportSource());
			e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
			e.setSegmentType(s.getSegmentType());
			e.setValue(length);
			e.setNIBRSErrorCode(NIBRSErrorCode._484);
			errorList.add(e);
		}

		return newVictim;

	}

	private PropertySegment buildPropertySegment(Segment s, GroupAIncidentReport parentIncident, List<NIBRSError> errorList) {

		PropertySegment newProperty = new PropertySegment();
		newProperty.setParentReport(parentIncident);
		String segmentData = s.getData();
		int length = s.getSegmentLength();

		if (length == FlatfileConstants.PROPERTY_SEGMENT_LENGTH) {

			String typeOfPropertyLoss = StringUtils.getStringBetween(38, 38, segmentData);
			newProperty.setTypeOfPropertyLoss(typeOfPropertyLoss);

			for (int i = 0; i < PropertySegment.PROPERTY_DESCRIPTION_COUNT; i++) {
				newProperty.setPropertyDescription(i, StringUtils.getStringBetween(39 + 19 * i, 40 + 19 * i, segmentData));
			}
			for (int i = 0; i < PropertySegment.VALUE_OF_PROPERTY_COUNT; i++) {
				String propertyValueString = StringUtils.getStringBetween(41 + 19 * i, 49 + 19 * i, segmentData);
				ParsedObject<Integer> propertyValue = newProperty.getValueOfProperty(i);
				propertyValue.setInvalid(false);
				propertyValue.setMissing(false);
				if (propertyValueString == null) {
					propertyValue.setValue(null);
					propertyValue.setInvalid(false);
					propertyValue.setMissing(true);
				} else {
					try {
						String valueOfPropertyPattern = "\\d{1,9}";
						if (propertyValueString.matches(valueOfPropertyPattern)){
							Integer propertyValueI = Integer.parseInt(propertyValueString);
							propertyValue.setValue(propertyValueI);
						}
						else{
							throw new NumberFormatException(); 
						}
					} catch (NumberFormatException nfe) {
						NIBRSError e = new NIBRSError();
						e.setContext(s.getReportSource());
						e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
						e.setSegmentType(s.getSegmentType());
						e.setValue(org.apache.commons.lang3.StringUtils.leftPad(propertyValueString, 9));
						e.setNIBRSErrorCode(NIBRSErrorCode._302);
						e.setWithinSegmentIdentifier(null);
						e.setDataElementIdentifier("16");
						errorList.add(e);
						propertyValue.setMissing(false);
						propertyValue.setInvalid(true);
					}
				}
			}
			for (int i = 0; i < PropertySegment.DATE_RECOVERED_COUNT; i++) {
				
				ParsedObject<LocalDate> d = newProperty.getDateRecovered(i);
				d.setMissing(false);
				d.setInvalid(false);
				String ds = StringUtils.getStringBetween(50 + 19 * i, 57 + 19 * i, segmentData);
				if (ds == null) {
					d.setMissing(true);
					d.setValue(null);
				} else {
					try {
						LocalDate dd = LocalDate.parse(ds, getDateFormat());
						d.setValue(dd);
					} catch (Exception pe) {
						NIBRSError e = new NIBRSError();
						e.setContext(s.getReportSource());
						e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
						e.setSegmentType(s.getSegmentType());
						e.setValue(ds);
						e.setNIBRSErrorCode(NIBRSErrorCode._305);
						e.setDataElementIdentifier("17");
						errorList.add(e);
						d.setInvalid(true);
						d.setValidationError(e);
					}
				}
				
			}

			parseIntegerObject(segmentData, newProperty.getNumberOfStolenMotorVehicles(), 229, 230);
			parseIntegerObject(segmentData, newProperty.getNumberOfRecoveredMotorVehicles(), 231, 232);

			for (int i = 0; i < PropertySegment.SUSPECTED_DRUG_TYPE_COUNT; i++) {
				newProperty.setSuspectedDrugType(i, StringUtils.getStringBetween(233 + 15 * i, 233 + 15 * i, segmentData));
				String drugQuantityWholePartString = StringUtils.getStringBetween(234 + 15 * i, 242 + 15 * i, segmentData);
				String drugQuantityFractionalPartString = StringUtils.getStringBetween(243 + 15 * i, 245 + 15 * i, segmentData);
				if (drugQuantityWholePartString != null || drugQuantityFractionalPartString != null) {
					String fractionalValueString = "000";
					String value = org.apache.commons.lang3.StringUtils.isBlank(drugQuantityWholePartString)? "0":drugQuantityWholePartString.trim();
					if (drugQuantityFractionalPartString != null) {
						fractionalValueString = drugQuantityFractionalPartString;
						value += fractionalValueString;
					}
					
					String drugQuantityFullValueString = org.apache.commons.lang3.StringUtils.trimToEmpty(drugQuantityWholePartString) + "." + fractionalValueString;
					
					try{
						Double doubleValue = new Double(drugQuantityFullValueString);
						newProperty.setEstimatedDrugQuantity(i, new ParsedObject<Double>(doubleValue));
					}
					catch (NumberFormatException ne){
						log.error(ne);
						ParsedObject<Double> estimatedDrugQuantity = ParsedObject.getInvalidParsedObject();
						newProperty.setEstimatedDrugQuantity(i, estimatedDrugQuantity);
						NIBRSError e = new NIBRSError();
						e.setContext(s.getReportSource());
						e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
						e.setSegmentType(s.getSegmentType());
						e.setValue(value);
						e.setNIBRSErrorCode(NIBRSErrorCode._302);
						e.setWithinSegmentIdentifier(null);
						e.setDataElementIdentifier("21");
						errorList.add(e);
						estimatedDrugQuantity.setValidationError(e);

					}
				}
				else{
					newProperty.setEstimatedDrugQuantity(i, ParsedObject.getMissingParsedObject());
				}
				
				newProperty.setTypeDrugMeasurement(i, StringUtils.getStringBetween(246 + 15 * i, 247 + 15 * i, segmentData));
			}

		} else {
			NIBRSError e = new NIBRSError();
			e.setContext(s.getReportSource());
			e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
			e.setSegmentType(s.getSegmentType());
			e.setValue(length);
			e.setNIBRSErrorCode(NIBRSErrorCode._401);
			errorList.add(e);
		}

		return newProperty;

	}

	private void parseIntegerObject(String segmentData,
			ParsedObject<Integer> parsedObject, 
			int startPosition, 
			int endPosition) {
		
		parsedObject.setMissing(false);
		parsedObject.setInvalid(false);
		
		String parsedString = 
				StringUtils.getStringBetween(startPosition, endPosition, segmentData);
		if (parsedString == null) {
			parsedObject.setMissing(true);
			parsedObject.setValue(null);
		} else {
			try {
				parsedObject.setValue(Integer.parseInt(parsedString));
			} catch (NumberFormatException nfe) {
				parsedObject.setInvalid(true);
			}
		}
	}

	private OffenseSegment buildOffenseSegment(Segment s, GroupAIncidentReport parentIncident, List<NIBRSError> errorList) {

		OffenseSegment newOffense = new OffenseSegment();
		newOffense.setParentReport(parentIncident);

		String segmentData = s.getData();
		int length = s.getSegmentLength();

		if (length == FlatfileConstants.OFFENSE_SINGLE_BIAS_SEGMENT_LENGTH || length == FlatfileConstants.OFFENSE_MULTIPLE_BIAS_SEGMENT_LENGTH) {

			newOffense.setUcrOffenseCode(StringUtils.getStringBetween(38, 40, segmentData));
			newOffense.setOffenseAttemptedCompleted(StringUtils.getStringBetween(41, 41, segmentData));
			newOffense.setLocationType(StringUtils.getStringBetween(45, 46, segmentData));
			
			String premisesEnteredString = StringUtils.getStringBetween(47, 48, segmentData);
			ParsedObject<Integer> premisesEntered = newOffense.getNumberOfPremisesEntered();
			
			if (premisesEnteredString == null) {
				premisesEntered.setMissing(true);
				premisesEntered.setInvalid(false);
				premisesEntered.setValue(null);
			} else {
				
				try {
					Integer value = Integer.parseInt(premisesEnteredString);
					premisesEntered.setValue(value);
					premisesEntered.setMissing(false);
					premisesEntered.setInvalid(false);
				} catch (NumberFormatException nfe) {
					NIBRSError e = new NIBRSError();
					e.setContext(s.getReportSource());
					e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
					e.setSegmentType(s.getSegmentType());
					e.setValue(premisesEnteredString);
					e.setNIBRSErrorCode(NIBRSErrorCode._204);
					e.setDataElementIdentifier("10");
					errorList.add(e);
					premisesEntered.setInvalid(true);
					premisesEntered.setValidationError(e);
				}
				
			}
			
			newOffense.setMethodOfEntry(StringUtils.getStringBetween(49, 49, segmentData));

			int biasMotivationFields = length == FlatfileConstants.OFFENSE_SINGLE_BIAS_SEGMENT_LENGTH ? 1 : OffenseSegment.BIAS_MOTIVATION_COUNT;

			for (int i = 0; i < biasMotivationFields; i++) {
				newOffense.setBiasMotivation(i, StringUtils.getStringBetween(62 + 2*i, 63 + 2*i, segmentData));
			}

			for (int i = 0; i < OffenseSegment.OFFENDERS_SUSPECTED_OF_USING_COUNT; i++) {
				newOffense.setOffendersSuspectedOfUsing(i, StringUtils.getStringBetween(42 + i, 42 + i, segmentData));
			}
			for (int i = 0; i < OffenseSegment.TYPE_OF_CRIMINAL_ACTIVITY_COUNT; i++) {
				newOffense.setTypeOfCriminalActivity(i, StringUtils.getStringBetween(50 + i, 50 + i, segmentData));
			}
			for (int i = 0; i < OffenseSegment.TYPE_OF_WEAPON_FORCE_INVOLVED_COUNT; i++) {
				newOffense.setTypeOfWeaponForceInvolved(i, StringUtils.getStringBetween(53 + 3 * i, 54 + 3 * i, segmentData));
			}
			for (int i = 0; i < OffenseSegment.AUTOMATIC_WEAPON_INDICATOR_COUNT; i++) {
				newOffense.setAutomaticWeaponIndicator(i, StringUtils.getStringBetween(55 + 3 * i, 55 + 3 * i, segmentData));
			}

		} else {
			NIBRSError e = new NIBRSError();
			e.setContext(s.getReportSource());
			e.setReportUniqueIdentifier(s.getSegmentUniqueIdentifier());
			e.setSegmentType(s.getSegmentType());
			e.setValue(length);
			e.setNIBRSErrorCode(NIBRSErrorCode._284);
			errorList.add(e);
		}

		return newOffense;

	}

}
