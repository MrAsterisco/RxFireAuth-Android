import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

admin.initializeApp();

function getSuccessURL(domain: string, code: string, idToken: string, state: string, name: string, email: string): string {
    let url = domain
        + "code=" + code 
        + "&idToken=" + idToken 
        + "&state=" + state;

    if (name !== undefined) {
        url += "&name=" + name;
    }

    if (email !== undefined) {
        url += "&email=" + email;
    }

    return url;
}

function getFailureURL(domain: string, reason: string): string {
    return domain + "&error=" + reason;
}

exports.handleAppleSignIn = functions.https.onRequest((request, response) => {
    const domain: string = request.query["domain"] as string;
    if (domain === undefined) {
        response.status(400).send("Missing Domain!");
        return;
    }

    if (request.method !== "POST") {
        response.redirect(getFailureURL(domain, "methodNotAllowed"))
        return;
    }

    const code = request.body["code"];
    const idToken = request.body["id_token"];
    const state = request.body["state"];

    if (code === undefined || idToken === undefined || state === undefined) {
        response.redirect(getFailureURL(domain, "badRequest"))
        return;
    }

    const userString = request.body["user"];
    let fullName: any = undefined;
    let email: any = undefined;
    if (userString !== undefined) {
        try {
            let userObject = JSON.parse(userString);
            let nameObject = userObject["name"];
            fullName = nameObject["firstName"] + " " + nameObject["lastName"];
            email = userObject["email"];
        } catch { }
    }

    response.redirect(
        getSuccessURL(domain, code, idToken, state, fullName, email)
    );
});
