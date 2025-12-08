import type { OidcStandardClaims } from "oidc-client-ts";

import { StoreFactories } from "@com.mgmtp.a12.client/client-core/lib/core/store";
import { UaaActions, UaaExtendedUser } from "@com.mgmtp.a12.uaa/uaa-authentication-client/lib";
import { LocaleActions } from "@com.mgmtp.a12.client/client-core/lib/core/locale";
import { Locale } from "@com.mgmtp.a12.utils/utils-localization/lib/main";

import { supportedLocales } from "../localization";
import { isRedirectFromKeyCloak } from "../uaa/integration";

interface UaaExtendedOauth2User extends UaaExtendedUser {
    profile: OidcStandardClaims;
}

/**
 * If Keycloak internationalization is being configured, this takes care of the retrieval of the used locale selected on the login screen.
 * Sets the locale in the application, english is taken as default if no locale is specified by Keycloak.
 *
 */
export const setLocaleKeycloakOnLoginMiddleware = StoreFactories.createMiddleware((api, next, action) => {
    if (UaaActions.loggedIn.match(action) && isRedirectFromKeyCloak()) {
        const user = action.payload.user as UaaExtendedOauth2User;
        const defaultLocale = getDefaultLocale(user) ?? supportedLocales[0];
        next(LocaleActions.set(defaultLocale));
    }
    return next(action);
});
const getDefaultLocale = (user: UaaExtendedOauth2User) => {
    const locale = user.profile.locale;
    return supportedLocales.find((item) => item.language === locale) as Locale;
};
