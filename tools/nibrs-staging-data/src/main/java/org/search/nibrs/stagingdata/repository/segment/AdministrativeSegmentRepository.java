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
package org.search.nibrs.stagingdata.repository.segment;

import java.sql.Date;
import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import org.search.nibrs.stagingdata.model.segment.AdministrativeSegment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Transactional
@Repository
public interface AdministrativeSegmentRepository 
	extends JpaRepository<AdministrativeSegment, Integer>, JpaSpecificationExecutor<AdministrativeSegment> {
	
	long deleteByIncidentNumber(String incidentNumber);
	
	@EntityGraph(value="allAdministrativeSegmentJoins", type=EntityGraphType.LOAD)
	List<AdministrativeSegment> findByIncidentNumber(String incidentNumber);
	
	@EntityGraph(value="allAdministrativeSegmentJoins", type=EntityGraphType.LOAD)
	AdministrativeSegment findByAdministrativeSegmentId(Integer administrativeSegmentId);
	
	@Query("SELECT count(*) > 0 from AdministrativeSegment a "
			+ "LEFT JOIN a.segmentActionType s "
			+ "WHERE a.administrativeSegmentId = "
			+ "		(SELECT max(administrativeSegmentId) FROM AdministrativeSegment "
			+ "			where incidentNumber = ?1 and ori = ?2) "
			+ "		AND s.nibrsCode != 'D' ")
	boolean existsByIncidentNumberAndOri(String incidentNumber, String ori);
	
	@Query("SELECT count(*) > 0 from AdministrativeSegment a "
			+ "LEFT JOIN a.segmentActionType s "
			+ "WHERE a.administrativeSegmentId = "
			+ "		(SELECT max(administrativeSegmentId) FROM AdministrativeSegment aa "
			+ "			where aa.incidentNumber = ?1 and aa.ori = ?2 AND "
			+ "				(?4 = null OR ?4 = 0 OR aa.owner.ownerId = ?4 )) "
			+ "		and cast(concat(a.yearOfTape, '-', a.monthOfTape, '-01') as date) > ?3")
	boolean existsByIncidentNumberAndOriAndSubmissionDateAndOwnerId(String incidentNumber, String ori, Date submissionDate, Integer ownerId);
	
	@Query("SELECT distinct a.agency.agencyId from AdministrativeSegment a "
			+ "WHERE ?1 = null OR a.owner.ownerId = ?1 ")
	Set<Integer> findAgencyIdsByOwnerId(Integer ownerId);
	
	@EntityGraph(value="allAdministrativeSegmentJoins", type=EntityGraphType.LOAD)
	List<AdministrativeSegment> findDistinctByOriAndIncidentDateTypeYearNumAndIncidentDateTypeMonthNum(String ori, Integer year,  Integer month);
		
	@EntityGraph(value="allAdministrativeSegmentJoins", type=EntityGraphType.LOAD)
	List<AdministrativeSegment> findDistinctByIncidentDateTypeYearNumAndIncidentDateTypeMonthNum(Integer year,  Integer month);
	
	@EntityGraph(value="allAdministrativeSegmentJoins", type=EntityGraphType.LOAD)
	List<AdministrativeSegment> findAllById(Iterable<Integer> ids);
	
	@Query("SELECT DISTINCT a.administrativeSegmentId from AdministrativeSegment a "
			+ "WHERE (?1 = null OR a.ori in (?1)) AND "
			+ "		(?4 = null OR a.agency.agencyId in (?4)) AND "
			+ "		(?2 = null OR cast(concat(a.yearOfTape, '-', a.monthOfTape, '-01') as date) >= ?2 ) AND "
			+ "		(?3 = null OR cast(concat(a.yearOfTape, '-', a.monthOfTape, '-01') as date) <= ?3) AND "
			+ "     ( a.submission = null )"
			+ "ORDER BY a.administrativeSegmentId asc ")
	List<Integer> findIdsByOriListAndSubmissionDateRange(List<String> oris, Date startDate, Date endDate, List<Integer> agencyIds);
	
	@Query("SELECT DISTINCT year(a.incidentDate) as incidentYear from AdministrativeSegment a "
			+ "WHERE a.agency.agencyId = ?1 AND (?2 = null OR ?2=0 OR a.owner.ownerId = ?2)"
			+ "ORDER BY incidentYear ")
	List<Integer> findDistinctYears(Integer agencyId, Integer ownerId);
	
	@Query("SELECT DISTINCT month(a.incidentDate) as incidentMonth from AdministrativeSegment a "
			+ "WHERE a.agency.agencyId = ?1 AND year(a.incidentDate) = ?2 AND (?3 = null OR ?3 = 0 OR a.owner.ownerId = ?3)"
			+ "ORDER BY incidentMonth ")
	List<Integer> findDistinctMonths(Integer agencyId, Integer Year, Integer ownerId);
	
	@Query("SELECT a.administrativeSegmentId from AdministrativeSegment a "
			+ "LEFT JOIN a.arresteeSegments ar "
			+ "WHERE a.administrativeSegmentId = ( SELECT max(administrativeSegmentId) "
			+ "				FROM AdministrativeSegment aa "
			+ "				WHERE aa.incidentNumber = a.incidentNumber AND"
			+ "					aa.agency.agencyId = a.agency.agencyId AND "
			+ "					(?4 = null OR ?4 = 0 OR aa.owner.ownerId = ?4 ) "
			+ "				GROUP BY aa.incidentNumber ) AND "
			+ "		(?1 = null OR a.ori = ?1 ) AND "
			+ "		( year(ar.arrestDate) = ?2 AND ( ?3=0 OR month(ar.arrestDate) = ?3) ) ")
	List<Integer> findIdsByOriAndArrestDate(String ori, Integer year, Integer month, Integer ownerId);
	
	@Query("SELECT a.administrativeSegmentId from AdministrativeSegment a "
			+ "LEFT JOIN a.arresteeSegments ar "
			+ "WHERE a.administrativeSegmentId = ( SELECT max(administrativeSegmentId) "
			+ "				FROM AdministrativeSegment aa "
			+ "				WHERE aa.incidentNumber = a.incidentNumber AND"
			+ "					aa.agency.agencyId = a.agency.agencyId AND "
			+ "					(?5 = null OR ?5 = 0 OR aa.owner.ownerId = ?5) "
			+ "				GROUP BY aa.incidentNumber ) AND "
			+ "		(?1 = null OR ?1='' OR a.agency.stateCode = ?1 ) AND "
			+ "		(?2 = null OR a.agency.agencyId = ?2 ) AND "
			+ "		( year(ar.arrestDate) = ?3 AND ( ?4=null OR ?4=0 OR month(ar.arrestDate) = ?4) ) ")
	List<Integer> findIdsByStateAndAgencyAndArrestDate(String stateCode, Integer agencyId, Integer arrestYear,
			Integer arrestMonth, Integer ownerId);

	
	@Query("SELECT count(DISTINCT a.administrativeSegmentId) from AdministrativeSegment a "
			+ "WHERE (?1 = null OR a.ori in (?1)) AND "
			+ "		(?4 = null OR a.agency.agencyId in (?4)) AND "
			+ "		(?2 = null OR cast(concat(a.yearOfTape, '-', a.monthOfTape, '-01') as date) >= ?2 ) AND "
			+ "		(?3 = null OR cast(concat(a.yearOfTape, '-', a.monthOfTape, '-01') as date) <= ?3)  AND "
			+ "     ( a.submission = null )")
	long countByOriListAndSubmissionDateRange(List<String> oris, Date startDate, Date endDate, List<Integer> agencyIds);
	
	@Query("SELECT a.administrativeSegmentId from AdministrativeSegment a "
			+ "WHERE a.administrativeSegmentId = ( SELECT max(administrativeSegmentId) "
			+ "				FROM AdministrativeSegment aa "
			+ "				WHERE aa.incidentNumber = a.incidentNumber AND "
			+ "					aa.agency.agencyId = a.agency.agencyId "
			+ "				GROUP BY aa.incidentNumber ) AND "
			+ "		(?1 = null OR a.ori = ?1) AND "
			+ "		(year(a.incidentDate) = ?2 AND ( ?3 = 0 OR month(a.incidentDate) = ?3)) ")
	List<Integer> findIdsByOriAndIncidentDate(String ori, Integer year, Integer month);
	
	Integer deleteByOriAndYearOfTapeAndMonthOfTape(String ori, String yearOfTape, String monthOfTape);
	
	@Query("SELECT distinct a.administrativeSegmentId from AdministrativeSegment a "
			+ "LEFT JOIN a.offenseSegments ao "
			+ "WHERE a.administrativeSegmentId = ( SELECT max(administrativeSegmentId) "
			+ "				FROM AdministrativeSegment aa "
			+ "				WHERE aa.incidentNumber = a.incidentNumber AND"
			+ "					aa.agency.agencyId = a.agency.agencyId AND "
			+ "					(?5 = null OR ?5 = 0 OR aa.owner.ownerId = ?5) "
			+ "				GROUP BY aa.incidentNumber ) AND "
			+ "		ao.ucrOffenseCodeType.nibrsCode in (?6)  AND"
			+ " 	(?1 = null OR ?1 = '' OR a.agency.stateCode = ?1) AND "
			+ "		(?2 = null OR a.agency.agencyId = ?2) AND "
			+ "		(year(a.incidentDate) = ?3 AND "
			+ "		(?4 = 0 OR month(a.incidentDate) = ?4)) ")
	List<Integer> findIdsBySummaryReportRequestAndOffenses(String stateCode, 
			Integer agencyId, Integer year, Integer month, Integer ownerId, 
			List<String> offenseCodes);
	
	@Query("SELECT count(distinct a.administrativeSegmentId) from AdministrativeSegment a "
			+ "LEFT JOIN a.offenseSegments ao "
			+ "LEFT JOIN a.propertySegments ap "
			+ "WHERE a.administrativeSegmentId = ( SELECT max(administrativeSegmentId) "
			+ "				FROM AdministrativeSegment aa "
			+ "				WHERE aa.incidentNumber = a.incidentNumber AND"
			+ "					aa.agency.agencyId = a.agency.agencyId AND "
			+ "					(?5 = null OR ?5 = 0 OR aa.owner.ownerId = ?5) "
			+ "				GROUP BY aa.incidentNumber ) AND "
			+ "		ao.ucrOffenseCodeType.ucrOffenseCodeTypeId =200 AND"
			+ "		(ao.offenseAttemptedCompleted = 'A' OR ao.offenseAttemptedCompleted = 'C') AND"
			+ "		(ap.typePropertyLossEtcType.typePropertyLossEtcTypeId = 2 ) AND"
			+ " 	(?1 = null OR ?1 = '' OR a.agency.stateCode = ?1) AND "
			+ "		(?2 = null OR a.agency.agencyId = ?2) AND "
			+ "		(year(a.incidentDate) = ?3 AND "
			+ "		(?4 = 0 OR month(a.incidentDate) = ?4)) ")
	Integer countArsonBySummaryReportRequestAndOffenses(String stateCode, 
			Integer agencyId, Integer year, Integer month, Integer ownerId);
	
	@Query("SELECT distinct a.administrativeSegmentId from AdministrativeSegment a "
			+ "LEFT JOIN a.offenseSegments ao "
			+ "LEFT JOIN a.propertySegments ap "
			+ "WHERE a.administrativeSegmentId = ( SELECT max(administrativeSegmentId) "
			+ "				FROM AdministrativeSegment aa "
			+ "				WHERE aa.incidentNumber = a.incidentNumber AND"
			+ "					aa.agency.agencyId = a.agency.agencyId AND "
			+ "					(?5 = null OR ?5 = 0 OR aa.owner.ownerId = ?5) "
			+ "				GROUP BY aa.incidentNumber ) AND "
			+ "		ao.ucrOffenseCodeType.ucrOffenseCodeTypeId =200 AND"
			+ "		(ao.offenseAttemptedCompleted = 'A' OR ao.offenseAttemptedCompleted = 'C') AND"
			+ "		(ap.typePropertyLossEtcType.typePropertyLossEtcTypeId = 2 ) AND"
			+ " 	(?1 = null OR ?1 = '' OR a.agency.stateCode = ?1) AND "
			+ "		(?2 = null OR a.agency.agencyId = ?2) AND "
			+ "		(year(a.incidentDate) = ?3 AND "
			+ "		(?4 = 0 OR month(a.incidentDate) = ?4)) ")
	List<Integer> findArsonIdsBySummaryReportRequest(String stateCode, 
			Integer agencyId, Integer year, Integer month, Integer ownerId);
	
	@Query("SELECT distinct a.administrativeSegmentId from AdministrativeSegment a "
			+ "LEFT JOIN a.offenseSegments ao "
			+ "LEFT JOIN a.arresteeSegments ar "
			+ "WHERE a.administrativeSegmentId = ( SELECT max(administrativeSegmentId) "
			+ "				FROM AdministrativeSegment aa "
			+ "				WHERE aa.incidentNumber = a.incidentNumber AND"
			+ "					aa.agency.agencyId = a.agency.agencyId AND "
			+ "					(?5 = null OR ?5 = 0 OR aa.owner.ownerId = ?5) "
			+ "				GROUP BY aa.incidentNumber ) AND "
			+ "		ao.ucrOffenseCodeType.nibrsCode in (?6) AND "
			+ "		(ar.arrestDate is null OR ar.arrestDate = (select min (arrestDate) from a.arresteeSegments )) AND "
			+ " 	(?1 = null OR ?1 = '' OR a.agency.stateCode = ?1) AND "
			+ "		(?2 = null OR a.agency.agencyId = ?2) AND "
			+ "		((year(a.exceptionalClearanceDate) = ?3 AND ( ?4 = 0 OR month(a.exceptionalClearanceDate) = ?4)) "
			+ "			OR ( ar.arrestDate is not null and year(ar.arrestDate.arrestDate) = ?3 AND ( ?4 = 0 OR month(ar.arrestDate) = ?4 ))) ")
	List<Integer> findIdsByStateCodeAndOriAndClearanceDateAndOffenses(String stateCode, Integer agencyId,
			Integer incidentYear, Integer incidentMonth, Integer ownerId, List<String> offenseCodes);
	
	@Query("SELECT distinct a.administrativeSegmentId from AdministrativeSegment a "
			+ "LEFT JOIN a.offenseSegments ao "
			+ "LEFT JOIN a.arresteeSegments ar "
			+ "LEFT JOIN a.propertySegments ap "
			+ "WHERE a.administrativeSegmentId = ( SELECT max(administrativeSegmentId) "
			+ "				FROM AdministrativeSegment aa "
			+ "				WHERE aa.incidentNumber = a.incidentNumber AND"
			+ "					aa.agency.agencyId = a.agency.agencyId AND "
			+ "					(?5 = null OR ?5 = 0 OR aa.owner.ownerId = ?5) "
			+ "				GROUP BY aa.incidentNumber ) AND "
			+ "		ao.ucrOffenseCodeType.ucrOffenseCodeTypeId =200 AND "
			+ "		(ao.offenseAttemptedCompleted = 'A' OR ao.offenseAttemptedCompleted = 'C') AND"
			+ "		(ap.typePropertyLossEtcType.typePropertyLossEtcTypeId = 2 ) AND"
			+ "		(ar.arrestDate = (select min (arrestDate) from a.arresteeSegments )) AND "
			+ " 	(?1 = null OR ?1 = '' OR a.agency.stateCode = ?1) AND "
			+ "		(?2 = null OR a.agency.agencyId = ?2) AND "
			+ "		((year(a.exceptionalClearanceDate) = ?3 AND ( ?4 = 0 OR month(a.exceptionalClearanceDate) = ?4)) "
			+ "			OR ( year(ar.arrestDate) = ?3 AND ( ?4 = 0 OR month(ar.arrestDate) = ?4 ))) ")
	List<Integer> findArsonIdsByStateCodeAndOriAndClearanceDate(String stateCode, Integer agencyId,
			Integer incidentYear, Integer incidentMonth, Integer ownerId);
	
	@Query("SELECT count(distinct a.administrativeSegmentId) from AdministrativeSegment a "
			+ "WHERE a.administrativeSegmentId in ?1 AND "
			+ "		((a.clearedExceptionallyType.clearedExceptionallyTypeId IN (1, 2, 3, 4, 5) AND "
			+ "			EXISTS (SELECT o from a.offenderSegments o WHERE (o.ageOfOffenderMin+o.ageOfOffenderMax)/2 < 18) AND "
			+ "			NOT EXISTS(SELECT o from a.offenderSegments o WHERE (o.ageOfOffenderMin+o.ageOfOffenderMax)/2 >= 18)) OR "
			+ "		 ( EXISTS (SELECT ar from a.arresteeSegments ar WHERE (ar.ageOfArresteeMin+ar.ageOfArresteeMax)/2 < 18) AND"
			+ "		 NOT EXISTS(SELECT ar from a.arresteeSegments ar WHERE (ar.ageOfArresteeMin+ar.ageOfArresteeMax)/2 >= 18)))")
	Integer countClearedArsonInvolvingOnlyJuvenile(List<Integer> administrativeIds);
	
	@Query("SELECT a.administrativeSegmentId from AdministrativeSegment a "
			+ "WHERE (?1 = null OR ?1 = 0 OR a.owner.ownerId = ?1) AND "
			+ "     ( ?2 = null OR a.agency.stateCode = ?2 ) AND "
			+ "		(?3 = null OR ?3 = 0 OR a.agency.agencyId = ?3 ) ")
	List<Integer> findIdsByOwnerStateAndAgency(Integer ownerId, String stateCode, Integer agencyId);
	
	@Query("SELECT count(a.administrativeSegmentId) from AdministrativeSegment a "
			+ "LEFT JOIN a.owner ao "
			+ "LEFT JOIN a.agency ag "
			+ "WHERE (?1 = null OR ?1 = 0 OR ao.ownerId = ?1) AND "
			+ "		(?3 = null OR ?3 = 0 OR ag.agencyId = ?3 OR ?2 = null OR ag.stateCode = ?2) ")
	Integer countIdsByOwnerStateAndAgency(Integer ownerId, String stateCode, Integer agencyId);

	@Query("SELECT DISTINCT year(a.incidentDate) as incidentYear from AdministrativeSegment a "
			+ "WHERE a.agency.stateCode = ?1 AND (?2 = null OR ?2=0 OR a.owner.ownerId = ?2)"
			+ "ORDER BY incidentYear ")
	List<Integer> findDistinctYearsByStateCode(String stateCode, Integer integer);

	@Query("SELECT DISTINCT month(a.incidentDate) as incidentMonth from AdministrativeSegment a "
			+ "WHERE a.agency.stateCode = ?1 AND year(a.incidentDate) = ?2 AND (?3 = null OR ?3 = 0 OR a.owner.ownerId = ?3)"
			+ "ORDER BY incidentMonth ")
	List<Integer> findDistinctMonthsByStateCode(String stateCode, Integer year, Integer integer);

	@Query("SELECT a.administrativeSegmentId from AdministrativeSegment a "
			+ "WHERE a.administrativeSegmentId = ( SELECT max(administrativeSegmentId) "
			+ "				FROM AdministrativeSegment aa "
			+ "				WHERE aa.incidentNumber = a.incidentNumber AND "
			+ "					aa.agency.agencyId = a.agency.agencyId AND "
			+ "					(?5 = null OR ?5 = 0 OR aa.owner.ownerId = ?5) "
			+ "				GROUP BY aa.incidentNumber ) AND "
			+ "		a.cargoTheftIndicatorType.cargoTheftIndicatorTypeId = 1 AND "
			+ "		(?1 = null OR ?1='' OR a.agency.stateCode = ?1) AND "
			+ "		(?2 = null OR a.agency.agencyId = ?2) AND "
			+ "		(year(a.incidentDate) = ?3 AND "
			+ "		( ?4 = 0 OR month(a.incidentDate) = ?4)) ")
	List<Integer> findCargoTheftIdsByStateAndAgencyAndIncidentDate(String stateCode, Integer agencyId,
			Integer incidentYear, Integer incidentMonth, Integer ownerId);

}
