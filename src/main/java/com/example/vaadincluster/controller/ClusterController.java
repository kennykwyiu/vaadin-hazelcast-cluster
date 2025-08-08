package com.example.vaadincluster.controller;

import com.example.vaadincluster.service.SessionService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cluster.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller for cluster monitoring and session management.
 * 
 * Provides endpoints for:
 * - Cluster status and health checks
 * - Session information and statistics
 * - Testing session replication
 */
@RestController
@RequestMapping("/api/cluster")
@CrossOrigin(origins = "*")
public class ClusterController {

    private final HazelcastInstance hazelcastInstance;
    private final SessionService sessionService;

    @Autowired
    public ClusterController(HazelcastInstance hazelcastInstance, SessionService sessionService) {
        this.hazelcastInstance = hazelcastInstance;
        this.sessionService = sessionService;
    }

    /**
     * Get cluster information
     */
    @GetMapping("/info")
    public Map<String, Object> getClusterInfo() {
        Set<Member> members = hazelcastInstance.getCluster().getMembers();
        Member localMember = hazelcastInstance.getCluster().getLocalMember();

        Map<String, Object> info = new HashMap<>();
        info.put("clusterName", hazelcastInstance.getConfig().getClusterName());
        info.put("instanceName", hazelcastInstance.getName());
        info.put("localMember", localMember.getAddress().toString());
        info.put("clusterSize", members.size());
        info.put("members", members.stream()
                .map(member -> Map.of(
                        "address", member.getAddress().toString(),
                        "uuid", member.getUuid().toString(),
                        "isLocal", member.equals(localMember)
                ))
                .collect(Collectors.toList()));
        info.put("clusterState", hazelcastInstance.getCluster().getClusterState().toString());
        info.put("timestamp", LocalDateTime.now().toString());

        return info;
    }

    /**
     * Get session statistics
     */
    @GetMapping("/sessions")
    public Map<String, Object> getSessionStats() {
        return sessionService.getClusterStats();
    }

    /**
     * Get current session information
     */
    @GetMapping("/session/current")
    public Map<String, Object> getCurrentSession(HttpSession session) {
        Map<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("sessionId", session.getId());
        sessionInfo.put("creationTime", session.getCreationTime());
        sessionInfo.put("lastAccessedTime", session.getLastAccessedTime());
        sessionInfo.put("maxInactiveInterval", session.getMaxInactiveInterval());
        sessionInfo.put("isNew", session.isNew());

        // Get session attributes
        Map<String, Object> attributes = new HashMap<>();
        session.getAttributeNames().asIterator().forEachRemaining(name -> 
                attributes.put(name, session.getAttribute(name)));
        sessionInfo.put("attributes", attributes);

        return sessionInfo;
    }

    /**
     * Test session by setting a value
     */
    @PostMapping("/session/test")
    public Map<String, Object> testSession(
            @RequestParam String key,
            @RequestParam String value,
            HttpSession session) {
        
        session.setAttribute(key, value);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Session attribute set successfully");
        result.put("sessionId", session.getId());
        result.put("key", key);
        result.put("value", value);
        result.put("timestamp", LocalDateTime.now().toString());
        
        return result;
    }

    /**
     * Get session value by key
     */
    @GetMapping("/session/get/{key}")
    public Map<String, Object> getSessionValue(
            @PathVariable String key,
            HttpSession session) {
        
        Object value = session.getAttribute(key);
        
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", session.getId());
        result.put("key", key);
        result.put("value", value);
        result.put("found", value != null);
        result.put("timestamp", LocalDateTime.now().toString());
        
        return result;
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", sessionService.isClusterHealthy() ? "UP" : "DOWN");
        health.put("clusterSize", hazelcastInstance.getCluster().getMembers().size());
        health.put("activeSessions", sessionService.getTotalActiveSessions());
        health.put("timestamp", LocalDateTime.now().toString());
        
        return health;
    }

    /**
     * Force session replication
     */
    @PostMapping("/sessions/replicate")
    public Map<String, Object> forceReplication() {
        sessionService.forceSessionReplication();
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Session replication forced");
        result.put("activeSessions", sessionService.getTotalActiveSessions());
        result.put("timestamp", LocalDateTime.now().toString());
        
        return result;
    }
}

