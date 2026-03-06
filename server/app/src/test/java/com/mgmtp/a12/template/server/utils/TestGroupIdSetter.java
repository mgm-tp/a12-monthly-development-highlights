package com.mgmtp.a12.template.server.utils;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.template.server.typings.pointers._person_dm._person.PAddresses;
import com.mgmtp.a12.template.server.typings.pointers._person_dm._person.PPhones;
import com.mgmtp.a12.template.server.typings.views.Person_DM;
import com.mgmtp.a12.template.server.typings.views._person_dm._person._phones.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class TestGroupIdSetter extends BaseSetUp {

    private static final PAddresses<Person_DM> FIRST_ADDRESS = Person_DM._pointer().person().addresses(1);
    private static final PAddresses<Person_DM> SECOND_ADDRESS = Person_DM._pointer().person().addresses(2);
    private static final PPhones<Person_DM> FIRST_PHONE = Person_DM._pointer().person().phones(1);
    private static final PPhones<Person_DM> SECOND_PHONE = Person_DM._pointer().person().phones(2);

    @Test
    void emptyDocument() {
        Person_DM personDoc = Person_DM._empty();

        Person_DM personDocWithIds = addIdToGroups(personDoc);

        assertNotNull(personDocWithIds);
    }

    @Test
    void addGroupIdToNewFirstInstance() {
        Person_DM personDoc = Person_DM._empty()
                ._with(FIRST_ADDRESS.street(), "test");

        Person_DM personDocWithIds = addIdToGroups(personDoc);

        assertNotBlank(personDocWithIds.person().addresses().getFirst().id());
    }

    @Test
    void replaceEmptyStringId() {
        Person_DM personDoc = Person_DM._empty()
                ._with(FIRST_ADDRESS.id(), "")
                ._with(FIRST_ADDRESS.street(), "test");

        Person_DM personDocWithIds = addIdToGroups(personDoc);

        assertNotBlank(personDocWithIds.person().addresses().getFirst().id());
    }

    @Test
    void replaceBlankId() {
        Person_DM personDoc = Person_DM._empty()
                ._with(FIRST_ADDRESS.id(), "  ")
                ._with(FIRST_ADDRESS.street(), "test");

        Person_DM personDocWithIds = addIdToGroups(personDoc);

        assertNotBlank(personDocWithIds.person().addresses().getFirst().id());
    }

    @Test
    void noAddition() {
        Person_DM personDoc = Person_DM._empty()
                ._with(FIRST_ADDRESS.id(), "test_id")
                ._with(FIRST_ADDRESS.street(), "test");

        Person_DM personDocWithIds = addIdToGroups(personDoc);

        assertEquals("test_id", personDocWithIds.person().addresses().getFirst().id());
    }

    @Test
    void addGroupIdToNewInstances1() {
        Person_DM personDoc = Person_DM._empty()
                ._with(FIRST_ADDRESS.street(), "test 1")
                ._with(SECOND_ADDRESS.street(), "test 2");

        Person_DM personDocWithIds = addIdToGroups(personDoc);

        assertNotBlank(personDocWithIds.person().addresses().getFirst().id());
        assertNotBlank(personDocWithIds.person().addresses().get(1).id());
    }

    @Test
    void addGroupIdToNewInstances2() {
        Person_DM personDoc = Person_DM._empty()
                ._with(FIRST_ADDRESS.street(), "test 1")
                ._with(SECOND_ADDRESS.street(), "test 2")
                ._with(FIRST_PHONE.type(), Type.MOBILE)
                ._with(SECOND_PHONE.type(), Type.WORK);

        Person_DM personDocWithIds = addIdToGroups(personDoc);

        assertNotBlank(personDocWithIds.person().addresses().getFirst().id());
        assertNotBlank(personDocWithIds.person().addresses().get(1).id());
        assertNotBlank(personDocWithIds.person().phones().getFirst().id());
        assertNotBlank(personDocWithIds.person().phones().get(1).id());
    }

    @Test
    void addGroupIdToNewSecondInstance() {
        Person_DM personDoc = Person_DM._empty()
                ._with(FIRST_ADDRESS.id(), "test_id")
                ._with(FIRST_ADDRESS.street(), "test 1")
                ._with(SECOND_ADDRESS.street(), "test 2");

        Person_DM personDocWithIds = addIdToGroups(personDoc);

        assertEquals("test_id", personDocWithIds.person().addresses().getFirst().id());
        assertNotBlank(personDocWithIds.person().addresses().get(1).id());
    }

    private Person_DM addIdToGroups(Person_DM personDoc) {
        DocumentV2 doc = personDoc._unwrap();
        DocumentV2 docWithIds = groupIdSetter.addIdToGroups(doc);
        return Person_DM._viewOf(docWithIds);
    }

    private void assertNotBlank(String s) {
        assertTrue(StringUtils.isNotBlank(s));
    }

}
