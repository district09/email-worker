#!/usr/bin/env groovy

/**

## Parameters

* `kind`: The kind of project that needs to be build. This refers to the jenkins slave needed to do a basic build of the project. Possible values are `maven`, `nodejs`, `node10`, `netcore21`, `netcore22`, ... (Required)
* `buildScript`: Custom buildscript that will run during the `Build and test` stage. Overrides the *default build and test execution*.
* `templatepath`: Use to override default openshift templates path. Default path is "root/openshift/" (Optional)
* `dotnetFolder`: The root folder where the project file is located.
* `dotnetSlnFile`: The name of the project file. 
* `dotnetTestFolder`: The folder where the test project file is located. No need to repeat the root, `dotnetFolder` is added in the pipeline. 
* `dotnetTestFile`: The name of the test project file. 
* `mainBranch`: Overwrites the main branch (default: `master`).
* `namespace`: The project namespace (Required)
* `service`: Arrays of all services that need to be build (Required)
* `repositoryUrl`: Git url where the project is located
* `gitCredentials`: CredentialID to use for git interactions

**/

d09Project  kind: "", 
            namespace: "", 
            service: [""],
            buildScript: "", 
            templatepath: "", 
            dotnetFolder: "",
            dotnetSlnFile: "", 
            dotnetTestFolder: "",
            dotnetTestFile: "",
            mainBranch: "",
            repositoryUrl: ""
