/*
 * SPDX-License-Identifier: EUPL-1.2 OR LicenseRef-commercial
 *
 * Copyright (c) 2012-2026 mgm technology partners GmbH
 *
 * Dual License
 * ------------
 * This source file is part of the mgm A12 Platform and available under
 * a choice of two different licenses:
 *
 * 1. Open-Source License - EUPL v1.2
 *    You may redistribute and/or modify this file under the terms of the
 *    European Union Public License, version 1.2 - see https://eupl.eu/.
 *
 * 2. Commercial License
 *    Alternatively, you may obtain a commercial license from
 *    mgm technology partners GmbH, that permits use of this software
 *    under different terms (including support and maintenance services).
 *
 *    Please contact a12-license@mgm-tp.com for more information.
 *
 * You must select and comply with exactly one of the above license options.
 *
 * Warranty Disclaimer (applies to either option)
 * ----------------------------------------------
 * THIS SOFTWARE IS PROVIDED "AS IS" AND WITHOUT WARRANTY OF ANY KIND,
 * WHETHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT, EXCEPT WHERE SUCH DISCLAIMERS ARE HELD TO BE
 * LEGALLY INVALID. SEE THE RESPECTIVE LICENSE TEXT FOR DETAILS.
 */

package com.mgmtp.a12.template.server.mapping;

