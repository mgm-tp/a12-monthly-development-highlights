import { Locator } from "@playwright/test";

import { expect, Page } from "../fixtures";
import { TestID } from "../types/testIds";

export class BasePage {
    constructor(protected readonly page: Page) {}

    async finishedLoading(scope: Locator | Page = this.page) {
        const loadings = await scope.getByTestId(TestID.PROGRESS_INDICATOR_OUTER_OVERLAY).all();
        for (const loading of loadings) {
            await expect(loading).toBeHidden();
        }
    }

    async gotoHome() {
        await this.page.goto("/");
        await this.finishedLoading();
    }

    async clickMenuItem(label: string) {
        await this.page
            .getByTestId(TestID.MENU_ITEM)
            .filter({ has: this.page.getByText(label, { exact: true }) })
            .click();
        await this.finishedLoading();
    }

    async switchOnEnglish() {
        await this.page.getByTestId(TestID.HEADER_TRIGGER_TEXT).filter({ hasText: "EN" }).click();
        await this.page.getByTestId(TestID.LIST_ITEM_TEXT).filter({ hasText: "English (EN)" }).click();
        const languagePopupTrigger = this.page
            .getByTestId(TestID.POPUP_TRIGGER_ELEMENT)
            .filter({ hasText: "EN" })
            .first();
        await expect(languagePopupTrigger).toBeVisible();
    }
}
