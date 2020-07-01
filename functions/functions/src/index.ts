import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

admin.initializeApp();

function getSuccessURL(domain: string, code: string, idToken: string, state: string, name: string, email: string): string {
    var url = domain
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
    var domain: string = request.query["domain"] as string;
    if (domain === undefined) {
        response.status(400).send("Missing Domain!");
        return;
    }

    if (request.method !== "POST") {
        response.redirect(getFailureURL(domain, "methodNotAllowed"))
        return;
    }

    var code = request.body["code"];
    var idToken = request.body["id_token"];
    var state = request.body["state"];

    if (code === undefined || idToken === undefined || state === undefined) {
        response.redirect(getFailureURL(domain, "badRequest"))
        return;
    }

    var userString = request.body["user"];
    var fullName: any = undefined;
    var email: any = undefined;
    if (userString !== undefined) {
        try {
            var userObject = JSON.parse(userString);
            var nameObject = userObject["name"];
            fullName = nameObject["firstName"] + " " + nameObject["lastName"];
            email = userObject["email"];
        } catch { }
    }

    response.redirect(
        getSuccessURL(domain, code, idToken, state, fullName, email)
    );
});
