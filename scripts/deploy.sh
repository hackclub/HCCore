#!/bin/bash

# Transfer the built plugin to the server
openssl aes-256-cbc -K $encrypted_758fa9b89a26_key \
    -iv $encrypted_758fa9b89a26_iv \
    -in ./deploy_key.enc \
    -out ./deploy_key -d
eval "$(ssh-agent -s)"
chmod 600 ./deploy_key
ssh-add ./deploy_key
scp -o "StrictHostKeyChecking=no" \
    -i ./deploy_key \
    ./target/HCCore.jar \
    $SERVER_USER:~/server/plugins

# Run the reload script
ssh $SERVER_USER "~/server/scripts/reload.sh"
