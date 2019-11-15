/**
 * alert-issuetracker
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
package com.synopsys.integration.alert.issuetracker;

import java.util.Collection;

public class IssueContentModel {
    private final String title;
    private final String description;
    private final Collection<String> additionalComments;
    private final IssueProperties issueProperties;
    private final OperationType operation;

    private IssueContentModel(IssueProperties issueProperties, OperationType operation, String title, String description, Collection<String> additionalComments) {
        this.issueProperties = issueProperties;
        this.operation = operation;
        this.title = title;
        this.description = description;
        this.additionalComments = additionalComments;
    }

    public static final IssueContentModel of(IssueProperties issueProperties, OperationType operation, String title, String description, Collection<String> additionalComments) {
        return new IssueContentModel(issueProperties, operation, title, description, additionalComments);
    }

    public IssueProperties getIssueProperties() {
        return issueProperties;
    }

    public OperationType getOperation() {
        return operation;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Collection<String> getAdditionalComments() {
        return additionalComments;
    }

}
