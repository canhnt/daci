#!/bin/bash

DOMAIN="daci-sne-demo"
REDIS_SERVER_ADDRESS="localhost"
TRANSFER_POLICY_FILE="policies/vi-sample1k-provider-policies.xml"
INTER_TENANT_POLICY_FILE="policies/vi-sample1k-intertenant-policies.xml"
INTRA_TENANT_POLICY_FILE= "policies/vi-sample1k-intratenant-policies.xml"

java -jar daci-setup.jar $DOMAIN $REDIS_SERVER_ADDRESS $INTER_TENANT_POLICY_FILE $INTRA_TENANT_POLICY_FILE
