# Vaadin 24 + Spring Boot 3.4.5 + Hazelcast Cluster Application

## Overview

This application demonstrates a complete implementation of a distributed web application using Vaadin 24 as the frontend framework, Spring Boot 3.4.5 as the backend framework, and Hazelcast for distributed session management across multiple embedded Tomcat instances. The application showcases how to achieve session sharing and clustering in a modern Java web application environment.

## Architecture

The application is built with the following key components:

### Core Technologies
- **Vaadin 24.5.4**: Modern Java web framework for building rich user interfaces
- **Spring Boot 3.4.5**: Enterprise-grade application framework with embedded Tomcat
- **Hazelcast 5.4.0**: In-memory data grid for distributed session management
- **Spring Session**: Integration layer for session management with Hazelcast
- **Embedded Tomcat**: Servlet container with clustering capabilities

### Session Management Architecture

The session management is implemented using a distributed architecture where:

1. **Session Storage**: Sessions are stored in Hazelcast distributed maps instead of local memory
2. **Session Replication**: Session data is automatically replicated across all cluster nodes
3. **Failover Support**: If one node fails, sessions remain available on other nodes
4. **Load Balancing**: Users can be served by any node in the cluster without losing session state

## Project Structure

```
vaadin-hazelcast-cluster/
├── src/main/java/com/example/vaadincluster/
│   ├── VaadinHazelcastClusterApplication.java    # Main application class
│   ├── config/
│   │   ├── HazelcastConfig.java                  # Hazelcast cluster configuration
│   │   └── SessionConfig.java                    # Session and Tomcat configuration
│   ├── views/
│   │   └── MainView.java                         # Main Vaadin UI view
│   ├── service/
│   │   └── SessionService.java                   # Session management service
│   └── controller/
│       └── ClusterController.java                # REST API for monitoring
├── src/main/resources/
│   ├── application.properties                    # Default configuration
│   ├── application-node1.properties              # Node 1 specific config
│   └── application-node2.properties              # Node 2 specific config
├── pom.xml                                       # Maven dependencies
├── start-node1.sh                               # Startup script for node 1
└── start-node2.sh                               # Startup script for node 2
```

## Configuration Details

### Maven Dependencies

The `pom.xml` file includes all necessary dependencies for the application:



#### Key Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| spring-boot-starter-web | 3.4.5 | Web application framework with embedded Tomcat |
| vaadin-spring-boot-starter | 24.5.4 | Vaadin integration with Spring Boot |
| hazelcast | 5.4.0 | Distributed computing platform |
| hazelcast-spring | 5.4.0 | Spring integration for Hazelcast |
| spring-session-hazelcast | Latest | Session management with Hazelcast backend |

### Hazelcast Configuration

The `HazelcastConfig.java` class provides comprehensive configuration for the distributed session management:

#### Cluster Configuration
```java
config.setInstanceName("vaadin-cluster-instance");
config.setClusterName("vaadin-cluster");
```

#### Network Configuration
The network configuration enables cluster discovery and communication:

```java
NetworkConfig networkConfig = config.getNetworkConfig();
networkConfig.setPort(5701);                    // Base port for Hazelcast
networkConfig.setPortAutoIncrement(true);       // Allow port increment
networkConfig.setPortCount(20);                 // Range of ports to try
```

#### Multicast Discovery
For local development and testing, multicast discovery is enabled:

```java
JoinConfig joinConfig = networkConfig.getJoin();
joinConfig.getMulticastConfig()
    .setEnabled(true)
    .setMulticastGroup("224.2.2.3")
    .setMulticastPort(54327);
```

#### Session Map Configuration
The session map is configured with backup and eviction policies:

```java
MapConfig sessionMapConfig = new MapConfig();
sessionMapConfig.setName(HazelcastIndexedSessionRepository.DEFAULT_SESSION_MAP_NAME);
sessionMapConfig.setBackupCount(1);             // One backup copy per session
sessionMapConfig.setTimeToLiveSeconds(1800);    // 30 minutes TTL
sessionMapConfig.setMaxIdleSeconds(1800);       // 30 minutes idle timeout
```

### Session Configuration

The `SessionConfig.java` class handles session ID resolution and Tomcat optimization:

#### Cookie-based Session ID Resolution
```java
@Bean
public HttpSessionIdResolver httpSessionIdResolver() {
    CookieHttpSessionIdResolver resolver = new CookieHttpSessionIdResolver();
    resolver.setCookieName("VAADINCLUSTER_SESSIONID");
    resolver.setCookieMaxAge(-1); // Session cookie
    return resolver;
}
```

#### Embedded Tomcat Optimization
The configuration includes optimizations for clustering:

```java
Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
protocol.setMaxThreads(200);                    // Maximum worker threads
protocol.setMinSpareThreads(10);                // Minimum spare threads
protocol.setConnectionTimeout(20000);           // Connection timeout (20s)
protocol.setKeepAliveTimeout(15000);            // Keep-alive timeout (15s)
protocol.setMaxKeepAliveRequests(100);          // Max keep-alive requests
```

### Application Properties Configuration

