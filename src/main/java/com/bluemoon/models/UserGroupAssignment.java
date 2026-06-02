package com.bluemoon.models;

public class UserGroupAssignment {
    private int userId;
    private String username;
    private int groupId;
    private String groupName;
    private String role;

    public UserGroupAssignment(int userId, String username, int groupId, String groupName, String role) {
        this.userId = userId;
        this.username = username;
        this.groupId = groupId;
        this.groupName = groupName;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public int getGroupId() { return groupId; }
    public String getGroupName() { return groupName; }
    public String getRole() { return role; }
}
