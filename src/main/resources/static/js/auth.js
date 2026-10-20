// LearnLoop Authentication State Manager
import { ApiClient } from './api.js';

let currentUser = null;

export const AuthState = {
    getUser() {
        return currentUser;
    },

    setUser(user) {
        currentUser = user;
        window.dispatchEvent(new CustomEvent('learnloop:auth-change', { detail: user }));
    },

    isAuthenticated() {
        return currentUser !== null;
    },

    getRole() {
        return currentUser ? currentUser.role : null;
    },

    isStudent() {
        return currentUser && currentUser.role === 'STUDENT';
    },

    isTeacher() {
        return currentUser && currentUser.role === 'TEACHER';
    },

    isAdmin() {
        return currentUser && currentUser.role === 'ADMIN';
    },

    async checkSession() {
        try {
            const res = await ApiClient.get('/auth/me');
            if (res && res.success && res.data) {
                this.setUser(res.data);
                return res.data;
            }
        } catch (e) {
            this.setUser(null);
            ApiClient.setToken(null);
        }
        return null;
    },

    async login(email, password) {
        const res = await ApiClient.post('/auth/login', { email, password });
        if (res && res.success && res.data) {
            ApiClient.setToken(res.data.token);
            // Fetch complete profile
            const profile = await this.checkSession();
            return profile;
        }
        throw new Error((res && res.message) || 'Login failed');
    },

    async register(email, password, fullName, role) {
        const res = await ApiClient.post('/auth/register', { email, password, fullName, role });
        if (res && res.success && res.data) {
            ApiClient.setToken(res.data.token);
            const profile = await this.checkSession();
            return profile;
        }
        throw new Error((res && res.message) || 'Registration failed');
    },

    async logout() {
        try {
            await ApiClient.post('/auth/logout');
        } catch (e) {
            // Ignore error on logout
        } finally {
            ApiClient.setToken(null);
            this.setUser(null);
        }
    }
};
