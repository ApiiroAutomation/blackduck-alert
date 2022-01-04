/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.provider.project;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;
import com.synopsys.integration.alert.database.DatabaseEntity;

@Entity
@Table(schema = "alert", name = "provider_projects")
public class ProviderProjectEntity extends BaseEntity implements DatabaseEntity {
    @Id
    @GeneratedValue(generator = "alert.provider_projects_id_seq_generator", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "alert.provider_projects_id_seq_generator", sequenceName = "alert.provider_projects_id_seq")
    @Column(name = "id")
    private Long id;
    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "href")
    private String href;

    @Column(name = "project_owner_email")
    private String projectOwnerEmail;

    @Column(name = "provider_config_id")
    private Long providerConfigId;

    public ProviderProjectEntity() {
        // JPA requires default constructor definitions
    }

    public ProviderProjectEntity(String name, String description, String href, String projectOwnerEmail, Long providerConfigId) {
        this.name = name;
        this.description = description;
        this.href = href;
        this.projectOwnerEmail = projectOwnerEmail;
        this.providerConfigId = providerConfigId;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getHref() {
        return href;
    }

    public String getProjectOwnerEmail() {
        return projectOwnerEmail;
    }

    public Long getProviderConfigId() {
        return providerConfigId;
    }

}
