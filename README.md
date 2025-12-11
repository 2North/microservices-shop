# Microservices Shop

Микросервисное приложение интернет-магазина на Java Spring Boot для лабораторной работы.

## Архитектура

```
┌─────────────────────────────────────────────────────────────────┐
│                         Frontend (UI)                            │
│                        localhost:3000                            │
└─────────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API Gateway (:8080)                         │
│              JWT валидация, маршрутизация                        │
└─────────────────────────────────────────────────────────────────┘
                               │
        ┌──────────────────────┼──────────────────────┐
        │                      │                      │
        ▼                      ▼                      ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│ User Service  │    │Product Service│    │ Order Service │
│    (:8081)    │    │    (:8082)    │    │    (:8083)    │
│               │    │               │    │               │
│  PostgreSQL   │    │  PostgreSQL   │    │  PostgreSQL   │
│   (userdb)    │    │  (productdb)  │    │   (orderdb)   │
└───────────────┘    └───────────────┘    └───────────────┘
                                                  │
                            ┌─────────────────────┴─────────────────────┐
                            │                                           │
                            ▼                                           ▼
                  ┌───────────────┐                          ┌───────────────┐
                  │   Inventory   │                          │ Notification  │
                  │   Service     │                          │   Service     │
                  │    (:8084)    │                          │    (:8085)    │
                  │               │                          │               │
                  │  PostgreSQL   │                          │  PostgreSQL   │
                  │ (inventorydb) │                          │(notificationdb)│
                  └───────────────┘                          └───────────────┘
```

## Микросервисы

| Сервис | Порт | Описание |
|--------|------|----------|
| API Gateway | 8080 | Маршрутизация, JWT авторизация |
| User Service | 8081 | Регистрация, логин, управление пользователями |
| Product Service | 8082 | Каталог товаров |
| Order Service | 8083 | Управление заказами |
| Inventory Service | 8084 | Управление складом |
| Notification Service | 8085 | Уведомления |
| Frontend | 3000 | Web UI |

## Технологии

- **Java 17**
- **Spring Boot 3.2**
- **Spring Cloud Gateway**
- **Spring Data JPA**
- **PostgreSQL**
- **JWT (JSON Web Tokens)**
- **Docker & Docker Compose**
- **Gradle (multi-module)**

## Запуск

### Docker Compose (рекомендуется)

```bash
# Сборка и запуск всех сервисов
docker-compose up --build

# Или в фоновом режиме
docker-compose up -d --build

# Просмотр логов
docker-compose logs -f

# Остановка
docker-compose down

# Остановка с удалением volumes
docker-compose down -v
```

### Локальный запуск (для разработки)

1. Запустите PostgreSQL для каждого сервиса или используйте H2 in-memory (измените application.yml)

2. Соберите проект:
```bash
./gradlew build
```

3. Запустите каждый сервис:
```bash
./gradlew :user-service:bootRun
./gradlew :product-service:bootRun
./gradlew :inventory-service:bootRun
./gradlew :notification-service:bootRun
./gradlew :order-service:bootRun
./gradlew :api-gateway:bootRun
```

## API Endpoints

### User Service
- `POST /api/users/register` - Регистрация
- `POST /api/users/login` - Логин
- `GET /api/users/me` - Текущий пользователь (требует токен)
- `GET /api/users/{id}` - Получить пользователя

### Product Service
- `GET /api/products` - Все товары
- `GET /api/products/{id}` - Товар по ID
- `GET /api/products/category/{category}` - Товары по категории
- `GET /api/products/search?q=query` - Поиск товаров
- `POST /api/products` - Создать товар
- `PUT /api/products/{id}` - Обновить товар
- `DELETE /api/products/{id}` - Удалить товар

### Order Service
- `POST /api/orders` - Создать заказ (требует токен)
- `GET /api/orders/my` - Мои заказы (требует токен)
- `GET /api/orders/{id}` - Заказ по ID
- `PATCH /api/orders/{id}/status?status=STATUS` - Изменить статус
- `POST /api/orders/{id}/cancel` - Отменить заказ

### Inventory Service
- `GET /api/inventory` - Все остатки
- `GET /api/inventory/product/{productId}` - Остаток по товару
- `GET /api/inventory/check/{productId}?quantity=N` - Проверить наличие
- `POST /api/inventory/add` - Добавить на склад
- `POST /api/inventory/reserve` - Зарезервировать
- `POST /api/inventory/confirm` - Подтвердить резерв
- `POST /api/inventory/release` - Отменить резерв

### Notification Service
- `GET /api/notifications` - Мои уведомления (требует токен)
- `GET /api/notifications/unread` - Непрочитанные
- `GET /api/notifications/unread/count` - Счётчик непрочитанных
- `PATCH /api/notifications/{id}/read` - Пометить прочитанным
- `POST /api/notifications/read-all` - Прочитать все

## Тестирование с Bruno

1. Откройте Bruno
2. Import Collection: `bruno-collection/`
3. Выберите Environment: `Local`
4. Выполните запросы:
   - Register User → сохраняет токен
   - Login User → сохраняет токен
   - Остальные запросы используют токен автоматически

## Взаимодействие сервисов

1. **Создание заказа:**
   - Order Service проверяет наличие через Inventory Service
   - Получает информацию о товарах из Product Service
   - Резервирует товары в Inventory Service
   - Отправляет уведомление через Notification Service

2. **Подтверждение заказа:**
   - Order Service подтверждает резерв в Inventory Service
   - Отправляет уведомление об изменении статуса

3. **Отмена заказа:**
   - Order Service освобождает резерв в Inventory Service
   - Отправляет уведомление об отмене

## Тестовые данные

При запуске автоматически создаются:
- 6 товаров (Electronics, Footwear, Clothing)
- Остатки на складе для всех товаров

## Авторизация

Используется JWT токен:
- Получите токен при регистрации/логине
- Передавайте в заголовке: `Authorization: Bearer <token>`
- Открытые эндпоинты: `/api/users/register`, `/api/users/login`, `/api/products/**`

## Полезные команды

```bash
# Просмотр логов конкретного сервиса
docker-compose logs -f order-service

# Пересборка одного сервиса
docker-compose up -d --build order-service

# Подключение к базе данных
docker exec -it microservices-shop-postgres-product-1 psql -U postgres -d productdb

# Проверка здоровья сервисов
curl http://localhost:8080/actuator/health
```

## Структура проекта

```
microservices-shop/
├── api-gateway/
├── user-service/
├── product-service/
├── order-service/
├── inventory-service/
├── notification-service/
├── frontend/
├── bruno-collection/
├── docker-compose.yml
├── Dockerfile
├── build.gradle
└── settings.gradle
```
