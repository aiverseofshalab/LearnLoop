// LearnLoop Administration & System Operations View
import { ApiClient } from '../api.js';
import { escapeHtml, openModal, closeModal, showToast, formatDate, renderEmptyState } from '../ui.js';

export async function renderAdminView() {
    const main = document.getElementById('main-content');

    main.innerHTML = `
        <div class="page-container">
            <div style="margin-bottom: 2rem;">
                <h1 style="font-size: 2rem; margin-bottom: 0.5rem;">Platform Administration 🛡️</h1>
                <p class="text-muted">Monitor platform activity, manage user accounts, inspect outbox pipelines, and maintain platform security.</p>
            </div>

            <!-- Operational Metrics Grid -->
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1.25rem; margin-bottom: 2.5rem;" id="admin-stats-grid">
                <div class="glass-card">
                    <p class="text-muted" style="font-size: 0.8rem; font-weight: 600;">TOTAL USERS</p>
                    <h2 style="font-size: 2rem; margin-top: 0.5rem;" id="admin-stat-users">-</h2>
                </div>
                <div class="glass-card">
                    <p class="text-muted" style="font-size: 0.8rem; font-weight: 600;">STUDENTS</p>
                    <h2 style="font-size: 2rem; margin-top: 0.5rem; color: #818cf8;" id="admin-stat-students">-</h2>
                </div>
                <div class="glass-card">
                    <p class="text-muted" style="font-size: 0.8rem; font-weight: 600;">INSTRUCTORS</p>
                    <h2 style="font-size: 2rem; margin-top: 0.5rem; color: #34d399;" id="admin-stat-teachers">-</h2>
                </div>
                <div class="glass-card">
                    <p class="text-muted" style="font-size: 0.8rem; font-weight: 600;">COURSES PUBLISHED</p>
                    <h2 style="font-size: 2rem; margin-top: 0.5rem; color: #fbbf24;" id="admin-stat-courses">-</h2>
                </div>
                <div class="glass-card">
                    <p class="text-muted" style="font-size: 0.8rem; font-weight: 600;">ENROLLMENTS</p>
                    <h2 style="font-size: 2rem; margin-top: 0.5rem;" id="admin-stat-enrollments">-</h2>
                </div>
                <div class="glass-card">
                    <p class="text-muted" style="font-size: 0.8rem; font-weight: 600;">OUTBOX BACKLOG</p>
                    <h2 style="font-size: 2rem; margin-top: 0.5rem; color: #a78bfa;" id="admin-stat-outbox">-</h2>
                </div>
            </div>

            <!-- User Management Card -->
            <div class="glass-card">
                <div style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 1rem; margin-bottom: 1.5rem;">
                    <div>
                        <h2 style="font-size: 1.35rem;">User Management</h2>
                        <p class="text-muted" style="font-size: 0.85rem;">Inspect accounts, assign roles, and activate or suspend access</p>
                    </div>
                    <div style="display: flex; gap: 0.75rem; flex-wrap: wrap;">
                        <input type="text" id="admin-user-search" class="form-input" placeholder="Search by name or email..." style="max-width: 250px;">
                        <select id="admin-role-filter" class="form-select" style="max-width: 140px;">
                            <option value="">All Roles</option>
                            <option value="STUDENT">Student</option>
                            <option value="TEACHER">Teacher</option>
                            <option value="ADMIN">Admin</option>
                        </select>
                        <select id="admin-status-filter" class="form-select" style="max-width: 140px;">
                            <option value="">All Statuses</option>
                            <option value="ACTIVE">Active</option>
                            <option value="SUSPENDED">Suspended</option>
                            <option value="INACTIVE">Inactive</option>
                        </select>
                    </div>
                </div>

                <div id="admin-users-table-container">
                    <div class="text-center" style="padding: 2.5rem;">
                        <p class="text-muted">Loading users...</p>
                    </div>
                </div>
            </div>
        </div>
    `;

    await loadAdminDashboard();

    const search = document.getElementById('admin-user-search');
    const roleFilter = document.getElementById('admin-role-filter');
    const statusFilter = document.getElementById('admin-status-filter');

    let debounce;
    search?.addEventListener('input', () => {
        clearTimeout(debounce);
        debounce = setTimeout(() => loadUsersTable(), 350);
    });
    roleFilter?.addEventListener('change', () => loadUsersTable());
    statusFilter?.addEventListener('change', () => loadUsersTable());
}

async function loadAdminDashboard() {
    try {
        const res = await ApiClient.get('/admin/analytics');
        if (res && res.success && res.data) {
            const data = res.data;
            document.getElementById('admin-stat-users').textContent = data.totalUsers;
            document.getElementById('admin-stat-students').textContent = data.totalStudents;
            document.getElementById('admin-stat-teachers').textContent = data.totalTeachers;
            document.getElementById('admin-stat-courses').textContent = `${data.publishedCourses} / ${data.totalCourses}`;
            document.getElementById('admin-stat-enrollments').textContent = data.totalEnrollments;
            document.getElementById('admin-stat-outbox').textContent = data.pendingOutboxEvents;
        }

        await loadUsersTable();
    } catch (e) {
        showToast('Error loading admin dashboard: ' + e.message, 'error');
    }
}

