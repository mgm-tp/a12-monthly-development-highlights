package com.mgmtp.a12.template.server.utils;

import com.mgmtp.a12.kernel.md.document.apiV2.DocumentPointer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {

    public  static final String WITH_ID_ANNOTATION = "WITH_ID";
    public static final String WITH_CHANGELOG_ANNOTATION = "WITH_CHANGELOG";

    public static final String CHANGELOG_GROUP_SUFFIX = "_Changelog";

    public static final String ID_FIELD_NAME = "Id";
    public static final DocumentPointer ID_POINTER = DocumentPointer.of(ID_FIELD_NAME);
    public static final String POINTER_WILDCARD = "[0]";

    public static final String PERSON_DM_NAME = "Person_DM";
}
