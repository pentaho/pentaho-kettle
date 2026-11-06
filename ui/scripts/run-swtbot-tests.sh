#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
UI_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
REPO_ROOT="$(cd "${UI_DIR}/.." && pwd)"

# Everything this script writes goes under WORK_DIR.
WORK_DIR="${SWTBOT_WORK_DIR:-${UI_DIR}/target}"
mkdir -p "${WORK_DIR}"
WORK_DIR="$(cd "${WORK_DIR}" && pwd)"
SWTBOT_CLASSES="${WORK_DIR}/swtbot-classes"
REPORTS_DIR="${WORK_DIR}/swtbot-reports"
STATUS_FILE="${REPORTS_DIR}/status.txt"
HOME_SANDBOX="${WORK_DIR}/swtbot-home"
M2_REPO="${M2_REPO:-${HOME}/.m2/repository}"

if [[ "$(uname -s)" == "Linux" && "$(uname -m)" != "x86_64" ]]; then
  echo "PDI ships Linux SWT for x86_64 only (libswt/linux/x86_64); this machine is $(uname -m)." >&2
  echo "Run on x86_64, or in a linux/amd64 container (see ui/scripts/swtbot-linux.Dockerfile)." >&2
  exit 1
fi

# Spoon under test. Precedence: PDI_ZIP, then PDI_INSTALL_DIR, then the zip built by assemblies/client.
if [[ -z "${PDI_ZIP:-}" && -n "${PDI_INSTALL_DIR:-}" ]]; then
  DI_DIR="${PDI_INSTALL_DIR}/data-integration"
else
  if [[ -z "${PDI_ZIP:-}" ]]; then
    PDI_ZIP="$(ls "${REPO_ROOT}"/assemblies/client/target/pdi-ce-*.zip 2>/dev/null | head -n 1 || true)"
  fi
  if [[ -z "${PDI_ZIP}" || ! -f "${PDI_ZIP}" ]]; then
    echo "PDI client zip not found. Build it (mvn install from the repository root, or -pl assemblies/client -am)," >&2
    echo "or set PDI_ZIP to a pdi-ce zip, or PDI_INSTALL_DIR to an extracted pdi-ce build." >&2
    exit 1
  fi
  PDI_ZIP="$(cd "$(dirname "${PDI_ZIP}")" && pwd)/$(basename "${PDI_ZIP}")"
  EXTRACT_DIR="${WORK_DIR}/swtbot-pdi"
  STAMP="${EXTRACT_DIR}/.extracted-from"
  if [[ ! -f "${STAMP}" || "${PDI_ZIP}" -nt "${STAMP}" || "$(cat "${STAMP}")" != "${PDI_ZIP}" ]]; then
    if ! command -v unzip > /dev/null; then
      echo "unzip is required to extract ${PDI_ZIP}." >&2
      exit 1
    fi
    echo "Extracting ${PDI_ZIP} to ${EXTRACT_DIR}"
    rm -rf "${EXTRACT_DIR}"
    mkdir -p "${EXTRACT_DIR}"
    unzip -q "${PDI_ZIP}" -d "${EXTRACT_DIR}"
    echo "${PDI_ZIP}" > "${STAMP}"
  fi
  DI_DIR="${EXTRACT_DIR}/data-integration"
fi

if [[ ! -d "${DI_DIR}" ]]; then
  echo "PDI distribution not found at ${DI_DIR}." >&2
  exit 1
fi

