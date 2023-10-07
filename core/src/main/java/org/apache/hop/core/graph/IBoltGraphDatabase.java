/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.core.graph;

import java.util.List;
import java.util.Map;

public interface IBoltGraphDatabase extends IGraphDatabase{

    String getNeo4jVersion();

    void setNeo4jVersion(String neo4jVersion);

    String getBrowserPort();

    void setBrowserPort(String browserPort);


    /** Set default options for all graph databases **/
    default void addDefaultOptions(){}

    /** @return true if the database supports a boolean, bit, logical, ... datatype */
    boolean isSupportsBooleanDataType();

    /** @param b Set to true if the database supports a boolean, bit, logical, ... datatype */
    void setSupportsBooleanDataType(boolean b);

    /** @return A manually entered URL which will be used over the internally generated one */
    List<String> getManualUrls();

    /**
     * @param manualUrl A manually entered URL which will be used over the internally generated one
     */

    void setManualUrls(List<String> manualUrl);

    String getDefaultBoltPort();

    void setDefaultBoltPort(String defaultBoltPort);

    String getBoltPort();

    void setBoltPort(String boltPort);

    boolean isRouting();

    void setRouting(boolean routing);

    String getRoutingVariable();

    void setRoutingVariable(String routingVariable);

    String getRoutingPolicy();

    void setRoutingPolicy(String routingPolicy);

    boolean isUsingEncryption();

    void setUsingEncryption(boolean usingEncryption);

    String getUsingEncryptionVariable();

    void setUsingEncryptionVariable(String usingEncryptionVariable);

    boolean isTrustAllCertificates();

    void setTrustAllCertificates(boolean trustAllCertificates);

    String getTrustAllCertificatesVariable();

    void setTrustAllCertificatesVariable(String trustAllCertificatesVariable);

    String getConnectionLivenessCheckTimeout();

    void setConnectionLivenessCheckTimeout(String connectionLivenessCheckTimeout);

    String getMaxConnectionLifetime();

    void setMaxConnectionLifetime(String maxconnectionLifetime);

    String getMaxConnectionPoolSize();

    void setMaxConnectionPoolSize(String maxConnectionPoolSize);

    String getConnectionAcquisitionTimeout();

    void setConnectionAcquisitionTimeout(String connectionAcquisitionTimeout);

    String getConnectionTimeout();

    void setConnectionTimeout(String connectionTimeout);

    String getMaxTransactionRetryTime();

    void setMaxTransactionRetryTime(String maxTransactionRetryTime);

    boolean isVersion4();

    void setVersion4(boolean version4);

    String getVersion4Variable();

    void setVersion4Variable(String version4Variable);

    boolean isAutomatic();

    void setAutomatic(boolean automatic);

    String getAutomaticVariable();

    void setAutomaticVariable(String automaticVariable);

    String getProtocol();

    void setProtocol(String protocol);

    Map<String, String> getAttributes();

    void setAttributes(Map<String, String> attributes);

    /**
     * @return true if the database supports range indexes.
     */
    boolean isSupportsRangeIndex();

    /**
     * @return true if the database supports lookup indexes
     */
    boolean isSupportsLookupIndex();

    /**
     * @return true if the database supports text indexes
     */
    boolean isSupportsTextIndex();

    /**
     * @return true if the database supports point indexes
    */
    boolean isSupportsPointIndex();

    /**
     * @return true if the database supports full text indexes
    */
    boolean isSupportsFullTextIndex();

    /**
     * @return true if the database supports BTree indexes
    */
    boolean isSupportsBTreeIndex();


}
