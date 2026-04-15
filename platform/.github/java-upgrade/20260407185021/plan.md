# Upgrade Plan: platform (20260407185021)

- **Generated**: 2026-04-08 00:00:00
- **HEAD Branch**: N/A
- **HEAD Commit ID**: N/A

## Available Tools

**JDKs**
- JDK 17.0.12: C:\Program Files\Java\jdk-17\bin (baseline verification)
- JDK 25.0.1: C:\Users\admin\.jdks\openjdk-25.0.1\bin (target LTS verification)

**Build Tools**
- Maven CLI: C:\Users\admin\.maven\maven-3.9.14\bin\mvn.cmd (installed in Step 1)
- Maven Wrapper: not present

## Guidelines

- Upgrade Java runtime to the latest LTS version.
- Non-git workspace detected; all changes remain in working directory without VCS commit history.

## Options

- Working branch: appmod/java-upgrade-20260407185021
- Run tests before and after the upgrade: true

## Upgrade Goals

- Upgrade Java runtime and compiler target from Java 7 to Java 25 (latest LTS).

### Technology Stack

| Technology/Dependency | Current | Min Compatible | Why Incompatible |
| --------------------- | ------- | -------------- | ---------------- |
| Java | 1.7 | 25 | User requested latest LTS runtime upgrade. |
| Maven CLI | Not installed | 4.0+ | Build tool missing; cannot run compile/test verification. |
| maven-compiler-plugin | 3.8.0 | 3.14.0 | Older plugin may not support `release=25` reliably. |
| maven-surefire-plugin | 2.22.1 | 3.2.5 | Older surefire has weaker compatibility with modern JDKs. |
| maven-war-plugin | 3.2.2 | 3.4.0 | Plugin line is old; align to current Java ecosystem support. |
| junit ⚠️ EOL | 4.11 | 4.13.2 | 4.11 is outdated and has known issues fixed in later 4.x. |

### Derived Upgrades

- Upgrade `maven.compiler.source` and `maven.compiler.target` from `1.7` to `25` to satisfy target runtime/bytecode level.
- Add `maven.compiler.release=25` for explicit cross-compile behavior on modern JDK.
- Upgrade `maven-compiler-plugin` from `3.8.0` to `3.14.0` for Java 25 support.
- Upgrade `maven-surefire-plugin` from `2.22.1` to `3.2.5` for JDK 25 test runtime compatibility.
- Upgrade `maven-war-plugin` from `3.2.2` to `3.4.0` to reduce plugin-level compatibility risks.
- Upgrade JUnit from `4.11` to `4.13.2` to avoid legacy test framework defects during runtime upgrade.

## Upgrade Steps

- **Step 1: Setup Environment**
  - **Rationale**: Install missing build tool required to execute compile/test verification for the upgrade.
  - **Changes to Make**:
    - [ ] Install Maven (latest available, target Maven 4.0+ compatibility for Java 25)
    - [ ] Verify Java 25 executable path is available
  - **Verification**:
    - Command: `appmod-list-mavens` and `appmod-list-jdks`
    - Expected: Maven available in PATH and JDK 25 detected

- **Step 2: Setup Baseline**
  - **Rationale**: Capture pre-upgrade compile/test status for acceptance comparison.
  - **Changes to Make**:
    - [ ] Run `mvn clean test-compile` with current configuration
    - [ ] Run `mvn clean test` to record baseline pass rate
  - **Verification**:
    - Command: `mvn clean test-compile -q` then `mvn clean test -q`
    - JDK: C:\Program Files\Java\jdk-17\bin
    - Expected: Baseline build/test results documented in progress file

- **Step 3: Upgrade Project to Java 25**
  - **Rationale**: Apply all POM-level changes required for Java 25 compilation and test execution.
  - **Changes to Make**:
    - [ ] Set `maven.compiler.source`, `maven.compiler.target`, `maven.compiler.release` to `25`
    - [ ] Upgrade Maven plugins: compiler/surefire/war
    - [ ] Upgrade JUnit to `4.13.2`
    - [ ] Resolve any compile or test API issues from toolchain upgrade
  - **Verification**:
    - Command: `mvn clean test-compile -q`
    - JDK: C:\Users\admin\.jdks\openjdk-25.0.1\bin
    - Expected: Compilation success for main and test code

- **Step 4: Final Validation**
  - **Rationale**: Confirm all goals are met with full rebuild and 100% passing tests.
  - **Changes to Make**:
    - [ ] Re-validate all target versions in `pom.xml`
    - [ ] Run clean build under Java 25 and fix any remaining failures
    - [ ] Run full test suite until 100% pass
  - **Verification**:
    - Command: `mvn clean test -q`
    - JDK: C:\Users\admin\.jdks\openjdk-25.0.1\bin
    - Expected: Build success and 100% test pass rate

## Key Challenges

- **Legacy Java 7 baseline**
  - **Challenge**: Large jump from Java 7 to Java 25 can surface source/bytecode and plugin compatibility issues.
  - **Strategy**: Upgrade build plugins first in the same step as Java target change and fix compile errors immediately.
- **Missing Maven installation**
  - **Challenge**: No Maven executable is currently available, blocking verification commands.
  - **Strategy**: Install Maven in Step 1 and standardize subsequent verification on that installation.
- **Non-version-controlled workspace**
  - **Challenge**: No branch/commit rollback capability.
  - **Strategy**: Keep changes minimal, incremental, and document each step result in progress file.
