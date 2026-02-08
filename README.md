# Real-Time Analytics Platform

## Overview
A lightweight analytics service built with Spring Boot, Redis, and React. It ingests high-frequency user events, calculates rolling metrics in real-time, and displays them on a dashboard.

graph TD
    %% Actors
    User([User / Browser])
    Gen([Mock Data Generator])

    %% Docker Container Context
    subgraph Docker_Environment [Docker Compose Environment]
        
        %% Frontend Service
        subgraph Frontend_Container [Frontend Service]
            React[React App]
        end

        %% Backend Service
        subgraph Backend_Container [Backend Service]
            Controller[API Controller]
            Limiter{Rate Limiter}
            Service[Analytics Service]
        end

        %% Database Service
        subgraph Data_Store [Redis Service]
            Redis[(Redis Cache)]
        end
    end

    %% Data Flow - Ingestion
    Gen -- "1. POST /api/events (JSON)" --> Controller
    Controller -- "2. Check Limit (AtomicInt)" --> Limiter
    Limiter -- "Allowed" --> Service
    Limiter -. "Blocked" .-x Controller
    
    Service -- "3. Write: ZADD (Timestamp Score)" --> Redis
    
    %% Data Flow - Dashboard
    User -- "4. View Dashboard" --> React
    React -- "5. Poll: GET /api/dashboard (30s)" --> Controller
    Controller -- "6. Read: ZCOUNT / ZRANGE" --> Redis
    Redis -- "7. Return Aggregated Stats" --> Controller
    Controller -- "8. Return JSON" --> React

    %% Styling
    style Redis fill:#ff7b7b,stroke:#333,stroke-width:2px
    style React fill:#61dafb,stroke:#333,stroke-width:2px,color:black
    style Backend_Container fill:#e8f5e9,stroke:#333
   

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
