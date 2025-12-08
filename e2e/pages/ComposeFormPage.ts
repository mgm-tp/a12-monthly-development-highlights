import { Locator } from "@playwright/test";

import { expect, Page } from "../fixtures";
import { TestData } from "../types";

import { FormPage } from "./FormPage";

export class ComposeFormPage extends FormPage {
    protected readonly relationForm: Locator;

    constructor(
        protected readonly page: Page,
        protected readonly formLocator: Locator = page.getByRole("form").first(),
        protected readonly relationFormLocator: Locator = page.getByRole("form").nth(1)
    ) {
        super(page, formLocator);
        this.relationForm = relationFormLocator;
    }

    async createDocument(data: TestData[], relationData: TestData[] = []): Promise<void> {
        for (const field of data) {
            await this.inputFieldValue(field);
        }

        if (relationData.length === 0) {
            await this.saveForm();
            return;
        }

        await this.form.getByRole("button").filter({ hasText: "Add" }).last().click();
        await expect(this.page.getByRole("form")).toHaveCount(2);
        const relationForm = new FormPage(this.page, this.relationForm);

        for (const field of relationData) {
            await relationForm.inputFieldValue(field);
        }

        await relationForm.saveForm();
        await this.saveForm();
    }
}
