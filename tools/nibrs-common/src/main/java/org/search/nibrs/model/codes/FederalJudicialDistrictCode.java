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
package org.search.nibrs.model.codes;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Code enum for Federal Judicial District Code (Data element 1A)
 */
public enum FederalJudicialDistrictCode {
	
	_002("002" , "Alabama, Middle District", "ALABAMA_MIDDLE"),
	_001("001" , "Alabama, Northern District", "ALABAMA_NORTHERN"),
	_003("003" , "Alabama, Southern District", "ALABAMA_SOUTHERN"),
	_006("006" , "Alaska", "ALASKA"),
	_008("008" , "Arizona", "ARIZONA"),
	_009("009" , "Arkansas, Eastern District", "ARKANSAS_EASTERN"),
	_010("010" , "Arkansas, Western District", "ARKANSAS_WESTERN"),
	_012("012" , "California, Central District", "CALIFORNIA_CENTRAL"),
	_097("097" , "California, Eastern District", "CALIFORNIA_EASTERN"),
	_011("011" , "California, Northern District", "CALIFORNIA_NORTHERN"),
	_098("098" , "California, Southern District", "CALIFORNIA_SOUTHERN"),
	_013("013" , "Colorado", "COLORADO"),
	_014("014" , "Connecticut", "CONNECTICUT"),
	_015("015" , "Delaware", "DELAWARE"),
	_016("016" , "District of Columbia", "WASHINGTON DC"),
	_018("018" , "Florida, Middle District", "FLORIDA_MIDDLE"),
	_017("017" , "Florida, Northern District", "FLORIDA_NORTHERN"),
	_004("004" , "Florida, Southern District", "FLORIDA_SOUTHERN"),
	_020("020" , "Georgia, Middle District", "GEORGIA_MIDDLE"),
	_019("019" , "Georgia, Northern District", "GEORGIA_NORTHERN"),
	_021("021" , "Georgia, Southern District", "GEORGIA_SOUTHERN"),
	_093("093" , "Guam", "GUAM"),
	_022("022" , "Hawaii", "HAWAII"),
	_023("023" , "Idaho", "IDAHO"),
	_026("026" , "Illinois, Central District", "ILLINOIS_CENTRAL"),
	_024("024" , "Illinois, Northern District", "ILLINOIS_NORTHERN"),
	_025("025" , "Illinois, Southern District", "ILLINOIS_SOUTHERN"),
	_027("027" , "Indiana, Northern District", "INDIANA_NORTHERN"),
	_028("028" , "Indiana, Southern District", "INDIANA_SOUTHERN"),
	_029("029" , "Iowa, Northern District", "IOWA_NORTHERN"),
	_030("030" , "Iowa, Southern District", "IOWA_SOUTHERN"),
	_031("031" , "Kansas", "KANSAS"),
	_032("032" , "Kentucky, Eastern District", "KENTUCKY_EASTERN"),
	_033("033" , "Kentucky, Western District", "KENTUCKY_WESTERN"),
	_034("034" , "Louisiana, Eastern District", "LOUISIANA_EASTERN"),
	_095("095" , "Louisiana, Middle District", "LOUISIANA_MIDDLE"),
	_035("035" , "Louisiana, Western District", "LOUISIANA_WESTERN"),
	_036("036" , "Maine", "MAINE"),
	_037("037" , "Maryland", "MARYLAND"),
	_038("038" , "Massachusetts", "MASSACHUSETTS"),
	_039("039" , "Michigan, Eastern District", "MICHIGAN_EASTERN"),
	_040("040" , "Michigan, Western District", "MICHIGAN_WESTERN"),
	_041("041" , "Minnesota", "MINNESOTA"),
	_042("042" , "Mississippi, Northern District", "MISSISSIPPI_NORTHERN"),
	_043("043" , "Mississippi, Southern District", "MISSISSIPPI_SOUTHERN"),
	_044("044" , "Missouri, Eastern District", "MISSOURI_EASTERN"),
	_045("045" , "Missouri, Western District", "MISSOURI_WESTERN"),
	_046("046" , "Montana", "MONTANA"),
	_047("047" , "Nebraska", "NEBRASKA"),
	_048("048" , "Nevada", "NEVADA"),
	_049("049" , "New Hampshire", "NEW HAMPSHIRE"),
	_050("050" , "New Jersey", "NEW JERSEY"),
	_051("051" , "New Mexico", "NEW MEXICO"),
	_053("053" , "New York, Eastern District", "NEW YORK_EASTERN"),
	_052("052" , "New York, Northern District", "NEW YORK_NORTHERN"),
	_054("054" , "New York, Southern District", "NEW YORK_SOUTHERN"),
	_055("055" , "New York, Western District", "NEW YORK_WESTERN"),
	_056("056" , "North Carolina, Eastern District", "NORTH CAROLINA_EASTERN"),
	_057("057" , "North Carolina, Middle District", "NORTH CAROLINA_MIDDLE"),
	_058("058" , "North Carolina, Western District", "NORTH CAROLINA_WESTERN"),
	_059("059" , "North Dakota", "NORTH DAKOTA"),
	_005("005" , "Northern Mariana Islands", "MARIANA ISLANDS_NORTHERN"),
	_060("060" , "Ohio, Northern District", "OHIO_NORTHERN"),
	_061("061" , "Ohio, Southern District", "OHIO_SOUTHERN"),
	_063("063" , "Oklahoma, Eastern District", "OKLAHOMA_EASTERN"),
	_062("062" , "Oklahoma, Northern District", "OKLAHOMA_NORTHERN"),
	_064("064" , "Oklahoma, Western District", "OKLAHOMA_WESTERN"),
	_065("065" , "Oregon", "OREGON"),
	_066("066" , "Pennsylvania, Eastern District", "PENNSYLVANIA_EASTERN"),
	_067("067" , "Pennsylvania, Middle District", "PENNSYLVANIA_MIDDLE"),
	_068("068" , "Pennsylvania, Western District", "PENNSYLVANIA_WESTERN"),
	_069("069" , "Puerto Rico", "PUERTO RICO"),
	_070("070" , "Rhode Island", "RHODE ISLAND"),
	_071("071" , "South Carolina", "SOUTH CAROLINA"),
	_073("073" , "South Dakota", "SOUTH DAKOTA"),
	_074("074" , "Tennessee, Eastern", "TENNESSEE_EASTERN"),
	_075("075" , "Tennessee, Middle", "TENNESSEE_MIDDLE"),
	_076("076" , "Tennessee, Western", "TENNESSEE_WESTERN"),
	_078("078" , "Texas, Eastern", "TEXAS_EASTERN"),
	_079("079" , "Texas, Northern", "TEXAS_NORTHERN"),
	_077("077" , "Texas, Southern", "TEXAS_SOUTHERN"),
	_080("080" , "Texas, Western", "TEXAS_WESTERN"),
	_081("081" , "Utah", "UTAH"),
	_082("082" , "Vermont", "VERMONT"),
	_094("094" , "Virgin Islands", "VIRGIN ISLANDS"),
	_083("083" , "Virginia, Eastern", "VIRGINIA_EASTERN"),
	_084("084" , "Virginia, Western", "VIRGINIA_WESTERN"),
	_085("085" , "Washington, Eastern", "WASHINGTON_EASTERN"),
	_086("086" , "Washington, Western", "WASHINGTON_WESTERN"),
	_087("087" , "West Virginia, Northern", "WEST VIRGINIA_NORTHERN"),
	_088("088" , "West Virginia, Southern", "WEST VIRGINIA_SOUTHERN"),
	_089("089" , "Wisconsin, Eastern", "WISCONSIN_EASTERN"),
	_090("090" , "Wisconsin, Western", "WISCONSIN_WESTERN"),
	_091("091" , "Wyoming", "WYOMING");
		
	private FederalJudicialDistrictCode(String code, String description, String iepdCode){
		this.code = code;
		this.description = description;
		this.iepdCode = iepdCode;
	}
	
	public String code;
	public String description;
	public String iepdCode;

	public static final Set<FederalJudicialDistrictCode> asSet() {
		return EnumSet.allOf(FederalJudicialDistrictCode.class);
	}
	
	public static final Set<String> codeSet() {
		Set<String> ret = new HashSet<>();
		for (FederalJudicialDistrictCode v : values()) {
			ret.add(v.code);
		}
		return ret;
	}
	
	public static final Set<String> noneOrUnknownValueCodeSet() {
		Set<String> ret = new HashSet<>();
		ret.add(_88.code);
		ret.add(_99.code);
		return ret;
	}
	
	public static final FederalJudicialDistrictCode valueOfIepdCode(String iepdCode){
		return	Arrays.stream(FederalJudicialDistrictCode.values()).filter(i->i.iepdCode.equals(iepdCode)).findFirst().orElse(null); 
	}

	public static final FederalJudicialDistrictCode valueOfCode(String code){
		return	Arrays.stream(FederalJudicialDistrictCode.values()).filter(i->i.code.equals(code)).findFirst().orElse(null); 
	}
	
}
