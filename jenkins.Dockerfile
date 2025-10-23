# Use the official Jenkins LTS image with Java 17
FROM jenkins/jenkins:lts-jdk17

# Switch to root user to install Docker CLI
USER root

# Install Docker CLI (to control the host's Docker daemon)
# --- THIS IS THE UPDATED SECTION ---
RUN apt-get update && \
    apt-get install -y ca-certificates curl && \
    install -m 0755 -d /etc/apt/keyrings && \
    curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc && \
    chmod a+r /etc/apt/keyrings/docker.asc && \
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null && \
    apt-get update && \
    apt-get install -y docker-ce-cli
# --- END OF UPDATED SECTION ---

# Add jenkins user to the docker group (using host's GID, 999 is common)
# This GID might need to match your host's docker group GID
RUN groupadd -g 999 docker && usermod -aG docker jenkins

# Copy the plugins list
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt

# Install all plugins from the list
RUN jenkins-plugin-cli -f /usr/share/jenkins/ref/plugins.txt

# Switch back to the jenkins user
USER jenkins