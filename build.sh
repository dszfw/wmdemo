#!/bin/bash
./mvnw clean install
docker build -t wm/demo .
