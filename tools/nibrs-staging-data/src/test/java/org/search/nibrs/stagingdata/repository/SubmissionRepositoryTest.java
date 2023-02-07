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
package org.search.nibrs.stagingdata.repository;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.search.nibrs.stagingdata.model.Submission;
import org.search.nibrs.stagingdata.model.Violation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SubmissionRepositoryTest {
	
	@Autowired
	public SubmissionRepository submissionRepository; 

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		List<Submission> submissionsBefore = submissionRepository.findAll(); 
		assertThat(submissionsBefore.size(), is(0));
		
		Submission submission = new Submission(); 
		submission.setAcceptedIndicator(true);
		submission.setIncidentIdentifier("incident1");
		submission.setRequestFilePath("requestFilePath1");
		submission.setResponseFilePath("responseFilePath1");
		submission.setResponseTimestamp(LocalDateTime.now());
		submission.setSubmissionTimestamp(LocalDateTime.now());
		submission.setMessageIdentifier(1);
		submission.setNibrsReportCategoryCode("GROUP A INCIDENT REPORT");
		
		Submission submissionSaved = submissionRepository.save(submission);
		List<Submission> submissionsAfterInsert1 = submissionRepository.findAll(); 
		List<Integer> submissionIdsAfterInsert1 = submissionRepository.findIdsByIncidentIdentifier("incident1"); 
		assertThat(submissionIdsAfterInsert1.size(), is(1));
		assertThat(submissionsAfterInsert1.size(), is(1));
		
		assertThat(submissionSaved.getAcceptedIndicator(), equalTo(submission.getAcceptedIndicator()));
		assertThat(submissionSaved.getIncidentIdentifier(), equalTo(submission.getIncidentIdentifier()));
		assertThat(submissionSaved.getRequestFilePath(), equalTo(submission.getRequestFilePath()));
		assertThat(submissionSaved.getResponseFilePath(), equalTo(submission.getResponseFilePath()));
		assertThat(submissionSaved.getResponseTimestamp(), equalTo(submission.getResponseTimestamp()));
		assertNotNull(submissionSaved.getSubmissionTimestamp());
		
		Submission fullSubmissionSaved = submissionRepository.findBySubmissionId(submissionSaved.getSubmissionId());
		assertThat(fullSubmissionSaved.getViolations().size(), equalTo(0));
		
		Violation violation1 = new Violation();
		violation1.setViolationCode("451");
		violation1.setViolationLevel("E");
		violation1.setViolationDescription("VICTIM NUMBER ALREADY EXISTS");
		violation1.setViolationTimestamp(LocalDateTime.now());
		violation1.setSubmission(submissionSaved);
		
		Violation violation2 = new Violation();
		violation2.setViolationCode("355");
		violation2.setViolationLevel("E");
		violation2.setViolationDescription("PROPERTY LOSS CODE (14) MUST=5 (RECOVERED) FOR DATA TO BE ENTERED");
		violation2.setViolationTimestamp(LocalDateTime.now());
		violation2.setSubmission(submissionSaved);
		
		Set<Violation> violations = new HashSet<>(); 
		violations.add(violation1); 
		violations.add(violation2); 
		submissionSaved.setViolations(violations);
		
		submissionRepository.saveAndFlush(submissionSaved);
		
		Submission fullSubmissionSaved2 = submissionRepository.findBySubmissionId(submissionSaved.getSubmissionId());
		assertThat(fullSubmissionSaved2.getViolations().size(), equalTo(2));
		
		for (Violation violation : fullSubmissionSaved2.getViolations()){
			if (violation.getViolationCode().equals(violation1.getViolationCode())){
				assertThat(violation.getViolationLevel(), equalTo(violation1.getViolationLevel())); 
				assertThat(violation.getViolationTimestamp(), equalTo(violation1.getViolationTimestamp())); 
			}
			else{
				assertThat(violation.getViolationLevel(), equalTo(violation2.getViolationLevel())); 
				assertThat(violation.getViolationTimestamp(), equalTo(violation2.getViolationTimestamp())); 
			}
		}
		
	}

}
