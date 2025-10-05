package com.digitopia.common.constants;

import java.util.UUID;

public final class AppConstants {
    private AppConstants() {}

    public static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLE = "X-User-Role";


    public static final int INVITATION_EXPIRY_DAYS = 7;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
}
