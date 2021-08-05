/*
 * channel-jira-cloud
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.validator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.CommonChannelDistributionValidator;
import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraJobCustomFieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.descriptor.api.JiraCloudChannelKey;

@Component
public class JiraCloudDistributionConfigurationValidator implements DistributionConfigurationValidator {
    private final Gson gson;
    private final JiraCloudChannelKey jiraCloudChannelKey;
    private final CommonChannelDistributionValidator commonChannelDistributionValidator;

    @Autowired
    public JiraCloudDistributionConfigurationValidator(Gson gson, JiraCloudChannelKey jiraCloudChannelKey, CommonChannelDistributionValidator commonChannelDistributionValidator) {
        this.gson = gson;
        this.jiraCloudChannelKey = jiraCloudChannelKey;
        this.commonChannelDistributionValidator = commonChannelDistributionValidator;
    }

    @Override
    public Set<AlertFieldStatus> validate(JobFieldModel jobFieldModel) {
        HashSet<AlertFieldStatus> validationResults = new HashSet<>();
        ConfigurationFieldValidator configurationFieldValidator = ConfigurationFieldValidator.fromJobFieldModel(jobFieldModel);

        commonChannelDistributionValidator.validate(configurationFieldValidator);
        configurationFieldValidator.validateRequiredFieldsAreNotBlank(JiraCloudDescriptor.KEY_JIRA_PROJECT_NAME, JiraCloudDescriptor.KEY_ISSUE_TYPE);
        configurationFieldValidator.validateRequiredRelatedSet(
            JiraCloudDescriptor.KEY_OPEN_WORKFLOW_TRANSITION, JiraCloudDescriptor.LABEL_OPEN_WORKFLOW_TRANSITION,
            JiraCloudDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION
        );

        // Validate custom field mappings
        jobFieldModel.getFieldModels()
            .stream()
            .filter(fieldModel -> jiraCloudChannelKey.getUniversalKey().equals(fieldModel.getDescriptorName()))
            .findFirst()
            .flatMap(fieldModel -> fieldModel.getFieldValueModel(JiraCloudDescriptor.KEY_FIELD_MAPPING))
            .flatMap(this::validateFieldMapping)
            .ifPresent(validationResults::add);

        validationResults.addAll(configurationFieldValidator.getValidationResults());
        return validationResults;
    }

    private Optional<AlertFieldStatus> validateFieldMapping(FieldValueModel fieldToValidate) {
        List<JiraJobCustomFieldModel> customFields = fieldToValidate.getValues()
            .stream()
            .map(fieldMappingString -> gson.fromJson(fieldMappingString, JiraJobCustomFieldModel.class))
            .collect(Collectors.toList());

        Set<String> fieldNames = new HashSet<>();
        List<String> duplicateNameList = new ArrayList<>();

        for (JiraJobCustomFieldModel jiraJobCustomFieldModel : customFields) {
            String currentFieldName = jiraJobCustomFieldModel.getFieldName();
            if (fieldNames.contains(currentFieldName)) {
                duplicateNameList.add(currentFieldName);
            }
            fieldNames.add(currentFieldName);
        }

        if (!duplicateNameList.isEmpty()) {
            String duplicateNames = StringUtils.join(duplicateNameList, ", ");
            String errorMessage = String.format("Duplicate field name(s): %s", duplicateNames);
            AlertFieldStatus fieldMappingError = AlertFieldStatus.error(JiraCloudDescriptor.KEY_FIELD_MAPPING, errorMessage);
            return Optional.of(fieldMappingError);
        }

        return Optional.empty();
    }

}
