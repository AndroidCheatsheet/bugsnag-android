Feature: Reports are ignored

Scenario: Exception classname ignored
    When I run "IgnoredExceptionScenario"
    Then I should receive no requests

Scenario: Disabled Exception Handler
    When I run "DisableAutoDetectErrorsScenario"
    Then I should receive no requests
