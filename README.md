# edge-dematic

Copyright (C) 2021 The Open Library Foundation

This software is distributed under the terms of the Apache License,
Version 2.0. See the file "[LICENSE](LICENSE)" for more information.

## Introduction
The purpose of this edge API is to bridge the gap between Dematic remote storage provider and FOLIO.

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
#### Dematic StagingDirector setup
1. Dematic StagingDirector connection should be established from the Dematic edge Folio module. Therefore Dematic edge module 
needs to know the name of all the tenants, which has StagingDirector connection. For the ephemeral configuration these names locate in the
`ephemeral.properties` (key `tenants`). In order to provide it before the deployment the list of tenant names (e.g. ids) should be put to AWS parameters store (as String). The tenant names list separated by 
coma (e.g. diku, someothertenantname) should be stored in AWS param store in the variable with 
key: `stagingDirector_tenants`. 
2. For each tenant using StagingDirector the corresponding user should be added 
to the AWS parameter store with key in the following format `stagingDirector_{{tenant}}_stagingDirector` (where salt and username are the same - `stagingDirector`) with value of corresponding `{{password}}` (as Secured String). 
This user should work as ordinary edge institutional user with the only one difference 
- his username and salt name are - stagingDirector.
3. User `stagingDirector` with password `{{password}}` and remote-storage.all permissions should be created on FOLIO. After that apikey can
be generated for making calls to Edge Dematic API.
   
#### Dematic EMS setup
The deployment information above is related only to Dematic StagingDirector edge user. For Dematic EMS another edge user (with corresponding API_KEY) should be created following the standard process for edge users creation. 

### Required Permissions
The following permissions should be granted to institutional users (as well as StagingDirectortenants) in order to use this edge API:
- `remote-storage.all`

### Configuration
Please refer to the [Configuration](https://github.com/folio-org/edge-common/blob/master/README.md#configuration) section in the [edge-common](https://github.com/folio-org/edge-common/blob/master/README.md) documentation to see all available system properties and their default values.

### Issue tracker
See project [EDGDEMATIC](https://issues.folio.org/browse/EDGDEMATIC)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker).

### Other documentation
Feature documentation [Remote Storage Integration](https://wiki.folio.org/display/DD/Remote+storages+integration).
Other [modules](https://dev.folio.org/source-code/#server-side) are described,
with further FOLIO Developer documentation at
[dev.folio.org](https://dev.folio.org/)
