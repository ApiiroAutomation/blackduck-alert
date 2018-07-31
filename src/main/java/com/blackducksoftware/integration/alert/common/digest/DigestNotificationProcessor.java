/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.alert.common.digest;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.channel.event.ChannelEvent;
import com.blackducksoftware.integration.alert.common.digest.filter.NotificationEventManager;
import com.blackducksoftware.integration.alert.common.enumeration.DigestType;
import com.blackducksoftware.integration.alert.common.model.NotificationModel;
import com.blackducksoftware.integration.alert.database.entity.NotificationContent;

@Component
public class DigestNotificationProcessor {
    private final NotificationEventManager eventManager;

    @Autowired
    public DigestNotificationProcessor(final NotificationEventManager eventManager) {
        this.eventManager = eventManager;
    }

    public List<ChannelEvent> processNotifications(final DigestType digestType, final List<NotificationContent> notificationList) {
        final DigestRemovalProcessor removalProcessor = new DigestRemovalProcessor();
        final List<NotificationModel> processedNotificationList = removalProcessor.process(notificationList);
        if (processedNotificationList.isEmpty()) {
            return Collections.emptyList();
        } else {
            return eventManager.createChannelEvents(digestType, processedNotificationList);
        }
    }
}
