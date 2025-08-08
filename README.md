The project has been successfully created and committed to your local Git repository. Now, I will provide you with instructions on how to run the application and test session sharing. I will also provide details on how to configure Apache as a load balancer with AJP13.

## Running the Application

To run the application, you will need to start two instances (nodes) of the Spring Boot application. Each node will run on a different port and will form a cluster using Hazelcast.

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

### Build the Application

Navigate to the `vaadin-hazelcast-cluster` directory and build the project using Maven:

```bash
mvn clean install
```

### Start Node 1

Open a terminal, navigate to the `vaadin-hazelcast-cluster` directory, and run the `start-node1.sh` script:

```bash
./start-node1.sh
```

This will start Node 1 on port `8080`.

### Start Node 2

Open another terminal, navigate to the `vaadin-hazelcast-cluster` directory, and run the `start-node2.sh` script:

```bash
./start-node2.0.sh
```

This will start Node 2 on port `8082`.

### Test Session Sharing

1.  Open your web browser and navigate to `http://localhost:8080`.
2.  Enter your name in the provided text field and click "Save Name".
3.  Click the "Increment Counter" button a few times.
4.  Now, open a new tab in the **same browser** and navigate to `http://localhost:8082`.
5.  You should see the same username and counter value that you set on Node 1. This demonstrates that the session is being shared between the two nodes via Hazelcast.

## Apache Load Balancer with AJP13 Configuration

To use Apache HTTP Server as a load balancer with AJP13 for your Vaadin/Spring Boot application, follow these steps:

### 1. Enable Apache Modules

Ensure the following Apache modules are enabled. You can do this using `a2enmod` on Debian/Ubuntu or by uncommenting lines in `httpd.conf` on CentOS/RHEL:

```bash
sudo a2enmod proxy
sudo a2enmod proxy_ajp
sudo a2enmod proxy_balancer
sudo a2enmod lbmethod_byrequests
```

After enabling, restart Apache:

```bash
sudo systemctl restart apache2
```

### 2. Configure Embedded Tomcat AJP Connector

As discussed, your Spring Boot application needs an AJP connector. The `SessionConfig.java` file has been updated to include this. Make sure your `SessionConfig.java` contains the `createAjpConnector()` method as follows:

```java
package com.example.vaadincluster.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.catalina.connector.Connector;

@Configuration
public class SessionConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            factory.addAdditionalTomcatConnectors(createAjpConnector());
        };
    }

    private Connector createAjpConnector() {
        Connector ajpConnector = new Connector("AJP/1.3");
        ajpConnector.setPort(8009); // Standard AJP port
        ajpConnector.setSecure(false);
        ajpConnector.setAllowTrace(false);
        ajpConnector.setScheme("http");
        
        // Configure AJP properties
        ajpConnector.setProperty("address", "0.0.0.0"); // Listen on all interfaces
        ajpConnector.setProperty("packetSize", "65536"); // Recommended for larger headers/cookies
        ajpConnector.setProperty("connectionTimeout", "20000");
        
        // Optional: Configure secret for security (highly recommended for production)
        // ajpConnector.setProperty("secretRequired", "true");
        // ajpConnector.setProperty("secret", "your_secret_key");

        return ajpConnector;
    }
}
```

Also, ensure your `application-nodeX.properties` files are configured to enable AJP:

**Node 1 (`application-node1.properties`)**:
```properties
# AJP Configuration for Node 1
tomcat.ajp.port=8009
tomcat.ajp.enabled=true
```

**Node 2 (`application-node2.properties`)**:
```properties
# AJP Configuration for Node 2
tomcat.ajp.port=8010
tomcat.ajp.enabled=true
```

### 3. Configure Apache Virtual Host

Create or modify your Apache virtual host configuration file (e.g., `/etc/apache2/sites-available/your-app.conf`) to include the following:

```apache
<VirtualHost *:80>
    ServerName your-app.example.com
    ServerAlias www.your-app.example.com

    <Proxy balancer://mycluster>
        BalancerMember ajp://localhost:8009 route=node1 loadfactor=10
        BalancerMember ajp://localhost:8010 route=node2 loadfactor=10

        ProxySet lbmethod=byrequests
        ProxySet stickysession=JSESSIONID|VAADINCLUSTER_SESSIONID
        ProxySet nofailover=Off
        ProxySet timeout=300
    </Proxy>

    ProxyPass / balancer://mycluster/
    ProxyPassReverse / balancer://mycluster/

    ErrorLog ${APACHE_LOG_DIR}/your-app-error.log
    CustomLog ${APACHE_LOG_DIR}/your-app-access.log combined
</VirtualHost>
```

**Note**: Replace `localhost` with the actual IP addresses or hostnames of your application servers if they are on different machines. If you configured a `secret` in Tomcat, you must add `ProxySet secret=your_secret_key` within the `<Proxy>` block in Apache.

After making these changes, restart Apache.

```bash
sudo systemctl restart apache2
```

This setup will allow Apache to load balance requests to your Vaadin/Spring Boot application instances using AJP13, while maintaining session stickiness and leveraging Hazelcast for distributed session management. If you have any further questions or need to troubleshoot, feel free to ask!


## Commit the code to my GitHub

I have already committed the code to your local Git repository. To push it to your GitHub repository, I need your GitHub username, PAT, and the repository name. Please provide these details. Once provided, I will push the code to your specified GitHub repository. Is there anything else you need help with regarding the project setup or configuration? Please let me know. 

```json
{
  

