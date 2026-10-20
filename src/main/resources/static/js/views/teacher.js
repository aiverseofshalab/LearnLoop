// LearnLoop Teacher View: Dashboard, Course Creator, Lesson Management, and Student Roster
import { ApiClient } from '../api.js';
import { AuthState } from '../auth.js';
import { escapeHtml, openModal, closeModal, showToast, renderEmptyState } from '../ui.js';

export async function renderTeacherDashboard() {
    const main = document.getElementById('main-content');
    main.innerHTML = `
        <div class="page-container">
            <div style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 1rem; margin-bottom: 2rem;">
                <div>
                    <h1 style="font-size: 2rem; margin-bottom: 0.5rem;">Instructor Dashboard 🎓</h1>
                    <p class="text-muted">Manage your courses, curriculum lessons, student doubts, and submissions.</p>
                </div>
                <div>
                    <button class="btn btn-primary" onclick="window.LearnLoopApp.openCreateCourseModal()">+ Create New Course</button>
                </div>
            </div>

            <!-- Stats Bar -->
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1.25rem; margin-bottom: 2.5rem;" id="teacher-stats-bar">
                <div class="glass-card">
                    <p class="text-muted" style="font-size: 0.8rem; font-weight: 600;">TOTAL COURSES</p>
                    <h2 style="font-size: 2rem; margin-top: 0.5rem;" id="teacher-stat-total">-</h2>
                </div>
                <div class="glass-card">
                    <p class="text-muted" style="font-size: 0.8rem; font-weight: 600;">PUBLISHED</p>
                    <h2 style="font-size: 2rem; margin-top: 0.5rem; color: #34d399;" id="teacher-stat-published">-</h2>
                </div>
                <div class="glass-card">
                    <p class="text-muted" style="font-size: 0.8rem; font-weight: 600;">DRAFTS</p>
                    <h2 style="font-size: 2rem; margin-top: 0.5rem; color: #cbd5e1;" id="teacher-stat-drafts">-</h2>
                </div>
                <div class="glass-card">
                    <p class="text-muted" style="font-size: 0.8rem; font-weight: 600;">TOTAL ENROLLMENTS</p>
                    <h2 style="font-size: 2rem; margin-top: 0.5rem; color: #818cf8;" id="teacher-stat-enrollments">-</h2>
                </div>
                <div class="glass-card">
                    <p class="text-muted" style="font-size: 0.8rem; font-weight: 600;">UNANSWERED DOUBTS</p>
                    <h2 style="font-size: 2rem; margin-top: 0.5rem; color: #fbbf24;" id="teacher-stat-doubts">-</h2>
                </div>
                <div class="glass-card">
                    <p class="text-muted" style="font-size: 0.8rem; font-weight: 600;">PENDING GRADES</p>
                    <h2 style="font-size: 2rem; margin-top: 0.5rem; color: #f43f5e;" id="teacher-stat-submissions">-</h2>
                </div>
            </div>

            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 1.25rem;">
                <h2 style="font-size: 1.4rem;">My Courses</h2>
            </div>

            <div id="teacher-courses-list" class="course-grid">
                <div class="text-center" style="grid-column: 1/-1; padding: 3rem;">
                    <p class="text-muted">Loading your courses...</p>
                </div>
            </div>
        </div>
    `;

    await loadTeacherDashboard();
}

async function loadTeacherDashboard() {
    try {
        const statsRes = await ApiClient.get('/teacher/analytics');
        if (statsRes && statsRes.success && statsRes.data) {
            const data = statsRes.data;
            document.getElementById('teacher-stat-total').textContent = data.totalCourses;
            document.getElementById('teacher-stat-published').textContent = data.publishedCourses;
            document.getElementById('teacher-stat-drafts').textContent = data.draftCourses;
            document.getElementById('teacher-stat-enrollments').textContent = data.totalEnrollments;
            document.getElementById('teacher-stat-doubts').textContent = data.unansweredQuestionsCount;
            document.getElementById('teacher-stat-submissions').textContent = data.pendingSubmissionsCount;
        }

        const coursesRes = await ApiClient.get('/teacher/courses', { page: 0, size: 50 });
        const listContainer = document.getElementById('teacher-courses-list');

        if (!coursesRes || !coursesRes.success || !coursesRes.data || coursesRes.data.content.length === 0) {
            listContainer.innerHTML = renderEmptyState(
                '📚',
                'No Courses Created Yet',
                'You have not authored any courses yet. Create your first course to begin publishing lessons!',
                '<button class="btn btn-primary" onclick="window.LearnLoopApp.openCreateCourseModal()">+ Author First Course</button>'
            );
            return;
        }

        listContainer.innerHTML = coursesRes.data.content.map(c => `
            <div class="glass-card course-card">
                <div>
                    <div class="course-meta">
                        <span class="badge ${c.status === 'PUBLISHED' ? 'badge-success' : 'badge-draft'}">${c.status}</span>
                        <span class="badge badge-primary">${escapeHtml(c.subject)}</span>
                    </div>
                    <h3 class="course-title">${escapeHtml(c.title)}</h3>
                    <p class="course-desc">${escapeHtml(c.shortDescription)}</p>
                    <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 1rem;">📖 ${c.lessonCount} lessons published</p>
                </div>
                <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                    <div style="display: flex; gap: 0.5rem;">
                        <button class="btn btn-primary btn-sm" style="flex: 1;" onclick="window.LearnLoopApp.openAddLessonModal('${c.id}', '${escapeHtml(c.title)}')">
                            + Add Lesson
                        </button>
                        ${c.status === 'DRAFT'
                            ? `<button class="btn btn-success btn-sm" onclick="window.LearnLoopApp.publishCourse('${c.id}')">Publish</button>`
                            : `<button class="btn btn-secondary btn-sm" onclick="window.LearnLoopApp.unpublishCourse('${c.id}')">Unpublish</button>`
                        }
                    </div>
                    <button class="btn btn-outline btn-sm" onclick="window.LearnLoopApp.viewCourseRoster('${c.id}', '${escapeHtml(c.title)}')">
                        👥 View Enrolled Students
                    </button>
                </div>
            </div>
        `).join('');
    } catch (e) {
        showToast('Error loading teacher dashboard: ' + e.message, 'error');
    }
}

