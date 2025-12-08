import { expect, test } from "../../fixtures";
import { USERNAMES } from "../../types";
import { TestID } from "../../types/testIds";

test.describe("Login flow", () => {
    for (const username of USERNAMES) {
        test(`should login with the ${username} user`, async ({ getPageAs }) => {
            const page = await getPageAs(username);
            await page.goto("/");

            const popupTrigger = page
                .getByTestId(TestID.APPLICATION_HEADER)
                .getByTestId(TestID.POPUP_TRIGGER_ELEMENT)
                .filter({ hasText: username })
                .first();
            await expect(popupTrigger).toBeVisible();
            await popupTrigger.click();
            await expect(page.getByTestId(TestID.POPUP_MENU)).toContainText("Logged in as");
            await expect(page.getByTestId(TestID.POPUP_MENU)).toContainText(username);
        });
    }
});
