import { test } from "@playwright/test";

import { deleteAuthDir } from "../../utils/files";

test("Clean auth setup", async () => {
    await deleteAuthDir();
});
