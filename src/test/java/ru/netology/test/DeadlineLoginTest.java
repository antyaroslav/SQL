package ru.netology.test;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.netology.data.DataHelper;
import ru.netology.data.DbUtils;
import ru.netology.page.LoginPage;
import ru.netology.page.VerificationPage;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DeadlineLoginTest {
    private static final String BASE_URL = System.getProperty("sut.url", System.getenv().getOrDefault("SUT_URL", "http://localhost:9999"));

    @BeforeAll
    static void configureUi() {
        Configuration.baseUrl = BASE_URL;
        Configuration.browserSize = "1920x1080";
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

        loginPage.invalidLogin(blockedUser);
        assertEquals("blocked", DbUtils.getUserStatus(blockedUser.getLogin()));
    }

    @Test
    @Disabled("Known SUT bug: the user is not blocked after three wrong password attempts in the current app-deadline.jar build")
    void shouldBlockUserAfterThreeWrongPasswordAttempts() {
        var validUser = DataHelper.getValidAuthInfo();
        var invalidPasswordUser = DataHelper.getAuthInfoWithWrongPassword(validUser);

        open(BASE_URL, LoginPage.class).invalidLogin(invalidPasswordUser);
        open(BASE_URL, LoginPage.class).invalidLogin(invalidPasswordUser);
        open(BASE_URL, LoginPage.class).invalidLogin(invalidPasswordUser);

        assertEquals("blocked", DbUtils.getUserStatus(validUser.getLogin()));
        open(BASE_URL, LoginPage.class).invalidLogin(validUser);
    }
}