// LearnLoop Discussion & Academic Doubt-Solving View
import { ApiClient } from '../api.js';
import { AuthState } from '../auth.js';
import { escapeHtml, openModal, closeModal, showToast, formatDate, renderEmptyState } from '../ui.js';

let activeCourseFilter = null;

export async function renderDiscussionView(courseId = null) {
    activeCourseFilter = courseId;
    const main = document.getElementById('main-content');

    main.innerHTML = `
        <div class="page-container">
            <div style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 1rem; margin-bottom: 2rem;">
                <div>
                    <h1 style="font-size: 2rem; margin-bottom: 0.5rem;">Academic Doubt-Solving Board 💡</h1>
                    <p class="text-muted">Ask questions, share explanations, and get verified answers from instructors.</p>
                </div>
                <div>
                    <button class="btn btn-primary" onclick="window.LearnLoopApp.openAskQuestionModal()">+ Ask a Doubt</button>
                </div>
            </div>

            <!-- Filters Bar -->
            <div class="glass-card" style="margin-bottom: 2rem; padding: 1.25rem;">
                <div style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 1rem;">
                    <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;" id="doubt-status-filters">
                        <button class="btn btn-outline btn-sm active" onclick="window.LearnLoopApp.filterDoubtsByStatus('')">All Questions</button>
                        <button class="btn btn-outline btn-sm" onclick="window.LearnLoopApp.filterDoubtsByStatus('OPEN')">Unanswered (Open)</button>
                        <button class="btn btn-outline btn-sm" onclick="window.LearnLoopApp.filterDoubtsByStatus('ANSWERED')">Answered</button>
                        <button class="btn btn-outline btn-sm" onclick="window.LearnLoopApp.filterDoubtsByStatus('RESOLVED')">Resolved</button>
                    </div>
                    <div style="display: flex; gap: 0.75rem;">
                        <input type="text" id="doubt-search-input" class="form-input" placeholder="Search questions..." style="max-width: 250px;">
                    </div>
                </div>
            </div>

            <!-- Question Thread List -->
            <div id="doubts-list-container">
                <div class="text-center" style="padding: 3rem;">
                    <p class="text-muted">Loading questions...</p>
                </div>
            </div>
        </div>
    `;

    await loadQuestions();

    const searchInput = document.getElementById('doubt-search-input');
    let debounce;
    searchInput?.addEventListener('input', () => {
        clearTimeout(debounce);
        debounce = setTimeout(() => loadQuestions(), 350);
    });
}

let currentSelectedStatus = '';

export function filterDoubtsByStatus(status) {
    currentSelectedStatus = status;
    document.querySelectorAll('#doubt-status-filters .btn').forEach(btn => btn.classList.remove('active'));
    event.target.classList.add('active');
    loadQuestions();
}

async function loadQuestions() {
    const container = document.getElementById('doubts-list-container');
    if (!container) return;

    const query = document.getElementById('doubt-search-input')?.value || '';

    try {
        const params = { page: 0, size: 25 };
        if (activeCourseFilter) params.courseId = activeCourseFilter;
        if (currentSelectedStatus) params.status = currentSelectedStatus;
        if (query.trim()) params.query = query.trim();

        const res = await ApiClient.get('/questions', params);
        if (!res || !res.success || !res.data || res.data.content.length === 0) {
            container.innerHTML = renderEmptyState(
                '💬',
                'No Questions Found',
                'There are no questions matching your current filters. Be the first to ask a doubt!',
                '<button class="btn btn-primary" onclick="window.LearnLoopApp.openAskQuestionModal()">Ask a Doubt</button>'
            );
            return;
        }

        container.innerHTML = res.data.content.map(q => {
            let badgeClass = 'badge-warning';
            if (q.status === 'ANSWERED') badgeClass = 'badge-primary';
            if (q.status === 'RESOLVED') badgeClass = 'badge-success';

            return `
                <div class="glass-card doubt-thread-card" onclick="window.LearnLoopApp.openQuestionThread('${q.id}')">
                    <div style="display: flex; align-items: flex-start; justify-content: space-between; gap: 1rem; margin-bottom: 0.75rem;">
                        <div>
                            <span class="badge ${badgeClass}" style="margin-bottom: 0.5rem;">${q.status}</span>
                            <h3 style="font-size: 1.2rem; color: var(--text-main);">${escapeHtml(q.title)}</h3>
                        </div>
                        <span class="badge badge-draft" style="font-size: 0.8rem;">💬 ${q.replyCount} replies</span>
                    </div>
                    <p class="text-muted" style="font-size: 0.9rem; margin-bottom: 1rem; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
                        ${escapeHtml(q.description)}
                    </p>
                    <div style="display: flex; align-items: center; justify-content: space-between; font-size: 0.85rem; color: var(--text-subtle);">
                        <span>📖 ${escapeHtml(q.courseTitle)}</span>
                        <span>Asked by <strong>${escapeHtml(q.studentName)}</strong> • ${formatDate(q.createdAt)}</span>
                    </div>
                </div>
            `;
        }).join('');
    } catch (e) {
        container.innerHTML = renderEmptyState('⚠️', 'Error Loading Questions', e.message);
    }
}

