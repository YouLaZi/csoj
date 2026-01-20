# Use an official OpenJDK runtime as a parent image
FROM openjdk:8-jdk-alpine

# Set the working directory in the container
WORKDIR /app

# Copy the current directory contents into the container at /app
# This step will be handled by volume mounting in DockerService, 
# but a placeholder COPY can be useful for standalone Dockerfile testing.
# COPY . /app

# Compile the Java code (assuming Main.java is in /app)
# This Dockerfile is generic; actual compilation and execution commands
# are sent by DockerService. This file primarily defines the environment.
# CMD ["/bin/sh"]