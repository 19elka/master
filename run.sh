set -e
echo "Stopping old container..."
docker stop book-app 2>/dev/null || true
docker rm book-app 2>/dev/null || true

echo "Starting book-app (profile: docker)..."
docker run -d --name book-app \
  --restart unless-stopped \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_LIQUIBASE_ENABLED=false \
  book-service:1.0

echo "Ready!"
echo "Swagger: http://localhost:8080/swagger-ui.html"
echo "Health:  http://localhost:8080/actuator/health"
echo "Logs:    docker logs -f book-app"
