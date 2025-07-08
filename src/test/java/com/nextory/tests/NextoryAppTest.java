package com.nextory.tests;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.MobileBy;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.touch.WaitOptions;
import io.appium.java_client.touch.offset.ElementOption;
import io.appium.java_client.touch.offset.PointOption;
import io.appium.java_client.TouchAction;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NextoryAppTest {

    private static AndroidDriver driver;
    private static WebDriverWait wait;
    private static final int TIMEOUT = 30;
    static String expectedTitle = "Harry Potter and the Chamber of Secrets";


    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        // Use environment variable or fallback
        String sdkRoot = System.getenv("ANDROID_HOME");
        if (sdkRoot == null) {
            sdkRoot = System.getenv("ANDROID_SDK_ROOT");
        }
        if (sdkRoot == null) {
            sdkRoot = System.getProperty("user.home") + "/Library/Android/sdk"; // macOS default
        }

        File emulatorBinary = new File(sdkRoot, "emulator/emulator");

        if (!emulatorBinary.exists()) {
            throw new IllegalStateException("Emulator binary not found at: " + emulatorBinary.getAbsolutePath());
        }

        // Launch emulator
        Process emulatorProcess = Runtime.getRuntime().exec(new String[]{emulatorBinary.getAbsolutePath(), "-avd", "Pixel_9_Pro_XL"});

        // Wait for emulator to boot
        waitForEmulator();

        // Appium options
        UiAutomator2Options options = new UiAutomator2Options();
        options.setPlatformName("Android");
        options.setDeviceName("emulator-5554");
        options.setAppPackage("com.gtl.nextory");
        options.setAppActivity("fr.youboox.app.MainActivity");
        options.setNoReset(false);
        options.setFullReset(false);
        options.setNewCommandTimeout(Duration.ofSeconds(300));
        options.setAutoGrantPermissions(true);

        driver = new AndroidDriver(new URL("http://127.0.0.1:4723"), options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT));
    }

    public void swipe(int startX, int startY, int endX, int endY, int durationMs) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);

        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(durationMs), PointerInput.Origin.viewport(), endX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipe));
    }


    public void scrollToAndClick(AndroidDriver driver, String categoryText) {
        int maxSwipes = 5;
        boolean found = false;

        for (int i = 0; i < maxSwipes; i++) {
            try {
                System.out.println("🔍 Attempting to find category: " + categoryText);
                String pageSource = driver.getPageSource();

                if (pageSource.contains(categoryText)) {
                    // Try matching by text first
                    try {
                        WebElement element = driver.findElement(By.xpath("//*[contains(@text, '" + categoryText + "')]"));
                        element.click();
                        found = true;
                        break;
                    } catch (Exception e1) {
                        // Try by content-desc if Compose uses accessibility labels
                        WebElement alt = driver.findElement(By.xpath("//*[contains(@content-desc, '" + categoryText + "')]"));
                        alt.click();
                        found = true;
                        break;
                    }
                } else {
                    System.out.println("Category not found on screen, swiping...");
                    swipe(600, 2000, 600, 800, 500);
                }

            } catch (Exception e) {
                swipe(600, 2000, 600, 800, 500);
            }
        }

        if (!found) {
            throw new RuntimeException("❌ Could not find category: " + categoryText + " after " + maxSwipes + " swipes.");
        }
    }


    private static void waitForEmulator() throws IOException, InterruptedException {
        // Resolve adb from the SDK root
        String sdkRoot = System.getenv("ANDROID_HOME");
        if (sdkRoot == null) {
            sdkRoot = System.getenv("ANDROID_SDK_ROOT");
        }
        if (sdkRoot == null) {
            sdkRoot = System.getProperty("user.home") + "/Library/Android/sdk"; // macOS default
        }

        File adbBinary = new File(sdkRoot, "platform-tools/adb");

        boolean ready = false;
        int attempts = 20;

        while (!ready && attempts-- > 0) {
            Process check = Runtime.getRuntime().exec(new String[]{adbBinary.getAbsolutePath(), "devices"});
            Scanner s = new Scanner(check.getInputStream()).useDelimiter("\\A");
            String output = s.hasNext() ? s.next() : "";
            if (output.contains("emulator-5554") && output.contains("device")) {
                ready = true;
            } else {
                Thread.sleep(5000);
            }
        }

        if (!ready) {
            throw new RuntimeException("Emulator did not boot in time.");
        }
    }


    @Test
    @Order(1)
    public void testCompleteUserFlow() throws InterruptedException {
        Thread.sleep(3000); // Wait for screen to load

        // Click "Sign up"
        WebElement signUpButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//android.view.View[@resource-id='WelcomeScreen.SignupButton']")));
        signUpButton.click();
        //Fill name

        WebElement nameField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//android.widget.EditText")));
        nameField.sendKeys(generateRandomName());

        // Now wait for and click "Continue"
        By continueBtnXpath = By.xpath("//*[@resource-id='Signup.ProfileName.ContinueButton']");
        WebElement continueButton = wait.until(
                ExpectedConditions.presenceOfElementLocated(continueBtnXpath)
        );
        continueButton.click();


        // Wait for and fill email
        List<WebElement> fields = driver.findElements(By.className("android.widget.EditText"));
        fields.get(0).sendKeys(generateRandomEmail()); // Email
        fields.get(1).sendKeys("TestPassword123!"); // Password

// Wait until the continue button becomes enabled
        List<WebElement> views = driver.findElements(By.className("android.view.View"));
        for (WebElement el : views) {
            String rid = el.getAttribute("resource-id");
            String text = el.getText();
            String enabled = el.getAttribute("enabled");
            System.out.println("ID: " + rid + ", text: " + text + ", enabled: " + enabled);
        }
        WebElement continueButton1 = driver.findElements(By.className("android.view.View"))
                .stream()
                .filter(el -> "SignupScreen.ContinueButton".equals(el.getAttribute("resource-id")) && el.isEnabled())
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Continue button not found"));

        continueButton1.click();
        //Select spanish
        WebElement spanishContainer = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//android.widget.TextView[@text='Spanish']/parent::android.view.View")
                )
        );
        spanishContainer.click();
        //continue with spanish language selection
        WebElement LanguagecontinueButton = wait.until(driver ->
                driver.findElement(By.xpath("//*[contains(@text, 'Continue')]"))
        );
        LanguagecontinueButton.click();

        //select 5-12 books
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement bookChoiceButton = wait.until(driver ->
                driver.findElement(By.xpath("//*[contains(@text, '5-12 books')]"))
        );

        bookChoiceButton.click();

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement bookCountContinueButton = wait.until(driver ->
                driver.findElement(By.xpath("//*[@resource-id='Onboarding.ReadingHabit.ContinueButton.1']"))
        );

        bookCountContinueButton.click();

        //select Before I sleep
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement beforeISleepButton = wait.until(driver ->
                driver.findElement(By.xpath("//android.widget.TextView[@text='Before I sleep']"))
        );

        beforeISleepButton.click();
        // Step 2: Tap "Continue"
        WebElement selectHabitContinue = wait.until(driver ->
                driver.findElement(By.xpath("//android.widget.TextView[@text='Continue']"))
        );
        selectHabitContinue.click();

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

