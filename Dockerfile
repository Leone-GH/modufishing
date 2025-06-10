FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew build

CMD ["java", "-jar", "build/libs/app.jar"]
