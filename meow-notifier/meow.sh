#!/bin/bash
export KAFKA_BROKERS="<BROKERS>"
export KAFKA_USERNAME="<USER>"
export KAFKA_PASSWORD="<PASS>"
export WEBHOOK_URL="<YOUR WEBHOOK>"
export LOGSENSE_TOKEN="<YOUR TOKEN>"
java -Dsa.tracer=logsense -javaagent:/var/lib/tomcat8/webapps/ROOT/WEB-INF/lib/logsense-opentracing-agent-1.1.1.jar -jar meow-notifier-0.0.1-SNAPSHOT-jar-with-dependencies.jar
