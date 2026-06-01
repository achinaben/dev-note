FROM eclipse-temurin:17-jre-alpine
ARG MODULE
COPY ${MODULE}/target/${MODULE}-*.jar /app/app.jar
WORKDIR /app
ENV JAVA_OPTS="-Xmx256m -Xms128m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
