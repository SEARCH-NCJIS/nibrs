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

import javax.transaction.Transactional;

import org.search.nibrs.stagingdata.model.segment.ArrestReportSegment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

@Transactional
public interface ArrestReportSegmentRepository extends JpaRepository<ArrestReportSegment, Integer>{
	long deleteByArrestTransactionNumber(String arrestTransactionNumber);
	
	@EntityGraph(value="allArrestReportSegmentJoins", type=EntityGraphType.LOAD)
	List<ArrestReportSegment> findByArrestTransactionNumber(String arrestTransactionNumber);
	
	@Query("SELECT count(*) > 0 from ArrestReportSegment a "
			+ "LEFT JOIN a.segmentActionType s "
			+ "WHERE a.arrestReportSegmentId = "
			+ "			(SELECT max(arrestReportSegmentId) FROM ArrestReportSegment "
			+ "				where arrestTransactionNumber = ?1 AND ori = ?2 ) "
			+ "		AND s.nibrsCode != 'D' ")
	boolean existsByArrestTransactionNumberAndOri(String arrestTransactionNumber, String ori);
	
	@Query("SELECT count(*) > 0 from ArrestReportSegment a "
			+ "LEFT JOIN a.segmentActionType s "
			+ "WHERE a.arrestReportSegmentId = "
			+ "			(SELECT max(arrestReportSegmentId) FROM ArrestReportSegment "
			+ "				where arrestTransactionNumber = ?1 AND ori = ?2 ) "
			+ "		and cast(concat(a.yearOfTape, '-', a.monthOfTape, '-01') as date) > ?3")
	boolean existsByArrestTransactionNumberAndOriAndSubmissionDate(String arrestTransactionNumber, String ori, Date submissionDate);
	
	@Query("SELECT distinct a.agency.agencyId from ArrestReportSegment a "
			+ "WHERE a.owner.ownerId = ?1 ")
	Set<Integer> findAgencyIdsByOwnerId(Integer ownerId);

	@EntityGraph(value="allArrestReportSegmentJoins", type=EntityGraphType.LOAD)
	ArrestReportSegment findByArrestReportSegmentId(Integer arrestReportSegmentId);

	@Query("SELECT max(a.arrestReportSegmentId) from ArrestReportSegment a "
			+ "WHERE (?1 = null OR a.ori = ?1) AND "
			+ "		(year(a.arrestDate) = ?2 AND ( ?3 = 0 OR month(a.arrestDate) = ?3)) "
			+ "GROUP BY a.arrestTransactionNumber ")
	List<Integer> findIdsByOriAndArrestDate(String ori, Integer year, Integer month);

	@Query("SELECT DISTINCT a.arrestReportSegmentId from ArrestReportSegment a "
			+ "INNER JOIN a.ucrOffenseCodeType u "
			+ "WHERE u.nibrsCode != '90I' AND "
			+ "		(?1 = null OR a.ori in (?1)) AND "
			+ "		(?4 = null OR a.agency.agencyId in (?4)) AND "
			+ "		(?2 = null OR cast(concat(a.yearOfTape, '-', a.monthOfTape, '-01') as date) >= ?2) AND "
			+ "		(?3 = null OR cast(concat(a.yearOfTape, '-', a.monthOfTape, '-01') as date) <= ?3) AND "
			+ "     ( a.submission = null )"
			+ "ORDER BY a.arrestReportSegmentId asc ")
	List<Integer> findIdsByOriListAndSubmissionDateRange(List<String> ori, Date startDate, 
			Date endDate, List<Integer> agencyIds);
	
	@Query("SELECT count(DISTINCT a.arrestReportSegmentId) from ArrestReportSegment a "
			+ "INNER JOIN a.ucrOffenseCodeType u "
			+ "WHERE u.nibrsCode != '90I' AND "
			+ "		(?1 = null OR a.ori in (?1)) AND "
			+ "		(?4 = null OR a.agency.agencyId in (?4)) AND "
			+ "		(?2 = null OR cast(concat(a.yearOfTape, '-', a.monthOfTape, '-01') as date) >= ?2) AND "
			+ "		(?3 = null OR cast(concat(a.yearOfTape, '-', a.monthOfTape, '-01') as date) <= ?3) AND "
			+ "     ( a.submission = null )"
			)
	long countByOriListAndSubmissionDateRange(List<String> oris, Date startDate, 
			Date endDate, List<Integer> agencyIds);
	
	@EntityGraph(value="allArrestReportSegmentJoins", type=EntityGraphType.LOAD)
	List<ArrestReportSegment> findAllById(Iterable<Integer> ids);
}