export async function openAskQuestionModal() {
    try {
        // Fetch courses the student is enrolled in, or teacher courses
        let courses = [];
        if (AuthState.isTeacher()) {
            const res = await ApiClient.get('/teacher/courses', { size: 50 });
            courses = res.data?.content || [];
        } else {
            const res = await ApiClient.get('/students/me/enrollments', { size: 50 });
            courses = (res.data?.content || []).map(e => ({ id: e.courseId, title: e.courseTitle }));
        }

        if (courses.length === 0) {
            showToast('You must be enrolled in at least one course to ask a doubt', 'warning');
            return;
        }

        const bodyHtml = `
            <form id="ask-question-form" onsubmit="event.preventDefault(); window.LearnLoopApp.submitQuestion();">
                <div class="form-group">
                    <label class="form-label">Related Course *</label>
                    <select id="question-course-select" class="form-select" required>
                        ${courses.map(c => `
                            <option value="${c.id}" ${activeCourseFilter === c.id ? 'selected' : ''}>
                                ${escapeHtml(c.title)}
                            </option>
                        `).join('')}
                    </select>
                </div>
                <div class="form-group">
                    <label class="form-label">Question Title *</label>
                    <input type="text" id="question-title-input" class="form-input" required placeholder="e.g. How do transactional outbox and Kafka idempotency interact?">
                </div>
                <div class="form-group">
                    <label class="form-label">Detailed Description of Your Doubt *</label>
                    <textarea id="question-desc-input" class="form-textarea" rows="5" required placeholder="Describe what you were trying to solve, your expected vs actual outcome, or specific concepts needing clarification..."></textarea>
                </div>
            </form>
        `;

        const footerHtml = `
            <button class="btn btn-secondary" onclick="window.LearnLoopApp.closeModal()">Cancel</button>
            <button class="btn btn-primary" onclick="window.LearnLoopApp.submitQuestion()">Submit Question</button>
        `;

        openModal('Ask a Doubt', bodyHtml, footerHtml);
    } catch (e) {
        showToast('Error preparing question form: ' + e.message, 'error');
    }
}

export async function submitQuestion() {
    const courseId = document.getElementById('question-course-select')?.value;
    const title = document.getElementById('question-title-input')?.value;
    const description = document.getElementById('question-desc-input')?.value;

    if (!courseId || !title || !description) {
        showToast('Please fill out all required fields', 'error');
        return;
    }

    try {
        const res = await ApiClient.post('/questions', { courseId, title, description });
        if (res && res.success) {
            closeModal();
            showToast('Question posted! Educators and peers will be notified.', 'success');
            await loadQuestions();
        }
    } catch (e) {
        showToast(e.message, 'error');
    }
}

