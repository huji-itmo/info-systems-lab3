#!/bin/bash

# sets env variables
set -a
. ./.env
set +a

sshpass -e ssh -p 2222 -L 8080:localhost:${DEPLOY_PORT} ${DEPLOY_USER}@${DEPLOY_HOST}
