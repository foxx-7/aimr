package com.aimr.notify.constant;

public interface ErrorConstants {

    String BLANK_NAME_FIELD_ERROR="Name field cannot be blank or empty";
    String BLANK_EMAIL_FIELD_ERROR="Email field cannot be blank or empty";

    String TEMPLATE_ALREADY_EXISTS_ERROR = "Template with provided properties already exists";
    String TEMPLATE_NOT_FOUND_ERROR = "Template with given credentials not found";
    String TEMPLATE_VARIABLE_ERROR = "Template variable is required! And max size should be 20";

    String PUT_CACHING_ERROR = "Error while caching data";
    String CACHE_PARSING_ERROR = "Error while parsing cache data";

    String SEND_NOTIFICATION_TEMPLATE_VARIABLE_ERROR = "Dynamic template variable is required!";
    String NOTIFICATION_NOT_FOUND_ERROR="Notification with provided credentials not found";

    String NAME_VARIABLE_ERROR = "Template name length cannot be more than 100";
    String MESSAGE_VARIABLE_ERROR = "Message template length cannot be more than 10000";

    String TENANT_RESOURCE_ACCESS_BREECH_ERROR="Access Denied: Resource belongs to another tenant";
    String TENANT_ALREADY_EXISTS_ERROR="Tenant with provided credentials already exists";
    String TENANT_NOT_FOUND_ERROR="Tenant with provided credentials not found";
    String MISSING_TENANT_OWNER_ID_ERROR="Owner id is required for tenant registration";
    String MEMBERSHIP_ALREADY_EXISTS_ERROR="Tenant membership for user with provided credentials already exists";

    String INVALID_UPDATE_FIELD_ERROR="Invalid update field provided";

    String USER_NOT_FOUND_ERROR="User with provided credentials not found";
    String USER_MEMBERSHIP_NOT_FOUND_ERROR="Membership for user with provided credentials not found";
    String INVITATION_ALREADY_SENT_ERROR="This invitation has already been sent";
    String EMAIL_ALREADY_IN_USE_ERROR="Provided email already in use";

    String KAFKA_PUBLISH_FAILURE_ERROR="Failed to publish to kafka queue: ";
    String CHANNEL_RATE_LIMIT_ERROR="Rate limit exceeded for channel '%s'. " +
            "Maximum %d requests per hour. Please retry in the next hour window.";
    String DUPLICATE_NOTIFICATION_REQUEST_ERROR="Duplicate notification request detected. The same notification was already dispatched. " +
            "You may resend %d minute(s) after initial send.";
    String NOTIFICATION_GUARD_CHECK_ERROR="Notification guard check failed due to an internal error. Please try again.";
}
