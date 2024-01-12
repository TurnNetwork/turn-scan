--
-- Copyright 2021 Apollo Authors
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

# Create Database
# ------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS ApolloConfigDB DEFAULT CHARACTER SET = utf8mb4;

Use ApolloConfigDB;

# Dump of table app
# ------------------------------------------------------------

DROP TABLE IF EXISTS `App`;

CREATE TABLE `App` (
                       `Id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT 'primary key',
                       `AppId` varchar(500) NOT NULL DEFAULT 'default' COMMENT 'AppID',
                       `Name` varchar(500) NOT NULL DEFAULT 'default' COMMENT 'Application name',
                       `OrgId` varchar(32) NOT NULL DEFAULT 'default' COMMENT 'DepartmentId',
                       `OrgName` varchar(64) NOT NULL DEFAULT 'default' COMMENT 'Department name',
                       `OwnerName` varchar(500) NOT NULL DEFAULT 'default' COMMENT 'ownerName',
                       `OwnerEmail` varchar(500) NOT NULL DEFAULT 'default' COMMENT 'ownerEmail',
                       `IsDeleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '1: deleted, 0: normal',
                       `DataChange_CreatedBy` varchar(64) NOT NULL DEFAULT 'default' COMMENT 'Creator's email prefix',
                       `DataChange_CreatedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
                       `DataChange_LastModifiedBy` varchar(64) DEFAULT '' COMMENT 'Last modified person's email prefix',
                       `DataChange_LastTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last modification time',
                       PRIMARY KEY (`Id`),
                       KEY `AppId` (`AppId`(191)),
                       KEY `DataChange_LastTime` (`DataChange_LastTime`),
                       KEY `IX_Name` (`Name`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Application Table';



# Dump of table appnamespace
# ------------------------------------------------------------

DROP TABLE IF EXISTS `AppNamespace`;

CREATE TABLE `AppNamespace` (
                                `Id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto-increment primary key',
                                `Name` varchar(32) NOT NULL DEFAULT '' COMMENT 'namespace name, note that it needs to be globally unique',
                                `AppId` varchar(64) NOT NULL DEFAULT '' COMMENT 'app id',
                                `Format` varchar(32) NOT NULL DEFAULT 'properties' COMMENT 'format type of namespace',
                                `IsPublic` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Is namespace public',
                                `Comment` varchar(64) NOT NULL DEFAULT '' COMMENT 'comment',
                                `IsDeleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '1: deleted, 0: normal',
                                `DataChange_CreatedBy` varchar(64) NOT NULL DEFAULT 'default' COMMENT 'Creator's email prefix',
                                `DataChange_CreatedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
                                `DataChange_LastModifiedBy` varchar(64) DEFAULT '' COMMENT 'Last modified person's email prefix',
                                `DataChange_LastTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last modification time',
                                PRIMARY KEY (`Id`),
                                KEY `IX_AppId` (`AppId`),
                                KEY `Name_AppId` (`Name`,`AppId`),
                                KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Apply namespace definition';



# Dump of table audit
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Audit`;

CREATE TABLE `Audit` (
                         `Id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT 'primary key',
                         `EntityName` varchar(50) NOT NULL DEFAULT 'default' COMMENT 'table name',
                         `EntityId` int(10) unsigned DEFAULT NULL COMMENT 'Record ID',
                         `OpName` varchar(50) NOT NULL DEFAULT 'default' COMMENT 'Operation type',
                         `Comment` varchar(500) DEFAULT NULL COMMENT 'Remarks',
                         `IsDeleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '1: deleted, 0: normal',
                         `DataChange_CreatedBy` varchar(64) NOT NULL DEFAULT 'default' COMMENT 'Creator's email prefix',
                         `DataChange_CreatedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
                         `DataChange_LastModifiedBy` varchar(64) DEFAULT '' COMMENT 'Last modified person's email prefix',
                         `DataChange_LastTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last modification time',
                         PRIMARY KEY (`Id`),
                         KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Log audit table';



# Dump of table cluster
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Cluster`;

CREATE TABLE `Cluster` (
                           `Id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto-increment primary key',
                           `Name` varchar(32) NOT NULL DEFAULT '' COMMENT 'Cluster name',
                           `AppId` varchar(64) NOT NULL DEFAULT '' COMMENT 'App id',
                           `ParentClusterId` int(10) unsigned NOT NULL DEFAULT '0' COMMENT 'parent cluster',
                           `IsDeleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '1: deleted, 0: normal',
                           `DataChange_CreatedBy` varchar(64) NOT NULL DEFAULT 'default' COMMENT 'Creator's email prefix',
                           `DataChange_CreatedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
                           `DataChange_LastModifiedBy` varchar(64) DEFAULT '' COMMENT 'Last modified person's email prefix',
                           `DataChange_LastTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last modification time',
                           PRIMARY KEY (`Id`),
                           KEY `IX_AppId_Name` (`AppId`,`Name`),
                           KEY `IX_ParentClusterId` (`ParentClusterId`),
                           KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Cluster';



# Dump of table commit
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Commit`;

CREATE TABLE `Commit` (
                          `Id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT 'primary key',
                          `ChangeSets` longtext NOT NULL COMMENT 'Modify changes set',
                          `AppId` varchar(500) NOT NULL DEFAULT 'default' COMMENT 'AppID',
                          `ClusterName` varchar(500) NOT NULL DEFAULT 'default' COMMENT 'ClusterName',
                          `NamespaceName` varchar(500) NOT NULL DEFAULT 'default' COMMENT 'namespaceName',
                          `Comment` varchar(500) DEFAULT NULL COMMENT 'Remarks',
                          `IsDeleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '1: deleted, 0: normal',
                          `DataChange_CreatedBy` varchar(64) NOT NULL DEFAULT 'default' COMMENT 'Creator's email prefix',
                          `DataChange_CreatedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
                          `DataChange_LastModifiedBy` varchar(64) DEFAULT '' COMMENT 'Last modified person's email prefix',
                          `DataChange_LastTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last modification time',
                          PRIMARY KEY (`Id`),
                          KEY `DataChange_LastTime` (`DataChange_LastTime`),
                          KEY `AppId` (`AppId`(191)),
                          KEY `ClusterName` (`ClusterName`(191)),
                          KEY `NamespaceName` (`NamespaceName`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='commit history table';

# Dump of table grayreleaserule
# ------------------------------------------------------------

DROP TABLE IF EXISTS `GrayReleaseRule`;

CREATE TABLE `GrayReleaseRule` (
                                   `Id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'primary key',
                                   `AppId` varchar(64) NOT NULL DEFAULT 'default' COMMENT 'AppID',
                                   `ClusterName` varchar(32) NOT NULL DEFAULT 'default' COMMENT 'Cluster Name',
                                   `NamespaceName` varchar(32) NOT NULL DEFAULT 'default' COMMENT 'Namespace Name',
                                   `BranchName` varchar(32) NOT NULL DEFAULT 'default' COMMENT 'branch name',
                                   `Rules` varchar(16000) DEFAULT '[]' COMMENT 'Grayscale rules',
                                   `ReleaseId` int(11) unsigned NOT NULL DEFAULT '0' COMMENT 'Grayscale corresponding release',
                                   `BranchStatus` tinyint(2) DEFAULT '1' COMMENT 'Grayscale branch status: 0: Delete branch, 1: Rules in use 2: Full release',
                                   `IsDeleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '1: deleted, 0: normal',
                                   `DataChange_CreatedBy` varchar(64) NOT NULL DEFAULT 'default' COMMENT 'Creator's email prefix',
                                   `DataChange_CreatedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
                                   `DataChange_LastModifiedBy` varchar(64) DEFAULT '' COMMENT 'Last modified person's email prefix',
                                   `DataChange_LastTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last modification time',
                                   PRIMARY KEY (`Id`),
                                   KEY `DataChange_LastTime` (`DataChange_LastTime`),
                                   KEY `IX_Namespace` (`AppId`,`ClusterName`,`NamespaceName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Grayscale rule table';


# Dump of table instance
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Instance`;

CREATE TABLE `Instance` (
                            `Id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto-increment Id',
                            `AppId` varchar(64) NOT NULL DEFAULT 'default' COMMENT 'AppID',
                            `ClusterName` varchar(32) NOT NULL DEFAULT 'default' COMMENT 'ClusterName',
                            `DataCenter` varchar(64) NOT NULL DEFAULT 'default' COMMENT 'Data Center Name',
                            `Ip` varchar(32) NOT NULL DEFAULT '' COMMENT 'instance ip',
                            `DataChange_CreatedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
                            `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last modification time',
                            PRIMARY KEY (`Id`),
                            UNIQUE KEY `IX_UNIQUE_KEY` (`AppId`,`ClusterName`,`Ip`,`DataCenter`),
                            KEY `IX_IP` (`Ip`),
                            KEY `IX_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Use configured application instance';



# Dump of table instanceconfig
# ------------------------------------------------------------

DROP TABLE IF EXISTS `InstanceConfig`;

CREATE TABLE `InstanceConfig` (
                                  `Id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto-increment Id',
                                  `InstanceId` int(11) unsigned DEFAULT NULL COMMENT 'Instance Id',
                                  `ConfigAppId` varchar(64) NOT NULL DEFAULT 'default' COMMENT 'Config App Id',
                                  `ConfigClusterName` varchar(32) NOT NULL DEFAULT 'default' COMMENT 'Config Cluster Name',
                                  `ConfigNamespaceName` varchar(32) NOT NULL DEFAULT 'default' COMMENT 'Config Namespace Name',
                                  `ReleaseKey` varchar(64) NOT NULL DEFAULT '' COMMENT 'Released Key',
                                  `ReleaseDeliveryTime` timestamp NULL DEFAULT NULL COMMENT 'Configuration acquisition time',
                                  `DataChange_CreatedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
                                  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last modification time',
                                  PRIMARY KEY (`Id`),
                                  UNIQUE KEY `IX_UNIQUE_KEY` (`InstanceId`,`ConfigAppId`,`ConfigNamespaceName`),
                                  KEY `IX_ReleaseKey` (`ReleaseKey`),
                                  KEY `IX_DataChange_LastTime` (`DataChange_LastTime`),
                                  KEY `IX_Valid_Namespace` (`ConfigAppId`,`ConfigClusterName`,`ConfigNamespaceName`,`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Configuration information of application instance';



# Dump of table item
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Item`;

CREATE TABLE `Item` (
                        `Id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto-increment Id',
                        `NamespaceId` int(10) unsigned NOT NULL DEFAULT '0' COMMENT 'Cluster NamespaceId',
                        `Key` varchar(128) NOT NULL DEFAULT 'default' COMMENT 'Configuration item Key',
                        `Value` longtext NOT NULL COMMENT 'Configuration item value',
                        `Comment` varchar(1024) DEFAULT '' COMMENT 'comment',
                        `LineNum` int(10) unsigned DEFAULT '0' COMMENT 'line number',
                        `IsDeleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '1: deleted, 0: normal',
                        `DataChange_CreatedBy` varchar(64) NOT NULL DEFAULT 'default' COMMENT 'Creator's email prefix',
                        `DataChange_CreatedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
                        `DataChange_LastModifiedBy` varchar(64) DEFAULT '' COMMENT 'Last modified person's email prefix',
                        `DataChange_LastTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last modification time',
                        PRIMARY KEY (`Id`),
                        KEY `IX_GroupId` (`NamespaceId`),
                        KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Configuration Project';



# Dump of table namespace
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Namespace`;

CREATE TABLE `Namespace` (
                             `Id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto-increment primary key',
                             `AppId` varchar(500) NOT NULL DEFAULT 'default' COMMENT 'AppID',
                             `ClusterName` varchar(500) NOT NULL DEFAULT 'default' COMMENT 'Cluster Name',
                             `NamespaceName` varchar(500) NOT NULL DEFAULT 'default' COMMENT 'Namespace Name',
                             `IsDeleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '1: deleted, 0: normal',
                             `DataChange_CreatedBy` varchar(64) NOT NULL DEFAULT 'default' COMMENT 'Creator's email prefix',
                             `DataChange_CreatedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
                             `DataChange_LastModifiedBy` varchar(64) DEFAULT '' COMMENT 'Last modified person's email prefix',
                             `DataChange_LastTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last modification time',
                             PRIMARY KEY (`Id`),
                             KEY `AppId_ClusterName_NamespaceName` (`AppId`(191),`ClusterName`(191),`NamespaceName`(191)),
                             KEY `DataChange_LastTime` (`DataChange_LastTime`),
                             KEY `IX_NamespaceName` (`NamespaceName`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='namespace';



# Dump of table namespacelock
# ------------------------------------------------------------

DROP TABLE IF EXISTS `NamespaceLock`;
CREATE TABLE `NamespaceLock` (
                                 `Id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto-increment id',
                                 `NamespaceId` int(10) unsigned NOT NULL DEFAULT '0' COMMENT 'Cluster NamespaceId',
                                 `DataChange_CreatedBy` varchar(64) NOT NULL DEFAULT 'default' COMMENT 'Creator's email prefix',
                                 `DataChange_CreatedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
                                 `DataChange_LastModifiedBy` varchar(64) DEFAULT '' COMMENT 'Last modified person's email prefix',
                                 `DataChange_LastTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last modification time',
                                 `IsDeleted` bit(1) DEFAULT b'0' COMMENT 'soft delete',
                                 PRIMARY KEY (`Id`),
                                 UNIQUE KEY `IX_NamespaceId` (`NamespaceId`),
                                 KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='namespace edit lock';



# Dump of table release
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Release`;

CREATE TABLE `Release` (
                           `Id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto-increment primary key',
                           `ReleaseKey` varchar(64) NOT NULL DEFAULT '' COMMENT 'Released Key',
                           `Name` varchar(64) NOT NULL DEFAULT 'default' COMMENT 'Publish name',
                           `Comment` varchar(256) DEFAULT NULL COMMENT 'Release Notes',
                           `AppId` varchar(500) NOT NULL DEFAULT 'default' COMMENT 'AppID',
                           `ClusterName` varchar(500) NOT NULL DEFAULT 'default' COMMENT 'ClusterName',
                           `NamespaceName` varchar(500) NOT NULL DEFAULT 'default' COMMENT 'namespaceName',
                           `Configurations` longtext NOT NULL COMMENT 'Release configuration',
                           `IsAbandoned` bit(1) NOT NULL DEFAULT b'0' COMMENT 'Whether it is abandoned',
                           `IsDeleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '1: deleted, 0: normal',
                           `DataChange_CreatedBy` varchar(64) NOT NULL DEFAULT 'default' COMMENT 'Creator's email prefix',
                           `DataChange_CreatedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
                           `DataChange_LastModifiedBy` varchar(64) DEFAULT '' COMMENT 'Last modified person's email prefix',
                           `DataChange_LastTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last modification time',
                           PRIMARY KEY (`Id`),
                           KEY `AppId_ClusterName_GroupName` (`AppId`(191),`ClusterName`(191),`NamespaceName`(191)),
                           KEY `DataChange_LastTime` (`DataChange_LastTime`),
                           KEY `IX_ReleaseKey` (`ReleaseKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Release';


# Dump of table releasehistory
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ReleaseHistory`;

CREATE TABLE `ReleaseHistory` (
                                  `Id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto-increment Id',
                                  `AppId` varchar(64) NOT NULL DEFAULT 'default' COMMENT 'AppID',
                                  `ClusterName` varchar(32) NOT NULL DEFAULT 'default' COMMENT 'ClusterName',
                                  `NamespaceName` varchar(32) NOT NULL DEFAULT 'default' COMMENT 'namespaceName',
                                  `BranchName` varchar(32) NOT NULL DEFAULT 'default' COMMENT 'Release branch name',
                                  `ReleaseId` int(11) unsigned NOT NULL DEFAULT '0' COMMENT 'Associated Release Id',
                                  `PreviousReleaseId` int(11) unsigned NOT NULL DEFAULT '0' COMMENT 'Previous ReleaseId',
                                  `Operation` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT 'Release type, 0: normal release, 1: rollback, 2: grayscale release, 3: grayscale rule update, 4: grayscale merge back to the main branch Release, 5: The main branch releases grayscale and automatically releases, 6: The main branch rolls back grayscale and automatically releases, 7: Abandon grayscale',
                                  `OperationContext` longtext NOT NULL COMMENT 'Publish context information',
                                  `IsDeleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '1: deleted, 0: normal',
                                  `DataChange_CreatedBy` varchar(64) NOT NULL DEFAULT 'default' COMMENT 'Creator's email prefix',
                                  `DataChange_CreatedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
                                  `DataChange_LastModifiedBy` varchar(64) DEFAULT '' COMMENT 'Last modified person's email prefix',
                                  `DataChange_LastTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last modification time',
                                  PRIMARY KEY (`Id`),
                                  KEY `IX_Namespace` (`AppId`,`ClusterName`,`NamespaceName`,`BranchName`),
                                  KEY `IX_ReleaseId` (`ReleaseId`),
                                  KEY `IX_DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Release history';


# Dump of table releasemessage
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ReleaseMessage`;

CREATE TABLE `ReleaseMessage` (
                                  `Id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto-increment primary key',
                                  `Message` varchar(1024) NOT NULL DEFAULT '' COMMENT 'Published message content',
                                  `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last modification time',
                                  PRIMARY KEY (`Id`),
                                  KEY `DataChange_LastTime` (`DataChange_LastTime`),
                                  KEY `IX_Message` (`Message`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Publish message';



# Dump of table serverconfig
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ServerConfig`;

CREATE TABLE `ServerConfig` (
                                `Id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto-increment Id',
                                `Key` varchar(64) NOT NULL DEFAULT 'default' COMMENT 'Configuration item Key',
                                `Cluster` varchar(32) NOT NULL DEFAULT 'default' COMMENT 'Configure the corresponding cluster, default is not for a specific cluster',
                                `Value` varchar(2048) NOT NULL DEFAULT 'default' COMMENT 'Configuration item value',
                                `Comment` varchar(1024) DEFAULT '' COMMENT 'comment',
                                `IsDeleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '1: deleted, 0: normal',
                                `DataChange_CreatedBy` varchar(64) NOT NULL DEFAULT 'default' COMMENT 'Creator's email prefix',
                                `DataChange_CreatedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
                                `DataChange_LastModifiedBy` varchar(64) DEFAULT '' COMMENT 'Last modified person's email prefix',
                                `DataChange_LastTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last modification time',
                                PRIMARY KEY (`Id`),
                                KEY `IX_Key` (`Key`),
                                KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Configuration service self-configuration';

# Dump of table accesskey
# ------------------------------------------------------------

DROP TABLE IF EXISTS `AccessKey`;

CREATE TABLE `AccessKey` (
                             `Id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Auto-increment primary key',
                             `AppId` varchar(500) NOT NULL DEFAULT 'default' COMMENT 'AppID',
                             `Secret` varchar(128) NOT NULL DEFAULT '' COMMENT 'Secret',
                             `IsEnabled` bit(1) NOT NULL DEFAULT b'0' COMMENT '1: enabled, 0: disabled',
                             `IsDeleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '1: deleted, 0: normal',
                             `DataChange_CreatedBy` varchar(64) NOT NULL DEFAULT 'default' COMMENT 'Creator's email prefix',
                             `DataChange_CreatedTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
                             `DataChange_LastModifiedBy` varchar(64) DEFAULT '' COMMENT 'Last modified person's email prefix',
                             `DataChange_LastTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last modification time',
                             PRIMARY KEY (`Id`),
                             KEY `AppId` (`AppId`(191)),
                             KEY `DataChange_LastTime` (`DataChange_LastTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Access Key';

# Config
# ------------------------------------------------------------
INSERT INTO `ServerConfig` (`Key`, `Cluster`, `Value`, `Comment`)
VALUES
('eureka.service.url', 'default', 'http://localhost:8080/eureka/', 'Eureka service Url, multiple services separated by English commas'),
    ('namespace.lock.switch', 'default', 'false', 'Only one person can modify the switch at a time'),
    ('item.key.length.limit', 'default', '128', 'item key maximum length limit'),
    ('item.value.length.limit', 'default', '20000', 'item value maximum length limit'),
    ('config-service.cache.enabled', 'default', 'false', 'Whether ConfigService enables caching. Enabling it can improve performance, but will increase memory consumption!');

# Sample Data
# ------------------------------------------------------------
INSERT INTO `App` (`AppId`, `Name`, `OrgId`, `OrgName`, `OwnerName`, `OwnerEmail`)
VALUES
  ('SampleApp', 'Sample App', 'TEST1', 'Sample Department 1', 'apollo', 'apollo@acme.com');

INSERT INTO `AppNamespace` (`Name`, `AppId`, `Format`, `IsPublic`, `Comment`)
VALUES
    ('application', 'SampleApp', 'properties', 0, 'default app namespace');

INSERT INTO `Cluster` (`Name`, `AppId`)
VALUES
    ('default', 'SampleApp');

INSERT INTO `Namespace` (`Id`, `AppId`, `ClusterName`, `NamespaceName`)
VALUES
(1, 'SampleApp', 'default', 'application');


INSERT INTO `Item` (`NamespaceId`, `Key`, `Value`, `Comment`, `LineNum`)
VALUES
    (1, 'timeout', '100', 'sample timeout configuration', 1);

INSERT INTO `Release` (`ReleaseKey`, `Name`, `Comment`, `AppId`, `ClusterName`, `NamespaceName`, `Configurations`)
VALUES
    ('20161009155425-d3a0749c6e20bc15', '20161009155424-release', 'Samplerelease', 'SampleApp', 'default', 'application', '{\"timeout\":\"100\"}');

INSERT INTO `ReleaseHistory` (`AppId`, `ClusterName`, `NamespaceName`, `BranchName`, `ReleaseId`, `PreviousReleaseId`, `Operation`, `OperationContext`, `DataChange_CreatedBy`, `DataChange_LastModifiedBy`)
VALUES
    ('SampleApp', 'default', 'application', 'default', 1, 0, 0, '{}', 'apollo', 'apollo');
INSERT INTO `ReleaseMessage` (`Message`)
VALUES
('SampleApp+default+application');

/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;