/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.web.controller.metadata;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.CommonDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.DescriptorMetadata;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;

@RestController
public class DescriptorController extends MetadataController {
    public static final String DESCRIPTORS_PATH = "/descriptors";

    private final Collection<Descriptor> descriptors;
    private final CommonDistributionUIConfig commonDistributionUIConfig;

    @Autowired
    public DescriptorController(final Collection<Descriptor> descriptors, final CommonDistributionUIConfig commonDistributionUIConfig) {
        this.descriptors = descriptors;
        this.commonDistributionUIConfig = commonDistributionUIConfig;
    }

    @GetMapping(DESCRIPTORS_PATH)
    public Set<DescriptorMetadata> getDescriptors(@RequestParam(required = false) final String name, @RequestParam(required = false) final String type, @RequestParam(required = false) final String context) {
        Predicate<Descriptor> filter = Descriptor::hasUIConfigs;
        if (name != null) {
            filter = filter.and(descriptor -> name.equalsIgnoreCase(descriptor.getName()));
        }

        final DescriptorType typeEnum = EnumUtils.getEnumIgnoreCase(DescriptorType.class, type);
        if (typeEnum != null) {
            filter = filter.and(descriptor -> typeEnum.equals(descriptor.getType()));
        } else if (type != null) {
            return Set.of();
        }

        final ConfigContextEnum contextEnum = EnumUtils.getEnumIgnoreCase(ConfigContextEnum.class, context);
        if (contextEnum != null) {
            filter = filter.and(descriptor -> descriptor.hasUIConfigForType(contextEnum));
        } else if (context != null) {
            return Set.of();
        }

        final Set<Descriptor> filteredDescriptors = filter(descriptors, filter);
        return generateUIComponents(filteredDescriptors, contextEnum);
    }

    private Set<Descriptor> filter(final Collection<Descriptor> descriptors, final Predicate<Descriptor> predicate) {
        return descriptors
                   .stream()
                   .filter(predicate)
                   .collect(Collectors.toSet());
    }

    private Set<DescriptorMetadata> generateUIComponents(final Set<Descriptor> filteredDescriptors, final ConfigContextEnum context) {
        final ConfigContextEnum[] applicableContexts;
        if (context != null) {
            applicableContexts = new ConfigContextEnum[] { context };
        } else {
            applicableContexts = ConfigContextEnum.values();
        }

        final Set<DescriptorMetadata> descriptorMetadata = new HashSet<>();
        for (final ConfigContextEnum applicableContext : applicableContexts) {
            for (final Descriptor descriptor : filteredDescriptors) {
                final Optional<DescriptorMetadata> optionalMetaData = descriptor.getMetaData(applicableContext);
                if (ConfigContextEnum.DISTRIBUTION == applicableContext) {
                    optionalMetaData.ifPresent(metadata -> descriptorMetadata.add(createMetaData(metadata)));
                } else {
                    optionalMetaData.ifPresent(descriptorMetadata::add);

                }
            }
        }
        return descriptorMetadata;
    }

    private DescriptorMetadata createMetaData(final DescriptorMetadata metadata) {
        final Set<String> channelDescriptors = descriptors.stream()
                                                   .filter(descriptor -> DescriptorType.CHANNEL == descriptor.getType())
                                                   .map(value -> value.getName())
                                                   .collect(Collectors.toSet());
        final Set<String> providerDescriptors = descriptors.stream()
                                                    .filter(descriptor -> DescriptorType.PROVIDER == descriptor.getType())
                                                    .map(value -> value.getName())
                                                    .collect(Collectors.toSet());
        final List<ConfigField> metaDataFields = new LinkedList<>();
        metaDataFields.addAll(metadata.getFields());
        metaDataFields.addAll(commonDistributionUIConfig.createCommonConfigFields(channelDescriptors, providerDescriptors));

        return new DescriptorMetadata(metadata.getLabel(), metadata.getUrlName(), metadata.getName(), metadata.getType(), metadata.getContext(), metadata.getFontAwesomeIcon(), metadata.isAutomaticallyGenerateUI(), metaDataFields);
    }
}
