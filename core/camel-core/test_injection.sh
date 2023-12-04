JAVA_HOME="/opt/homebrew/opt/openjdk@17" mvn surefire:test -Dtest=PropertiesComponentNestedFalseTest#testNestedFalse -Dconfig.inject.cli="cool.other.name=Cheese2"
