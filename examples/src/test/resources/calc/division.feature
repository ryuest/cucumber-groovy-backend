Feature: Live Score Data are propagated to Diffusion clients for football matches

  Scenario: Regular numbers
    Given I have entered 3 into the calculator
    And I have entered 2 into the calculator
    When I press divide
    Then the stored result should be 1.5

  @Football
  Scenario: Football Match
    Given A client subscribe for event, summary and incident topics of football match "Czech Republic vs San Marino" to Diffusion
    When Subscribed match is played

  @Tennis
  Scenario: Tennis Match
    Given A client subscribe for event, summary and incident topics of tennis match "Samantha-Murray-vs-Bunyawi-Thamchaiwat" to Diffusion
    When Subscribed match is played

  @Basketball
  Scenario: Basketball Match
    Given A client subscribe for event, summary and incident topics of basketball match "orleans-vs-rouen" to Diffusion
    When Subscribed match is played


