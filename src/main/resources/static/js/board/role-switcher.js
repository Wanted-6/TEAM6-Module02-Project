(function () {
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
            return role === "ADMIN" || currentMemberId === authorId;
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

    function canManageComment(role, currentMemberId, postType, authorId, courseInstructorId) {
        if (!postType || !authorId) {
            return false;
        }

        if (role === "ADMIN") {
            return true;
        }

        if (currentMemberId === authorId) {
            return true;
        }

        if (role === "INSTRUCTOR" && (postType === "COURSE_NOTICE" || postType === "SECTION_QNA")) {
            return !!courseInstructorId && currentMemberId === courseInstructorId;
        }

        return false;
    }

    function applyRole(role, currentMemberId) {
        if (!role || !currentMemberId) {
            return;
        }

        document.body.dataset.viewerRole = role;
        document.body.dataset.viewerMemberId = currentMemberId;

        document.querySelectorAll('.role-panel').forEach(element => {
            element.remove();
        });

        document.querySelectorAll("[data-role-only]").forEach(element => {
            const allowed = (element.dataset.roleOnly || "")
                .split(",")
                .map(value => value.trim())
                .filter(Boolean);

            const visible = allowed.length === 0 || allowed.includes(role);
            element.classList.toggle("is-hidden", !visible);
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

        document.querySelectorAll("[data-comment-action]").forEach(element => {
            const postType = element.dataset.postType;
            const authorId = element.dataset.authorId;
            const courseInstructorId = element.dataset.courseInstructorId;
            const visible = canManageComment(role, currentMemberId, postType, authorId, courseInstructorId);
            element.classList.toggle("is-hidden", !visible);
        });
    }

    document.addEventListener("DOMContentLoaded", () => {
        const currentRole = document.body.dataset.currentRole;
        const currentMemberId = document.body.dataset.currentMemberId;
        applyRole(currentRole, currentMemberId);
    });
})();
