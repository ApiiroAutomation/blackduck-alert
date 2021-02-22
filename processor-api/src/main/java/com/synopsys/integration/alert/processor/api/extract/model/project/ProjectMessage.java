/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.processor.api.extract.model.project;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.CombinableModel;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessage;

public class ProjectMessage extends ProviderMessage<ProjectMessage> {
    private final MessageReason messageReason;
    private final ProjectOperation operation;

    private final LinkableItem project;
    private final LinkableItem projectVersion;
    private final List<BomComponentDetails> bomComponents;

    public static ProjectMessage projectStatusInfo(ProviderDetails providerDetails, LinkableItem project, ProjectOperation operation) {
        return new ProjectMessage(providerDetails, MessageReason.PROJECT_STATUS, operation, project, null, List.of());
    }

    public static ProjectMessage projectVersionStatusInfo(ProviderDetails providerDetails, LinkableItem project, LinkableItem projectVersion, ProjectOperation operation) {
        return new ProjectMessage(providerDetails, MessageReason.PROJECT_VERSION_STATUS, operation, project, projectVersion, List.of());
    }

    public static ProjectMessage componentUpdate(ProviderDetails providerDetails, LinkableItem project, LinkableItem projectVersion, List<BomComponentDetails> bomComponents) {
        return new ProjectMessage(providerDetails, MessageReason.COMPONENT_UPDATE, null, project, projectVersion, bomComponents);
    }

    public static ProjectMessage componentConcern(ProviderDetails providerDetails, LinkableItem project, LinkableItem projectVersion, List<BomComponentDetails> bomComponents) {
        return new ProjectMessage(providerDetails, MessageReason.COMPONENT_CONCERN, null, project, projectVersion, bomComponents);
    }

    private ProjectMessage(
        ProviderDetails providerDetails,
        MessageReason messageReason,
        ProjectOperation operation,
        LinkableItem project,
        @Nullable LinkableItem projectVersion,
        List<BomComponentDetails> bomComponents
    ) {
        super(providerDetails);
        this.messageReason = messageReason;
        this.operation = operation;
        this.project = project;
        this.projectVersion = projectVersion;
        this.bomComponents = bomComponents;
    }

    public MessageReason getMessageReason() {
        return messageReason;
    }

    public Optional<ProjectOperation> getOperation() {
        return Optional.ofNullable(operation);
    }

    public LinkableItem getProject() {
        return project;
    }

    public Optional<LinkableItem> getProjectVersion() {
        return Optional.ofNullable(projectVersion);
    }

    // TODO consider making this a sorted set
    public List<BomComponentDetails> getBomComponents() {
        return bomComponents;
    }

    public boolean hasBomComponents() {
        return !bomComponents.isEmpty();
    }

    @Override
    public List<ProjectMessage> combine(ProjectMessage otherMessage) {
        List<ProjectMessage> uncombinedMessages = List.of(this, otherMessage);

        if (!getProviderDetails().equals(otherMessage.getProviderDetails())) {
            return uncombinedMessages;
        }

        if (!messageReason.equals(otherMessage.getMessageReason())) {
            return uncombinedMessages;
        }

        if (!project.equals(otherMessage.project)) {
            return uncombinedMessages;
        }

        if (messageReason.equals(MessageReason.PROJECT_STATUS)) {
            if (operation.equals(otherMessage.operation)) {
                return List.of(this);
            } else {
                return List.of();
            }
        }

        if (null == projectVersion || !projectVersion.equals(otherMessage.projectVersion)) {
            return uncombinedMessages;
        }

        if (messageReason.equals(MessageReason.PROJECT_VERSION_STATUS)) {
            if (operation.equals(otherMessage.operation)) {
                return List.of(this);
            } else {
                return List.of();
            }
        }

        return combineBomComponents(otherMessage.getBomComponents());
    }

    private List<ProjectMessage> combineBomComponents(List<BomComponentDetails> otherMessageBomComponents) {
        List<BomComponentDetails> combinedBomComponents = CombinableModel.combine(bomComponents, otherMessageBomComponents);
        ProjectMessage projectMessageWithCombinedComponents = new ProjectMessage(getProviderDetails(), messageReason, operation, project, projectVersion, combinedBomComponents);
        return List.of(projectMessageWithCombinedComponents);
    }

}
