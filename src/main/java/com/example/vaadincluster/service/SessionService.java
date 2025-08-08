package com.example.vaadincluster.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.hazelcast.HazelcastIndexedSessionRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

/**
 * Service for managing sessions and cluster operations.
 * 
 * Provides utilities for:
 * - Session monitoring across the cluster
 * - Session statistics
 * - Cluster health checks
 */
@Service
public class SessionService {

    private final HazelcastInstance hazelcastInstance;

    @Autowired
    public SessionService(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    /**
     * Get the session map from Hazelcast
     */
    public IMap<String, Object> getSessionMap() {
        return hazelcastInstance.getMap(HazelcastIndexedSessionRepository.DEFAULT_SESSION_MAP_NAME);
    }

    /**
     * Get total number of active sessions across the cluster
     */
    public int getTotalActiveSessions() {
        return getSessionMap().size();
    }

    /**
     * Get all session IDs in the cluster
     */
    public Set<String> getAllSessionIds() {
        return getSessionMap().keySet();
    }

    /**
     * Get session data for a specific session ID
     */
    public Object getSessionData(String sessionId) {
        return getSessionMap().get(sessionId);
    }

    /**
     * Check if the cluster is healthy
     */
    public boolean isClusterHealthy() {
        try {
            return hazelcastInstance.getCluster().getMembers().size() > 0 &&
                   hazelcastInstance.getLifecycleService().isRunning();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get cluster statistics
     */
    public Map<String, Object> getClusterStats() {
        return Map.of(
            "clusterSize", hazelcastInstance.getCluster().getMembers().size(),
            "activeSessions", getTotalActiveSessions(),
            "instanceName", hazelcastInstance.getName(),
            "clusterName", hazelcastInstance.getConfig().getClusterName(),
            "isHealthy", isClusterHealthy()
        );
    }

    /**
     * Force session replication across the cluster
     */
    public void forceSessionReplication() {
        IMap<String, Object> sessionMap = getSessionMap();
        // Force replication by touching the map
        sessionMap.keySet().forEach(key -> {
            Object value = sessionMap.get(key);
            if (value != null) {
                sessionMap.set(key, value);
            }
        });
    }
}

