// LearnLoop Student View: Dashboard, Enrolled Courses, and Interactive Lesson Viewer
import { ApiClient } from '../api.js';
import { AuthState } from '../auth.js';
import { escapeHtml, openModal, closeModal, showToast, renderEmptyState } from '../ui.js';

export async function renderStudentDashboard() {
    const main = document.getElementById('main-content');
    main.innerHTML = `
        <div class="page-container">
            <div style="margin-bottom: 2rem;">
                <h1 style="font-size: 2rem; margin-bottom: 0.5rem;">Welcome back, ${escapeHtml(AuthState.getUser()?.fullName || 'Student')}! 👋</h1>
                <p class="text-muted">Track your academic progress, enrolled courses, and lesson completions.</p>
            </div>

            <!-- Stats Bar -->
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1.25rem; margin-bottom: 2.5rem;" id="student-stats-container">
                <div class="glass-card">
                    <p class="text-muted" style="font-size: 0.85rem; font-weight: 600;">ENROLLED COURSES</p>
                    <h2 style="font-size: 2.25rem; margin: 0.5rem 0 0;" id="stat-enrolled-count">-</h2>
                </div>
                <div class="glass-card">
                    <p class="text-muted" style="font-size: 0.85rem; font-weight: 600;">OVERALL PROGRESS</p>
                    <h2 style="font-size: 2.25rem; margin: 0.5rem 0 0; color: #818cf8;" id="stat-progress-pct">-</h2>
                </div>
                <div class="glass-card">
                    <p class="text-muted" style="font-size: 0.85rem; font-weight: 600;">LESSONS COMPLETED</p>
                    <h2 style="font-size: 2.25rem; margin: 0.5rem 0 0; color: #34d399;" id="stat-completed-lessons">-</h2>
                </div>
            </div>

            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 1.25rem;">
                <h2 style="font-size: 1.4rem;">My Enrolled Courses</h2>
                <a href="#courses" class="btn btn-outline btn-sm">Browse Catalog</a>
            </div>

            <div id="enrolled-courses-list" class="course-grid">
                <div class="text-center" style="grid-column: 1/-1; padding: 3rem;">
                    <p class="text-muted">Loading your courses...</p>
                </div>
            </div>
        </div>
    `;

    await loadStudentDashboardData();
}

async function loadStudentDashboardData() {
    try {
        const res = await ApiClient.get('/students/me/dashboard');
        if (!res || !res.success || !res.data) return;

        const data = res.data;
        document.getElementById('stat-enrolled-count').textContent = data.totalEnrolledCourses;
        document.getElementById('stat-progress-pct').textContent = `${data.overallProgressPercentage}%`;
        document.getElementById('stat-completed-lessons').textContent = data.totalCompletedLessons;

        const listContainer = document.getElementById('enrolled-courses-list');
        if (!data.enrolledCourses || data.enrolledCourses.length === 0) {
            listContainer.innerHTML = renderEmptyState(
                '🎓',
                'No Enrolled Courses Yet',
                'You have not enrolled in any courses yet. Browse our course catalog to start learning!',
                '<a href="#courses" class="btn btn-primary">Discover Courses</a>'
            );
            return;
        }

        listContainer.innerHTML = data.enrolledCourses.map(course => `
            <div class="glass-card course-card">
                <div>
                    <div class="course-meta">
                        <span class="badge badge-primary">${escapeHtml(course.courseSubject)}</span>
                        <span class="badge badge-success">${course.progressPercentage}% Complete</span>
                    </div>
                    <h3 class="course-title">${escapeHtml(course.courseTitle)}</h3>
                    <p class="text-muted" style="font-size: 0.85rem; margin-bottom: 1rem;">Instructor: ${escapeHtml(course.teacherName)}</p>
                    
                    <div style="margin-bottom: 1.25rem;">
                        <div style="display: flex; justify-content: space-between; font-size: 0.8rem; color: var(--text-muted); margin-bottom: 0.3rem;">
                            <span>Progress</span>
                            <span>${course.completedLessons} / ${course.totalLessons} lessons</span>
                        </div>
                        <div class="progress-container">
                            <div class="progress-fill" style="width: ${course.progressPercentage}%;"></div>
                        </div>
                    </div>
                </div>
                <div>
                    <button class="btn btn-primary" style="width: 100%;" onclick="window.LearnLoopApp.openLessonViewer('${course.courseId}')">
                        Continue Learning →
                    </button>
                </div>
            </div>
        `).join('');
    } catch (e) {
        console.error('Failed to load student dashboard', e);
        showToast('Error loading dashboard: ' + e.message, 'error');
    }
}

