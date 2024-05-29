## 2.2.2 2024-05-29

* [EDGDEMATIC-99](https://issues.folio.org/browse/EDGDEMATIC-99) - Re release the fix.

## 2.2.1 2024-05-28

* [EDGDEMATIC-100](https://issues.folio.org/browse/EDGDEMATIC-100) - Enhance HTTP Endpoint Security with TLS and FIPS-140-2 Compliant Cryptography
* [EDGDEMATIC-99](https://issues.folio.org/browse/EDGDEMATIC-99) - Enhance RemoteStorageClient TLS Configuration for Secure Connections to OKAPI


## 2.2.0 2024-03-20

* [EDGDEMATIC-95](https://issues.folio.org/browse/EDGDEMATIC-95) - Spring Boot 3.1.6 fixing Denial of Service (DoS)
* [EDGDEMATIC-97] - Upgrade versions in pom for Q release.


## 2.1.1 2023-12-13

* [EDGDEMATIC-95](https://issues.folio.org/browse/EDGDEMATIC-95) - Spring Boot 3.1.6 fixing Denial of Service (DoS)

## 2.1.0 2023-10-12

* [EDGDEMATIC-85](https://issues.folio.org/browse/EDGDEMATIC-85) - Migrate to folio-spring-support v7.0.0
* [EDGDEMATIC-87](https://issues.folio.org/browse/EDGDEMATIC-87) - Enable API-related GitHub Workflows, replace those Jenkins stages
* [EDGDEMATIC-88](https://issues.folio.org/browse/EDGDEMATIC-88) - Edge-dematic module not started properly.
* [EDGDEMATIC-90](https://issues.folio.org/browse/EDGDEMATIC-90) - Release: edge-dematic
* [EDGDEMATIC-91](https://issues.folio.org/browse/EDGDEMATIC-91) - Implement RTR
* [EDGDEMATIC-89](https://issues.folio.org/browse/EDGDEMATIC-89) -Disruptions in communication between StagingDirector and FOLIO
* [EDGDEMATIC-94](https://issues.folio.org/browse/EDGDEMATIC-94) -Upgrade to Spring Boot 3.1 and folio-spring-support 7.2

## 2.0.0 2023-02-23

* [EDGDEMATIC-61](https://issues.folio.org/browse/EDGDEMATIC-61) - Logging improvement
* [EDGDEMATIC-77](https://issues.folio.org/browse/EDGDEMATIC-77) - Logging improvement - Configuration
* [EDGDEMATIC-78](https://issues.folio.org/browse/EDGDEMATIC-78) - Update to Java 17
* [EDGDEMATIC-79](https://issues.folio.org/browse/EDGDEMATIC-79) - Update the module to Spring boot v3.0.0 and identify issues
* [EDGDEMATIC-81](https://issues.folio.org/browse/EDGDEMATIC-81) - Failed accessioning due to missing pick request - Dematic
* [EDGDEMATIC-84](https://issues.folio.org/browse/EDGDEMATIC-84) -Build failing due to dependency issue


## 1.7.0 - Released
This release includes minor improvements and upgrades

[Full Changelog](https://github.com/folio-org/edge-dematic/compare/v1.6.1...v1.7.0)

### Bug Fixes
* [EDGDEMATIC-74](https://issues.folio.org/browse/EDGDEMATIC-74) - Investigate message needed to connect Dematic EMS via TCP/IP
* [EDGDEMATIC-73](https://issues.folio.org/browse/EDGDEMATIC-73) - edge-dematic: spring upgrade

## 1.6.1 - Released
This release includes bug fixes

### Bug Fixes
* [EDGDEMATIC-70](https://issues.folio.org/browse/EDGDEMATIC-70) - edge-common 4.4.1 fixing disabled SSL in Vert.x WebClient

[Full Changelog](https://github.com/folio-org/edge-dematic/compare/v1.6.0...v1.6.1)

## 1.6.0 - Released
This release includes minor improvements (libraries dependencies cleanup and update)

[Full Changelog](https://github.com/folio-org/edge-dematic/compare/v1.5.0...v1.6.0)

### Technical tasks
* [EDGDEMATIC-66](https://issues.folio.org/browse/EDGDEMATIC-66) - edge-dematic Spring 2.7 for Morning Glory R2 2022
* [EDGDEMATIC-60](https://issues.folio.org/browse/EDGDEMATIC-60) - edge-dematic: folio-spring-base v4.1.0 update

## 1.5.0 - Released
This release was focused on log4j vulnerability fix and minor improvements

[Full Changelog](https://github.com/folio-org/edge-dematic/compare/v1.3.0...v1.5.0)

### Technical tasks
* [EDGDEMATIC-58](https://issues.folio.org/browse/EDGDEMATIC-58) - edge-dematic: folio-spring-base v3 update
* [EDGDEMATIC-52](https://issues.folio.org/browse/EDGDEMATIC-52) - Kiwi R3 2021 - Log4j edge- modules 2.17.0 upgrade
* [EDGDEMATIC-47](https://issues.folio.org/browse/EDGDEMATIC-47) - Kiwi R3 2021 - Log4j vulnerability verification and correction

## 1.3.0 - Released
This release includes minor improvements for system user and GET request

[Full Changelog](https://github.com/folio-org/edge-dematic/compare/v1.2.0...v1.3.0)

### Stories
* [EDGDEMATIC-26](https://issues.folio.org/browse/EDGDEMATIC-26) - Improvement: Making system user configurable

### Bug Fixes
* [EDGDEMATIC-31](https://issues.folio.org/browse/EDGDEMATIC-31) - GET asrService/asr/lookupNewAsrItems doesn't retrieve new accession queue records


## 1.2.0 - Released
This release includes minor improvements on deployment process

[Full Changelog](https://github.com/folio-org/edge-dematic/compare/v1.1.0...v1.2.0)

### Stories
* [EDGDEMATIC-27](https://issues.folio.org/browse/EDGDEMATIC-27) - Update edge-dematic documentation
* [EDGDEMATIC-19](https://issues.folio.org/browse/EDGDEMATIC-19) - Make deployment parameters configurable

## 1.1.0 - Released
This release includes fix for remote configuration retrieving

[Full Changelog](https://github.com/folio-org/edge-dematic/compare/v1.0.3...v1.1.0)

### Bug Fixes
* [EDGDEMATIC-28](https://issues.folio.org/browse/EDGDEMATIC-28) - edge-dematic doesn't update remote storage configurations for Dematic StagingDirector

## 1.0.3 - Released
This release includes fix for AWS deployment issue

[Full Changelog](https://github.com/folio-org/edge-dematic/compare/v1.0.2...v1.0.3)

### Bug Fixes
* [EDGDEMATIC-20](https://issues.folio.org/browse/EDGDEMATIC-20) - edge-dematic fails during deployment time

## 1.0.2 - Released
This release includes documentation updating (README.md) with deployment instructions

[Full Changelog](https://github.com/folio-org/edge-dematic/compare/v1.0.1...v1.0.2)

## 1.0.1 Released
The focus of this release was to fix deployment issue

[Full Changelog](https://github.com/folio-org/edge-dematic/compare/v1.0.0...v1.0.1)

### Bug Fixes
* [EDGDEMATIC-16](https://issues.folio.org/browse/EDGDEMATIC-16) - Deployment issue: edge-dematic can't be configured for integration with AWS

## 1.0.0 - Released

The primary focus of this release was to implement backend logic for Dematic integration

### Stories
* [EDGDEMATIC-12](https://issues.folio.org/browse/EDGDEMATIC-12) - Health-check for edge-dematic 
* [EDGDEMATIC-8](https://issues.folio.org/browse/EDGDEMATIC-8) - Handle dematic status IN
* [EDGDEMATIC-5](https://issues.folio.org/browse/EDGDEMATIC-5) - Project setup 
* [EDGDEMATIC-2](https://issues.folio.org/browse/EDGDEMATIC-2) - Dematic Staging Director integration
* [EDGDEMATIC-1](https://issues.folio.org/browse/EDGDEMATIC-1) - Dematic EMS integration

