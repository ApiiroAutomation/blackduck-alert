package com.synopsys.integration.alert.performance;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryPageModel;
import com.synopsys.integration.alert.common.rest.model.NotificationConfig;
import com.synopsys.integration.blackduck.api.manual.component.VulnerabilityNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.wait.WaitJobTask;

public class NotificationWaitJobTask implements WaitJobTask {
    private final IntLogger intLogger;
    private final DateTimeFormatter dateTimeFormatter;
    private final Gson gson;
    private final AlertRequestUtility alertRequestUtility;

    private final LocalDateTime startSearchTime;
    private final int numberOfJobsNeedToMatch;

    public NotificationWaitJobTask(IntLogger intLogger, DateTimeFormatter dateTimeFormatter, Gson gson, AlertRequestUtility alertRequestUtility, LocalDateTime startSearchTime, int numberOfJobsNeedToMatch) {
        this.intLogger = intLogger;
        this.dateTimeFormatter = dateTimeFormatter;
        this.gson = gson;
        this.alertRequestUtility = alertRequestUtility;
        this.startSearchTime = startSearchTime;
        this.numberOfJobsNeedToMatch = numberOfJobsNeedToMatch;
    }

    @Override
    public boolean isComplete() throws IntegrationException {
        return waitForNotificationToBeProcessedByAllJobs();
    }

    private boolean waitForNotificationToBeProcessedByAllJobs() throws IntegrationException {
        Response response = alertRequestUtility.executeGetRequest("api/audit?pageNumber=0&pageSize=2&searchTerm=VULNERABILITY&sortField=createdAt&sortOrder=desc&onlyShowSentNotifications=false", "Could not get the Alert audit entries.");
        String contentString = response.getContentString();
        AuditEntryPageModel auditEntryPageModel = gson.fromJson(contentString, AuditEntryPageModel.class);
        Optional<AuditEntryModel> matchingAuditEntry = auditEntryPageModel.getContent().stream()
                                                           .filter(auditEntryModel -> isNotificationAfterTime(startSearchTime, auditEntryModel.getNotification()))
                                                           .filter(auditEntryModel -> NotificationType.VULNERABILITY.name().equals(auditEntryModel.getNotification().getNotificationType()))
                                                           .filter(auditEntryModel -> isNotificationForNewVulnerabilities(auditEntryModel.getNotification()))
                                                           .findFirst();
        if (matchingAuditEntry.isPresent()) {
            AuditEntryModel auditEntryModel = matchingAuditEntry.get();
            intLogger.info(String.format("The notification has been processed by %s jobs.", auditEntryModel.getJobs().size()));
            return auditEntryModel.getJobs().size() == numberOfJobsNeedToMatch;
        }
        return false;
    }

    private boolean isNotificationAfterTime(LocalDateTime startSearchTime, NotificationConfig notificationConfig) {
        String createdAt = notificationConfig.getCreatedAt();
        LocalDateTime createdAtTime = LocalDateTime.parse(createdAt, dateTimeFormatter);
        return createdAtTime.isAfter(startSearchTime);
    }

    private boolean isNotificationForNewVulnerabilities(NotificationConfig notificationConfig) {
        JsonObject jsonObject = gson.fromJson(notificationConfig.getContent(), JsonObject.class);
        JsonElement content = jsonObject.get("content");
        VulnerabilityNotificationContent notification = gson.fromJson(content, VulnerabilityNotificationContent.class);
        notification.getNewVulnerabilityCount();
        return notification.getNewVulnerabilityCount() > 0;
    }
}
