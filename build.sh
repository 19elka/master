set -e  # Остановка при ошибке
echo "Building book-service:1.0..."
docker build --no-cache -t book-service:1.0 .
echo "Build complete! $(docker images book-service:1.0 | awk 'NR==2 {print $7/1024/1024 "MB"}')"
