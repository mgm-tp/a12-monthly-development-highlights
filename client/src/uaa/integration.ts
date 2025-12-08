import {
    AuthenticationState,
    UaaClient,
    UaaClientConfiguration,
    UaaSelectors
} from "@com.mgmtp.a12.uaa/uaa-authentication-client";
import { LoggerFactory } from "@com.mgmtp.a12.utils/utils-logging";

const logger = LoggerFactory.getLogger("uaa/integration");

export function isRedirectFromKeyCloak() {
    const appURL = new URL(window.location.href);
    return appURL.searchParams.has("state");
}

/**
 * Initializes the {@link UaaClient} which handles the login from Keycloak and propagates it to UAA.
 *
 * Initially called when starting the application.
 */
export async function uaaIntegration(clientConfiguration: UaaClientConfiguration) {
    await UaaClient.init(clientConfiguration);

    const appURL = new URL(window.location.href);
    const uaaOidcClient = UaaClient.getOidcClient();

    if (isRedirectFromKeyCloak()) {
        try {
            logger.info("UAA process for callback.");
            uaaOidcClient.initConnector();
            await uaaOidcClient.processLoginCallback();
        } catch {
            // Restart the login process
            uaaOidcClient.login();
        } finally {
            // Remove keycloak params from the url
            const baseUrl = `${appURL.origin}${appURL.pathname}`;
            window.history.pushState("name", "", baseUrl);
        }
    } else {
        logger.info("Start trigger UAA process for login.");
        const authenticatedState = UaaSelectors.state(clientConfiguration.store?.getState());
        const isNotAuthenticated = authenticatedState === AuthenticationState.NOT_AUTHENTICATED;
        if (isNotAuthenticated) {
            uaaOidcClient.login();
        }
    }
}