#### Default Configuration (`application.properties`)
```properties
# Application Configuration
spring.application.name=vaadin-hazelcast-cluster
server.port=8080

# Vaadin Configuration
vaadin.launch-browser=false
vaadin.whitelisted-packages=com.example.vaadincluster

# Session Configuration
server.servlet.session.timeout=30m
server.servlet.session.cookie.name=VAADINCLUSTER_SESSIONID
server.servlet.session.cookie.http-only=true
server.servlet.session.tracking-modes=cookie

# Logging Configuration
logging.level.com.example.vaadincluster=DEBUG
logging.level.com.hazelcast=INFO
logging.level.org.springframework.session=DEBUG
```

#### Node-Specific Configuration
Each node can have specific configuration overrides:

**Node 1 (`application-node1.properties`)**:
```properties
server.port=8080
spring.application.name=vaadin-hazelcast-cluster-node1
hazelcast.instance.name=vaadin-cluster-node1
hazelcast.network.port=5701
```

**Node 2 (`application-node2.properties`)**:
```properties
server.port=8082
spring.application.name=vaadin-hazelcast-cluster-node2
hazelcast.instance.name=vaadin-cluster-node2
hazelcast.network.port=5702
```

## User Interface Components

### Main View (`MainView.java`)

The main view provides a comprehensive interface for testing session clustering functionality:

#### Session Information Display
- **Session ID**: Unique identifier for the current session
- **Counter Value**: Persistent counter stored in the session
- **Username**: User-defined name stored in the session
- **Session Timestamps**: Creation and last access times
- **Session Configuration**: Timeout and other session parameters

#### Cluster Information Display
- **Cluster Name and Size**: Current cluster configuration
- **Member List**: All active cluster members with addresses
- **Local Member**: Identification of the current node
- **Cluster State**: Overall health and status of the cluster

#### Interactive Testing Components
- **Counter Increment**: Button to increment a session-stored counter
- **Name Storage**: Text field and button to store user information
- **Session Reset**: Button to invalidate and reset the current session
- **Information Refresh**: Button to update all displayed information

### Session Testing Functionality

The application provides several mechanisms to test session sharing:

1. **Counter Persistence**: A simple integer counter that persists across requests and nodes
2. **User Data Storage**: Text-based user information that demonstrates string data persistence
3. **Session Metadata**: Display of session creation time, last access, and configuration
4. **Real-time Updates**: Automatic refresh of cluster and session information

## REST API Endpoints

The application includes a comprehensive REST API for monitoring and testing:

### Cluster Information Endpoints

#### GET `/api/cluster/info`
Returns detailed cluster information including:
- Cluster name and instance details
- List of all cluster members with addresses and UUIDs
- Local member identification
- Cluster state and timestamp

#### GET `/api/cluster/health`
Provides health check information:
- Overall cluster status (UP/DOWN)
- Number of active cluster members
- Total active sessions across the cluster
- Timestamp of the health check

### Session Management Endpoints

#### GET `/api/cluster/sessions`
Returns session statistics:
- Total number of active sessions
- Cluster health status
- Instance and cluster identification

#### GET `/api/cluster/session/current`
Provides detailed information about the current session:
- Session ID and timing information
- All session attributes and their values
- Session configuration parameters

#### POST `/api/cluster/session/test`
Allows testing session storage by setting key-value pairs:
```bash
curl -X POST "http://localhost:8080/api/cluster/session/test?key=testKey&value=testValue"
```

#### GET `/api/cluster/session/get/{key}`
Retrieves specific session values by key:
```bash
curl "http://localhost:8080/api/cluster/session/get/testKey"
```

#### POST `/api/cluster/sessions/replicate`
Forces session replication across the cluster for testing purposes.

## Building and Running the Application

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher
- Network connectivity for multicast (for local testing)

### Building the Application
```bash
cd vaadin-hazelcast-cluster
mvn clean compile
```

### Running Multiple Nodes

#### Starting Node 1
```bash
./start-node1.sh
```
This starts the first node on port 8080 with Hazelcast port 5701.

#### Starting Node 2 (in a separate terminal)
```bash
./start-node2.sh
```
This starts the second node on port 8082 with Hazelcast port 5702.

### Manual Startup Commands

If you prefer manual control, you can start nodes with specific profiles:

**Node 1**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=node1
```

**Node 2**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=node2 \
  -Dspring-boot.run.jvmArguments="-Dserver.port=8082"
```

## Testing Session Sharing

### Basic Session Sharing Test

1. **Start both nodes** using the startup scripts
2. **Access Node 1** at `http://localhost:8080`
3. **Set a username** and increment the counter several times
4. **Note the session ID** displayed in the session information
5. **Access Node 2** at `http://localhost:8082`
6. **Use the same browser** (to maintain the session cookie)
7. **Verify** that the username and counter values are preserved

### Advanced Testing Scenarios

#### Cross-Node Session Persistence
1. Create session data on Node 1
2. Stop Node 1
3. Access Node 2 with the same session
4. Verify that all session data is still available

