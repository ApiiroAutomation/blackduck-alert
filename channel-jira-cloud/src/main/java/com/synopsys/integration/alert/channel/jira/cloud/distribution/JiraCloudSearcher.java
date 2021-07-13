/*
 * channel-jira-cloud
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.distribution;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.api.channel.issue.convert.ProjectMessageToIssueModelTransformer;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraIssueAlertPropertiesManager;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraIssueStatusCreator;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraSearcher;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraSearcherResponseModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.model.IssueSearchResponseModel;
import com.synopsys.integration.jira.common.cloud.service.IssueSearchService;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.model.components.IssueFieldsComponent;
import com.synopsys.integration.jira.common.model.components.StatusDetailsComponent;
import com.synopsys.integration.jira.common.model.response.IssueResponseModel;
import com.synopsys.integration.jira.common.model.response.TransitionsResponseModel;

public class JiraCloudSearcher extends JiraSearcher {
    private final IssueSearchService issueSearchService;
    private final IssueService issueService;

    public JiraCloudSearcher(String jiraProjectKey, IssueSearchService issueSearchService, JiraIssueAlertPropertiesManager issuePropertiesManager, ProjectMessageToIssueModelTransformer modelTransformer, IssueService issueService, JiraIssueStatusCreator jiraIssueStatusCreator) {
        super(jiraProjectKey, issuePropertiesManager, modelTransformer, jiraIssueStatusCreator);
        this.issueSearchService = issueSearchService;
        this.issueService = issueService;
    }

    @Override
    protected List<JiraSearcherResponseModel> executeQueryForIssues(String jql) throws IntegrationException {
        IssueSearchResponseModel issueSearchResponseModel = issueSearchService.queryForIssues(jql);
        return issueSearchResponseModel.getIssues()
                   .stream()
                   .map(this::convertModel)
                   .collect(Collectors.toList());
    }

    @Override
    protected StatusDetailsComponent fetchIssueStatus(String issueKey) throws IntegrationException {
        return issueService.getStatus(issueKey);
    }

    @Override
    protected TransitionsResponseModel fetchIssueTransitions(String issueKey) throws IntegrationException {
        return issueService.getTransitions(issueKey);
    }

    private JiraSearcherResponseModel convertModel(IssueResponseModel issue) {
        IssueFieldsComponent nullableIssueFields = issue.getFields();
        String existingIssueSummary = Optional.ofNullable(nullableIssueFields)
                                          .map(IssueFieldsComponent::getSummary)
                                          .orElse(issue.getKey());
        return new JiraSearcherResponseModel(issue.getSelf(), issue.getKey(), issue.getId(), existingIssueSummary);
    }

}
