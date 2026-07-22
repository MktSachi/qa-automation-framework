# QA Automation Framework

An end-to-end, enterprise-style QA automation pipeline built entirely with free and open-source tools. This project demonstrates the complete lifecycle of test automation: designing test cases from requirements, implementing them with Selenium and the Page Object Model, executing them in a containerized environment through Jenkins, and reporting results across Allure, Jira (Zephyr Scale), and email.

## Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Setup Instructions](#setup-instructions)
- [Running Tests Locally](#running-tests-locally)
- [CI/CD Pipeline](#cicd-pipeline)
- [Test Reporting](#test-reporting)
- [Jira and Zephyr Scale Integration](#jira-and-zephyr-scale-integration)
- [Triggering Execution from Jira](#triggering-execution-from-jira)
- [Notifications](#notifications)
- [Known Limitations](#known-limitations)
- [Possible Future Improvements](#possible-future-improvements)

## Overview

This project automates login functionality testing for [saucedemo.com](https://www.saucedemo.com), a public demo application built for automation practice. While the application under test is simple, the surrounding pipeline reflects practices used in real enterprise QA environments:

- Test cases are defined from requirements before any code is written.
- Automation is implemented using the Page Object Model to keep code maintainable.
- Execution is containerized using Docker to eliminate environment inconsistencies.
- A Jenkins pipeline, defined as code, orchestrates every stage from source checkout to notification.
- Results are reported in three places: a technical report (Allure), a business-facing report (Jira/Zephyr Scale), and email notifications.
- Execution can be triggered either from Jenkins directly or from Jira, giving both technical and non-technical stakeholders a way to run the suite.

## Technology Stack

| Layer | Tool | Purpose |
|---|---|---|
| Language | Java 21 | Core implementation language |
| Browser automation | Selenium WebDriver 4.21.0 | Drives browser interactions |
| Test framework | TestNG | Test structure, assertions, execution |
| Build tool | Maven | Dependency management and build lifecycle |
| Containerization | Docker (Selenium Standalone Chrome image) | Isolated, reproducible browser environment |
| Version control | Git and GitHub | Source control |
| CI/CD | Jenkins (Pipeline as Code) | Build orchestration |
| Reporting | Allure Reports | Detailed technical test reporting |
| Test management | Jira with Zephyr Scale | Business-facing test case and execution tracking |
| Notifications | Gmail SMTP via Jenkins Email Extension Plugin | Build outcome notifications |
| Tunneling | ngrok | Exposes local Jenkins for Jira-triggered execution |

## Architecture

```
Jira (Test Cases, Trigger)
        |
        v
ngrok tunnel (public HTTPS endpoint)
        |
        v
Jenkins Pipeline
        |
        +--> Docker container (Selenium Standalone Chrome)
        |
        +--> Maven / TestNG test execution
        |
        +--> Allure report generation
        |
        +--> Zephyr Scale result upload (auto-created test cycle)
        |
        +--> Email notification (success / unstable / failure)
```

Jenkins can be triggered in two ways:

1. Directly, by a user or developer running the job from the Jenkins dashboard.
2. Remotely, by clicking a button in Jira, which sends an authenticated HTTP request through an ngrok tunnel to Jenkins' remote build trigger endpoint.

Both paths execute the identical pipeline defined in the `Jenkinsfile`.

## Project Structure

```
qa-automation-framework/
|
├── pom.xml
├── testng.xml
├── Jenkinsfile
├── docker-compose.yml
├── .gitignore
|
├── src/
│   ├── main/java/com/yourname/qaframework/
│   │   ├── base/
│   │   │   └── BaseTest.java
│   │   ├── pages/
│   │   │   ├── LoginPage.java
│   │   │   └── ProductsPage.java
│   │   └── utils/
│   │       └── ConfigReader.java
│   │
│   └── test/
│       ├── java/com/yourname/qaframework/tests/
│       │   └── LoginTest.java
│       └── resources/
│           ├── config.properties
│           └── allure.properties
|
└── target/   (build output, excluded from version control)
```

### Design Notes

- **`base/BaseTest.java`** centralizes browser setup and teardown so every test starts from a known, clean state.
- **`pages/`** contains the Page Object Model classes. Each class represents one screen of the application and exposes methods describing user actions (for example, `login()`), rather than exposing raw locators to test classes.
- **`utils/ConfigReader.java`** externalizes environment-specific values (base URL, browser, timeouts) so the same codebase can run against different environments without code changes.
- **`tests/LoginTest.java`** contains only assertions and calls into Page Object methods, keeping test logic readable and decoupled from implementation detail.

## Prerequisites

The following must be installed before setting up this project:

- Java JDK 21
- Apache Maven
- Git
- Docker Desktop
- Jenkins (running locally or on a server)
- Allure Commandline
- A Jira Cloud account with Zephyr Scale installed (for test management integration)
- ngrok (only required if triggering Jenkins from Jira)

## Setup Instructions

1. Clone the repository:
   ```
   git clone https://github.com/MktSachi/qa-automation-framework.git
   ```
2. Install dependencies by running:
   ```
   mvn clean install
   ```
3. Confirm Docker Desktop is running.
4. Start the Selenium Chrome container:
   ```
   docker compose up -d
   ```
5. Run the test suite locally to confirm the setup (see below).

## Running Tests Locally

With the Docker container running:

```
mvn clean test
```

This command compiles the project and executes the TestNG suite defined in `testng.xml`, using the containerized Chrome instance as the execution target.

Test results are written to `target/surefire-reports/`. Allure result data is written to `allure-results/` at the project root.

To view an Allure report locally:

```
allure serve allure-results
```

## CI/CD Pipeline

The Jenkins pipeline is defined entirely in the `Jenkinsfile` at the project root, following the Pipeline as Code approach. This means the build definition is version-controlled alongside the application code rather than configured manually inside Jenkins' UI.

### Pipeline Stages

1. **Checkout** — pulls the latest code from GitHub.
2. **Start Selenium Container** — starts the Dockerized Chrome environment via Docker Compose and waits for it to become ready.
3. **Build** — compiles the project with Maven.
4. **Clean Previous Results** — clears any stale Allure result data from previous runs.
5. **Create Zephyr Test Cycle** — creates a new, uniquely named Test Cycle in Zephyr Scale for this specific build.
6. **Test** — executes the TestNG suite. Test assertion failures do not halt the pipeline; they are recorded and reported.
7. **Fix Zephyr Key Format** — adjusts generated JUnit XML so test names match Jira's key format before upload.
8. **Push Results to Zephyr** — uploads the JUnit-format results to the Test Cycle created in step 5.
9. **Report** — publishes JUnit-format results within Jenkins.
10. **Allure Report** — generates and publishes the interactive Allure report for this build.

### Build Result Philosophy

The Jenkins build result (Success, Unstable, Failure) is intentionally kept distinct from individual test outcomes:

- **Success** — the pipeline and all test cases completed without issue.
- **Unstable** — the pipeline itself ran correctly, but one or more test assertions failed. This is a normal, expected outcome and does not indicate a broken pipeline.
- **Failure** — the pipeline itself broke (for example, a failed checkout, a compilation error, or a container that failed to start). This indicates an infrastructure or environment problem, not a test result.

Detailed pass/fail information for individual test cases is always available in the Allure report and in the corresponding Zephyr Scale Test Cycle, regardless of the overall build result.

## Test Reporting

Two separate reporting surfaces are used, each serving a different audience:

- **Allure Report** — a detailed, technical report generated on every pipeline run, showing execution timelines, step-level detail, and failure stack traces. Intended for engineers.
- **Zephyr Scale Test Cycle** — a business-facing view inside Jira showing which test cases passed or failed for a given run, intended for QA leads, product managers, and other non-technical stakeholders.

## Jira and Zephyr Scale Integration

Test cases are maintained in Jira using the Zephyr Scale test management add-on, serving as the single source of truth for what the application is expected to do. Each Zephyr test case has a unique key (for example, `QAF-T1`), which corresponds to a matching automated test method in the codebase.

On every pipeline run:

1. A new Test Cycle is created automatically in Zephyr Scale, named using the Jenkins build number.
2. Tests execute against the application.
3. Results are uploaded to the newly created cycle, updating each test case's execution status automatically.

This removes the need for manual test cycle management and ensures every pipeline run has its own distinct, timestamped execution record in Jira.

## Triggering Execution from Jira

In addition to running the pipeline directly from Jenkins, the pipeline can be triggered from within Jira itself, using Jira's built-in Automation feature.

### How it works

1. Jenkins exposes a token-protected remote build trigger endpoint.
2. Since Jira Cloud cannot reach a locally hosted Jenkins instance directly, ngrok is used to expose Jenkins over a temporary public HTTPS URL.
3. A Jira Automation rule, configured with a manual trigger, sends an authenticated HTTP request to this endpoint when a user clicks the associated button on a Jira issue.
4. Jenkins authenticates the request using an API token passed through an HTTP Basic Authorization header, which also satisfies Jenkins' CSRF protection requirements without needing to disable them.

This allows a QA lead or another team member to initiate a full regression run without needing direct access to Jenkins, while direct access to Jenkins remains fully available for anyone who prefers it.

## Notifications

On completion of each pipeline run, an email is sent summarizing the outcome:

- **Success** — all tests passed, with a link to the Allure report.
- **Unstable** — the pipeline completed, but one or more tests failed, with a link to the Allure report for detail.
- **Failure** — the pipeline itself failed, with a link to the Jenkins console output for troubleshooting.

Email delivery uses Gmail SMTP, authenticated through the Jenkins Email Extension Plugin using an application-specific password and an explicitly configured authentication mechanism.

## Known Limitations

This project intentionally uses free-tier and local infrastructure, which introduces a few limitations worth noting:

- Jenkins runs locally rather than on a persistent server, so it is only reachable while the host machine is on and Jenkins is running.
- The ngrok tunnel used for Jira-triggered execution is temporary. Free-tier ngrok URLs expire and change on restart, requiring the Jira Automation rule's URL to be updated periodically. In a production environment, Jenkins would instead run on a permanently accessible server, removing this constraint entirely.
- Test execution depends on reaching a live, public website (saucedemo.com). Any loss of internet connectivity on the host machine will cause test execution to fail, independent of the framework or pipeline configuration.
- Zephyr Scale, used for test management, operates on a free trial rather than a permanently free tier.

## Possible Future Improvements

- Expand automated coverage beyond the login flow (for example, cart and checkout functionality).
- Host Jenkins on a persistent, internet-accessible server to remove the ngrok dependency.
- Point test execution at a locally hosted version of the application under test, reducing dependency on external network reliability.
- Scope the Jira manual trigger specifically to Test Cycle issues rather than all issue types.
- Introduce parallel test execution to reduce overall pipeline duration as test coverage grows.
