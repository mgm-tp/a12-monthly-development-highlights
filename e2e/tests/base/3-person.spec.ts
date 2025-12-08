import path from "path";

import { expect, Page, test } from "../../fixtures";
import { DataType, FieldTestData, FileTestData, TestData, TestUsername } from "../../types";
import { TestID } from "../../types/testIds";
import { OverviewPage } from "../../pages/OverviewPage";
import { FormPage } from "../../pages/FormPage";

const personData: TestData[] = [
    { label: "First Name", value: "Paddington", type: DataType.String },
    { label: "Last Name", value: "Brown", type: DataType.String },
    {
        label: "Email Address",
        value: "paddingtonbrown@mgm-tp.com",
        type: DataType.String
    },
    { label: "Date of Birth", value: "10/13/1958", type: DataType.String },
    { label: "Place of Birth", value: "Darkest Peru", type: DataType.String },
    { label: "Nationality", value: "British", type: DataType.String },
    { label: "Gender", value: "MALE", type: DataType.Select }
];
const ownershipTestData: Record<TestUsername, TestData[]> = {
    admin: [
        { label: "First Name", value: "Thanh", type: DataType.String },
        { label: "Last Name", value: "Luke", type: DataType.String },
        {
            label: "Email Address",
            value: "thanh.luke@mgm-tp.com",
            type: DataType.String
        },
        { label: "Date of Birth", value: "09/09/1988", type: DataType.String },
        { label: "Place of Birth", value: "Da Lat", type: DataType.String },
        { label: "Nationality", value: "Viet Nam", type: DataType.String },
        { label: "Gender", value: "MALE", type: DataType.Select }
    ],
    user1: [
        { label: "First Name", value: "Black", type: DataType.String },
        { label: "Last Name", value: "Francis", type: DataType.String },
        {
            label: "Email Address",
            value: "BlackFrancis@mgm-tp.com",
            type: DataType.String
        },
        { label: "Date of Birth", value: "06/06/1965", type: DataType.String },
        {
            label: "Place of Birth",
            value: "Boston, Massachusetts",
            type: DataType.String
        },
        { label: "Nationality", value: "U.S.", type: DataType.String },
        { label: "Gender", value: "MALE", type: DataType.Select }
    ],
    user2: [
        { label: "First Name", value: "Joey", type: DataType.String },
        { label: "Last Name", value: "Santiago", type: DataType.String },
        {
            label: "Email Address",
            value: "Joey.Santiago@mgm-tp.com",
            type: DataType.String
        },
        { label: "Date of Birth", value: "12/12/1960", type: DataType.String },
        {
            label: "Place of Birth",
            value: "Longmeadow, Massachusetts",
            type: DataType.String
        },
        { label: "Nationality", value: "U.S.", type: DataType.String },
        { label: "Gender", value: "MALE", type: DataType.Select }
    ]
};

const updateData: TestData[] = [
    { label: "Email Address", value: "pbrown@mgm-tp.com", type: DataType.String },
    { label: "Date of Birth", value: "06/25/1958", type: DataType.String }
];

const profileData: FileTestData = {
    locator: `[data-role=${TestID.FILE_UPLOAD_CONTROL}]`,
    filePath: path.resolve(__dirname, "../../fixtures/image.png"),
    value: "image.png",
    type: DataType.File
};

const phoneNumberData: FieldTestData[][] = [
    [{ label: "Phone Number", value: "123456789", type: DataType.String }],
    [
        { label: "Phone Number", value: "987654321", type: DataType.String },
        { label: "Type", value: "MOBILE", type: DataType.Select }
    ],
    [
        { label: "Phone Number", value: "123123123", type: DataType.String },
        { label: "Type", value: "WORK", type: DataType.Select }
    ]
];

const addressData: FieldTestData[][] = [
    [
        { label: "Street", value: "71 Quang Trung", type: DataType.String },
        { label: "City", value: "Da nang", type: DataType.String },
        { label: "Country", value: "Viet Nam", type: DataType.String },
        { label: "Post Code", value: "50200", type: DataType.String }
    ],
    [
        { label: "Street", value: "Taunusstr. 23", type: DataType.String },
        { label: "City", value: "Munich", type: DataType.String },
        { label: "Country", value: "German", type: DataType.String },
        { label: "Post Code", value: "80689", type: DataType.String }
    ],
    [
        { label: "Street", value: "Letenské náměstí 4/157", type: DataType.String },
        { label: "City", value: "Prague", type: DataType.String },
        { label: "Country", value: "Czech", type: DataType.String },
        { label: "Post Code", value: "11800", type: DataType.String }
    ]
];

