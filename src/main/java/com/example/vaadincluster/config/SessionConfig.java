package com.example.vaadincluster.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;

/**
 * Session configuration for embedded Tomcat with Hazelcast clustering.
 * 
 * This configuration ensures:
 * - Proper session ID resolution via cookies
 * - Embedded Tomcat optimization for clustering
 * - Session sticky behavior configuration
 */
@Configuration
public class SessionConfig {

    /**
     * Configure HTTP session ID resolver to use cookies
     */
    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        CookieHttpSessionIdResolver resolver = new CookieHttpSessionIdResolver();
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setCookieName("VAADINCLUSTER_SESSIONID");
        cookieSerializer.setCookieMaxAge(1800); // 30 minutes
        cookieSerializer.setCookiePath("/");
        resolver.setCookieSerializer(cookieSerializer);
        return resolver;
    }
    
    /**
     * Customize embedded Tomcat for clustering
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
                
                // Optimize for clustering
                protocol.setMaxThreads(200);
                protocol.setMinSpareThreads(10);
                protocol.setConnectionTimeout(20000);
                protocol.setKeepAliveTimeout(15000);
                protocol.setMaxKeepAliveRequests(100);
                
                // Enable compression
                protocol.setCompression("on");
                protocol.setCompressionMinSize(1024);
                protocol.setCompressibleMimeType("text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json");
            });
            
            // Add additional connector for cluster communication if needed
            factory.addAdditionalTomcatConnectors(createClusterConnector());
        };
    }
    
    /**
     * Create AJP connector for Apache load balancer communication
     */
    private Connector createClusterConnector() {
        // Create AJP connector for Apache integration
        Connector ajpConnector = new Connector("AJP/1.3");
        ajpConnector.setPort(8009); // Standard AJP port
        ajpConnector.setSecure(false);
        ajpConnector.setAllowTrace(false);
        ajpConnector.setScheme("http");
        
        // Configure AJP properties
        ajpConnector.setProperty("address", "0.0.0.0"); // Listen on all interfaces
        ajpConnector.setProperty("packetSize", "65536");
        ajpConnector.setProperty("connectionTimeout", "20000");
        
        // Optional: Configure secret for security (recommended for production)
        // ajpConnector.setProperty("secretRequired", "true");
        // ajpConnector.setProperty("secret", "your_secret_key");
        
        return ajpConnector;
    }
}

