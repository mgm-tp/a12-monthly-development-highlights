import { expect, Page, test } from "../../fixtures";
import { OverviewPage } from "../../pages/OverviewPage";
import { TreePage } from "../../pages/TreePage";
import { DataType, TestData } from "../../types";
import { TestID } from "../../types/testIds";

test.describe.serial("Organization Module", () => {
    test.describe("Setup test data", () => {
        let overviewPage: OverviewPage;

        const testData: { personData: TestData[]; companyData: TestData[] }[] = [
            {
                personData: [
                    { label: "First Name", value: "Safe", type: DataType.String },
                    { label: "Last Name", value: "Corax", type: DataType.String },
                    { label: "Email Address", value: "corvuscorax@mgm-tp.com", type: DataType.String },
                    { label: "Date of Birth", value: "10/13/1958", type: DataType.String },
                    { label: "Place of Birth", value: "Lycaeus", type: DataType.String },
                    { label: "Nationality", value: "British", type: DataType.String },
                    { label: "Gender", value: "Male", type: DataType.Select }
                ],
                companyData: [
                    { label: "Company Name", value: "Safe Heaven", type: DataType.String },
                    { label: "Website", value: "https://safeheaven.com", type: DataType.String }
                ]
            },
            {
                personData: [
                    { label: "First Name", value: "John", type: DataType.String },
                    { label: "Last Name", value: "Smith", type: DataType.String },
                    { label: "Email Address", value: "johnsmith@mgm-tp.com", type: DataType.String },
                    { label: "Date of Birth", value: "11/20/1960", type: DataType.String },
                    { label: "Place of Birth", value: "Lycaeus", type: DataType.String },
                    { label: "Nationality", value: "British", type: DataType.String },
                    { label: "Gender", value: "Male", type: DataType.Select }
                ],
                companyData: [
                    { label: "Company Name", value: "Business Partner", type: DataType.String },
                    { label: "Website", value: "https://businesspartner.com", type: DataType.String }
                ]
            }
        ];

        test.beforeEach(async ({ getPageAs }) => {
            const page = await getPageAs("admin");
            overviewPage = new OverviewPage(page);
            await overviewPage.gotoHome();
            await overviewPage.switchOnEnglish();
            await overviewPage.clickMenuItem("Employment");
        });

        test("should add persons with companies", async () => {
            for (const { personData, companyData } of testData) {
                await overviewPage.addComposeDocument(personData, companyData);
            }
        });
    });

    test.describe("Organization Tree", () => {
        let treePage: TreePage;
        let page: Page;
        test.beforeEach(async ({ getPageAs }) => {
            page = await getPageAs("admin");
            treePage = new TreePage(page);
            await treePage.gotoHome();
            await treePage.switchOnEnglish();
            await treePage.clickMenuItem("Organization");
        });

        test("expand and collapse all", async () => {
            const treeRootNodes = await treePage.getTreeNodesAtLevel(0).all();
            await Promise.all(treeRootNodes.map(async (node) => treePage.shouldNotBeExpanded(node)));

            await treePage.expandAll();
            await Promise.all(treeRootNodes.map(async (node) => treePage.shouldBeExpanded(node)));

            await treePage.collapseAll();
            await Promise.all(treeRootNodes.map(async (node) => treePage.shouldNotBeExpanded(node)));
        });

        test("drag and drop", async () => {
            await treePage.expandAll();
            const rows = page.getByTestId(TestID.TABLE_BODY_ROW);
            const source = rows.getByTestId(TestID.TREE_NODE_TITLE).filter({ hasText: "John" });
            await expect(source).toBeVisible();
            const target = treePage.getTreeNodesAtLevel(0).filter({ hasText: "Safe Heaven" });
            await expect(target).toBeVisible();
            await treePage.dragAndDrop(source, target);

            const modal = page.getByTestId(TestID.MODAL_OVERLAY_CONTENT);
            await treePage.finishedLoading();
            await expect(modal).toBeVisible();

            await modal.getByLabel("Role").getByTestId(TestID.SELECT_INPUT).selectOption("Other");
            await modal.getByRole("button", { name: "Save" }).click();
            await treePage.finishedLoading();

            await treePage.assertExpandableRowTitles(["Safe Heaven"]);
            await treePage.assertNotExpandableRowTitles(["John", "Safe"]);
        });

        test("expand and collapse a node", async () => {
            const row = page.getByTestId(TestID.TABLE_BODY_ROW).filter({ hasText: "Safe Heaven" });
            await treePage.shouldNotBeExpanded(row);

            await treePage.expandNode(row);
            await treePage.shouldBeExpanded(row);

            await treePage.collapseNode(row);
            await treePage.shouldNotBeExpanded(row);
        });
    });
});
