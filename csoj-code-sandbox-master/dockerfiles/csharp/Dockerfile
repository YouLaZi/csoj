# Use an official .NET SDK image as a parent image
FROM mcr.microsoft.com/dotnet/sdk:6.0

# Set the working directory in the container
WORKDIR /app

# Copy the current directory contents into the container at /app
# This step will be handled by volume mounting in DockerService,
# but a placeholder COPY can be useful for standalone Dockerfile testing.
# COPY . /app

# This Dockerfile is generic; actual compilation (e.g., csc or dotnet build) 
# and execution commands (e.g., dotnet run or directly running the .exe)
# are sent by DockerService. This file primarily defines the environment.
# CMD ["/bin/sh"]