import { Page, test } from "../../fixtures";
import { OverviewPage } from "../../pages/OverviewPage";

test.describe("Person Module - Clean up", () => {
    let page: Page;
    let overviewPage: OverviewPage;
    test.beforeEach(async ({ getPageAs }) => {
        page = await getPageAs("admin");
        overviewPage = new OverviewPage(page);
        await overviewPage.gotoHome();
        await overviewPage.clickMenuItem("Person");
    });

    test("should delete all persons in the overview", async () => {
        await overviewPage.deleteAllRows();
    });
});
