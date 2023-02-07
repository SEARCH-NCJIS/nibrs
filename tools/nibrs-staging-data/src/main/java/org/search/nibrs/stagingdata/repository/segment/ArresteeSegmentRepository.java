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

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import org.search.nibrs.stagingdata.model.segment.ArresteeSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

@Transactional
public interface ArresteeSegmentRepository extends JpaRepository<ArresteeSegment, Integer>{
	
	@Query("SELECT ar from ArresteeSegment ar "
		+ "WHERE ar.administrativeSegment.administrativeSegmentId in (?3) AND "
		+ "	   ( year(ar.arrestDate) = ?1 AND ( ?2=0 OR month(ar.arrestDate) = ?2 )) ")
	List<ArresteeSegment> findByAdministrativeSegmentIdsAndArrestDate(Integer arrestYear, Integer arrestMonth, List<Integer> ids); 
}
