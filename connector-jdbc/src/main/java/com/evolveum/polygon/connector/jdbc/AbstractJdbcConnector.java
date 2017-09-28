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
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;

/**
 * @author Lukas Skublik
 *
 */
public class AbstractJdbcConnector<C extends AbstractJdbcConfiguration> implements Connector {

	private C configuration;
	private Connection connection;
	private Map<String, Integer> sqlTypes = new HashMap<String, Integer>();
	private List<String> namesOfRequiredColumns = new ArrayList<String>();
	
	private static final Log LOGGER = Log.getLog(AbstractJdbcConnector.class);

	
	@Override
	public void dispose() {
		try {
			if(connection != null && !connection.isClosed()){
				this.connection.close();
				this.connection = null;
			}
		} catch (SQLException ex) {
			LOGGER.error(ex.getMessage());
			if(rethrowSQLException(ex.getErrorCode())){
				throw new ConnectorException(ex.getMessage(), ex);
			}
		}
	}

	@Override
	public C getConfiguration() {
		return configuration;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init(Configuration configuration) {
		LOGGER.info("Initialize");
		this.connection = openConnection((C) configuration);
		this.configuration = (C) configuration;
	}
	
	/**
	 * @return the conn
	 */
	public Connection getConnection() {
		return connection;
	}
	
	/**
	 * @return the sqlTypes
	 */
	public Map<String, Integer> getSqlTypes() {
		return sqlTypes;
	}
	
	private Connection openConnection(C config){
		GuardedString password = config.getUserPassword();
		String username = config.getUsername();
		String[] prop = config.getJNDIProperties();
		Properties connectionProps = JdbcUtil.convertArrayToProperties(prop);
		String datasource = config.getDatasource();
		
		
		try {
		if(!datasource.equals("")){
			if(!username.equals("") && password != null){
				
				Context initialContext = new InitialContext(connectionProps);
				DataSource databaseConn = (DataSource) initialContext.lookup(datasource);
				return databaseConn.getConnection(username, convertPasswordAsClearText(password));
			} else {
				Context initialContext = new InitialContext();
				DataSource databaseConn = (DataSource) initialContext.lookup(datasource);
				return databaseConn.getConnection();
			}
		} else {
			String driver = config.getJdbcDriver();
			String jdbcUrl = config.getJdbcUrl();
			
			Class.forName(driver);
			if(!username.equals("")){
				return DriverManager.getConnection(jdbcUrl, username, convertPasswordAsClearText(password));
			} else {
				return DriverManager.getConnection(jdbcUrl);
			}
			
		}
		
		} catch (ClassNotFoundException e) {
			LOGGER.error("Selected JDBC Driver "+ config.getJdbcDriver() +" is not found on classpath. " + e);
		} catch (SQLException ex) {
			LOGGER.error(ex.getMessage());
			if(rethrowSQLException(ex.getErrorCode())){
				throw new ConnectorException(ex.getMessage(), ex);
			}
		} catch (NamingException e) {
			LOGGER.error("It was not possible constructs an initial context using the supplied environment. " + e);
		}
		return null;
	}
	
	private String convertPasswordAsClearText(GuardedString pass){
		final StringBuilder sbPass = new StringBuilder();
		pass.access(new GuardedString.Accessor() {
			@Override
			public void access(char[] chars) {
				sbPass.append(new String(chars));
			}
		});
		return sbPass.toString();
	}
	
	public List<List<Attribute>> executeQueryOnTable(String sql, List<SQLParameter> sqlValuesOfParameters){
		return execute(sql, sqlValuesOfParameters, true);
	}
	
	public List<List<Attribute>> executeQueryOnTable(String sql){
		return execute(sql, null, true);
	}
	
	public void executeUpdateOnTable(String sql, List<SQLParameter> sqlValuesOfParameters){
		execute(sql, sqlValuesOfParameters, false);
	}
	
	public void executeUpdateOnTable(String sql){
		execute(sql, null, false);
	}
	
	private List<List<Attribute>> execute(String sql, List<SQLParameter> sqlValuesOfParameters, boolean queryOrUpdate){
		LOGGER.info("Execute qsl request: {0}", sql);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<List<Attribute>> ret = new ArrayList<List<Attribute>>();
		
		try {
			pstmt = getConnection().prepareStatement(sql);
			if(sql.contains("?") && sqlValuesOfParameters != null){
				setSqlParameters(pstmt, sql, sqlValuesOfParameters);
			}
			if(queryOrUpdate){
				rs = pstmt.executeQuery();
				ret = processingResult(rs);
			} else {
				pstmt.executeUpdate();
			}
			
		} catch (SQLException ex) {
			LOGGER.error(ex.getMessage());
			if(rethrowSQLException(ex.getErrorCode())){
				throw new ConnectorException(ex.getMessage(), ex);
			}
		} finally{
			try {
				if(rs!=null){
		            rs.close();
				}
				if(pstmt!=null){
		            pstmt.close();
				}
			} catch (SQLException ex) {
				LOGGER.error(ex.getMessage());
				if(rethrowSQLException(ex.getErrorCode())){
					throw new ConnectorException(ex.getMessage(), ex);
				}
			}
		}
		return ret;
	}
	
	private List<List<Attribute>> processingResult(ResultSet rs) {
		
		List<List<Attribute>> ret = new ArrayList<List<Attribute>>();
		
		try {
			
			while(rs.next()){
				int i = 1;
				List<Attribute> oneRow = new ArrayList<Attribute>();
				while(i <= rs.getMetaData().getColumnCount()){
					AttributeBuilder attrB = new AttributeBuilder();
					ResultSetMetaData metaData = rs.getMetaData();
					attrB.setName(metaData.getColumnName(i).toLowerCase());
					Object value = null;
					int type = metaData.getColumnType(i);
					if(!getConfiguration().isAllNative()){
						if(getConfiguration().isNativeTimestamps() && Types.TIMESTAMP == type){
							value = JdbcUtil.getValueOfColumn(Types.TIMESTAMP, i, rs);
						} else if(JdbcUtil.getTypeOfAttribute(type).isAssignableFrom(String.class)){
							value = JdbcUtil.getValueOfColumn(Types.VARCHAR, i, rs);
						} else {
							value = JdbcUtil.getValueOfColumn(type, i, rs);
						}
					} else { 
						value = JdbcUtil.getValueOfColumn(type, i, rs);
					}
					attrB.addValue(value);
					oneRow.add(attrB.build());
					i++;
				}
				ret.add(oneRow);
			}
		} catch (SQLException ex) {
			LOGGER.error(ex.getMessage());
			if(rethrowSQLException(ex.getErrorCode())){
				throw new ConnectorException(ex.getMessage(), ex);
			}
		}
		return ret;
	}

	private int countChar(String str, char sch){
		int count = 0;
		for(char ch :str.toCharArray()){
			if(sch == ch){
				count++;
			}
		}
		return count;
	}
	
	private void setSqlParameters(PreparedStatement pstmt, String sql, List<SQLParameter> sqlValuesOfParameters) throws SQLException {
		if(Integer.compare(countChar(sql, '?'), sqlValuesOfParameters.size()) != 0){
			throw new IllegalArgumentException("Count of provided parameters and count of needed parameters in sql query is not same.");
		}
		
		for(int i=1; i< sqlValuesOfParameters.size()+1;i++){
			
			int sqlType = sqlValuesOfParameters.get(i-1).getSqlType();
			Object value = sqlValuesOfParameters.get(i-1).getValue();
			
			
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
	
	public Set<AttributeInfo> buildAttributeInfosFromTable(String nameOfTable, String keyNameOfTable, List<String> excludedNames) {
		this.namesOfRequiredColumns.clear();
		this.sqlTypes.clear();
		if (nameOfTable == null) {
			LOGGER.error("Attribute nameOfTable not provided.");
			throw new InvalidAttributeValueException("Attribute nameOfTable not provided.");
		}
		if (keyNameOfTable == null) {
			LOGGER.error("Attribute keyNameOfTable not provided.");
			throw new InvalidAttributeValueException("Attribute keyNameOfTable not provided.");
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ").append(nameOfTable).append(" WHERE ").append(keyNameOfTable).append(" IS NULL");
		String sql = sb.toString();
		
		ResultSet result = null;
		Statement stmt = null;
		try {
			stmt = getConnection().createStatement();

			result = stmt.executeQuery(sql);
			Set<AttributeInfo> attrsInfo = new HashSet<AttributeInfo>();
			ResultSetMetaData metaData = result.getMetaData();
			int countOfAttributes = metaData.getColumnCount();
			for (int i = 1; i <= countOfAttributes; i++) {
				final String nameOfColumn = metaData.getColumnName(i);
				final AttributeInfoBuilder attrInfoBuilder = new AttributeInfoBuilder();
				final Integer numberOfType = metaData.getColumnType(i);
				this.sqlTypes.put(nameOfColumn.toLowerCase(), numberOfType);
				if (excludedNames == null || !excludedNames.contains(nameOfColumn)) {
					
					Class<?> type = JdbcUtil.getTypeOfAttribute(numberOfType);
					attrInfoBuilder.setName(nameOfColumn.toLowerCase());
					attrInfoBuilder.setType(type);
					boolean required = isRequired(metaData, i);
					if(required && type.equals(String.class)){
						this.namesOfRequiredColumns.add(nameOfColumn.toLowerCase());
					}
					attrInfoBuilder.setRequired(required);
					attrInfoBuilder.setMultiValued(isMultivalue(metaData, i));
					attrsInfo.add(attrInfoBuilder.build());
				}
			}
			return attrsInfo;
		} catch (SQLException ex) {
			LOGGER.error(ex.getMessage());
			if(rethrowSQLException(ex.getErrorCode())){
				throw new ConnectorException(ex.getMessage(), ex);
			}
		}
		return null;
	}
	
	protected boolean isMultivalue(ResultSetMetaData metaData, int i) { 
		return false; 
	}
	
	protected boolean isRequired(ResultSetMetaData metaData, int i){
		try {
			return metaData.isNullable(i)==ResultSetMetaData.columnNoNulls;
		} catch (SQLException ex) {
			LOGGER.error(ex.getMessage());
			if(rethrowSQLException(ex.getErrorCode())){
				throw new ConnectorException(ex.getMessage(), ex);
			}
		}
		return false; 
		}
	
	public List<String> getNamesOfRequiredColumns() {
		return namesOfRequiredColumns;
	}
	
	public boolean rethrowSQLException(int eCode) {
		if(eCode !=0 || getConfiguration().isRethrowAllSQLExceptions()){
			return true;
		}
		return false;
    }
	
}
