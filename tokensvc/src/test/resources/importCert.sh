#!/bin/bash

ALIAS=$1

CERTFILE=$2

KEYPASS="trusted"

STORETYPE="JKS"

KEYSTORE='trusted-keystore.jks'

STOREPASS="trusted"



keytool -importcert -alias "$ALIAS" -file "$CERTFILE" -keypass $KEYPASS -storetype $STORETYPE -storepass $STOREPASS -keystore $KEYSTORE
