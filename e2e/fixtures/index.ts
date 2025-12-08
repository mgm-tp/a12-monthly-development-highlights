import { test as base, expect, Page, BrowserContext } from "@playwright/test";

import { StorageState, TestUsername } from "../types";
import { getUserStorageState } from "../utils/files";

type WorkerFixtureOptions = {
    getPageAs: (userName: TestUsername) => Promise<Page>;
};

export const test = base.extend<{}, WorkerFixtureOptions>({
    getPageAs: [
        async ({ browser }, use) => {
            const storageStates: Map<TestUsername, StorageState> = new Map();
            const contexts: BrowserContext[] = [];

            async function getPageAs(username: TestUsername): Promise<Page> {
                if (!storageStates.has(username)) {
                    const storageState = await getUserStorageState(username);
                    storageStates.set(username, storageState);
                }

                const context = await browser.newContext({
                    storageState: storageStates.get(username)
                });
                contexts.push(context);
                const page: Page = await context.newPage();
                return page;
            }

            await use(getPageAs);

            await Promise.allSettled(
                contexts.map(async (context) => {
                    try {
                        await context.close();
                    } catch (error) {
                        console.warn("Failed to close browser context:", error);
                    }
                })
            );
        },
        { scope: "worker" }
    ]
});

export { expect, Page };
