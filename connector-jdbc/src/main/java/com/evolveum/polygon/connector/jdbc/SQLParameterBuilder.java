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

import java.sql.Types;

/**
 * @author Lukas Skublik
 *
 */
public class SQLParameterBuilder {
	
	private int sqlType = Types.NULL;
	private Object value = null;
	private String name = "";
	
	/**
	 * 
	 */
	public SQLParameter build(int type, Object value, String name) {
		SQLParameterBuilder sqlBuilder = new SQLParameterBuilder();
		sqlBuilder.setValue(value);
		sqlBuilder.setSqlType(type);
		sqlBuilder.setName(name);
		return sqlBuilder.build();
	}
	
	/**
	 * 
	 */
	public SQLParameter build(int type, Object value) {
		SQLParameterBuilder sqlBuilder = new SQLParameterBuilder();
		sqlBuilder.setValue(value);
		sqlBuilder.setSqlType(type);
		return sqlBuilder.build();
	}
	
	/**
	 * 
	 */
	public SQLParameter build(int type) {
		SQLParameterBuilder sqlBuilder = new SQLParameterBuilder();
		sqlBuilder.setSqlType(type);
		return sqlBuilder.build();
	}
	
	/**
	 * 
	 */
	public SQLParameter build() {
		return new SQLParameter(sqlType, value, name);
	}
	
	/**
	 * @param sqlType the sqlType to set
	 */
	public void setSqlType(int sqlType) {
		this.sqlType = sqlType;
	}
	
	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}
