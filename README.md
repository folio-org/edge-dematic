# edge-dematic

Copyright (C) 2021-2023 The Open Library Foundation

This software is distributed under the terms of the Apache License,
Version 2.0. See the file "[LICENSE](LICENSE)" for more information.

## Introduction
The purpose of this edge API is to bridge the gap between Dematic remote storage provider and FOLIO.

There are two modules involved in Dematic workflow: mod-remote-storage, which interacts with other Folio modules and edge-dematic, which acts as a gate between Dematic and Folio:

`FOLIO <–> mod-remote-storage <–> edge-dematic <–> Dematic`

Edge-dematic supports two separate ways of communication 
* HTTP endpoints for Dematic EMS 
* TCP/IP sockets for Dematic StagingDirector. 

Dematic EMS interacts with Folio via HTTP endpoints (see [API Details](#api-details)). All flows – accession, retrieve and return – are initiated by Dematic. For each flow Dematic EMS polls appropriate edge-dematic endpoint. Each request must contain apikey as a query parameter: `/asrService/lookupNewASRItems/aaa-bbb-ccc?apikey=someApiKey`

Dematic StagingDirector (hereinafter SD) requires two TCP/IP sockets (channels):
* primary channel for sending requests to SD
* status channel for responses from SD 

Both connections initiated and maintained on Folio side by `edge-dematic` module. 

Accession and retrieval flows initiated by Folio – at the configurable interval edge-dematic checks two queues: accession queue and retrieval queue (filled by `mod-remote-storage` when circulation events occur). If new records are present, module sends requests to SD via primary channel. SD then sends responses or, in case of item return, a return message via status channel.

## Additional information

### API Details
API provides the following URLs for working with remote storage configurations:

| Method | URL| Description | 
|---|---|---|
| GET | /asrService/asr/lookupNewAsrItems/{remoteStorageConfigurationId}  | The polling API for accessions |
| GET | /asrService/asr/updateASRItemStatusBeingRetrieved/{remoteStorageConfigurationId} | The polling API for retrievals |
| GET | /asrService/asr//updateASRItemStatusBeingRetrieved/{remoteStorageConfigurationId} | The API for retrieve |
| POST | /asrService/asr/updateASRItemStatusAvailable/{remoteStorageConfigurationId} | The API for return |

### Deployment information

#### Rancher
1. Check that mod-remote-storage has been installed and has been registered to okapi.
2. Create a new user named `stagingDirector` in FOLIO. You may also use `diku_admin` for testing and avoid this step.
3. Create a secret in the rancher cluster. Make the key of this secret `ephemeral.properties` and the value similar to `secureStore.type=Ephemeral tenants=diku diku=diku_admin,admin,stagingDirector`.
4. Add this secret as a volume mount to the workload for the edge module container. Set the mount point of this volume to `\etc\edge`.
5. Set the `JAVA_OPTIONS` environment variable for the workload to something similar to `-Dsecure_store_props=/etc/edge/ephemeral.properties -Dokapi_url=http://okapi:9130 -Dlog_level=DEBUG -Dstaging_director_client=diku_admin`. 
6. Redeploy the container. This will make the container aware of the new secret and volume mount.

##### Other rancher considerations
If you are deploying using a FOLIO helm chart, you may want to take adavantage of overriding the chart's yml with answer keys and values to enable the ingress. Here is an example:

| Key | Value |
|---|---|
|ingress.annotations.external-dns\.alpha\.kubernetes\.io/target|f2b6996c-kubesystem-albing-accc-1096161577.us-west-2.elb.amazonaws.com|
|ingress.enabled|true|
|ingress.hosts[0].host|core-platform-edge-orders.ci.folio.org|
|ingress.hosts[0].paths[0]|/|

#### Dematic StagingDirector setup
1. Dematic StagingDirector connection should be established from the Dematic edge Folio module. Therefore Dematic edge module needs to know the name of all the tenants, which has StagingDirector connection. For the ephemeral configuration these names locate in the `ephemeral.properties` (key `tenants`). In order to provide it before the deployment the list of tenant names (e.g. ids) should be put to AWS parameters store (as String). The tenant names list separated by coma (e.g. diku, someothertenantname) should be stored in AWS param store in the variable with key: `stagingDirector_tenants` by default or could be provided its own key through `staging_director_tenants` parameter of starting module.
2. For each tenant using StagingDirector the corresponding user should be added to the AWS parameter store with key in the following format `{{username}}_{{tenant}}_{{username}}` (where salt and username are the same - `{{username}}`) with value of corresponding `{{password}}` (as Secured String). This user should work as ordinary edge institutional user with the only one difference - his username and salt name are - `{{username}}`. By default the value of `{{username}}` is `stagingDirector`. It could be changed through `staging_director_client` parameter of starting module.
3. User `{{username}}` with password `{{password}}` and remote-storage.all permissions should be created on FOLIO. After that apikey can be generated for making calls to Edge Dematic API.

##### Create Dematic StagingDirector configuration
1. Log in to Folio, go to "Settings" -> "Remote storage" -> "Configurations", click "New" button.
2. Enter General information settings:
* Select "Dematic StagingDirector" in Provider name box
* Enter Remote storage name
* Enter IP address and port in URL (for primary channel) and Status URL (for status channel). Address and port separated by colon (no whitespaces or other symbols), for example `192.168.1.1:1234`.
3. Set Data synchronization schedule. This setting defines timeframe to scan accession and retrieval queues and data exchange with provider.
4. Click "Save & close" button.

*Note: Dematic StagingDirector configuration settings applied only upon module startup, so in case of their changes, edge-dematic service must be restarted.*   

#### Dematic EMS setup
The deployment information above is related only to Dematic StagingDirector edge user. For Dematic EMS another edge user (with corresponding API_KEY) should be created following the standard process for edge users creation.

##### Create Dematic EMS configuration
1. Log in to Folio, go to "Settings" -> "Remote storage" -> "Configurations", click "New" button.
2. Enter General information settings:
* Select "Dematic EMS" in Provider name box
* Enter Remote storage name
3. Click "Save & close" button.

*Note: Since Dematic EMS flows initiated on provider side, all other settings can be omitted.*

### Required Permissions
The following permissions should be granted to institutional users (as well as StagingDirectortenants) in order to use this edge API: `remote-storage.all`.

### Configuration
Please refer to the [Configuration](https://github.com/folio-org/edge-common/blob/master/README.md#configuration) section in the [edge-common](https://github.com/folio-org/edge-common/blob/master/README.md) documentation to see all available system properties and their default values.

### TLS Configuration for HTTP Endpoints

To configure Transport Layer Security (TLS) for HTTP endpoints in edge module, the following configuration parameters can be used. These parameters allow you to specify key and keystore details necessary for setting up TLS.

#### Configuration Parameters

1. **`spring.ssl.bundle.jks.web-server.key.password`**
  - **Description**: Specifies the password for the private key in the keystore.
  - **Example**: `spring.ssl.bundle.jks.web-server.key.password=SecretPassword`

2. **`spring.ssl.bundle.jks.web-server.key.alias`**
  - **Description**: Specifies the alias of the key within the keystore.
  - **Example**: `spring.ssl.bundle.jks.web-server.key.alias=localhost`

3. **`spring.ssl.bundle.jks.web-server.keystore.location`**
  - **Description**: Specifies the location of the keystore file in the local file system.
  - **Example**: `spring.ssl.bundle.jks.web-server.keystore.location=/some/secure/path/test.keystore.bcfks`

4. **`spring.ssl.bundle.jks.web-server.keystore.password`**
  - **Description**: Specifies the password for the keystore.
  - **Example**: `spring.ssl.bundle.jks.web-server.keystore.password=SecretPassword`

5. **`spring.ssl.bundle.jks.web-server.keystore.type`**
  - **Description**: Specifies the type of the keystore. Common types include `JKS`, `PKCS12`, and `BCFKS`.
  - **Example**: `spring.ssl.bundle.jks.web-server.keystore.type=BCFKS`

6. **`server.ssl.bundle`**
  - **Description**: Specifies which SSL bundle to use for configuring the server. This parameter links to the defined SSL bundle, for example, `web-server`.
  - **Example**: `server.ssl.bundle=web-server`

7. **`server.port`**
  - **Description**: Specifies the port on which the server will listen for HTTPS requests.
  - **Example**: `server.port=8443`

#### Example Configuration

To enable TLS for the edge module using the above parameters, you need to provide them as the environment variables. Below is an example configuration:

```properties
spring.ssl.bundle.jks.web-server.key.password=SecretPassword
spring.ssl.bundle.jks.web-server.key.alias=localhost
spring.ssl.bundle.jks.web-server.keystore.location=classpath:test/test.keystore.bcfks
spring.ssl.bundle.jks.web-server.keystore.password=SecretPassword
spring.ssl.bundle.jks.web-server.keystore.type=BCFKS

server.ssl.bundle=web-server
server.port=8443
```
Also, you can use the relaxed binding with the upper case format, which is recommended when using system environment variables.
```properties
SPRING_SSL_BUNDLE_JKS_WEBSERVER_KEY_PASSWORD=SecretPassword
SPRING_SSL_BUNDLE_JKS_WEBSERVER_KEY_ALIAS=localhost
SPRING_SSL_BUNDLE_JKS_WEBSERVER_KEYSTORE_LOCATION=classpath:test/test.keystore.bcfks
SPRING_SSL_BUNDLE_JKS_WEBSERVER_KEYSTORE_PASSWORD=SecretPassword
SPRING_SSL_BUNDLE_JKS_WEBSERVER_KEYSTORE_TYPE=BCFKS

SERVER_SSL_BUNDLE=web-server
SERVER_PORT=8443
```

### TLS Configuration for Feign HTTP Clients

To configure Transport Layer Security (TLS) for HTTP clients created using Feign annotations in the edge module, you can use the following configuration parameters. These parameters allow you to specify trust store details necessary for setting up TLS for Feign clients.

#### Configuration Parameters

1. **`folio.client.okapiUrl`**
  - **Description**: Specifies the base URL for the Okapi service.
  - **Example**: `folio.client.okapiUrl=https://okapi:443`

2. **`folio.client.tls.enabled`**
  - **Description**: Enables or disables TLS for the Feign clients.
  - **Example**: `folio.client.tls.enabled=true`

3. **`folio.client.tls.trustStorePath`**
  - **Description**: Specifies the location of the trust store file.
  - **Example**: `folio.client.tls.trustStorePath=classpath:/some/secure/path/test.truststore.bcfks`

4. **`folio.client.tls.trustStorePassword`**
  - **Description**: Specifies the password for the trust store.
  - **Example**: `folio.client.tls.trustStorePassword="SecretPassword"`

5. **`folio.client.tls.trustStoreType`**
  - **Description**: Specifies the type of the trust store. Common types include `JKS`, `PKCS12`, and `BCFKS`.
  - **Example**: `folio.client.tls.trustStoreType=bcfks`

#### Note
The `trustStorePath`, `trustStorePassword`, and `trustStoreType` parameters can be omitted if the server provides a public certificate.

#### Example Configuration

To enable TLS for Feign HTTP clients using the above parameters, you need to provide them as the environment variables. Below is an example configuration:

```properties
folio.client.okapiUrl=https://okapi:443
folio.client.tls.enabled=true
folio.client.tls.trustStorePath=classpath:test/test.truststore.bcfks
folio.client.tls.trustStorePassword=SecretPassword
folio.client.tls.trustStoreType=bcfks
```
Also, you can use the relaxed binding with the upper case format, which is recommended when using system environment variables.
```properties
FOLIO_CLIENT_OKAPIURL=https://okapi:443
FOLIO_CLIENT_TLS_ENABLED=true
FOLIO_CLIENT_TLS_TRUSTSTOREPATH=classpath:test/test.truststore.bcfks
FOLIO_CLIENT_TLS_TRUSTSTOREPASSWORD=SecretPassword
FOLIO_CLIENT_TLS_TRUSTSTORETYPE=bcfks
```

### System user

| Name                          | Default value | Description                                                                 |
|:------------------------------|:-------------:|:----------------------------------------------------------------------------|
| SYSTEM_USER_ENABLED           |     true      | Defines if system user must be created during service tenant initialization |
| SYSTEM_USER_NAME              |  system-user  | Username of system user                                                     |
| SYSTEM_USER_PASSWORD          |       -       | Internal user password                                                      |

### Issue tracker
See project [EDGDEMATIC](https://issues.folio.org/browse/EDGDEMATIC)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker).

### Other documentation
Feature documentation [Remote Storage Integration](https://wiki.folio.org/display/DD/Remote+storages+integration).
Other [modules](https://dev.folio.org/source-code/#server-side) are described,
with further FOLIO Developer documentation at
[dev.folio.org](https://dev.folio.org/)
