/**
 * Copyright (c) 2016 Evolveum
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.polygon.rest;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.*;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Set;

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
        this.configuration = (C) configuration;
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

        if (configuration.getTrustAllCertificates()) {
            try {
                final SSLContext sslContext = new SSLContextBuilder()
                        .loadTrustMaterial(null, new org.apache.http.ssl.TrustStrategy() {
                            @Override
                            public boolean isTrusted(X509Certificate[] x509CertChain, String authType) throws CertificateException {
                                return true;
                            }
                        })
                        .build();
                httpClientBuilder.setSSLContext(sslContext);
                httpClientBuilder.setConnectionManager(
                        new PoolingHttpClientConnectionManager(
                                RegistryBuilder.<ConnectionSocketFactory>create()
                                        .register("http", PlainConnectionSocketFactory.INSTANCE)
                                        .register("https", new SSLConnectionSocketFactory(sslContext,
                                                NoopHostnameVerifier.INSTANCE))
                                        .build()
                        ));
            } catch (Exception e) {
                throw new ConnectorIOException(e.getMessage(), e);
            }
        }

        CloseableHttpClient httpClient = httpClientBuilder.build();
        return httpClient;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Executes the request using the HTTP client.
     */
    public HttpResponse execute(HttpUriRequest request) {
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
    public void processResponseErrors(HttpResponse response) {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode >= 200 && statusCode <= 299) {
            return;
        }
        String message = "HTTP error " + statusCode + " " + response.getStatusLine().getReasonPhrase();
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
            throw new UnsupportedOperationException("Sorry, no cofee: " + message);
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

    protected String getStringAttr(Set<Attribute> attributes, String attrName) throws InvalidAttributeValueException {
        return getAttr(attributes, attrName, String.class);
    }

    protected String getStringAttr(Set<Attribute> attributes, String attrName, String defaultVal) throws InvalidAttributeValueException {
        return getAttr(attributes, attrName, String.class, defaultVal);
    }

    protected String getStringAttr(Set<Attribute> attributes, String attrName, String defaultVal, String defaultVal2, boolean notNull) throws InvalidAttributeValueException {
        String ret = getAttr(attributes, attrName, String.class, defaultVal);
        if (notNull && ret == null) {
            if (notNull && defaultVal == null)
                return defaultVal2;
            return defaultVal;
        }
        return ret;
    }

    protected String getStringAttr(Set<Attribute> attributes, String attrName, String defaultVal, boolean notNull) throws InvalidAttributeValueException {
        String ret = getAttr(attributes, attrName, String.class, defaultVal);
        if (notNull && ret == null)
            return defaultVal;
        return ret;
    }

    protected <T> T getAttr(Set<Attribute> attributes, String attrName, Class<T> type) throws InvalidAttributeValueException {
        return getAttr(attributes, attrName, type, null);
    }

    protected <T> T getAttr(Set<Attribute> attributes, String attrName, Class<T> type, T defaultVal, boolean notNull) throws InvalidAttributeValueException {
        T ret = getAttr(attributes, attrName, type, defaultVal);
        if (notNull && ret == null)
            return defaultVal;
        return ret;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttr(Set<Attribute> attributes, String attrName, Class<T> type, T defaultVal) throws InvalidAttributeValueException {
        for (Attribute attr : attributes) {
            if (attrName.equals(attr.getName())) {
                List<Object> vals = attr.getValue();
                if (vals == null || vals.isEmpty()) {
                    // set empty value
                    return null;
                }
                if (vals.size() == 1) {
                    Object val = vals.get(0);
                    if (val == null) {
                        // set empty value
                        return null;
                    }
                    if (type.isAssignableFrom(val.getClass())) {
                        return (T) val;
                    }
                    throw new InvalidAttributeValueException("Unsupported type " + val.getClass() + " for attribute " + attrName);
                }
                throw new InvalidAttributeValueException("More than one value for attribute " + attrName);
            }
        }
        // set default value when attrName not in changed attributes
        return defaultVal;
    }

    protected String[] getMultiValAttr(Set<Attribute> attributes, String attrName, String[] defaultVal) {
        for (Attribute attr : attributes) {
            if (attrName.equals(attr.getName())) {
                List<Object> vals = attr.getValue();
                if (vals == null || vals.isEmpty()) {
                    // set empty value
                    return new String[0];
                }
                String[] ret = new String[vals.size()];
                for (int i = 0; i < vals.size(); i++) {
                    Object valAsObject = vals.get(i);
                    if (valAsObject == null)
                        throw new InvalidAttributeValueException("Value " + null + " must be not null for attribute " + attrName);

                    String val = (String) valAsObject;
                    ret[i] = val;
                }
                return ret;
            }
        }
        // set default value when attrName not in changed attributes
        return defaultVal;
    }


    protected <T> T addAttr(ConnectorObjectBuilder builder, String attrName, T attrVal) {
        if (attrVal != null) {
            builder.addAttribute(attrName, attrVal);
        }
        return attrVal;
    }

}
