package com.mgmtp.a12.template.server.changelog;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedGroupView;
import com.mgmtp.a12.template.server.typings.pointers._changemetadata_dm.PChangeMetadata;
import com.mgmtp.a12.template.server.typings.views._changemetadata_dm.ChangeMetadata;

import java.time.Instant;

import static com.mgmtp.a12.template.server.utils.Constants.CHANGELOG_GROUP_SUFFIX;
import static com.mgmtp.a12.template.server.utils.Constants.POINTER_WILDCARD;

class CommonChangelogService {

    protected final String userName;
    protected final Instant now;

    CommonChangelogService(String userName, Instant now) {
        this.userName = userName;
        this.now = now;
    }

    /**
     * Adds the metadata to the changelog entry and afterward the extended changelog entry is added to the given
     * document.
     */
    <CE extends TypedGroupView<CE>> DocumentV2 addChangelogEntryToDocument(
            DocumentV2 result,
            CE changelogEntry,
            PChangeMetadata<CE> changeMetadataPointer,
            DocumentPointer entityPointer
    ) {
        var pcm = ChangeMetadata._pointer();
        ChangeMetadata changeMeta = ChangeMetadata._empty()
                ._with(pcm.changedBy(), userName)
                ._with(pcm.changedAt(), now);

        changelogEntry = changelogEntry._with(changeMetadataPointer, changeMeta);

        // Note: the changelogGroupName is derived dynamically, so here the compile-time checks against the DM via
        // typed accessors cannot be used / would not make sense
        // -> we use plain DocumentPointer and the _unwrap()'ed changelogEntry
        String entityName = entityPointer.getPathParts().getLast().name();
        String changelogGroupName = entityName + CHANGELOG_GROUP_SUFFIX + POINTER_WILDCARD;
        DocumentPointer changelogGroupPointer =
                entityPointer.parent().withConcatenated(DocumentPointer.of(changelogGroupName));
        result = result.withGroupRepetitionAppended(changelogGroupPointer, changelogEntry._unwrap());
        return result;
    }

}
