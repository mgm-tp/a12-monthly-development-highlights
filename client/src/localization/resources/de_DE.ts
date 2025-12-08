import { LocalizationKeyTreeType } from "../keys";

export const de_DE: LocalizationKeyTreeType = {
    application: {
        title: "Your-Project-Name",
        header: {
            userinfo: {
                labels: {
                    loggedInAs: "Angemeldet als",
                    logoutButton: "Ausloggen"
                }
            }
        }
    },
    keycloak: {
        processing: {
            message:
                "Sie werden gerade weitergeleitet oder authentifiziert. Bitte haben Sie kurz Geduld, dies kann einen Moment dauern."
        },
        error: {
            message: "Keycloak ist aktuell nicht erreichbar, bitte versuchen Sie es später erneut."
        }
    },
    error: {
        security: {
            notAuthorized: {
                description: "Sie haben keine Berechtigung diese Operation durchzuführen."
            }
        },
        attachment: {
            invalidType: "Ungültiger MIME-Typ."
        },
        "content-store": {
            content: {
                invalidSize: "Der Attachment-Inhalt überschreitet die zulässige Maximalgröße."
            }
        }
    }
};
