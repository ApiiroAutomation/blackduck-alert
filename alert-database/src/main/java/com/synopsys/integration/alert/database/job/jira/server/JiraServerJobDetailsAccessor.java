/**
 * alert-database
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
package com.synopsys.integration.alert.database.job.jira.server;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;

@Component
public class JiraServerJobDetailsAccessor {
    private final JiraServerJobDetailsRepository jiraServerJobDetailsRepository;

    @Autowired
    public JiraServerJobDetailsAccessor(JiraServerJobDetailsRepository jiraServerJobDetailsRepository) {
        this.jiraServerJobDetailsRepository = jiraServerJobDetailsRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public JiraServerJobDetailsEntity saveJiraServerJobDetails(UUID jobId, JiraServerJobDetailsModel jiraServerJobDetails) {
        JiraServerJobDetailsEntity jiraServerJobDetailsToSave = new JiraServerJobDetailsEntity(
            jobId,
            jiraServerJobDetails.isAddComments(),
            jiraServerJobDetails.getIssueCreatorUsername(),
            jiraServerJobDetails.getProjectNameOrKey(),
            jiraServerJobDetails.getIssueType(),
            jiraServerJobDetails.getResolveTransition(),
            jiraServerJobDetails.getReopenTransition()
        );
        return jiraServerJobDetailsRepository.save(jiraServerJobDetailsToSave);
    }

}
