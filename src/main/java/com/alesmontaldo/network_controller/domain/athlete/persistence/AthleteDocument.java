package com.alesmontaldo.network_controller.domain.athlete.persistence;

import com.alesmontaldo.network_controller.domain.activity.persistence.ActivityDocument;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "athletes")
public class AthleteDocument {
    
    @Id
    private String id;
    private String firstName;
    private String lastName;
    
    @DocumentReference
    private List<ActivityDocument> activities = new ArrayList<>();
    
    public AthleteDocument() {
    }
    
    public AthleteDocument(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public List<ActivityDocument> getActivities() {
        return activities;
    }
    
    public void setActivities(List<ActivityDocument> activities) {
        this.activities = activities;
    }
    
    public void addActivity(ActivityDocument activity) {
        this.activities.add(activity);
    }
}
