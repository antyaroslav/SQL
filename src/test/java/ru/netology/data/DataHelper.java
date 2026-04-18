package ru.netology.data;

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

    public static final class AuthInfo {
        private final String login;
        private final String password;

        public AuthInfo(String login, String password) {
            this.login = login;
            this.password = password;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }
    }

    public static final class VerificationCode {
        private final String code;

        public VerificationCode(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
}
