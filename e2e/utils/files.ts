import fs from "node:fs/promises";
import path from "node:path";

import { TestUsername, StorageState } from "../types";

const AUTH_DIR = path.resolve(".auth");

export async function ensureAuthDirExists(): Promise<void> {
    await fs.mkdir(AUTH_DIR, { recursive: true });
}

export function getUserAuthStorageStatePath(username: TestUsername): string {
    return path.join(AUTH_DIR, `${username}.json`);
}

export async function deleteAuthDir(): Promise<void> {
    await fs.rm(AUTH_DIR, { recursive: true, force: true });
}

export async function writeUserStorageState(username: TestUsername, storageState: StorageState): Promise<void> {
    await fs.writeFile(getUserAuthStorageStatePath(username), JSON.stringify(storageState, null, 2));
}

export async function getUserStorageState(username: TestUsername): Promise<StorageState> {
    const data = await fs.readFile(path.join(AUTH_DIR, `${username}.json`), "utf-8");
    return JSON.parse(data);
}