export function openCreateCourseModal() {
    const bodyHtml = `
        <form id="create-course-form" onsubmit="event.preventDefault(); window.LearnLoopApp.submitCreateCourse();">
            <div class="form-group">
                <label class="form-label">Course Title *</label>
                <input type="text" id="course-form-title" class="form-input" required placeholder="e.g. Modern Full-Stack Development with Spring Boot & Kafka">
            </div>
            <div class="form-group">
                <label class="form-label">Subject / Category *</label>
                <input type="text" id="course-form-subject" class="form-input" required placeholder="e.g. Computer Science, Mathematics, Physics">
            </div>
            <div class="form-group">
                <label class="form-label">Difficulty Level *</label>
                <select id="course-form-difficulty" class="form-select">
                    <option value="BEGINNER">Beginner</option>
                    <option value="INTERMEDIATE" selected>Intermediate</option>
                    <option value="ADVANCED">Advanced</option>
                </select>
            </div>
            <div class="form-group">
                <label class="form-label">Short Description (Max 500 chars) *</label>
                <input type="text" id="course-form-short-desc" class="form-input" required placeholder="Concise overview displayed on cards">
            </div>
            <div class="form-group">
                <label class="form-label">Detailed Description *</label>
                <textarea id="course-form-detailed-desc" class="form-textarea" required placeholder="Comprehensive curriculum description, prerequisites, and learning objectives"></textarea>
            </div>
            <div class="form-group">
                <label class="form-label">Thumbnail Image URL</label>
                <input type="url" id="course-form-thumbnail" class="form-input" placeholder="https://example.com/thumbnail.jpg">
            </div>
        </form>
    `;

    const footerHtml = `
        <button class="btn btn-secondary" onclick="window.LearnLoopApp.closeModal()">Cancel</button>
        <button class="btn btn-primary" onclick="window.LearnLoopApp.submitCreateCourse()">Save Draft Course</button>
    `;

    openModal('Create New Course', bodyHtml, footerHtml);
}

export async function submitCreateCourse() {
    const title = document.getElementById('course-form-title')?.value;
    const subject = document.getElementById('course-form-subject')?.value;
    const difficultyLevel = document.getElementById('course-form-difficulty')?.value;
    const shortDescription = document.getElementById('course-form-short-desc')?.value;
    const detailedDescription = document.getElementById('course-form-detailed-desc')?.value;
    const thumbnailUrl = document.getElementById('course-form-thumbnail')?.value || null;

    if (!title || !subject || !shortDescription || !detailedDescription) {
        showToast('Please fill out all required fields', 'error');
        return;
    }

    try {
        const res = await ApiClient.post('/teacher/courses', {
            title,
            subject,
            difficultyLevel,
            shortDescription,
            detailedDescription,
            thumbnailUrl
        });

        if (res && res.success) {
            closeModal();
            showToast('Course draft created successfully! You can now add lessons.', 'success');
            await loadTeacherDashboard();
        }
    } catch (e) {
        showToast('Failed to create course: ' + e.message, 'error');
    }
}

export async function publishCourse(courseId) {
    try {
        const res = await ApiClient.post(`/teacher/courses/${courseId}/publish`);
        if (res && res.success) {
            showToast('Course published! It is now live in the student catalog.', 'success');
            await loadTeacherDashboard();
        }
    } catch (e) {
        showToast('Failed to publish course: ' + e.message, 'error');
    }
}

export async function unpublishCourse(courseId) {
    try {
        const res = await ApiClient.post(`/teacher/courses/${courseId}/unpublish`);
        if (res && res.success) {
            showToast('Course moved back to DRAFT state.', 'info');
            await loadTeacherDashboard();
        }
    } catch (e) {
        showToast('Failed to unpublish course: ' + e.message, 'error');
    }
}

