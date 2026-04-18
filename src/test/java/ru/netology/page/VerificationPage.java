package ru.netology.page;

import com.codeborne.selenide.SelenideElement;
import ru.netology.data.DataHelper;

import java.util.function.Supplier;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class VerificationPage {
    private static final String ERROR_TITLE = "Ошибка";

    private final SelenideElement codeField = $("[data-test-id='code'] input");
    private final SelenideElement verifyButton = $("[data-test-id='action-verify']");
    private final SelenideElement errorNotificationTitle = $("[data-test-id='error-notification'] .notification__title");
    private final SelenideElement errorNotificationContent = $("[data-test-id='error-notification'] .notification__content");

    public VerificationPage() {
        codeField.shouldBe(visible);
    }

    public DashboardPage validVerify(DataHelper.VerificationCode verificationCode) {
        return submitCode(verificationCode, DashboardPage::new);
    }

    public VerificationPage invalidVerify(DataHelper.VerificationCode verificationCode, String expectedMessage) {
        return submitCode(verificationCode, () -> {
            errorNotificationTitle.shouldHave(exactText(ERROR_TITLE));
            errorNotificationContent.shouldHave(exactText(expectedMessage));
            return this;
        });
    }

    private <T> T submitCode(DataHelper.VerificationCode verificationCode, Supplier<T> resultSupplier) {
        codeField.setValue(verificationCode.getCode());
        verifyButton.click();
        return resultSupplier.get();
    }
}