async function loadUsersTable() {
    const container = document.getElementById('admin-users-table-container');
    if (!container) return;

    const query = document.getElementById('admin-user-search')?.value || '';
    const role = document.getElementById('admin-role-filter')?.value || '';
    const status = document.getElementById('admin-status-filter')?.value || '';

    try {
        const params = { page: 0, size: 25 };
        if (query.trim()) params.query = query.trim();
        if (role) params.role = role;
        if (status) params.status = status;

        const res = await ApiClient.get('/admin/users', params);
        if (!res || !res.success || !res.data || res.data.content.length === 0) {
            container.innerHTML = renderEmptyState('👥', 'No Users Found', 'No accounts match the selected filters.');
            return;
        }

        container.innerHTML = `
            <div class="data-table-wrapper">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>User</th>
                            <th>Role</th>
                            <th>Status</th>
                            <th>Joined</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${res.data.content.map(u => `
                            <tr>
                                <td>
                                    <div><strong>${escapeHtml(u.fullName)}</strong></div>
                                    <div style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(u.email)}</div>
                                </td>
                                <td>
                                    <span class="badge ${u.role === 'ADMIN' ? 'badge-danger' : (u.role === 'TEACHER' ? 'badge-warning' : 'badge-primary')}">
                                        ${u.role}
                                    </span>
                                </td>
                                <td>
                                    <span class="badge ${u.status === 'ACTIVE' ? 'badge-success' : (u.status === 'SUSPENDED' ? 'badge-danger' : 'badge-draft')}">
                                        ${u.status}
                                    </span>
                                </td>
                                <td>${new Date(u.createdAt).toLocaleDateString()}</td>
                                <td>
                                    <div style="display: flex; gap: 0.5rem;">
                                        ${u.status === 'ACTIVE'
                                            ? `<button class="btn btn-danger btn-sm" onclick="window.LearnLoopApp.updateUserStatus('${u.id}', 'SUSPENDED')">Suspend</button>`
                                            : `<button class="btn btn-success btn-sm" onclick="window.LearnLoopApp.updateUserStatus('${u.id}', 'ACTIVE')">Activate</button>`
                                        }
                                        <button class="btn btn-secondary btn-sm" onclick="window.LearnLoopApp.openEditUserModal('${u.id}', '${escapeHtml(u.fullName)}', '${u.role}', '${u.status}')">
                                            Edit Role
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
    } catch (e) {
        container.innerHTML = renderEmptyState('⚠️', 'Error Loading Users', e.message);
    }
}

export async function updateUserStatus(userId, newStatus) {
    try {
        const res = await ApiClient.patch(`/admin/users/${userId}/status`, { status: newStatus });
        if (res && res.success) {
            showToast(`User status updated to ${newStatus}`, 'success');
            await loadUsersTable();
        }
    } catch (e) {
        showToast(e.message, 'error');
    }
}

export function openEditUserModal(userId, fullName, currentRole, currentStatus) {
    const bodyHtml = `
        <form id="edit-user-form" onsubmit="event.preventDefault(); window.LearnLoopApp.submitEditUser('${userId}');">
            <p class="text-muted" style="margin-bottom: 1.25rem;">Editing privileges for: <strong>${escapeHtml(fullName)}</strong></p>
            <div class="form-group">
                <label class="form-label">Role Assignment *</label>
                <select id="edit-user-role" class="form-select">
                    <option value="STUDENT" ${currentRole === 'STUDENT' ? 'selected' : ''}>STUDENT</option>
                    <option value="TEACHER" ${currentRole === 'TEACHER' ? 'selected' : ''}>TEACHER</option>
                    <option value="ADMIN" ${currentRole === 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                </select>
            </div>
            <div class="form-group">
                <label class="form-label">Account Status *</label>
                <select id="edit-user-status" class="form-select">
                    <option value="ACTIVE" ${currentStatus === 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                    <option value="INACTIVE" ${currentStatus === 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                    <option value="SUSPENDED" ${currentStatus === 'SUSPENDED' ? 'selected' : ''}>SUSPENDED</option>
                </select>
            </div>
        </form>
    `;

    const footerHtml = `
        <button class="btn btn-secondary" onclick="window.LearnLoopApp.closeModal()">Cancel</button>
        <button class="btn btn-primary" onclick="window.LearnLoopApp.submitEditUser('${userId}')">Save Changes</button>
    `;

    openModal('Edit User Permissions', bodyHtml, footerHtml);
}

export async function submitEditUser(userId) {
    const role = document.getElementById('edit-user-role')?.value;
    const status = document.getElementById('edit-user-status')?.value;

    try {
        const res = await ApiClient.patch(`/admin/users/${userId}/status`, { role, status });
        if (res && res.success) {
            closeModal();
            showToast('User permissions successfully updated!', 'success');
            await loadUsersTable();
            await loadAdminDashboard();
        }
    } catch (e) {
        showToast(e.message, 'error');
    }
}