export function openAddLessonModal(courseId, courseTitle) {
    const bodyHtml = `
        <form id="add-lesson-form" onsubmit="event.preventDefault(); window.LearnLoopApp.submitAddLesson('${courseId}');">
            <p class="text-muted" style="margin-bottom: 1rem; font-size: 0.9rem;">Course: <strong>${escapeHtml(courseTitle)}</strong></p>
            <div class="form-group">
                <label class="form-label">Lesson Title *</label>
                <input type="text" id="lesson-form-title" class="form-input" required placeholder="e.g. Chapter 1: Introduction to In-Memory Databases">
            </div>
            <div class="form-group">
                <label class="form-label">Content Type *</label>
                <select id="lesson-form-type" class="form-select" onchange="window.LearnLoopApp.toggleLessonContentField()">
                    <option value="TEXT">Text Material</option>
                    <option value="VIDEO_URL">External Educational Video URL</option>
                </select>
            </div>
            <div class="form-group" id="lesson-content-text-group">
                <label class="form-label">Lesson Text Content *</label>
                <textarea id="lesson-form-content-text" class="form-textarea" rows="6" placeholder="Write comprehensive markdown/text explanation for the lesson..."></textarea>
            </div>
            <div class="form-group hidden" id="lesson-content-url-group">
                <label class="form-label">Educational Video URL *</label>
                <input type="url" id="lesson-form-content-url" class="form-input" placeholder="https://www.youtube.com/watch?v=...">
            </div>
            <div class="form-group">
                <label class="form-label">Brief Description (Optional)</label>
                <input type="text" id="lesson-form-desc" class="form-input" placeholder="e.g. 15 minute overview of essential concepts">
            </div>
        </form>
    `;

    const footerHtml = `
        <button class="btn btn-secondary" onclick="window.LearnLoopApp.closeModal()">Cancel</button>
        <button class="btn btn-primary" onclick="window.LearnLoopApp.submitAddLesson('${courseId}')">Add Lesson</button>
    `;

    openModal('Add Lesson', bodyHtml, footerHtml);
}

export function toggleLessonContentField() {
    const type = document.getElementById('lesson-form-type')?.value;
    const textGroup = document.getElementById('lesson-content-text-group');
    const urlGroup = document.getElementById('lesson-content-url-group');

    if (type === 'VIDEO_URL') {
        textGroup?.classList.add('hidden');
        urlGroup?.classList.remove('hidden');
    } else {
        textGroup?.classList.remove('hidden');
        urlGroup?.classList.add('hidden');
    }
}

export async function submitAddLesson(courseId) {
    const title = document.getElementById('lesson-form-title')?.value;
    const contentType = document.getElementById('lesson-form-type')?.value;
    const description = document.getElementById('lesson-form-desc')?.value;
    
    let contentUrlOrText = '';
    if (contentType === 'VIDEO_URL') {
        contentUrlOrText = document.getElementById('lesson-form-content-url')?.value;
    } else {
        contentUrlOrText = document.getElementById('lesson-form-content-text')?.value;
    }

    if (!title || !contentUrlOrText) {
        showToast('Please provide a lesson title and content', 'error');
        return;
    }

    try {
        const res = await ApiClient.post(`/teacher/courses/${courseId}/lessons`, {
            title,
            contentType,
            contentUrlOrText,
            description,
            status: 'PUBLISHED'
        });

        if (res && res.success) {
            closeModal();
            showToast('Lesson added successfully to course!', 'success');
            await loadTeacherDashboard();
        }
    } catch (e) {
        showToast('Failed to add lesson: ' + e.message, 'error');
    }
}

export async function viewCourseRoster(courseId, courseTitle) {
    try {
        const res = await ApiClient.get(`/teacher/courses/${courseId}/enrollments`);
        if (!res || !res.success || !res.data) return;

        const roster = res.data.content;
        const bodyHtml = `
            <p class="text-muted" style="margin-bottom: 1.25rem;">Enrolled students for: <strong>${escapeHtml(courseTitle)}</strong> (${roster.length} Total)</p>
            ${roster.length === 0
                ? '<p class="text-muted">No students have enrolled in this course yet.</p>'
                : `
                    <div class="data-table-wrapper">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Enrolled Date</th>
                                    <th>Status</th>
                                    <th>Progress</th>
                                    <th>Completed</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${roster.map(e => `
                                    <tr>
                                        <td>${new Date(e.enrolledAt).toLocaleDateString()}</td>
                                        <td><span class="badge badge-success">${e.status}</span></td>
                                        <td><strong>${e.progressPercentage}%</strong></td>
                                        <td>${e.completedLessons} / ${e.totalLessons} lessons</td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>
                `
            }
        `;

        openModal('Course Enrollment Roster', bodyHtml, '<button class="btn btn-secondary" onclick="window.LearnLoopApp.closeModal()">Close</button>');
    } catch (e) {
        showToast('Failed to load roster: ' + e.message, 'error');
    }
}
