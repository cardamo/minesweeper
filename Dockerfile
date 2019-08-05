FROM hseeberger/scala-sbt:11.0.3_1.2.8_2.13.0 AS builder
WORKDIR /build
COPY project/ ./project
COPY build.sbt ./
RUN sbt update
COPY conf conf
COPY app app
RUN sbt stage
RUN mkdir ./target/tmp && mv ./target/universal/stage/lib/minesweeper*.jar ./target/tmp

FROM openjdk:11-jre-slim
EXPOSE 9000
CMD rm -f /app/RUNNING_PID && /app/bin/minesweeper
COPY --from=builder /build/target/universal/stage/lib/ /app/lib/
COPY --from=builder /build/target/universal/stage/conf/ /app/conf/
COPY --from=builder /build/target/universal/stage/bin/ /app/bin/
COPY --from=builder /build/target/tmp/*.jar /app/lib/
