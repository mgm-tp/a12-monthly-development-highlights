import React, { ReactElement } from "react";

import { ReadonlyObjectMap } from "@com.mgmtp.a12.formengine/formengine-core/lib/models";
import { View } from "@com.mgmtp.a12.client/client-core/lib/core/view";
import IExternalEnumerationProvider from "@com.mgmtp.a12.formengine/formengine-core/lib/back-end/services/external-enumeration-provider";
import { FormEngineViews } from "@com.mgmtp.a12.formengine/formengine-core/lib/client-extensions";
import { ConnectorLocator, RestServerConnector } from "@com.mgmtp.a12.utils/utils-connector/lib/main";

type ExternalEnumerationMap = ReadonlyObjectMap<{ [key: string]: string | undefined }>;

interface Wish {
    de: string;
    en: string;
}

async function loadWishes(): Promise<Wish[]> {
    const connector = ConnectorLocator.getInstance().getServerConnector() as RestServerConnector;
    return connector
        .fetchData({
            relativeUrl: "/christmas-wishes.json",
            method: "GET"
        })
        .then((response) => response.json());
}

function mapWishesToExternalEnum(wishes: Wish[]): ExternalEnumerationMap {
    return wishes.reduce(
        (result, wish, id) => ({
            ...result,
            [id]: { en: wish.en, de: wish.de }
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