const newDateOfBirth = updateData[1].value;

test.describe.serial("Person Module", () => {
    test.describe("Person Module - CRUD", () => {
        let page: Page;
        let overviewPage: OverviewPage;
        let formPage: FormPage;

        test.beforeEach(async ({ getPageAs }) => {
            page = await getPageAs("admin");
            overviewPage = new OverviewPage(page);
            formPage = new FormPage(page);
            await overviewPage.gotoHome();
            await overviewPage.switchOnEnglish();
            await overviewPage.clickMenuItem("Person");
        });

        test("should create a new person", async () => {
            await overviewPage.addDocument(personData);
        });

        test("should add profile picture for person", async () => {
            const row = overviewPage.getRow(personData[0].value);
            await row.click();

            await formPage.toBeVisible();
            await formPage.uploadFileField(profileData);
            await formPage.saveForm();
            await formPage.toBeHidden();

            await overviewPage.assertThumbnailInTable(profileData.value, row);
        });

        test("Should update a person", async () => {
            await page.getByTestId(TestID.TABLE_BODY_ROW).first().click();

            await formPage.toBeVisible();
            await formPage.updateDocument(updateData);
            await formPage.toBeHidden();

            await overviewPage.assertDocumentsInTable([newDateOfBirth]);
        });

        test("should add 3 addresses", async () => {
            const sectionHeadlineLabel = "Address";

            const row = overviewPage.getRow(newDateOfBirth);
            await row.click();

            await formPage.toBeVisible();
            await formPage.inputInlineRepeatFieldValues(sectionHeadlineLabel, addressData);
            await formPage.saveForm();
            await formPage.toBeHidden();

            await row.click();
            await formPage.toBeVisible();
            await formPage.assertInlineRepeatFieldValues(sectionHeadlineLabel, addressData);
        });

        test("should add 3 phone numbers", async () => {
            const sectionHeadlineLabel = "Phone";
            const row = overviewPage.getRow(newDateOfBirth);
            await row.click();
            await formPage.toBeVisible();
            await formPage.inputInlineRepeatFieldValues(sectionHeadlineLabel, phoneNumberData);
            await formPage.saveForm();
            await formPage.toBeHidden();

            await row.click();
            await formPage.toBeVisible();
            await formPage.assertInlineRepeatFieldValues(sectionHeadlineLabel, phoneNumberData);
        });

        test("should delete a person", async () => {
            const row = overviewPage.getRow(newDateOfBirth);
            await overviewPage.deleteRow(row);
            await expect(row).toBeHidden();
        });
    });

    test.describe("Person Module - Document ownership", () => {
        async function setupTest(user: TestUsername, getPageAs: (user: TestUsername) => Promise<Page>) {
            const page = await getPageAs(user as TestUsername);
            const overviewPage = new OverviewPage(page);
            await overviewPage.gotoHome();
            await overviewPage.switchOnEnglish();
            await overviewPage.clickMenuItem("Person");
            return { page, overviewPage };
        }

        Object.entries(ownershipTestData).forEach(([user, testData]) => {
            test(`should create a new person with ${user}`, async ({ getPageAs }) => {
                const { overviewPage } = await setupTest(user as TestUsername, getPageAs);
                await overviewPage.addDocument(testData);
            });
        });

        Object.entries(ownershipTestData).forEach(([user, testData]) => {
            test(`should show documents by ${user} ownership accordingly`, async ({ getPageAs }) => {
                const { page, overviewPage } = await setupTest(user as TestUsername, getPageAs);

                await overviewPage.assertDocumentsInTable([testData[0].value]);
                await page.getByTestId(TestID.TABLE_BODY_ROW).filter({ hasText: testData[0].value }).click();

                const formPage = new FormPage(page);
                await formPage.toBeVisible();
                await formPage.assertFormValues(testData);
            });
        });

        Object.entries(ownershipTestData).forEach(([user, testData]) => {
            test(`Owner ${user} can delete its document`, async ({ getPageAs }) => {
                const { overviewPage } = await setupTest(user as TestUsername, getPageAs);
                await overviewPage.deleteDocument(testData[0].value);
            });
        });
    });
});
