// LearnLoop Assignment & Submission Management View
import { ApiClient } from '../api.js';
import { AuthState } from '../auth.js';
import { escapeHtml, openModal, closeModal, showToast, formatDate, renderEmptyState } from '../ui.js';

let activeCourseFilter = null;

export async function renderAssignmentView(courseId = null) {
    activeCourseFilter = courseId;
    const isTeacher = AuthState.isTeacher();
    const main = document.getElementById('main-content');

    main.innerHTML = `
        <div class="page-container">
            <div style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 1rem; margin-bottom: 2rem;">
                <div>
                    <h1 style="font-size: 2rem; margin-bottom: 0.5rem;">${isTeacher ? 'Assignment & Grading Hub 📝' : 'Course Assignments 📝'}</h1>
                    <p class="text-muted">${isTeacher ? 'Design assignments, review student submissions, and release evaluative feedback.' : 'Complete coursework, submit solutions, and review instructor grading.'}</p>
                </div>
                <div>
                    ${isTeacher ? `<button class="btn btn-primary" onclick="window.LearnLoopApp.openCreateAssignmentModal()">+ Create Assignment</button>` : ''}
                </div>
            </div>

            <!-- Assignments List -->
            <div id="assignments-list-container">
                <div class="text-center" style="padding: 3rem;">
                    <p class="text-muted">Loading assignments...</p>
                </div>
            </div>
        </div>
    `;

    if (isTeacher) {
        await loadTeacherAssignments();
    } else {
        await loadStudentAssignments();
    }
}

async function loadStudentAssignments() {
    const container = document.getElementById('assignments-list-container');
    if (!container) return;

    try {
        const enrollmentsRes = await ApiClient.get('/students/me/enrollments', { size: 50 });
        const enrolledCourses = enrollmentsRes.data?.content || [];

        if (enrolledCourses.length === 0) {
            container.innerHTML = renderEmptyState(
                '📝',
                'No Assignments Available',
                'You must enroll in courses to see assigned coursework.',
                '<a href="#courses" class="btn btn-primary">Browse Courses</a>'
            );
            return;
        }

        // Fetch assignments for each enrolled course
        const allAssignments = [];
        for (const ec of enrolledCourses) {
            const res = await ApiClient.get(`/courses/${ec.courseId}/assignments`);
            if (res && res.success && res.data) {
                allAssignments.push(...res.data);
            }
        }

        if (allAssignments.length === 0) {
            container.innerHTML = renderEmptyState(
                '🎉',
                'All Caught Up!',
                'There are no pending assignments across your enrolled courses.'
            );
            return;
        }

        container.innerHTML = `
            <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 1.5rem;">
                ${allAssignments.map(a => {
                    const hasSub = a.submitted && a.mySubmission;
                    const isGraded = hasSub && a.mySubmission.status === 'GRADED';

                    return `
                        <div class="glass-card">
                            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.75rem;">
                                <span class="badge badge-primary">${escapeHtml(a.courseTitle)}</span>
                                <span class="badge ${isGraded ? 'badge-success' : (hasSub ? 'badge-warning' : 'badge-danger')}">
                                    ${isGraded ? `Graded: ${a.mySubmission.marksObtained} / ${a.maxMarks}` : (hasSub ? 'Submitted' : 'Pending Submission')}
                                </span>
                            </div>
                            <h3 style="font-size: 1.25rem; margin-bottom: 0.5rem;">${escapeHtml(a.title)}</h3>
                            <p class="text-muted" style="font-size: 0.9rem; margin-bottom: 1rem; line-height: 1.5;">${escapeHtml(a.instructions)}</p>
                            
                            <div style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 1.25rem;">
                                <div>📅 Due: <strong>${formatDate(a.dueDate)}</strong></div>
                                <div>🎯 Maximum Marks: <strong>${a.maxMarks}</strong></div>
                            </div>

                            ${isGraded ? `
                                <div style="background: rgba(16, 185, 129, 0.1); border-left: 3px solid var(--secondary); padding: 0.75rem 1rem; border-radius: var(--radius-sm); margin-bottom: 1.25rem;">
                                    <div style="font-size: 0.8rem; font-weight: 700; color: #34d399; margin-bottom: 0.25rem;">INSTRUCTOR FEEDBACK:</div>
                                    <p style="font-size: 0.85rem; color: #f1f5f9;">${escapeHtml(a.mySubmission.feedback || 'No written feedback provided.')}</p>
                                </div>
                            ` : ''}

                            <button class="btn ${hasSub ? 'btn-secondary' : 'btn-primary'}" style="width: 100%;" onclick="window.LearnLoopApp.openSubmitAssignmentModal('${a.id}', '${escapeHtml(a.title)}', '${a.maxMarks}', ${hasSub ? JSON.stringify(a.mySubmission).replace(/"/g, '&quot;') : 'null'})">
                                ${hasSub ? 'Edit / Resubmit Solution' : 'Submit Assignment →'}
                            </button>
                        </div>
                    `;
                }).join('')}
            </div>
        `;
    } catch (e) {
        container.innerHTML = renderEmptyState('⚠️', 'Error Loading Assignments', e.message);
    }
}

