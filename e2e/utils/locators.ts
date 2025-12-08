import { Locator } from "@playwright/test";

import { Page } from "../fixtures";

export function getByLabelWithOptionalAsterisk(scope: Page | Locator, label: string): Locator {
    return scope.getByLabel(new RegExp(`^${label}\\s*\\*?$`));
}
