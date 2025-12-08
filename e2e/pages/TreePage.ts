import { expect, Locator } from "@playwright/test";

import { Attribute, TestID } from "../types/testIds";

import { OverviewPage } from "./OverviewPage";

export class TreePage extends OverviewPage {
    getTreeNodesAtLevel(level: number = 0): Locator {
        return this.page.locator(`[${Attribute.DATA_TREE_LEVEL}="${level}"]`).getByTestId(TestID.TABLE_BODY_ROW);
    }

    async assertExpandableRowTitles(documents: string[]) {
        for (const content of documents) {
            await expect(
                this.page
                    .getByTestId(TestID.TABLE_BODY_ROW)
                    .filter({ has: this.page.getByTestId(TestID.TREE_NODE_EXPANDER) })
                    .getByTestId(TestID.TREE_NODE_NAME)
                    .filter({ hasText: content })
            ).toBeVisible();
        }
        return;
    }

    async assertNotExpandableRowTitles(documents: string[]) {
        for (const content of documents) {
            await expect(
                this.page
                    .getByTestId(TestID.TABLE_BODY_ROW)
                    .filter({ hasNot: this.page.getByTestId(TestID.TREE_NODE_EXPANDER) })
                    .getByTestId(TestID.TREE_NODE_NAME)
                    .filter({ hasText: content })
            ).toBeVisible();
        }
    }

    async expandAll() {
        await this.page
            .getByTestId(TestID.CONTENTBOX_GROUP_ACTION_BAR)
            .getByTestId(TestID.POPUP_TRIGGER_ELEMENT)
            .click();
        await expect(this.page.getByTestId(TestID.POPUP_MENU)).toBeVisible();
        await this.page
            .getByTestId(TestID.POPUP_MENU)
            .getByTestId(TestID.LIST_ITEM)
            .filter({ hasText: "Expand All" })
            .click();
        await this.finishedLoading();
    }

    async collapseAll() {
        await this.page
            .getByTestId(TestID.CONTENTBOX_GROUP_ACTION_BAR)
            .getByTestId(TestID.POPUP_TRIGGER_ELEMENT)
            .click();
        await this.page
            .getByTestId(TestID.POPUP_MENU)
            .getByTestId(TestID.LIST_ITEM)
            .filter({ hasText: "Collapse All" })
            .click();
    }

    async shouldBeExpanded(row: Locator) {
        await expect(row.getByTestId(TestID.TREE_NODE_EXPANDER).getByLabel("Collapse subitems")).toBeVisible();
    }

    async shouldNotBeExpanded(row: Locator) {
        await expect(row.getByTestId(TestID.TREE_NODE_EXPANDER).getByLabel("Expand subitems")).toBeVisible();
    }

    async expandNode(row: Locator) {
        await row.getByRole("button", { name: "Expand subitems" }).click();
    }

    async collapseNode(row: Locator) {
        await row.getByRole("button", { name: "Collapse subitems" }).click();
    }

    async dragAndDrop(source: Locator, target: Locator) {
        await source.dragTo(target);
    }
}
