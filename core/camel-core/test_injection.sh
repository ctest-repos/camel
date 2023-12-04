(cd ../camel-base/ && mvn clean install -DskipTests)
mvn test -Dtest=PropertiesComponentNestedFalseTest#testNestedFalse -Dconfig.inject.cli="cool.other.name=Cheese2"
