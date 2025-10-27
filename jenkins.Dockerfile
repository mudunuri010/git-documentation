# Use the official Jenkins LTS image with Java 17
FROM jenkins/jenkins:2.532-jdk17

# Switch to root user to install Docker CLI
USER root

# Install Docker CLI
RUN apt-get update && \
    apt-get install -y ca-certificates curl && \
    install -m 0755 -d /etc/apt/keyrings && \
    curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc && \
    chmod a+r /etc/apt/keyrings/docker.asc && \
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null && \
    apt-get update && \
    apt-get install -y docker-ce-cli

# Add jenkins user to the docker group
RUN groupadd -g 999 docker && usermod -aG docker jenkins

# Copy the plugins list
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt

# Install all plugins from the list
RUN jenkins-plugin-cli -f /usr/share/jenkins/ref/plugins.txt

# Create directories
RUN mkdir -p /var/jenkins_home/init.groovy.d && \
    mkdir -p /var/jenkins_home/scripts && \
    chown -R jenkins:jenkins /var/jenkins_home

# ✅ Copy init groovy script AND scripts
COPY jenkins_home/init.groovy.d/create-job.groovy /usr/share/jenkins/ref/init.groovy.d/create-job.groovy
COPY scripts/ /var/jenkins_home/scripts/

# Make scripts executable and set permissions
RUN chmod +x /var/jenkins_home/scripts/*.sh && \
    chown -R jenkins:jenkins /var/jenkins_home/scripts /usr/share/jenkins/ref/init.groovy.d

# Switch back to jenkins user
USER jenkins