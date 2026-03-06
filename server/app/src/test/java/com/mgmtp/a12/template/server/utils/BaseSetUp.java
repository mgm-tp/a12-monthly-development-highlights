package com.mgmtp.a12.template.server.utils;

import com.mgmtp.a12.dataservices.model.persistence.IModelLoader;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseSetUp {

    // runtime model
    protected static final String PERSON_DM_ID = "Person_DM";
    protected static final DocumentModelServiceFactory DM_SERVICE_FACTORY = new DocumentModelServiceFactory();
    protected static final IDocumentModelSerializer DM_SERIALIZER = DM_SERVICE_FACTORY.createDocumentModelSerializer();
    protected static final IDocumentModelService DM_SERVICE = DM_SERVICE_FACTORY.createDocumentModelService();

    protected IDocumentModelService documentModelService;
    protected DocumentModelResolver documentModelResolver;
    protected IModelLoader<IDocumentModel> documentModelLoader;
    protected GroupIdSetter groupIdSetter;

    @BeforeEach
    void setUp() {
        DocumentModelServiceFactory documentModelServiceFactory = new DocumentModelServiceFactory();
        documentModelService = documentModelServiceFactory.createDocumentModelService();
        IDocumentModel personDm = readDocumentModel(PERSON_DM_ID);

        documentModelResolver = new DocumentModelResolver();
        documentModelResolver.addDocumentModel(personDm);

        documentModelLoader = mock(IModelLoader.class);
        when(documentModelLoader.loadModel(anyString()))
                .thenReturn(personDm);

        groupIdSetter = new GroupIdSetter(documentModelLoader, DM_SERVICE);
    }

    private IDocumentModel readDocumentModel(String documentModelId) {
        IDocumentModel documentModel;
        try (InputStream is = BaseSetUp.class.getResourceAsStream(documentModelId + ".json");
             Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            documentModel = DM_SERIALIZER.deserialize(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        Assertions.assertNotNull(documentModel);
        return documentModel;
    }
}
