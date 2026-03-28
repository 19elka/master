#!/bin/bash
set -e

echo "========================================="
echo "Building ALL services"
echo "========================================="

echo ""
echo "Building service-a..."
./gradlew :service-a:clean :service-a:bootJar
docker build -f service-a/Dockerfile -t service-a:latest .

echo ""
echo "Building service-b..."
./gradlew :service-b:clean :service-b:bootJar
docker build -f service-b/Dockerfile -t service-b:latest .

echo ""
echo "Building book-service..."
./gradlew :service-book:clean :service-book:bootJar
docker build -f service-book/Dockerfile -t book-service:1.0 .

echo ""
echo "========================================="
echo "ALL services built!"
echo "========================================="
echo ""
echo "Load images to Minikube:"
echo "  minikube image load service-a:latest"
echo "  minikube image load service-b:latest"
echo "  minikube image load book-service:1.0"