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

import java.math.BigDecimal;
import java.sql.Types;

/**
 * @author Lukas Skublik
 *
 */
public class SQLParameter {

	private int sqlType;
	private Object value;
	private String name;
	
	/**
	 * 
	 */
	public SQLParameter(int sqlType, Object value, String name) {
		this.sqlType = sqlType;
		this.value = value;
		this.name = name;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * @return the sqlType
	 */
	public int getSqlType() {
		return sqlType;
	}
	
	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	} 
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName()).append(" = ");
		
		if(getSqlType() == Types.BIGINT){
        	sb.append("(BIGINT)");
        }else if(getSqlType() == Types.DOUBLE){
        	sb.append("(DOUBLE)");
        }else if(getSqlType() == Types.FLOAT){
        	sb.append("(FLOAT)");
        }else if(getSqlType() == Types.REAL){
        	sb.append("(REAL)");
        }else if(getSqlType() == Types.INTEGER){
        	sb.append("(INTEGER)");
        }else if(getSqlType() == Types.BIT){
        	sb.append("(BIT)");
        }else if(getSqlType() == Types.BOOLEAN ){
        	sb.append("(BOOLEAN)");
        }else if(getSqlType() == Types.TINYINT){
        	sb.append("(TINYINT)");
        }else if(getSqlType() == Types.BLOB){
        	sb.append("(BLOB)");
        }else if(getSqlType() == Types.BINARY){
        	sb.append("(BINARY)");
        }else if(getSqlType() == Types.VARBINARY){
        	sb.append("(VARBINARY)");
        }else if(getSqlType() == Types.LONGVARBINARY){
        	sb.append("(LONGVARBINARY)");
        }else if(getSqlType() == Types.DECIMAL){
        	sb.append("(DECIMAL)");
        }else if(getSqlType() == Types.NUMERIC){
        	sb.append("(NUMERIC)");
        }else if(getSqlType() == Types.VARCHAR){
        	sb.append("(VARCHAR)");
        }else {
        	sb.append("(Number of SQL Type: ").append(getSqlType()).append(")");
        }
		sb.append(" ").append(getValue());
		
		
		return sb.toString();
	}
}
