import { test } from "../../fixtures";
import { OverviewPage } from "../../pages/OverviewPage";
import { DataType, TestData } from "../../types";

const projectData: TestData[] = [
    { label: "Name", value: "FlexiFlow", type: DataType.String },
    { label: "Description", value: "Flexible Flow", type: DataType.String }
];

const now = new Date().valueOf();
const taskData: TestData[] = [
    { label: "Title", value: `Create CDM document ${now}`, type: DataType.String },
    { label: "Status", value: "Done", type: DataType.Select }
];

test.describe.serial("Task CDM Module", () => {
    let overviewPage: OverviewPage;
    test.beforeEach(async ({ getPageAs }) => {
        const page = await getPageAs("admin");
        overviewPage = new OverviewPage(page);
        await overviewPage.gotoHome();
        await overviewPage.switchOnEnglish();
        await overviewPage.clickMenuItem("Task");
    });

    test("should add Task with Project", async () => {
        await overviewPage.addComposeDocument(taskData, projectData);
    });

    test("should export documents to CSV", async () => {
        await overviewPage.exportDocumentsToCSV("export_Task_CDM.csv");
    });
});
