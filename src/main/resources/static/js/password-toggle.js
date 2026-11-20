/*
 * Adds an eye button inside every password field so users can check what
 * they typed. The field returns to hidden automatically when the form is
 * submitted.
 */
(function () {
    "use strict";

    var EYE = '<svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true" focusable="false">' +
        '<path fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" ' +
        'd="M1 12s4-7 11-7 11 7 11 7-4 7-11 7S1 12 1 12z"/><circle cx="12" cy="12" r="3" fill="none" ' +
        'stroke="currentColor" stroke-width="2"/></svg>';
    var EYE_OFF = '<svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true" focusable="false">' +
        '<path fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" ' +
        'd="M17.94 17.94A10.07 10.07 0 0 1 12 19c-7 0-11-7-11-7a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 ' +
        '12 4c7 0 11 7 11 7a18.5 18.5 0 0 1-2.16 3.19M14.12 14.12a3 3 0 1 1-4.24-4.24M1 1l22 22"/></svg>';

    function enhance(input) {
        if (input.dataset.pwToggle === "done") return;
        input.dataset.pwToggle = "done";

        var wrap = document.createElement("span");
        wrap.className = "pw-field";
        // Move the field's outer spacing to the wrapper so the eye stays centred.
        var cs = window.getComputedStyle(input);
        wrap.style.margin = cs.marginTop + " " + cs.marginRight + " " + cs.marginBottom + " " + cs.marginLeft;
        input.style.margin = "0";
        input.parentNode.insertBefore(wrap, input);
        wrap.appendChild(input);

        var button = document.createElement("button");
        button.type = "button";
        button.className = "pw-toggle";
        button.setAttribute("aria-label", "Show password");
        button.setAttribute("aria-pressed", "false");
        button.title = "Show password";
        button.innerHTML = EYE;
        wrap.appendChild(button);

        button.addEventListener("click", function () {
            var reveal = input.type === "password";
            input.type = reveal ? "text" : "password";
            button.innerHTML = reveal ? EYE_OFF : EYE;
            button.setAttribute("aria-label", reveal ? "Hide password" : "Show password");
            button.setAttribute("aria-pressed", String(reveal));
            button.title = reveal ? "Hide password" : "Show password";
            var end = input.value.length;
            input.focus();
            try { input.setSelectionRange(end, end); } catch (e) { /* ignore */ }
        });

        if (input.form) {
            input.form.addEventListener("submit", function () {
                input.type = "password";
                button.innerHTML = EYE;
                button.setAttribute("aria-pressed", "false");
            });
        }
    }

    function init() {
        Array.prototype.forEach.call(document.querySelectorAll('input[type="password"]'), enhance);
    }

    if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", init);
    else init();
})();