async function loadTeacherAssignments() {
    const container = document.getElementById('assignments-list-container');
    if (!container) return;

    try {
        const coursesRes = await ApiClient.get('/teacher/courses', { size: 50 });
        const courses = coursesRes.data?.content || [];

        if (courses.length === 0) {
            container.innerHTML = renderEmptyState(
                '📚',
                'No Courses Found',
                'Create and publish courses before creating assignments.',
                '<button class="btn btn-primary" onclick="window.LearnLoopApp.openCreateCourseModal()">Create Course</button>'
            );
            return;
        }

        const allAssignments = [];
        for (const c of courses) {
            const res = await ApiClient.get(`/courses/${c.id}/assignments`);
            if (res && res.success && res.data) {
                allAssignments.push(...res.data);
            }
        }

        if (allAssignments.length === 0) {
            container.innerHTML = renderEmptyState(
                '📝',
                'No Assignments Created Yet',
                'Assign coursework to your enrolled students to test their learning progress.',
                '<button class="btn btn-primary" onclick="window.LearnLoopApp.openCreateAssignmentModal()">+ Create Assignment</button>'
            );
            return;
        }

        container.innerHTML = `
            <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 1.5rem;">
                ${allAssignments.map(a => `
                    <div class="glass-card">
                        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.75rem;">
                            <span class="badge badge-primary">${escapeHtml(a.courseTitle)}</span>
                            <span class="badge badge-draft">Max: ${a.maxMarks} pts</span>
                        </div>
                        <h3 style="font-size: 1.25rem; margin-bottom: 0.5rem;">${escapeHtml(a.title)}</h3>
                        <p class="text-muted" style="font-size: 0.9rem; margin-bottom: 1rem; line-height: 1.5;">${escapeHtml(a.instructions)}</p>
                        <div style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 1.25rem;">
                            📅 Due Date: <strong>${formatDate(a.dueDate)}</strong>
                        </div>
                        <button class="btn btn-primary btn-sm" style="width: 100%;" onclick="window.LearnLoopApp.openTeacherSubmissionsModal('${a.id}', '${escapeHtml(a.title)}')">
                            📥 View & Grade Submissions
                        </button>
                    </div>
                `).join('')}
            </div>
        `;
    } catch (e) {
        container.innerHTML = renderEmptyState('⚠️', 'Error Loading Assignments', e.message);
    }
}

export async function openCreateAssignmentModal() {
    try {
        const coursesRes = await ApiClient.get('/teacher/courses', { size: 50 });
        const courses = coursesRes.data?.content || [];

        if (courses.length === 0) {
            showToast('You must author a course before creating assignments', 'warning');
            return;
        }

        const now = new Date();
        now.setDate(now.getDate() + 7);
        const defaultDue = now.toISOString().slice(0, 16);

        const bodyHtml = `
            <form id="create-assignment-form" onsubmit="event.preventDefault(); window.LearnLoopApp.submitCreateAssignment();">
                <div class="form-group">
                    <label class="form-label">Course *</label>
                    <select id="assignment-course-select" class="form-select" required>
                        ${courses.map(c => `
                            <option value="${c.id}" ${activeCourseFilter === c.id ? 'selected' : ''}>
                                ${escapeHtml(c.title)}
                            </option>
                        `).join('')}
                    </select>
                </div>
                <div class="form-group">
                    <label class="form-label">Assignment Title *</label>
                    <input type="text" id="assignment-title-input" class="form-input" required placeholder="e.g. Major Project Milestone 1">
                </div>
                <div class="form-group">
                    <label class="form-label">Maximum Marks (e.g. 100) *</label>
                    <input type="number" id="assignment-maxmarks-input" class="form-input" required value="100" min="1" max="1000">
                </div>
                <div class="form-group">
                    <label class="form-label">Due Date & Time *</label>
                    <input type="datetime-local" id="assignment-duedate-input" class="form-input" required value="${defaultDue}">
                </div>
                <div class="form-group">
                    <label class="form-label">Instructions & Rubric *</label>
                    <textarea id="assignment-instructions-input" class="form-textarea" rows="4" required placeholder="Describe task requirements, submission expectations, and grading criteria..."></textarea>
                </div>
            </form>
        `;

        const footerHtml = `
            <button class="btn btn-secondary" onclick="window.LearnLoopApp.closeModal()">Cancel</button>
            <button class="btn btn-primary" onclick="window.LearnLoopApp.submitCreateAssignment()">Publish Assignment</button>
        `;

        openModal('Create Course Assignment', bodyHtml, footerHtml);
    } catch (e) {
        showToast('Error opening form: ' + e.message, 'error');
    }
}

