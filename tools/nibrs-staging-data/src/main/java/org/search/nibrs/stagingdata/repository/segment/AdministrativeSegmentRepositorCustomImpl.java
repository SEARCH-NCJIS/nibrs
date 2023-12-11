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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;
import org.search.nibrs.stagingdata.model.ArresteeSegmentWasArmedWith;
import org.search.nibrs.stagingdata.model.PropertyType;
import org.search.nibrs.stagingdata.model.Submission;
import org.search.nibrs.stagingdata.model.SuspectedDrugType;
import org.search.nibrs.stagingdata.model.TypeOfWeaponForceInvolved;
import org.search.nibrs.stagingdata.model.UcrOffenseCodeType;
import org.search.nibrs.stagingdata.model.VictimOffenderAssociation;
import org.search.nibrs.stagingdata.model.search.IncidentPointer;
import org.search.nibrs.stagingdata.model.search.IncidentSearchRequest;
import org.search.nibrs.stagingdata.model.segment.AdministrativeSegment;
import org.search.nibrs.stagingdata.model.segment.ArresteeSegment;
import org.search.nibrs.stagingdata.model.segment.OffenderSegment;
import org.search.nibrs.stagingdata.model.segment.OffenseSegment;
import org.search.nibrs.stagingdata.model.segment.PropertySegment;
import org.search.nibrs.stagingdata.model.segment.VictimSegment;
import org.springframework.stereotype.Repository;

@Repository
public class AdministrativeSegmentRepositorCustomImpl implements AdministrativeSegmentRepositoryCustom{
	@PersistenceContext
    private EntityManager entityManager;

	@Override
	public List<IncidentPointer> findAllByCriteria(IncidentSearchRequest incidentSearchRequest) {
		
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<IncidentPointer> query = criteriaBuilder.createQuery(IncidentPointer.class);
        Root<AdministrativeSegment> root = query.from(AdministrativeSegment.class);
        
        Subquery<Integer> offenseCodeTypeIdSubQuery = query.subquery(Integer.class);
        Root<OffenseSegment> offenseSubRoot = offenseCodeTypeIdSubQuery.from(OffenseSegment.class);
        offenseCodeTypeIdSubQuery.select(criteriaBuilder.min(offenseSubRoot.get("ucrOffenseCodeType").get("ucrOffenseCodeTypeId")));
        offenseCodeTypeIdSubQuery.where(criteriaBuilder.equal(
        		offenseSubRoot.get("administrativeSegment").get("administrativeSegmentId"), 
        		root.get("administrativeSegmentId")));
        
        Join<AdministrativeSegment, OffenseSegment> offenseSegmentJoin = root.join("offenseSegments", JoinType.LEFT);
        offenseSegmentJoin.on(criteriaBuilder.equal(offenseSegmentJoin.get("ucrOffenseCodeType").get("ucrOffenseCodeTypeId"), offenseCodeTypeIdSubQuery));
        
        Join<OffenseSegment, UcrOffenseCodeType> ucrOffenseCodeTypeJoin = offenseSegmentJoin.join("ucrOffenseCodeType", JoinType.LEFT);
        
        Join<AdministrativeSegment, Submission> submissionJoin = root.join("submission", JoinType.LEFT);
        
		query.multiselect(root.get("administrativeSegmentId"),
			criteriaBuilder.literal("GroupA"),root.get("incidentNumber"), 
			root.get("agency").get("agencyId"), root.get("agency").get("agencyName"), root.get("incidentDate"), 
			offenseSegmentJoin.get("ucrOffenseCodeType").get("ucrOffenseCodeTypeId"),
			ucrOffenseCodeTypeJoin.get("nibrsCode"),
			root.get("monthOfTape"), root.get("yearOfTape"), 
			root.get("reportTimestamp"), 
			submissionJoin.get("acceptedIndicator"), 
			submissionJoin.get("faultCode"),
			submissionJoin.get("submissionTimestamp")
		);
		

        List<Predicate> queryPredicates = getAdministrativeSegmentPredicates(incidentSearchRequest, root, criteriaBuilder, submissionJoin);
        
        query.where(criteriaBuilder.and(
        		queryPredicates.toArray( new Predicate[queryPredicates.size()])));
        
		return entityManager.createQuery(query)
	            .getResultList();
	}

	@Override
	public long countAllByCriteria(IncidentSearchRequest incidentSearchRequest) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<AdministrativeSegment> root = query.from(AdministrativeSegment.class);
        Join<AdministrativeSegment, Submission> submissionJoin = root.join("submission", JoinType.LEFT);

