(function () {
    document.addEventListener("DOMContentLoaded", function () {
        document.querySelectorAll(".post-row").forEach(function (row) {
            row.addEventListener("click", function (event) {
                if (event.target.closest("a, button, input, select, textarea, label")) {
                    return;
                }

                const href = row.dataset.href;
                if (href) {
                    window.location.href = href;
                }
            });
        });
    });
})();
