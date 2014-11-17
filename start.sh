#!/bin/sh

java -cp target/patricks-1.0-SNAPSHOT-jar-with-dependencies.jar financial.Core -server -Xms512m -Xmx1024m -XX:PermSize=32m -XX:MaxPermSize=128m -XX:PreBlockSpin=20 -XX:ParallelGCThreads=8 -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:SurvivorRatio=8 -XX:TargetSurvivorRatio=90 -Dlog4j.configuration=/log4j.properties