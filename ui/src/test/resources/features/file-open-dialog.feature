@ui @local @no-repository @file-open-save
Feature: Open files with the File Open dialog
  As an ETL developer
  I want to find and open transformations and jobs from the File Open dialog
  So that I can work on files stored on my computer

  Background:
    Given Spoon is started successfully in the automated test environment
    And no transformations or jobs are open
    And the Open dialog is shown from the File menu

  @smoke
  Scenario Outline: Open a sample <file type> by entering its path
    Given the user has entered the path of the sample "<sample file>"
    And "<file name>" is selected in the file list
    When the user clicks Open
    Then the Open dialog closes
    And "<name>" is open as the active <file type>

    Examples:
      | file type      | sample file                                          | file name                 | name                  |
      | transformation | transformations/Generate Row - basics.ktr            | Generate Row - basics.ktr | Generate Row - basics |
      | job            | jobs/parallel synchronized jobs/SyncJobs Example1.kjb | SyncJobs Example1.kjb     | SyncJobs Example1     |

  @smoke
  Scenario: Open a transformation by double-clicking it in the file list
    Given the user has entered the path of the sample folder "transformations"
    And "Generate Row - basics.ktr" is listed in the file list
    When the user double-clicks "Generate Row - basics.ktr" in the file list
    Then the Open dialog closes
    And "Generate Row - basics" is open as the active transformation

  @smoke
  Scenario: Cancelling the Open dialog opens nothing
    Given the user has entered the path of the sample "transformations/Generate Row - basics.ktr"
    When the user clicks Cancel
    Then the Open dialog closes
    And no transformations or jobs are open
