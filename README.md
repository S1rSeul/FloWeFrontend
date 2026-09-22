# Инструкция по поднятию docker образа backend у себя на ПК

1. Скачать [Docker](https://www.docker.com/products/docker-desktop/)
2. Клонировать docker-compose файл в корень проекта

# Первый запуск

## 1. Скачать свежий образ  
`docker compose -f docker-compose.backend.yml pull api`

## 2. Поднять backend + БД
`docker compose -f docker-compose.backend.yml up -d`

## 3. Проверить, что backend жив
`curl http://localhost:8080/actuator/health`

# Обновление backend после изменений
`docker compose -f docker-compose.backend.yml pull api`  
`docker compose -f docker-compose.backend.yml up -d`
