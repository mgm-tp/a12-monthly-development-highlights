import { CurrentActivityLocationManager } from "@com.mgmtp.a12.client/client-core/lib/extensions/deep-linking";
import { ApplicationModel, ModelSelectors } from "@com.mgmtp.a12.client/client-core/lib/core/model";
import { Activity } from "@com.mgmtp.a12.client/client-core/lib/core/activity";

import { store } from "../index";

import { decodeActivityLocation, SLICES_STRING } from "./customDeepLinkCoder";

export const customLocationManager: CurrentActivityLocationManager = {
    getLocation(): string {
        return location.toString();
    },
    getDeepLink(): string {
        const hash = location.hash.replace(/^#/, "");
        const paths = `${location.pathname}${location.hash}`.split("/").filter((part) => part.length > 0);
        if (paths.length === 0) {
            return hash;
        }

        const locationMap = locationMapFromAM();
        const descriptors = paths.map((path) => {
            const elements = path.split("#");
            const entry = Array.from(MODULE_MAP.entries()).find(([, val]) => val === elements[0]);
            const sceneName = entry ? entry[0] : elements[0];
            const matchedConditions = locationMap.get(sceneName) || [];
            let descriptor = matchConditionsToDescriptor(sceneName, matchedConditions);

            if (elements.length > 1) {
                descriptor = { ...descriptor, ...decodeActivityLocation(elements[1]) };
            }
            return descriptor;
        });
        const mainDescriptor = descriptors.pop() || {};
        let priorScene = undefined;
        while (descriptors.length > 0) {
            const descriptor = descriptors.shift();
            priorScene = descriptor ? btoa(JSON.stringify({ ...descriptor, priorScene })) : undefined;
        }
        const { id, ...rest } = mainDescriptor;
        const combinedDescriptor = { ...rest, instance: id, priorScene };

        return encode(combinedDescriptor);
    },
    setDeepLink(deepLink: string): void {
        const nextHash = deepLink.replace(/^#/, "");
        const descriptor = decodeActivityLocation(nextHash);
        const { priorScene, slices, ...plainDescriptor } = descriptor;
        const locationMap = locationMapFromAM();
        let path = "";
        if (priorScene) {
            const priorSceneDescriptor: Activity.Descriptor = JSON.parse(atob(priorScene));
            const initialSlices = priorSceneDescriptor.slices;
            const adjustedDescriptor = { ...priorSceneDescriptor };
            delete adjustedDescriptor["slices"];
            path += createPathFromDescriptor(locationMap, adjustedDescriptor, initialSlices) + "/";
        }

        path += createPathFromDescriptor(locationMap, plainDescriptor, slices);
        history.replaceState(undefined, "", `/${path}`);
    }
};

const matchConditionsToDescriptor = (
    sceneName: string,
    conditions: ApplicationModel.MatchCondition[]
): Activity.Descriptor => {
    let descriptor: Activity.Descriptor = {};
    conditions.forEach((condition) => {
        if (ApplicationModel.MatchCondition.Equality.isInstance(condition)) {
            descriptor = { ...descriptor, [condition.key]: condition.mustEqual };
        }
    });
    const state = store?.getState();
    const appModel = ModelSelectors.applicationModel()(state);
    let scene: ApplicationModel.Scene | undefined;
    appModel.content.modules.forEach((module) => {
        module.flows.forEach((flow) => {
            scene = flow.scenes.find((scene) => scene.name === sceneName);
        });
    });
    if (scene) {
        const onEnter = scene.sceneChange?.onEnter ?? [];
        if (onEnter.length > 0) {
            onEnter
                .find((directive) => directive.type === "VIEW_ADD")
                ?.models?.forEach((model) => {
                    descriptor = { ...descriptor, model: model.documentModel };
                });
        }
    }
    return descriptor;
};

function encode(descriptor: Activity.Descriptor): string {
    return Object.keys(descriptor)
        .filter((propName) => descriptor[propName] !== undefined)
        .map((propName) => `${encodeURIComponent(propName)}:${encodeURIComponent(descriptor[propName] ?? "")}`)
        .join(",");
}

const locationMapFromAM = (): Map<string, ApplicationModel.MatchCondition[]> => {
    const state = store?.getState();
    const appModel = ModelSelectors.applicationModel()(state);

    const locationMap = new Map<string, ApplicationModel.MatchCondition[]>();
    appModel.content.modules.forEach((module) => {
        module.flows.forEach((flow) => {
            flow.scenes.forEach((scene) => {
                const matchConditions = [...scene.matchConditions];
                locationMap.set(scene.name, matchConditions);
            });
        });
    });
    return locationMap;
};

const findMatchedEntry = (
    locationMap: Map<string, ApplicationModel.MatchCondition[]>,
    descriptor: Activity.Descriptor
): [string, ApplicationModel.MatchCondition[]] | undefined => {
    return Array.from(locationMap.entries()).find(([, val]) =>
        val.every((condition) => ApplicationModel.MatchCondition.evaluate(condition, descriptor))
    );
};

const createPathFromDescriptor = (
    locationMap: Map<string, ApplicationModel.MatchCondition[]>,
    descriptor: Activity.Descriptor,
    slices?: string
): string => {
    const entry = findMatchedEntry(locationMap, descriptor);
    if (entry) {
        const [sceneName] = entry;
        let path = MODULE_MAP.get(sceneName) || sceneName;
        if (descriptor.instance) {
            path += `#id:${encodeURIComponent(descriptor.instance)}`;
        }
        if (slices) {
            path += `${descriptor.instance ? "," : "#"}${SLICES_STRING}:${slices}`;
        }
        return path;
    }
    return "";
};

const MODULE_MAP = new Map<string, string>([
    ["PersonOverview", "person"],
    ["PersonForm", "personDetail"]
]);
