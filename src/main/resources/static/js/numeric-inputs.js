(function () {
    "use strict";

    // Scrolling over a focused number field must not alter its value.
    document.addEventListener("wheel", function (event) {
        const target = event.target;
        if (target instanceof HTMLInputElement && target.type === "number" && document.activeElement === target) {
            event.preventDefault();
            target.blur();
        }
    }, { passive: false });
})();