export async function submitCreateAssignment() {
    const courseId = document.getElementById('assignment-course-select')?.value;
    const title = document.getElementById('assignment-title-input')?.value;
    const maxMarks = document.getElementById('assignment-maxmarks-input')?.value;
    const dueDate = document.getElementById('assignment-duedate-input')?.value;
    const instructions = document.getElementById('assignment-instructions-input')?.value;

    if (!courseId || !title || !maxMarks || !dueDate || !instructions) {
        showToast('Please fill out all required fields', 'error');
        return;
    }

    try {
        const res = await ApiClient.post(`/teacher/courses/${courseId}/assignments`, {
            title,
            maxMarks: parseFloat(maxMarks),
            dueDate: new Date(dueDate).toISOString(),
            instructions
        });

        if (res && res.success) {
            closeModal();
            showToast('Assignment created! Students will see it in their coursework.', 'success');
            await loadTeacherAssignments();
        }
    } catch (e) {
        showToast(e.message, 'error');
    }
}

export function openSubmitAssignmentModal(assignmentId, title, maxMarks, existingSub = null) {
    const bodyHtml = `
        <form id="submit-assignment-form" onsubmit="event.preventDefault(); window.LearnLoopApp.submitAssignmentSolution('${assignmentId}');">
            <p class="text-muted" style="margin-bottom: 1rem; font-size: 0.9rem;">
                Assignment: <strong>${escapeHtml(title)}</strong> (Max Marks: ${maxMarks})
            </p>
            <div class="form-group">
                <label class="form-label">Your Solution / Code / Written Answers *</label>
                <textarea id="solution-text-input" class="form-textarea" rows="6" required placeholder="Type or paste your solution, explanation, or code here...">${existingSub ? escapeHtml(existingSub.textContent) : ''}</textarea>
            </div>
            <div class="form-group">
                <label class="form-label">External Resource Link (GitHub, Google Drive, Loom URL)</label>
                <input type="url" id="solution-url-input" class="form-input" placeholder="https://github.com/..." value="${existingSub && existingSub.resourceUrl ? escapeHtml(existingSub.resourceUrl) : ''}">
            </div>
        </form>
    `;

    const footerHtml = `
        <button class="btn btn-secondary" onclick="window.LearnLoopApp.closeModal()">Cancel</button>
        <button class="btn btn-success" onclick="window.LearnLoopApp.submitAssignmentSolution('${assignmentId}')">
            ${existingSub ? 'Update Submission' : 'Submit Assignment'}
        </button>
    `;

    openModal(existingSub ? 'Edit Assignment Submission' : 'Submit Assignment', bodyHtml, footerHtml);
}

export async function submitAssignmentSolution(assignmentId) {
    const textContent = document.getElementById('solution-text-input')?.value;
    const resourceUrl = document.getElementById('solution-url-input')?.value || null;

    if (!textContent || !textContent.trim()) {
        showToast('Please enter your solution text before submitting', 'error');
        return;
    }

    try {
        const res = await ApiClient.post(`/assignments/${assignmentId}/submissions`, {
            textContent: textContent.trim(),
            resourceUrl
        });

        if (res && res.success) {
            closeModal();
            showToast('Assignment submitted successfully!', 'success');
            await loadStudentAssignments();
        }
    } catch (e) {
        showToast(e.message, 'error');
    }
}

