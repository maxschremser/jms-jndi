# IBM MQ Container

The docker-compose.yaml file contains two services:
- **ibmmq-init**: Creates the JNDI Bindings in the mounted directory
- **ibmmq**: Starts the MQ Server

## User
The **app** User has the password **passw0rd**, can be changed in the .mq.env file.
The **app** User can use:
- Channel **DEV.APP.SVRCONN** 
- BROWSE and GET from all Queues starting with **DEV.**


## Java Libraries
The Java libraries will be mounted into the directory $projectDir/maven/lib from where Gradle can pick them up.

## Start ibmmq-init Container
```bash
docker-compose up ibmmq-init
```
Hit Ctrl+C to stop the container.

## Start ibmmq Container
```bash
docker-compose up -d ibmmq
```

## Start JmsClient

