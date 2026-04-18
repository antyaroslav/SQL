package ru.netology.page;

import com.codeborne.selenide.SelenideElement;
import ru.netology.data.DataHelper;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class VerificationPage {
    private static final String ERROR_TITLE = "Ошибка";

    private final SelenideElement codeField = $("[data-test-id='code'] input");
    private final SelenideElement verifyButton = $("[data-test-id='action-verify']");
    private final SelenideElement errorNotificationTitle =
            $("[data-test-id='error-notification'] .notification__title");
    private final SelenideElement errorNotificationContent =
            $("[data-test-id='error-notification'] .notification__content");

    public VerificationPage() {
        codeField.shouldBe(visible);
    }

    public DashboardPage validVerify(DataHelper.VerificationCode verificationCode) {
        submitCode(verificationCode);
        return new DashboardPage();
    }

    public VerificationPage invalidVerify(DataHelper.VerificationCode verificationCode, String expectedMessage) {
        submitCode(verificationCode);
        errorNotificationTitle.shouldHave(exactText(ERROR_TITLE));
        errorNotificationContent.shouldHave(text(expectedMessage));
        return this;
    }

    private void submitCode(DataHelper.VerificationCode verificationCode) {
        codeField.setValue(verificationCode.getCode());
        verifyButton.click();
    }
}
