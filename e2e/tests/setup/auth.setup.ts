import { expect, test } from "@playwright/test";

import { TestUsername, USERNAMES } from "../../types";
import USERS from "../../fixtures/users.json";
import { TestID } from "../../types/testIds";
import { ensureAuthDirExists, getUserAuthStorageStatePath } from "../../utils/files";

test("Auth setup", async ({ browser }) => {
    await ensureAuthDirExists();

    async function setupAuthForUser(username: TestUsername) {
        const context = await browser.newContext();
        const page = await context.newPage();
        const user = USERS[username];
        await page.goto("/");
        await page.fill("#username", user.username);
        await page.fill("#password", user.password);
        await page.press("#password", "Enter");

        const popupTrigger = page
            .getByTestId(TestID.APPLICATION_HEADER)
            .getByTestId(TestID.POPUP_TRIGGER_ELEMENT)
            .filter({ hasText: username })
            .first();
        await expect(popupTrigger).toBeVisible({ timeout: 30_000 });
        await context.storageState({ path: getUserAuthStorageStatePath(username) });
    }

    await Promise.all(USERNAMES.map((username) => setupAuthForUser(username)));
});
