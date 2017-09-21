/**
 * Copyright (c) 2010-2017 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.polygon.connector.jdbc;

import java.util.List;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.framework.common.objects.filter.Filter;

/**
 * @author Lukas Skublik
 *
 */
public class SelectSQLBuilder {
	
	private String topClause = "";
	private String namesOfColumn = "*";
	private String intoClause = "";
	private String nameOfTable = "";
	private String joinClause = "";
	private String whereClause = "";
	private int countForLimitClause = -1;
	private String nameForGroupByClause = "";
	private String havingClause = "";
	private String nameOfAttributeForOrder = "";
	
	public String build(){
		if(StringUtil.isBlank(this.nameOfTable)){
			throw new IllegalArgumentException("Variable nameOfTable can not be empty.");
		}
		
		if(StringUtil.isBlank(this.whereClause)){
			throw new IllegalArgumentException("Variable whereClause can not be empty.");
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT ").append(this.topClause).append(" ").append(this.namesOfColumn).append(this.intoClause).append(" FROM ").append(this.nameOfTable).append(" ").append(this.joinClause).append(" ").append(this.whereClause);
		if(this.countForLimitClause != -1){
			sb.append(" LIMIT ").append(this.countForLimitClause);
		}
		if(!StringUtil.isBlank(this.nameForGroupByClause)){
			sb.append(" GROUP BY ").append(this.nameForGroupByClause);
		}
		sb.append(" ").append(this.havingClause);
		if(!StringUtil.isBlank(this.nameOfAttributeForOrder)){
			sb.append(" ORDER BY ").append(this.nameOfAttributeForOrder);
		}
		return sb.toString();
	}
	
	/**
	 * @param countForLimitClause the countForLimitClause to set
	 */
	public void setCountForLimitClause(int countForLimitClause) {
		this.countForLimitClause = countForLimitClause;
	}
	
	/**
	 * @param topClause the topClause to set
	 */
	public void setTopClause(String topClause) {
		this.topClause = topClause;
	}
	
	public void setAllNamesOfColumns(String stringContainingAllNames){
		this.namesOfColumn = stringContainingAllNames;
	}
	
	public void setAllNamesOfColumns(String[] allNames){
		StringBuilder sb = new StringBuilder();
		for(String name : allNames){
			if(sb.length() != 0){
				sb.append(", ");
			}
			sb.append(name);
		}
		this.namesOfColumn = sb.toString();
	}
	
	public void addNameOfColumnWithAlias(String name, String alias){
		addNameOfColumnWithFunctionAndAlias("", name, alias);
	}
	
	public void addNameOfColumnWithFunction(String function, String name){
		addNameOfColumnWithFunctionAndAlias(function, name, "");
	}
	
	public void addNameOfColumn(String name){
		addNameOfColumnWithFunctionAndAlias("", name, "");
	}
	
	public void addNameOfColumnWithFunctionAndAlias(String function, String name, String alias){
		StringBuilder sb = new StringBuilder();
		if(!StringUtil.isBlank(this.namesOfColumn)){
			sb.append(this.namesOfColumn).append(", ");
		}
		if(!StringUtil.isBlank(this.namesOfColumn)){
			sb.append(function).append("(").append(name).append(")");
			
		} else {
			sb.append(name);
		}
		
		if(!StringUtil.isBlank(alias)){
			sb.append(" AS ").append(alias);
		}
		this.namesOfColumn = sb.toString();
	}
	
	
	/**
	 * @param intoClause the intoClause to set
	 */
	public void setIntoClause(String intoClause) {
		this.intoClause = intoClause;
	}
	
	/**
	 * @param nameOfTable the nameOfTable to set
	 */
	public void setALLNamesOfTables(String namesOfTables) {
		this.nameOfTable = namesOfTables;
	}
	
	public void addNameOfTable(String name, String alias) {
		StringBuilder sb = new StringBuilder();
		if(!StringUtil.isBlank(this.nameOfTable)){
			sb.append(this.nameOfTable).append(", ");
		}
		sb.append(name);
		
		if(!StringUtil.isBlank(alias)){
			sb.append(" AS ").append(alias);
		}
		this.nameOfTable = sb.toString();
	}
	
	public void addNameOfTable(String name) {
		addNameOfTable(name, "");
	}
	
	/**
	 * @param joinClause the joinClause to set
	 */
	public void setJoinClause(String joinClause) {
		this.joinClause = joinClause;
	}
	
	/**
	 * @param whereClause the whereClause to set
	 */
	public void setWhereClause(String whereClause) {
		this.whereClause = whereClause;
	}
	
	/**
	 * @param whereClause the whereClause to set
	 */
	
	/**
	 * @param nameForGroupByClause the nameForGroupByClause to set
	 */
	public void setNameForGroupByClause(String nameForGroupByClause) {
		StringBuilder sb = new StringBuilder();
		sb.append(" GROUP BY ").append(nameForGroupByClause);
		this.nameForGroupByClause = nameForGroupByClause;
	}
	
	/**
	 * @param havingClause the havingClause to set
	 */
	public void setHavingClause(String havingClause) {
		this.havingClause = havingClause;
	}
	
	public void setAddNameOfColumnToOrderByClause(String name, Boolean ascending) {
		setAddNameOfColumnToOrderByClause("", name, ascending);
	}
	
	public void setAddNameOfColumnToOrderByClause(String name) {
		setAddNameOfColumnToOrderByClause("", name, true);
	}
	
	public void setAddNameOfColumnToOrderByClause(String function, String name) {
		setAddNameOfColumnToOrderByClause(function, name, true);
	}
	
	public void setAddNameOfColumnToOrderByClause(String function, String name, Boolean ascending) {
		StringBuilder sb = new StringBuilder();
		if(!StringUtil.isBlank(this.nameOfAttributeForOrder)){
			sb.append(this.nameOfAttributeForOrder).append(", ");
		}
		if(!StringUtil.isBlank(this.nameOfAttributeForOrder)){
			sb.append(function).append("(").append(name).append(")");
			
		} else {
			sb.append(name);
		}
		
		if(!ascending){
			sb.append(" ASC");
		} else {
			sb.append(" DESC");
		}
		this.namesOfColumn = sb.toString();
	}
	
}
