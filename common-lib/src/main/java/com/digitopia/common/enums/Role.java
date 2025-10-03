package com.digitopia.common.enums;

public enum Role {

    ADMIN,
    MANAGER,
    USER;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
