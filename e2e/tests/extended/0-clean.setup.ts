import { Page, test } from "../../fixtures";
import { OverviewPage } from "../../pages/OverviewPage";

test.describe("Company Module - Clean up", () => {
    let page: Page;
    let overviewPage: OverviewPage;
    test.beforeEach(async ({ getPageAs }) => {
        page = await getPageAs("admin");
        overviewPage = new OverviewPage(page);
        await overviewPage.gotoHome();
        await overviewPage.clickMenuItem("Company");
    });

    test("should delete all companies in the overview", async () => {
        await overviewPage.deleteAllRows();
    });
});
