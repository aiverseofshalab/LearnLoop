// LearnLoop Notifications Drawer & Polling Manager
import { ApiClient } from '../api.js';
import { AuthState } from '../auth.js';
import { escapeHtml, showToast, formatDate, renderEmptyState } from '../ui.js';

let pollingInterval = null;

export const NotificationManager = {
    startPolling() {
        this.stopPolling();
        this.fetchUnreadCount();
        pollingInterval = setInterval(() => {
            if (AuthState.isAuthenticated()) {
                this.fetchUnreadCount();
            }
        }, 15000);
    },

    stopPolling() {
        if (pollingInterval) {
            clearInterval(pollingInterval);
            pollingInterval = null;
        }
    },

    async fetchUnreadCount() {
        if (!AuthState.isAuthenticated()) return;
        try {
            const res = await ApiClient.get('/notifications/unread-count');
            if (res && res.success && res.data) {
                const count = res.data.unreadCount;
                const badge = document.getElementById('topbar-unread-badge');
                if (badge) {
                    if (count > 0) {
                        badge.textContent = count > 99 ? '99+' : count;
                        badge.style.display = 'flex';
                    } else {
                        badge.style.display = 'none';
                    }
                }
            }
        } catch (e) {
            // Background polling error silent handling
        }
    },

    async toggleDrawer() {
        const drawer = document.getElementById('notifications-drawer');
        if (!drawer) return;

        drawer.classList.toggle('active');
        if (drawer.classList.contains('active')) {
            await this.loadNotificationsList();
        }
    },

    closeDrawer() {
        const drawer = document.getElementById('notifications-drawer');
        if (drawer) {
            drawer.classList.remove('active');
        }
    },

    async loadNotificationsList() {
        const listContainer = document.getElementById('drawer-notifications-list');
        if (!listContainer) return;

        try {
            const res = await ApiClient.get('/notifications', { page: 0, size: 30 });
            if (!res || !res.success || !res.data || res.data.content.length === 0) {
                listContainer.innerHTML = renderEmptyState('🔔', 'No Notifications', 'You have no new or recent notifications.');
                return;
            }

            listContainer.innerHTML = res.data.content.map(n => `
                <div class="notification-item ${n.read ? '' : 'unread'}" onclick="window.LearnLoopApp.handleNotificationClick('${n.id}', '${n.referenceType}', '${n.referenceId}')">
                    <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 0.35rem;">
                        <span class="badge ${n.type === 'ENROLLMENT' ? 'badge-primary' : (n.type === 'ASSIGNMENT_GRADED' ? 'badge-success' : 'badge-warning')}" style="font-size: 0.65rem;">
                            ${n.type}
                        </span>
                        <span class="text-muted" style="font-size: 0.75rem;">${formatDate(n.createdAt)}</span>
                    </div>
                    <h4 style="font-size: 0.95rem; margin-bottom: 0.25rem; color: var(--text-main);">${escapeHtml(n.title)}</h4>
                    <p style="font-size: 0.85rem; color: var(--text-muted); line-height: 1.4;">${escapeHtml(n.message)}</p>
                </div>
            `).join('');

            // Also refresh unread count
            await this.fetchUnreadCount();
        } catch (e) {
            listContainer.innerHTML = renderEmptyState('⚠️', 'Error', e.message);
        }
    },

    async markAllRead() {
        try {
            const res = await ApiClient.post('/notifications/read-all');
            if (res && res.success) {
                showToast('All notifications marked as read', 'success');
                await this.loadNotificationsList();
            }
        } catch (e) {
            showToast(e.message, 'error');
        }
    },

    async handleNotificationClick(id, refType, refId) {
        try {
            await ApiClient.patch(`/notifications/${id}/read`);
        } catch (e) {
            // Ignore if already marked
        }

        this.closeDrawer();
        this.fetchUnreadCount();

        // Navigate to target resource
        if (refType === 'COURSE' && refId) {
            window.LearnLoopApp.openLessonViewer(refId);
        } else if (refType === 'QUESTION' && refId) {
            window.location.hash = '#discussions';
            setTimeout(() => window.LearnLoopApp.openQuestionThread(refId), 300);
        } else if (refType === 'ASSIGNMENT' && refId) {
            window.location.hash = '#assignments';
        }
    }
};
