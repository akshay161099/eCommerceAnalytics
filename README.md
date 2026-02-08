# Real-Time Analytics Platform

## Overview
A lightweight analytics service built with Spring Boot, Redis, and React. It ingests high-frequency user events, calculates rolling metrics in real-time, and displays them on a dashboard.

graph TD
    %% Actors
    Client[User Browser]
    Generator[Mock Data Generator]

    %% Components
    subgraph Docker_Compose [Docker Compose Environment]
        
        subgraph Frontend_Container [Frontend (React + Vite)]
            Dashboard[Dashboard UI]
        end

        subgraph Backend_Container [Backend (Spring Boot)]
            API[API Controller]
            Limiter[Rate Limiter (Token Bucket)]
            Service[Analytics Service]
        end

        subgraph Data_Store [Data Layer]
            Redis[(Redis)]
        end
    end

    %% Flows
    Generator -- "1. POST /events (JSON)" --> API
    Client -- "View Dashboard" --> Dashboard
    
    %% Backend Logic
    API -- "2. Check Limit" --> Limiter
    Limiter -- "Allowed" --> Service
    Limiter -- "Blocked" --> API
    
    %% Redis Interactions
    Service -- "3. Write (ZADD)" --> Redis
    Service -- "4. Read Aggregations (ZCOUNT/ZRANGE)" --> Redis
    
    %% Frontend Polling
    Dashboard -- "5. GET /dashboard (Poll 30s)" --> API

## Tech Stack
- **Backend**: Java 17, Spring Boot 3
- **Database**: Redis (Sorted Sets for time-series windows)
- **Frontend**: React (Vite)
- **Infrastructure**: Docker Compose

## Core Features
1. **Event Ingestion**: Handles JSON events with rate limiting (100 req/sec).
2. **Real-Time Metrics**:
   - Active Users (Rolling 5 min window)
   - Top Pages (Rolling 15 min window)
   - Active Sessions per User
3. **Mock Generator**: Built-in utility generating traffic for demo purposes.

## Setup Instructions

### Prerequisites
- Docker & Docker Compose installed.

### Running the App
1. Clone the repository.
2. Navigate to the root folder.
3. Run the following command:
   ```bash
   docker-compose up --build
