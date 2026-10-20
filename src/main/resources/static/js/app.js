// LearnLoop Main Application Controller & Router
import { ApiClient } from './api.js';
import { AuthState } from './auth.js';
import { openModal, closeModal, showToast, escapeHtml } from './ui.js';
import { renderLandingView } from './views/landing.js';
import { renderStudentDashboard, openCourseDetailsModal, enrollInCourse, renderLessonViewer, selectLesson, markLessonComplete } from './views/student.js';
import { renderTeacherDashboard, openCreateCourseModal, submitCreateCourse, publishCourse, unpublishCourse, openAddLessonModal, toggleLessonContentField, submitAddLesson, viewCourseRoster } from './views/teacher.js';
import { renderDiscussionView, filterDoubtsByStatus, openAskQuestionModal, submitQuestion, openQuestionThread, submitReply, resolveQuestion } from './views/discussion.js';
import { renderAssignmentView, openCreateAssignmentModal, submitCreateAssignment, openSubmitAssignmentModal, submitAssignmentSolution, openTeacherSubmissionsModal, openGradeSubmissionModal, submitGrade } from './views/assignment.js';
import { renderAdminView, updateUserStatus, openEditUserModal, submitEditUser } from './views/admin.js';
import { NotificationManager } from './views/notifications.js';

class LearnLoopApp {
    constructor() {
        this.init();
    }

    async init() {
        this.registerServiceWorker();

        // Listen for auth state changes
        window.addEventListener('learnloop:auth-change', (e) => {
            this.updateNavigation(e.detail);
            if (e.detail) {
                NotificationManager.startPolling();
            } else {
                NotificationManager.stopPolling();
            }
        });

        // Listen for unauthorized 401
        window.addEventListener('learnloop:unauthorized', () => {
            showToast('Session expired. Please log in again.', 'info');
            window.location.hash = '#landing';
            this.openAuthModal('login');
        });

        // Listen for hash changes
        window.addEventListener('hashchange', () => this.handleRouting());

        // Check active session
        const user = await AuthState.checkSession();
        this.updateNavigation(user);
        if (user) {
            NotificationManager.startPolling();
        }

        // Initial route
        this.handleRouting();
    }

    registerServiceWorker() {
        if ('serviceWorker' in navigator) {
            window.addEventListener('load', () => {
                navigator.serviceWorker.register('/sw.js')
                    .then(reg => console.log('LearnLoop Service Worker registered:', reg.scope))
                    .catch(err => console.log('Service Worker registration skipped:', err));
            });
        }
    }

    updateNavigation(user) {
        const sidebar = document.getElementById('app-sidebar');
        const mainWrapper = document.getElementById('main-wrapper');
        const topbarAuth = document.getElementById('topbar-auth-section');
        const sidebarNav = document.getElementById('sidebar-nav-links');
        const userBadge = document.getElementById('sidebar-user-badge');
        const notifBtn = document.getElementById('topbar-notification-btn');

        if (!user) {
            // Unauthenticated: hide sidebar, full width content
            sidebar?.classList.add('hidden');
            mainWrapper?.classList.add('full-width');
            if (notifBtn) notifBtn.style.display = 'none';

            if (topbarAuth) {
                topbarAuth.innerHTML = `
                    <button class="btn btn-outline btn-sm" onclick="window.LearnLoopApp.openAuthModal('login')">Sign In</button>
                    <button class="btn btn-primary btn-sm" onclick="window.LearnLoopApp.openAuthModal('register')">Get Started</button>
                `;
            }
        } else {
            // Authenticated: show sidebar
            sidebar?.classList.remove('hidden');
            mainWrapper?.classList.remove('full-width');
            if (notifBtn) notifBtn.style.display = 'flex';

            if (userBadge) {
                userBadge.innerHTML = `
                    <div class="avatar-placeholder">${user.fullName.charAt(0).toUpperCase()}</div>
                    <div style="overflow: hidden;">
                        <div style="font-weight: 600; font-size: 0.9rem; white-space: nowrap; text-overflow: ellipsis; overflow: hidden;">${escapeHtml(user.fullName)}</div>
                        <div style="font-size: 0.75rem; color: var(--text-muted);">${user.role}</div>
                    </div>
                `;
            }

            if (topbarAuth) {
                topbarAuth.innerHTML = `
                    <span class="badge ${user.role === 'ADMIN' ? 'badge-danger' : (user.role === 'TEACHER' ? 'badge-warning' : 'badge-primary')}">
                        ${user.role}
                    </span>
                    <button class="btn btn-outline btn-sm" onclick="window.LearnLoopApp.handleLogout()">Logout</button>
                `;
            }

            // Generate Role-Specific Sidebar Links
            if (sidebarNav) {
                let links = '';
                if (user.role === 'STUDENT') {
                    links = `
                        <a href="#student-dashboard" class="nav-link" id="nav-student-dashboard">📊 Dashboard</a>
                        <a href="#courses" class="nav-link" id="nav-courses">🔍 Browse Courses</a>
                        <a href="#discussions" class="nav-link" id="nav-discussions">💡 Doubt Solving</a>
                        <a href="#assignments" class="nav-link" id="nav-assignments">📝 Assignments</a>
                    `;
                } else if (user.role === 'TEACHER') {
                    links = `
                        <a href="#teacher-dashboard" class="nav-link" id="nav-teacher-dashboard">📊 Dashboard</a>
                        <a href="#courses" class="nav-link" id="nav-courses">🔍 All Courses</a>
                        <a href="#discussions" class="nav-link" id="nav-discussions">💡 Student Doubts</a>
                        <a href="#assignments" class="nav-link" id="nav-assignments">📝 Assignments & Grading</a>
                    `;
                } else if (user.role === 'ADMIN') {
                    links = `
                        <a href="#admin" class="nav-link" id="nav-admin">🛡️ Admin Overview</a>
                        <a href="#courses" class="nav-link" id="nav-courses">🔍 Course Moderation</a>
                        <a href="#discussions" class="nav-link" id="nav-discussions">💡 Discussion Board</a>
                    `;
                }
                sidebarNav.innerHTML = links;
            }
        }
    }

