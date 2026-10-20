// LearnLoop UI Utilities: Toasts, Modals, Sanitization, Date Formatting

export function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    const div = document.createElement('div');
    div.textContent = String(str);
    return div.innerHTML;
}

export function formatDate(dateString) {
    if (!dateString) return '';
    const d = new Date(dateString);
    if (isNaN(d.getTime())) return dateString;
    return d.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

export function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'error') icon = '⚠️';

    toast.innerHTML = `<span>${icon}</span><span>${escapeHtml(message)}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

export function openModal(title, bodyHtml, footerHtml = '') {
    const overlay = document.getElementById('app-modal-overlay');
    const headerTitle = document.getElementById('app-modal-title');
    const body = document.getElementById('app-modal-body');
    const footer = document.getElementById('app-modal-footer');

    if (!overlay || !body) return;

    headerTitle.textContent = title;
    body.innerHTML = bodyHtml;
    footer.innerHTML = footerHtml;
    footer.style.display = footerHtml ? 'flex' : 'none';

    overlay.classList.add('active');
}

export function closeModal() {
    const overlay = document.getElementById('app-modal-overlay');
    if (overlay) {
        overlay.classList.remove('active');
    }
}

export function renderEmptyState(icon, title, message, actionHtml = '') {
    return `
        <div class="glass-card text-center" style="padding: 3.5rem 1.5rem; margin: 1.5rem 0;">
            <div style="font-size: 3rem; margin-bottom: 1rem;">${icon}</div>
            <h3 style="font-size: 1.35rem; margin-bottom: 0.5rem;">${escapeHtml(title)}</h3>
            <p class="text-muted" style="max-width: 450px; margin: 0 auto 1.5rem;">${escapeHtml(message)}</p>
            ${actionHtml}
        </div>
    `;
}
