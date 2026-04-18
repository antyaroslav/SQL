package ru.netology.test;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.netology.data.DataHelper;
import ru.netology.data.DbUtils;
import ru.netology.page.LoginPage;
import ru.netology.page.VerificationPage;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DeadlineLoginTest {
    private static final String BASE_URL = System.getProperty("sut.url", System.getenv().getOrDefault("SUT_URL", "http://localhost:9999"));
    private static final String INVALID_CREDENTIALS_MESSAGE = "Неверно указан логин или пароль";
    private static final String BLOCKED_USER_MESSAGE = "Пользователь заблокирован";
    private static final String INVALID_CODE_MESSAGE = "Неверно указан код";

    @BeforeAll
    static void configureUi() {
        Configuration.baseUrl = BASE_URL;
        Configuration.browserSize = "1920x1080";
    }

    @AfterAll
    static void cleanDb() {
        DbUtils.cleanDatabase();
    }

    @BeforeEach
    void resetDb() {
        DbUtils.resetDemoState();
    }

    @Test
    void shouldLoginWithValidCredentialsAndVerificationCode() {
        var authInfo = DataHelper.getValidAuthInfo();
        var loginPage = open(BASE_URL, LoginPage.class);

        VerificationPage verificationPage = loginPage.validLogin(authInfo);
        var verificationCode = DataHelper.getVerificationCodeFor(authInfo);
        verificationPage.validVerify(verificationCode);
    }

    @Test
    void shouldNotLoginBlockedUser() {
        var blockedUser = DataHelper.getBlockedAuthInfo();
        var loginPage = open(BASE_URL, LoginPage.class);

        loginPage.invalidLogin(blockedUser, BLOCKED_USER_MESSAGE);
        assertEquals("blocked", DbUtils.getUserStatus(blockedUser.getLogin()));
    }

    @Test
    void shouldBlockUserAfterThreeWrongPasswordAttempts() {
        var validUser = DataHelper.getValidAuthInfo();
        var invalidPasswordUser = DataHelper.getAuthInfoWithWrongPassword(validUser);

        open(BASE_URL, LoginPage.class).invalidLogin(invalidPasswordUser, INVALID_CREDENTIALS_MESSAGE);
        open(BASE_URL, LoginPage.class).invalidLogin(invalidPasswordUser, INVALID_CREDENTIALS_MESSAGE);
        open(BASE_URL, LoginPage.class).invalidLogin(invalidPasswordUser, INVALID_CREDENTIALS_MESSAGE);

        assertEquals("blocked", DbUtils.getUserStatus(validUser.getLogin()));
        open(BASE_URL, LoginPage.class).invalidLogin(validUser, BLOCKED_USER_MESSAGE);
    }

    @Test
    void shouldNotVerifyWithInvalidCode() {
        var authInfo = DataHelper.getValidAuthInfo();
        var loginPage = open(BASE_URL, LoginPage.class);

        VerificationPage verificationPage = loginPage.validLogin(authInfo);
        verificationPage.invalidVerify(new DataHelper.VerificationCode("000000"), INVALID_CODE_MESSAGE);
    }
}