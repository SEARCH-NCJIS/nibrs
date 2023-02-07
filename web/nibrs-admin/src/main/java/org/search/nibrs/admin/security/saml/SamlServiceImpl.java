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
package org.search.nibrs.admin.security.saml;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.BooleanUtils;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.search.nibrs.admin.AppProperties;
import org.search.nibrs.xml.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


@Service("samlService")
public class SamlServiceImpl implements SamlService{
	
	private final Logger log = LoggerFactory.getLogger(SamlServiceImpl.class);

	@Resource
	AppProperties appProperties;

	public Element getSamlAssertion(HttpServletRequest request) {
		Element assertion = (Element) request.getAttribute("samlAssertion");

		if (assertion == null) {
			try {
				assertion = retrieveAssertionFromShibboleth(request);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (assertion == null && appProperties.getAllowAccessWithoutSamlToken()){
			log.info("Creating demo user saml assertion.");
			assertion = createDemoUserSamlAssertion();
		}

		if (assertion != null) {
			try {
				log.debug(XmlUtils.getStringFromNode(assertion));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			log.info("SAML assertion is null ");
		}
		return assertion;
	}
	
	Element retrieveAssertionFromShibboleth(HttpServletRequest request) throws Exception
	{
		log.debug("getting SAML assertion from http servlet request: " + request);
		if (request == null) return null;
		// Note: pulled this straight from Andrew's demo JSP that displays the assertion and http request...
		
		/*
		 *  fix for Exception in thread "main" javax.net.ssl.SSLHandshakeException:
		 *       sun.security.validator.ValidatorException:
		 *           PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException:
		 *               unable to find valid certification path to requested target
		 */
		 TrustManager[] trustAllCerts = new TrustManager[]{
				    new X509TrustManager() {
				        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				            return null;
				        }
				        public void checkClientTrusted(
				            java.security.cert.X509Certificate[] certs, String authType) {
				        }
				        public void checkServerTrusted(
				            java.security.cert.X509Certificate[] certs, String authType) {
				        }
				    }
				};
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				return true;  // andrew had this as false...dont know how that would work...
			}
		};
		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		/*
		 * end of the fix
		 */
		 //Hard coded to pick up a single assertion...could loop through assertion headers if there will  be more than one
		String assertionHttpHeaderName = request.getHeader("Shib-Assertion-01");
		log.info("Loading assertion from: " + assertionHttpHeaderName);
		
		if(assertionHttpHeaderName == null){
			log.warn("Shib-Assertion-01 header was null, Returning null asssertion document element");
			return null;
		}
		
		URL url = new URL(assertionHttpHeaderName);
		URLConnection con = url.openConnection();

		InputStream is = con.getInputStream();
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document assertionDoc = db.parse(is);
		
		return assertionDoc.getDocumentElement();
		
	}
	
    private Element createDemoUserSamlAssertion() {
    	
    	Element samlAssertion = null;
        try {
            Map<SamlAttribute, String> customAttributes = new HashMap<SamlAttribute, String>();
//            customAttributes.put(SamlAttribute.FederationId, "");
//            customAttributes.put(SamlAttribute.FederationId, "HIJIS:IDP:HCJDC:USER:demouser");
            customAttributes.put(SamlAttribute.FederationId, "HIJIS:IDP:HCJDC:USER:demouser");
//            customAttributes.put(SamlAttribute.FederationId, "HIJIS:IDP:HCJDC:USER:demouser2");
            customAttributes.put(SamlAttribute.EmployerORI, "OK072015A");
            //customAttributes.put(SamlAttribute.EmployerORI, "1234567890");
            customAttributes.put(SamlAttribute.FirearmsRegistrationRecordsPersonnelIndicator, "true");
            customAttributes.put(SamlAttribute.SupervisoryRoleIndicator, "true");
            customAttributes.put(SamlAttribute.GivenName, "Demo");
            customAttributes.put(SamlAttribute.SurName, "User");
//            customAttributes.put(SamlAttribute.FederatedQueryUserIndicator, "");
//                customAttributes.put("gfipm:2.0:user:EmployerORI", "H00000001");
            if (BooleanUtils.isNotTrue(appProperties.getDemoLawEnforcementEmployerIndicator())){
        		customAttributes.put(SamlAttribute.LawEnforcementEmployerIndicator, "false");
            }

            customAttributes.put(SamlAttribute.EmployerOrganizationCategoryText, "Municipal Court");
//            customAttributes.put(SamlAttribute.EmployerOrganizationCategoryText, "District Attorney");
            samlAssertion = SamlTokenUtils.createStaticAssertionAsElement("http://ojbc.org/ADS/AssertionDelegationService", 
                    SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS, 
                    SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1, true, true, customAttributes);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return samlAssertion;
    }

}