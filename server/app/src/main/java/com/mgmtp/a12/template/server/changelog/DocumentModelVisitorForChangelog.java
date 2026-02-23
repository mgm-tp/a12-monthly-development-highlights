package com.mgmtp.a12.template.server.changelog;

import com.mgmtp.a12.kernel.md.model.api.IField;
import com.mgmtp.a12.kernel.md.model.api.IGroup;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Set;

import static com.mgmtp.a12.template.server.utils.Constants.WITH_CHANGELOG_ANNOTATION;

@Getter
class DocumentModelVisitorForChangelog extends DocumentModelVisitor {

    @Getter(AccessLevel.NONE)
	private final IDocumentModelService documentModelService;

	private final Set<String> fieldPathsWithChangelog = new HashSet<>();
	private final Set<String> groupPathsWithChangelog = new HashSet<>();

	DocumentModelVisitorForChangelog(IDocumentModelService documentModelService) {
		this.documentModelService = documentModelService;
	}

	@Override
	public DocumentModelWalker.VisitProcess visitField(@NonNull IField field) {
		if (field.getAnnotations().stream().anyMatch(a -> WITH_CHANGELOG_ANNOTATION.equals(a.getName()))) {
			fieldPathsWithChangelog.add(documentModelService.getPath(field));
		}
		return super.visitField(field);
	}

	@Override
	public DocumentModelWalker.VisitProcess visitGroup(@NonNull IGroup group) {
		if (group.getAnnotations().stream().anyMatch(a -> WITH_CHANGELOG_ANNOTATION.equals(a.getName()))) {
            groupPathsWithChangelog.add(documentModelService.getPath(group));
		}
		return super.visitGroup(group);
	}
}
