/*
 * Dashboard tabs.
 *
 * Groups each dashboard's sections into a few task-based tabs so users see
 * only what they are working on instead of scrolling through every section.
 * Nothing is removed from the page: sections are only shown/hidden, so all
 * existing buttons, forms and scripts keep working.
 *
 * If a script scrolls to or focuses something in another tab (e.g. "Use
 * customer ID" filling the Create Account form), that tab opens automatically.
 */
(function () {
    "use strict";

    var CONFIG = {
        customer: {
            pinned: ["welcome"],
            tabs: [
                { id: "home", label: "Overview", sections: ["My Profile", "#profileEditForm", "My Accounts", "Account Balance"] },
                { id: "transfer", label: "Send Money", sections: ["Money Transfer", "Saved Beneficiaries"] },
                { id: "history", label: "Transactions & Statements", sections: ["Transaction History", "Account Statement"] },
                { id: "support", label: "Help & Support", sections: ["Raise a Support Ticket", "My Support Tickets"] }
            ]
        },
        employee: {
            pinned: ["welcome"],
            tabs: [
                { id: "customers", label: "Customers", sections: ["Create Customer", "Search Customers"] },
                { id: "open", label: "Open Account", sections: ["Create Account", "ID Document Applications"] },
                { id: "manage", label: "Manage Accounts", sections: ["Account Management", "Deposit / Withdrawal", "Edit Account Details", "All Accounts"] },
                { id: "transactions", label: "Transactions", sections: ["Transaction History"] }
            ]
        },
        helpdesk: {
            pinned: [],
            tabs: [
                { id: "tickets", label: "Tickets", sections: ["#ticketFilterSection", "#newTicketsSection", "#agentRaisedTicketsSection", "#allTicketsSection", "#myTicketsSection", "#employeeTicketSection", "#managerReviewSection", "#ticketConversationSection"] },
                { id: "customers", label: "Customer Lookup", sections: ["Customer Search", "Customer Details", "#createTicketForCustomerSection", "Account Information", "Transaction Investigation"] },
                { id: "investigation", label: "Investigation History", sections: ["#investigationSection"] },
                { id: "overview", label: "Statistics", sections: ["#statisticsSection", "#dailyTicketChartSection"] }
            ]
        },
        admin: {
            pinned: [],
            tabs: [
                { id: "staff", label: "Staff", sections: ["Staff Management", "#editCard"] },
                { id: "add-staff", label: "Add Staff", sections: ["Create Staff Account"] },
                { id: "audit", label: "Audit History", sections: ["Audit History"] },
                { id: "settings", label: "Settings", sections: ["#siteThemeSettings"] }
            ]
        },
        "bank-manager": {
            pinned: ["Bank Manager Dashboard"],
            tabs: [
                { id: "documents", label: "ID Documents", sections: ["Pending ID Document Verification"] },
                { id: "address", label: "Address Changes", sections: ["Pending Customer Address Changes"] },
                { id: "help", label: "Instructions", sections: ["Verification Instructions"] }
            ]
        }
    };

    var body = document.body;
    var dashboard = body && body.getAttribute("data-dashboard");
    var config = CONFIG[dashboard];
    if (!config) return;

    var STORAGE_KEY = "webdisplay.tab." + dashboard;
    var container = document.querySelector("body > .container, body > .dashboard-container");
    if (!container) return;

    function headingText(el) {
        var h = null;
        for (var i = 0; i < el.children.length; i++) {
            if (/^H[1-3]$/.test(el.children[i].tagName)) { h = el.children[i]; break; }
        }
        return h ? h.textContent.replace(/^\s*\d+\.\s*/, "").trim() : "";
    }

    function matches(el, key) {
        if (key.charAt(0) === "#") return el.id === key.slice(1);
        if (el.classList.contains(key)) return true;
        return headingText(el).toLowerCase() === key.toLowerCase();
    }

    var sections = Array.prototype.filter.call(container.children, function (el) {
        return !/^(SCRIPT|STYLE|TEMPLATE)$/.test(el.tagName);
    });

    // Assign each section to a tab (or keep it pinned/always visible).
    var tabOf = new Map();
    var members = {};
    config.tabs.forEach(function (t) { members[t.id] = []; });
    sections.forEach(function (el) {
        if (config.pinned.some(function (k) { return matches(el, k); })) return;
        for (var i = 0; i < config.tabs.length; i++) {
            var t = config.tabs[i];
            if (t.sections.some(function (k) { return matches(el, k); })) {
                tabOf.set(el, t.id);
                members[t.id].push(el);
                el.classList.add("wd-tab-panel");
                return;
            }
        }
        // Unknown sections stay visible so nothing new is ever hidden by mistake.
    });

    // Build the tab bar.
    var bar = document.createElement("nav");
    bar.className = "wd-tabs";
    bar.setAttribute("aria-label", "Dashboard sections");
    var list = document.createElement("div");
    list.className = "wd-tabs-list";
    list.setAttribute("role", "tablist");
    bar.appendChild(list);

    var buttons = {};
    config.tabs.forEach(function (t) {
        if (!members[t.id].length) return;
        var b = document.createElement("button");
        b.type = "button";
        b.className = "wd-tab";
        b.id = "wd-tab-" + t.id;
        b.setAttribute("role", "tab");
        b.setAttribute("data-tab", t.id);
        b.textContent = t.label;
        b.addEventListener("click", function () { show(t.id, { scroll: true }); });
        list.appendChild(b);
        buttons[t.id] = b;
    });

    // The bar goes right above the first tabbed section; pinned blocks
    // (welcome banner etc.) stay above it.
    var firstPanel = sections.filter(function (el) { return tabOf.has(el); })[0];
    if (!firstPanel) return;
    container.insertBefore(bar, firstPanel);

    // Arrow-key navigation between tabs.
    list.addEventListener("keydown", function (e) {
        if (e.key !== "ArrowRight" && e.key !== "ArrowLeft") return;
        var visible = Array.prototype.filter.call(list.children, function (b) { return !b.hidden; });
        var i = visible.indexOf(document.activeElement);
        if (i < 0) return;
        var next = visible[(i + (e.key === "ArrowRight" ? 1 : -1) + visible.length) % visible.length];
        next.focus();
        next.click();
    });

    var current = null;

    function available(tabId) {
        // A tab is hidden when every one of its sections is hidden for this
        // user's role (pages use the "hidden" class for that).
        return members[tabId].some(function (el) { return !el.classList.contains("hidden"); });
    }

    function refreshAvailability() {
        Object.keys(buttons).forEach(function (id) { buttons[id].hidden = !available(id); });
        if (current && !available(current)) show(firstAvailable());
    }

    function firstAvailable() {
        for (var i = 0; i < config.tabs.length; i++) {
            var id = config.tabs[i].id;
            if (buttons[id] && available(id)) return id;
        }
        return null;
    }

    function show(tabId, opts) {
        if (!tabId || !buttons[tabId]) return;
        current = tabId;
        tabOf.forEach(function (id, el) { el.classList.toggle("wd-tab-hidden", id !== tabId); });
        Object.keys(buttons).forEach(function (id) {
            var active = id === tabId;
            buttons[id].classList.toggle("active", active);
            buttons[id].setAttribute("aria-selected", String(active));
            buttons[id].tabIndex = active ? 0 : -1;
        });
        try { sessionStorage.setItem(STORAGE_KEY, tabId); } catch (e) { /* ignore */ }
        if (opts && opts.scroll) {
            var top = bar.getBoundingClientRect().top + window.pageYOffset - 8;
            if (window.pageYOffset > top) window.scrollTo({ top: top, behavior: "smooth" });
        }
        buttons[tabId].scrollIntoView({ block: "nearest", inline: "nearest" });
        document.dispatchEvent(new CustomEvent("wd:tabchange", { detail: { tab: tabId } }));
    }

    /** Opens the tab that contains the element (used before scrolling/focusing it). */
    function reveal(el) {
        if (!el || !el.closest) return;
        var panel = el.closest(".wd-tab-panel");
        if (panel && tabOf.has(panel) && tabOf.get(panel) !== current) show(tabOf.get(panel));
    }

    var nativeScroll = Element.prototype.scrollIntoView;
    Element.prototype.scrollIntoView = function () {
        if (!this.classList || !this.classList.contains("wd-tab")) reveal(this);
        return nativeScroll.apply(this, arguments);
    };
    var nativeFocus = HTMLElement.prototype.focus;
    HTMLElement.prototype.focus = function () {
        reveal(this);
        return nativeFocus.apply(this, arguments);
    };

    // Keep tab visibility in sync with role-based show/hide done by the page.
    var observer = new MutationObserver(refreshAvailability);
    tabOf.forEach(function (id, el) { observer.observe(el, { attributes: true, attributeFilter: ["class"] }); });

    var initial = null;
    var hash = (location.hash || "").replace(/^#/, "");
    if (buttons[hash]) initial = hash;
    if (!initial) { try { initial = sessionStorage.getItem(STORAGE_KEY); } catch (e) { /* ignore */ } }
    if (!buttons[initial] || !available(initial)) initial = firstAvailable();
    refreshAvailability();
    show(initial);

    window.WDNav = {
        show: function (tabId) { show(tabId, { scroll: true }); },
        reveal: reveal,
        current: function () { return current; }
    };
})();