        List<Predicate> predicates = getAdministrativeSegmentPredicates(incidentSearchRequest, root, criteriaBuilder, submissionJoin);
        
		query.select(criteriaBuilder.count(root.get("administrativeSegmentId")))
			.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
		return entityManager.createQuery(query).getSingleResult();
	}

	private List<Predicate> getAdministrativeSegmentPredicates(IncidentSearchRequest incidentSearchRequest,
			Root<AdministrativeSegment> root, CriteriaBuilder criteriaBuilder, Join<AdministrativeSegment, Submission> submissionJoin) {
		List<Predicate> predicates = new ArrayList<>();
        if(incidentSearchRequest != null) {
        	
            if (incidentSearchRequest.getFbiSubmissionStatus() != null) {
            	switch (incidentSearchRequest.getFbiSubmissionStatus()){
            	case NOT_SUBMITTED: 
                	predicates.add(criteriaBuilder.and(criteriaBuilder.isNull(root.get("submission")))); 
            		break; 
            	case ACCEPTED: 
                	predicates.add(criteriaBuilder.and(criteriaBuilder.isTrue(submissionJoin.get("acceptedIndicator")))); 
            		break; 
            	case REJECTED: 
                	predicates.add(criteriaBuilder.and(criteriaBuilder.isFalse(submissionJoin.get("acceptedIndicator")))); 
            		break; 
            	}
            }
            
        	if (incidentSearchRequest.getFbiSubmissionTimestampStart() != null) {
        		predicates.add(criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(submissionJoin.get("submissionTimestamp"), incidentSearchRequest.getFbiSubmissionTimestampStart())));
        	}
        	if (incidentSearchRequest.getReportTimestampEndDate() != null) {
        		predicates.add(criteriaBuilder.and(criteriaBuilder.lessThan(submissionJoin.get("submissionTimestamp"), incidentSearchRequest.getReportTimestampEndDate())));
        	}
        	
        	if (StringUtils.isNotBlank(incidentSearchRequest.getStateCode())) {
        		predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("agency").get("stateCode"), incidentSearchRequest.getStateCode())));
        	}
        	
        	if (incidentSearchRequest.getAgencyIds() != null && incidentSearchRequest.getAgencyIds().size() > 0) {
        		predicates.add(criteriaBuilder.and(root.get("agency").get("agencyId").in(incidentSearchRequest.getAgencyIds())));
        	}
        	
        	if (incidentSearchRequest.getOwnerId() != null) {
        		predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("owner").get("ownerId"), incidentSearchRequest.getOwnerId())));
        	}
        	
        	if (incidentSearchRequest.getIncidentIdentifier() != null) {
        		predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("incidentNumber"), incidentSearchRequest.getIncidentIdentifier())));
        	}
        	
        	if (incidentSearchRequest.getIncidentDateRangeStartDate() != null) {
        		predicates.add(criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(root.get("incidentDate"), incidentSearchRequest.getIncidentDateRangeStartDate())));
        	}
        	if (incidentSearchRequest.getIncidentDateRangeEndDate() != null) {
        		predicates.add(criteriaBuilder.and(criteriaBuilder.lessThanOrEqualTo(root.get("incidentDate"), incidentSearchRequest.getIncidentDateRangeEndDate())));
        	}
        	
        	if (incidentSearchRequest.getReportTimestampStartDate() != null) {
        		predicates.add(criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(root.get("reportTimestamp"), incidentSearchRequest.getReportTimestampStartDate())));
        	}
        	if (incidentSearchRequest.getReportTimestampEndDate() != null) {
        		predicates.add(criteriaBuilder.and(criteriaBuilder.lessThan(root.get("reportTimestamp"), incidentSearchRequest.getReportTimestampEndDate())));
        	}
        	
        	if (incidentSearchRequest.getUcrOffenseCodeTypeId() != null) {
        		Join<AdministrativeSegment, OffenseSegment> joinOptions = root.join("offenseSegments", JoinType.LEFT);
        		predicates.add(criteriaBuilder.and(criteriaBuilder.equal(joinOptions.get("ucrOffenseCodeType").get("ucrOffenseCodeTypeId"), 
        				incidentSearchRequest.getUcrOffenseCodeTypeId())));
        	}
        	
        	if (incidentSearchRequest.getSubmissionMonth() != null) {
        		predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("monthOfTape"), incidentSearchRequest.getSubmissionMonth())));
        	}
        	
         	if (incidentSearchRequest.getSubmissionYear() != null) {
        		predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("yearOfTape"), incidentSearchRequest.getSubmissionYear())));
        	}
        	
         	if (incidentSearchRequest.getSubmissionStartDate() != null ) {
         		predicates.add(criteriaBuilder.and(criteriaBuilder.greaterThanOrEqualTo(
         				criteriaBuilder.function("concat", String.class, root.get("yearOfTape"), criteriaBuilder.literal("-"), root.get("monthOfTape"),  criteriaBuilder.literal("-01")), 
         				incidentSearchRequest.getSubmissionStartDate().toString())));
         	}
         	if (incidentSearchRequest.getSubmissionEndDate() != null ) {
         		predicates.add(criteriaBuilder.and(criteriaBuilder.lessThanOrEqualTo(
         				criteriaBuilder.function("concat", String.class, root.get("yearOfTape"), criteriaBuilder.literal("-"), root.get("monthOfTape"),  criteriaBuilder.literal("-01")), 
         				incidentSearchRequest.getSubmissionEndDate().toString())));
         	}
        }
		return predicates;
	}

	public int updateSubmissionId(Integer administrativeSegmentId, Integer submissionId) {
	      Query query = entityManager.createQuery("UPDATE AdministrativeSegment a SET a.submission.submissionId = :submissionId "
	              + "WHERE a.administrativeSegmentId = :administrativeSegmentId");
	      query.setParameter("administrativeSegmentId", administrativeSegmentId);
	      query.setParameter("submissionId", submissionId);
	      int rowsUpdated = query.executeUpdate();
	      return rowsUpdated;
	  }

	@Override
	public Integer deleteByIds(List<Integer> administrativeSegmentIds) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		deletePropertySegments(administrativeSegmentIds, criteriaBuilder);
		deleteOffenseSegments(administrativeSegmentIds, criteriaBuilder);
		deleteOffenderSegments(administrativeSegmentIds, criteriaBuilder);
		deleteVictimSegments(administrativeSegmentIds, criteriaBuilder);
		deleteArresteeSegments(administrativeSegmentIds, criteriaBuilder);
		return deleteAdministrativeSegments(administrativeSegmentIds, criteriaBuilder);
	}

	private Integer deleteAdministrativeSegments(List<Integer> administrativeSegmentIds,
			CriteriaBuilder criteriaBuilder) {
		CriteriaDelete<AdministrativeSegment> administrativeSegmentDelete = criteriaBuilder.createCriteriaDelete(AdministrativeSegment.class);
		Root<AdministrativeSegment> administrativeSegmentRoot = administrativeSegmentDelete.from(AdministrativeSegment.class);
		administrativeSegmentDelete.where(administrativeSegmentRoot.get("administrativeSegmentId").in(administrativeSegmentIds));
		
		return entityManager.createQuery(administrativeSegmentDelete).executeUpdate();
	}

	private Integer deleteArresteeSegments(List<Integer> administrativeSegmentIds, CriteriaBuilder criteriaBuilder) {
		CriteriaDelete<ArresteeSegmentWasArmedWith> arresteeSegmentWasArmedWithDelete = criteriaBuilder.createCriteriaDelete(ArresteeSegmentWasArmedWith.class);
		Root<ArresteeSegmentWasArmedWith> arresteeSegmentWasArmedWithRoot = arresteeSegmentWasArmedWithDelete.from(ArresteeSegmentWasArmedWith.class);
		
		Subquery<Integer> victimOffenderAssociationSubQuery = createArresteeSegmentSubquery(administrativeSegmentIds, criteriaBuilder,
				arresteeSegmentWasArmedWithDelete);
		
		arresteeSegmentWasArmedWithDelete.where(arresteeSegmentWasArmedWithRoot.get("arresteeSegment").get("arresteeSegmentId").in(victimOffenderAssociationSubQuery));
		entityManager.createQuery(arresteeSegmentWasArmedWithDelete).executeUpdate();

		CriteriaDelete<ArresteeSegment> arresteeSegmentDelete = criteriaBuilder.createCriteriaDelete(ArresteeSegment.class);
		Root<ArresteeSegment> arresteeSegmentRoot = arresteeSegmentDelete.from(ArresteeSegment.class);
		arresteeSegmentDelete.where(arresteeSegmentRoot.get("administrativeSegment").get("administrativeSegmentId").in(administrativeSegmentIds));
		
		return entityManager.createQuery(arresteeSegmentDelete).executeUpdate();
	}

	private Subquery<Integer> createArresteeSegmentSubquery(List<Integer> administrativeSegmentIds,
			CriteriaBuilder criteriaBuilder,
			CriteriaDelete<ArresteeSegmentWasArmedWith> criteriaDelete) {
		Subquery<Integer> arresteeSegmentSubQuery = criteriaDelete.subquery(Integer.class);
        Root<ArresteeSegment> arresteeSegmentSubQueryRoot = arresteeSegmentSubQuery.from(ArresteeSegment.class);
        arresteeSegmentSubQuery.select(arresteeSegmentSubQueryRoot.get("arresteeSegmentId"));
        
        arresteeSegmentSubQuery.where(arresteeSegmentSubQueryRoot.get("administrativeSegment").get("administrativeSegmentId").in(administrativeSegmentIds));
		return arresteeSegmentSubQuery;
	}

	private Integer deleteVictimSegments(List<Integer> administrativeSegmentIds, CriteriaBuilder criteriaBuilder) {
		CriteriaDelete<VictimSegment> victimSegmentDelete = criteriaBuilder.createCriteriaDelete(VictimSegment.class);
		Root<VictimSegment> victimSegmentRoot = victimSegmentDelete.from(VictimSegment.class);
		victimSegmentDelete.where(victimSegmentRoot.get("administrativeSegment").get("administrativeSegmentId").in(administrativeSegmentIds));
		
		
		return entityManager.createQuery(victimSegmentDelete).executeUpdate();
	}

	private Integer deleteOffenderSegments(List<Integer> administrativeSegmentIds, CriteriaBuilder criteriaBuilder) {
		CriteriaDelete<VictimOffenderAssociation> victimOffenderAssociationDelete = criteriaBuilder.createCriteriaDelete(VictimOffenderAssociation.class);
		Root<VictimOffenderAssociation> victimOffenderAssociationRoot = victimOffenderAssociationDelete.from(VictimOffenderAssociation.class);
		
		Subquery<Integer> victimOffenderAssociationSubQuery = createOffenderSegmentSubquery(administrativeSegmentIds, criteriaBuilder,
				victimOffenderAssociationDelete);
		
		victimOffenderAssociationDelete.where(victimOffenderAssociationRoot.get("offenderSegment").get("offenderSegmentId").in(victimOffenderAssociationSubQuery));
		entityManager.createQuery(victimOffenderAssociationDelete).executeUpdate();
		
		CriteriaDelete<OffenderSegment> offenderSegmentDelete = criteriaBuilder.createCriteriaDelete(OffenderSegment.class);
		Root<OffenderSegment> offenderSegmentRoot = offenderSegmentDelete.from(OffenderSegment.class);
		offenderSegmentDelete.where(offenderSegmentRoot.get("administrativeSegment").get("administrativeSegmentId").in(administrativeSegmentIds));
		
		
		return entityManager.createQuery(offenderSegmentDelete).executeUpdate();
	}

	private Subquery<Integer> createOffenderSegmentSubquery(List<Integer> administrativeSegmentIds,
			CriteriaBuilder criteriaBuilder,
			CriteriaDelete<VictimOffenderAssociation> criteriaDelete) {
		Subquery<Integer> offenderSegmentSubQuery = criteriaDelete.subquery(Integer.class);
        Root<OffenderSegment> offenderSegmentSubQueryRoot = offenderSegmentSubQuery.from(OffenderSegment.class);
        offenderSegmentSubQuery.select(offenderSegmentSubQueryRoot.get("offenderSegmentId"));
        
        offenderSegmentSubQuery.where(offenderSegmentSubQueryRoot.get("administrativeSegment").get("administrativeSegmentId").in(administrativeSegmentIds));
		return offenderSegmentSubQuery;
	}

	private Integer deletePropertySegments(List<Integer> administrativeSegmentIds, CriteriaBuilder criteriaBuilder) {
		CriteriaDelete<PropertyType> propertyTypeDelete = criteriaBuilder.createCriteriaDelete(PropertyType.class);
		Root<PropertyType> propertyTypeRoot = propertyTypeDelete.from(PropertyType.class);
		
        Subquery<Integer> propertyTypeSubQuery = createPropertySegmentSubquery(administrativeSegmentIds, criteriaBuilder,
        		propertyTypeDelete);
    	
        propertyTypeDelete.where(propertyTypeRoot.get("propertySegment").get("propertySegmentId").in(propertyTypeSubQuery));
        entityManager.createQuery(propertyTypeDelete).executeUpdate();
        
        CriteriaDelete<SuspectedDrugType> suspectedDrugTypeDelete = criteriaBuilder.createCriteriaDelete(SuspectedDrugType.class);
        Root<SuspectedDrugType> suspectedDrugTypeRoot = suspectedDrugTypeDelete.from(SuspectedDrugType.class);
        
        Subquery<Integer> suspectedDrugTypeSubQuery = createPropertySegmentSubquery(administrativeSegmentIds, criteriaBuilder,
        		suspectedDrugTypeDelete);
        
        suspectedDrugTypeDelete.where(suspectedDrugTypeRoot.get("propertySegment").get("propertySegmentId").in(suspectedDrugTypeSubQuery));
        entityManager.createQuery(suspectedDrugTypeDelete).executeUpdate();
        
		CriteriaDelete<PropertySegment> propertySegmentDelete = criteriaBuilder.createCriteriaDelete(PropertySegment.class);
		Root<PropertySegment> propertySegmentRoot = propertySegmentDelete.from(PropertySegment.class);
		propertySegmentDelete.where(propertySegmentRoot.get("administrativeSegment").get("administrativeSegmentId").in(administrativeSegmentIds));
        
        
		return entityManager.createQuery(propertySegmentDelete).executeUpdate();
	}

	private Integer deleteOffenseSegments(List<Integer> administrativeSegmentIds, CriteriaBuilder criteriaBuilder) {
		CriteriaDelete<TypeOfWeaponForceInvolved> typeOfWeaponForceInvolvedDelete = criteriaBuilder.createCriteriaDelete(TypeOfWeaponForceInvolved.class);
		Root<TypeOfWeaponForceInvolved> typeOfWeaponForceInvolvedRoot = typeOfWeaponForceInvolvedDelete.from(TypeOfWeaponForceInvolved.class);
		
        Subquery<Integer> typeOfWeaponForceInvolvedSubQuery = createOffenseSegmentSubquery(administrativeSegmentIds, criteriaBuilder,
        		typeOfWeaponForceInvolvedDelete);
    	
        typeOfWeaponForceInvolvedDelete.where(typeOfWeaponForceInvolvedRoot.get("offenseSegment").get("offenseSegmentId").in(typeOfWeaponForceInvolvedSubQuery));
        entityManager.createQuery(typeOfWeaponForceInvolvedDelete).executeUpdate();
        
		CriteriaDelete<OffenseSegment> offenseSegmentDelete = criteriaBuilder.createCriteriaDelete(OffenseSegment.class);
		Root<OffenseSegment> offenseSegmentRoot = offenseSegmentDelete.from(OffenseSegment.class);
		offenseSegmentDelete.where(offenseSegmentRoot.get("administrativeSegment").get("administrativeSegmentId").in(administrativeSegmentIds));
		
		
		return entityManager.createQuery(offenseSegmentDelete).executeUpdate();
	}
	
	private Subquery<Integer> createOffenseSegmentSubquery(List<Integer> administrativeSegmentIds,
			CriteriaBuilder criteriaBuilder,
			CriteriaDelete<?> criteriaDelete) {
		Subquery<Integer> offenseSegmentSubQuery = criteriaDelete.subquery(Integer.class);
        Root<OffenseSegment> offenseSegmentSubQueryRoot = offenseSegmentSubQuery.from(OffenseSegment.class);
        offenseSegmentSubQuery.select(offenseSegmentSubQueryRoot.get("offenseSegmentId"));
        
        offenseSegmentSubQuery.where(offenseSegmentSubQueryRoot.get("administrativeSegment").get("administrativeSegmentId").in(administrativeSegmentIds));
		return offenseSegmentSubQuery;
	}

	private Subquery<Integer> createPropertySegmentSubquery(List<Integer> administrativeSegmentIds,
			CriteriaBuilder criteriaBuilder, CriteriaDelete<?> criteriaDelete) {
		Subquery<Integer> propertySegmentSubQuery = criteriaDelete.subquery(Integer.class);
        Root<PropertySegment> propertySegmentSubQueryRoot = propertySegmentSubQuery.from(PropertySegment.class);
        propertySegmentSubQuery.select(propertySegmentSubQueryRoot.get("propertySegmentId"));
        
        propertySegmentSubQuery.where(propertySegmentSubQueryRoot.get("administrativeSegment").get("administrativeSegmentId").in(administrativeSegmentIds));
		return propertySegmentSubQuery;
	}

}
