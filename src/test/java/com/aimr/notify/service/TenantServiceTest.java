package com.aimr.notify.service;

import com.aimr.notify.dao.interfaces.TenantMembershipDao;
import com.aimr.notify.dao.interfaces.ApiKeyDao;
import com.aimr.notify.dao.interfaces.CacheService;
import com.aimr.notify.dao.interfaces.TenantDao;
import com.aimr.notify.exception.ResourceNotFoundException;
import com.aimr.notify.exception.ValidationException;
import com.aimr.notify.model.entity.Tenant;
import com.aimr.notify.model.dto.request.RegisterTenantRequest;
import com.aimr.notify.model.dto.response.TenantResponse;
import com.aimr.notify.service.impl.TenantServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.aimr.notify.constant.ErrorConstants.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantDao tenantDao;

    @Mock
    private TenantMembershipDao membershipDao;

    @Mock
    private ApiKeyDao apiKeyDao;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private TenantServiceImpl tenantService;

    private static final String OWNER_ID = "owner-123";
    private static final String TENANT_ID = "tenant-456";
    private static final String TENANT_NAME = "Acme Corp";

    private Tenant mockTenant;

    @BeforeEach
    void setUp() {
        mockTenant = new Tenant();
        mockTenant.setId(TENANT_ID);
        mockTenant.setName(TENANT_NAME);
    }

    @Nested
    @DisplayName("registerTenant")
    class RegisterTenant {

        private RegisterTenantRequest request;

        @BeforeEach
        void setUp() {
            request = new RegisterTenantRequest();
            request.setName(TENANT_NAME);
        }

        @Test
        @DisplayName("should register tenant successfully when name is unique for owner")
        void registerTenant_success() {
            when(tenantDao.tenantExistsByName(TENANT_NAME.toLowerCase()))
                    .thenReturn(false);

            TenantResponse response = tenantService.registerTenant(OWNER_ID, request);

            assertThat(response.getOwnerId()).isNotNull();
            assertThat(response.getName()).isEqualTo(TENANT_NAME.toLowerCase());
            verify(tenantDao).tenantExistsByName(TENANT_NAME.toLowerCase());
        }

        @Test
        @DisplayName("should throw ValidationException when tenant with same name already exists for owner")
        void registerTenant_duplicateName_throwsValidationException() {
            when(tenantDao.tenantExistsByName(TENANT_NAME.toLowerCase()))
                    .thenReturn(true);

            assertThatThrownBy(() -> tenantService.registerTenant(OWNER_ID, request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining(TENANT_ALREADY_EXISTS_ERROR);
        }

        @Test
        @DisplayName("should generate a non-null UUID for the new tenant id")
        void registerTenant_generatesId() {
            when(tenantDao.tenantExistsByName(TENANT_NAME.toLowerCase()))
                    .thenReturn(false);

            TenantResponse response = tenantService.registerTenant(OWNER_ID, request);

            assertThat(response.getOwnerId()).isNotNull().isNotBlank();
        }
    }

    @Nested
    @DisplayName("deleteTenant")
    class DeleteTenant {

        @Test
        @DisplayName("should delete tenant when found for owner")
        void deleteTenant_success() {
            when(tenantDao.findTenantByIdAndOwnerId(TENANT_ID, OWNER_ID))
                    .thenReturn(Optional.of(mockTenant));

            tenantService.deleteTenant(TENANT_ID, OWNER_ID);

            verify(tenantDao).deleteTenant(mockTenant);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when tenant not found for owner")
        void deleteTenant_notFound_throwsResourceNotFoundException() {
            when(tenantDao.findTenantByIdAndOwnerId(TENANT_ID, OWNER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> tenantService.deleteTenant(TENANT_ID, OWNER_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(TENANT_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("should not call deleteTenant on dao when tenant is not found")
        void deleteTenant_notFound_doesNotCallDelete() {
            when(tenantDao.findTenantByIdAndOwnerId(TENANT_ID, OWNER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> tenantService.deleteTenant(TENANT_ID, OWNER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(tenantDao, never()).deleteTenant(any());
        }
    }

    @Nested
    @DisplayName("updateTenant")
    class UpdateTenant {

        @Test
        @DisplayName("should update tenant name successfully")
        void updateTenant_name_success() {
            when(tenantDao.findTenantByIdAndOwnerId(TENANT_ID, OWNER_ID))
                    .thenReturn(Optional.of(mockTenant));

            Map<String, Object> update = new HashMap<>();
            update.put("name", "New Name");

            TenantResponse response = tenantService.updateTenant(TENANT_ID, OWNER_ID, update);

            assertThat(response.getName()).isEqualTo("new name");
            verify(tenantDao).saveTenant(mockTenant);
        }

        @Test
        @DisplayName("should throw ValidationException when name is blank")
        void updateTenant_blankName_throwsValidationException() {
            when(tenantDao.findTenantByIdAndOwnerId(TENANT_ID, OWNER_ID))
                    .thenReturn(Optional.of(mockTenant));

            Map<String, Object> update = new HashMap<>();
            update.put("name", "   ");

            assertThatThrownBy(() -> tenantService.updateTenant(TENANT_ID, OWNER_ID, update))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining(BLANK_NAME_FIELD_ERROR);

            verify(tenantDao, never()).saveTenant(any());
        }

        @Test
        @DisplayName("should throw ValidationException for an unrecognised update field")
        void updateTenant_invalidField_throwsValidationException() {
            when(tenantDao.findTenantByIdAndOwnerId(TENANT_ID, OWNER_ID))
                    .thenReturn(Optional.of(mockTenant));

            Map<String, Object> update = new HashMap<>();
            update.put("nonExistentField", "value");

            assertThatThrownBy(() -> tenantService.updateTenant(TENANT_ID, OWNER_ID, update))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining(INVALID_UPDATE_FIELD_ERROR);

            verify(tenantDao, never()).saveTenant(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when tenant not found for owner")
        void updateTenant_notFound_throwsResourceNotFoundException() {
            when(tenantDao.findTenantByIdAndOwnerId(TENANT_ID, OWNER_ID))
                    .thenReturn(Optional.empty());

            Map<String, Object> update = Map.of("name", "Anything");

            assertThatThrownBy(() -> tenantService.updateTenant(TENANT_ID, OWNER_ID, update))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(TENANT_NOT_FOUND_ERROR);
        }

        @Test
        @DisplayName("should return updated TenantResponse after save")
        void updateTenant_returnsUpdatedResponse() {
            when(tenantDao.findTenantByIdAndOwnerId(TENANT_ID, OWNER_ID))
                    .thenReturn(Optional.of(mockTenant));

            Map<String, Object> update = Map.of("name", "Updated Name");

            TenantResponse response = tenantService.updateTenant(TENANT_ID, OWNER_ID, update);

            assertThat(response).isNotNull();
            assertThat(response.getName()).isEqualTo("updated name");
        }
    }
}
