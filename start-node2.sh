#!/bin/bash

# Start Node 2 of Vaadin Hazelcast Cluster
echo "Starting Vaadin Hazelcast Cluster - Node 2"
echo "Port: 8082"
echo "Hazelcast Port: 5702"
echo "Profile: node2"
echo ""

# Build the application if not already built
if [ ! -d "target" ]; then
    echo "Building application..."
    mvn clean compile
fi

# Start the application with node2 profile
mvn spring-boot:run -Dspring-boot.run.profiles=node2 -Dspring-boot.run.jvmArguments="-Dserver.port=8082 -Dhazelcast.network.port=5702"

