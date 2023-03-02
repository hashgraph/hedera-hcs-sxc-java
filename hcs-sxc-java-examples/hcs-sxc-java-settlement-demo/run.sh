#!/bin/sh

cp ./docker-compose.yml ./config/docker-compose.yml
docker-compose up --remove-orphans -d

echo "Starting log stream in 2s, CTRL+C to stop (will leave containers running)"

sleep 2
docker-compose logs -f --tail=10
