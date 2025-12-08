import { Page, Response } from "@playwright/test";

export enum API_PATH {
    RPC = "/v2/rpc",
    ATTACHMENT = "/v2/attachment",
    SYNC = "/v2/sync",
    REMINDERS = "/reminders"
}

export type HttpMethod = "GET" | "POST" | "PUT" | "DELETE" | "PATCH";

function apiUrlFromPage(page: Page, apiPath: string) {
    const { origin } = new URL(page.url());
    return `${origin}/api${apiPath.startsWith("/") ? "" : "/"}${apiPath}`;
}

export async function waitForApiReponse({
    page,
    apiPath,
    expectedStatusCode,
    method = "POST"
}: {
    page: Page;
    apiPath: API_PATH;
    expectedStatusCode: number;
    method?: HttpMethod;
}): Promise<Response> {
    const urlPrefix = apiUrlFromPage(page, apiPath);
    const response = await page.waitForResponse(
        (resp) =>
            resp.request().url().startsWith(urlPrefix) &&
            resp.request().method() === method &&
            resp.status() === expectedStatusCode
    );

    return response;
}