export async function openCourseDetailsModal(courseId) {
    try {
        const res = await ApiClient.get(`/courses/${courseId}`);
        if (!res || !res.success || !res.data) return;

        const course = res.data;
        const isStudent = AuthState.isStudent();

        const bodyHtml = `
            <div style="margin-bottom: 1.5rem;">
                <div style="display: flex; gap: 0.5rem; margin-bottom: 0.75rem;">
                    <span class="badge badge-primary">${escapeHtml(course.subject)}</span>
                    <span class="badge badge-draft">${escapeHtml(course.difficultyLevel)}</span>
                </div>
                <h2 style="font-size: 1.6rem; margin-bottom: 0.75rem;">${escapeHtml(course.title)}</h2>
                <p class="text-muted" style="margin-bottom: 1rem; line-height: 1.6;">${escapeHtml(course.detailedDescription)}</p>
                <p style="font-size: 0.9rem;"><strong>Instructor:</strong> ${escapeHtml(course.teacherName)}</p>
            </div>

            <h3 style="font-size: 1.15rem; margin-bottom: 1rem; border-top: 1px solid var(--border-subtle); padding-top: 1rem;">
                Course Syllabus (${course.lessons ? course.lessons.length : 0} Lessons)
            </h3>
            <div style="display: flex; flex-direction: column; gap: 0.5rem; max-height: 250px; overflow-y: auto;">
                ${course.lessons && course.lessons.length > 0
                    ? course.lessons.map((lesson, idx) => `
                        <div style="display: flex; align-items: center; justify-content: space-between; padding: 0.65rem 0.85rem; background: rgba(255,255,255,0.03); border-radius: var(--radius-sm);">
                            <span style="font-size: 0.9rem;">${idx + 1}. ${escapeHtml(lesson.title)}</span>
                            <span class="badge badge-draft" style="font-size: 0.7rem;">${escapeHtml(lesson.contentType)}</span>
                        </div>
                    `).join('')
                    : '<p class="text-muted" style="font-size: 0.9rem;">No lessons published yet.</p>'
                }
            </div>
        `;

        let footerHtml = '';
        if (course.isEnrolled) {
            footerHtml = `
                <button class="btn btn-secondary" onclick="window.LearnLoopApp.closeModal()">Close</button>
                <button class="btn btn-primary" onclick="window.LearnLoopApp.closeModal(); window.LearnLoopApp.openLessonViewer('${course.id}')">
                    Open Course Lessons
                </button>
            `;
        } else if (isStudent) {
            footerHtml = `
                <button class="btn btn-secondary" onclick="window.LearnLoopApp.closeModal()">Close</button>
                <button class="btn btn-success" onclick="window.LearnLoopApp.enrollCourse('${course.id}')">
                    Enroll in Course Free
                </button>
            `;
        } else if (!AuthState.isAuthenticated()) {
            footerHtml = `
                <button class="btn btn-secondary" onclick="window.LearnLoopApp.closeModal()">Close</button>
                <button class="btn btn-primary" onclick="window.LearnLoopApp.closeModal(); window.LearnLoopApp.openAuthModal('register')">
                    Register to Enroll
                </button>
            `;
        } else {
            footerHtml = `<button class="btn btn-secondary" onclick="window.LearnLoopApp.closeModal()">Close</button>`;
        }

        openModal(course.title, bodyHtml, footerHtml);
    } catch (e) {
        showToast('Failed to load course details: ' + e.message, 'error');
    }
}

export async function enrollInCourse(courseId) {
    try {
        const res = await ApiClient.post(`/courses/${courseId}/enroll`);
        if (res && res.success) {
            closeModal();
            showToast('🎉 Enrolled successfully! Check notifications for confirmation.', 'success');
            // Route to student dashboard
            window.location.hash = '#student-dashboard';
        }
    } catch (e) {
        showToast(e.message, 'error');
    }
}

