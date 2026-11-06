Feature: Create a new local transformation from the menu
  @smoke @ui @local @no-repository
  Scenario: User opens a new transformation from File -> New -> Transformation
    Given Spoon is started successfully in the automated test environment
    And the desktop shell is available for menu interaction
    When the user selects File -> New -> Transformation
    Then a new transformation editor tab is opened
    And the transformation editor is visible and ready for editing
    And no repository connection error is shown
