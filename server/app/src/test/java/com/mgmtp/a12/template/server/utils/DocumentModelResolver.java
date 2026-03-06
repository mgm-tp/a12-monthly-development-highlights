package com.mgmtp.a12.template.server.utils;

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

public class DocumentModelResolver implements IDocumentModelResolver {

    private final Map<String, IDocumentModel> documentModels = new HashMap<>();

    @Override
    public IDocumentModel getDocumentModelById(@NonNull String id) {
        return documentModels.get(id);
    }

    public void addDocumentModel(@NonNull IDocumentModel documentModel) {
        documentModels.put(documentModel.getHeader().getId(), documentModel);
    }
}
