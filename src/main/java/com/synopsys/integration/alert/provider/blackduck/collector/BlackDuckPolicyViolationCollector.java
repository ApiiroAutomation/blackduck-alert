/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider.blackduck.collector;

import java.net.IDN;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.jayway.jsonpath.TypeRef;
import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.alert.common.message.model.CategoryItem;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonExtractor;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonField;
import com.synopsys.integration.alert.common.workflow.filter.field.JsonFieldAccessor;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentProcessor;
import com.synopsys.integration.alert.provider.blackduck.collector.item.BlackDuckPolicyLinkableItem;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckContent;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BlackDuckPolicyViolationCollector extends BlackDuckPolicyCollector {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public BlackDuckPolicyViolationCollector(final JsonExtractor jsonExtractor, final List<MessageContentProcessor> messageContentProcessorList) {
        super(jsonExtractor, messageContentProcessorList, Arrays.asList(BlackDuckContent.RULE_VIOLATION, BlackDuckContent.RULE_VIOLATION_CLEARED));
    }

    @Override
    protected void addCategoryItems(final SortedSet<CategoryItem> categoryItems, final JsonFieldAccessor jsonFieldAccessor, final List<JsonField<?>> notificationFields, final AlertNotificationWrapper notificationContent) {
        final ItemOperation operation = getOperationFromNotification(notificationContent);
        if (operation == null) {
            return;
        }

        final List<JsonField<PolicyInfo>> policyFields = getFieldsOfType(notificationFields, new TypeRef<PolicyInfo>() {});
        final List<JsonField<ComponentVersionStatus>> componentFields = getFieldsOfType(notificationFields, new TypeRef<ComponentVersionStatus>() {});
        final List<JsonField<String>> stringFields = getStringFields(notificationFields);

        final Map<String, PolicyInfo> policyItems = getFieldValueObjectsByLabel(jsonFieldAccessor, policyFields, BlackDuckContent.LABEL_POLICY_INFO_LIST).stream()
                                                        .collect(Collectors.toMap(PolicyInfo::getPolicy, Function.identity()));
        final List<ComponentVersionStatus> componentVersionStatuses = getFieldValueObjectsByLabel(jsonFieldAccessor, componentFields, BlackDuckContent.LABEL_COMPONENT_VERSION_STATUS);
        final String projectVersionUrl = getFieldValueObjectsByLabel(jsonFieldAccessor, stringFields, BlackDuckContent.LABEL_PROJECT_VERSION_NAME + JsonField.LABEL_URL_SUFFIX)
                                             .stream()
                                             .findFirst()
                                             .orElse("");

        final Map<PolicyComponentMapping, BlackDuckPolicyLinkableItem> policyComponentToLinkableItemMapping = createPolicyComponentToLinkableItemMapping(componentVersionStatuses, policyItems, projectVersionUrl);
        for (final Map.Entry<PolicyComponentMapping, BlackDuckPolicyLinkableItem> policyComponentToLinkableItem : policyComponentToLinkableItemMapping.entrySet()) {
            final PolicyComponentMapping policyComponentMapping = policyComponentToLinkableItem.getKey();

            final SortedSet<LinkableItem> linkablePolicyItems = policyComponentMapping.getPolicies()
                                                                    .stream()
                                                                    .map(this::createPolicyLinkableItem)
                                                                    .collect(Collectors.toCollection(TreeSet::new));
            final BlackDuckPolicyLinkableItem blackDuckPolicyLinkableItem = policyComponentToLinkableItem.getValue();
            final SortedSet<LinkableItem> applicableItems = blackDuckPolicyLinkableItem.getComponentData();
            addApplicableItems(categoryItems, notificationContent.getId(), linkablePolicyItems, operation, applicableItems);
        }
    }

    private ItemOperation getOperationFromNotification(final AlertNotificationWrapper notificationContent) {
        final ItemOperation operation;
        final String notificationType = notificationContent.getNotificationType();
        if (NotificationType.RULE_VIOLATION_CLEARED.name().equals(notificationType)) {
            operation = ItemOperation.DELETE;
        } else if (NotificationType.RULE_VIOLATION.name().equals(notificationType)) {
            operation = ItemOperation.ADD;
        } else {
            operation = null;
            logger.error("Unrecognized notification type: The notification type '{}' is not valid for this collector.", notificationType);
        }

        return operation;
    }

    private Map<PolicyComponentMapping, BlackDuckPolicyLinkableItem> createPolicyComponentToLinkableItemMapping(final Collection<ComponentVersionStatus> componentVersionStatuses, final Map<String, PolicyInfo> policyItems,
        final String projectVersionUrl) {
        final Map<PolicyComponentMapping, BlackDuckPolicyLinkableItem> policyComponentToLinkableItemMapping = new HashMap<>();
        for (final ComponentVersionStatus componentVersionStatus : componentVersionStatuses) {
            String projectVersionLink = projectVersionUrl;
            try {
                final String projectVersionWithComponentLink = String.format("%s?q:componentName=%s", projectVersionUrl, componentVersionStatus.getComponentName());
                final URL encodedUrl = new URL(projectVersionWithComponentLink);
                final URI uri = new URI(encodedUrl.getProtocol(), encodedUrl.getUserInfo(), IDN.toASCII(encodedUrl.getHost()), encodedUrl.getPort(), encodedUrl.getPath(), encodedUrl.getQuery(), encodedUrl.getRef());
                projectVersionLink = uri.toASCIIString();
            } catch (final MalformedURLException | URISyntaxException e) {
                logger.error("There was a problem parsing the project version URL", e);
            }
            final PolicyComponentMapping policyComponentMapping = createPolicyComponentMapping(componentVersionStatus, policyItems);
            BlackDuckPolicyLinkableItem blackDuckPolicyLinkableItem = policyComponentToLinkableItemMapping.get(policyComponentMapping);
            if (blackDuckPolicyLinkableItem == null) {
                blackDuckPolicyLinkableItem = createBlackDuckPolicyLinkableItem(componentVersionStatus, projectVersionLink);
            } else {
                blackDuckPolicyLinkableItem.addComponentVersionItem(componentVersionStatus.getComponentVersionName(), projectVersionLink);
            }
            policyComponentToLinkableItemMapping.put(policyComponentMapping, blackDuckPolicyLinkableItem);
        }
        return policyComponentToLinkableItemMapping;
    }

    private PolicyComponentMapping createPolicyComponentMapping(final ComponentVersionStatus componentVersionStatus, final Map<String, PolicyInfo> policyItems) {
        final String componentName = componentVersionStatus.getComponentName();

        final Set<PolicyInfo> policies = componentVersionStatus.getPolicies().stream()
                                             .filter(policyUrl -> policyItems.containsKey(policyUrl))
                                             .map(policyUrl -> policyItems.get(policyUrl))
                                             .collect(Collectors.toSet());

        return new PolicyComponentMapping(componentName, policies);
    }

    private BlackDuckPolicyLinkableItem createBlackDuckPolicyLinkableItem(final ComponentVersionStatus componentVersionStatus, final String projectVersionWithComponentLink) {
        final BlackDuckPolicyLinkableItem blackDuckPolicyLinkableItem = new BlackDuckPolicyLinkableItem();

        final String componentName = componentVersionStatus.getComponentName();
        if (StringUtils.isNotBlank(componentName)) {
            blackDuckPolicyLinkableItem.addComponentNameItem(componentName, componentVersionStatus.getComponent());
        }

        final String componentVersionName = componentVersionStatus.getComponentVersionName();
        if (StringUtils.isNotBlank(componentVersionName)) {
            blackDuckPolicyLinkableItem.addComponentVersionItem(componentVersionName, projectVersionWithComponentLink);
        }

        return blackDuckPolicyLinkableItem;
    }

    private LinkableItem createPolicyLinkableItem(final PolicyInfo policyInfo) {
        final String policyName = policyInfo.getPolicyName();
        final String severity = policyInfo.getSeverity();
        String displayName = policyName;
        if (StringUtils.isNotBlank(severity)) {
            displayName = String.format("%s (%s)", policyName, severity);
        }
        final LinkableItem linkableItem = new LinkableItem(BlackDuckContent.LABEL_POLICY_NAME, displayName, null);
        linkableItem.setCollapsible(true);
        linkableItem.setSummarizable(true);
        return linkableItem;
    }

    private class PolicyComponentMapping extends AlertSerializableModel {
        // Do not delete this member. This is used for checking equals and filtering.
        private final String componentName;
        private final Set<PolicyInfo> policies;

        public PolicyComponentMapping(final String componentName, final Set<PolicyInfo> policies) {
            this.componentName = componentName;
            this.policies = policies;
        }

        public Set<PolicyInfo> getPolicies() {
            return policies;
        }

    }

}
