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
import java.util.Objects;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

//@Component
public class CustomHttpSessionListener implements HttpSessionListener{

private static final Log LOG= LogFactory.getLog(CustomHttpSessionListener.class);

 @Override
 public void sessionCreated(HttpSessionEvent se) {
     LOG.error("New session is created.");
     User principal = SecurityContextHolder.getContext().getAuthentication() != null? (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal():null;
     LOG.info("principal: " + Objects.toString(principal));
 }

 @Override
 public void sessionDestroyed(HttpSessionEvent se) {
     LOG.error("Session destroyed.");

 }}