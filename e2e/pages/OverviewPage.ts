import { Locator } from "@playwright/test";

import { TestID } from "../types/testIds";
import { TestData } from "../types";
import { Page, expect } from "../fixtures";
import { API_PATH, waitForApiReponse } from "../utils/api";

import { BasePage } from "./BasePage";
import { FormPage } from "./FormPage";
import { ComposeFormPage } from "./ComposeFormPage";

export class OverviewPage extends BasePage {
    private readonly table: Locator;

    constructor(protected readonly page: Page) {
        super(page);
        this.table = page.getByTestId(TestID.TABLE);
    }
    async assertDocumentsInTable(documents: string[]) {
        for (const doc of documents) {
            await expect(this.table.getByTestId(TestID.TABLE_BODY_ROW).filter({ hasText: doc })).toBeVisible();
        }
    }

    async assertThumbnailInTable(thumbnailFileName: string, locator: Locator = this.table) {
        await expect(locator.getByRole("img", { name: thumbnailFileName })).toBeVisible();
    }

    async addDocument(document: TestData[], formLocator?: Locator) {
        await this.page.getByRole("button", { name: "Add" }).click();
        const formPage = new FormPage(this.page, formLocator);
        await formPage.finishedLoading();
        await formPage.toBeVisible();
        await formPage.createDocument(document);
        await this.assertDocumentsInTable([document[0].value]);
    }

    async addComposeDocument(
        document: TestData[],
        relationData: TestData[] = [],
        formLocator?: Locator,
        relationFormLocator?: Locator
    ) {
        await this.page.getByRole("button", { name: "Add" }).click();
        const composeFormPage = new ComposeFormPage(this.page, formLocator, relationFormLocator);
        await composeFormPage.toBeVisible();
        await composeFormPage.createDocument(document, relationData);
        await this.assertDocumentsInTable([document[0].value]);
    }

    getRow(document: string): Locator {
        const table = this.page.getByTestId(TestID.TABLE);
        const row = table.getByTestId(TestID.TABLE_BODY_ROW).filter({ hasText: document });
        return row;
    }

    async deleteRow(row: Locator, hasConfirmation: boolean = true) {
        await expect(row).toBeVisible();
        await row.getByRole("button").filter({ hasText: "delete" }).click();
        if (hasConfirmation) {
            await this.page.getByRole("button", { name: "Delete" }).click();
        }
        await waitForApiReponse({ page: this.page, apiPath: API_PATH.RPC, expectedStatusCode: 200 });
        await waitForApiReponse({ page: this.page, apiPath: API_PATH.RPC, expectedStatusCode: 200 });
    }

    async deleteAllRows(hasConfirmation: boolean = true) {
        let rowCount = await this.page.getByTestId(TestID.TABLE_BODY_ROW).count();

        while (rowCount > 0) {
            const firstRow = this.page.getByTestId(TestID.TABLE_BODY_ROW).first();
            await this.deleteRow(firstRow, hasConfirmation);
            await this.finishedLoading();
            rowCount = await this.page.getByTestId(TestID.TABLE_BODY_ROW).count();
        }

        await expect(this.page.getByTestId(TestID.TABLE_BODY_ROW)).toHaveCount(0);
        await expect(this.page.getByTestId(TestID.MESSAGE)).toHaveText("No results found");
    }

    async deleteDocument(document: string, hasConfirmation: boolean = true): Promise<void> {
        const row = this.getRow(document);
        await this.deleteRow(row, hasConfirmation);
    }

    async exportDocumentsToCSV(expectedFileName: string): Promise<void> {
        const downloadedFilePromise = this.page.waitForEvent("download");
        await this.page.getByRole("button", { name: "Export" }).click();
        await this.page.getByRole("button", { name: "OK" }).click();
        await waitForApiReponse({ page: this.page, apiPath: API_PATH.RPC, expectedStatusCode: 200 });
        const downloadFile = await downloadedFilePromise;
        expect(downloadFile.suggestedFilename()).toBe(expectedFileName);
    }
}
