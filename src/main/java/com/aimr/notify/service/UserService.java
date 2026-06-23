package com.aimr.notify.service;

import com.aimr.notify.domain.aop.annotation.ValidateTenant;
import com.aimr.notify.infra.postgres.dao.InvitationDao;
import com.aimr.notify.infra.postgres.dao.TenantMembershipDao;
import com.aimr.notify.infra.postgres.dao.UserDao;
import com.aimr.notify.exception.ResourceNotFoundException;
import com.aimr.notify.exception.ValidationException;
import com.aimr.notify.api.dto.request.InviteUserRequest;
import com.aimr.notify.api.dto.response.TenantMembershipResponse;
import com.aimr.notify.api.dto.response.AuthenticatedUserDetails;
import com.aimr.notify.domain.entity.TeamInvitation;
import com.aimr.notify.domain.entity.TenantMembership;
import com.aimr.notify.domain.entity.User;
import com.aimr.notify.domain.enums.InvitationStatus;
import com.aimr.notify.domain.enums.MembershipStatus;
import com.aimr.notify.domain.enums.Role;
import com.aimr.notify.util.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static com.aimr.notify.constant.ErrorConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserDao userDao;
    private final TenantMembershipDao membershipDao;
    private final InvitationDao invitationDao;


    @Override
    @NullMarked
    public AuthenticatedUserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        User user = userDao.fetchUserByEmail(email).orElseThrow(()->
                new UsernameNotFoundException(USER_NOT_FOUND_ERROR));
        return AuthenticatedUserDetails.from(user);
    }


    @Transactional
    @ValidateTenant
    public void sendInvitation(final InviteUserRequest request) {
        String tenantId = CommonUtils.getCurrentTenantId();

        invitationDao.fetchInvitationByEmailAndTenantId(request.email(), tenantId).ifPresent(_ -> {
            throw new ValidationException(INVITATION_ALREADY_SENT_ERROR, HttpStatus.BAD_REQUEST.value());
        });

        userDao.fetchUserByEmail(request.email()).ifPresentOrElse(
            existingUser -> {
                membershipDao.fetchMembershipByUserIdAndTenantId(existingUser.getId(), tenantId).ifPresent(_ -> {
                    throw new ValidationException(MEMBERSHIP_ALREADY_EXISTS_ERROR, HttpStatus.BAD_REQUEST.value());
                });
                TenantMembership membership = TenantMembership.builder()
                        .id(CommonUtils.generateUUIDv7())
                        .userId(existingUser.getId())
                        .tenantId(tenantId)
                        .role(request.role())
                        .status(MembershipStatus.INVITED)
                        .invitedByUserId(CommonUtils.getCurrentUserId())
                        .build();
                membershipDao.saveEntity(membership);
            },
            () -> {
                TeamInvitation teamInvitation = TeamInvitation.builder()
                        .id(CommonUtils.generateUUIDv7())
                        .email(request.email())
                        .tenantId(tenantId)
                        .role(request.role())
                        .token(UUID.randomUUID().toString())
                        .invitedByUserId(CommonUtils.getCurrentUserId())
                        .status(InvitationStatus.PENDING)
                        .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                        .build();
                invitationDao.saveInvitation(teamInvitation);
            }
        );
    }

    @ValidateTenant
    public TenantMembershipResponse loadMembershipByUserIdAndTenantId(final String userId, final String tenantId) {
        TenantMembership membership = membershipDao.fetchMembershipByUserIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_MEMBERSHIP_NOT_FOUND_ERROR, HttpStatus.NOT_FOUND.value()));
        return TenantMembershipResponse.from(membership);
    }

    @ValidateTenant
    public void renounceUserMembership(final String userId, final String tenantId) {
        TenantMembership membership = membershipDao.fetchMembershipByUserIdAndTenantId(userId, tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(USER_MEMBERSHIP_NOT_FOUND_ERROR, HttpStatus.NOT_FOUND.value()));
        if(membership.getRole() == Role.OWNER){
            throw new ValidationException("Only non-owner memberships can be revoked", HttpStatus.BAD_REQUEST.value());
        }
        membershipDao.deleteMembership(membership);
    }

    @ValidateTenant
    public void updateMembershipPrivilege(final String userId, final Role role) {
        TenantMembership membership = membershipDao.fetchMembershipByUserIdAndTenantId(userId, CommonUtils.getCurrentTenantId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(USER_MEMBERSHIP_NOT_FOUND_ERROR, HttpStatus.NOT_FOUND.value()));
        membership.setRole(role);
        membershipDao.saveEntity(membership);
    }

    public void deleteUser(final String userId){
        //Todo: cascade delete
    }
}