#### Load Balancing Simulation
1. Start both nodes
2. Use different browser tabs to access both nodes
3. Perform operations on both nodes simultaneously
4. Verify that session data remains consistent

#### Session Replication Verification
1. Use the REST API to monitor session counts:
   ```bash
   curl http://localhost:8080/api/cluster/sessions
   curl http://localhost:8082/api/cluster/sessions
   ```
2. Create sessions on one node
3. Verify that session counts are consistent across nodes

### Monitoring and Debugging

#### Cluster Status Monitoring
Use the health check endpoint to monitor cluster status:
```bash
# Check Node 1
curl http://localhost:8080/api/cluster/health

# Check Node 2
curl http://localhost:8082/api/cluster/health
```

#### Session Data Inspection
Inspect session data using the REST API:
```bash
# Get current session info
curl http://localhost:8080/api/cluster/session/current

# Test session storage
curl -X POST "http://localhost:8080/api/cluster/session/test?key=debug&value=test123"

# Retrieve stored value
curl http://localhost:8080/api/cluster/session/get/debug
```

#### Log Analysis
The application provides detailed logging for troubleshooting:
- **Hazelcast logs**: Cluster formation and member discovery
- **Spring Session logs**: Session creation, replication, and expiration
- **Application logs**: Custom session operations and UI interactions

## Production Deployment Considerations

### Network Configuration

For production deployment, consider the following network configurations:

#### TCP/IP Discovery
Replace multicast discovery with TCP/IP for production environments:
```java
joinConfig.getTcpIpConfig()
    .setEnabled(true)
    .addMember("node1.example.com:5701")
    .addMember("node2.example.com:5701");
```

#### AWS Discovery
For AWS deployments, use AWS discovery:
```java
joinConfig.getAwsConfig()
    .setEnabled(true)
    .setProperty("access-key", "your-access-key")
    .setProperty("secret-key", "your-secret-key")
    .setProperty("region", "us-west-2");
```

### Security Configuration

#### SSL/TLS Configuration
Enable SSL for secure communication:
```java
SSLConfig sslConfig = config.getNetworkConfig().getSSLConfig();
sslConfig.setEnabled(true)
    .setProperty("keyStore", "/path/to/keystore.jks")
    .setProperty("keyStorePassword", "password");
```

#### Session Security
Configure secure session cookies for production:
```properties
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=strict
server.servlet.session.cookie.http-only=true
```

### Performance Tuning

#### Hazelcast Performance
- **Memory Configuration**: Adjust heap size based on expected session volume
- **Backup Strategy**: Configure appropriate backup counts for fault tolerance
- **Eviction Policies**: Implement LRU or other eviction strategies for memory management

#### Tomcat Performance
- **Thread Pool Sizing**: Adjust thread pools based on expected load
- **Connection Pooling**: Configure database connection pools appropriately
- **Compression**: Enable response compression for better performance

### Monitoring and Metrics

#### Hazelcast Management Center
For production monitoring, consider using Hazelcast Management Center:
```java
ManagementCenterConfig managementCenterConfig = new ManagementCenterConfig();
managementCenterConfig.setConsoleEnabled(true);
managementCenterConfig.setUrl("http://management-center:8080/mancenter");
config.setManagementCenterConfig(managementCenterConfig);
```

#### Spring Boot Actuator
The application includes Actuator endpoints for monitoring:
- `/actuator/health`: Application health status
- `/actuator/info`: Application information
- `/actuator/sessions`: Session management information

## Troubleshooting

### Common Issues and Solutions

#### Cluster Formation Problems
**Symptom**: Nodes don't discover each other
**Solutions**:
- Verify multicast is enabled on the network
- Check firewall settings for Hazelcast ports
- Ensure nodes are on the same network segment
- Review Hazelcast logs for discovery errors

#### Session Not Shared
**Symptom**: Session data doesn't persist across nodes
**Solutions**:
- Verify Hazelcast cluster is formed properly
- Check session cookie configuration
- Ensure session map is properly configured
- Review Spring Session logs

#### Performance Issues
**Symptom**: Slow response times or high memory usage
**Solutions**:
- Adjust Hazelcast memory settings
- Optimize session data size
- Configure appropriate eviction policies
- Monitor garbage collection

### Debug Configuration

For debugging, enable detailed logging:
```properties
logging.level.com.hazelcast=DEBUG
logging.level.org.springframework.session=DEBUG
logging.level.com.example.vaadincluster=DEBUG
```

## Conclusion

This Vaadin 24 + Spring Boot 3.4.5 + Hazelcast cluster application demonstrates a complete implementation of distributed session management in a modern Java web application. The application provides:

- **Seamless Session Sharing**: Sessions are automatically replicated across all cluster nodes
- **High Availability**: Application remains functional even if individual nodes fail
- **Scalability**: New nodes can be added to the cluster dynamically
- **Monitoring Capabilities**: Comprehensive REST API and UI for cluster monitoring
- **Production Ready**: Configurable for various deployment environments

The implementation showcases best practices for building distributed Java web applications with modern frameworks and provides a solid foundation for enterprise-grade applications requiring session clustering and high availability.

---

*Documentation generated by Manus AI*

