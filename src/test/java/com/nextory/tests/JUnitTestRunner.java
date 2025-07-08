package com.nextory.tests;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * JUnit 5 Test Runner for Nextory App Tests
 * This class can be used to run the tests programmatically
 */
public class JUnitTestRunner {

    public static void main(String[] args) {
        System.out.println("Starting Nextory App Test Execution with JUnit 5...");

        // Create launcher discovery request
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(NextoryAppTest.class))
                .build();

        // Create launcher
        Launcher launcher = LauncherFactory.create();

        // Register test execution listener
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);

        // Execute tests
        launcher.execute(request);

        // Print test execution summary
        TestExecutionSummary summary = listener.getSummary();

        System.out.println("\n" + "=".repeat(60));
        System.out.println("TEST EXECUTION SUMMARY");
        System.out.println("=".repeat(60));

        System.out.println("Tests started: " + summary.getTestsStartedCount());
        System.out.println("Tests succeeded: " + summary.getTestsSucceededCount());
        System.out.println("Tests failed: " + summary.getTestsFailedCount());
        System.out.println("Tests skipped: " + summary.getTestsSkippedCount());
        long durationMillis = summary.getTimeFinished() - summary.getTimeStarted();
        System.out.println("Total time: " + durationMillis + " ms");

        if (summary.getTestsFailedCount() > 0) {
            System.out.println("\nFAILED TESTS:");
            summary.getFailures().forEach(failure -> {
                System.out.println("- " + failure.getTestIdentifier().getDisplayName());
                System.out.println("  Reason: " + failure.getException().getMessage());
            });
        }

        System.out.println("=".repeat(60));

        // Exit with appropriate code
        System.exit(summary.getTestsFailedCount() > 0 ? 1 : 0);
    }
}