export async function renderLessonViewer(courseId) {
    const main = document.getElementById('main-content');
    main.innerHTML = `
        <div class="page-container">
            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 1.5rem;">
                <button class="btn btn-secondary btn-sm" onclick="window.history.back()">← Back</button>
                <div style="display: flex; gap: 0.75rem;">
                    <a href="#discussions?courseId=${courseId}" class="btn btn-outline btn-sm">💬 Course Doubts</a>
                    <a href="#assignments?courseId=${courseId}" class="btn btn-outline btn-sm">📝 Assignments</a>
                </div>
            </div>

            <div class="lesson-layout" id="lesson-viewer-container">
                <div class="text-center" style="grid-column: 1/-1; padding: 4rem;">
                    <p class="text-muted">Loading lessons...</p>
                </div>
            </div>
        </div>
    `;

    try {
        const res = await ApiClient.get(`/courses/${courseId}/lessons`);
        if (!res || !res.success || !res.data || res.data.length === 0) {
            document.getElementById('lesson-viewer-container').innerHTML = renderEmptyState(
                '📖',
                'No Lessons Available',
                'This course does not have any active lessons yet.'
            );
            return;
        }

        const lessons = res.data;
        const container = document.getElementById('lesson-viewer-container');

        container.innerHTML = `
            <div class="lesson-sidebar">
                <h3 style="font-size: 1.1rem; margin-bottom: 1rem;">Course Playlist</h3>
                <div id="lesson-items-playlist">
                    ${lessons.map((lesson, idx) => `
                        <div class="lesson-item ${idx === 0 ? 'active' : ''}" 
                             id="lesson-item-${lesson.id}"
                             onclick="window.LearnLoopApp.selectLesson('${lesson.id}', '${courseId}')">
                            <span style="font-size: 0.9rem; font-weight: 500;">
                                ${idx + 1}. ${escapeHtml(lesson.title)}
                            </span>
                            <span id="lesson-status-${lesson.id}">
                                ${lesson.completed ? '✅' : '⚪'}
                            </span>
                        </div>
                    `).join('')}
                </div>
            </div>

            <div class="lesson-content-card" id="active-lesson-display">
                <!-- Active lesson loads here -->
            </div>
        `;

        // Load first lesson by default
        await selectLesson(lessons[0].id, courseId);
    } catch (e) {
        showToast('Error loading lessons: ' + e.message, 'error');
    }
}

export async function selectLesson(lessonId, courseId) {
    // Update active highlight in playlist
    document.querySelectorAll('.lesson-item').forEach(item => item.classList.remove('active'));
    document.getElementById(`lesson-item-${lessonId}`)?.classList.add('active');

    const display = document.getElementById('active-lesson-display');
    if (!display) return;

    display.innerHTML = '<p class="text-muted">Loading lesson content...</p>';

    try {
        const res = await ApiClient.get(`/lessons/${lessonId}`);
        if (!res || !res.success || !res.data) return;

        const lesson = res.data;

        let contentHtml = '';
        if (lesson.contentType === 'VIDEO_URL') {
            // Check if YouTube
            let videoUrl = lesson.contentUrlOrText;
            if (videoUrl.includes('youtube.com/watch?v=')) {
                videoUrl = videoUrl.replace('watch?v=', 'embed/');
            } else if (videoUrl.includes('youtu.be/')) {
                videoUrl = videoUrl.replace('youtu.be/', 'youtube.com/embed/');
            }

            contentHtml = `
                <div class="video-responsive">
                    <iframe src="${escapeHtml(videoUrl)}" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
                </div>
            `;
        } else {
            contentHtml = `
                <div class="lesson-body-text">
                    ${escapeHtml(lesson.contentUrlOrText)}
                </div>
            `;
        }

        display.innerHTML = `
            <div style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 1rem; border-bottom: 1px solid var(--border-subtle); padding-bottom: 1rem;">
                <div>
                    <span class="badge badge-primary">${escapeHtml(lesson.contentType)}</span>
                    <h1 style="font-size: 1.8rem; margin-top: 0.5rem;">${escapeHtml(lesson.title)}</h1>
                </div>
                <div id="mark-complete-btn-container">
                    ${lesson.completed 
                        ? `<span class="badge badge-success" style="font-size: 0.85rem; padding: 0.5rem 1rem;">✅ Completed</span>`
                        : `<button class="btn btn-success" onclick="window.LearnLoopApp.markLessonComplete('${lesson.id}')">
                            Mark as Complete ✓
                           </button>`
                    }
                </div>
            </div>

            ${lesson.description ? `<p class="text-muted" style="margin-top: 1rem; font-size: 0.95rem;">${escapeHtml(lesson.description)}</p>` : ''}

            ${contentHtml}
        `;
    } catch (e) {
        display.innerHTML = renderEmptyState('⚠️', 'Error Loading Lesson', e.message);
    }
}

export async function markLessonComplete(lessonId) {
    try {
        const res = await ApiClient.post(`/lessons/${lessonId}/complete`);
        if (res && res.success) {
            showToast('Lesson marked complete! Progress updated.', 'success');

            // Update UI checkmark and button state
            const statusEl = document.getElementById(`lesson-status-${lessonId}`);
            if (statusEl) statusEl.textContent = '✅';

            const btnContainer = document.getElementById('mark-complete-btn-container');
            if (btnContainer) {
                btnContainer.innerHTML = `<span class="badge badge-success" style="font-size: 0.85rem; padding: 0.5rem 1rem;">✅ Completed</span>`;
            }
        }
    } catch (e) {
        showToast('Failed to mark lesson complete: ' + e.message, 'error');
    }
}
