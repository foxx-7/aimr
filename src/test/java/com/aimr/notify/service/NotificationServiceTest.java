package com.aimr.notify.service;

import com.aimr.notify.dao.interfaces.NotificationDao;
import com.aimr.notify.dao.interfaces.TemplateDao;
import com.aimr.notify.exception.NotificationDispatchException;
import com.aimr.notify.exception.ValidationException;
import com.aimr.notify.models.dto.ChannelStatusCount;
import com.aimr.notify.models.dto.IngestTopicDTO;
import com.aimr.notify.models.dto.NotificationCursor;
import com.aimr.notify.models.dto.NotificationSummary;
import com.aimr.notify.models.entity.IdempotencyKey;
import com.aimr.notify.models.entity.Notification;
import com.aimr.notify.models.entity.Template;
import com.aimr.notify.models.enums.*;
import com.aimr.notify.models.dto.request.SendNotificationRequest;
import com.aimr.notify.models.dto.response.NotificationSearchResponse;
import com.aimr.notify.pubsub.queue.publisher.interfaces.GenericPublisher;
import com.aimr.notify.service.impl.NotificationServiceImpl;
import com.aimr.notify.util.CommonUtils;
import lombok.NonNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.*;

import static com.aimr.notify.constants.ApplicationConstants.NOTIFICATION_SEARCH_PAGE_SIZE;
import static com.aimr.notify.models.enums.NotificationStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationServiceImpl.
 * Dependencies are mocked — no Spring context, no DB, no Kafka.
 * Static methods (CommonUtils) are mocked using Mockito's mockStatic().
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    // ── Mocks (fake versions of every dependency) ────────────────────────────

    @Mock
    private TemplateDao templateDao;

    @Mock
    private GenericPublisher genericPublisher;

    @Mock
    private NotificationDao notificationDao;

    @Mock
    private JsonMapper jsonMapper;

    @Mock
    private KafkaTemplate<@NonNull String, @NonNull Object> kafkaTemplate;

    // ── Class under test (Mockito injects the mocks above into this) ─────────

    @InjectMocks
    private NotificationServiceImpl notificationService;

    // ── Shared testdata.properties ─────────────────────────────────────────────────────

    private static final String TENANT_ID = "tenant-abc";
    private static final String REQUEST_ID = "req-uuid-v4";
    private static final String TRACE_ID = "trace-xyz";
    private static final String TEMPLATE_ID = "template-001";

    // ─────────────────────────────────────────────────────────────────────────
    // sendNotification()
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("sendNotification()")
    class SendNotification {

        // The request we'll reuse across tests
        private SendNotificationRequest request;

        // A template whose variables match the request
        private Template template;

        @BeforeEach
        void setup() {
            // Build a request with one dynamic variable: "name"
            request = new SendNotificationRequest();
            request.setTemplateId(TEMPLATE_ID);
            request.setDispatchChannel(NotificationChannel.EMAIL);
            request.setDynamicVariables(Map.of("name", "Kevin"));

            // Build a matching template that also expects "name"
            template = new Template();
            template.setId(TEMPLATE_ID);
            template.setName("Welcome Email");
            template.setMessageTemplate("Hello {{name}}");
            template.setTemplateVariables(Map.of("name", ""));
        }

        @Test
        @DisplayName("should return requestId when notification is queued successfully")
        void shouldReturnRequestIdOnSuccess() {
            // --- ARRANGE ---

            // mockStatic lets us control what static methods return
            try (MockedStatic<CommonUtils> utils = mockStatic(CommonUtils.class)) {
                utils.when(CommonUtils::getCurrentTenantId).thenReturn(TENANT_ID);
                utils.when(CommonUtils::getCurrentTraceId).thenReturn(TRACE_ID);
                utils.when(CommonUtils::generateUUIDv4).thenReturn(REQUEST_ID);
                utils.when(CommonUtils::generateUUIDv7).thenReturn("notif-uuid-v7");

                // templateDao.findTemplateByTenantIdAndId() returns our template
                when(templateDao.findTemplateByTenantIdAndId(TENANT_ID, TEMPLATE_ID))
                        .thenReturn(Optional.of(template));

                // genericPublisher returns true for successful enqueue
                when(genericPublisher.sendDataToIngest(any())).thenReturn(true);

                // --- ACT ---
                String result = notificationService.sendNotification(request);

                // --- ASSERT ---

                // 1. The method returns the requestId
                assertEquals(REQUEST_ID, result);

                // 2. The notification was saved to MongoDB once
                verify(notificationDao, times(1)).saveNotification(any(Notification.class));

                // 3. The event was published to Kafka once
                verify(genericPublisher, times(1)).sendDataToIngest(any(IngestTopicDTO.class));
            }
        }

        @Test
        @DisplayName("should throw ValidationException when template is not found")
        void shouldThrowWhenTemplateNotFound() {
            try (MockedStatic<CommonUtils> utils = mockStatic(CommonUtils.class)) {
                utils.when(CommonUtils::getCurrentTenantId).thenReturn(TENANT_ID);

                // templateDao returns empty — template doesn't exist
                when(templateDao.findTemplateByTenantIdAndId(TENANT_ID, TEMPLATE_ID))
                        .thenReturn(Optional.empty());

                // --- ASSERT ---
                // The method must throw ValidationException
                assertThrows(ValidationException.class,
                        () -> notificationService.sendNotification(request));

                // Nothing should be saved or published
                verify(notificationDao, never()).saveNotification(any());
                verify(genericPublisher, never()).sendDataToIngest(any());
            }
        }

        @Test
        @DisplayName("should throw ValidationException when dynamic variables do not match template")
        void shouldThrowWhenDynamicVariablesMismatch() {
            try (MockedStatic<CommonUtils> utils = mockStatic(CommonUtils.class)) {
                utils.when(CommonUtils::getCurrentTenantId).thenReturn(TENANT_ID);

                // Template expects "name" AND "company", but request only sends "name"
                template.setTemplateVariables(Map.of("name", "", "company", ""));
                when(templateDao.findTemplateByTenantIdAndId(TENANT_ID, TEMPLATE_ID))
                        .thenReturn(Optional.of(template));

                assertThrows(ValidationException.class,
                        () -> notificationService.sendNotification(request));

                verify(notificationDao, never()).saveNotification(any());
                verify(genericPublisher, never()).sendDataToIngest(any());
            }
        }

        @Test
        @DisplayName("should mark notification FAILED and throw when Kafka publish fails")
        void shouldMarkFailedAndThrowWhenKafkaFails() {
            try (MockedStatic<CommonUtils> utils = mockStatic(CommonUtils.class)) {
                utils.when(CommonUtils::getCurrentTenantId).thenReturn(TENANT_ID);
                utils.when(CommonUtils::getCurrentTraceId).thenReturn(TRACE_ID);
                utils.when(CommonUtils::generateUUIDv4).thenReturn(REQUEST_ID);
                utils.when(CommonUtils::generateUUIDv7).thenReturn("notif-uuid-v7");

                when(templateDao.findTemplateByTenantIdAndId(TENANT_ID, TEMPLATE_ID))
                        .thenReturn(Optional.of(template));

                // Capture status at the time saveNotification is called to handle in-place mutation
                List<NotificationStatus> capturedStatuses = new ArrayList<>();
                doAnswer(invocation -> {
                    Notification n = invocation.getArgument(0);
                    capturedStatuses.add(n.getStatus());
                    return null;
                }).when(notificationDao).saveNotification(any(Notification.class));

                // Kafka explodes
                doThrow(new RuntimeException("Kafka broker unreachable"))
                        .when(genericPublisher).sendDataToIngest(any());

                // --- ASSERT ---
                assertThrows(NotificationDispatchException.class,
                        () -> notificationService.sendNotification(request));

                /*
                 * saveNotification should be called TWICE:
                 *   1st call → save PENDING notification before Kafka publish
                 *   2nd call → save FAILED notification after Kafka blows up
                 */
                verify(notificationDao, times(2)).saveNotification(any(Notification.class));

                assertEquals(2, capturedStatuses.size());
                assertEquals(PENDING, capturedStatuses.get(0)); // 1st save: PENDING
                assertEquals(FAILED, capturedStatuses.get(1)); // 2nd save: FAILED
            }
        }

        @Test
        @DisplayName("should mark notification FAILED and throw when Kafka publish returns false")
        void shouldMarkFailedAndThrowWhenKafkaPublishReturnsFalse() {
            try (MockedStatic<CommonUtils> utils = mockStatic(CommonUtils.class)) {
                utils.when(CommonUtils::getCurrentTenantId).thenReturn(TENANT_ID);
                utils.when(CommonUtils::getCurrentTraceId).thenReturn(TRACE_ID);
                utils.when(CommonUtils::generateUUIDv4).thenReturn(REQUEST_ID);
                utils.when(CommonUtils::generateUUIDv7).thenReturn("notif-uuid-v7");

                when(templateDao.findTemplateByTenantIdAndId(TENANT_ID, TEMPLATE_ID))
                        .thenReturn(Optional.of(template));

                List<NotificationStatus> capturedStatuses = new ArrayList<>();
                doAnswer(invocation -> {
                    Notification n = invocation.getArgument(0);
                    capturedStatuses.add(n.getStatus());
                    return null;
                }).when(notificationDao).saveNotification(any(Notification.class));

                // Kafka returns false
                when(genericPublisher.sendDataToIngest(any())).thenReturn(false);

                assertThrows(NotificationDispatchException.class,
                        () -> notificationService.sendNotification(request));

                verify(notificationDao, times(2)).saveNotification(any(Notification.class));

                assertEquals(2, capturedStatuses.size());
                assertEquals(PENDING, capturedStatuses.get(0));
                assertEquals(FAILED, capturedStatuses.get(1));
            }
        }
    }

    @Nested
    @DisplayName("browseNotification()")//*passed*
    class BrowseNotification {

        @Test
        @DisplayName("should return hasMore=false and no cursor when results fit in one page")
        void shouldReturnSinglePageWhenResultsFit() {
            try (MockedStatic<CommonUtils> utils = mockStatic(CommonUtils.class)) {
                utils.when(CommonUtils::getCurrentTenantId).thenReturn(TENANT_ID);

                // Return fewer notifications than the page size
                List<Notification> fakeResults = buildNotifications(3);
                when(notificationDao.searchNotification(
                        eq(TENANT_ID), any(), isNull(), any(), any()))
                        .thenReturn(fakeResults);

                NotificationSearchResponse response = notificationService
                        .browseNotification(Instant.now(), null, null, null);

                assertFalse(response.isHasMore());
                assertNull(response.getNextCursor());
                assertEquals(3, response.getNotifications().size());
            }
        }

        @Test
        @DisplayName("should return hasMore=true and a cursor when more results exist")
        void shouldReturnNextCursorWhenMoreResultsExist() throws Exception {
            try (MockedStatic<CommonUtils> utils = mockStatic(CommonUtils.class)) {
                utils.when(CommonUtils::getCurrentTenantId).thenReturn(TENANT_ID);

                /*
                 * The service fetches pageSize + 1 records to detect if more exist.
                 * So we return pageSize + 1 notifications to trigger hasMore=true.
                 */
                int pageSize = NOTIFICATION_SEARCH_PAGE_SIZE;
                List<Notification> fakeResults = buildNotifications(pageSize + 1);

                when(notificationDao.searchNotification(
                        eq(TENANT_ID), any(), isNull(), any(), any()))
                        .thenReturn(fakeResults);

                // jsonMapper.writeValueAsString is called during cursor encoding
                when(jsonMapper.writeValueAsString(any())).thenReturn("{\"id\":\"x\"}");

                NotificationSearchResponse response = notificationService
                        .browseNotification(Instant.now(), null, null, null);

                assertTrue(response.isHasMore());
                assertNotNull(response.getNextCursor()); // cursor was encoded
                assertEquals(pageSize, response.getNotifications().size()); // only pageSize returned
            }
        }

        @Test
        @DisplayName("should decode cursor and pass it to the dao when cursor is provided")
        void shouldDecodeCursorAndPassToDao() throws Exception {
            try (MockedStatic<CommonUtils> utils = mockStatic(CommonUtils.class)) {
                utils.when(CommonUtils::getCurrentTenantId).thenReturn(TENANT_ID);

                // Build a real Base64-encoded cursor that the service can decode
                NotificationCursor cursorObj = new NotificationCursor(Instant.now(), "notif-123");
                String cursorJson = "{\"createdAt\":\"" + cursorObj.getCreatedAt() + "\",\"id\":\"notif-123\"}";
                String encodedCursor = Base64.getEncoder()
                        .encodeToString(cursorJson.getBytes());

                when(jsonMapper.readValue(eq(cursorJson), eq(NotificationCursor.class)))
                        .thenReturn(cursorObj);
                when(notificationDao.searchNotification(
                        eq(TENANT_ID), any(), eq(cursorObj), any(), any()))
                        .thenReturn(List.of());

                notificationService.browseNotification(Instant.now(), null, null, encodedCursor);

                // Verify the decoded cursor was forwarded to the DAO
                verify(notificationDao).searchNotification(
                        eq(TENANT_ID), any(), eq(cursorObj), any(), any());
            }
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when cursor is malformed")
        void shouldThrowWhenCursorIsMalformed() {
            try (MockedStatic<CommonUtils> utils = mockStatic(CommonUtils.class)) {
                utils.when(CommonUtils::getCurrentTenantId).thenReturn(TENANT_ID);

                // Not valid Base64
                String badCursor = "!!!not-base64!!!";

                assertThrows(IllegalArgumentException.class,
                        () -> notificationService.browseNotification(
                                Instant.now(), null, null, badCursor));
            }
        }
    }

    @Nested
    @DisplayName("getNotificationSummary()")
    class GetNotificationSummary {

        @Test
        @DisplayName("should return 0 overall rate when there are no notifications")
        void shouldReturnZeroRateWhenNoCounts() {
            try (MockedStatic<CommonUtils> utils = mockStatic(CommonUtils.class)) {
                utils.when(CommonUtils::getCurrentTenantId).thenReturn(TENANT_ID);

                // DAO returns empty list — no notifications at all
                when(notificationDao.getStatusCountsByChannel(eq(TENANT_ID), any()))
                        .thenReturn(List.of());

                NotificationSummary summary = notificationService
                        .getNotificationSummary(SummaryWindow.LAST_24_HOURS);

                assertEquals(0L, summary.grandTotalSent());
                assertEquals(0.0, summary.overallDeliveryRate());
            }
        }

        @Test
        @DisplayName("should calculate correct delivery rate from channel counts")
        void shouldCalculateDeliveryRateCorrectly() {
            try (MockedStatic<CommonUtils> utils = mockStatic(CommonUtils.class)) {
                utils.when(CommonUtils::getCurrentTenantId).thenReturn(TENANT_ID);

                // 75 delivered, 25 failed → 75% delivery rate
                List<ChannelStatusCount> counts = List.of(
                        new ChannelStatusCount(NotificationChannel.EMAIL, DELIVERED, 75L),
                        new ChannelStatusCount(NotificationChannel.EMAIL, FAILED, 25L)
                );

                when(notificationDao.getStatusCountsByChannel(eq(TENANT_ID), any()))
                        .thenReturn(counts);

                NotificationSummary summary = notificationService
                        .getNotificationSummary(SummaryWindow.LAST_24_HOURS);

                assertEquals(100L, summary.grandTotalSent());
                assertEquals(75.0, summary.overallDeliveryRate(), 0.01); // allow tiny floating point delta
            }
        }
    }

    @Nested
    @DisplayName("markNotificationStatus()")
    class MarkNotificationStatus {

        @Test
        @DisplayName("should update status and save when notification is found")
        void shouldUpdateStatusWhenFound() {
            Notification notification = new Notification();
            notification.setRequestId(REQUEST_ID);
            notification.setStatus(PENDING);

            when(notificationDao.fetchNotificationByTenantIdAndRequestId(TENANT_ID, REQUEST_ID))
                    .thenReturn(Optional.of(notification));

            notificationService.markNotificationStatus(TENANT_ID, REQUEST_ID, "mail-id-123", DELIVERED);

            // Verify status was updated on the object
            assertEquals(DELIVERED, notification.getStatus());
            assertEquals("mail-id-123", notification.getMailId());

            // Verify save was called
            verify(notificationDao, times(1)).saveNotification(notification);
        }

        @Test
        @DisplayName("should not save when notification is not found (just logs warning)")
        void shouldNotSaveWhenNotFound() {
            when(notificationDao.fetchNotificationByTenantIdAndRequestId(TENANT_ID, REQUEST_ID))
                    .thenReturn(Optional.empty());

            // Should NOT throw — just log a warning
            assertDoesNotThrow(() ->
                    notificationService.markNotificationStatus(TENANT_ID, REQUEST_ID, null, FAILED));

            verify(notificationDao, never()).saveNotification(any());
        }

        @Test
        @DisplayName("should not set mailId when it is null")
        void shouldNotSetMailIdWhenNull() {
            Notification notification = new Notification();
            notification.setStatus(PENDING);

            when(notificationDao.fetchNotificationByTenantIdAndRequestId(TENANT_ID, REQUEST_ID))
                    .thenReturn(Optional.of(notification));

            notificationService.markNotificationStatus(TENANT_ID, REQUEST_ID, null, DELIVERED);

            assertNull(notification.getMailId()); // mailId should remain null
            assertEquals(DELIVERED, notification.getStatus());
        }
    }

    @Nested
    @DisplayName("getIdempotencyKey()")
    class GetIdempotencyKey {

        @Test
        @DisplayName("should return IdempotencyKey when notification exists")
        void shouldReturnIdempotencyKeyWhenFound() {
            Notification notification = new Notification();
            notification.setMailId("ses-mail-001");
            notification.setStatus(DELIVERED);

            when(notificationDao.fetchNotificationByTenantIdAndRequestId(TENANT_ID, REQUEST_ID))
                    .thenReturn(Optional.of(notification));

            IdempotencyKey key = notificationService.getIdempotencyKey(TENANT_ID, REQUEST_ID);

            assertEquals("ses-mail-001", key.getKey());
            assertEquals(DELIVERED, key.getNotificationStatus());
        }

        @Test
        @DisplayName("should throw ValidationException when notification does not exist")
        void shouldThrowWhenNotificationNotFound() {
            when(notificationDao.fetchNotificationByTenantIdAndRequestId(TENANT_ID, REQUEST_ID))
                    .thenReturn(Optional.empty());

            assertThrows(ValidationException.class,
                    () -> notificationService.getIdempotencyKey(TENANT_ID, REQUEST_ID));
        }
    }

    /**
     * Builds a list of dummy notifications for pagination tests.
     */
    private List<Notification> buildNotifications(int count) {
        List<Notification> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Notification n = new Notification();
            n.setId("notif-" + i);
            n.setCreatedAt(Instant.now());
            n.setStatus(DELIVERED);
            list.add(n);
        }
        return list;
    }
}