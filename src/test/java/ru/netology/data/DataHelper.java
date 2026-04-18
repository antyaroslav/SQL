package ru.netology.data;

import lombok.Value;

public final class DataHelper {
    private DataHelper() {
    }

    public static AuthInfo getValidAuthInfo() {
        return new AuthInfo("vasya", "qwerty123");
    }

    public static AuthInfo getBlockedAuthInfo() {
        return new AuthInfo("petya", "123qwerty");
    }

    public static AuthInfo getAuthInfoWithWrongPassword(AuthInfo authInfo) {
        return new AuthInfo(authInfo.getLogin(), "wrong-pass");
    }

    public static VerificationCode getVerificationCodeFor(AuthInfo authInfo) {
        return new VerificationCode(DbUtils.getVerificationCode(authInfo.getLogin()));
    }

    @Value
    public static class AuthInfo {
        String login;
        String password;
    }

    @Value
    public static class VerificationCode {
        String code;
    }
}