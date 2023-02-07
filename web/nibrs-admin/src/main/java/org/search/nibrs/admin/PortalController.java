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

import java.io.IOException;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.search.nibrs.admin.security.AuthUser;
import org.search.nibrs.admin.services.rest.RestService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes({"showUserInfoDropdown", "agencyMapping"})
public class PortalController {
	@Resource
	AppProperties appProperties;
	
	@Resource
	RestService restService;

	@GetMapping("/")
	public String getSplashPage(Model model) throws IOException {
	
	    return "index";
	}
	
	@GetMapping("/home")
	public String getHomePage(Model model) throws IOException {
		
		AuthUser authUser = (AuthUser) model.getAttribute("authUser");
        if (!model.containsAttribute("agencyMapping")){
			if (appProperties.getPrivateSummaryReportSite()) {
				model.addAttribute("agencyMapping", restService.getAgencies(null));
			}
			else if (authUser != null){
				model.addAttribute("agencyMapping", restService.getAgencies(authUser.getUserId()));
			}
			else {
				model.addAttribute("agencyMapping", new HashMap<Integer, String>());
			}
        }
		return "home";
	}
	
	@GetMapping("/logout")
	public String logout(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    if (auth != null){    
	        new SecurityContextLogoutHandler().logout(request, response, auth);
	    }
	    return "redirect:" + appProperties.getSignOutUrl();
	}

	@GetMapping("/logoutSuccess")
	public String logoutSuccess(Model model) throws IOException {
		
		return "logoutSuccess";
	}
	
}

