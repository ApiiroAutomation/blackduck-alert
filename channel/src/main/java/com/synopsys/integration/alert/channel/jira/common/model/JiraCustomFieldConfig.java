/**
 * channel
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
package com.synopsys.integration.alert.channel.jira.common.model;

public class JiraCustomFieldConfig {
    private final String name;
    private final String fieldValue;
    private final String fieldType;

    public JiraCustomFieldConfig(String fieldName, String fieldValue, String fieldType) {
        this.name = fieldName;
        this.fieldValue = fieldValue;
        this.fieldType = fieldType;
    }

    public String getName() {
        return name;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public String getFieldType() {
        return fieldType;
    }

}
