FROM adoptopenjdk:8-jre-hotspot
CMD bin/start.sh -p $CONSOLE_PORT
ENV CONSOLE_PORT=8899
ADD target/elastic-job-lite-console-*.tar.gz /
RUN mv $(ls -d /elastic-job-lite-console-*) /elastic-job-lite-console
WORKDIR /elastic-job-lite-console
