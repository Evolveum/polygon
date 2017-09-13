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

/**
 * @author Lukas Skublik
 *
 */


import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;
import org.identityconnectors.framework.spi.operations.SyncOp;

public class AbstractJdbcConfiguration extends AbstractConfiguration {

	static Log LOGGER = Log.getLog(AbstractJdbcConfiguration.class);
	
	public static final String DEFAULT_DRIVER = "oracle.jdbc.driver.OracleDriver";
	public static final String DEFAULT_URL = "jdbc:oracle:thin:@%H:%P:%D";
	public static final String EMPTY_STRING = "";
	
	private String host = EMPTY_STRING;
	private String port = EMPTY_STRING;
	private String nameOfDatabase = EMPTY_STRING;
	private String username = EMPTY_STRING;
	private GuardedString UserPassword;
	private String jdbcDriver = DEFAULT_DRIVER;
	private String usedJdbcUrl = DEFAULT_URL;
	private String quoting = EMPTY_STRING;
	private boolean enableEmptyStr = false;
	private boolean nativeTimestamps = false;
	private boolean allNative = false;
	private boolean rethrowAllSQLExceptions = true;
	private boolean suppressPass = true;
	private String validConnectionQuery;
	private String changeLogColumn = EMPTY_STRING;
	private String datasource = EMPTY_STRING;
	private String[] JNDIProperties;
	
	
	@ConfigurationProperty(order = 1, displayMessageKey = "HOST_DISPLAY_NAME", helpMessageKey = "HOST_HELP")
	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}
    
	@ConfigurationProperty(order = 2, displayMessageKey = "PORT_DISPLAY_NAME", helpMessageKey = "PORT_HELP")
	public String getPort() {
		return this.port;
	}

	public void setPort(String port) {
		this.port = port;
	}
    
	@ConfigurationProperty(order = 3, displayMessageKey = "NAME_OF_DATABASE_DISPLAY_NAME", helpMessageKey = "NAME_OF_DATABASE_HELP")
	public String getNameOfDatabase() {
		return this.nameOfDatabase;
	}
	
	public void setNameOfDatabase(String nameOfDatabase) {
		this.nameOfDatabase = nameOfDatabase;
	}
    
	@ConfigurationProperty(order = 4, displayMessageKey = "USERNAME_DISPLAY_NAME", helpMessageKey = "USERNAME_HELP")
	public String getUsername() {
		return this.username;
	}
	
	public void setUsername(String name) {
		this.username = name;
	}
	
	@ConfigurationProperty ( order=5, confidential=true, displayMessageKey = "USER_PASSWORD_DISPLAY_NAME", helpMessageKey = "USER_PASSWORD_HELP")
	public GuardedString getUserPassword() {
		return this.UserPassword;
	}

	public void setUserPassword(GuardedString password) {
		this.UserPassword = password;
	}
	
	@ConfigurationProperty(order = 6, displayMessageKey = "JDBC_DRIVER_DISPLAY_NAME", helpMessageKey = "JDBC_DRIVER_HELP")
	public String getJdbcDriver() {
		return this.jdbcDriver;
	}

	public void setJdbcDriver(String usedJdbcDriver) {
		this.jdbcDriver = usedJdbcDriver;
	}
	
	@ConfigurationProperty(order = 7, displayMessageKey = "URL_DISPLAY_NAME", helpMessageKey = "URL_HELP")
	public String getJdbcUrl() {
		return usedJdbcUrl;
	}
	
	public void setJdbcUrl(String jdbcUrl) {
		this.usedJdbcUrl = jdbcUrl;
	}

	@ConfigurationProperty(order = 8, displayMessageKey = "QUOTING_DISPLAY_NAME", helpMessageKey = "QUOTING_HELP")
	public String getQuoting() {
		return this.quoting;
	}

	public void setQuoting(String quoting) {
		this.quoting = quoting;
	}

	@ConfigurationProperty(order = 9,  displayMessageKey = "ENABLE_EMPTY_STRING_DISPLAY_NAME",  helpMessageKey = "ENABLE_EMPTY_STRING_HELP")
	public boolean isEnableEmptyStr() {
		return enableEmptyStr;
	}

	public void setEnableEmptyStr(boolean enableEmptyStr) {
		this.enableEmptyStr = enableEmptyStr;
	}
	
	@ConfigurationProperty(order = 10, displayMessageKey = "NATIVE_TIMESTAMPS_DISPLAY_NAME", helpMessageKey = "NATIVE_TIMESTAMPS_HELP")
	public boolean isNativeTimestamps() {
		return nativeTimestamps;
	}

	public void setNativeTimestamps(boolean nativeTimestamps) {
		this.nativeTimestamps = nativeTimestamps;
	}
	
	@ConfigurationProperty(order = 11, displayMessageKey = "ALL_NATIVE_DISPLAY_NAME", helpMessageKey = "ALL_NATIVE_HELP")
	public boolean isAllNative() {
		return allNative;
	}
	
	public void setAllNative(boolean allNative) {
		this.allNative = allNative;
	}

	@ConfigurationProperty(order = 12, displayMessageKey = "RETHROW_ALL_SQLEXCEPTIONS_DISPLAY_NAME", helpMessageKey = "RETHROW_ALL_SQLEXCEPTIONS_HELP")
	public boolean isRethrowAllSQLExceptions() {
		return rethrowAllSQLExceptions;
	}

	public void setRethrowAllSQLExceptions(boolean rethrowAllSQLExceptions) {
		this.rethrowAllSQLExceptions = rethrowAllSQLExceptions;
	}
	
	@ConfigurationProperty(order = 13, displayMessageKey = "VALID_CONNECTION_QUERY_DISPLAY_NAME", helpMessageKey = "VALID_CONNECTION_QUERY_HELP")
	public String getValidConnectionQuery() {
		return this.validConnectionQuery;
	}

	public void setValidConnectionQuery(String value) {
		this.validConnectionQuery = value;
	}

	@ConfigurationProperty(order = 14, operations = SyncOp.class, displayMessageKey = "CHANGE_LOG_COLUMN_DISPLAY_NAME", helpMessageKey = "CHANGE_LOG_COLUMN_HELP")
	public String getChangeLogColumn() {
		return this.changeLogColumn;
	}

	public void setChangeLogColumn(String value) {
		this.changeLogColumn = value;
	}
    
	@ConfigurationProperty(order = 15, displayMessageKey = "SUPRESS_PASSWORD_DISPLAY_NAME", helpMessageKey = "SUPRESS_PASSWORD_HELP")
	public boolean getSuppressPassword() {
		return suppressPass;
		}
	
	public void setSuppressPassword(boolean suppressPass) {
		this.suppressPass = suppressPass;
	}
	
	@ConfigurationProperty(order = 16, displayMessageKey = "DATASOURCE_DISPLAY_NAME",  helpMessageKey = "DATASOURCE_HELP")
	public String getDatasource() {
		return datasource;
	}

	public void setDatasource(String datasource) {
		this.datasource = datasource;
	}
	
	@ConfigurationProperty(order = 17, displayMessageKey = "JNDI_PROPERTIES_DISPLAY_NAME", helpMessageKey = "JNDI_PROPERTIES_HELP")
	public String[] getJNDIProperties() {
		return JNDIProperties;
	}

	public void setJNDIProperties(String[] JNDIProperties) {
		this.JNDIProperties = JNDIProperties;
	}
	
	@Override
	public void validate() {
		LOGGER.info("Validate Configuration.");
		if (StringUtil.isBlank(getJdbcUrl())) {
			throw new IllegalArgumentException("Configuration parameter 'JDBC Connection URL' is not provided.");
		}
		if(StringUtil.isBlank(getDatasource())){
			
			if (getJdbcUrl().contains("%H") && StringUtil.isBlank(getHost())) {
				throw new IllegalArgumentException("Configuration parameter 'Host' is not provided.");
			}
			if(getJdbcUrl().contains("%P") && StringUtil.isBlank(getPort())) {
				throw new IllegalArgumentException("Configuration parameter 'Port' is not provided.");
			}
			if(getJdbcUrl().contains("%D") && StringUtil.isBlank(getNameOfDatabase())) {
				throw new IllegalArgumentException("Configuration parameter 'Database' is not provided.");
			}
			
			if (getUsername() == null) {
				throw new IllegalArgumentException("Configuration parameter 'User' is not provided.");
			}
			if (getUserPassword() == null) {
				throw new IllegalArgumentException("Configuration parameter 'Password' is not provided.");
			}
			
			if (StringUtil.isBlank(getJdbcDriver())) {
				throw new IllegalArgumentException("Configuration parameter 'JDBC Driver' is not provided.");
			}
			try {
				Class.forName(getJdbcDriver());
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Selected JDBC Driver "+ getJdbcDriver() +" is not found on classpath.");
			}
		} else {
			JdbcUtil.convertArrayToProperties(getJNDIProperties());
		}
		
		String quoting = getQuoting();
		if(!(quoting.equalsIgnoreCase("NONE") || quoting.equalsIgnoreCase("SINGLE") || quoting.equalsIgnoreCase("DOUBLE")
				|| quoting.equalsIgnoreCase("BACK") || quoting.equalsIgnoreCase("BRACKETS"))){
			throw new IllegalArgumentException("Quoting ''"+quoting+"'' has invalid structure.");
		}
		LOGGER.ok("Configuration is valid.");
	}
    
	public String formatingJdbcUrl() {
		LOGGER.info("Starting of method formatingJdbcUrl.");
		String JdbcUrl = getJdbcUrl();
		int lengthOfUrl = JdbcUrl.length();
		StringBuilder ret = new StringBuilder();
		for(int i=0; i<lengthOfUrl; i++) {
			char ch = JdbcUrl.charAt(i);
			if (ch!='%'){
				ret.append(ch);
			} else if (i + 1 < lengthOfUrl) {
				i++;
				ch = JdbcUrl.charAt(i);
				switch(ch){
					case'%':{
						ret.append(ch);
						break;
					}
					case'H':{
						ret.append(getHost());
						break;
					}
					case'P':{
						ret.append(getPort());
						break;
					}
					case'D':{
						ret.append(getNameOfDatabase());
						break;
					}
				}
			}
		}
		String formattedJdbcURL = ret.toString();
		StringBuilder OkMsg = new StringBuilder("JdbcUrl was formated to ");
		OkMsg.append(formattedJdbcURL).append(".");
		LOGGER.ok(OkMsg.toString());
		return formattedJdbcURL;
	}
}