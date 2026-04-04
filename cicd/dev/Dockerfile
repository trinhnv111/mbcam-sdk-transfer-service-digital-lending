FROM k8sdev.mbbank.com.vn/common/openjdk:11.0.5-jre-slim
# Add a volume pointing to /tmp
VOLUME /tmp 

WORKDIR /app
# Add argument spring profile
ARG ENV_PROFILE
ENV ENV_PROFILE=$ENV_PROFILE 

COPY libs/crypto/* /lib/
RUN chmod +x /lib/*
RUN ldconfig

# Add the application's jar to the container
ADD /build/libs/mbcam-sdk-transfer-service-1.0.jar mbcam-sdk-transfer-service-1.0.jar

RUN addgroup --system --gid 10000 appadmin
RUN adduser --system --uid 10000 --group appadmin
RUN chown -R appadmin:appadmin /app

# Set the default user.
USER appadmin

# Run the jar file
ENTRYPOINT exec java $JVM_OPTS -Djava.security.egd=file:/dev/./urandom -jar mbcam-sdk-transfer-service-1.0.jar --spring.config.location=file:/deployment/config/application.properties