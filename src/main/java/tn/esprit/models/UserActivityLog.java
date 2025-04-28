package tn.esprit.models;

import java.sql.Timestamp;

public class UserActivityLog {
    private int id;
    private int userId;
    private String action;
    private String details;
    private Timestamp timestamp;

    public UserActivityLog(int id, int userId, String action, String details, Timestamp timestamp) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.details = details;
        this.timestamp = timestamp;
    }
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
    public Timestamp getTimestamp() { return timestamp; }
}
