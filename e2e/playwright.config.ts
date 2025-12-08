import { defineConfig, devices } from "@playwright/test";

import { BASE_URL } from "./utils/config";

export default defineConfig({
    testDir: "./tests",
    forbidOnly: !!process.env.CI,
    fullyParallel: true,
    retries: process.env.CI ? 1 : 0,
    workers: process.env.CI ? 3 : undefined,
    reporter: "html",
    use: {
        headless: true,
        viewport: { width: 1200, height: 660 },
        baseURL: BASE_URL,
        testIdAttribute: "data-role",
        trace: "on-first-retry"
    },
    timeout: 60_000,
    expect: {
        timeout: 10_000
    },
    projects: [
        {
            name: "base",
            use: { ...devices["Desktop Chrome"], channel: "chromium" },
            testDir: "./tests/base",
            dependencies: ["setup-base"]
        },
        {
            name: "setup-base",
            use: { ...devices["Desktop Chrome"], channel: "chromium" },
            testDir: "./tests/base",
            testMatch: /0-clean\.setup\.ts/,
            dependencies: ["setup-auth"]
        },

        {
            name: "extended",
            use: { ...devices["Desktop Chrome"], channel: "chromium" },
            testDir: "./tests/extended",
            dependencies: ["setup-extended"]
        },
        {
            name: "setup-extended",
            use: { ...devices["Desktop Chrome"], channel: "chromium" },
            testDir: "./tests/extended",
            testMatch: /0-clean\.setup\.ts/,
            dependencies: ["setup-auth"]
        },

        { name: "setup-auth", testMatch: /auth\.setup\.ts/, teardown: "cleanup" },
        { name: "cleanup", testMatch: /auth\.teardown\.ts/ }
    ]
});
