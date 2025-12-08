import { expect, Page, test } from "../../fixtures";
import { BasePage } from "../../pages/BasePage";

test.describe("CE Welcome page module", () => {
    let app: BasePage;
    let page: Page;

    test.beforeEach(async ({ getPageAs }) => {
        page = await getPageAs("admin");
        app = new BasePage(page);
        await app.gotoHome();
        await app.switchOnEnglish();
        await app.clickMenuItem("Welcome");
    });

    test("should display welcome page", async () => {
        await expect(page.getByRole("heading", { name: "Basic Workspace" })).toBeVisible();
        await expect(page.getByRole("paragraph").filter({ hasText: "This is an example workspace" })).toBeVisible();
    });
});
