package ru.netology.page;

import com.codeborne.selenide.SelenideElement;
import ru.netology.data.DataHelper;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class LoginPage {
    private static final String ERROR_TITLE = "Ошибка";

    private final SelenideElement loginField = $("[data-test-id='login'] input");
    private final SelenideElement passwordField = $("[data-test-id='password'] input");
    private final SelenideElement loginButton = $("[data-test-id='action-login']");
    private final SelenideElement errorNotificationTitle = $("[data-test-id='error-notification'] .notification__title");
    private final SelenideElement errorNotificationContent = $("[data-test-id='error-notification'] .notification__content");

    public LoginPage() {
        loginField.shouldBe(visible);
    }

    public VerificationPage validLogin(DataHelper.AuthInfo authInfo) {
        login(authInfo);
        return new VerificationPage();
    }

    public LoginPage invalidLogin(DataHelper.AuthInfo authInfo, String expectedMessage) {
        login(authInfo);
        errorNotificationTitle.shouldHave(exactText(ERROR_TITLE));
        errorNotificationContent.shouldHave(exactText(expectedMessage));
        return this;
    }

    private void login(DataHelper.AuthInfo authInfo) {
        loginField.setValue(authInfo.getLogin());
        passwordField.setValue(authInfo.getPassword());
        loginButton.click();
    }
}