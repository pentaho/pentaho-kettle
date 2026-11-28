@ui @local @no-repository
Feature: Editor tabs for multiple open transformations and jobs
  As a data integration developer
  I want each open transformation and job in its own editor tab
  So that I can work on several files and switch between them

  Background:
    Given Spoon is started successfully in the automated test environment
    And no transformations or jobs are open

  @smoke
  Scenario: Each new transformation and job opens in its own tab
    When the user creates 3 transformations and 2 jobs from the File -> New menu
    Then 3 transformations and 2 jobs are open
    And each new transformation and job has its own tab labeled with its name

  @smoke
  Scenario Outline: Selecting a tab makes its file the active one
    Given a new <first file> and then a new <second file> are open
    When the user selects the tab of the <first file>
    Then the tab of the <first file> is the selected tab
    And the <first file> is the active file

    Examples:
      | first file     | second file    |
      | transformation | job            |
      | job            | transformation |
