# Quick Start Guide

## Prerequisites
- Java 11 or higher
- Maven 3.6 or higher

## Quick Setup (5 minutes)

### 1. Build the Application
```bash
cd vaadin-hazelcast-cluster
mvn clean compile
```

### 2. Start Node 1
```bash
./start-node1.sh
```
Wait for the application to start (you'll see "Started VaadinHazelcastClusterApplication" in the logs).

### 3. Start Node 2 (in a new terminal)
```bash
./start-node2.sh
```

### 4. Test Session Sharing

1. **Open Node 1**: http://localhost:8080
2. **Enter your name** and click "Save Name"
3. **Increment the counter** a few times
4. **Open Node 2**: http://localhost:8082 (same browser)
5. **Verify** your name and counter value are preserved!

## What You'll See

### Main Interface
- **Session Information**: Shows your session ID, counter value, username, and timestamps
- **Cluster Information**: Displays cluster members and status
- **Testing Controls**: Buttons to increment counter, save name, reset session, and refresh info

### Cluster Status
- Both nodes should show "Cluster Size: 2 members"
- You'll see both node addresses listed
- The local node will be marked as "(Local)"

## API Testing

Test the REST API endpoints:

```bash
# Check cluster health
curl http://localhost:8080/api/cluster/health

# Get cluster info
curl http://localhost:8080/api/cluster/info

# Test session storage
curl -X POST "http://localhost:8080/api/cluster/session/test?key=test&value=hello"

# Retrieve session value
curl http://localhost:8080/api/cluster/session/get/test
```

## Troubleshooting

### Nodes Don't Form Cluster
- Ensure both nodes are running
- Check that multicast is enabled on your network
- Verify no firewall is blocking ports 5701-5702

### Session Not Shared
- Use the same browser for both nodes
- Check that cookies are enabled
- Verify cluster formation in the UI

### Build Issues
- Ensure Java 11+ is installed: `java -version`
- Ensure Maven is installed: `mvn -version`
- Check internet connection for dependency downloads

## Next Steps

- Read the full [README.md](README.md) for detailed configuration
- Explore the source code to understand the implementation
- Modify the configuration for your specific needs
- Deploy to production with appropriate security settings

