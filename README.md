# Quotify

> A small, production-oriented Android app demonstrating **Clean Architecture**, **Modularization**, **Jetpack Compose**, and **offline-first** patterns.  
> Ideal portfolio piece for mid → senior Android developer interviews.

[![Kotlin](https://img.shields.io/badge/Kotlin-7f52ff?style=flat&logo=kotlin&logoColor=white)]()
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-000000?style=flat&logo=jetpackcompose&logoColor=white)]()
[![Hilt](https://img.shields.io/badge/Hilt-DABFFF?style=flat)]()
[![License](https://img.shields.io/badge/License-MIT-blue.svg)]()

---

## 🔎 Project Summary

**Quotify** fetches inspirational quotes from the public Quotable API, lets users browse, favorite, share, and view quote details. The project focuses on architecture, testing, and best practices rather than many features.

**Goals**
- Demonstrate Clean Architecture and multi-module structure.
- Use Jetpack Compose and modern Android libraries.
- Show offline caching (Room) and proper DI (Hilt).
- Provide unit & UI tests and CI via GitHub Actions.

---

## ✨ Features (MVP)

- Home: Paginated list of quotes (pull-to-refresh, loading / error states)
- Detail: Large-typography quote view, share, copy, favorite
- Favorites: Local Room-backed list, swipe-to-delete
- Offline-first: Cache quotes in Room and fall back to cache when offline
- Light/Dark theme
- Simple, reusable UI components and theming

---

## 🛠 Tech Stack

- Kotlin, Coroutines & Flow  
- Jetpack Compose (Material 3)  
- Hilt for DI  
- Retrofit + OkHttp for networking  
- Room for local persistence  
- Paging 3 (optional)  
- Jetpack Navigation (Compose)  
- JUnit, MockK, Turbine for tests  
- GitHub Actions for CI

---

## 📁 Project Structure (planned)

/app (Android app module)
/core
/core-ui
/core-model
/core-network
/core-database
/feature-home
/feature-detail
/feature-favorites


---

## 🧭 Architecture Diagram

Paste the following Mermaid diagram into your README (GitHub renders Mermaid):

```mermaid
flowchart LR
  subgraph Network
    API[Quotable API]
  end

  subgraph AppLayer
    UI[Compose UI] --> VM[ViewModel]
    VM --> UseCase[UseCases]
    UseCase --> Repo[QuoteRepository]
    Repo -->|remote| RemoteDataSource
    Repo -->|local| LocalDataSource
    RemoteDataSource --> API
    LocalDataSource --> DB[(Room Database)]
  end

  DB -->|entities| LocalDataSource
  RemoteDataSource -->|dto| Repo
  LocalDataSource -->|domain models| Repo

  style API fill:#f9f,stroke:#333,stroke-width:1px
  style DB fill:#fffbcc,stroke:#333,stroke-width:1px
