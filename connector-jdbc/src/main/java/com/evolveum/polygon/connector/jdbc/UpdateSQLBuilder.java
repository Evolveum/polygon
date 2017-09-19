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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.framework.common.objects.Uid;

/**
 * @author Lukas Skublik
 *
 */
public class UpdateSQLBuilder implements InsertOrUpdateSQL {

	
	private String nameOfTable = "";
	private String bodyOfSQLRequest = "";
	private String whereClause = "";
	private List<SQLParameter> parameters = null;
	
	@Override
	public SQLRequest build(){
		
		if(StringUtil.isBlank(this.nameOfTable)){
			throw new IllegalArgumentException("Variable nameOfTable can not be empty.");
		}
		
		if(StringUtil.isBlank(this.bodyOfSQLRequest)){
			throw new IllegalArgumentException("Variable bodyOfSQLRequest can not be empty.");
		}
		
		if(StringUtil.isBlank(this.whereClause)){
			throw new IllegalArgumentException("Variable whereClause can not be empty.");
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ").append(" ").append(this.nameOfTable).append(" SET ").append(this.bodyOfSQLRequest).append(" ").append(this.whereClause); 
		return new SQLRequest(sb.toString(), this.parameters);
	}
	
	public SQLRequest build(String nameOfTable, List<SQLParameter> parameters, Uid userUid, String nameOfUidParameter){
		setNameOfTable(nameOfTable);
		setNameAndValueOfcolumn(parameters);
		setWhereClause(userUid, nameOfUidParameter);
		
		return build();
	}
	
	/**
	 * @param whereClause the whereClause to set
	 */
	public void setWhereClause(Uid userUid, String nameOfUidParameter) {
		StringBuilder sb = new StringBuilder();
		sb.append(" WHERE ").append(nameOfUidParameter).append(" = ").append(userUid.getUidValue());
		this.whereClause = sb.toString();
	}
	
	/**
	 * @param nameOfTable the nameOfTable to set
	 */
	@Override
	public void setNameOfTable(String nameOfTable) {
		this.nameOfTable = nameOfTable;
	}
	
	@Override
	public void setNameAndValueOfcolumn(List<SQLParameter> parameters){
		if(parameters == null){
			throw new IllegalArgumentException("SQL parameters can not be null.");
		}
		for (SQLParameter parameter : parameters){
			setNameAndValueOfColumn(parameter);
		}
		
	}
	
	@Override
	public void setNameAndValueOfColumn(SQLParameter parameter){
		
		if(parameter == null){
			throw new IllegalArgumentException("SQL parameter can not be null.");
		}
		
		String name = parameter.getName();
		if(name == null){
			throw new IllegalArgumentException("Name of SQL parameter can not be null.");
		}

		StringBuilder sb = new StringBuilder();
		if(!StringUtil.isBlank(this.bodyOfSQLRequest)){
			sb.append(this.bodyOfSQLRequest).append(", ");
		}
		
		sb.append(name).append(" = ").append("?");
		this.bodyOfSQLRequest = sb.toString();
		if(this.parameters == null){
			this.parameters = new ArrayList<SQLParameter>();
		}
		this.parameters.add(parameter);
	}
	
}