// Step 1: Tap "Chill and unwind"
        WebElement chillAndUnwindOption = wait.until(driver ->
                driver.findElement(By.xpath("//android.widget.TextView[@text='Chill and unwind']"))
        );
        chillAndUnwindOption.click();

// Step 2: Tap "Continue"
        WebElement SelectHabit2continueButton = wait.until(driver ->
                driver.findElement(By.xpath("//android.widget.TextView[@text='Continue']"))
        );
        SelectHabit2continueButton.click();
// Select 10 mins
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

// Step 1: Tap "10 min/day"
        WebElement option10Min = wait.until(driver ->
                driver.findElement(By.xpath("//android.widget.TextView[@text='10 min/day']"))
        );
        option10Min.click();

// Optional: wait a moment for the Continue button to become enabled
        Thread.sleep(500); // or use a custom wait for enabled if needed

// Step 2: Tap "Yes, set a daily habit"
        WebElement yesSetHabitButton = wait.until(driver ->
                driver.findElement(By.xpath("//android.widget.TextView[@text='Yes, set a daily habit']"))
        );
        yesSetHabitButton.click();
        //continue to select favorites
        WebElement SelectFavoritesContinueButton = wait.until(driver ->
                driver.findElement(By.xpath("//android.view.View[@resource-id='Onboarding.PickYourFavorites.ContinueButton' and @clickable='true']"))
        );
        SelectFavoritesContinueButton.click();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

