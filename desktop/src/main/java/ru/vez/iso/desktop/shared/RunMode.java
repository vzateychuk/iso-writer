package ru.vez.iso.desktop.shared;

/**
 * This value indicates which mode the application is running
 * */
public enum RunMode {
    PROD,   // Full functionality: alerts dialogs, full network ops
    DEV,    // No confirmations dialogs, full network ops
    NOOP    // No Network communications. Synthetic data providers
}
