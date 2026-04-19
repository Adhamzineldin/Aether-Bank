@echo off
REM Phase 7 Cleanup Script - Delete 22 redundant files

setlocal enabledelayedexpansion

echo.
echo ============================================
echo Phase 7 Cleanup - Delete 22 Redundant Files
echo ============================================
echo.

REM Navigate to support directory
cd /d "C:\Users\moham\IdeaProjects\Aether-Bank\backend\card-service\src\main\java\com\maayn\cardservice\service\support"

if errorlevel 1 (
    echo ERROR: Could not navigate to support directory
    exit /b 1
)

echo Deleting 21 files from support/ directory...
echo.

REM Delete files from support/
set files=AbstractCardCreator.java CardAccessService.java CardCreator.java CardCreatorFactory.java CardIssuanceValidator.java CreditCardCreator.java CreditCardPaymentFlow.java CvvValidator.java DebitCardCreator.java DebitCardPaymentFlow.java ExpiryDateValidator.java IbanValidator.java MerchantPaymentRequestDto.java MerchantPaymentService.java MerchantPaymentValidator.java MerchantVaultAccountService.java PaymentFlow.java PaymentFlowConfiguration.java PaymentFlowFactory.java PaymentFlowType.java TransactionGateway.java

for %%F in (%files%) do (
    if exist "%%F" (
        del /Q "%%F"
        echo [✓] Deleted: %%F
    ) else (
        echo [!] Not found: %%F
    )
)

echo.
echo ============================================
echo Remaining files in support/:
echo ============================================
dir /B *.java
echo.

REM Navigate to usecase directory
cd /d "C:\Users\moham\IdeaProjects\Aether-Bank\backend\card-service\src\main\java\com\maayn\cardservice\service\usecase"

if errorlevel 1 (
    echo ERROR: Could not navigate to usecase directory
    exit /b 1
)

echo.
echo Deleting 1 file from usecase/ directory...
echo.

if exist "CardIssuanceService.java" (
    del /Q "CardIssuanceService.java"
    echo [✓] Deleted: CardIssuanceService.java
) else (
    echo [!] Not found: CardIssuanceService.java
)

echo.
echo ============================================
echo Replacing CardPaymentService.java...
echo ============================================
echo.

if exist "CardPaymentService.java" (
    del /Q "CardPaymentService.java"
    echo [✓] Deleted old: CardPaymentService.java
) else (
    echo [!] Old file not found
)

if exist "CardPaymentService_NEW.java" (
    ren "CardPaymentService_NEW.java" "CardPaymentService.java"
    echo [✓] Renamed: CardPaymentService_NEW.java -> CardPaymentService.java
) else (
    echo [!] ERROR: CardPaymentService_NEW.java not found
    exit /b 1
)

echo.
echo ============================================
echo Phase 7 Cleanup Complete!
echo ============================================
echo.
echo Next step: Run: mvn clean compile
echo.

pause
