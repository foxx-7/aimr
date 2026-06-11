package com.aimr.notify.constant;

public interface ApplicationConstants {

    String X_TENANT_ID = "x-tenant-id";
    String X_REQUEST_ID = "x-request-id";

    String AUTH_V1_PATHS="/api/v1/auth/**";
    String REGISTER_TENANT_V1_PATH="/api/v1/tenants";

    Integer NOTIFICATION_SEARCH_WINDOW_HOURS=24;
    Integer NOTIFICATION_SEARCH_PAGE_SIZE=50;

    String CURSOR_ENCODING_FAILURE_ERROR="failure to encode pagination cursor";
    String CURSOR_DECODING_FAILURE_ERROR="invalid or tampered cursor value";

    String OBJECT_CREATED_SUCCESS_MESSAGE="object creation successful";
    String OBJECT_RETRIEVAL_SUCCESS_MESSAGE="object retrieval successful";
    String OBJECT_DELETION_SUCCESS_MESSAGE="object deletion successful";
    String OBJECT_UPDATED_SUCCESS_MESSAGE="object update successful";

    int MAX_EMAIL_RETRIES=3;

    int PASSWORD_ENCODER_SALT_LENGTH=16;
    int PASSWORD_ENCODER_HASH_LENGTH=32;
    int PASSWORD_ENCODER_PARALLELISM=1;
    int PASSWORD_ENCODER_ITERATION_COUNT=2;
    int PASSWORD_ENCODER_MEMORY_SIZE=19456;

    String DUPLICATE_KEY_PREFIX = "notif:dedup:";
    String RATE_LIMiT__KEY_PREFIX  = "notif:rl:";
    int RATE_LIMIT_WINDOW_SECONDS = 3600;



}