import com.mgmtp.a12.dataservices.document.DocumentSpec;
import com.mgmtp.a12.dataservices.model.GenericModel;
import com.mgmtp.a12.dataservices.model.ModelService;
import com.mgmtp.a12.dataservices.query.Paging;
import com.mgmtp.a12.dataservices.query.QueryService;
import com.mgmtp.a12.dataservices.query.constraint.matching.ExactMatchOperator;
import com.mgmtp.a12.dataservices.query.topology.QueryRoot;
import com.mgmtp.a12.kernel.mapping.facade.DynamicMappingServiceConfig;
import com.mgmtp.a12.kernel.mapping.facade.IMappingModelResolver;
import com.mgmtp.a12.kernel.mapping.facade.MappingServiceFactory;
import com.mgmtp.a12.kernel.mapping.rt.api.IMappingService;
import com.mgmtp.a12.kernel.mapping.rt.api.MappedField;
import com.mgmtp.a12.kernel.mapping.rt.api.MappingResult;
import com.mgmtp.a12.kernel.mapping.rt.api.SourceDocument;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.facade.DocumentServiceFactory;
import com.mgmtp.a12.kernel.md.rt.api.DocumentProcessingConfig;
import com.mgmtp.a12.kernel.md.rt.api.IMessage;
import com.mgmtp.a12.model.notification.RankedNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanySummaryDynamicService {

    private static final ConcurrentMap<String, IMappingService> CACHE = new ConcurrentHashMap<>();

    private final QueryService queryService;
    private final ModelService modelService;
    private final DocumentServiceFactory documentServiceFactory;

    public DocumentV2 compileCompanySummary(String documentId) {

        IDocumentV2Serializer documentSerializer = documentServiceFactory.createDocumentV2Serializer();

        // Fetch the CDD from Data Services using the QUERY API  and the docRef provided
        QueryRoot queryRoot = QueryRoot.builder()
                .targetDocumentModel("CompanyWithPersons_CDM")
                .constraint(ExactMatchOperator.builder()
                        .field("/__meta/docRef")
                        .value(documentId)
                        .build())
                .projectionName("cdd")
                .paging(new Paging(0, 10))
                .build();
        List<DocumentSpec> documentSpecs = queryService.query(queryRoot, null).getContent().stream()
                .map(DocumentSpec.class::cast)
                .toList();
        if (documentSpecs.isEmpty()) {
            throw new IllegalArgumentException("CDD with docRef '" + documentId + "' not found.");
        }
        if (documentSpecs.size() > 1) {
            throw new IllegalArgumentException("More than one CDD for docRef '" + documentId + "' found.");
        }

        // Read in the first result into a Kernel DocumentV2 doc

        DocumentSpec docSpec = documentSpecs.getFirst();
        List<RankedNotification> problems = new ArrayList<>();
        DocumentV2 source = documentSerializer.deserializeV2(
                new StringReader(docSpec.getDocument()),
                docSpec.getDocRef().getDocumentModelName(),
                DocumentDeserializationConfig.builder()
                        .ignoreUnknownEntities(true)
                        .build(),
                problems::add
        );
        if (!problems.isEmpty()) {
            throw new IllegalArgumentException("CDD with docRef '" + documentId + "' cannot be deserialized:\n"
                    + problems.stream().map(RankedNotification::getMessage));
        }

        // Prepare the Source + Target Documents for the Mapping

        DocumentV2 additionalSource = DocumentV2.empty("AdditionalMappingInput_DM");
        additionalSource = additionalSource.withFieldValue("/AdditionalMappingInput/Nationality", "German");
        additionalSource = additionalSource.withFieldValue("/AdditionalMappingInput/Country", "Germany");

        DocumentV2 target = DocumentV2.empty("CompanySummary_DM");
        // the following value will be overridden
        target = target.withFieldValue("/Company/CompanyDetails/CompanyName", "Dummy");


        List<SourceDocument> sourceInstances = List.of(
                SourceDocument.of("CompanyWithPersons", documentId, source),
                SourceDocument.of("additionalParameters", "additionalParameters_ID", additionalSource)
        );

        // Connect to the generated Mapping Code

        List<RankedNotification> notifications = new ArrayList<>();

        String mappingModelId = "CompanySummary_MA";
        IMappingService mappingService = CACHE.computeIfAbsent(
                mappingModelId,
                key -> createMappingService(key, notifications)
        );

        // Execute the Mapping and Apply the Mapping Result

        DocumentProcessingConfig mappingConfig = DocumentProcessingConfig.builder(Locale.ENGLISH).build();

        MappingResult mappingResult = mappingService.transferData(
                sourceInstances,
                target,
                mappingConfig
        );
        if (!notifications.isEmpty()) {
            throw new IllegalArgumentException("While mapping, the following notifications were reported:\n"
                    + notifications.stream().map(RankedNotification::getMessage));
        }

        log.debug("Mapping done: ");
        log.debug("----- Target Messages:\n{}", mappingResult.targetMessages().stream()
                .map(IMessage::getErrorText)
                .collect(Collectors.joining("\n - ", " - ", "\n-----")));

        log.debug("----- Mapped Entities:\n{}", mappingFieldsToString(mappingResult));

        return mappingResult.applyTo(target);
    }

    private IMappingService createMappingService(String mappingModelId, Collection<RankedNotification> notifications) {
        Function<String, Reader> idToReader = id -> {
            GenericModel model = modelService.load(id);
            return new StringReader(model.getContent().getRawContent());
        };
        IMappingModelResolver mappingModelResolver = IMappingModelResolver.of(
                idToReader,
                notifications::add
        );
        DynamicMappingServiceConfig serviceConfig = DynamicMappingServiceConfig.of(
                mappingModelId,
                mappingModelResolver
        );
        return MappingServiceFactory.createMappingServiceFromDynamicCode(serviceConfig);
    }

    private String mappingFieldsToString(MappingResult mappingResult) {
        return mappingResult.mappedFields().stream()
                .map(this::mappedFieldToString)
                .collect(Collectors.joining("\n - ", " - ", "\n-----"));
    }

    private String mappedFieldToString(MappedField mappedField) {
        String sourceFieldsStr = mappedField.operationSourceFields().stream()
                .map(sourceField -> "%s:%s".formatted(
                        sourceField.sourceInstanceId(),
                        documentPointerToString(sourceField.field())
                ))
                .collect(Collectors.joining(", "));
        return "[%s] -> %s".formatted(sourceFieldsStr, documentPointerToString(mappedField.targetField()));
    }

    private String documentPointerToString(DocumentPointer documentPointer) {
        return documentPointer.getPathParts().stream()
                .map(pathPart -> "%s[%s]".formatted(pathPart.name(), pathPart.repetitionIndex()))
                .collect(Collectors.joining("/", "/", ""));
    }

}
