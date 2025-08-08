package com.example.vaadincluster.config;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.hazelcast.HazelcastIndexedSessionRepository;
import org.springframework.session.hazelcast.PrincipalNameExtractor;

/**
 * Hazelcast configuration for distributed session management.
 * 
 * This configuration sets up:
 * - Hazelcast cluster with multicast discovery
 * - Session replication across cluster nodes
 * - Network configuration for clustering
 * - Session map configuration with backup and TTL
 */
@Configuration
public class HazelcastConfig {

    /**
     * Configure Hazelcast instance for session clustering
     */
    @Bean
    public HazelcastInstance hazelcastInstance() {
        Config config = new Config();
        
        // Instance name and cluster configuration
        config.setInstanceName("vaadin-cluster-instance");
        config.setClusterName("vaadin-cluster");
        
        // Network configuration for clustering
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPort(5701);
        networkConfig.setPortAutoIncrement(true);
        networkConfig.setPortCount(20);
        
        // Join configuration - using multicast for local development
        JoinConfig joinConfig = networkConfig.getJoin();
        joinConfig.getMulticastConfig()
                .setEnabled(true)
                .setMulticastGroup("224.2.2.3")
                .setMulticastPort(54327);
        
        // Disable other join methods
        joinConfig.getTcpIpConfig().setEnabled(false);
        joinConfig.getAwsConfig().setEnabled(false);
        joinConfig.getGcpConfig().setEnabled(false);
        joinConfig.getAzureConfig().setEnabled(false);
        joinConfig.getKubernetesConfig().setEnabled(false);
        joinConfig.getEurekaConfig().setEnabled(false);
        
        // Configure session map
        MapConfig sessionMapConfig = new MapConfig();
        sessionMapConfig.setName(HazelcastIndexedSessionRepository.DEFAULT_SESSION_MAP_NAME);
        
        // Session backup and eviction configuration
        sessionMapConfig.setBackupCount(1); // One backup copy
        sessionMapConfig.setAsyncBackupCount(0);
        
        // TTL configuration for sessions (30 minutes)
        sessionMapConfig.setTimeToLiveSeconds(1800);
        sessionMapConfig.setMaxIdleSeconds(1800);
        
        // Eviction policy
        EvictionConfig evictionConfig = new EvictionConfig();
        evictionConfig.setEvictionPolicy(EvictionPolicy.LRU);
        evictionConfig.setMaxSizePolicy(MaxSizePolicy.PER_NODE);
        evictionConfig.setSize(10000);
        sessionMapConfig.setEvictionConfig(evictionConfig);
        
        // Add session map configuration
        config.addMapConfig(sessionMapConfig);
        
        // Management center configuration (optional)
        ManagementCenterConfig managementCenterConfig = new ManagementCenterConfig();
        managementCenterConfig.setConsoleEnabled(true);
        config.setManagementCenterConfig(managementCenterConfig);
        
        // Serialization configuration
        SerializationConfig serializationConfig = config.getSerializationConfig();
        serializationConfig.addPortableFactory(1, new SessionPortableFactory());
        
        return Hazelcast.newHazelcastInstance(config);
    }
    
    /**
     * Principal name extractor for session attribution
     */
    @Bean
    public PrincipalNameExtractor principalNameExtractor() {
        return PrincipalNameExtractor.class::getName;
    }
    
    /**
     * Portable factory for session serialization
     */
    private static class SessionPortableFactory implements com.hazelcast.nio.serialization.PortableFactory {
        @Override
        public com.hazelcast.nio.serialization.Portable create(int classId) {
            return null; // Default implementation
        }
    }
}

