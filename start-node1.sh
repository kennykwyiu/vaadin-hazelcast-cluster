#!/bin/bash

# Start Node 1 of Vaadin Hazelcast Cluster
echo "Starting Vaadin Hazelcast Cluster - Node 1"
echo "Port: 8080"
echo "Hazelcast Port: 5701"
echo "Profile: node1"
echo ""

# Build the application if not already built
if [ ! -d "target" ]; then
    echo "Building application..."
    mvn clean compile
fi

# Start the application with node1 profile
mvn spring-boot:run -Dspring-boot.run.profiles=node1 -Dspring-boot.run.jvmArguments="-Dserver.port=8080 -Dhazelcast.network.port=5701"

