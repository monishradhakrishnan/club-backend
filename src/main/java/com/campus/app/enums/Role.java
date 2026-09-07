package com.campus.app.enums;

public enum Role {
    ADMIN,    // full system control
    STAFF,    // club coordinator / faculty advisor - approves budgets, generates OTP
    STUDENT   // club member - applies for budget, marks attendance via OTP
}
