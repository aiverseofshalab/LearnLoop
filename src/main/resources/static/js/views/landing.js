// LearnLoop Public Landing & Course Discovery View
import { ApiClient } from '../api.js';
import { AuthState } from '../auth.js';
import { escapeHtml, openModal, closeModal, showToast, renderEmptyState } from '../ui.js';

export async function renderLandingView() {
    const main = document.getElementById('main-content');
    main.innerHTML = `
        <div class="hero-section">
            <div class="container">
                <div class="hero-pill">
                    <span>✨</span> Next-Gen Educational Platform
                </div>
                <h1 class="hero-title">
                    Master Any Subject with <span class="text-gradient">LearnLoop</span>
                </h1>
                <p class="hero-description">
                    The modern learning management system connecting curious students, dedicated educators, and administrators in one unified ecosystem. Structured courses, doubt resolution, and real-time Kafka-driven updates.
                </p>
                <div class="hero-actions" id="landing-hero-actions">
                    ${AuthState.isAuthenticated() 
                        ? `<a href="${AuthState.isTeacher() ? '#teacher-dashboard' : (AuthState.isAdmin() ? '#admin' : '#student-dashboard')}" class="btn btn-primary">Go to Dashboard</a>`
                        : `<button class="btn btn-primary" onclick="window.LearnLoopApp.openAuthModal('register')">Get Started Free</button>
                           <button class="btn btn-secondary" onclick="window.LearnLoopApp.openAuthModal('login')">Sign In</button>`
                    }
                </div>
            </div>
        </div>

        <div class="container" style="padding-bottom: 4rem;">
            <!-- Features Row -->
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 1.5rem; margin-bottom: 3.5rem;">
                <div class="glass-card">
                    <div style="font-size: 2rem; margin-bottom: 0.75rem;">📚</div>
                    <h3 style="font-size: 1.15rem; margin-bottom: 0.5rem;">Structured Curriculum</h3>
                    <p class="text-muted" style="font-size: 0.9rem;">Organized text and video lessons with progress tracking and step-by-step completion checks.</p>
                </div>
                <div class="glass-card">
                    <div style="font-size: 2rem; margin-bottom: 0.75rem;">💡</div>
                    <h3 style="font-size: 1.15rem; margin-bottom: 0.5rem;">Doubt Solving Board</h3>
                    <p class="text-muted" style="font-size: 0.9rem;">Ask academic questions on any course or lesson and receive verified instructor answers.</p>
                </div>
                <div class="glass-card">
                    <div style="font-size: 2rem; margin-bottom: 0.75rem;">📝</div>
                    <h3 style="font-size: 1.15rem; margin-bottom: 0.5rem;">Assignments & Grading</h3>
                    <p class="text-muted" style="font-size: 0.9rem;">Submit assignments with code and links. Get evaluative marks and written teacher feedback.</p>
                </div>
                <div class="glass-card">
                    <div style="font-size: 2rem; margin-bottom: 0.75rem;">⚡</div>
                    <h3 style="font-size: 1.15rem; margin-bottom: 0.5rem;">Reliable Outbox Events</h3>
                    <p class="text-muted" style="font-size: 0.9rem;">Powered by Spring Kafka transactional outbox for guaranteed, idempotent notification delivery.</p>
                </div>
            </div>

            <!-- Course Discovery Section -->
            <div style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 1rem; margin-bottom: 1.5rem;">
                <div>
                    <h2 style="font-size: 1.85rem;">Explore Courses</h2>
                    <p class="text-muted" style="font-size: 0.95rem;">Browse publicly available courses published by our verified educators</p>
                </div>
                <div style="display: flex; gap: 0.75rem; flex-wrap: wrap;">
                    <input type="text" id="course-search-input" class="form-input" placeholder="Search by title or topic..." style="max-width: 260px;">
                    <select id="course-subject-filter" class="form-select" style="max-width: 180px;">
                        <option value="">All Subjects</option>
                    </select>
                </div>
            </div>

            <div id="course-catalog-grid" class="course-grid">
                <div class="text-center" style="grid-column: 1/-1; padding: 3rem;">
                    <div style="font-size: 2rem; animation: pulse 1s infinite;">⏳</div>
                    <p class="text-muted" style="margin-top: 0.5rem;">Loading courses...</p>
                </div>
            </div>
        </div>
    `;

    // Load subjects filter and published courses
    await loadSubjects();
    await loadPublishedCourses();

    // Hook search and filter events
    const searchInput = document.getElementById('course-search-input');
    const subjectFilter = document.getElementById('course-subject-filter');

    let debounceTimer;
    searchInput?.addEventListener('input', () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => loadPublishedCourses(), 350);
    });

    subjectFilter?.addEventListener('change', () => loadPublishedCourses());
}

