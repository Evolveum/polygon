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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

import org.identityconnectors.common.StringUtil;

/**
 * @author Lukas Skublik
 *
 */
public final class JdbcUtil {

	public static Properties convertArrayToProperties(String[] entries) {
		Properties properties = new Properties();
        if (entries != null) {
            for (String entry : entries) {
                if (StringUtil.isNotBlank(entry)) {
                    int firstSeparator = entry.indexOf('=');
                    if (firstSeparator == -1) {
                    	StringBuilder sb = new StringBuilder();
                    	sb.append("Invalid value in JNDI entry for ").append(entry);
                    	throw new IllegalArgumentException(sb.toString());
                    }
                    if (firstSeparator == 0) {
                    	StringBuilder sb = new StringBuilder();
                    	sb.append("First character cannot be '=' for ").append(entry);
                    	throw new IllegalArgumentException(sb.toString());
                    }
                    final String key = entry.substring(0, firstSeparator);
                    final String value =
                    		firstSeparator == entry.length() - 1 ? null : entry.substring(firstSeparator + 1);
                    properties.put(key, value);
                }
            }
        }
        return properties;
    }
	
	public static Class<?> getTypeOfAttribute(int type) {
        if(type == Types.BIGINT){
        	return Long.class;
        }else if(type == Types.DOUBLE){
        	return Double.class;
        }else if(type == Types.FLOAT || type == Types.REAL){
        	return Float.class;
        }else if(type == Types.INTEGER){
        	return Integer.class;
        }else if(type == Types.BOOLEAN || type == Types.BIT){
        	return Boolean.class;
        }else if(type == Types.TINYINT){
        	return Byte.class;
        }else if(type == Types.BLOB || type == Types.BINARY || type == Types.VARBINARY || type == Types.LONGVARBINARY){
        	return byte[].class;
        }else if(type == Types.DECIMAL || type == Types.NUMERIC){
        	return BigDecimal.class;
        }else{
        	return String.class;
        }
    }
	
}