    handleRouting() {
        const hash = window.location.hash || '#landing';
        const [route, queryString] = hash.split('?');
        const params = new URLSearchParams(queryString || '');
        const user = AuthState.getUser();

        // Update active sidebar nav styling
        document.querySelectorAll('.nav-link').forEach(link => {
            link.classList.remove('active');
            if (link.getAttribute('href') === route) {
                link.classList.add('active');
            }
        });

        // Close mobile drawer on route transition
        document.getElementById('app-sidebar')?.classList.remove('mobile-open');

        // Route dispatcher
        if (route === '#student-dashboard') {
            if (!user) return this.redirectToLogin();
            renderStudentDashboard();
        } else if (route === '#teacher-dashboard') {
            if (!user || user.role !== 'TEACHER') return this.redirectToLogin();
            renderTeacherDashboard();
        } else if (route === '#admin') {
            if (!user || user.role !== 'ADMIN') return this.redirectToLogin();
            renderAdminView();
        } else if (route === '#discussions') {
            renderDiscussionView(params.get('courseId'));
        } else if (route === '#assignments') {
            renderAssignmentView(params.get('courseId'));
        } else if (route === '#lesson') {
            const courseId = params.get('courseId');
            if (courseId) {
                renderLessonViewer(courseId);
            } else {
                window.location.hash = '#courses';
            }
        } else {
            // Default: Landing / Course Catalog
            renderLandingView();
        }
    }

    redirectToLogin() {
        window.location.hash = '#landing';
        this.openAuthModal('login');
    }

    openAuthModal(initialTab = 'login') {
        const bodyHtml = `
            <div class="tabs-nav">
                <button class="tab-btn ${initialTab === 'login' ? 'active' : ''}" id="auth-tab-login" onclick="window.LearnLoopApp.switchAuthTab('login')">Sign In</button>
                <button class="tab-btn ${initialTab === 'register' ? 'active' : ''}" id="auth-tab-register" onclick="window.LearnLoopApp.switchAuthTab('register')">Create Account</button>
            </div>

            <!-- Login Form -->
            <form id="auth-form-login" class="${initialTab === 'login' ? '' : 'hidden'}" onsubmit="event.preventDefault(); window.LearnLoopApp.submitLogin();">
                <div class="form-group">
                    <label class="form-label">Email Address *</label>
                    <input type="email" id="login-email" class="form-input" required placeholder="your.email@example.com">
                </div>
                <div class="form-group">
                    <label class="form-label">Password *</label>
                    <input type="password" id="login-password" class="form-input" required placeholder="••••••••">
                </div>
                <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 1rem;">Sign In to LearnLoop</button>
            </form>

            <!-- Register Form -->
            <form id="auth-form-register" class="${initialTab === 'register' ? '' : 'hidden'}" onsubmit="event.preventDefault(); window.LearnLoopApp.submitRegister();">
                <div class="form-group">
                    <label class="form-label">Full Name *</label>
                    <input type="text" id="reg-name" class="form-input" required placeholder="e.g. Maya Patel">
                </div>
                <div class="form-group">
                    <label class="form-label">Email Address *</label>
                    <input type="email" id="reg-email" class="form-input" required placeholder="your.email@example.com">
                </div>
                <div class="form-group">
                    <label class="form-label">Password (Min 8 chars) *</label>
                    <input type="password" id="reg-password" class="form-input" minlength="8" required placeholder="••••••••">
                </div>
                <div class="form-group">
                    <label class="form-label">Account Role *</label>
                    <select id="reg-role" class="form-select" required>
                        <option value="STUDENT" selected>Student (Learn, ask doubts, submit assignments)</option>
                        <option value="TEACHER">Educator / Teacher (Author courses, answer doubts, grade)</option>
                    </select>
                </div>
                <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 1rem;">Create Account Free</button>
            </form>
        `;

        openModal('Welcome to LearnLoop', bodyHtml, '');
    }

