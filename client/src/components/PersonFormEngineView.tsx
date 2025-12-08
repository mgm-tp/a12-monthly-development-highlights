import React, { ReactElement } from "react";

import { ReadonlyObjectMap } from "@com.mgmtp.a12.formengine/formengine-core/lib/models";
import { View } from "@com.mgmtp.a12.client/client-core/lib/core/view";
import IExternalEnumerationProvider from "@com.mgmtp.a12.formengine/formengine-core/lib/back-end/services/external-enumeration-provider";
import { FormEngineViews } from "@com.mgmtp.a12.formengine/formengine-core/lib/client-extensions";
import {
    Dispatcher,
    QueryJsonRpc2Request,
    QueryJsonRpc2Response
} from "@com.mgmtp.a12.dataservices/dataservices-access";

type ExternalEnumerationMap = ReadonlyObjectMap<{ [key: string]: string | undefined }>;

interface WishDocument {
    Christmas: {
        Wish: {
            de: string;
            en: string;
        };
    };
}

async function loadWishes(): Promise<WishDocument[]> {
    const request: QueryJsonRpc2Request = {
        method: "QUERY",
        jsonrpc: "2.0",
        id: "loadWishes",
        params: {
            query: {
                targetDocumentModel: "ChristmasWish_DM",
                projectionName: "document",
                paging: {
                    pageNumber: 0,
                    pageSize: 100
                }
            }
        }
    };

    const [response] = await Dispatcher.rpc("en", [request]);

    return response && QueryJsonRpc2Response.isInstance(response)
        ? ((response.result.entries?.map((entry) => entry?.document) ?? []) as WishDocument[])
        : [];
}

function mapWishesToExternalEnum(wishes: WishDocument[]): ExternalEnumerationMap {
    return wishes.reduce(
        (result, wish, id) => ({
            ...result,
            [id]: wish.Christmas.Wish
        }),
        {}
    );
}

export default function PersonFormEngineView(props: View): ReactElement {
    const [wishes, setWishes] = React.useState<ExternalEnumerationMap>({});

    React.useEffect(() => {
        let ignore = false;
        loadWishes().then((wishes) => {
            if (!ignore) {
                setWishes(mapWishesToExternalEnum(wishes));
            }
        });
        return () => {
            ignore = true;
        };
    }, []);

    const externalEnumerationProvider: IExternalEnumerationProvider = (source: string): ExternalEnumerationMap => {
        switch (source) {
            case "christmas-wishes": {
                return wishes;
            }
            default:
                throw new Error("unknown external enumeration source: " + source);
        }
    };

    return <FormEngineViews.FormEngine {...props} externalEnumerationProvider={externalEnumerationProvider} />;
}
