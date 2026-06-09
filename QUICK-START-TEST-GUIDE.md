# Quick Start Guide - CVE-2025-48924 Testing

## Overview

This guide provides step-by-step instructions to quickly test the critical components of the commons-lang3 3.18.0 upgrade in pentaho-kettle.

**Estimated Time**: 15-20 minutes

---

## Option 1: Automated Quick Test (Recommended)

### Step 1: Make Test Script Executable
```bash
cd /Users/ramaizmansoor/Developer/pentaho-kettle
chmod +x run-cve-2025-48924-tests.sh
```

### Step 2: Run Critical Tests Only
```bash
./run-cve-2025-48924-tests.sh critical
```

**What This Does**:
- ✅ Verifies commons-lang3 3.18.0 dependency
- ✅ Checks for old commons-lang 2.6 (ensures it's gone)
- ✅ Builds core, engine, and ui modules
- ✅ Runs all 17 unit tests from ClassUtilsUpgradeTest
- ✅ Validates CVE-2025-48924 specific fixes (StackOverflowError prevention)

**Expected Output**:
```
========================================
CVE-2025-48924 Commons-Lang3 Upgrade Test Suite
========================================
[Phase 1: Dependency Verification]
✓ commons-lang3:3.18.0 found
✓ No old commons-lang 2.x found

[Phase 2: Build Verification]
✓ Core module compiled successfully
✓ Engine module compiled successfully
✓ UI module compiled successfully

[Phase 3: Unit Tests Execution]
✓ ClassUtilsUpgradeTest: 17/17 PASSED

[Phase 4: CVE-2025-48924 Specific Validation]
✓ CVE-specific test cases present and configured
✓ ClassUtils compilation verified

========================================
TEST EXECUTION SUMMARY
========================================
Total Tests: 10
Passed: 10
Failed: 0
Success Rate: 100.00%

OVERALL STATUS: ✓ ALL TESTS PASSED
```

**Test Results Location**: 
- Main Report: `test-results-YYYYMMDD_HHMMSS/test-report.txt`
- Detailed Logs: `test-results-YYYYMMDD_HHMMSS/`

---

## Option 2: Manual Quick Test (Step by Step)

### Step 1: Verify Dependencies (2 minutes)

```bash
cd /Users/ramaizmansoor/Developer/pentaho-kettle

# Check for commons-lang3 3.18.0
mvn dependency:tree | grep "commons-lang3"

# Expected: org.apache.commons:commons-lang3:jar:3.18.0:compile

# Verify NO old commons-lang 2.6
mvn dependency:tree | grep "commons-lang:commons-lang:2"

# Expected: (empty - nothing should match)
```

**✅ Pass Criteria**: 
- commons-lang3 3.18.0 found
- No commons-lang 2.x found

---

### Step 2: Build Modules (5 minutes)

```bash
# Build core module
mvn clean compile -DskipTests -pl core

# Expected: BUILD SUCCESS

# Build engine module  
mvn clean compile -DskipTests -pl engine

# Expected: BUILD SUCCESS

# Build ui module
mvn clean compile -DskipTests -pl ui

# Expected: BUILD SUCCESS
```

**✅ Pass Criteria**: All three modules compile without errors

---

### Step 3: Run Critical Unit Tests (8 minutes)

```bash
# Run ClassUtilsUpgradeTest (the main CVE test suite)
mvn test -pl core -Dtest=ClassUtilsUpgradeTest

# Watch for:
# [INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
```

**Critical Tests Include**:
- `testLongClassNameNoStackOverflow` - Verifies 50,000+ char names don't crash
- `testDeeplyNestedClassNameNoStackOverflow` - Verifies 1,000+ nested names don't crash
- `testGetAllInterfaces*` - Validates proxy creation in repository/Kerberos
- `testBackwardCompatibility` - Ensures old code still works

**✅ Pass Criteria**: 17/17 tests pass with 0 failures and 0 errors

---

### Step 4: Verify API Changes (3 minutes)

```bash
# Check HTML escaping fix
grep -n "escapeHtml4" core/src/main/java/org/pentaho/di/core/Const.java

# Expected: Multiple matches (lines 3985, 3999)

# Check StringBuilder replacement
grep -n "StringBuilder" core/src/main/java/org/pentaho/di/core/Const.java

# Expected: Multiple matches showing StrBuilder replaced

# Check ExceptionUtils fix
grep -n "getStackTrace" engine/src/main/java/org/pentaho/di/www/BodyHttpServlet.java

# Expected: Match on line 68
```

**✅ Pass Criteria**: All API changes verified

---

## Option 3: Minimal Quick Test (10 minutes)

**For fastest validation**, run just these 3 commands:

```bash
# 1. Check dependencies (30 seconds)
mvn dependency:tree | grep -E "commons-lang3:3.18.0|commons-lang:commons-lang:2"

# Expected: commons-lang3:3.18.0 found, NO commons-lang 2.x

# 2. Build core (3 minutes)
mvn -q clean compile -DskipTests -pl core && echo "✓ CORE COMPILE SUCCESS"

# 3. Run CVE tests (6 minutes)
mvn test -pl core -Dtest=ClassUtilsUpgradeTest 2>&1 | grep -E "Tests run|Failures|Errors|BUILD"

# Expected: Tests run: 17, Failures: 0, Errors: 0
```

---

## Test Results Interpretation

### All Tests Pass ✅
```
Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```
**Action**: Ready for Spoon application testing
→ Proceed to: **Manual Spoon Testing** (see below)

### Some Tests Fail ❌
```
Tests run: 17, Failures: 2, Errors: 0, Skipped: 0
BUILD FAILURE
```
**Action**: Review failures
```bash
# View detailed failure output
cat target/surefire-reports/org.pentaho.di.core.ClassUtilsUpgradeTest.txt
```

### Compilation Error ❌
```
[ERROR] COMPILATION ERROR:
[ERROR] ... cannot find symbol ... method escapeHtml4
```
**Action**: Verify API changes in Const.java (see Option 3, Step 4)

---

## Manual Spoon Testing (After Unit Tests Pass)

Once all critical tests pass, verify Spoon application functionality:

### Test 1: Launch Spoon (2 minutes)
```bash
# macOS/Linux
./spoon.sh

# Windows
spoon.bat
```

**Verify**:
- ✅ Application launches without errors
- ✅ No StackOverflowError in console
- ✅ UI displays normally
- ✅ Check logs: `~/.pentaho/spoon/logs/spoon.log`
  ```bash
  tail -f ~/.pentaho/spoon/logs/spoon.log | grep -i "error\|exception\|commons"
  ```

### Test 2: Repository Connection (5 minutes)
**Location**: File → Connect to Repository

**Verify**:
- ✅ Connection dialog appears
- ✅ Connection succeeds without errors
- ✅ Repository objects display correctly
- ✅ ClassUtils.getAllInterfaces() works (uses proxy creation)

### Test 3: Execute Transformation (5 minutes)
**Create Simple Transformation**:
1. File → New → Transformation
2. Add input step (e.g., Generate Rows)
3. Add output step (e.g., Dummy)
4. Connect steps
5. Run transformation

**Verify**:
- ✅ Transformation runs without errors
- ✅ Data processes correctly
- ✅ StringUtils methods work
- ✅ No commons-lang related errors

---

## Troubleshooting

### Issue: "commons-lang 2.6 found in dependencies"
```bash
# Solution: Rebuild clean
mvn clean install -DskipTests

# Verify exclusions in pom.xml
grep -A 5 "simple-jndi" core/pom.xml
```

### Issue: "Cannot find symbol: method escapeHtml4"
```bash
# Solution: Verify Const.java was updated
grep "escapeHtml4" core/src/main/java/org/pentaho/di/core/Const.java

# If not found, file wasn't updated properly - check git status
git status core/src/main/java/org/pentaho/di/core/Const.java
```

### Issue: Tests run but "Failures: X"
```bash
# View detailed test output
mvn test -pl core -Dtest=ClassUtilsUpgradeTest -e 2>&1 | tail -100

# Check specific test failure
cat core/target/surefire-reports/org.pentaho.di.core.ClassUtilsUpgradeTest.txt
```

### Issue: Build fails with compilation errors
```bash
# Full rebuild with dependency resolution
mvn clean dependency:resolve compile -DskipTests -pl core

# Check for import issues
grep "import org.apache.commons.lang\." core/src/main/java/**/*.java

# Should show: org.apache.commons.lang3 (NOT org.apache.commons.lang)
```

---

## Success Checklist

After running quick tests, verify:

- [ ] Automated script completed without failures (or manual tests all passed)
- [ ] Dependency verification: commons-lang3 3.18.0 only
- [ ] Core/Engine/UI modules compile cleanly
- [ ] ClassUtilsUpgradeTest: 17/17 tests pass
- [ ] CVE-specific tests pass (no StackOverflowError)
- [ ] API changes verified (escapeHtml4, StringBuilder, getStackTrace)
- [ ] Spoon launches without errors
- [ ] Repository connections work
- [ ] Transformations execute successfully

**✅ If all boxes checked**: **UPGRADE VALIDATED AND READY FOR DEPLOYMENT**

---

## Next Steps

### After Quick Tests Pass:
1. ✅ Run full test suite: `./run-cve-2025-48924-tests.sh all`
2. ✅ Review full test report
3. ✅ Run performance benchmarks (if available)
4. ✅ Execute full Spoon test plan (see SPOON-TEST-PLAN-CVE-2025-48924.md)
5. ✅ Deploy to staging/production

### For Full Testing:
- See: [SPOON-TEST-PLAN-CVE-2025-48924.md](SPOON-TEST-PLAN-CVE-2025-48924.md)
- See: [CVE-2025-48924-REMEDIATION-COMPLETE.md](CVE-2025-48924-REMEDIATION-COMPLETE.md)

---

## Command Reference

### All Test Phases
```bash
./run-cve-2025-48924-tests.sh all       # Complete test suite
./run-cve-2025-48924-tests.sh critical  # Critical tests only (recommended)
./run-cve-2025-48924-tests.sh build     # Build verification only
./run-cve-2025-48924-tests.sh unit      # Unit tests only
```

### Manual Commands
```bash
# Check dependency
mvn dependency:tree | grep commons-lang

# Build modules
mvn clean compile -DskipTests -pl core,engine,ui

# Run specific tests
mvn test -pl core -Dtest=ClassUtilsUpgradeTest
mvn test -pl core -Dtest=ConstTest

# View logs
tail -f ~/.pentaho/spoon/logs/spoon.log
```

---

**Document**: Quick Start Guide for CVE-2025-48924 Testing  
**Created**: 2026-06-09  
**Update**: After running tests, document results and outcomes  
**Estimated Duration**: 15-20 minutes for critical tests
