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
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
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
	
	public static Class<?> getTypeOfAttribute(int type, String timestampPresentation) {
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
        }else if((type == Types.TIMESTAMP || type == Types.DATE) && timestampPresentation != null && timestampPresentation.equalsIgnoreCase("unixEpoch")){
        	return Long.class;
        } else {
        	return String.class;
        }
    }
	
	public static Object getValueOfColumn(int type, int i, ResultSet rs, String timestampPresentation) throws SQLException{
		
		if(type == Types.TINYINT){
			return rs.getByte(i);
		} else if(type == Types.DECIMAL || type == Types.NUMERIC){
			return rs.getBigDecimal(i);
		} else if(type == Types.DOUBLE || type == Types.FLOAT || type == Types.REAL || type == Types.INTEGER || type == Types.BIGINT){
			return rs.getObject(i);
		} else if(type == Types.BLOB || type == Types.BINARY || type == Types.VARBINARY || type == Types.LONGVARBINARY){
			return rs.getObject(i);
		} else if(type == Types.TIMESTAMP){
			return toConnId(rs.getTimestamp(i), timestampPresentation);
		} else if(type == Types.DATE){
			return toConnId(rs.getDate(i), timestampPresentation);
		} else if(type == Types.TIME){
			return toConnId(rs.getTime(i));
		} else if(type == Types.BIT || type == Types.BOOLEAN){
			return rs.getBoolean(i);
		} else if(type == Types.NULL){
			return rs.getObject(i);
		} else {
			return rs.getString(i);
		}
	}
	
	public static Object toConnId(Date date, String timestampPresentation){
		if(timestampPresentation != null && timestampPresentation.equalsIgnoreCase("unixEpoch")){
			return date.getTime();
		} else if(timestampPresentation != null && timestampPresentation.equalsIgnoreCase("string")){
			return new SimpleDateFormat("yyyy-MM-dd").format(date);
		} else {
			throw new IllegalArgumentException("Timestamp Presentation mode has invalid value: '" + timestampPresentation + "'");
		}
	}
	
	public static Object toConnId(Time time){
		try {
			return new SimpleDateFormat("HH:mm:ss.SSSXXX").format(time);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("It is not possible to parse Time from input value:'"+time.toString()+"' ");
		}
	}
	
	public static Object toConnId(Timestamp timestamp, String timestampPresentation){
		if(timestampPresentation != null && timestampPresentation.equalsIgnoreCase("unixEpoch")){
			return timestamp.getTime();
		} else if(timestampPresentation != null && timestampPresentation.equalsIgnoreCase("string")){
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(timestamp);
		} else {
			throw new IllegalArgumentException("Timestamp Presentation mode has invalid value: '" + timestampPresentation + "'");
		}
	}
	
	public static int countChar(String str, char sch){
		int count = 0;
		for(char ch :str.toCharArray()){
			if(sch == ch){
				count++;
			}
		}
		return count;
	}
	
	public static void setSqlParameters(PreparedStatement pstmt, String sql, List<SQLParameter> sqlValuesOfParameters) throws SQLException {
		if(Integer.compare(countChar(sql, '?'), sqlValuesOfParameters.size()) != 0){
			throw new IllegalArgumentException("Count of provided parameters and count of needed parameters in sql query is not same.");
		}
		
		for(int i=1; i< sqlValuesOfParameters.size()+1;i++){
			
			int sqlType = sqlValuesOfParameters.get(i-1).getSqlType();
			Object valueWithOriginalType = sqlValuesOfParameters.get(i-1).getValue();
			
			Object value = convertTypeOfValueToTypeFromTable(valueWithOriginalType, sqlType);
			
			if(value == null) {
				pstmt.setObject(i, sqlType);
	        }else if(sqlType == Types.NULL) {
	        	pstmt.setObject(i, value);  
	        }else if(value instanceof String){
	        	pstmt.setString(i, (String)value);
	        }else if(value instanceof Integer){
	        	pstmt.setInt(i, (Integer)value);
	        }else if(value instanceof Boolean){
	        	pstmt.setBoolean(i, (Boolean)value);
	        }else if(value instanceof Double){
	        	pstmt.setDouble(i, (Double)value);
	        }else if(value instanceof Float){
	        	pstmt.setFloat(i, (Float)value);
	        }else if(value instanceof Long){
	        	pstmt.setLong(i, (Long)value);
	        }else if(value instanceof byte[]){
	        	pstmt.setBytes(i, (byte[])value);
	        }else if(value instanceof Timestamp){
	        	pstmt.setTimestamp(i, (Timestamp)value);
	        }else if(value instanceof Date){
	        	pstmt.setDate(i, (Date)value);
	        }else if(value instanceof Time){
	        	pstmt.setTime(i, (Time)value);
	        }else if(value instanceof Date){
	        	pstmt.setDate(i, (Date)value);
	        }else if(value instanceof BigDecimal){
	        	pstmt.setBigDecimal(i, (BigDecimal)value);
	        }else if(value instanceof BigInteger){
	        	pstmt.setLong(i, ((BigInteger)value).longValue());
	        }else if(value instanceof Byte){
	        	pstmt.setByte(i, (Byte)value);
	        }else if(value instanceof Blob){
	        	pstmt.setBlob(i, (Blob)value);
	        } else {
	        	pstmt.setObject(i, value);
	        }	
		}
	}
	
	private static Object convertTypeOfValueToTypeFromTable(Object value, int sqlType){
		if (value == null && (sqlType == Types.BIT || sqlType == Types.BOOLEAN)) {
			return Boolean.FALSE;
		} else if(value == null){
			return null;
		}
		if(sqlType == Types.DECIMAL || sqlType == Types.NUMERIC || sqlType == Types.DOUBLE){
			if(value instanceof BigDecimal || value instanceof Double || value instanceof Float){
				return value;
			} else if(value instanceof String){
				return Double.valueOf((String) value);
			} else {
				return Double.valueOf(value.toString());
			}
		} else if(sqlType == Types.FLOAT || sqlType == Types.REAL){
			if(value instanceof BigDecimal || value instanceof Double || value instanceof Float){
				return value;
			} else if(value instanceof String){
				return Float.valueOf((String) value);
			} else {
				return Float.valueOf(value.toString());
			}
		} else if(sqlType == Types.BIGINT){
			if(value instanceof BigInteger || value instanceof Long || value instanceof Integer){
				return value;
			} else if(value instanceof String){
				return Long.valueOf((String) value);
			} else {
				return Long.valueOf(value.toString());
			}
		} else if(sqlType == Types.TIMESTAMP){
			return stringOrLongToTimestamp(value);
		} else if(sqlType == Types.DATE){
			return stringOrLongToDate(value);
		} else if(sqlType == Types.TIME){
			return stringToTime(value);
		} else if(sqlType == Types.BIT || sqlType == Types.BOOLEAN && value instanceof String){
			return Boolean.valueOf((String)value);
		} else if(sqlType == Types.LONGVARCHAR || sqlType == Types.VARCHAR || sqlType == Types.CHAR){
			if( value instanceof String) {
				return value;
			} else {
				return value.toString();
			}
		}
		return value;
	}
	
	public static Timestamp stringOrLongToTimestamp(Object value){
		if(value instanceof String){
			Timestamp parsedTimestamp;
			try {
				parsedTimestamp = Timestamp.valueOf((String)value);
			} catch (IllegalArgumentException e) {
				try {
					java.util.Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse((String)value);
					parsedTimestamp = new Timestamp(date.getTime());
				} catch (ParseException ex) {
					throw new IllegalArgumentException("It is not possible to parse Timestamp from input string.");
				}
			}
			return parsedTimestamp;
		} else if(value instanceof Long){
			try {
				return new Timestamp((Long)value);
			} catch (NumberFormatException exp) {
				throw new IllegalArgumentException("It is not possible to create Timestamp from input value of type Long.");
			}
		} else {
			throw new IllegalArgumentException("Wrong type of input value. It is possible to create Timestamp from value of type Long or String");
		}
	}
	
	public static Time stringToTime(Object value){
		if(value instanceof String){
			Time parsedTime;
			try {
				parsedTime = Time.valueOf((String)value);
			} catch (IllegalArgumentException e) {
				try {
					java.util.Date date = new SimpleDateFormat("HH:mm:ss.SSSXXX").parse((String)value);
					parsedTime = new Time(date.getTime());
				} catch (ParseException ex) {
					throw new IllegalArgumentException("It is not possible to parse Time from input string.");
				}
			}
			return parsedTime;
		} else {
			throw new IllegalArgumentException("Wrong type of input value. It is possible to create Time from value of String type.");
		}
		
	}

	public static Date stringOrLongToDate(Object value){
		if(value instanceof String){
			Date parsedDate;
			try {
				parsedDate = Date.valueOf((String)value);
			} catch (IllegalArgumentException e) {
				try {
					java.util.Date date = new SimpleDateFormat("yyyy-MM-dd").parse((String)value);
					parsedDate = new Date(date.getTime());
				} catch (ParseException ex) {
					throw new IllegalArgumentException("It is not possible to parse Date from input string.");
				}
			}
			return parsedDate;
		} else if(value instanceof Long){
			try {
				return new Date((Long)value);
			} catch (NumberFormatException exp) {
				throw new IllegalArgumentException("It is not possible to create Date from input value of type Long.");
			}
		} else {
			throw new IllegalArgumentException("Wrong type of input value. It is possible to create Date from value of type Long or String");
		}
	}
	
}