// Swipe up
        swipe(600, 2000, 600, 800, 500);


// Try finding the text after swipe
        scrollToAndClick(driver, "Easy-to-read");
        scrollToAndClick(driver, "Sleep and relaxation");
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        swipe(600, 1800, 600, 1500, 300);
        //continue
        WebElement continueAfterCatagoryButton = wait.until(driver ->
                driver.findElement(By.xpath("//android.widget.TextView[@text='Continue']"))
        );
        continueAfterCatagoryButton.click();

        WebElement getStartedButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.id("onboarding.notifications.allow")
                )
        );
        getStartedButton.click();
        //Homescreen search
        // Click the Search tab first
        WebElement searchTab = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("TabBar.search"))
        );
        searchTab.click();

// Wait for the EditText to be clickable (check if it becomes enabled)
        WebElement searchInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("Search.button"))
        );

// Optional: Wait until it becomes enabled (loop or custom wait if needed)
        int retry = 0;
        while (!searchInput.isEnabled() && retry++ < 5) {
            Thread.sleep(1000);
            searchInput = driver.findElement(By.id("Search.button"));
        }

// Click and send text
        WebElement editText = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.className("android.widget.EditText"))
        );
        editText.click();
        editText.sendKeys("Harry Potter");

//find chamber of secrets and click
        scrollToAndClick(driver, "Chamber of Secrets");


        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement titleElement = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//android.widget.TextView[@resource-id='Bookcard.BookTitle']")
                )
        );

// Step 3: Verify title
        String actualTitle = titleElement.getText();

        if (expectedTitle.equals(actualTitle)) {
            System.out.println("✅ Title match: " + actualTitle);
        } else {
            System.out.println("❌ Title mismatch. Expected: '" + expectedTitle + "', Found: '" + actualTitle + "'");
        }
        WebElement listenButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//android.view.View[@resource-id='BookCard.ListenButton']")
                )
        );
        listenButton.click();
        System.out.println("🎧 Clicked Listen.");

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

// Wait for the Play button to be clickable and click it
        WebElement playButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.id("Player.PlayButton")
                )
        );
        playButton.click();

        System.out.println("▶️ Clicked the Play button.");
//play pause
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

// Locate and click the Pause button
        WebElement pauseButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.id("Player.PauseButton")
                )
        );

        pauseButton.click();
        System.out.println("⏸️ Pause button clicked.");
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

// Locate the parent View of the down arrow (clickable)
        WebElement downArrow = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//android.view.View[@resource-id='DownArrow_button']")
                )
        );

// Click it
        downArrow.click();
        System.out.println("🔽 Minimized the player.");

        //miniplayer verification
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

// Wait for the title to appear in mini-player
        WebElement miniTitle = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//android.widget.TextView[contains(@text, 'Harry Potter')]")
                )
        );

// Compare text
        actualTitle = miniTitle.getText();
        if (actualTitle.equals(expectedTitle)) {
            System.out.println("✅ Mini-player displays correct title: " + actualTitle);
        } else {
            System.out.println("❌ Title mismatch. Found: " + actualTitle);
        }
    }


    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private String generateRandomName() {
        String[] firstNames = {"John", "Jane", "Michael", "Sarah", "David", "Emma", "Robert", "Lisa"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis"};
        Random random = new Random();
        return firstNames[random.nextInt(firstNames.length)] + " " + lastNames[random.nextInt(lastNames.length)];
    }

    private String generateRandomEmail() {
        Random random = new Random();
        String randomString = "test" + random.nextInt(10000);
        return randomString + "@nextory.com";
    }
}
