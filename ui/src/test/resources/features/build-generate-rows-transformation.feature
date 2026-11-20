@ui @local @no-repository @canvas
Feature: Build and run a Generate Rows transformation on the canvas
  As an ETL developer
  I want to build a transformation from the Design tab, connect its steps, and configure them in their dialogs
  So that I can create and run a working transformation without opening an existing file

  Background:
    Given Spoon is started successfully in the automated test environment
    And no transformations or jobs are open
    And a new transformation is open

  Scenario: Drag a step from a Design tab folder onto the canvas
    When the user drags "Generate rows" from the "Input" folder of the Design tab onto the canvas
    Then the transformation has 1 step
    And the transformation has a step named "Generate rows" drawn on the canvas

  Scenario: Filter the Design tab with the search box
    When the user types "dummy" in the Design tab search box
    Then the Design tab lists "Dummy (do nothing)" under "Flow"
    And the Design tab does not list "Generate rows"

  Scenario: Drag a search result onto the canvas
    Given the transformation has a "Generate rows" step
    And the user has typed "dummy" in the Design tab search box
    When the user drags "Dummy (do nothing)" from the search results onto the canvas
    Then the transformation has 2 steps
    And the transformation has a step named "Dummy (do nothing)" drawn on the canvas
    And the transformation has no hops

  Scenario: Connect two steps with a hop
    Given the transformation has a "Generate rows" step and a "Dummy (do nothing)" step
    When the user Shift-drags from "Generate rows" to "Dummy (do nothing)"
    Then the transformation has 1 hop
    And the hop goes from "Generate rows" to "Dummy (do nothing)" and is enabled

  Scenario: Configure the Generate Rows step in its dialog
    Given "Generate rows" is connected to "Dummy (do nothing)"
    When the user double-clicks "Generate rows", renames it to "Generate Rows", sets Limit to 10, adds these fields, and clicks OK:
      | Name             | Type      | Format     | Length | Precision | Decimal | Value      |
      | FieldAsNumber    | Number    |            |        |           | .       | 1.2345     |
      | FieldAsString    | String    |            | 20     |           |         | Test       |
      | FieldAsDate      | Date      | yyyy-MM-dd |        |           |         | 2006-07-27 |
      | FieldAsBoolean   | Boolean   |            |        |           |         | True       |
      | FieldAsInteger   | Integer   |            |        |           |         | 12345      |
      | FieldAsBigNumber | BigNumber |            | 10     | 5         | .       | 1.2345     |
    Then the step is named "Generate Rows" and its row limit is 10
    And the step has the 6 fields from the table, in order
    And the hop goes from "Generate Rows" to "Dummy (do nothing)"

  @smoke
  Scenario: Run the transformation built on the canvas
    Given the "Generate Rows" step is configured with the 6 fields and connected to "Dummy (do nothing)"
    And the user has saved the transformation as "build-generate-rows.ktr" in the home folder
    When the user runs the transformation with the default run options
    Then the transformation finishes without errors
    And the "Generate Rows" step writes 10 rows
    And the "Dummy (do nothing)" step reads 10 rows
    And the Logging tab shows "Dummy (do nothing).0 - Finished processing (I=0, O=0, R=10, W=10, U=0, E=0)"
