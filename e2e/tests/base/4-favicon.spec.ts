import { expect } from "@playwright/test";

import { test } from "../../fixtures";
import { BasePage } from "../../pages/BasePage";

test.describe("Favicon test", () => {
    test("favicon should display correctly", async ({ getPageAs }) => {
        const page = await getPageAs("admin");
        const app = new BasePage(page);
        await app.gotoHome();
        const favicon = page.locator('link[rel="icon"]');
        await expect(favicon).toHaveAttribute("href", /favicon\.svg(\?.*)?$/);
    });
});
