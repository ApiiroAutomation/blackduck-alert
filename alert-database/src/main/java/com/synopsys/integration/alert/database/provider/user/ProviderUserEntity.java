/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.provider.user;

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
@Table(schema = "alert", name = "provider_users")
public class ProviderUserEntity extends BaseEntity implements DatabaseEntity {
    @Id
    @GeneratedValue(generator = "alert.provider_users_id_seq_generator", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "alert.provider_users_id_seq_generator", sequenceName = "alert.provider_users_id_seq")
    @Column(name = "id")
    private Long id;
    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "opt_out")
    private Boolean optOut;

    @Column(name = "provider_config_id")
    private Long providerConfigId;

    public ProviderUserEntity() {
        // JPA requires default constructor definitions
    }

    public ProviderUserEntity(String emailAddress, Boolean optOut, Long providerConfigId) {
        this.emailAddress = emailAddress;
        this.optOut = optOut;
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

    public String getEmailAddress() {
        return emailAddress;
    }

    public Boolean getOptOut() {
        return optOut;
    }

    public Long getProviderConfigId() {
        return providerConfigId;
    }

}
