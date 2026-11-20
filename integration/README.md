# PDI Integration Tests

**WARNING:** The tests contained in this module need review and update. They were not being actively ran
on the ant version of the project so the best course of action seemed to be moving them into their own
module considering they required several parts of the project to execute.

## Docker integration tests

`docker-it/` is a separate Maven module. It runs KTR files in PDI containers, with Pan and Carte. It does not change
the tests in `src/it`. Run it with `-DrunDockerITs`, as described in [docker-it/README.md](docker-it/README.md).
`mvn verify -DrunITs` runs only the tests in `src/it`.