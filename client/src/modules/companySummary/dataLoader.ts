/*
 * SPDX-License-Identifier: EUPL-1.2 OR LicenseRef-commercial
 *
 * Copyright (c) 2012-2026 mgm technology partners GmbH
 *
 * Dual License
 * ------------
 * This source file is part of the mgm A12 Platform and available under
 * a choice of two different licenses:
 *
 * 1. Open-Source License - EUPL v1.2
 *    You may redistribute and/or modify this file under the terms of the
 *    European Union Public License, version 1.2 - see https://eupl.eu/.
 *
 * 2. Commercial License
 *    Alternatively, you may obtain a commercial license from
 *    mgm technology partners GmbH, that permits use of this software
 *    under different terms (including support and maintenance services).
 *
 *    Please contact a12-license@mgm-tp.com for more information.
 *
 * You must select and comply with exactly one of the above license options.
 *
 * Warranty Disclaimer (applies to either option)
 * ----------------------------------------------
 * THIS SOFTWARE IS PROVIDED "AS IS" AND WITHOUT WARRANTY OF ANY KIND,
 * WHETHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT, EXCEPT WHERE SUCH DISCLAIMERS ARE HELD TO BE
 * LEGALLY INVALID. SEE THE RESPECTIVE LICENSE TEXT FOR DETAILS.
 */

import { Activity, DataLoader } from "@com.mgmtp.a12.client/client-core";
import { FormActivity } from "@com.mgmtp.a12.formengine/formengine-core";
import { JsonRpc2Request, JsonRpc2Response } from "@com.mgmtp.a12.dataservices/dataservices-access";

import { makeRpcRequest } from "../../utils/request";
import { JSON_RPC_V2 } from "../../utils/constants";

import {
    COMPANY_SUMMARY_DOCUMENT_MODEL,
    COMPANY_SUMMARY_MODULE,
    GET_COMPANY_SUMMARY_ID,
    GET_COMPANY_SUMMARY_METHOD
} from "./utils/constants";
import type { CompanySummaryDocument } from "./utils/types";

type Data = FormActivity.Data.SingleDocumentData;

export class CompanySummaryDocumentDataLoader implements DataLoader {
    readonly name: string = "CompanySummaryDocumentDataLoader";
    private operationCounter = 0;

    canHandle(activityDescriptor: Activity.Descriptor): boolean {
        return activityDescriptor.module === COMPANY_SUMMARY_MODULE && activityDescriptor.instance !== undefined;
    }

    async load(activity: Activity): Promise<Data> {
        const instance = activity.descriptor.instance;
        if (!instance) {
            throw new Error("No instance to load.");
        }

        const requestId = `${GET_COMPANY_SUMMARY_ID}-${this.operationCounter++}`;
        const res = await getCompanySummaryByDocumentId(instance, requestId);
        const document = { id: instance, modelId: COMPANY_SUMMARY_DOCUMENT_MODEL, ...res };
        return {
            document
        };
    }

    save(): Promise<Data> {
        throw new Error("Method not implemented.");
    }

    delete(): Promise<void> {
        throw new Error("Method not implemented.");
    }
}

export async function getCompanySummaryByDocumentId(
    documentId: string,
    requestId: string
): Promise<CompanySummaryDocument> {
    const request: JsonRpc2Request = {
        jsonrpc: JSON_RPC_V2,
        method: GET_COMPANY_SUMMARY_METHOD,
        id: requestId,
        params: {
            documentId
        }
    };

    const documentResponse = await makeRpcRequest([request]);
    if (JsonRpc2Response.hasErrors(documentResponse)) {
        throw new Error("Failed to fetch company summary document!");
    }

    const [data] = documentResponse;
    return data?.result;
}
