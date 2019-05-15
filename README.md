# Project Title

Tool for migrating ported game data from nurgs to the m4

## Getting Started

### Installing

Get latest copy from master, then do the ff:

```
cd ~project root directory
mvn clean install
```
This will generate/build a new jar which is located in `"project root"/target/` folder.
Or just download the `"nurgs-rounds-to-m4-{version}.jar"` directly and proceed on running as defined below.

## Running 

To run the jar, simply run the command below

```
"java -jar {jar} --partnerCode=OG --gameCode=REINDEER_WILDS_M4_RECORDER --from=1557288821399 --until=1557903854000 --threadCount=2 --pageSize=100 --enableWriteToCassandra=false"
```

### Parameters Explained

Application will need the following parameter:
| Parameter Name        | Required           | Description  |
| ------------- |:-------------:|:----- |
|partnerCode|true|Partner Code|
|gameCode|true|Game Code|
|from|true|inclusive round start time in milliseconds|
|until|true|exclusive round start time in milliseconds|
|threadCount|false|number of threads used for this job. default is 10|
|pageSize|false|size per page queried in mongo. default is 50|
|enableWriteToCassandra|false|default to false for testing purposes.|
|outputFolder|false|output folder for saving all the records process by each thread. default to "output", adjacent to jar path|