export async function openTeacherSubmissionsModal(assignmentId, assignmentTitle) {
    try {
        const res = await ApiClient.get(`/teacher/assignments/${assignmentId}/submissions`);
        if (!res || !res.success || !res.data) return;

        const submissions = res.data;
        const bodyHtml = `
            <p class="text-muted" style="margin-bottom: 1.25rem;">Submissions for: <strong>${escapeHtml(assignmentTitle)}</strong> (${submissions.length} Total)</p>
            ${submissions.length === 0
                ? '<p class="text-muted">No students have submitted solutions for this assignment yet.</p>'
                : `
                    <div class="data-table-wrapper">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Student</th>
                                    <th>Status</th>
                                    <th>Marks</th>
                                    <th>Submitted</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${submissions.map(s => `
                                    <tr>
                                        <td><strong>${escapeHtml(s.studentName)}</strong></td>
                                        <td>
                                            <span class="badge ${s.status === 'GRADED' ? 'badge-success' : 'badge-warning'}">
                                                ${s.status}
                                            </span>
                                        </td>
                                        <td>${s.marksObtained !== null ? `${s.marksObtained} / ${s.maxMarks}` : '-'}</td>
                                        <td>${formatDate(s.submittedAt)}</td>
                                        <td>
                                            <button class="btn btn-primary btn-sm" onclick="window.LearnLoopApp.openGradeSubmissionModal('${s.id}', '${escapeHtml(s.studentName)}', '${s.maxMarks}', ${s.marksObtained || 0}, '${escapeHtml(s.feedback || '')}', ${JSON.stringify(s.textContent).replace(/"/g, '&quot;')}, '${escapeHtml(s.resourceUrl || '')}')">
                                                Grade
                                            </button>
                                        </td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    </div>
                `
            }
        `;

        openModal('Student Submissions', bodyHtml, '<button class="btn btn-secondary" onclick="window.LearnLoopApp.closeModal()">Close</button>');
    } catch (e) {
        showToast('Error loading submissions: ' + e.message, 'error');
    }
}

export function openGradeSubmissionModal(submissionId, studentName, maxMarks, currentMarks, currentFeedback, textContent, resourceUrl) {
    const bodyHtml = `
        <div style="margin-bottom: 1.5rem;">
            <p class="text-muted" style="font-size: 0.9rem;">Student: <strong>${escapeHtml(studentName)}</strong> (Max Score: ${maxMarks})</p>
            
            <div style="margin: 1rem 0; padding: 1rem; background: rgba(0,0,0,0.25); border-radius: var(--radius-md); border: 1px solid var(--border-subtle); max-height: 200px; overflow-y: auto;">
                <div style="font-size: 0.8rem; font-weight: 700; color: #818cf8; margin-bottom: 0.35rem;">STUDENT SUBMISSION:</div>
                <p style="white-space: pre-wrap; font-size: 0.95rem; color: #f8fafc;">${escapeHtml(textContent)}</p>
                ${resourceUrl ? `<div style="margin-top: 0.75rem;"><a href="${escapeHtml(resourceUrl)}" target="_blank" rel="noopener" class="btn btn-outline btn-sm">🔗 Open External Resource Link</a></div>` : ''}
            </div>

            <form id="grade-submission-form" onsubmit="event.preventDefault(); window.LearnLoopApp.submitGrade('${submissionId}', ${maxMarks});">
                <div class="form-group">
                    <label class="form-label">Marks Awarded (Max ${maxMarks}) *</label>
                    <input type="number" step="0.5" id="grade-marks-input" class="form-input" required min="0" max="${maxMarks}" value="${currentMarks}">
                </div>
                <div class="form-group">
                    <label class="form-label">Written Feedback for Student</label>
                    <textarea id="grade-feedback-input" class="form-textarea" rows="3" placeholder="Explain what the student did well, areas for improvement, or grading rationale...">${escapeHtml(currentFeedback)}</textarea>
                </div>
            </form>
        </div>
    `;

    const footerHtml = `
        <button class="btn btn-secondary" onclick="window.LearnLoopApp.closeModal()">Cancel</button>
        <button class="btn btn-success" onclick="window.LearnLoopApp.submitGrade('${submissionId}', ${maxMarks})">Release Grade & Feedback</button>
    `;

    openModal(`Grade Submission - ${studentName}`, bodyHtml, footerHtml);
}

export async function submitGrade(submissionId, maxMarks) {
    const marksObtained = parseFloat(document.getElementById('grade-marks-input')?.value);
    const feedback = document.getElementById('grade-feedback-input')?.value;

    if (isNaN(marksObtained) || marksObtained < 0 || marksObtained > maxMarks) {
        showToast(`Please enter valid marks between 0 and ${maxMarks}`, 'error');
        return;
    }

    try {
        const res = await ApiClient.patch(`/teacher/submissions/${submissionId}/grade`, {
            marksObtained,
            feedback: feedback ? feedback.trim() : null
        });

        if (res && res.success) {
            closeModal();
            showToast('Grade released! Student notified via Kafka event pipeline.', 'success');
            await loadTeacherAssignments();
        }
    } catch (e) {
        showToast(e.message, 'error');
    }
}
