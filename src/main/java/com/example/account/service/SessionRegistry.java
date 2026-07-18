package com.example.account.service;

import com.example.account.security.UserSession;

import java.util.Arrays;

public final class SessionRegistry {
    private UserSession[] sessions = new UserSession[8];
    private int size;

    public SessionRegistry() {
    }

    UserSession issueAdmin(String username) {
        return register(UserSession.admin(username));
    }

    UserSession issueCustomer(String username, String customerId) {
        return register(UserSession.customer(username, customerId));
    }

    boolean isActive(UserSession session) {
        if (session == null) {
            return false;
        }
        for (int index = 0; index < size; index++) {
            if (sessions[index].token().equals(session.token())) {
                return true;
            }
        }
        return false;
    }

    void invalidate(UserSession session) {
        if (session == null) {
            return;
        }
        for (int index = 0; index < size; index++) {
            if (sessions[index].token().equals(session.token())) {
                remove(index);
                return;
            }
        }
    }

    void invalidateCustomer(String customerId) {
        for (int index = 0; index < size; ) {
            if (customerId.equals(sessions[index].customerId())) {
                remove(index);
            } else {
                index++;
            }
        }
    }

    private UserSession register(UserSession session) {
        if (size == sessions.length) {
            sessions = Arrays.copyOf(sessions, sessions.length * 2);
        }
        sessions[size++] = session;
        return session;
    }

    private void remove(int index) {
        int elementsToMove = size - index - 1;
        if (elementsToMove > 0) {
            System.arraycopy(sessions, index + 1, sessions, index, elementsToMove);
        }
        sessions[--size] = null;
    }
}
