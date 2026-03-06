package com.mgmtp.a12.template.server.changelog;

import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.template.server.typings.pointers._person_dm._person.PAddresses;
import com.mgmtp.a12.template.server.typings.pointers._person_dm._person.PPersonalData;
import com.mgmtp.a12.template.server.typings.pointers._person_dm._person.PPhones;
import com.mgmtp.a12.template.server.typings.pointers._person_dm._person._personaldata.PPhoto;
import com.mgmtp.a12.template.server.typings.views.Person_DM;
import com.mgmtp.a12.template.server.typings.views._person_dm._person._personaldata.Gender;
import com.mgmtp.a12.template.server.typings.views._repetitionchange_dm._repetitionchange.ChangeType;
import com.mgmtp.a12.template.server.utils.BaseSetUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class TestChangelogService extends BaseSetUp {

    private static final PPersonalData<Person_DM> PERSONAL_DATA =
            Person_DM._pointer().person().personalData();
    private static final PAddresses<Person_DM> FIRST_ADDRESS =
            Person_DM._pointer().person().addresses(1);
    private static final PAddresses<Person_DM> SECOND_ADDRESS =
            Person_DM._pointer().person().addresses(2);
    private static final PPhones<Person_DM> FIRST_PHONE =
            Person_DM._pointer().person().phones(1);
    private static final PPhoto<Person_DM> PHOTO =
            Person_DM._pointer().person().personalData().photo();

    private static final String TEST_USER = "testUser";

    private ChangelogService changelogService;

    @BeforeEach
    void setUpChangelogService() {
        setupSecurityContext(TEST_USER);
        changelogService = new ChangelogService(documentModelLoader, documentModelService);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Field Changelog Tests")
    class FieldChangelogTests {

        @Test
        @DisplayName("Should add changelog entry when non-repeatable field value changes")
        void fieldValueChangedAddsChangelogEntry() {
            Person_DM reference = Person_DM._empty()
                    ._with(PERSONAL_DATA.firstName(), "John");

            Person_DM document = Person_DM._empty()
                    ._with(PERSONAL_DATA.firstName(), "Jane");

            Person_DM result = addChangelogInfo(document, reference);

            assertEquals(1, result.person().personalData().firstName_Changelog().size());
            var changelog = result.person().personalData().firstName_Changelog().getFirst();
            assertEquals("John", changelog.oldValue());
            assertEquals("Jane", changelog.newValue());
            assertEquals(TEST_USER, changelog.changeMetadata().changedBy());
            assertNotNull(changelog.changeMetadata().changedAt());
        }

        @Test
        @DisplayName("Should add changelog entry when field is newly set (no reference value)")
        void newFieldValueAddsChangelogEntryWithNewValueOnly() {
            Person_DM reference = Person_DM._empty();

            Person_DM document = Person_DM._empty()
                    ._with(PERSONAL_DATA.firstName(), "Jane");

            Person_DM result = addChangelogInfo(document, reference);

            assertEquals(1, result.person().personalData().firstName_Changelog().size());
            var changelog = result.person().personalData().firstName_Changelog().getFirst();
            assertNull(changelog.oldValue());
            assertEquals("Jane", changelog.newValue());
        }

        @Test
        @DisplayName("Should not add changelog entry when field value is unchanged")
        void fieldValueUnchangedNoChangelogEntry() {
            Person_DM reference = Person_DM._empty()
                    ._with(PERSONAL_DATA.firstName(), "John");

            Person_DM document = Person_DM._empty()
                    ._with(PERSONAL_DATA.firstName(), "John");

            Person_DM result = addChangelogInfo(document, reference);

            assertTrue(result.person().personalData().firstName_Changelog().isEmpty());
        }

        @Test
        @DisplayName("Should add multiple changelog entries when multiple fields change")
        void multipleFieldsChangedAddsMultipleChangelogEntries() {
            Person_DM reference = Person_DM._empty()
                    ._with(PERSONAL_DATA.firstName(), "John")
                    ._with(PERSONAL_DATA.lastName(), "Doe")
                    ._with(PERSONAL_DATA.gender(), Gender.MALE);

            Person_DM document = Person_DM._empty()
                    ._with(PERSONAL_DATA.firstName(), "Jane")
                    ._with(PERSONAL_DATA.lastName(), "Smith")
                    ._with(PERSONAL_DATA.gender(), Gender.FEMALE);

            Person_DM result = addChangelogInfo(document, reference);

            assertEquals(1, result.person().personalData().firstName_Changelog().size());
            assertEquals(1, result.person().personalData().lastName_Changelog().size());
            assertEquals(1, result.person().personalData().gender_Changelog().size());
        }

        @Test
        @DisplayName("Should add changelog entry when field is cleared")
        void fieldClearedAddsChangelogEntry() {
            Person_DM reference = Person_DM._empty()
                    ._with(PERSONAL_DATA.firstName(), "John");

            Person_DM document = Person_DM._empty();

            Person_DM result = addChangelogInfo(document, reference);

            assertEquals(1, result.person().personalData().firstName_Changelog().size());
            var changelog = result.person().personalData().firstName_Changelog().getFirst();
            assertEquals("John", changelog.oldValue());
            assertNull(changelog.newValue());
        }
    }

    @Nested
    @DisplayName("Group Repetition Changelog Tests")
    class GroupRepetitionChangelogTests {

        @Test
        @DisplayName("Should add ADDED changelog entry when new address repetition is added")
        void newAddressAddedAddsAddedChangelogEntry() {
            Person_DM reference = Person_DM._empty();

            Person_DM document = Person_DM._empty()
                    ._with(FIRST_ADDRESS.id(), "addr-1")
                    ._with(FIRST_ADDRESS.street(), "Main Street")
                    ._with(FIRST_ADDRESS.city(), "New York");

            Person_DM result = addChangelogInfo(document, reference);

            assertEquals(1, result.person().addresses_Changelog().size());
            var changelog = result.person().addresses_Changelog().getFirst();
            assertEquals(ChangeType.ADDED, changelog.changeType());
            assertNotNull(changelog.repetitionDetails());
            assertEquals(TEST_USER, changelog.changeMetadata().changedBy());
        }

        @Test
        @DisplayName("Should add DELETED changelog entry when address repetition is removed")
        void addressDeletedAddsDeletedChangelogEntry() {
            Person_DM reference = Person_DM._empty()
                    ._with(FIRST_ADDRESS.id(), "addr-1")
                    ._with(FIRST_ADDRESS.street(), "Main Street");

            Person_DM document = Person_DM._empty();

            Person_DM result = addChangelogInfo(document, reference);

            assertEquals(1, result.person().addresses_Changelog().size());
            var changelog = result.person().addresses_Changelog().getFirst();
            assertEquals(ChangeType.DELETED, changelog.changeType());
        }

        @Test
        @DisplayName("Should not add group changelog when same repetition exists in both")
        void sameRepetitionExistsNoGroupChangelog() {
            Person_DM reference = Person_DM._empty()
                    ._with(FIRST_ADDRESS.id(), "addr-1")
                    ._with(FIRST_ADDRESS.street(), "Main Street");

            Person_DM document = Person_DM._empty()
                    ._with(FIRST_ADDRESS.id(), "addr-1")
                    ._with(FIRST_ADDRESS.street(), "Main Street");

            Person_DM result = addChangelogInfo(document, reference);

            assertTrue(result.person().addresses_Changelog().isEmpty());
        }

        @Test
        @DisplayName("Should handle multiple additions and deletions")
        void multipleAdditionsAndDeletionsAddsCorrectChangelogEntries() {
            Person_DM reference = Person_DM._empty()
                    ._with(FIRST_ADDRESS.id(), "addr-1")
                    ._with(FIRST_ADDRESS.street(), "Street 1")
                    ._with(SECOND_ADDRESS.id(), "addr-2")
                    ._with(SECOND_ADDRESS.street(), "Street 2");

            Person_DM document = Person_DM._empty()
                    ._with(FIRST_ADDRESS.id(), "addr-1")
                    ._with(FIRST_ADDRESS.street(), "Street 1")
                    ._with(SECOND_ADDRESS.id(), "addr-3")
                    ._with(SECOND_ADDRESS.street(), "Street 3");

            Person_DM result = addChangelogInfo(document, reference);

            var changelogs = result.person().addresses_Changelog();
            assertEquals(2, changelogs.size());
            assertTrue(changelogs.stream().anyMatch(c -> c.changeType() == ChangeType.ADDED));
            assertTrue(changelogs.stream().anyMatch(c -> c.changeType() == ChangeType.DELETED));
            var street1Changelogs = result.person().addresses().getFirst().street_Changelog();
            assertEquals(0, street1Changelogs.size());
            var street2Changelogs = result.person().addresses().get(1).street_Changelog();
            assertEquals(1, street2Changelogs.size());
        }

        @Test
        @DisplayName("Sequential changes should also lead to the correst changelog entries")
        void sequentialChangesAddsCorrectChangelogEntries() {
            Person_DM reference = Person_DM._empty();

            // add first address
            Person_DM document = Person_DM._empty()
                    ._with(FIRST_ADDRESS.id(), "addr-1")
                    ._with(FIRST_ADDRESS.street(), "Street 1");

            Person_DM result = addChangelogInfo(document, reference);

            var changelogs = result.person().addresses_Changelog();
            assertEquals(1, changelogs.size());
            assertSame(ChangeType.ADDED, changelogs.getFirst().changeType());
            var street1Changelogs = result.person().addresses().getFirst().street_Changelog();
            assertEquals(1, street1Changelogs.size());

            // add second address
            reference = result;
            document = result._with(SECOND_ADDRESS.id(), "addr-2")
                    ._with(SECOND_ADDRESS.street(), "Street 2");

            result = addChangelogInfo(document, reference);

            changelogs = result.person().addresses_Changelog();
            assertEquals(2, changelogs.size());
            assertSame(ChangeType.ADDED, changelogs.get(0).changeType());
            assertSame(ChangeType.ADDED, changelogs.get(1).changeType());
            street1Changelogs = result.person().addresses().getFirst().street_Changelog();
            assertEquals(1, street1Changelogs.size());
            var street2Changelogs = result.person().addresses().get(1).street_Changelog();
            assertEquals(1, street2Changelogs.size());

            // change second address
            reference = result;
            document = result._with(SECOND_ADDRESS.street(), "Street 2 changed");

            result = addChangelogInfo(document, reference);

            changelogs = result.person().addresses_Changelog();
            assertEquals(2, changelogs.size());
            assertSame(ChangeType.ADDED, changelogs.get(0).changeType());
            assertSame(ChangeType.ADDED, changelogs.get(1).changeType());
            street1Changelogs = result.person().addresses().getFirst().street_Changelog();
            assertEquals(1, street1Changelogs.size());
            street2Changelogs = result.person().addresses().get(1).street_Changelog();
            assertEquals(2, street2Changelogs.size());

            // delete first address
            reference = result;
            document = Person_DM._viewOf(result._unwrap().withGroupRemoved(FIRST_ADDRESS._unwrap()));

            result = addChangelogInfo(document, reference);

            changelogs = result.person().addresses_Changelog();
            assertEquals(3, changelogs.size());
            assertSame(ChangeType.ADDED, changelogs.get(0).changeType());
            assertSame(ChangeType.ADDED, changelogs.get(1).changeType());
            assertSame(ChangeType.DELETED, changelogs.get(2).changeType());
            street1Changelogs = result.person().addresses().getFirst().street_Changelog();
            assertEquals(2, street1Changelogs.size());
        }
    }

    @Nested
    @DisplayName("Repeatable Field Changelog Tests")
    class RepeatableFieldChangelogTests {

        @Test
        @DisplayName("Should add field changelog for changed field within existing group repetition")
        void fieldChangedWithinGroupAddsFieldChangelog() {
            String addressId = "addr-1";

            Person_DM reference = Person_DM._empty()
                    ._with(FIRST_ADDRESS.id(), addressId)
                    ._with(FIRST_ADDRESS.street(), "Old Street");

            Person_DM document = Person_DM._empty()
                    ._with(FIRST_ADDRESS.id(), addressId)
                    ._with(FIRST_ADDRESS.street(), "New Street");

            Person_DM result = addChangelogInfo(document, reference);

            var streetChangelogs = result.person().addresses().getFirst().street_Changelog();
            assertEquals(1, streetChangelogs.size());
            assertEquals("Old Street", streetChangelogs.getFirst().oldValue());
            assertEquals("New Street", streetChangelogs.getFirst().newValue());
        }

        @Test
        @DisplayName("Should add field changelog for changed field within existing group repetition "
                + "- This should also work for group repetitions > 1")
        void fieldChangedWithinGroupAddsFieldChangelogHigherRepetitions() {
            String addressId2 = "addr-2";

            Person_DM reference = Person_DM._empty()
                    ._with(FIRST_ADDRESS.id(), "addr-1")
                    ._with(FIRST_ADDRESS.street(), "Unchanged Other Street")
                    ._with(SECOND_ADDRESS.id(), addressId2)
                    ._with(SECOND_ADDRESS.street(), "Old Street");

            Person_DM document = Person_DM._empty()
                    ._with(FIRST_ADDRESS.id(), "addr-1")
                    ._with(FIRST_ADDRESS.street(), "Unchanged Other Street")
                    ._with(SECOND_ADDRESS.id(), addressId2)
                    ._with(SECOND_ADDRESS.street(), "New Street");

            Person_DM result = addChangelogInfo(document, reference);

            var streetChangelogs = result.person().addresses().get(1).street_Changelog();
            assertEquals(1, streetChangelogs.size());
            assertEquals("Old Street", streetChangelogs.getFirst().oldValue());
            assertEquals("New Street", streetChangelogs.getFirst().newValue());
        }

        @Test
        @DisplayName("Should add field changelog with newValue only for new repetition fields")
        void newRepetitionFieldsAddsChangelogWithNewValueOnly() {
            Person_DM reference = Person_DM._empty();

            Person_DM document = Person_DM._empty()
                    ._with(FIRST_ADDRESS.id(), "addr-new")
                    ._with(FIRST_ADDRESS.street(), "New Street")
                    ._with(FIRST_ADDRESS.city(), "New City");

            Person_DM result = addChangelogInfo(document, reference);

            var streetChangelogs = result.person().addresses().getFirst().street_Changelog();
            assertEquals(1, streetChangelogs.size());
            assertNull(streetChangelogs.getFirst().oldValue());
            assertEquals("New Street", streetChangelogs.getFirst().newValue());
        }
    }

    @Nested
    @DisplayName("Attachment Changelog Tests")
    class AttachmentChangelogTests {

        @Test
        @DisplayName("Should add attachment changelog when new photo is added")
        void newPhotoAddedAddsAttachmentChangelog() {
            Person_DM reference = Person_DM._empty();

            Person_DM document = Person_DM._empty()
                    ._with(PHOTO.attachment_id(), "photo-1")
                    ._with(PHOTO.original_filename(), "photo.jpg");

            Person_DM result = addChangelogInfo(document, reference);

            var photoChangelogs = result.person().personalData().photo_Changelog();
            assertEquals(1, photoChangelogs.size());
            assertNotNull(photoChangelogs.getFirst().newAttachment());
            assertNull(photoChangelogs.getFirst().oldAttachment());
        }

        @Test
        @DisplayName("Should add attachment changelog when photo is changed")
        void photoChangedAddsAttachmentChangelogWithOldAndNew() {
            Person_DM reference = Person_DM._empty()
                    ._with(PHOTO.attachment_id(), "photo-old")
                    ._with(PHOTO.original_filename(), "old.jpg");

            Person_DM document = Person_DM._empty()
                    ._with(PHOTO.attachment_id(), "photo-new")
                    ._with(PHOTO.original_filename(), "new.jpg");

            Person_DM result = addChangelogInfo(document, reference);

            var photoChangelogs = result.person().personalData().photo_Changelog();
            assertEquals(1, photoChangelogs.size());
            assertNotNull(photoChangelogs.getFirst().newAttachment());
            assertNotNull(photoChangelogs.getFirst().oldAttachment());
        }

        @Test
        @DisplayName("Should not add attachment changelog when attachment_id is unchanged")
        void sameAttachmentIdNoChangelog() {
            String attachmentId = "photo-1";

            Person_DM reference = Person_DM._empty()
                    ._with(PHOTO.attachment_id(), attachmentId);

            Person_DM document = Person_DM._empty()
                    ._with(PHOTO.attachment_id(), attachmentId);

            Person_DM result = addChangelogInfo(document, reference);

            assertTrue(result.person().personalData().photo_Changelog().isEmpty());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle both empty documents without errors")
        void bothDocumentsEmptyNoChangelog() {
            Person_DM reference = Person_DM._empty();
            Person_DM document = Person_DM._empty();

            // Just verify no exception is thrown - empty documents return null for person()
            Person_DM result = addChangelogInfo(document, reference);

            assertNotNull(result);
        }

        @Test
        @DisplayName("Should record correct username in changelog metadata")
        void changelogMetadataHasCorrectUsername() {
            String expectedUser = "specificUser";
            setupSecurityContext(expectedUser);

            Person_DM reference = Person_DM._empty();
            Person_DM document = Person_DM._empty()
                    ._with(PERSONAL_DATA.firstName(), "Test");

            Person_DM result = addChangelogInfo(document, reference);

            assertEquals(expectedUser,
                    result.person().personalData().firstName_Changelog().getFirst()
                            .changeMetadata().changedBy());
        }

        @Test
        @DisplayName("Should record timestamp in changelog metadata")
        void changelogMetadataHasTimestamp() {
            Person_DM reference = Person_DM._empty();
            Person_DM document = Person_DM._empty()
                    ._with(PERSONAL_DATA.firstName(), "Test");

            Person_DM result = addChangelogInfo(document, reference);

            assertNotNull(result.person().personalData().firstName_Changelog().getFirst()
                    .changeMetadata().changedAt());
        }

        @Test
        @DisplayName("Should handle combined field, group, and attachment changes")
        void combinedChangesAllChangelogsAdded() {
            Person_DM reference = Person_DM._empty()
                    ._with(PERSONAL_DATA.firstName(), "John")
                    ._with(FIRST_ADDRESS.id(), "addr-old")
                    ._with(FIRST_ADDRESS.street(), "Old Street")
                    ._with(PHOTO.attachment_id(), "photo-old");

            Person_DM document = Person_DM._empty()
                    ._with(PERSONAL_DATA.firstName(), "Jane")
                    ._with(FIRST_ADDRESS.id(), "addr-new")
                    ._with(FIRST_ADDRESS.street(), "New Street")
                    ._with(PHOTO.attachment_id(), "photo-new");

            Person_DM result = addChangelogInfo(document, reference);

            assertFalse(result.person().personalData().firstName_Changelog().isEmpty());
            assertFalse(result.person().addresses_Changelog().isEmpty());
            assertFalse(result.person().personalData().photo_Changelog().isEmpty());
        }
    }

    private Person_DM addChangelogInfo(Person_DM document, Person_DM reference) {
        DocumentV2 result = changelogService.addChangelogInfo(
                document._unwrap(),
                reference._unwrap()
        );
        return Person_DM._viewOf(result);
    }

    private void setupSecurityContext(String username) {
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, Collections.emptyList());

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
