package com.mgmtp.a12.template.server.changelog;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.typed.TypedGroupView;
import com.mgmtp.a12.template.server.typings.pointers._changemetadata_dm.PChangeMetadata;

import java.time.Instant;

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

        // build ChangeMetadata with TAC

        // Note: the changelogGroupName is derived dynamically, so here the compile-time checks against the DM via
        // typed accessors cannot be used / would not make sense
        // -> we use plain DocumentPointer and the _unwrap()'ed changelogEntry

        return result;
    }

}