# Test classes: arguments, else SWTBOT_TESTS, else every class under src/swtbot/java with an @Test method.
TESTS=( "$@" )
if [[ ${#TESTS[@]} -eq 0 && -n "${SWTBOT_TESTS:-}" ]]; then
  read -r -a TESTS <<< "${SWTBOT_TESTS//,/ }"
fi
if [[ ${#TESTS[@]} -eq 0 ]]; then
  while IFS= read -r test_file; do
    TESTS+=( "$(echo "${test_file#./}" | sed 's|\.java$||; s|/|.|g')" )
  done < <(cd "${UI_DIR}/src/swtbot/java" && grep -rl '@Test' . | sort)
fi

unset JAVA_TOOL_OPTIONS JDK_JAVA_OPTIONS

# Test libraries come from the local Maven repository. SWTBot is not on Maven Central; the bootstrap script installs it.
SWTBOT_JAR="${M2_REPO}/org/eclipse/swtbot/org.eclipse.swtbot.swt.finder/4.2.0/org.eclipse.swtbot.swt.finder-4.2.0.jar"
JUNIT_JAR="${M2_REPO}/junit/junit/4.13.2/junit-4.13.2.jar"
HAMCREST_CORE_JAR="${M2_REPO}/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"
HAMCREST_LIBRARY_JAR="${M2_REPO}/org/hamcrest/hamcrest-library/1.3/hamcrest-library-1.3.jar"
if [[ ! -f "${SWTBOT_JAR}" ]]; then
  bash "${SCRIPT_DIR}/bootstrap-swtbot-repository.sh"
fi
if [[ ! -f "${JUNIT_JAR}" ]]; then
  mvn -q dependency:get -Dartifact=junit:junit:4.13.2
fi
if [[ ! -f "${HAMCREST_LIBRARY_JAR}" ]]; then
  mvn -q dependency:get -Dartifact=org.hamcrest:hamcrest-library:1.3
fi

JAVA="java"
JAVAC="javac"
if [[ -n "${JAVA_HOME:-}" ]]; then
  JAVA="${JAVA_HOME}/bin/java"
  JAVAC="${JAVA_HOME}/bin/javac"
fi

LAUNCH_PREFIX=()
JVM_OPTS=()
case "$(uname -s)" in
  Darwin)
    JVM_OPTS+=( -XstartOnFirstThread )
    case "$(uname -m)" in
      arm64) SWT_DIR="${DI_DIR}/libswt/osx64_aarch" ;;
      *) SWT_DIR="${DI_DIR}/libswt/osx64" ;;
    esac
    ;;
  Linux)
    SWT_DIR="${DI_DIR}/libswt/linux/x86_64"
    export SWT_GTK3=0 GDK_NATIVE_WINDOWS=1 LIBOVERLAY_SCROLLBAR=0 UBUNTU_MENUPROXY=0
    if [[ -z "${DISPLAY:-}" ]]; then
      if ! command -v xvfb-run > /dev/null; then
        echo "No DISPLAY and xvfb-run is not installed." >&2
        exit 1
      fi
      LAUNCH_PREFIX=( xvfb-run -a )
    fi
    ;;
  *)
    echo "Unsupported OS for this script: $(uname -s)" >&2
    exit 1
    ;;
esac

# Same JVM layout spoon.sh/launcher.jar uses: SWT for this platform, the distribution, then the SWTBot harness.
APP_CP="${SWT_DIR}/swt.jar:${DI_DIR}/classes:${DI_DIR}:${DI_DIR}/lib/*"
SWTBOT_CP="${SWTBOT_JAR}:${HAMCREST_CORE_JAR}:${HAMCREST_LIBRARY_JAR}:${JUNIT_JAR}"

rm -rf "${SWTBOT_CLASSES}" "${REPORTS_DIR}" "${HOME_SANDBOX}"
mkdir -p "${SWTBOT_CLASSES}" "${REPORTS_DIR}" "${HOME_SANDBOX}/user-home" "${HOME_SANDBOX}/kettle-home"
find "${UI_DIR}/src/swtbot/java" -name '*.java' > "${WORK_DIR}/swtbot-sources.txt"
"${JAVAC}" -nowarn -d "${SWTBOT_CLASSES}" -cp "${SWTBOT_CP}:${APP_CP}" @"${WORK_DIR}/swtbot-sources.txt"

# Fresh, separate home folders for every run so the user's ~/.kettle and ~/.pentaho are never read or changed.
# KETTLE_HOME and user.home are deliberately different folders so tests can check which one each setting uses.
unset PENTAHO_METASTORE_FOLDER
export KETTLE_HOME="${HOME_SANDBOX}/kettle-home"

JVM_OPTS+=(
  -Xms1024m -Xmx2048m
  -Duser.home="${HOME_SANDBOX}/user-home"
  -DKETTLE_HOME="${KETTLE_HOME}"
  -Djava.library.path="${SWT_DIR}"
  -Djava.locale.providers=COMPAT,SPI
  -Dcom.google.protobuf.use_unsafe_pre22_gencode=true
  -Dswtbot.status.file="${STATUS_FILE}"
  --add-opens=java.base/sun.net.www.protocol.jar=ALL-UNNAMED
  --add-opens=java.base/java.lang=ALL-UNNAMED
  --add-opens=java.base/java.io=ALL-UNNAMED
  --add-opens=java.base/java.lang.reflect=ALL-UNNAMED
  --add-opens=java.base/java.net=ALL-UNNAMED
  --add-opens=java.base/java.security=ALL-UNNAMED
  --add-opens=java.base/java.util=ALL-UNNAMED
  --add-opens=java.base/sun.net.www.protocol.file=ALL-UNNAMED
  --add-opens=java.base/sun.net.www.protocol.ftp=ALL-UNNAMED
  --add-opens=java.base/sun.net.www.protocol.http=ALL-UNNAMED
  --add-opens=java.base/sun.net.www.protocol.https=ALL-UNNAMED
  --add-opens=java.base/sun.reflect.misc=ALL-UNNAMED
  --add-opens=java.management/javax.management=ALL-UNNAMED
  --add-opens=java.management/javax.management.openmbean=ALL-UNNAMED
  --add-opens=java.naming/com.sun.jndi.ldap=ALL-UNNAMED
  --add-opens=java.base/java.math=ALL-UNNAMED
  --add-opens=java.base/sun.nio.ch=ALL-UNNAMED
  --add-opens=java.base/java.nio=ALL-UNNAMED
  --add-opens=java.security.jgss/sun.security.krb5=ALL-UNNAMED
)

# Spoon resolves plugins and ui/ resources relative to the data-integration directory.
cd "${DI_DIR}"
set +e
${LAUNCH_PREFIX[@]+"${LAUNCH_PREFIX[@]}"} "${JAVA}" "${JVM_OPTS[@]}" -cp "${SWTBOT_CLASSES}:${SWTBOT_CP}:${APP_CP}" \
  org.pentaho.di.ui.swtbot.SpoonSwtBotRunner "${TESTS[@]}"
EXIT_CODE=$?
set -e

if [[ ! -f "${STATUS_FILE}" ]]; then
  echo "Spoon exited before the SWTBot scenarios finished (exit code ${EXIT_CODE})." >&2
  exit 1
fi
cat "${STATUS_FILE}"
echo "Reports: ${REPORTS_DIR}"
exit "${EXIT_CODE}"
