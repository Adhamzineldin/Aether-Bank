package com.maayn.transactionservice.config;

import java.util.UUID;

public class Keys {
    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    
    public static UUID getSystemUserId() {
        return SYSTEM_USER_ID;
    }

}
