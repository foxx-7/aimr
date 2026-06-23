package com.aimr.notify.api.dto.request;



public record UpdateBindingRequest(
    String name,
    String bindingAddress
) {
}