    switchAuthTab(tab) {
        document.getElementById('auth-tab-login')?.classList.toggle('active', tab === 'login');
        document.getElementById('auth-tab-register')?.classList.toggle('active', tab === 'register');
        document.getElementById('auth-form-login')?.classList.toggle('hidden', tab !== 'login');
        document.getElementById('auth-form-register')?.classList.toggle('hidden', tab !== 'register');
    }

    async submitLogin() {
        const email = document.getElementById('login-email')?.value;
        const password = document.getElementById('login-password')?.value;

        if (!email || !password) {
            showToast('Please enter both email and password', 'error');
            return;
        }

        try {
            const user = await AuthState.login(email.trim(), password);
            closeModal();
            showToast(`Welcome back, ${user.fullName}!`, 'success');
            
            // Route to appropriate dashboard
            if (user.role === 'TEACHER') {
                window.location.hash = '#teacher-dashboard';
            } else if (user.role === 'ADMIN') {
                window.location.hash = '#admin';
            } else {
                window.location.hash = '#student-dashboard';
            }
        } catch (e) {
            showToast(e.message, 'error');
        }
    }

    async submitRegister() {
        const fullName = document.getElementById('reg-name')?.value;
        const email = document.getElementById('reg-email')?.value;
        const password = document.getElementById('reg-password')?.value;
        const role = document.getElementById('reg-role')?.value;

        if (!fullName || !email || !password || !role) {
            showToast('Please fill out all required registration fields', 'error');
            return;
        }

        try {
            const user = await AuthState.register(email.trim(), password, fullName.trim(), role);
            closeModal();
            showToast(`Account created! Welcome to LearnLoop, ${user.fullName}.`, 'success');

            if (user.role === 'TEACHER') {
                window.location.hash = '#teacher-dashboard';
            } else {
                window.location.hash = '#student-dashboard';
            }
        } catch (e) {
            showToast(e.message, 'error');
        }
    }

    async handleLogout() {
        await AuthState.logout();
        showToast('Logged out successfully', 'info');
        window.location.hash = '#landing';
        renderLandingView();
    }

    toggleMobileMenu() {
        const sidebar = document.getElementById('app-sidebar');
        sidebar?.classList.toggle('mobile-open');
    }

    // Expose methods for UI elements
    closeModal() { closeModal(); }
    viewCourseDetails(id) { openCourseDetailsModal(id); }
    enrollCourse(id) { enrollInCourse(id); }
    openLessonViewer(courseId) { window.location.hash = `#lesson?courseId=${courseId}`; }
    selectLesson(lessonId, courseId) { selectLesson(lessonId, courseId); }
    markLessonComplete(lessonId) { markLessonComplete(lessonId); }
    
    // Teacher methods
    openCreateCourseModal() { openCreateCourseModal(); }
    submitCreateCourse() { submitCreateCourse(); }
    publishCourse(id) { publishCourse(id); }
    unpublishCourse(id) { unpublishCourse(id); }
    openAddLessonModal(courseId, title) { openAddLessonModal(courseId, title); }
    toggleLessonContentField() { toggleLessonContentField(); }
    submitAddLesson(courseId) { submitAddLesson(courseId); }
    viewCourseRoster(courseId, title) { viewCourseRoster(courseId, title); }

    // Discussion methods
    filterDoubtsByStatus(status) { filterDoubtsByStatus(status); }
    openAskQuestionModal() { openAskQuestionModal(); }
    submitQuestion() { submitQuestion(); }
    openQuestionThread(id) { openQuestionThread(id); }
    submitReply(id) { submitReply(id); }
    resolveQuestion(id) { resolveQuestion(id); }

    // Assignment methods
    openCreateAssignmentModal() { openCreateAssignmentModal(); }
    submitCreateAssignment() { submitCreateAssignment(); }
    openSubmitAssignmentModal(id, title, maxMarks, existingSub) { openSubmitAssignmentModal(id, title, maxMarks, existingSub); }
    submitAssignmentSolution(id) { submitAssignmentSolution(id); }
    openTeacherSubmissionsModal(id, title) { openTeacherSubmissionsModal(id, title); }
    openGradeSubmissionModal(id, name, max, marks, fb, text, url) { openGradeSubmissionModal(id, name, max, marks, fb, text, url); }
    submitGrade(id, max) { submitGrade(id, max); }

    // Admin methods
    updateUserStatus(id, status) { updateUserStatus(id, status); }
    openEditUserModal(id, name, role, status) { openEditUserModal(id, name, role, status); }
    submitEditUser(id) { submitEditUser(id); }

    // Notifications
    toggleNotifications() { NotificationManager.toggleDrawer(); }
    closeNotifications() { NotificationManager.closeDrawer(); }
    markAllNotificationsRead() { NotificationManager.markAllRead(); }
    handleNotificationClick(id, refType, refId) { NotificationManager.handleNotificationClick(id, refType, refId); }
}

// Bootstrap application instance on window
window.LearnLoopApp = new LearnLoopApp();
