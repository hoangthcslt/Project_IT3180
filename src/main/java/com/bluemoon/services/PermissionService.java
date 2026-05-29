package com.bluemoon.services;

import com.bluemoon.models.Permission;
import com.bluemoon.models.User;
import com.bluemoon.models.UserGroup;
import com.bluemoon.models.UserGroupAssignment;
import com.bluemoon.repositories.PermissionRepository;

import java.util.List;
import java.util.Set;

public class PermissionService {
    private final PermissionRepository repository;

    public PermissionService() {
        this.repository = new PermissionRepository();
    }

    public List<UserGroup> findGroups(String keyword) {
        return repository.findGroups(keyword);
    }

    public boolean saveGroup(UserGroup group) {
        if (group.getTenNhom() == null || group.getTenNhom().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên nhóm không được để trống.");
        }
        if (group.getMoTa() == null || group.getMoTa().trim().isEmpty()) {
            throw new IllegalArgumentException("Mô tả không được để trống.");
        }
        group.setTenNhom(group.getTenNhom().trim());
        group.setMoTa(group.getMoTa().trim());
        return group.getId() == 0 ? repository.insertGroup(group) : repository.updateGroup(group);
    }

    public boolean deleteGroup(int groupId) {
        return repository.deleteGroup(groupId);
    }

    public List<Permission> getSystemPermissions() {
        return repository.getSystemPermissions();
    }

    public Set<String> getPermissionCodesByGroup(int groupId) {
        return repository.getPermissionCodesByGroup(groupId);
    }

    public Set<String> getPermissionCodesByUser(int userId) {
        return repository.getPermissionCodesByUser(userId);
    }

    public boolean hasUserGroup(int userId) {
        return repository.hasUserGroup(userId);
    }

    public void saveGroupPermissions(int groupId, Set<String> permissionCodes) {
        repository.saveGroupPermissions(groupId, permissionCodes);
    }

    public List<UserGroupAssignment> findUserAssignments(String keyword) {
        return repository.findUserAssignments(keyword);
    }

    public List<User> findAllUsers() {
        return repository.findAllUsers();
    }

    public void assignUserToGroup(int userId, int groupId) {
        repository.assignUserToGroup(userId, groupId);
    }

    public void updateUserAndGroup(int userId, String username, String role, int groupId) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username khong duoc de trong.");
        }
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Vai tro khong duoc de trong.");
        }
        if (groupId <= 0) {
            throw new IllegalArgumentException("Nhom khong hop le.");
        }
        repository.updateUserAndGroup(userId, username.trim(), role.trim(), groupId);
    }

    public void insertUserAndGroup(String username, String password, String role, int groupId) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username khong duoc de trong.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password khong duoc de trong.");
        }
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Vai tro khong duoc de trong.");
        }
        if (groupId <= 0) {
            throw new IllegalArgumentException("Nhom khong hop le.");
        }

        repository.insertUserAndGroup(username.trim(), password.trim(), role.trim(), groupId);
    }

    public void deleteUser(int userId) {
        repository.deleteUser(userId);
    }
}
