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

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DeadlineLoginTest {
    private static final String BASE_URL =
            System.getProperty("sut.url", System.getenv().getOrDefault("SUT_URL", "http://localhost:9999"));
    private static final String INVALID_CREDENTIALS_MESSAGE = "Неверно указан логин или пароль";
    private static final String BLOCKED_USER_MESSAGE = "Пользователь заблокирован";
    private static final String INVALID_CODE_MESSAGE = "Неверно указан код";
    private static Process sutProcess;

    @BeforeAll
    static void configureUi() throws Exception {
        Configuration.baseUrl = BASE_URL;
        Configuration.browserSize = "1920x1080";
        startSutIfNeeded();
    }

    @AfterAll
    static void cleanUp() {
        stopOwnedSut();
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
        var invalidCode = DataHelper.getInvalidVerificationCodeFor(authInfo);
        verificationPage.invalidVerify(invalidCode, INVALID_CODE_MESSAGE);
    }

    private static void startSutIfNeeded() throws Exception {
        if (isAppAvailable()) {
            return;
        }

        sutProcess = new ProcessBuilder("java", "-jar", "artifacts\\app-deadline.jar")
                .directory(new File("Q:\\neto\\SQL\\SQL"))
                .redirectErrorStream(true)
                .start();
        sutProcess.getOutputStream().close();
        waitUntilAppIsAvailable(Duration.ofSeconds(30));
    }

    private static boolean isAppAvailable() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(BASE_URL).openConnection();
            connection.setConnectTimeout(1500);
            connection.setReadTimeout(1500);
            connection.setRequestMethod("GET");
            int status = connection.getResponseCode();
            connection.disconnect();
            return status >= 200 && status < 500;
        } catch (IOException e) {
            return false;
        }
    }

    private static void waitUntilAppIsAvailable(Duration timeout) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            if (isAppAvailable()) {
                return;
            }
            if (sutProcess != null && !sutProcess.isAlive()) {
                throw new IllegalStateException("SUT process terminated before becoming available");
            }
            Thread.sleep(1000);
        }
        throw new IllegalStateException("SUT did not become available on " + BASE_URL);
    }

    private static void stopOwnedSut() {
        if (sutProcess == null) {
            return;
        }
        sutProcess.destroy();
        try {
            sutProcess.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
