# Use an official GCC image as a parent image
FROM gcc:latest

# Set the working directory in the container
WORKDIR /app

# Copy the current directory contents into the container at /app
# This step will be handled by volume mounting in DockerService,
# but a placeholder COPY can be useful for standalone Dockerfile testing.
# COPY . /app

# This Dockerfile is generic; actual compilation (e.g., g++) and execution commands
# are sent by DockerService. This file primarily defines the environment.
# CMD ["/bin/sh"]