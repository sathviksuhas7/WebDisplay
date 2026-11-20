(function () {
    "use strict";

    const script = document.currentScript;
    const contextPath = script && script.dataset.contextPath ? script.dataset.contextPath : "";
    const loginUrl = contextPath + "/login?expired=true";
    const heartbeatUrl = contextPath + "/api/session/heartbeat";
    let checking = false;
    let redirecting = false;

    async function verifySession() {
        if (checking || redirecting || document.visibilityState === "hidden") return;
        checking = true;
        try {
            const response = await fetch(heartbeatUrl, {
                method: "GET",
                credentials: "same-origin",
                cache: "no-store",
                headers: { "Accept": "application/json", "Cache-Control": "no-cache" }
            });
            if (response.status === 401 || response.status === 403) {
                redirecting = true;
                window.location.replace(loginUrl);
                return;
            }
            if (!response.ok) return;
            const body = await response.json();
            if (body.message !== "SESSION_ACTIVE") {
                redirecting = true;
                window.location.replace(loginUrl);
            }
        } catch (error) {
            // A temporary network failure must not log out a user. The next
            // visibility change or interval will retry the check.
        } finally {
            checking = false;
        }
    }

    window.addEventListener("pageshow", verifySession);
    document.addEventListener("visibilitychange", function () {
        if (document.visibilityState === "visible") verifySession();
    });
    window.setInterval(verifySession, 60000);
    verifySession();
})();