export async function openQuestionThread(questionId) {
    try {
        const res = await ApiClient.get(`/questions/${questionId}`);
        if (!res || !res.success || !res.data) return;

        const q = res.data;
        const currentUserId = AuthState.getUser()?.id;
        const isAuthorOrTeacher = currentUserId === q.studentId || AuthState.isTeacher() || AuthState.isAdmin();

        const bodyHtml = `
            <div style="border-bottom: 1px solid var(--border-subtle); padding-bottom: 1.25rem; margin-bottom: 1.5rem;">
                <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 0.5rem;">
                    <span class="badge ${q.status === 'RESOLVED' ? 'badge-success' : (q.status === 'ANSWERED' ? 'badge-primary' : 'badge-warning')}">
                        ${q.status}
                    </span>
                    <span class="text-muted" style="font-size: 0.85rem;">${formatDate(q.createdAt)}</span>
                </div>
                <h2 style="font-size: 1.4rem; margin-bottom: 0.75rem;">${escapeHtml(q.title)}</h2>
                <p style="font-size: 1rem; line-height: 1.7; color: #e2e8f0; white-space: pre-wrap; margin-bottom: 1rem;">
                    ${escapeHtml(q.description)}
                </p>
                <div style="font-size: 0.85rem; color: var(--text-muted);">
                    Course: <strong>${escapeHtml(q.courseTitle)}</strong> • Asked by: <strong>${escapeHtml(q.studentName)}</strong>
                </div>

                ${isAuthorOrTeacher && q.status !== 'RESOLVED'
                    ? `<div style="margin-top: 1rem;">
                        <button class="btn btn-outline btn-sm" onclick="window.LearnLoopApp.resolveQuestion('${q.id}')">
                            Mark as Resolved ✓
                        </button>
                       </div>`
                    : ''
                }
            </div>

            <!-- Replies Stream -->
            <h3 style="font-size: 1.15rem; margin-bottom: 1rem;">Discussion Replies (${q.replies ? q.replies.length : 0})</h3>
            <div class="replies-container" id="replies-list-container">
                ${q.replies && q.replies.length > 0
                    ? q.replies.map(r => `
                        <div class="reply-bubble ${r.teacherReply ? 'instructor' : ''}">
                            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 0.5rem;">
                                <div style="display: flex; align-items: center; gap: 0.5rem;">
                                    <strong>${escapeHtml(r.userName)}</strong>
                                    ${r.teacherReply 
                                        ? `<span class="badge badge-success" style="font-size: 0.65rem;">Verified Instructor</span>`
                                        : `<span class="badge badge-draft" style="font-size: 0.65rem;">Student</span>`
                                    }
                                </div>
                                <span class="text-muted" style="font-size: 0.75rem;">${formatDate(r.createdAt)}</span>
                            </div>
                            <p style="font-size: 0.95rem; line-height: 1.6; color: #f1f5f9; white-space: pre-wrap;">
                                ${escapeHtml(r.content)}
                            </p>
                        </div>
                    `).join('')
                    : '<p class="text-muted" style="font-size: 0.9rem;">No replies yet. Be the first to post an explanation!</p>'
                }
            </div>

            <!-- Reply Box -->
            <div style="margin-top: 1.5rem; border-top: 1px solid var(--border-subtle); padding-top: 1.25rem;">
                <label class="form-label">Write your reply or answer:</label>
                <textarea id="reply-text-input" class="form-textarea" rows="3" placeholder="Provide a helpful explanation or answer..."></textarea>
                <div style="display: flex; justify-content: flex-end; margin-top: 0.75rem;">
                    <button class="btn btn-primary btn-sm" onclick="window.LearnLoopApp.submitReply('${q.id}')">
                        Post Reply
                    </button>
                </div>
            </div>
        `;

        openModal('Discussion Thread', bodyHtml, '<button class="btn btn-secondary" onclick="window.LearnLoopApp.closeModal()">Close</button>');
    } catch (e) {
        showToast('Error opening thread: ' + e.message, 'error');
    }
}

export async function submitReply(questionId) {
    const content = document.getElementById('reply-text-input')?.value;
    if (!content || !content.trim()) {
        showToast('Please enter a reply before submitting', 'error');
        return;
    }

    try {
        const res = await ApiClient.post(`/questions/${questionId}/replies`, { content: content.trim() });
        if (res && res.success) {
            showToast('Reply posted! Question author will receive a Kafka notification.', 'success');
            // Refresh thread modal
            await openQuestionThread(questionId);
            // Refresh list in background
            loadQuestions();
        }
    } catch (e) {
        showToast(e.message, 'error');
    }
}

export async function resolveQuestion(questionId) {
    try {
        const res = await ApiClient.patch(`/questions/${questionId}/status`, { status: 'RESOLVED' });
        if (res && res.success) {
            showToast('Doubt marked as resolved! Great teamwork.', 'success');
            await openQuestionThread(questionId);
            loadQuestions();
        }
    } catch (e) {
        showToast(e.message, 'error');
    }
}
