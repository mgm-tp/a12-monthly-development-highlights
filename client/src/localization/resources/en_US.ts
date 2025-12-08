import { LocalizationKeyTreeType } from "../keys";

export const en_US: LocalizationKeyTreeType = {
    application: {
        title: "Your-Project-Name",
        header: {
            userinfo: {
                labels: {
                    loggedInAs: "Logged in as",
                    logoutButton: "Logout"
                }
            }
        }
    },
    keycloak: {
        processing: {
            message: "You are being redirected or authenticated. Please be patient, this might take a moment."
        },
        error: {
            message: "Keycloak is currently unavailable, please try again later."
        }
    },
    error: {
        security: {
            notAuthorized: {
                description: "You are not allowed to perform the requested operation."
            }
        },
        attachment: {
            invalidType: "Invalid MIME type."
        },
        "content-store": {
            content: {
                invalidSize: "The attachment content exceeds the maximum permitted size."
            }
        }
    }
};
