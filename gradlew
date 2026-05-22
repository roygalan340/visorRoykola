
#!/usr/bin/env sh

# Definitions
DIRNAME=`dirname "$0"`
if [ -z "$DIRNAME" ]; then
  DIRNAME="."
fi
APP_BASE_NAME=`basename "$0"`
APP_HOME=`cd "$DIRNAME" >/dev/null; pwd`

# Target Gradle execution command
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

if [ ! -x "$JAVACMD" ] ; then
    die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
fi

# Execute Gradle
exec "$JAVACMD" -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
