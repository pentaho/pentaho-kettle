@ui @local @no-repository @kettle-home
Feature: Spoon keeps the user's settings under KETTLE_HOME
  As a user who sets KETTLE_HOME to keep PDI settings in one place
  I want everything Spoon saves for me to go under that folder
  So that my home folder is not used and the settings can be moved or shared as one folder

  Background:
    Given KETTLE_HOME and the user's home folder are different, empty folders
    And Spoon has been started with no repository connected

  @smoke
  Scenario: kettle.properties is created under KETTLE_HOME
    Then "kettle.properties" exists in the ".kettle" folder under KETTLE_HOME
    And the user's home folder has no ".kettle" folder

  Scenario: A new global database connection is saved under KETTLE_HOME
    When a global database connection named "swtbot-kettle-home-db" is saved
    Then a file under KETTLE_HOME mentions "swtbot-kettle-home-db"
    And no file in the user's home folder mentions "swtbot-kettle-home-db"

  @known-issue @PDI-18465 @PDI-19945
  Scenario: A new global run configuration is saved under KETTLE_HOME
    When a global run configuration named "swtbot-kettle-home-run" is saved
    Then a file under KETTLE_HOME mentions "swtbot-kettle-home-run"
    And no file in the user's home folder mentions "swtbot-kettle-home-run"
