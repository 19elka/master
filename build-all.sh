#!/bin/bash
set -e

echo "========================================="
echo "Building ALL services"
echo "========================================="

echo ""
echo "Building service-a..."
cd service-a
./gradlew clean bootJar
docker build -t service-a:latest .
cd ..

echo ""
echo "Building service-b..."
cd service-b
./gradlew clean bootJar
docker build -t service-b:latest .
cd ..

echo ""
echo "Building book-service..."
cd book-service
./gradlew clean bootJar
docker build -t book-service:1.0 .
cd ..

echo ""
echo "========================================="
echo "ALL services built!"
echo "========================================="
echo ""
echo "Load images to Minikube:"
echo "  minikube image load service-a:latest"
echo "  minikube image load service-b:latest"
echo "  minikube image load book-service:1.0"