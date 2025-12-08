import USERS from "../fixtures/users.json";

export interface FileTestData {
    locator: string;
    filePath: string;
    value: string;
    type: DataType.File;
}
export interface FieldTestData {
    label: string;
    value: string;
    type: Exclude<DataType, DataType.File>;
}

export type TestData = FieldTestData | FileTestData;

export enum DataType {
    String,
    Select,
    Check,
    File
}

export type TestUsername = keyof typeof USERS;
export const USERNAMES = Object.keys(USERS) as TestUsername[];

export type StorageState =
    | string
    | {
        cookies: Array<{
            name: string;
            value: string;
            domain: string;
            path: string;
            expires: number;
            httpOnly: boolean;
            secure: boolean;
            sameSite: "Strict" | "Lax" | "None";
        }>;
        origins: Array<{
            origin: string;
            localStorage: Array<{
                name: string;
                value: string;
            }>;
        }>;
    };
