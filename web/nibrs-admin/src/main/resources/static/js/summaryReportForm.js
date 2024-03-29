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

 $(function(){
   console.log(agencyMapping);
   $(".chosen-select").chosen();  
   $('[data-toggle="popover"]').popover(); 
   $('.incidentYear').mask('9999');
   
   $('#summaryReportRequestForm input').keypress(function (e) {
     if (e.which == 13) {
       $('#submit').click();
       return false;    
     }
   });
   
   setAgencyIdRequiredAttr();
   setStateCodeRequiredAttr();
   refreshAgencyDropDown();
   refreshIncidentYearDropDown();
   refreshIncidentMonthDropDown();
   
   $('#portalContent').on('change', '#summaryReportRequestForm #agencyId', function(){
	   setStateCodeRequiredAttr();
	   refreshIncidentYearDropDown();
   });
   
   $('#portalContent').on('change', '#summaryReportRequestForm #stateCode', function(){
	   //console.log("stateCode changed to " + $('#stateCode').val());
	   setAgencyIdRequiredAttr();
	   refreshAgencyDropDown();
   });

   $('#portalContent').on('change', '#summaryReportRequestForm #incidentYear', function(){
	   refreshIncidentMonthDropDown()
   });
   
   function setAgencyIdRequiredAttr(){
	   if ($('#stateCode').val()){
		   $('#agencyId').attr('required', false);
	   }
	   else{
		   $('#agencyId').attr('required', true);
	   }
   }

   function setStateCodeRequiredAttr(){
	   if ($('#agencyId').val()){
		   $('#stateCode').attr('required', false);
	   }
	   else{
		   $('#stateCode').attr('required', true);
	   }
   }
   
   function refreshAgencyDropDown(){
     
     var requestData = {
    	stateCode: $("#stateCode").val() 		
     }; 
     if (typeof _csrf_param_name !== 'undefined'){
        requestData[_csrf_param_name] = _csrf_token;
     }

     xhr = $.get( context +"incidents/agencies", requestData , function(data) {
       $('#agencyId').empty();
       if (Object.keys(data).length == 1){
    	   $('#agencyId').append($('<option selected></option>').attr('value', Object.values(data)[0]).text(Object.keys(data)[0]));
       }
       else if (Object.keys(data).length > 1){
	       $('#agencyId').append('<option value="">All</option>');
	       $.each( data, function( key, value ) {
	    	   $('#agencyId').append($('<option></option>').attr('value', value).text(key));
	       });
       }
       $('#agencyId').trigger("chosen:updated");
	   refreshIncidentYearDropDown();

     }).fail(ojbc.displayFailMessage);
   }

   function refreshIncidentMonthDropDown(){
	   agencyId = $('#agencyId').val(); 
	   stateCode = $('#stateCode').val(); 
	   incidentYear = $('#incidentYear').val();
	   if (agencyId && incidentYear){
		   xhr = $.get( context +"months/" + incidentYear + "/" + agencyId, function(data) {
			   $('#incidentMonth').empty();
			   if (data.length == 1){
                   $('#incidentMonth').append($('<option selected></option>').attr('value', data[0]).text(data[0]));
			   }
			   else if (data.length > 1){
				   $('#incidentMonth').append('<option value="0">All months</option>');
				   data.forEach( function(item, index) {
					   $('#incidentMonth').append($('<option></option>').attr('value', item).text(item));
				   });
			   }
			   $('#incidentMonth').trigger("chosen:updated");
		   }).fail(ojbc.displayFailMessage);
	   }
	   else if (stateCode && incidentYear){
		   xhr = $.get( context +"state/months/" + incidentYear + "/" + stateCode, function(data) {
			   $('#incidentMonth').empty();
			   if (data.length == 1){
                   $('#incidentMonth').append($('<option selected></option>').attr('value', data[0]).text(data[0]));
			   }
			   else if (data.length > 1){
				   $('#incidentMonth').append('<option value="0">All months</option>');
				   data.forEach( function(item, index) {
					   $('#incidentMonth').append($('<option></option>').attr('value', item).text(item));
				   });
			   }
			   $('#incidentMonth').trigger("chosen:updated");
		   }).fail(ojbc.displayFailMessage);
	   }
   }
   
   function refreshIncidentYearDropDown(){
//	   console.log("in the refreshIncidentYearDropDown method"); 
//	   console.log("stateCode: " + $('#stateCode').val()); 
	   agencyId = $('#agencyId').val();
	   stateCode = $('#stateCode').val();
	   if (agencyId){
		   console.log("call the getYears method"); 
		   xhr = $.get( context +"years/" + agencyId, function(data) {
			   $('#incidentYear').empty();
			   if (data.length == 1){
                   $('#incidentYear').append($('<option selected></option>').attr('value', data[0]).text(data[0]));
				   refreshIncidentMonthDropDown();
			   }
			   else if (data.length > 1){
				   $('#incidentYear').append('<option value="">Please select ...</option>');
				   data.forEach( function(item, index) {
	                   $('#incidentYear').append($('<option></option>').attr('value', item).text(item));
	               });
			   }
			   $('#incidentYear').trigger("chosen:updated");
	       }).fail(ojbc.displayFailMessage);
	   }
	   else if (stateCode){
//		   console.log("call the getYearsByState method"); 
		   xhr = $.get( context +"state/years/" + stateCode, function(data) {
//			   console.log("years: " + data); 
			   $('#incidentYear').empty();
			   if (data.length == 1){
                   $('#incidentYear').append($('<option selected></option>').attr('value', data[0]).text(data[0]));
				   refreshIncidentMonthDropDown();
			   }
			   else if (data.length > 1){
				   $('#incidentYear').append('<option value="">Please select ...</option>');
				   data.forEach( function(item, index) {
	                   $('#incidentYear').append($('<option></option>').attr('value', item).text(item));
	               });
			   }
			   $('#incidentYear').trigger("chosen:updated");
	       }).fail(ojbc.displayFailMessage);
	   }
   }
   
   $('#submit').click( function(){
	   $("#loadingText").removeClass("d-none");
     var formData = $('#summaryReportRequestForm').serialize(); 
     
     if (typeof _csrf_param_name !== 'undefined'){
    	 formData += '&'+_csrf_param_name+'='+_csrf_token;
     }
     //console.log("formData" + formData);
     
     var form = document.getElementById('summaryReportRequestForm'); 
     
     var isValidForm = form.checkValidity(); 
     form.classList.add('was-validated'); 
     if ( isValidForm === true){
    	 var reportTypes = $('#reportType').val();
    	 var xhr = [], i, j=0;
    	 var len=reportTypes.length;
    	 for( i=0; i < len; i++){
    		 var reportType = reportTypes[i];
    		 console.log("reportType: " + reportType);
    		 xhr[i] = new XMLHttpRequest();
    		 xhr[i].onreadystatechange = function() {
//    			 console.log("i:" + i); 
//    			 console.log("len -1 :" + (len -1)); 
		    	  if( this.readyState === 4){
		    		if (++j === len ){
		    			$("#loadingAjaxPane").hide();
		    		}
		    	  }
		    	  else{
					var loadingDiv =  $("#loadingAjaxPane");
					var portalContentDiv = $("#portalContent");
					
					loadingDiv.css('height', portalContentDiv.height() + 20);
					loadingDiv.css('width', portalContentDiv.width());

					$("#loadingAjaxPane").show();
		    	  }
		     };
		     xhr[i].open('POST', context + "summaryReports/"+reportType, true);
		     xhr[i].setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
		     xhr[i].responseType = 'blob';
//	    	 console.log("xhr[i]: " + xhr[i]); 
		
		     xhr[i].onload = function() {
		         if (this.status === 200) {
		             var blob = this.response;
			    	 var fileName = "";
			    	 var disposition = this.getResponseHeader('Content-Disposition');
			         if (disposition && disposition.indexOf('attachment') !== -1) {
			             var filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
			             var matches = filenameRegex.exec(disposition);
			             if (matches != null && matches[1]) fileName = matches[1].replace(/['"]/g, '');
		         	 }
		             if(window.navigator.msSaveOrOpenBlob) {
		                 window.navigator.msSaveBlob(blob, fileName);
		             }
		             else{
		                 var downloadLink = window.document.createElement('a');
		                 var contentTypeHeader = this.getResponseHeader("Content-Type");
		                 downloadLink.href = window.URL.createObjectURL(new Blob([blob], { type: contentTypeHeader }));
		                 downloadLink.download = fileName;
		                 document.body.appendChild(downloadLink);
		                 downloadLink.click();
		                 document.body.removeChild(downloadLink);
		            }
		        }
		        else{
		        	 console.log("An error occurred while downloading the summary report " + this.status + ": " + this.statusText);
	 	             bootpopup({
	            	    title: "Error",
	            	    content: [
	            	        'An error occurred while processing your request. Please contact SEARCH at <a href="mailto:nibrs@search.org">nibrs@search.org</a> or try again later.'
	            	        ],
	            	    close: function() {}
	            	 });
		        }
		      }
		     xhr[i].send(formData);
    	 }
     }
   });
   
   $('#reset').click (function(){
     xhr = $.get(context + "summaryReports/searchForm/reset", function(data) {
    	 $('#portalContent').html(data);
    	 $('.chosen-select').chosen();
     }).fail(ojbc.displayFailMessage);
   });
   

 });
