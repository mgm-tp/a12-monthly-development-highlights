/*
 * (c) copyright 2012-2025 mgm technology partners GmbH.
 * This software, the underlying source code and other artifacts are protected by copyright.
 * All rights, in particular the right to use, reproduce, publish and edit are reserved.
 * A simple right of use (license) can be acquired for use, duplication, publication, editing etc.
 * Requests for this can be made at A12-license@mgm-tp.com or other official channels of the copyright holder.
 */

import type { Action } from "typescript-fsa";

import { Activity, ActivityActions, ActivitySelectors } from "@com.mgmtp.a12.client/client-core/lib/core/activity";
import { DeepLinkingFactories } from "@com.mgmtp.a12.client/client-core/lib/extensions/deep-linking";

import { store } from "../index";

type EncodePayload = {
    initialActivityId: string | undefined;
    descriptor: Activity.Descriptor;
    slices?: Activity.DataHolder["slices"];
};

function processActionsFromDescriptor(activityDescriptor: Activity.Descriptor): Action<ActivityActions.PushPayload>[] {
    const { priorScene, slices, ...adjustedDescriptor } = activityDescriptor;
    const actions: Action<ActivityActions.PushPayload>[] = [];
    let initialAction;
    if (priorScene) {
        const initialActivityDescriptor = JSON.parse(atob(priorScene));
        const { slices, ...restDescriptor } = initialActivityDescriptor;
        initialAction = ActivityActions.create({
            activityDescriptor: restDescriptor,
            slices: slices ? JSON.parse(atob(slices)) : undefined
        });
        actions.push(initialAction);
    }
    const mainAction = ActivityActions.create({
        activityDescriptor: adjustedDescriptor,
        initiatingActivityId: initialAction ? initialAction.payload.activity.id : undefined,
        slices: slices ? JSON.parse(atob(slices)) : undefined
    });
    actions.push(mainAction);
    return actions;
}
/**
 * @internal
 *
 * Deep link coder that encodes only activity descriptor in the deep link.
 */
export class CustomDeepLinkCoder implements DeepLinkingFactories.DeepLinkCoder {
    encode(activity: Activity): string {
        return [activity]
            .map<EncodePayload>((a) => {
                const slices = Activity.findDefaultDataHolder(a)?.slices;
                return {
                    initialActivityId: a.initiatingActivityId,
                    descriptor: a.descriptor,
                    slices
                };
            })
            .map(encode)
            .join("/");
    }

    decode(deepLink: string): Action<ActivityActions.PushPayload>[] {
        return deepLink
            .split("/")
            .filter((part) => part)
            .map(decodeActivityLocation)
            .reduce((actions: Action<ActivityActions.PushPayload>[], activityDescriptor) => {
                const actionsFromDescriptor = processActionsFromDescriptor(activityDescriptor);
                return actions.concat(actionsFromDescriptor);
            }, []);
    }
}

function encode({ initialActivityId, descriptor, slices }: EncodePayload): string {
    // need to access properties and their values in a generic way
    let encodedDescriptor = Object.keys(descriptor)
        .filter((propName) => descriptor[propName] !== undefined)
        .map((propName) => `${encodeURIComponent(propName)}:${encodeURIComponent(descriptor[propName] ?? "")}`)
        .join(",");
    if (slices && Object.keys(slices).length > 0) {
        const encodedSlices = btoa(JSON.stringify(slices));
        encodedDescriptor += `,slices:${encodeURIComponent(encodedSlices)}`;
    }
    if (initialActivityId) {
        const state = store.getState();
        const initialActivity = ActivitySelectors.activityById(initialActivityId)(state);
        if (initialActivity) {
            const initialSlices = Activity.findDefaultDataHolder(initialActivity)?.slices;
            let priorScene;
            if (initialSlices && Object.keys(initialSlices).length > 0) {
                const encodedSlices = btoa(JSON.stringify(initialSlices));
                priorScene = btoa(JSON.stringify({ ...initialActivity.descriptor, slices: encodedSlices }));
            } else {
                priorScene = btoa(JSON.stringify(initialActivity.descriptor));
            }
            encodedDescriptor += `,priorScene:${encodeURIComponent(priorScene)}`;
        }
    }
    return encodedDescriptor;
}

const activityLinkPattern = /([^:,]*):([^,]*)/g;
function decodeActivityLocation(activityLink: string): Activity.Descriptor {
    return JSON.parse(`{${decodeURIComponent(activityLink.replace(activityLinkPattern, '"$1":"$2"'))}}`);
}
