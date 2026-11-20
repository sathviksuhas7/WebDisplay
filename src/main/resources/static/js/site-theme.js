/*
 * Site-wide Light/Dark theme (chosen by the Admin, stored in the database).
 *
 * Loaded synchronously in <head> so the saved theme is applied before the
 * page paints (no white flash). The last known theme is cached in
 * localStorage and confirmed with the server once the page loads.
 */
(function () {
    "use strict";

    var script = document.currentScript;
    var contextPath = script ? (script.getAttribute("data-context-path") || "") : "";
    var STORAGE_KEY = "webdisplay.siteTheme";
    var root = document.documentElement;

    function readCache() {
        try { return localStorage.getItem(STORAGE_KEY); } catch (e) { return null; }
    }

    function writeCache(theme) {
        try { localStorage.setItem(STORAGE_KEY, theme); } catch (e) { /* storage unavailable */ }
    }

    function normalize(theme) {
        return String(theme || "LIGHT").toUpperCase() === "DARK" ? "DARK" : "LIGHT";
    }

    function applyTheme(theme) {
        var value = normalize(theme);
        root.classList.toggle("dark-theme", value === "DARK");
        root.setAttribute("data-site-theme", value);
        root.style.colorScheme = value === "DARK" ? "dark" : "light";
        var selector = document.getElementById("siteThemeSelect");
        if (selector) selector.value = value;
        writeCache(value);
        return value;
    }

    // 1. Immediately, from cache.
    applyTheme(readCache());

    // 2. Confirm with the server (the Admin may have changed it).
    function loadTheme() {
        fetch(contextPath + "/api/theme", { credentials: "same-origin", cache: "no-store" })
            .then(function (r) { if (!r.ok) throw new Error("HTTP " + r.status); return r.json(); })
            .then(function (data) { applyTheme(data.theme); })
            .catch(function () { /* keep the cached theme */ });
    }

    function saveTheme() {
        var selector = document.getElementById("siteThemeSelect");
        var message = document.getElementById("siteThemeMessage");
        var button = document.getElementById("saveSiteThemeButton");
        var csrf = document.querySelector('meta[name="csrf-token"]');
        if (!selector || !message || !button) return;

        button.disabled = true;
        var label = button.textContent;
        button.textContent = "Saving...";
        message.className = "message";
        message.textContent = "";
        message.style.display = "";

        fetch(contextPath + "/api/admin/theme", {
            method: "POST",
            credentials: "same-origin",
            headers: { "Content-Type": "application/json", "X-CSRF-TOKEN": csrf ? csrf.content : "" },
            body: JSON.stringify({ theme: selector.value })
        })
            .then(function (r) {
                return r.json().catch(function () { return {}; }).then(function (data) {
                    if (!r.ok) throw new Error(data.message || "Unable to save the theme.");
                    return data;
                });
            })
            .then(function (data) {
                applyTheme(data.theme);
                message.textContent = data.message || "Site theme updated successfully.";
                message.className = "message success";
                message.style.display = "block";
            })
            .catch(function (error) {
                message.textContent = error.message;
                message.className = "message error";
                message.style.display = "block";
            })
            .finally(function () {
                button.disabled = false;
                button.textContent = label;
            });
    }

    // Live preview while choosing (not saved until "Save Theme").
    document.addEventListener("change", function (event) {
        if (event.target && event.target.id === "siteThemeSelect") {
            var value = normalize(event.target.value);
            root.classList.toggle("dark-theme", value === "DARK");
        }
    });

    function init() {
        loadTheme();
        var button = document.getElementById("saveSiteThemeButton");
        if (button) button.addEventListener("click", saveTheme);
    }

    if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", init);
    else init();

    window.WebDisplayTheme = { apply: applyTheme };
})();
