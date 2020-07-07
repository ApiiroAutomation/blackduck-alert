/**
 * alert-jira
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.jira.server.util;

import com.synopsys.integration.alert.jira.common.JiraConstants;
import com.synopsys.integration.alert.jira.common.JiraIssueSearchProperties;
import com.synopsys.integration.alert.jira.common.util.JiraIssuePropertyHandler;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.rest.service.IssuePropertyService;
import com.synopsys.integration.jira.common.server.model.IssueSearchResponseModel;
import com.synopsys.integration.jira.common.server.service.IssueSearchService;

public class JiraServerIssuePropertyHandler extends JiraIssuePropertyHandler<IssueSearchResponseModel> {
    private final IssueSearchService issueSearchService;
    private final IssuePropertyService issuePropertyService;

    public JiraServerIssuePropertyHandler(IssueSearchService issueSearchService, IssuePropertyService issuePropertyService) {
        this.issueSearchService = issueSearchService;
        this.issuePropertyService = issuePropertyService;
    }

    @Override
    public IssueSearchResponseModel queryForIssues(String query) throws IntegrationException {
        return issueSearchService.queryForIssues(query);
    }

    @Override
    public void addPropertiesToIssue(String issueKey, JiraIssueSearchProperties properties) throws IntegrationException {
        issuePropertyService.setProperty(issueKey, JiraConstants.JIRA_ISSUE_PROPERTY_KEY, properties);
    }
}
