/**
 * blackduck-alert
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
package com.synopsys.integration.alert.web.certificates;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.web.controller.BaseController;
import com.synopsys.integration.alert.web.model.CertificateModel;

@RestController
@RequestMapping(CertificatesController.API_BASE_URL)
public class CertificatesController extends BaseController {
    public static final String API_BASE_URL = "/certificates";
    private static final Logger logger = LoggerFactory.getLogger(CertificatesController.class);

    private final ResponseFactory responseFactory;
    private final ContentConverter contentConverter;
    private final CertificateActions actions;

    @Autowired
    public CertificatesController(ResponseFactory responseFactory, ContentConverter contentConverter, CertificateActions actions) {
        this.responseFactory = responseFactory;
        this.contentConverter = contentConverter;
        this.actions = actions;
    }

    @GetMapping
    public ResponseEntity<String> readCertificates() {
        return responseFactory.createOkContentResponse(contentConverter.getJsonString(actions.readCertificates()));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<String> readCertificate(@PathVariable Long id) {
        Optional<CertificateModel> certificate = actions.readCertificate(id);
        if (certificate.isPresent()) {
            return responseFactory.createOkContentResponse(contentConverter.getJsonString(certificate.get()));
        }
        return responseFactory.createNotFoundResponse("Certificate resource not found");
    }

    @PostMapping
    public ResponseEntity<String> importCertificate(@RequestBody CertificateModel certificateModel) {
        try {
            CertificateModel certificate = actions.createCertificate(certificateModel);
            return responseFactory.createOkContentResponse(contentConverter.getJsonString(certificate));
        } catch (AlertException ex) {
            logger.error("There was an issue updating the certificate: {}", ex.getMessage());
            logger.debug("Cause", ex);
            return responseFactory.createInternalServerErrorResponse("", "There was an issue importing the certificate.");
        }
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<String> updateCertificate(@PathVariable Long id, @RequestBody CertificateModel certificateModel) {
        try {

            Optional<CertificateModel> certificate = actions.updateCertificate(id, certificateModel);
            if (certificate.isPresent()) {
                return responseFactory.createOkContentResponse(contentConverter.getJsonString(certificate.get()));
            }
            return responseFactory.createNotFoundResponse("Certificate resource not found");
        } catch (AlertException ex) {
            logger.error("There was an issue updating the certificate: {}", ex.getMessage());
            logger.debug("Cause", ex);
            return responseFactory.createInternalServerErrorResponse(Long.toString(id), "There was an issue updating the certificate.");
        }
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> deleteCertificate(@PathVariable Long id) {
        try {
            actions.deleteCertificate(id);
            return responseFactory.createOkResponse(Long.toString(id), "Certificate deleted");
        } catch (AlertException ex) {
            logger.error("There was an issue deleting the certificate: {}", ex.getMessage());
            logger.debug("Cause", ex);
            return responseFactory.createInternalServerErrorResponse(Long.toString(id), "There was an issue deleting the certificate.");
        }
    }
}
