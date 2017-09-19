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

import org.identityconnectors.framework.common.objects.Uid;

/**
 * @author Lukas Skublik
 *
 */
public class DeleteBuilder {

	private String nameOfTable = "";
	private String whereClause = "";
	
	
	
	public String build(){
		
		if(this.nameOfTable.isEmpty()){
			throw new IllegalArgumentException("Variable nameOfTable can not be empty.");
		}
		
		if(this.whereClause.isEmpty()){
			throw new IllegalArgumentException("Variable whereClause can not be empty.");
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE ").append(" ").append(this.nameOfTable).append(" ").append(this.whereClause); 
		return sb.toString();
	}
	
	public String build(String nameOfTable, Uid userUid, String nameOfUidParameter){
		setNameOfTable(nameOfTable);
		setWhereClause(userUid, nameOfUidParameter);
		return build();
	}
	
	public void setWhereClause(Uid userUid, String nameOfUidParameter) {
		StringBuilder sb = new StringBuilder();
		sb.append(" WHERE ").append(nameOfUidParameter).append(" = ").append(userUid.getUidValue());
		this.whereClause = sb.toString();
	}
	
	/**
	 * @param nameOfTable the nameOfTable to set
	 */
	public void setNameOfTable(String nameOfTable) {
		this.nameOfTable = nameOfTable;
	}
}
