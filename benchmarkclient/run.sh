#!/bin/sh

NUM=4

ADDRESS="http://localhost:8181/cxf/authzservice/"

TENANT_FILE="src/main/resources/tenants-1k.txt"

java -jar benchmarkclient.jar $NUM $ADDRESS $TENANT_FILE

