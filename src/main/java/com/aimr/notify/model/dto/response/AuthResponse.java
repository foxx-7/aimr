package com.aimr.notify.model.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String email;
    private List<TenantMembershipResponse> membershipDetails;

}