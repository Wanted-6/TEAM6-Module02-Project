const ATTENDANCE_BUTTON_CLASS_GROUPS = {
    available: ['bg-primary', 'text-on-primary', 'hover:brightness-105'],
    late: ['bg-amber-400', 'text-slate-900', 'hover:brightness-105'],
    absent: ['bg-red-500', 'text-white', 'hover:brightness-105'],
    completedPresent: ['bg-green-600', 'text-white', 'cursor-default'],
    completedLate: ['bg-amber-400', 'text-slate-900', 'cursor-default'],
    completedAbsent: ['bg-red-500', 'text-white', 'cursor-default']
};

const ATTENDANCE_MESSAGE = {
    available: '출석 가능',
    late: '지각 예정',
    absent: '결석 예정',
    blocked: '아직 출석 가능한 기간이 아닙니다.'
};

document.addEventListener('DOMContentLoaded', function () {
    const accordionToggles = Array.from(document.querySelectorAll('.attendance-accordion-toggle'));
    const attendanceCheckAtInput = document.getElementById('attendanceCheckAtInput');
    const attendanceButtons = Array.from(document.querySelectorAll('[data-attendance-btn]'));

    const parseCheckAt = () => {
        if (!attendanceCheckAtInput || !attendanceCheckAtInput.value) {
            return new Date();
        }

        return new Date(attendanceCheckAtInput.value);
    };

    const calculateAttendanceState = (openDateText, checkAtDate) => {
        const presentStart = new Date(`${openDateText}T00:00:00`);
        const presentEnd = new Date(presentStart.getTime() + 24 * 60 * 60 * 1000);
        const lateEnd = new Date(presentStart.getTime() + 4 * 24 * 60 * 60 * 1000);

        if (checkAtDate < presentStart) {
            return { type: 'blocked', label: ATTENDANCE_MESSAGE.blocked };
        }

        if (checkAtDate < presentEnd) {
            return { type: 'available', label: ATTENDANCE_MESSAGE.available };
        }

        if (checkAtDate < lateEnd) {
            return { type: 'late', label: ATTENDANCE_MESSAGE.late };
        }

        return { type: 'absent', label: ATTENDANCE_MESSAGE.absent };
    };

    const clearButtonClasses = (button) => {
        Object.values(ATTENDANCE_BUTTON_CLASS_GROUPS).flat().forEach((className) => {
            button.classList.remove(className);
        });
    };

    const applyButtonStateStyle = (button, stateType) => {
        clearButtonClasses(button);

        if (stateType === 'late') {
            button.classList.add(...ATTENDANCE_BUTTON_CLASS_GROUPS.late);
            return;
        }

        if (stateType === 'absent') {
            button.classList.add(...ATTENDANCE_BUTTON_CLASS_GROUPS.absent);
            return;
        }

        button.classList.add(...ATTENDANCE_BUTTON_CLASS_GROUPS.available);
    };

    const applyCompletedStyle = (button, status) => {
        clearButtonClasses(button);

        if (status === 'LATE') {
            button.classList.add(...ATTENDANCE_BUTTON_CLASS_GROUPS.completedLate);
            return;
        }

        if (status === 'ABSENT') {
            button.classList.add(...ATTENDANCE_BUTTON_CLASS_GROUPS.completedAbsent);
            return;
        }

        button.classList.add(...ATTENDANCE_BUTTON_CLASS_GROUPS.completedPresent);
    };

    const applySavedStatus = (button, statusLabel, savedStatus) => {
        if (savedStatus === 'LATE') {
            button.textContent = '지각 출석 완료';
            button.disabled = true;
            applyCompletedStyle(button, savedStatus);
            statusLabel.textContent = '지각 출석 완료';
            statusLabel.className = 'text-[11px] font-semibold text-amber-500';
            return true;
        }

        if (savedStatus === 'ABSENT') {
            button.textContent = '결석 출석 완료';
            button.disabled = true;
            applyCompletedStyle(button, savedStatus);
            statusLabel.textContent = '결석 출석 완료';
            statusLabel.className = 'text-[11px] font-semibold text-red-500';
            return true;
        }

        if (savedStatus === 'PRESENT') {
            button.textContent = '출석 완료';
            button.disabled = true;
            applyCompletedStyle(button, savedStatus);
            statusLabel.textContent = '출석 완료';
            statusLabel.className = 'text-[11px] font-semibold text-green-600';
            return true;
        }

        return false;
    };

    accordionToggles.forEach((toggle) => {
        const content = toggle.parentElement.querySelector('.attendance-accordion-content');
        const icon = toggle.querySelector('.attendance-accordion-icon');

        const syncAccordionState = (isOpen) => {
            if (!content || !icon) {
                return;
            }

            toggle.setAttribute('aria-expanded', String(isOpen));
            content.classList.toggle('hidden', !isOpen);
            icon.textContent = isOpen ? 'expand_less' : 'expand_more';
        };

        syncAccordionState(toggle.getAttribute('aria-expanded') === 'true');

        toggle.addEventListener('click', function () {
            const willOpen = content.classList.contains('hidden');

            accordionToggles.forEach((item) => {
                const itemContent = item.parentElement.querySelector('.attendance-accordion-content');
                const itemIcon = item.querySelector('.attendance-accordion-icon');
                const isCurrent = item === toggle ? willOpen : false;

                if (!itemContent || !itemIcon) {
                    return;
                }

                item.setAttribute('aria-expanded', String(isCurrent));
                itemContent.classList.toggle('hidden', !isCurrent);
                itemIcon.textContent = isCurrent ? 'expand_less' : 'expand_more';
            });
        });
    });

    attendanceButtons.forEach((button) => {
        const wrapper = button.parentElement;
        const message = wrapper.querySelector('[data-attendance-message]');
        const progressWrap = wrapper.querySelector('[data-attendance-progress-wrap]');
        const progressBar = wrapper.querySelector('[data-attendance-progress-bar]');
        const statusLabel = wrapper.querySelector('[data-attendance-status-label]');

        if (!message || !progressWrap || !progressBar || !statusLabel) {
            return;
        }

        let progress = 0;
        let intervalId = null;
        let completedStatus = button.dataset.savedStatus || null;

        const memberId = button.dataset.memberId;
        const sectionId = button.dataset.sectionId;
        const openDate = button.dataset.openDate;

        const setButtonLabel = (label) => {
            button.textContent = label;
        };

        const refreshPredictedState = () => {
            if (applySavedStatus(button, statusLabel, completedStatus)) {
                return { type: 'completed' };
            }

            const currentState = calculateAttendanceState(openDate, parseCheckAt());
            statusLabel.textContent = currentState.label;

            if (currentState.type === 'late') {
                statusLabel.className = 'text-[11px] font-semibold text-amber-500';
            } else if (currentState.type === 'absent') {
                statusLabel.className = 'text-[11px] font-semibold text-red-500';
            } else if (currentState.type === 'blocked') {
                statusLabel.className = 'text-[11px] font-semibold text-slate-500';
            } else {
                statusLabel.className = 'text-[11px] font-semibold text-blue-600';
            }

            applyButtonStateStyle(button, currentState.type);
            return currentState;
        };

        const resetProgress = () => {
            progress = 0;
            progressBar.style.width = '0%';
            progressWrap.classList.add('hidden');
        };

        const setResumeState = () => {
            intervalId = null;
            setButtonLabel('이어보기');
            message.textContent = '학습이 멈췄습니다. 이어보기를 누르면 계속 진행됩니다.';
        };

        const markCompleted = (status, serverMessage) => {
            completedStatus = status;
            button.dataset.savedStatus = status;
            intervalId = null;
            message.textContent = serverMessage;
            progressBar.style.width = '100%';
            progressWrap.classList.remove('hidden');
            refreshPredictedState();
        };

        const saveAttendance = () => {
            const params = new URLSearchParams();
            params.set('memberId', memberId);
            params.set('sectionId', sectionId);

            if (attendanceCheckAtInput && attendanceCheckAtInput.value) {
                params.set('checkedAt', `${attendanceCheckAtInput.value}:00`);
            }

            fetch('/student/attendance/check', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: params.toString()
            })
                .then(async (response) => {
                    const data = await response.json();

                    if (!response.ok) {
                        throw new Error(data.message || '출석 처리에 실패했습니다.');
                    }

                    return data;
                })
                .then((data) => {
                    markCompleted(data.status, data.message);
                    setTimeout(() => {
                        window.location.reload();
                    }, 800);
                })
                .catch((error) => {
                    setResumeState();
                    message.textContent = error.message || '출석 처리에 실패했습니다. 다시 시도해 주세요.';
                });
        };

        button.addEventListener('click', function () {
            if (completedStatus) {
                return;
            }

            const currentState = refreshPredictedState();

            if (currentState.type === 'blocked') {
                message.textContent = '아직 출석 가능한 기간이 아닙니다.';
                resetProgress();
                return;
            }

            if (intervalId) {
                clearInterval(intervalId);
                setResumeState();
                return;
            }

            progressWrap.classList.remove('hidden');
            setButtonLabel('중단하기');
            message.textContent = progress === 0
                ? '학습을 시작했습니다. 3초 동안 유지하면 출석이 반영됩니다.'
                : '이어서 학습 중입니다.';

            intervalId = setInterval(() => {
                progress += 100 / 30;
                progressBar.style.width = `${Math.min(progress, 100)}%`;

                if (progress >= 100) {
                    clearInterval(intervalId);
                    intervalId = null;
                    progress = 100;
                    progressBar.style.width = '100%';
                    message.textContent = '학습이 완료되어 출석을 반영하고 있습니다.';
                    saveAttendance();
                }
            }, 100);
        });

        refreshPredictedState();
    });

    if (attendanceCheckAtInput) {
        attendanceCheckAtInput.addEventListener('change', function () {
            attendanceButtons.forEach((button) => {
                if (!button.dataset.savedStatus) {
                    button.dispatchEvent(new Event('attendance-preview-refresh'));
                }
            });
        });
    }

    attendanceButtons.forEach((button) => {
        button.addEventListener('attendance-preview-refresh', function () {
            const wrapper = button.parentElement;
            const statusLabel = wrapper.querySelector('[data-attendance-status-label]');
            const openDate = button.dataset.openDate;
            const savedStatus = button.dataset.savedStatus;
            const state = calculateAttendanceState(openDate, parseCheckAt());

            if (!statusLabel || savedStatus) {
                return;
            }

            statusLabel.textContent = state.label;

            if (state.type === 'late') {
                statusLabel.className = 'text-[11px] font-semibold text-amber-500';
            } else if (state.type === 'absent') {
                statusLabel.className = 'text-[11px] font-semibold text-red-500';
            } else if (state.type === 'blocked') {
                statusLabel.className = 'text-[11px] font-semibold text-slate-500';
            } else {
                statusLabel.className = 'text-[11px] font-semibold text-blue-600';
            }

            applyButtonStateStyle(button, state.type);
        });
    });
});