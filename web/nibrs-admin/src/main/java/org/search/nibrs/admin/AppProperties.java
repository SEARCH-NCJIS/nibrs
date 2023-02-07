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
package org.search.nibrs.admin;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("admin")
public class AppProperties {

	private Boolean allowSubmitToFbi = true;
	private Boolean privateSummaryReportSite=false; 
	private String xmlDocumentDownloadRootFolder = "/tmp/nibrs/xmlDownload";
	
	private Boolean securityEnabled = false;
	private Boolean allowAccessWithoutSamlToken = false;
	private String signOutUrl = "/logoutSuccess"; 
	private Boolean showUserInfoDropdown = false;
	private Boolean demoLawEnforcementEmployerIndicator = true;
	private Integer ajpPort = 9090; 
	private Boolean ajpEnabled = true; 
	private Boolean flatFileToXmlFileConversion = false; 
	private Integer flatFileToXmlFileConversionUpperLimit = 5000; 
	
	private String externalTemplatesFolder;
	private String brandImagePath="/images/search-logo-scales-transparent.png";
	
	private Boolean inactivityTimeout=true;
	private Integer inactivityTimeoutInSeconds = 1800; 
	
	private String restServiceBaseUrl = "http://localhost:9080";
	
	private final Map<String, String> externalLinksMapping = new HashMap<>();
	private final Map<String, String> aboutLinksMapping = new HashMap<>();

	public AppProperties() {
		super();
	}

	public Boolean getAjpEnabled() {
		return ajpEnabled;
	}

	public void setAjpEnabled(Boolean ajpEnabled) {
		this.ajpEnabled = ajpEnabled;
	}

	public Integer getAjpPort() {
		return ajpPort;
	}

	public void setAjpPort(Integer ajpPort) {
		this.ajpPort = ajpPort;
	}

	public Boolean getDemoLawEnforcementEmployerIndicator() {
		return demoLawEnforcementEmployerIndicator;
	}

	public void setDemoLawEnforcementEmployerIndicator(Boolean demoLawEnforcementEmployerIndicator) {
		this.demoLawEnforcementEmployerIndicator = demoLawEnforcementEmployerIndicator;
	}

	public String getSignOutUrl() {
		return signOutUrl;
	}

	public void setSignOutUrl(String signOutUrl) {
		this.signOutUrl = signOutUrl;
	}

	public Boolean getAllowAccessWithoutSamlToken() {
		return allowAccessWithoutSamlToken;
	}

	public void setAllowAccessWithoutSamlToken(Boolean allowAccessWithoutSamlToken) {
		this.allowAccessWithoutSamlToken = allowAccessWithoutSamlToken;
	}

	public Boolean getShowUserInfoDropdown() {
		return showUserInfoDropdown;
	}

	public void setShowUserInfoDropdown(Boolean showUserInfoDropdown) {
		this.showUserInfoDropdown = showUserInfoDropdown;
	}

	public Boolean getSecurityEnabled() {
		return securityEnabled;
	}

	public void setSecurityEnabled(Boolean securityEnabled) {
		this.securityEnabled = securityEnabled;
	}

	public Boolean getInactivityTimeout() {
		return inactivityTimeout;
	}

	public void setInactivityTimeout(Boolean inactivityTimeout) {
		this.inactivityTimeout = inactivityTimeout;
	}

	public Integer getInactivityTimeoutInSeconds() {
		return inactivityTimeoutInSeconds;
	}

	public void setInactivityTimeoutInSeconds(Integer inactivityTimeoutInSeconds) {
		this.inactivityTimeoutInSeconds = inactivityTimeoutInSeconds;
	}

	public String getRestServiceBaseUrl() {
		return restServiceBaseUrl;
	}

	public void setRestServiceBaseUrl(String restServiceBaseUrl) {
		this.restServiceBaseUrl = restServiceBaseUrl;
	}

	public String getExternalTemplatesFolder() {
		return externalTemplatesFolder;
	}

	public void setExternalTemplatesFolder(String externalTemplatesFolder) {
		this.externalTemplatesFolder = externalTemplatesFolder;
	}

	public Boolean getAllowSubmitToFbi() {
		return allowSubmitToFbi;
	}

	public void setAllowSubmitToFbi(Boolean allowSubmitToFbi) {
		this.allowSubmitToFbi = allowSubmitToFbi;
	}

	public Boolean getPrivateSummaryReportSite() {
		return privateSummaryReportSite;
	}

	public void setPrivateSummaryReportSite(Boolean privateSummaryReportSite) {
		this.privateSummaryReportSite = privateSummaryReportSite;
	}

	public String getBrandImagePath() {
		return brandImagePath;
	}

	public void setBrandImagePath(String brandImagePath) {
		this.brandImagePath = brandImagePath;
	}

	public Map<String, String> getExternalLinksMapping() {
		return externalLinksMapping;
	}

	public Map<String, String> getAboutLinksMapping() {
		return aboutLinksMapping;
	}

	public String getXmlDocumentDownloadRootFolder() {
		return xmlDocumentDownloadRootFolder;
	}

	public void setXmlDocumentDownloadRootFolder(String xmlDocumentDownloadRootFolder) {
		this.xmlDocumentDownloadRootFolder = xmlDocumentDownloadRootFolder;
	}

	public Boolean getFlatFileToXmlFileConversion() {
		return flatFileToXmlFileConversion;
	}

	public void setFlatFileToXmlFileConversion(Boolean flatFileToXmlFileConversion) {
		this.flatFileToXmlFileConversion = flatFileToXmlFileConversion;
	}

	public Integer getFlatFileToXmlFileConversionUpperLimit() {
		return flatFileToXmlFileConversionUpperLimit;
	}

	public void setFlatFileToXmlFileConversionUpperLimit(Integer flatFileToXmlFileConversionUpperLimit) {
		this.flatFileToXmlFileConversionUpperLimit = flatFileToXmlFileConversionUpperLimit;
	}

}