FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY lib/ lib/
COPY src/java/ src/java/
COPY web/ web/
RUN mkdir -p /out/WEB-INF/classes /out/WEB-INF/lib \
    && cp -R web/. /out/ \
    && rm -f /out/META-INF/context.xml \
    && find src/java -name '*.java' > sources.txt \
    && javac -encoding UTF-8 --release 21 -cp 'lib/*' -d /out/WEB-INF/classes @sources.txt \
    && find lib -name '*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' -exec cp {} /out/WEB-INF/lib/ \;

FROM tomcat:11.0-jre21-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /out/ /usr/local/tomcat/webapps/ROOT/
EXPOSE 8080
CMD ["catalina.sh", "run"]
