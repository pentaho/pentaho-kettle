@ui @local @no-repository
Feature: <Capability, in the user's language>
  As a <role>
  I want <capability>
  So that <benefit>

  Background:
    Given Spoon is started successfully in the automated test environment
    And <shared starting state, e.g. no transformations or jobs are open>

  @smoke
  Scenario: <One behavior, stated as an outcome>
    When the user <single declarative action>
    Then <observable outcome>
    And <observable outcome>

  @regression
  Scenario Outline: <Same behavior with varying data>
    Given <scenario-specific state using <parameter>>
    When the user <action using <parameter>>
    Then <outcome using <parameter>>

    Examples:
      | parameter | other parameter |
      | value 1   | value A         |
      | value 2   | value B         |
