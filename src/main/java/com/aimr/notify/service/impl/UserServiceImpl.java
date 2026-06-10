package com.aimr.notify.service.impl;

import com.aimr.notify.dao.interfaces.InvitationDao;
import com.aimr.notify.dao.interfaces.TenantMembershipDao;
import com.aimr.notify.dao.interfaces.UserDao;
import com.aimr.notify.exception.ResourceNotFoundException;
import com.aimr.notify.exception.ValidationException;
import com.aimr.notify.models.dto.request.InviteUserRequest;
import com.aimr.notify.models.dto.response.TenantMembershipResponse;
import com.aimr.notify.models.dto.response.AuthenticatedUserDetails;
import com.aimr.notify.models.entity.Invitation;
import com.aimr.notify.models.entity.TenantMembership;
import com.aimr.notify.models.entity.User;
import com.aimr.notify.models.enums.InvitationStatus;
import com.aimr.notify.models.enums.MembershipStatus;
import com.aimr.notify.models.enums.Role;
import com.aimr.notify.service.interfaces.UserService;
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

import static com.aimr.notify.constants.ErrorConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService , UserDetailsService {

    private final UserDao userDao;
    private final TenantMembershipDao membershipDao;
    private final InvitationDao invitationDao;


    @Override
    @NullMarked
    public AuthenticatedUserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        User user = userDao.findByEmail(email).orElseThrow(()->
                new UsernameNotFoundException(USER_NOT_FOUND_ERROR));
        return new AuthenticatedUserDetails(user);
    }


    @Transactional
    @Override
    public void sendInvitation(final InviteUserRequest request) {
        String tenantId = CommonUtils.getCurrentTenantId();

        invitationDao.findInvitationByEmailAndTenantId(request.getEmail(), tenantId).ifPresent(_ -> {
            throw new ValidationException(INVITATION_ALREADY_SENT_ERROR, HttpStatus.BAD_REQUEST.value());
        });

        userDao.findByEmail(request.getEmail()).ifPresentOrElse(
            existingUser -> {
                membershipDao.findMembershipByUserIdAndTenantId(existingUser.getId(), tenantId).ifPresent(_ -> {
                    throw new ValidationException(USER_ALREADY_EXISTS_ERROR, HttpStatus.BAD_REQUEST.value());
                });
                TenantMembership membership = TenantMembership.builder()
                        .id(CommonUtils.generateUUIDv7())
                        .userId(existingUser.getId())
                        .tenantId(tenantId)
                        .role(request.getRole())
                        .status(MembershipStatus.INVITED)
                        .invitedByUserId(CommonUtils.getCurrentUserId())
                        .build();
                membershipDao.saveMembership(membership);
            },
            () -> {
                Invitation invitation = Invitation.builder()
                        .id(CommonUtils.generateUUIDv7())
                        .email(request.getEmail())
                        .tenantId(tenantId)
                        .role(request.getRole())
                        .token(UUID.randomUUID().toString())
                        .invitedByUserId(CommonUtils.getCurrentUserId())
                        .status(InvitationStatus.PENDING)
                        .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                        .build();
                invitationDao.saveInvitation(invitation);
            }
        );
    }

    @Override
    public TenantMembershipResponse fetchMembershipByUserIdAndTenantId(final String userId, final String tenantId) {
        TenantMembership membership = membershipDao.findMembershipByUserIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_MEMBERSHIP_NOT_FOUND_ERROR, HttpStatus.NOT_FOUND.value()));
        return new TenantMembershipResponse(membership);
    }

    @Override
    public void renounceUserMembership(final String userId, final String tenantId) {
        TenantMembership membership = membershipDao.findMembershipByUserIdAndTenantId(userId, tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(USER_MEMBERSHIP_NOT_FOUND_ERROR, HttpStatus.NOT_FOUND.value()));
        if(membership.getRole() == Role.OWNER){
            throw new ValidationException("Only non-owner memberships can be revoked", HttpStatus.BAD_REQUEST.value());
        }
        membershipDao.deleteMembership(membership);
    }

    @Override
    public void updateMembershipPrivilege(final String userId, final Role role) {
        TenantMembership membership = membershipDao.findMembershipByUserIdAndTenantId(userId, CommonUtils.getCurrentTenantId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(USER_MEMBERSHIP_NOT_FOUND_ERROR, HttpStatus.NOT_FOUND.value()));
        membership.setRole(role);
        membershipDao.saveMembership(membership);
    }
}
