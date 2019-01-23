[![Build Status](https://jenkins.norgene.no/buildStatus/icon?job=LocalEGA-deploy-swarm/master)](https://jenkins.norgene.no/job/LocalEGA-deploy-swarm/job/master/)

# Docker Swarm Deployment

![](https://habrastorage.org/webt/zt/rm/bk/ztrmbknpfaz9ybmoy3j12x5tlcw.gif)

## Prerequisites

The deployment tool of LocalEGA for Docker Swarm is based on [Gradle](https://gradle.org/), so you will need Gradle 5 to
be installed on your machine in order to use it. On MacOS with Homebrew it can be done by executing
`brew install gradle`. Please, refer to official documentation to find instruction for other platforms.

Make sure [Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html) is set up.

## Structure

Gradle project has the following groups of tasks:

- `cluster` - code related to Docker Machine and Docker Swarm cluster provisioning
- `cega` - "fake" CentralEGA bootstrapping and deployment code
- `lega` - main LocalEGA microservices bootstrapping and deployment code
- `swarm` - root project aggregating both `cega` and `lega` 
- `test` - sample test case: generating a file, encrypting it, uploading to the inbox and ingesting it

## Cluster provisioning

Docker Swarm cluster can be provisioned using `gradle provision` command. Provisioning is done via 
[Docker Machine](https://docs.docker.com/machine/). Two providers are supported at the moment: `virtualbox` (default 
one) and `openstack`. 

To provision cluster in the OpenStack one needs to have OpenStack configuration file with filled
settings from [this list](https://docs.docker.com/machine/drivers/openstack/) (there's a sample file called 
`openstack.properties.sample` in the project folder). Then the command will look like this:
`gradle provision -PopenStackConfig=/absolute/path/to/openstack.properties`. 

Note that it may take a while to provision the cluster in OpenStack. To see how many nodes are ready one can run
`gradle list`. By default machine names are `cega` and `lega`.

`gradle destroy` will remove all the virtual machines and destroy the cluster.

## Bootstrapping

Here's an example of bootstrapping with local VMs (VirtualBox driver) (NOTE that LEGA Private **should** be bootstrapped 
prior to LEGA Public, because LEGA Public depends on some LEGA Private configs):
```
gradle :cega:createConfiguration
gradle :lega-private:createConfiguration
gradle :lega-public:createConfiguration
```

This can be replaced with a single command `gradle bootstrap`.

If Docker Machine VM names are not default (i.e. not `cega` and `lega`) you will have to use additional parameters:
```
gradle :cega:createConfiguration -Pmachine=<CEGA_MACHINE_NAME>
gradle :lega-private:createConfiguration -Pmachine=<LEGA_PRIVATE_MACHINE_NAME>
gradle :lega-public:createConfiguration -Pmachine=<LEGA_PUBLIC_MACHINE_NAME> -PcegaIP=$(docker-machine ip <CEGA_MACHINE_NAME>) -PlegaPrivateIP=$(docker-machine ip <LEGA_PRIVATE_MACHINE_NAME>)
```

During bootstrapping, two test users are generated: `john` and `jane`. Credentials, keys and other config information
can be found under `.tmp` folder of each subproject.

## Deploying

After successful bootstrapping, deploying should be as simple as:
```
gradle :cega:deployStack
gradle :lega-private:deployStack
gradle :lega-public:deployStack
```

This can be replaced with a single command `gradle deploy`.

You can also use `-Pmachine=<MACHINE_NAME>` option with any of those commands to specify machine name.

To make sure that the system is deployed you can execute `gradle ls`.

`gradle :cega:removeStack`, `gradle :lega-private:removeStack`, `lega-public :cega:removeStack` will remove deployed stacks 
(yet preserving bootstrapped configuration). To clean configurations and remove stack you can use script like this:
```
gradle :cega:removeStack
gradle :cega:clearConfiguration
gradle :lega-private:removeStack
gradle :lega-private:clearConfiguration
gradle :lega-public:removeStack
gradle :lega-public:clearConfiguration
```

## Testing

There's a built-in simple test to check that the basic scenario works fine. Try to execute `gradle ingest` after
successful deploying to check if ingestion works. It will automatically generate 10MBs file, encrypt it with `Crypt4GH`,
upload to the inbox of test-user `john`, ingest this file and check if it has successfully landed to the vault.

Note that in case of non-standard machine names, additional parameters will be required:
`gradle ingest -PcegaIP=$(docker-machine ip <CEGA_MACHINE_NAME>) -PlegaPublicIP=$(docker-machine ip <LEGA_PUBLIC_MACHINE_NAME>) -PlegaPrivateIP=$(docker-machine ip <LEGA_PRIVATE_MACHINE_NAME>)`

## Demo

There's a short demo recorded with explanations on provisioning and deployment process:
[![Demo](https://img.youtube.com/vi/8hvXxqW8uP0/0.jpg)](https://www.youtube.com/watch?v=8hvXxqW8uP0)

Also there's an updated Asciinema recording:
[![asciicast](https://asciinema.org/a/211883.svg)](https://asciinema.org/a/211883)
