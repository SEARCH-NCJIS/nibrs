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
package org.search.nibrs.stagingdata.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.search.nibrs.stagingdata.AppProperties;
import org.search.nibrs.stagingdata.model.FileUploadLogs;
import org.search.nibrs.stagingdata.repository.FileUploadLogsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fileUploadLogs")
public class FileUploadLogsController {
	private final Log log = LogFactory.getLog(this.getClass());

	@Autowired
	private FileUploadLogsRepository fileUploadLogsRepository;
	@Autowired
	public AppProperties appProperties;

	@PostMapping("")
	public FileUploadLogs saveFileUploadLogs(@RequestBody FileUploadLogs fileUploadLogs){
		
		log.info("About to save FileUploadLogs " + fileUploadLogs);
		return fileUploadLogsRepository.save(fileUploadLogs);
	}

}