async function loadSubjects() {
    try {
        const res = await ApiClient.get('/courses/subjects');
        if (res && res.success && res.data) {
            const select = document.getElementById('course-subject-filter');
            if (select) {
                res.data.forEach(subj => {
                    const opt = document.createElement('option');
                    opt.value = subj;
                    opt.textContent = subj;
                    select.appendChild(opt);
                });
            }
        }
    } catch (e) {
        console.error('Failed to load subjects', e);
    }
}

async function loadPublishedCourses() {
    const grid = document.getElementById('course-catalog-grid');
    if (!grid) return;

    const query = document.getElementById('course-search-input')?.value || '';
    const subject = document.getElementById('course-subject-filter')?.value || '';

    try {
        const params = { page: 0, size: 24 };
        if (query.trim()) params.query = query.trim();
        if (subject) params.subject = subject;

        const res = await ApiClient.get('/courses', params);
        if (!res || !res.success || !res.data || res.data.content.length === 0) {
            grid.innerHTML = renderEmptyState(
                '🔍',
                'No Courses Found',
                'No courses match your current search criteria. Try a different search or subject filter.'
            );
            return;
        }

        grid.innerHTML = res.data.content.map(course => `
            <div class="glass-card course-card">
                <div>
                    <img src="${course.thumbnailUrl ? escapeHtml(course.thumbnailUrl) : 'data:image/svg+xml;utf8,<svg xmlns=\'http://www.w3.org/2000/svg\' width=\'300\' height=\'160\' viewBox=\'0 0 300 160\'><rect fill=\'%231e293b\' width=\'300\' height=\'160\'/><text fill=\'%23818cf8\' font-family=\'sans-serif\' font-size=\'20\' font-weight=\'bold\' x=\'50%25\' y=\'50%25\' text-anchor=\'middle\' dominant-baseline=\'middle\'>${encodeURIComponent(escapeHtml(course.title))}</text></svg>'}"
                         alt="${escapeHtml(course.title)}"
                         class="course-thumbnail"
                         onerror="this.src='data:image/svg+xml;utf8,<svg xmlns=\'http://www.w3.org/2000/svg\' width=\'300\' height=\'160\' viewBox=\'0 0 300 160\'><rect fill=\'%231e293b\' width=\'300\' height=\'160\'/><text fill=\'%23818cf8\' font-family=\'sans-serif\' font-size=\'20\' font-weight=\'bold\' x=\'50%25\' y=\'50%25\' text-anchor=\'middle\' dominant-baseline=\'middle\'>${encodeURIComponent(escapeHtml(course.title))}</text></svg>'">
                    <div class="course-meta">
                        <span class="badge badge-primary">${escapeHtml(course.subject)}</span>
                        <span class="badge badge-draft">${escapeHtml(course.difficultyLevel)}</span>
                    </div>
                    <h3 class="course-title">${escapeHtml(course.title)}</h3>
                    <p class="course-desc">${escapeHtml(course.shortDescription)}</p>
                </div>
                <div>
                    <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 1rem; font-size: 0.85rem; color: var(--text-muted);">
                        <span>👤 ${escapeHtml(course.teacherName)}</span>
                        <span>📖 ${course.lessonCount} lessons</span>
                    </div>
                    <button class="btn btn-primary" style="width: 100%;" onclick="window.LearnLoopApp.viewCourseDetails('${course.id}')">
                        View Course Details
                    </button>
                </div>
            </div>
        `).join('');
    } catch (e) {
        grid.innerHTML = renderEmptyState('⚠️', 'Error Loading Courses', e.message);
    }
}
