import path from "path";

import { test } from "../../fixtures";
import { OverviewPage } from "../../pages/OverviewPage";
import { DataType, FieldTestData, TestData } from "../../types";
import { TestID } from "../../types/testIds";

const personData: TestData[] = [
    { label: "First Name", value: "Corvus", type: DataType.String },
    { label: "Last Name", value: "Corax", type: DataType.String },
    { label: "Gender", value: "Male", type: DataType.Select },
    { label: "Date of Birth", value: "10/13/1958", type: DataType.String },
    { label: "Place of Birth", value: "Lycaeus", type: DataType.String },
    { label: "Email Address", value: "corvuscorax@mgm-tp.com", type: DataType.String },
    { label: "Nationality", value: "British", type: DataType.String },
    {
        locator: `[data-role=${TestID.FILE_UPLOAD_CONTROL}]`,
        filePath: path.resolve(__dirname, "../../fixtures/image.png"),
        value: "image.png",
        type: DataType.File
    }
];

const companyData: FieldTestData[] = [
    { label: "Company Name", value: "Raven Guard", type: DataType.String },
    { label: "Website", value: "https://ravenguard.com", type: DataType.String }
];

test.describe("Employment Module", () => {
    test.describe.serial("Employment Module - Create", () => {
        let overviewPage: OverviewPage;

        test.beforeEach(async ({ getPageAs }) => {
            const page = await getPageAs("admin");
            overviewPage = new OverviewPage(page);
            await overviewPage.gotoHome();
            await overviewPage.clickMenuItem("Employment");
        });

        test("should create a new employment", async () => {
            await overviewPage.addComposeDocument(personData, companyData);

            await overviewPage.assertThumbnailInTable("image.png", overviewPage.getRow(personData[0].value));

            await overviewPage.clickMenuItem("Employment");
            await overviewPage.assertDocumentsInTable([companyData[0].value]);
        });
    });
});
