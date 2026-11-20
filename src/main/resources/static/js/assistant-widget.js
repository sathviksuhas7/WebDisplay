/*
 * WebDisplay customer chat assistant (floating chat window).
 *
 * - Launcher button in the bottom-right corner, opens a chat window.
 * - Greets the customer and offers quick-reply chips.
 * - Answers come from /api/customer/assistant/chat (customer identity is
 *   taken from the server session, never from this page).
 * - Answers can include buttons that open the right dashboard tab and field.
 * - Conversation is kept for this browser tab until logout.
 */
(function () {
    "use strict";

    var script = document.currentScript;
    var contextPath = script ? (script.getAttribute("data-context-path") || "") : "";
    var API = contextPath + "/api/customer/assistant";
    var STORE = "webdisplay.assistant.history";
    var OPEN_STORE = "webdisplay.assistant.open";

    var ICON_CHAT = '<svg viewBox="0 0 24 24" width="26" height="26" aria-hidden="true"><path fill="currentColor" d="M12 3C6.48 3 2 6.94 2 11.8c0 2.4 1.1 4.57 2.88 6.16-.15 1.3-.7 2.52-1.62 3.47a.5.5 0 0 0 .4.85c1.98-.12 3.65-.86 4.86-1.76 1.07.31 2.24.48 3.48.48 5.52 0 10-3.94 10-8.8S17.52 3 12 3Zm-4.5 10.3a1.3 1.3 0 1 1 0-2.6 1.3 1.3 0 0 1 0 2.6Zm4.5 0a1.3 1.3 0 1 1 0-2.6 1.3 1.3 0 0 1 0 2.6Zm4.5 0a1.3 1.3 0 1 1 0-2.6 1.3 1.3 0 0 1 0 2.6Z"/></svg>';
    var ICON_CLOSE = '<svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true"><path fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" d="M6 6l12 12M18 6 6 18"/></svg>';
    var ICON_SEND = '<svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true"><path fill="currentColor" d="M3.4 20.4 21 12 3.4 3.6l-.01 6.53L15 12 3.39 13.87z"/></svg>';
    var ICON_RESET = '<svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true"><path fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" d="M3 12a9 9 0 1 0 3-6.7M3 4v5h5"/></svg>';
    var BOT_AVATAR = '<span class="wda-avatar" aria-hidden="true">WD</span>';

    function csrf() {
        var meta = document.querySelector('meta[name="csrf-token"]');
        return meta ? meta.content : "";
    }

    function el(tag, cls, html) {
        var e = document.createElement(tag);
        if (cls) e.className = cls;
        if (html != null) e.innerHTML = html;
        return e;
    }

    // ---------- Build UI ----------
    var launcher = el("button", "wda-launcher", ICON_CHAT + '<span class="wda-launcher-label">Need help?</span>');
    launcher.type = "button";
    launcher.setAttribute("aria-label", "Open chat assistant");
    launcher.setAttribute("aria-expanded", "false");
    launcher.setAttribute("aria-controls", "wdaWindow");

    var win = el("section", "wda-window");
    win.id = "wdaWindow";
    win.setAttribute("role", "dialog");
    win.setAttribute("aria-label", "WebDisplay chat assistant");
    win.innerHTML =
        '<header class="wda-header">' +
        '  <div class="wda-title">' + BOT_AVATAR +
        '    <div><strong>WebDisplay Assistant</strong><span class="wda-status"><i></i>Online · replies instantly</span></div>' +
        '  </div>' +
        '  <div class="wda-header-actions">' +
        '    <button type="button" class="wda-icon-btn" data-wda="reset" title="Start a new chat" aria-label="Start a new chat">' + ICON_RESET + '</button>' +
        '    <button type="button" class="wda-icon-btn" data-wda="close" title="Close" aria-label="Close chat">' + ICON_CLOSE + '</button>' +
        '  </div>' +
        '</header>' +
        '<div class="wda-messages" aria-live="polite" aria-relevant="additions"></div>' +
        '<div class="wda-chips" aria-label="Suggested questions"></div>' +
        '<form class="wda-compose" autocomplete="off">' +
        '  <label class="wda-sr" for="wdaInput">Type your message</label>' +
        '  <textarea id="wdaInput" rows="1" maxlength="1000" placeholder="Type your question…"></textarea>' +
        '  <button type="submit" class="wda-send" aria-label="Send message">' + ICON_SEND + '</button>' +
        '</form>' +
        '<p class="wda-footnote">🔒 Never share your password, OTP or PIN in chat.</p>';

    var messages = win.querySelector(".wda-messages");
    var chips = win.querySelector(".wda-chips");
    var form = win.querySelector(".wda-compose");
    var input = win.querySelector("#wdaInput");
    var sendBtn = win.querySelector(".wda-send");

    var history = [];
    var busy = false;

    // ---------- Rendering ----------
    function formatText(text) {
        // Escape, then turn line breaks and bullets into simple markup.
        var safe = String(text || "")
            .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;");
        return safe.split("\n").map(function (line) {
            if (/^\s*[•\-]\s+/.test(line)) return '<span class="wda-li">' + line.replace(/^\s*[•\-]\s+/, "") + "</span>";
            if (/^\s*\d+\.\s+/.test(line)) return '<span class="wda-li wda-num">' + line + "</span>";
            return line ? "<span>" + line + "</span>" : "";
        }).join("");
    }

    function timeLabel(ts) {
        try { return new Date(ts).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }); }
        catch (e) { return ""; }
    }

    function renderMessage(m) {
        var row = el("div", "wda-row " + (m.who === "user" ? "wda-user" : "wda-bot"));
        if (m.who !== "user") row.appendChild(el("span", "", BOT_AVATAR));
        var bubble = el("div", "wda-bubble" + (m.error ? " wda-error" : ""));
        bubble.innerHTML = formatText(m.text);
        if (m.actions && m.actions.length) {
            var acts = el("div", "wda-actions");
            m.actions.forEach(function (a) {
                var b = el("button", "wda-action");
                b.type = "button";
                b.textContent = a.label + " →";
                b.addEventListener("click", function () { runAction(a); });
                acts.appendChild(b);
            });
            bubble.appendChild(acts);
        }
        var meta = el("span", "wda-time");
        meta.textContent = timeLabel(m.ts);
        bubble.appendChild(meta);
        row.appendChild(bubble);
        messages.appendChild(row);
    }

    function setChips(list) {
        chips.innerHTML = "";
        (list || []).slice(0, 6).forEach(function (text) {
            var c = el("button", "wda-chip");
            c.type = "button";
            c.textContent = text;
            c.addEventListener("click", function () { send(text); });
            chips.appendChild(c);
        });
        chips.hidden = !chips.children.length;
    }

    function scrollToEnd() { messages.scrollTop = messages.scrollHeight; }

    function add(m) {
        m.ts = m.ts || Date.now();
        history.push(m);
        if (history.length > 60) history = history.slice(-60);
        save();
        renderMessage(m);
        scrollToEnd();
    }

    function showTyping(on) {
        var t = messages.querySelector(".wda-typing");
        if (on && !t) {
            t = el("div", "wda-row wda-bot wda-typing", BOT_AVATAR + '<div class="wda-bubble"><span class="wda-dots"><i></i><i></i><i></i></span></div>');
            messages.appendChild(t);
            scrollToEnd();
        } else if (!on && t) {
            t.remove();
        }
    }

    // ---------- Persistence ----------
    function save() {
        try { sessionStorage.setItem(STORE, JSON.stringify({ history: history, chips: currentChips })); } catch (e) { /* ignore */ }
    }
    var currentChips = [];
    function restore() {
        try {
            var data = JSON.parse(sessionStorage.getItem(STORE) || "null");
            if (data && data.history && data.history.length) {
                history = data.history;
                history.forEach(renderMessage);
                currentChips = data.chips || [];
                setChips(currentChips);
                scrollToEnd();
                return true;
            }
        } catch (e) { /* ignore */ }
        return false;
    }

    // ---------- Server calls ----------
    function request(url, options) {
        return fetch(url, Object.assign({ credentials: "same-origin", cache: "no-store" }, options))
            .then(function (r) {
                return r.json().catch(function () { return {}; }).then(function (data) {
                    if (!r.ok) throw new Error(data.message || "The assistant is unavailable right now. Please try again.");
                    return data;
                });
            });
    }

    function handleReply(data) {
        add({ who: "bot", text: data.reply || "Sorry, I couldn't prepare an answer.", actions: data.actions || [] });
        currentChips = data.suggestions || [];
        setChips(currentChips);
        save();
    }

    function greet() {
        showTyping(true);
        request(API + "/welcome", { method: "GET" })
            .then(function (data) { showTyping(false); handleReply(data); })
            .catch(function () {
                showTyping(false);
                handleReply({
                    reply: "Hi! I'm your WebDisplay assistant. Ask me about your balance, transactions, tickets or how to do something on this portal.",
                    suggestions: ["Check my balance", "Recent transactions", "Send money", "My support tickets"]
                });
            });
    }

    function send(text) {
        var message = String(text || "").trim();
        if (!message || busy) return;
        busy = true;
        sendBtn.disabled = true;
        setChips([]);
        add({ who: "user", text: message });
        input.value = "";
        autosize();
        showTyping(true);
        var started = Date.now();
        request(API + "/chat", {
            method: "POST",
            headers: { "Content-Type": "application/json", "X-CSRF-TOKEN": csrf() },
            body: JSON.stringify({ message: message })
        }).then(function (data) {
            // Short natural pause so the reply doesn't feel abrupt.
            var wait = Math.max(0, 450 - (Date.now() - started));
            setTimeout(function () { showTyping(false); handleReply(data); }, wait);
        }).catch(function (error) {
            showTyping(false);
            add({ who: "bot", text: error.message, error: true });
            setChips(currentChips);
        }).finally(function () {
            busy = false;
            sendBtn.disabled = false;
            input.focus();
        });
    }

    // ---------- Actions (navigate the dashboard) ----------
    function runAction(a) {
        if (!a) return;
        if (a.tab && a.tab.indexOf("link:") === 0) {
            window.location.href = contextPath + a.tab.slice(5);
            return;
        }
        if (a.prefill && a.prefill.open === "profile" && typeof window.showProfileEdit === "function") {
            window.showProfileEdit();
        } else if (a.tab && window.WDNav) {
            window.WDNav.show(a.tab);
        }
        if (a.prefill) {
            Object.keys(a.prefill).forEach(function (key) {
                if (key === "open") return;
                var field = document.getElementById(key);
                if (field && "value" in field) {
                    field.value = a.prefill[key];
                    field.dispatchEvent(new Event("change", { bubbles: true }));
                }
            });
        }
        var target = a.focus ? document.getElementById(a.focus) : null;
        if (window.matchMedia("(max-width: 640px)").matches) open(false);
        if (target) {
            setTimeout(function () {
                target.scrollIntoView({ behavior: "smooth", block: "center" });
                if (/^(INPUT|TEXTAREA|SELECT)$/.test(target.tagName)) target.focus({ preventScroll: true });
                target.classList.add("wda-highlight");
                setTimeout(function () { target.classList.remove("wda-highlight"); }, 1800);
            }, 60);
        }
    }

    // ---------- Open / close ----------
    function open(state) {
        win.classList.toggle("open", state);
        launcher.classList.toggle("open", state);
        launcher.setAttribute("aria-expanded", String(state));
        launcher.setAttribute("aria-label", state ? "Close chat assistant" : "Open chat assistant");
        launcher.innerHTML = state ? ICON_CLOSE : ICON_CHAT + '<span class="wda-launcher-label">Need help?</span>';
        try { sessionStorage.setItem(OPEN_STORE, state ? "1" : "0"); } catch (e) { /* ignore */ }
        if (state) {
            if (!history.length) greet();
            setTimeout(function () { input.focus(); scrollToEnd(); }, 50);
        }
    }

    function reset() {
        history = [];
        currentChips = [];
        messages.innerHTML = "";
        setChips([]);
        try { sessionStorage.removeItem(STORE); } catch (e) { /* ignore */ }
        greet();
    }

    function autosize() {
        input.style.height = "auto";
        input.style.height = Math.min(input.scrollHeight, 120) + "px";
    }

    launcher.addEventListener("click", function () { open(!win.classList.contains("open")); });
    win.querySelector('[data-wda="close"]').addEventListener("click", function () { open(false); launcher.focus(); });
    win.querySelector('[data-wda="reset"]').addEventListener("click", reset);
    form.addEventListener("submit", function (e) { e.preventDefault(); send(input.value); });
    input.addEventListener("input", autosize);
    input.addEventListener("keydown", function (e) {
        if (e.key === "Enter" && !e.shiftKey) { e.preventDefault(); send(input.value); }
    });
    document.addEventListener("keydown", function (e) {
        if (e.key === "Escape" && win.classList.contains("open")) { open(false); launcher.focus(); }
    });

    function init() {
        document.body.appendChild(win);
        document.body.appendChild(launcher);
        restore();
        var wasOpen = false;
        try { wasOpen = sessionStorage.getItem(OPEN_STORE) === "1"; } catch (e) { /* ignore */ }
        if (wasOpen) open(true);
    }

    if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", init);
    else init();

    window.WebDisplayAssistant = { open: function () { open(true); }, ask: function (q) { open(true); send(q); } };
})();
