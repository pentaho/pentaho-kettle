@ui @local @no-repository @samples
Feature: Run a bundled sample transformation
  As an ETL developer
  I want to run the sample transformations that ship with PDI from Spoon
  So that I can see how PDI processes data without setting up any external systems

  Background:
    Given Spoon is started successfully in the automated test environment
    And no transformations or jobs are open

  @smoke
  Scenario: Run the "Generate Row - basics" sample with the default run options
    Given the sample transformation "Generate Row - basics" is open in its own tab
    When the user runs the transformation with the default run options
    Then the transformation finishes without errors
    And the "Generate Rows" step writes 10 rows
    And the "Dummy (do nothing)" step reads 10 rows
