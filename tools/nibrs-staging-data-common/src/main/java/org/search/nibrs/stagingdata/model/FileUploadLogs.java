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
package org.search.nibrs.stagingdata.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@JsonIdentityInfo(
	generator = ObjectIdGenerators.PropertyGenerator.class, 
	property = "fileUploadLogsId")
public class FileUploadLogs implements Serializable{
	private static final long serialVersionUID = 6449178331536877659L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer fileUploadLogsId;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ownerId")
	private Owner owner; 

	private String uploadFileNames; 
	private Integer persistedCount; 
	private Integer failedToPersistCount; 
	private Integer validationErrorsCount; 
	private LocalDateTime fileUploadCompleteTimestamp;
	
	public FileUploadLogs() {
		super();
		setFileUploadCompleteTimestamp(LocalDateTime.now());
	}

	public Owner getOwner() {
		return owner;
	}

	public void setOwner(Owner owner) {
		this.owner = owner;
	}

	public Integer getFileUploadLogsId() {
		return fileUploadLogsId;
	}

	public void setFileUploadLogsId(Integer fileUploadLogsId) {
		this.fileUploadLogsId = fileUploadLogsId;
	}

	public String getUploadFileNames() {
		return uploadFileNames;
	}

	public void setUploadFileNames(String uploadFileNames) {
		this.uploadFileNames = uploadFileNames;
	}

	public Integer getPersistedCount() {
		return persistedCount;
	}

	public void setPersistedCount(Integer persistedCount) {
		this.persistedCount = persistedCount;
	}

	public Integer getFailedToPersistCount() {
		return failedToPersistCount;
	}

	public void setFailedToPersistCount(Integer failedToPersistCount) {
		this.failedToPersistCount = failedToPersistCount;
	}

	public Integer getValidationErrorsCount() {
		return validationErrorsCount;
	}

	public void setValidationErrorsCount(Integer validationErrorsCount) {
		this.validationErrorsCount = validationErrorsCount;
	}

	public LocalDateTime getFileUploadCompleteTimestamp() {
		return fileUploadCompleteTimestamp;
	}

	public void setFileUploadCompleteTimestamp(LocalDateTime fileUploadCompleteTimestamp) {
		this.fileUploadCompleteTimestamp = fileUploadCompleteTimestamp;
	}

	@Override
	public int hashCode() {
		return Objects.hash(failedToPersistCount, fileUploadCompleteTimestamp, fileUploadLogsId, owner, persistedCount,
				uploadFileNames, validationErrorsCount);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof FileUploadLogs)) {
			return false;
		}
		FileUploadLogs other = (FileUploadLogs) obj;
		return Objects.equals(failedToPersistCount, other.failedToPersistCount)
				&& Objects.equals(fileUploadCompleteTimestamp, other.fileUploadCompleteTimestamp)
				&& Objects.equals(fileUploadLogsId, other.fileUploadLogsId) && Objects.equals(owner, other.owner)
				&& Objects.equals(persistedCount, other.persistedCount)
				&& Objects.equals(uploadFileNames, other.uploadFileNames)
				&& Objects.equals(validationErrorsCount, other.validationErrorsCount);
	}
	
	@Override
	public String toString(){
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
}
