/**
 * Copyright (c) 2016 Evolveum
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
package com.evolveum.polygon.rest;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConnectionFailedException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.exceptions.OperationTimeoutException;
import org.identityconnectors.framework.common.exceptions.PermissionDeniedException;
import org.identityconnectors.framework.common.exceptions.PreconditionFailedException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;

/**
 * @author semancik
 *
 */
public abstract class AbstractRestConnector<C extends AbstractRestConfiguration> implements Connector {
	
	private static final Log LOG = Log.getLog(AbstractRestConnector.class);
	
	private C configuration;
	private CloseableHttpClient httpClient = null;
	
	public AbstractRestConnector() {
		super();
		LOG.info("Creating {0} connector instance {1}", this.getClass().getSimpleName(), this);
	}
	
	@Override
    public C getConfiguration() {
        return configuration;
    }

	@Override
	public void init(Configuration configuration) {
		LOG.info("Initializing {0} connector instance {1}", this.getClass().getSimpleName(), this);
        this.configuration = (C)configuration;
        this.httpClient = createHttpClient();
	}

	private CloseableHttpClient createHttpClient() {
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		
		URI serviceAddress = URI.create(configuration.getServiceAddress());
		final HttpHost httpHost = new HttpHost(serviceAddress.getHost(), 
				serviceAddress.getPort(), serviceAddress.getScheme());
		
		switch (AbstractRestConfiguration.AuthMethod.valueOf(getConfiguration().getAuthMethod())) {
			case BASIC:
				final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
				configuration.getPassword().access(new GuardedString.Accessor() {
					@Override
					public void access(char[] clearChars) {
						credentialsProvider.setCredentials(new AuthScope(httpHost.getHostName(), httpHost.getPort()),
								new UsernamePasswordCredentials(configuration.getUsername(), new String(clearChars)));
					}
				});
				httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
				break;
			
			case NONE:
                break;
                
            default:
            	throw new IllegalArgumentException("Unknown authentication method " + getConfiguration().getAuthMethod());
				
		}
		
		CloseableHttpClient httpClient = httpClientBuilder.build();
		return httpClient;
	}

	public CloseableHttpClient getHttpClient() {
		return httpClient;
	}
	
	/**
	 * Executes the request using the HTTP client.
	 */
	public CloseableHttpResponse execute(HttpUriRequest request) {
		try {
			return getHttpClient().execute(request);
		} catch (IOException e) {
			throw new ConnectorIOException(e.getMessage(), e);
		}
	}
	
	/**
	 * Returns URIBuilder that is pre-configured with the service address that
	 * is defined in the connector configuration.
	 */
	protected URIBuilder getURIBuilder() {
		URI serviceAddress = URI.create(configuration.getServiceAddress());
		URIBuilder uriBuilder = new URIBuilder();
		uriBuilder.setScheme(serviceAddress.getScheme());
		uriBuilder.setHost(serviceAddress.getHost());
		uriBuilder.setPort(serviceAddress.getPort());
		uriBuilder.setPath(serviceAddress.getPath());
		return uriBuilder;
	}
	

	/**
	 * Checks HTTP response for errors. If the response is an error then the method
	 * throws the ConnId exception that is the most appropriate match for the error.
	 */
	protected void processResponseErrors(HttpResponse response) {
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode >= 200 && statusCode <= 299) {
			return;
		}
		String message = "HTTP error "+statusCode+" "+response.getStatusLine().getReasonPhrase();
		LOG.error("{0}", message);
		if (statusCode == 400 || statusCode == 405 || statusCode == 406) {
			throw new ConnectorIOException(message);
		}
		if (statusCode == 401 || statusCode == 402 || statusCode == 403 || statusCode == 407) {
			throw new PermissionDeniedException(message);
		}
		if (statusCode == 404 || statusCode == 410) {
			throw new UnknownUidException(message);
		}
		if (statusCode == 408) {
			throw new OperationTimeoutException(message);
		}
		if (statusCode == 412) {
			throw new PreconditionFailedException(message);
		}
		if (statusCode == 418) {
			throw new UnsupportedOperationException("Sorry, no cofee: "+message);
		}
		// TODO: other codes
		throw new ConnectorException(message);
	}


	@Override
	public void dispose() {
		if (httpClient != null) {
			try {
				httpClient.close();
			} catch (IOException e) {
				LOG.error("Error closing HTTP client: {0}", e.getMessage(), e);
			}
		}
	}

}
