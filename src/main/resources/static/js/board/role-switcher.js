(function () {
    const STORAGE_KEY = "lms_demo_role";
    const MEMBER_STORAGE_KEY = "lms_demo_member_id";
    const DEFAULT_ROLE = "STUDENT";
    const DEFAULT_MEMBER_BY_ROLE = {
        ADMIN: "14",
        INSTRUCTOR: "11",
        STUDENT: "1"
    };
    const ROLE_LABELS = {
        ADMIN: "ADMIN",
        INSTRUCTOR: "INSTRUCTOR",
        STUDENT: "STUDENT"
    };

    function getRole() {
        return localStorage.getItem(STORAGE_KEY) || DEFAULT_ROLE;
    }

    function setRole(role) {
        localStorage.setItem(STORAGE_KEY, role);
        localStorage.setItem(MEMBER_STORAGE_KEY, DEFAULT_MEMBER_BY_ROLE[role] || DEFAULT_MEMBER_BY_ROLE[DEFAULT_ROLE]);
    }

    function getCurrentMemberId(role) {
        return localStorage.getItem(MEMBER_STORAGE_KEY) || DEFAULT_MEMBER_BY_ROLE[role] || DEFAULT_MEMBER_BY_ROLE[DEFAULT_ROLE];
    }

    function canModifyOrDelete(role, currentMemberId, postType, authorId) {
        if (!postType || !authorId) {
            return false;
        }

        if (postType === "ADMIN_NOTICE") {
            return role === "ADMIN" && currentMemberId === authorId;
        }

        if (postType === "COURSE_NOTICE") {
            return role === "INSTRUCTOR" && currentMemberId === authorId;
        }

        if (postType === "FREE") {
            return currentMemberId === authorId;
        }

        if (postType === "SECTION_QNA") {
            if (role === "INSTRUCTOR") {
                return true;
            }
            return role === "STUDENT" && currentMemberId === authorId;
        }

        return false;
    }

    function canReadSecret(role, currentMemberId, postType, authorId, isSecret) {
        if (!isSecret || postType !== "SECTION_QNA") {
            return true;
        }

        if (role === "ADMIN" || role === "INSTRUCTOR") {
            return true;
        }

        return currentMemberId === authorId;
    }

    function applyRole(role) {
        const currentMemberId = getCurrentMemberId(role);

        document.body.dataset.viewerRole = role;
        document.body.dataset.viewerMemberId = currentMemberId;

        document.querySelectorAll("[data-role-only]").forEach(element => {
            const allowed = (element.dataset.roleOnly || "")
                .split(",")
                .map(value => value.trim())
                .filter(Boolean);

            const visible = allowed.length === 0 || allowed.includes(role);
            element.classList.toggle("is-hidden", !visible);
        });

        document.querySelectorAll("[data-current-role]").forEach(element => {
            element.textContent = ROLE_LABELS[role] || role;
        });

        document.querySelectorAll("[data-current-member-id]").forEach(element => {
            element.textContent = currentMemberId;
        });

        document.querySelectorAll("[data-current-role-input]").forEach(element => {
            element.value = role;
        });

        document.querySelectorAll("[data-current-member-id-input]").forEach(element => {
            element.value = currentMemberId;
        });

        document.querySelectorAll("[data-sync-current-member]").forEach(element => {
            element.value = currentMemberId;
        });

        document.querySelectorAll("[data-board-action]").forEach(element => {
            const postType = element.dataset.postType;
            const authorId = element.dataset.authorId;
            const visible = canModifyOrDelete(role, currentMemberId, postType, authorId);
            element.classList.toggle("is-hidden", !visible);
        });

        document.querySelectorAll("[data-secret-guard]").forEach(element => {
            const postType = element.dataset.postType;
            const authorId = element.dataset.authorId;
            const isSecret = element.dataset.secret === "true";
            const visible = canReadSecret(role, currentMemberId, postType, authorId, isSecret);
            element.classList.toggle("blurred-secret", !visible);
            element.classList.toggle("secret-blocked", !visible);
        });

        document.querySelectorAll(".role-btn").forEach(button => {
            button.classList.toggle("active", button.dataset.role === role);
        });

        document.dispatchEvent(new CustomEvent("lms-role-change", {
            detail: {
                role,
                memberId: currentMemberId
            }
        }));
    }

    document.addEventListener("DOMContentLoaded", () => {
        const currentRole = getRole();
        if (!localStorage.getItem(MEMBER_STORAGE_KEY)) {
            localStorage.setItem(MEMBER_STORAGE_KEY, DEFAULT_MEMBER_BY_ROLE[currentRole] || DEFAULT_MEMBER_BY_ROLE[DEFAULT_ROLE]);
        }
        applyRole(currentRole);

        document.querySelectorAll(".role-btn").forEach(button => {
            button.addEventListener("click", () => {
                const role = button.dataset.role;
                setRole(role);
                applyRole(role);
            });
        });
    });
})();
