import { test, expect, Page } from "../../fixtures";
import { BasePage } from "../../pages/BasePage";
import { TestID } from "../../types/testIds";

test.describe("Test language switching", () => {
    let page: Page;
    test.beforeEach(async ({ getPageAs }) => {
        page = await getPageAs("admin");
        const app = new BasePage(page);
        await app.gotoHome();
    });

    test("should display in English", async () => {
        await page.getByTestId(TestID.HEADER_TRIGGER_TEXT).filter({ hasText: "EN" }).click();
        await page.getByTestId(TestID.LIST_ITEM_TEXT).filter({ hasText: "English (EN)" }).click();
        const languagePopupTrigger = page.getByTestId(TestID.POPUP_TRIGGER_ELEMENT).filter({ hasText: "EN" }).first();
        await expect(languagePopupTrigger).toBeVisible();
        await expect(page.getByTestId(TestID.CONTENTBOX_TITLE)).toHaveText("Overview");
    });

    test("should display in German", async () => {
        await page.getByTestId(TestID.HEADER_TRIGGER_TEXT).filter({ hasText: "EN" }).click();
        await page.getByTestId(TestID.LIST_ITEM_TEXT).filter({ hasText: "Deutsch (DE)" }).click();
        const languagePopupTrigger = page.getByTestId(TestID.POPUP_TRIGGER_ELEMENT).filter({ hasText: "DE" }).first();
        await expect(languagePopupTrigger).toBeVisible();
        await expect(page.getByTestId(TestID.CONTENTBOX_TITLE)).toHaveText("Überblick");
    });
});
