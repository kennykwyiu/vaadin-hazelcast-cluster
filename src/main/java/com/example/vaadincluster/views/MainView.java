package com.example.vaadincluster.views;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cluster.Member;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * Main view for testing Hazelcast session clustering with Vaadin.
 * 
 * This view demonstrates:
 * - Session data persistence across cluster nodes
 * - Cluster member information
 * - Session counter functionality
 * - Real-time cluster status
 */
@Route("")
@PageTitle("Vaadin Hazelcast Cluster Demo")
public class MainView extends VerticalLayout {

    private final HazelcastInstance hazelcastInstance;
    
    private Div sessionInfoDiv;
    private Div clusterInfoDiv;
    private Div counterDiv;
    private TextField userNameField;
    private Button incrementButton;
    private Button resetButton;
    private Button refreshButton;
    
    private static final String SESSION_COUNTER_KEY = "sessionCounter";
    private static final String SESSION_USERNAME_KEY = "sessionUsername";
    private static final String SESSION_CREATED_KEY = "sessionCreated";

    @Autowired
    public MainView(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
        
        initializeView();
        updateDisplays();
    }
    
    private void initializeView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        // Header
        H1 title = new H1("Vaadin 24 + Hazelcast Cluster Demo");
        title.getStyle().set("color", "#1976d2");
        
        Paragraph description = new Paragraph(
            "This application demonstrates session sharing across multiple Vaadin application instances " +
            "using Hazelcast distributed session management with embedded Tomcat clustering."
        );
        description.getStyle().set("color", "#666");
        
        // Session Information Section
        H2 sessionHeader = new H2("Session Information");
        sessionInfoDiv = new Div();
        sessionInfoDiv.getStyle()
                .set("border", "1px solid #ddd")
                .set("padding", "15px")
                .set("border-radius", "5px")
                .set("background-color", "#f9f9f9");
        
        // Cluster Information Section
        H2 clusterHeader = new H2("Cluster Information");
        clusterInfoDiv = new Div();
        clusterInfoDiv.getStyle()
                .set("border", "1px solid #ddd")
                .set("padding", "15px")
                .set("border-radius", "5px")
                .set("background-color", "#f0f8ff");
        
        // Session Testing Section
        H2 testingHeader = new H2("Session Testing");
        
        userNameField = new TextField("Your Name");
        userNameField.setPlaceholder("Enter your name");
        userNameField.setWidth("300px");
        
        counterDiv = new Div();
        counterDiv.getStyle()
                .set("font-size", "18px")
                .set("font-weight", "bold")
                .set("margin", "10px 0");
        
        // Buttons
        incrementButton = new Button("Increment Counter", e -> incrementCounter());
        incrementButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        resetButton = new Button("Reset Session", e -> resetSession());
        resetButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        
        refreshButton = new Button("Refresh Info", e -> updateDisplays());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        
        Button saveNameButton = new Button("Save Name", e -> saveName());
        saveNameButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        
        HorizontalLayout buttonLayout = new HorizontalLayout(
            incrementButton, resetButton, refreshButton, saveNameButton
        );
        buttonLayout.setSpacing(true);
        
        HorizontalLayout nameLayout = new HorizontalLayout(userNameField, saveNameButton);
        nameLayout.setAlignItems(Alignment.END);
        
        // Add all components
        add(
            title,
            description,
            sessionHeader,
            sessionInfoDiv,
            clusterHeader,
            clusterInfoDiv,
            testingHeader,
            nameLayout,
            counterDiv,
            buttonLayout
        );
        
