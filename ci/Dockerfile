FROM eclipse-temurin:21-jdk@sha256:762d16be48b976c97801d198e5085b1f6facb13718a1e8fb67cb25d0b25337e1

ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=$ANDROID_HOME
ENV PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH

RUN apt-get update && apt-get install -y --no-install-recommends \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p $ANDROID_HOME/cmdline-tools \
    && wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline-tools.zip \
    && unzip -q cmdline-tools.zip -d $ANDROID_HOME/cmdline-tools/ \
    && mv $ANDROID_HOME/cmdline-tools/cmdline-tools $ANDROID_HOME/cmdline-tools/latest \
    && rm cmdline-tools.zip \
    && yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses > /dev/null 2>&1 \
    && $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager \
        "platform-tools" \
        "platforms;android-36" \
        "build-tools;36.0.0" \
        "cmdline-tools;latest"

RUN mkdir -p /workspace
WORKDIR /workspace

COPY gradlew /workspace/
COPY gradle/wrapper/gradle-wrapper.jar /workspace/gradle/wrapper/
COPY gradle/wrapper/gradle-wrapper.properties /workspace/gradle/wrapper/
COPY gradle/libs.versions.toml /workspace/gradle/
COPY settings.gradle.kts /workspace/
COPY build.gradle.kts /workspace/

RUN ./gradlew --version

COPY . /workspace/

RUN yes | sdkmanager --licenses > /dev/null 2>&1 || true

CMD ["./gradlew"]