        // Initialize session if needed
        initializeSession();
    }
    
    private void initializeSession() {
        jakarta.servlet.http.HttpSession session = (jakarta.servlet.http.HttpSession) VaadinSession.getCurrent().getSession();
        
        if (session.getAttribute(SESSION_COUNTER_KEY) == null) {
            session.setAttribute(SESSION_COUNTER_KEY, 0);
            session.setAttribute(SESSION_CREATED_KEY, LocalDateTime.now().toString());
        }
    }
    
    private void incrementCounter() {
        jakarta.servlet.http.HttpSession session = (jakarta.servlet.http.HttpSession) VaadinSession.getCurrent().getSession();
        Integer counter = (Integer) session.getAttribute(SESSION_COUNTER_KEY);
        counter = (counter == null) ? 1 : counter + 1;
        session.setAttribute(SESSION_COUNTER_KEY, counter);
        
        updateDisplays();
        
        Notification notification = Notification.show("Counter incremented to " + counter);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
    
    private void resetSession() {
        jakarta.servlet.http.HttpSession session = (jakarta.servlet.http.HttpSession) VaadinSession.getCurrent().getSession();
        session.invalidate();
        
        // Refresh the page
        getUI().ifPresent(ui -> ui.getPage().reload());
        
        Notification notification = Notification.show("Session reset!");
        notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
    }
    
    private void saveName() {
        String name = userNameField.getValue();
        if (name != null && !name.trim().isEmpty()) {
            jakarta.servlet.http.HttpSession session = (jakarta.servlet.http.HttpSession) VaadinSession.getCurrent().getSession();
            session.setAttribute(SESSION_USERNAME_KEY, name.trim());
            updateDisplays();
            
            Notification notification = Notification.show("Name saved: " + name);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } else {
            Notification notification = Notification.show("Please enter a valid name");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
    
    private void updateDisplays() {
        updateSessionInfo();
        updateClusterInfo();
        updateCounter();
    }
    
    private void updateSessionInfo() {
        jakarta.servlet.http.HttpSession session = (jakarta.servlet.http.HttpSession) VaadinSession.getCurrent().getSession();
        
        String sessionId = session.getId();
        Integer counter = (Integer) session.getAttribute(SESSION_COUNTER_KEY);
        String username = (String) session.getAttribute(SESSION_USERNAME_KEY);
        String created = (String) session.getAttribute(SESSION_CREATED_KEY);
        
        StringBuilder info = new StringBuilder();
        info.append("<strong>Session ID:</strong> ").append(sessionId).append("<br>");
        info.append("<strong>Counter Value:</strong> ").append(counter != null ? counter : 0).append("<br>");
        info.append("<strong>Username:</strong> ").append(username != null ? username : "Not set").append("<br>");
        info.append("<strong>Session Created:</strong> ").append(created != null ? created : "Unknown").append("<br>");
        info.append("<strong>Max Inactive Interval:</strong> ").append(session.getMaxInactiveInterval()).append(" seconds<br>");
        info.append("<strong>Last Accessed:</strong> ").append(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
        
        sessionInfoDiv.getElement().setProperty("innerHTML", info.toString());
    }
    
    private void updateClusterInfo() {
        Set<Member> members = hazelcastInstance.getCluster().getMembers();
        Member localMember = hazelcastInstance.getCluster().getLocalMember();
        
        StringBuilder info = new StringBuilder();
        info.append("<strong>Cluster Name:</strong> ").append(hazelcastInstance.getConfig().getClusterName()).append("<br>");
        info.append("<strong>Instance Name:</strong> ").append(hazelcastInstance.getName()).append("<br>");
        info.append("<strong>Local Member:</strong> ").append(localMember.getAddress()).append("<br>");
        info.append("<strong>Cluster Size:</strong> ").append(members.size()).append(" members<br>");
        info.append("<strong>Cluster Members:</strong><br>");
        
        for (Member member : members) {
            String memberInfo = member.getAddress().toString();
            if (member.equals(localMember)) {
                memberInfo += " <strong>(Local)</strong>";
            }
            info.append("&nbsp;&nbsp;• ").append(memberInfo).append("<br>");
        }
        
        info.append("<strong>Cluster State:</strong> ").append(hazelcastInstance.getCluster().getClusterState()).append("<br>");
        info.append("<strong>Cluster Time:</strong> ").append(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
        
        clusterInfoDiv.getElement().setProperty("innerHTML", info.toString());
    }
    
    private void updateCounter() {
        jakarta.servlet.http.HttpSession session = (jakarta.servlet.http.HttpSession) VaadinSession.getCurrent().getSession();
        Integer counter = (Integer) session.getAttribute(SESSION_COUNTER_KEY);
        
        counterDiv.removeAll();
        counterDiv.add(new Span("Current Counter Value: " + (counter != null ? counter : 0)));
    }
}

